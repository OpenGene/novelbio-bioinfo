package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listoperate.BinarySearch;
import com.novelbio.listoperate.BsearchSiteDu;

public class CoordTransformer {
	
	Map<String, List<CoordPair>> mapChrId2LsCoorPairs;
	
	/** 坐标转换 */
	public VarInfo coordTransform(Align alignRef) {
		List<CoordPair> lsCoordPairs = mapChrId2LsCoorPairs.get(alignRef.getRefID());
		if (ArrayOperate.isEmpty(lsCoordPairs)) {
			return null;
		}
		return coordTransform(lsCoordPairs, alignRef);
	}
	
	@VisibleForTesting
	protected static VarInfo coordTransform(List<CoordPair> lsCoordPairs, Align alignRef) {
		BinarySearch<CoordPair> binarySearch = new BinarySearch<>(lsCoordPairs);
		BsearchSiteDu<CoordPair> bsearchSiteDu = binarySearch.searchLocationDu(alignRef.getStartAbs(), alignRef.getEndAbs());
		List<CoordPair> lsCoordPairsOverlap = bsearchSiteDu.getAllElement();
		if (lsCoordPairsOverlap.isEmpty()) {
			return null;
		}
		if (lsCoordPairsOverlap.size() > 1) {
			//暂时不支持跨越多个区段
			return null;
		}
		
		CoordPair coordPair = lsCoordPairs.get(0);
		//暂时不支持
		int biasStart = 0, biasEnd = 0;
		if (alignRef.getStartAbs() < coordPair.getStartAbs()) {
			biasStart = coordPair.getStartAbs() - alignRef.getStartAbs();
			alignRef.setStartAbs(coordPair.getStartAbs());
		}
		if (alignRef.getEndAbs() > coordPair.getEndAbs()) {
			biasEnd = alignRef.getEndAbs() - coordPair.getEndAbs();
			alignRef.setEndAbs(coordPair.getEndAbs());
		}
		
		int start = alignRef.getStartAbs(), end = alignRef.getEndAbs();
		VarInfo varInfo = coordPair.searchVarInfo(start, end);
		if (varInfo == null) {
			return varInfo;
		}
		varInfo.setStartBias(varInfo.getStartBias()+biasStart);
		varInfo.setEndBias(varInfo.getEndBias()+biasEnd);
		return varInfo;
	}
	
}
