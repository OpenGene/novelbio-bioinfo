package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class StringTie  implements IntCmdSoft, IntReconstructIsoUnit {
	private static final Logger logger = Logger.getLogger(StringTie.class);
	private static final String tmpFolder = "tmp/";
	
	/** Stringtie所在路径 */
	String exePath;
	
	/** 重构的最短转录本的长度 */
	int minIsoLen = 200;
	/** 最小的anchor length for junctions (默认：10) */
	int minAnchorJuncLen = 10;
	/** 最小的junction coverage */
	int minJuncCoverage = 1;
	/** gap between read mappings triggering a new bundle */
	int gapToNewIso = 50;
	/** output file with reference transcripts that are covered by reads */
	boolean outputExistIso = true;
	
	String gtfFile;
	/** only estimates the abundance of given reference transcripts (requires {@link #gtfFile}) */
	boolean justOutRefIso = false;
	
	/** 最小的junction coverage (默认: 1) */
	int minJuncCov = 1;
	
	/** 转录本组装考虑的每bp最少reads覆盖数 */
	int minAssBaseCov = 3;
	
	int threadNum = 8;
	/** 输出文件名 */
	String outfile;

	/** 重新计算是否使用以前的结果 */
	boolean isUseOldResult = true;
	
	/** 是否只输出参考转录本的fpkm值 */
	boolean isRefFPKM = false;
	
	List<String> lsCmd = new ArrayList<>();
	
	/** 输出文件路径 */
	private String outPath;
	
	public  StringTie() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.stringtie);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	/** 输出文件路径 */
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	/** 是否使用以前跑出来的结果，默认为ture<br>
	 * 意思就是如果以前跑出来过结果，这次就直接跳过
	 * @param isUseOldResult
	 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	
	public void setMinIsoLen(int minIsoLen) {
		this.minIsoLen = minIsoLen;
	}
	
	private String[] getMinIsoLen() {
		return new String[]{"-m", minIsoLen + ""};
	}
	
	public void setMinAnchorJuncLen(int minAnchorJuncLen) {
		this.minAnchorJuncLen = minAnchorJuncLen;
	}
	private String[] getMinAnchorJuncLen() {
		return new String[]{"-a", minAnchorJuncLen + ""};
	}
	public void setGapToNewIso(int gapToNewIso) {
		this.gapToNewIso = gapToNewIso;
	}
	private String[] getGapToNewIso() {
		return new String[]{"-g", gapToNewIso + ""};
	}
	
	public void setThreadNum(int threadNum) {
		if (threadNum <= 0) {
			threadNum = 1;
		}
		this.threadNum = threadNum;
	}
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}
	public void setJustOutRefIso(boolean justOutRefIso) {
		this.justOutRefIso = justOutRefIso;
	}
	private String[] getJustOutRefIso() {
		if (justOutRefIso) {
			return new String[]{"-C"};
		} else {
			return null;
		}
	}
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	private String[] getGtfFile() {
		if (!FileOperate.isFileExistAndBigThanSize(gtfFile, 0)) {
			return null;
		}
		return new String[]{"-G", gtfFile};
	}
	public void setOutfile(String outfile) {
		this.outfile = outfile;
	}

	public void setMinJuncCoverage(int minJuncCoverage) {
		this.minJuncCoverage = minJuncCoverage;
	}
	public String[] getMinJuncCoverage() {
		return new String[]{"-j", minJuncCoverage + ""};
	}
	
	public void setMinAssBaseCov(int minAssBaseCov) {
		this.minAssBaseCov = minAssBaseCov;
	}
	public String[] getMinAssBaseCov() {
		return new String[]{"-c", minAssBaseCov + ""};
	}
	
	/** 是否只输出参考转录本的fpkm值 */
	public void setIsRefFPKM(boolean isRefFPKM) {
		this.isRefFPKM = isRefFPKM;
	}
	/** do not assemble any transcripts on these reference sequence(s) */
	private String getIsRefFPKM() {
		if (isRefFPKM) {
			return "-e";
		} else {
			return null;
		}
	}
	
	public void reconstruct(String bamFile) {
		List<String> lsCmd = getLsCmd(bamFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.addCmdParamInput(bamFile);
		if (!StringOperate.isRealNull(gtfFile)) {
			cmdOperate.addCmdParamInput(gtfFile);
		}
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(getOutGtfName(bamFile));
		this.lsCmd.add(cmdOperate.getCmdExeStr());
		cmdOperate.runWithExp("StringTie error on file " + FileOperate.getFileName(bamFile));
	}
	
	@Override
	public String getOutGtfName(String bamFile) {
		String prefix = FileOperate.getFileNameSep(bamFile)[0];
		String outGTFPath = FileOperate.addSep(outPath) + CufflinksGTF.tmpFolder + prefix + ".stringtie.transcripts.gtf";
		return outGTFPath;
	}
	
	/**
	 * 合并前缀相同的bam文件，并返回待执行的lsCmd
	 * @param prefix
	 * @return
	 */
	public List<String> getLsCmd(String bamFileName) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "stringtie");
		lsCmd.add(bamFileName);
		ArrayOperate.addArrayToList(lsCmd, getGtfFile());
		ArrayOperate.addArrayToList(lsCmd, getMinAnchorJuncLen());
		ArrayOperate.addArrayToList(lsCmd, getMinIsoLen());
		ArrayOperate.addArrayToList(lsCmd, getGapToNewIso());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getMinAssBaseCov());
		ArrayOperate.addArrayToList(lsCmd, getMinJuncCoverage());
		ArrayOperate.addArrayToList(lsCmd, getJustOutRefIso());
		addLsCmdStr(lsCmd, getIsRefFPKM());
		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefixCmd(bamFileName));
		return lsCmd;
	}
	private void addLsCmdStr(List<String> lsCmd, String param) {
		if (param == null || param.equals("")) return;
		lsCmd.add(param);
	}
	
	/** 用于CMD */
	private String[] getOutPathPrefixCmd(String bamFile) {
		return new String[]{"-o", getOutGtfName(bamFile)};
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	public String getVersion() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "stringtie");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.run();
		List<String> lsInfo = cmdOperate.getLsErrOut();
		String cufflinksVersion = "";
		try {
			cufflinksVersion = lsInfo.get(0).toLowerCase().replace("StringTie", "").replace("usage:", "").trim();
		} catch (Exception e) {}
		return cufflinksVersion;
	}
	
}
