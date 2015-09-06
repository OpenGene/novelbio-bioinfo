package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.sam.SamRGroup;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;

/** 所有与输入文件无关的参数 */
public class MapParamBwaMem {
	private String exePath = "";
	
	/**指定线程数**/
	int nThreads;
	/**输出所有找到比对的单端和不配对的双端的读长。**/
	boolean isOutputSingleReads = false;
	/** 指定种子的最大长度，比对匹配短于这个数据的都会被过滤掉，当这个数据在20附近的时候对结果不会有显著影响[19]；**/
	int  minSeedLen;
	/** 带宽，设置了软件查询的gap的最长长度[100]**/
	int  bandWidth;
	/**用于结果过滤的，当最好的打分和现在的打分相差超过这个分数的时候，过滤掉这个结果。[100] **/
	int zDropoff;
	/** 设定新的种子的长度倍数，这个参数对于性能有很大影响，这个值越大，比对的速度就会越快，但是的准确度会降低。[1.5]**/
	double seedSplitRatio;
	/**设定当一条读长在基因组中的次数高于这个值的时候，就舍弃这个数据。[10000]**/
	int maxOcc;
	/**设定匹配打分[1]**/
	int  matchScore;
	/**设定不匹配罚分[4]**/
	int mmPenalty;
	/** gap的起始罚分[6]**/
	int gapOpenPen;
	/**gap的延伸罚分[1]**/
	int gapExtPen;
	/**剪切罚分[5]**/
	int clipPen;
	/**对于不配对的读长配对的罚分[9]**/
	int unpairPen;
	/**指定输出的sam文件的header区[null]**/
	String RGline;
	/** 最低mapping 质量 */
	int minMapQuality;
	/**是否对于结果是否使用hard clipping**/
	boolean hardClipping;
	
	/** The BWA-MEM algorithm performs local alignment. It may produce 
	 * multiple primary alignments for different part of a query sequence.
	 *  This is a crucial feature for long sequences. However, some tools 
	 *  such as Picard’s markDuplicates does not work with split alignments.
	 *   One may consider to use option -M to flag shorter split hits as secondary.
	 */
	boolean markShorterSplitAsSecondary = false;
	
	/**设定读入的第一个fq文件是交错的配对数据。**/
	boolean staggeredPairingFQ = false;
	
	/**在双端模式中，使用SW(Smith-Waterman algorithm)来检测没有回帖的数据，而不会尝试去找到适合双端的匹配形式
	 * In the paired-end mode, perform SW to rescue missing hits only but do not try to find hits that fit a proper pair.
	 */
	boolean swData = true;
	
	public void setExePath(String exePath) {
	    this.exePath = exePath;
    }
	public String getExePath() {
	    return exePath;
    }
	/**指定线程数**/
	public void setThreadNum(int nThreads) {
		this.nThreads = nThreads;
	}
	protected String[] getThreadNum() {
		if (nThreads <= 0) {
			return null;
		}
		return new String[]{"-t" ,nThreads + "" };
	}
	
	/**输出所有找到比对的单端和不配对的双端的读长，默认是false，选择为true会出来一堆乱七八糟的东西 **/
	public void setOutputSingleReads(boolean isOutputSingleReads) {
		this.isOutputSingleReads = isOutputSingleReads;
	}
	protected String getIsOutputSingleReads() {
		if (isOutputSingleReads) {
			return "-a";
		} else {
			return null;
		}
	}
	
	/** 指定种子的最大长度，比对匹配短于这个数据的都会被过滤掉，当这个数据在20附近的时候对结果不会有显著影响[19]；*/
	public void setMinSeedLen(int minSeedLen) {
		this.minSeedLen = minSeedLen;
	}
	protected String[] getMinSeedLenParam() {
		if (minSeedLen <= 15) {
			return null;
		}
		return new String[]{"-k" ,minSeedLen + "" };
	}
	
	/** 带宽，设置了软件查询的gap的最长长度[100] */
	public void setGapBandWidth(int bandWidth) {
		this.bandWidth = bandWidth;
	}
	protected String[] getBandWidthParam() {
		if (bandWidth <= 0) {
			return null;
		}
		return new String[]{"-w" ,bandWidth + "" };
	}
	
