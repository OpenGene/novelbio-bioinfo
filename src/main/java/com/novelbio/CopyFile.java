package com.novelbio;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class CopyFile {
	List<String> lsFileNeedToCopy = new ArrayList<String>();
	Set<String> setFileAlreadyCopied = new LinkedHashSet<String>();
	TxtReadandWrite txtWriteCopiedFile;
	/** 超过该时间修改的文件就不移动，-1表示全部移动 */
	long time = -1;
	
	public static void main(String[] args) {
		String inputFile = args[0];
		String date = null;
		if (args.length > 1 && args[1] != null) {
			date = args[1];
		}
		
		String fileCopied = FileOperate.changeFileSuffix(inputFile, "_copied", null);
		CopyFile copyFile = new CopyFile();
		copyFile.readFileNeedToCopy(inputFile);
		copyFile.readFileAlreadyCopied(fileCopied);
		copyFile.setTimeCutoff(date, "yyyy-MM-dd");
		copyFile.copyFile();
		
	}
	
	public void readFileNeedToCopy(String fileNeedToMove) {
		TxtReadandWrite txtReadInput = new TxtReadandWrite(fileNeedToMove);
		for (String string : txtReadInput.readlines()) {
			string = string.trim();
			if (string.startsWith("#") || string.equals("")) continue;	
			lsFileNeedToCopy.add(string);
		}
		txtReadInput.close();
	}
	
	public void readFileAlreadyCopied(String fileCopied) {
		if (FileOperate.isFileExist(fileCopied)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(fileCopied);
			for (String string : txtRead.readlines()) {
				setFileAlreadyCopied.add(string);
			}
			txtRead.close();
		}
		FileOperate.DeleteFileFolder(fileCopied);
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		txtWriteCopiedFile = new TxtReadandWrite(fileCopied, true);
		for (String string : setFileAlreadyCopied) {
			txtWriteCopiedFile.writefileln(string);
		}
		txtWriteCopiedFile.flush();
	}
	
	public void setTimeCutoff(String date, String pattern) {
		if (StringOperate.isRealNull(date)) {
			return;
		}
		time = DateUtil.string2DateLong(date, "yyyy-MM-dd");
	}
	
	public void copyFile() {
		for (String string : lsFileNeedToCopy) {
			File file = FileOperate.getFile(string);
			copyFile(file);
		}
		txtWriteCopiedFile.close();
	}
	
	private void copyFile(File file) {
		String fileName = file.getAbsolutePath();
		if (setFileAlreadyCopied.contains(fileName)) {
			return;
		}
		if (FileOperate.isFileDirectory(file)) {
			List<File> lsSubFile = FileOperate.getFoldFileLs(file);
			for (File fileSub : lsSubFile) {
				copyFile(fileSub);
			}
		} else {
			if (time > 0 && FileOperate.getTimeLastModify(file) > time) {
				return;
			}
			String fileNew = fileName.replace(FileHadoop.getHdfsSymbol(), "/media/nbfs");
			FileOperate.createFolders(FileOperate.getPathName(fileNew));
			boolean isSucess = FileOperate.copyFile(fileName, fileNew, true);
			if (isSucess) {
				txtWriteCopiedFile.writefileln(fileName);
				txtWriteCopiedFile.flush();
			}
		}
	}
	
}
