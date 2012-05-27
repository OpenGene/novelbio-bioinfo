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
 * ��set�����кܶ���������趨�����趨����Ĭ��
 * @author zong0jie
 *
 */
public class FastQMapBwa extends FastQMapAbs{
	/**
	 * �ڴ˴�С���µ�genomeֱ�Ӷ����ڴ��԰�������mapping
	 * ��λ��KB
	 * �ƺ���ֵ˫�˲�����
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);
	private int mapQ = 10;
	
	String sampleGroup = "";
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
	
	boolean readInMemory = false;
	/**
	 * �Ƿ�index�����ڴ�
	 */
	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}
	/**
	 * @param fastQ
	 * @param outFileName ����ļ���
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
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
	 * ˫��ֻ��unique mapping
	 * @param seqFile1
	 * @param seqFile2 û�о�дnull
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFileName ����ļ���
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
	 */
	public FastQMapBwa(String seqFile1, String seqFile2,
			int FastQFormateOffset, int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		super.uniqMapping = uniqMapping;
		super.outFileName = outFileName;
	}
	/**
	 * ˫��ֻ��unique mapping
	 * @param seqFile1
	 * @param seqFile2 û�о�дnull
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFileName ����ļ���
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFileName ����ļ���
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ���
	 * @param IndexFile
	 */
	public FastQMapBwa(String seqFile1
			, int QUALITY,String outFileName, boolean uniqMapping) {
		super(seqFile1, QUALITY);
		this.outFileName = outFileName;
		this.uniqMapping = uniqMapping;
	}
	
	/**
	 * ��filterReads���õ����˺��FastQ�ļ�����mapping��
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param Qvalue_Num ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ���
	 * int[0][0] = 13  int[0][1] = 7 :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 * �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQSoapMap����ʵ����Ҳ���ǻ�������FastQ�ļ����ѣ�mapping����ļ����䡣
	 * ���Բ���Ҫָ���µ�mapping�ļ�
	 * ������null
	 */
	protected FastQMapBwa createFastQMap(FastQOld fastQ) 
	{
		FastQMapBwa fastQSoapMap= new FastQMapBwa(fastQ.getFileName(), fastQ.getSeqFile2(), fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
		return fastQSoapMap;
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
	/**
	 * �����趨��������solid
	 */
	@Override
	public SamFile mapReads() {
		IndexMake();
//		linux��������
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai
//		bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq 
		
		
		String cmd = ""; cmd = super.ExePath + "bwa aln ";
		cmd = cmd + "-n " + mismatch + " "; //5%�Ĵ�����
		cmd = cmd + "-o 1 "; //һ��gap
		cmd = cmd + "-e " + gapLength + " "; //��gap���5bp��
		cmd = cmd + "-l 25 "; //���ӳ���
		cmd = cmd + "-t 4 "; //4���߳�
		cmd = cmd + "-O 10 "; //Gap open penalty. gap����
		if (getOffset() == FASTQ_ILLUMINA_OFFSET) {
			cmd = cmd + "-I "; //Illumina ��ƫ��
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
	
		////////////////////////�����趨�˽�����������ڴ������///////////////////////////////////////////////////////////////////
//		˫��
//		bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
//		TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
		if (isPairEnd()) {
			cmd = super.ExePath + "bwa sampe " + sampleGroup + "-a " + maxInsert;
			if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
				cmd = cmd + " -P ";//������������ڴ�
			}
			cmd = cmd + " -n 10 -N 10 ";
			cmd = cmd + chrFile + " " + sai1 + " "  + sai2 + " "  + getFileName() + " "  + getSeqFile2();
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
	 * ����bed�ļ��������˫�˾ͷ���˫�˵�bed�ļ�
	 * ����ǵ��˾ͷ����ӳ���bed�ļ���Ĭ���ӳ���extendTo bp
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
	 * ǿ�Ʒ��ص��˵�bed�ļ������ڸ�macs��peak��
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
	 * ���ݻ������С�жϲ������ֱ��뷽ʽ
	 * @return �Ѿ���ǰ��Ԥ���ո�ֱ�������idex�ͺ�
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

	@Override
	protected void IndexMake() {
//		linux�������� 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c ��solid��
		if (FileOperate.isFileExist(chrFile + ".bwt") == true) {
			return;
		}
		String cmd = super.ExePath + "bwa index ";
		cmd = cmd + getChrLen();//�������㷨
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + chrFile;
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("bwaMakeIndex");
	}
	
	/**
	 * ���˵�����reads
	 */
	public FastQMapBwa filterReads(String fileFilterOut)
	{
		return (FastQMapBwa) super.filterReads(fileFilterOut);
	}
	
	
	
}