	/** Off-diagonal X-dropoff (Z-dropoff). Stop extension when the difference between 
	 * the best and the current extension score is above |i-j|*A+INT, where i and j are the
	 *  current positions of the query and reference, respectively, and A is the matching 
	 *  score. Z-dropoff is similar to BLAST’s X-dropoff except that it doesn’t penalize 
	 *  gaps in one of the sequences in the alignment. Z-dropoff not only avoids unnecessary 
	 *  extension, but also reduces poor alignments inside a long good alignment. [100] */
	public void setzDropoff(int zDropoff) {
		this.zDropoff = zDropoff;
	}
	protected String[] getzDropoffParam() {
		if (zDropoff <= 0) {
			return null;
		}
		return new String[]{"-d" ,zDropoff + "" };
	}

	/**
	 * Trigger re-seeding for a MEM longer than minSeedLen*FLOAT. 
	 * This is a key heuristic parameter for tuning the performance. L
	 * arger value yields fewer seeds, which leads to faster alignment
	 * speed but lower accuracy. [1.5]
	 */
	public void setSeedSplitRatio(double seedSplitRatio) {
		this.seedSplitRatio = seedSplitRatio;
	}
	protected String[] getSeedSplitRatioParam() {
		if (seedSplitRatio <= 0 || seedSplitRatio > 10) {
			return null;
		}
		return new String[]{"-r" ,seedSplitRatio + "" };
	}
	
	/** Discard a MEM if it has more than INT occurence in the genome. 
	 * This is an insensitive parameter. [10000] */
	public void setMaxOcc(int maxOcc) {
		this.maxOcc = maxOcc;
	}
	protected String[] getMaxOccParam() {
		if (maxOcc <= 0) {
			return null;
		}
		return new String[]{"-c" ,maxOcc + "" };
	}
	
	/**
	 * In the paired-end mode, perform SW(Smith-Waterman algorithm) to rescue missing hits only but do not try to find hits that fit a proper pair. 
	 * @param swData
	 */
	public void setSwData(boolean swData) {
		this.swData = swData;
	}
	protected String getSwDataParam(){
		if (swData) {
			return "-P";
		}
		return null;
	}
	
	/** 设定匹配打分[1] */
	public void setMatchScore(int matchScore) {
		this.matchScore = matchScore;
	}
	protected String[] getMatchScoreParam() {
		if (matchScore <= 0) {
			return null;
		}
		return new String[]{"-A" ,matchScore + "" };
	}

	/** 设定mismatch罚分[4] */
	public void setMmPenalty(int mmPenalty) {
		this.mmPenalty = mmPenalty;
	}
	protected String[] getMmPenaltyParam() {
		if (mmPenalty <= 0) {
			return null;
		}
		return new String[]{"-B" ,mmPenalty + "" };
	}
	
	/** gap的起始罚分[6] */
	public void setGapOpenPen(int gapOpenPen) {
		this.gapOpenPen = gapOpenPen;
	}
	protected String[] getGapOpenPenParam() {
		if (gapOpenPen <= 0) {
			return null;
		}
		return new String[]{"-O" ,gapOpenPen + "" };
	}

	/** gap的延伸罚分[1] */
	public void setGapExtPen(int gapExtPen) {
		this.gapExtPen = gapExtPen;
	}
	protected String[] getGapExtPenParam() {
		if (gapExtPen <= 0) {
			return null;
		}
		return new String[]{"-E" ,gapExtPen + "" };
	}
	
	/** Clipping penalty. When performing SW(Smith-Waterman algorithm) extension, BWA-MEM keeps track of the best 
	 * score reaching the end of query. If this score is larger than the best SW score minus the 
	 * clipping penalty, clipping will not be applied. Note that in this case, the SAM AS tag 
	 * reports the best SW score; clipping penalty is not deducted. [5]  */
	public void setClipPen(int clipPen) {
		this.clipPen = clipPen;
	}
	protected String[] getClipPenParam() {
		if (clipPen <= 0) {
			return null;
		}
		return new String[]{"-L" ,clipPen + "" };
	}
	
	/** Penalty for an unpaired read pair. BWA-MEM scores an unpaired read pair as scoreRead1+scoreRead2-INT
	 * and scores a paired as scoreRead1+scoreRead2-insertPenalty. It compares these two scores to determine 
	 * whether we should force pairing. [9]  */
	public void setUnpairPen(int unpairPen) {
		this.unpairPen = unpairPen;
	}
	protected String[] getUnpairPenParam() {
		if (unpairPen <= 0) {
			return null;
		}
		return new String[]{"-U" ,unpairPen + "" };
	}
	
