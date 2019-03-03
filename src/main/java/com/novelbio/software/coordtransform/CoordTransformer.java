package com.novelbio.software.coordtransform;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.bed.BedRecord;
import com.novelbio.bioinfo.fasta.ExceptionSeqFastaNoChr;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqHashInt;
import com.novelbio.software.snpanno.SnpInfo;
import com.novelbio.software.snpanno.SnpInfo.EnumHgvsVarType;

import lombok.experimental.var;

public class CoordTransformer {
		
	SeqHashInt seqHashAlt;
	
	CoordPairSearchAbs coordPairSearch;
	
	public static void main(String[] args) {
		SnpInfo snpInfo = new SnpInfo("chr1", 1234, "A", "AAT");
		System.out.println(snpInfo.getAlign());
	}

	void setSeqHashAlt(SeqHashInt seqHashAlt) {
		this.seqHashAlt = seqHashAlt;
	}
	
	public void setCoordPairSearch(CoordPairSearchAbs coordPairSearch) {
		this.coordPairSearch = coordPairSearch;
	}
	
	public SnpInfo coordTransform(SnpInfo snpInfo) {
		Align alignRef = new Align(snpInfo.getAlign());
		VarInfo varInfo = coordTransform(alignRef);
		if (varInfo == null) {
			return null;
		}
		return transformSnpInfo(snpInfo, varInfo, seqHashAlt);
	}
	
	/** 坐标转换
	 * 没有转出来则为null
	 * @param alignRef
	 * @return
	 */
	public VarInfo coordTransform(Align alignRef) {
		return coordTransform(coordPairSearch, alignRef);
	}
	
	/** 坐标转换 */
	public BedRecord coordTransform(BedRecord bedRecord) {
		VarInfo varInfo = coordTransform(coordPairSearch, bedRecord);
		if (varInfo == null) {
			return null;
		}
		BedRecord bedRecordTrans = bedRecord.clone();
		if (varInfo != null) {
			bedRecordTrans.setChrId(varInfo.getChrId());
			bedRecordTrans.setStartEndLoc(varInfo.getStartCis(), varInfo.getEndCis());
			bedRecordTrans.setCis5to3(varInfo.isCis5to3());
		}
		return bedRecordTrans;
	}
	@VisibleForTesting
	protected static SnpInfo transformSnpInfo(SnpInfo snpInfo, VarInfo varInfo, SeqHashInt seqHashAlt) {
		String ref = snpInfo.getSeqRef();
		String alt = snpInfo.getSeqAlt();
		String refAlt = null, altAlt = null;
		SnpInfo snpInfoAlt;
		if (snpInfo.getVarType() == EnumHgvsVarType.NOVAR || snpInfo.getVarType() == EnumHgvsVarType.Substitutions) {
			try {
				refAlt = seqHashAlt.getSeqCis(varInfo).toString();
				altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			} catch (ExceptionSeqFastaNoChr e) {
				return null;
			}
			refAlt = seqHashAlt.getSeqCis(varInfo).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			snpInfoAlt = new SnpInfo(varInfo.getChrId(), varInfo.getStartAbs(), refAlt, altAlt);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Insertions) {
			int start = varInfo.isCis() ? varInfo.getStartAbs() : varInfo.getStartAbs()-1;
			String snpHead = seqHashAlt.getSeq(varInfo.getChrId(), start, start).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			snpInfoAlt = new SnpInfo(varInfo.getChrId(), start, snpHead, snpHead+altAlt);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Deletions) {
			int start = varInfo.getStartAbs();
			String snpHead = seqHashAlt.getSeq(varInfo.getChrId(), start-1, start-1).toString();
			altAlt = seqHashAlt.getSeqCis(varInfo).toString();
			snpInfoAlt = new SnpInfo(varInfo.getChrId(),start-1, snpHead + altAlt, snpHead);
		} else if (snpInfo.getVarType() == EnumHgvsVarType.Indels) {
			refAlt = seqHashAlt.getSeqCis(varInfo).toString();
			altAlt = varInfo.isCis() ? alt : SeqFasta.reverseComplement(alt);
			if (varInfo.getStartBias() > 0 || varInfo.getEndBias() > 0) {
				return null;
			}
			snpInfoAlt = new SnpInfo(varInfo.getChrId(), varInfo.getStartAbs(), refAlt, altAlt);
		} else {
			throw new ExceptionNBCCoordTransformer("unsupported type " + snpInfo.getVarType() + " " + snpInfo.toString());
		}
		if (snpInfoAlt != null) {
			boolean correct = varInfo.isCis() && alt.equalsIgnoreCase(altAlt) 
					|| !varInfo.isCis() && alt.equalsIgnoreCase(SeqFasta.reverseComplement(altAlt));
			if (!correct) {
				throw new ExceptionNBCCoordTransformer("incorrect snp! strand: " + varInfo.isCis() + " ref: " + snpInfo.toString() + " alt: " + snpInfoAlt.toString());
			}
		}
		
		return snpInfoAlt;
	}
	
	@VisibleForTesting
	protected static VarInfo coordTransform(CoordPairSearchAbs coordPairSearch, Alignment alignRef) {
		List<CoordPair> lsCoordPairsOverlap = coordPairSearch.findCoordPairsOverlap(alignRef);
		if (lsCoordPairsOverlap.isEmpty()) {
			return null;
		}
		if (lsCoordPairsOverlap.size() > 1) {
			//暂时不支持跨越多个区段
			return null;
		}
		
		CoordPair coordPair = lsCoordPairsOverlap.get(0);
		int start = alignRef.getStartAbs(), end = alignRef.getEndAbs();

		//暂时不支持
		int biasStart = 0, biasEnd = 0;
		if (alignRef.getStartAbs() < coordPair.getStartAbs()) {
			biasStart = coordPair.getStartAbs() - alignRef.getStartAbs();
			start = coordPair.getStartAbs();
		}
		if (alignRef.getEndAbs() > coordPair.getEndAbs()) {
			biasEnd = alignRef.getEndAbs() - coordPair.getEndAbs();
			end = coordPair.getEndAbs();
		}
		
		VarInfo varInfo = coordPairSearch.findVarInfo(coordPair, start, end);
		if (varInfo == null) {
			return varInfo;
		}
		varInfo.setStartBias(varInfo.getStartBias()+biasStart);
		varInfo.setEndBias(varInfo.getEndBias()+biasEnd);
		if (alignRef.isCis5to3() != null && !alignRef.isCis5to3()) {
			varInfo.setCis5to3(!varInfo.isCis5to3());
		}
		return varInfo;
	}
	
	/** 输出为mummer的coord格式 */
	public static void writeToMummer(Map<String, List<CoordPair>> mapChrId2LsCoorPairs, String mummerCoord) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(mummerCoord, true);
		for (List<CoordPair> lsCoordPair : mapChrId2LsCoorPairs.values()) {
			for (CoordPair coordPair : lsCoordPair) {
				txtWrite.writefileln(coordPair.toString());
			}
		}
		txtWrite.close();
	}
	
	/** 输出为liftover的chain格式 */
	public static void writeToChain(Map<String, List<CoordPair>> mapChrId2LsCoorPairs, String chainFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(chainFile, true);
		for (List<CoordPair> lsCoordPair : mapChrId2LsCoorPairs.values()) {
			for (CoordPair coordPair : lsCoordPair) {
				txtWrite.writefileln(coordPair.toStringHead());
				for (String indel : coordPair.readPerIndel()) {
					txtWrite.writefileln(indel);
				}
				txtWrite.writefileln();
			}
		}
		txtWrite.close();
	}
}
