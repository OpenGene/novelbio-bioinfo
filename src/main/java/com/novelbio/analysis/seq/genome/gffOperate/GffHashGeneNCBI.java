package com.novelbio.analysis.seq.genome.gffOperate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * 应该是标准的gff3格式，仅用于NCBI的gff3文件
 * 
 * 获得Gff的基因数组信息,本类必须实例化才能使用<br/>
 * 输入Gff文件，最后获得两个哈希表和一个list表,
 * 结构如下：<br/>
 * 1.hash（ChrID）--ChrList--GffDetail(GffDetail类,实际是GffDetailUCSCgene子类)<br/>
 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
 * chr格式，全部小写 chr1,chr2,chr11<br/>
 * 
 * 2.hash（LOCID）--GffDetail，其中LOCID代表具体的基因编号 <br/>
 * 
 * 3.list（LOCID）--LOCList，按顺序保存LOCID<br/>
 * 
 * 每个基因的起点终点和CDS的起点终点保存在GffDetailList类中<br/>
 */
public class GffHashGeneNCBI extends GffHashGeneAbs{
	private static final Logger logger = Logger.getLogger(GffHashGeneNCBI.class);
	
	/** 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认  NCBI的ID  */
	protected static String regGeneName = "(?<=gene\\=)[\\w\\-%]+";
	/**  可变剪接mRNA的正则，默认 NCBI的ID */
	protected static String regSplitmRNA = "(?<=transcript_id\\=)[\\w\\-\\.]+";
	/**  可变剪接mRNA的产物的正则，默认 NCBI的symbol */
	protected static String regProduct = "(?<=product\\=)[\\w\\-%]+";
	/** geneID的正则 */
	protected static String regGeneID = "(?<=Dbxref\\=GeneID\\:)\\d+";
	/** Name的正则 */
	protected static String regName = "(?<=Name\\=)[\\w\\-%]+";
	/** ID的正则 */
	protected static String regID = "(?<=ID\\=)[\\w\\-\\.]+";
	/** parentID的正则 */
	protected static String regParentID = "(?<=Parent\\=)[\\w\\.\\-%]+";

	/** gene类似名 */
	private static HashSet<String> setIsGene = new HashSet<String>();
	/** gene类似名 */
	private static HashSet<String> setIsChromosome = new HashSet<String>();
		
