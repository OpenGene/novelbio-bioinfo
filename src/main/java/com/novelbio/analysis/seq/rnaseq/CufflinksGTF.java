package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;

public class CufflinksGTF {
	private static Logger logger = Logger.getLogger(MapTophat.class);

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
	int intronLenMin = 50;
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	int intronLenMax = 500000;
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
	

	GffChrAbs gffChrAbs;
	boolean booSetIntronMin = false;
	boolean booSetIntronMax = false;
	
	boolean mergeBamFileByPrefix = false;
	
	/** 最后获得的结果 */
	List<String> lsCufflinksResult = new ArrayList<String>();
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		booSetIntronMin = false;
		booSetIntronMax = false;
	}

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

	/** 用于CMD */
	private String getOutPathPrefixCmd(String prefix) {
		return "-o " + CmdOperate.addQuot(getOutPathPrefix(prefix)) + " ";
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
	private String getAnchoProportion() {
		return "-A " + smallAnchorFraction + " ";
	}

	private void setIntronLen() {
		if (booSetIntronMax && booSetIntronMin) {
			return;
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			ArrayList<Integer> lsIntronSortedS2M = gffChrAbs.getGffHashGene().getLsIntronSortedS2M();
			int intronLenMin = lsIntronSortedS2M.get(50);
			int intronLenMax = lsIntronSortedS2M.get(lsIntronSortedS2M.size() - 10);
			if (intronLenMin < this.intronLenMin) {
				this.intronLenMin = intronLenMin;
				booSetIntronMin = true;
			}
			if (intronLenMin < 20) {
				this.intronLenMin = 20;
			}
			if (intronLenMax*2 < this.intronLenMax) {
				this.intronLenMax = intronLenMax*2;
				booSetIntronMax = true;
			}
		}
	}

	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private String getIntronLenMin() {
		return "--min-intron-length " + intronLenMin + " ";
	}

	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
		booSetIntronMax = true;
	}

	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
		booSetIntronMin = true;
	}

	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private String getIntronLenMax() {
		return "--max-intron-length " + intronLenMax + " ";
	}

	private String getSamFileMerged(String prefix) {
		List<SamFile> lsSamFiles = mapPrefix2SamFiles.get(prefix);
		String samFile = "";
		if (lsSamFiles.size() == 1) {
			samFile = CmdOperate.addQuot(lsSamFiles.get(0).getFileName() );
		}
		else {
			String mergeSamFile = outPathPrefix + "merge" + DateUtil.getDateAndRandom() + ".bam";
			SamFile mergeFile = SamFile.mergeBamFile(mergeSamFile, lsSamFiles);
			if (mergeFile != null) {
				mergeSamFile = mergeFile.getFileName();
				samFile = CmdOperate.addQuot(mergeSamFile);
				lsMergeSamFile.add(mergeSamFile);
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

	private String getThreadNum() {
		return "-p " + threadNum + " ";
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
	private String getGtfFile() {
		setGTFfile();
		if (FileOperate.isFileExist(gtfFile)) {
			return "-g \"" + gtfFile + "\" ";
		}
		return "";
	}

	private void setGTFfile() {
		if (FileOperate.isFileExistAndBigThanSize(gtfFile, 100)) {
			return;
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			String path = FileOperate.getParentPathName(mapPrefix2SamFiles.values().iterator().next().getFileName());
			String outGTF = path + gffChrAbs.getSpecies().getAbbrName() + DateUtil.getDateAndRandom() + ".GTF";
			gffChrAbs.getGffHashGene().writeToGTF(outGTF, "novelbio");
			this.gtfFile = outGTF;
		}
	}

	private String getCorrectChrFile() {
		if (FileOperate.isFileExist(chrFile)) {
			return "-b \"" + chrFile + "\" ";
		}
		return "";
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
		if (strandSpecifictype == StrandSpecific.NONE) {
			return "";
		} else if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			return "--library-type fr-firststrand ";
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			return "--library-type fr-secondstrand ";
		}
		return "";
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public void runCufflinks() {
		setIntronLen();
		lsMergeSamFile = new ArrayList<String>();
		lsCufflinksResult.clear();
		// linux命令如下
		/**
		 * cufflinks -o
		 * /media/winE/NBC/Project/RNASeq_GF110614/resultTmp/cufflinks -p 4 -G
		 * /media/winE/Bioinformatics/GenomeData/Arabidopsis\
		 * TAIR9/TAIR10GFF/TAIR10_GTF3_genes.gtf -b
		 * /media/winE/Bioinformatics/GenomeData/Arabidopsis\
		 * TAIR9/ChromFa/TAIR10_chr_All.fas -u -N --compatible-hits-norm -L COL
		 * -I 15000 --min-intron-length 20
		 * /media/winE/NBC/Project/RNASeq_GF110614
		 * /resultTmp/tophatResult/accepted_hits.bam
		 */
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
	
	private String runCufflinksMergedBam(String prefix) {
		String outGTF = getOutPathPrefix(prefix);
		String cmd = "";
		cmd = ExePathCufflinks + "cufflinks ";
		cmd = cmd + getOutPathPrefixCmd(prefix) + getAnchoProportion()
				+ getIntronLenMin() + getIntronLenMax() + getGtfFile()
				+ getStrandSpecifictype() + getThreadNum()
				+ getCorrectChrFile();
		String cmdRun = cmd + getSamFileMerged(prefix);
		try {
			CmdOperate cmdOperate = new CmdOperate(cmdRun, "cufflinks");
			cmdOperate.run();
			if (cmdOperate.isFinished()) {
				return outGTF;
			}
		} catch (Exception e) {
			logger.error(prefix + " cufflinks error");
		}
		return null;
	}
	
	private List<String> runCufflinksSepBam(String prefix) {
		List<String> lsResult  = new ArrayList<String>();
		List<String> lsSamfiles = getSamFileSeperate(prefix);
		int i = 0;
		for (String string : lsSamfiles) {
			i++;
			String prefixThis = prefix + "i";
			String cmd = "";
			cmd = ExePathCufflinks + "cufflinks ";
			cmd = cmd + getOutPathPrefixCmd(prefixThis) + getAnchoProportion()
					+ getIntronLenMin() + getIntronLenMax() + getGtfFile()
					+ getStrandSpecifictype() + getThreadNum()
					+ getCorrectChrFile();
			String cmdRun = cmd + string;
			try {
				CmdOperate cmdOperate = new CmdOperate(cmdRun, "cufflinks");
				cmdOperate.run();
				if (cmdOperate.isFinished()) {
					lsResult.add(getOutPathPrefix(prefixThis));
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
