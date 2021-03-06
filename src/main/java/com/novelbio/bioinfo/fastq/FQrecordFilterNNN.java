package com.novelbio.bioinfo.fastq;

import org.apache.log4j.Logger;

/**
 * 过滤fastq两端的低质量序列
 * @author zong0jie
 *
 */
public class FQrecordFilterNNN extends FQrecordFilter {
	private static Logger logger = Logger.getLogger(FQrecordFilterNNN.class);
	
	boolean isFilterNNN = true;
	/** 几个好的序列，就是说NNNCNNN这种，坏的中间夹一个好的 一般为1 */
	int numGoodBp = 1;
	int cutoffQuality = 15;

	public void setTrimNNN(boolean isFilterNNN) {
		this.isFilterNNN = isFilterNNN;
	}
	
	/**
	 * @param isFilterNNN
	 * @param qualityCutoff 碱基质量的最低值，小于该值会被cut
	 */
	public void setTrimNNN(boolean isFilterNNN, int qualityCutoff) {
		this.isFilterNNN = isFilterNNN;
		this.cutoffQuality = qualityCutoff;
	}
	@Override
	public boolean isUsing() {
		return isFilterNNN;
	}
    
	/**
	 * @param numMM 几个好的序列，就是说NNNCNNN这种，坏的中间夹一个好的 一般为1
	 */
	public void setNumGoodBp(int numGoodBp) {
		this.numGoodBp = numGoodBp;
	}
	
	/**
	 * 过滤左端低质量序列，Q10，Q13以下为低质量序列，一路剪切直到全部切光为止
	 * @return
	 * 	 * 返回该NNN的第最后一个碱基在序列上的位置，从1开始记数
	 * 也就是该NNN有多少个碱基，可以直接用substring(return)来截取
	 * 返回-1表示出错
	 */
	protected int trimLeft(FastQRecord fastQRecord) {
		int left = fastQRecord.seqQuality.length();
		char[] chrSeq = fastQRecord.getSeqFasta().toString().toCharArray();
		char[] chrIn = fastQRecord.seqQuality.toCharArray();
		int numMismatch = 0;
		int con = -1;//记录连续的低质量的字符有几个
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - fastqOffset >= cutoffQuality && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
				numMismatch++;
				con++;
			} else {
				con = -1;
			}
			if (numMismatch > numGoodBp) {
				left = i - con;//把最后不是a的还的加回去
				break;
			}
		}
		return left;
	}
	/**
	 * 过滤右端低质量序列，Q10，Q13以下为低质量序列，一路剪切直到全部切光为止
	 * @return
	 * 	 * 返回该NNN的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该NNN前面有多少个碱基，可以直接用substring(0,return)来截取
	 * 返回-1表示出错
	 */
	protected int trimRight(FastQRecord fastQRecord) {
		int right = 0;
		char[] chrSeq = fastQRecord.getSeqFasta().toString().toCharArray();
		char[] chrIn = fastQRecord.seqQuality.toCharArray(); int lenIn =  fastQRecord.seqQuality.length();
		int numMismatch = 0;
		int con = 0;//记录连续的低质量的字符有几个
		for (int i = lenIn - 1; i >= 0; i--) {
			if ((int)chrIn[i] - fastqOffset >= cutoffQuality && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numGoodBp) {
				right = i+con;
				break;
			}
		}
		return right;
	}

}
