package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class StringTie  implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(StringTie.class);
	public static final String tmpFolder = "tmpStringTie/";
	
	ArrayListMultimap<String, SamFile> mapPrefix2SamFiles = ArrayListMultimap.create();
	Set<String> setPrefix= new LinkedHashSet<String>();
	
	/** 如果输入多个bam文件，则将他们合并为一个
	 * 用来最后删除mergedbam文件用的
	 *  */
	List<String> lsMergeSamFile;
	
	/** Stringtie所在路径 */
	String ExePathStringTie = "/home/novelbio/bianlianle/software/";
	
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
	/** 遇到错误跳过的模式 */
	boolean skipErrorMode = false;
	
	boolean mergeBamFileByPrefix = false;
	
	/** 长度小于该长度的单exon转录本就过滤掉 */
	int isoLenFilter = 200;
	/** fpkm小于该数值的新转录本就过滤掉 */
	double fpkmFilter = 10;
	
	/** 是否只输出参考转录本的fpkm值 */
	boolean isRefFPKM = false;
	
	/** 最后获得的结果 */
	List<String> lsStringTieResult = new ArrayList<String>();
	
	List<String> lsCmd = new ArrayList<>();
	public  StringTie() {
//		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.stringtie);
//		this.ExePathStringTie = softWareInfo.getExePathRun();
		this.ExePathStringTie = ExePathStringTie;
	}
	/** 是否使用以前跑出来的结果，默认为ture<br>
	 * 意思就是如果以前跑出来过结果，这次就直接跳过
	 * @param isUseOldResult
	 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	/** 遇到错误是否跳过，不跳过就抛出异常 */
	public void setSkipErrorMode(boolean skipErrorMode) {
		this.skipErrorMode = skipErrorMode;
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
			return new String[]{"-C", threadNum + ""};
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
	
	/** 是否用上四分之一位点标准化 */
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
	
	
	/** 输出文件路径 */
	String outPathPrefix = "";
	
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	
	/**
	 * 是否根据prefix合并bam文件，然后再做StringTie分析
	 * 默认false
	 */
	public void setIsMergeBamByPrefix(boolean isMergeBamByPrefix) {
		this.mergeBamFileByPrefix = isMergeBamByPrefix;
	}
	public boolean isMergeBamFileByPrefix() {
		return mergeBamFileByPrefix;
	}
	
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * 输入的多个bam文件会merge成为一个然后做StringTie的重建转录本
	 * @param fqFile
	 */
	public void setLsBamFile2Prefix(ArrayList<String[]> lsSamfiles2Prefix) {
		mapPrefix2SamFiles.clear();
		for (String[] strings : lsSamfiles2Prefix) {
			mapPrefix2SamFiles.put(strings[1].trim(), new SamFile(strings[0]));
			setPrefix.add(strings[1].trim());
		}
	}
	
	private String getSamFileMerged(String prefix) {
		List<SamFile> lsSamFiles = mapPrefix2SamFiles.get(prefix);
		String samFile = "";
		if (lsSamFiles.size() == 1) {
			samFile = lsSamFiles.get(0).getFileName();
		}
		else {
			samFile = outfile + "merge" + DateUtil.getDateAndRandom() + ".bam";
			SamFile mergeFile = SamFile.mergeBamFile(samFile, lsSamFiles);
			if (mergeFile != null) {
				samFile = mergeFile.getFileName();
				lsMergeSamFile.add(samFile);
			} else {
				throw new RuntimeException("cufflinks merge unsucess:" + prefix);
			}
		}
		return samFile;
	}
	
	public void runStringTie() {
		lsMergeSamFile = new ArrayList<String>();
		lsStringTieResult.clear();
		lsCmd.clear();
		for (String prefix : setPrefix) {
			if (mergeBamFileByPrefix) {
				String cufResultTmp = runMergeBamGtf(prefix);
				if (cufResultTmp != null) {
					lsStringTieResult.add(cufResultTmp);
				}
			} else {
				lsStringTieResult.addAll(runSepBamGtf(prefix));
			}
		}
//		deleteMergeFile();
	}
	
	/**
	 * 返回合并好的bam文件
	 * @param prefix
	 * @return
	 */
	private String runMergeBamGtf(String prefix) {
		String mergedBamFile = getSamFileMerged(prefix);
		return runAndGetStringTieResult(mergedBamFile, prefix);
	}
	/**
	 * 返回合并好的bam文件
	 * @param prefix
	 * @return
	 */
	private List<String> runSepBamGtf(String prefix) {
		List<String> lsGtf = new ArrayList<>();
		List<String> lsSamfiles = getSamFileSeperate(prefix);
		int i = 0;
		for (String bamFile : lsSamfiles) {
			i++;
			String prefixThis = prefix + i;
			String tmpGtf = runAndGetStringTieResult(bamFile, prefixThis);
			if (tmpGtf != null) {
				lsGtf.add(tmpGtf);
			}
		}
		return lsGtf;
	}
	
	private List<String> getSamFileSeperate(String prefix) {
		List<SamFile> lsSamFiles = mapPrefix2SamFiles.get(prefix);
		List<String> lsResult = new ArrayList<String>();
		for (SamFile samFile : lsSamFiles) {
			lsResult.add(samFile.getFileName());
		}
		return lsResult;
	}
	
	private String runAndGetStringTieResult(String bamFile, String prefix) {
		
		List<String> lsCmd = getLsCmd(bamFile, prefix);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String outGTFPath = getOutPathPrefix(prefix);
		String outGtf = "";
		if (isUseOldResult
				&& FileOperate.isFileExistAndBigThanSize(outGTFPath, 0)
				) {
			outGtf = outGTFPath;
		} else {
			cmdOperate.setRedirectInToTmp(true);
			cmdOperate.addCmdParamInput(bamFile);			
			cmdOperate.run();
			if (cmdOperate.isFinishedNormal()) {
				outGtf = outGTFPath;
			} else {
				if (skipErrorMode) {
					outGtf = null;
				} else {
					String errInfo = cmdOperate.getErrOut();
					throw new RuntimeException("StringTie error:" + prefix + ":\n" + cmdOperate.getCmdExeStrReal() + "\n" + errInfo);
				}
			}
		}
		this.lsCmd.add(cmdOperate.getCmdExeStr());
		
		if (outGtf != null) {
			String outGtfModify = FileOperate.changeFileSuffix(outGtf, "_filterWithFPKMlessThan" + fpkmFilter, null);
			CufflinksGTF.filterGtfFile(outGtf, outGtfModify, fpkmFilter, isoLenFilter);
			outGtf = outGtfModify;
		}
		return outGtf;
	}
	/** 设定好本类后，不进行计算，直接返回输出的结果文件名 */
	public List<String> getLsGtfFileName() {
		List<String> lsResult = new ArrayList<>();
		for (String prefix : setPrefix) {
			if (mergeBamFileByPrefix) {
				String outGTFPath = getGtfFileFromPrefix(prefix);
				lsResult.add(outGTFPath);
			} else {
				List<String> lsSamfiles = getSamFileSeperate(prefix);
				int i = 0;
				for (String bamFile : lsSamfiles) {
					i++;
					String prefixThis = prefix + i;
					String subGtf = getGtfFileFromPrefix(prefixThis);
					lsResult.add(subGtf);
				}
			}
		}
		return lsResult;
	}
	private String getGtfFileFromPrefix(String prefix) {
		
		String outGTFPath = getOutPathPrefix(prefix);
		String outGtf = outGTFPath;
		outGtf = FileOperate.changeFileSuffix(outGtf, "_filterWithFPKMlessThan" + fpkmFilter, null);
		return outGtf;
	}
	
	/** 获得结果 */
	public List<String> getLsStringTieResult() {
		return lsStringTieResult;
	}
	
	private void deleteMergeFile() {
		if (!mergeBamFileByPrefix) return;
		
		if (lsMergeSamFile.size() > 0) {
			for (String mergedSamFile : lsMergeSamFile) {
				FileOperate.delFile(mergedSamFile);
			}
		}
	}
	
	/**
	 * 合并前缀相同的bam文件，并返回待执行的lsCmd
	 * @param prefix
	 * @return
	 */
	public List<String> getLsCmd(String bamFileName, String prefix) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePathStringTie + "stringtie");
		ArrayOperate.addArrayToList(lsCmd, getGtfFile());
		ArrayOperate.addArrayToList(lsCmd, getMinAnchorJuncLen());
		ArrayOperate.addArrayToList(lsCmd, getMinIsoLen());
		ArrayOperate.addArrayToList(lsCmd, getGapToNewIso());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getMinAssBaseCov());
		ArrayOperate.addArrayToList(lsCmd, getMinJuncCoverage());
		addLsCmdStr(lsCmd, getIsRefFPKM());
		lsCmd.add(bamFileName);
		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefixCmd(prefix));
		return lsCmd;
	}
	private void addLsCmdStr(List<String> lsCmd, String param) {
		if (param == null || param.equals("")) return;
		lsCmd.add(param);
	}
	
	/** 用于CMD */
	private String[] getOutPathPrefixCmd(String prefix) {
		return new String[]{">", getOutPathPrefix(prefix)};
	}
	/** 输出文件名 */
	private String getOutPathPrefix(String prefix) {
		String outPath = "";
		if (outPathPrefix.endsWith("/") || outPathPrefix.endsWith("\\")) {
			outPath = outPathPrefix + prefix;
		} else {
			outPath = outPathPrefix + "_" + prefix;
		}
		if (mergeBamFileByPrefix) {
			outPath += "_mergeByPrefix";
		}
		return outPath + ".transcripts.gtf";
	}
	
	
	
	@Override
	public List<String> getCmdExeStr() {
//		List<String> lsResult = new ArrayList<>();
//		
//		List<String> lsCmd = getLsCmd("","");
//		CmdOperate cmdOperate = new CmdOperate(lsCmd);
//		lsResult.add(cmdOperate.getCmdExeStr());
		
//		String version = getVersion();
//		if (version != null) {
//			lsCmd.add(0, "stringtie version:" + getVersion());
//		}
		return lsCmd;
	}
	public String getVersion() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(ExePathStringTie + "stringtie");
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
