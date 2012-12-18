package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 解析samtools产生的pile up信息，将每一行生成一个本类，专门存储堆叠信息
 * 有设定flag就当snp，没有设定flag就当indel
 * @author zong0jie
 *
 */
public class RefSiteSnpIndel implements Comparable<RefSiteSnpIndel>, Cloneable{
	private static Logger logger = Logger.getLogger(RefSiteSnpIndel.class);
	protected static final String SampleDefaultName = "sample";
	/** 
	 * <b>里面都是正向的序列</b>
	 * 该位置可能有不止一种的插入缺失或是碱基替换类型，那么就用该hash表来存储这么多种信息<br>
	 *Key: referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType <br>
	 * value: SiteSnpIndelInfo 类  */
	Map<String, SiteSnpIndelInfo> mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();
	/** 样本和正常reads之间的关系 */
	Map<String, SampleRefReadsInfo> mapSample2NormReadsInfo = new HashMap<String, SampleRefReadsInfo>();

	protected String chrID;
	String refBase = "";
	/** snp在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处  
	 * -1表示没有该项目
	 * */
	double prop = -1;
	/** 本snp或indel所在的起点 */
	protected int refSnpIndelStart = 0;
	/**
	 * snp或indel所在的转录本
	 */
	GffGeneIsoInfo gffGeneIsoInfo;
	GffChrAbs gffChrAbs;
		String sampleName = SampleDefaultName;
	
