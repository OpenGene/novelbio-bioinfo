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
public class FastQ extends SeqComb {
	private static Logger logger = Logger.getLogger(FastQ.class);
	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;

	TxtReadandWrite txtSeqFile2 = new TxtReadandWrite();

	int offset = 0;
	boolean booPairEnd = false;
	// 有时候有两个fastQ文件，这个仅仅在双端测序的时候出现，这时候需要协同过滤
	String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * 双端的时候只有两个序列都是好的才保留
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	/**
	 * FastQ文件的第四行是序列的质量行，所以为4-1 = 3
	 */
	int QCline = 3;
	/**
	 * 第一条reads的长度
	 */
	int readsLen = 0;
	/**
	 * 默认中等质量控制
	 */
	int quality = 20;

	/**
	 * fastQ里面asc||码的指标与个数
	 */
	HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();

	// ///////////////////////// barcode 所需的变量
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 记录barcode的信息 key: barcode 的序列 value: barcode所对应的名字
	 */
	HashMap<String, String> hashBarcodeName = new HashMap<String, String>();
	/**
	 * 顺序记录barcode，这个是如果错配的话，可以在该list里面查找
	 */
	ArrayList<String> lsBarCode = new ArrayList<String>();
	/**
	 * 记录barcode的长度
	 */
	TreeSet<Integer> treeLenBarcode = new TreeSet<Integer>();

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 返回第二个FastQ文件的文件名 如果没有则返回null
	 * 
	 * @return
	 */
	public String getSeqFile2() {
		return seqFile2;
	}

	/**
	 * 返回FastQ的格式位移，一般是 FASTQ_SANGER_OFFSET 或 FASTQ_ILLUMINA_OFFSET
	 * 
	 * @return
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * 返回文件设定的过滤质量
	 * 
	 * @return
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * 返回是否是双端测序的FastQ文件，其实也就是看是否有两个FastQ文件
	 * 
	 * @return
	 */
	public boolean getBooPairEnd() {
		return booPairEnd;
	}

	/**
	 * 获得第一条reads的长度，返回负数说明出错
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
	 * 输入前先判断文件是否存在,最好能判断两个文件是否是同一个测序的两端 那么可以判断是否为fastQ格式和fasQ格式第一行是否一致
	 * 
	 * @param seqFile1
	 *            序列文件
	 * @param seqFile2
	 *            双端测序会有两个文件，没有就填null，会检查该文件是否存在
	 * @param fastQFormat
	 *            哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种
	 *            不知道就写0，程序会从文件中判断
	 * @param QUALITY
	 *            QUALITY_LOW等
	 * 
	 */
	public FastQ(String seqFile1, String seqFile2, int FastQFormateOffset,
			int QUALITY) {
		super(seqFile1, 4);// fastQ一般4行为一个序列
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
	 * 自动判断 FastQ的格式
	 * 
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, String seqFile2, int QUALITY) {
		super(seqFile1, 4);// fastQ一般4行为一个序列
		if (FileOperate.isFileExist(seqFile2.trim())) {
			booPairEnd = true;
			this.seqFile2 = seqFile2;
		}
		offset = 0;
		setHashFastQFilter(QUALITY);

	}

