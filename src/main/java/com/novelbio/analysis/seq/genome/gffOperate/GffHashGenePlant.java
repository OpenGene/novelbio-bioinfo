package com.novelbio.analysis.seq.genome.gffOperate;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.database.model.modgeneid.GeneType;

/**
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
public class GffHashGenePlant extends GffHashGeneAbs{
	private static Logger logger = Logger.getLogger(GffHashGenePlant.class);

	/** 基因名字的正则 */
	protected String GeneName = "";
	/** 可变剪接mRNA的正则 */
	protected String splitmRNA = "";
	/** 可变剪接mRNA的正则 */
	protected String splitmRNAParent = "(?<=Parent\\=)[\\w\\-%\\:\\.\\{\\}]+";
	/** gene类似名 */
	private static HashSet<String> hashgene = new HashSet<String>();
	
	/**
	 * @param gfffilename
	 * @param Species 水稻还是拟南芥，只有这两个选择 在Species类中的常量
	 * Species.ARABIDOPSIS和DB.equals(Species.RICE
	 * @throws Exception
	 */
	public GffHashGenePlant(GffType gffType) {
		if (gffType == GffType.Plant) {
			GeneName= "(?<=Name\\=)[\\w\\-%\\:\\.\\{\\}]+";
			splitmRNA= "(?<=Name\\=)[\\w\\-%\\:\\.\\{\\}]+";
		}
		else if (gffType == GffType.TIGR) {
			GeneName = "(?<=Alias\\=)[\\w\\-%\\:\\.\\{\\}]+";
			splitmRNA = "(?<=Alias\\=)[\\w\\-%\\:\\.\\{\\}]+";
		}
	}

	/** 设定mRNA和gene的类似名，在gff文件里面出现的 */
	private void setHashName() {
		if (hashgene.isEmpty()) {
			hashgene.add("gene");
			hashgene.add("transposable_element_gene");
			hashgene.add("transposable_element");
			hashgene.add("pseudogene");
		}
	}

   protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
	   setHashName();
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
	   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename, false);
	   ListGff LOCList = null;
	   
	   PatternOperate patGeneName = new PatternOperate(GeneName, false);
	   PatternOperate patMRNAname = new PatternOperate(splitmRNA, false);
	   PatternOperate patMRNAParentName = new PatternOperate(splitmRNAParent, false);
	   
	   String chrIDtmp = "";
	   boolean mRNAconclusion = false;//是否结束了一个mRNA
	   
	   GeneType geneType = null;//是否为transposon element
	   GffDetailGene gffDetailLOC= null;
	   for (String content : txtgff.readlines()) {
		   if(content.length() == 0 || content.charAt(0)=='#') {
			   continue;
		   }
		   ////////////////// 需要进行替换的地方 /////////////////////////////////////////////////////////////
		   if (geneType != GeneType.mRNA) {
			   content = content.replace("pseudogenic_exon", "CDS");
			   content = content.replace("exon", "CDS");
		   }
		   String[] ss = content.split("\t");//按照tab分开
		   ss[8] = HttpFetch.decode(ss[8]);
		   chrIDtmp = ss[0];//小写的chrID
		   String chrIDtmpLowCase = chrIDtmp.toLowerCase();
		   //新的染色体
		   if (!mapChrID2ListGff.containsKey(chrIDtmpLowCase)) {
			   LOCList = new ListGff();//新建一个LOCList并放入Chrhash
			   LOCList.setName(chrIDtmp);
			   mapChrID2ListGff.put(chrIDtmpLowCase, LOCList);
		   } else {
			   LOCList = mapChrID2ListGff.get(chrIDtmpLowCase);
		   }
		   /**
		    * 当读取到gene时，就是读到了一个新的基因，那么将这个基因的起点，终点和每个CDS的长度都放入list数组中
		    */
		   if (hashgene.contains(ss[2])) {
			   geneType = GeneType.getGeneType(ss[2]);
			   //TODO geneType可能为null
			   if (gffDetailLOC != null) {
				   for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailLOC.getLsCodSplit()) {
					   if (gffGeneIsoInfo.isEmpty()) {
						   gffGeneIsoInfo.addExonNorm(ss[6].equals("+") || ss[6].equals("."), gffDetailLOC.getStartCis(), gffDetailLOC.getEndCis());
						   logger.error("该基因没有exon，设定其exon为基因长度：" + gffGeneIsoInfo.getName());
					   }
				   }
				   gffDetailLOC.combineExon();
				  
				   mRNAconclusion = false;// 全新的基因，将其归位false\
			   }
			   gffDetailLOC = null;//清空
			   
			   String geneName = patGeneName.getPatFirst(content);//查找基因名字
			   if(geneName != null) {
				   gffDetailLOC = new GffDetailGene(LOCList, geneName, ss[6].equals("+") || ss[6].equals("."));//新建一个基因类
				   gffDetailLOC.setTaxID(taxID);
				   gffDetailLOC.setStartAbs(  Integer.parseInt(ss[3].toLowerCase()) ); gffDetailLOC.setEndAbs( Integer.parseInt(ss[4]));//基因起止      		
				   LOCList.add(gffDetailLOC);//添加进入LOClist
			   }
			   else {
				   logger.error("GffHashPlantGeneError: 文件  "+gfffilename+"  在本行可能没有指定的基因ID  " + " " +content);
			   }
			   continue;
		   }
		   
		   if (gffDetailLOC == null) {
			   continue;
		   }
		   /**
      	    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
      	    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
      	    * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
      	    */
		   if (GeneType.getMapMRNA2GeneType().containsKey(ss[2].toLowerCase())) {
			   //如果刚刚读取的是一个mRNA的话
			   if (mRNAconclusion) {
				   gffDetailLOC.combineExon();
				   mRNAconclusion =false;
			   }
			   
			   String mRNAname = patMRNAname.getPatFirst(ss[8]);
			   if (mRNAname == null) {
				   mRNAname = patMRNAParentName.getPatFirst(ss[8]);
			   }
			   
			   if(mRNAname != null) {
				   if (geneType == null || geneType == GeneType.mRNA) {
					   geneType = GeneType.getGeneType(ss[2]);
				   }
				   gffDetailLOC.addsplitlist(mRNAname, gffDetailLOC.getNameSingle(), geneType);
			   } else {
				   logger.error("GffHashPlantGeneError: 文件  "+gfffilename+"  在本行可能没有指定的基因ID  " + content);
			   }
		   }
		   
		   //遇到5UTR
		   else if (ss[2].equals("five_prime_UTR") || ss[2].equals("5'-UTR") || ss[2].equals("three_prime_UTR") || ss[2].equals("3'-UTR")) {
			   int start = Integer.parseInt(ss[3]), end = Integer.parseInt(ss[4]);
			   gffDetailLOC.addExonNorm(ss[6].equals("+") || ss[6].equals("."), start, end);
			   mRNAconclusion = true;
		   } else if (ss[2].equals("CDS")) {
			   int start = Integer.parseInt(ss[3]), end = Integer.parseInt(ss[4]);
			   gffDetailLOC.addExonNorm(ss[6].equals("+") || ss[6].equals("."), start, end);
			   gffDetailLOC.setATGUAG(start, end);
			   mRNAconclusion = true;
		   } else if (!ss[2].equals("protein") && !ss[2].equals("exon") && !ss[2].equals("intron")) {
			   logger.error(ss[2]);
		   }
	   }
	   if (gffDetailLOC != null) {
		   for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailLOC.getLsCodSplit()) {
			   if (gffGeneIsoInfo.isEmpty()) {
				   gffGeneIsoInfo.addExonNorm(null, gffDetailLOC.getStartCis(), gffDetailLOC.getEndCis());
				   logger.error("该基因没有exon，设定其exon为基因长度：" + gffGeneIsoInfo.getName());
			   }
			   gffDetailLOC.combineExon();
		   }
	   }

	   
	   LOCList.trimToSize();
	   txtgff.close();
   }
}
