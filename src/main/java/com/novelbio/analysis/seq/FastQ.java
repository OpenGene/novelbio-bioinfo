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
	
	private TxtReadandWrite txtSeqFile2 = new TxtReadandWrite();

	private int offset = 0;
	private boolean booPairEnd = false;
	// 有时候有两个fastQ文件，这个仅仅在双端测序的时候出现，这时候需要协同过滤
	private String seqFile2 = null;
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * 双端的时候只有两个序列都是好的才保留
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/**
	 * FastQ文件的第四行是序列的质量行，所以为4-1 = 3
	 */
	private int QCline = 3;
	/**
	 * 第一条reads的长度
	 */
	private int readsLen = 0;
	/**
	 * 最短reads的长度，小于该长度的reads就跳过
	 */
	private int readsLenMin = 25;
	
	private int adaptermaxMismach = 2;
	private int adaptermaxConMismatch = 1;
	
	public static void main(String[] args) {
		FastQ.convertSff2FastQ("/media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_miRNA.fasta", true);
		
	}
	
	
	/**
	 * 设定最短reads的长度，小于该长度的reads就跳过，默认为25
	 */
	public void setReadsLenMin(int readsLenMin) {
		this.readsLenMin = readsLenMin;
	}
	/**
	 * 根据具体的序列调整
	 * @param maxMismach 默认是2
	 * @param maxConMismatch 默认是1
	 */
	public void setAdapterParam(int maxMismach, int maxConMismatch) {
		this.adaptermaxConMismatch = maxConMismatch;
		this.adaptermaxMismach = maxMismach;
	}
	/**
	 * 默认中等质量控制
	 */
	private int quality = QUALITY_MIDIAN;
	
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
	////////  参 数 设 定  ////////////////////
	boolean trimPolyA_right = false;
	/**
	 * true的话，没有polyA的序列不要
	 */
	boolean flagPolyA = false;
	/**
	 * true的话，没有polyT的序列不要
	 */
	boolean flagPolyT = false;
	boolean trimPolyT_left = false;
	/**
	 * 接头是小写
	 * 这种情况目前只在ion proton的数据中发现
	 */
	boolean adaptorLowercase = false;
	/**
	 * 是否将序列两边的NNN删除
	 */
	boolean trimNNN = true;
	/**
	 * 是否将序列两边的NNN删除
	 * 默认是删除的，但是感觉速度好慢然后cufflink还有问题
	 */
	public void setTrimNNN(boolean trimNNN)
	{
		this.trimNNN = trimNNN;
	}
	
	/**
	 * 设定了adaptor就不要设定PolyA
	 * @param trimPolyA_right
	 */
	public void setTrimPolyA(boolean trimPolyA_right, boolean flagPlogA) {
		this.trimPolyA_right = trimPolyA_right;
		this.flagPolyA = flagPlogA;
	}
	/**
	 * 设定了adaptor就不要设定PolyA
	 * @param trimPolyA_right
	 */
	public void setTrimPolyT(boolean trimPolyT_left, boolean flagPlogT) {
		this.trimPolyT_left = trimPolyT_left;
		this.flagPolyT = flagPlogT;
	}
	
	
	
	String adaptorLeft = "";
	String adaptorRight = "";
	/**
	 * 注意adapter里面不要有非ATGC的东西
	 * @param adaptor
	 */
	public void setAdaptorLeft(String adaptor) {
		this.adaptorLeft = adaptor.trim();
	}
	/**
	 * 设定了polyA就不要设定adaptor
	 * 注意adapter里面不要有非ATGC的东西
	 * @param adaptor
	 */
	public void setAdaptorRight(String adaptor) {
		this.adaptorRight = adaptor.trim();
	}
	/**
	 * 接头是小写 这种情况目前只在ion proton的数据中发现
	 * 貌似454都这个德性
	 */
	public void setAdaptorLowercase(boolean adaptorLowercase) {
		this.adaptorLowercase = adaptorLowercase;
	}
	//////////////////////////
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
		setFastQFormat();
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
	public boolean isPairEnd() {
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
	 * 输入前先判断文件是否存在,最好能判断两个文件是否是同一个测序的两端 那么可以判断是否为fastQ格式和fasQ格式第一行是否一致
	 * 标准文件名的话，自动判断是否为gz压缩
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
	 * 自动判断 FastQ的格式
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
	 * 自动判断 FastQ的格式
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
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
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
	 * 待测试
	 * 先去adaptoer，然后去polyA(右端)和polyT(左端)，然后去两端NNN，然后去总体低质量
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
					count = 0;// 清零
					continue;
				}
				///////////// polyA ///////////////////////////////////////////////////////
				if (trimPolyA_right) {
					seqBlock1 = trimPolyAR(seqBlock1, 2);
					if (booPairEnd)
						seqBlock2 = trimPolyAR(seqBlock2, 2);
					
					if (seqBlock1 == null || seqBlock2 == null) {
						seqBlock1 = ""; seqBlock2 = "";
						count = 0;// 清零
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
						count = 0;// 清零
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
						count = 0;// 清零
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
						count = 0;// 清零
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
				// 清空
				seqBlock1 = "";
				seqBlock2 = "";
				count = 0;// 清零
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
	 * cutOff选择10即认为10，包括10以下的序列都不好，需要cut掉
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	private String trimLowCase(String fastQBlock)
	{
		String ss = fastQBlock.split(TxtReadandWrite.huiche)[1];//获得的是序列而不是quality信息
		char[] info = ss.toCharArray();
		int numStart = 0;
		//从前向后，遇到小写就计数
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
	 * cutOff选择10即认为10，包括10以下的序列都不好，需要cut掉
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
	 * 过滤右端低质量序列，Q10，Q13以下为低质量序列，一路剪切直到全部切光为止
	 * @param seqIn 质量列
	 * @param cutOff 低质量序列的cutOff, 小于等于他就会被cut
	 * @param numMM 几个好的序列，就是说NNNCNNN这种，坏的中间夹一个好的 一般为1
	 * @return
	 * 	 * 返回该NNN的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该NNN前面有多少个碱基，可以直接用substring(0,return)来截取
	 * 返回-1表示出错
	 */
	private int trimNNNRight(String seqIn,int cutOff, int numMM) {
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//记录连续的低质量的字符有几个
		for (int i = lenIn-1; i >= 0; i--) {
			if ((int)chrIn[i] - offset > cutOff) {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM) {
				return i+con;//把最后不是a的还的加回去
			}
		}
//		logger.info("no useful seq: "+ seqIn);
		return 0;
	}
	/**
	 * 
	 * 过滤左端低质量序列，Q10，Q13以下为低质量序列，一路剪切直到全部切光为止
	 * @param seqIn 质量列
	 * @param cutOff 低质量序列的cutOff, 小于等于他就会被cut
	 * @param numMM 几个好的序列，就是说NNNCNNN这种，坏的中间夹一个好的 一般为1
	 * @return
	 * 	 * 返回该NNN的第最后一个碱基在序列上的位置，从1开始记数
	 * 也就是该NNN有多少个碱基，可以直接用substring(return)来截取
	 * 返回-1表示出错
	 */
	private int trimNNNLeft(String seqIn,int cutOff, int numMM) {
		char[] chrIn = seqIn.toCharArray();
		int numMismatch = 0;
		int con = -1;//记录连续的低质量的字符有几个
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - offset > cutOff) {
				numMismatch++;
				con++;
			}
			else {
				con = -1;
			}
			if (numMismatch > numMM) {
				return i - con;//把最后不是a的还的加回去
			}
		}
//		logger.info("no useful seq: "+ seqIn);
		return seqIn.length();
	}
	
	/**
	 * 每四行一个block，用来处理该block的方法，主要是截短
	 * block必须用TxtReadandWrite.huiche换行
	 * @param block
	 * @param start 和substring一样的用法
	 * @param end 和substring一样的用法
	 * @return 返回截短后的string
	 * 一样还是用TxtReadandWrite.huiche换行，最后没有TxtReadandWrite.huiche
	 * 如果截短后的长度小于设定的最短reads长度，那么就返回null
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
	 * 过滤右侧polyA
	 * @param block
	 * @param mismatch 可以设定的稍微长一点点，因为里面有设定最长连续错配为1了，所以这里建议2-3
	 * @return 返回截短后的string
	 * 一样还是用TxtReadandWrite.huiche换行，最后没有TxtReadandWrite.huiche
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
	 * 过滤左侧polyT
	 * @param block
	 * @param mismatch 可以设定的稍微长一点点，因为里面有设定最长连续错配为1了，所以这里建议2-3
	 * @return 返回截短后的string
	 * 一样还是用TxtReadandWrite.huiche换行，最后没有TxtReadandWrite.huiche
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
	 * 过滤左右两侧的接头
	 * 如果截短后的长度小于设定的最短reads长度，那么就返回null
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

		if (seq1.endsWith("BBBBBBB") || (seq2 != null && seq2.endsWith("BBBBBBB"))) {
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
	public int[][] copeFastQ(int FASTQ_FORMAT_OFFSET, String fastQSeq, int... Qvalue) {
		if (FASTQ_FORMAT_OFFSET == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = fastQSeq.toCharArray();
		//reads长度分布，一般用于454
//		gffHashBin.addNumber(gffreadsLen, fastq.length);
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - FASTQ_FORMAT_OFFSET;
			/////////////////////////序列质量，每个碱基的质量分布统计/////////////////////////////////////////////////
//			gffHashBin.addNumber(m+gffbpName, qualityScore);
			//////////////////////////////////////////////////////////////////////////
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if (qualityScore <= Qvalue[i]) {//注意是小于等于
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
	 * 将mismatich比对指标文件，看是否符合
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



	// ////////////////// barcode 筛选序列
	// ///////////////////////////////////////////////////////////////////////////////////

//	/**
//	 * 将序列按照barcode分割成几个不同的文件，并分别保存为相关的文件名
//	 * 
//	 * @param outFilePrix
//	 * @param barcodeAndName
//	 *            一个barcode序列--自动转换为大写，一个barcode名字 所以barcodeAndName必须是一个偶数长度的数组
//	 */
//	public void sepBarCode(String outFilePrix, String... barcodeAndName) {
//		if (barcodeAndName.length % 2 != 0) {
//			String out = "";
//			for (String string : barcodeAndName) {
//				out = out + string + "  ";
//			}
//			logger.error(outFilePrix + " barcode 输入错误: " + out);
//		}
//		setHashBarCode(barcodeAndName);
//	}

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
		txtSeqFile.setParameter(compressInType, seqFile, false, true);
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
//	 * 指定阈值，将fastQ文件过滤polyA，目前只能针对单端右侧的polyA
//	 * 
//	 * @param filterNum 序列最短多长，建议22
//	 * @return 返回已经过滤好的FastQ，其实里面也就是换了两个FastQ文件而已
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
//				count = 0;// 清零
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
	 * 将fastq文件转化为fasta文件<br>
	 * 产生的文件为单端： fastaFile<br>
	 * 双端： 如果有后缀名: 如fasta.aa<br>
	 * 则为 fasta.aa 和 fasta2.aa<br>
	 * 没有后缀名则为 fasta 和 fasta2<br>
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
	/**
	 * 统计reads分布和每个reads质量的方法
	 */
	ListHashBin gffHashBin = new ListHashBin();
	String gffreadsLen = "Reads Length";
	String gffbpName = "BP";
	/**
	 * 初始化reads分布统计类
	 * @reads最长多少
	 */
	private void initialGffHashBin(int maxReadsLen) {

		ArrayList<String[]> lsInfo = new ArrayList<String[]>();
		//reads 长度
		for (int i = 1; i <= maxReadsLen; i++) {
			lsInfo.add(new String[]{gffreadsLen, i+ "", i + ""});
		}
		//每个碱基的质量
		for (int i = 1; i <= maxReadsLen; i++) {
			for (int j = 1; j < 60; j++) {
				lsInfo.add(new String[]{i+gffbpName, j+ "", j + ""});
			}
		}
		gffHashBin.ReadGff(lsInfo);
	}
	/**
	 * 给定文件名，获得 文件名.fasta 和 文件名.
	 * @param fileName
	 * @param illuminaOffset 是否为illumina的offset
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
		//计数器，因为每两行为一个单元
		int num = 0;
		String title = ""; String fasta = ""; String quality = "";
		for (String contentFasta : txtReadFasta.readlines()) {
			String contentQuality = txtQuality.next();
			//标题行
			if (num == 0) {
				if (!contentFasta.equals(contentQuality)) {
					logger.error("sff转换出错拉，看看fasta和quality是不是来自同一个文件");
				}
				title = "@" + contentFasta.substring(1);
				num++;
			}
			//具体内容
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
	 * 给定一系列offset，将数字转化为fastq的quality行
	 * @param illumina 是否是illumina的offset 
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
