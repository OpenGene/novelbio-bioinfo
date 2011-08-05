package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

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

	TxtReadandWrite txtSeqFile2 = new TxtReadandWrite();

	int offset = 0;
	boolean booPairEnd = false;
	// ��ʱ��������fastQ�ļ������������˫�˲����ʱ����֣���ʱ����ҪЭͬ����
	String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * ˫�˵�ʱ��ֻ���������ж��ǺõĲű���
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	/**
	 * FastQ�ļ��ĵ����������е������У�����Ϊ4-1 = 3
	 */
	int QCline = 3;
	/**
	 * ��һ��reads�ĳ���
	 */
	int readsLen = 0;
	/**
	 * Ĭ���е���������
	 */
	int quality = 20;

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
	public boolean getBooPairEnd() {
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
		txtSeqFile.setParameter(seqFile, false, true);
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
			hashFastQFilter.put(10, 0);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		} else if (QUALITY == QUALITY_LOW) {
			quality = QUALITY;
			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 8);
			hashFastQFilter.put(20, 15);
		} else if (QUALITY == QUALITY_MIDIAN
				|| QUALITY == QUALITY_MIDIAN_PAIREND) {
			quality = QUALITY;
			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}

	/**
	 * ����ǰ���ж��ļ��Ƿ����,������ж������ļ��Ƿ���ͬһ����������� ��ô�����ж��Ƿ�ΪfastQ��ʽ��fasQ��ʽ��һ���Ƿ�һ��
	 * 
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
		if (seqFile2!= null && FileOperate.isFileExist(seqFile2.trim())) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
		}
		if (FastQFormateOffset == FASTQ_SANGER_OFFSET) {
			offset = 33;
		} else if (FastQFormateOffset == FASTQ_ILLUMINA_OFFSET) {
			offset = 64;
		} else {
			offset = 0;
		}

		setHashFastQFilter(QUALITY);
	}

	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * 
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, String seqFile2, int QUALITY) {
		super(seqFile1, 4);// fastQһ��4��Ϊһ������
		if (FileOperate.isFileExist(seqFile2.trim())) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
		}
		offset = 0;
		setHashFastQFilter(QUALITY);

	}

	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * 
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, int QUALITY) {
		super(seqFile1, 4);// fastQһ��4��Ϊһ������
		offset = 0;
		setHashFastQFilter(QUALITY);
	}

	/**
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
	public FastQ filterReads(String fileFilterOut) throws Exception {
		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader readerSeq = txtSeqFile.readfile();
		BufferedReader readerSeq2 = null;

		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		if (!booPairEnd) {
			txtOutFile.setParameter(fileFilterOut.trim(), true, false);
		} else {
			txtOutFile.setParameter(fileFilterOut.trim() + "_1", true, false);
		}
		TxtReadandWrite txtOutFile2 = null;
		if (booPairEnd) {
			txtSeqFile2.setParameter(seqFile2, false, true);
			readerSeq2 = txtSeqFile2.readfile();
			txtOutFile2 = new TxtReadandWrite();
			txtOutFile2.setParameter(fileFilterOut.trim() + "_2", true, false);
		}
		setFastQFormat();

		String content = "";
		String content2 = null;
		int count = 0;
		String tmpResult1 = "";
		String tmpResult2 = "";
		while ((content = readerSeq.readLine()) != null) {
			if (booPairEnd) {
				content2 = readerSeq2.readLine().trim();
			}
			if (count == QCline) {
				if (QC(content, content2)) {
					tmpResult1 = tmpResult1 + content + "\n";
					txtOutFile.writefile(tmpResult1);
					if (booPairEnd) {
						tmpResult2 = tmpResult2 + content2 + "\n";
						txtOutFile2.writefile(tmpResult2);
					}
				}
				// ���
				tmpResult1 = "";
				tmpResult2 = "";
				count = 0;// ����
				continue;
			}
			tmpResult1 = tmpResult1 + content + "\n";
			if (booPairEnd) {
				tmpResult2 = tmpResult2 + content2 + "\n";
			}
			count++;
		}
		FastQ fastQ = null;

		if (booPairEnd) {
			fastQ = new FastQ(fileFilterOut.trim() + "_1", fileFilterOut.trim()
					+ "_2", offset, quality);
		} else {
			fastQ = new FastQ(fileFilterOut.trim(), null, offset, quality);
		}
		txtSeqFile.close();
		txtSeqFile2.close();
		txtOutFile.close();
		txtOutFile2.close();
		return fastQ;
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

		if (seq1.endsWith("BBBBB") || (seq2 != null && seq2.endsWith("BBBBB"))) {
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
		txtSeqFile.setParameter(seqFile, false, true);
		ArrayList<String> lsreads = null;
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
			lsreads = txtSeqFile.readFirstLines(Num * block);
		} catch (Exception e) {
			logger.error(seqFile + " may not exits");
			return null;
		}
		for (int i = QCline; i < lsreads.size(); i = i + block) {
			lsResult.add(lsreads.get(i));
		}
		lsreads.clear();
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
	public static int[][] copeFastQ(int FASTQ_FORMAT_OFFSET, String fastQSeq,
			int... Qvalue) {
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = fastQSeq.toCharArray();
		for (char c : fastq) {
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if ((int) c - FASTQ_FORMAT_OFFSET <= Qvalue[i]) {
					qNum[i][1]++;
					continue;
				} else {
					break;
				}
			}
		}
		return qNum;
	}

	// ////////////////// barcode ɸѡ����
	// ///////////////////////////////////////////////////////////////////////////////////

	/**
	 * �����а���barcode�ָ�ɼ�����ͬ���ļ������ֱ𱣴�Ϊ��ص��ļ���
	 * 
	 * @param outFilePrix
	 * @param barcodeAndName
	 *            һ��barcode����--�Զ�ת��Ϊ��д��һ��barcode���� ����barcodeAndName������һ��ż�����ȵ�����
	 */
	public void sepBarCode(String outFilePrix, String... barcodeAndName) {
		if (barcodeAndName.length % 2 != 0) {
			String out = "";
			for (String string : barcodeAndName) {
				out = out + string + "  ";
			}
			logger.error(outFilePrix + " barcode �������: " + out);
		}
		setHashBarCode(barcodeAndName);
	}

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
			txtBarcod.setParameter(resultFileName[k], true, false);
			hashBarcodeTxt.put(barcodename, txtBarcod);
			k++;
			if (booPairEnd) {
				TxtReadandWrite txtBarcod2 = new TxtReadandWrite();
				resultFileName[k] = filePath+fileName[0]+"_"+barcodename+"2."+fileName[1];
				txtBarcod2.setParameter(resultFileName[k], true, false);
				hashBarcodeTxt2.put(barcodename, txtBarcod2);
				k++;
			}
		}
		TxtReadandWrite txtBarcod = new TxtReadandWrite();
		resultFileName[k] = filePath+fileName[0]+"_"+"notfind."+fileName[1];
		txtBarcod.setParameter(resultFileName[k], true, false);
		hashBarcodeTxt.put("notfind", txtBarcod);
		k++;
		if (booPairEnd) {
			TxtReadandWrite txtBarcod2 = new TxtReadandWrite();
			resultFileName[k] = filePath+fileName[0]+"_"+"notfind2."+fileName[1];
			txtBarcod2.setParameter(resultFileName[k], true, false);
			hashBarcodeTxt2.put("notfind", txtBarcod2);
			k++;
		}

		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader reader1 = txtSeqFile.readfile();
		BufferedReader reader2 = null;
		if (booPairEnd) {
			txtSeqFile2.setParameter(seqFile2, false, true);
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
				else {
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
		txtSeqFile.setParameter(seqFile, false, true);
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
	
	/**
	 * ָ����ֵ����fastQ�ļ�����polyA��Ŀǰֻ����Ե����Ҳ��polyA
	 * @return �����Ѿ����˺õ�FastQ����ʵ����Ҳ���ǻ�������FastQ�ļ�����
	 * @return filterNum ������̶೤������22
	 * @throws Exception
	 */
	public FastQ trimPolyA(int filterNum,String fileFilterOut) throws Exception {
		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader readerSeq = txtSeqFile.readfile();
		
		TxtReadandWrite txtOutFile = new TxtReadandWrite();
		txtOutFile.setParameter(fileFilterOut.trim(), true, false);

		setFastQFormat();

		String content = "";
		int count = 0;int lastID = -10;
		String tmpResult1 = "";
		while ((content = readerSeq.readLine()) != null) {
			if (count == 1) {
				lastID = trimPolyA(content, 1);
				if (lastID >= filterNum) {
					tmpResult1 = tmpResult1 + content.substring(0,lastID) + "\n";
				}
				count++;
				continue;
			}
			if (count == QCline) {
				if (lastID >= filterNum) {
					tmpResult1 = tmpResult1 + content.substring(0,lastID) + "\n";
					txtOutFile.writefile(tmpResult1);
				}
				count = 0;// ����
				tmpResult1 = "";
				continue;
			}
			tmpResult1 = tmpResult1 + content + "\n";
			count++;
		}
		FastQ fastQ = null;
		fastQ = new FastQ(fileFilterOut.trim(), null, offset, quality);
		txtSeqFile.close();
		txtOutFile.close();
		return fastQ;
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
		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader reader = txtSeqFile.readfile();
		
		TxtReadandWrite txtFasta1 = new TxtReadandWrite();
		txtFasta1.setParameter(fastaFile, true, false);
		
		TxtReadandWrite txtFasta2 = new TxtReadandWrite();
		BufferedReader reader2 = null;
		if (booPairEnd) {
			txtSeqFile2.setParameter(seqFile2, false, true);
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

}
