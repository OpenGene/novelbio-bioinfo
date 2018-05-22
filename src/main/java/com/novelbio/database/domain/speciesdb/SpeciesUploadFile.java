package com.novelbio.database.domain.speciesdb;

import java.io.InputStream;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.service.species.MgmtSpecies;
import com.novelbio.generalconf.PathDetailNBC;

/** 上传文件 */
public class SpeciesUploadFile {
	
	MgmtSpecies mgmtSpecies = MgmtSpecies.getInstance();
	
	public void uploadFile(int taxId, String version, String fileType) {
		
	}
	public void uploadChrFile(int taxId, String version, InputStream is) {
		String basePath = PathDetailNBC.getGenomePath() + SpeciesFile.SPECIES_FOLDER + FileOperate.getSepPath()
		+ taxId + FileOperate.getSepPath() + version + FileOperate.getSepPath();
		String resultFile = basePath + "ChromFa/chrAll.fa";
		
	}
}
