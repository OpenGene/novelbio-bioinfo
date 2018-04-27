package com.novelbio.database.domain.species;

import java.io.IOException;
import java.io.InputStream;

import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.model.species.TaxInfo;
import com.novelbio.database.service.servgeneanno.ManageSpecies;

/**
 * 上传species文件的类，不考虑并发问题
 * @author zong0jie
 *
 */
public class SpeciesFileUpload {
	String speciesFileId;
	int taxId;
	EnumSpeciesFile speciesFileType;
	/** 文件名，相对路径 */
	String fileName;
	long fileSize;
	GffType gffType;
	String gffDB;
	InputStream inputStream;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public void setGffDB(String gffDB) {
		this.gffDB = gffDB;
	}
	public void setGffType(GffType gffType) {
		this.gffType = gffType;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public void setSpeciesFileId(String speciesFileId) {
		this.speciesFileId = speciesFileId;
	}
	public void setSpeciesFileType(EnumSpeciesFile speciesFileType) {
		this.speciesFileType = speciesFileType;
	}
	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}
	
	public void upload() throws IOException, ExceptionNbcSpeciesUpload {
		if (speciesFileType == EnumSpeciesFile.rrnaFile) {
			uploadRrnaFile();
		} else {
			uploadSpeciesFile();
		}
	}
	
	private void uploadSpeciesFile() throws IOException, ExceptionNbcSpeciesUpload {
		SpeciesFile speciesFile = SpeciesFile.findById(speciesFileId);
		String savePath = speciesFileType.getSavePath(taxId, speciesFile);
		if (StringOperate.isRealNull(savePath)) {
			throw new ExceptionNbcSpeciesUpload("保存路径错误，请检查");
		}
		
		if (speciesFileType == EnumSpeciesFile.chromSeqFile 
				|| speciesFileType == EnumSpeciesFile.refseqAllIsoPro 
				|| speciesFileType == EnumSpeciesFile.refseqAllIsoRNA
				|| speciesFileType == EnumSpeciesFile.refseqNCfile
				|| speciesFileType == EnumSpeciesFile.refseqOneIsoPro
				|| speciesFileType == EnumSpeciesFile.refseqOneIsoRNA
				) {
			if (!fileName.endsWith("fa")) {
				fileName = FileOperate.changeFileSuffix(fileName, null, "fa");
			}
		}
		String newFileName = FileOperate.addSep(savePath) + fileName;
		String newFileTmp = FileOperate.changeFileSuffix(newFileName, DateUtil.getDateAndRandom(), null) + ".tmp.upload";
		try {
			FileOperate.uploadFile(inputStream, newFileTmp, false, fileSize);
		} catch (IOException e) {
			FileOperate.deleteFileFolder(newFileTmp);
			throw e;
		}
		SpeciesFileDelete speciesFileDelete = new SpeciesFileDelete(speciesFile);
		if (speciesFileType == EnumSpeciesFile.gffGeneFile) {
			speciesFileDelete.deleteGffFile(gffDB);
		} else {
			speciesFileDelete.deleteFile(speciesFileType);
		}
		FileOperate.moveFile(true, newFileTmp, newFileName);
		speciesFile.addPathInfo(speciesFileType, fileName, gffType, gffDB);
		speciesFile.save();
	}
	
	private void uploadRrnaFile() throws IOException, ExceptionNbcSpeciesUpload {
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(taxId);
		String savePath = speciesFileType.getSavePath(taxId, speciesFile);
		if (StringOperate.isRealNull(savePath)) {
			throw new ExceptionNbcSpeciesUpload("保存路径错误，请检查");
		}
		
		fileName = FileOperate.changeFileSuffix(fileName, null, "fa");
		String newFileName = FileOperate.addSep(savePath) + fileName;
		String newFileTmp = FileOperate.changeFileSuffix(newFileName, DateUtil.getDateAndRandom(), null);
		try {
			FileOperate.uploadFile(inputStream, newFileTmp, false, fileSize);
		} catch (IOException e) {
			FileOperate.deleteFileFolder(newFileTmp);
			throw e;
		}
		TaxInfo taxInfo = ManageSpecies.getInstance().queryTaxInfo(taxId);
		if(!StringOperate.isRealNull(taxInfo.getRrnaFile())) {
			// 如果历史文件存在，才进行删除
			String oldFileName = FileOperate.addSep(savePath) + taxInfo.getRrnaFile();
			FileOperate.deleteFileFolder(oldFileName);
		}
		
		FileOperate.moveFile(true, newFileTmp, newFileName);
		taxInfo.setRrnaFile(fileName);
		ManageSpecies.getInstance().saveTaxInfo(taxInfo);
	}
	
	public static class ExceptionNbcSpeciesUpload extends Exception {
		private static final long serialVersionUID = -1647863177047730575L;

		public ExceptionNbcSpeciesUpload(String msg) {
			super(msg);
		}
	}
}
