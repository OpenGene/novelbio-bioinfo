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
	refseqNCfile("refrna"),
	gffGeneFile("gff"),
	refseqAllIsoRNA("refrna"),
	refseqOneIsoRNA("refrna"),
	
	refseqAllIsoPro("refprotein"),
	refseqOneIsoPro("refprotein"),
	
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
	;
	/**
	 * 对应保存的文件夹
	 */
	protected String folder;
	
	EnumSpeciesFile(String folder) {
		this.folder = folder;
	}
	
	/**
	 * 获得保存物种文件的路径
	 * @param speciesFile
	 * @return
	 */
	public String getSavePath(SpeciesFile speciesFile) {
		String basePath = speciesFile.speciesVersionPath();
		if(StringOperate.isRealNull(basePath))
			return null;
		return basePath + folder + FileOperate.getSepPath();
	}
	
}
