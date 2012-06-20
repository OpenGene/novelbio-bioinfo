package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.FastQOld;
import com.novelbio.base.cmd.CMDcallback;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 在set里面有很多参数可以设定，不设定就用默认
 * @author zong0jie
 *
 */
public class MapBwa {
	public static void main(String[] args) {
		
	}
	CmdOperate cmdOperate = null;
	/** bwa所在路径 */
	String ExePath = "";
	String chrFile = "";
	/**
	 * 在此大小以下的genome直接读入内存以帮助快速mapping
	 * 单位，KB
	 * 似乎该值双端才有用
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);
	boolean uniqMapping = true;
	String sampleGroup = "";
	String outFileName = "";
	String leftFq = "";
	String rightFq = "";
	boolean pairend = false;
	/** 默认是solexa的最短插入  */
	int minInsert = 0;
	/**  默认是solexa的最长插入  */
	int maxInsert = 500;
	/** 含有几个gap */
	int gapNum = 1;
	/** gap的长度 */
	int gapLength = 3;
	/** 线程数量 */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "2";

	/** 是否将index读入内存，仅对双端有效 */
	boolean readInMemory = false;

	/**
	 * @param fastQ
	 * @param outFileName 结果文件名
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	public MapBwa(FastQOld fastQ, String outFileName, boolean uniqMapping ) {
		this.uniqMapping = uniqMapping;
		this.outFileName = outFileName;
		leftFq = fastQ.getFileName();
		pairend = false;
	}
	/**
	 * 双端只做unique mapping
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFileName 结果文件名
	 */
	public MapBwa(String seqFile1, String seqFile2, String outFileName) {
		leftFq = seqFile1;
		rightFq = seqFile2;
		this.outFileName = outFileName;
		pairend = true;
	}
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件名
	 * @param IndexFile
	 */
	public MapBwa(String seqFile,String outFileName, boolean uniqMapping) {
		leftFq = seqFile;
		this.outFileName = outFileName;
		this.uniqMapping = uniqMapping;
		pairend = false;
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
	 * @param mismatch
	 */
	private String getMismatch() {
		return "-n " + mismatch + " ";
	}
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePath, String chrFile) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	
		this.chrFile = chrFile;
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
	/**
	 * bwa中不考虑minInsertLen
	 * 设定插入片段长度，默认是solexa的长度，150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen) {
		this.minInsert = minInsertLen;
		this.maxInsert = maxInsertLen;
	}
	/**
	 * 本次mapping的组
	 * @param sampleGroup
	 * "@RQ\tID:< ID >\tLB:< LIBRARY_NAME >\tSM:< SAMPLE_NAME >\tPL:ILLUMINA" <br>
	 * example: "@RG\tID:Exome1\tLB:Exome1\tSM:Exome1\tPL:ILLUMINA"
	 */
	public void setSampleGroup(String sampleGroup) {
		if (sampleGroup.trim().equals("")) {
			this.sampleGroup = "";
		}
		else {
			this.sampleGroup = " -r " + "\""+sampleGroup+"\"" + " ";
		}
	}
	

	/**
	 * 默认gap为3，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}
	/**
	 * 默认gap为3，如果是indel查找的话，设置到5或者6比较合适
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
		FastQ fastQ = new FastQ(leftFq, FastQ.QUALITY_MIDIAN);
		return fastQ.getOffset();
	}
	/**
	 * 参数设定不能用于solid
	 */
	public SamFile mapReads() {
		IndexMake();
//		linux命令如下
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		String cmd = ""; cmd = ExePath + "bwa aln ";
		cmd = cmd + getMismatch() + getGapNum() + getGapLen() + getThreadNum(); //5%的错误率
		cmd = cmd + "-l 25 "; //种子长度
		cmd = cmd + "-O 10 "; //Gap open penalty. gap罚分
		if (getOffset() == FastQOld.FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina 的偏移
		}
		String sai1 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0];
		if (isPairend())
			sai1 = sai1 + "_1.sai"; 
		else
			sai1 = sai1 + ".sai";
		String cmd1 = cmd + chrFile + " " + leftFq + " > " + sai1;
		System.out.println(cmd1);
		cmdOperate = new CmdOperate(cmd1,"bwaMapping1");
		cmdOperate.run();
		
		String sai2 = "";
		if (isPairend()) {
			sai2 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0] + "_2.sai"; 
			String cmd2 = cmd + chrFile + " " + rightFq + " > " + sai2;
			System.out.println(cmd2);
			cmdOperate = new CmdOperate(cmd2,"bwaMapping2");
			cmdOperate.run();
		}
	
		////////////////////////这里设定了将基因组读入内存的限制///////////////////////////////////////////////////////////////////
//		双端
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairend()) {
			cmd = this.ExePath + "bwa sampe " + sampleGroup + "-a " + maxInsert;
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//将基因组读入内存
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + chrFile + " " + sai1 + " "  + sai2 + " "  + leftFq + " "  + rightFq;
			cmd = cmd + " > " + outFileName;
		}
		//这里可能不需要，unique mapping不是在sam文件中设定的
		else {
			cmd = this.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
			cmd = cmd + chrFile + " " + sai1 + " "  + leftFq;
			cmd = cmd + " > " + outFileName;
		}
		System.out.println(cmd);
		cmdOperate = new CmdOperate(cmd,"bwaMappingSAI");
		cmdOperate.run();
		SamFile samFile = new SamFile(outFileName);
		samFile.setPairend(isPairend());
		samFile.setUniqMapping(uniqMapping);
		return samFile;
	}
	
	/**
	 * 根据基因组大小判断采用哪种编码方式
	 * @return 已经在前后预留空格，直接添加上idex就好
	 * 小于500MB的用 -a is
	 * 大于500MB的用 -a bwtsw
	 */
	private String getChrLen()
	{
		TxtReadandWrite txt = new TxtReadandWrite(chrFile, false);
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
		cmd = cmd + chrFile;
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMakeIndex");
		cmdOperate.run();
	}
	
	public void suspend() {
		
	}
}
