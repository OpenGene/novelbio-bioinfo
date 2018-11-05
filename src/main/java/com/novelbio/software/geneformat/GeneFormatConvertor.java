package com.novelbio.software.geneformat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffType;

public class GeneFormatConvertor {
	
	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption("intype", true, "gff/gtf/bed/ucsc");
		opts.addOption("in", true, "mummer delta");
		opts.addOption("outtype", true, "query.fa.fai");
		opts.addOption("out", true, "subject.fa.fai");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			System.out.println(getHelp());
			System.exit(1); 
		}

		String intype = cliParser.getOptionValue("intype", "");
		String in = cliParser.getOptionValue("in", "");
		String outtype = cliParser.getOptionValue("outtype");
		String out = cliParser.getOptionValue("out");
		GffHashGene gffHashGene = new GffHashGene(GffType.valueOf(intype), in);
		gffHashGene.writeToFile(GffType.valueOf(outtype), out);
	}
	
	private static String getHelp() {
		List<String> lsHelp = new ArrayList<>();
		lsHelp.add("java -jar geneFormatConvertor.jar --intype gff --in hg19.gff --outtype ucsc --out hg19.ucsc");
		lsHelp.add("");
		lsHelp.add("--intype         type of input file, have gff, gtf, ucsc");
		lsHelp.add("--in         input file, like hg19.gff");
		lsHelp.add("--outtype         type of output file, have gff, gtf, bed, ucsc");
		lsHelp.add("--out         output file, like hg19.gtf");
		return ArrayOperate.cmbString(lsHelp, "\n");
	}
	
}
