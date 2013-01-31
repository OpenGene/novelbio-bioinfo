package com.novelbio.analysis.seq.fastq;

public class FQrecordFilterPloyAT extends FQrecordFilter {
	
	int mismatch = 2;
	int maxConteniunNone = 1;
	boolean filterA = false;
	boolean filterT = false;
	/**
	 * 错配，表示polyA中不是A的数字<br>
	 * 可以设定的稍微长一点点，因为里面有设定最长连续错配为1了，所以这里建议2-3
	 * @param mismatch
	 */
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}
	public void setFilterA(boolean filterA) {
		this.filterA = filterA;
	}
	public void setFilterT(boolean filterT) {
		this.filterT = filterT;
	}
	@Override
	public boolean isUsing() {
		return filterA || filterT;
	}
	/**
	 * @param maxConteniunNoneA maxConteniunNoneA 最长连续错配
	 * 譬如 AAACTAAAA这时候连续错配了2个，默认容许1个
	 */
	public void setMaxConteniunNone(int maxConteniunNone) {
		this.maxConteniunNone = maxConteniunNone;
	}
	@Override
	protected int trimLeft() {
		if (filterT) {
			return trimPolyT(fastQRecord.getSeqFasta().toString(), mismatch, maxConteniunNone);
		} else {
			return 0;
		}
	}

	@Override
	protected int trimRight() {
		if (filterA) {
			return trimPolyA(fastQRecord.getSeqFasta().toString(), mismatch, maxConteniunNone);
		} else {
			return fastQRecord.getLength();
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
				return i - con + 1;//把最后不是a的还的加回去
			}
		}
		return lenIn;
	}

}
