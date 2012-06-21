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
 * ��set�����кܶ���������趨�����趨����Ĭ��
 * @author zong0jie
 *
 */
public class MapBwa {
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);
	/**
	 * �ڴ˴�С���µ�genomeֱ�Ӷ����ڴ��԰�������mapping
	 * ��λ��KB
	 * �ƺ���ֵ˫�˲�����
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	
	CmdOperate cmdOperate = null;
	/** bwa����·�� */
	String ExePath = "";
	String chrFile;

	boolean uniqMapping = true;
	String sampleGroup = "";
	String outFileName = "";
	String leftFq = "";
	String rightFq = "";
	boolean pairend = false;
	/** Ĭ����solexa����̲���  */
	int minInsert = 0;
	/**  Ĭ����solexa�������  */
	int maxInsert = 500;
	/** ���м���gap */
	int gapNum = 1;
	/** gap�ĳ��� */
	int gapLength = 3;
	/** �߳����� */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "2";

	/** �Ƿ�index�����ڴ棬����˫����Ч */
	boolean readInMemory = false;

	/**
	 * @param fastQ
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
	 */
	public MapBwa(FastQOld fastQ, String outFileName, boolean uniqMapping ) {
		this.uniqMapping = uniqMapping;
		this.outFileName = outFileName;
		leftFq = fastQ.getFileName();
		pairend = false;
	}
	/**
	 * ˫��ֻ��unique mapping
	 * @param seqFile1
	 * @param seqFile2 û�о�дnull
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 */
	public MapBwa(String seqFile1, String seqFile2, String outFileName) {
		leftFq = seqFile1;
		rightFq = seqFile2;
		this.outFileName = outFileName;
		pairend = true;
	}
	/**
	 * @param seqFile1
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 * @param uniqMapping �Ƿ�unique mapping
	 */
	public MapBwa(String seqFile,String outFileName, boolean uniqMapping) {
		leftFq = seqFile;
		this.outFileName = outFileName;
		this.uniqMapping = uniqMapping;
		pairend = false;
	}
	/**
	 * �ٷ�֮���ٵ�mismatch�����߼���mismatch
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
	 * �ٷ�֮���ٵ�mismatch�����߼���mismatch
	 * @param mismatch
	 */
	private String getMismatch() {
		return "-n " + mismatch + " ";
	}
	/**
	 * �趨bwa���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePath, String chrFile) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	
		this.chrFile = chrFile;
	}
	/** �Ƿ�Ϊ˫�� */
	private boolean isPairend() {
		return pairend;
	}
	/** �߳�������Ĭ��4�߳� */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	private String getThreadNum() {
		return "-t " + threadNum + " ";
	}
	/**
	 * �Ƿ�index�����ڴ棬����˫�˲�������
	 */
	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}
	/**
	 * bwa�в�����minInsertLen
	 * �趨����Ƭ�γ��ȣ�Ĭ����solexa�ĳ��ȣ�150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen) {
		this.minInsert = minInsertLen;
		this.maxInsert = maxInsertLen;
	}
	/**
	 * ����mapping����
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
	 * Ĭ��gapΪ3�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}
	/**
	 * Ĭ��gapΪ3�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	private String getGapLen() {
		return "-e "+gapLength + " ";
	}
	/** �ȶԵ�ʱ��������༸��gap Ĭ��Ϊ1��1���͹��ˣ����ǳ����ر𳤻�����454*/
	public void setGapNum(int gapnum) {
		this.gapNum = gapnum;
	}
	/** �ȶԵ�ʱ��������༸��gap Ĭ��Ϊ1��1���͹��ˣ����ǳ����ر𳤻�����454*/
	private String getGapNum() {
		return "-o " + gapNum + " ";
	}
	private int getOffset() {
		FastQ fastQ = new FastQ(leftFq, FastQ.QUALITY_MIDIAN);
		return fastQ.getOffset();
	}
	/**
	 * �����趨��������solid
	 */
	public SamFile mapReads() {
		outFileName = addSamToFileName(outFileName);
		IndexMake();
//		linux��������
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		String cmd = ""; cmd = ExePath + "bwa aln ";
		cmd = cmd + getMismatch() + getGapNum() + getGapLen() + getThreadNum(); //5%�Ĵ�����
		cmd = cmd + "-l 25 "; //���ӳ���
		cmd = cmd + "-O 10 "; //Gap open penalty. gap����
		if (getOffset() == FastQOld.FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina ��ƫ��
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
	
		////////////////////////�����趨�˽�����������ڴ������///////////////////////////////////////////////////////////////////
//		˫��
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairend()) {
			cmd = this.ExePath + "bwa sampe " + sampleGroup + "-a " + maxInsert;
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//������������ڴ�
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + chrFile + " " + sai1 + " "  + sai2 + " "  + leftFq + " "  + rightFq;
			cmd = cmd + " > " + outFileName;
		}
		//������ܲ���Ҫ��unique mapping������sam�ļ����趨��
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
	private String addSamToFileName(String outFileName) {
		if (outFileName.endsWith(".sam"))
			return outFileName;
		else if (outFileName.endsWith("."))
			return outFileName + "sam";
		else
			return outFileName + ".sam";
	}
	/**
	 * ���ݻ������С�жϲ������ֱ��뷽ʽ
	 * @return �Ѿ���ǰ��Ԥ���ո�ֱ�������idex�ͺ�
	 * С��500MB���� -a is
	 * ����500MB���� -a bwtsw
	 */
	private String getChrLen() {
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
//		linux�������� 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c ��solid��
		if (FileOperate.isFileExist(chrFile + ".bwt") == true) {
			return;
		}
		String cmd = this.ExePath + "bwa index ";
		cmd = cmd + getChrLen();//�������㷨
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + chrFile;
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMakeIndex");
		cmdOperate.run();
	}
	
	public void suspend() {
		
	}
}
