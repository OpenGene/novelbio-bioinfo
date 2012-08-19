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
 * 
 */
class FastQReader extends MTRecoreReader<FastQRecord, FastqRecordInfoRead> {
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
	/**
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
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
	 * ������ȡ�ļ�
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<FastQRecord> readPerlines() throws Exception {
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
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastqRecordInfoRead fastqRecordInfoRead = new FastqRecordInfoRead(readsNum, new FastQRecord[]{fastQRecord});
			setRunInfo(fastqRecordInfoRead);
			absQueue.add(fastqRecordInfoRead);
		}
	}
	private void readPE() {
		readsNum = 0;
		Iterator<FastQRecord> itMateFastqRecord = fastQReadMate.readlines().iterator();
		for (FastQRecord fastQRecord : readlines()) {
			FastQRecord fastQRecord2 = itMateFastqRecord.next();
			readsNum++;
			wait_To_Cope_AbsQueue();
			if (flagStop) {
				break;
			}
			FastqRecordInfoRead fastqRecordInfoRead = new FastqRecordInfoRead(readsNum, new FastQRecord[]{fastQRecord, fastQRecord2});
			setRunInfo(fastqRecordInfoRead);
			absQueue.add(fastqRecordInfoRead);
		}
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
		txtSeqFile.close();
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
