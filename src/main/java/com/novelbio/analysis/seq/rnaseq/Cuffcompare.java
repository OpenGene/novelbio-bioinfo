package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Cuffcompare {
	//cuffcompare �Ĳ���
	String ExePath = "";
	
	String seqFasta = "";
	String refGtfFile = "";
	ArrayList<String> lsInputGtfFile = new ArrayList<String>();
	String outPath = "";
	/** �Ƿ�ɾ��һЩδ֪�ļ� */
	boolean clearFile = false;
	
	/**
	 * �趨samtools���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(ExePath);
		}
	}
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
	public void setLsInputGtfFile(ArrayList<String> lsInputGtfFile) {
		this.lsInputGtfFile = lsInputGtfFile;
	}
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getOutPath() {
		return " -o " + outPath;
	}
	public String getSeqFasta() {
		if (FileOperate.isFileExist(seqFasta)) {
			return " -s " + CmdOperate.addQuot(seqFasta);
		}
		return " ";
	}
	
	/** ֻ��gtf�ļ�������������cuffdiff�������ļ� */
	private String getRefGtfOnly() {
		clearFile = true;
		return " -CG -r " + CmdOperate.addQuot(refGtfFile) + " " + CmdOperate.addQuot(refGtfFile);
	}
	private String getRefGtf() {
		if (FileOperate.isFileExistAndBigThanSize(refGtfFile, 1)) {
			return " -r " + CmdOperate.addQuot(refGtfFile) + " ";
		}
		return " ";
	}
	
	private String getLsInputGtf() {
		String out = CmdOperate.addQuot(lsInputGtfFile.get(0));
		for (int i = 1; i < lsInputGtfFile.size(); i++) {
			out = out + "," + CmdOperate.addQuot(lsInputGtfFile.get(i));
		}
		return " " + out;
	}
	
	/**
	 * ����޸ĵ�gtf�ļ���
	 */
	public String runCompareGtf() {
		String cmd = ExePath + "cuffcompare " + getSeqFasta() + getOutPath();
		if (lsInputGtfFile.size() == 0) {
			cmd = cmd + getRefGtfOnly();
		} else {
			cmd = cmd + getRefGtf() + getLsInputGtf();
		}
		CmdOperate cmdOperate = new CmdOperate(cmd, "cuffcompare");
		cmdOperate.run();
		if (clearFile) {
			clearUnknownFile();
		}
		return changeGtfFileName();
	}
	
	/** ɾ��һЩδ֪�ļ� */
	private void clearUnknownFile() {
		String fileRefmap = outPath + ".finalTranscript.gtf.refmap";
		String fileTmap = outPath + ".finalTranscript.gtf.tmap";
		if (!FileOperate.isFileExist(fileRefmap)) {
			fileRefmap = FileOperate.getParentPathName( FileOperate.getParentPathName(fileRefmap)) + ".finalTranscript.gtf.refmap";
			fileTmap = FileOperate.getParentPathName( FileOperate.getParentPathName(fileRefmap)) + ".finalTranscript.gtf.tmap";
		}
		
		String fileLoci = outPath + ".loci";
		String fileStats = outPath + ".stats";
		String fileTracking = outPath + ".tracking";
		
		FileOperate.delFile(fileRefmap);
		FileOperate.delFile(fileTmap);
		FileOperate.delFile(fileLoci);
		FileOperate.delFile(fileStats);
		FileOperate.delFile(fileTracking);
	}
	
	/**
	 * �޸�cuffcompare������gtf�ļ�����ͬʱ�����޸ĺ���ļ���
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
