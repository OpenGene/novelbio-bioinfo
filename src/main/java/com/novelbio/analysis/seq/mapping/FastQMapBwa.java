package com.novelbio.analysis.seq.mapping;

import java.util.Date;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
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
public class FastQMapBwa extends FastQMapAbs{
	/**
	 * 在此大小以下的genome直接读入内存以帮助快速mapping
	 * 单位，KB
	 * 似乎该值双端才有用
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);
	private int mapQ = 10;
	
	String sampleGroup = "";
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
	
	boolean readInMemory = false;
	/**
	 * 是否将index读入内存
	 */
	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}
	/**
	 * @param fastQ
	 * @param outFileName 结果文件名
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	protected FastQMapBwa(FastQOld fastQ, String outFileName, boolean uniqMapping ) 
	{
		 this(fastQ.getFileName(), fastQ.getSeqFile2(),fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
	}
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "2";
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
	 * 双端只做unique mapping
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFileName 结果文件名
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	public FastQMapBwa(String seqFile1, String seqFile2,
			int FastQFormateOffset, int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		super.uniqMapping = uniqMapping;
		super.outFileName = outFileName;
	}
	/**
	 * 双端只做unique mapping
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFileName 结果文件名
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 * @param IndexFile
	 */
	public FastQMapBwa(String seqFile1, String seqFile2,
			 int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, seqFile2, QUALITY);
		super.uniqMapping = uniqMapping;
		super.outFileName = outFileName;
	}
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFileName 结果文件名
	 * @param uniqMapping
	 */
	public FastQMapBwa(String seqFile1,
			int FastQFormateOffset, int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, null, FastQFormateOffset, QUALITY);
		super.uniqMapping = uniqMapping;
		super.outFileName = outFileName;
	}
	
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件名
	 * @param IndexFile
	 */
	public FastQMapBwa(String seqFile1
			, int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, QUALITY);
		this.outFileName = outFileName;
		this.uniqMapping = uniqMapping;
	}
	
	/**
	 * 先filterReads，得到过滤后的FastQ文件后，再mapping，
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param Qvalue_Num 二维数组 每一行代表一个Qvalue 以及最多出现的个数
	 * int[0][0] = 13  int[0][1] = 7 :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut 结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 * 分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQSoapMap，其实里面也就是换了两个FastQ文件而已，mapping结果文件不变。
	 * 所以不需要指定新的mapping文件
	 * 出错返回null
	 */
	protected FastQMapBwa createFastQMap(FastQOld fastQ) 
	{
		FastQMapBwa fastQSoapMap= new FastQMapBwa(fastQ.getFileName(), fastQ.getSeqFile2(), fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
		return fastQSoapMap;
	}
	
	
	
	
	private int gapLength = 3;
	/**
	 * 默认gap为3，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}

	public void setMapQ(int mapQ) {
		this.mapQ = mapQ;
	}
	/**
	 * 参数设定不能用于solid
	 */
	@Override
	public SamFile mapReads() {
		IndexMake();
//		linux命令如下
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		
		
		String cmd = ""; cmd = super.ExePath + "bwa aln ";
		cmd = cmd + "-n " + mismatch + " "; //5%的错误率
		cmd = cmd + "-o 1 "; //一个gap
		cmd = cmd + "-e " + gapLength + " "; //该gap最多5bp长
		cmd = cmd + "-l 25 "; //种子长度
		cmd = cmd + "-t 4 "; //4个线程
		cmd = cmd + "-O 10 "; //Gap open penalty. gap罚分
		if (getOffset() == FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina 的偏移
		}
		
		String sai1 = FileOperate.changeFileSuffix(getFileName(),"_1","sai");
		sai1 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(sai1)[0] + ".sai"; 
		String cmd1 = cmd + chrFile + " " + getFileName() + " > " + sai1;
		System.out.println(cmd1);
		CmdOperate cmdOperate = new CmdOperate(cmd1);
		cmdOperate.doInBackground("bwaMapping1");
		
		String sai2 = "";
		if (isPairEnd()) {
			sai2 = FileOperate.changeFileSuffix(getSeqFile2(),"_2","sai");
			sai2 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(sai2)[0] + ".sai"; 
			String cmd2 = cmd + chrFile + " " + getSeqFile2() + " > " + sai2;
			System.out.println(cmd2);
			cmdOperate = new CmdOperate(cmd2);
			cmdOperate.doInBackground("bwaMapping2");
		}
	
		////////////////////////这里设定了将基因组读入内存的限制///////////////////////////////////////////////////////////////////
//		双端
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairEnd()) {
			cmd = super.ExePath + "bwa sampe " + sampleGroup + "-a " + maxInsert;
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//将基因组读入内存
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + chrFile + " " + sai1 + " "  + sai2 + " "  + getFileName() + " "  + getSeqFile2();
			cmd = cmd + " > " + outFileName;
		}
		//这里可能不需要，unique mapping不是在sam文件中设定的
		else {
//			if (uniqMapping) {
//				cmd = super.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
//			}
//			else {
//				cmd = super.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
//			}
			cmd = super.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
			
			
			cmd = cmd + chrFile + " " + sai1 + " "  + getFileName();
			cmd = cmd + " > " + outFileName;
		}
		System.out.println(cmd);
		cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMappingSAI");
		SamFile samFile = new SamFile(outFileName);
		samFile.setPairend(isPairEnd());
		samFile.setUniqMapping(uniqMapping);
		return samFile;
	}
	/**
	 * 返回bed文件，如果是双端就返回双端的bed文件
	 * 如果是单端就返回延长的bed文件，默认延长至extendTo bp
	 * @return
	 */
	@Override
	public BedSeq getBedFile(String bedFile) {
		if (!FileOperate.isFileExist(outFileName)) { 
			mapReads();
		}
		SAMtools saMtools = new SAMtools(outFileName, isPairEnd(), mapQ);
		saMtools.setGetSeqName(true);
		if (isPairEnd()) {
			return saMtools.sam2bed(compressOutType, bedFile, uniqMapping);
		}
		else {
			BedSeq bedSeq = saMtools.sam2bed(compressOutType, FileOperate.changeFileSuffix(bedFile, "_fromSam", "bed"), uniqMapping);
			return bedSeq.extend(extendTo, bedFile);
		}
	}
	
	/**
	 * 强制返回单端的bed文件，用于给macs找peak用
	 * @return
	 */
	@Override
	public BedSeq getBedFileSE(String bedFile) {
		if (!FileOperate.isFileExist(outFileName)) { 
			mapReads();
		}
		SAMtools saMtools = new SAMtools(outFileName, false, mapQ);
		return saMtools.sam2bed(compressOutType, bedFile, uniqMapping);
		
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

	@Override
	protected void IndexMake() {
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用
		if (FileOperate.isFileExist(chrFile + ".bwt") == true) {
			return;
		}
		String cmd = super.ExePath + "bwa index ";
		cmd = cmd + getChrLen();//用哪种算法
		//TODO :考虑是否自动判断为solid
		cmd = cmd + chrFile;
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMakeIndex");
	}
	
	/**
	 * 过滤低质量reads
	 */
	public FastQMapBwa filterReads(String fileFilterOut)
	{
		return (FastQMapBwa) super.filterReads(fileFilterOut);
	}
	
	
	
}