	/** 设定读入的fq文件是交错的配对数据 */
	public void setStaggeredPairingFQ(boolean staggeredPairingFQ) {
		this.staggeredPairingFQ = staggeredPairingFQ;
	}
	/** 设定读入的第一个fq文件是交错的配对数据 */
	protected String getStaggeredPairingFQParam(){
		if (staggeredPairingFQ) {
			return "-p";
		}
		return null;
	}
	
	
	public void setSampleGroup(String sampleID, String LibraryName,
			String SampleName, String Platform) {
		SamRGroup samRGroup = new SamRGroup(sampleID, LibraryName, SampleName, Platform);
		RGline = samRGroup.toString();
	}
	/**
	 *  Complete read group header line. ’\t’ can be used in STR and will be
	 *  converted to a TAB in the output SAM. The read group ID will be 
	 *  attached to every read in the output. An example is ’@RG\tID:foo\tSM:bar’.
	 * @param rGline
	 */
	protected String[] getRGlineParam() {
		if (StringOperate.isRealNull(RGline)) {
			return null;
		}
		return new String[]{"-R" ,RGline + "" };
	}
	
	/** 最低mapping 质量 */
	public void setMinMapQuality(int minMapQuality) {
		this.minMapQuality = minMapQuality;
	}
	protected String[] getMinMapQuality() {
		if (minMapQuality <= 0) {
			return null;
		}
		return new String[]{"-T", minMapQuality + ""};
	}
	
	/** 默认为false */
	public void setHardClipping(boolean hardClipping) {
		this.hardClipping = hardClipping;
	}
	protected String getHardClippingParam(){
		if (hardClipping) {
			return "-H";
		}
		return null;
	}
	
	/**
	 * 默认为false <br>
	 * The BWA-MEM algorithm performs local alignment. It may produce 
	 * multiple primary alignments for different part of a query sequence.
	 *  This is a crucial feature for long sequences. However, some tools 
	 *  such as Picard’s markDuplicates does not work with split alignments.
	 *   One may consider to use option -M to flag shorter split hits as secondary.
	 */
	public void setMarkShorterSplitAsSecondary(boolean markShorterSplitAsSecondary) {
		this.markShorterSplitAsSecondary = markShorterSplitAsSecondary;
	}
	protected String getMarkShorterSplitAsSecondary(){
		if (markShorterSplitAsSecondary) {
			return "-M";
		}
		return null;
	}
	
	public List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<String>();
		lsCmd.add(exePath + "bwa");
		lsCmd.add("mem");
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		addStringParam(lsCmd, getIsOutputSingleReads());
		ArrayOperate.addArrayToList(lsCmd, getMinSeedLenParam());
		ArrayOperate.addArrayToList(lsCmd, getBandWidthParam());
		ArrayOperate.addArrayToList(lsCmd, getzDropoffParam());
		ArrayOperate.addArrayToList(lsCmd, getSeedSplitRatioParam());
		ArrayOperate.addArrayToList(lsCmd, getMaxOccParam());
		ArrayOperate.addArrayToList(lsCmd, getMatchScoreParam());
		ArrayOperate.addArrayToList(lsCmd, getMmPenaltyParam());
		ArrayOperate.addArrayToList(lsCmd, getGapOpenPenParam());
		ArrayOperate.addArrayToList(lsCmd, getGapExtPenParam());
		ArrayOperate.addArrayToList(lsCmd, getClipPenParam());
		ArrayOperate.addArrayToList(lsCmd, getUnpairPenParam());
		ArrayOperate.addArrayToList(lsCmd, getRGlineParam());
		ArrayOperate.addArrayToList(lsCmd, getMinMapQuality());
		addStringParam(lsCmd, getMarkShorterSplitAsSecondary());
		addStringParam(lsCmd, getHardClippingParam());
		addStringParam(lsCmd, getStaggeredPairingFQParam());
		addStringParam(lsCmd, getSwDataParam());
		return lsCmd;
	}
	
	protected void addStringParam(List<String> lsCmd, String param) {
		if (StringOperate.isRealNull(param)) {
			return;
		}
		lsCmd.add(param);
	}
}
