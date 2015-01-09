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
	
	//输入文件：需要进行CDS预测的序列文件，fasta格式文件
	List<String> lsInputFile;
	String inputFile;
	String exePath = "";	
	List<String> lsInputFilePre;
	
	//矩阵分值文件，最小阈值，默认100；
	int minMatrixValue = 100;
	//score matrices 文件
	String scoreMatFile;
	//最短长度
	int minCDSLength = 50;
	int posStrand = 0;
	int skipMinLen = 10;
	
	String outputDir;
	//翻译的蛋白序列文件
	String pepFile;
	//输出结果文件
	String cdsResultFile;
	
	public ESTScanCDSPredict() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.estscan);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	public void setLstInputFile(List<String> lsInputFile) {
		this.lsInputFile = lsInputFile;
	}
	
	public void setLstInputFilePre(List<String> lsInputFilePre) {
		this.lsInputFilePre = lsInputFilePre;
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
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public void setPepFile(String pepFile) {
		this.pepFile = pepFile;
	}
	
	public void setCdsResultFile(String cdsResultFile) {
		this.cdsResultFile = cdsResultFile;
	}
	public void run() {
		for (int i = 0; i < lsInputFile.size(); i++) {
			setInputFile(lsInputFile.get(i));
			setPepFile(outputDir + lsInputFilePre.get(i) + "_pep.fa");
			setCdsResultFile(outputDir + lsInputFilePre.get(i) + "_cds.fa");
			running();
		}
	}
	public void running() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);	
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(pepFile);	
//		cmdOperate.addCmdParamOutput(cdsResultFile);	
		cmdOperate.runWithExp("ESTScan error:");
	}

	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("perl");
		lsCmd.add(exePath + "ESTScan");
		ArrayOperate.addArrayToList(lsCmd, getInputFile(inputFile));
		ArrayOperate.addArrayToList(lsCmd, getMinMatrixValue());
		ArrayOperate.addArrayToList(lsCmd, getScoreMatFile());
		ArrayOperate.addArrayToList(lsCmd, getMinCDSLength());
		ArrayOperate.addArrayToList(lsCmd, getPosStrand());
		ArrayOperate.addArrayToList(lsCmd, getSkipMinLen());
		ArrayOperate.addArrayToList(lsCmd, getPepFile());
		ArrayOperate.addArrayToList(lsCmd, getCdsResultFile());
		System.out.println(lsCmd);
		return lsCmd;
	}
	
	private String[] getInputFile(String inputFile) {
		return new String[]{inputFile};
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
		return new String[] {"-t", pepFile};
	}
	
	private String[] getCdsResultFile() {
		return new String[] {">", cdsResultFile};
	}
	
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
}