	public RefSiteSnpIndel() {}
	/**
	 * @param gffChrAbs
	 * @param sampleName
	 */
	public RefSiteSnpIndel(GffChrAbs gffChrAbs, String sampleName) {
		this.gffChrAbs = gffChrAbs;
		setSampleName(sampleName);
	}
	/**
	 * @param gffChrAbs
	 * @param chrID
	 * @param refSnpIndelStart
	 */
	public RefSiteSnpIndel(GffChrAbs gffChrAbs,String chrID, int refSnpIndelStart) {
		this.gffChrAbs = gffChrAbs;
		this.chrID = chrID;
		this.refSnpIndelStart = refSnpIndelStart;
	    setGffIso();
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null) {
			return;
		}
		if (this.gffChrAbs != null && this.gffChrAbs.getSpecies() != null && this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setRefSnpIndelStart(String chrID, int refSnpIndelStart) {
		this.chrID = chrID;
		this.refSnpIndelStart = refSnpIndelStart;
		setGffIso();
	}

	/**
	 * 设定样本名，必须在最早的时候设定，这样所有后期的信息都会添加到该sample中
	 * @param sampleName null就不设定
	 */
	public void setSampleName(String sampleName) {
		if (sampleName == null) {
			return;
		}
		this.sampleName = sampleName;
	}
	
	public boolean isContainsSample(String sampleName) {
		return mapSample2NormReadsInfo.containsKey(sampleName);
	}
	/**
	 * AD Allelic depths for the ref and alt alleles in the order listed
	 * @return
	 */
	public int getReadsNumRef() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getAllelic_depths_Ref();
	}
	/**
	 * GQ The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT.
	 *  In diploid case, if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood 
	 *  of the NGS sequencing data under the model of that the sample is 0/0, 0/1/, or 1/1. 好像是碱基的质量
	 * @return
	 */
	public double getGenotype_Quality() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.Genotype_Quality;
	}
	
	public int getReadsNumAll() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getReadsDepth();
	}
	/**
	 * SB, How much evidence is there for Strand Bias (the variation being seen
	 *  on only the forward or only the reverse strand) in the reads? Higher SB 
	 *  values denote more bias (and therefore are more likely to indicate false 
	 *  positive calls).
	 * @return
	 */
	public double getStrand_Bias() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getStrand_Bias();
	}
	/**
	 * 物种ID
	 * @return
	 */
	public int getTaxID() {
		if (gffChrAbs == null) {
			return 0;
		}
		return gffChrAbs.getTaxID();
	}
	public void setRefBase(String refBase) {
		this.refBase = refBase;
	}
	/**
	 * snp在基因长度的百分比
	 * 越小越靠近头部
	 * -1表示没有该项目
	 */
	public double getProp() {
		return prop;
	}
	/**
	 * 参考序列
	 * @return
	 */
	public String getRefBase() {
		return refBase;
	}
	/**
	 * 获得snp或indel在ref上的起点，实际位点
	 * @return
	 */
	public int getRefSnpIndelStart() {
		return refSnpIndelStart;
	}
	/**
	 * 获得所在的转录本
	 * @return
	 */
	public GffGeneIsoInfo getGffIso() {
		setGffIso();
		return gffGeneIsoInfo;
	}
	/**
	 * 判断另一个snp或者indel是不是与本mapInfo在同一个转录本中
	 * 两个refSiteSnpIndel都必须有gffGeneIsoInfo设置好
	 * @param refSiteSnpIndel
	 * @return
	 */
	public boolean isSameIso(RefSiteSnpIndel RefSiteSnpIndel) {
		if (gffGeneIsoInfo != null && gffGeneIsoInfo.equals(RefSiteSnpIndel.getGffIso())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据设定的列信息，填充refSiteSnpIndel信息
	 * 仅仅读取snp信息，不读取多少snp信息
	 */
	public void setNBCLines(String sampleName, String novelBioLine) {
		setSampleName(sampleName);
		String[] inputLines = novelBioLine.split("\t");
		//TODO :chrID是否需要小写
		chrID = inputLines[0];
		refSnpIndelStart = Integer.parseInt(inputLines[1]); 
		setGffIso();
		getAndAddAllenInfo(inputLines[6], inputLines[7]);
		
	}

	/**
	 * 根据设定的列信息，填充refSiteSnpIndel信息
	 */
	public void setVcfLines(String sampleName, VcfCols vcfCols, String vcfLines) {
		setSampleName(sampleName);
		String[] inputLines = vcfLines.split("\t");
		refSnpIndelStart = Integer.parseInt(inputLines[vcfCols.colSnpStart]); 
		//TODO :chrID是否需要小写
		chrID = inputLines[vcfCols.colChrID];
		setGffIso();
		SiteSnpIndelInfo siteSnpIndelInfo = getAndAddAllenInfo(inputLines[vcfCols.colRefsequence], inputLines[vcfCols.colThisSequence]);
		if (vcfCols.colBaseInfo >= 0)
			setBaseInfo(inputLines[vcfCols.colBaseInfo]);
		if (vcfCols.colQuality >= 0)
			siteSnpIndelInfo.setQuality(inputLines[vcfCols.colQuality]);
		if (vcfCols.colFiltered >= 0)
			siteSnpIndelInfo.setVcfFilterInfo(inputLines[vcfCols.colFiltered]);
		if (vcfCols.colFlagTitle >= 0 && vcfCols.colFlagDetail >= 0) {
			setFlag(inputLines[vcfCols.colFlagTitle], inputLines[vcfCols.colFlagDetail]);
			setDepthAlt(siteSnpIndelInfo, inputLines[vcfCols.colFlagTitle], inputLines[vcfCols.colFlagDetail]);
		}
		if (vcfCols.colSnpDBID>=0) {
			if (!inputLines[vcfCols.colSnpDBID].equals(".")) {
				siteSnpIndelInfo.setDBSnpID(inputLines[vcfCols.colSnpDBID]);
			}
		}
	}
	/**
	 * 就看这三项：AF,AN,SB
	 *  AB=0.841;AC=1;AF=0.50;AN=2;BaseQRankSum=0.097;DP=63;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;
	 *  给定GATKinfo，设定信息
	 * @param GATKInfo
	 */
	private void setBaseInfo(String GATKInfo) {
		String[] ssValue = GATKInfo.split(";");
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		for (String string : ssValue) {
			String[] tmpInfo = string.split("=");
			if (tmpInfo[0].equals("SB")) {
				sampleRefReadsInfo.setStrand_Bias(Double.parseDouble(tmpInfo[1]));
			}
		}
	}
	private SampleRefReadsInfo getAndCreateSampleRefReadsInfo() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			sampleRefReadsInfo = new SampleRefReadsInfo();
			mapSample2NormReadsInfo.put(sampleName, sampleRefReadsInfo);
		}
		return sampleRefReadsInfo;
	}
	
	/** 设定vcf中的reads depth那个列，主要是设定从vcf中读取的reads depth信息<br>
	 * "GT:AD:DP:GQ:PL", <br>
	 * "0/1:119,100:315:99:3214,0,3784"<br>
	 *  */
	private void setDepthAlt(SiteSnpIndelInfo sampleRefReadsInfo, String flagTitle, String flagDetail) {
		//TODO 这里我删除了一个Allelic_depths_Alt的项目，考虑如何很好的添加进去
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				sampleRefReadsInfo.setThisReadsNum(Integer.parseInt(info[1]));
			}
		}
	}
	
	/**
	 * 这里我删除了一个Allelic_depths_Alt的项目，考虑如何很好的添加进去
	 * 设置<br>
	 * GT:AD:DP:GQ:PL<br>
	 * 0/1:53,10:63:99:150,0,673<br>
	 */
	private void setFlag(String flagTitle, String flagDetail) {
		//TODO 这里我删除了一个Allelic_depths_Alt的项目，考虑如何很好的添加进去
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				sampleRefReadsInfo.setRefDepth(Integer.parseInt(info[0]));
			}
			else if (ssFlag[i].equals("DP")) {
				sampleRefReadsInfo.setReadDepth( Integer.parseInt(ssValue[i]));
			}
			else if (ssFlag[i].equals("GQ")) {
				sampleRefReadsInfo.setGenotype_Quality(Double.parseDouble(ssValue[i]));
			}
			else if (ssFlag[i].equals("GQ")) {
				
			}
		}
	}
	
	/** 这是当samPileup文件跳过某一行时设定的，表示已经查过这一行了只不过没找到东西
	 * 然后各种值会设定为0
	 *  */
	public void setSearchSamPileUpFileTrue() {
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.setSearchSampileupFile(true);
	}
	
	/**
	 *  在已有refbase信息的基础上，查找该refSnpIndelStart位点有哪些indel或snp
	 *  找到的indel所对应的refbase可能和原来的refbase不一样
	 * @param samString
	 */
	public void setSamToolsPilup(String samString) {
		String[] ss = samString.split("\t");
		this.chrID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//本行舍不设定都无所谓，因为输入的时候就是要求相同的ID
		this.refBase = ss[2];
		setGffIso();
		setAllenInfo(Integer.parseInt(ss[3]), ss[4]);
	}

	/**
	 * snp或indel所在的转录本
	 * 同时设定setProp，cis5to3，和name，都用gffGeneIsoInfo的信息
	 */
	private void setGffIso() {
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null || (gffGeneIsoInfo != null && prop >= 0))
			return;

		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(chrID, refSnpIndelStart);
		this.gffGeneIsoInfo = gffCodGene.getCodInCDSIso();
		if (gffGeneIsoInfo == null && gffCodGene.isInsideLoc()) {
			gffGeneIsoInfo = gffCodGene.getGffDetailThis().getLongestSplitMrna();
		}
		if (gffGeneIsoInfo == null) {
			return;
		}
		setProp();
	}
	/**
	 * refBase在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处
	 * 越小越靠近头部
	 * 0-1之间
	 */
	private void setProp() {
		if (gffGeneIsoInfo.getCodLoc(getRefSnpIndelStart()) != GffGeneIsoInfo.COD_LOC_EXON) {
			return;
		}
		this.prop = (double)gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())
				/ 
				(gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())  - gffGeneIsoInfo.getCod2TESmRNA(getRefSnpIndelStart()));
	}
	
	public String getRefID() {
		return chrID;
	}
	
	/**
	 * 重新设定Allelic_depths_Ref，和hashAlle信息
	 *  给定samtools产生的pile up那个pileup信息，计算该位点的堆叠情况<br>
	 * 格式如下<br> ...........,.............,....,....,.,.,..,..,...,....,.^!.<br>解释:<br>
	 *  <b>.</b> :match to the reference base on the forward strand<br>
	 *  <b>,</b> :match on the reverse strand, <br>
	 *  <b>’>’</b> or<b> ’<’ </b> :a reference skip<br>
	 *  <b>‘ACGTN’ </b> :mismatch on the forward strand<br> 
	 *  <b>‘acgtn’</b> :mismatch on the reverse strand<br>
	 *  <b> ‘\+[0-9]+[ACGTNacgtn]+’</b> :insertion between this reference position and the next reference position.
	 *  The length of the insertion is given by the integer in the pattern, followed by the inserted sequence.<br>
	 *  <b>‘-[0-9]+[ACGTNacgtn]+’</b> represents a deletion from the reference. The deleted bases will be presented as<b> ‘*’</b> in the following lines. 
	 *  <b>‘^’</b>the start of a read. The ASCII of the character following ‘^’ minus 33 gives the mapping quality. 
	 *  <b>‘$’</b> marks the end of a read segment.
	 * @param pileUpInfo 输入 ...........,.............,....,....,.,.,..,..,...,....,.^!. 这种东西
	 */
	private void setAllenInfo(int readsDepth, String pileUpInfo) {
		clearSampleReadsNum();
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.setSearchSampileupFile(true);
		sampleRefReadsInfo.setReadDepth(readsDepth);
		String thisSeq = refBase;
		char[] pipInfo = pileUpInfo.toCharArray();
		for (int i = 0; i < pipInfo.length; i++) {
			char c = pipInfo[i];
			if (c == '$' || c == '<' || c == '>') continue;
			if (c == '^' ) {
				i ++; continue;//^后面是mapping质量，所以跳过
			}
			else if (c == 'n' || c== 'N') {//不确定的错配不理会
				if (isNextSiteIndel(pipInfo, i)) {//后面是indel才将thisSeq设定为N，否则直接跳过
					thisSeq = "N";
				}
				continue;
			}
			else if (c == ',' || c == '.') {
				if (!isNextSiteIndel(pipInfo, i)) {
					sampleRefReadsInfo.addRefDepth(1); continue;
				}
			}
			else if (c == '+' || c == '-') {
				i = setIndel(thisSeq, c, pipInfo, i);
				thisSeq = refBase;//复位reference
			}
			else if (c == '*') {
				continue;
			}
			//mismatch
			else {
				thisSeq = setMisMatchAndGetRefBase(pipInfo, i);
			}
		}
	}
	
	/** 有些已经在vcf里面查过的snp会有该snp的depth信息，所以这里先清空本样本所有snp数值 */
	private void clearSampleReadsNum() {
		for (SiteSnpIndelInfo siteSnpIndelInfo : mapAllen2Num.values()) {
			siteSnpIndelInfo.setSampleName(sampleName);
			siteSnpIndelInfo.setThisReadsNum(0);
		}
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.readDepth = 0;
		sampleRefReadsInfo.Allelic_depths_Ref = 0;
	}
	private boolean isNextSiteIndel(char[] pipInfo, int thisIndex) {
		int nextIndex = thisIndex + 1;
		if (nextIndex < pipInfo.length && (pipInfo[nextIndex] == '+' || pipInfo[nextIndex] == '-'))
			return true;
		return false;
	}
	/** 设定indel信息，同时返回结果的index
	 * 也就是输入的pipInfo读取到的位点
	 * @param thisSeq
	 * @param indelSymbol
	 * @param pipInfo
	 * @param index
	 * @return
	 */
	private int setIndel(String thisSeq, char indelSymbol, char[] pipInfo, int index) {
		String referenceSeq = refBase;
		int tmpInDelNum = 0;
		index ++;
		//如果开头是“+”号，则获得+号后的数字，也就是indel的长度
		for (; index < pipInfo.length; index++) {
			char tmpNum = pipInfo[index];
			//转换为数字字符
			if (tmpNum >= 48 && tmpNum <=57) {
				tmpInDelNum = tmpInDelNum*10 + tmpNum -  48;
			}
			else {//不是字符说明读过头了，就返回一位
				index--;
				break;
			}
		}
		//获得具体的字符
		char[] tmpSeq = new char[tmpInDelNum];
		for (int j = 0; j < tmpSeq.length; j++) {
			index++;
			tmpSeq[j] = pipInfo[index];
		}
		String indel = String.copyValueOf(tmpSeq);
		if (indelSymbol == '+') {
			thisSeq = thisSeq + indel;
		} else {
			referenceSeq = referenceSeq + indel;
		}
		
		addSnpIndel2Map(referenceSeq, thisSeq, 1);
		return index;
	}
	
	/** 设定错配位点，如果错配位点后面还跟着插入或缺失，则直接返回。<br>
	 * 否则就将错配装入snpInfo
	 * @param referenceSeq
	 * @param pipInfo
	 * @param thisIndex
	 * @return 如果是Indel，返回该位点错配的碱基序列
	 * 如果不是Indel，返回refbase
	 */
	private String setMisMatchAndGetRefBase(char[] pipInfo, int thisIndex) {
		String thisSeq = pipInfo[thisIndex] + "";
		if (isNextSiteIndel(pipInfo, thisIndex)) {
			return thisSeq;
		}
		addSnpIndel2Map(refBase, thisSeq, 1);
		return refBase;
	}
	
	/** 给定snp的refsequence和thisSeq，将错配信息加入mapAllen2Num中 */
	private SiteSnpIndelInfo addSnpIndel2Map(String referenceSeq, String thisSeq, int snpNum) {
		String snpIndelInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
		SiteSnpIndelInfo siteSnpIndelInfo = null;
		
		if (mapAllen2Num.containsKey(snpIndelInfo)) {
			siteSnpIndelInfo = mapAllen2Num.get(snpIndelInfo);
			siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
			siteSnpIndelInfo.addThisBaseNum(snpNum);
		}
		else {
			siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, referenceSeq, thisSeq);
			siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
			siteSnpIndelInfo.setThisReadsNum(snpNum);
			if (siteSnpIndelInfo.getSnpIndelType() != SnpIndelType.CORRECT) {
				mapAllen2Num.put(snpIndelInfo, siteSnpIndelInfo);
			}
		}
		return siteSnpIndelInfo;
	}
	
	/**
	 * 给定一系列siteSnpIndel位点，将其设定到本RefSite中。
	 * <b>本对象中的mapAllen2Num会完全被替换掉</b>
	 * <br>
	 * <br>
	 * 用途:<br>
	 * 当用snpFilter过滤一个refSiteSnpIndel对象后，可以获得一系列的位点。
	 * 可以将这些位点设置到该refSiteSnpIndel的clone中去
	 * @param lsSiteSnpIndelInfo
	 */
	public void setLsSiteSnpIndelInfo(ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfo) {
		mapAllen2Num.clear();
		for (SiteSnpIndelInfo siteSnpIndelInfo : lsSiteSnpIndelInfo) {
			siteSnpIndelInfo.refSiteSnpIndelParent = this;
			mapAllen2Num.put(siteSnpIndelInfo.getMismatchInfo(), siteSnpIndelInfo);
		}
	}
	/** 将错配类型位点加入mapAllen2Num，同时返回产生的siteSnpIndelInfo
	 * <b>如果没有错配，则不加入mapAllen2Num</b>
	 *  */
	public SiteSnpIndelInfo getAndAddAllenInfo(String referenceSeq, String thisSeq) {
		return addSnpIndel2Map(referenceSeq, thisSeq, 0);
	}
	/**
	 * 将另一个refSiteSnpIndel的所有snpIndel信息装入本类，那么如果遇到相同的snpIndel就仅记载该样本这个snp的数量信息
	 * @param refSiteSnpIndel
	 */
	public void addAllenInfo(RefSiteSnpIndel refSiteSnpIndel) {
		Collection<SiteSnpIndelInfo> colSiteSnpIndelInfosInput = refSiteSnpIndel.mapAllen2Num.values();
		for (SiteSnpIndelInfo siteSnpIndelInfoInput : colSiteSnpIndelInfosInput) {
			SiteSnpIndelInfo siteSnpIndelInfoThis = mapAllen2Num.get(siteSnpIndelInfoInput.getMismatchInfo());
			if (siteSnpIndelInfoThis == null) {
				mapAllen2Num.put(siteSnpIndelInfoInput.getMismatchInfo(), siteSnpIndelInfoInput);
			}
			else {
				siteSnpIndelInfoThis.addSiteSnpIndelInfo(siteSnpIndelInfoInput);
			}
		}
		for (Entry<String, SampleRefReadsInfo> entry : refSiteSnpIndel.mapSample2NormReadsInfo.entrySet()) {
			if (mapSample2NormReadsInfo.containsKey(entry.getKey())) {
				continue;
			}
			mapSample2NormReadsInfo.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 给定refSiteSnpIndel，根据其<b>ref</b>,<b>refbase</b>，<b>thisbase</b>和<b>indel</b>的type，查找本位置某种type indel的数量。<br>
	 * 注意，输入的refSiteSnpIndel必须只能有一种type。也就是只能指定一种形式的错配，<br>
	 * 此外输入的indel在查找的时候会将第一位删除，因为GATK出来的第一位是indel的前一位<br>
	 * 返回该种形式错配以及相应序列所含有的reads堆叠数
	 * 从hash表中获得
	 * @param refSiteSnpIndelParent 正常的别的样本的信息
	 * @return 如果输入的refSiteSnpIndelQuery中没有错配的信息，则返回null
	 */
	public SiteSnpIndelInfo getSnpIndelNum(RefSiteSnpIndel refSiteSnpIndelQuery) {
		if (refSiteSnpIndelQuery.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("输入的查找位点不是同一个，本位点：" + getRefSnpIndelStart() + "查找位点：" + refSiteSnpIndelQuery.getRefSnpIndelStart());
			return null;
		}
		SiteSnpIndelInfo siteSnpIndelInfoQuery = refSiteSnpIndelQuery.getSiteSnpInfoBigAllen();
		return getSnpIndel(siteSnpIndelInfoQuery);
	}
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 因为本位点可能有多种错配，所以给定一个然后查找，看能找到几个
	 * 从hash表中获得
	 * @param referenceSeq
	 * @param thisSeq
	 * @param snpType
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndel(SiteSnpIndelInfo siteSnpIndelInfo) {
		if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.CORRECT) {
			logger.error("输入的查找位点没有错配信息，本位点：" + getRefSnpIndelStart());
			return null;
		}
		return getSnpIndel(siteSnpIndelInfo.referenceSeq, siteSnpIndelInfo.thisSeq);
	}
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 因为本位点可能有多种错配，所以给定一个然后查找，看能找到几个
	 * 从hash表中获得
	 * @param referenceSeq
	 * @param thisSeq
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndel(String referenceSeq, String thisSeq) {
		String tmpInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
		SiteSnpIndelInfo siteSnpIndelInfo = mapAllen2Num.get(tmpInfo);
		if (siteSnpIndelInfo == null) {
			siteSnpIndelInfo = getSiteSnpIndelInfoNone(referenceSeq, thisSeq);
		}
		if (siteSnpIndelInfo != null) {
			siteSnpIndelInfo.setSampleName(sampleName);
		}
		return siteSnpIndelInfo;
	}
	/**
	 * 返回数量最大的snp位点
	 * 已经设定了sampleName
	 */
	public SiteSnpIndelInfo getSiteSnpInfoBigAllen() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = getLsAllenInfoSortBig2Small();
		if (lsAllenInfo.size() > 0) {
			return lsAllenInfo.get(0);
		}
		return SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, refBase, refBase);
	}
	/**
	 * 返回所有的非ref的基因以及对应的种类和数量
	 * 每个都设定sampleName
	 * 如果没有SiteSnp，则返回本项
	 */
	public ArrayList<SiteSnpIndelInfo> getLsAllenInfoSortBig2Small() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		for (SiteSnpIndelInfo siteSnpIndelInfo : lsAllenInfo) {
			siteSnpIndelInfo.setSampleName(sampleName);
		}
		Collections.sort(lsAllenInfo, new compRefSiteSnpIndelBig2Small(sampleName));
		return lsAllenInfo;
	}
	//TODO check
	/** 根据是否查找过samPileUp文件，返回空值或是设定为0的SiteSnpIndelInfo */
	private SiteSnpIndelInfo getSiteSnpIndelInfoNone(String refSequence, String thisSequence) {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return null;
		}
		if (sampleRefReadsInfo.isSearchSampileupFile() == true) {
			SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, refSequence, thisSequence);
			siteSnpIndelInfo.setSampleName(sampleName);
			siteSnpIndelInfo.setThisReadsNum(0);
			return siteSnpIndelInfo;
		}
		else {
			return null;
		}
	}
	/**
	 * 给定refSiteSnpIndel，根据其<b>ref</b>,<b>refbase</b>，<b>thisbase</b>和<b>indel</b>的type，查找本位置某种type indel的数量。<br>
	 * 注意，输入的refSiteSnpIndel必须只能有一种type。也就是只能指定一种形式的错配，<br>
	 * 此外输入的indel在查找的时候会将第一位删除，因为GATK出来的第一位是indel的前一位<br>
	 * 返回该种形式错配以及相应序列所含有的reads堆叠数
	 * 从hash表中获得
	 * @param refSiteSnpIndel 正常的别的样本的信息
	 * @return 返回描述性的话:<br>
	 * refID \t  refStart \t refBase  \t  depth \t indelBase  \t indelNum   <br>
	 * 出错返回"";
	 */
	public String getSeqTypeNumStr(RefSiteSnpIndel refSiteSnpIndel) {
		SiteSnpIndelInfo siteSnpIndelInfoQuery = refSiteSnpIndel.getSiteSnpInfoBigAllen();
		return getSeqTypeNumStr(siteSnpIndelInfoQuery);
	}
	/**
	 * 给定refSiteSnpIndel，根据其<b>ref</b>,<b>refbase</b>，<b>thisbase</b>和<b>indel</b>的type，查找本位置某种type indel的数量。<br>
	 * 注意，输入的refSiteSnpIndel必须只能有一种type。也就是只能指定一种形式的错配，<br>
	 * 此外输入的indel在查找的时候会将第一位删除，因为GATK出来的第一位是indel的前一位<br>
	 * 返回该种形式错配以及相应序列所含有的reads堆叠数
	 * 从hash表中获得
	 * @param SampleName
	 * @param SiteSnpIndelInfo 正常的别的样本的信息
	 * @return 返回描述性的话:<br>
	 * refID \t  refStart \t refBase  \t  depth \t indelBase  \t indelNum   <br>
	 * 出错返回"";
	 */
	public String getSeqTypeNumStr(SiteSnpIndelInfo siteSnpIndelInfoQuery) {
		SiteSnpIndelInfo siteSnpIndelInfo = getSnpIndel(siteSnpIndelInfoQuery);
		if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.CORRECT) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + siteSnpIndelInfoQuery.getReferenceSeq()+"\t" +getReadsNumRef();
		tmpResult = tmpResult + "\t" +siteSnpIndelInfo.getThisSeq() + "\t" + siteSnpIndelInfo.getReadsNum();
		return tmpResult;
	}
	
	/**
	 * 返回全部snp类型和样本的信息
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp() {
		return toStringLsSnp(null,false);
	}
	/**
	 * 返回挑选出的snp类型和样本的信息
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(ArrayList<SiteSnpIndelInfo> lsMismatchInfo) {
		return toStringLsSnp(null,false, lsMismatchInfo);
	}
	/**
	 * 给定样本名，返回全部snp类型和样本的信息
	 * @param lsSampleNames 样本名
	 * @param getGATK 是否仅将GATK认定的snp提取出来
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(Collection<String> lsSampleNames, boolean getGATK) {
		return toStringLsSnp(lsSampleNames, getGATK, new ArrayList<SiteSnpIndelInfo>());
	}
	/**
	 * @param lsSampleNames
	 * @param getGATK
	 * @param lsMismatchInfo 仅选出指定的snp size为0则返回本位点全体snp和indel，如果为空表示获取全部，不能为null
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(Collection<String> lsSampleNames, boolean getGATK, ArrayList<SiteSnpIndelInfo> lsMismatchInfo) {
		HashSet<String> setSnpSite = new HashSet<String>();
		for (SiteSnpIndelInfo siteSnpIndelInfo : lsMismatchInfo) {
			setSnpSite.add(siteSnpIndelInfo.getMismatchInfo());
		}
		return getStringLsSnp(lsSampleNames,false, setSnpSite);
	}
	/**
	 * 给定样本名，返回全部snp类型和样本的信息
	 * @param lsSampleNames 样本名, 全部就选null
	 * @param getGATK 是否仅将GATK认定的snp提取出来 没有GATK就选false
	 * @param setMismatchInfo 仅选出指定的snp size为0则返回本位点全体snp和indel
	 * @return
	 */
	private ArrayList<String[]> getStringLsSnp(Collection<String> lsSampleNames, boolean getGATK, Set<String> setMismatchInfo) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		LinkedList<String> lsResultTmp = new LinkedList<String>();
		lsResultTmp.add(chrID);//0
		lsResultTmp.add(refSnpIndelStart + "");//1
		
		if (gffGeneIsoInfo != null) {
			lsResultTmp.add(gffGeneIsoInfo.getName());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getSymbol());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getDescription());
		}
		else{
			lsResultTmp.add("");
			lsResultTmp.add("");
			lsResultTmp.add("");
		}
		if (prop >= 0)
			lsResultTmp.add(prop + "");
		else
			lsResultTmp.add("");
		
		//对于每个snp的样式
		for (Entry<String, SiteSnpIndelInfo> entry : mapAllen2Num.entrySet()) {
			SiteSnpIndelInfo siteSnpIndelInfo = entry.getValue();
			if (getGATK && !isGATKfiltered(siteSnpIndelInfo))
				continue;
			if (!isFilteredSite(setMismatchInfo, siteSnpIndelInfo))
				continue;
			
			LinkedList<String> lsTmpInfo = copyList(lsResultTmp);
			
			lsTmpInfo.add(siteSnpIndelInfo.getReferenceSeq());
			lsTmpInfo.add(siteSnpIndelInfo.getThisSeq());
			lsTmpInfo.add(siteSnpIndelInfo.getSnpIndelRs().getSnpRsID());
			if (lsSampleNames == null) {
				lsSampleNames = mapSample2NormReadsInfo.keySet();
			}
			for (String sampleName : lsSampleNames) {
				SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
				if (sampleRefReadsInfo == null) {
//					for (int i = 0; i < 4; i++)
//						lsTmpInfo.add("");
					continue;
				}
				siteSnpIndelInfo.setSampleName(sampleName);
				lsTmpInfo.add(sampleRefReadsInfo.getReadsDepth() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getReadsNum() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getVcfInfoFilter());
				lsTmpInfo.add(siteSnpIndelInfo.getQuality());
			}
			
			lsTmpInfo.add(siteSnpIndelInfo.getOrfShift() + "");
			lsTmpInfo.add(siteSnpIndelInfo.isExon() + "");
			if (siteSnpIndelInfo.isCDS()) {
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toStringAA1());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toStringAA1());
				lsTmpInfo.add(siteSnpIndelInfo.getAAattrConvert());
				lsTmpInfo.add(siteSnpIndelInfo.getSplitTypeEffected());
			}
			else {
//				for (int i = 0; i < 6; i++)
//					lsTmpInfo.add("");
			}
			String[] infpoStrings = lsTmpInfo.toArray(new String[0]);
			lsResult.add(infpoStrings);
		}
		return lsResult;
	}

	/** 
	 * 根据给定的样本名，产生title
	 * @param lsSampleNames
	 * @return
	 */
	public static String[] getTitleFromSampleName(Collection<String> lsSampleNames) {
		LinkedList<String> lsTitle = new LinkedList<String>();
		lsTitle.add("ChrID");
		lsTitle.add("Loc");
		lsTitle.add("GeneID");
		lsTitle.add("GeneSymbol");
		lsTitle.add("Description");
		lsTitle.add("Distance2GeneStart");
		lsTitle.add("RefSequence");
		lsTitle.add("ThisSequence");
		lsTitle.add("DBsnpID");
		for (String sampleName : lsSampleNames) {
			lsTitle.add(sampleName + "_ReadsDepth");
			lsTitle.add(sampleName + "_ThisReadsNum");
			lsTitle.add(sampleName + "_FilteredFlag");
			lsTitle.add(sampleName + "_Quality");
		}
		lsTitle.add("OrfShift");
		lsTitle.add("IsInExon");
		lsTitle.add("RefAAnr");
		lsTitle.add("RefAA");
		lsTitle.add("ThisAAnr");
		lsTitle.add("ThisAA");
		lsTitle.add("Chemical Transform");
		lsTitle.add("split info");
		String[] infpoStrings = lsTitle.toArray(new String[0]);
		return infpoStrings;
	}
	/**
	 * 在指定的，已经通过过滤的集合中，查找是否存在某个snp
	 * 存在了返回true
	 * @param setMismatchInfo 指定的通过筛选的snp集合 空集或者size为0直接返回true
	 * @param siteSnpIndelInfo 本个待filter的snp信息
	 * @return
	 */
	private boolean isFilteredSite(Set<String> setMismatchInfo, SiteSnpIndelInfo siteSnpIndelInfo) {
		if (setMismatchInfo.size() == 0) {
			return true;
		}
		else if (setMismatchInfo.contains(siteSnpIndelInfo.getMismatchInfo())) {
			return true;
		}
		return false;
	}
	
	private boolean isGATKfiltered(SiteSnpIndelInfo siteSnpIndelInfo) {
		boolean result = false;
		Map<String, SampleSnpReadsQuality> mapSample2Snp = siteSnpIndelInfo.mapSample2thisBaseNum;
		for (SampleSnpReadsQuality sampleSnpReadsQuality : mapSample2Snp.values()) {
			if (sampleSnpReadsQuality.quality != null && !sampleSnpReadsQuality.quality.equals("")) {
				return true;
			}
		}
		return result;
	}
	
	private LinkedList<String> copyList(List<String> lsSrc) {
		LinkedList<String> lsResult = new LinkedList<String>();
		for (String string : lsSrc) {
			lsResult.add(string);
		}
		return lsResult;
	}

	/**
	 * 用于比较的，从小到大比
	 * 先比refID，然后比start，end，或者比flag或者比score
	 * 比score的时候就不考虑refID了
	 */
	public int compareTo(RefSiteSnpIndel mapInfoOther) {
		int i = chrID.compareTo(mapInfoOther.chrID);
		if (i != 0) {
			return i;
		}
		Integer site1 = refSnpIndelStart;
		Integer site2 = mapInfoOther.refSnpIndelStart;
		return site1.compareTo(site2);
	}
	
	/**
	 * 浅层clone <br>
	 * gffChrAbs和gffGeneIsoInfo
	 * 这两个仅仅传递地址<br><br>
	 * mapAllen2Num和mapSample2NormReadsInfo
	 * 这两个clone了，但是内部元素都是一样的引用。<br>
	 * 也就是说<b>删除</b>mapAllen2Num里面的元素，老的RefSiteSnpIndel<b>不变</b><br>
	 * 但是<b>修改</b>mapAllen2Num里面的元素，老的RefSiteSnpIndel<b>会变</b><br>
	 */
	public RefSiteSnpIndel clone() {
		RefSiteSnpIndel refSiteSnpIndel;
		try {
			//TODO
			refSiteSnpIndel = (RefSiteSnpIndel) super.clone();
			refSiteSnpIndel.gffChrAbs = gffChrAbs;
			refSiteSnpIndel.gffGeneIsoInfo = gffGeneIsoInfo;
			refSiteSnpIndel.chrID = chrID;
			refSiteSnpIndel.prop = prop;
			refSiteSnpIndel.refBase = refBase;
			refSiteSnpIndel.refSnpIndelStart = refSnpIndelStart;
			refSiteSnpIndel.sampleName = sampleName;
			
			refSiteSnpIndel.mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();
			for (String allen : mapAllen2Num.keySet()) {
				SiteSnpIndelInfo siteSnpIndelInfo = mapAllen2Num.get(allen);
				refSiteSnpIndel.mapAllen2Num.put(allen, siteSnpIndelInfo);
			}
			
			refSiteSnpIndel.mapSample2NormReadsInfo = new HashMap<String, SampleRefReadsInfo>();
			for (String sampleName : mapSample2NormReadsInfo.keySet()) {
				SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
				refSiteSnpIndel.mapSample2NormReadsInfo.put(sampleName, sampleRefReadsInfo);
			}
			
			return refSiteSnpIndel;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		logger.error("克隆出错");
		return null;
	}
	public void clear() {
		gffChrAbs = null;
		gffGeneIsoInfo = null;
		mapAllen2Num.clear();
		mapSample2NormReadsInfo.clear();
	}
}
/** 某个样本在该位点的reference reads数量 */
class SampleRefReadsInfo {
	/** AD
	 * ref的reads数量
	 */
	int Allelic_depths_Ref = 0;
	/**  DP
	 * 全部过滤后的reads数量，only filtered reads used for calling
	 */
	int readDepth = 0;
	/**
	 * GQ
	 * The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT. In diploid case, 
	 * if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood of the NGS sequencing data
	 *  under the model of that the sample is 0/0, 0/1/, or 1/1. 
	 * 好像是碱基的质量
	 */
	double Genotype_Quality = 0;
	/**
	 * SB, 
	 * How much evidence is there for Strand Bias (the variation being seen on only the forward or only the reverse strand) in the reads?
	 *  Higher SB values denote more bias (and therefore are more likely to indicate false positive calls).
	 */
	double Strand_Bias = 0;
	/** 意思就是在sampileup文件中查过了这一行 */
	boolean searchSampileupFile = false;
	
