package com.novelbio.analysis.seq.fastq;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.base.multithread.txtreadcopewrite.MTRecordRead;
import com.novelbio.base.multithread.txtreadcopewrite.MTRecoreReader;

/**
 * FastQ的各个指标<br>
 * Q10: 0.1 <br>
 * Q13: 0.05 <br>
 * Q20: 0.01 <br>
 * Q30: 0.001 <br>
 * 2010年 Illumina HiSeq2000测序仪，双端50bp Q30>90% 双端100bp Q30>85%
 * 
 * @author zong0jie
 */
class FastQReader {
	private static Logger logger = Logger.getLogger(FastQReader.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
	
	String seqFile = "";
	long readsNum = 0;
	
	protected TxtReadandWrite txtSeqFile;
	protected String compressInType = TxtReadandWrite.TXT;
	
	/** 另一端的读取文件，双端读取的时候才有用，两端是对应的读 */
	FastQReader fastQReadMate;
	
	int readsLenAvg = 0;
	
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public void setFastqFile(String seqFile) {
		String houzhui = FileOperate.getFileNameSep(seqFile)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP);
		}
		else {
			setCompressType(TxtReadandWrite.TXT);
		}
		this.seqFile = seqFile;
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
		readsNum = 0;
		getOffset();
	}

	public int getOffset() {
		setFastQFormatLen();
		return offset;
	}
	/**
	 * 设定文件压缩格式
	 * 从TxtReadandWrite.TXT来
	 * @param cmpInType 读取的压缩格式 null或""表示不变
	 * @param cmpOutType 写入的压缩格式 null或""表示不变
	 */
	public void setCompressType(String cmpInType) {
		if (cmpInType != null && !cmpInType.equals("")) {
			this.compressInType = cmpInType;
		}
	}
	/** 输入的压缩格式 */
	public String getCompressInType() {
		return compressInType;
	}
	/** 返回文件名 */
	public String getFileName() {
		return seqFile;
	}
	
	/** 设定另一个FastqRead，也就是双端的另一端 */
	protected void setFastQReadMate(FastQReader fastQReadMate) {
		this.fastQReadMate = fastQReadMate;
	}
	
	protected boolean isPairEnd() {
		if (fastQReadMate != null && fastQReadMate.txtSeqFile != null) {
			return true;
		}
		return false;
	}
	/**
	 * 读取前几行，不影响{@link #readlines()}
	 * @param num
	 * @return
	 */
	public ArrayList<FastQRecord> readHeadLines(int num) {
		ArrayList<FastQRecord> lsResult = new ArrayList<FastQRecord>();
		int i = 0;
		for (FastQRecord info : readlines(true)) {
			if (i >= num) {
				break;
			}
			lsResult.add(info);
		}
		return lsResult;
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<FastQRecord> readlines(int lines) {
		lines = lines - 1;
		try {
			Iterable<FastQRecord> itContent = readPerlines(true);
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
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<FastQRecord> readlines(boolean initial) {
		try {
			Iterable<FastQRecord> itContent = readPerlines(initial);
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 迭代读取文件
	 * @param initial 是否进行初始化，主要用在多线程过滤reads的时候可以先不初始化，在多线程时候才初始化
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord> readPerlines(final boolean initial) throws Exception {
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
							fastQRecord = new FastQRecord(linestr, offset, initial);
							readsNum++;
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
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<FastQRecord[]> readlinesPE(int lines) {
		lines = lines - 1;
		try {
			Iterable<FastQRecord[]> itContent = readPerlinesPE(true);
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
	 * 从第几行开始读，是实际行
	 * @param initial 是否进行初始化，主要用在多线程过滤reads的时候，在装入队列时可以先不初始化，在多线程时候才初始化
	 * @return
	 */
	public Iterable<FastQRecord[]> readlinesPE(boolean initial) {
		try {
			Iterable<FastQRecord[]> itContent = readPerlinesPE(initial);
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 迭代读取文件
	 * @param initial 是否进行初始化，主要用在多线程过滤reads的时候可以先不初始化，在多线程时候才初始化
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord[]> readPerlinesPE(final boolean initial) throws Exception {
		final BufferedReader bufread1 =  txtSeqFile.readfile();
		final BufferedReader bufread2 = fastQReadMate.txtSeqFile.readfile();
		return new Iterable<FastQRecord[]>() {
			public Iterator<FastQRecord[]> iterator() {
				return new Iterator<FastQRecord[]>() {
					FastQRecord[] fastQRecords = getLine();
					public boolean hasNext() {
						return fastQRecords != null;
					}
					public FastQRecord[] next() {
						FastQRecord[] retval = fastQRecords;
						fastQRecords = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					FastQRecord[] getLine() {
						FastQRecord[] fastQRecord = new FastQRecord[2];
						try {
							String linestr1 = bufread1.readLine();
							String linestr2 = bufread2.readLine();
							for (int i = 0; i < 3; i++) {
								String lineTmp1 = bufread1.readLine();
								String lineTmp2 = bufread2.readLine();
								if (linestr1 == null || linestr2 == null) {
									return null;
								}
								linestr1 = linestr1 + TxtReadandWrite.ENTER_LINUX + lineTmp1;
								linestr2 = linestr2 + TxtReadandWrite.ENTER_LINUX + lineTmp2;
							}
							fastQRecord[0] = new FastQRecord(linestr1, offset, initial);
							fastQRecord[1] = new FastQRecord(linestr2, offset, initial);
							fastQRecord[0].setFastqOffset(offset);
							fastQRecord[0].setFastqOffset(offset);
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
	 * 获得第一条reads的长度，返回负数说明出错
	 * @return
	 */
	public int getReadsLenAvg() {
		if (readsLenAvg > 0) {
			return readsLenAvg;
		}
		setFastQFormatLen();
		return readsLenAvg;
	}
	/**
	 * 如果FastQ格式没有设定好，通过该方法设定FastQ格式
	 */
	private void setFastQFormatLen() {
		if (offset != 0) {
			return;
		}
		ArrayList<FastQRecord> lsFastQRecordsTop500 = getLsFastQSeq(500);
		offset = guessFastOFormat(lsFastQRecordsTop500);
		readsLenAvg = getReadsLenAvg(lsFastQRecordsTop500);
	}
	
	/**
	 * 提取FastQ文件中的质控序列，提取个5000行就差不多了 因为fastQ文件中质量都在第三行，所以只提取第三行的信息
	 * @param Num
	 *            提取多少行，指最后提取的行数
	 * @return fastQ质控序列的list 出错返回null
	 */
	private ArrayList<FastQRecord> getLsFastQSeq(int Num) {
		ArrayList<FastQRecord> lsFastqRecord = new ArrayList<FastQRecord>();
		int thisnum = 0;
		for (FastQRecord fastQRecord : readlines(true)) {
			if (thisnum > Num) break;
			if (fastQRecord.getSeqQuality().contains("BBB")) {
				continue;
			}
			thisnum ++ ;
			lsFastqRecord.add(fastQRecord);
		}
		return lsFastqRecord;
	}
	/**
	 * 给定一系列的fastQ格式，猜测该fastQ是属于sanger还是solexa
	 * @param lsFastQ 每一个string 就是一个fastQ
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
		logger.error(txtSeqFile.getFileName() + " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// 都没判断出来，猜测为illumina格式
		return FASTQ_ILLUMINA_OFFSET;
	}
	
	/**
	 * 给定一系列的fastQ格式，获得平均reads长度
	 * @param lsFastQ
	 *            :每一个string 就是一个fastQ
	 * @return 平均reads长度
	 */
	private int getReadsLenAvg(ArrayList<FastQRecord> lsFastqRecord) {
		if (lsFastqRecord.size() == 0) {
			return 0;
		}
		int readsLenSum = 0;
		for (FastQRecord fastQRecord : lsFastqRecord) {
			readsLenSum = readsLenSum + fastQRecord.getLength();
		}
		return readsLenSum/lsFastqRecord.size();
	}
	
	public void close() {
		try {
			txtSeqFile.close();
			if (fastQReadMate != null) {
				fastQReadMate.close();
			}
		} catch (Exception e) { }
	}
}

class FastqRecordInfoRead implements MTRecordRead{
	long readsNum = 0;
	FastQRecord[] fastQRecord;
	public FastqRecordInfoRead(long readsNum, FastQRecord[] fastQRecord) {
		this.readsNum = readsNum;
		this.fastQRecord = fastQRecord;
	}
}
