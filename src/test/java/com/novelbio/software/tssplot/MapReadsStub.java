package com.novelbio.software.tssplot;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.bioinfo.mappedreads.MapReadsAbs;

/** 仅用于测试，目前用在 {@link TestGene2Value} 上
 * 里面的值是从1-7000递增
 * @author zong0jie
 * @data 2016年11月20日
 */
public class MapReadsStub extends MapReadsAbs {
	double[] value = new double[7000];

	public MapReadsStub() {
		for (int i = 0; i < value.length; i++) {
			value[i] = i+1;
		}
	}

	@Override
	protected void ReadMapFileExp() throws Exception {
	}

	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum) {
		startLoc--;
		endLoc--;
		double[] tmpReadsNum = getSub(startLoc, endLoc);
		if (tmpReadsNum == null) {
			return null;
		}
		double[] resultTagDensityNum = MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
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
