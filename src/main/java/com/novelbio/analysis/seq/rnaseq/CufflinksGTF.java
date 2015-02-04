package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.BamReadsInfo;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class CufflinksGTF implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(CufflinksGTF.class);
	static int intronMin = 50;
	static int intronMax = 500000;
	/** 重新计算是否使用以前的结果 */
	boolean isUseOldResult = true;
	/** 遇到错误跳过的模式 */
	boolean skipErrorMode = false;
	
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
	String chrFile;

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	Double smallAnchorFraction;
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	Integer intronLenMin;
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	Integer intronLenMax;
	/** 线程数 */
	int threadNum = 4;
	/** 默认是solexa的最长插入 */
	int maxInsert = 450;
	/** 给定GTF的文件 */
	String gtfFile;
	boolean isReconstruct = true;
	/** 输出文件路径 */
	String outPathPrefix = "";
	/** 是否用上四分之一位点标准化 */
	boolean isUpQuartileNormalized = false;

	boolean mergeBamFileByPrefix = false;
	
	/** fpkm小于该数值的新转录本就过滤掉 */
	double fpkmFilter = 10;
	/** 长度小于该长度的单exon转录本就过滤掉 */
	int isoLenFilter = 200;
	/** 最后获得的结果 */
	List<String> lsCufflinksResult = new ArrayList<String>();
	
	List<String> lsCmd = new ArrayList<>();
	
	public CufflinksGTF() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		ExePathCufflinks = softWareInfo.getExePathRun();
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
	/** fpkm小于该数值的新转录本就过滤掉 */
	public void setFpkmFilter(double fpkmFilter) {
		this.fpkmFilter = fpkmFilter;
	}
	/** 长度小于该长度的单exon转录本就过滤掉 */
	public void setIsoLenFilter(int isoLenFilter) {
		this.isoLenFilter = isoLenFilter;
	}
	/**
	 * 是否根据prefix合并bam文件，然后再做cufflinks分析
	 * 默认false
	 */
	public void setIsMergeBamByPrefix(boolean isMergeBamByPrefix) {
		this.mergeBamFileByPrefix = isMergeBamByPrefix;
	}
	public boolean isMergeBamFileByPrefix() {
		return mergeBamFileByPrefix;
	}
	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * 
	 * @param exePath
	 *            如果在根目录下则设置为""或null
	 * @param chrFile
	 *            单条染色体
	 */
	public void setChrFile(String chrFile) {
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
		if (mergeBamFileByPrefix) {
			outPath += "_mergeByPrefix";
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
		if (smallAnchorFraction == null) {
			return null;
		}
		return new String[]{"-A", smallAnchorFraction + ""};
	}

	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private List<String> getIntronLen() {
		List<String> lsIntronLen = new ArrayList<>();
		if (intronLenMin != null) {
			lsIntronLen.add("--min-intron-length");
			lsIntronLen.add(intronLenMin + "");
		}
		if (intronLenMax != null) {
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
		} else if (intronLenMinMax[0] > intronMin) {
			intronLenMin = 50;
		} else {
			intronLenMin = intronLenMinMax[0];
		}
		
		if (intronLenMinMax[1] < intronMax) {
			intronLenMax = intronLenMinMax[1];
		}
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
			samFile = outPathPrefix + "merge" + DateUtil.getDateAndRandom() + ".bam";
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
	
	private List<String> getSamFileSeperate(String prefix) {
		List<SamFile> lsSamFiles = mapPrefix2SamFiles.get(prefix);
		List<String> lsResult = new ArrayList<String>();
		for (SamFile samFile : lsSamFiles) {
			lsResult.add(samFile.getFileName());
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
	 * @param gtfFile
	 * @param isReconstruct 是否发现新的转录本<br>
	 * true: 用输入的gff文件指导重建转录本的工作
	 * false: 仅用输入的gff文件进行定量工作
	 */
	public void setGtfFile(String gtfFile, boolean isReconstruct) {
		this.gtfFile = gtfFile;
		this.isReconstruct = isReconstruct;
	}

	/** 用GTF文件来辅助预测 */
	private String[] getGtfFile() {
		if (FileOperate.isFileExistAndBigThanSize(gtfFile, 0)) {
			String param = isReconstruct ? "-g" : "-G";
			return new String[]{param,  gtfFile};
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
	 *            is sequenced.<br><b>意思mapping到反的上面去。</b><br>
	 *            <b>fr-secondstrand</b> Ligation, Standard SOLiD Same as above
	 *            except we enforce the rule that the left-most end of the
	 *            fragment (in transcript coordinates) is the first sequenced
	 *            (or only sequenced for single-end reads). Equivalently, it is
	 *            assumed that only the strand generated during second strand
	 *            synthesis is sequenced.<b>意思mapping到正的上面去。</b>
	 */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		this.strandSpecifictype = strandSpecifictype;
	}
	/**
	 * 返回链的方向
	 * @return
	 */
	private String[] getStrandSpecifictype(String bamFile) {
		if (strandSpecifictype == StrandSpecific.UNKNOWN && FileOperate.isFileExistAndBigThanSize(gtfFile, 0)) {
			GffHashGene gffHashGene = new GffHashGene(GffType.GTF, gtfFile);
			BamReadsInfo bamReadsInfo = new BamReadsInfo();
			bamReadsInfo.setSamFile(new SamFile(bamFile));
			bamReadsInfo.setGffHashGene(gffHashGene);
			bamReadsInfo.calculate();
			strandSpecifictype = bamReadsInfo.getStrandSpecific();
		}
		
		if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			return new String[]{"--library-type", "fr-secondstrand"};
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			return new String[]{"--library-type", "fr-firststrand"};
		}
		return null;
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public void runCufflinks() {
		lsMergeSamFile = new ArrayList<String>();
		lsCufflinksResult.clear();
		lsCmd.clear();
		for (String prefix : setPrefix) {
			if (mergeBamFileByPrefix) {
				String cufResultTmp = runMergeBamGtf(prefix);
				if (cufResultTmp != null) {
					lsCufflinksResult.add(cufResultTmp);
				}
			} else {
				lsCufflinksResult.addAll(runSepBamGtf(prefix));
			}
		}
		
		deleteMergeFile();
	}
	
	/**
	 * 返回合并好的bam文件
	 * @param prefix
	 * @return
	 */
	private String runMergeBamGtf(String prefix) {
		String mergedBamFile = getSamFileMerged(prefix);
		return getCufflinksResult(mergedBamFile, prefix);
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
			String tmpGtf = getCufflinksResult(bamFile, prefixThis);
			if (tmpGtf != null) {
				lsGtf.add(tmpGtf);
			}
		}
		return lsGtf;
	}
	
	private String getCufflinksResult(String bamFile, String prefix) {
		List<String> lsCmd = getLsCmd(bamFile, prefix);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String outGTFPath = FileOperate.addSep(getOutPathPrefix(prefix));
		String outGtf = "";
		if (isUseOldResult
				&& FileOperate.isFileExistAndBigThanSize(outGTFPath + "/genes.fpkm_tracking" , 0)
				&& FileOperate.isFileExistAndBigThanSize(outGTFPath + "/isoforms.fpkm_tracking" , 0)
				&& FileOperate.isFileExist(outGTFPath + "/skipped.gtf")
				&& FileOperate.isFileExistAndBigThanSize(outGTFPath + "/transcripts.gtf" , 0)
				) {
			outGtf = outGTFPath + "transcripts.gtf";
		} else {
			cmdOperate.setRedirectInToTmp(true);
			cmdOperate.addCmdParamInput(bamFile);
			cmdOperate.setRedirectOutToTmp(true);
			cmdOperate.addCmdParamOutput(getOutPathPrefix(prefix));
			
			cmdOperate.run();
			if (cmdOperate.isFinishedNormal()) {
				outGtf = outGTFPath + "transcripts.gtf";
			} else {
				if (skipErrorMode) {
					outGtf = null;
				} else {
					String errInfo = cmdOperate.getErrOut();
					throw new RuntimeException("cufflinks error:" + prefix + ":\n" + cmdOperate.getCmdExeStrReal() + "\n" + errInfo);
				}
			}
		}
		this.lsCmd.add(cmdOperate.getCmdExeStr());
		
		if (outGtf != null) {
			String outGtfModify = FileOperate.changeFileSuffix(outGtf, "_filterWithFPKMlessThan" + fpkmFilter, null);
			filterGtfFile(outGtf, outGtfModify, fpkmFilter, isoLenFilter);
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
		String outGTFPath = FileOperate.addSep(getOutPathPrefix(prefix));
		String outGtf = outGTFPath + "transcripts.gtf";
		outGtf = FileOperate.changeFileSuffix(outGtf, "_filterWithFPKMlessThan" + fpkmFilter, null);
		return outGtf;
	}
	
	/** 获得结果 */
	public List<String> getLsCufflinksResult() {
		return lsCufflinksResult;
	}
	
	public static void filterGtfFile(String outGtf, String outFinal, double fpkmFilter, int isoLenFilter) {
		TxtReadandWrite txtRead = new TxtReadandWrite(outGtf);
		
		/** 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认  NCBI的ID  */
		String transIdreg = "(?<=transcript_id \")[\\w\\-%\\:\\.]+";
		String fpkmreg = "(?<=FPKM \")[\\d\\.]+";
		String geneNamereg = "(?<=gene_id \")[\\w\\-%\\:\\.]+";
		PatternOperate patTransId = new PatternOperate(transIdreg, false);
		PatternOperate patFpkm = new PatternOperate(fpkmreg, false);
		PatternOperate patGeneName = new PatternOperate(geneNamereg, false);
		Map<String, Double> mapIso2Fpkm = new HashMap<String, Double>();
		Map<String, String> mapIso2GeneName = new HashMap<String, String>();
		for (String content : txtRead.readlines()) {
			String transId = patTransId.getPatFirst(content);
			double fpkm = Double.parseDouble(patFpkm.getPatFirst(content));
			String geneName = patGeneName.getPatFirst(content);
			mapIso2Fpkm.put(transId, fpkm);
			if (geneName != null) {
				mapIso2GeneName.put(transId, geneName);
			}
		}
		txtRead.close();
		
		GffHashGene gffHashGene = new GffHashGene(GffType.GTF, outGtf);
		GffHashGene gffHashGeneNew = new GffHashGene();
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			GffDetailGene gffDetailGeneNew = gffDetailGene.clone();
			gffDetailGeneNew.clearIso();
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				
				if (mapIso2GeneName.containsKey(iso.getName())
						||  iso.size() > 1 || (mapIso2Fpkm.containsKey(iso.getName()) && mapIso2Fpkm.get(iso.getName()) >= fpkmFilter && (iso.getLen() >= isoLenFilter))
						) {
					gffDetailGeneNew.addIsoSimple(iso);
				}

			}
			if (gffDetailGeneNew.getLsCodSplit().size() > 0) {
				gffHashGeneNew.addGffDetailGene(gffDetailGeneNew);
			}
		}
		logger.info("before filter iso with only one exon have " + getIsoHaveOneExonNum(gffHashGene));
		logger.info("after filter iso with only one exon have " + getIsoHaveOneExonNum(gffHashGeneNew));
		logger.info("before filter geneNum " + gffHashGene.getGffDetailAll().size());
		logger.info("after filter geneNum " + gffHashGeneNew.getGffDetailAll().size());
		gffHashGeneNew.writeToGTF(outFinal);
	}
	
	/** 仅含一个exon的iso的数量 */
	private static int getIsoHaveOneExonNum(GffHashGene gffHashGene) {
		int isoExon1NumNew = 0;
		for (GffDetailGene gene : gffHashGene.getGffDetailAll()) {
			for (GffGeneIsoInfo isoInfo : gene.getLsCodSplit()) {
				if (isoInfo.size() == 1) {
					isoExon1NumNew++;
				}
			}
		}
		return isoExon1NumNew;
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
		ArrayOperate.addArrayToList(lsCmd, getStrandSpecifictype(bamFileName));
		addLsCmdStr(lsCmd, getIsUpQuartile());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getCorrectChrFile());
		lsCmd.add(bamFileName);
		return lsCmd;
	}
	private void addLsCmdStr(List<String> lsCmd, String param) {
		if (param == null || param.equals("")) return;
		lsCmd.add(param);
	}
	
	private void deleteMergeFile() {
		if (!mergeBamFileByPrefix) return;
		
		if (lsMergeSamFile.size() > 0) {
			for (String mergedSamFile : lsMergeSamFile) {
				FileOperate.delFile(mergedSamFile);
			}
		}
	}
	
	public String getCufflinksGTFPath() {
		return FileOperate.addSep(outPathPrefix) + "transcripts.gtf";
	}
	
	/** 最后再获得 */
	@Override
	public List<String> getCmdExeStr() {
		String version = getVersion();
		if (version != null) {
			lsCmd.add(0, "cufflinks version:" + getVersion());
		}
		return lsCmd;
	}
	
	public String getVersion() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(ExePathCufflinks + "cufflinks");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.run();
		List<String> lsInfo = cmdOperate.getLsErrOut();
		String cufflinksVersion = "";
		try {
			cufflinksVersion = lsInfo.get(0).toLowerCase().replace("cufflinks", "").trim();
		} catch (Exception e) {}
		return cufflinksVersion;
	}
}
