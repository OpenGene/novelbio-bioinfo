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
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fasta.SeqHashInt;

public class CoordTransformerGenerator {
	
	public static void main(String[] args) {
		String mummerPath = "/media/winE/mywork/hongjun-gwas/chromosome/";
		String chrAlt = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta";
		String refFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-4.0/IRGSP-4.0.chrAll.fa.fai";
		String altFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta.fai";

		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerPath+"irgsp-4vs1.coords",
				mummerPath+"irgsp-4vs1.delta", refFai, altFai, 0.99);

		CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformerMummer(mummerPath+"irgsp-4vs1.coords",
				mummerPath+"irgsp-4vs1.delta", chrAlt, refFai, altFai, 0.99);
		
		
		CoordTransformer.writeToChain(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.chain");
		CoordTransformer.writeToMummer(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.mummer.coord");
		
		TxtReadandWrite txtWriteAll = new TxtReadandWrite(mummerPath + "allsite.txt", true);
		
		List<String> lsFiles = FileOperate.getLsFoldFileName("/media/winE/mywork/hongjun-gwas/MAP文件");
		for (String file : lsFiles) {
			TxtReadandWrite txtRead = new TxtReadandWrite(file);
			TxtReadandWrite txtWriteExist = new TxtReadandWrite(FileOperate.changeFileSuffix(file, ".irgsp1", null), true);
			TxtReadandWrite txtWriteNone = new TxtReadandWrite(FileOperate.changeFileSuffix(file, ".irgsp1.notexist", null), true);
			for (String content : txtRead.readlines()) {
				String[] ss = content.split("\t");
				Align align = new Align(ss[0], Integer.parseInt(ss[3]), Integer.parseInt(ss[3]));
				VarInfo varInfoAlt = coordTransformer.coordTransform(align);
				if (varInfoAlt == null) {
					txtWriteNone.writefileln(ss[1]);
					txtWriteAll.writefileln(ss[0] +"\t" + ss[3] + "\tNONE\tNONE");
					continue;
				}
				txtWriteExist.writefileln(varInfoAlt.getChrId() + "\t" + ss[1] + "\t" + ss[2] + "\t" + varInfoAlt.getStartCis());
				txtWriteAll.writefileln(ss[0] +"\t" + ss[3] + "\t"+ varInfoAlt.getChrId() + "\t" + varInfoAlt.getStartCis());
			}
			txtWriteExist.close();
			txtWriteNone.close();
			txtRead.close();
		}
		txtWriteAll.close();
	}
	
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
	
	public static CoordTransformer generateTransformerMummer(String mummerFile, String mummerDelta, String chrAlt, String refFai, String altFai, double cutoff) {
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerFile, mummerDelta, refFai, altFai, cutoff);
		CoordTransformer coordTransformer = new CoordTransformer();
		
		CoordPairSearch coordPairSearch = new CoordPairSearch(mapChrId2LsCoordPair);
		coordTransformer.setCoordPairSearch(coordPairSearch);
		
		if (StringOperate.isRealNull(chrAlt)) {
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
				mapChrId2LsCoordPair.put(lsCoordResult.get(0).getChrId(), lsCoordResult);
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