	public SampleRefReadsInfo() { }
	
	public SampleRefReadsInfo(int readDepth) {
		this.readDepth = readDepth;
	}
	
	protected void setSearchSampileupFile(boolean searchSampileupFile) {
		this.searchSampileupFile = searchSampileupFile;
	}
	public boolean isSearchSampileupFile() {
		return searchSampileupFile;
	}
	public void setReadDepth(int readDepth) {
		this.readDepth = readDepth;
	}
	public void setRefDepth(int allelic_depths_Ref) {
		this.Allelic_depths_Ref = allelic_depths_Ref;
	}
	/** 加上与ref相符的reads数量 */
	public void addRefDepth(int num) {
		this.Allelic_depths_Ref = Allelic_depths_Ref + num;
	}
	public void setGenotype_Quality(double genotype_Quality) {
		Genotype_Quality = genotype_Quality;
	}
	/** 设置GATK的vcf信息 */
	public void setStrand_Bias(double strand_Bias) {
		Strand_Bias = strand_Bias;
	}
	public int getAllelic_depths_Ref() {
		return Allelic_depths_Ref;
	}
	public double getGenotype_Quality() {
		return Genotype_Quality;
	}

	public int getReadsDepth() {
		return readDepth;
	}
	public double getStrand_Bias() {
		return Strand_Bias;
	}
}
/** 设定需要排序的样本，也就是输入的名字，然后根据该样本的信息进行排序 */
class compRefSiteSnpIndelBig2Small implements Comparator<SiteSnpIndelInfo> {
	String sampleName;
	public compRefSiteSnpIndelBig2Small(String sampleName) {
		this.sampleName = sampleName;
	}
	//倒序排列
	public int compare(SiteSnpIndelInfo o1, SiteSnpIndelInfo o2) {
		o1.setSampleName(sampleName);
		o2.setSampleName(sampleName);
		Integer readsNum1 = o1.getReadsNum();
		Integer readsNum2 = o2.getReadsNum();
		return -readsNum1.compareTo(readsNum2);
	}
}