	public static void main(String[] args) {
////		GffHashGeneNCBI.modifyNCBIgffFile("/media/winE/Bioinformatics/genome/checken/gal4_UCSC/gff/ref_Gallus_gallus-4.0_top_level.gff3");
//		GffHashGeneNCBI gffHashGeneNCBI = new GffHashGeneNCBI();
//		gffHashGeneNCBI.ReadGffarray("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/ref_GRCh37.p9_top_level_modify.gff3");
//		GffGeneIsoInfo gffGeneIsoInfo = gffHashGeneNCBI.searchISO("XM_003481161");		
//		System.out.println(gffGeneIsoInfo.getName());
//		gffGeneIsoInfo = gffHashGeneNCBI.searchISO("IGKV");
//		System.out.println(gffGeneIsoInfo.getName());
//		System.out.println(gffGeneIsoInfo.getATGsite());
//		System.out.println(gffGeneIsoInfo.getUAGsite());
//		System.out.println(gffGeneIsoInfo.get(0).getStartCis());
//		System.out.println(gffGeneIsoInfo.get(1).getEndCis());
		TxtReadandWrite txtRead = new TxtReadandWrite("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/ref_GRCh37.p9_top_level.gff3", false);
		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/ref_GRCh37.p9_top_levelaaaa.gff3", true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if (ss.length >= 3 && ss[2].equals("region")) {
				txtWrite.writefileln(string);
			}
		}
		txtRead.close();
		txtWrite.close();
	
	}
	
	/** "(?<=gene\\=)\\w+" */
	PatternOperate patGeneName = null;
	/**  "(?<=transcript_id\\=)\\w+" */
	PatternOperate patmRNAName = null;
	/** "(?<=Dbxref\\=GeneID\\:)\\d+" */
	PatternOperate patGeneID = null;
	/** "(?<=Name\\=)\\w+" */
	PatternOperate patName = null;
	/** "(?<=ID\\=)\\w+" */
	PatternOperate patID = null;
	/** "(?<=Parent\\=)\\w+" */
	PatternOperate patParentID = null;
	/** "(?<=product\\=)\\w+" */
	PatternOperate patProduct = null;

	private HashMap<String, String> mapRnaID2GeneID = new HashMap<String, String>();
	private LinkedHashMap<String, GffDetailGene> mapGenID2GffDetail = new LinkedHashMap<String, GffDetailGene>();
	
	/** 这两个是一对，一个是rnaID对应多个iso，常见于TRNA
	 * 另一个这个存储ISO对应的坐标 */
	private ArrayListMultimap<String, GffGeneIsoInfo> mapRnaID2LsIso = ArrayListMultimap.create();
	private ArrayListMultimap<String, ExonInfo> mapRnaID2LsIsoLocInfo = ArrayListMultimap.create();
	
	/** 将第一列换算为chrID */
	private Map<String, String> mapID2ChrID = new HashMap<String, String>();
	
	private Map<String, GeneType> mapGeneID2GeneType = new HashMap<String, GeneType>();
	
	/** 
	 * 一般的转录本都会先出现exon，然后出现CDS，如下<br>
	 * hr3	RefSeq	mRNA	59958839	59959481<br>
	 * chr3	RefSeq	exon	59959427	59959481<br>
	 * chr3	RefSeq	exon	59958839	59959233<br>
	 * chr3	RefSeq	CDS	59959427	59959481<br>
	 * chr3	RefSeq	CDS	59958839	59959233<br>
	 *但是有些转录本不会出现exon，但是后面会出现CDS，如下<br>
	 * chr3	RefSeq	gene	59962472	59963232<br>
	 * chr3	RefSeq	V_gene_segment	59963181	59963232<br>
	 * chr3	RefSeq	V_gene_segment	59962472	59962797<br>
	 * chr3	RefSeq	CDS	59963181	59963229<br>
	 * chr3	RefSeq	CDS	59962472	59962797<br>
	 * 那么本map就用来记录该转录本是否出现了exon，如果出现了exon，CDS就只用来设定ATG和UAG。
	 * 如果没有出现exon，CDS就要当exon来设定。
	 */
	private HashMap<String, Boolean> mapGeneName2IsHaveExon = new HashMap<String, Boolean>();
	int numCopedIDsearch = 0;//查找taxID的次数最多10次
	/** 默认连上数据库 */
	boolean database = true;
	/**
	 * 设定mRNA和gene的类似名，在gff文件里面出现的
	 */
	private void setHashName() {
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("transposable_element");
			setIsGene.add("pseudogene");
//			setIsGene.add("tRNA");
		}
		if (setIsChromosome.isEmpty()) {
			setIsChromosome.add("chromosome");
		}
	}
	private void setPattern() {
		patGeneName = new PatternOperate(regGeneName, false);
		patmRNAName = new PatternOperate(regSplitmRNA, false);
		patProduct = new PatternOperate(regProduct, false);
		
		patGeneID = new PatternOperate(regGeneID, false);
		patID = new PatternOperate(regID, false);
		patName = new PatternOperate(regName, false);
		patParentID = new PatternOperate(regParentID, false);
	}
	/**
	 * 最底层读取gff的方法，本方法只能读取UCSCknown gene<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表<br/>
	 * 结构如下：<br/>
     * 输入Gff文件，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * 其中LOCID代表具体的条目编号，在UCSCkonwn gene里面没有转录本一说，
	 * 只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存LOCID,这里不考虑多个转录本，每一个转录本就是一个单独的LOCID <br>
     * <b>4. LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起： NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
   protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
	   setHashName();
	   setPattern();
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);
	   
	   //当前的geneID，主要是给tRNA和miRNA用的，因为别的mRNA都有parent geneID可以通过这个ID回溯geneName
	   //但是tRNA和miRNA就没有这个parent geneID，所以就记载下来给他们用
	   String[] thisGeneIDandName = null;
	   String[] thisRnaIDandName = null;	   
	   
	   for (String content : txtgff.readlines()) {
		   if(content.charAt(0)=='#') continue;
		   String[] ss = content.split("\t");//按照tab分开
		   if (ss[2].equals("match") || ss[2].equals("chromosome") || ss[0].startsWith("NW_") || ss[0].startsWith("NT_")) {
			   continue;
		   }
		   ss[0] = getChrID(ss);
		   if (ss[2].equals("region")) {
			   continue;
		   }
		   if (content.contains("AT3TE00010")) {
			   logger.error("stop");
		   }
		   //读取到gene
		   if (setIsGene.contains(ss[2])) {
			   thisGeneIDandName = addNewGene(ss);
		   }
		   /**
      	    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
      	    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
      	    * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
      	    */
		   else if (GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())) {
			   thisRnaIDandName = addMRNA(thisGeneIDandName, ss);
		   }
		   else if (ss[2].contains("exon")) {
			   if (!addExon(thisGeneIDandName, thisRnaIDandName, ss)) {
				   continue;
			   }
		   }
		   else if (ss[2].equals("CDS")) {
			   addCDS(thisGeneIDandName, thisRnaIDandName, ss);
		   }
		   else
			   logger.error("出现未知exon：" +  ArrayOperate.cmbString(ss, "\t"));
	   }
	   setGffList();
	   txtgff.close();
	   
	   clear();
   }
   
   /**
    * 输入NC编号，返回染色体ID
    * @param ss
    * @return
    */
   private String getChrID(String[] ss) {
	   String chrID;
	   String regxChrID = "(?<=chromosome\\=)\\w+";
	   try {
		   if (ss[2].equals("region")) {
			   if (ss[8].contains("genome=genomic")) {
				return null;
			   } else if (ss[8].contains("genome=mitochondrion")) {
				   chrID = "chrm";
			   } else if (ss[8].contains("genome=chloroplast")) {
				   chrID = "chrc";
			   } else {
				   try {
					   chrID = "chr" + PatternOperate.getPatLoc(ss[8], regxChrID, false).get(0)[0];
				   } catch (Exception e) {
					   logger.error("本位置出错，错误的region，本来一个region应该是一个染色体，这里不知道是什么 " + ArrayOperate.cmbString(ss, "\t"));
					   chrID = "unkonwn";
				   }
			   }
			   mapID2ChrID.put(ss[0], chrID);
		   }
	   } catch (Exception e) { }
	   
	   if (ss[0].toLowerCase().startsWith("chr")) {
			return ss[0];
		}
	   return mapID2ChrID.get(ss[0]);
   }
   /** 当读取到gene时，就是读到了一个新的基因，那么新建一个基因
    * 并且返回string[2]<br>
    * 0: geneID<br>
    * 1: geneName
    */
   private String[] addNewGene(String[] ss) {
	 //when read the # and the line contains gene, it means the new LOC
	   String geneID = patID.getPatFirst(ss[8]);
	   String geneName = getGeneName(ss[8]); setTaxID(ss, geneName);
	   
	   GffDetailGene gffDetailLOC = mapGenID2GffDetail.get(geneID);
	   mapGeneID2GeneType.put(geneID, GeneType.getGeneType(ss[2]));
	   if (gffDetailLOC == null) {
		   gffDetailLOC=new GffDetailGene(ss[0], geneName, ss[6].equals("+"));//新建一个基因类
	   }
	   gffDetailLOC.setTaxID(taxID);
	   gffDetailLOC.setStartAbs( Integer.parseInt(ss[3])); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//基因起止
	   mapGenID2GffDetail.put(geneID, gffDetailLOC);
	   
	   mapGeneName2IsHaveExon.put(geneID, false);
	   return new String[]{geneID, geneName};
   }
   /**
    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
    * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
    * 并且返回string[2]<br>
    * 0: rnaID<br>
    * 1: rnaName
    */
   private String[]  addMRNA(String[] lastGeneIDandName, String[] ss) {
	   String rnaID = patID.getPatFirst(ss[8]);
	   String rnaName = add_MapRnaID2RnaName_And_MapRnaID2GeneID(lastGeneIDandName, rnaID, ss[8]);
	   GffDetailGene gffDetailGene = getGffDetailRnaID(rnaID);
	   
	   String[] mRNAname = getMrnaName(rnaName, ss);
	   try {
		   GeneType geneType = mapGeneID2GeneType.get(mapRnaID2GeneID.get(rnaID));
		   if (geneType == null) {
			   geneType = GeneType.getGeneType(mRNAname[1]);
		   }
		   GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.addsplitlist(mRNAname[0], geneType);//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
		   mapRnaID2LsIso.put(rnaID, gffGeneIsoInfo);
		   ExonInfo exonInfo = new ExonInfo("", true, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		   mapRnaID2LsIsoLocInfo.put(rnaID, exonInfo);
	   } catch (Exception e) {
//		   gffDetailGene = getGffDetailRnaID(rnaID);
//		   gffDetailGene.addsplitlist(mRNAname[0], mapMRNA2GeneType.get(mRNAname[1]));//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
		   logger.error("错误，需要检查：" + mRNAname[0] + " " + mRNAname[1]);
	   }
	   return new String[]{rnaID, rnaName};
	   
   }
   /**
    * @param lastGeneIDandName
    * @param rnaID
    * @param ss
    * @return  返回加入的rna名字
    */
   private String add_MapRnaID2RnaName_And_MapRnaID2GeneID(String[] lastGeneIDandName, String rnaID, String ss8) {
	   String rnaName = patmRNAName.getPatFirst(ss8);
	   if (rnaName == null) {
		   rnaName = lastGeneIDandName[1];
	   }
	   //tRNA这种里面是没有parentID的，所以就将其上一行的geneID抓过来就行了
	   String geneID = patParentID.getPatFirst(ss8);
	   if (geneID == null) {
		   geneID = lastGeneIDandName[0];
	   }
	   mapRnaID2GeneID.put(rnaID, geneID);
	   return rnaName;
   }
   
   private boolean addExon(String[] lastGeneID2Name, String[] lastRnaID2Name, String[] ss) {
	   String rnaID = getRNAID(lastGeneID2Name, lastRnaID2Name, ss[8]);
	   
	   GffGeneIsoInfo gffGeneIsoInfo = null;
	   int exonStart = Integer.parseInt(ss[3]);
	   int exonEnd = Integer.parseInt(ss[4]);
	   
	   try {
		   gffGeneIsoInfo = getGffIso(rnaID, exonStart, exonEnd);//TODO
	   } catch (Exception e) {
		   logger.error("出现未知exon：" + ArrayOperate.cmbString(ss, "\t"));
		   gffGeneIsoInfo = getGffIso(rnaID, exonStart, exonEnd);//TODO
		  return false;
	   }
	   String geneID = getGeneID(rnaID);
	   if (mapGeneName2IsHaveExon.get(geneID) == null) {
		   logger.error("没有找到相应的GeneID:" + geneID);
	   }
	   if (mapGeneName2IsHaveExon.get(geneID) == false) {
		   gffGeneIsoInfo.addFirstExon(exonStart, exonEnd);
		   mapGeneName2IsHaveExon.put(geneID, true);
	   } else {
		   gffGeneIsoInfo.addExon(exonStart, exonEnd);
	   }
	   
	   return true;
   }
   
   private void addCDS(String[] lastGeneID2Name, String[] lastRnaID2Name, String[] ss) {
	   int cdsStart = Integer.parseInt(ss[3]);
	   int cdsEnd = Integer.parseInt(ss[4]);
	   String rnaID = getRNAID(lastGeneID2Name, lastRnaID2Name, ss[8]);
	   String geneID = getGeneID(rnaID);
	   GffGeneIsoInfo gffGeneIsoInfo = getGffIso(rnaID, cdsStart, cdsEnd);
	   gffGeneIsoInfo.setATGUAG(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
	   if (mapGeneName2IsHaveExon.get(geneID) == null) {
		   logger.error("没有找到相应的GeneID:" + geneID);
	   }
	   if (!mapGeneName2IsHaveExon.get(geneID)) {
		   gffGeneIsoInfo.addExon(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
	   }
   }
   
   /**
    * 首先查找ss8，看能否找到RNAID，找不到就返回上一个RNAID，再为null就返回上一个geneID
    * @param lastGeneID
    * @param lastRNAID
    * @param
    */
   private String getRNAID(String[] lastGeneID2Name, String[] lastRNAID2Name, String ss8) {
	   String rnaID = patParentID.getPatFirst(ss8);
	   if (rnaID == null) {
		   rnaID = lastRNAID2Name[0];
		   if (rnaID == null) {
			   rnaID = lastGeneID2Name[0];
		   }
	   }
	   return rnaID;
   }
   
   private String getGeneName(String content) {
	   String geneName = patGeneName.getPatFirst(content);//查找基因名字
	   if (geneName == null) {
		   if (database) {
			   try {
				   String geneID = patGeneID.getPatFirst(content);
				   GeneID copedID  = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
				   geneName = copedID.getAccID();
			   } catch (Exception e) {
				   database = false;
			   }
		   }
	   }
	   if (geneName == null) {
		   geneName = patName.getPatFirst(content);
	   } if (geneName == null) {
		   logger.error("GffHashPlantGeneError: 文件  "+ getGffFilename() + "  在本行可能没有指定的基因ID  " +content);
	   }
	   return geneName;
   }
   /**
    * @param content 相关的某一行
    * @return
    * string[2]
    * 0: geneName
    * 1: NCBI读取的type
    */
   private String[] getMrnaName(String thisMRNAname, String[] content) {
	   String[] result = new String[2];
	   result[0] = thisMRNAname;
	   result[1] = content[2];//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
	   return result;
//	   
//	   String mRNAname = patmRNAName.getPatFirst(content[8]);//mRNApattern.matcher(content);
//	   if (mRNAname == null) {
//		   mRNAname = thisMRNAname;
//	   }
//	   if(mRNAname != null) {
//		   result[0] = mRNAname;
//		   result[1] = content[2];//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
//	   }
//	   else {
//		   try {
//			   String geneID = patGeneID.getPatFirst(content[8]);
//			   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
//			   result[0] = copedID.getSymbol();//这里有问题
//			   result[1] = content[2];//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
//		   } catch (Exception e) {
//			   System.out.println("GffHashPlantGeneError: 文件  "+getGffFilename()+"  在本行可能没有指定的基因ID  " +content);
//			   return null;
//		   }
//	   }
//	   return result;
   }
   /**
    * 设定taxID
    * @param geneName
    */
   private void setTaxID(String[] ss, String geneName) {
	   if (taxID != 0)
		   return;
	   
	   if (ss[2].equals("region")) {
		   //把ID=id0;Dbxref=taxon:9823;breed=mixed;chromosome=1;gbkey=Src;genom 里面的9823抓出来
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
    * 从hashGenID2GffDetail中获得该GffDetailGene
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param genID
    * @return null 表示没有找到相应的GffDetail信息
    */
   private GffDetailGene getGffDetailGenID(String genID) {
	   return mapGenID2GffDetail.get(genID);
   }
   /**
    * 从hashRnaID2GeneID中获得该GffDetailGene
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param genID
    * @return null 表示没有找到相应的GffDetail信息
    */
   private GffDetailGene getGffDetailRnaID(String rnaID) {
	   String genID = mapRnaID2GeneID.get(rnaID);
	   return getGffDetailGenID(genID);
   }
   
   private String getGeneID(String rnaID) {
	   String geneID = mapRnaID2GeneID.get(rnaID);
	   if (geneID == null) {
		   geneID = rnaID;
	   }
	   return geneID;
   }
   
   /**
    * 从hashRnaID2RnaName中获得该RNA的GffGeneIsoInfo
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param rnaID 输入的rnaID
    * @param startExon 输入exon的起点和终点，查找lsGffISO，只有当ISO cover 这对坐标时，才会返回相应的ISO
    * 主要用于这种情况：<br>
    * NC_000001.10	RefSeq	gene	94313129	94313213<br>
    * NC_000001.10	RefSeq	tRNA	94313129	94313165<br>
    * NC_000001.10	RefSeq	tRNA	94313178	94313213<br>
    * NC_000001.10	RefSeq	exon	94313129	94313165<br>
    * NC_000001.10	RefSeq	exon	94313178	94313213<br>
    * 这时候两个tRNA的rnaID是一样的，但是这两个tRNA确实是两个不同的iso，所以就要根据坐标将两个exon分别装入两个iso中
    * @param endExon 如果startExon和endExon中有一个小于0，则直接返回listIso的第一个ISO
    * @return
    */
   private GffGeneIsoInfo getGffIso(String rnaID, int startExon, int endExon) {
	   List<GffGeneIsoInfo> lsGffGeneIsoInfo = mapRnaID2LsIso.get(rnaID);
	   List<ExonInfo> lsGffLoc = mapRnaID2LsIsoLocInfo.get(rnaID);
	   if (lsGffGeneIsoInfo.size() == 0) {
		   mapRnaID2GeneID.put(rnaID, rnaID);
		   GffDetailGene gffDetailGene = getGffDetailGenID(rnaID);
		   GffGeneIsoInfo gffGeneIsoInfo = null;
		   gffGeneIsoInfo = gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(), GeneType.ncRNA);
		
		   mapRnaID2LsIso.put(rnaID, gffGeneIsoInfo);
		   lsGffGeneIsoInfo = mapRnaID2LsIso.get(rnaID);
	   }
	   
	   if (lsGffGeneIsoInfo.size() > 1 && startExon > 0 && endExon > 0) {
		   for (int i = 0; i < lsGffLoc.size(); i++) {
			   ExonInfo exonInfo = lsGffLoc.get(i);
			   if (exonInfo.getStartAbs() <= startExon && exonInfo.getEndAbs() >= endExon) {
				   return lsGffGeneIsoInfo.get(i);
			   }
		   }
	   }
	   return lsGffGeneIsoInfo.get(0);
	}
   
   //TODO 考虑将该方法放到超类中
   /**
    * 将locGff中的信息整理然后装入ChrHash中
    */
   private void setGffList() {
	   mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
	   ListGff LOCList = null;
	   for (GffDetailGene gffDetailGene : mapGenID2GffDetail.values()) {
		   String chrIDlowCase = gffDetailGene.getRefID().toLowerCase();
			 //新的染色体
		   if (!mapChrID2ListGff.containsKey(chrIDlowCase)) { //新的染色体 
			   LOCList = new ListGff();//新建一个LOCList并放入Chrhash
			   LOCList.setName(chrIDlowCase);
			   mapChrID2ListGff.put(gffDetailGene.getRefID().toLowerCase(), LOCList);
		   }
		   if (gffDetailGene.getLsCodSplit().size() == 0) {
			   gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(), GeneType.ncRNA);
			   gffDetailGene.addExon(gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs());
		   }
		   for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			   if (gffGeneIsoInfo.size() == 0) {
				   gffGeneIsoInfo.addExon(gffDetailGene.getStartCis(), gffDetailGene.getEndCis());
			   }
		   }
		   LOCList.add(gffDetailGene);
	   }
   }

   /**
    * 读取完毕后清空一些变量
    */
   private void clear() {
	   patGeneName = null;
	   patmRNAName = null;
	   patGeneID = null;
	   patName = null;
	   patID = null;
	   patParentID = null;
	   patProduct = null;

	   mapRnaID2GeneID = null;
	   mapGenID2GffDetail = null;
		
	   mapRnaID2LsIso = null;
	   mapRnaID2LsIsoLocInfo = null;
	   mapID2ChrID = null;
	   mapGeneName2IsHaveExon = null;
   }
   
   /**
    * 将NCBIgff中的chrID转换为标准ChrID，然后将其中的scaffold删除
    * 同时修正tRNA的问题
    * @param NCBIgff /media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10.2_gnomon_top_level.gff3
    */
   public static void modifyNCBIgffFile(String NCBIgff) {
	   String regxChrID = "(?<=chromosome\\=)\\w+";
	   TxtReadandWrite txtGff = new TxtReadandWrite(NCBIgff, false);
	   TxtReadandWrite txtGffOut = new TxtReadandWrite(FileOperate.changeFileSuffix(NCBIgff, "_modify", null), true);
	   /** 将不同的chrID表也写入对照表中 */
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
					   logger.error("本位置出错，错误的region，本来一个region应该是一个染色体，这里不知道是什么 " + string);
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
    * 获得tRNA的两行的最小和最大值，作为tRNA的起点和终点
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
