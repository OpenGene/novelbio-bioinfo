package com.novelbio.software.coordtransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.bed.BedRecord;

public class MainCoordTransform {
	public static void main(String[] args) {
//		String ss = "Convert --mummerCoord /media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/IRGSPv4_VS_IRGSP1.0.coords"
//				+ " --mummerDelta /media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/IRGSPv4_VS_IRGSP1.0.delta"
//				+ " --queryfai /media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/IRGSP_v4.genome.fa.fai"
//				+ " --subjectfai /media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/oryza_sativa.IRGSP-1.0.dna.fa.fai"
//				+ " -cutoff 0.99 -output /media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/irgspvs_vs_irgsp1.chain";
//		args = ss.split(" ");
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
	
	
	public static void runFormatConvert(String[] args) {
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
			e.printStackTrace();
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
		Map<String, List<CoordPair>> mapChrId2LsCoordPair = CoordTransformerGenerator.readMummerFile(mummerCoord,
				mummerDelta, queryfai, subjectfai, cutoff);
		CoordTransformer.writeToChain(mapChrId2LsCoordPair, output);
	}

	public static void runCoordTransform(String[] args) {
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
			e.printStackTrace();
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
			mapChrId2LsCoordPair = CoordTransformerGenerator.readChainFile(chain);
		} else if (!StringOperate.isRealNull(mummerCoord) && !StringOperate.isRealNull(mummerDelta)) {
			mapChrId2LsCoordPair = CoordTransformerGenerator.readMummerFile(mummerCoord,
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
	
}
