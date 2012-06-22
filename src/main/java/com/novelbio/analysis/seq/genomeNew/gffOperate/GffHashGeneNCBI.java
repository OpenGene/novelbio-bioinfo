package com.novelbio.analysis.seq.genomeNew.gffOperate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.GeneID;

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
	public static void main(String[] args) {
		String NCBIgff = "/media/winE/Bioinformatics/GenomeData/soybean/gff/ref_V1.0_top_level.gff3";
		setGFF(NCBIgff);
	}
	
	
	private static Logger logger = Logger.getLogger(GffHashGeneNCBI.class);
	/** �������ֵ����򣬿��Ըĳ�ʶ�������������,���������Ͻ棬Ĭ��  NCBI��ID  */
	protected String regGeneName = "(?<=gene\\=)\\w+";
	/**  �ɱ����mRNA������Ĭ�� NCBI��ID */
	protected String regSplitmRNA = "(?<=transcript_id\\=)\\w+";
	/** geneID������ */
	protected String regGeneID = "(?<=Dbxref\\=GeneID\\:)\\d+";
	/** ID������ */
	protected String regID = "(?<=ID\\=)\\w+";
	/** parentID������ */
	protected String regParentID = "(?<=Parent\\=)\\w+";

	/** mRNA������ */
	private static HashMap<String, Integer> hashmRNA = new HashMap<String, Integer>();
	
	
	
	/** gene������ */
	private static HashSet<String> hashgene = new HashSet<String>();
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
	
	private void setPattern() {
		patGeneName = new PatternOperate(regGeneName, false);
		patmRNAName = new PatternOperate(regSplitmRNA, false);
		patGeneID = new PatternOperate(regGeneID, false);
		patID = new PatternOperate(regID, false);
		patParentID = new PatternOperate(regParentID, false);
	}
	/**
	 * �趨mRNA��gene������������gff�ļ�������ֵ�
	 */
	private void setHashName() {
		if (hashmRNA.isEmpty()) {
			hashmRNA.put("mRNA_TE_gene",GffGeneIsoInfo.TYPE_GENE_MRNA_TE);
			hashmRNA.put("mRNA",GffGeneIsoInfo.TYPE_GENE_MRNA);
			hashmRNA.put("miRNA",GffGeneIsoInfo.TYPE_GENE_MIRNA);
//			hashmRNA.put("tRNA",GffGeneIsoInfo.TYPE_GENE_TRNA);
			hashmRNA.put("pseudogenic_transcript",GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT);
			hashmRNA.put("snoRNA",GffGeneIsoInfo.TYPE_GENE_SNORNA);
			hashmRNA.put("snRNA",GffGeneIsoInfo.TYPE_GENE_SNRNA);
			hashmRNA.put("rRNA",GffGeneIsoInfo.TYPE_GENE_RRNA);
			hashmRNA.put("ncRNA",GffGeneIsoInfo.TYPE_GENE_NCRNA);
			hashmRNA.put("transcript",GffGeneIsoInfo.TYPE_GENE_MISCRNA);
		}
		if (hashgene.isEmpty()) {
			hashgene.add("gene");
			hashgene.add("transposable_element_gene");
			hashgene.add("pseudogene");
			hashgene.add("tRNA");
		}
	}
	private HashMap<String, String> hashGenID2GeneName = new HashMap<String, String>();
	private HashMap<String, String> hashRnaID2GeneID = new HashMap<String, String>();
	private HashMap<String, String> hashRnaID2RnaName = new HashMap<String, String>();
	private LinkedHashMap<String, GffDetailGene> hashGenID2GffDetail = new LinkedHashMap<String, GffDetailGene>();
	
	
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
   protected void ReadGffarrayExcep(String gfffilename) throws Exception
   {
	   setHashName();
	   setPattern();
	   locHashtable = new LinkedHashMap<String, GffDetailGene>();// �洢ÿ��LOCID���������Ϣ�Ķ��ձ�
	   LOCIDList = new ArrayList<String>();// ˳��洢ÿ������ţ��������������ȡ��������
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);	   
	   String chrnametmpString=""; //Ⱦɫ�����ʱ����
