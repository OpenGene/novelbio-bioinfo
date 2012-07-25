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
	private static Logger logger = Logger.getLogger(GffHashGeneNCBI.class);
	
	/** 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认  NCBI的ID  */
	protected String regGeneName = "(?<=gene\\=)\\w+";
	/**  可变剪接mRNA的正则，默认 NCBI的ID */
	protected String regSplitmRNA = "(?<=transcript_id\\=)\\w+";
	/** geneID的正则 */
	protected String regGeneID = "(?<=Dbxref\\=GeneID\\:)\\d+";
	/** ID的正则 */
	protected String regID = "(?<=ID\\=)\\w+";
	/** parentID的正则 */
	protected String regParentID = "(?<=Parent\\=)\\w+";
	/** mRNA类似名 */
	//TODO 考虑用enum的map来实现
	private static HashMap<String, Integer> mapMRNA2ID = new HashMap<String, Integer>();

	/** gene类似名 */
	private static HashSet<String> setIsGene = new HashSet<String>();
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
	
	private HashMap<String, String> mapGenID2GeneName = new HashMap<String, String>();
	private HashMap<String, String> hashRnaID2GeneID = new HashMap<String, String>();
	private HashMap<String, String> hashRnaID2RnaName = new HashMap<String, String>();
	private LinkedHashMap<String, GffDetailGene> hashGenID2GffDetail = new LinkedHashMap<String, GffDetailGene>();
	
	int numCopedIDsearch = 0;//查找taxID的次数最多10次

	/**
	 * 设定mRNA和gene的类似名，在gff文件里面出现的
	 */
	private void setHashName() {
		if (mapMRNA2ID.isEmpty()) {
			mapMRNA2ID.put("mRNA_TE_gene",GffGeneIsoInfo.TYPE_GENE_MRNA_TE);
			mapMRNA2ID.put("mRNA",GffGeneIsoInfo.TYPE_GENE_MRNA);
			mapMRNA2ID.put("miRNA",GffGeneIsoInfo.TYPE_GENE_MIRNA);
//			hashmRNA.put("tRNA",GffGeneIsoInfo.TYPE_GENE_TRNA);
			mapMRNA2ID.put("pseudogenic_transcript",GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT);
			mapMRNA2ID.put("snoRNA",GffGeneIsoInfo.TYPE_GENE_SNORNA);
			mapMRNA2ID.put("snRNA",GffGeneIsoInfo.TYPE_GENE_SNRNA);
			mapMRNA2ID.put("rRNA",GffGeneIsoInfo.TYPE_GENE_RRNA);
			mapMRNA2ID.put("ncRNA",GffGeneIsoInfo.TYPE_GENE_NCRNA);
			mapMRNA2ID.put("transcript",GffGeneIsoInfo.TYPE_GENE_MISCRNA);
		}
		if (setIsGene.isEmpty()) {
			setIsGene.add("gene");
			setIsGene.add("transposable_element_gene");
			setIsGene.add("pseudogene");
			setIsGene.add("tRNA");
		}
	}
	private void setPattern() {
		patGeneName = new PatternOperate(regGeneName, false);
		patmRNAName = new PatternOperate(regSplitmRNA, false);
		patGeneID = new PatternOperate(regGeneID, false);
		patID = new PatternOperate(regID, false);
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
   protected void ReadGffarrayExcep(String gfffilename) throws Exception {
	   setHashName();
	   setPattern();
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);	   
	   String tmpChrName="";
	   
	   for (String content : txtgff.readlines()) {
		   if(content.charAt(0)=='#') continue;
		   String[] ss = content.split("\t");//按照tab分开
		   
		   tmpChrName = ss[0].toLowerCase();//小写的chrID

		   /** 当读取到gene时，就是读到了一个新的基因，那么将这个基因的起点，终点和每个CDS的长度都放入list数组中   */
		   if (setIsGene.contains(ss[2])) {//when read the # and the line contains gene, it means the new LOC
			   String genID = patID.getPatFirst(ss[8]);
			   String geneName = getGeneName(ss[8]); setTaxID(ss, geneName);
			   mapGenID2GeneName.put(genID, geneName);
			   GffDetailGene gffDetailLOC = getGffDetailGenID(patID.getPatFirst(ss[8]));
			   if (gffDetailLOC == null) {
				   gffDetailLOC=new GffDetailGene(tmpChrName, geneName, ss[6].equals("+"));//新建一个基因类
			   }
			   gffDetailLOC.setTaxID(taxID);
			   gffDetailLOC.setStartAbs( Integer.parseInt(ss[3])); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//基因起止      		
			   hashGenID2GffDetail.put(genID, gffDetailLOC);
      	   }
		   /**
      	    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
      	    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
      	    * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
      	    */
		   else if (mapMRNA2ID.containsKey(ss[2])) {
			   String rnaID = patID.getPatFirst(ss[8]);
			   hashRnaID2RnaName.put(rnaID, patmRNAName.getPatFirst(ss[8]));
			   hashRnaID2GeneID.put(rnaID, patParentID.getPatFirst(ss[8]));
			   GffDetailGene gffDetailGene = null;
			   gffDetailGene = getGffDetailRnaID(rnaID);
			  
			   String[] mRNAname = getMrnaName(ss);
			   try {
				   gffDetailGene.addsplitlist(mRNAname[0], Integer.parseInt(mRNAname[1]));//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
			   } catch (Exception e) {
				  gffDetailGene = getGffDetailRnaID(rnaID);
				   gffDetailGene.addsplitlist(mRNAname[0], Integer.parseInt(mRNAname[1]));//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
				   logger.error(mRNAname[0] + " " + mRNAname[1]);
			}
			
		   }
		   else if (ss[2].contains("exon")) {
			   GffGeneIsoInfo gffGeneIsoInfo = null;
			   try {
				   gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
			} catch (Exception e) {
				 gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
				 logger.error("出现未知exon：" + ss[2]);
			}
			  
			   gffGeneIsoInfo.addExon(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		   }
		   else if (ss[2].equals("CDS")) {
			   GffGeneIsoInfo gffGeneIsoInfo = getGffIso(patParentID.getPatFirst(ss[8]));
			   gffGeneIsoInfo.setATGUAG(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
		   }
		   else
			   logger.error("出现未知exon：" + ss[2]);
	   }
	   setGffList();
	   txtgff.close();
   }
   private String getGeneName(String content) {
	   String geneName = patGeneName.getPatFirst(content);//查找基因名字
	   if (geneName == null) {
		   String geneID = patGeneID.getPatFirst(content);
		   if (geneID == null) {
			System.out.println("stop");
		}
		   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
		   geneName = copedID.getAccID();
	   }
	   if (geneName == null) {
		   logger.error("GffHashPlantGeneError: 文件  "+ getGffFilename() + "  在本行可能没有指定的基因ID  " +content);
	   }
	   return geneName;
   }
   /**
    * @param content 相关的某一行
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
		   result[1] = mapMRNA2ID.get(content[2]) + "";//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
	   }
	   else {
		   try {
			   String geneID = patGeneID.getPatFirst(content[8]);
			   GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, geneID, taxID);
			   result[0] = copedID.getAccID();//这里有问题
			   result[1] = mapMRNA2ID.get(content[2]) + "";//每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
		   } catch (Exception e) {
			   System.out.println("GffHashPlantGeneError: 文件  "+getGffFilename()+"  在本行可能没有指定的基因ID  " +content);
			   return null;
		   }
	   }
	   return result;
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
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param genID
    * @return null 表示没有找到相应的GffDetail信息
    */
   private GffDetailGene getGffDetailGenID(String genID) {
	   return hashGenID2GffDetail.get(genID);
   }
   /**
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param genID
    * @return null 表示没有找到相应的GffDetail信息
    */
   private GffDetailGene getGffDetailRnaID(String rnaID) {
	   String genID = hashRnaID2GeneID.get(rnaID);
	   return getGffDetailGenID(genID);
   }
   /**
    * 这里的genID不是我们数据库里面的geneID，而是NCBI gff所特有的ID
    * @param genID
    * @return null 表示没有找到相应的GffDetail信息
    */
   private GffGeneIsoInfo getGffIso(String rnaID) {
	   String rnaName = hashRnaID2RnaName.get(rnaID);
	   GffDetailGene gffDetailGene = null;
	   if (rnaName == null) {
		   hashRnaID2GeneID.put(rnaID, rnaID);
		   gffDetailGene = getGffDetailGenID(rnaID);
		   hashRnaID2RnaName.put(rnaID, gffDetailGene.getNameSingle());
		   rnaName = gffDetailGene.getNameSingle();
		   gffDetailGene.addsplitlist(gffDetailGene.getNameSingle(), GffGeneIsoInfo.TYPE_GENE_NCRNA);
	   }
	   else {
		   gffDetailGene = getGffDetailRnaID(rnaID);
	   }
	   return gffDetailGene.getIsolist(rnaName);		
	}
   /**
    * 将locGff中的信息整理然后装入ChrHash中
    */
   private void setGffList() {
	   mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
	   ListGff LOCList = null;
	   for (GffDetailGene gffDetailGene : hashGenID2GffDetail.values()) {
			 //新的染色体
		   if (!mapChrID2ListGff.containsKey(gffDetailGene.getParentName())) { //新的染色体 
			   LOCList = new ListGff();//新建一个LOCList并放入Chrhash
			   LOCList.setName(gffDetailGene.getParentName());
			   mapChrID2ListGff.put(gffDetailGene.getParentName(), LOCList);
		   }
		   LOCList.add(gffDetailGene);
	   }

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
