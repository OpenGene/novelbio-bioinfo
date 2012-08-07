package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FastQRecord implements Cloneable {
	private static Logger logger = Logger.getLogger(FastQRecord.class);

	public static int FASTQ_SANGER_OFFSET = 33;
	public static int FASTQ_ILLUMINA_OFFSET = 64;
	// ///////////////// fastq的质量
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * 双端的时候只有两个序列都是好的才保留
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/**
	 * fastQ里面asc||码的指标与个数
	 */
	static HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();
	
	private SeqFasta seqFasta = new SeqFasta();
	protected int fastqOffset = FASTQ_SANGER_OFFSET;
	protected String seqQuality = "";
	/** 序列质量控制，低于该质量就说明本记录有问题 */
	protected int quality = QUALITY_MIDIAN;
	/** 裁剪序列时最短为多少 */
	private int readsMinLen = 22;
	
	private static int errorTrimAdapterReadsNum = 0;

	/**
	 * 将mismatich比对指标文件，看是否符合
	 * @param thisFastQ
	 * @return
	 */
	private static boolean filterFastQ(int[][] thisFastQ) {
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
	 * 设定全局过滤指标
	 * 
	 * @param QUALITY
	 */
	public static void setHashFastQFilter(int QUALITY) {
		if (QUALITY == QUALITY_HIGM) {
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		} else if (QUALITY == QUALITY_LOW) {
			// hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 10);
			hashFastQFilter.put(20, 20);
		} else if (QUALITY == QUALITY_MIDIAN
				|| QUALITY == QUALITY_MIDIAN_PAIREND) {
			// hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		} else if (QUALITY == QUALITY_LOW_454) {
			// hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 6);
			hashFastQFilter.put(13, 15);
			hashFastQFilter.put(20, 50);
		} else {
			// hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	public static void setErrorTrimAdapterReadsNum(int errorTrimAdapterReadsNum) {
		FastQRecord.errorTrimAdapterReadsNum = errorTrimAdapterReadsNum;
	}
	public static int getErrorTrimAdapterReadsNum() {
		return errorTrimAdapterReadsNum;
	}
	
	public FastQRecord() {
		seqFasta = new SeqFasta();
		seqFasta.setTOLOWCASE(null);
	}
	public void setName(String SeqName) {
		seqFasta.setName(SeqName);
	}
	public void setSeq(String Seq) {
		seqFasta.setSeq(Seq);
	}
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}
	/**
	 * 每四行一个记录，将这四行用linux回车隔开，然后输入
	 * @param fastqlines
	 */
	public FastQRecord(String fastqlines) {
		String[] ss = fastqlines.split(TxtReadandWrite.ENTER_LINUX);
		if (ss.length == 1) {
			ss = fastqlines.split(TxtReadandWrite.ENTER_WINDOWS);
		}
		seqFasta.setName(ss[0].substring(1));
		seqFasta.setSeq(ss[1]);
		setFastaQuality(ss[3]);
	}
	
	/** 裁剪序列时最短为多少， 默认为22
	 */
	public void setTrimMinLen(int trimMinLen) {
		this.readsMinLen = trimMinLen;
	}
	/**
	 * 设定序列质量，用phred格式设定
	 * @param fastaQuality
	 */
	public void setFastaQuality(String fastaQuality) {
		this.seqQuality = fastaQuality;
	}
	public String getSeqQuality() {
		return seqQuality;
	}
	public int getLength() {
		return seqFasta.getLength();
	}
	/**
	 * 设定偏移
	 * FASTQ_SANGER_OFFSET
	 * @param fastqOffset
	 */
	public void setFastqOffset(int fastqOffset) {
		this.fastqOffset = fastqOffset;
	}
	//////////////////////// 过滤低质量的序列 ///////////////////////////////////////////
	/**
	 * 注意两个以下的adaptor无法过滤
	 * 过滤右侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设右侧最多只有一整个接头。那么先将接头直接对到右侧对齐，然后循环的将接头对到reads上去。
	 * @param seqAdaptorL 左端接头 无所谓大小写 接头可以只写一部分 null或""表示不用过滤该接头
	 * @param seqAdaptorR 右端接头 无所谓大小写 接头可以只写一部分 null或""表示不用过滤该接头
	 * @param mapNumLeft 第一次接头左端或右端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到 建议设定为：seqIn.length() +1- seqAdaptor.length()
	 * 如果mapNum<0, 则自动设定为seqIn.length() +1- seqAdaptor.length()等形式
	 * @param mapNumRight 同mapNumLeft，针对右端接头
	 * @param numMM 最多容错几个mismatch 2个比较好
	 * @param conNum 最多容错连续几个mismatch，1个比较好
	 * @param perMm 最多容错百分比 设定为30吧，这个是怕adaptor太短
	 * @return 返回该tag的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(0,return)来截取
	 * -1说明没有adaptor
	 */
	public FastQRecord trimAdaptor(String seqAdaptorL, String seqAdaptorR, int mapNumLeft, int mapNumRight, int numMM, int conNum, int perMm) {
		if ((seqAdaptorL == null || seqAdaptorL.equals("")) && (seqAdaptorR == null || seqAdaptorR.equals(""))) {
			return this;
		}
		int leftNum = 0, rightNum = seqFasta.getLength();
		if (seqAdaptorL != null && !seqAdaptorL.equals("")) {
			if (mapNumLeft >= 0)
				leftNum = 	trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqFasta.getLength() - mapNumLeft, numMM,conNum, perMm);
			else
				leftNum = 	trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqAdaptorL.length(), numMM,conNum, perMm);
		}
		
		if (seqAdaptorR != null && !seqAdaptorR.equals("")) {
			if (mapNumRight >= 0)
				rightNum = 	trimAdaptorR(seqFasta.toString(), seqAdaptorR, mapNumRight, numMM,conNum, perMm);
			else//TODO 确定这里设定多少合适：SeqSequence.length() - seqAdaptorL.length()
				rightNum = 	trimAdaptorR(seqFasta.toString(), seqAdaptorR, seqFasta.getLength() - seqAdaptorR.length(), numMM,conNum, perMm);
		}
		return trimSeq(leftNum, rightNum);
	}
	/**
	 * cutOff选择10即认为10，包括10以下的序列都不好，需要cut掉
	 * @param numMM 几个好的序列，就是说NNNCNNN这种，坏的中间夹一个好的 一般为1
	 * @return
	 */
	public FastQRecord trimNNN( int numMM) {
		int numStart = trimNNNLeft(seqQuality, 10, numMM);
		int numEnd = trimNNNRight(seqQuality, 10, numMM);
		return trimSeq(numStart, numEnd);
	}
	/**
	 * cutOff选择10即认为10，包括10以下的序列都不好，需要cut掉
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	public FastQRecord trimLowCase() {
		char[] info = seqFasta.toString().toCharArray();
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
		return trimSeq(numStart, numEnd);
	}
	/**
	 * 过滤右侧polyA
	 * @param block
	 * @param mismatch 可以设定的稍微长一点点，因为里面有设定最长连续错配为1了，所以这里建议2-3
	 * @return 返回截短后的string
	 * 一样还是用TxtReadandWrite.huiche换行，最后没有TxtReadandWrite.huiche
	 */
	public FastQRecord trimPolyAR( int mismatch) {
		int num = 	trimPolyA(seqFasta.toString(), mismatch,1);
		return trimSeq(0, num);
	}
	/**
	 * 过滤左侧polyT
	 * @param block
	 * @param mismatch 可以设定的稍微长一点点，因为里面有设定最长连续错配为1了，所以这里建议2-3
	 * @return 返回截短后的string
	 * 一样还是用TxtReadandWrite.huiche换行，最后没有TxtReadandWrite.huiche
	 */
	public FastQRecord trimPolyTL( int mismatch) {
		int num = trimPolyT(seqFasta.toString(), mismatch,1);
		return trimSeq(num, seqFasta.getLength());
	}
	/**
	 * 注意两个以下的adaptor无法过滤
	 * 过滤右侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设右侧最多只有一整个接头。那么先将接头直接对到右侧对齐，然后循环的将接头对到reads上去。
	 * @param seqIn 输入序列 无所谓大小写
	 * @param seqAdaptor 接头 无所谓大小写 接头可以只写一部分
	 * @param mapNum 第一次接头左端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到 建议设定为：seqIn.length() +1- seqAdaptor.length()
	 * @param numMM 最多容错几个mismatch 2个比较好
	 * @param conNum 最多容错连续几个mismatch，1个比较好
	 * @param perMm 最多容错百分比 设定为30吧，这个是怕adaptor太短
	 * @return 返回该tag的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(0,return)来截取
	 * -1说明没有adaptor
	 */
	private int trimAdaptorR(String seqIn, String seqAdaptor, int mapNum, int numMM, int conNum, float perMm) {
		if (seqAdaptor.equals("")) {
			return seqIn.length();
		}
		mapNum--;
		if (mapNum < 0) {
			mapNum =0;
		}
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
		int con = 0;//记录连续的非匹配的字符有几个
//		从左到右搜索chrIn
		for (int i = mapNum; i < lenIn; i++) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = 0; j < lenA; j++) {
				if (i+j >= lenIn)
					break;
				if (chrIn[i+j] == chrAdaptor[j] || chrIn[i+j] == 'N') {
					pm++;
					con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			int lenAdaptor = pm + mm;
			if (mm <= numMM && ((float)mm/lenAdaptor) <= perMm && lenAdaptor > 4) {
				return i;
			}
		}
		int num = blastSeq(false, seqIn, seqAdaptor, numMM, (int) perMm);
		if (num > -1) {
			return num;
		}
		return seqIn.length();
	}

	/**
	 * 注意两个以下的adaptor无法过滤
	 * 过滤左侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设左侧最多只有一整个接头。那么先将接头直接对到左侧对齐，然后循环的将接头对到reads上去。
	 * @param seqIn 输入序列 无所谓大小写
	 * @param seqAdaptor 接头 无所谓大小写
	 * @param mapNum 第一次接头右端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到 建议设定为：adaptorLeft.length()
	 * @param numMM 最多容错几个mismatch 1个比较好
	 * @param conNum 最多容错连续几个mismatch，1个比较好
	 * @param perMm 最多容错百分比,100进制，设定为30吧，这个是怕adaptor太短
	 * @return 返回该tag的最一个碱基在序列上的位置，从1开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(return)来截取
	 * -1说明没有adaptor
	 */
	private int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int conNum, int numMM, float perMm) {
		if (seqAdaptor.equals("")) {
			return 0;
		}
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); //int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
		int con = 0;//记录连续的非匹配的字符有几个
