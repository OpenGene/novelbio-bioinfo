package com.novelbio.analysis.seq.fastq;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hg.doc.fa;
import com.novelbio.base.dataOperate.TxtReadandWrite;

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
class FastQReader implements Closeable {
	private static Logger logger = Logger.getLogger(FastQReader.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
		
	protected TxtReadandWrite txtSeqFile;
	
	/** 另一端的读取文件，双端读取的时候才有用，两端是对应的读 */
	FastQReader fastQReadMate;
	boolean isCheckFormat = true;
	int readsLenAvg = 0;
	boolean isInterleaved = false;
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public FastQReader(File seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, false);
		getOffset();
	}
	
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public FastQReader(String seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, false);
		getOffset();
	}
	/** 是否检查文件格式，true检查，false不检查 */
	public void setCheckFormat(boolean isCheckFormat) {
		this.isCheckFormat = isCheckFormat;
	}
	public int getOffset() {
		setFastQFormatLen();
		return offset;
	}
	public int getIsInterval() {
		setFastQFormatLen();
		return offset;
	}
	/** 返回文件名 */
	public String getFileName() {
		return txtSeqFile.getFileName();
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
		for (FastQRecord info : readlines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(info);
		}
		close();
		return lsResult;
	}
	
	/** 读取的具体长度，出错返回 -1 */
	public long getReadByte() {
		if (txtSeqFile != null) {
			return txtSeqFile.getReadByte();
		}
		return -1;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		if (txtSeqFile != null) {
			return txtSeqFile.getReadPercentage();
		}
		return -1;
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<FastQRecord> readlines(int lines) {
		lines = lines - 1;
		Iterable<FastQRecord> itContent = readPerlines();
		if (lines > 0) {
			for (int i = 0; i < lines; i++) {
				itContent.iterator().hasNext();
			}
		}
		return itContent;
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param initial 是否进行初始化
	 * @return
	 */
	public Iterable<FastQRecord> readlines() {
		return readPerlines();
	}
	
	/**
	 * 迭代读取文件
	 * @param initial 是否进行初始化，主要用在多线程过滤reads的时候可以先不初始化，在多线程时候才初始化
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord> readPerlines() {
		final BufferedReader bufread =  txtSeqFile.readfile();
		final long[] lineNum = new long[1];
		final int[] errorNum = new int[1];
		final LinkedList<String> lsStr = new LinkedList<>();
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
						lsStr.clear();
						lineNum[0] += 4;
						FastQRecord fastQRecord = null;
						try {
							for (int i = 0; i < 4; i++) {
								String lineTmp = bufread.readLine();
								if (lineTmp == null) {
									if (i != 0 && isCheckFormat) {
										throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq file error on line: " + lineNum[0]/4);
									} else {
										return null;
									}
								}
								lsStr.add(lineTmp);
							}
							fastQRecord = new FastQRecord(lsStr, offset);
						} catch (IOException ioEx) {
							throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq file error on line: " + lineNum[0]/4);
						} catch (ExceptionFastq efastq) {
							if (isCheckFormat) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq file error on line: " + lineNum[0]/4);
							} else {
								logger.error("fastq file error on line: " + lineNum[0]/4 + "\n" + 
										txtSeqFile.getFileName() );
								while (true) {
									String next = null;
									try {
										next = bufread.readLine();
										lineNum[0]++;
									} catch (Exception e) {
										throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file error on line: " + lineNum[0]/4, e);
									}
									if (next == null) {
										return null;
									}
									lsStr.removeFirst();
									lsStr.add(next);
									errorNum[0]++;
									try {
										fastQRecord = new FastQRecord(lsStr, offset);
										errorNum[0] = 0;
										break;
									} catch (Exception e) {}
									if (errorNum[0] > 10000) {
										throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file error on line: " + lineNum[0]/4);
									}
								}
							}
						} catch (OutOfMemoryError e) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file error on line: " + lineNum[0]/4);
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
			Iterable<FastQRecord[]> itContent = readPerlinesPE();
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
	public Iterable<FastQRecord[]> readlinesPE() {
		try {
			Iterable<FastQRecord[]> itContent = readPerlinesPE();
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
	private Iterable<FastQRecord[]> readPerlinesPE() throws Exception {
		final BufferedReader bufread1 =  txtSeqFile.readfile();
		final BufferedReader bufread2 = fastQReadMate.txtSeqFile.readfile();
		final long[] lineNum = new long[1];
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
						lineNum[0]++;
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
							fastQRecord[0] = new FastQRecord(linestr1, offset);
							fastQRecord[1] = new FastQRecord(linestr2, offset);
							fastQRecord[0].setFastqOffset(offset);
							fastQRecord[0].setFastqOffset(offset);
						} catch (IOException ioEx) {
							fastQRecord = null;
						} catch (ExceptionFastq efastq) {
							if (isCheckFormat) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq format error on line: " + lineNum[0]);
							}
						}
						return fastQRecord;
					}
				};
			}
		};
	}
	
	/**
	 * 获得前1000条reads的平均长度，返回负数说明出错
	 * @return
	 */
	public int getReadsLenAvg() {
		if (readsLenAvg > 0) {
			return readsLenAvg;
		}
		setFastQFormatLen();
		return readsLenAvg;
	}
	
	/** 如果FastQ格式没有设定好，通过该方法设定FastQ格式 */
	private void setFastQFormatLen() {
		if (offset != 0) {
			return;
		}
		ArrayList<FastQRecord> lsFastQRecordsTop1000 = getLsFastQSeq(1000);
		offset = guessFastOFormat(lsFastQRecordsTop1000);
		readsLenAvg = getReadsLenAvg(lsFastQRecordsTop1000);
		isInterleaved = isInterleaved(lsFastQRecordsTop1000);
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
		for (FastQRecord fastQRecord : readlines()) {
			if (thisnum > Num) break;
			if (fastQRecord.getSeqQuality().contains("BBB")) {
				continue;
			}
			thisnum ++ ;
			lsFastqRecord.add(fastQRecord);
		}
		close();
		return lsFastqRecord;
	}
	/**
	 * 给定一系列的fastQ格式，猜测该fastQ是属于sanger还是solexa
	 * @param lsFastQ 每一个string 就是一个fastQ
	 * @return FASTQ_ILLUMINA或者FASTQ_SANGER
	 */
	private int guessFastOFormat(ArrayList<FastQRecord> lsFastqRecord) {
		ArrayList<Integer> lsQuality = new ArrayList<Integer>();
		for (FastQRecord fastQRecord : lsFastqRecord) {
			char[] fastq = fastQRecord.getSeqQuality().toCharArray();
			for (int i = 0; i < fastq.length; i++) {
				lsQuality.add((int) fastq[i]);
			}
		}
		Collections.sort(lsQuality);
		if (lsQuality.size() <= 0) {
			return FASTQ_SANGER_OFFSET;
		}
		if (getPercentage(lsQuality, 5) < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (getPercentage(lsQuality, 95) > 95) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		// 如果前两个都没搞定，后面还能判定
		if (lsQuality.get(10) < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (lsQuality.get(lsQuality.size() - 10) > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		
		int offset = guessFormate(lsQuality);
		if (offset > 0) {
			return offset;
		}
		logger.error(txtSeqFile.getFileName() + " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// 都没判断出来，猜测为illumina格式
		return FASTQ_SANGER_OFFSET;
	}
	
	private int guessFormate(List<Integer> lsQuality) {
		int illumina = 0;
		int sanger = 0;
		for (int percentage = 20; percentage < 80; percentage = percentage + 5) {
			int score = lsQuality.get((int) (lsQuality.size() * (0.01 * percentage)));
			if (score >= 75) {
				illumina ++;
			} 
			if (score >= 80) {
				illumina ++;
			}
			if (score >= 90) {
				illumina ++;
			}
			if (score <= 78) {
				sanger ++;
			}
			if (score <= 75) {
				sanger ++;
			}
			if (score <= 70) {
				sanger ++;
			}
		}
		if (illumina > sanger) {
			return FASTQ_ILLUMINA_OFFSET;
		} else if (illumina < sanger) {
			return FASTQ_SANGER_OFFSET;
		} else {
			return 0;
		}
	}
	
	
	/**
	 * 获得分位点数字
	 * @param lsQuality 从小到大排序的list
	 * @param percentage 100*分位点，譬如95就表示list中从小到大排名95%的数字
	 * @return
	 */
	private int getPercentage(List<Integer> lsQuality, int percentage) {
		return lsQuality.get((int) (lsQuality.size() * (0.01 * percentage)));
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
	
	/**
	 * 给定一系列的fastQ格式，获得平均reads长度
	 * @param lsFastQ
	 *            :每一个string 就是一个fastQ
	 * @return 平均reads长度
	 */
	private boolean isInterleaved(ArrayList<FastQRecord> lsFastqRecord) {
		if (lsFastqRecord.size() == 0) {
			return false;
		}
		boolean isInterleaved = true;
		boolean first = true;
		FastQRecord fastqLast = null;
		for (FastQRecord fastQRecord : lsFastqRecord) {
			if (first) {
				fastqLast = fastQRecord;
				first = false;
				continue;
			} else {
				first = true;
				if (!fastQRecord.getName().split(" ")[0].equals(fastqLast.getName().split(" ")[0])) {
					isInterleaved = false;
					break;
				}
			}
		}
		return isInterleaved;
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

