package com.novelbio.analysis.tools.formatConvert;

import java.util.List;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.util.MathUtils;

import com.novelbio.generalConf.NovelBioConst;

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
	 * ����һϵ�е�fastQ��ʽ���²��fastQ������sanger����solexa
	 * @param lsFastQ :ÿһ��string ����һ��fastQ
	 */
	public static String guessFastOFormat(List<String> lsFastQ) {
		double min25 = 70; double max75 = 70;
		DescriptiveStatistics desStat = new DescriptiveStatistics();
		for (String string : lsFastQ)
		{
			if (string.trim().equals("")) {
				continue;
			}
			char[] fastq = string.toCharArray();
			for (int i = 0; i < fastq.length; i++) {
				desStat.addValue((double)fastq[i]);
			}
		}
		min25 = desStat.getPercentile(5);
		max75 = desStat.getPercentile(90);
		if (min25 < 59) {
			setFastQoffset(NovelBioConst.FASTQ_SANGER);
			return NovelBioConst.FASTQ_SANGER;
		}
		if (max75 > 95) {
			setFastQoffset(NovelBioConst.FASTQ_ILLUMINA);
			return NovelBioConst.FASTQ_ILLUMINA;
		}
		//���ǰ������û�㶨�����滹���ж�
		if (desStat.getMin() < 59) {
			setFastQoffset(NovelBioConst.FASTQ_SANGER);
			return NovelBioConst.FASTQ_SANGER;
		}
		if (desStat.getMax() > 103) {
			setFastQoffset(NovelBioConst.FASTQ_ILLUMINA);
			return NovelBioConst.FASTQ_ILLUMINA;
		}
		return NovelBioConst.FASTQ_ILLUMINA;
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
