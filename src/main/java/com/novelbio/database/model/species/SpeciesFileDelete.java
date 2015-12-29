package com.novelbio.database.model.species;

import java.util.List;

import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.EnumSpeciesFile;
import com.novelbio.database.domain.geneanno.SpeciesFile;

public class SpeciesFileDelete {
	
	SpeciesFile speciesFile;
	
	public SpeciesFileDelete(SpeciesFile speciesFile) {
		this.speciesFile = speciesFile;
	}
	
	/** 删除文件并保存 */
	public void deleteGffFile(String gffDb) {
		deleteGff(gffDb);
	}
	
	/** 可能会有并发问题，删除之前先从数据库中提取 */
	public void deleteFile(EnumSpeciesFile fileType) {
		if (fileType == EnumSpeciesFile.gffGeneFile) {
			throw new ExceptionNbcParamError("unsupport input param, cannot delete gfffile use this method");
        }
		
		switch (fileType) {
		case chromSeqFile: {
			deleteChrom();
			break;
		}
		case gffRepeatFile: {
			FileOperate.DeleteFileFolder(speciesFile.getGffRepeatFile());
			speciesFile.setGffRepeatFile(null);
			break;
		}
		case refseqAllIsoRNA: {
			deleteRefseqAll();
			break;
		}
		case refseqOneIsoRNA: {
			deleteRefseqOne();
			break;
		}
		case refseqAllIsoPro: {
			deleteRefProAll();
			break;
		}
		case refseqOneIsoPro: {
			deleteRefProOne();
			break;
		}
		case refseqNCfile: {
			deleteRefNC();
			break;
		}
		default:
			break;
		}
	}
	
	private void deleteGff(String gffDb) {
		if (StringOperate.isRealNull(gffDb)) {
			return;
		}
		String gffFile = speciesFile.getGffFile(gffDb);
		if (FileOperate.DeleteFileFolder(gffFile)) {
			speciesFile.removeGffDB(gffDb);
			speciesFile.save();
        }
	}
	
	/**
	 * 删除所有Chromose相关的文件
	 * 不保存
	 */
	public void deleteChrom() {
		//删除染色体文件
		String chromeSeq = speciesFile.getChromSeqFile();
		if (FileOperate.isFileExistAndBigThan0(chromeSeq)) {
			FileOperate.DeleteFileFolder(chromeSeq);
			FileOperate.DeleteFileFolder(chromeSeq + ".fai");
			FileOperate.DeleteFileFolder(FileOperate.changeFileSuffix(chromeSeq, "", "fai"));
        }
		speciesFile.setChromSeq(null);
		speciesFile.save();
	}
	
	/** 删除所有Chromose相关的文件并保存 */
	public void deleteRefNC() {
		//删除染色体文件
		String refseqNc = speciesFile.getRefseqNCfile();
		if (FileOperate.isFileExistAndBigThan0(refseqNc)) {
			deleteRef(refseqNc);
        }
		speciesFile.setRefseqNCfile(null);
		speciesFile.save();
	}
	
	/** 删除所有refseq AllIso相关的文件并保存 */
	public void deleteRefseqAll() {
		deleteRefFile(true, false);
	}
	
	/** 删除所有refseq OneIso相关的文件并保存 */
	public void deleteRefseqOne() {
		deleteRefFile(false, false);
	}
	/** 删除所有refseq AllIso相关的文件并保存 */
	public void deleteRefProAll() {
		deleteRefFile(true, true);
	}
	/** 删除所有refseq OneIso相关的文件并保存 */
	public void deleteRefProOne() {
		deleteRefFile(false, true);
	}
	
	private void deleteRefFile(boolean isAllIso, boolean isProtein) {
		String refFileAll = speciesFile.getRefSeqFile(isAllIso, isProtein);
		deleteRef(refFileAll);
		speciesFile.setRefSeqFileName(null, isAllIso, isProtein);
		speciesFile.save();
	}
	
	/** 删除给定的refRNA或refProtein，以及相关的blast文件等 */
	private void deleteRef(String refFileName) {
		String refPath = FileOperate.getParentPathNameWithSep(refFileName);
		String refName = FileOperate.getFileNameSep(refFileName)[0];
		List<String> lsRefInfo = FileOperate.getFoldFileNameLs(refPath, refName, "*");
		for (String refInfo : lsRefInfo) {
			FileOperate.DeleteFileFolder(refInfo);
		}
	}
	
}