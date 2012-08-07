package com.novelbio.analysis.seq.fastq;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import com.novelbio.base.RunProcess;
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
 * 
 */
class FastQRead extends RunProcess<FastqRecordInfoRead>{
	private static Logger logger = Logger.getLogger(FastQRead.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
	
	protected TxtReadandWrite txtSeqFile;
	protected String compressInType = TxtReadandWrite.TXT;
	
	int readsLenAvg = 0;

	int maxNumReadInLs = 5000;
	ArrayBlockingQueue<FastQRecord> lsFastQRecords = new ArrayBlockingQueue<FastQRecord>(maxNumReadInLs);

	
	/**
	 * 输入前先判断文件是否存在,最好能判断两个文件是否是同一个测序的两端 那么可以判断是否为fastQ格式和fasQ格式第一行是否一致
	 * 标准文件名的话，自动判断是否为gz压缩
	 * @param seqFile1  序列文件
	 * @param fastQFormat 哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种
	 *            不知道就写0，程序会从文件中判断
	 * @param QUALITY QUALITY_LOW等
	 * 
	 */
	public FastQRead(String seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, false);
		
		String houzhui = FileOperate.getFileNameSep(seqFile)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP);
		}
		else {
			setCompressType(TxtReadandWrite.TXT);
		}
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
		return txtSeqFile.getFileName();
	}
	
	/** 在每个filterReads中都设定本读取类 */
	public void setLsFilterReads(ArrayList<? extends FastQRecordCope<?>> lsFilterRecords) {
		for (FastQRecordCope<?> fastqFilterRecord : lsFilterRecords) {
			fastqFilterRecord.setFastQRead(this);
			fastqFilterRecord.setLsFastQRecords(lsFastQRecords);
		}
	}
	/** 在每个filterReads中都设定本读取类 */
	public void addFilterReads(FastQfilterRecord filterRecords) {
		filterRecords.setFastQRead(this);
		filterRecords.setLsFastQRecords(lsFastQRecords);
	}
	public Iterable<FastQRecord> readlines() {
		return readlines(0);
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
		long num = 0;
		for (FastQRecord fastQRecord : readlines()) {
			num++;
			suspendCheck();
			if (flagStop) {
				break;
			}
			while (lsFastQRecords.size() == maxNumReadInLs) {
				try { Thread.sleep(50); } catch (InterruptedException e) { }
			}
			setRunInfo(new FastqRecordInfoRead(num, fastQRecord));
			lsFastQRecords.add(fastQRecord);
		}
		logger.info("finishedRead");
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
}

class FastqRecordInfoRead {
	long readsNum = 0;
	FastQRecord fastQRecord;
	public FastqRecordInfoRead(long readsNum, FastQRecord fastQRecord) {
		this.readsNum = readsNum;
		this.fastQRecord = fastQRecord;
	}
}