//	   GffDetailGene gffDetailLOC= null;
	   int m = 0;
	   for (String content : txtgff.readlines()) {
		   m++;
		   if(content.charAt(0)=='#') continue;
		   String[] ss = content.split("\t");//����tab�ֿ�
		   
		   chrnametmpString = ss[0].toLowerCase();//Сд��chrID

		   /** ����ȡ��geneʱ�����Ƕ�����һ���µĻ�����ô������������㣬�յ��ÿ��CDS�ĳ��ȶ�����list������   */
		   if (hashgene.contains(ss[2])) {//when read the # and the line contains gene, it means the new LOC
			   String genID = patID.getPatFirst(ss[8]);
			   String geneName = getGeneName(ss[8]); setTaxID(ss, geneName);
			   hashGenID2GeneName.put(genID, geneName);
			   GffDetailGene gffDetailLOC = getGffDetailGenID(patID.getPatFirst(ss[8]));
			   if (gffDetailLOC == null) {
				   gffDetailLOC=new GffDetailGene(chrnametmpString, geneName, ss[6].equals("+"));//�½�һ��������
			   }
			   gffDetailLOC.setTaxID(taxID);
			   gffDetailLOC.setStartAbs( Integer.parseInt(ss[3])); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//������ֹ      		
			   locHashtable.put(gffDetailLOC.getName().toLowerCase(), gffDetailLOC);//��ӽ���hash��LOCID��--GeneInforlist��ϣ��ȷ��������������ǵ���֮��Ĺ�ϵ    
			   hashGenID2GffDetail.put(genID, gffDetailLOC);
			   LOCIDList.add(gffDetailLOC.getName());
      	   }
		   /**
      	    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
      	    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
      	    * һ��������mRNA����Ҫ��ʼָ��5UTR��3UTR��CDS��������ֹ
      	    */
		   else if (hashmRNA.containsKey(ss[2])) {
			   String rnaID = patID.getPatFirst(ss[8]);
			   hashRnaID2RnaName.put(rnaID, patmRNAName.getPatFirst(ss[8]));
			   hashRnaID2GeneID.put(rnaID, patParentID.getPatFirst(ss[8]));
			   GffDetailGene gffDetailGene = null;
			   gffDetailGene = getGffDetailRnaID(rnaID);
			  
			   String[] mRNAname = getMrnaName(ss);
			   try {
				   gffDetailGene.addsplitlist(mRNAname[0], Integer.parseInt(mRNAname[1]));//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
			   } catch (Exception e) {
				  gffDetailGene = getGffDetailRnaID(rnaID);
				   gffDetailGene.addsplitlist(mRNAname[0], Integer.parseInt(mRNAname[1]));//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
				   logger.error(mRNAname[0] + " " + mRNAname[1]);
			}
			
		   }
		   else if (ss[2].contains("exon")) {
			   GffGeneIsoInfo gffGeneIsoInfo = null;
			   try {
				   gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
			} catch (Exception e) {
				 gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
				 logger.error("����δ֪exon��" + ss[2]);
			}
			  
			   gffGeneIsoInfo.addExon(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		   }
		   else if (ss[2].equals("CDS")) {
			   GffGeneIsoInfo gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
			   gffGeneIsoInfo.setATGUAG(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		   }
		   else
			   logger.error("����δ֪exon��" + ss[2]);
	   }
	   setGffList();
	   txtgff.close();
   }
   private String getGeneName(String content) {
	   String geneName = patGeneName.getPatFirst(content);//���һ�������
	   if (geneName == null) {
		   String geneID = patGeneID.getPatFirst(content);
		   if (geneID == null) {
			System.out.println("stop");
		}
		   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
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
    * 1: type
    */
   private String[] getMrnaName(String[] content) {
	   String[] result = new String[2];
	   String mRNAname = patmRNAName.getPatFirst(content[8]);//mRNApattern.matcher(content);
	   if(mRNAname != null) {
		   result[0] = mRNAname;
		   result[1] = hashmRNA.get(content[2]) + "";//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
	   }
	   else {
		   try {
			   String geneID = patGeneID.getPatFirst(content[8]);
			   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
			   result[0] = copedID.getAccID();//����������
			   result[1] = hashmRNA.get(content[2]) + "";//ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
		   } catch (Exception e) {
			   System.out.println("GffHashPlantGeneError: �ļ�  "+getGffFilename()+"  �ڱ��п���û��ָ���Ļ���ID  " +content);
			   return null;
		   }
	   }
	   return result;
   }
   int numCopedIDsearch = 0;//����taxID�Ĵ������10��
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
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffDetailGene getGffDetailGenID(String genID) {
	   return hashGenID2GffDetail.get(genID);
   }
   /**
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffDetailGene getGffDetailRnaID(String rnaID) {
	   String genID = hashRnaID2GeneID.get(rnaID);
	   return getGffDetailGenID(genID);
   }
   /**
    * �����genID�����������ݿ������geneID������NCBI gff�����е�ID
    * @param genID
    * @return null ��ʾû���ҵ���Ӧ��GffDetail��Ϣ
    */
   private GffGeneIsoInfo getGffIso(String rnaID) {
	   String rnaName = hashRnaID2RnaName.get(rnaID);
	   GffDetailGene gffDetailGene = null;
	   if (rnaName == null) {
		   hashRnaID2GeneID.put(rnaID, rnaID);
		   gffDetailGene = getGffDetailGenID(rnaID);
		   hashRnaID2RnaName.put(rnaID, gffDetailGene.getName());
		   rnaName = gffDetailGene.getName();
		   gffDetailGene.addsplitlist(gffDetailGene.getName(), GffGeneIsoInfo.TYPE_GENE_NCRNA);
	   }
	   else {
		   gffDetailGene = getGffDetailRnaID(rnaID);
	   }
	   return gffDetailGene.getIsolist(rnaName);		
	}

   
   
   /**
    * ��locGff�е���Ϣ����Ȼ��װ��ChrHash��
    */
   private void setGffList() {
	   Chrhash = new LinkedHashMap<String, ListGff>();
	   ListGff LOCList = null;
	   for (GffDetailGene gffDetailGene : hashGenID2GffDetail.values()) {
			 //�µ�Ⱦɫ��
		   if (!Chrhash.containsKey(gffDetailGene.getParentName())) { //�µ�Ⱦɫ�� 
			   LOCList = new ListGff();//�½�һ��LOCList������Chrhash
			   LOCList.setName(gffDetailGene.getParentName());
			   Chrhash.put(gffDetailGene.getParentName(), LOCList);
		   }
		   LOCList.add(gffDetailGene);
	   }

   }
   
   /**
    * ��NCBIgff�е�chrIDת��Ϊ��׼ChrID��Ȼ�����е�scaffoldɾ��
    * ͬʱ����tRNA������
    * @param NCBIgff /media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10.2_gnomon_top_level.gff3
    */
   public static void setGFF(String NCBIgff) {
	   String regxChrID = "(?<=chromosome\\=)\\w+";
	   TxtReadandWrite txtGff = new TxtReadandWrite(NCBIgff, false);
	   TxtReadandWrite txtGffOut = new TxtReadandWrite(FileOperate.changeFileSuffix(NCBIgff, "_modify", null), true);
	   String chrID = "";
	   boolean tRNAflag = false; String[] tRNAtmp = null;
	   for (String string : txtGff.readlines()) {
		   if (string.startsWith("#")) {
			continue;
		   }
		   String[] ss = string.split("\t");
		   if (ss[2].equals("match") || ss[0].startsWith("NW_")) {
			   continue;
		   }
		   if (ss[2].equals("region")) {
			   if (ss[8].contains("genome=genomic")) {
				continue;
			   }
			   else if (ss[8].contains("genome=mitochondrion")) {
				   chrID = "chrm";
			   }
			   else if (ss[8].contains("genome=chloroplast")) {
				   chrID = "chrc";
			   }
			   else {
				   chrID = "chr" + PatternOperate.getPatLoc(ss[8], regxChrID, false).get(0)[0];
			   }
		   }
		   ss[0] = chrID;
		   
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
	   txtGff.close();
	   txtGffOut.close();
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
