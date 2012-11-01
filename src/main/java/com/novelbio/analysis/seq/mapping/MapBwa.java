package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 在set里面有很多参数可以设定，不设定就用默认
 * @author zong0jie
 *
 */
public class MapBwa implements MapDNA {
	public static void main(String[] args) {
		MapBwa mapBwa = new MapBwa();
		mapBwa.setSampleGroup("asa", null, null, null);
		System.out.println(mapBwa.sampleGroup);
	}
	
	
	private static Logger logger = Logger.getLogger(MapBwa.class);
	/**
	 * 在此大小以下的genome直接读入内存以帮助快速mapping
	 * 单位，KB
	 * 似乎该值双端才有用
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	
	CmdOperate cmdOperate = null;
	/** bwa所在路径 */
	String ExePath = "";
	String chrFile;
	String sampleGroup = "";
	String outFileName = "";
	String leftFq = "";
	String rightFq = "";
	boolean pairend = false;

	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** 含有几个gap */
	int gapNum = 1;
	/** gap的长度 */
	int gapLength = 6;
	/** 线程数量 */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** 是否将index读入内存，仅对双端有效 */
	boolean readInMemory = false;
	
	public MapBwa() {}
	/**
	 * @param fastQ
	 * @param outFileName 结果文件名，后缀自动改为sam
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	public MapBwa(FastQ fastQ, String outFileName) {
		this.outFileName = outFileName;
		leftFq = fastQ.getReadFileName();
		pairend = false;
	}
	/**
	 * 双端只做unique mapping
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param outFileName 结果文件名，后缀自动改为sam
	 */
	public MapBwa(String seqFile1, String seqFile2, String outFileName) {
		leftFq = seqFile1;
		rightFq = seqFile2;
		this.outFileName = outFileName;
		pairend = true;
	}
	/**
	 * @param seqFile1
	 * @param outFileName 结果文件名，后缀自动改为sam
	 * @param uniqMapping 是否unique mapping
	 */
	public MapBwa(String seqFile,String outFileName) {
		leftFq = seqFile;
		this.outFileName = outFileName;
		pairend = false;
	}
	/** 输入已经过滤好的fastq文件 */
	public void setFqFile(String leftFq, String rightFq) {
		if (FileOperate.isFileExistAndBigThanSize(leftFq, 10) && FileOperate.isFileExistAndBigThanSize(rightFq, 10)) {
			this.leftFq = leftFq;
			this.rightFq = rightFq;
			pairend = true;
		}
		else if (FileOperate.isFileExistAndBigThanSize(leftFq, 10)) {
			this.leftFq = leftFq;
			pairend = false;
		}
		else if (FileOperate.isFileExistAndBigThanSize(rightFq, 10)) {
			this.leftFq = rightFq;
			pairend = false;
		}
	}
	/** 输入已经过滤好的fastq文件 */
	public void setFqFile(FastQ leftFq, FastQ rightFq) {
		String leftFqFile = "", rightFqFile = "";
		if (leftFq != null) {
			leftFqFile = leftFq.getReadFileName();
		}
		if (rightFq != null) {
			rightFqFile = rightFq.getReadFileName();
		}
		setFqFile(leftFqFile, rightFqFile);
	}
	/**
	 * @param outFileName 结果文件名，后缀自动改为sam
	 */
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
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
	private String getMismatch() {
		return "-n " + mismatch + " ";
	}
	public void setChrFile(String chrFile) {
		this.chrFile = chrFile;
	}
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(exePath);
		}
	}
	/** 是否为双端 */
	private boolean isPairend() {
		return pairend;
	}
	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	private String getThreadNum() {
		return "-t " + threadNum + " ";
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
		if (sampleID == null || sampleID.equals("")) {
			sampleGroup = "";
			return;
		}
		this.sampleGroup = " -r " + "\""+"@RG\\tID:" + sampleID;
		if (LibraryName != null && !LibraryName.trim().equals("")) {
			sampleGroup = sampleGroup + "\\tLB:" + LibraryName.trim();
		}
		
		if (SampleName != null && !SampleName.trim().equals(""))
			sampleGroup = sampleGroup + "\\tSM:" + SampleName.trim();
		else
			sampleGroup = sampleGroup + "\\tSM:" + sampleID.trim();
		
		if (Platform != null && !Platform.trim().equals(""))
			sampleGroup = sampleGroup + "\\tPL:" + Platform + "\" ";
		else
			sampleGroup = sampleGroup + "\\tPL:Illumina" + "\" ";
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
	private String getGapLen() {
		return "-e "+gapLength + " ";
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	public void setGapNum(int gapnum) {
		this.gapNum = gapnum;
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	private String getGapNum() {
		return "-o " + gapNum + " ";
	}
	private int getOffset() {
		FastQ fastQ = new FastQ(leftFq);
		return fastQ.getOffset();
	}
	private String getInsertSize() {
		int insertMax = 500;
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return " -a " + insertMax + " ";
		}
		return "";
	}
	private boolean isPairEnd() {
		if (!FileOperate.isFileExist(leftFq) || FileOperate.isFileExist(rightFq)) {
			return false;
		}
		return true;
	}
	/**
	 * 参数设定不能用于solid
	 */
	public SamFile mapReads() {
		outFileName = addSamToFileName(outFileName);
		IndexMake();
//		linux命令如下
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		String cmd = ""; cmd = ExePath + "bwa aln ";
		cmd = cmd + getMismatch() + getGapNum() + getGapLen() + getThreadNum(); //5%的错误率
		cmd = cmd + "-l 25 "; //种子长度
		cmd = cmd + "-O 10 "; //Gap open penalty. gap罚分
		if (getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina 的偏移
		}
		String sai1 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0];
		if (isPairend())
			sai1 = sai1 + "_1.sai"; 
		else
			sai1 = sai1 + ".sai";
		String cmd1 = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(leftFq) + " > " + CmdOperate.addQuot(sai1);
		System.out.println(cmd1);
		cmdOperate = new CmdOperate(cmd1,"bwaMapping1");
		cmdOperate.run();
		
		String sai2 = "";
		if (isPairend()) {
			sai2 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0] + "_2.sai"; 
			String cmd2 = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(rightFq) + " > " + CmdOperate.addQuot(sai2);
			cmdOperate = new CmdOperate(cmd2,"bwaMapping2");
			cmdOperate.run();
		}
	
		////////////////////////这里设定了将基因组读入内存的限制///////////////////////////////////////////////////////////////////
//		双端
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairend()) {
			cmd = this.ExePath + "bwa sampe " + sampleGroup + getInsertSize();
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//将基因组读入内存
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(sai1) + " "  + CmdOperate.addQuot(sai2) + " "  + CmdOperate.addQuot(leftFq) + " "  + CmdOperate.addQuot(rightFq);
			cmd = cmd + " > " + CmdOperate.addQuot(outFileName);
		}
		//这里可能不需要，unique mapping不是在sam文件中设定的
		else {
			cmd = this.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
			cmd = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(sai1) + " "  + CmdOperate.addQuot(leftFq);
			cmd = cmd + " > " +  CmdOperate.addQuot(outFileName);
		}
		cmdOperate = new CmdOperate(cmd,"bwaMappingSAI");
		cmdOperate.run();
		SamFile samFile = new SamFile(outFileName);
		return samFile;
	}
	
	protected static String addSamToFileName(String outFileName) {
		if (outFileName.endsWith(".sam"))
			return outFileName;
		else if (outFileName.endsWith("."))
			return outFileName + "sam";
		else
			return outFileName + ".sam";
	}
	/**
	 * 根据基因组大小判断采用哪种编码方式
	 * @return 已经在前后预留空格，直接添加上idex就好
	 * 小于500MB的用 -a is
	 * 大于500MB的用 -a bwtsw
	 */
	private String getChrLen() {
		long size = (long) FileOperate.getFileSize(chrFile);
		if (size/1024 > 500) {
			return " -a bwtsw ";
		}
		else {
			return " -a is ";
		}
	}

	protected void IndexMake() {
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用
		if (FileOperate.isFileExist(chrFile + ".bwt") == true) {
			return;
		}
		String cmd = this.ExePath + "bwa index ";
		cmd = cmd + getChrLen();//用哪种算法
		//TODO :考虑是否自动判断为solid
		cmd = cmd + CmdOperate.addQuot(chrFile);
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMakeIndex");
		cmdOperate.run();
	}
	
	public void suspend() {
		
	}
}
