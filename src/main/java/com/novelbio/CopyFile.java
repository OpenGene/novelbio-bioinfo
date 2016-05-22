package com.novelbio;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.curator.CuratorNBC;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class CopyFile {
	private static final Logger logger = Logger.getLogger(CopyFile.class);
	
	List<String> lsFileNeedToCopy = new ArrayList<String>();
	Set<String> setFileAlreadyCopied = new LinkedHashSet<String>();
	TxtReadandWrite txtWriteCopiedFile;
	/** 超过该时间修改的文件就不移动，-1表示全部移动 */
	long time = -1;
	TxtReadandWrite txtWriteCannotCopy;
	public static void main(String[] args) {
//		String[] ss = new String[]{"/media/hdfs/nbCloud/public/nbcplatform/copy/needCopy1.txt", "2014-12-21"};
//		main2(ss);
		
		CuratorNBC curatorNBC = new CuratorNBC();
		InterProcessMutex mutex = curatorNBC.getInterProcessMutex("test");
		try {
			mutex.acquire();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("test");
		try {
			mutex.release();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	
	public static void main2(String[] args) {
		String inputFile = args[0];
		String date = null;
		if (args.length > 1 && args[1] != null) {
			date = args[1];
		}
		
		String fileCopied = FileOperate.changeFileSuffix(inputFile, "_copied", null);
		CopyFile copyFile = new CopyFile();
		copyFile.readFileNeedToCopy(inputFile);
		copyFile.readFileAlreadyCopied(fileCopied);
		copyFile.setCannotCopy(FileOperate.changeFileSuffix(inputFile, "_cannotCopy", null));
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
		FileOperate.deleteFileFolder(fileCopied);
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		txtWriteCopiedFile = new TxtReadandWrite(fileCopied, true);
		for (String string : setFileAlreadyCopied) {
			txtWriteCopiedFile.writefileln(string);
		}
		txtWriteCopiedFile.flush();
	}
	
	/** 将无法拷贝的文件写入该文本中 */
	public void setCannotCopy(String fileName) {
		txtWriteCannotCopy = new TxtReadandWrite(fileName, true);
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
		txtWriteCannotCopy.close();
	}
	
	private void copyFile(File file) {
		String fileName = file.getAbsolutePath();
		if (setFileAlreadyCopied.contains(fileName)) {
			return;
		}
		logger.info("copy filePath:" + fileName);
		if (FileOperate.isFileDirectory(file)) {
			List<File> lsSubFile = FileOperate.getFoldFileLs(file);
			for (File fileSub : lsSubFile) {
				copyFile(fileSub);
			}
		} else {
			if (time > 0 && FileOperate.getTimeLastModify(file) > time) {
				return;
			}
			String fileNew = FileHadoop.convertToLocalPath(fileName);
			if (fileName.endsWith("Zone.Identifier")) {
				return;
			}
			FileOperate.createFolders(FileOperate.getPathName(fileNew));
			try {
				FileOperate.copyFile(fileName, fileNew, true);
				txtWriteCopiedFile.writefileln(fileName);
				txtWriteCopiedFile.flush();
			} catch (Exception e) {
				txtWriteCannotCopy.writefileln("cannot copy: " + fileName);
				txtWriteCannotCopy.flush();
            }
		}
	}
	
}
