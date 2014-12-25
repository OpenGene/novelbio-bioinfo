package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.ssh2.log.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class VarScanSomatic implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(VarScanSomatic.class);
	
	String exePath = "";
	String varScanCom = "";
	
	/** 输入文件*/
	String inputFile;
	/** Minimum read depth at a position to make a call [8]*/
	
	/** 输入Con文件*/
	List<String> lsConFile;
	/** 输入Con文件名称*/
	List<String> lsConPrefix;
	/** 输入Tum文件*/
	List<String> lsTumFile;
	/** 输入Tum文件名称*/
	List<String> lsTumPrefix;
	int minCoverage;
	/** Minimum coverage in normal to call somatic [6] */
	int minCovNor;
	/** Minimum coverage in tumor to call somatic [6] */
	int minCovTum;
	/** P-value threshold to call a somatic site [0.05] */
	double somaPValue;
	/** Minimum variant allele frequency threshold [0.01]*/
	double minVarFreq;
	/** Default p-value threshold for calling variants [0.99]*/
	double pValue;
	/** Minimum frequency to call homozygote [0.75]*/
	double minFreqForHom;
	/** output directory*/
	String outputDir;
	/** If set to 1, outputs in VCF format */
	int outputVcf;
	/** Output file for SNP calls [default: output.snp] */
	String outputSnp;
	/** Output file for indel calls [default: output.indel] */
	String outputIndel;
	/** Minimum coverage in normal to call somatic [8] */
	
	public VarScanSomatic() {
		 SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.varscan);
		 this.exePath = softWareInfo.getExePathRun();
	}
	
	public void setVarScanCom(String varScanCom) {
		this.varScanCom = varScanCom;
	}
	
	public void setLsConFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public void setLsConFile(List<String> lsConFile) {
		this.lsConFile = lsConFile;
	}
	public void setLsConPrefix(List<String> lsConPrefix) {
		this.lsConPrefix = lsConPrefix;
	}
	public void setLsTumFile(List<String> lsTumFile) {
		this.lsTumFile = lsTumFile;
	}
	public void setLsTumPrefix(List<String> lsTumPrefix) {
		this.lsTumPrefix = lsTumPrefix;
	}
	public void setMinCoverage(int minCoverage) {
		this.minCoverage = minCoverage;
	}

	public void setMinVarFreq(double minVarFreq) {
		this.minVarFreq = minVarFreq;
	}

	public void setPValue(double pValue) {
		this.pValue = pValue;
	}

	public void setMinFreqForHom(double minFreqForHom) {
		this.minFreqForHom = minFreqForHom;
	}
	
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public void setOutoutVcf(int outputVcf) {
		this.outputVcf = outputVcf;
	}

	public void setMinCovNor(int minCovNor) {
		this.minCovNor = minCovNor;
	}

	public void setMinCovTum(int minCovTum) {
		this.minCovTum = minCovTum;
	}

	public void setOutputIndel(String outputIndel) {
		this.outputIndel = outputIndel;
	}

	public void setOutputSnp(String outputSnp) {
		this.outputSnp = outputSnp;
	}

	public void setSomaPValue(double somaPValue) {
		this.somaPValue = somaPValue;
	}

	public void run() {
		for (int i = 0; i < lsConFile.size(); i++) {
			setLsConFile(lsConFile.get(i) + " " + lsTumFile.get(i));
			setOutputSnp(outputDir + lsConPrefix.get(i) + "VS" + lsTumPrefix.get(i) + "_snp.txt");
			setOutputIndel(outputDir + lsConPrefix.get(i) + "VS" + lsTumPrefix.get(i) + "_indel.txt");
			running();
		}
	}
	public void running() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.set(false);
		cmdOperate.run();
	}
	
	public List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java");
		lsCmd.add("-Xmx10g");
		lsCmd.add("-jar");
		lsCmd.add(exePath + "varscan.jar");
		lsCmd.add("somatic");
		ArrayOperate.addArrayToList(lsCmd, getInputFile(inputFile));
		ArrayOperate.addArrayToList(lsCmd, getMinCoverage());
		ArrayOperate.addArrayToList(lsCmd, getMinCovNor());
		ArrayOperate.addArrayToList(lsCmd, getMinCovTum());
		ArrayOperate.addArrayToList(lsCmd, getMinVarFreq());
		ArrayOperate.addArrayToList(lsCmd, getMinFreqForHom());
		ArrayOperate.addArrayToList(lsCmd, getPValue());
		ArrayOperate.addArrayToList(lsCmd, getSomaPValue());
		ArrayOperate.addArrayToList(lsCmd, getOutputVcf());
		ArrayOperate.addArrayToList(lsCmd, getOutputSnp());
		ArrayOperate.addArrayToList(lsCmd, getOutputIndel());
		return lsCmd;
	}
	private String[] getInputFile(String inputFile) {
		return new String[]{inputFile};
	}
	private String[] getMinCoverage() {
		return new String[] { "--min-coverage", minCoverage + "" };
	}
	
	private String[] getMinCovNor() {
		return new String[] { "--min-coverage-normal", minCovNor + ""};
	}
	
	private String[] getMinCovTum() {
		return new String[] { "--min-coverage-tumor", minCovTum + ""};
	}
	private String[] getMinVarFreq() {
		return new String[] { "--min-var-freq", minVarFreq + "" };
	}
	private String[] getMinFreqForHom() {
		return new String[] { "--min-freq-for-hom", minFreqForHom + "" };
	}
	private String[] getPValue() {
		return new String[] { "--p-value", pValue + "" };
	}
	
	private String[] getSomaPValue() {
		return new String[] { "--min-coverage-tumor", somaPValue + ""};
	}
	private String[] getOutputVcf() {
		if (outputVcf != 1) {
			return null;
		}
		return new String[] { "--output-vcf", outputVcf + "" };
	}
	
	private String[] getOutputSnp() {
		return new String[] { "--output-snp", outputSnp};
	}
	
	private String[] getOutputIndel() {
		return new String[] { "--output-indel", outputIndel};
	}
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
}
