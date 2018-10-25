package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.bed.BedRecord;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fasta.SeqHashInt;

public class CoordTransformerGenerator {
	
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("Convert")) {
				runFormatConvert(args);
			} else if (args[0].equals("Transform")) {
				runCoordTransform(args);
			} else {
				System.out.println(getHelp());
				System.exit(1); 
			}
		} else {
			System.out.println(getHelp());
			System.exit(1); 
		}
	}
	
	private static void runFormatConvert(String[] args) {
		Options opts = new Options();
		opts.addOption("mummerCoord", true, "mummer.coord");
		opts.addOption("mummerDelta", true, "mummer delta");
		opts.addOption("queryfai", true, "query.fa.fai");
		opts.addOption("subjectfai", true, "subject.fa.fai");
		opts.addOption("cutoff", true, "cutoff");
		opts.addOption("output", true, "output");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.out.println(getHelp());
			System.exit(1); 
		}

		String mummerCoord = cliParser.getOptionValue("mummerCoord", "");
		String mummerDelta = cliParser.getOptionValue("mummerDelta", "");
		String queryfai = cliParser.getOptionValue("queryfai");
		String subjectfai = cliParser.getOptionValue("subjectfai");
		String cutoffStr = cliParser.getOptionValue("cutoff", "0.99");
		double cutoff = Double.parseDouble(cutoffStr);
		String output = cliParser.getOptionValue("output");
		if (!output.endsWith(".chain")) {
			output = output+".chain";
		}
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerCoord,
				mummerDelta, queryfai, subjectfai, cutoff);
		CoordTransformer.writeToChain(mapChrId2LsCoordPair, output);
	}

	private static void runCoordTransform(String[] args) {
		Options opts = new Options();
		//Mummer
		opts.addOption("mummerCoord", true, "mummer.coord");
		opts.addOption("mummerDelta", true, "mummer delta");
		opts.addOption("cutoff", true, "cutoff");
		//Liftover
		opts.addOption("chain", true, "liftover.chain");
		
		opts.addOption("format", true, "format");
		opts.addOption("subjectFa", true, "subject.fa");

		opts.addOption("in", true, "input");
		opts.addOption("out", true, "output");
		opts.addOption("outNoFind", true, "output");

		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.out.println(getHelp()); 
			System.exit(1); 
		}

		String mummerCoord = cliParser.getOptionValue("mummerCoord", "");
		String mummerDelta = cliParser.getOptionValue("mummerDelta", "");
		String cutoffStr = cliParser.getOptionValue("cutoff", "0.99");
		double cutoff = 0;
		if (!StringOperate.isRealNull(cutoffStr)) {
			cutoff = Double.parseDouble(cutoffStr);
		}
		
		String chain = cliParser.getOptionValue("chain", "");
		
		String format = cliParser.getOptionValue("format", "");
		//目前仅用于snp
		String subjectFa = cliParser.getOptionValue("subjectFa", "");

		String in = cliParser.getOptionValue("in", "");
		String out = cliParser.getOptionValue("out");
		String outNoFind = cliParser.getOptionValue("outNoFind");

		Map<String, List<CoordPair>> mapChrId2LsCoordPair = null;
		if (!StringOperate.isRealNull(chain)) {
			mapChrId2LsCoordPair = readChainFile(chain);
		} else if (!StringOperate.isRealNull(mummerCoord) && !StringOperate.isRealNull(mummerDelta)) {
			mapChrId2LsCoordPair = readMummerFile(mummerCoord,
					mummerDelta, null, null, cutoff);
		}
		CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformer(mapChrId2LsCoordPair, subjectFa);
		if (StringOperate.isEqualIgnoreCase(format, "bed")) {
			TxtReadandWrite bedRead = new TxtReadandWrite(in);
			TxtReadandWrite bedWrite = new TxtReadandWrite(out, true);
			TxtReadandWrite bedWriteCannotTransform = new TxtReadandWrite(outNoFind, true);

			for (String content : bedRead.readlines()	) {
				BedRecord bedRecord = new BedRecord(content);
				BedRecord bedRecordTrans = coordTransformer.coordTransform(bedRecord);
				if (bedRecordTrans != null) {
					bedWrite.writefileln(bedRecordTrans.toString());
				} else {
					bedWriteCannotTransform.writefileln(bedRecord.toString());
				}
			}
			bedRead.close();
			bedWrite.close();
			bedWriteCannotTransform.close();
		} else {
			System.err.println("unsupported format type " + format);
			System.out.println(getHelp()); 
			System.exit(1); 
		}
	}
	
	private static String getHelp() {
		List<String> lsHelp = new ArrayList<>();
		lsHelp.add("Convert Mummer file to Liftover Chain file:\n");
		lsHelp.add("java -jar coordTransform.jar Convert "
				+ " --mummerCoord mummer.coord --mummerDelta mummer.delta"
				+ " --queryfai query.fa.fai --subjectfai subject.fa.fai"
				+ " --cutoff 0.99"
				+ " --output outfile.chain"
				);
		lsHelp.add("Coordinate Transform:\n");
		lsHelp.add("java -jar coordTransform.jar Transform "
				+ " --mummerCoord mummer.coord --mummerDelta mummer.delta"
				+ " --cutoff 0.99"
				+ " --format bed"
				+ " --in input.bed"
				+ " --out output.bed"
				+ " --outNoFind output.cannot.transform.bed"
				);
		lsHelp.add("java -jar coordTransform.jar Transform "
				+ " --chain liftover.chain"
				+ " --format bed"
				+ " --in input.bed"
				+ " --out output.bed"
				+ " --outNoFind output.cannot.transform.bed"
				);
		lsHelp.add("");
		lsHelp.add("Parameters:");
		lsHelp.add("");
		lsHelp.add("Convert        Using to convert the mummer output file to liftover chain file");
		lsHelp.add("--mummerCoord         mummer coord file, like hg18_vs_hg19.coord");
		lsHelp.add("--mummerDelta         mummer delta file, like hg18_vs_hg19.delta");
		lsHelp.add("--queryfai         faidx file of query reference, like hg18.fa.fai");
		lsHelp.add("--subjectfai         faidx file of subject reference, like hg19.fa.fai");
		lsHelp.add("--cutoff         default is 0.99. Mummer result contains many matched compares, we just need compares with identity larger than the cutoff");
		lsHelp.add("--output         the liftover chain file that can be used by liftover");
		lsHelp.add("");
		lsHelp.add("");
		lsHelp.add("Transform        doing coordinate transform. Note that transform can use either mummer_out_file or liftover_chain_file."
				+ " If someone set both two files, chain file will be used");
		lsHelp.add("");
		lsHelp.add("--mummerCoord         mummer coord file, like hg18_vs_hg19.coord");
		lsHelp.add("--mummerDelta         mummer delta file, like hg18_vs_hg19.delta");
		lsHelp.add("--cutoff         default is 0.99. Mummer result contains many matched compares, we just need compares with identity larger than the cutoff");
		lsHelp.add("");
		lsHelp.add("--chain         liftover chain file. Not that mummer");
		lsHelp.add("");
		lsHelp.add("--format         input file format, Only support bed file at this time");
		lsHelp.add("--in         input.bed");
		lsHelp.add("--out         output.bed");
		lsHelp.add("--outNoFind         output.coord_cannot_find.bed");
		return ArrayOperate.cmbString(lsHelp, "\n");
	}
	
