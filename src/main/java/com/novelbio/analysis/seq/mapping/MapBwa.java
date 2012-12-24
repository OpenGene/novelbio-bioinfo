package com.novelbio.analysis.seq.mapping;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ��set�����кܶ���������趨�����趨����Ĭ��
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

	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** ���м���gap */
	int gapNum = 1;
	/** gap�ĳ��� */
	int gapLength = 20;
	/** �߳����� */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** �Ƿ�index�����ڴ棬����˫����Ч */
	boolean readInMemory = true;
	
	public MapBwa() {}
	/**
	 * @param fastQ
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
	 */
	public MapBwa(FastQ fastQ, String outFileName) {
		this.outFileName = outFileName;
		leftFq = fastQ.getReadFileName();
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
	}
	/**
	 * @param seqFile1
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 * @param uniqMapping �Ƿ�unique mapping
	 */
	public MapBwa(String seqFile,String outFileName) {
		leftFq = seqFile;
		this.outFileName = outFileName;
	}
	/** �����Ѿ����˺õ�fastq�ļ� */
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
	/** ���ӳ��� */
	private String getSeedSize() {
		return " -l 25 ";
	}
	/**
	 * gap����
	 * @return
	 */
	private String getOpenPanalty() {
		return " -O 10 ";
	}
	/**
	 * ��illumina32��׼����64��׼
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
	 * ����sai����Ϣ, <b>��������</b>
	 * @param Sai1orSai2 ˫�˵Ļ���sai1������1��sai2������2������saiҲ����1
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
	 * ���ݻ������С�����ǽ�����������ڴ�
	 * @return
	 */
	private String readInMemory() {
		if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
			return " -P ";
		}
		return "";
	}
	/**
	 * �����趨��������solid
	 */
	public SamFile mapReads() {
		outFileName = addSamToFileName(outFileName);
		IndexMake();
		bwaAln();
		bwaSamPeSe();

		return copeAfterMapping(outFileName);
	}
	
	/**
	 * linux��������<br>
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
	 * �����趨�˽�����������ڴ������
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
		//������ܲ���Ҫ��unique mapping������sam�ļ����趨��
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
	 * ɾ��sai�ļ�
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
	public void suspend() {
		
	}
}
