package com.novelBio.tools.formatConvert;

import com.novelBio.generalConf.NovelBioConst;

public class FastQ {
	
	static int offset = 0;
	/**
	 * @param fastQFormat ����fastQ��ʽ��������sanger��solexa����
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
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
	}
	
	/**
	 * ����һ��fastQ��ascII�룬ͬʱָ��һϵ�е�Qֵ������asc||С�ڸ�Qֵ��char�ж���
	 * ����Qvalue�����˳�����������Ӧ��int[]
	 * @param fastQ �����fastQ�ַ���
	 * @param Qvalue Qvalue����ֵ������ָ�������һ��ΪQ13����ʱΪQ10�������ά���ٿƵ�FASTQ format
	 * @return
	 * int ����˳��С��ÿ��Qvalue��������ע�⣬����Qvalue ����ѡ����
	 */
	public static int[] copeFastQ(String fastQFormat,int...Qvalue) 
	{
		if (offset == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
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
