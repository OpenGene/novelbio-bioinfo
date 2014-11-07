package com.novelbio.database.model.species;

import java.io.IOException;
import java.io.InputStream;

import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.EnumSpeciesFile;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.model.species.Species.EnumSpeciesType;

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
	
	public void upload() throws IOException, SpeciesUploadException {
		if (speciesFileType == EnumSpeciesFile.rrnaFile) {
			uploadRrnaFile();
		} else {
			uploadSpeciesFile();
		}
	}
	
	private void uploadSpeciesFile() throws IOException, SpeciesUploadException {
		SpeciesFile speciesFile = SpeciesFile.findById(speciesFileId);
		String savePath = speciesFileType.getSavePath(taxId, speciesFile);
		if (StringOperate.isRealNull(savePath)) {
			throw new SpeciesUploadException("保存路径错误，请检查");
		}
		
		String newFileName = FileOperate.addSep(savePath) + fileName;
		String newFileTmp = FileOperate.changeFileSuffix(newFileName, DateUtil.getDateAndRandom(), null);
		try {
			FileOperate.uploadFile(inputStream, newFileTmp, false, fileSize);
		} catch (IOException e) {
			FileOperate.DeleteFileFolder(newFileTmp);
			throw e;
		}
		speciesFile.deleteFile(speciesFileType, gffDB);
		if (!FileOperate.moveFile(true, newFileTmp, newFileName)) {
			throw new SpeciesUploadException("保存出错");
		}
		speciesFile.addPathInfo(speciesFileType, fileName, gffType, gffDB);
		speciesFile.save();
	}
	
	private void uploadRrnaFile() throws IOException, SpeciesUploadException {
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(taxId);
		String savePath = speciesFileType.getSavePath(taxId, speciesFile);
		if (StringOperate.isRealNull(savePath)) {
			throw new SpeciesUploadException("保存路径错误，请检查");
		}
		
		String newFileName = FileOperate.addSep(savePath) + fileName;
		String newFileTmp = FileOperate.changeFileSuffix(newFileName, DateUtil.getDateAndRandom(), null);
		try {
			FileOperate.uploadFile(inputStream, newFileTmp, false, fileSize);
		} catch (IOException e) {
			FileOperate.DeleteFileFolder(newFileTmp);
			throw e;
		}
		if (!FileOperate.moveFile(true, newFileTmp, newFileName)) {
			throw new SpeciesUploadException("保存出错");
		}
		TaxInfo taxInfo = TaxInfo.findByTaxID(taxId);
		String oldFile = taxInfo.getRrnaFileWithPath();
		FileOperate.DeleteFileFolder(oldFile);
		taxInfo.setRrnaFile(fileName);
		taxInfo.save();
	}
	
	public static class SpeciesUploadException extends Exception {
		public SpeciesUploadException(String msg) {
			super(msg);
		}
	}
}