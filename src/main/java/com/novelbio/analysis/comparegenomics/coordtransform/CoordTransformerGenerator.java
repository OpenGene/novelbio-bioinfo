package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class CoordTransformerGenerator {
	
	public static void main(String[] args) {
		String mummerPath = "/media/winE/mywork/hongjun-gwas/chromosome/";
		String chrAlt = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta";
		String refFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-4.0/IRGSP-4.0.chrAll.fa.fai";
		String altFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta.fai";

		CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformerMummer(mummerPath+"irgsp-4vs1.coords",
				mummerPath+"irgsp-4vs1.delta", chrAlt, refFai, altFai, 0.99);
		coordTransformer.writeToChain(mummerPath+"irgsp-4vs1.chain");
		coordTransformer.writeToMummer(mummerPath+"irgsp-4vs1.mummer.coord");
		
		TxtReadandWrite txtWriteUnknown = new TxtReadandWrite(mummerPath + "unknownsite.txt", true);
		
		List<String> lsFiles = FileOperate.getLsFoldFileName("/media/winE/mywork/hongjun-gwas/MAP文件");
		for (String file : lsFiles) {
			TxtReadandWrite txtRead = new TxtReadandWrite(file);
			TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(file, ".irgsp1", null), true);
			for (String content : txtRead.readlines()) {
				String[] ss = content.split("\t");
				Align align = new Align(ss[0], Integer.parseInt(ss[3]), Integer.parseInt(ss[3]));
				VarInfo varInfoAlt = coordTransformer.coordTransform(align);
				if (varInfoAlt == null) {
					txtWriteUnknown.writefileln(content);
					continue;
				}
				txtWrite.writefileln(varInfoAlt.getRefID() + "\t" + ss[1] + "\t" + ss[2] + "\t" + varInfoAlt.getStartCis());
			}
			txtWrite.close();
			txtRead.close();
		}
		txtWriteUnknown.close();
	}
	
	public static CoordTransformer generateTransformerChain(String chainFile, String chrAlt) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readChainFile(chainFile);
		CoordTransformer coordTransformer = new CoordTransformer();
		coordTransformer.setMapChrId2LsCoorPairs(mapChrId2LsCoordPair);
		SeqHashInt seqHashAlt = new SeqHash(chrAlt);
		coordTransformer.setSeqHashAlt(seqHashAlt);
		return coordTransformer;
	}
	
	public static CoordTransformer generateTransformerMummer(String mummerFile, String mummerDelta, String chrAlt, String refFai, String altFai, double cutoff) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerFile, mummerDelta, refFai, altFai, cutoff);
		CoordTransformer coordTransformer = new CoordTransformer();
		coordTransformer.setMapChrId2LsCoorPairs(mapChrId2LsCoordPair);
		SeqHashInt seqHashAlt = new SeqHash(chrAlt);
		coordTransformer.setSeqHashAlt(seqHashAlt);
		return coordTransformer;
	}
	
	public static Map<String, List<CoordPair>> readChainFile(String chainFile) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = new LinkedHashMap<>();
		
		TxtReadandWrite txtRead = new TxtReadandWrite(chainFile);
		CoordPair coordPair = null;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("chain")) {
				coordPair = new CoordPair();
				coordPair.initialChainLiftover(content);
				String chrId = coordPair.getChrRef();
				List<CoordPair> lsCoordPairs = mapChrId2LsCoordPair.get(chrId);
				if (lsCoordPairs == null) {
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
	
	public static Map<String, List<CoordPair>> readMummerFile(String mummerFile, String mummerDelta, String refFai, String altFai, double cutoff) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = new LinkedHashMap<>();
		CoordPairMummerReader coordPairReader = new CoordPairMummerReader(mummerFile, refFai, altFai);
		coordPairReader.setIdentityCutoff(cutoff);
		while (coordPairReader.hasNext()) {
			List<CoordPair> lsCoordPairs = coordPairReader.readNext();
			CoordReaderMummer coordMummerReader = new CoordReaderMummer();
			coordMummerReader.setLsPairs(new LinkedList<>(lsCoordPairs));
			coordMummerReader.handleLsCoordPairs();
			List<CoordPair> lsCoordResult = coordMummerReader.getLsPairsResult();
			if (!ArrayOperate.isEmpty(lsCoordResult)) {
				mapChrId2LsCoordPair.put(lsCoordResult.get(0).getRefID(), lsCoordResult);
			}
		}
		coordPairReader.close();
		
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