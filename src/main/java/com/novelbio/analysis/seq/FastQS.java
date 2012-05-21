package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.FastQRecord;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * FastQ的各个指标<br>
 * Q10: 0.1 <br>
 * Q13: 0.05 <br>
 * Q20: 0.01 <br>
 * Q30: 0.001 <br>
 * 2010年 Illumina HiSeq2000测序仪，双端50bp Q30>90% 双端100bp Q30>85%
 * 
 * @author zong0jie
 * 
 */
public class FastQS extends SeqComb {
	public static void main(String[] args) {
		String fastqFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/WE_add.clean.fq.gz";
		
		FastQS fastQS = new FastQS(fastqFile, FastQS.QUALITY_MIDIAN);
		fastQS.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQS.setAdaptorLeft("GCACGGCTTC");
		fastQS.setAdaptorRight("CCTAGCCTGGT");
		fastQS.filterReads();
	}
	private static Logger logger = Logger.getLogger(FastQS.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
	private boolean booPairEnd = false;
	// 有时候有两个fastQ文件，这个仅仅在双端测序的时候出现，这时候需要协同过滤
	private String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * 双端的时候只有两个序列都是好的才保留
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/** 第一条reads的长度 */
	private int readsLen = 0;
	/**
	 * 最短reads的长度，小于该长度的reads就跳过
	 */
	private int readsLenMin = 21;
	private int adaptermaxMismach = 2;
	private int adaptermaxConMismatch = 1;
	String adaptorLeft = "";
	String adaptorRight = "";
	/** 是否从序列开始扫描接头，当接头很长，只设定了一部分接头，或者有接头多聚体的时候选择 */
	boolean adaptorLeftAll = false;
	/** 是否从序列开始扫描接头，当接头很长，只设定了一部分接头，或者有接头多聚体的时候选择 */
	boolean adaptorRightAll = false;
	/**
	 * 设定最短reads的长度，小于该长度的reads就跳过，默认为25
	 */
	public void setLenReadsMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/**
	 * 根据具体的序列调整
	 * @param maxMismach 默认是2
	 * @param maxConMismatch 默认是1
	 */
	public void setAdapterParam(int maxMismach, int maxConMismatch) {
		this.adaptermaxConMismatch = maxConMismatch;
		this.adaptermaxMismach = maxMismach;
	}
	/**
	 * 默认中等质量控制
	 */
	private int quality = QUALITY_MIDIAN;
	
	/**
	 * fastQ里面asc||码的指标与个数
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();

	// ///////////////////////// barcode 所需的变量///////////////////////////////////////////////////////////////////
	/**  记录barcode的信息 key: barcode 的序列 value: barcode所对应的名字 */
	HashMap<String, String> hashBarcodeName = new HashMap<String, String>();
	/** 顺序记录barcode，这个是如果错配的话，可以在该list里面查找  */
	ArrayList<String> lsBarCode = new ArrayList<String>();
	/** 记录barcode的长度 */
	TreeSet<Integer> treeLenBarcode = new TreeSet<Integer>();
	////////  参 数 设 定  ////////////////////
	boolean trimPolyA_right = false;
	boolean trimPolyT_left = false;
	/** 接头是小写,这种情况目前只在ion proton的数据中发现 */
	boolean adaptorLowercase = false;
	/** 是否将序列两边的NNN删除  */
	boolean trimNNN = true;
	/**
	 * 是否将序列两边的NNN删除
	 * 默认是删除的，但是感觉速度好慢然后cufflink还有问题
	 */
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	/**
	 * 设定了adaptor就不要设定PolyA
	 * @param trimPolyA_right
	 * @param flagPolyA true 表示没有polyA的序列不要
	 */
	public void setTrimPolyA(boolean trimPolyA_right) {
		this.trimPolyA_right = trimPolyA_right;
	}
	/**
	 * 设定了adaptor就不要设定PolyA
	 * @param trimPolyA_right
	 * @param flagPolyT true 表示没有polyT的序列不要
	 */
	public void setTrimPolyT(boolean trimPolyT_left) {
		this.trimPolyT_left = trimPolyT_left;
	}
	/**
	 * 注意adapter里面不要有非ATGC的东西
	 * @param adaptor 接头可以只写一部分
	 */
	public void setAdaptorLeft(String adaptor) {
		this.adaptorLeft = adaptor.trim();
	}
	/**
	 * 设定了polyA就不要设定adaptor
	 * 注意adapter里面不要有非ATGC的东西
	 * @param adaptor 接头可以只写一部分
	 */
	public void setAdaptorRight(String adaptor) {
		this.adaptorRight = adaptor.trim();
	}
	/** 是否从序列开始扫描接头，当接头很长，只设定了一部分接头，或者有接头多聚体的时候选择
	 * 默认只从设定的最长接头位置开始扫描
	 */
	public void setAdaptorLeftScanAll(boolean scanAll) {
		this.adaptorLeftAll = scanAll;
	}
	/** 是否从序列开始扫描接头，当接头很长，只设定了一部分接头，或者有接头多聚体的时候选择
	 * 默认只从设定的最长接头位置开始扫描
	 */
	public void setAdaptorRightScanAll(boolean scanAll) {
		this.adaptorRightAll = scanAll;
	}
	/**
	 * 接头是小写 这种情况目前只在ion proton的数据中发现
	 * 貌似454都这个德性
	 */
	public void setCaseLowAdaptor(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	//////////////////////////
	/**
	 * 返回第二个FastQ文件的文件名 如果没有则返回null
	 * @return
	 */
	public String getSeqFile2() {
		return seqFile2;
	}
	/**
	 * 返回FastQ的格式位移，一般是 FASTQ_SANGER_OFFSET 或 FASTQ_ILLUMINA_OFFSET
	 * @return
	 */
	public int getOffset() {
		setFastQFormat();
		return offset;
	}
	/**
	 * 返回文件设定的过滤质量
	 * @return
	 */
	public int getQuality() {
		return quality;
	}
	/**
	 * 返回是否是双端测序的FastQ文件，其实也就是看是否有两个FastQ文件
	 * @return
	 */
	public boolean isPairEnd() {
		return booPairEnd;
	}
	/**
	 * 获得第一条reads的长度，返回负数说明出错
	 * @return
	 */
	public int getFirstReadsLen() {
		if (readsLen > 0) {
			return readsLen;
		}
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		ArrayList<String> lsreads = null;
		try {
			lsreads = txtSeqFile.readFirstLines(4);
		} catch (Exception e) {
			logger.error(seqFile + " may not exits");
			return -1;
		}
		readsLen = lsreads.get(3).trim().length();
		return readsLen;
	}
	
	private void setHashFastQFilter(int QUALITY) {
		if (QUALITY == QUALITY_HIGM) {
			quality = QUALITY;
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		} else if (QUALITY == QUALITY_LOW) {
			quality = QUALITY;
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 10);
			hashFastQFilter.put(20, 20);
		} else if (QUALITY == QUALITY_MIDIAN
				|| QUALITY == QUALITY_MIDIAN_PAIREND) {
			quality = QUALITY;
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		} else if (QUALITY == QUALITY_LOW_454) {
			quality = QUALITY;
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 6);
			hashFastQFilter.put(13, 15);
			hashFastQFilter.put(20, 50);
		}
		else {
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	/**
	 * 输入前先判断文件是否存在,最好能判断两个文件是否是同一个测序的两端 那么可以判断是否为fastQ格式和fasQ格式第一行是否一致
	 * 标准文件名的话，自动判断是否为gz压缩
	 * @param seqFile1
	 *            序列文件
	 * @param fastQFormat
	 *            哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种
	 *            不知道就写0，程序会从文件中判断
	 * @param QUALITY
	 *            QUALITY_LOW等
	 * 
	 */
	public FastQS(String seqFile,  int FastQFormateOffset, int QUALITY) {
		super(seqFile, 4);// fastQ一般4行为一个序列
		String houzhui = FileOperate.getFileNameSep(seqFile)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		}
		else {
			setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
		}
		txtSeqFile.setParameter(compressInType, seqFile, false,true);
		if (FastQFormateOffset == FASTQ_SANGER_OFFSET) {
			offset = 33;
		} else if (FastQFormateOffset == FASTQ_ILLUMINA_OFFSET) {
			offset = 64;
		}
		setHashFastQFilter(QUALITY);
	}

	public void setCompressType(String cmpInType, String cmpOutType) {
		super.setCompressType(cmpInType, cmpOutType);
	}
	/**
	 * 自动判断 FastQ的格式
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQS(String seqFile1, int QUALITY) {
		this(seqFile1, 0, QUALITY);
		String houzhui = FileOperate.getFileNameSep(seqFile1)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		}
		else {
			setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
		}
	}
	/**
	 * 自动判断 FastQ的格式
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQS(String seqFile1, boolean creatNew) {
		super(seqFile1, 4, creatNew);
	}
	/**
	 * 读取前几行，不影响{@link #readlines()}
	 * @param num
	 * @return
	 */
	public ArrayList<FastQRecord> readHeadLines(int num) {
		ArrayList<FastQRecord> lsResult = new ArrayList<FastQRecord>();
		int i = 0;
		for (FastQRecord fastQRecord : readlines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(fastQRecord);
		}
		return lsResult;
	}
	/**
	 * 读取前几行，不影响{@link #readlines()}
	 * @param num
	 * @return
	 */
	public FastQRecord readFirstLine() {
		int i = 0;
		for (FastQRecord fastQRecord : readlines()) {
			return fastQRecord;
		}
		return null;
	}
	/**
	 * 写完后务必用此方法关闭
	 * 关闭输入流，并将fastQ写入转化为fastQ读取
	 */
	public void closeWrite() {
		txtSeqFile.close();
		super.compressInType = super.compressOutType;
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
	}
	public Iterable<FastQRecord> readlines() {
		try {
			return readPerlines();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<FastQRecord> readlines(int lines) {
		lines = lines - 1;
		try {
			Iterable<FastQRecord> itContent = readPerlines();
			if (lines > 0) {
				for (int i = 0; i < lines; i++) {
					itContent.iterator().hasNext();
				}
			}
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 返回Iterator-FastQRecord
	 * 这样可以直接用next来获得下一个
	 * @return
	 */
	public Iterator<FastQRecord> readlinesIterator() {
		return readlines().iterator();
	}
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord> readPerlines() throws Exception {
		txtSeqFile.setFiletype(compressInType);
		 final BufferedReader bufread =  txtSeqFile.readfile(); 
		return new Iterable<FastQRecord>() {
			public Iterator<FastQRecord> iterator() {
				return new Iterator<FastQRecord>() {
					FastQRecord fastQRecord = getLine();
					public boolean hasNext() {
						return fastQRecord != null;
					}
					public FastQRecord next() {
						FastQRecord retval = fastQRecord;
						fastQRecord = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					FastQRecord getLine() {
						FastQRecord fastQRecord = null;
						try {
							String linestr = bufread.readLine();
							for (int i = 0; i < 3; i++) {
								String lineTmp = bufread.readLine();
								if (linestr == null) {
									return null;
								}
								linestr = linestr + TxtReadandWrite.ENTER_LINUX + lineTmp;
							}
							fastQRecord = new FastQRecord(linestr);
						} catch (IOException ioEx) {
							fastQRecord = null;
						}
						return fastQRecord;
					}
				};
			}
		};
	}	
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为_filtered_1和_filtered_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	public FastQS filterReads() {
		String fileName = getFileName().trim();
		if (fileName.endsWith("gz")) {
			fileName = fileName.substring(0, fileName.length() - 3);
		}
		String suffix = "fq";
		if (compressOutType == TxtReadandWrite.GZIP) {
			suffix = "fq.gz";
		}
		fileName = FileOperate.changeFileSuffix(fileName, "_filtered", suffix);
		return filterReads(fileName);
	}
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为_filtered_1和_filtered_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	public FastQS[] filterReads(FastQS fastQSPE) {
		String fileOut = getFileName().trim();
		if (fileOut.endsWith("gz")) {
			fileOut = fileOut.substring(0, fileOut.length() - 3);
		}
		String suffix = "fq";
		if (compressOutType == TxtReadandWrite.GZIP) {
			suffix = "fq.gz";
		}
		String fileOut1 = FileOperate.changeFileSuffix(fileOut, "filtered_1", suffix);
		String fileOut2 = FileOperate.changeFileSuffix(fileOut, "filtered_2", suffix);
		
		return filterReads(fastQSPE, fileOut1, fileOut2);
	}
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * 
	 * @param Qvalue_Num
	 *            二维数组 每一行代表一个Qvalue 以及最多出现的个数 int[0][0] = 13 int[0][1] = 7
	 *            :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	public FastQS filterReads(String fileFilterOut) {
		setFastQFormat();
		try {
			return filterReadsExp( fileFilterOut);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("filter Error: "+ fileFilterOut);
			return null;
		}
	}
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * 
	 * @param fastQSPE2 本文件必须存在
	 *            二维数组 每一行代表一个Qvalue 以及最多出现的个数 int[0][0] = 13 int[0][1] = 7
	 *            :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	public FastQS[] filterReads(FastQS fastQSPE2, String fileFilterOut1, String fileFilterOut2) {
		setFastQFormat();
		try {
			return filterReadsExpPairEnd(fastQSPE2, fileFilterOut1, fileFilterOut2);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("filter Error: "+ fileFilterOut2);
			return null;
		}
	}
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * 默认将结果文件的后缀改为fq或fq.gz
	 * @param Qvalue_Num
	 *            二维数组 每一行代表一个Qvalue 以及最多出现的个数 int[0][0] = 13 int[0][1] = 7
	 *            :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	private FastQS filterReadsExp(String fileFilterOut) throws Exception {
		FastQS fastQ = new FastQS(fileFilterOut, true);
		fastQ.setCompressType(compressOutType, compressOutType);
		int mapNumLeft = -1, mapNumRight = 1;
		if (adaptorLeftAll)
			mapNumLeft = 1;
		if (adaptorRightAll)
			mapNumRight = 1;
		for (FastQRecord fastQRecord : readlines()) {
			fastQRecord.setFastqOffset(offset);
			fastQRecord.setTrimMinLen(readsLenMin);
			fastQRecord = fastQRecord.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, mapNumRight, adaptermaxMismach, adaptermaxConMismatch, 20);
			
			if (fastQRecord == null) continue;
			if (trimPolyA_right) {
				fastQRecord = fastQRecord.trimPolyAR(2);
				if (fastQRecord == null) continue;
			}
			if (trimPolyT_left) {
				fastQRecord = fastQRecord.trimPolyTL(2);
				if (fastQRecord == null) continue;
			}
			if (trimNNN) {
				fastQRecord = fastQRecord.trimNNN(2);
				if (fastQRecord == null) continue;
			}
			if (adaptorLowercase) {
				fastQRecord = fastQRecord.trimLowCase();
				if (fastQRecord == null) continue;
			}
			if (!fastQRecord.QC()) continue;
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.closeWrite();
		return fastQ;
	}
	/**
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param fastQS2 双端的另一端的fastq文件
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @throws Exception
	 */
	private FastQS[] filterReadsExpPairEnd(FastQS fastQS2, String fileFilterOut1, String fileFilterOut2) throws Exception {
		FastQS fastQ1 = new FastQS(fileFilterOut1, true);
		FastQS fastQ2 = new FastQS(fileFilterOut2, true);
		fastQ1.setCompressType(compressOutType, compressOutType);
		fastQ2.setCompressType(compressOutType, compressOutType);
		
		Iterator<FastQRecord> itFastqRecord = fastQS2.readlinesIterator();
		
		int mapNumLeft = -1, mapNumRight = 1;
		if (adaptorLeftAll)
			mapNumLeft = 1;
		if (adaptorRightAll)
			mapNumRight = 1;
		
		for (FastQRecord fastQRecord : readlines()) {
			FastQRecord fastQRecord2 = itFastqRecord.next();
					
			fastQRecord.setFastqOffset(offset);
			fastQRecord.setTrimMinLen(readsLenMin);
			fastQRecord = fastQRecord.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, mapNumRight, adaptermaxMismach, adaptermaxConMismatch, 20);
			
			fastQRecord2.setFastqOffset(offset);
			fastQRecord2.setTrimMinLen(readsLenMin);
			fastQRecord2 = fastQRecord2.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, mapNumRight, adaptermaxMismach, adaptermaxConMismatch, 20);
			
			if (fastQRecord == null || fastQRecord2 == null) continue;
			if (trimPolyA_right) {
				fastQRecord = fastQRecord.trimPolyAR(2); fastQRecord2 = fastQRecord2.trimPolyAR(2);
				if (fastQRecord == null || fastQRecord2 == null) continue;
			}
			if (trimPolyT_left) {
				fastQRecord = fastQRecord.trimPolyTL(2); fastQRecord2 = fastQRecord2.trimPolyTL(2);
				if (fastQRecord == null || fastQRecord2 == null) continue;
			}
			if (trimNNN) {
				fastQRecord = fastQRecord.trimNNN(2); fastQRecord2 = fastQRecord2.trimNNN(2);
				if (fastQRecord == null || fastQRecord2 == null) continue;
			}
			if (adaptorLowercase) {
				fastQRecord = fastQRecord.trimLowCase(); fastQRecord2 = fastQRecord2.trimLowCase();
				if (fastQRecord == null || fastQRecord2 == null) continue;
			}
			if (!fastQRecord.QC() && !fastQRecord2.QC() ) continue;
			fastQ1.writeFastQRecord(fastQRecord);
			fastQ2.writeFastQRecord(fastQRecord2);
		}
		fastQ1.closeWrite();
		fastQ2.closeWrite();
		return new FastQS[]{fastQ1, fastQ2};
	}
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * 内部关闭
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		for (FastQRecord fastQRecord : lsFastQRecords) {
			txtSeqFile.writefileln(fastQRecord.toString());
		}
		closeWrite();
	}
	/**
	 * 如果FastQ格式没有设定好，通过该方法设定FastQ格式
	 */
	private void setFastQFormat() {
		if (offset != 0) {
			return;
		}
		int fastQformat = guessFastOFormat(getLsFastQSeq(1000));
		if (fastQformat == FASTQ_ILLUMINA_OFFSET) {
			offset = FASTQ_ILLUMINA_OFFSET;
			return;
		}
		if (fastQformat == FASTQ_SANGER_OFFSET) {
			offset = FASTQ_SANGER_OFFSET;
			return;
		}
	}
	/**
	 * 提取FastQ文件中的质控序列，提取个5000行就差不多了 因为fastQ文件中质量都在第三行，所以只提取第三行的信息
	 * 
	 * @param Num
	 *            提取多少行，指最后提取的行数
	 * @return fastQ质控序列的list 出错返回null
	 */
	private ArrayList<FastQRecord> getLsFastQSeq(int Num) {
		ArrayList<FastQRecord> lsFastqRecord = new ArrayList<FastQRecord>();
		int thisnum = 0;
		for (FastQRecord fastQRecord : readlines()) {
			if (thisnum > Num) break;
			if (fastQRecord.getSeqQuality().contains("BBB")) {
				thisnum ++ ;
				continue;
			}
			lsFastqRecord.add(fastQRecord);
		}
		return lsFastqRecord;
	}
	/**
	 * 给定一系列的fastQ格式，猜测该fastQ是属于sanger还是solexa
	 * 
	 * @param lsFastQ
	 *            :每一个string 就是一个fastQ
	 * @return FASTQ_ILLUMINA或者FASTQ_SANGER
	 */
	private int guessFastOFormat(ArrayList<FastQRecord> lsFastqRecord) {
		ArrayList<Double> lsQuality = new ArrayList<Double>();
		for (FastQRecord fastQRecord : lsFastqRecord) {
			char[] fastq = fastQRecord.getSeqQuality().toCharArray();
			for (int i = 0; i < fastq.length; i++) {
				lsQuality.add((double) fastq[i]);
			}
		}
		Collections.sort(lsQuality);
		double min5 = lsQuality.get((int) (lsQuality.size() * 0.05));
		double max95 = lsQuality.get((int) (lsQuality.size() * 0.95));
		if (min5 < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (max95 > 95) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		// 如果前两个都没搞定，后面还能判定
		if (lsQuality.get(0) < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (lsQuality.get(lsQuality.size() - 1) > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		logger.error(seqFile
				+ " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// 都没判断出来，猜测为illumina格式
		return FASTQ_ILLUMINA_OFFSET;
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
	public void convertToFasta(String fastaFile) throws Exception {
		TxtReadandWrite txtFasta = new TxtReadandWrite(fastaFile, true);
		for (FastQRecord fastQRecord : readlines()) {
			txtFasta.writefile(fastQRecord.toStringNRfasta());
		}
		txtFasta.close();
	}
	/**
	 * 统计reads分布和每个reads质量的方法
	 */
	ListHashBin gffHashBin = new ListHashBin();
	String gffreadsLen = "Reads Length";
	String gffbpName = "BP";
	/**
	 * 初始化reads分布统计类
	 * @reads最长多少
	 */
	private void initialGffHashBin(int maxReadsLen) {
		ArrayList<String[]> lsInfo = new ArrayList<String[]>();
		//reads 长度
		for (int i = 1; i <= maxReadsLen; i++) {
			lsInfo.add(new String[]{gffreadsLen, i+ "", i + ""});
		}
		//每个碱基的质量
		for (int i = 1; i <= maxReadsLen; i++) {
			for (int j = 1; j < 60; j++) {
				lsInfo.add(new String[]{i+gffbpName, j+ "", j + ""});
			}
		}
		gffHashBin.ReadGff(lsInfo);
	}
	/**
	 * 给定文件名，获得 文件名.fasta 和 文件名.
	 * @param fileName
	 * @param illuminaOffset 是否为illumina的offset
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
		//计数器，因为每两行为一个单元
		int num = 0;
		String title = ""; String fasta = ""; String quality = "";
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//标题行
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sff转换出错拉，看看fasta和quality是不是来自同一个文件");
				}
				title = "@" + contentFasta.substring(1);
				num++;
			}
			//具体内容
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
	
}
