package com.novelbio.analysis.seq.genome.gffOperate;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * Ӧ���Ǳ�׼��gff3��ʽ��������NCBI��gff3�ļ�
 * 
 * ���Gff�Ļ���������Ϣ,�������ʵ��������ʹ��<br/>
 * ����Gff�ļ��������������ϣ���һ��list��,
 * �ṹ���£�<br/>
 * 1.hash��ChrID��--ChrList--GffDetail(GffDetail��,ʵ����GffDetailUCSCgene����)<br/>
 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
 * 
 * 2.hash��LOCID��--GffDetail������LOCID�������Ļ����� <br/>
 * 
 * 3.list��LOCID��--LOCList����˳�򱣴�LOCID<br/>
 * 
 * ÿ�����������յ��CDS������յ㱣����GffDetailList����<br/>
 */
public class GffHashGeneNCBI extends GffHashGeneAbs{
	private static Logger logger = Logger.getLogger(GffHashGeneNCBI.class);
	
	/** �������ֵ����򣬿��Ըĳ�ʶ�������������,���������Ͻ棬Ĭ��  NCBI��ID  */
	protected static String regGeneName = "(?<=gene\\=)[\\w\\-%]+";
	/**  �ɱ����mRNA������Ĭ�� NCBI��ID */
	protected static String regSplitmRNA = "(?<=transcript_id\\=)[\\w,\\-]+";
	/**  �ɱ����mRNA�Ĳ��������Ĭ�� NCBI��symbol */
	protected static String regProduct = "(?<=product\\=)[\\w\\-%]+";
	/** geneID������ */
	protected static String regGeneID = "(?<=Dbxref\\=GeneID\\:)\\d+";
	/** ID������ */
	protected static String regID = "(?<=ID\\=)\\w+";
	/** parentID������ */
	protected static String regParentID = "(?<=Parent\\=)[\\w\\-%]+";
	/** mRNA������ */
	//TODO ������enum��map��ʵ��
	private static HashMap<String, GeneType> mapMRNA2GeneType = new HashMap<String, GeneType>();

	/** gene������ */
	private static HashSet<String> setIsGene = new HashSet<String>();
	
	public static void main(String[] args) {
//		GffHashGeneNCBI.modifyNCBIgffFile("/media/winE/Bioinformatics/genome/checken/gal4_UCSC/gff/ref_Gallus_gallus-4.0_top_level.gff3");
		GffHashGeneNCBI gffHashGeneNCBI = new GffHashGeneNCBI();
		gffHashGeneNCBI.ReadGffarray("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/ref_GRCh37.p9_top_level_modify.gff3");
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGeneNCBI.searchISO("XM_003481161");		
		System.out.println(gffGeneIsoInfo.getName());
		gffGeneIsoInfo = gffHashGeneNCBI.searchISO("IGKV");
		System.out.println(gffGeneIsoInfo.getName());
		System.out.println(gffGeneIsoInfo.getATGsite());
		System.out.println(gffGeneIsoInfo.getUAGsite());
		System.out.println(gffGeneIsoInfo.get(0).getStartCis());
		System.out.println(gffGeneIsoInfo.get(1).getEndCis());
	}
	
	/** "(?<=gene\\=)\\w+" */
	PatternOperate patGeneName = null;
	/**  "(?<=transcript_id\\=)\\w+" */
	PatternOperate patmRNAName = null;
	/** "(?<=Dbxref\\=GeneID\\:)\\d+" */
	PatternOperate patGeneID = null;
	/** "(?<=ID\\=)\\w+" */
	PatternOperate patID = null;
	/** "(?<=Parent\\=)\\w+" */
	PatternOperate patParentID = null;
	/** "(?<=product\\=)\\w+" */
	PatternOperate patProduct = null;

