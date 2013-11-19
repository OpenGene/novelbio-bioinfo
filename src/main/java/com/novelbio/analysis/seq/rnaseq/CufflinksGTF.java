package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class CufflinksGTF {
	private static Logger logger = Logger.getLogger(MapTophat.class);
	static int intronMin = 50;
	static int intronMax = 500000;
	StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	ArrayListMultimap<String, SamFile> mapPrefix2SamFiles = ArrayListMultimap.create();
	Set<String> setPrefix= new LinkedHashSet<String>();
	
	/** 如果输入多个bam文件，则将他们合并为一个
	 * 用来最后删除mergedbam文件用的
	 *  */
	List<String> lsMergeSamFile;
	
	/** cufflinks所在路径 */
	String ExePathCufflinks = "";
	/** 用于校正的染色体 */
	String chrFile = "";

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	double smallAnchorFraction = 0.09;
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	int intronLenMin = intronMin;
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	int intronLenMax = intronMax;
	/** indel的长度，默认为3 */
	int indelLen = 6;
	/** 线程数 */
	int threadNum = 4;
	/** 默认是solexa的最长插入 */
	int maxInsert = 450;
	/** 错配，这个走默认比较好，默认为2 */
	int mismatch = 2;
	/** 给定GTF的文件 */
	String gtfFile = "";
	/** 输出文件路径 */
	String outPathPrefix = "";
	/** 是否用上四分之一位点标准化 */
	boolean isUpQuartileNormalized = true;

	boolean mergeBamFileByPrefix = false;
	
	/** 最后获得的结果 */
	List<String> lsCufflinksResult = new ArrayList<String>();


	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * 
	 * @param exePath
	 *            如果在根目录下则设置为""或null
	 * @param chrFile
	 *            单条染色体
	 */
	public void setExePath(String ExePathCufflinks, String chrFile) {
		if (ExePathCufflinks == null || ExePathCufflinks.trim().equals("")) {
			this.ExePathCufflinks = "";
		} else {
			this.ExePathCufflinks = FileOperate.addSep(ExePathCufflinks);
		}
		this.chrFile = chrFile;
	}
	
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/** 是否用上四分之一位点标准化 */
	public void setUpQuartileNormalized(boolean isUpQuartileNormalized) {
		this.isUpQuartileNormalized = isUpQuartileNormalized;
	}
	
	/** 用于CMD */
	private String[] getOutPathPrefixCmd(String prefix) {
		return new String[]{"-o", getOutPathPrefix(prefix)};
	}
	/** 输出文件名 */
	private String getOutPathPrefix(String prefix) {
		String outPath = "";
		if (outPathPrefix.endsWith("/") || outPathPrefix.endsWith("\\")) {
			outPath = outPathPrefix + prefix;
		} else {
			outPath = outPathPrefix + "_" + prefix;
		}
		return outPath;
	}
	
	/**
	 * 在junction 的一头搭上exon的最短比例 0-1之间，默认0.09
	 * */
	public void setSmallAnchorFraction(double anchorLength) {
		if (anchorLength <= 0 || anchorLength >= 1) {
			return;
		}
		this.smallAnchorFraction = anchorLength;
	}

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	private String[] getAnchoProportion() {
		return new String[]{"-A", smallAnchorFraction + ""};
	}

	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private List<String> getIntronLen() {
		List<String> lsIntronLen = new ArrayList<>();
		if (intronLenMin != intronMin) {
			lsIntronLen.add("--min-intron-length");
			lsIntronLen.add(intronLenMin + "");
		}
		if (intronLenMax != intronMax) {
			lsIntronLen.add("--max-intron-length");
			lsIntronLen.add(intronLenMax + "");
		}
		return lsIntronLen;
	}

	/** 内含子最短和最长，最短默认50，最长默认500000，需根据不同物种进行设置 */
	public void setIntronLen(int intronLenMin, int intronLenMax) {
		this.intronLenMin = intronLenMin;
		this.intronLenMax = intronLenMax;
	}
	/** 内含子最短和最长，最短默认50，最长默认500000，需根据不同物种进行设置 */
	public void setIntronLen(int[] intronLenMinMax) {
		if (intronLenMinMax == null) {
			return;
		}
		if (intronLenMinMax[0] < 20) {
			intronLenMin = 20;
		} else if (intronLenMinMax[0] > 50) {
			intronLenMin = 50;
		} else {
			intronLenMin = intronLenMinMax[0];
		}
		
		if (intronLenMinMax[1] < 500000) {
			intronLenMax = intronLenMinMax[1];
		}
	}
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private String[] getIntronLenMax() {
		return new String[]{"--max-intron-length", intronLenMax + ""};
	}
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private String getIsUpQuartile() {
		if (isUpQuartileNormalized) {
			return "-N";
		} else {
			return null;
		}
	}
	
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * 输入的多个bam文件会merge成为一个然后做cufflinks的重建转录本
	 * @param fqFile
	 */
	public void setBam(ArrayList<String[]> lsSamfiles2Prefix) {
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
			samFile = outPathPrefix + "merge" + DateUtil.getDateAndRandom() + ".bam";
			SamFile mergeFile = SamFile.mergeBamFile(samFile, lsSamFiles);
			if (mergeFile != null) {
				samFile = mergeFile.getFileName();
			} else {
				throw new RuntimeException("cufflinks merge unsucess:" + prefix);
			}
		}
		return samFile;
	}
	
	private List<String> getSamFileSeperate(String prefix) {
		List<SamFile> lsSamFiles = mapPrefix2SamFiles.get(prefix);
		List<String> lsResult = new ArrayList<String>();
		for (SamFile samFile : lsSamFiles) {
			lsResult.add(CmdOperate.addQuot(samFile.getFileName()));
		}
		return lsResult;
	}
	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum) {
		if (threadNum <= 0) {
			threadNum = 1;
		}
		this.threadNum = threadNum;
	}

	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}

	/**
	 * 用gtf文件辅助mapping
	 * 
	 * @param gtfFile
	 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}

	/** 用GTF文件来辅助预测 */
	private String[] getGtfFile() {
		if (FileOperate.isFileExistAndBigThanSize(gtfFile, 0)) {
			return new String[]{"-g",  gtfFile};
		}
		return null;
	}
	
	/** 运行完一遍后再使用，因为如果是设定的GffChrAbs，程序会将其中的GFF转化为GTF文件 */
	public String getGtfReffile() {
		return gtfFile;
	}

	private String[] getCorrectChrFile() {
		if (FileOperate.isFileExist(chrFile)) {
			return new String[]{"-b", chrFile};
		}
		return null;
	}

	/**
	 * STRAND_NULL等，貌似是设置RNA-Seq是否为链特异性测序的，吃不准
	 * 
	 * @param strandSpecifictype
	 * <br>
	 *            <b>fr-unstranded</b> Standard Illumina Reads from the
	 *            left-most end of the fragment (in transcript coordinates) map
	 *            to the transcript strand, and the right-most end maps to the
	 *            opposite strand.<br>
	 *            <b>fr-firststrand</b> dUTP, NSR, NNSR Same as above except we
	 *            enforce the rule that the right-most end of the fragment (in
	 *            transcript coordinates) is the first sequenced (or only
	 *            sequenced for single-end reads). Equivalently, it is assumed
	 *            that only the strand generated during first strand synthesis
	 *            is sequenced.<br>
	 *            <b>fr-secondstrand</b> Ligation, Standard SOLiD Same as above
	 *            except we enforce the rule that the left-most end of the
	 *            fragment (in transcript coordinates) is the first sequenced
	 *            (or only sequenced for single-end reads). Equivalently, it is
	 *            assumed that only the strand generated during second strand
	 *            synthesis is sequenced.
	 */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		this.strandSpecifictype = strandSpecifictype;
	}
	/**
	 * 返回链的方向
	 * @return
	 */
	private String getStrandSpecifictype() {
		if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			return "--library-type fr-firststrand";
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			return "--library-type fr-secondstrand";
		}
		return null;
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public void runCufflinks() {
		lsMergeSamFile = new ArrayList<String>();
		lsCufflinksResult.clear();
		for (String prefix : setPrefix) {
			if (mergeBamFileByPrefix) {
				String cufResultTmp = runCufflinksMergedBam(prefix);
				if (cufResultTmp != null) {
					lsCufflinksResult.add(cufResultTmp);
				}
			} else {
				lsCufflinksResult.addAll(runCufflinksSepBam(prefix));
			}
		}
		
		//TODO 运行结束后考虑删除merge的bam文件
//		deleteMergeFile();
	}
	
	/**
	 * 合并前缀相同的bam文件，并返回待执行的lsCmd
	 * @param prefix
	 * @return
	 */
	private List<String> getLsCmdMergedBam(String prefix) {
		String mergedBamFile = getSamFileMerged(prefix);
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePathCufflinks + "cufflinks");
		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefixCmd(prefix));
		ArrayOperate.addArrayToList(lsCmd, getAnchoProportion());
		lsCmd.addAll(getIntronLen());
		ArrayOperate.addArrayToList(lsCmd, getGtfFile());
		addLsCmdStr(lsCmd, getStrandSpecifictype());
		addLsCmdStr(lsCmd, getIsUpQuartile());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getCorrectChrFile());
		lsCmd.add(mergedBamFile);
		lsMergeSamFile.add(mergedBamFile);
		return lsCmd;
	}
	
	private void addLsCmdStr(List<String> lsCmd, String param) {
		if (param == null || param.equals("")) return;
		lsCmd.add(param);
	}
	
	private String runCufflinksMergedBam(String prefix) {
		String outGTF = getOutPathPrefix(prefix);
		String cmd = "";
		cmd = ExePathCufflinks + "cufflinks ";
		cmd = cmd + getOutPathPrefixCmd(prefix) + getAnchoProportion()
				+ getIntronLenMin() + getIntronLenMax() + getGtfFile()
				+ getStrandSpecifictype() + getIsUpQuartile() + getThreadNum()
				+ getCorrectChrFile();
		String cmdRun = cmd + getSamFileMerged(prefix);
		try {
			CmdOperate cmdOperate = new CmdOperate(cmdRun, "cufflinks");
			cmdOperate.run();
			if (cmdOperate.isFinished()) {
				return FileOperate.addSep(outGTF) + "transcripts.gtf";
			}
		} catch (Exception e) {
			logger.error(prefix + " cufflinks error", e);
		}
		return null;
	}
	
	/**
	 * 合并前缀相同的bam文件，并返回待执行的lsCmd
	 * @param prefix
	 * @return
	 */
	private List<String> getLsCmd(String bamFileName, String prefix) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePathCufflinks + "cufflinks");
		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefixCmd(prefix));
		ArrayOperate.addArrayToList(lsCmd, getAnchoProportion());
		lsCmd.addAll(getIntronLen());
		ArrayOperate.addArrayToList(lsCmd, getGtfFile());
		addLsCmdStr(lsCmd, getStrandSpecifictype());
		addLsCmdStr(lsCmd, getIsUpQuartile());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getCorrectChrFile());
		lsCmd.add(bamFileName);
		return lsCmd;
	}
	
	private List<String> runCufflinksSepBam(String prefix) {
		List<String> lsResult  = new ArrayList<String>();
		List<String> lsSamfiles = getSamFileSeperate(prefix);
		int i = 0;
		for (String string : lsSamfiles) {
			i++;
			String prefixThis = prefix + i;
			String cmd = "";
			cmd = ExePathCufflinks + "cufflinks ";
			cmd = cmd + getOutPathPrefixCmd(prefixThis) + getAnchoProportion()
					+ getIntronLenMin() + getIntronLenMax() + getGtfFile()
					+ getStrandSpecifictype() + getIsUpQuartile() + getThreadNum()
					+ getCorrectChrFile();
			String cmdRun = cmd + string;
			try {
				CmdOperate cmdOperate = new CmdOperate(cmdRun, "cufflinks");
				cmdOperate.run();
				if (cmdOperate.isFinished()) {
					lsResult.add(FileOperate.addSep(getOutPathPrefix(prefixThis)) + "transcripts.gtf");
				}
			} catch (Exception e) {
				logger.error(prefixThis + " cufflinks error");
			}
		}
		if (lsResult.size() > 0) {
			return lsResult;
		} else {
			return null;
		}
	}
	
	/** 获得结果 */
	public List<String> getLsCufflinksResult() {
		return lsCufflinksResult;
	}
	
//	private void deleteMergeFile() {
//		if (lsSamFiles.size() > 0) {
//			FileOperate.delFile(mergeSamFile);
//		}
//	}
	public String getCufflinksGTFPath() {
		return FileOperate.addSep(outPathPrefix) + "transcripts.gtf";
	}
}
