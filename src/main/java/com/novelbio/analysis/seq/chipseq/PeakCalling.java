package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

public interface PeakCalling {
	
	/**
	 * û��ʵ�֣���Ҫ���า��
	 * @param bedTreat ʵ��
	 * @param bedCol ����
	 * @param species ���֣�����effective genome size����hs��mm��dm��ce��os
	 * @param outFile Ŀ���ļ��У����ü�"/"
	 * @throws Exception 
	 */
	public void peakCallling(String bedCol,String species, String outFilePath ,String prix);
	
	
}