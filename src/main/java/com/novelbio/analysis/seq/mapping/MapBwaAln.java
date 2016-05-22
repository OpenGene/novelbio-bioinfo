package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRGroup;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 在set里面有很多参数可以设定，不设定就用默认
 * @author zong0jie
 *
 */
@Component
@Scope("prototype")
public class MapBwaAln extends MapDNA {
	private static final Logger logger = LoggerFactory.getLogger(MapBwaAln.class);
	/**
	 * 在此大小以下的genome直接读入内存以帮助快速mapping
	 * 单位，KB
	 * 似乎该值双端才有用
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	/** 临时文件，如sai文件等的保存路径 */
	String tmpPath;
	String[] sampleGroup;
	String leftCombFq;
	String rightCombFq;
	
	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** 含有几个gap */
	int gapNum;
	/** gap的长度 */
	int gapLength;
	/** 线程数量 */
	int threadNum = 4;
	/** 比对的种子长度 */
	int seedLen;
	/** gap的open罚分 */
	int openPanalty;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** 是否将index读入内存，仅对双端有效 */
	boolean readInMemory = true;
	
	public MapBwaAln() {
		super(SoftWare.bwa_aln);
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
	
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatch
	 */
	public void setMismatch(double mismatch) {
		if (mismatch >= 1 || mismatch == 0) {
			this.mismatch = (int)mismatch+"";
		}
		else {
			this.mismatch = mismatch + "";
		}
	}
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatchScore
	 */
	private String[] getMismatch() {
		return new String[]{"-n", mismatch + ""};
	}

	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	private String[] getThreadNum() {
		return new String[]{"-t", threadNum + ""};
	}
	/**
	 * 是否将index读入内存，仅对双端测序有用
	 */
	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}
	
