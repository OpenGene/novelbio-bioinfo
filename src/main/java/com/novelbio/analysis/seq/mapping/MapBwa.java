package com.novelbio.analysis.seq.mapping;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 在set里面有很多参数可以设定，不设定就用默认
 * @author zong0jie
 *
 */
public class MapBwa extends MapDNA {
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

	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** 含有几个gap */
	int gapNum = 1;
	/** gap的长度 */
	int gapLength = 20;
	/** 线程数量 */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** 是否将index读入内存，仅对双端有效 */
	boolean readInMemory = true;
	
	public MapBwa() {}
	/**
	 * @param fastQ
	 * @param outFileName 结果文件名，后缀自动改为sam
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	public MapBwa(FastQ fastQ, String outFileName) {
		this.outFileName = outFileName;
		leftFq = fastQ.getReadFileName();
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
	}
	/**
	 * @param seqFile1
	 * @param outFileName 结果文件名，后缀自动改为sam
	 * @param uniqMapping 是否unique mapping
	 */
	public MapBwa(String seqFile,String outFileName) {
		leftFq = seqFile;
		this.outFileName = outFileName;
	}
	/** 输入已经过滤好的fastq文件 */
	public void setFqFile(String leftFq, String rightFq) {
		if (FileOperate.isFileExistAndBigThanSize(leftFq, 1) && FileOperate.isFileExistAndBigThanSize(rightFq, 1)) {
			this.leftFq = leftFq;
			this.rightFq = rightFq;
		}
		else if (FileOperate.isFileExistAndBigThanSize(leftFq, 1)) {
			this.leftFq = leftFq;
		}
		else if (FileOperate.isFileExistAndBigThanSize(rightFq, 1)) {
			this.leftFq = rightFq;
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
		this.sampleGroup = "@RG\\tID:" + sampleID;
		if (LibraryName != null && !LibraryName.trim().equals("")) {
			sampleGroup = sampleGroup + "\\tLB:" + LibraryName.trim();
		}
		
		if (SampleName != null && !SampleName.trim().equals("")) {
			sampleGroup = sampleGroup + "\\tSM:" + SampleName.trim();
		} else {
			sampleGroup = sampleGroup + "\\tSM:" + sampleID.trim();
		}
		
		if (Platform != null && !Platform.trim().equals("")) {
			sampleGroup = sampleGroup + "\\tPL:" + Platform + "\" ";
		} else {
			sampleGroup = sampleGroup + "\\tPL:Illumina";
		}
		sampleGroup = " -r " + CmdOperate.addQuot(sampleGroup) + " ";
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
		if (!FileOperate.isFileExist(leftFq) || !FileOperate.isFileExist(rightFq)) {
			return false;
		}
		return true;
	}
	/** 种子长度 */
	private String getSeedSize() {
		return " -l 25 ";
	}
	/**
	 * gap罚分
	 * @return
	 */
	private String getOpenPanalty() {
		return " -O 10 ";
	}
	/**
	 * 是illumina32标准还是64标准
	 * @return
	 */
	private String getFastQoffset() {
		FastQ fastQ = new FastQ(leftFq);
		int offset = fastQ.getOffset();
		if (offset == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return  " -I ";
		}
		return "";
	}
	/**
	 * 返回sai的信息, <b>不加引号</b>
	 * @param Sai1orSai2 双端的话，sai1就输入1，sai2就输入2。单端sai也输入1
	 * @return
	 */
	private String getSai(int Sai1orSai2) {
		String sai = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0];
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
	 * @return
	 */
	private String readInMemory() {
		if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
			return " -P ";
		}
		return "";
	}
	/**
	 * 参数设定不能用于solid
	 */
	public SamFile mapReads() {
		outFileName = addSamToFileName(outFileName);
		IndexMake();
		bwaAln();
		bwaSamPeSe();

		return copeAfterMapping(outFileName);
	}
	
	/**
	 * linux命令如下<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai<br>
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq
	 */
	private void bwaAln() {
		String cmd = ""; cmd = ExePath + "bwa aln ";
		cmd = cmd + getMismatch() + getGapNum() + getGapLen() + getThreadNum()
				+ getSeedSize() + getOpenPanalty() + getFastQoffset();
		
		String cmd1 = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(leftFq) + " > " + CmdOperate.addQuot(getSai(1));
		cmdOperate = new CmdOperate(cmd1,"bwaMapping1");
		cmdOperate.run();
		
		if (isPairEnd()) {
			String cmd2 = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(rightFq) + " > " + CmdOperate.addQuot(getSai(2));
			cmdOperate = new CmdOperate(cmd2,"bwaMapping2");
			cmdOperate.run();
		}
	}
	/**
	 * 这里设定了将基因组读入内存的限制
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
	 * TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
	 */
	private void bwaSamPeSe() {
		String cmd = "";
		if (isPairEnd()) {
			cmd = this.ExePath + "bwa sampe " + sampleGroup + getInsertSize() + readInMemory();
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(getSai(1)) + " "  + CmdOperate.addQuot(getSai(2)) + " "  + CmdOperate.addQuot(leftFq) + " "  + CmdOperate.addQuot(rightFq);
			cmd = cmd + " > " + CmdOperate.addQuot(outFileName);
		}
		//这里可能不需要，unique mapping不是在sam文件中设定的
		else {
			cmd = this.ExePath + "bwa samse " + sampleGroup + "-n 50 ";
			cmd = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(getSai(1)) + " "  + CmdOperate.addQuot(leftFq);
			cmd = cmd + " > " +  CmdOperate.addQuot(outFileName);
		}
		cmdOperate = new CmdOperate(cmd,"bwaMappingSAI");
		cmdOperate.run();
	}
	
	protected static String addSamToFileName(String outFileName) {
		if (outFileName.endsWith(".sam")) {
			return outFileName;
		} else if (outFileName.endsWith(".")) {
			return outFileName + "sam";
		} else {
			return outFileName + ".sam";
		}
	}
	private SamFile copeAfterMapping(String outSamFile) {
		if (!FileOperate.isFileExistAndBigThanSize(outSamFile, 1)) {
			return null;
		}
		SamFile samFile = new SamFile(outFileName);
		SamFile bamFile = samFile.convertToBam();
		samFile.close();
		deleteFile(samFile, bamFile);
		return bamFile;
	}
	
	/**
	 * 删除sai文件
	 * @param samFileName
	 */
	private void deleteFile(SamFile samFile, SamFile bamFile) {
		FileOperate.DeleteFileFolder(getSai(1));
		if (isPairEnd()) {
			FileOperate.DeleteFileFolder(getSai(2));
		}
		double samFileSize = FileOperate.getFileSize(samFile.getFileName());
		if (FileOperate.isFileExistAndBigThanSize(bamFile.getFileName(), samFileSize/15)) {
			FileOperate.delFile(samFile.getFileName());
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
	public void suspend() {
		
	}
}
