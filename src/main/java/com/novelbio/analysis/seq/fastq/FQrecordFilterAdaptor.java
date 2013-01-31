package com.novelbio.analysis.seq.fastq;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFasta;

public class FQrecordFilterAdaptor extends FQrecordFilter {
	Logger logger = Logger.getLogger(FQrecordFilterAdaptor.class); 
	String seqAdaptorL;
	String seqAdaptorR;
	int mapNumLeft = -1;
	int mapNumRight = -1;
	int numMM = 2;
	int conNum = 1;
	int perMm = 30;
	
	public void setSeqAdaptorL(String seqAdaptorL) {
		if (seqAdaptorL != null) {
			seqAdaptorL = seqAdaptorL.trim();
		}
		this.seqAdaptorL = seqAdaptorL;
	}
	public void setSeqAdaptorR(String seqAdaptorR) {
		if (seqAdaptorR != null) {
			seqAdaptorR = seqAdaptorR.trim();
		}
		this.seqAdaptorR = seqAdaptorR;
	}
	public void setMapNumLeft(int mapNumLeft) {
		this.mapNumLeft = mapNumLeft;
	}
	public void setMapNumRight(int mapNumRight) {
		this.mapNumRight = mapNumRight;
	}
	@Override
	public boolean isUsing() {
		if ((seqAdaptorL != null && seqAdaptorL.length() > 1) 
				|| (seqAdaptorR != null && seqAdaptorR.length() > 1)) {
			return true;
		}
		return false;
	}
	/**
	 * 设定接头的错配信息
	 * @param numAllMismatch 最多容错几个mismatch 2个比较好
	 */
	public void setNumMM(int numAllMismatch) {
		this.numMM = numAllMismatch;
	}
	/**
	 * 设定接头的错配信息
	 * @param continueMismatch 最多容错连续几个mismatch，1个比较好
	 */
	public void setConNum(int continueMismatch) {
		this.conNum = continueMismatch;
	}
	/**
	 * 设定接头的错配信息
	 * @param perMismatchNum 最多容错百分比 设定为30吧，这个是怕adaptor太短
	 */
	public void setPerMm(int perMismatchNum) {
		this.perMm = perMismatchNum;
	}
	
	@Override
	protected int trimLeft() {
		SeqFasta seqFasta = fastQRecord.getSeqFasta();
		int leftNum = 0;
		if (seqAdaptorL == null || seqAdaptorL.equals("")) {
			return leftNum;
		}
		if (mapNumLeft >= 0) {
			leftNum = trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqFasta.Length() - mapNumLeft, numMM,conNum, perMm);
		} else {
			leftNum = trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqAdaptorL.length(), numMM,conNum, perMm);
		}
		return leftNum;
	}

	@Override
	protected int trimRight() {
		SeqFasta seqFasta = fastQRecord.getSeqFasta();
		int rightNum = seqFasta.Length();
		if (seqAdaptorR == null || seqAdaptorR.equals("")) {
			return rightNum;
		}
		if (mapNumRight >= 0) {
			rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, mapNumRight, numMM,conNum, perMm);
		} else {
			rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, seqFasta.Length() - seqAdaptorR.length(), numMM,conNum, perMm);
		}
		if (rightNum > seqFasta.Length()) {
			logger.error("stop");
			if (mapNumRight >= 0) {
				rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, mapNumRight, numMM,conNum, perMm);
			} else {
				rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, seqFasta.Length() - seqAdaptorR.length(), numMM,conNum, perMm);
			}
		}
		return rightNum;
	}
	
	/**
	 * 注意两个以下的adaptor无法过滤
	 * 过滤右侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设右侧最多只有一整个接头。那么先将接头直接对到右侧对齐，然后循环的将接头对到reads上去。
	 * @param seqIn 输入序列 无所谓大小写
	 * @param seqAdaptor 接头 无所谓大小写 接头可以只写一部分
	 * @param mapNumRight 第一次接头左端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到 建议设定为：seqIn.length() +1- seqAdaptor.length()
	 * @param numMM 最多容错几个mismatch 2个比较好
	 * @param conNum 最多容错连续几个mismatch，1个比较好
	 * @param perMm 最多容错百分比 设定为30吧，这个是怕adaptor太短
	 * @return 返回该tag的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(0,return)来截取
	 * -1说明没有adaptor
	 */
	public static int trimAdaptorR(String seqIn, String seqAdaptor, int mapNumRight, int numMM, int conNum, float perMm) {
		if (seqAdaptor.equals("")) {
			return seqIn.length();
		}
		
		mapNumRight--;
		if (mapNumRight < 0) {
			mapNumRight =0;
		}
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
		int con = 0;//记录连续的非匹配的字符有几个
//		从左到右搜索chrIn
		for (int i = mapNumRight; i < lenIn; i++) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = 0; j < lenA; j++) {
				if (i+j >= lenIn)
					break;
				if (chrIn[i+j] == chrAdaptor[j] || chrIn[i+j] == 'N') {
					pm++;
					con = 0;
				} else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			if (isMatch(pm, mm, seqAdaptor.length(), numMM, perMm)) {
				return i+1;
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
	public static int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int conNum, int numMM, float perMm) {
		if (seqAdaptor == null || seqAdaptor.equals("")) {
			return 0;
		}

		mapNum--;
		if (mapNum >= seqIn.length() || mapNum < 0) {
			mapNum = seqIn.length() - 1;
		}
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
				if (chrAdaptor[j] == 'N' || chrIn[i+j-lenA+1] == chrAdaptor[j] || chrIn[i+j-lenA+1] == 'N') {
					pm++; con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			if (isMatch(pm, mm, seqAdaptor.length(), numMM, perMm)) {
				return i+1;
			}
		}
		int num = blastSeq(true, seqIn, seqAdaptor, numMM, perMm);
		if (num > -1) {
			return num;
		}
		return 0;
	}
	/** 判定是否通过质检 */
	private static boolean isMatch(int pm, int mm, int seqAdaptorLen,int maxMMnum, float perMm) {
		if (pm >= ((double)seqAdaptorLen * (1 - perMm/100)) 
				&&  mm <= maxMMnum && ((float)mm/seqAdaptorLen) <= perMm/100 ) 
		{
			return true;
		}
		return false;
	}
	/** 用blast的方法来找接头 */
	private static int blastSeq(boolean leftAdaptor, String seqSeq, String seqAdaptor, int numMM, float perMm) {
		BlastSeqFasta blastSeqFasta = new BlastSeqFasta(seqSeq, seqAdaptor);
		blastSeqFasta.setSpaceScore(-2);
		blastSeqFasta.blast();
		blastSeqFasta.blast();
		if ((double)blastSeqFasta.getMatchNum()/seqAdaptor.length() < (1-((double)perMm/100)) || blastSeqFasta.getGapNumQuery() + blastSeqFasta.getGapNumSubject() > numMM
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

}