//	public static void main1(String[] args) {
//		String mummerPath = "/media/winE/mywork/nongkeyuan/coordtransform/tigr6VStigr7/";
//		String refFai = "/media/winE/mywork/nongkeyuan/coordtransform/tigr6VStigr7/tigr6.fa.fai";
//		String altFai = "/media/winE/mywork/nongkeyuan/coordtransform/tigr6VStigr7/tigr7.fa.fai";
//
////		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerPath+"tigr6VStigr7.coords",
////				mummerPath+"tigr6VStigr7.delta", refFai, altFai, 0.99);
//		
//		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readChainFile(mummerPath+"irgsp-4vs1.chain");
//		
//		CoordTransformer.writeToChain(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.2.chain");
//		CoordTransformer.writeToMummer(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.mummer.2.coord");
//		
////		CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformerMummer(mummerPath+"irgsp-4vs1.coords",
////				mummerPath+"irgsp-4vs1.delta", chrAlt, refFai, altFai, 0.99);
//		
//	}
//	
//	public static void main2(String[] args) {
//		String mummerPath = "/media/winE/mywork/hongjun-gwas/chromosome/";
//		String chrAlt = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta";
//		String refFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-4.0/IRGSP-4.0.chrAll.fa.fai";
//		String altFai = "/media/winE/mywork/hongjun-gwas/chromosome/IRGSP-1.0.chrAll.fasta.fai";
//
//		Map<String, List<CoordPair>> mapChrId2LsCoordPair = readMummerFile(mummerPath+"irgsp-4vs1.coords",
//				mummerPath+"irgsp-4vs1.delta", refFai, altFai, 0.99);
//
//		CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformerMummer(mummerPath+"irgsp-4vs1.coords",
//				mummerPath+"irgsp-4vs1.delta", chrAlt, refFai, altFai, 0.99);
//		
//		
//		CoordTransformer.writeToChain(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.chain");
//		CoordTransformer.writeToMummer(mapChrId2LsCoordPair, mummerPath+"irgsp-4vs1.mummer.coord");
//		
//		TxtReadandWrite txtWriteAll = new TxtReadandWrite(mummerPath + "allsite.txt", true);
//		
//		List<String> lsFiles = FileOperate.getLsFoldFileName("/media/winE/mywork/hongjun-gwas/MAP文件");
//		for (String file : lsFiles) {
//			TxtReadandWrite txtRead = new TxtReadandWrite(file);
//			TxtReadandWrite txtWriteExist = new TxtReadandWrite(FileOperate.changeFileSuffix(file, ".irgsp1", null), true);
//			TxtReadandWrite txtWriteNone = new TxtReadandWrite(FileOperate.changeFileSuffix(file, ".irgsp1.notexist", null), true);
//			for (String content : txtRead.readlines()) {
//				String[] ss = content.split("\t");
//				Align align = new Align(ss[0], Integer.parseInt(ss[3]), Integer.parseInt(ss[3]));
//				VarInfo varInfoAlt = coordTransformer.coordTransform(align);
//				if (varInfoAlt == null) {
//					txtWriteNone.writefileln(ss[1]);
//					txtWriteAll.writefileln(ss[0] +"\t" + ss[3] + "\tNONE\tNONE");
//					continue;
//				}
//				txtWriteExist.writefileln(varInfoAlt.getChrId() + "\t" + ss[1] + "\t" + ss[2] + "\t" + varInfoAlt.getStartCis());
//				txtWriteAll.writefileln(ss[0] +"\t" + ss[3] + "\t"+ varInfoAlt.getChrId() + "\t" + varInfoAlt.getStartCis());
//			}
//			txtWriteExist.close();
//			txtWriteNone.close();
//			txtRead.close();
//		}
//		txtWriteAll.close();
//	}
	
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