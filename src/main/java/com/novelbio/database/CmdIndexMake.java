package com.novelbio.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.novelbio.base.StringOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.SpeciesIndexMappingMaker;

public class CmdIndexMake {
	
	public static void main(String[] args) {
		if (args == null || args.length == 0 
				|| args[0].replace("-", "").equalsIgnoreCase("help")) {
			System.out.println("java -jar indexmake.jar -taxid 9606 -version GRCh38 -software hisat2 -islock true");
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
		
		
		
		Species species = new Species(Integer.parseInt(taxId));
		species.setVersion(version);
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
}
