package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
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
 * ��set�����кܶ���������趨�����趨����Ĭ��
 * @author zong0jie
 *
 */
public class MapBwa {
	/** bwa����·�� */
	String ExePath = "";
	String chrFile = "";
	/**
	 * �ڴ˴�С���µ�genomeֱ�Ӷ����ڴ��԰�������mapping
	 * ��λ��KB
	 * �ƺ���ֵ˫�˲�����
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);
	private int mapQ = 10;
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
	 * @param outFileName ����ļ���
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
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFileName ����ļ���
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ���
	 * @param IndexFile
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
	
	public void setExePath(String exePath, String chrFile) {
		this.ExePath = exePath;
		this.chrFile = chrFile;
	}
	
	private boolean isPairend() {
		return pairend;
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
	
	private int gapLength = 3;
	/**
	 * Ĭ��gapΪ3�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}

	public void setMapQ(int mapQ) {
		this.mapQ = mapQ;
	}
	private int getOffset() {
		FastQOld fastQ = new FastQOld(leftFq, FastQOld.QUALITY_MIDIAN);
		return fastQ.getOffset();
	}
	/**
	 * �����趨��������solid
	 */
	public SamFile mapReads() {
		IndexMake();
//		linux��������
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		
		
		String cmd = ""; cmd = ExePath + "bwa aln ";
		cmd = cmd + "-n " + mismatch + " "; //5%�Ĵ�����
		cmd = cmd + "-o 1 "; //һ��gap
		cmd = cmd + "-e " + gapLength + " "; //��gap���5bp��
		cmd = cmd + "-l 25 "; //���ӳ���
		cmd = cmd + "-t 4 "; //4���߳�
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
		CmdOperate cmdOperate = new CmdOperate(cmd1);
		cmdOperate.doInBackground("bwaMapping1");
		
		String sai2 = "";
		if (isPairend()) {
			sai2 = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0] + "_2.sai"; 
			String cmd2 = cmd + chrFile + " " + rightFq + " > " + sai2;
			System.out.println(cmd2);
			cmdOperate = new CmdOperate(cmd2);
			cmdOperate.doInBackground("bwaMapping2");
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
//			if (uniqMapping) {
//				cmd = super.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
//			}
//			else {
//				cmd = super.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
//			}
			cmd = this.ExePath + "bwa samse " + sampleGroup + "-n 100 ";
			
			
			cmd = cmd + chrFile + " " + sai1 + " "  + leftFq;
			cmd = cmd + " > " + outFileName;
		}
		System.out.println(cmd);
		cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMappingSAI");
		SamFile samFile = new SamFile(outFileName);
		samFile.setPairend(isPairend());
		samFile.setUniqMapping(uniqMapping);
		return samFile;
	}
	
	/**
	 * ���ݻ������С�жϲ������ֱ��뷽ʽ
	 * @return �Ѿ���ǰ��Ԥ���ո�ֱ��������idex�ͺ�
	 * С��500MB���� -a is
	 * ����500MB���� -a bwtsw
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
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMakeIndex");
	}
	
}