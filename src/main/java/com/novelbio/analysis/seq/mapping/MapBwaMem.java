package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

@Component
@Scope("prototype")
public class MapBwaMem extends MapDNA {
	String leftCombFq;
	String rightCombFq;
		
	/** 临时文件 */
	String tmpPath;
	CmdOperate cmdOperate;
	
	MapParamBwaMem bwaMemParam = new MapParamBwaMem();
	
	public MapBwaMem() {
		super(SoftWare.bwa_mem);
		bwaMemParam.setExePath(indexMaker.getExePath());
	}
	
	/**指定线程数**/
	public void setThreadNum(int nThreads) {
		bwaMemParam.setThreadNum(nThreads);
	}

	/**输出所有找到比对的单端和不配对的双端的读长，应该是仅双端起作用，默认是false **/
	public void setOutputSingleReads(boolean isOutputSingleReads) {
		bwaMemParam.setOutputSingleReads(isOutputSingleReads);
	}
	
	/** 指定种子的最大长度，比对匹配短于这个数据的都会被过滤掉，当这个数据在20附近的时候对结果不会有显著影响[19]；*/
	public void setMinSeedLen(int minSeedLen) {
		bwaMemParam.setMinSeedLen(minSeedLen);
	}
	
	/** 带宽，设置了软件查询的gap的最长长度[100] */
	public void setGapBandWidth(int bandWidth) {
		bwaMemParam.setGapBandWidth(bandWidth);
	}
	
	/** Off-diagonal X-dropoff (Z-dropoff). Stop extension when the difference between 
	 * the best and the current extension score is above |i-j|*A+INT, where i and j are the
	 *  current positions of the query and reference, respectively, and A is the matching 
	 *  score. Z-dropoff is similar to BLAST’s X-dropoff except that it doesn’t penalize 
	 *  gaps in one of the sequences in the alignment. Z-dropoff not only avoids unnecessary 
	 *  extension, but also reduces poor alignments inside a long good alignment. [100] */
	public void setzDropoff(int zDropoff) {
		bwaMemParam.setzDropoff(zDropoff);
	}
	
	/**
	 * Trigger re-seeding for a MEM longer than minSeedLen*FLOAT. 
	 * This is a key heuristic parameter for tuning the performance. L
	 * arger value yields fewer seeds, which leads to faster alignment
	 * speed but lower accuracy. [1.5]
	 */
	public void setSeedSplitRatio(double seedSplitRatio) {
		bwaMemParam.setSeedSplitRatio(seedSplitRatio);
	}
	
	/** Discard a MEM if it has more than INT occurence in the genome. 
	 * This is an insensitive parameter. [10000] */
	public void setMaxOcc(int maxOcc) {
		bwaMemParam.setMaxOcc(maxOcc);
	}
	
	/**
	 * In the paired-end mode, perform SW(Smith-Waterman algorithm) to rescue missing hits only but do not try to find hits that fit a proper pair. 
	 * @param swData
	 */
	public void setSwData(boolean swData) {
		bwaMemParam.setSwData(swData);
	}
	
	/** 设定匹配打分[1] */
	public void setMatchScore(int matchScore) {
		bwaMemParam.setMatchScore(matchScore);
	}

	/** 设定mismatch罚分[4] */
	public void setMmPenalty(int mmPenalty) {
		bwaMemParam.setMmPenalty(mmPenalty);
	}
	
	/** gap的起始罚分[6] */
	public void setGapOpenPen(int gapOpenPen) {
		bwaMemParam.setGapOpenPen(gapOpenPen);
	}
	
	/** gap的延伸罚分[1] */
	public void setGapExtPen(int gapExtPen) {
		bwaMemParam.setGapExtPen(gapExtPen);
	}
	
	/** Clipping penalty. When performing SW(Smith-Waterman algorithm) extension, BWA-MEM keeps track of the best 
	 * score reaching the end of query. If this score is larger than the best SW score minus the 
	 * clipping penalty, clipping will not be applied. Note that in this case, the SAM AS tag 
	 * reports the best SW score; clipping penalty is not deducted. [5]  */
	public void setClipPen(int clipPen) {
		bwaMemParam.setClipPen(clipPen);
	}

	/** Penalty for an unpaired read pair. BWA-MEM scores an unpaired read pair as scoreRead1+scoreRead2-INT
	 * and scores a paired as scoreRead1+scoreRead2-insertPenalty. It compares these two scores to determine 
	 * whether we should force pairing. [9]  */
	public void setUnpairPen(int unpairPen) {
		bwaMemParam.setUnpairPen(unpairPen);
	}
	
