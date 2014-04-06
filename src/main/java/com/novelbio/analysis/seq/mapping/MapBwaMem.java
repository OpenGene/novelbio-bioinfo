package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRGroup;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

@Component
@Scope("prototype")
public class MapBwaMem extends MapDNA {
	/**指定线程数**/
	int nThreads;
	/**输出所有找到比对的单端和不配对的双端的读长。**/
	boolean isOutputSingleReads = true;
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
	
	/**是否输出所有找到比对的单端和不配对的双端的读长。**/
	boolean allOut;
	/**设定读入的第一个fq文件是交错的配对数据。**/
	boolean staggeredPairingFQ = false;
	
	/**在双端模式中，使用SW(Smith-Waterman algorithm)来检测没有回帖的数据，而不会尝试去找到更适合的位点。*/
	boolean swData = true;
	
	String leftCombFq;
	String rightCombFq;
	
	String exePath;
	
	public MapBwaMem() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.bwa_men);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	/**指定线程数**/
	public void setThreadNum(int nThreads) {
		this.nThreads = nThreads;
	}
	private String[] getThreadNum() {
		if (nThreads <= 0) {
			return null;
		}
		return new String[]{"-t" ,nThreads + "" };
	}
	
	/**输出所有找到比对的单端和不配对的双端的读长，应该是仅双端起作用，默认是true **/
	public void setOutputSingleReads(boolean isOutputSingleReads) {
		this.isOutputSingleReads = isOutputSingleReads;
	}
	private String getIsOutputSingleReads() {
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
	private String[] getMinSeedLenParam() {
		if (minSeedLen <= 15) {
			return null;
		}
		return new String[]{"-k" ,minSeedLen + "" };
	}
	
	/** 带宽，设置了软件查询的gap的最长长度[100] */
	public void setGapBandWidth(int bandWidth) {
		this.bandWidth = bandWidth;
	}
	private String[] getBandWidthParam() {
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
	private String[] getzDropoffParam() {
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
	private String[] getSeedSplitRatioParam() {
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
	private String[] getMaxOccParam() {
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
	private String getSwDataParam(){
		if (swData) {
			return "-P";
		}
		return null;
	}
	
	/** 设定匹配打分[1] */
	public void setMatchScore(int matchScore) {
		this.matchScore = matchScore;
	}
	private String[] getMatchScoreParam() {
		if (matchScore <= 0) {
			return null;
		}
		return new String[]{"-A" ,matchScore + "" };
	}

	/** 设定mismatch罚分[4] */
	public void setMmPenalty(int mmPenalty) {
		this.mmPenalty = mmPenalty;
	}
	private String[] getMmPenaltyParam() {
		if (mmPenalty <= 0) {
			return null;
		}
		return new String[]{"-B" ,mmPenalty + "" };
	}
	
	/** gap的起始罚分[6] */
	public void setGapOpenPen(int gapOpenPen) {
		this.gapOpenPen = gapOpenPen;
	}
	private String[] getGapOpenPenParam() {
		if (gapOpenPen <= 0) {
			return null;
		}
		return new String[]{"-O" ,gapOpenPen + "" };
	}

	/** gap的延伸罚分[1] */
	public void setGapExtPen(int gapExtPen) {
		this.gapExtPen = gapExtPen;
	}
	private String[] getGapExtPenParam() {
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
	private String[] getClipPenParam() {
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
	private String[] getUnpairPenParam() {
		if (unpairPen <= 0) {
			return null;
		}
		return new String[]{"-U" ,unpairPen + "" };
	}
	
	/** 设定读入的第一个fq文件是交错的配对数据 */
	public void setStaggeredPairingFQ(boolean staggeredPairingFQ) {
		this.staggeredPairingFQ = staggeredPairingFQ;
	}
	private String getStaggeredPairingFQParam(){
		if (staggeredPairingFQ) {
			return "-p";
		}
		return null;
	}
	
	
	@Override
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
	private String[] getRGlineParam() {
		if (StringOperate.isRealNull(RGline)) {
			return null;
		}
		return new String[]{"-R" ,RGline + "" };
	}
	
	/** 最低mapping 质量 */
	public void setMinMapQuality(int minMapQuality) {
		this.minMapQuality = minMapQuality;
	}
	private String[] getMinMapQuality() {
		if (minMapQuality <= 0) {
			return null;
		}
		return new String[]{"-T", minMapQuality + ""};
	}
	
	/** 默认为false */
	public void setHardClipping(boolean hardClipping) {
		this.hardClipping = hardClipping;
	}
	private String getHardClippingParam(){
		if (hardClipping) {
			return "-H";
		}
		return null;
	}
	
	/** 是否输出所有找到比对的单端和不配对的双端的读长 */
	public void setIsOutAllReads(boolean allOut) {
		this.allOut = allOut;
	}
	private String getAllOutParam(){
		if (allOut) {
			return "-a";
		}
		return null;
	}
	
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	@Override
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
		leftCombFq = null;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	@Override
	public void setRightFq(List<FastQ> lsRightFastQs) {
		if (lsRightFastQs == null) return;
		this.lsRightFq = lsRightFastQs;
		rightCombFq = null;
	}
	
	private void combSeq() {
		boolean singleEnd = (lsLeftFq.size() > 0 && lsRightFq.size() > 0) ? false : true;
		if ( leftCombFq != null && (singleEnd || (!singleEnd && rightCombFq != null))) {
			return;
		}
		String outPath = FileOperate.getPathName(outFileName);
		if (lsLeftFq.size() == 1) {
			leftCombFq = lsLeftFq.get(0).getReadFileName();
		} else {
			leftCombFq = MapBwaAln.combSeq(outPath, singleEnd, true, prefix, lsLeftFq);
		}
		if (lsRightFq.size() == 1) {
			rightCombFq = lsRightFq.get(0).getReadFileName();
		} else {
			rightCombFq = MapBwaAln.combSeq(outPath, singleEnd, false, prefix, lsRightFq);
		}
	}

	protected boolean isPairEnd() {
		if (leftCombFq == null || rightCombFq == null) {
			return false;
		}
		return true;
	}
	/**
	 * 返回输入的文件，根据是否为pairend，调整返回的结果
	 * @return
	 */
	private List<String> getLsFqFile() {
		List<String> lsOut = new ArrayList<>();
		if (leftCombFq != null) {
			lsOut.add(leftCombFq);
		}
		if (rightCombFq != null) {
			lsOut.add(leftCombFq);
		}
		return lsOut;
	}
	
	protected List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<String>();
		lsCmd.add(exePath + "bwa");
		lsCmd.add("mem");
		addStringParam(lsCmd, getAllOutParam());
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
		addStringParam(lsCmd, getHardClippingParam());
		addStringParam(lsCmd, getStaggeredPairingFQParam());
		addStringParam(lsCmd, getSwDataParam());
		
		addStringParam(lsCmd, getHardClippingParam());
		lsCmd.add(chrFile);
		lsCmd.addAll(getLsFqFile());
		lsCmd.add(">");
		lsCmd.add(outFileName);
		return lsCmd;
	}
	
	private void addStringParam(List<String> lsCmd, String param) {
		if (StringOperate.isRealNull(param)) {
			return;
		}
		lsCmd.add(param);
	}
	

	@Override
	protected SamFile mapping() {
		combSeq();
		List<String> lsCmd = getLsCmd();
		
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		SamFile samResult = copeSamStream(false, inputStream, isNeedSort);
		if (samResult != null && !cmdOperate.isRunning() && cmdOperate.isFinishedNormal()) {
			return samResult;
		} else {
			deleteFailFile();
			return null;
		}
	}
	
	public void run() {
		List<String> lsListCmd = new ArrayList<String>();
		lsListCmd.addAll(getLsCmd());
		CmdOperate cmdOperate = new CmdOperate(lsListCmd);
		cmdOperate.run();
	}


	@Override
	public List<String> getCmdExeStr() {
		combSeq();
		List<String> lsCmdResult = new ArrayList<>();
		lsCmdResult.add("bwa version: " + MapBwaAln.getVersion(this.exePath));
		lsCmdResult.addAll(getLsCmd());
		return lsCmdResult;
	}

	/**
	 * the mem command will infer the read orientation and the insert size distribution from a batch of reads.
	 */
	@Override
	@Deprecated
	public void setMapLibrary(MapLibrary mapLibrary) {}

	@Override
	protected boolean makeIndex() {
		List<String> lsCmd = MapBwaAln.getLsCmdIndex(exePath, chrFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		return cmdOperate.isFinishedNormal();
	}

	@Override
	protected void deleteIndex() {
		MapBwaAln.deleteIndexBwa(chrFile);
	}

	@Override
	protected boolean isIndexExist() {
		return MapBwaAln.isIndexExist(chrFile);
	}
	
}
