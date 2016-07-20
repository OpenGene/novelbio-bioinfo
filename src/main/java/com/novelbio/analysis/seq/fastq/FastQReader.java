package com.novelbio.analysis.seq.fastq;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
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
 */
class FastQReader implements Closeable {
	private static Logger logger = Logger.getLogger(FastQReader.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
		
	protected TxtReadandWrite txtSeqFile;
	
	/** 另一端的读取文件，双端读取的时候才有用，两端是对应的读 */
	FastQReader fastQReadMate;
	boolean isCheckFormat = false;
	int readsLenAvg = 0;
	boolean isInterleaved = false;
	
	/** 如果遇到reads错误，往下读几行 */
	int readNextNum = 0;
	
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public FastQReader(File seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, false);
		getOffset();
	}
	
	/** 如果遇到reads错误，往下读几行，用于双端读取的时候，
	 * 如果左端和右端不符合，则很可能某一端的文件出错，这
	 * 时候就要左右端分别往下读几行
	 */
	public int getReadNextNum() {
		return readNextNum;
	}
	/** 如果读到正确的fastqrecord，那么这个就要归0，等待下次使用 */
	public void resetReadNextNum() {
		readNextNum = 0;
	}
	
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public FastQReader(String seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, false);
		getOffset();
	}
	
	/** 标准文件名的话，自动判断是否为gz压缩 */
	public FastQReader(InputStream inStream) {
		txtSeqFile = new TxtReadandWrite(inStream);
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
										throw new ExceptionFastq(txtSeqFile.getFileName() + " check fqformat and fastq file error on line: " + lineNum[0] + getPercentageInfoString());
									} else {
										return null;
									}
								}
								lsStr.add(lineTmp);
							}
							fastQRecord = new FastQRecord(lsStr, offset);
						} catch (IOException ioEx) {
							throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq file error on line: " + lineNum[0] + " caused by io exception" + getPercentageInfoString(), ioEx);
						} catch (ExceptionFastq efastq) {
							if (isCheckFormat) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " fastq file error on line: " + lineNum[0] + getPercentageInfoString(), efastq);
							} else {
								readNextNum = 0;

								String errMsg = "fastq file error on line: " + lineNum[0];
								if (txtSeqFile.getFileName() != null) {
									errMsg += " fileName: " + txtSeqFile.getFileName(); 
								}
								logger.error(errMsg);
								while (true) {
									String next = null;
									try {
										readNextNum++;
										next = bufread.readLine();
										lineNum[0]++;
									} catch (Exception e) {
										throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file error on line: " + lineNum[0] + getPercentageInfoString(), e);
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
										throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file may error, error on line: " 
												+ lineNum[0] + " many lines were error" + getPercentageInfoString());
									}
								}
							}
						} catch (OutOfMemoryError e) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + "fastq file error on line: " 
										+ lineNum[0] + " due to OutOfMemoryError" + getPercentageInfoString(), e);
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
		//双端的话内部不检查
		setCheckFormat(false);
		fastQReadMate.setCheckFormat(false);
		
		final Iterator<FastQRecord> itL =  readlines().iterator();
		final Iterator<FastQRecord> itR = fastQReadMate.readlines().iterator();
		final int retryNum = 10000;
		final int[] errorNum = new int[]{0};
		final long[] lineNum = new long[1];
		return new Iterable<FastQRecord[]>() {
			public Iterator<FastQRecord[]> iterator() {
				return new Iterator<FastQRecord[]>() {
					// 如果fastqRecord不均一，则需要
					LinkedList<FastQRecord> lsFastqRecordsLeft = new LinkedList<>();
					LinkedList<FastQRecord> lsFastqRecordsRight = new LinkedList<>();

					
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
					
					private FastQRecord getLeftFq() {
						FastQRecord fqL = null;
						if (!lsFastqRecordsLeft.isEmpty()) {
							fqL = lsFastqRecordsLeft.poll();
						} else {
							fqL = itL.next();
						}
						return fqL;
					}
					private FastQRecord getRightFq() {
						FastQRecord fqR = null;
						if (!lsFastqRecordsRight.isEmpty()) {
							fqR = lsFastqRecordsRight.poll();
						} else {
							fqR = itR.next();
						}
						return fqR;
					}
					
					private boolean isLeftEmpty() {
						return lsFastqRecordsLeft.isEmpty() && !itL.hasNext();
					}
					
					FastQRecord[] getLine() {
						lineNum[0]++;

						
						FastQRecord[] fastQRecord = new FastQRecord[2];
						fastQRecord[0] = getLeftFq();
						fastQRecord[1] = getRightFq();

						if (fastQRecord[0] == null && fastQRecord[1] == null) {
							return null;
						} else if (!(fastQRecord[0] != null && fastQRecord[1] != null)) {
							throw new ExceptionFastq(txtSeqFile.getFileName() + " is not pairend at num " +  lineNum[0] + getPercentageInfoString()
									+ FileOperate.getFileName(getFileName()) + " " + FileOperate.getFileName(fastQReadMate.getFileName()));
						}
						
						
						if (!FastQRecord.isPairedByName(fastQRecord[0], fastQRecord[1])) {
							errorNum[0]++;
							if (errorNum[0] > 10) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " pairend file have lots of reads that does't paired, please check.\n error lines: " +  lineNum[0] + getPercentageInfoString()
										+ FileOperate.getFileName(getFileName()) + " " + FileOperate.getFileName(fastQReadMate.getFileName()));
							}
							
							boolean readCorrecttRecord = false;
							
							LinkedList<FastQRecord> lsFastqL = new LinkedList<>();
							lsFastqL.addFirst(fastQRecord[0]);
							
							for (int i = 0; i < retryNum && !isLeftEmpty(); i++) {
								fastQRecord[0] = getLeftFq();
								if (FastQRecord.isPairedByName(fastQRecord[0], fastQRecord[1])) {
									readCorrecttRecord = true;
									break;
								} else {
									lsFastqL.addFirst(fastQRecord[0]);
								}
							}
							if (!readCorrecttRecord) {
								for (FastQRecord fq : lsFastqL) {
									lsFastqRecordsLeft.addFirst(fq);
								}
								
								
								fastQRecord[0] = getLeftFq();
								for (int i = 0; i < retryNum; i++) {
									fastQRecord[1] = getRightFq();
									if (FastQRecord.isPairedByName(fastQRecord[0], fastQRecord[1])) {
										readCorrecttRecord = true;
										break;
									}
								}
							}
							
							if (!readCorrecttRecord) {
								throw new ExceptionFastq("input file is not pairend at num " + lineNum[0] + getPercentageInfoString()
										+ FileOperate.getFileName(getFileName()) + " " + FileOperate.getFileName(fastQReadMate.getFileName()));
							}
							
						}
						
						return fastQRecord;
					}
				};
			}
		};
	}
	
	
	
	public Iterable<FastQRecord[]> readlinesInterleavedPE() {
		final Iterator<FastQRecord> itFqPE = readlines().iterator();
		return new Iterable<FastQRecord[]>() {

			@Override
			public Iterator<FastQRecord[]> iterator() {
				final int[] errorNum = new int[] { 0 };
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
						FastQRecord fqLeft = new FastQRecord();
						fqLeft.setName("");
						// 右端序列
						FastQRecord fqRight = null;
						int i = 0;
						boolean isError = false;
						boolean isPairend = false;

						if (!itFqPE.hasNext()) {
							return null;
						}

						while (itFqPE.hasNext()) {
							FastQRecord fQRecord = itFqPE.next();
							if (!FastQRecord.isPairedByName(fqLeft, fQRecord)) {
								fqLeft = fQRecord;
								isPairend = false;
								i++;
							} else {
								fqRight = fQRecord;
								isPairend = true;
								break;
							}
							if (i > 10 && !isError) {
								isError = true;
								errorNum[0]++;
							}
							if (i > 1000 || errorNum[0] > 100) {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " is not pairend" + getPercentageInfoString());
							}
						}

						if (!isPairend) {
							if (!itFqPE.hasNext()) {
								return null;
							} else {
								throw new ExceptionFastq(txtSeqFile.getFileName() + " is not pairend" + getPercentageInfoString());
							}
						}
						return new FastQRecord[] { fqLeft, fqRight };
					}
				};
			}
		};
	}
	
	/** 获得读取到文件的百分比，前后自带空格 */
	private String getPercentageInfoString() {
		double readPercentage = txtSeqFile.getReadPercentage()*100;
		DecimalFormat df = new DecimalFormat("#0.0#");

		String percentageInfo = readPercentage >= 0? "\nAlready read "+ df.format(readPercentage) + "% of the file, if you think it almost finish, just use the tmp.fq.gz file. " : "";
		return percentageInfo;
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
		// 都没判断出来，猜测为illumina新格式
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

