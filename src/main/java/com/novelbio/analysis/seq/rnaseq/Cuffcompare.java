package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Cuffcompare {
	//cuffcompare 的参数
	String ExePath = "";
	
	String seqFasta = "";
	String refGtfFile = "";
	ArrayList<String> lsInputGtfFile = new ArrayList<String>();
	String outPath = "";
	/** 是否删除一些未知文件 */
	boolean clearFile = false;
	
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePathCuff(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(ExePath);
		}
	}

	public void setSeqFasta(String seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setRefGtfFile(String refGtfFile) {
		this.refGtfFile = refGtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getOutPath() {
		return " -o " + outPath;
	}
	public String getSeqFasta() {
		return " -s " + CmdOperate.addQuot(seqFasta);
	}
	
	/** 只有gtf文件，就是想生成cuffdiff的输入文件 */
	private String getRefGtfOnly() {
		clearFile = true;
		return " -CG -r " + CmdOperate.addQuot(refGtfFile) + " " + CmdOperate.addQuot(refGtfFile);
	}
	private String getRefGtf() {
		return " -r " + CmdOperate.addQuot(refGtfFile) + " ";
	}
	
	private String getLsInputGtf() {
		String out = CmdOperate.addQuot(lsInputGtfFile.get(0));
		for (int i = 1; i < lsInputGtfFile.size(); i++) {
			out = out + "," + CmdOperate.addQuot(lsInputGtfFile.get(i));
		}
		return " " + out;
	}
	
	/**
	 * 获得修改的gtf文件名
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
	
	/** 删除一些未知文件 */
	private void clearUnknownFile() {
		String fileRefmap = outPath + ".finalTranscript.gtf.refmap";
		String fileTmap = outPath + ".finalTranscript.gtf.tmap";
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
	 * 修改cuffcompare产生的gtf文件名，同时返回修改后的文件名
	 * @return
	 */
	private String changeGtfFileName() {
		String fileName = outPath + ".combined.gtf";
		String newFileName = FileOperate.changeFileSuffix(fileName, "", ".gtf");
		FileOperate.changeFileName(fileName, outPath + ".gtf", true);
		return newFileName;
	}

}
