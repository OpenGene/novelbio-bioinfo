package com.novelbio.software.geneformat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.bioinfo.gwas.convertformat.PlinkPed2Vcf;
import com.novelbio.bioinfo.gwas.convertformat.PlinkPedFastaConvertor;

public class MainPlinkPedFormat {
	
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("ped2vcf")) {
				if (args[1].equals("--help") || args[1].equals("-h")) {
					System.out.println(getHelpPed2Vcf());
					System.out.println(0);
				}
				runPed2Vcf(args);
			} else if (args[0].equals("vcf2ped")) {
				if (args[1].equals("--help") || args[1].equals("-h")) {
					System.out.println(getHelpVcf2Ped());
					System.out.println(0);
				}
				runVcf2Ped(args);
			} else if (args[0].equals("ped2fasta")) {
				if (args[1].equals("--help") || args[1].equals("-h")) {
					System.out.println(getHelpPed2Fasta());
					System.out.println(0);
				}
				runPed2Fasta(args);
			} else if (args[0].equals("fasta2ped")) {
				if (args[1].equals("--help") || args[1].equals("-h")) {
					System.out.println(getHelpFasta2Ped());
					System.out.println(0);
				}
				runFasta2Ped(args);
			} else if (args[0].equals("--list")) {
				System.out.println(getHelpToolsList());
				System.exit(0); 
			} else {
				System.out.println(getHelp());
				System.exit(1); 
			}
		} else {
			System.out.println(getHelp());
			System.exit(1); 
		}
	}
	
	public static void runPed2Vcf(String[] args) {
		Options opts = new Options();
		opts.addOption("ped", true, "ped");
		opts.addOption("mid", true, "mid");
		opts.addOption("vcf", true, "vcf");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(getHelpPed2Vcf());
			System.exit(1); 
		}

		String ped = cliParser.getOptionValue("ped", "");
		String mid = cliParser.getOptionValue("mid", "");
		String vcf = cliParser.getOptionValue("vcf");
		PlinkPed2Vcf ped2Vcf = new PlinkPed2Vcf();
		ped2Vcf.convertPed2Vcf(ped, mid, vcf);
	}

	public static void runVcf2Ped(String[] args) {
		Options opts = new Options();
		opts.addOption("vcf", true, "vcf");
		opts.addOption("ped", true, "ped");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(getHelpVcf2Ped());
			System.exit(1); 
		}

		String vcf = cliParser.getOptionValue("vcf", "");
		String ped = cliParser.getOptionValue("ped");
		PlinkPed2Vcf ped2Vcf = new PlinkPed2Vcf();
		ped2Vcf.convertVcf2Ped(vcf, ped);
	}

	public static void runPed2Fasta(String[] args) {
		Options opts = new Options();
		opts.addOption("ped", true, "ped");
		opts.addOption("fasta", true, "fasta");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(getHelpPed2Fasta());
			System.exit(1); 
		}

		String fasta = cliParser.getOptionValue("fasta", "");
		String ped = cliParser.getOptionValue("ped");
		PlinkPedFastaConvertor.convertPed2Fasta(ped, fasta);
	}
	
	public static void runFasta2Ped(String[] args) {
		Options opts = new Options();
		opts.addOption("fasta", true, "fasta");
		opts.addOption("ped", true, "ped");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(getHelpFasta2Ped());
			System.exit(1); 
		}

		String fasta = cliParser.getOptionValue("fasta", "");
		String ped = cliParser.getOptionValue("ped");
		PlinkPedFastaConvertor.convertFasta2Ped(fasta, ped);
	}
	public static String getHelp() {
		String help = "Usage template for all tools "
				+ "\njava -jar PlinkFormatConvertor.jar AnyTool toolArgs "
				+ "\n"
				+ "\nGetting help"
				+ "\njava -jar PlinkFormatConvertor.jar --list       Print the list of available tools "
				+ "\njava -jar PlinkFormatConvertor.jar Tool --help  Print help on a particular tool";
		return help;
	}
	public static String getHelpToolsList() {
		String help = "USAGE:  <program name> [-h]"
				+ "\nAvailable Programs:"
				+ "\n--------------------------------------------------------------------------------------"
				+ "\n    ped2vcf                   convert plink ped file to vcf file, need mid file as input too."
				+ "\n    vcf2ped                   convert vcf file to plink ped file."
				+ "\n    ped2fasta                 convert plink ped file to fasta file."
				+ "\n    fasta2ped                 convert fasta file to plink ped file."
				+ "\n--------------------------------------------------------------------------------------";
		return help;
	}
	public static String getHelpPed2Vcf() {
		String help = "java -jar PlinkFormatConvertor.jar ped2vcf --ped /path/to/in.ped --mid /path/to/in.mid --vcf /path/to/result.vcf"
				+ "\n"
				+ "\n--ped input ped file"
				+ "\n--mid input mid file, 6 columns:"
				+ "\n\tcolumn 1: chrId"
				+ "\n\tcolumn 2: snp-marker"
				+ "\n\tcolumn 3: 0"
				+ "\n\tcolumn 4: position"
				+ "\n\tcolumn 5: ref base"
				+ "\n\tcolumn 6: alt base"
				+ "\n\texample:"
				+ "\n\tchr1\t10100025983\t0\t25983\tC\tT"
				+ "\n--vcf output vcf file";
		return help;
	}
	
	public static String getHelpVcf2Ped() {
		String help = "java -jar PlinkFormatConvertor.jar vcf2ped --vcf /path/to/in.vcf --ped /path/to/result.ped"
				+ "\n"
				+ "\n--vcf input vcf format file"
				+ "\n--ped output ped format file";
		return help;
	}
	
	public static String getHelpPed2Fasta() {
		String help = "java -jar PlinkFormatConvertor.jar ped2fasta --ped /path/to/in.ped --fasta /path/to/out.fasta"
				+ "\n"
				+ "\n--ped input ped format file"
				+ "\n--fasta output fasta format file";
		return help;
	}
	
	public static String getHelpFasta2Ped() {
		String help = "java -jar PlinkFormatConvertor.jar fasta2ped --fasta /path/to/in.fasta --ped /path/to/out.ped"
				+ "\n"
				+ "\n--vcf input vcf file"
				+ "\n--ped output ped file";
		return help;
	}
	
}
