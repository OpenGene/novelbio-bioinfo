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
 * FastQ�ĸ���ָ��<br>
 * Q10: 0.1 <br>
 * Q13: 0.05 <br>
 * Q20: 0.01 <br>
 * Q30: 0.001 <br>
 * 2010�� Illumina HiSeq2000�����ǣ�˫��50bp Q30>90% ˫��100bp Q30>85%
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
	
	/** ��һ�˵Ķ�ȡ�ļ���˫�˶�ȡ��ʱ������ã������Ƕ�Ӧ�Ķ� */
	FastQReader fastQReadMate;
	
	int readsLenAvg = 0;
	
	/** ��׼�ļ����Ļ����Զ��ж��Ƿ�Ϊgzѹ�� */
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
	}
	/** ���趨�ͻ��Զ��ж� */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getOffset() {
		setFastQFormatLen();
		return offset;
	}
	/**
	 * �趨�ļ�ѹ����ʽ
	 * ��TxtReadandWrite.TXT��
	 * @param cmpInType ��ȡ��ѹ����ʽ null��""��ʾ����
	 * @param cmpOutType д���ѹ����ʽ null��""��ʾ����
	 */
	public void setCompressType(String cmpInType) {
		if (cmpInType != null && !cmpInType.equals("")) {
			this.compressInType = cmpInType;
		}
	}
	/** �����ѹ����ʽ */
	public String getCompressInType() {
		return compressInType;
	}
	/** �����ļ��� */
	public String getFileName() {
		return seqFile;
	}
	
	/** �趨��һ��FastqRead��Ҳ����˫�˵���һ�� */
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
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * ������ȡ�ļ�
	 * @param initial �Ƿ���г�ʼ������Ҫ���ڶ��̹߳���reads��ʱ������Ȳ���ʼ�����ڶ��߳�ʱ��ų�ʼ��
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
							fastQRecord = new FastQRecord(linestr, initial);
							fastQRecord.setFastqOffset(offset);
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param initial �Ƿ���г�ʼ������Ҫ���ڶ��̹߳���reads��ʱ����װ�����ʱ�����Ȳ���ʼ�����ڶ��߳�ʱ��ų�ʼ��
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
	 * ������ȡ�ļ�
	 * @param initial �Ƿ���г�ʼ������Ҫ���ڶ��̹߳���reads��ʱ������Ȳ���ʼ�����ڶ��߳�ʱ��ų�ʼ��
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
							fastQRecord[0] = new FastQRecord(linestr1, initial);
							fastQRecord[1] = new FastQRecord(linestr2, initial);
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
	 * ��õ�һ��reads�ĳ��ȣ����ظ���˵������
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
	 * ���FastQ��ʽû���趨�ã�ͨ���÷����趨FastQ��ʽ
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
	 * ��ȡFastQ�ļ��е��ʿ����У���ȡ��5000�оͲ���� ��ΪfastQ�ļ����������ڵ����У�����ֻ��ȡ�����е���Ϣ
	 * @param Num
	 *            ��ȡ�����У�ָ�����ȡ������
	 * @return fastQ�ʿ����е�list ������null
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
	 * ����һϵ�е�fastQ��ʽ���²��fastQ������sanger����solexa
	 * @param lsFastQ ÿһ��string ����һ��fastQ
	 * @return FASTQ_ILLUMINA����FASTQ_SANGER
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
		// ���ǰ������û�㶨�����滹���ж�
		if (lsQuality.get(0) < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (lsQuality.get(lsQuality.size() - 1) > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		logger.error(txtSeqFile.getFileName() + " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// ��û�жϳ������²�Ϊillumina��ʽ
		return FASTQ_ILLUMINA_OFFSET;
	}
	
	/**
	 * ����һϵ�е�fastQ��ʽ�����ƽ��reads����
	 * @param lsFastQ
	 *            :ÿһ��string ����һ��fastQ
	 * @return ƽ��reads����
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
