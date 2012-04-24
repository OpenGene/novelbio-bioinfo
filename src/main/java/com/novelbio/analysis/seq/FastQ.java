package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.print.attribute.standard.Fidelity;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

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
	
	private TxtReadandWrite txtSeqFile2 = new TxtReadandWrite();

	private int offset = 0;
	private boolean booPairEnd = false;
	// ��ʱ��������fastQ�ļ������������˫�˲����ʱ����֣���ʱ����ҪЭͬ����
	private String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * ˫�˵�ʱ��ֻ���������ж��ǺõĲű���
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/**
	 * FastQ�ļ��ĵ����������е������У�����Ϊ4-1 = 3
	 */
	private int QCline = 3;
	/**
	 * ��һ��reads�ĳ���
	 */
	private int readsLen = 0;
	/**
	 * ���reads�ĳ��ȣ�С�ڸó��ȵ�reads������
	 */
	private int readsLenMin = 25;
	
	private int adaptermaxMismach = 2;
	private int adaptermaxConMismatch = 1;
	
	public static void main(String[] args) {
		FastQ.convertSff2FastQ("/media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_miRNA.fasta", true);
		
	}
	
	
	/**
	 * �趨���reads�ĳ��ȣ�С�ڸó��ȵ�reads��������Ĭ��Ϊ25
	 */
	public void setReadsLenMin(int readsLenMin) {
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
	 * Ĭ���е���������
	 */
	private int quality = QUALITY_MIDIAN;
	
	/**
	 * fastQ����asc||���ָ�������
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();

	// ///////////////////////// barcode ����ı���
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * ��¼barcode����Ϣ key: barcode ������ value: barcode����Ӧ������
	 */
	HashMap<String, String> hashBarcodeName = new HashMap<String, String>();
	/**
	 * ˳���¼barcode��������������Ļ��������ڸ�list�������
	 */
	ArrayList<String> lsBarCode = new ArrayList<String>();
	/**
	 * ��¼barcode�ĳ���
	 */
	TreeSet<Integer> treeLenBarcode = new TreeSet<Integer>();
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////  �� �� �� ��  ////////////////////
	boolean trimPolyA_right = false;
	/**
	 * true�Ļ���û��polyA�����в�Ҫ
	 */
	boolean flagPolyA = false;
	/**
	 * true�Ļ���û��polyT�����в�Ҫ
	 */
	boolean flagPolyT = false;
	boolean trimPolyT_left = false;
	/**
	 * ��ͷ��Сд
	 * �������Ŀǰֻ��ion proton�������з���
	 */
	boolean adaptorLowercase = false;
	/**
	 * �Ƿ��������ߵ�NNNɾ��
	 */
	boolean trimNNN = true;
	/**
	 * �Ƿ��������ߵ�NNNɾ��
	 * Ĭ����ɾ���ģ����Ǹо��ٶȺ���Ȼ��cufflink��������
	 */
	public void setTrimNNN(boolean trimNNN)
	{
		this.trimNNN = trimNNN;
	}
	
	/**
	 * �趨��adaptor�Ͳ�Ҫ�趨PolyA
	 * @param trimPolyA_right
	 */
	public void setTrimPolyA(boolean trimPolyA_right, boolean flagPlogA) {
		this.trimPolyA_right = trimPolyA_right;
		this.flagPolyA = flagPlogA;
	}
	/**
	 * �趨��adaptor�Ͳ�Ҫ�趨PolyA
	 * @param trimPolyA_right
	 */
	public void setTrimPolyT(boolean trimPolyT_left, boolean flagPlogT) {
		this.trimPolyT_left = trimPolyT_left;
		this.flagPolyT = flagPlogT;
	}
	
	
	
	String adaptorLeft = "";
	String adaptorRight = "";
	/**
	 * ע��adapter���治Ҫ�з�ATGC�Ķ���
	 * @param adaptor
	 */
	public void setAdaptorLeft(String adaptor) {
		this.adaptorLeft = adaptor.trim();
	}
	/**
	 * �趨��polyA�Ͳ�Ҫ�趨adaptor
	 * ע��adapter���治Ҫ�з�ATGC�Ķ���
	 * @param adaptor
	 */
	public void setAdaptorRight(String adaptor) {
		this.adaptorRight = adaptor.trim();
	}
	/**
	 * ��ͷ��Сд �������Ŀǰֻ��ion proton�������з���
	 * ò��454���������
	 */
	public void setAdaptorLowercase(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	//////////////////////////
	/**
	 * ���صڶ���FastQ�ļ����ļ��� ���û���򷵻�null
	 * 
	 * @return
	 */
	public String getSeqFile2() {
		return seqFile2;
	}

	/**
	 * ����FastQ�ĸ�ʽλ�ƣ�һ���� FASTQ_SANGER_OFFSET �� FASTQ_ILLUMINA_OFFSET
	 * 
	 * @return
	 */
	public int getOffset() {
		setFastQFormat();
		return offset;
	}

	/**
	 * �����ļ��趨�Ĺ�������
	 * 
	 * @return
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * �����Ƿ���˫�˲����FastQ�ļ�����ʵҲ���ǿ��Ƿ�������FastQ�ļ�
	 * 
	 * @return
	 */
	public boolean isPairEnd() {
		return booPairEnd;
	}
	
	/**
	 * ��õ�һ��reads�ĳ��ȣ����ظ���˵������
	 * 
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
	 * ����ǰ���ж��ļ��Ƿ����,������ж������ļ��Ƿ���ͬһ����������� ��ô�����ж��Ƿ�ΪfastQ��ʽ��fasQ��ʽ��һ���Ƿ�һ��
	 * ��׼�ļ����Ļ����Զ��ж��Ƿ�Ϊgzѹ��
	 * @param seqFile1
	 *            �����ļ�
	 * @param seqFile2
	 *            ˫�˲�����������ļ���û�о���null��������ļ��Ƿ����
	 * @param fastQFormat
	 *            ����fastQ��ʽ��������FASTQ_SANGER_OFFSET��FASTQ_ILLUMINA_OFFSET����
	 *            ��֪����д0���������ļ����ж�
	 * @param QUALITY
	 *            QUALITY_LOW��
	 * 
	 */
	public FastQ(String seqFile1, String seqFile2, int FastQFormateOffset,
			int QUALITY) {
		super(seqFile1, 4);// fastQһ��4��Ϊһ������
		String houzhui = FileOperate.getFileNameSep(seqFile1)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		}
		else {
			setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
		}
		txtSeqFile.setParameter(compressInType, seqFile1, false,true);
		if (seqFile2 != null && !seqFile2.trim().equals("") && FileOperate.isFileExist(seqFile2.trim())) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
			txtSeqFile2.setParameter(compressInType, seqFile2, false, true);
		}
		if (FastQFormateOffset == FASTQ_SANGER_OFFSET) {
			offset = 33;
		} else if (FastQFormateOffset == FASTQ_ILLUMINA_OFFSET) {
			offset = 64;
		}

		setHashFastQFilter(QUALITY);
	}

	public void setCompressType(String cmpInType, String cmpOutType) {
		super.setCompressType(cmpInType, cmpOutType);
		if (txtSeqFile2 != null) {
			txtSeqFile2.setFiletype(cmpInType);
		}
	}
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * 
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY FastQ.Quality
	 */
	public FastQ(String seqFile1, String seqFile2, int QUALITY) {
		this(seqFile1, seqFile2, 0, QUALITY);
		String houzhui = FileOperate.getFileNameSep(seqFile1)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		}
		else {
			setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
		}
	}

	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * 
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, int QUALITY) {
		this(seqFile1, null, QUALITY);
		String houzhui = FileOperate.getFileNameSep(seqFile1)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		}
		else {
			setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
		}
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
	private FastQ filterReadsExp(String fileFilterOut) throws Exception {
		setFastQFormat();
		txtSeqFile.reSetInfo();//setParameter(compressInType, seqFile, false, true);
		BufferedReader readerSeq = txtSeqFile.readfile();
		BufferedReader readerSeq2 = null;
		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		String suffix = "fq";
		if (compressOutType == TxtReadandWrite.GZIP) {
			suffix = "gz";
		}
		if (!booPairEnd) 
			txtOutFile.setParameter(compressOutType, fileFilterOut.trim(), true, false);
		else 
			txtOutFile.setParameter(compressOutType, FileOperate.changeFileSuffix(fileFilterOut.trim(), "_1", suffix), true, false);
		
		TxtReadandWrite txtOutFile2 = new TxtReadandWrite();;
		if (booPairEnd) {
			txtSeqFile2.reSetInfo();
			readerSeq2 = txtSeqFile2.readfile();
			txtOutFile2.setParameter(compressOutType, FileOperate.changeFileSuffix(fileFilterOut.trim(), "_2", suffix), true, false);
		}

		String content = "";
		String content2 = null;
		int count = 0;
		String seqBlock1 = "";
		String seqBlock2 = "";
		while ((content = readerSeq.readLine()) != null) {
			count ++;
			seqBlock1 = seqBlock1 + content + TxtReadandWrite.huiche;
			
			if (booPairEnd) {
				content2 = readerSeq2.readLine();
				seqBlock2 = seqBlock2 + content2 + TxtReadandWrite.huiche;
			}
			
			if (count == block)
			{
				seqBlock1 = seqBlock1.trim();
				if (booPairEnd)
					seqBlock2 = seqBlock2.trim();
				
				//////////////  adaptor  ///////////////////////////////////////////////
				seqBlock1 = trimAdaptor(seqBlock1);
				if (booPairEnd)
					seqBlock2 = trimAdaptor(seqBlock2);
				
				
				if (seqBlock1 == null || seqBlock2 == null) {
					seqBlock1 = ""; seqBlock2 = "";
					count = 0;// ����
					continue;
				}
				///////////// polyA ///////////////////////////////////////////////////////
				if (trimPolyA_right) {
					seqBlock1 = trimPolyAR(seqBlock1, 2);
					if (booPairEnd)
						seqBlock2 = trimPolyAR(seqBlock2, 2);
					
					if (seqBlock1 == null || seqBlock2 == null) {
						seqBlock1 = ""; seqBlock2 = "";
						count = 0;// ����
						continue;
					}
				}
				///////////// polyT ///////////////////////////////////////////////////////
				if (trimPolyT_left) {
					seqBlock1 = trimPolyTL(seqBlock1, 2);
					if (booPairEnd)
						seqBlock2 = trimPolyTL(seqBlock2, 2);
					
					if (seqBlock1 == null || seqBlock2 == null) {
						seqBlock1 = ""; seqBlock2 = "";
						count = 0;// ����
						continue;
					}
				}
			
				///////////////  tail NNN  ////////////////////////////////////////////
				if (trimNNN) {
					seqBlock1 = trimNNN(seqBlock1, 2);
					if (booPairEnd)
						seqBlock2 = trimNNN(seqBlock2, 2);
					
					if (seqBlock1 == null || seqBlock2 == null) {
						seqBlock1 = ""; seqBlock2 = "";
						count = 0;// ����
						continue;
					}
				}
				///////////// Lowcase ///////////////////////////////////////////////////////
				if (adaptorLowercase) {
					seqBlock1 = trimLowCase(seqBlock1);
					if (booPairEnd)
						seqBlock2 = trimLowCase(seqBlock2);
					
					if (seqBlock1 == null || seqBlock2 == null) {
						seqBlock1 = ""; seqBlock2 = "";
						count = 0;// ����
						continue;
					}
				}
				///////////////////  QC  /////////////////////////////////////////////////////////
				
				if (QCBlock(seqBlock1, seqBlock2)) {
					txtOutFile.writefileln(seqBlock1);
					if (booPairEnd) {
						txtOutFile2.writefileln(seqBlock2);
					}
				}
				// ���
				seqBlock1 = "";
				seqBlock2 = "";
				count = 0;// ����
				continue;
			}
		}
		FastQ fastQ = null;

		if (booPairEnd) {
			fastQ = new FastQ(fileFilterOut.trim() + "_1", fileFilterOut.trim()
					+ "_2", offset, quality);
		} else {
			fastQ = new FastQ(fileFilterOut.trim(), null, offset, quality);
		}
		fastQ.setCompressType(compressOutType, compressOutType);
		txtSeqFile.close();
		txtSeqFile2.close();
		txtOutFile.close();
		txtOutFile2.close();
		return fastQ;
	}
	
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	private String trimLowCase(String fastQBlock)
	{
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[1];//��õ������ж�����quality��Ϣ
		char[] info = ss.toCharArray();
		int numStart = 0;
		//��ǰ�������Сд�ͼ���
		for (char c : info) {
			if ((int)c > 90 )
				numStart++;
			else
				break;
		}
		int numEnd = info.length;
		for (int i = info.length - 1; i >= 0; i--) {
			if ((int)info[i] > 90 )
				numEnd--;
			else
				break;
		}
		if (numStart >= numEnd) {
			numStart = numEnd;
		}
//		int numEnd = trimNNNRight(ss, 10, numMM);
		return trimBlockSeq(fastQBlock, numStart, numEnd);
	}
	
	
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	private String trimNNN(String fastQBlock, int numMM)
	{
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[3];
		int numStart = trimNNNLeft(ss, 10, numMM);
//		if (numStart > 0) {
//			System.out.println(ss);
//		}
		int numEnd = trimNNNRight(ss, 10, numMM);
		return trimBlockSeq(fastQBlock, numStart, numEnd);
	}
	
	/**
	 * 
	 * �����Ҷ˵��������У�Q10��Q13����Ϊ���������У�һ·����ֱ��ȫ���й�Ϊֹ
	 * @param seqIn ������
	 * @param cutOff ���������е�cutOff, С�ڵ������ͻᱻcut
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 * 	 * ���ظ�NNN�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�NNNǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * ����-1��ʾ����
	 */
	private int trimNNNRight(String seqIn,int cutOff, int numMM) {
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//��¼�����ĵ��������ַ��м���
		for (int i = lenIn-1; i >= 0; i--) {
			if ((int)chrIn[i] - offset > cutOff) {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM) {
				return i+con;//�������a�Ļ��ļӻ�ȥ
			}
		}
//		logger.info("no useful seq: "+ seqIn);
		return 0;
	}
	/**
	 * 
	 * ������˵��������У�Q10��Q13����Ϊ���������У�һ·����ֱ��ȫ���й�Ϊֹ
	 * @param seqIn ������
	 * @param cutOff ���������е�cutOff, С�ڵ������ͻᱻcut
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 * 	 * ���ظ�NNN�ĵ����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�NNN�ж��ٸ����������ֱ����substring(return)����ȡ
	 * ����-1��ʾ����
	 */
	private int trimNNNLeft(String seqIn,int cutOff, int numMM) {
		char[] chrIn = seqIn.toCharArray();
		int numMismatch = 0;
		int con = -1;//��¼�����ĵ��������ַ��м���
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - offset > cutOff) {
				numMismatch++;
				con++;
			}
			else {
				con = -1;
			}
			if (numMismatch > numMM) {
				return i - con;//�������a�Ļ��ļӻ�ȥ
			}
		}
