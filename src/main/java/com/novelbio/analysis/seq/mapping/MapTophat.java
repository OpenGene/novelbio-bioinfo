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
public class MapTophat {
	private static Logger logger = Logger.getLogger(MapTophat.class);

	/** �����Ǳ�ʾ�з���Ĳ����޷��� */
	public static final int STRAND_NULL = 0;
	/** �����Ǳ�ʾ�з���Ĳ��򣬵�һ�����ķ��� */
	public static final int STRAND_FIRSTSTRAND = 0;
	/** �����Ǳ�ʾ�з���Ĳ��򣬵ڶ������ķ��� */
	public static final int STRAND_SECONDSTRAND = 0;
	int strandSpecifictype = STRAND_NULL;
	ArrayList<FastQ> lsLeftFq = new ArrayList<FastQ>();
	ArrayList<FastQ> lsRightFq = new ArrayList<FastQ>();
	/** bowtie����·�� */
	String ExePathBowtie = "";
	/** ���ȶԵ�Ⱦɫ�� */
	String chrFile = "";
	/** Ĭ����bowtie2 ��mapping */
	int bowtieVersion = MapBowtie.VERSION_BOWTIE2;
	/** �Ƿ�Ϊ˫�˲��� */
	boolean pairend = false;
	/** ��junction ��һͷ������Ҫ�����bp�ļ�� */
	int anchorLength = 10;
	/** anchor�ϵ�mismithch��Ĭ��Ϊ0 */
	int anchorMismatch = 0;
	/** �ں�����̶��٣�Ĭ��50������ݲ�ͬ���ֽ������� */
	int intronLenMin = 50;
	/** �ں�������٣�Ĭ��500000������ݲ�ͬ���ֽ������� */
	int intronLenMax = 500000;
	/** indel�ĳ��ȣ�Ĭ��Ϊ3 */
	int indelLen = 3;
	/** �߳��� */
	int threadNum = 4;
	/** Ĭ����solexa������� */
	int maxInsert = 450;
	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
	int mismatch = 2;
	/** ����GTF���ļ� */
	String gtfFile = "";
	/** ����ļ� */
	String outPathPrefix = "";
	MapBowtie mapBowtie = new MapBowtie();
	
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * 
	 * @param exePath
	 *            ����ڸ�Ŀ¼��������Ϊ""��null
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
	 * ���볤�ȣ�Ĭ����illumina��450
	 * 
	 * @param insert
	 */
	public void setInsert(int insert) {
		maxInsert = insert;
	}

	/**
	 * ������˵����У����û����ǰ�����
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
	 * �����Ҷ˵����У����û����ǰ�����
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
	 * -r 150�ȣ���ʾpairend�м�ĳ���
	 * 
	 * @return
	 */
	private String getInsert() {
		FastQ fastQ = lsLeftFq.get(0);
		int len = fastQ.getReadsLenAvg();
		return "-r " + (maxInsert - len * 2) + " ";
	}

	/** ��junction ��һͷ������Ҫ�����bp�ļ����Ĭ��Ϊ10 */
	public void setAnchorLength(int anchorLength) {
		this.anchorLength = anchorLength;
	}

	/** ��junction ��һͷ������Ҫ�����bp�ļ�� */
	private String getAnchoLen() {
		return "-a " + anchorLength + " ";
	}

	/** anchor�ϵ�mismithch��Ĭ��Ϊ0���������Ϊ1 */
	public void setAnchorMismatch(int anchorMismatch) {
		this.anchorMismatch = anchorMismatch;
	}

	private String getAnchorMismatch() {
		return "-m " + anchorMismatch + " ";
	}

	/**
	 * �ں�����̶��٣�Ĭ��50������ݲ�ͬ���ֽ�������
	 * 
	 * @param intronLenMin
	 */
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
	}

	/** �ں�����̶��٣�Ĭ��50������ݲ�ͬ���ֽ������� */
	private String getIntronLenMin() {
		return "-i " + intronLenMin + " ";
	}

	/**
	 * �ں�������٣�Ĭ��500000������ݲ�ͬ���ֽ�������
	 * 
	 * @param intronLenMax
	 */
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
	}

	/** �ں�������٣�Ĭ��500000������ݲ�ͬ���ֽ������� */
	private String getIntronLenMax() {
		return "-I " + intronLenMax + " ";
	}

	/** �趨indel */
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}

	private String getIndelLen() {
		return "--max-insertion-length " + indelLen + " --max-deletion-length "
				+ indelLen + " ";
	}

	/** �߳�������Ĭ��4�߳� */
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
	 * �Ƿ�ʹ��bowtie2���з���
	 * 
	 * @param bowtie2
	 */
	public void setBowtieVersion(int bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
		mapBowtie.setBowtieVersion(bowtieVersion);
	}

	/** �Ƿ�ʹ��bowtie2���з��� */
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

	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}

	/** ���䣬�����Ĭ�ϱȽϺã�Ĭ��Ϊ2 */
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
	 * ��gtf�ļ�����mapping
	 * 
	 * @param gtfFile
	 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}

	/**
	 * �Ȳ��趨�����Ǽ���--transcriptome-index�Ǹ�ѡ��
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
	 * �������ķ���
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
	 * �����趨��������solid ��û����gtf��ѡ�Ҳ����Ĭ��û��gtf
	 */
	public SamFile mapReads() {
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
		cmd = ExePathBowtie + "tophat " + getBowtie();
		if (pairend) {
			cmd = cmd + getInsert(); // ���볤��
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
		return null;// ����Ƿ���һ��bam�ļ�
	}
}
