package com.novelbio.analysis.seq.chipseq.peakcalling;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class PeakCallingMacs {
	private String pathInput;
	private String pathInputCol;
	private String outFileName;
	private int  tsize = 50;
	private double pvalue = 0.01;
	private long genomeLength;
	private int mfoldMin = 2;
	private int mfoldMax = 300;
	private FormatSeq FileType = FormatSeq.BED;
	
	public void setFileType(FormatSeq fileType) {
		FileType = fileType;
	}
	
	public void setMfoldMax(int mfoldMax) {
		this.mfoldMax = mfoldMax;
	}
	
	public void setMfoldMin(int mfoldMin) {
		this.mfoldMin = mfoldMin;
	}
	
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	
	public void setTsize(int tsize) {
		this.tsize = tsize;
	}
	
	public void setPathinputCol(String pathinputCol) {
		this.pathInputCol = pathinputCol;
	}
	
	public void setPathinput(String pathinput) {
		this.pathInput = pathinput;
	}
	
	public void setPathoutput(String pathoutput) {
		this.outFileName = pathoutput;
	}

	public void setGenomeLength(long genomeLength) {
		this.genomeLength = genomeLength;
	}
	/** 获得结果文件，如果结果文件不存在，就进行peakcalling*/
	public String getResultPeakFile() {
		String resultFile = FileOperate.changeFileSuffix(outFileName, "_peaks", "xls");
		if (!FileOperate.isFileExist(resultFile)) {
			runPeakCalling();
		}
		return resultFile;
	}
	/**
	 * 判定输入文件
	 */
	private String getFileType() {
		String result = "";
		if (FileType == FormatSeq.BAM) {
			result = "-f BAM"; 
		} else if (FileType == FormatSeq.BED) {
			result = "-f BED"; 
		} else if (FileType == FormatSeq.SAM) {
			result = "-f SAM"; 
		}
		return result;
	}
	private String getControl() {
		if (FileOperate.isFileExist(pathInputCol)) {
			return " -c " + CmdOperate.addQuot(pathInputCol);
		}
		return "";
	}
	private String getGenomeLength() {
		return " -g " +genomeLength;
	}
	private String getPathinput() {
		return " -t " + CmdOperate.addQuot(pathInput);
	}
	private String getPathoutput() {
		return " -n " + CmdOperate.addQuot(outFileName);
	}
	private String getPvalue() {
		return " -p " + pvalue;
	}
	private String getTsize() {
		return " -s " + tsize;
	}
	public String getMfoldMin() {
		return " -m " + mfoldMin;
	}
	public String getMfoldMax() {
		return "," + mfoldMax;
	}
	
	private String getCmd() {
		String cmd = "macs14 " + getFileType() + getGenomeLength() + getPathinput() + getControl()
				+ getPathoutput() + getPvalue() + getTsize() +  getMfoldMin() + getMfoldMax();
		return cmd;
	}
	
	public void runPeakCalling() {
		String cmd = getCmd();
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd, "macsPeakCalling");
		cmdOperate.run();
	}
	
	public void clear() {
		pathInput = null;
		pathInputCol = null;
		outFileName = null;
		tsize = 50;
		pvalue = 0.01;
		genomeLength = 0;
		mfoldMin = 2;
		mfoldMax = 300	;
		FileType = FormatSeq.BED;
	}
}
