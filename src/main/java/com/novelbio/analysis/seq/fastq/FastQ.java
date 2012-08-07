package com.novelbio.analysis.seq.fastq;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

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
public class FastQ extends SeqComb {
	private static Logger logger = Logger.getLogger(FastQ.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	
	private int offset = 0;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * ˫�˵�ʱ��ֻ���������ж��ǺõĲű���
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/** ��һ��reads�ĳ��� */
	private int readsLenAvg = 0;
	/**
	 * ���reads�ĳ��ȣ�С�ڸó��ȵ�reads������
	 */
	private int readsLenMin = 21;
	private int adaptermaxMismach = 2;
	private int adaptermaxConMismatch = 1;
	String adaptorLeft = "";
	String adaptorRight = "";
	/** �Ƿ�����п�ʼɨ���ͷ������ͷ�ܳ���ֻ�趨��һ���ֽ�ͷ�������н�ͷ������ʱ��ѡ�� */
	boolean adaptorScanLeftStart = false;
	/** �Ƿ�����п�ʼɨ���ͷ������ͷ�ܳ���ֻ�趨��һ���ֽ�ͷ�������н�ͷ������ʱ��ѡ�� */
	boolean adaptorScanRightStart = false;
	
	int noAdaptorReads = 0;
	int readsNum = 0;
	
	/**
	 * Ĭ���е���������
	 */
	private int quality = QUALITY_MIDIAN;
	
	/**
	 * fastQ����asc||���ָ�������
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();

	// ///////////////////////// barcode ����ı���///////////////////////////////////////////////////////////////////
	/**  ��¼barcode����Ϣ key: barcode ������ value: barcode����Ӧ������ */
	HashMap<String, String> hashBarcodeName = new HashMap<String, String>();
	/** ˳���¼barcode��������������Ļ��������ڸ�list�������  */
	ArrayList<String> lsBarCode = new ArrayList<String>();
	/** ��¼barcode�ĳ��� */
	TreeSet<Integer> treeLenBarcode = new TreeSet<Integer>();
	////////  �� �� �� ��  ////////////////////
	private boolean trimPolyA_right = false;
	private boolean trimPolyT_left = false;
	/** ��ͷ��Сд,�������Ŀǰֻ��ion proton�������з��� */
	private boolean adaptorLowercase = false;
	/** �Ƿ��������ߵ�NNNɾ��  */
	private boolean trimNNN = true;
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, boolean creatNew) {
		super(seqFile1, 4, creatNew);
	}
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQ(String seqFile1) {
		this(seqFile1, 0, FastQ.QUALITY_MIDIAN);
	}
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, int QUALITY) {
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
	 * ����ǰ���ж��ļ��Ƿ����,������ж������ļ��Ƿ���ͬһ����������� ��ô�����ж��Ƿ�ΪfastQ��ʽ��fasQ��ʽ��һ���Ƿ�һ��
	 * ��׼�ļ����Ļ����Զ��ж��Ƿ�Ϊgzѹ��
	 * @param seqFile1  �����ļ�
	 * @param fastQFormat ����fastQ��ʽ��������FASTQ_SANGER_OFFSET��FASTQ_ILLUMINA_OFFSET����
	 *            ��֪����д0���������ļ����ж�
	 * @param QUALITY QUALITY_LOW��
	 * 
	 */
	public FastQ(String seqFile,  int FastQFormateOffset, int QUALITY) {
		super(seqFile, 4);// fastQһ��4��Ϊһ������
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
	public int getReadsNumNoAdaptor() {
		return noAdaptorReads;
	}
	
	public int getSeqNum() {
		if (readsNum == 0) {
			readsNum = super.getSeqNum();
		}
		return readsNum;
	}
	/**
	 * �趨���reads�ĳ��ȣ�С�ڸó��ȵ�reads��������Ĭ��Ϊ25
	 */
	public void setLenReadsMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/**
	 * ���ݾ�������е���
	 * @param maxMismach Ĭ����2
	 * @param maxConMismatch Ĭ����1
	 */
	public void setAdapterParam(int maxMismach, int maxConMismatch) {
		this.adaptermaxConMismatch = maxConMismatch;
		this.adaptermaxMismach = maxMismach;
	}
	
	/**
	 * �Ƿ��������ߵ�NNNɾ��
	 * Ĭ����ɾ���ģ����Ǹо��ٶȺ���Ȼ��cufflink��������
	 */
	public void setTrimNNN(boolean trimNNN) {
		this.trimNNN = trimNNN;
	}
	/**
	 * �趨��adaptor�Ͳ�Ҫ�趨PolyA
	 * @param trimPolyA_right
	 * @param flagPolyA true ��ʾû��polyA�����в�Ҫ
	 */
	public void setTrimPolyA(boolean trimPolyA_right) {
		this.trimPolyA_right = trimPolyA_right;
	}
	/**
	 * �趨��adaptor�Ͳ�Ҫ�趨PolyA
	 * @param trimPolyA_right
	 * @param flagPolyT true ��ʾû��polyT�����в�Ҫ
	 */
	public void setTrimPolyT(boolean trimPolyT_left) {
		this.trimPolyT_left = trimPolyT_left;
	}
	/**
	 * ע��adapter���治Ҫ�з�ATGC�Ķ���
	 * @param adaptor ��ͷ����ֻдһ���֣�����Ϊnull
	 */
	public void setAdaptorLeft(String adaptor) {
		this.adaptorLeft = adaptor.trim();
	}
	/**
	 * �趨��polyA�Ͳ�Ҫ�趨adaptor
	 * ע��adapter���治Ҫ�з�ATGC�Ķ���
	 * @param adaptor ��ͷ����ֻдһ���֣�����Ϊnull
	 */
	public void setAdaptorRight(String adaptor) {
		this.adaptorRight = adaptor.trim();
	}
	/** �Ƿ�����п�ʼɨ���ͷ������ͷ�ܳ���ֻ�趨��һ���ֽ�ͷ�������н�ͷ������ʱ��ѡ��
	 * Ĭ��ֻ���趨�����ͷλ�ÿ�ʼɨ��
	 */
	public void setAdaptorLeftScanAll(boolean scanAll) {
		this.adaptorScanLeftStart = scanAll;
	}
	/** �Ƿ�����п�ʼɨ���ͷ������ͷ�ܳ���ֻ�趨��һ���ֽ�ͷ�������н�ͷ������ʱ��ѡ��
	 * Ĭ��ֻ���趨�����ͷλ�ÿ�ʼɨ��
	 */
	public void setAdaptorRightScanAll(boolean scanAll) {
		this.adaptorScanRightStart = scanAll;
	}
	/**
	 * ��ͷ��Сд �������Ŀǰֻ��ion proton�������з���
	 * ò��454���������
	 */
	public void setCaseLowAdaptor(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	//////////////////////////
	/**
	 * ����FastQ�ĸ�ʽλ�ƣ�һ���� FASTQ_SANGER_OFFSET �� FASTQ_ILLUMINA_OFFSET
	 * @return
	 */
	public int getOffset() {
		setFastQFormatLen();
		return offset;
	}
	/**
	 * �����ļ��趨�Ĺ�������
	 * @return
	 */
	public int getQuality() {
		return quality;
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

	public void setCompressType(String cmpInType, String cmpOutType) {
		super.setCompressType(cmpInType, cmpOutType);
	}
	/**
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
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
	 * ��ȡǰ���У���Ӱ��{@link #readlines()}
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
	 * д�������ô˷����ر�
	 * �ر�������������fastQд��ת��ΪfastQ��ȡ
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
	 * ����Iterator-FastQRecord
	 * ��������ֱ����next�������һ����
	 * Ҳ���ǣ�<br>
	 * Iterator < FastQRecord > it = Fastq.readlinesIterator()<br>
	 * for(FastqRecord fastqrc : it)<br>
	 * { 
	 * ��������
	 * }
	 * @return
	 */
	public Iterator<FastQRecord> readlinesIterator() {
		return readlines().iterator();
	}
	/**
	 * ������ȡ�ļ�
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
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�Ϊ_filtered_1��_filtered_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	public FastQ filterReads() {
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
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�Ϊ_filtered_1��_filtered_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	public FastQ[] filterReads(FastQ fastQSPE) {
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
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * 
	 * @param Qvalue_Num
	 *            ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ��� int[0][0] = 13 int[0][1] = 7
	 *            :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	public FastQ filterReads(String fileFilterOut) {
		setFastQFormatLen();
		try {
			return filterReadsExp( fileFilterOut);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("filter Error: "+ fileFilterOut);
			return null;
		}
	}
	/**
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * 
	 * @param fastQSPE2 ���ļ��������
	 *            ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ��� int[0][0] = 13 int[0][1] = 7
	 *            :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	public FastQ[] filterReads(FastQ fastQSPE2, String fileFilterOut1, String fileFilterOut2) {
		setFastQFormatLen();
		try {
			return filterReadsExpPairEnd(fastQSPE2, fileFilterOut1, fileFilterOut2);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("filter Error: "+ fileFilterOut2);
			return null;
		}
	}
	/**
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * Ĭ�Ͻ�����ļ��ĺ�׺��Ϊfq��fq.gz
	 * @param Qvalue_Num
	 *            ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ��� int[0][0] = 13 int[0][1] = 7
	 *            :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	private FastQ filterReadsExp(String fileFilterOut) throws Exception {
		int readsNumFilter = 0; readsNum = 0;
		
		FastQ fastQ = new FastQ(fileFilterOut, true);
		fastQ.setCompressType(compressOutType, compressOutType);
		int mapNumLeft = -1, mapNumRight = 1;
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
		for (FastQRecord fastQRecord : readlines()) {
			readsNum++;
			fastQRecord.setFastqOffset(offset);
			fastQRecord.setTrimMinLen(readsLenMin);
			

			fastQRecord.setFastqOffset(phredOffset);
			fastQRecord.setTrimMinLen(readsLenMin);
			fastQRecord.setMapFastqFilter(mapFastQFilter);
			boolean filterSucess = fastQRecord.trimAdaptor(adaptorLeft, adaptorRight, mapNumLeft, 
					mapNumRight, adaptermaxMismach, adaptermaxConMismatch, proportionMisMathch);
			
			if (!filterSucess) continue;
			if (trimPolyA_right && !fastQRecord.trimPolyAR(2)) {
				continue;
			}
			if (trimPolyT_left && !fastQRecord.trimPolyTL(2)) {
				continue;
			}
			if (trimNNN && !fastQRecord.trimNNN(2)) {
				continue;
			}
			if (adaptorLowercase && !fastQRecord.trimLowCase()) {
				continue;
			}
			if (!fastQRecord.QC()) continue;
			
			readsNumFilter++;
			fastQ.writeFastQRecord(fastQRecord);
		}
		fastQ.readsNum = readsNumFilter;
		fastQ.noAdaptorReads = FastQRecord.getErrorTrimAdapterReadsNum();
		fastQ.closeWrite();
		return fastQ;
	}
	/**
	 * ������
	 * ��ȥadaptoer��Ȼ��ȥpolyA(�Ҷ�)��polyT(���)��Ȼ��ȥ����NNN��Ȼ��ȥ���������
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param fastQS2 ˫�˵���һ�˵�fastq�ļ�
	 * @param fileFilterOut
	 *            ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 *            �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @throws Exception
	 */
	private FastQ[] filterReadsExpPairEnd(FastQ fastQS2, String fileFilterOut1, String fileFilterOut2) throws Exception {
		int readsNumFilter = 0; readsNum = 0;
		FastQRecord.setErrorTrimAdapterReadsNum(0);
		FastQ fastQ1 = new FastQ(fileFilterOut1, true);
		FastQ fastQ2 = new FastQ(fileFilterOut2, true);
		fastQ1.setCompressType(compressOutType, compressOutType);
		fastQ2.setCompressType(compressOutType, compressOutType);
		
		Iterator<FastQRecord> itFastqRecord = fastQS2.readlinesIterator();
		
		int mapNumLeft = -1, mapNumRight = 1;
		if (adaptorScanLeftStart)
			mapNumLeft = 1;
		if (adaptorScanRightStart)
			mapNumRight = 1;
		
		for (FastQRecord fastQRecord : readlines()) {
			readsNum++;
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
			readsNumFilter++;
			fastQ1.writeFastQRecord(fastQRecord);
			fastQ2.writeFastQRecord(fastQRecord2);
		}
		fastQ1.closeWrite();
		fastQ2.closeWrite();
		fastQ1.noAdaptorReads = FastQRecord.getErrorTrimAdapterReadsNum();
		fastQ2.noAdaptorReads = FastQRecord.getErrorTrimAdapterReadsNum();
		fastQ1.readsNum = readsNumFilter;
		fastQ2.readsNum = readsNumFilter;
		return new FastQ[]{fastQ1, fastQ2};
	}
	/**
	 * <b>д�������� {@link #closeWrite} �����ر�</b>
	 * ������ʱ��Ҫ�趨Ϊcreatģʽ
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * �ڲ��ر�
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		for (FastQRecord fastQRecord : lsFastQRecords) {
			txtSeqFile.writefileln(fastQRecord.toString());
		}
		closeWrite();
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
		logger.error(seqFile
				+ " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
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
	/**
	 * ��fastq�ļ�ת��Ϊfasta�ļ�<br>
	 * �������ļ�Ϊ���ˣ� fastaFile<br>
	 * ˫�ˣ� ����к�׺��: ��fasta.aa<br>
	 * ��Ϊ fasta.aa �� fasta2.aa<br>
	 * û�к�׺����Ϊ fasta �� fasta2<br>
	 * @param fastaFile
	 * @throws Exception 
	 */
	public void convertToFasta(String fastaFile) throws Exception {
		TxtReadandWrite txtFasta = new TxtReadandWrite(fastaFile, true);
		for (FastQRecord fastQRecord : readlines()) {
			txtFasta.writefile(fastQRecord.getSeqFasta().toStringNRfasta());
		}
		txtFasta.close();
	}
	/**
	 * ͳ��reads�ֲ���ÿ��reads�����ķ���
	 */
	ListHashBin gffHashBin = new ListHashBin();
	String gffreadsLen = "Reads Length";
	String gffbpName = "BP";
	/**
	 * ��ʼ��reads�ֲ�ͳ����
	 * @reads�����
	 */
	private void initialGffHashBin(int maxReadsLen) {
		ArrayList<String[]> lsInfo = new ArrayList<String[]>();
		//reads ����
		for (int i = 1; i <= maxReadsLen; i++) {
			lsInfo.add(new String[]{gffreadsLen, i+ "", i + ""});
		}
		//ÿ�����������
		for (int i = 1; i <= maxReadsLen; i++) {
			for (int j = 1; j < 60; j++) {
				lsInfo.add(new String[]{i+gffbpName, j+ "", j + ""});
			}
		}
		gffHashBin.ReadGff(lsInfo);
	}
	/**
	 * �����ļ�������� �ļ���.fasta �� �ļ���.
	 * @param fileName
	 * @param illuminaOffset �Ƿ�Ϊillumina��offset
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
		//����������Ϊÿ����Ϊһ����Ԫ
		int num = 0;
		String title = ""; String fasta = ""; String quality = "";
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//������
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sffת��������������fasta��quality�ǲ�������ͬһ���ļ�");
				}
				title = "@" + contentFasta.substring(1);
				num++;
			}
			//��������
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
	 * ����һϵ��offset��������ת��Ϊfastq��quality��
	 * @param illumina �Ƿ���illumina��offset 
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
		return mapReadsQualtiy;
	}
}
