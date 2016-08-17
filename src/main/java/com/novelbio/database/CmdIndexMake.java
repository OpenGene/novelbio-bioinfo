package com.novelbio.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.SpeciesFileExtract;
import com.novelbio.database.model.species.SpeciesFileSepChr;
import com.novelbio.database.model.species.SpeciesIndexMappingMaker;
import com.novelbio.database.model.species.SpeciesMirnaFile;

public class CmdIndexMake {
	private static final Logger logger = LoggerFactory.getLogger(CmdIndexMake.class);
	
	public static void main(String[] args) {
		if (args == null || args.length == 0 
				|| args[0].replace("-", "").equalsIgnoreCase("help")) {
			System.out.println("java -jar indexmake.jar -taxid 9606 -version GRCh38 -software mirna -islock true");
			System.out.println("software have several params, below is the list");
			
			System.out.println("mirna");
			System.out.println("rfam");
			System.out.println("sepchr");
			System.out.println("refseq");

			System.out.println("");
			System.out.println("bwa_mem");
			System.out.println("bowtie");
			System.out.println("bowtie2");
			System.out.println("");
			System.out.println("hisat2");
			System.out.println("tophat");
			System.out.println("mapsplice");
			System.exit(0);
        }
		Options opts = new Options();
		opts.addOption("taxid", true, "taxid");
		opts.addOption("version", true, "version");
		opts.addOption("software", true, "software");
		opts.addOption("islock", true, "islock");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		String taxId = cliParser.getOptionValue("taxid");
		String version = cliParser.getOptionValue("version");
		String softwareStr = cliParser.getOptionValue("software");
		
		boolean isLock = true;
		String isLockStr = cliParser.getOptionValue("islock");
		if (!StringOperate.isRealNull(isLockStr) && (!isLockStr.trim().equalsIgnoreCase("t") && !isLockStr.trim().equalsIgnoreCase("true"))) {
			isLock = false;
		}
		
//		String taxId = "3702";
//		String version = "tair10";
//		String softwareStr = "rfam";
//		boolean isLock = false;

		Species species = new Species(Integer.parseInt(taxId));
		species.setVersion(version);

		faidexChrFile(species);

		if (StringOperate.isRealNull(softwareStr)) {
			extractMirna(species);
			extractRfam(species);
			sepChr(species, isLock);
			extractRefseq(species);

			index(species, softwareStr, isLock);
			extractMirna(species);
			return;
		}

		if (softwareStr.equals("mirna")) {
			extractMirna(species);
		} else if (softwareStr.equals("rfam")) {
			extractRfam(species);
		} else if (softwareStr.equals("sepchr")) {
			sepChr(species, isLock);
		} else if (softwareStr.equals("refseq")) {
			extractRefseq(species);
		} else {
			index(species, softwareStr, isLock);
		}
	}
	
	private static void faidexChrFile(Species species) {
		SpeciesFile speciesFile = species.getSelectSpeciesFile();
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.indexChrFile();
	}
	
	/** 把染色体切分成一条染色体一个文本，放在一个文件夹中 */
	private static void sepChr(Species species, boolean isLock) {
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setSpeciesFile(species.getSelectSpeciesFile());
		sepChr.setLock(isLock);
		sepChr.generateChrSepFiles();
	}
	
	private static void index(Species species, String softwareStr, boolean isLock) {
		SpeciesFile speciesFile = species.getSelectSpeciesFile();
		SpeciesIndexMappingMaker speciesIndexMappingMaker = new SpeciesIndexMappingMaker(speciesFile);
		speciesIndexMappingMaker.setLock(isLock);
		
		if (softwareStr == null) {
			speciesIndexMappingMaker.makeIndex();
		} else {
			if (softwareStr.equals("bwa")) {
				softwareStr = SoftWare.bwa_mem.toString();
			}
			speciesIndexMappingMaker.makeIndexChr(softwareStr);
		}
	}
	
	private static void extractMirna(Species species) {
		SpeciesMirnaFile speciesMirnaFile = new SpeciesMirnaFile(species.getTaxInfo());
		speciesMirnaFile.extractMiRNA();
	}
	
	private static void extractRfam(Species species) {
		SpeciesFile speciesFile = species.getSelectSpeciesFile();
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.extractRfamFile();		
	}
	
	private static void extractRefseq(Species species) {
		SpeciesFile speciesFile = species.getSelectSpeciesFile();
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.extractRefSeq();
		speciesFileExtract.indexRefseqFile();
	}
	
}
