package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.base.dataStructure.listOperate.ElementAbsDouble;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListAbsDouble;

public class FreqStatisticInt{
	ListHashBin gffHashBin = new ListHashBin();
	String name = "Freq";

	/**
	 * 
	 * @param name
	 * @param location
	 */
	public void addNumber(int location)
	{
		GffCodPeak gffCodPeak = gffHashBin.searchLocatioClone(name, location);
		if (!gffCodPeak.isInsideLoc()) {
			return;
		}
		GffDetailPeak gffDetailPeak = gffCodPeak.getGffDetailThis();
		gffDetailPeak.addReadsInElementNum();
	}
	/**
	 * 返回区间以及每个区间的数量
	 * key：int的区间
	 * value：具体数量
	 * @return
	 */
	public LinkedHashMap<int[], Integer> getFreq()
	{
		LinkedHashMap<int[], Integer> hashResult = new LinkedHashMap<int[], Integer>();
		ListAbsSearch<GffDetailPeak> lsPeak = gffHashBin.getListDetail(name);
		for (GffDetailPeak gffDetailPeak : lsPeak) {
			int[] interval = new int[2];
			interval[0] = gffDetailPeak.getStartAbs();
			interval[1]= gffDetailPeak.getEndAbs();
			hashResult.put(interval, gffDetailPeak.getReadsInElementNum());
		}
		return hashResult;
	}
}