	private HashMap<String, String> hashRnaID2GeneID = new HashMap<String, String>();
	private LinkedHashMap<String, GffDetailGene> hashGenID2GffDetail = new LinkedHashMap<String, GffDetailGene>();
	private LinkedHashMap<String, GffGeneIsoInfo> hashRnaID2Iso = new LinkedHashMap<String, GffGeneIsoInfo>();
	/** 
	 * һ���ת¼�������ȳ���exon��Ȼ�����CDS������<br>
	 * hr3	RefSeq	mRNA	59958839	59959481<br>
	 * chr3	RefSeq	exon	59959427	59959481<br>
	 * chr3	RefSeq	exon	59958839	59959233<br>
	 * chr3	RefSeq	CDS	59959427	59959481<br>
	 * chr3	RefSeq	CDS	59958839	59959233<br>
	 *������Щת¼���������exon�����Ǻ�������CDS������<br>
	 * chr3	RefSeq	gene	59962472	59963232<br>
	 * chr3	RefSeq	V_gene_segment	59963181	59963232<br>
	 * chr3	RefSeq	V_gene_segment	59962472	59962797<br>
	 * chr3	RefSeq	CDS	59963181	59963229<br>
	 * chr3	RefSeq	CDS	59962472	59962797<br>
	 * ��ô��map��������¼��ת¼���Ƿ������exon�����������exon��CDS��ֻ�����趨ATG��UAG��
	 * ���û�г���exon��CDS��Ҫ��exon���趨��
	 */
	private HashMap<String, Boolean> mapGeneName2IsHaveExon = new HashMap<String, Boolean>();
	int numCopedIDsearch = 0;//����taxID�Ĵ������10��

