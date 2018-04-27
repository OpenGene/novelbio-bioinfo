package com.novelbio.analysis.seq.mapping;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexTophat;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.analysis.seq.sam.SamToFastq;
import com.novelbio.analysis.seq.sam.SamToFastq.EnumSamToFastqType;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

/**
 * 还没返回结果的bam文件 tophat的mapping * tophat -r 120 -a 10 -m 1 -i 20 -I 6000
 * --solexa1.3-quals -F 0.15 -p 4 --coverage-search --min-coverage-intron 20
 * --max-coverage-intron 6000 --min-segment-intron 20 --max-segment-intron 6000
 * -G /media/winE/Bioinformatics/GenomeData/Arabidopsis\
 * TAIR9/TAIR10GFF/TAIR10_GTF3_genes.gtf -o
 * /media/winE/NBC/Project/RNASeq_GF110614/rawdata/tophatResult/
 * /media/winE/Bioinformatics/GenomeData/Arabidopsis\ TAIR9/ChromFa/TAIR10_ath
 * /media
 * /winE/NBC/Project/RNASeq_GF110614/rawdata/data/Col_L1_1.fq,/media/winE/NBC
 * /Project/RNASeq_GF110614/rawdata/data/Col_L2_1.fq
 * /media/winE/NBC/Project/RNASeq_GF110614
 * /rawdata/data/Col_L1_2.fq,/media/winE/NBC
 * /Project/RNASeq_GF110614/rawdata/data/Col_L2_2.fq
 * 
 * @author zong0jie
 * 
 */
public class MapTophat implements MapRNA {
	private static Logger logger = LoggerFactory.getLogger(MapTophat.class);
	public static final String UnmapSuffix = ".tophat.unmapped.bam";//没有mapping的bam文件的前缀
	/** mapping文件的后缀，包含 ".bam" 字符串 */
	public static final String TophatSuffix = ".tophat.sorted.bam";
	/** tophat mapping完后又用bowtie2 mapping的后缀，包含 ".bam" 字符串 */
	public static final String TophatAllSuffix = ".tophatAll.bam";
	
	StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	
	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	int anchorLength = 10;
	/** anchor上的mismithch，默认为0 */
	int anchorMismatch = 0;
	
	int intronLenMin = 50;
	int intronLenMax = 500000;
	
	/** 序列中包含的全部indel长度，默认为3 */
	int indelLen = 6;
	int mismatch = 3;
	
	int threadNum = 4;
	
	/** 默认是solexa的最长插入 */
	int maxInsert = 450;
	
	
	/** 给定GTF的文件 */
	String gtfFile = "";
	
	/** 输出文件 */
	String outPathPrefix = "";
	
	String chrIndexFile;
	
	/** 只有基于数据库的才需要把gtf文件移动到chr文件边上 */
	boolean isMoveGtfToChr = false;
	/** bowtie就是用来做索引的 */
	IndexTophat indexMaker;

	
	int sensitiveLevel = MapBowtie2.Sensitive_Sensitive;
	
	/** 将没有mapping上的reads用bowtie2比对到基因组上，仅用于proton数据 */
	boolean mapUnmapedReads = false;
	/** 比对到的index */
	String bwaIndex;
		
	/** 第二次mapping所使用的命令 */
	List<String> 	lsCmdMapping2nd = new ArrayList<>();
		
	public MapTophat() {
		indexMaker = (IndexTophat)IndexMappingMaker.createIndexMaker(SoftWare.tophat);
		indexMaker.setBowtieVersion(SoftWare.bowtie2);	
	}
	/** 如果有gtf文件，是否将gtf文件移动到chr文件同一文件夹下并建索引
	 * 只有基于数据库的才需要把gtf文件移动到chr文件边上 */
	public void setMoveGtfToChr(boolean isMoveGtfToChr) {
	    this.isMoveGtfToChr = isMoveGtfToChr;
    }
	
	public void setBowtieVersion(SoftWare bowtieVersion) {
		indexMaker.setBowtieVersion(bowtieVersion);
	}
	
	/**
	 * 是否将没有mapping上的reads用bowtie2比对到基因组上，<b>注意目前仅用于proton数据</b>
	 * @param mapUnmapedReads
	 * @param bowtie2ChrIndex
	 */
	public void setMapUnmapedReads(boolean mapUnmapedReads, String bwaIndex) {
		this.mapUnmapedReads = mapUnmapedReads;
		if (mapUnmapedReads) {
			this.bwaIndex = bwaIndex;
		}
	}

