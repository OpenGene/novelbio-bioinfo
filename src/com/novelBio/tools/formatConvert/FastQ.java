package com.novelBio.tools.formatConvert;

import com.novelBio.generalConf.NovelBioConst;

public class FastQ {
	
	static int offset = 0;
	/**
	 * @param fastQFormat 哪种fastQ格式，现在有sanger，solexa两种
	 */
	public static void setFastQoffset(String fastQFormat)
	{
		if (fastQFormat.equals(NovelBioConst.FASTQ_SANGER)) {
			offset = 33;
		}
		else if (fastQFormat.equals(NovelBioConst.FASTQ_ILLUMINA)) {
			offset = 64;
		}
		else {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
	}
	
	/**
	 * 给定一行fastQ的ascII码，同时指定一系列的Q值，返回asc||小于该Q值的char有多少
	 * 按照Qvalue输入的顺序，输出就是相应的int[]
	 * @param fastQ 具体的fastQ字符串
	 * @param Qvalue Qvalue的阈值，可以指定多个，一般为Q13，有时为Q10，具体见维基百科的FASTQ format
	 * @return
	 * int 按照顺序，小于每个Qvalue的数量，注意，等于Qvalue 不挑选出来
	 */
	public static int[] copeFastQ(String fastQFormat,int...Qvalue) 
	{
		if (offset == 0) {
			System.out.println("FastQ.copeFastQ ,没有指定offset");
		}
		int[] qNum = new int[Qvalue.length];
		char[] fastq = fastQFormat.toCharArray();
		for (char c : fastq) {
			for (int i = 0; i < Qvalue.length; i++) {
				if ((int)c - offset < Qvalue[i]) {
					qNum[i] ++;
				}
			}
		}
		return qNum;
	}
}
