package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.binarysearch.BinarySearch;
import com.novelbio.bioinfo.base.binarysearch.BsearchSite;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;

public class CoordPairSearch extends CoordPairSearchAbs {
	
	Map<String, List<CoordPair>> mapChrId2LsCoorPairs;
	
	public CoordPairSearch(Map<String, List<CoordPair>> mapChrId2LsCoorPairs) {
		this.mapChrId2LsCoorPairs = mapChrId2LsCoorPairs;
	}
	
	@Override
	public List<CoordPair> findCoordPairsOverlap(Align alignRef) {
		List<CoordPair> lsCoordPairs = mapChrId2LsCoorPairs.get(alignRef.getChrId());
		if (lsCoordPairs == null) {
			return new ArrayList<>();
		}
		BinarySearch<CoordPair> binarySearch = new BinarySearch<>(lsCoordPairs);
		BsearchSiteDu<CoordPair> bsearchSiteDu = binarySearch.searchLocationDu(alignRef.getStartAbs(), alignRef.getEndAbs());
		List<CoordPair> lsCoordPairsOverlap = bsearchSiteDu.getAllElement();
		return lsCoordPairsOverlap;
	}
	
	//======== 查找indelRef =================
	
	protected boolean isCoordLsIndelEmpty(CoordPair coordPair) {
		return ArrayOperate.isEmpty(coordPair.lsIndel);
	}
	
	protected IndelForRef getIndelForRef(CoordPair coordPair, int start) {
		BinarySearch<IndelForRef> binarySearch = new BinarySearch<>(coordPair.lsIndel, true);
		return getIndelForRef(binarySearch.searchLocation(start));
	}
	
	protected IndelRefPair getIndelForRef(CoordPair coordPair, int start, int end) {
		IndelRefPair indelRefPair = new IndelRefPair();
		BinarySearch<IndelForRef> binarySearch = new BinarySearch<>(coordPair.lsIndel, true);
		BsearchSiteDu<IndelForRef> bsiteDu = binarySearch.searchLocationDu(start, end);
		if (bsiteDu == null) {
			return indelRefPair;
		}
		IndelForRef left = getIndelForRef(bsiteDu.getSiteLeft());
		IndelForRef right = getIndelForRef(bsiteDu.getSiteRight());
		indelRefPair.setLeft(left);
		indelRefPair.setRight(right);
		indelRefPair.setLsCovered(bsiteDu.getCoveredElement());
		return indelRefPair;
	}
	
	private IndelForRef getIndelForRef(BsearchSite<IndelForRef> bsearchSite) {
		if (bsearchSite == null) return null;
		
		return bsearchSite.isInsideLoc() ? bsearchSite.getAlignThis() : bsearchSite.getAlignUp();
	}

}
