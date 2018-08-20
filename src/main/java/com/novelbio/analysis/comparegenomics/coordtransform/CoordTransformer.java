package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.analysis.seq.snphgvs.SnpInfo.EnumHgvsVarType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listoperate.BinarySearch;
import com.novelbio.listoperate.BsearchSiteDu;

public class CoordTransformer {
	
	Map<String, List<CoordPair>> mapChrId2LsCoorPairs;
	
	SeqHashInt seqHashAlt;
	
	public static void main(String[] args) {
		SnpInfo snpInfo = new SnpInfo("chr1", 1234, "A", "AAT");
		System.out.println(snpInfo.getAlign());
	}
	
	void setMapChrId2LsCoorPairs(Map<String, List<CoordPair>> mapChrId2LsCoorPairs) {
		this.mapChrId2LsCoorPairs = mapChrId2LsCoorPairs;
	}
	void setSeqHashAlt(SeqHashInt seqHashAlt) {
		this.seqHashAlt = seqHashAlt;
	}
	
	public SnpInfo coordTransform(SnpInfo snpInfo) {
		Align alignRef = new Align(snpInfo.getAlign());
		VarInfo varInfo = coordTransform(alignRef);
		if (varInfo == null) {
			return null;
		}
		return transformSnpInfo(snpInfo, varInfo, seqHashAlt);
	}
	
	@VisibleForTesting
	protected static SnpInfo transformSnpInfo(SnpInfo snpInfo, VarInfo varInfo, SeqHashInt seqHashAlt) {
		String ref = snpInfo.getSeqRef();
		String alt = snpInfo.getSeqAlt();

		String refAlt, altAlt;
		SnpInfo snpInfoAlt;
		if (snpInfo.getVarType() == EnumHgvsVarType.Substitutions) {
			refAlt = seqHashAlt.getSeqCis(varInfo).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			snpInfoAlt = new SnpInfo(varInfo.getRefID(), varInfo.getStartAbs(), refAlt, altAlt);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Insertions) {
			int start = varInfo.isCis() ? varInfo.getStartAbs() : varInfo.getStartAbs()-1;
			String snpHead = seqHashAlt.getSeq(varInfo.getRefID(), start, start).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			snpInfoAlt = new SnpInfo(varInfo.getRefID(), start, snpHead, snpHead+altAlt);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
			int start = varInfo.getStartAbs();
			String snpHead = seqHashAlt.getSeq(varInfo.getRefID(), start-1, start-1).toString();
			altAlt = seqHashAlt.getSeqCis(varInfo).toString();
			snpInfoAlt = new SnpInfo(varInfo.getRefID(),start-1, snpHead + altAlt, snpHead);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Indels) {
			refAlt = seqHashAlt.getSeqCis(varInfo).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			if (varInfo.getStartBias() > 0 || varInfo.getEndBias() > 0) {
				return null;
			}
			snpInfoAlt = new SnpInfo(varInfo.getRefID(), varInfo.getStartAbs(), refAlt, altAlt);
		} else {
			throw new ExceptionNBCCoordTransformer("unsupported type " + snpInfo.getVarType() + " " + snpInfo.toString());
		}
		return snpInfoAlt;
	}
	
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
		
		CoordPair coordPair = lsCoordPairsOverlap.get(0);
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
	
	/** 输出为liftover的chain格式 */
	public void writeToChain(String chainFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(chainFile, true);
		for (List<CoordPair> lsCoordPair : mapChrId2LsCoorPairs.values()) {
			for (CoordPair coordPair : lsCoordPair) {
				txtWrite.writefileln(coordPair.toStringHead());
				for (String indel : coordPair.readPerIndel()) {
					txtWrite.writefileln(indel);
				}
			}
		}
		txtWrite.close();
	}
	
	/** 输出为mummer的coord格式 */
	public void writeToMummer(String mummerCoord) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(mummerCoord, true);
		for (List<CoordPair> lsCoordPair : mapChrId2LsCoorPairs.values()) {
			for (CoordPair coordPair : lsCoordPair) {
				txtWrite.writefileln(coordPair.toString());
			}
		}
		txtWrite.close();
	}
	
}
