package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.FastQOld;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

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
public class MapTophat {
	private static Logger logger = Logger.getLogger(MapTophat.class);

	/** 可能是表示有方向的测序，无方向 */
	public static final int STRAND_NULL = 0;
	/** 可能是表示有方向的测序，第一条链的方向 */
	public static final int STRAND_FIRSTSTRAND = 0;
	/** 可能是表示有方向的测序，第二条链的方向 */
	public static final int STRAND_SECONDSTRAND = 0;
	int strandSpecifictype = STRAND_NULL;
	ArrayList<FastQ> lsLeftFq = new ArrayList<FastQ>();
	ArrayList<FastQ> lsRightFq = new ArrayList<FastQ>();
	/** bowtie所在路径 */
	String ExePathBowtie = "";
	/** 待比对的染色体 */
	String chrFile = "";
	/** 默认用bowtie2 做mapping */
	int bowtieVersion = MapBowtie.VERSION_BOWTIE2;
	/** 是否为双端测序 */
	boolean pairend = false;
	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	int anchorLength = 10;
	/** anchor上的mismithch，默认为0 */
	int anchorMismatch = 0;
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	int intronLenMin = 50;
	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	int intronLenMax = 500000;
	/** indel的长度，默认为3 */
	int indelLen = 3;
	/** 线程数 */
	int threadNum = 4;
	/** 默认是solexa的最长插入 */
	int maxInsert = 450;
	/** 错配，这个走默认比较好，默认为2 */
	int mismatch = 2;
	/** 给定GTF的文件 */
	String gtfFile = "";
	/** 输出文件 */
	String outPathPrefix = "";
	MapBowtie mapBowtie = new MapBowtie();
	
	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * 
	 * @param exePath
	 *            如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePathBowtie, String chrFile) {
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.ExePathBowtie = "";
		else
			this.ExePathBowtie = FileOperate.addSep(exePathBowtie);
		this.chrFile = chrFile;
		mapBowtie.setExePath(exePathBowtie, chrFile);
	}

	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}

	private String getOutPathPrefix() {
		return "-o " + outPathPrefix + " ";
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
	 * 
	 * @param fqFile
	 */
	public void setLeftFq(String... fqFile) {
		lsLeftFq.clear();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string, FastQ.QUALITY_MIDIAN);
			lsLeftFq.add(fastQ);
		}
	}

	/**
	 * 设置右端的序列，设置会把以前的清空
	 * 
	 * @param fqFile
	 */
	public void setRightFq(String... fqFile) {
		lsRightFq.clear();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string, FastQ.QUALITY_MIDIAN);
			lsRightFq.add(fastQ);
		}
	}

	/**
	 * -r 150等，表示pairend中间的长度
	 * 
	 * @return
	 */
	private String getInsert() {
		FastQ fastQ = lsLeftFq.get(0);
		int len = fastQ.getReadsLenAvg();
		return "-r " + (maxInsert - len * 2) + " ";
	}

	/** 在junction 的一头上至少要搭到多少bp的碱基，默认为10 */
	public void setAnchorLength(int anchorLength) {
		this.anchorLength = anchorLength;
	}

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	private String getAnchoLen() {
		return "-a " + anchorLength + " ";
	}

	/** anchor上的mismithch，默认为0，最多设置为1 */
	public void setAnchorMismatch(int anchorMismatch) {
		this.anchorMismatch = anchorMismatch;
	}

	private String getAnchorMismatch() {
		return "-m " + anchorMismatch + " ";
	}

	/**
	 * 内含子最短多少，默认50，需根据不同物种进行设置
	 * 
	 * @param intronLenMin
	 */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
	}

	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	private String getIntronLenMin() {
		return "-i " + intronLenMin + " ";
	}

	/**
	 * 内含子最长多少，默认500000，需根据不同物种进行设置
	 * 
	 * @param intronLenMax
	 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
	}

	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private String getIntronLenMax() {
		return "-I " + intronLenMax + " ";
	}

	/** 设定indel */
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}

	private String getIndelLen() {
		return "--max-insertion-length " + indelLen + " --max-deletion-length "
				+ indelLen + " ";
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
	 * 是否使用bowtie2进行分析
	 * 
	 * @param bowtie2
	 */
	public void setBowtieVersion(int bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
		mapBowtie.setBowtieVersion(bowtieVersion);
	}

	/** 是否使用bowtie2进行分析 */
	private String getBowtie() {
		if (bowtieVersion == MapBowtie.VERSION_BOWTIE1) {
			return " --bowtie1 ";
		}
		else if (bowtieVersion == MapBowtie.VERSION_BOWTIE2) {
			return "";
		}
		return "";
	}

	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return " --solexa1.3-quals ";
		}
		return "";
	}

	/** 错配，这个走默认比较好，默认为2 */
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}

	/** 错配，这个走默认比较好，默认为2 */
	public String getMismatch() {
		return "--read-mismatches " + mismatch + " ";
	}

	private String getMinCoverageIntron() {
		if (intronLenMin < 50) {
			return "--min-coverage-intron " + intronLenMin
					+ " --min-segment-intron " + intronLenMin + " ";
		}
		return "";
	}

	private String getMaxCoverageIntron() {
		if (intronLenMax < 20000) {
			return "--max-coverage-intron " + intronLenMax + " ";
		}
		return "";
	}

	private String getMaxSegmentIntron() {
		if (intronLenMax < 500000) {
			return "--max-segment-intron " + intronLenMax + " ";
		}
		return "";
	}

	/**
	 * 用gtf文件辅助mapping
	 * 
	 * @param gtfFile
	 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}

	/**
	 * 先不设定，考虑集成--transcriptome-index那个选项
	 * 
	 * @return
	 */
	public String getGtfFile() {
		if (FileOperate.isFileExist(gtfFile)) {
			return "-G " + gtfFile + " ";
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
	public void setStrandSpecifictype(int strandSpecifictype) {
		this.strandSpecifictype = strandSpecifictype;
	}

	/**
	 * 返回链的方向
	 * 
	 * @return
	 */
	private String getStrandSpecifictype() {
		if (strandSpecifictype == STRAND_NULL) {
			return "";
		} else if (strandSpecifictype == STRAND_FIRSTSTRAND) {
			return "--library-type fr-firststrand";
		} else if (strandSpecifictype == STRAND_SECONDSTRAND) {
			return "--library-type fr-secondstrand";
		}
		return "";
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public SamFile mapReads() {
		mapBowtie.IndexMakeBowtie();
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0)
			pairend = true;
		else
			pairend = false;
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

		String cmd = "";
		cmd = ExePathBowtie + "tophat " + getBowtie();
		if (pairend) {
			cmd = cmd + getInsert(); // 插入长度
		}
		cmd = cmd + getAnchoLen() + getAnchorMismatch() + getIntronLenMin()
				+ getIntronLenMax() + getIndelLen();
		if (bowtieVersion == MapBowtie.VERSION_BOWTIE2) {
			cmd = cmd + getIndelLen();
		}
		cmd = cmd + getOffset() + getThreadNum();
		cmd = cmd + getStrandSpecifictype();
		cmd = cmd + getMinCoverageIntron() + getMaxCoverageIntron()
				+ getMaxSegmentIntron();
		cmd = cmd + getOutPathPrefix();

		cmd = cmd + " " + chrFile + " ";
		cmd = cmd + " " + lsLeftFq.get(0).getFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			cmd = cmd + "," + lsLeftFq.get(i).getFileName();
		}
		if (lsRightFq.size() > 0) {
			cmd = cmd + " " + lsRightFq.get(0).getFileName();
			for (int i = 1; i < lsRightFq.size(); i++) {
				cmd = cmd + "," + lsRightFq.get(i).getFileName();
			}
		}
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMapping");
		cmdOperate.run();
		return null;// 最后考虑返回一个bam文件
	}
}
