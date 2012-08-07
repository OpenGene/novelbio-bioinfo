package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQfile {
	private static Logger logger = Logger.getLogger(FastQ.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * ˫�˵�ʱ��ֻ���������ж��ǺõĲű���
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	
	FastQRead fastQRead = new FastQRead();
	FastQwrite fastQwrite = new FastQwrite();
	FastQfilter fastQfilter = new FastQfilter();
	/** Ĭ���Ƕ�ȡ */
	public FastQfile(String fastqFile) {
		fastQRead.setFastqFile(fastqFile);
	}
	public void setOffset(int offset) {
		fastQRead.setOffset(offset);
	}
	public void getOffset() {
		fastQRead.getOffset();
	}
	public void setFilterParam(FastQfilterRecord fastQfilterRecordParam) {
		fastQfilter.setFilterParam(fastQfilterRecordParam);
	}
	public void setFastqRead(String fileName) {
		fastQRead.setFastqFile(fileName);
	}
	public void setFastqWrite(String fileName) {
		fastQwrite.setFastqFile(fileName);
	}
	public Iterable<FastQRecord> readlines() {
		return fastQRead.readlines();
	}
	public Iterable<FastQRecord> readlines(int startLines) {
		return fastQRead.readlines(startLines);
	}
	public ArrayList<FastQRecord> readHeadLines(int num) {
		return fastQRead.readHeadLines(num);
	}
	public FastQfile filterReads(FastQfilterRecord fastQfilterRecordParam) {
		fastQfilter.setFastQRead(fastQRead);
		
		if (fastQwrite == null)
			fastQwrite = new FastQwrite();
		
		if (fastQwrite.getFastqFile() == null) {
			String fastqWriteFile = FileOperate.changeFileSuffix(fastQRead.getFileName(), "_Filtered", "fastq");
			fastQwrite.setFastqFile(fastqWriteFile);
		}
		fastQfilter.setFastqWrite(fastQwrite);
		fastQfilter.setFilterParam(fastQfilterRecordParam);
		fastQfilter.setFilterThreadNum(5);
		fastQfilter.execute();
		close();
		FastQfile fastQfile = new FastQfile(fastQwrite.getFastqFile());
		return fastQfile;
	}
	/** �ڽ���filter��ʱ��Ҳ���Ե���gui���в����� */
	public FastQfilter getFastQfilter() {
		return fastQfilter;
	}
	public void writeFastQRecord(FastQRecord fastQRecord) {
		fastQwrite.writeFastQRecord(fastQRecord);
	}
	public void close() {
		fastQRead.close();
		fastQwrite.close();
	}
	/**
	 * ��fastq�ļ�ת��Ϊfasta�ļ�<br>
	 * �������ļ�Ϊ���ˣ� fastaFile<br>
	 * ˫�ˣ� ����к�׺��: ��fasta.aa<br>
	 * ��Ϊ fasta.aa �� fasta2.aa<br>
	 * û�к�׺����Ϊ fasta �� fasta2<br>
	 * @param fastaFile
	 * @throws Exception 
	 */
	public void convertToFasta(String fastaFile) {
		TxtReadandWrite txtFasta = new TxtReadandWrite(fastaFile, true);
		for (FastQRecord fastQRecord : fastQRead.readlines()) {
			txtFasta.writefile(fastQRecord.getSeqFasta().toStringNRfasta());
		}
		txtFasta.close();
	}
	/**
	 * �����ļ�������� �ļ���.fasta �� �ļ���.
	 * @param fileName
	 * @param illuminaOffset �Ƿ�Ϊillumina��offset
	 */
	public static void convertSff2FastQ(String fastaFile, boolean illuminaOffset) {
		int offset = FASTQ_SANGER_OFFSET;
		if (illuminaOffset)
			offset = FASTQ_ILLUMINA_OFFSET;		

		String fastaQuality = fastaFile + ".qual";
		String fastQ = FileOperate.changeFileSuffix(fastaFile, null, "fastq");
		TxtReadandWrite txtReadFasta = new TxtReadandWrite(fastaFile, false);
		TxtReadandWrite txtReadQualtiy = new TxtReadandWrite(fastaQuality, false);
		TxtReadandWrite txtOutFastQ = new TxtReadandWrite(fastQ, true);
		
		Iterator<String> txtQuality = txtReadQualtiy.readlines().iterator();
		//����������Ϊÿ����Ϊһ����Ԫ
		int num = 0;
		String title = ""; String fasta = ""; String quality = "";
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//������
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sffת��������������fasta��quality�ǲ�������ͬһ���ļ�");
				}
				title = "@" + contentFasta.substring(1);
				num++;
			}
			//��������
			else if (num == 1) {
				fasta = contentFasta;
				quality = convert2Phred(contentQuality, offset);
				String tmpOut = title + TxtReadandWrite.ENTER_LINUX + fasta + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + quality;
				txtOutFastQ.writefileln(tmpOut);
				num = 0;
			}
		}
		txtOutFastQ.close();
	}
	/**
	 * ����һϵ��offset��������ת��Ϊfastq��quality��
	 * @param illumina �Ƿ���illumina��offset 
	 * @return
	 */
	private static String convert2Phred(String qualityNum, int offset) {
		String[] quality = qualityNum.split(" ");
		char[] tmpResultChar = new char[quality.length];
		for (int i = 0; i < quality.length; i++) {
			String string = quality[i];
			tmpResultChar[i] = (char) (offset + Integer.parseInt(string));
		}
		return String.valueOf(tmpResultChar);
	}
	
	public static HashMap<String, Integer> getMapReadsQuality() {
		HashMap<String, Integer> mapReadsQualtiy = new LinkedHashMap<String, Integer>();
		
		mapReadsQualtiy.put("MidanQuality", QUALITY_MIDIAN);
		mapReadsQualtiy.put("Midan_PEQuality", QUALITY_MIDIAN_PAIREND);
		mapReadsQualtiy.put("HigtQuality", QUALITY_HIGM);
		mapReadsQualtiy.put("LowQuality", QUALITY_LOW);
		mapReadsQualtiy.put("LowQuality454", QUALITY_LOW_454);
		return mapReadsQualtiy;
	}
}
