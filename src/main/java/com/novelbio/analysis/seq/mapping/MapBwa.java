package com.novelbio.analysis.seq.mapping;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ��set�����кܶ���������趨�����趨����Ĭ��
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
	 * �ڴ˴�С���µ�genomeֱ�Ӷ����ڴ��԰�������mapping
	 * ��λ��KB
	 * �ƺ���ֵ˫�˲�����
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	
	CmdOperate cmdOperate = null;
	/** bwa����·�� */
	String ExePath = "";
	String chrFile;
	String sampleGroup = "";
	String outFileName = "";
	String leftFq = "";
	String rightFq = "";
	boolean pairend = false;

	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** ���м���gap */
	int gapNum = 1;
	/** gap�ĳ��� */
	int gapLength = 6;
	/** �߳����� */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** �Ƿ�index�����ڴ棬����˫����Ч */
	boolean readInMemory = false;
	
	public MapBwa() {}
	/**
	 * @param fastQ
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
	 */
	public MapBwa(FastQ fastQ, String outFileName) {
		this.outFileName = outFileName;
		leftFq = fastQ.getReadFileName();
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
	public MapBwa(String seqFile,String outFileName) {
		leftFq = seqFile;
		this.outFileName = outFileName;
		pairend = false;
	}
	/** �����Ѿ����˺õ�fastq�ļ� */
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
	/** �����Ѿ����˺õ�fastq�ļ� */
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
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 */
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
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
	 * @param mismatchScore
	 */
	private String getMismatch() {
		return "-n " + mismatch + " ";
	}
	public void setChrFile(String chrFile) {
		this.chrFile = chrFile;
	}
	/**
	 * �趨bwa���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(exePath);
		}
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
	
	public void setMapLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	/**
	 * ����mapping���飬���в����������пո�
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
	 * Ĭ��gapΪ4�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}
	/**
	 * Ĭ��gapΪ4�������indel���ҵĻ������õ�5����6�ȽϺ���
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
		if (getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina ��ƫ��
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
	
		////////////////////////�����趨�˽�����������ڴ������///////////////////////////////////////////////////////////////////
//		˫��
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairend()) {
			cmd = this.ExePath + "bwa sampe " + sampleGroup + getInsertSize();
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//������������ڴ�
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + CmdOperate.addQuot(chrFile) + " " + CmdOperate.addQuot(sai1) + " "  + CmdOperate.addQuot(sai2) + " "  + CmdOperate.addQuot(leftFq) + " "  + CmdOperate.addQuot(rightFq);
			cmd = cmd + " > " + CmdOperate.addQuot(outFileName);
		}
		//������ܲ���Ҫ��unique mapping������sam�ļ����趨��
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
	 * ���ݻ������С�жϲ������ֱ��뷽ʽ
	 * @return �Ѿ���ǰ��Ԥ���ո�ֱ�������idex�ͺ�
	 * С��500MB���� -a is
	 * ����500MB���� -a bwtsw
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
//		linux�������� 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c ��solid��
		if (FileOperate.isFileExist(chrFile + ".bwt") == true) {
			return;
		}
		String cmd = this.ExePath + "bwa index ";
		cmd = cmd + getChrLen();//�������㷨
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + CmdOperate.addQuot(chrFile);
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMakeIndex");
		cmdOperate.run();
	}
	
	public void suspend() {
		
	}
}
