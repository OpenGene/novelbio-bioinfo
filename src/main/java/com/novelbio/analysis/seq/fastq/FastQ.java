package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQ {

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
	public static int QUALITY_LOW_454 = 60;
	public static int QUALITY_LOW_PGM = 70;
	
	private int threadNum_FilterFastqRecord = 10;
	
	FastQReader fastQRead = new FastQReader();
	FastQwrite fastQwrite = new FastQwrite();
	FastQfilter fastQfilter = new FastQfilter(fastQRead, fastQwrite);
	
	/** Ĭ���Ƕ�ȡ */
	public FastQ(String fastqFile) {
		fastQRead.setFastqFile(fastqFile);
	}
	/** Ĭ���Ƕ�ȡ */
	public FastQ(String fastqFile, boolean createNew) {
		fastQwrite.setFastqFile(fastqFile);
	}
	public void setOffset(int offset) {
		fastQRead.setOffset(offset);
	}
	public int getOffset() {
		return fastQRead.getOffset();
	}
	public void setCompressType(String compressInType, String compressOutType) {
		fastQRead.setCompressType(compressInType);
		fastQwrite.setCompressType(compressOutType);
	}
	public void setFilter(FastQRecordFilter fastQfilterRecord) {
		fastQfilter.setFilter(fastQfilterRecord);
	}
	public void setFastqRead(String fileName) {
		fastQRead.setFastqFile(fileName);
	}
	public void setFastqWrite(String fileName) {
		fastQwrite.setFastqFile(fileName);
	}
	public Iterable<FastQRecord> readlines() {
		return fastQRead.readlines(true);
	}
	/** ��ȡfastq��ʱ���Ƿ��ʼ��
	 * ��Ҫ���ڶ��̹߳���reads��ʱ�򣬿����ڹ���reads��ʱ��Ž��г�ʼ��
	 *  */
	public Iterable<FastQRecord> readlines(boolean initial) {
		return fastQRead.readlines(initial);
	}
	public Iterable<FastQRecord> readlines(int startLines) {
		return fastQRead.readlines(startLines);
	}
	public ArrayList<FastQRecord> readHeadLines(int num) {
		return fastQRead.readHeadLines(num);
	}
	public String getReadFileName() {
		return fastQRead.getFileName();
	}
	public int getReadsLenAvg() {
		return fastQRead.getReadsLenAvg();
	}
	/** ������֮����ܻ�õ�ֵ */
	public long getSeqNum() {
		return fastQRead.readsNum;
	}
	/**
	 * @return null ����
	 */
	public FastQ filterReads() {
		setFilterReadsOutName(true, getOutFileName());
		filterReadsRun();

		FastQ fastQfileOut1 = new FastQ(fastQwrite.getFileName());
		fastQfileOut1.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		return fastQfileOut1;
	}
	/** ˫��reads���� */
	public FastQ[] filterReads(FastQ fastQfile2) {
		fastQRead.setFastQReadMate(fastQfile2.fastQRead);
		fastQwrite.setFastQwriteMate(fastQfile2.fastQwrite);
		
		setFilterReadsOutName(false, getOutFileName());
		filterReadsRun();
		
		FastQ fastQfileOut1 = new FastQ(fastQwrite.getFileName());
		fastQfileOut1.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		FastQ fastQfileOut2 = new FastQ(fastQfile2.fastQwrite.getFileName());
		fastQfileOut2.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		return new FastQ[]{fastQfileOut1, fastQfileOut2};
	}
	private String getOutFileName() {
		String writeName = fastQwrite.getFileName().trim();
		if (writeName.equals("") ) {
			String fileFastqRead = fastQRead.getFileName();
			if (fileFastqRead.endsWith(".gz")) {
				fileFastqRead = fileFastqRead.substring(0, fileFastqRead.length() - 3);//remove the ".gz"
			}
			writeName = FileOperate.changeFileSuffix(fileFastqRead, "_filtered", "fastq");
		}
		return writeName;
	}
	/** �趨���˺������ļ��� */
	private void setFilterReadsOutName(boolean singleEnd, String outFileName) {
		if (singleEnd) {
			fastQwrite.setFastqFile(outFileName);
		}
		else {
			String outFile1 = FileOperate.changeFileSuffix(outFileName, "_1", null);
			String outFile2 = FileOperate.changeFileSuffix(outFileName, "_2", null);
			fastQwrite.setFastqFile(outFile1);
			fastQwrite.fastQwriteMate.setFastqFile(outFile2);
		}
	}
	
	private void filterReadsRun() {
		fastQfilter.setThreadNum(threadNum_FilterFastqRecord);
		fastQfilter.run();
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
		if (!FileOperate.isFileExist(fastQRead.getFileName())  && FileOperate.isFileExist(fastQwrite.getFileName())) {
			fastQRead.setFastqFile(fastQwrite.getFileName());
		}
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
		for (FastQRecord fastQRecord : fastQRead.readlines(true)) {
			txtFasta.writefile(fastQRecord.getSeqFasta().toStringNRfasta());
		}
		txtFasta.close();
	}
	/**
	 * �����ļ�������� �ļ���.fasta �� �ļ���.
	 * @param showMessage
	 * @param illuminaOffset �Ƿ�Ϊillumina��offset
	 */
	public static void convertSff2FastQ(String fastaFile, boolean illuminaOffset) {
		int offset = FASTQ_SANGER_OFFSET;
		if (illuminaOffset)
			offset = FASTQ_ILLUMINA_OFFSET;		

		String fastaQuality = fastaFile + ".qual";
		TxtReadandWrite txtReadFasta = new TxtReadandWrite(fastaFile, false);
		TxtReadandWrite txtReadQualtiy = new TxtReadandWrite(fastaQuality, false);
		
		String fastQ = FileOperate.changeFileSuffix(fastaFile, null, "fastq");
		FastQ txtOutFastQ = new FastQ(fastQ, true);
		
		Iterator<String> txtQuality = txtReadQualtiy.readlines().iterator();
		//����������Ϊÿ����Ϊһ����Ԫ
		int num = 0;
		FastQRecord fastQRecord = null;
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//������
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sffת��������������fasta��quality�ǲ�������ͬһ���ļ�");
				}
				fastQRecord = new FastQRecord();
				fastQRecord.setName(contentFasta.substring(1));
				num++;
			}
			//��������
			else if (num == 1) {
				fastQRecord.setSeq(contentFasta);
				fastQRecord.setFastaQuality(convert2Phred(contentQuality, offset));
				txtOutFastQ.writeFastQRecord(fastQRecord);
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
		mapReadsQualtiy.put("LowQualityPGM", QUALITY_LOW_PGM);
		return mapReadsQualtiy;
	}
	
	public static HashMap<Integer, Integer> getMapFastQFilter(int QUALITY) {
		HashMap<Integer, Integer> mapFastQFilter = new HashMap<Integer, Integer>();
		if (QUALITY == FastQ.QUALITY_HIGM) {
			mapFastQFilter.put(10, 1);
			mapFastQFilter.put(13, 3);
			mapFastQFilter.put(20, 5);
		} else if (QUALITY == FastQ.QUALITY_LOW) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 4);
			mapFastQFilter.put(13, 10);
			mapFastQFilter.put(20, 20);
		} else if (QUALITY == FastQ.QUALITY_MIDIAN
				|| QUALITY == FastQ.QUALITY_MIDIAN_PAIREND) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		} else if (QUALITY == FastQ.QUALITY_LOW_454) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 6);
			mapFastQFilter.put(13, 15);
			mapFastQFilter.put(20, 40);
		} else if (QUALITY == FastQ.QUALITY_LOW_PGM) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 6);
			mapFastQFilter.put(15, 20);
//			mapFastQFilter.put(20, 80);
		} else {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		}
		return mapFastQFilter;
	}
}