	/**
	 * 自动判断 FastQ的格式
	 * 
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQ(String seqFile1, int QUALITY) {
		super(seqFile1, 4);// fastQ一般4行为一个序列
		offset = 0;
		setHashFastQFilter(QUALITY);
	}

	/**
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * 
	 * @param Qvalue_Num
	 *            二维数组 每一行代表一个Qvalue 以及最多出现的个数 int[0][0] = 13 int[0][1] = 7
	 *            :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut
	 *            结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 *            分别为fileFilterOut_1和fileFilterOut_2
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
				// 清空
				tmpResult1 = "";
				tmpResult2 = "";
				count = 0;// 清零
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
	 * 给定双端测序的两条序列，看这两条序列的质量是否符合要求 首先会判定质量是否以BBBBB结尾，是的话直接跳过 有高中低三档选择
	 * 
	 * @param seq1
	 *            双端测序的第一端
	 * @param seq2
	 *            双端测序的第二端，没有则为null或""
	 * @return
	 */
	private boolean QC(String seq1, String seq2) {
		boolean booQC1 = false;
		boolean booQC2 = false;

		if (seq1.endsWith("BBBBB") || (seq2 != null && seq2.endsWith("BBBBB"))) {
			return false;
		}

		/**
		 * 就看Q10，Q13和Q20就行了
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
	 * 提取FastQ文件中的质控序列，提取个5000行就差不多了 因为fastQ文件中质量都在第三行，所以只提取第三行的信息
	 * 
	 * @param Num
	 *            提取多少行，指最后提取的行数
	 * @return fastQ质控序列的list 出错返回null
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
	 * 给定一系列的fastQ格式，猜测该fastQ是属于sanger还是solexa
	 * 
	 * @param lsFastQ
	 *            :每一个string 就是一个fastQ
	 * @return FASTQ_ILLUMINA或者FASTQ_SANGER
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
		// 如果前两个都没搞定，后面还能判定
		if (desStat.getMin() < 59) {
			return FASTQ_SANGER_OFFSET;
		}
		if (desStat.getMax() > 103) {
			return FASTQ_ILLUMINA_OFFSET;
		}
		logger.error(seqFile
				+ " has a problem, FastQ can not gess the fastQ format, set the format as FASTQ_ILLUMINA_OFFSET");
		// 都没判断出来，猜测为illumina格式
		return FASTQ_ILLUMINA_OFFSET;
	}

	/**
	 * 给定一行fastQ的ascII码，同时指定一系列的Q值，返回asc||小于该Q值的char有多少
	 * 按照Qvalue输入的顺序，输出就是相应的int[]
	 * 
	 * @param FASTQ_FORMAT_OFFSET
	 *            offset是多少，FASTQ_SANGER_OFFSET和
	 * @param fastQSeq
	 *            具体的fastQ字符串
	 * @param Qvalue
	 *            Qvalue的阈值，可以指定多个<b>必须从小到大排列</b>，一般为Q13，有时为Q10，具体见维基百科的FASTQ
	 *            format
	 * @return int 按照顺序，小于等于每个Qvalue的数量
	 */
	public static int[][] copeFastQ(int FASTQ_FORMAT_OFFSET, String fastQSeq,
			int... Qvalue) {
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
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

	// ////////////////// barcode 筛选序列
	// ///////////////////////////////////////////////////////////////////////////////////

	/**
	 * 将序列按照barcode分割成几个不同的文件，并分别保存为相关的文件名
	 * 
	 * @param outFilePrix
	 * @param barcodeAndName
	 *            一个barcode序列--自动转换为大写，一个barcode名字 所以barcodeAndName必须是一个偶数长度的数组
	 */
	public void sepBarCode(String outFilePrix, String... barcodeAndName) {
		if (barcodeAndName.length % 2 != 0) {
			String out = "";
			for (String string : barcodeAndName) {
				out = out + string + "  ";
			}
			logger.error(outFilePrix + " barcode 输入错误: " + out);
		}
		setHashBarCode(barcodeAndName);
	}

	/**
	 * 指定输出的文件全名，自动将路径最后的文件名设置为前缀 如输入OutFilePathPrix为 : /usr/local/bar.txt 文件名为:
	 * /usr/local/bar_barcodename.txt 双端则另一个为： /usr/local/bar_barcodename2.txt
	 * 
	 * @param OutPrix
	 *            输出文件全名
	 * @param maxmismatch
	 *            barcode 最大错配
	 * @param barcodeAndName
	 *            一个barcode序列--自动转换为大写，一个barcode名字
	 * @throws Exception
	 * @return barcode文件名,如果双端则
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
		//生成相应的结果文件txt类
		int k = 0;//resultFileName的数组
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
		int count = 0; //计数，第几行
		TxtReadandWrite txtTmp1 = null; TxtReadandWrite txtTmp2 = null;//最后用这个来指向hashBarcodeTxt中的对象，然后结果就往里面写
		String tmpresult1 = ""; String tmpresult2 = "";//写入的东西
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
		
			if (count == 1) {//序列行
				//假设两个文本的barcode一样，选出barcodename
				barInfo = getBarCodeInfo(content1, maxmismatch);
				if (barInfo == null) {//如果第一个文本没有barcode，那么就找第二个。如果没有第二个文本，就跳出
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
	 * 输入一个一维数组，将其装入HashBarCode表 数组格式为 0：barcode 序列 1：barcode对应的名称 2：barcode 序列
	 * 3：barcode对应的名称
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
		// ////////////// 如果barcode不等长的话，将barcode的长度列入一个list
		// /////////////////////////
		for (String barcode : lsBarCode) {
			treeLenBarcode.add(barcode.length());
		}
	}

	/**
	 * 给定barcode序列，两个barcode不适合用mismatch，获得barcodeName和具体序列
	 * 
	 * @param barcodeSeq
	 * @param maxmismatch
	 *            允许barcode最大错配数
	 * @return string[2]: 0 barcode name 1: barcode seq
	 *         如果找到了bacode，那么返回该barcode所对应的name，没找到则返回null
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
	 * 给定一段barcode序列，返回barcode名字
	 * 
	 * @param barcodeseq
	 * @param maxmismatch
	 *            最多错配
	 * 
	 * @return
	 */
	private String testBarCodeName(String barcodeseq, int maxmismatch) {
		barcodeseq = barcodeseq.toUpperCase();
		String result = hashBarcodeName.get(barcodeseq);
		if (result != null || maxmismatch < 1) {
			return result;
		}
		// 存储mismatch的前两个碱基，如果前两个一样，那么就说明无法通过barcode识别，那么返回null
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
				if (mismatch > maxmismatch) // barcode两个错配就可以跳过了
				{
					break;
				}
			}
			/**
			 * 如果只有一个错配，显然就是该barcode了
			 */
			if (mismatch <= 1 && barcodeseq.length() > 2) {
				return hashBarcodeName.get(barcode);
			}
			/**
			 * 找最少错配的barcode并返回barcodeName
			 */
			if (mismatch <= tmpMismatch[0]) {
				tmpMismatch[0] = mismatch;
				tmpMismatch[1] = tmpMismatch[0];
				tmpbarcodeID = m;
			}
		}
		// 最少的两个错配都一样，无法区分到底是哪个barcode，跳过
		if (tmpMismatch[0] == tmpMismatch[1]) {
			return null;
		}
		/**
		 * 看最少错配的barcode是什么
		 */
		if (tmpMismatch[0] < maxmismatch) {
			return hashBarcodeName.get(lsBarCode.get(tmpbarcodeID));
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 专门给吴宗福设计的代码，将一个fastQ文件分割为两个
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
		int flag = 1;// 标记是1端还是2端
		boolean flagWrite = false;// 读取两个端点后才写入文本
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
	 * 指定阈值，将fastQ文件过滤polyA，目前只能针对单端右侧的polyA
	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
	 * @return filterNum 序列最短多长，建议22
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
				count = 0;// 清零
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
	 * 将fastq文件转化为fasta文件<br>
	 * 产生的文件为单端： fastaFile<br>
	 * 双端： 如果有后缀名: 如fasta.aa<br>
	 * 则为 fasta.aa 和 fasta2.aa<br>
	 * 没有后缀名则为 fasta 和 fasta2<br>
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
		int count = 0; // 第二行是序列
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
