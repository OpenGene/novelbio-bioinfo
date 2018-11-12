package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fasta.SeqHashInt;

public class CoordTransformerGenerator {
	
	public static CoordTransformer generateTransformerChain(String chainFile, String chrAlt) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readChainFile(chainFile);
		CoordTransformer coordTransformer = new CoordTransformer();
		
		CoordPairSearch coordPairSearch = new CoordPairSearch(mapChrId2LsCoordPair);
		coordTransformer.setCoordPairSearch(coordPairSearch);

		if (StringOperate.isRealNull(chrAlt)) {
			SeqHashInt seqHashAlt = new SeqHash(chrAlt);
			coordTransformer.setSeqHashAlt(seqHashAlt);
		}
		return coordTransformer;
	}
	
	/** 给数据库使用 */
	public static CoordTransformer generateTransformer(CoordPairSearchAbs coordPairSearchAbs) {
		CoordTransformer coordTransformer = new CoordTransformer();		
		coordTransformer.setCoordPairSearch(coordPairSearchAbs);
		return coordTransformer;
	}
	
	public static CoordTransformer generateTransformer(Map<String, List<CoordPair>> mapChrId2LsCoordPair, String chrAlt) {
		CoordTransformer coordTransformer = new CoordTransformer();
		CoordPairSearch coordPairSearch = new CoordPairSearch(mapChrId2LsCoordPair);
		coordTransformer.setCoordPairSearch(coordPairSearch);
		
		if (!StringOperate.isRealNull(chrAlt)) {
			SeqHashInt seqHashAlt = new SeqHash(chrAlt);
			coordTransformer.setSeqHashAlt(seqHashAlt);
		}
		return coordTransformer;
	}
	
	public static CoordTransformer generateTransformerMummer(String mummerFile, String mummerDelta, String chrAlt, String refFai, String altFai, double cutoff) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerFile, mummerDelta, refFai, altFai, cutoff);
		CoordTransformer coordTransformer = new CoordTransformer();
		
		CoordPairSearch coordPairSearch = new CoordPairSearch(mapChrId2LsCoordPair);
		coordTransformer.setCoordPairSearch(coordPairSearch);
		
		if (!StringOperate.isRealNull(chrAlt)) {
			SeqHashInt seqHashAlt = new SeqHash(chrAlt);
			coordTransformer.setSeqHashAlt(seqHashAlt);
		}
		return coordTransformer;
	}
	
	/** 将mummer转换为liftover chain文件 */
	public static void convertMummer2Chain(String mummerFile, String mummerDelta, String refFai, String altFai, double cutoff, String chainFile) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerFile, mummerDelta, refFai, altFai, cutoff);
		CoordTransformer.writeToChain(mapChrId2LsCoordPair, chainFile);
	}
	
	
	public static Map<String, List<CoordPair>> readChainFile(String chainFile) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = new LinkedHashMap<>();
		
		TxtReadandWrite txtRead = new TxtReadandWrite(chainFile);
		CoordPair coordPair = null;
		for (String content : txtRead.readlines()) {
			if (StringOperate.isRealNull(content)) {
				continue;
			}
			if (content.startsWith("chain")) {
				coordPair = new CoordPair();
				coordPair.initialChainLiftover(content);
				String chrId = coordPair.getChrRef();
				List<CoordPair> lsCoordPairs = mapChrId2LsCoordPair.get(chrId);
				if (lsCoordPairs == null) {
					lsCoordPairs = new ArrayList<>();
					mapChrId2LsCoordPair.put(chrId, lsCoordPairs);
				}
				lsCoordPairs.add(coordPair);
				continue;
			}
			
			coordPair.addChainLiftover(content);
		}
		txtRead.close();
		return mapChrId2LsCoordPair;
	}
	
	public static Map<String, List<CoordPair>> readMummerFile(String mummerFile, String mummerDelta, double cutoff) {
		return readMummerFile(mummerFile, mummerDelta, null, null, cutoff);
	}
	public static Map<String, List<CoordPair>> readMummerFile(String mummerFile, String mummerDelta, String refFai, String altFai, double cutoff) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = new LinkedHashMap<>();
		CoordPairMummerReader coordPairReader = new CoordPairMummerReader(mummerFile, refFai, altFai);
		coordPairReader.setIdentityCutoff(cutoff);
		while (coordPairReader.hasNext()) {
			List<CoordPair> lsCoordPairs = coordPairReader.readNext();
			
			if (!ArrayOperate.isEmpty(lsCoordPairs)) {
				String chrId = lsCoordPairs.get(0).getChrId();
				List<CoordPair> lsCoord = mapChrId2LsCoordPair.get(chrId);
				if (lsCoord == null) {
					mapChrId2LsCoordPair.put(chrId, lsCoordPairs);
				} else {
					lsCoord.addAll(lsCoordPairs);
				}
			}
		}
		coordPairReader.close();

		for (String chrId : mapChrId2LsCoordPair.keySet()) {
			List<CoordPair> lsCoordPairs = mapChrId2LsCoordPair.get(chrId);
			CoordReaderMummer coordMummerReader = new CoordReaderMummer();
			coordMummerReader.setLsPairs(new LinkedList<>(lsCoordPairs));
			coordMummerReader.handleLsCoordPairs();
			List<CoordPair> lsCoordResult = coordMummerReader.getLsPairsResult();
			lsCoordPairs.clear();
			lsCoordPairs.addAll(lsCoordResult);
		}
		
		MummerDeltaReader mummerDeltaReader = new MummerDeltaReader();
		mummerDeltaReader.setMapChrId2CoordPair(mapChrId2LsCoordPair);
		mummerDeltaReader.generateMapLoc2Pair();
		mummerDeltaReader.readDelta(mummerDelta);
		
		List<String> lsChrId = new ArrayList<>(mapChrId2LsCoordPair.keySet());
		for (String chrId : lsChrId) {
			List<CoordPair> lsCoordPairs = mapChrId2LsCoordPair.get(chrId);
			lsCoordPairs = mergeLsCoord(lsCoordPairs);
			mapChrId2LsCoordPair.put(chrId, lsCoordPairs);
		}
		return mapChrId2LsCoordPair;
	}
	
	@VisibleForTesting
	protected static List<CoordPair> mergeLsCoord(List<CoordPair> lsCoordPair) {
		List<CoordPair> lsCoordPairResult = new ArrayList<>();
		
		CoordPair coordPairLast = null;
		for (CoordPair coordPair : lsCoordPair) {
			if (lsCoordPairResult.isEmpty()) {
				lsCoordPairResult.add(coordPair);
				coordPairLast = coordPair;
				continue;
			}
			if (coordPairLast.isCanAdd(coordPair)) {
				coordPairLast.addCoordPair(coordPair);
			} else {
				lsCoordPairResult.add(coordPair);
				coordPairLast = coordPair;
			}
		}
		return lsCoordPairResult;
	}
	
}