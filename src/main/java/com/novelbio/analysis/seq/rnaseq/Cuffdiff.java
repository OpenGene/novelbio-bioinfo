package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import net.sf.picard.sam.SamAlignmentMerger;

import com.novelbio.analysis.seq.mapping.MapLibrary;
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
	
	String seqFastaPath = "";
	String gtfFile = "";
	
	String outPath = "";
	/** 是否删除一些未知文件 */
	boolean clearFile = false;
	
	//cuffdiff的参数
	String exePath = "";
	int threadNum = 4;
	/** 
	 * 装载输入样本Sam的文件
	 * 每个子类listSam表示一系列的重复
	 *  */
	ArrayList<ArrayList<String>> lsSample2lsSamFile;
	/** 样本名 */
	ArrayList<String> lsSampleName;
	
	MapLibrary mapLibrary;
	
	/** 是否删除一些未知文件 */
	public void setClearFile(boolean clearFile) {
		this.clearFile = clearFile;
	}
	
	/**
	 * 设定cuffdiff所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.exePath = "";
		} else {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	
	public void setSeqFasta(String seqFastaPath) {
		this.seqFastaPath = seqFastaPath;
	}
	/** 必须是cuffcompare处理过的gtf文件 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getThreadNum() {
		return " -p " + threadNum + " ";
	}
	
	private String getPathAndOtherParam() {
		return " -N -b " + seqFastaPath + " ";
	}

	private String getMapLibrary() {
		//TODO 考虑返回建库方式
		return "";
	}
	
	private String getGtfFile() {
		return CmdOperate.addQuot(gtfFile);
	}
	/** 获得样本名称 */
	private String getSampleName() {
		String out = " -L " + CmdOperate.addQuot(lsSampleName.get(0));
		for (int i = 1; i < lsSampleName.size(); i++) {
			out = out + "," + CmdOperate.addQuot(lsSampleName.get(i));
		}
		return out;
	}
	
	private String getSamleFile() {
		String sampleFile = "";
		for (ArrayList<String> lsFile : lsSample2lsSamFile) {
			sampleFile = sampleFile + " " + CmdOperate.addQuot(lsFile.get(0));
			for (int i = 1; i < lsFile.size(); i++) {
				sampleFile = sampleFile + "," + CmdOperate.addQuot(lsFile.get(i));
			}
		}
		return sampleFile;
	}
	/**
	 * 获得修改的gtf文件名
	 */
	public String runCompareGtf() {
		String cmd = exePath + "cuffdiff " + getMapLibrary() + getPathAndOtherParam() + getThreadNum() + getSampleName();
		cmd = cmd + getGtfFile() + getSamleFile();
		CmdOperate cmdOperate = new CmdOperate(cmd, "cuffdiff");
		cmdOperate.run();
		//TODO 获得返回的结果
	}
}