	/** 设定读入的第一个fq文件是交错的配对数据 */
	public void setStaggeredPairingFQ(boolean staggeredPairingFQ) {
		bwaMemParam.setStaggeredPairingFQ(staggeredPairingFQ);
	}
	
	public void setSampleGroup(String sampleID, String LibraryName,
			String SampleName, String Platform) {
		bwaMemParam.setSampleGroup(sampleID, LibraryName, SampleName, Platform);
	}
	
	/** 最低mapping 质量 */
	public void setMinMapQuality(int minMapQuality) {
		bwaMemParam.setMinMapQuality(minMapQuality);
	}
	
	/** 默认为false */
	public void setHardClipping(boolean hardClipping) {
		bwaMemParam.setHardClipping(hardClipping);
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
		bwaMemParam.setMarkShorterSplitAsSecondary(markShorterSplitAsSecondary);
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
	protected List<String> getLsFqFile() {
		List<String> lsOut = new ArrayList<>();
		if (leftCombFq != null) {
			lsOut.add(leftCombFq);
		}
		if (rightCombFq != null) {
			lsOut.add(rightCombFq);
		}
		return lsOut;
	}
	
	protected List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<String>();
		lsCmd.addAll(bwaMemParam.getLsCmd());
		
		lsCmd.add(indexMaker.getChrFile());
		lsCmd.addAll(getLsFqFile());
		lsCmd.add(">");
		lsCmd.add(outFileName);
		return lsCmd;
	}

	

	@Override
	protected SamFile mapping() {
		generateTmpPath();
		
		combSeq();
		List<String> lsCmd = getLsCmd();
		
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		Thread thread = new Thread(cmdOperate);
		thread.setDaemon(true);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		SamFile samResult = copeSamStream(true, inputStream, isNeedSort);
		FileOperate.deleteFileFolder(leftCombFq);
		FileOperate.deleteFileFolder(rightCombFq);
		if (samResult != null && !cmdOperate.isRunning() && cmdOperate.isFinishedNormal()) {
			return samResult;
		} else {
			deleteFailFile();
			if(!cmdOperate.isFinishedNormal()) {
				throw new ExceptionCmd("bwa aln mapping error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
			return null;
		}
	}
	
	protected void generateTmpPath() {
		if (tmpPath == null) {
			tmpPath = FileOperate.addSep(CmdOperate.getCmdTmpPath()) + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
			FileOperate.createFolders(tmpPath);
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
		generateTmpPath();
		combSeq();
		List<String> lsCmdResult = new ArrayList<>();
		lsCmdResult.add("bwa version: " + indexMaker.getVersion());
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmdResult.add(cmdOperate.getCmdExeStr());
		return lsCmdResult;
	}
	
	protected void combSeq() {
		//左右两端都为空，说明需要输入std信息了
		if (lsLeftFq.isEmpty() && lsRightFq.isEmpty()) {
			leftCombFq = null;
			rightCombFq = null;
			return;
		}
		boolean singleEnd = (lsLeftFq.size() > 0 && lsRightFq.size() > 0) ? false : true;
		if ( leftCombFq != null && (singleEnd || (!singleEnd && rightCombFq != null))) {
			return;
		}
		String outPath = tmpPath;
		if (lsLeftFq.size() == 1) {
			String leftFileName = lsLeftFq.get(0).getReadFileName();
			leftCombFq = tmpPath + FileOperate.getFileName(leftFileName);
			FileOperate.copyFile(leftFileName, leftCombFq, true);
		} else {
			leftCombFq = MapBwaAln.combSeq(outPath, singleEnd, true, prefix, lsLeftFq);
		}
		if (lsRightFq.size() == 1) {
			String rightFileName = lsRightFq.get(0).getReadFileName();
			rightCombFq = tmpPath + FileOperate.getFileName(rightFileName);
			FileOperate.copyFile(rightFileName, rightCombFq, true);
		} else {
			rightCombFq = MapBwaAln.combSeq(outPath, singleEnd, false, prefix, lsRightFq);
		}
	}
	
	/**
	 * the mem command will infer the read orientation and the insert size distribution from a batch of reads.
	 */
	@Override
	@Deprecated
	public void setMapLibrary(MapLibrary mapLibrary) {}
	
}
