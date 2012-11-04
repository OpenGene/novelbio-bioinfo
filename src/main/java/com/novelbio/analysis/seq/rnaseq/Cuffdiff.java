package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Cuffdiff {
	public static void main(String[] args) {
		Cuffdiff cuffdiff = new Cuffdiff();
		Species species = new Species(9606);
		cuffdiff.setGtfFile("/media/winF/NBC/Project/Project_FY/20120920/cufflinks/finalTranscript.gtf");
		cuffdiff.setSeqFasta(species.getChromFaPath());
		cuffdiff.setOutPath("/media/winF/NBC/Project/Project_FY/20120920/cufflinks/");
		cuffdiff.getModifiedGtf();
	}
	//cuffcompare 的参数
	String ExePathCuffCompare = "";
	
	String seqFasta = "";
	String gtfFile = "";
	
	String outPath = "";
	/** 是否删除一些未知文件 */
	boolean clearFile = false;
	
	//cuffdiff的参数
	String exePathCuffDiff = "";
	int threadNum = 4;
	/** 
	 * 装载输入样本Sam的文件
	 * 每个子类listSam表示一系列的重复
	 *  */
	ArrayList<ArrayList<String>> lsSampleSamFile;
	/** 样本名 */
	ArrayList<String> lsSampleName;
	
	/** 是否删除一些未知文件 */
	public void setClearFile(boolean clearFile) {
		this.clearFile = clearFile;
	}
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePathCuffCompare(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePathCuffCompare = "";
		} else {
			this.ExePathCuffCompare = FileOperate.addSep(ExePathCuffCompare);
		}
	}
	
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePathCuffDiff(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePathCuffCompare = "";
		} else {
			this.ExePathCuffCompare = FileOperate.addSep(ExePathCuffCompare);
		}
	}
	
	public void setSeqFasta(String seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getOutPath() {
		return " -o " + outPath;
	}
	private String getInputGtf() {
		return " -r " + CmdOperate.addQuot(gtfFile) + " " + CmdOperate.addQuot(gtfFile);
	}
	public String getSeqFasta() {
		return " -s " + CmdOperate.addQuot(seqFasta);
	}
	
	/**
	 * 获得修改的gtf文件名
	 */
	private String getModifiedGtf() {
		String cmd = ExePathCuffCompare + "cuffcompare " + getSeqFasta() + getOutPath() + " -CG " + getInputGtf();
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
