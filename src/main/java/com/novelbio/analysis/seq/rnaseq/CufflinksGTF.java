package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.BamReadsInfo;
import com.novelbio.analysis.seq.sam.ExceptionSamStrandErrorRuntime;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class CufflinksGTF implements IntCmdSoft, IntReconstructIsoUnit {
	private static final Logger logger = Logger.getLogger(CufflinksGTF.class);
	public static final String tmpFolder = "tmp/";
	
	static int intronMin = 50;
	static int intronMax = 500000;
	/** 重新计算是否使用以前的结果 */
	boolean isUseOldResult = true;
	
	private StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	
	/** cufflinks所在路径 */
	private String exePath = "";
	/** 用于校正的染色体 */
	private String chrFile;

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	private Double smallAnchorFraction;
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private Integer intronLenMin;
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private Integer intronLenMax;
	/** 线程数 */
	private int threadNum = 4;
	/** 给定GTF的文件 */
	private String gtfFile;
	private boolean isReconstruct = true;

	/** 是否用上四分之一位点标准化 */
	private boolean isUpQuartileNormalized = false;
	
	private List<String> lsCmd = new ArrayList<>();
	
	/** 输出文件路径，必须是文件夹 */
	private String outPath = "";
	
	public CufflinksGTF() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		exePath = softWareInfo.getExePathRun();
	}
	
	/** 是否使用以前跑出来的结果，默认为ture<br>
	 * 意思就是如果以前跑出来过结果，这次就直接跳过
	 * @param isUseOldResult
	 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	
	/** 用于校正的染色体 */
	public void setChrFile(String chrFile) {
		this.chrFile = chrFile;
	}
	/** 输出文件路径，注意只能是文件夹 */
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	/** 是否用上四分之一位点标准化 */
	public void setUpQuartileNormalized(boolean isUpQuartileNormalized) {
		this.isUpQuartileNormalized = isUpQuartileNormalized;
	}
	
	/** 用于CMD */
	private String[] getOutPathPrefixCmd(String prefix) {
		return new String[]{"-o", getOutPathPrefix(prefix)};
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
		if (intronLenMin != null && intronLenMin > 0) {
			lsIntronLen.add("--min-intron-length");
			lsIntronLen.add(intronLenMin + "");
		}
		if (intronLenMax != null && intronLenMax > 0) {
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
	public void setIntronLenMin(Integer intronLenMin) {
		this.intronLenMin = intronLenMin;
	}
	public void setIntronLenMax(Integer intronLenMax) {
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
		if (!FileOperate.isFileExistAndBigThan0(gtfFile)) {
			throw new ExceptionNbcParamError("gtffile " + gtfFile + " is not exist");
		}
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
		if (FileOperate.isFileExistAndNotDir(chrFile)) {
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
			try {
				bamReadsInfo.calculate();
			} catch (Exception e) {
				throw new ExceptionSamStrandErrorRuntime(e.getMessage() + " please choose the correct strand Type");
			}
			strandSpecifictype = bamReadsInfo.getStrandSpecific();
		}
		
		if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			return new String[]{"--library-type", "fr-secondstrand"};
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			return new String[]{"--library-type", "fr-firststrand"};
		}
		return null;
	}

	public void reconstruct(String bamFile) {
		String prefix = FileOperate.getFileNameSep(bamFile)[0];
		List<String> lsCmd = getLsCmd(bamFile, prefix);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		this.lsCmd.add(cmdOperate.getCmdExeStr());
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.addCmdParamInput(bamFile);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(getOutPathPrefix(prefix));
		cmdOperate.runWithExp("cufflinks error on file " + FileOperate.getFileName(bamFile) );
		FileOperate.moveFile(true, getOutPathResultRaw(prefix), getOutGtfName(bamFile));
	}
	
	@Override
	public String getOutGtfName(String bamFile) {
		String prefix = FileOperate.getFileNameSep(bamFile)[0];
		String outGTFPath = FileOperate.addSep(outPath) + prefix + ".cufflinks.transcripts.gtf";
		return outGTFPath;
	}
	
	/** 输出文件名 */
	private String getOutPathPrefix(String prefix) {
		return FileOperate.addSep(outPath) + tmpFolder + prefix;
	}
	/** 输出临时文件夹 */
	private String getOutPathResultRaw(String prefix) {
		return FileOperate.addSep(getOutPathPrefix(prefix)) + "transcripts.gtf";
	}
	/**
	 * 合并前缀相同的bam文件，并返回待执行的lsCmd
	 * @param prefix
	 * @return
	 */
	private List<String> getLsCmd(String bamFileName, String prefix) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "cufflinks");
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
	
	@Override
	public List<String> getCmdExeStr() {
		String version = getVersion();
		if (version != null) {
			lsCmd.add(0, "cufflinks version:" + getVersion());
		}
		return lsCmd;
	}
	
	private String getVersion() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "cufflinks");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.run();
		List<String> lsInfo = cmdOperate.getLsErrOut();
		String cufflinksVersion = "";
		try {
			cufflinksVersion = lsInfo.get(0).toLowerCase().replace("cufflinks", "").trim();
		} catch (Exception e) {}
		return cufflinksVersion;
	}

}
