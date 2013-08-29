package com.novelbio.analysis.seq.fastq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQ {
	public static void main(String[] args) {
		FastQReadingChannel fastQReadingChannel = new FastQReadingChannel();
		ArrayList<FastQ[]> lsFastQs = new ArrayList<FastQ[]>();
		FastQ fastq1 = new FastQ("/media/winD/NBC/Project/Test/aaa.fq");
		FastQ[] fasatqQs = new FastQ[]{fastq1, new FastQ("/media/winD/NBC/Project/Test/aaa2.fq")};
		lsFastQs.add(fasatqQs);
//		fasatqQs = new FastQ[]{new FastQ("/media/winD/NBC/Project/Test/shnc_GGCTAC_L004_R1_002.fastq.gz"), new FastQ("/media/winD/NBC/Project/Test/shnc_GGCTAC_L004_R2_002.fastq.gz")};
//		lsFastQs.add(fasatqQs);
//		fasatqQs = new FastQ[]{new FastQ("/media/winD/NBC/Project/Test/shnc_GGCTAC_L004_R1_003.fastq.gz"), new FastQ("/media/winD/NBC/Project/Test/shnc_GGCTAC_L004_R2_003.fastq.gz")};
//		lsFastQs.add(fasatqQs);
		
		FastQRecordFilter fastQRecordFilter = new FastQRecordFilter();
		fastQRecordFilter.setFilterParamTrimNNN(true);
		fastQReadingChannel.setFastQRead(lsFastQs);
		FastQC fastQCLeftBefore = new FastQC("/media/winD/NBC/Project/Test/shnc_com1Before.fq", true);
		FastQC fastQCRightBefore = new FastQC("/media/winD/NBC/Project/Test/shnc_com2Before.fq", true);
		FastQC fastQCLeftAfter = new FastQC("/media/winD/NBC/Project/Test/shnc_com1After.fq", true);
		FastQC fastQCRightAfter = new FastQC("/media/winD/NBC/Project/Test/shnc_com2After.fq", true);
		
		fastQReadingChannel.setFastQC(fastQCLeftBefore, fastQCRightBefore);
		fastQReadingChannel.setFilter(fastQRecordFilter, fastq1.getOffset());
		fastQReadingChannel.setFastQC(fastQCLeftAfter, fastQCRightAfter);
		
		FastQ fastqWrite1 = new FastQ("/media/winD/NBC/Project/Test/shnc_com1.fq", true);
		FastQ fastqWrite2 = new FastQ("/media/winD/NBC/Project/Test/shnc_com2.fq", true);
		fastQReadingChannel.setFastQWrite(fastqWrite1, fastqWrite2);
		fastQReadingChannel.setThreadNum(4);
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		fastQReadingChannel.run();
		System.out.println(dateUtil.getEclipseTime());
		fastQCLeftAfter.saveToPath("/media/winD/NBC/Project/Test/QC/LeftAfter");
		fastQCLeftBefore.saveToPath("/media/winD/NBC/Project/Test/QC/LeftBefore");
		fastQCRightAfter.saveToPath("/media/winD/NBC/Project/Test/QC/RightAfter");
		fastQCRightBefore.saveToPath("/media/winD/NBC/Project/Test/QC/RightBefore");
	}
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
	/** 不过滤 */
	public static final int QUALITY_NONE = 80;
	/** 将quality值变成最高 */
	public static final int QUALITY_CHANGE_TO_BEST = 90;
	
	//=======================================================================
	private int threadNum_FilterFastqRecord = 10;
	
	FastQReader fastQRead;
	FastQwriter fastQwrite;
	FastQReadingChannel fastqReadingChannel = new FastQReadingChannel();
		
	/** 过滤后的文件名 */
	String filterOutName = "";
	
	boolean read = true;
	
	/** 默认是读取 */
	public FastQ(String fastqFile) {
		this(fastqFile, false);
	}

	public FastQ(String fastqFile, boolean createNew) {
		if (createNew) {
			fastQwrite = new FastQwriter(fastqFile);
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
		fastqReadingChannel.setFilter(fastQfilterRecord, getOffset());
	}
	
	/** 读取的具体长度，出错返回 -1 */
	public long getReadByte() {
		if (fastQRead != null) {
			return fastQRead.getReadByte();
		}
		return -1;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		if (fastQRead != null) {
			return fastQRead.getReadPercentage();
		}
		return -1;
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
	/**
	 * 获得前1000条reads的平均长度，返回负数说明出错
	 * @return
	 */
	public int getReadsLenAvg() {
		return fastQRead.getReadsLenAvg();
	}

	/**
	 * @return null 出错
	 */
	public FastQ filterReads() {
		FastQ[] fastQs = new FastQ[1];
		fastQs[0] = this;
		List<FastQ[]> lsFastQs = new ArrayList<FastQ[]>();
		lsFastQs.add(fastQs);
		
		FastQ[] fastqFilter = filterReadsRun(lsFastQs);
		return fastqFilter[0];
	}
	
	/** 双端reads过滤/
	 * @param fastQfile2 不能为null，为null会报错
	 * @return
	 */
	public FastQ[] filterReads(FastQ fastQfile2) {
		if (fastQfile2 == null) {
			throw new RuntimeException();
		}
		FastQ[] fastQs = new FastQ[2];
		fastQs[0] = this; fastQs[1] = fastQfile2;
		List<FastQ[]> lsFastQs = new ArrayList<FastQ[]>();
		lsFastQs.add(fastQs);
		
		FastQ[] fastQsFilter = filterReadsRun(lsFastQs);
		return fastQsFilter;
	}
	
	/** 设定过滤后的文件名
	 * 不舍定就走默认
	 */
	public void setFilterOutName(String outFileName) {
		this.filterOutName = outFileName;
	}

	/** 过滤并返回过滤后的结果 */
	private FastQ[] filterReadsRun(List<FastQ[]> lsFastQs) {
		FastQ fastQfile2 = null;
		if (lsFastQs.get(0).length == 2) {
			fastQfile2 = lsFastQs.get(0)[1];
		}
				
		String[] writeFileName = getFilterReadsOutName(fastQfile2);
		FastQ fastqWrite1 = new FastQ(writeFileName[0], true);
		FastQ fastqWrite2 = null;
		if (writeFileName[1] != null) {
			fastqWrite2 = new FastQ(writeFileName[1], true);
		}
	
		fastqReadingChannel.setFastQWrite(fastqWrite1, fastqWrite2);		
		fastqReadingChannel.setFastQRead(lsFastQs);
		fastqReadingChannel.setThreadNum(threadNum_FilterFastqRecord);
		fastqReadingChannel.run();
		return fastqReadingChannel.getFqWrite();
	}
	
	/** 设定过滤后的输出文件名
	 * @param fastQMate 双端的另一端，根据该对象是否为null，返回过滤后的文件名
	 */
	private String[] getFilterReadsOutName(FastQ fastQMate) {
		String[] filteredFileName = new String[2];
		if (filterOutName == null || filterOutName.equals("") ) {
			String fileFastqRead = fastQRead.getFileName();
			if (fileFastqRead.endsWith(".gz")) {
				fileFastqRead = fileFastqRead.substring(0, fileFastqRead.length() - 3);//remove the ".gz"
			}
			filterOutName = FileOperate.changeFileSuffix(fileFastqRead, "_filtered", "fastq");
		}
		filteredFileName[0] = filterOutName + ".gz";
		if (fastQMate != null) {
			String outFile1 = FileOperate.changeFileSuffix(filterOutName, "_1", null);
			String outFile2 = FileOperate.changeFileSuffix(filterOutName, "_2", null);
			filteredFileName[0] = outFile1 + ".gz";
			filteredFileName[1] = outFile2 + ".gz";
		}
		return filteredFileName;
	}
	
	/** 在进行filter的时候也可以导入gui进行操作吧 */
	public FastQReadingChannel getFastQfilter() {
		return fastqReadingChannel;
	}
	public void writeFastQRecord(FastQRecord fastQRecord) {
		fastQwrite.writeFastQRecord(fastQRecord);
	}
	
	public void close() {
		try {
			fastQRead.close();
		} catch (Exception e) { 
		}
		try {
			fastQwrite.close();
		} catch (Exception e) { 
//			e.printStackTrace();
		}
	
		if (!read) {
			try {
				if ((fastQRead == null || !FileOperate.isFileExist(fastQRead.getFileName()))  && FileOperate.isFileExist(fastQwrite.getFileName())) {
					fastQRead = new FastQReader(fastQwrite.getFileName());
					if (fastQRead.fastQReadMate != null) {
						fastQRead.fastQReadMate = new FastQReader(fastQwrite.fastQwriteMate.getFileName());
					}
					read = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
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
		mapReadsQualtiy.put("NotFilter", QUALITY_NONE);
		mapReadsQualtiy.put("Change_Q_ToBest", QUALITY_CHANGE_TO_BEST);
		return mapReadsQualtiy;
	}
	
	public static HashMap<Integer, Integer> getMapFastQFilter(int QUALITY) {
		HashMap<Integer, Integer> mapFastQFilter = new HashMap<Integer, Integer>();
		if (QUALITY == FastQ.QUALITY_HIGM) {
			mapFastQFilter.put(10, 3);
			mapFastQFilter.put(13, 3);
			mapFastQFilter.put(20, 10);
		} else if (QUALITY == FastQ.QUALITY_LOW) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 5);
			mapFastQFilter.put(13, 10);
			mapFastQFilter.put(20, 30);
		} else if (QUALITY == FastQ.QUALITY_MIDIAN
				|| QUALITY == FastQ.QUALITY_MIDIAN_PAIREND) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 4);
			mapFastQFilter.put(13, 7);
			mapFastQFilter.put(20, 20);
		} else if (QUALITY == FastQ.QUALITY_LOW_454) {
			// hashFastQFilter.put(2, 1);
			mapFastQFilter.put(10, 8);
			mapFastQFilter.put(15, 15);
		} else if (QUALITY == FastQ.QUALITY_LOW_PGM) {
			mapFastQFilter.put(10, 10);
			mapFastQFilter.put(13, 30);
		} else if (QUALITY == FastQ.QUALITY_NONE || QUALITY == FastQ.QUALITY_CHANGE_TO_BEST) {
			//空的就不会过滤
		} else {
			mapFastQFilter.put(10, 2);
			mapFastQFilter.put(13, 6);
			mapFastQFilter.put(20, 10);
		}
		return mapFastQFilter;
	}
}
