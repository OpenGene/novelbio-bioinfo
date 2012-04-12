package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashBin;
import com.novelbio.base.dataStructure.listOperate.ElementAbsDouble;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.base.dataStructure.listOperate.ListAbsDouble;

public class FreqStatisticInt{
	GffHashBin gffHashBin = new GffHashBin();
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
		gffDetailPeak.addNumber();
	}
	/**
	 * ���������Լ�ÿ�����������
	 * key��int������
	 * value����������
	 * @return
	 */
	public LinkedHashMap<int[], Integer> getFreq()
	{
		LinkedHashMap<int[], Integer> hashResult = new LinkedHashMap<int[], Integer>();
		ListAbs<GffDetailPeak> lsPeak = gffHashBin.getListDetail(name);
		for (GffDetailPeak gffDetailPeak : lsPeak) {
			int[] interval = new int[2];
			interval[0] = gffDetailPeak.getStartAbs();
			interval[1]= gffDetailPeak.getEndAbs();
			hashResult.put(interval, gffDetailPeak.getNumber());
		}
		return hashResult;
	}
}

