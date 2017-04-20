package com.novelbio.analysis.seq.fastq;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.PathDetailNBC;

public class FastQ {
	private static final Logger logger = Logger.getLogger(FastQ.class);
	
	public static final int FASTQ_SANGER_OFFSET = 33;
	public static final int FASTQ_ILLUMINA_OFFSET = 64;
	
	/** 双端fastq合并为一个时的文件名后缀 */
	public static final String FASTQ_Interleaved_Suffix = "_Interleaved";
	/** 和 path.properties 文件中的 change to best 一样 */
	static final String FASTQ_QUALITY_CHANGE_TO_BEST = "ChangeToBest";

//	
//	public static final int QUALITY_LOW = 10;
//	public static final int QUALITY_MIDIAN = 20;
//	
//	/** 双端的时候只有两个序列都是好的才保留 */
//	public static final int QUALITY_MIDIAN_PAIREND = 40;
//	public static final int QUALITY_HIGH = 50;
//	public static final int QUALITY_LOW_454 = 60;
//	public static final int QUALITY_LOW_PGM = 70;
//	/** 不过滤 */
//	public static final int QUALITY_NONE = 80;
//	/** 将quality值变成最高 */
//	public static final int QUALITY_CHANGE_TO_BEST = 90;
	
	//=======================================================================
	private int threadNum_FilterFastqRecord = 10;
	
	FastQReader fastQRead;
	FastQwriter fastQwrite;
	FastQReadingChannel fastqReadingChannel = new FastQReadingChannel();
		
	/** 过滤后的文件名 */
	String filterOutName = "";
	
	boolean read = true;
	
	/** 默认是读取 */
	public FastQ(File file) {
		this(file, false);
	}
	/** 默认是读取 */
	public FastQ(String fastqFile) {
		this(fastqFile, false);
	}
	
	public FastQ(File fastqFile, boolean createNew) {
		if (createNew) {
			fastQwrite = new FastQwriter(fastqFile);
			read = false;
		} else {
			fastQRead = new FastQReader(fastqFile);
			read = true;
		}
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
	
	public FastQ(InputStream inputStream) {
		fastQRead = new FastQReader(inputStream);
		read = true;
	}
	
	public FastQReader getFastQRead() {
		return fastQRead;
	}

	
	/** 只有初始化读取文件后，才能设定这个 */
	public void setCheckFormat(boolean isCheckFormat) {
		fastQRead.setCheckFormat(isCheckFormat);
	}
	
	public int getOffset() {
		return fastQRead.getOffset();
	}
	
	public void setFilter(FastQFilter fastQfilterRecord) {
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
	
	public Iterable<FastQRecord[]> readlinesPE(FastQ fastqMate) {
		fastQRead.setFastQReadMate(fastqMate.getFastQRead());
		return fastQRead.readlinesPE();
	}
	
	public Iterable<FastQRecord[]> readlinesInterleavedPE() {
		return fastQRead.readlinesInterleavedPE();
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
		return fastqReadingChannel.getFqFiltered();
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
				if ((fastQRead == null || !FileOperate.isFileExistAndNotDir(fastQRead.getFileName()))  && FileOperate.isFileExistAndNotDir(fastQwrite.getFileName())) {
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
		for (FastQRecord fastQRecord : fastQRead.readlines()) {
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
	
	public static Map<String, String> getMapReadsQuality() {
		return new HashMap<>();
	}
	
	/**
	 * reads质量过滤参数
	 * @param QUALITY 选定等级
	 * @return map<br>
	 * key 具体的碱基质量等级，如10，13，20<br>
	 * value 指定的碱基质量不得超过的比例<br>
	 * 如质量小于10的碱基数量不得超过 reads长度的 10%
	 */
	public static Map<Integer, Double> getMapQuality2Num(String QUALITY) {
		Map<Integer, Double> mapQuality2CutoffNum = new HashMap<Integer, Double>();

		if (QUALITY.equalsIgnoreCase("ChangeToBest") || QUALITY.equalsIgnoreCase("NotFilter")) {
			return mapQuality2CutoffNum;
		}
		
		String[] ss = QUALITY.split(";");
		for (String quality2property : ss) {
			String[] quality2propertyArray = quality2property.split(",");
			mapQuality2CutoffNum.put(Integer.parseInt(quality2propertyArray[0]), Double.parseDouble(quality2propertyArray[1]));
		}
		return mapQuality2CutoffNum;
	}
	
	/** 给定一系列fastq文件，判定这些文件的readsquality是否一致并返回，如果不一致则抛出异常 */
	public static int getFastqOffset(List<FastQ> lsFastq) {
		if (lsFastq == null || lsFastq.isEmpty()) {
			throw new ExceptionFastq("no fastq input file");
		}
		int offset = lsFastq.get(0).getOffset();
		for (int i = 1; i < lsFastq.size(); i++) {
			FastQ fastQ = lsFastq.get(i);
			if (offset != fastQ.getOffset()) {
				throw new ExceptionFastq("fastq quality is not consistent " + lsFastq.get(0).getReadFileName() + " is phred" + offset + " while " +  fastQ.getReadFileName() + " is phred" + offset);
			}
		}
		return offset;
	}
}