//		logger.info("no useful seq: "+ seqIn);
		return seqIn.length();
	}
	
	/**
	 * ÿ����һ��block�����������block�ķ�������Ҫ�ǽض�
	 * block������TxtReadandWrite.huiche����
	 * @param block
	 * @param start ��substringһ�����÷�
	 * @param end ��substringһ�����÷�
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 * ����ض̺�ĳ���С���趨�����reads���ȣ���ô�ͷ���null
	 */
	private String trimBlockSeq(String block, int start, int end)
	{
		if (end - start + 1 < readsLenMin) {
			return null;
		}
		String[] ss = block.split(TxtReadandWrite.huiche);
		if (start == 0 && end == ss[3].length()) {
			return block.trim();
		}
		ss[1] = ss[1].substring(start, end);
		ss[3] = ss[3].substring(start, end);
		String ssResult = ss[0] + TxtReadandWrite.huiche + ss[1] + TxtReadandWrite.huiche + ss[2] + TxtReadandWrite.huiche + ss[3];
		return ssResult;
	}
	/**
	 * �����Ҳ�polyA
	 * @param block
	 * @param mismatch �����趨����΢��һ��㣬��Ϊ�������趨���������Ϊ1�ˣ��������ｨ��2-3
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 */
	private String trimPolyAR(String fastQBlock, int mismatch)
	{
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[1];
		int num = super.trimPolyA(ss, mismatch,1);
		if (flagPolyA && num == ss.length()) {
			return null;
		}
		return trimBlockSeq(fastQBlock, 0, num);
	}
	/**
	 * �������polyT
	 * @param block
	 * @param mismatch �����趨����΢��һ��㣬��Ϊ�������趨���������Ϊ1�ˣ��������ｨ��2-3
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 */
	private String trimPolyTL(String fastQBlock, int mismatch)
	{
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[1];
		int num = super.trimPolyT(ss, mismatch,1);
		if (flagPolyT && num == 0) {
			return null;
		}
		return trimBlockSeq(fastQBlock, num, ss.length());
	}
	/**
	 * ������������Ľ�ͷ
	 * ����ض̺�ĳ���С���趨�����reads���ȣ���ô�ͷ���null
	 * @param fastQBlock
	 * @return
	 */
	private String trimAdaptor(String fastQBlock) {
		if (adaptorLeft.equals("") && adaptorRight.equals("")) {
			return fastQBlock.trim();
		}
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[1];
		int leftNum = super.trimAdaptorL(ss, adaptorLeft, adaptorLeft.length(), adaptermaxMismach,adaptermaxConMismatch, 30);
		int rightNum = super.trimAdaptorR(ss, adaptorRight,ss.length() - adaptorRight.length(), adaptermaxMismach,adaptermaxConMismatch, 30);
		return trimBlockSeq(fastQBlock, leftNum, rightNum);
	}
	
	
	private boolean QCBlock(String seqBlock1, String seqBlock2) {
		if (seqBlock1 == null && seqBlock2 == null) {
			return false;
		}
		String ss1 = seqBlock1.split(TxtReadandWrite.huiche)[3];
		String ss2 = null;
		if (seqBlock2 != null && !seqBlock2.equals("")) {
			ss2 = seqBlock2.split(TxtReadandWrite.huiche)[3];
		}
		else {
			ss2 = null;
		}
		if (QC(ss1, ss2)) {
			return true;
		}
		return false;
	}
	/**
	 * ����˫�˲�����������У������������е������Ƿ����Ҫ�� ���Ȼ��ж������Ƿ���BBBBB��β���ǵĻ�ֱ������ �и��е�����ѡ��
	 * 
	 * @param seq1
	 *            ˫�˲���ĵ�һ��
	 * @param seq2
	 *            ˫�˲���ĵڶ��ˣ�û����Ϊnull��""
	 * @return
	 */
	private boolean QC(String seq1, String seq2) {
		boolean booQC1 = false;
		boolean booQC2 = false;

		if (seq1.endsWith("BBBBBBB") || (seq2 != null && seq2.endsWith("BBBBBBB"))) {
			return false;
		}

		/**
		 * �Ϳ�Q10��Q13��Q20������
		 */
		int[][] seqQC1 = copeFastQ(offset, seq1, 2, 10, 13, 20);
		booQC1 = filterFastQ(seqQC1);
		int[][] seqQC2 = null;
		if (seq2 != null && !seq2.trim().equals("")) {
			seqQC2 = copeFastQ(offset, seq2, 2, 10, 13, 20);
			booQC2 = filterFastQ(seqQC2);
		}

		if (quality == QUALITY_HIGM || quality == QUALITY_MIDIAN_PAIREND) {
			if (seq2 == null || seq2.trim().equals("")) {
				return booQC1;
			} else {
				return booQC1 && booQC2;
			}
		} else if (quality == QUALITY_MIDIAN || quality == QUALITY_LOW) {
			return booQC1 || booQC2;
		}
		return true;
	}
	/**
	 * ����һ��fastQ��ascII�룬ͬʱָ��һϵ�е�Qֵ������asc||С�ڸ�Qֵ��char�ж���
	 * ����Qvalue�����˳�����������Ӧ��int[]
	 * 
	 * @param FASTQ_FORMAT_OFFSET
	 *            offset�Ƕ��٣�FASTQ_SANGER_OFFSET��
	 * @param fastQSeq
	 *            �����fastQ�ַ���
	 * @param Qvalue
	 *            Qvalue����ֵ������ָ�����<b>�����С��������</b>��һ��ΪQ13����ʱΪQ10�������ά���ٿƵ�FASTQ
	 *            format
	 * @return int ����˳��С�ڵ���ÿ��Qvalue������
	 */
	public int[][] copeFastQ(int FASTQ_FORMAT_OFFSET, String fastQSeq, int... Qvalue) {
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = fastQSeq.toCharArray();
		//reads���ȷֲ���һ������454
//		gffHashBin.addNumber(gffreadsLen, fastq.length);
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - FASTQ_FORMAT_OFFSET;
			/////////////////////////����������ÿ������������ֲ�ͳ��/////////////////////////////////////////////////
//			gffHashBin.addNumber(m+gffbpName, qualityScore);
			//////////////////////////////////////////////////////////////////////////
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if (qualityScore <= Qvalue[i]) {//ע����С�ڵ���
					qNum[i][1]++;
					continue;
				} else {
					break;
				}
			}
		}
		return qNum;
	}
	/**
	 * ��mismatich�ȶ�ָ���ļ������Ƿ����
	 * @param thisFastQ
	 * @return
	 */
	private boolean filterFastQ(int[][] thisFastQ) {
		for (int[] is : thisFastQ) {
			Integer Num = hashFastQFilter.get(is[0]);
			if (Num == null) {
				continue;
			} else if (Num < is[1]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ���FastQ��ʽû���趨�ã�ͨ���÷����趨FastQ��ʽ
	 */
	private void setFastQFormat() {
		if (offset != 0) {
			return;
		}
		int fastQformat = guessFastOFormat(getLsFastQSeq(5000));
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
	 * 
	 * @param Num
	 *            ��ȡ�����У�ָ�����ȡ������
	 * @return fastQ�ʿ����е�list ������null
	 */
	private ArrayList<String> getLsFastQSeq(int Num) {
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
			String content = "";
			BufferedReader reader = txtSeqFile.readfile(); int thisnum = 0;
			while ((content = reader.readLine()) != null && thisnum < Num) {
				if (thisnum%4 == QCline) {
					if (content.contains("BBB")) {
						thisnum ++;
						continue;
					}
					lsResult.add(content);
				}
				thisnum ++;
			}
		} catch (Exception e) {
			logger.error(seqFile + " may not exits");
			return null;
		}
		return lsResult;
	}

	/**
	 * ����һϵ�е�fastQ��ʽ���²��fastQ������sanger����solexa
	 * 
	 * @param lsFastQ
	 *            :ÿһ��string ����һ��fastQ
	 * @return FASTQ_ILLUMINA����FASTQ_SANGER
	 */
	public int guessFastOFormat(List<String> lsFastQ) {
		double min25 = 70;
		double max75 = 70;
		DescriptiveStatistics desStat = new DescriptiveStatistics();
		for (String string : lsFastQ) {
			if (string.trim().equals("")) {
				continue;
			}
			char[] fastq = string.toCharArray();
			for (int i = 0; i < fastq.length; i++) {
				desStat.addValue((double) fastq[i]);
			}
		}
		min25 = desStat.getPercentile(5);
		max75 = desStat.getPercentile(90);
		if (min25 < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (max75 > 95) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		// ���ǰ������û�㶨�����滹���ж�
		if (desStat.getMin() < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (desStat.getMax() > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		logger.error(seqFile
				+ " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// ��û�жϳ������²�Ϊillumina��ʽ
		return FASTQ_ILLUMINA_OFFSET;
	}



	// ////////////////// barcode ɸѡ����
	// ///////////////////////////////////////////////////////////////////////////////////

//	/**
//	 * �����а���barcode�ָ�ɼ�����ͬ���ļ������ֱ𱣴�Ϊ��ص��ļ���
//	 * 
//	 * @param outFilePrix
//	 * @param barcodeAndName
//	 *            һ��barcode����--�Զ�ת��Ϊ��д��һ��barcode���� ����barcodeAndName������һ��ż�����ȵ�����
//	 */
//	public void sepBarCode(String outFilePrix, String... barcodeAndName) {
//		if (barcodeAndName.length % 2 != 0) {
//			String out = "";
//			for (String string : barcodeAndName) {
//				out = out + string + "  ";
//			}
//			logger.error(outFilePrix + " barcode �������: " + out);
//		}
//		setHashBarCode(barcodeAndName);
//	}

	/**
	 * ָ��������ļ�ȫ�����Զ���·�������ļ�������Ϊǰ׺ ������OutFilePathPrixΪ : /usr/local/bar.txt �ļ���Ϊ:
	 * /usr/local/bar_barcodename.txt ˫������һ��Ϊ�� /usr/local/bar_barcodename2.txt
	 * 
	 * @param OutPrix
	 *            ����ļ�ȫ��
	 * @param maxmismatch
	 *            barcode ������
	 * @param barcodeAndName
	 *            һ��barcode����--�Զ�ת��Ϊ��д��һ��barcode����
	 * @throws Exception
	 * @return barcode�ļ���,���˫����
	 */
	public String[] filterBarcode(String OutFilePathPrix,int maxmismatch,String...barcodeAndName) throws Exception 
	{
		String filePath = FileOperate.getParentPathName(OutFilePathPrix) + "/";
		String fileName[] = FileOperate.getFileNameSep(OutFilePathPrix);
		setHashBarCode(barcodeAndName);

		HashMap<String, TxtReadandWrite> hashBarcodeTxt = new HashMap<String, TxtReadandWrite>();
		HashMap<String, TxtReadandWrite> hashBarcodeTxt2 = new HashMap<String, TxtReadandWrite>();
		String[] resultFileName = null;
		if (booPairEnd) {
			resultFileName = new String[barcodeAndName.length+2];
			resultFileName[resultFileName.length-2] = filePath+fileName[0]+"_"+"notfind."+fileName[1];
			resultFileName[resultFileName.length-1] = filePath+fileName[0]+"_"+"notfind2."+fileName[1];
		}
		else {
			resultFileName = new String[barcodeAndName.length/2+1];
			resultFileName[resultFileName.length-1] = filePath+fileName[0]+"_"+"notfind"+"."+fileName[1];
		}
		//������Ӧ�Ľ���ļ�txt��
		int k = 0;//resultFileName������
		for(Entry<String,String> entry:hashBarcodeName.entrySet())
		{
			String barcodename = entry.getValue();
			TxtReadandWrite txtBarcod = new TxtReadandWrite();
			resultFileName[k] = filePath+fileName[0]+"_"+barcodename+"."+fileName[1];
			txtBarcod.setParameter(compressOutType, resultFileName[k], true, false);
			hashBarcodeTxt.put(barcodename, txtBarcod);
			k++;
			if (booPairEnd) {
				TxtReadandWrite txtBarcod2 = new TxtReadandWrite();
				resultFileName[k] = filePath+fileName[0]+"_"+barcodename+"2."+fileName[1];
				txtBarcod2.setParameter(compressOutType, resultFileName[k], true, false);
				hashBarcodeTxt2.put(barcodename, txtBarcod2);
				k++;
			}
		}
		TxtReadandWrite txtBarcod = new TxtReadandWrite();
		resultFileName[k] = filePath+fileName[0]+"_"+"notfind."+fileName[1];
		txtBarcod.setParameter(compressOutType, resultFileName[k], true, false);
		hashBarcodeTxt.put("notfind", txtBarcod);
		k++;
		if (booPairEnd) {
			TxtReadandWrite txtBarcod2 = new TxtReadandWrite();
			resultFileName[k] = filePath+fileName[0]+"_"+"notfind2."+fileName[1];
			txtBarcod2.setParameter(compressOutType, resultFileName[k], true, false);
			hashBarcodeTxt2.put("notfind", txtBarcod2);
			k++;
		}

		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		BufferedReader reader1 = txtSeqFile.readfile();
		BufferedReader reader2 = null;
		if (booPairEnd) {
			txtSeqFile2.reSetInfo();
			reader2 = txtSeqFile2.readfile();
		}
		String content1 = ""; String content2 = "";
		int count = 0; //�������ڼ���
		TxtReadandWrite txtTmp1 = null; TxtReadandWrite txtTmp2 = null;//����������ָ��hashBarcodeTxt�еĶ���Ȼ������������д
		String tmpresult1 = ""; String tmpresult2 = "";//д��Ķ���
		String[] barInfo = null;
		while ((content1 = reader1.readLine()) != null) {
			if (booPairEnd) {
				content2 = reader2.readLine();
			}
			if (count == 0) {
				tmpresult1 = content1;
				if (booPairEnd) {
					tmpresult2 = content2;
				}
				count ++;
				continue;
			}
		
			if (count == 1) {//������
				//���������ı���barcodeһ����ѡ��barcodename
				barInfo = getBarCodeInfo(content1, maxmismatch);
				if (barInfo == null) {//�����һ���ı�û��barcode����ô���ҵڶ��������û�еڶ����ı���������
					if (booPairEnd) {
						barInfo = getBarCodeInfo(content2, maxmismatch);
						if (barInfo == null)
						{
							txtTmp1 = hashBarcodeTxt.get("notfind");
							txtTmp2 = hashBarcodeTxt2.get("notfind");
							tmpresult1 = tmpresult1 + "\n" + content1;
							tmpresult2 = tmpresult2 + "\n" + content2;
						}
					}
					else {
						txtTmp1 = hashBarcodeTxt.get("notfind");
						tmpresult1 = tmpresult1 + "\n" + content1;
					}
				}
				if (barInfo != null) {
					txtTmp1 = hashBarcodeTxt.get(barInfo[0]);
					tmpresult1 = tmpresult1 + "\n" + content1.substring(barInfo[1].length());
					if (booPairEnd) {
						txtTmp2 = hashBarcodeTxt2.get(barInfo[0]);
						tmpresult2 = tmpresult2 + "\n" + content2.substring(barInfo[1].length());
					}
				}
				count++;
				continue;
			}
			
			if (count == 3)
			{
				if( barInfo != null) {
					tmpresult1 = tmpresult1 + "\n" + content1.substring(barInfo[1].length());
					txtTmp1.writefileln(tmpresult1);
					if (booPairEnd) {
						tmpresult2 = tmpresult2 + "\n" + content2.substring(barInfo[1].length());
						txtTmp2.writefileln(tmpresult2);
					}
				}
				else {
					tmpresult1 = tmpresult1 + "\n" + content1;
					txtTmp1.writefileln(tmpresult1);
					if (booPairEnd) {
						tmpresult2 = tmpresult2 + "\n" + content2;
						txtTmp2.writefileln(tmpresult2);
					}
				}
				count = 0;
				tmpresult1 = ""; tmpresult2 = "";
				continue;
			}
			count++;
//			if( barInfo != null) {
				tmpresult1 = tmpresult1 + "\n" + content1;
				if (booPairEnd) {
					tmpresult2 = tmpresult2 + "\n" + content2;
				}
//			}
		}
		Collection<TxtReadandWrite> hashValtxt = hashBarcodeTxt.values();
		for(TxtReadandWrite txt:hashValtxt)
		{
			txt.close();
		}
		if (booPairEnd) {
			Collection<TxtReadandWrite> hashValtxt2 = hashBarcodeTxt2.values();
			for(TxtReadandWrite txt:hashValtxt2)
			{
				txt.close();
			}
		}

		return resultFileName;
	}

	/**
	 * ����һ��һά���飬����װ��HashBarCode�� �����ʽΪ 0��barcode ���� 1��barcode��Ӧ������ 2��barcode ����
	 * 3��barcode��Ӧ������
	 */
	private void setHashBarCode(String[] barcodeAndName) {
		if (!hashBarcodeName.isEmpty()) {
			return;
		}
		for (int i = 0; i < barcodeAndName.length - 1; i = i + 2) {
			hashBarcodeName.put(barcodeAndName[i].trim().toUpperCase(),
					barcodeAndName[i + 1].trim());
			lsBarCode.add(barcodeAndName[i].trim().toUpperCase());
		}
		// ////////////// ���barcode���ȳ��Ļ�����barcode�ĳ�������һ��list
		// /////////////////////////
		for (String barcode : lsBarCode) {
			treeLenBarcode.add(barcode.length());
		}
	}

	/**
	 * ����barcode���У�����barcode���ʺ���mismatch�����barcodeName�;�������
	 * 
	 * @param barcodeSeq
	 * @param maxmismatch
	 *            ����barcode��������
	 * @return string[2]: 0 barcode name 1: barcode seq
	 *         ����ҵ���bacode����ô���ظ�barcode����Ӧ��name��û�ҵ��򷵻�null
	 */
	private String[] getBarCodeInfo(String seq, int maxmismatch) {
		for (Integer lenbarcode : treeLenBarcode) {
			String barcodeseq = seq.substring(0, lenbarcode);
			String[] barcodename = new String[2];
			if ((barcodename[0] = testBarCodeName(barcodeseq, maxmismatch)) != null) {
				barcodename[1] = barcodeseq;
				return barcodename;
			}
		}
		return null;
	}

	/**
	 * ����һ��barcode���У�����barcode����
	 * 
	 * @param barcodeseq
	 * @param maxmismatch
	 *            ������
	 * 
	 * @return
	 */
	private String testBarCodeName(String barcodeseq, int maxmismatch) {
		barcodeseq = barcodeseq.toUpperCase();
		String result = hashBarcodeName.get(barcodeseq);
		if (result != null || maxmismatch < 1) {
			return result;
		}
		// �洢mismatch��ǰ������������ǰ����һ������ô��˵���޷�ͨ��barcodeʶ����ô����null
		int[] tmpMismatch = { 10, 10 };
		int tmpbarcodeID = -1;
		for (int m = 0; m < lsBarCode.size(); m++) {
			String barcode = lsBarCode.get(m);
			if (barcodeseq.length() != barcode.length()) {
				continue;
			}
			char[] charbarcodeSeq = barcodeseq.toCharArray();
			char[] charbarcode = barcode.toCharArray();
			int mismatch = 0;
			for (int i = 0; i < barcode.length(); i++) {
				if (charbarcode[i] != charbarcodeSeq[i]) {
					mismatch++;
				}
				if (mismatch > maxmismatch) // barcode��������Ϳ���������
				{
					break;
				}
			}
			/**
			 * ���ֻ��һ�����䣬��Ȼ���Ǹ�barcode��
			 */
			if (mismatch <= 1 && barcodeseq.length() > 2) {
				return hashBarcodeName.get(barcode);
			}
			/**
			 * �����ٴ����barcode������barcodeName
			 */
			if (mismatch <= tmpMismatch[0]) {
				tmpMismatch[0] = mismatch;
				tmpMismatch[1] = tmpMismatch[0];
				tmpbarcodeID = m;
			}
		}
		// ���ٵ��������䶼һ�����޷����ֵ������ĸ�barcode������
		if (tmpMismatch[0] == tmpMismatch[1]) {
			return null;
		}
		/**
		 * �����ٴ����barcode��ʲô
		 */
		if (tmpMismatch[0] < maxmismatch) {
			return hashBarcodeName.get(lsBarCode.get(tmpbarcodeID));
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ר�Ÿ����ڸ���ƵĴ��룬��һ��fastQ�ļ��ָ�Ϊ����
	 * 
	 * @throws Exception
	 */
	public void WZFsepFastQ(String outFile) throws Exception {
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outFile + 1 + ".fastq", true, false);
		TxtReadandWrite txtOut2 = new TxtReadandWrite();
		txtOut2.setParameter(outFile + 2 + ".fastq", true, false);
		BufferedReader reader = txtSeqFile.readfile();
		String content = "";
		String out1 = "";
		String out2 = "";
		int flag = 1;// �����1�˻���2��
		boolean flagWrite = false;// ��ȡ�����˵���д���ı�
		int count = 0;
		while ((content = reader.readLine()) != null) {
			if (count == 0) {
				if (flagWrite) {
					flagWrite = false;
					txtOut1.writefile(out1);
					txtOut2.writefile(out2);
				}

				if (content.endsWith("/1")) {
					out1 = content + "\n";
					flag = 1;
					count++;
					continue;
				}
				if (content.endsWith("/2")) {
					String con1 = out1.split("\n")[0];
					if (!con1.substring(0, con1.length() - 1).equals(
							content.substring(0, content.length() - 1))) {
						logger.error("/1 and /2 are not equal"
								+ out1.split("\n")[0] + content);
						out1 = "";
						out2 = "";
						continue;
					}
					out2 = content + "\n";
					flag = 2;
					flagWrite = true;
					count++;
					continue;
				}
			}
			if (flag == 1) {
				out1 = out1 + content + "\n";
			}
			if (flag == 2) {
				out2 = out2 + content + "\n";
			}
			count++;
			if (count == 4) {
				count = 0;
			}
		}
		txtOut1.close();
		txtOut2.close();
		txtSeqFile.close();
		txtSeqFile2.close();
	}
	
//	
//	public FastQ trimPolyA(int filterNum,String fileFilterOut) {
//		try {
//			return trimPolyAExp(filterNum, fileFilterOut);
//		} catch (Exception e) {
//			logger.error("trimPolyA error:" + fileFilterOut);
//			return null;
//		}
//	}
//	/**
//	 * ָ����ֵ����fastQ�ļ�����polyA��Ŀǰֻ����Ե����Ҳ��polyA
//	 * 
//	 * @param filterNum ������̶೤������22
//	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
//	 * @throws Exception
//	 */
//	private FastQ trimPolyAExp(int filterNum,String fileFilterOut) throws Exception {
//		txtSeqFile.setParameter(seqFile, false, true);
//		BufferedReader readerSeq = txtSeqFile.readfile();
//		
//		TxtReadandWrite txtOutFile = new TxtReadandWrite();
//		txtOutFile.setParameter(fileFilterOut.trim(), true, false);
//
//		setFastQFormat();
//
//		String content = "";
//		int count = 0;int lastID = -10;
//		String tmpResult1 = "";
//		while ((content = readerSeq.readLine()) != null) {
//			if (count == 1) {
//				lastID = trimPolyA(content, 1);
//				if (lastID >= filterNum) {
//					tmpResult1 = tmpResult1 + content.substring(0,lastID) + "\n";
//				}
//				count++;
//				continue;
//			}
//			if (count == QCline) {
//				if (lastID >= filterNum) {
//					tmpResult1 = tmpResult1 + content.substring(0,lastID) + "\n";
//					txtOutFile.writefile(tmpResult1);
//				}
//				count = 0;// ����
//				tmpResult1 = "";
//				continue;
//			}
//			tmpResult1 = tmpResult1 + content + "\n";
//			count++;
//		}
//		FastQ fastQ = null;
//		fastQ = new FastQ(fileFilterOut.trim(), null, offset, quality);
//		txtSeqFile.close();
//		txtOutFile.close();
//		return fastQ;
//	}
	
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
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
		BufferedReader reader = txtSeqFile.readfile();
		
		TxtReadandWrite txtFasta1 = new TxtReadandWrite();
		txtFasta1.setParameter(fastaFile, true, false);
		
		TxtReadandWrite txtFasta2 = new TxtReadandWrite();
		BufferedReader reader2 = null;
		if (booPairEnd) {
			txtSeqFile2.reSetInfo();
			reader2 = txtSeqFile2.readfile();
			FileOperate.getFileNameSep(fastaFile);
			String filepath = "";
			if (FileOperate.getFileNameSep(fastaFile)[1].equals("")) {
				filepath = FileOperate.getParentPathName(fastaFile) +  FileOperate.getFileNameSep(fastaFile)[0] + "2" ;
			}
			else {
				filepath = FileOperate.getParentPathName(fastaFile)+"/" +  FileOperate.getFileNameSep(fastaFile)[0] + "2."+ FileOperate.getFileNameSep(fastaFile)[1];
			}
			txtFasta2.setParameter(filepath, true, false);
		}
		
		String content = ""; String content2 = "";
		String head1 = ""; String head2 = "";
		int count = 0; // �ڶ���������
		while ((content = reader.readLine()) != null) {
			if (booPairEnd) {
				content2 = reader2.readLine();
			}
			
			
			if (count == 0) {
				head1 = content.substring(1);
				if (booPairEnd) {
					head2 = content2.substring(1);
				}
				count ++;
				continue;
			}
			else if (count == 1) {
				txtFasta1.writefileln(">"+head1);
				txtFasta1.writefileln(content);
				if (booPairEnd) {
					txtFasta2.writefileln(">"+head2);
					txtFasta2.writefileln(content2);
				}
				count++;
				continue;
			}
			else if (count == 2) {
				count++; continue;
			}
			else if (count == 3) {
				count = 0;
				head1 = "";  head2 = "";
				continue;
			}
			logger.error("count error:" + count);
		}
		txtFasta1.close();
		txtFasta2.close();
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
				String tmpOut = title + TxtReadandWrite.huiche + fasta + TxtReadandWrite.huiche + "+" + TxtReadandWrite.huiche + quality;
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
	private static String convert2Phred(String qualityNum, int offset)
	{
		String[] quality = qualityNum.split(" ");
		char[] tmpResultChar = new char[quality.length];
		for (int i = 0; i < quality.length; i++) {
			String string = quality[i];
			tmpResultChar[i] = (char) (offset + Integer.parseInt(string));
		}
		return String.valueOf(tmpResultChar);
	}
	
}
