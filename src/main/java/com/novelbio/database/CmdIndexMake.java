package com.novelbio.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.StringOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.SpeciesFileExtract;
import com.novelbio.database.model.species.SpeciesIndexMappingMaker;
import com.novelbio.database.model.species.SpeciesMirnaFile;

public class CmdIndexMake {
	
	public static void main(String[] args) {
//		if (args == null || args.length == 0 
//				|| args[0].replace("-", "").equalsIgnoreCase("help")) {
//			System.out.println("java -jar indexmake.jar -taxid 9606 -version GRCh38 -software hisat2 -islock true");
//			System.exit(0);
//        }
//		Options opts = new Options();
//		opts.addOption("taxid", true, "taxid");
//		opts.addOption("version", true, "version");
//		opts.addOption("software", true, "software");
//		opts.addOption("islock", true, "islock");
//		CommandLine cliParser = null;
//		try {
//			cliParser = new GnuParser().parse(opts, args);
//		} catch (Exception e) {
//			System.exit(1);
//		}
//		String taxId = cliParser.getOptionValue("taxid");
//		String version = cliParser.getOptionValue("version");
//		String softwareStr = cliParser.getOptionValue("software");
//		
//		boolean isLock = true;
//		String isLockStr = cliParser.getOptionValue("islock");
//		if (!StringOperate.isRealNull(isLockStr) && (!isLockStr.trim().equalsIgnoreCase("t") && !isLockStr.trim().equalsIgnoreCase("true"))) {
//			isLock = false;
//		}
		
		String taxId = "9913";
		String version = "bt_alt_Btau_5.0.1";
		String softwareStr = "rfam";
		boolean isLock = false;
		
		Species species = new Species(Integer.parseInt(taxId));
		species.setVersion(version);
		
		if (StringOperate.isRealNull(softwareStr)) {
			extractMirna(species);
			extractRfam(species);
			index(species, softwareStr, isLock);
		}
		
		if (softwareStr.equals("mirna")) {
			extractMirna(species);
		} else if (StringOperate.isRealNull(softwareStr) || softwareStr.equals("rfam")) {
			extractRfam(species);
		} else {
			index(species, softwareStr, isLock);
		}
	}
	
	private static void index(Species species, String softwareStr, boolean isLock) {
		SpeciesFile speciesFile = species.getSelectSpeciesFile();
		SpeciesIndexMappingMaker speciesIndexMappingMaker = new SpeciesIndexMappingMaker(speciesFile);
		speciesIndexMappingMaker.setLock(isLock);
		
		if (softwareStr == null) {
			speciesIndexMappingMaker.makeIndex();
		} else {
			SoftWare softWare = SoftWare.valueOf(softwareStr);
			speciesIndexMappingMaker.makeIndexChr(softWare);
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
}
