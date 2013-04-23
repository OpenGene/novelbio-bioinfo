package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQ {
	private static final Logger logger = Logger.getLogger(FastQ.class);
	
	public static final int FASTQ_SANGER_OFFSET = 33;
	public static final int FASTQ_ILLUMINA_OFFSET = 64;
	
	public static final int QUALITY_LOW = 10;
	public static final int QUALITY_MIDIAN = 20;
	
	/** 双端的时候只有两个序列都是好的才保留 */
	public static final int QUALITY_MIDIAN_PAIREND = 40;
	public static final int QUALITY_HIGM = 50;
	public static final int QUALITY_LOW_454 = 60;
	public static final int QUALITY_LOW_PGM = 70;
	public static final int QUALITY_NONE = 80;
	public static final int QUALITY_NOTFILTER = 90;
	
	private int threadNum_FilterFastqRecord = 10;
	
	FastQReader fastQRead;
	FastQwrite fastQwrite;
	FastQfilter fastQfilter = new FastQfilter();
	
	/** 过滤后的文件名 */
	String filterOutName = "";
	
	boolean read = true;
	
	
	/** 默认是读取 */
	public FastQ(String fastqFile) {
		this(fastqFile, false);
	}

	public FastQ(String fastqFile, boolean createNew) {
		if (createNew) {
			fastQwrite = new FastQwrite(fastqFile);
			read = false;
		} else {
			fastQRead = new FastQReader(fastqFile);
			read = true;
		}
	}
	
	public int getOffset() {
		return fastQRead.getOffset();
	}
	
	public void setFilter(FastQRecordFilter fastQfilterRecord) {
		fastQfilter.setFilter(fastQfilterRecord);
	}

	public Iterable<FastQRecord> readlines() {
		return fastQRead.readlines(true);
	}
	/** 读取fastq的时候是否初始化
	 * 主要用在多线程过滤reads的时候，可以在过滤reads的时候才进行初始化
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
	/** 过滤完之后才能获得的值 */
	public long getSeqNum() {
		return fastQRead.readsNum;
	}
	/**
	 * @return null 出错
	 */
	public FastQ filterReads() {
		setFilterReadsOutName(null);
		filterReadsRun();

		FastQ fastQfileOut1 = new FastQ(fastQwrite.getFileName());
		fastQfileOut1.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		return fastQfileOut1;
	}
	/** 双端reads过滤 */
	public FastQ[] filterReads(FastQ fastQfile2) {
		setFilterReadsOutName(fastQfile2);
		fastQRead.setFastQReadMate(fastQfile2.fastQRead);		
		
		filterReadsRun();
		
		FastQ fastQfileOut1 = new FastQ(fastQwrite.getFileName());
		fastQfileOut1.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		FastQ fastQfileOut2 = new FastQ(fastQfile2.fastQwrite.getFileName());
		fastQfileOut2.fastQRead.readsNum = fastQfilter.allFilteredReadsNum;
		return new FastQ[]{fastQfileOut1, fastQfileOut2};
	}
	
	/** 设定过滤后的文件名
	 * 不舍定就走默认
	 */
	public void setFilterOutName(String outFileName) {
		this.filterOutName = outFileName;
	}

	/** 设定过滤后的输出文件名
	 * 同时初始化fastQwrite
	 */
	private void setFilterReadsOutName(FastQ fastQMate) {
		if (filterOutName.equals("") ) {
			String fileFastqRead = fastQRead.getFileName();
			if (fileFastqRead.endsWith(".gz")) {
				fileFastqRead = fileFastqRead.substring(0, fileFastqRead.length() - 3);//remove the ".gz"
			}
			filterOutName = FileOperate.changeFileSuffix(fileFastqRead, "_filtered", "fastq");
		}
		
		if (fastQMate == null) {
			fastQwrite = new FastQwrite(filterOutName);
		}
		else {
			String outFile1 = FileOperate.changeFileSuffix(filterOutName, "_1", null);
			String outFile2 = FileOperate.changeFileSuffix(filterOutName, "_2", null);
			fastQwrite = new FastQwrite(outFile1);
			fastQMate.fastQwrite = new FastQwrite(outFile2);
			fastQwrite.fastQwriteMate = fastQMate.fastQwrite;
		}
	}
	
	private void filterReadsRun() {
		fastQfilter.setFastQReadAndWrite(fastQRead, fastQwrite);
		fastQfilter.setThreadNum(threadNum_FilterFastqRecord);
		fastQfilter.run();
	}
	
	/** 在进行filter的时候也可以导入gui进行操作吧 */
	public FastQfilter getFastQfilter() {
		return fastQfilter;
	}
	public void writeFastQRecord(FastQRecord fastQRecord) {
		fastQwrite.writeFastQRecord(fastQRecord);
	}
	
	public void close() {
		try {
			fastQRead.close();
		} catch (Exception e) { 	}
		try {
			fastQwrite.close();
		} catch (Exception e) { 	}
	
		if (!read) {
			try {
				if (!FileOperate.isFileExist(fastQRead.getFileName())  && FileOperate.isFileExist(fastQwrite.getFileName())) {
					fastQRead = new FastQReader(fastQwrite.getFileName());
					if (fastQRead.fastQReadMate != null) {
						fastQRead.fastQReadMate = new FastQReader(fastQwrite.fastQwriteMate.getFileName());
					}
					read = true;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	
	}
	/**
	 * 将fastq文件转化为fasta文件<br>
	 * 产生的文件为单端： fastaFile<br>
	 * 双端： 如果有后缀名: 如fasta.aa<br>
	 * 则为 fasta.aa 和 fasta2.aa<br>
	 * 没有后缀名则为 fasta 和 fasta2<br>
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
	 * 给定文件名，获得 文件名.fasta 和 文件名.
	 * @param showMessage
	 * @param illuminaOffset 是否为illumina的offset
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
		//计数器，因为每两行为一个单元
		int num = 0;
		FastQRecord fastQRecord = null;
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//标题行
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sff转换出错拉，看看fasta和quality是不是来自同一个文件");
				}
				fastQRecord = new FastQRecord();
				fastQRecord.setName(contentFasta.substring(1));
				num++;
			}
			//具体内容
			else if (num == 1) {
				fastQRecord.setSeq(contentFasta);
				fastQRecord.setFastaQuality(convert2Phred(contentQuality, offset));
				txtOutFastQ.writeFastQRecord(fastQRecord);
				num = 0;
			}
		}
		txtReadFasta.close();
		txtReadQualtiy.close();
		txtOutFastQ.close();
	}
	/**
	 * 给定一系列offset，将数字转化为fastq的quality行
	 * @param illumina 是否是illumina的offset 
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
		mapReadsQualtiy.put("None", QUALITY_NONE);
		mapReadsQualtiy.put("NotFilter", QUALITY_NOTFILTER);
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
			mapFastQFilter.put(10, 10);
			mapFastQFilter.put(15, 30);
		} else if (QUALITY == FastQ.QUALITY_NONE || QUALITY == FastQ.QUALITY_NOTFILTER) {
			//空的就不会过滤
		} else {
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		}
		return mapFastQFilter;
	}
}
