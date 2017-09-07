package com.novelbio.analysis.seq.chipseq;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsBSP.CpGCalculator;
import com.novelbio.base.dataStructure.MathComput;

/** 仅用于测试，目前用在 {@link TestGene2Value} 上
 * 里面的值是从1-7000递增
 * @author zong0jie
 * @data 2016年11月20日
 */
public class MapReadsBSPStub extends MapReadsAbs {
	double[] value = new double[7000];
	CpGCalculator cpGCalculator;
	
	public MapReadsBSPStub() {
		for (int i = 0; i < value.length; i++) {
			int mc = i;
			int nonmc = i+20;
			int type = i%4;
			if (type == 0) {
				value[i] = 0;
				continue;
			}
			value[i] = mc*100000 + nonmc*10 + type;
		}
	}

	public void setCpGCalculator(CpGCalculator cpGCalculator) {
		this.cpGCalculator = cpGCalculator;
	}
	
	@Override
	protected void ReadMapFileExp() throws Exception {
	}

	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum) {
		double[] tmpResult = getRangeInfo(1, chrID, startLoc, endLoc, 0);
		return cpGCalculator.calculateCpGInfo(tmpResult, binNum);
	}

	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum, int endNum, int type) {
		startNum--;
		endNum--;
		double[] result = getSub(startNum, endNum);
		return result;
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		startNum--;
		endNum--;
		double[] result = getSub(startNum, endNum);
		if (binNum > 0) {
			return MathComput.mySpline(result, binNum, 0, 0, type);
        }
		return result;
	}

	@Override
	protected long getAllReadsNum() {
		return 0;
	}

	private double[] getSub(int start, int end) {
		double[] valueThis = new double[end - start + 1];
		for (int i = start; i <= end; i++) {
			valueThis[i - start] = value[i];
		}
		return valueThis;
	}

	@Override
	public void clear() {
	}

	@Override
	public void setReadsInfoFile(String mapFile) {
		// TODO Auto-generated method stub
		
	}

}
