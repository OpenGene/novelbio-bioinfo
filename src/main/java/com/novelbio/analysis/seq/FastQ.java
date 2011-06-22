package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @author zong0jie
 *
 */
public class FastQ extends Seq{

	static int FASTQ_SANGER_OFFSET = 33;
	static int FASTQ_ILLUMINA_OFFSET = 64;

	private static Logger logger = Logger.getLogger(FastQ.class);  

	TxtReadandWrite txtSeqFile2 = new TxtReadandWrite();
	
	int offset = 0;
	boolean booPairEnd = false;
	//��ʱ��������fastQ�ļ������������˫�˲����ʱ����֣���ʱ����ҪЭͬ����
	String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	public static int QUALITY_HIGM = 30;
	/**
	 * FastQ�ļ��ĵ����������е������У�����Ϊ4-1 = 3
	 */
	int QCline = 3;
	
	/**
	 * Ĭ���е���������
	 */
	int quality = 20;
	
	/**
	 * fastQ����asc||���ָ�������
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();
	/**
	 * ���صڶ���FastQ�ļ����ļ���
	 * ���û���򷵻�null
	 * @return
	 */
	public String getSeqFile2() {
		return seqFile2;
	}
	/**
	 * ����FastQ�ĸ�ʽλ�ƣ�һ����
	 * FASTQ_SANGER_OFFSET
	 * ��
	 * FASTQ_ILLUMINA_OFFSET
	 * @return
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * �����ļ��趨�Ĺ�������
	 * @return
	 */
	public int getQuality()
	{
		return quality;
	}
	/**
	 * �����Ƿ���˫�˲����FastQ�ļ�����ʵҲ���ǿ��Ƿ�������FastQ�ļ�
	 * @return
	 */
	public boolean getBooPairEnd() {
		return booPairEnd;
	}
	/**
	 * ����ǰ���ж��ļ��Ƿ����,������ж������ļ��Ƿ���ͬһ�����������
	 * ��ô�����ж��Ƿ�ΪfastQ��ʽ��fasQ��ʽ��һ���Ƿ�һ��
	 * @param seqFile1 �����ļ�
	 * @param seqFile2 ˫�˲�����������ļ���û�о���null��������ļ��Ƿ����
	 * @param fastQFormat ����fastQ��ʽ��������FASTQ_SANGER_OFFSET��FASTQ_ILLUMINA_OFFSET����
	 * ��֪����д0���������ļ����ж�
	 * @param QUALITY QUALITY_LOW��
	 * 
	 */
	public FastQ(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, 4);//fastQһ��4��Ϊһ������
		if (FileOperate.isFileExist(seqFile2.trim()) ) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
		}
		if (FastQFormateOffset == FASTQ_SANGER_OFFSET) {
			offset = 33;
		}
		else if (FastQFormateOffset == FASTQ_ILLUMINA_OFFSET) {
			offset = 64;
		}
		else {
			offset = 0;
		}
		
		if (QUALITY == QUALITY_HIGM) {
			quality = QUALITY;
			hashFastQFilter.put(10, 0);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		}
		else if (QUALITY == QUALITY_LOW) {
			quality = QUALITY;
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 8);
			hashFastQFilter.put(20, 15);
		}
		else if (QUALITY == QUALITY_MIDIAN) {
			quality = QUALITY;
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQ (String seqFile1,String seqFile2,int QUALITY) {
		super(seqFile1, 4);//fastQһ��4��Ϊһ������
		if (FileOperate.isFileExist(seqFile2.trim()) ) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
		}
		offset = 0;
		if (QUALITY == QUALITY_HIGM) {
			quality = QUALITY;
			hashFastQFilter.put(10, 0);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		}
		else if (QUALITY == QUALITY_LOW) {
			quality = QUALITY;
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 8);
			hashFastQFilter.put(20, 15);
		}
		else if (QUALITY == QUALITY_MIDIAN) {
			quality = QUALITY;
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	/**
	 * �Զ��ж� FastQ�ĸ�ʽ
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQ (String seqFile1,int QUALITY) {
		super(seqFile1, 4);//fastQһ��4��Ϊһ������
		offset = 0;
		if (QUALITY == QUALITY_HIGM) {
			quality = QUALITY;
			hashFastQFilter.put(10, 0);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		}
		else if (QUALITY == QUALITY_LOW) {
			quality = QUALITY;
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 8);
			hashFastQFilter.put(20, 15);
		}
		else if (QUALITY == QUALITY_MIDIAN) {
			quality = QUALITY;
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	
	

	
	/**
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param Qvalue_Num ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ���
	 * int[0][0] = 13  int[0][1] = 7 :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 * �ֱ�ΪfileFilterOut_1��fileFilterOut_2
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
		}
		else {
			txtOutFile.setParameter(fileFilterOut.trim()+"_1", true, false);
		}
		TxtReadandWrite txtOutFile2 = null;
		if (booPairEnd) {
			txtSeqFile2.setParameter(seqFile2, false, true);
			readerSeq2 = txtSeqFile2.readfile();
			txtOutFile2 = new TxtReadandWrite();
			txtOutFile2.setParameter(fileFilterOut.trim()+"_2", false, true);
		}
		setFastQFormat();
		
		String content = ""; String content2 = null; int count = 0;
		String tmpResult1 = ""; String tmpResult2 = "";
		while ((content = readerSeq.readLine()) != null) {
			if (booPairEnd) {
				content2 = readerSeq2.readLine().trim();
			}
			if (count == QCline) {
				if (QC(content, content2)) {
					tmpResult1 = tmpResult1+content+"\n";
					txtOutFile.writefile(tmpResult1);
					if (booPairEnd) {
						tmpResult2 = tmpResult2 +content2+"\n";
						txtOutFile2.writefile(tmpResult2);
					}
				}
				//���
				tmpResult1= "";tmpResult2 = "";
			}
			tmpResult1 = tmpResult1 + content + "\n";
			if (booPairEnd) {
				tmpResult2 = tmpResult2 + content2 + "\n";
			}
			count++;
		}
		FastQ fastQ = null;
		
		if (booPairEnd) {
			fastQ = new FastQ(fileFilterOut.trim()+"_1", fileFilterOut.trim()+"_2", offset, quality);
		}
		else {
			fastQ = new FastQ(fileFilterOut.trim(), null, offset, quality);
		}
		txtSeqFile.close();
		txtSeqFile2.close();
		txtOutFile.close();
		txtOutFile2.close();
		return fastQ;
	}
	
	/**
	 * ����˫�˲�����������У������������е������Ƿ����Ҫ��
	 * �и��е�����ѡ��
	 * @param seq1 ˫�˲���ĵ�һ��
	 * @param seq2 ˫�˲���ĵڶ��ˣ�û����Ϊnull��""
	 * @return
	 */
	private boolean QC(String seq1,String seq2) {
		boolean booQC1 = false; 
		boolean booQC2 = false;
		/**
		 * �Ϳ�Q10��Q13��Q20������
		 */
		int[][] seqQC1 = copeFastQ(offset, seq1, 10,13,20);
		booQC1 = filterFastQ(seqQC1);
		int[][] seqQC2 = null;
		if (seq2 != null && !seq2.trim().equals("")) {
			seqQC2 = copeFastQ(offset, seq2, 10,13,20);
			booQC2 = filterFastQ(seqQC2);
		}
	
		if (quality == QUALITY_HIGM) {
			if (seq2 ==null || seq2.trim().equals("") ) {
				return booQC1;
			}
			else {
				return booQC1&&booQC2;
			}
		}
		else if (quality == QUALITY_MIDIAN || quality == QUALITY_LOW) {
			return booQC1||booQC2;
		}
		return true;
	}

	private boolean filterFastQ(int[][] thisFastQ)
	{
		for (int[] is : thisFastQ) {
			Integer Num = hashFastQFilter.get(is[0]);
			if (Num == null) {
				continue;
			}
			else if (Num < is[1]) {
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
	 * ��ȡFastQ�ļ��е��ʿ����У���ȡ��5000�оͲ����
	 * ��ΪfastQ�ļ����������ڵ����У�����ֻ��ȡ�����е���Ϣ
	 * @param Num ��ȡ�����У�ָ�����ȡ������
	 * @return fastQ�ʿ����е�list
	 * ������null
	 */
	private ArrayList<String> getLsFastQSeq(int Num) {
		txtSeqFile.setParameter(seqFile, false, true);
		ArrayList<String> lsreads  = null;
		ArrayList<String> lsResult = null;
		try {
			lsreads = txtSeqFile.readFirstLines(Num*block);
		} catch (Exception e) {
			logger.error(seqFile +" may not exits");
			return null;
		}
		for (int i = QCline; i < lsreads.size(); i=i+block) {
			lsResult.add(lsreads.get(i));
		}
		lsreads.clear();
		return lsResult;
	}

	
	/**
	 * ����һϵ�е�fastQ��ʽ���²��fastQ������sanger����solexa
	 * @param lsFastQ :ÿһ��string ����һ��fastQ
	 * @return FASTQ_ILLUMINA����FASTQ_SANGER
	 */
	public static int guessFastOFormat(List<String> lsFastQ) {
		double min25 = 70; double max75 = 70;
		DescriptiveStatistics desStat = new DescriptiveStatistics();
		for (String string : lsFastQ)
		{
			if (string.trim().equals("")) {
				continue;
			}
			char[] fastq = string.toCharArray();
			for (int i = 0; i < fastq.length; i++) {
				desStat.addValue((double)fastq[i]);
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
		//���ǰ������û�㶨�����滹���ж�
		if (desStat.getMin() < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (desStat.getMax() > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		System.out.println("FastQ can not gess the fastQ format");
		//��û�жϳ������²�Ϊillumina��ʽ
		return FASTQ_ILLUMINA_OFFSET;
	}
	/**
	 * ����һ��fastQ��ascII�룬ͬʱָ��һϵ�е�Qֵ������asc||С�ڸ�Qֵ��char�ж���
	 * ����Qvalue�����˳�����������Ӧ��int[]
	 * @param FASTQ_FORMAT_OFFSET offset�Ƕ��٣�FASTQ_SANGER_OFFSET��
	 * @param fastQSeq �����fastQ�ַ���
	 * @param Qvalue Qvalue����ֵ������ָ�����<b>�����С��������</b>��һ��ΪQ13����ʱΪQ10�������ά���ٿƵ�FASTQ format
	 * @return
	 * int ����˳��С�ڵ���ÿ��Qvalue������
	 */
	public static int[][] copeFastQ(int FASTQ_FORMAT_OFFSET,String fastQSeq,int...Qvalue) 
	{
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = fastQSeq.toCharArray();
		for (char c : fastq) {
			for (int i = Qvalue.length -1; i >= 0; i++) {
				if ((int)c - FASTQ_FORMAT_OFFSET <= Qvalue[i]) {
					qNum[i][1] ++;
					continue;
				}
				else {
					break;
				}
			}
		}
		return qNum;
	}

}
