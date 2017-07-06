package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class Cuffcompare {
	//cuffcompare 的参数
	String ExePath = "";
	
	String seqFasta = "";
	String refGtfFile = "";
	List<String> lsInputGtfFile = new ArrayList<String>();
	String outPath = "";
	/** 是否删除一些未知文件 */
	boolean clearFile = false;
	
	public Cuffcompare() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		ExePath = softWareInfo.getExePathRun();
	}
	
	/** 是否删除结果中的一些未知文件 */
	public void setClearFile(boolean clearFile) {
		this.clearFile = clearFile;
	}
	public void setSeqFasta(String seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setRefGtfFile(String refGtfFile) {
		if (FileOperate.isFileExistAndBigThanSize(refGtfFile, 10)) {
			this.refGtfFile = refGtfFile;
		}
	}
	
	/**
	 * <b>直接添加GTF文件会出错</b><br>
	 * 添加的务必是从cufflinks或者cuffcompare所得到的GTF文件
	 * @param lsInputGtfFile
	 */
	public void setLsInputGtfFile(List<String> lsInputGtfFile) {
		this.lsInputGtfFile = lsInputGtfFile;
	}
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String[] getOutPath() {
		return new String[]{"-o", outPath};
	}
	public String[] getSeqFasta() {
		if (FileOperate.isFileExistAndNotDir(seqFasta)) {
			return new String[]{"-s", seqFasta};
		}
		return null;
	}
	
	/** 只有gtf文件，就是想生成cuffdiff的输入文件 */
	private String[] getRefGtfOnly() {
		clearFile = true;
		return new String[]{"-CG", "-r", refGtfFile, refGtfFile};
	}
	private String[] getRefGtf() {
		if (FileOperate.isFileExistAndBigThanSize(refGtfFile, 1)) {
			return new String[]{"-r", refGtfFile};
		}
		return null;
	}
	
	/**
	 * 获得修改的gtf文件名
	 */
	public String runCompareGtf() {
		List<String> lsCmd = new ArrayList<>();
		
		lsCmd.add(ExePath + "cuffcompare");
		ArrayOperate.addArrayToList(lsCmd, getSeqFasta());
		ArrayOperate.addArrayToList(lsCmd, getOutPath());
		
		
		if (lsInputGtfFile.size() == 0) {
			ArrayOperate.addArrayToList(lsCmd, getRefGtfOnly());
		} else {
			ArrayOperate.addArrayToList(lsCmd, getRefGtf());
			lsCmd.addAll(lsInputGtfFile);
		}
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		if (clearFile) {
			clearUnknownFile();
		}
		return changeGtfFileName();
	}
	
	/** 删除一些未知文件 */
	private void clearUnknownFile() {
		String prefix = "";
		if (!outPath.endsWith("/") && !outPath.endsWith("\\")) {
			prefix = FileOperate.getFileName(outPath);
		}
		
		String fileRefmap = FileOperate.changeFilePrefix(refGtfFile, prefix + ".", null) + ".refmap";
		String fileTmap = FileOperate.changeFilePrefix(refGtfFile, prefix + ".", null) + ".tmap";
		
		String fileLoci = outPath + ".loci";
		String fileStats = outPath + ".stats";
		String fileTracking = outPath + ".tracking";
		
		FileOperate.deleteFileFolder(fileRefmap);
		FileOperate.deleteFileFolder(fileTmap);
		FileOperate.deleteFileFolder(fileLoci);
		FileOperate.deleteFileFolder(fileStats);
		FileOperate.deleteFileFolder(fileTracking);
	}
	
	/**
	 * 修改cuffcompare产生的gtf文件名，同时返回修改后的文件名
	 * @return
	 */
	private String changeGtfFileName() {
		String fileName = outPath + ".combined.gtf";
		String newFileName = fileName;
		if (outPath.endsWith("/") || outPath.endsWith("\\")) {
			newFileName = outPath + "combined.gtf";
		}
		
		FileOperate.changeFileName(fileName, FileOperate.getFileName(newFileName), true);
		return newFileName;
	}

}