	public void setMapLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		SamRGroup samRGroup = new SamRGroup(sampleID, LibraryName, SampleName, Platform);
		sampleGroup = new String[]{"-r", samRGroup.toString()};
	}
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	private String[] getGapLen() {
		if (gapLength <= 0) {
			return null;
		}
		return new String[]{"-e", gapLength + ""};
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	public void setGapNum(int gapnum) {
		this.gapNum = gapnum;
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	private String[] getGapNum() {
		if (gapNum <= 0) {
			return null;
		}
		return new String[]{"-o", gapNum + ""};
	}
	private String[] getInsertSize() {
		int insertMax = 500;
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return new String[]{"-a", insertMax + ""};
		}
		return null;
	}
	protected boolean isPairEnd() {
		if (leftCombFq == null || rightCombFq == null) {
			return false;
		}
		return true;
	}
	/** 比对的种子长度 */
	public void setSeedLen(int seedLen) {
		this.seedLen = seedLen;
	}
	/** 种子长度 */
	private String[] getSeedSize() {
		if (seedLen <= 0) {
			return null;
		}
		return new String[]{"-l", seedLen + ""};
	}
	
	/** gap的open罚分 */
	public void setGepOpenPanalty(int gepOpenPanalty) {
		this.openPanalty = gepOpenPanalty;
	}
	/** gap的open罚分 */
	private String[] getOpenPanalty() {
		if (openPanalty <= 0) {
			return null;
		}
		return new String[]{"-O", openPanalty +""};
	}
	/**
	 * 是illumina32标准还是64标准
	 * @return 64标准返回"-l", 32标准返回null
	 */
	private String getFastQoffset() {
		FastQ fastQ = lsLeftFq.get(0);
		int offset = fastQ.getOffset();
		if (offset == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return  "-I";
		}
		return null;
	}
	/**
	 * 返回sai的信息, <b>不加引号</b>
	 * @param Sai1orSai2 双端的话，sai1就输入1，sai2就输入2。单端sai也输入1
	 * @return
	 */
	private String getSai(int Sai1orSai2) {
		String sai = tmpPath + FileOperate.getFileNameSep(outFileName)[0];
		if (Sai1orSai2 == 1) {
			if (isPairEnd()) {
				sai = sai + "_1.sai"; 
			} else {
				sai = sai + ".sai";
			}
		} else if (Sai1orSai2 ==2) {
			sai = sai + "_2.sai"; 
		}
		return sai;
	}
	
	/**
	 * 根据基因组大小，考虑将基因组读入内存
	 * @return 没有则返回null
	 */
	private String readInMemory() {
		if (FileOperate.isFileExistAndBigThanSize(indexMaker.getChrFile(), 0) && FileOperate.getFileSizeLong(indexMaker.getChrFile())/1024 < GENOME_SIZE_IN_MEMORY || readInMemory) {
			return "-P";
		}
		return null;
	}
	
	@Override
	protected SamFile mapping() {
		generateTmpPath();
		combSeq();
		bwaAln();
		SamFile samFile = bwaSamPeSe();
		FileOperate.deleteFileFolder(leftCombFq);
		FileOperate.deleteFileFolder(rightCombFq);
		return samFile;
	}
	
	private void generateTmpPath() {
		if (tmpPath == null) {
			tmpPath = FileOperate.addSep(CmdOperate.getCmdTmpPath()) + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
			FileOperate.createFolders(tmpPath);
		}
	}
	
	private void combSeq() {
		boolean singleEnd = (lsLeftFq.size() > 0 && lsRightFq.size() > 0) ? false : true;
		if ( FileOperate.isFileExistAndBigThanSize(leftCombFq, 0) &&
				(singleEnd || 
						(!singleEnd && FileOperate.isFileExistAndBigThanSize(rightCombFq, 0) )
				)
			)
		{
			return;
		}
		String outPath = tmpPath;
		if (lsLeftFq.size() == 1) {
			String leftFileName = lsLeftFq.get(0).getReadFileName();
			leftCombFq = tmpPath + FileOperate.getFileName(leftFileName);
			FileOperate.copyFile(leftFileName, leftCombFq, true);
		} else {
			leftCombFq = combSeq(outPath, singleEnd, true, prefix, lsLeftFq);
		}
		if (singleEnd) return;
		
		if (lsRightFq.size() == 1) {
			String rightFileName = lsRightFq.get(0).getReadFileName();
			rightCombFq = tmpPath + FileOperate.getFileName(rightFileName);
			FileOperate.copyFile(rightFileName, rightCombFq, true);
		} else {
			rightCombFq = combSeq(outPath, singleEnd, false, prefix, lsRightFq);
		}
	}
	
	/** 返回是否成功设置名字
	 * 如果设置了名字，就要将名字清除
	 * @return
	 */
	private boolean setSeqName() {
		boolean singleEnd = (lsLeftFq.size() > 0 && lsRightFq.size() > 0) ? false : true;
		if ( leftCombFq != null && (singleEnd || (!singleEnd && rightCombFq != null))) {
			return false;
		}
		String outPath = tmpPath;
		if (lsLeftFq.size() == 1) {
			String leftFileName = lsLeftFq.get(0).getReadFileName();
			leftCombFq = tmpPath + FileOperate.getFileName(leftFileName);
		} else {
			leftCombFq = getCombSeqName(outPath, singleEnd, true, prefix, lsLeftFq);
		}
		
		if (singleEnd) return true;
		
		if (!singleEnd && lsRightFq.size() == 1) {
			String rightFileName = lsRightFq.get(0).getReadFileName();
			rightCombFq = tmpPath + FileOperate.getFileName(rightFileName);
		} else {
			rightCombFq = getCombSeqName(outPath, singleEnd, false, prefix, lsRightFq);
		}
		return true;
	}
	
	/** 将输入的fastq文件合并为一个 */
	protected static String combSeq(String outPath, boolean singleEnd, boolean left, String prefix, List<FastQ> lsFastq) {
		if (lsFastq == null || lsFastq.size() == 0) {
			return null;
		}
		String fastqFile = getCombSeqName(outPath, singleEnd, left, prefix, lsFastq);

		FastQ fastqComb = new FastQ(fastqFile, true);
		for (FastQ fastQ : lsFastq) {
			for (FastQRecord fastQRecord : fastQ.readlines()) {
				fastqComb.writeFastQRecord(fastQRecord);
			}
			fastQ.close();
		}
		fastqComb.close();
		return fastqComb.getReadFileName();
	}
	
	/** 将输入的fastq文件合并为一个 */
	protected static String getCombSeqName(String outPath, boolean singleEnd, boolean left, String prefix, List<FastQ> lsFastq) {
		String fastqFile = outPath + prefix;
		if (prefix.equals("")) {
			fastqFile = fastqFile + "combine";
		} else {
			fastqFile = fastqFile + "_combine";
		}
		if (singleEnd) {
			fastqFile += ".fq.gz";
		} else {
			if (left) {
				fastqFile += "_1.fq.gz";
			} else if (!left) {
				fastqFile += "_2.fq.gz";
			}
		}
		return fastqFile;
	}
	
	/**
	 * linux命令如下<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai<br>
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq
	 *
	 * @return 是否成功运行
	 */
	private void bwaAln() {
		List<String> lsCmdLeft = getLsCmdAln(true);
		CmdOperate cmdOperate = new CmdOperate(lsCmdLeft);
		cmdOperate.runWithExp("bwa aln error:");
		
		if (isPairEnd()) {
			List<String> lsCmdRight = getLsCmdAln(false);
			cmdOperate = new CmdOperate(lsCmdRight);
			cmdOperate.runWithExp("bwa aln error:");
		}
	}
	
	private List<String> getLsCmdAln(boolean firstOrSecond) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(indexMaker.getExePath() + "bwa");
		lsCmd.add("aln");
		ArrayOperate.addArrayToList(lsCmd, getMismatch());
		ArrayOperate.addArrayToList(lsCmd, getGapNum());
		ArrayOperate.addArrayToList(lsCmd, getGapLen());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getSeedSize());
		ArrayOperate.addArrayToList(lsCmd, getOpenPanalty());
		addLsCmd(lsCmd, getFastQoffset());
		lsCmd.add(indexMaker.getIndexName());
		if (firstOrSecond) {
			lsCmd.add(leftCombFq);
			lsCmd.add(">");
			lsCmd.add(getSai(1));
		} else {
			lsCmd.add(rightCombFq);
			lsCmd.add(">");
			lsCmd.add(getSai(2));	
		}
		return lsCmd;
	}
	
	/**
	 * @param lsCmd
	 * @param param null则不添加入lsCmd
	 */
	private void addLsCmd(List<String> lsCmd, String param) {
		if (param == null) return;
		lsCmd.add(param);
	}
	
	/**
	 * 这里设定了将基因组读入内存的限制
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
	 * TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
	 */
	private SamFile bwaSamPeSe() {
		List<String> lsCmd = getLsCmdSam();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		Thread thread = new Thread(cmdOperate);
		thread.setDaemon(true);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		SamFile samResult = copeSamStream(false, inputStream, isNeedSort);
		if (samResult != null && !cmdOperate.isRunning() && cmdOperate.isFinishedNormal()) {
			deleteFile();
			return samResult;
		} else {
			deleteFailFile();
			if(!cmdOperate.isFinishedNormal()) {
				throw new ExceptionCmd("bwa aln mapping error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
			return null;
		}
	}
	
	private List<String> getLsCmdSam() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(indexMaker.getExePath() + "bwa");
		if (isPairEnd()) {
			lsCmd.add("sampe");
			ArrayOperate.addArrayToList(lsCmd, sampleGroup);
			ArrayOperate.addArrayToList(lsCmd, getInsertSize());
			addLsCmd(lsCmd, readInMemory());
			lsCmd.add("-n"); lsCmd.add(10+"");
			lsCmd.add("-N"); lsCmd.add(10+"");
			lsCmd.add(indexMaker.getIndexName());
			lsCmd.add(getSai(1));
			lsCmd.add(getSai(2));
			lsCmd.add(leftCombFq);
			lsCmd.add(rightCombFq);
		} else {
			lsCmd.add("samse");
			ArrayOperate.addArrayToList(lsCmd, sampleGroup);
			lsCmd.add("-n"); lsCmd.add(50+"");
			lsCmd.add(indexMaker.getIndexName());
			lsCmd.add(getSai(1));
			lsCmd.add(leftCombFq);
		}
		lsCmd.add(">");
		lsCmd.add(outFileName);
		return lsCmd;
	}
	
	/**
	 * 删除sai文件
	 * @param samFileName
	 */
	private void deleteFile() {
		FileOperate.deleteFileFolder(getSai(1));
		if (isPairEnd()) {
			FileOperate.deleteFileFolder(getSai(2));
		}
	}
	
	@Override
	public List<String> getCmdExeStr() {
		generateTmpPath();
		boolean isSetSucess = setSeqName();
		List<String> lsCmdResult = new ArrayList<>();
		String version = indexMaker.getVersion();
		if (version != null) {
			lsCmdResult.add("bwa version: " + indexMaker.getVersion());
		}
		List<String> lsCmd = getLsCmdAln(true);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsCmdResult.add(cmdOperate.getCmdExeStr());
		if (isPairEnd()) {
			lsCmd = getLsCmdAln(false);
			cmdOperate = new CmdOperate(lsCmd);
			lsCmdResult.add(cmdOperate.getCmdExeStr());
		}
		lsCmd = getLsCmdSam();
		cmdOperate = new CmdOperate(lsCmd);
		lsCmdResult.add(cmdOperate.getCmdExeStr());
		if (isSetSucess) {
			leftCombFq = null;
			rightCombFq = null;
		}
		return lsCmdResult;
	}

}
