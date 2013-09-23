package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class PeakCallingMacs implements IntCmdSoft {
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
	private String[] getFileType() {
		String[] result = new String[2];
		result[0] = "-f";
		if (FileType == FormatSeq.BAM) {
			result[1] = "BAM"; 
		} else if (FileType == FormatSeq.BED) {
			result[1] = "BED";
		} else if (FileType == FormatSeq.SAM) {
			result[1] = "SAM";
		} else {
			result = null;
		}
		return result;
	}
	private String[] getControl() {
		if (FileOperate.isFileExist(pathInputCol)) {
			return new String[]{"-c", pathInputCol};
		}
		return null;
	}
	private String[] getGenomeLength() {
		return new String[]{"-g", genomeLength + ""};
	}
	private String[] getPathinput() {
		return new String[]{"-t", pathInput};
	}
	private String[] getPathoutput() {
		return new String[]{"-n", outFileName};
	}
	private String[] getPvalue() {
		return new String[]{"-p", pvalue+""};
	}
	private String[] getTsize() {
		return new String[]{"-s", tsize+""};
	}
	public String[] getMfold() {
		return new String[]{"-m", mfoldMin+"," + mfoldMax};
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("macs14");
		ArrayOperate.addArrayToList(lsCmd, getFileType());
		ArrayOperate.addArrayToList(lsCmd, getGenomeLength());
		ArrayOperate.addArrayToList(lsCmd, getPathinput());
		ArrayOperate.addArrayToList(lsCmd, getControl());
		ArrayOperate.addArrayToList(lsCmd, getPathoutput());
		ArrayOperate.addArrayToList(lsCmd, getPvalue());
		ArrayOperate.addArrayToList(lsCmd, getTsize());
		ArrayOperate.addArrayToList(lsCmd, getMfold());
		return lsCmd;
	}
	
	public void runPeakCalling() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
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

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
	
}
