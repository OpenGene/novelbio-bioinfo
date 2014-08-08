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
	
	rrnaFile("rrnaFile") {
		public String getSavePath(SpeciesFile speciesFile) {
			int taxid = speciesFile.getTaxID();
			String path = PathDetailNBC.getGenomePath() + "rrna" + FileOperate.getSepPath();
			path = path + taxid + FileOperate.getSepPath();
			return path;
		}
	},
	
	ChromSepPath("Chrom_Sep") {
		public String getSavePath(SpeciesFile speciesFile) {
			String basePath = SpeciesFile.getPathParent();
			String pathToVersion = speciesFile.getPathToVersion();
			if(StringOperate.isRealNull(pathToVersion))
				return null;
			return basePath + folder + FileOperate.getSepPath() + pathToVersion;
		}
	},
	
	COG("COG") {
		public String getSavePath(SpeciesFile speciesFile) {
			String basePath = SpeciesFile.getPathParent();
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
	public String getSavePath(SpeciesFile speciesFile) {
		String basePath = speciesFile.getSpeciesVersionPath();
		if(StringOperate.isRealNull(basePath))
			return null;
		return basePath + folder + FileOperate.getSepPath();
	}
	
}
