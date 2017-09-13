package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.List;

public class DepthPercentage {
	/** 总共有多少位点 */
	long allSite;
	/**
	 * 覆盖度为n时，有多少位点，其中下标为覆盖度，值为位点数
	 * 譬如 覆盖度为2的位点有3万个，则 {@link #lsCoverageNum}.get(2)[0] == 30000
	 * 
	 * 其中{@link #lsCoverageNum}.get(0)表示覆盖度为0的位点有多少个
	 */
	List<int[]> lsCoverageNum = new ArrayList<>();
	
	public void addCoverageSite(CpGInfo cpGInfo) {
		int coverage = cpGInfo.getCoverage();
		if (lsCoverageNum.size() <= coverage) {
			for (int i = lsCoverageNum.size(); i <= coverage; i++) {
				int[] num = new int[1];
				lsCoverageNum.add(num);
			}
		}
		int[] num = lsCoverageNum.get(coverage);
		num[0]++;
		allSite++;
	}
	
	/**
	 * 获得覆盖度信息，是一个double型数组
	 * 其中
	 *  0: 累计0位覆盖度，1表示100%的位点都有至少0条reads覆盖
	 *  ...
	 * 10: 累计10位覆盖度, 0.3 表示30%的位点都有至少10条reads覆盖
	 * @return
	 */
	public double[] getCoverageInfo() {
		double[] coverages = new double[lsCoverageNum.size()];
		int sum = 0;
		for (int i = lsCoverageNum.size() - 1; i >= 0; i--) {
			sum += lsCoverageNum.get(i)[0];
			coverages[i] = (double)sum/allSite;
		}
		return coverages;
	}
	
}