	/**
	 * �趨mRNA��gene������������gff�ļ�������ֵ�
	 */
	private void setHashName() {
		if (mapMRNA2GeneType.isEmpty()) {
			mapMRNA2GeneType = GeneType.getMapMRNA2GeneType();
//			mapMRNA2GeneType.remove("tRNA");//tRNA����gene����
		}
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("pseudogene");
//			setIsGene.add("tRNA");
		}
	}
	private void setPattern() {
		patGeneName = new PatternOperate(regGeneName, false);
		patmRNAName = new PatternOperate(regSplitmRNA, false);
		patProduct = new PatternOperate(regProduct, false);
		
		patGeneID = new PatternOperate(regGeneID, false);
		patID = new PatternOperate(regID, false);
		patParentID = new PatternOperate(regParentID, false);
	}
	/**
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCknown gene<br>
	 * ����Gff�ļ��������������ϣ���һ��list��<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ��������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID,���ﲻ���Ƕ��ת¼����ÿһ��ת¼������һ��������LOCID <br>
     * <b>4. LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
   protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
	   setHashName();
	   setPattern();
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);
	   
	   //��ǰ��geneID����Ҫ�Ǹ�tRNA��miRNA�õģ���Ϊ���mRNA����parent geneID����ͨ�����ID����geneName
	   //����tRNA��miRNA��û�����parent geneID�����Ծͼ���������������
	   String[] thisGeneIDandName = null;
	   
	   for (String content : txtgff.readlines()) {
		   if(content.charAt(0)=='#') continue;
		   
		   if (content.contains("C10orf108")) {
			   System.out.println("stop");
		   }
		   
		   
		   String[] ss = content.split("\t");//����tab�ֿ�
		   //����ȡ��geneʱ�����Ƕ�����һ���µĻ�����ô������������㣬�յ��ÿ��CDS�ĳ��ȶ�����list������
		   if (setIsGene.contains(ss[2])) {
			   thisGeneIDandName = addNewGene(ss);
		   }
		   /**
      	    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
      	    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
      	    * һ��������mRNA����Ҫ��ʼָ��5UTR��3UTR��CDS��������ֹ
      	    */
		   else if (mapMRNA2GeneType.containsKey(ss[2])) {
			   addMRNA(thisGeneIDandName, ss);
		   }
		   else if (ss[2].contains("exon")) {
			   if (!addExon(ss)) {
				   continue;
			   }
		   }
		   else if (ss[2].equals("CDS")) {
			   addCDS(ss);
		   }
		   else
			   logger.error("����δ֪exon��" + ss[2]);
	   }
	   setGffList();
	   txtgff.close();
   }
   /** ����ȡ��geneʱ�����Ƕ�����һ���µĻ�����ô������������㣬�յ��ÿ��CDS�ĳ��ȶ�����list������
    * ���ҷ��ظ�geneID��geneName
    *    */
   private String[] addNewGene(String[] ss) {
	 //when read the # and the line contains gene, it means the new LOC
	   String geneID = patID.getPatFirst(ss[8]);
	   String geneName = getGeneName(ss[8]); setTaxID(ss, geneName);
	   GffDetailGene gffDetailLOC = getGffDetailGenID(geneID);
	   if (gffDetailLOC == null) {
		   gffDetailLOC=new GffDetailGene(ss[0], geneName, ss[6].equals("+"));//�½�һ��������
	   }
	   gffDetailLOC.setTaxID(taxID);
	   gffDetailLOC.setStartAbs( Integer.parseInt(ss[3])); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//������ֹ
	   hashGenID2GffDetail.put(geneID, gffDetailLOC);
	   mapGeneName2IsHaveExon.put(geneID, false);
	   String[] geneIDandName = new String[2];
	   geneIDandName[0] = geneID;
	   geneIDandName[1] = geneName;
	   return geneIDandName;
   }
   /**
    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
    * һ��������mRNA����Ҫ��ʼָ��5UTR��3UTR��CDS��������ֹ
    */
   private void addMRNA(String[] lastGeneIDandName, String[] ss) {
	   String rnaID = patID.getPatFirst(ss[8]);
	   String rnaName = add_MapRnaID2RnaName_And_MapRnaID2GeneID(lastGeneIDandName, rnaID, ss);
	   GffDetailGene gffDetailGene = getGffDetailRnaID(rnaID);
	   
	   String[] mRNAname = getMrnaName(rnaName, ss);
	   try {
		   GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.addsplitlist(mRNAname[0], mapMRNA2GeneType.get(mRNAname[1]));//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
		   hashRnaID2Iso.put(rnaID, gffGeneIsoInfo);
	   } catch (Exception e) {
//		   gffDetailGene = getGffDetailRnaID(rnaID);
//		   gffDetailGene.addsplitlist(mRNAname[0], mapMRNA2GeneType.get(mRNAname[1]));//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
		   logger.error("������Ҫ��飺" + mRNAname[0] + " " + mRNAname[1]);
	   }
   }
   /**
    * @param lastGeneIDandName
    * @param rnaID
    * @param ss
    * @return  ���ؼ����rna����
    */
   private String add_MapRnaID2RnaName_And_MapRnaID2GeneID(String[] lastGeneIDandName, String rnaID, String[] ss) {
	   String rnaName = patmRNAName.getPatFirst(ss[8]);
	   if (rnaName == null) {
		   rnaName = lastGeneIDandName[1];
	   }
	   //tRNA����������û��parentID�ģ����Ծͽ�����һ�е�geneIDץ����������
	   String geneID = patParentID.getPatFirst(ss[8]);
	   if (geneID == null) {
		   geneID = lastGeneIDandName[0];
	   }
	   hashRnaID2GeneID.put(rnaID, geneID);
	   return rnaName;
   }
   
   private boolean addExon(String[] ss) {
	   GffGeneIsoInfo gffGeneIsoInfo = null;
	   String rnaID = patParentID.getPatFirst(ss[8]);
	   try {
		   gffGeneIsoInfo = getGffIso(rnaID);
	   } catch (Exception e) {
		   logger.error("����δ֪exon��" + ArrayOperate.cmbString(ss, "\t"));
		  return false;
	   }
	   String geneID = getGeneID(rnaID);
	   mapGeneName2IsHaveExon.put(geneID, true);
	   if (gffGeneIsoInfo == null) {
		System.out.println("stop");
		gffGeneIsoInfo = getGffIso(rnaID);
	}
	   gffGeneIsoInfo.addExon(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
	   return true;
   }
   
   private void addCDS(String[] ss) {
	   String rnaID = patParentID.getPatFirst(ss[8]);
	   String geneID = getGeneID(rnaID);
	   GffGeneIsoInfo gffGeneIsoInfo = getGffIso(rnaID);
	   gffGeneIsoInfo.setATGUAG(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
	   if (mapGeneName2IsHaveExon.get(geneID) == null) {
		   logger.error("stop");
	   }
	   if (!mapGeneName2IsHaveExon.get(geneID)) {
		   gffGeneIsoInfo.addExon(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
	   }
   }
   private String getGeneName(String content) {
	   String geneName = patGeneName.getPatFirst(content);//���һ�������
	   if (geneName == null) {
		   String geneID = patGeneID.getPatFirst(content);
		   GeneID copedID = null;
		   try {
			   copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
		} catch (Exception e) {
			   copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
		}
		   geneName = copedID.getAccID();
	   }
	   if (geneName == null) {
		   logger.error("GffHashPlantGeneError: �ļ�  "+ getGffFilename() + "  �ڱ��п���û��ָ���Ļ���ID  " +content);
	   }
	   return geneName;
   }
   /**
    * @param content ��ص�ĳһ��
    * @return
    * string[2]
    * 0: geneName
    * 1: NCBI��ȡ��type
    */
   private String[] getMrnaName(String thisMRNAname, String[] content) {
	   String[] result = new String[2];
	   result[0] = thisMRNAname;
	   result[1] = content[2];//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
	   return result;
//	   
//	   String mRNAname = patmRNAName.getPatFirst(content[8]);//mRNApattern.matcher(content);
//	   if (mRNAname == null) {
//		   mRNAname = thisMRNAname;
//	   }
//	   if(mRNAname != null) {
//		   result[0] = mRNAname;
//		   result[1] = content[2];//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
//	   }
//	   else {
//		   try {
//			   String geneID = patGeneID.getPatFirst(content[8]);
//			   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
//			   result[0] = copedID.getSymbol();//����������
//			   result[1] = content[2];//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
//		   } catch (Exception e) {
//			   System.out.println("GffHashPlantGeneError: �ļ�  "+getGffFilename()+"  �ڱ��п���û��ָ���Ļ���ID  " +content);
//			   return null;
//		   }
//	   }
//	   return result;
   }
   /**
    * �趨taxID
    * @param geneName
    */
   private void setTaxID(String[] ss, String geneName) {
	   if (taxID != 0)
		   return;
	   
	   if (ss[2].equals("region")) {
		   //��ID=id0;Dbxref=taxon:9823;breed=mixed;chromosome=1;gbkey=Src;genom �����9823ץ����
		   try {  taxID = Integer.parseInt(PatternOperate.getPatLoc(ss[8], "(?<=Dbxref\\=taxon\\:)\\w+", false).get(0)[0]);  } catch (Exception e) { }
		   return;
	   }
	   if (taxID == 0 && numCopedIDsearch < 20) {
		   	ArrayList<GeneID> lsCopedIDs = GeneID.createLsCopedID(geneName, taxID, false);
		   	if (lsCopedIDs.size() == 1) {
		   		taxID = lsCopedIDs.get(0).getTaxID();
		   	}
		   	numCopedIDsearch ++;
	   }
   }
   /**
    * ��hashGenID2GffDetail�л�ø�GffDetailGene
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffDetailGene getGffDetailGenID(String genID) {
	   return hashGenID2GffDetail.get(genID);
   }
   /**
    * ��hashRnaID2GeneID�л�ø�GffDetailGene
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffDetailGene getGffDetailRnaID(String rnaID) {
	   String genID = hashRnaID2GeneID.get(rnaID);
	   return getGffDetailGenID(genID);
   }
   
   private String getGeneID(String rnaID) {
	   String geneID = hashRnaID2GeneID.get(rnaID);
	   if (geneID == null) {
		   geneID = rnaID;
	   }
	   return geneID;
   }
   /**
    * ��hashRnaID2RnaName�л�ø�RNA��GffGeneIsoInfo
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffGeneIsoInfo getGffIso(String rnaID) {
	   GffGeneIsoInfo gffGeneIsoInfo = hashRnaID2Iso.get(rnaID);
	   if (gffGeneIsoInfo == null) {
		   hashRnaID2GeneID.put(rnaID, rnaID);
		   GffDetailGene gffDetailGene = getGffDetailGenID(rnaID);
		   gffGeneIsoInfo = gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(), GeneType.ncRNA);
		   hashRnaID2Iso.put(rnaID, gffGeneIsoInfo);
	   }
	   return gffGeneIsoInfo;
	}
   
   //TODO ���ǽ��÷����ŵ�������
   /**
    * ��locGff�е���Ϣ����Ȼ��װ��ChrHash��
    */
   private void setGffList() {
	   mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
	   ListGff LOCList = null;
	   for (GffDetailGene gffDetailGene : hashGenID2GffDetail.values()) {
		   String chrIDlowCase = gffDetailGene.getParentName().toLowerCase();
			 //�µ�Ⱦɫ��
		   if (!mapChrID2ListGff.containsKey(chrIDlowCase)) { //�µ�Ⱦɫ�� 
			   LOCList = new ListGff();//�½�һ��LOCList������Chrhash
			   LOCList.setName(chrIDlowCase);
			   mapChrID2ListGff.put(gffDetailGene.getParentName().toLowerCase(), LOCList);
		   }
		   if (gffDetailGene.getLsCodSplit().size() == 0) {
			   gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(), GeneType.ncRNA);
			   gffDetailGene.addExon(gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs());
		   }
		   LOCList.add(gffDetailGene);
	   }
   }

   /**
    * ��NCBIgff�е�chrIDת��Ϊ��׼ChrID��Ȼ�����е�scaffoldɾ��
    * ͬʱ����tRNA������
    * @param NCBIgff /media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10.2_gnomon_top_level.gff3
    */
   public static void modifyNCBIgffFile(String NCBIgff) {
	   String regxChrID = "(?<=chromosome\\=)\\w+";
	   TxtReadandWrite txtGff = new TxtReadandWrite(NCBIgff, false);
	   TxtReadandWrite txtGffOut = new TxtReadandWrite(FileOperate.changeFileSuffix(NCBIgff, "_modify", null), true);
	   /** ����ͬ��chrID��Ҳд����ձ��� */
	   HashMap<String, String> mapAccID2ChrID = new HashMap<String, String>();
	   TxtReadandWrite txtGffOutConvertTab = new TxtReadandWrite(FileOperate.changeFileSuffix(NCBIgff, "_modify_ChrID_Tab", null), true);
	   
	   String chrID = "";
	   boolean tRNAflag = false; String[] tRNAtmp = null;
	   for (String string : txtGff.readlines()) {
		   if (string.startsWith("#")) {
			continue;
		   }
		   String[] ss = string.split("\t");
		   if (ss[2].equals("match") || ss[0].startsWith("NW_") || ss[0].startsWith("NT_")) {
			   continue;
		   }
		   
		   if (ss[2].equals("region")) {
			   if (ss[8].contains("genome=genomic")) {
				continue;
			   } else if (ss[8].contains("genome=mitochondrion")) {
				   chrID = "chrM";
			   } else if (ss[8].contains("genome=chloroplast")) {
				   chrID = "chrc";
			   } else {
				   try {
					   chrID = "chr" + PatternOperate.getPatLoc(ss[8], regxChrID, false).get(0)[0];
				   } catch (Exception e) {
					   logger.error("��λ�ó��������region������һ��regionӦ����һ��Ⱦɫ�壬���ﲻ֪����ʲô " + string);
					   chrID = "unkonwn";
				   }
			   }
			   mapAccID2ChrID.put(ss[0], chrID);
		   }
		   ss[0] = chrID;
		   if (chrID.equals("unknown")) {
			   continue;
		   }
		   if (tRNAflag) {
			   if (!ss[2].equals("tRNA")) {
				   txtGffOut.writefileln(tRNAtmp);
				   txtGffOut.writefileln(ss);
			   }
			   else {
				   int start = minmax(true, tRNAtmp[3], tRNAtmp[4], ss[3], ss[4]);
				   int end = minmax(false, tRNAtmp[3], tRNAtmp[4], ss[3], ss[4]);
				   tRNAtmp[3] = start + "";
				   tRNAtmp[4] = end + "";
				   txtGffOut.writefileln(tRNAtmp);
			   }
			   tRNAflag = false;
			   continue;
		   }
		   else {
			   if (ss[2].equals("tRNA")) {
				   tRNAflag = true;
				   tRNAtmp = ss;
				   continue;
			   }
		   }
		   txtGffOut.writefileln(ss);
		   
	   }
	   for (Entry<String, String> entry : mapAccID2ChrID.entrySet()) {
		   txtGffOutConvertTab.writefileln(entry.getKey() + "\t" + entry.getValue());
	   }
	   txtGff.close();
	   txtGffOut.close();
	   txtGffOutConvertTab.close();
   }
   /**
    * ���tRNA�����е���С�����ֵ����ΪtRNA�������յ�
    * @param min
    * @param is
    * @return
    */
   private static int minmax(boolean min,String...is) {
	   int[] intis = new int[is.length];
	   for (int i = 0; i < is.length; i++) {
		intis[i] = Integer.parseInt(is[i]);
	}
	   MathComput.sort(intis, min);
	   return intis[0];
   }
}