//		从右到左搜索chrIn
		for (int i = mapNum; i >= 0 ; i--) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = chrAdaptor.length-1; j >= 0; j--) {
				if (i+j-lenA+1 < 0)
					break;
				if (chrIn[i+j-lenA+1] == chrAdaptor[j] || chrIn[i+j-lenA+1] == 'N') {
					pm++; con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			int lenAdaptor = pm + mm;
			if (mm <= numMM && ((float)(mm/lenAdaptor)) <= perMm/100 && lenAdaptor > 4) {
				return i+1;
			}
		}
		int num = blastSeq(true, seqIn, seqAdaptor, numMM, perMm);
		if (num > -1) {
			return num;
		}
		return 0;
	}
	
	private int blastSeq(boolean leftAdaptor, String seqSeq, String seqAdaptor, int numMM, float perMm) {
		BlastSeqFasta blastSeqFasta = new BlastSeqFasta(seqSeq, seqAdaptor);
		blastSeqFasta.setSpaceScore(-2);
		blastSeqFasta.blast();
		if (blastSeqFasta.getGapNumQuery() + blastSeqFasta.getGapNumSubject() > numMM
			|| blastSeqFasta.getMisMathchNum() > numMM 
			|| (float)(blastSeqFasta.getGapNumQuery() + blastSeqFasta.getGapNumSubject() + blastSeqFasta.getMisMathchNum())/seqAdaptor.length() > perMm/100
				) 
		{
			return -1;
		}
		if (leftAdaptor) {
			return blastSeqFasta.getEndQuery();
		}
		else {
			return blastSeqFasta.getStartQuery();
		}
	}
	/**
	 * 过滤右侧polyA，当为AAANNNAAANANAA时，无视N继续过滤
	 * @param seqIn
	 * @param numMM 几个错配 一般为1
	 * @param maxConteniunNoneA 最长连续错配
	 * @return
	 * 返回该Seq的第一个A在序列上的位置，从0开始记数
	 * 如果没有A，返回值 == Seq.length()
	 * 也就是该polyA前面有多少个碱基，可以直接用substring(0,return)来截取
	 */
	private int trimPolyA(String seqIn, int numMM, int maxConteniunNoneA) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//记录连续的非A的字符有几个
		for (int i = lenIn-1; i >= 0; i--) {
			if (chrIn[i] != 'A' && chrIn[i] != 'N') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM || con > maxConteniunNoneA) {
				return i+con;//把最后不是a的还的加回去
			}
		}
		return 0;
	}
	/**
	 * 过滤左侧polyT，当为TTTNNNTTTNTNTT时，无视N继续过滤
	 * @param seqIn
	 * @param numMM 几个错配 一般为1
	 * @param maxConteniunNoneA 最长连续错配
	 * @return
	 * 返回该tag的最后一个碱基在序列上的位置，从1开始记数
	 * 也就是该polyT有多少个碱基，可以直接用substring(return)来截取
	 */
	private int trimPolyT(String seqIn, int numMM, int maxConteniunNoneT) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//记录连续的非A的字符有几个
		for (int i = 0; i < lenIn; i++) {
			if (chrIn[i] != 'T' && chrIn[i] != 'N') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM || con > maxConteniunNoneT) {
				return i-con+1;//把最后不是a的还的加回去
			}
		}
		return lenIn;
	}
	/**
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
		char[] chrSeq = this.seqFasta.toString().toCharArray();
		char[] chrIn = seqIn.toCharArray();
		int numMismatch = 0;
		int con = -1;//记录连续的低质量的字符有几个
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - fastqOffset > cutOff && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
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
		return seqIn.length();
	}
	/**
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
		char[] chrSeq = this.seqFasta.toString().toCharArray();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//记录连续的低质量的字符有几个
		for (int i = lenIn-1; i >= 0; i--) {
			if ((int)chrIn[i] - fastqOffset > cutOff && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM) {
				return i+con;
			}
		}
		return 0;
	}
	/**
	 * 给定左右的坐标，然后将seqfasta截短
	 * @param start 和substring一样的用法
	 * @param end 和substring一样的用法
	 * @return 返回截短后的string
	 * 如果截短后的长度小于设定的最短reads长度，那么就返回null
	 */
	private FastQRecord trimSeq(int start, int end) {
		if (end - start < readsMinLen) {
			return null;
		}
		FastQRecord result = new FastQRecord();
		if (start == 0 && end == seqQuality.length()) {
			return clone();
		}
		result.seqFasta = seqFasta.trimSeq(start, end);
		result.fastqOffset= fastqOffset;
		result.readsMinLen = readsMinLen;
		result.seqQuality = seqQuality.substring(start, end);
		return result;
	}
	/**
	 * 返回fastq格式的文本
	 * @return
	 */
	public String toString() {
		if (seqQuality.length() != seqFasta.getLength()) {
			char[] quality = new char[seqFasta.getLength()];
			if (fastqOffset == FASTQ_ILLUMINA_OFFSET) {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'f';
				}
			}
			else {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'A';
				}
			}
			seqQuality = String.copyValueOf(quality);
		}
		return "@" + seqFasta.getSeqName() + TxtReadandWrite.ENTER_LINUX + seqFasta.toString() + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + seqQuality;
	}
	/**
	 * 克隆序列
	 */
	public FastQRecord clone() {
		FastQRecord seqFasta = null;
		try {
			seqFasta = (FastQRecord) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		seqFasta.seqQuality = seqQuality;
		seqFasta.fastqOffset = fastqOffset;
		seqFasta.readsMinLen = readsMinLen;
		return seqFasta;
	}

	/////////////////////////////// 序列质量控制，仅对fastq文件 //////////////////////////////
	/**
	 * 最开始要设定高中低档
	 * 看本序列的质量是否符合要求 首先会判定质量是否以BBBBB结尾，是的话直接跳过 
	 * @return
	 */
	public boolean QC() {
		if (seqFasta.toString().length() < readsMinLen) {
			return false;
		}
		if (this.fastqOffset == FASTQ_ILLUMINA_OFFSET && seqQuality.endsWith("BBBBBBB") ) {
			return false;
		}
		/** 就看Q10，Q13和Q20就行了 */
		int[][] seqQC1 = copeFastQ(2, 10, 13, 20);
		return filterFastQ(seqQC1);
	}
	
	/**
	 * 给定一行fastQ的ascII码，同时指定一系列的Q值，返回asc||小于该Q值的char有多少
	 * 按照Qvalue输入的顺序，输出就是相应的int[]
	 * @param Qvalue Qvalue的阈值，可以指定多个<b>必须从小到大排列</b>，一般为Q13，有时为Q10，具体见维基百科的FASTQ format
	 * @return int 按照顺序，小于等于每个Qvalue的数量
	 */
	private int[][] copeFastQ(int... Qvalue) {
		if (fastqOffset == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = seqQuality.toCharArray();
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - fastqOffset;
			/////////////////////////序列质量，每个碱基的质量分布统计/////////////////////////////////////////////////
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
}