	public void setRefIndex(String chrFile) {
		this.chrIndexFile = chrFile;
	}
	
	/** 设定reads的敏感性，越敏感速度越慢
	 * {@link #Sensitive_Sensitive}这种
	 *  */
	public void setSensitiveLevel(int sensitiveLevel) {
		this.sensitiveLevel = sensitiveLevel;
	}
	
	/**
	 * 以 / 结尾表示是输入到文件夹，就不做修饰
	 * 如果以名字结尾表示输入到文件，最后把bam文件和junction文件改名并提取到上层文件夹下
	 */
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/** 在junction 的一头上至少要搭到多少bp的碱基，默认为10 */
	public void setAnchorLength(int anchorLength) {
		this.anchorLength = anchorLength;
	}
	/** 设定indel */
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}
	/**
	 * 内含子最长多少，默认500000，需根据不同物种进行设置
	 * 
	 * @param intronLenMax
	 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
	}
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
	}
	/** anchor上的mismithch，默认为0，最多设置为1 */
	public void setAnchorMismatch(int anchorMismatch) {
		this.anchorMismatch = anchorMismatch;
	}
	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum) {
		if (threadNum <= 0) {
			threadNum = 1;
		}
		this.threadNum = threadNum;
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
	
	private String[] getOutPathPrefix() {
		return new String[]{"-o", outPathPrefix};
	}
	/**
	 * 插入长度，默认是illumina：450
	 * 
	 * @param insert
	 */
	public void setInsert(int insert) {
		maxInsert = insert;
	}
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq.clear();
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq.clear();
		if (lsRightFastQs == null) return;
		this.lsRightFq = lsRightFastQs;
	}
	
	private List<String> getLsFqFile() {
		List<String> lsCmd = new ArrayList<>();
		String lsFileName = lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			lsFileName = lsFileName + "," + lsLeftFq.get(i).getReadFileName();
		}
		lsCmd.add(lsFileName);
		if (lsRightFq.size() > 0) {
			lsFileName = lsRightFq.get(0).getReadFileName();
			for (int i = 1; i < lsRightFq.size(); i++) {
				lsFileName = lsFileName + "," + lsRightFq.get(i).getReadFileName();
			}
			lsCmd.add(lsFileName);
		}
		return lsCmd;
	}
	
	private boolean isPairend() {
		boolean pairend;
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0) {
			pairend = true;
		} else {
			pairend = false;
		}
		return pairend;
	}
	/**
	 * -r 150等，表示pairend中间的长度
	 * @return
	 */
	private String[] getInsert() {
		FastQ fastQ = lsLeftFq.get(0);
		int len = fastQ.getReadsLenAvg();
		return new String[]{"-r", (maxInsert - len * 2) + ""};
	}

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	private String[] getAnchoLen() {
		return new String[]{"-a", anchorLength + ""};
	}
	private String[] getAnchorMismatch() {
		return new String[]{"-m", anchorMismatch + ""};
	}
	
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private String[] getIntronLenMin() {
		return new String[]{"-i", intronLenMin + ""};
	}

	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private String[] getIntronLenMax() {
		return new String[]{"-I", intronLenMax + ""};
	}
	private List<String> getIndelLen() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("--max-insertion-length"); lsCmd.add(indelLen + "");
		lsCmd.add("--max-deletion-length"); lsCmd.add(indelLen + "");
		lsCmd.add("--read-gap-length"); lsCmd.add((int)(indelLen * 1.2) + "");
		lsCmd.add("--read-edit-dist"); lsCmd.add((int)(indelLen*1.5 + mismatch) + "");
		return lsCmd;
	}

	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}

	/** 是否使用bowtie2进行分析 */
	private String getBowtie() {
		if (indexMaker.getBowtieSoft() == SoftWare.bowtie) {
			return "--bowtie1";
		}
		else if (indexMaker.getBowtieSoft() == SoftWare.bowtie2) {
			return null;
		}
		return null;
	}

	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return "--solexa1.3-quals";
		}
		return null;
	}

	/** 错配，这个走默认比较好，默认为2 */
	public void setMismatch(int mismatch) {
		if (mismatch > 2) {
			mismatch = 2;
		}
		this.mismatch = mismatch;
	}

	/** 错配，这个走默认比较好，默认为2 */
	private String[] getMismatch() {
		return new String[]{"--read-mismatches", mismatch + ""};
	}
	
	private String getSensitive() {
		if (sensitiveLevel == MapBowtie2.Sensitive_Fast) {
			return "--b2-fast";
		} else if (sensitiveLevel == MapBowtie2.Sensitive_Very_Fast) {
			return "--b2-very-fast";
		} else if (sensitiveLevel == MapBowtie2.Sensitive_Sensitive) {
			return "--b2-sensitive";
		} else if (sensitiveLevel == MapBowtie2.Sensitive_Very_Sensitive) {
			return "--b2-very-sensitive";
		}
		return null;
	}
	
	private List<String> getMinCoverageIntron() {
		List<String> lsCmd = new ArrayList<>();
		if (intronLenMin < 50) {
			lsCmd.add("--min-coverage-intron"); lsCmd.add(intronLenMin + "");
			lsCmd.add("--min-segment-intron"); lsCmd.add(intronLenMin + "");
		}
		return lsCmd;
	}

	private String[] getMaxCoverageIntron() {
		if (intronLenMax < 20000) {
			return new String[]{"--max-coverage-intron", intronLenMax + ""};
		}
		return null;
	}

	private String[] getMaxSegmentIntron() {
		if (intronLenMax < 500000) {
			return new String[]{"--max-segment-intron", intronLenMax + ""};
		}
		return null;
	}
	/**
	 * 用gtf文件辅助mapping
	 * 如果设定为null，则表示不使用gtf文件
	 * 如果设定为“”等不存在文件的，则使用GffChrAbs中的Gff文件
	 * @param gtfFile
	 */
	public void setGtfFiles(String gtfFile) {
		this.gtfFile = gtfFile;
		if (!FileOperate.isFileExistAndBigThan0(gtfFile)) return;
		
		this.gtfFile = gtfFile;
		GffHashGene gffHashGene = new GffHashGene(gtfFile);
//		int[] intronMinMax = getIntronMinMax(gffHashGene, this.intronLenMin, this.intronLenMax);
//		this.intronLenMin = intronMinMax[0];
//		this.intronLenMax = intronMinMax[1];
	}
	
	/**
	 * 先不设定，考虑集成--transcriptome-index那个选项
	 * @return
	 */
	private List<String> getGtfFile() {
		List<String> lsCmd = new ArrayList<>();
		if (FileOperate.isFileExistAndBigThanSize(gtfFile, 0.1)) {
			lsCmd.add("--transcriptome-index=" + indexMaker.getIndexGff());
		}
		return lsCmd;
	}
	
	/**
	 * 返回链的方向
	 * @return
	 */
	private String[] getStrandSpecifictype() {
		if (lsRightFq == null || lsRightFq.isEmpty()) {
			return null;
		}
		
		String[] cmd = null;
		if (strandSpecifictype == StrandSpecific.NONE) {
			
		} else if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			cmd = new String[]{"--library-type", "fr-secondstrand"};
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			cmd = new String[]{"--library-type", "fr-firststrand"};
		}
		return cmd;
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public void mapReads() {
		indexMaker.setChrIndex(chrIndexFile);
		indexMaker.setGtfFile(gtfFile, isMoveGtfToChr);
		indexMaker.IndexMake();
				
		lsCmdMapping2nd.clear();

		String prefix = FileOperate.getFileName(outPathPrefix);
		String parentPath = FileOperate.getParentPathNameWithSep(outPathPrefix);
		String tophatBam = parentPath + prefix + TophatSuffix;
		String unmappedBam = parentPath + prefix + UnmapSuffix;
		if (!FileOperate.isFileExistAndBigThanSize(tophatBam, 1_000_000) ||
				!FileOperate.isFileExistAndBigThanSize(unmappedBam, 1_000)
				) {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			cmdOperate.setRedirectInToTmp(true);
			for (FastQ fqL : lsLeftFq) {
				cmdOperate.addCmdParamInput(fqL.getReadFileName());
			}
			for (FastQ fqR : lsRightFq) {
				cmdOperate.addCmdParamInput(fqR.getReadFileName());
			}
			
			cmdOperate.setRedirectOutToTmp(true);
			cmdOperate.addCmdParamOutput(outPathPrefix);
			cmdOperate.run();
			if (!cmdOperate.isFinishedNormal()) {
				FileOperate.deleteFileFolder(FileOperate.addSep(outPathPrefix) + "tmp");
				throw new ExceptionCmd("error running tophat:" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
			changeFileName();
		}

		if (mapUnmapedReads) {
			String finalBam = FileOperate.getParentPathNameWithSep(outPathPrefix) + prefix + TophatAllSuffix;
			lsCmdMapping2nd = mapUnmapedReads(threadNum, bwaIndex, tophatBam, unmappedBam, finalBam);
		}
	}	
	
	private List<String> getLsCmd() {
		// linux命令如下
		/**
		 * tophat -r 120 -a 10 -m 1 -i 20 -I 6000 --solexa1.3-quals -F 0.15 -p 4
		 * --coverage-search --min-coverage-intron 20 --max-coverage-intron 6000
		 * --min-segment-intron 20 --max-segment-intron 6000 -G
		 * /media/winE/Bioinformatics/GenomeData/Arabidopsis\
		 * TAIR9/TAIR10GFF/TAIR10_GTF3_genes.gtf -o
		 * /media/winE/NBC/Project/RNASeq_GF110614/rawdata/tophatResult/
		 * /media/winE/Bioinformatics/GenomeData/Arabidopsis\
		 * TAIR9/ChromFa/TAIR10_ath
		 * /media/winE/NBC/Project/RNASeq_GF110614/rawdata
		 * /data/Col_L1_1.fq,/media
		 * /winE/NBC/Project/RNASeq_GF110614/rawdata/data/Col_L2_1.fq
		 * /media/winE
		 * /NBC/Project/RNASeq_GF110614/rawdata/data/Col_L1_2.fq,/media
		 * /winE/NBC/Project/RNASeq_GF110614/rawdata/data/Col_L2_2.fq
		 */
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(indexMaker.getExePath() + "tophat"); addLsCmdParam(lsCmd, getBowtie());
		if (isPairend()) {
			ArrayOperate.addArrayToList(lsCmd, getInsert());
		}
		ArrayOperate.addArrayToList(lsCmd, getAnchoLen());
		ArrayOperate.addArrayToList(lsCmd, getAnchorMismatch());
		ArrayOperate.addArrayToList(lsCmd, getIntronLenMin());
		ArrayOperate.addArrayToList(lsCmd, getIntronLenMax());
		lsCmd.addAll(getGtfFile());
		if (indexMaker.getBowtieSoft() == SoftWare.bowtie2) {
			ArrayOperate.addArrayToList(lsCmd, getMismatch());
			lsCmd.addAll(getIndelLen());
			addLsCmdParam(lsCmd, getSensitive());
		}
		addLsCmdParam(lsCmd, getOffset());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getStrandSpecifictype());
		lsCmd.addAll(getMinCoverageIntron());
		ArrayOperate.addArrayToList(lsCmd, getMaxCoverageIntron());
		ArrayOperate.addArrayToList(lsCmd, getMaxSegmentIntron());
		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefix());
		lsCmd.add(indexMaker.getIndexName());
		lsCmd.addAll(getLsFqFile());
		return lsCmd;
	}
	
	private void addLsCmdParam(List<String> lsCmd, String param) {
		if(param == null) return;
		lsCmd.add(param);
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("tophat version: " + indexMaker.getVersion());
		lsCmd.add(indexMaker.getBowtieSoft() + " version: " + indexMaker.getVersionBowtie());
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
		if (!lsCmdMapping2nd.isEmpty()) {
			lsCmd.addAll(lsCmdMapping2nd);
		}
		return lsCmd;
	}
	
	private void changeFileName() {
		if (outPathPrefix.endsWith("/") || outPathPrefix.endsWith("\\")) {
			return;
		}
		String prefix = FileOperate.getFileName(outPathPrefix);
		String parentPath = FileOperate.getParentPathNameWithSep(outPathPrefix);
		FileOperate.moveFile(FileOperate.addSep(outPathPrefix) + "accepted_hits.bam", parentPath, prefix + TophatSuffix,false);
		FileOperate.moveFile(FileOperate.addSep(outPathPrefix) + "unmapped.bam", parentPath, prefix + UnmapSuffix,false);
		FileOperate.moveFile(FileOperate.addSep(outPathPrefix) + "junctions.bed", parentPath, prefix + ".junctions.bed",false);
	}
	
	/**
	 *  将没有mapping上的reads重新mapping，仅用于单端的proton数据
	 * @param threadNum
	 * @param bwaIndex
	 * @param acceptedBam
	 * @param unmappedBam mapsplice本项填null
	 * @param outFinalBam
	 * @return 返回使用的命令行
	 */
	protected static List<String> mapUnmapedReads(int threadNum, String bwaIndex, 
			String acceptedBam, String unmappedBam, String outFinalBam) {
		String unmappedFq = null;
		String unmapBamGetSeq = unmappedBam;
		if (unmappedBam == null) {
			unmappedFq = FileOperate.changeFileSuffix(acceptedBam, "_unmapped", "fq.gz");
			unmapBamGetSeq = acceptedBam;
		} else {
			unmappedFq = FileOperate.changeFileSuffix(unmappedBam, "", "fq.gz");
		}
		//没有比对上的reads用bowtie2再次比对
		String mapBowtieBam = FileOperate.changeFileSuffix(unmappedFq, "_bwaMap", "bam");
		SamFile samFileMapBowtie = null;
		MapBwaMem mapBwaMem = null;
		if (!FileOperate.isFileExistAndBigThanSize(mapBowtieBam, 1_000_000)) {
			SamFile unmappedSam = new SamFile(unmapBamGetSeq);
			SamToFastq samToFastq = new SamToFastq();
			samToFastq.setOutFileInfo(unmappedSam.isPairend(), unmappedFq, EnumSamToFastqType.UnmappedReads);
			samToFastq.initialFq();
			AlignSamReading alignSamReading = new AlignSamReading(unmappedSam);
			alignSamReading.addAlignmentRecorder(samToFastq);
			alignSamReading.run();
			
			FastQ[] fastQ = samToFastq.getResultFastQ();
			mapBwaMem = new MapBwaMem();
			mapBwaMem.setThreadNum(threadNum);
			mapBwaMem.setChrIndex(bwaIndex);
			if (fastQ.length == 2) {
				mapBwaMem.setFqFile(fastQ[0], fastQ[1]);
			} else {
				mapBwaMem.setFqFile(fastQ[0], null);
			}
			String mapFile = FileOperate.changeFileSuffix(mapBowtieBam, "_TmpMapping", "bam");
			mapBwaMem.setOutFileName(mapFile);
			mapBwaMem.mapReads();
			FileOperate.moveFile(true, mapFile, mapBowtieBam);
		}
		samFileMapBowtie = new SamFile(mapBowtieBam);
		SamFile samFileTophat = new SamFile(acceptedBam);
		SAMFileHeader header = samFileTophat.getHeader();
		header.setSortOrder(SortOrder.unsorted);
		String outFinalBamTmp = FileOperate.changeFileSuffix(outFinalBam, "_tmpMerge", null);
		SamFile samFileOut = new SamFile(outFinalBamTmp, header);
		for (SamRecord samRecord : samFileTophat.readLines()) {
			if (samRecord.isMapped()) {
				samFileOut.writeSamRecord(samRecord);
			}
		}
		samFileTophat.close();
		for (SamRecord samRecord : samFileMapBowtie.readLines()) {
			samFileOut.writeSamRecord(samRecord);
		}
		samFileMapBowtie.close();
		samFileOut.close();
		FileOperate.moveFile(true, outFinalBamTmp, outFinalBam);
		if (mapBwaMem != null) {
			return mapBwaMem.getCmdExeStr();
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public String getFinishName() {
		String prefix = FileOperate.getFileName(outPathPrefix);
		String parentPath = FileOperate.getParentPathNameWithSep(outPathPrefix);
		if (!mapUnmapedReads) {
			return parentPath + prefix + TophatSuffix;
		} else {
			return parentPath + prefix + TophatAllSuffix;
		}
	}
	@Override
	public IndexMappingMaker getIndexMappingMaker() {
	    return indexMaker;
    }
	
	protected static int[] getIntronMinMax(GffHashGene gffHashGene, int intronMinDefault, int intronMaxDefault) {
		int[] result = new int[]{intronMinDefault, intronMaxDefault};
		
		if (gffHashGene == null) {
			return result;
		}
		
		ArrayList<Integer> lsIntronSortedS2M = gffHashGene.getLsIntronSortedS2M();
		if (lsIntronSortedS2M.size() < 50) {
			return result;
		}
//		int intronLenMin = lsIntronSortedS2M.get(50);
		int intronLenMax = lsIntronSortedS2M.get(lsIntronSortedS2M.size() - 10);
//		if (intronLenMin < result[0]) {
//			result[0] = intronLenMin;
//		}
//		if (intronLenMin < 20) {
//			result[0] = 20;
//		}
		if (intronLenMax*2 < result[1]) {
			result[1] = intronLenMax*2;
		}
		
		return result;
	}
}
