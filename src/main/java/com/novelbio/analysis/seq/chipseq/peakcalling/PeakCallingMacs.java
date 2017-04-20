package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class PeakCallingMacs implements IntCmdSoft {
	private String pathInput;
	private String pathInputCol;
	private String outFileName;
	private int  tsize = 50;
	private double pvalue = 0.01;
	private long genomeLength;
	private int mfoldMin = 2;
	private int mfoldMax = 300;
	
	private boolean isNolambda = false;
	
	private FormatSeq FileType = FormatSeq.BED;
	
	/** 文件类型，只能是bed或者bam文件 */
	public void setFileType(FormatSeq fileType) {
		FileType = fileType;
	}
	
	/**
	 * 默认2<br><br>
	 *  Select the regions within MFOLD range of high- <br>
    confidence enrichment ratio against background to<br>
    build model. The regions must be lower than upper<br>
    limit, and higher than the lower limit. DEFAULT:10,30
    */
	public void setMfoldMax(int mfoldMax) {
		this.mfoldMax = mfoldMax;
	}
	
	/** 
	 * 默认300<br><br>
	 * Select the regions within MFOLD range of high-<br>
    confidence enrichment ratio against background to<br>
    build model. The regions must be lower than upper<br>
    limit, and higher than the lower limit. DEFAULT:10,30
    */
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
	
	/** 默认为false<br><br>
	 *  If True, MACS will use fixed background lambda as<br>
                        local lambda for every peak region. Normally, MACS<br>
                        calculates a dynamic local lambda to reflect the local<br>
                        bias due to potential chromatin structure.<br>

	 * @param isNolambda
	 */
	public void setIsNolambda(boolean isNolambda) {
		this.isNolambda = isNolambda;
	}
	
	/** 获得结果文件，如果结果文件不存在，就进行peakcalling*/
	public String getResultPeakFile() {
		return FileOperate.changeFileSuffix(outFileName, "peaks", "xls");
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
		if (FileOperate.isFileExistAndNotDir(pathInputCol)) {
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
	private String[] getMfold() {
		return new String[]{"-m", mfoldMin+"," + mfoldMax};
	}
	
	private String[] getNolambda() {
		if (isNolambda) {
			return new String[]{"--nolambda"};
		}
		return null;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.macs);
		String exePath = softWareInfo.getExePathRun();
		lsCmd.add(exePath + "macs14");
		ArrayOperate.addArrayToList(lsCmd, getFileType());
		ArrayOperate.addArrayToList(lsCmd, getGenomeLength());
		ArrayOperate.addArrayToList(lsCmd, getPathinput());
		ArrayOperate.addArrayToList(lsCmd, getControl());
		ArrayOperate.addArrayToList(lsCmd, getPathoutput());
		ArrayOperate.addArrayToList(lsCmd, getPvalue());
		ArrayOperate.addArrayToList(lsCmd, getTsize());
		ArrayOperate.addArrayToList(lsCmd, getMfold());
		ArrayOperate.addArrayToList(lsCmd, getNolambda());
		return lsCmd;
	}
	
	public void runPeakCalling() {
		String resultFile = getResultPeakFile();
		if (FileOperate.isFileExistAndBigThan0(resultFile)) {
			return;
		}
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.runWithExp();
		String resultFileTmp = FileOperate.changeFileSuffix(outFileName, "_peaks", "xls");
		FileOperate.moveFile(true, resultFileTmp, resultFile);
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
