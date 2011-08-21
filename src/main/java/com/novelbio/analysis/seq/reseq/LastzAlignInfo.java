package com.novelbio.analysis.seq.reseq;

public class LastzAlignInfo {

	/**
	 * alignment的分数
	 */
	int score = 0; 
	/**
	 * 第一条链的名字
	 */
	String seqName1 = "";
	/**
	 * 第二条链的名字
	 */
	String seqName2 = "";
	/**
	 * align在第一条链上的长度
	 */
	int alignLen1 = 0;
	/**
	 * align在第二条链上的长度
	 */
	int alignLen2 = 0;
	/**
	 * 第二条链相对于第一条链的方向
	 */
	boolean strand = true;
	/**
	 * 第一条链的起点
	 */
	int start1 = 0;
	/**
	 * 第一条链的终点
	 */
	int end1 = 0;
	/**
	 * 第二条链的起点
	 */
	int start2 = 0;
	/**
	 * 第二条链的终点
	 */
	int end2 = 0;
	/**
	 * 第一条链的长度
	 */
	int seqLen1 = 0;
	/**
	 * 
	 */
	int seqLen2 = 0;
}
