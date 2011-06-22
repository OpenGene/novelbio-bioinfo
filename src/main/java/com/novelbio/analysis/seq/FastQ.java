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
 * FastQ的各个指标<br>
 * Q10: 0.1 <br>
 * Q13: 0.05 <br>
 * Q20: 0.01 <br>
 * Q30: 0.001 <br>
 * 2010年 Illumina HiSeq2000测序仪，双端50bp Q30>90% 双端100bp Q30>85%
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
	//有时候有两个fastQ文件，这个仅仅在双端测序的时候出现，这时候需要协同过滤
	String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	public static int QUALITY_HIGM = 30;
	/**
	 * FastQ文件的第四行是序列的质量行，所以为4-1 = 3
	 */
	int QCline = 3;
	
	/**
	 * 默认中等质量控制
	 */
	int quality = 20;
	
	/**
	 * fastQ里面asc||码的指标与个数
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();
	/**
	 * 返回第二个FastQ文件的文件名
	 * 如果没有则返回null
	 * @return
	 */
	public String getSeqFile2() {
		return seqFile2;
	}
	/**
	 * 返回FastQ的格式位移，一般是
	 * FASTQ_SANGER_OFFSET
	 * 或
	 * FASTQ_ILLUMINA_OFFSET
	 * @return
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * 返回文件设定的过滤质量
	 * @return
	 */
	public int getQuality()
	{
		return quality;
	}
	/**
	 * 返回是否是双端测序的FastQ文件，其实也就是看是否有两个FastQ文件
	 * @return
	 */
	public boolean getBooPairEnd() {
		return booPairEnd;
	}
	/**
	 * 输入前先判断文件是否存在,最好能判断两个文件是否是同一个测序的两端
	 * 那么可以判断是否为fastQ格式和fasQ格式第一行是否一致
	 * @param seqFile1 序列文件
	 * @param seqFile2 双端测序会有两个文件，没有就填null，会检查该文件是否存在
	 * @param fastQFormat 哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种
	 * 不知道就写0，程序会从文件中判断
	 * @param QUALITY QUALITY_LOW等
	 * 
	 */
	public FastQ(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, 4);//fastQ一般4行为一个序列
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
	 * 自动判断 FastQ的格式
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQ (String seqFile1,String seqFile2,int QUALITY) {
		super(seqFile1, 4);//fastQ一般4行为一个序列
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
	 * 自动判断 FastQ的格式
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQ (String seqFile1,int QUALITY) {
		super(seqFile1, 4);//fastQ一般4行为一个序列
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
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param Qvalue_Num 二维数组 每一行代表一个Qvalue 以及最多出现的个数
	 * int[0][0] = 13  int[0][1] = 7 :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut 结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 * 分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
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
				//清空
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
	 * 给定双端测序的两条序列，看这两条序列的质量是否符合要求
	 * 有高中低三档选择
	 * @param seq1 双端测序的第一端
	 * @param seq2 双端测序的第二端，没有则为null或""
	 * @return
	 */
	private boolean QC(String seq1,String seq2) {
		boolean booQC1 = false; 
		boolean booQC2 = false;
		/**
		 * 就看Q10，Q13和Q20就行了
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
	 * 如果FastQ格式没有设定好，通过该方法设定FastQ格式
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
	 * 提取FastQ文件中的质控序列，提取个5000行就差不多了
	 * 因为fastQ文件中质量都在第三行，所以只提取第三行的信息
	 * @param Num 提取多少行，指最后提取的行数
	 * @return fastQ质控序列的list
	 * 出错返回null
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
	 * 给定一系列的fastQ格式，猜测该fastQ是属于sanger还是solexa
	 * @param lsFastQ :每一个string 就是一个fastQ
	 * @return FASTQ_ILLUMINA或者FASTQ_SANGER
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
		//如果前两个都没搞定，后面还能判定
		if (desStat.getMin() < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (desStat.getMax() > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		System.out.println("FastQ can not gess the fastQ format");
		//都没判断出来，猜测为illumina格式
		return FASTQ_ILLUMINA_OFFSET;
	}
	/**
	 * 给定一行fastQ的ascII码，同时指定一系列的Q值，返回asc||小于该Q值的char有多少
	 * 按照Qvalue输入的顺序，输出就是相应的int[]
	 * @param FASTQ_FORMAT_OFFSET offset是多少，FASTQ_SANGER_OFFSET和
	 * @param fastQSeq 具体的fastQ字符串
	 * @param Qvalue Qvalue的阈值，可以指定多个<b>必须从小到大排列</b>，一般为Q13，有时为Q10，具体见维基百科的FASTQ format
	 * @return
	 * int 按照顺序，小于等于每个Qvalue的数量
	 */
	public static int[][] copeFastQ(int FASTQ_FORMAT_OFFSET,String fastQSeq,int...Qvalue) 
	{
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
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
