package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

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
	private static Logger logger = Logger.getLogger(MapTophat.class);

	StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	/** bowtie所在路径 */
	String ExePathTophat = "";
	/** 默认用bowtie2 做mapping */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	
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
	/** 是否自动产生GTF，如果产生了，最后要删除的 */
	boolean generateGtfFile = false;
	
	/** 输出文件 */
	String outPathPrefix = "";
	/** bowtie就是用来做索引的 */
	MapBowtie mapBowtie = new MapBowtie();
	GffChrAbs gffChrAbs;
	boolean booSetIntronMin = false;
	boolean booSetIntronMax = false;
	
	/** 输入的gffChrAbs中只需要含有GffHashGene即可 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * 
	 * @param exePath
	 *            如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePathTophat, String exePathBowtie) {
		if (exePathTophat == null || exePathTophat.trim().equals(""))
			this.ExePathTophat = "";
		else
			this.ExePathTophat = FileOperate.addSep(exePathTophat);
		mapBowtie.setExePathBowtie(exePathBowtie);
	}
	public void setFileRef(String chrFile) {
		mapBowtie.setChrFile(chrFile);
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
	@Override
	public SoftWare getBowtieVersion() {
		return bowtieVersion;
	}
	/**
	 * 内含子最长多少，默认500000，需根据不同物种进行设置
	 * 
	 * @param intronLenMax
	 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
		booSetIntronMax = true;
	}
	/** 内含子最短多少，默认50，需根据不同物种进行设置 */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
		booSetIntronMin = true;
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
	/**
	 * 是否使用bowtie2进行分析
	 * 
	 * @param bowtie2
	 */
	public void setBowtieVersion(SoftWare bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	
	private String getOutPathPrefix() {
		return "-o " + CmdOperate.addQuot(outPathPrefix) + " ";
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
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		if (lsRightFastQs == null) return;
		
		this.lsRightFq = lsRightFastQs;
	}
	
	private String getLsFqFile() {
		String lsFileName = CmdOperate.addQuot(lsLeftFq.get(0).getReadFileName());
		for (int i = 1; i < lsLeftFq.size(); i++) {
			lsFileName = lsFileName + "," + CmdOperate.addQuot(lsLeftFq.get(i).getReadFileName());
		}
		if (lsRightFq.size() > 0) {
			lsFileName = lsFileName + " " + CmdOperate.addQuot(lsRightFq.get(0).getReadFileName());
			for (int i = 1; i < lsRightFq.size(); i++) {
				lsFileName = lsFileName + "," + CmdOperate.addQuot(lsRightFq.get(i).getReadFileName());
			}
		}
		return lsFileName;
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
	private String getInsert() {
		FastQ fastQ = lsLeftFq.get(0);
		int len = fastQ.getReadsLenAvg();
		return "-r " + (maxInsert - len * 2) + " ";
	}

	/** 在junction 的一头上至少要搭到多少bp的碱基 */
	private String getAnchoLen() {
		return "-a " + anchorLength + " ";
	}
	private String getAnchorMismatch() {
		return "-m " + anchorMismatch + " ";
	}
	private void setIntronLen() {
		if (booSetIntronMax && booSetIntronMin) {
			return;
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			ArrayList<Integer> lsIntronSortedS2M = gffChrAbs.getGffHashGene().getLsIntronSortedS2M();
			if (lsIntronSortedS2M.size() < 50) {
				return;
			}
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
		return "-i " + intronLenMin + " ";
	}

	/** 内含子最长多少，默认500000，需根据不同物种进行设置 */
	private String getIntronLenMax() {
		return "-I " + intronLenMax + " ";
	}
	private String getIndelLen() {
		String param = "--max-insertion-length " + indelLen + " --max-deletion-length " + 
				indelLen + " " + "--read-gap-length " + (int)(indelLen * 1.2) + " --read-edit-dist " + (int)(indelLen*1.5 + mismatch) + " ";
		return param;
	}

	private String getThreadNum() {
		return "-p " + threadNum + " ";
	}

	/** 是否使用bowtie2进行分析 */
	private String getBowtie() {
		if (bowtieVersion == SoftWare.bowtie) {
			return " --bowtie1 ";
		}
		else if (bowtieVersion == SoftWare.bowtie2) {
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
		if (mismatch > 2) {
			mismatch = 2;
		}
		this.mismatch = mismatch;
	}

	/** 错配，这个走默认比较好，默认为2 */
	private String getMismatch() {
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
	 * 如果设定为null，则表示不使用gtf文件
	 * 如果设定为“”等不存在文件的，则使用GffChrAbs中的Gff文件
	 * @param gtfFile
	 */
	public void setGtf_Gene2Iso(String gtfFile) {
		this.gtfFile = gtfFile;
	}

	/**
	 * 先不设定，考虑集成--transcriptome-index那个选项
	 * @return
	 */
	private String getGtfFile() {
		if (FileOperate.isFileExist(gtfFile)) {
			return "-G \"" + gtfFile + "\" ";
		}
		return "";
	}
	private void setGTFfile() {
		if (gtfFile == null || FileOperate.isFileExistAndBigThanSize(gtfFile, 0.1)) {
			generateGtfFile = false;
			logger.error("not generate GTF");
			return;
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			String path = FileOperate.getParentPathName(lsLeftFq.get(0).getReadFileName());
			String outGTF = path + FileOperate.getFileNameSep(gffChrAbs.getGffHashGene().getGffFilename())[0] + DateUtil.getDateAndRandom() + ".GTF";
			gffChrAbs.getGffHashGene().writeToGTF(outGTF, "novelbio");
			logger.error("Generate GTF To:" + outGTF);
			this.gtfFile = outGTF;
			generateGtfFile = true;
		} else {
			logger.error("Error Not Generate GTF To:");
		}
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
	public void mapReads() {
		setIntronLen();
		setGTFfile();
		mapBowtie.setSubVersion(bowtieVersion);
		mapBowtie.IndexMake(false);
		
		String cmd = getCmd();
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMapping");
		cmdOperate.run();
		
		if (generateGtfFile) {
			FileOperate.delFile(gtfFile);
		}
		changeFileName();
	}
	
	private String getCmd() {
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
		cmd = ExePathTophat + "tophat " + getBowtie();
		if (isPairend()) {
			cmd = cmd + getInsert(); // 插入长度
		}
		cmd = cmd + getAnchoLen() + getAnchorMismatch() + getIntronLenMin() + getIntronLenMax() + getGtfFile();
//		//本步很慢，一般不使用
//		cmd = cmd + "--coverage-search ";
		if (bowtieVersion == SoftWare.bowtie2) {
			cmd = cmd + getMismatch() + getIndelLen();
		}
		cmd = cmd + getOffset() + getThreadNum();
		cmd = cmd + getStrandSpecifictype();
		cmd = cmd + getMinCoverageIntron() + getMaxCoverageIntron() + getMaxSegmentIntron();
		cmd = cmd + getOutPathPrefix();

		cmd = cmd + " " + mapBowtie.getChrNameWithoutSuffix() + " ";
		cmd = cmd + " " + getLsFqFile();
		return cmd;
	}
	
	private void changeFileName() {
		if (outPathPrefix.endsWith("/") || outPathPrefix.endsWith("\\")) {
			return;
		}
		String prefix = FileOperate.getFileName(outPathPrefix);
		String parentPath = FileOperate.getParentPathName(outPathPrefix);
		FileOperate.moveFile(FileOperate.addSep(outPathPrefix) + "accepted_hits.bam", parentPath, prefix + "_accepted_hits.bam",false);
		FileOperate.moveFile(FileOperate.addSep(outPathPrefix) + "junctions.bed", parentPath, prefix + "_junctions.bed",false);
	}
}
