package com.novelbio.database.domain.geneanno;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.PathDetailNBC;
/**
 * 物种下所有文件的类型
 * @author novelbio
 *
 */
public enum EnumSpeciesFile {

	chromSeqFile("ChromFa"),
	gffRepeatFile("gff"),
	refseqNCfile("refncrna"),
	gffGeneFile("gff"),
	refseqAllIsoRNA("refrna_all_iso"),
	refseqOneIsoRNA("refrna_one_iso"),
	
	refseqAllIsoPro("refprotein_all_iso"),
	refseqOneIsoPro("refprotein_one_iso"),
	
	/** 用来作go，pathway，COG背景的基因 */
	bgGeneFile("BGgene") {
		/** 返回具体的文件路径 */
		public String getSavePath(int taxId, SpeciesFile speciesFile) {
			validateSpeciesFile(taxId, speciesFile);
			
			String basePath = PathDetailNBC.getGenomePath();
			String pathToVersion = speciesFile.getPathToVersion();
			if(StringOperate.isRealNull(pathToVersion))
				return null;
			return basePath + folder + FileOperate.getSepPath() + pathToVersion + speciesFile.getTaxID() + "_" + speciesFile.getVersion() + "_BGgeneList.txt";
		}
	},
	
	rrnaFile("rrnaFile") {
		public String getSavePath(int taxId, SpeciesFile speciesFile) {			
			String path = PathDetailNBC.getGenomePath() + "rrna" + FileOperate.getSepPath();
			path = path + taxId + FileOperate.getSepPath();
			return path;
		}
	},
	
	ChromSepPath("Chrom_Sep") {
		public String getSavePath(int taxId, SpeciesFile speciesFile) {
			validateSpeciesFile(taxId, speciesFile);
			
			String basePath = PathDetailNBC.getGenomePath();
			String pathToVersion = speciesFile.getPathToVersion();
			if(StringOperate.isRealNull(pathToVersion))
				return null;
			return basePath + folder + FileOperate.getSepPath() + pathToVersion;
		}
	},
	
	COG("COG") {
		public String getSavePath(int taxId, SpeciesFile speciesFile) {
			validateSpeciesFile(taxId, speciesFile);
			
			String basePath = PathDetailNBC.getGenomePath();
			String pathToVersion = speciesFile.getPathToVersion();
			if(StringOperate.isRealNull(pathToVersion))
				return null;
			return basePath + folder + FileOperate.getSepPath() + pathToVersion;
		}
	},
	
	;
	/**
	 * 对应保存的文件夹
	 */
	protected String folder;
		
	EnumSpeciesFile(String folder) {
		this.folder = folder;
	}
	
	/**
	 * 获得保存物种文件的路径，最后加上"/"
	 * @param speciesFile
	 * @return
	 */
	public String getSavePath(int taxId, SpeciesFile speciesFile) {
		validateSpeciesFile(taxId, speciesFile);
		
		String basePath = speciesFile.getSpeciesVersionPath();
		if(StringOperate.isRealNull(basePath))
			return null;
		return basePath + folder + FileOperate.getSepPath();
	}
	
	private static void validateSpeciesFile(int taxId, SpeciesFile speciesFile) {
		if (speciesFile == null || speciesFile.getTaxID() == 0) {
			throw new ExceptionNoSpeciesFile(taxId + "Have No SpeciesFile Exist");
		}
	}
	
	public static class ExceptionNoSpeciesFile extends RuntimeException {
		public ExceptionNoSpeciesFile() {
			super();
		}
		
		public ExceptionNoSpeciesFile(String msg) {
			super(msg);
		}
		
		public ExceptionNoSpeciesFile(Throwable e) {
			super(e);
		}
		
		public ExceptionNoSpeciesFile(String msg, Throwable e) {
			super(msg, e);
		}
	}
	
}
