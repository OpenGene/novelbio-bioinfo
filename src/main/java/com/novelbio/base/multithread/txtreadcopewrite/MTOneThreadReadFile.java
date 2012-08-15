package com.novelbio.base.multithread.txtreadcopewrite;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.fastq.FastQRecordCope;
import com.novelbio.analysis.seq.fastq.FastQfilterRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 多线程中一个线程读取文件 */
public class MTOneThreadReadFile {

	
	String seqFile = "";
	long readsNum = 0;
	
	protected TxtReadandWrite txtSeqFile;
	protected String compressInType = TxtReadandWrite.TXT;
	
	/** 另一端的读取文件，双端读取的时候才有用，两端是对应的读 */
	FastQRead fastQReadMate;
	int maxNumReadInLs = 5000;
	ArrayBlockingQueue<FastQRecord[]> lsFastQRecords = new ArrayBlockingQueue<FastQRecord[]>(maxNumReadInLs);
	
	/** 在每个filterReads中都设定本读取类 */
	public void setLsFilterReads(ArrayList<? extends MTmulitCopeInfo<?>> lsCopeInfo) {
		for (MTmulitCopeInfo<?> copeInfo : lsCopeInfo) {
			copeInfo.setReadInfo(this);
			copeInfo.setLsFastQRecords(lsFastQRecords);
		}
	}
	/** 在每个filterReads中都设定本读取类 */
	public void addFilterReads(FastQfilterRecord filterRecords) {
		filterRecords.setFastQRead(this);
		filterRecords.setLsFastQRecords(lsFastQRecords);
	}
	/** 设定另一个FastqRead，也就是双端的另一端 */
	protected void setFastQReadMate(FastQRead fastQReadMate) {
		this.fastQReadMate = fastQReadMate;
	}
	
	public Iterable<FastQRecord> readlines() {
		return readlines(0);
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
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord> readPerlines() throws Exception {
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
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
							fastQRecord.setFastqOffset(offset);
						} catch (IOException ioEx) {
							fastQRecord = null;
						}
						return fastQRecord;
					}
				};
			}
		};
	}
	@Override
	protected void running() {
		setFastQFormatLen();
		if (fastQReadMate == null) {
			readSE();
		}
		else {
			readPE();
		}
		logger.info("finishedRead");
	}
	
	private void readSE() {
		readsNum = 0;
		for (FastQRecord fastQRecord : readlines()) {
			readsNum++;
			suspendCheck();
			if (flagStop) {
				break;
			}
			while (lsFastQRecords.size() == maxNumReadInLs) {
				try { Thread.sleep(50); } catch (InterruptedException e) { }
			}
			FastQRecord[] fastQRecords = new FastQRecord[]{fastQRecord};
			setRunInfo(new FastqRecordInfoRead(readsNum, fastQRecords));
			lsFastQRecords.add(fastQRecords);
		}
	}
	private void readPE() {
		readsNum = 0;
		Iterator<FastQRecord> itMateFastqRecord = fastQReadMate.readlines().iterator();
		for (FastQRecord fastQRecord : readlines()) {
			FastQRecord fastQRecord2 = itMateFastqRecord.next();
			readsNum++;
			suspendCheck();
			if (flagStop) {
				break;
			}
			while (lsFastQRecords.size() == maxNumReadInLs) {
				try { Thread.sleep(50); } catch (InterruptedException e) { }
			}
			FastQRecord[] fastQRecords = new FastQRecord[]{fastQRecord, fastQRecord2};
			setRunInfo(new FastqRecordInfoRead(readsNum, fastQRecords));
			lsFastQRecords.add(fastQRecords);
		}
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
		int fastQformat = guessFastOFormat(lsFastQRecordsTop500);
		readsLenAvg = getReadsLenAvg(lsFastQRecordsTop500);
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
		txtSeqFile.close();
	}

}
