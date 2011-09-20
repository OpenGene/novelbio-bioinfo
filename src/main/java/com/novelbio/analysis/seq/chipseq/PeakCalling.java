package com.novelbio.analysis.seq.chipseq;

public interface PeakCalling {
	
	/**
	 * 没有实现，需要子类覆盖
	 * @param bedTreat 实验
	 * @param bedCol 对照
	 * @param species 物种，用于effective genome size，有hs，mm，dm，ce，os
	 * @param outFile 目标文件夹，不用加"/"
	 * @throws Exception 
	 */
	public void peakCallling( String bedTreat,String bedCol,String species, String outFilePath ,String prix);
}
