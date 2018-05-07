package com.novelbio.database.domain.speciesdb;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.generalConf.PathDetailNBC;

public class SpeciesPath {
	/** 基因组所在文件根目录，结尾包含 "/" */
	public static final String genomePath = PathDetailNBC.getGenomePath(); 
	/** 物种文件夹名称 */
	public static final String SPECIES_FOLDER = "species";
	/** 染色体所在的子文件夹 */
	public static final String chromePath = "ChromFa";
	
	/** 染色体所在的子文件夹 */
	public static final String chromeFile = "chrAll.fa";
	
	/** 获得染色体所在的路径 */
	public static String getChromeFile(int taxId, String version) {
		StringBuilder stringBuilder = getPathToVersion(taxId, version);
		stringBuilder.append(chromePath + "/");
		stringBuilder.append(chromeFile);
		return stringBuilder.toString();
	}
	
	public static String getFile(int taxId, String version, String fileType) {
		StringBuilder stringBuilder = getPathToVersion(taxId, version);
		stringBuilder.append(fileType);
	}
	
	/** 到version所在的文件夹，结尾有"/" */
	private static StringBuilder getPathToVersion(int taxId, String version) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(PathDetailNBC.getGenomePath());
		stringBuilder.append(SPECIES_FOLDER + "/");
		stringBuilder.append(taxId + "/");
		stringBuilder.append(version + "/");
		return stringBuilder;
	}
	
}
