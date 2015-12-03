package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * simple script : ESTScan -m 100 -d 50 -i 50 -M at.smat -p 4 -N 0 -w 60 -s 1 All-Unigene,final.fa -t All-Unigene.final.pep > All-Unigene.final.cds
 * @author bll
 *
 */

public class ESTScanCDSPredict implements IntCmdSoft {

	String inputFile;
	String exePath = "";	
	
	//矩阵分值文件，最小阈值，默认100；
	int minMatrixValue = 100;
	//score matrices 文件
	String scoreMatFile;
	//最短长度
	int minCDSLength = 50;
	int posStrand = 0;
	int skipMinLen = 10;
	
	String outputPrefix;
	
	public ESTScanCDSPredict() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.estscan);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public void setMinMatrixValue(int minMatrixValue) {
		if (minMatrixValue > 0) {
			this.minMatrixValue = minMatrixValue;
		}
	}
	
	public void setScoreMatFile(String scoreMatFile) {
		FileOperate.checkFileExistAndBigThanSize(scoreMatFile, 0);
		this.scoreMatFile = scoreMatFile;
	}
	
	public void setMinCDSLength(int minCDSLength) {
		if (minCDSLength > 0) {
			this.minCDSLength = minCDSLength;
		}
	}
	
	public void setPosStrand(int posStrand) {
		if (posStrand > 0) {
			this.posStrand = posStrand;
		}
	}
	
	public void setSkipMinLen(int skipMinLen) {
		if (skipMinLen > 0) {
			this.skipMinLen = skipMinLen;
		}
	}
	
	/** 设定输出文件夹和前缀<br> 
	 * 例如：/home/novelbio/test
	 * @param outputDir
	 */
	public void setOutDirPrefix(String outputDir) {
		this.outputPrefix = outputDir;
	}
	
	/** 获得输出的蛋白序列文件 */
	public String getPepResultFile() {
		if (outputPrefix.endsWith("\\") || outputPrefix.endsWith("/")) {
			return outputPrefix + "pep.fa";
		} else {
			return outputPrefix + "_pep.fa";
		}
		
	}
	/** 获得输出的cds序列文件 */
	public String getCdsResultFile() {
		if (outputPrefix.endsWith("\\") || outputPrefix.endsWith("/")) {
			return outputPrefix + "cds.fa";
		} else {
			return outputPrefix + "_cds.fa";
		}
	}
	
	public void run() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);	
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(getPepResultFile());	
		cmdOperate.setStdOutPath(getCdsResultFile(), true, false);
		cmdOperate.runWithExp("ESTScan error:");
	}

	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("perl");
		lsCmd.add(exePath + "ESTScan");
		lsCmd.add(inputFile);
		ArrayOperate.addArrayToList(lsCmd, getMinMatrixValue());
		ArrayOperate.addArrayToList(lsCmd, getScoreMatFile());
		ArrayOperate.addArrayToList(lsCmd, getMinCDSLength());
		ArrayOperate.addArrayToList(lsCmd, getPosStrand());
		ArrayOperate.addArrayToList(lsCmd, getSkipMinLen());
		ArrayOperate.addArrayToList(lsCmd, getPepFile());
		ArrayOperate.addArrayToList(lsCmd, getCdsFile());
		return lsCmd;
	}
	
	private String[] getMinMatrixValue() {
		return new String[] {"-m", minMatrixValue + ""};
	}
	
	private String[] getScoreMatFile() {
		return new String[] {"-M", scoreMatFile};
	}
	
	private String[] getMinCDSLength() {
		return new String[] {"-l", minCDSLength + ""};
	}
	
	private String[] getPosStrand() {
		return new String[] {"-S", posStrand + ""};
	}
	
	private String[] getSkipMinLen() {
		return new String[] {"-s", skipMinLen + ""};
	}
	
	private String[] getPepFile() {
		return new String[] {"-t", getPepResultFile()};
	}
	
	private String[] getCdsFile() {
		return new String[] {">", getCdsResultFile()};
	}
	
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
}
