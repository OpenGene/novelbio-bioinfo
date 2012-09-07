package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/**
 * ��û���ؽ����bam�ļ� tophat��mapping * tophat -r 120 -a 10 -m 1 -i 20 -I 6000
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
public class MapTophat implements MapRNA{
	public static void main(String[] args) {
		String fastqFile = "/media/winF/NBC/Project/RNA-Seq_HPWtest/FangLan/3_AGTTCC_L003_R1_001_filtered.fq";
		Species species = new Species(10090);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		MapTophat mapTophat = new MapTophat();
		mapTophat.setAnchorLength(10);
		mapTophat.setAnchorMismatch(0);
		mapTophat.setBowtieVersion(MapBowtie.VERSION_BOWTIE1);
		mapTophat.setExePath("", species.getIndexChr(SoftWare.bowtie));
		mapTophat.setGffChrAbs(gffChrAbs);
		mapTophat.setLeftFq(fastqFile);
		mapTophat.setOutPathPrefix("/media/winF/NBC/Project/RNA-Seq_HPWtest/FangLan/tophatN");
		mapTophat.setThreadNum(4);
		mapTophat.mapReads();
	}
	
	private static Logger logger = Logger.getLogger(MapTophat.class);

	/** �����Ǳ�ʾ�з���Ĳ����޷��� */
	public static final int STRAND_NULL = 0;
	/** �����Ǳ�ʾ�з���Ĳ��򣬵�һ�����ķ��� */
	public static final int STRAND_FIRSTSTRAND = 0;
	/** �����Ǳ�ʾ�з���Ĳ��򣬵ڶ������ķ��� */
	public static final int STRAND_SECONDSTRAND = 0;
	int strandSpecifictype = STRAND_NULL;
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	/** bowtie����·�� */
	String ExePathTophat = "";
	/** ���ȶԵ�Ⱦɫ�� */
	String chrFile = "";
	/** Ĭ����bowtie2 ��mapping */
	SoftWare bpwtieVersion = SoftWare.bowtie;

	boolean pairend = false;
	
	/** ��junction ��һͷ������Ҫ�����bp�ļ�� */
	int anchorLength = 10;
	/** anchor�ϵ�mismithch��Ĭ��Ϊ0 */
	int anchorMismatch = 0;
	
	int intronLenMin = 50;
	int intronLenMax = 500000;
	/** indel�ĳ��ȣ�Ĭ��Ϊ3 */
	int indelLen = 3;
	
	int threadNum = 4;
	
	/** Ĭ����solexa������� */
	int maxInsert = 450;
	int mismatch = 2;
	
	/** ����GTF���ļ� */
	String gtfFile = "";
	/** ����ļ� */
	String outPathPrefix = "";
	/** bowtie���������������� */
	MapBowtie mapBowtie = new MapBowtie();
	GffChrAbs gffChrAbs;
	boolean booSetIntronMin = false;
	boolean booSetIntronMax = false;
	
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * 
	 * @param exePath
	 *            ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePathTophat, String exePathBowtie) {
		if (exePathTophat == null || exePathTophat.trim().equals(""))
			this.ExePathTophat = "";
		else
			this.ExePathTophat = FileOperate.addSep(exePathTophat);
		mapBowtie.setExePath(exePathBowtie, chrFile);
	}
	public void setFileRef(String chrFile) {
		this.chrFile = chrFile;
	}
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/** ��junction ��һͷ������Ҫ�����bp�ļ����Ĭ��Ϊ10 */
	public void setAnchorLength(int anchorLength) {
		this.anchorLength = anchorLength;
	}
	/** �趨indel */
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}
	@Override
	public SoftWare getBowtieVersion() {
		return bpwtieVersion;
	}
	/**
	 * �ں�������٣�Ĭ��500000������ݲ�ͬ���ֽ�������
	 * 
	 * @param intronLenMax
	 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
		booSetIntronMax = true;
	}
	/** �ں�����̶��٣�Ĭ��50������ݲ�ͬ���ֽ������� */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
		booSetIntronMin = true;
	}
	/** anchor�ϵ�mismithch��Ĭ��Ϊ0���������Ϊ1 */
	public void setAnchorMismatch(int anchorMismatch) {
		this.anchorMismatch = anchorMismatch;
	}
	/** �߳�������Ĭ��4�߳� */
	public void setThreadNum(int threadNum) {
		if (threadNum <= 0) {
			threadNum = 1;
		}
		this.threadNum = threadNum;
	}
	/**
	 * STRAND_NULL�ȣ�ò��������RNA-Seq�Ƿ�Ϊ�������Բ���ģ��Բ�׼
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
	 * �Ƿ�ʹ��bowtie2���з���
	 * 
	 * @param bowtie2
	 */
	public void setBowtieVersion(SoftWare bowtieVersion) {
		this.bpwtieVersion = bowtieVersion;
		mapBowtie.setBowtieVersion(bowtieVersion);
	}
	
	private String getOutPathPrefix() {
		return "-o " + outPathPrefix + " ";
	}
	/**
	 * ���볤�ȣ�Ĭ����illumina��450
	 * 
	 * @param insert
	 */
	public void setInsert(int insert) {
		maxInsert = insert;
	}
	/**
	 * ������˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * �����Ҷ˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	/**
	 * -r 150�ȣ���ʾpairend�м�ĳ���
	 * @return
	 */
	private String getInsert() {
		FastQ fastQ = lsLeftFq.get(0);
		int len = fastQ.getReadsLenAvg();
		return "-r " + (maxInsert - len * 2) + " ";
	}

	/** ��junction ��һͷ������Ҫ�����bp�ļ�� */
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
			int intronLenMin = lsIntronSortedS2M.get(50);
			int intronLenMax = lsIntronSortedS2M.get(lsIntronSortedS2M.size() - 50);
			if (intronLenMin < this.intronLenMin) {
				this.intronLenMin = intronLenMin;
				booSetIntronMin = true;
			}
			if (intronLenMax < this.intronLenMax) {
				this.intronLenMax = intronLenMax;
				booSetIntronMax = true;
			}
		}
	}
	
	/** �ں�����̶��٣�Ĭ��50������ݲ�ͬ���ֽ������� */
	private String getIntronLenMin() {
		return "-i " + intronLenMin + " ";
	}

	/** �ں�������٣�Ĭ��500000������ݲ�ͬ���ֽ������� */
	private String getIntronLenMax() {
		return "-I " + intronLenMax + " ";
	}
	private String getIndelLen() {
		return "--max-insertion-length " + indelLen + " --max-deletion-length "
				+ indelLen + " ";
	}

	private String getThreadNum() {
		return "-p " + threadNum + " ";
	}

	/** �Ƿ�ʹ��bowtie2���з��� */
	private String getBowtie() {
		if (bpwtieVersion == SoftWare.bowtie) {
			return " --bowtie1 ";
		}
		else if (bpwtieVersion == SoftWare.bowtie2) {
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

	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
	public void setMismatch(int mismatch) {
		if (mismatch > 2) {
			mismatch = 2;
		}
		this.mismatch = mismatch;
	}

	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
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
	 * ��gtf�ļ�����mapping
	 * @param gtfFile
	 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}

	/**
	 * �Ȳ��趨�����Ǽ���--transcriptome-index�Ǹ�ѡ��
	 * @return
	 */
	private String getGtfFile() {
		if (FileOperate.isFileExist(gtfFile)) {
			return "-G " + gtfFile + " ";
		}
		return "";
	}
	private void setGTFfile() {
		if (FileOperate.isFileExistAndBigThanSize(gtfFile, 100)) {
			return;
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			String path = FileOperate.getParentPathName(lsLeftFq.get(0).getReadFileName());
			String outGTF = path + gffChrAbs.getSpecies().getAbbrName() + DateTime.getDateAndRandom() + ".GTF";
			gffChrAbs.getGffHashGene().writeToGTF(outGTF, "novelbio");
			this.gtfFile = outGTF;
		}
	}
	/**
	 * �������ķ���
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
	 * �����趨��������solid ��û����gtf��ѡ�Ҳ����Ĭ��û��gtf
	 */
	public void mapReads() {
		setIntronLen();
		
		mapBowtie.IndexMakeBowtie();
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0)
			pairend = true;
		else
			pairend = false;
		// linux��������
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
		if (pairend) {
			cmd = cmd + getInsert(); // ���볤��
		}
		cmd = cmd + getAnchoLen() + getAnchorMismatch() + getIntronLenMin()
				+ getIntronLenMax() + getIndelLen();
		if (bpwtieVersion == SoftWare.bowtie2) {
			cmd = cmd + getIndelLen();
		}
		cmd = cmd + getOffset() + getThreadNum();
		cmd = cmd + getStrandSpecifictype();
		cmd = cmd + getMinCoverageIntron() + getMaxCoverageIntron()
				+ getMaxSegmentIntron();
		cmd = cmd + getOutPathPrefix();

		cmd = cmd + " " + chrFile + " ";
		cmd = cmd + " " + lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			cmd = cmd + "," + lsLeftFq.get(i).getReadFileName();
		}
		if (lsRightFq.size() > 0) {
			cmd = cmd + " " + lsRightFq.get(0).getReadFileName();
			for (int i = 1; i < lsRightFq.size(); i++) {
				cmd = cmd + "," + lsRightFq.get(i).getReadFileName();
			}
		}
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMapping");
		cmdOperate.run();
	}
	
	public static HashMap<String, Integer> getMapStr2StrandType() {
		LinkedHashMap<String, Integer> mapStr2StrandType = new LinkedHashMap<String, Integer>();
		mapStr2StrandType.put("STRAND_NULL", STRAND_NULL);
		mapStr2StrandType.put("STRAND_FIRSTSTRAND", STRAND_FIRSTSTRAND);
		mapStr2StrandType.put("STRAND_SECONDSTRAND", STRAND_SECONDSTRAND);
		return mapStr2StrandType;
	}
}
