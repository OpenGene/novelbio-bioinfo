package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
/**
 * 还没返回结果的bam文件
 * @author zong0jie
 *
 */
public class MapRsem {
	public static void main(String[] args) {
		String fastqFile = "/media/winF/NBC/Project/RNA-Seq_HPWtest/FangLan/3_AGTTCC_L003_R1_001_filtered.fq";
		String outFile = "/media/winF/NBC/Project/RNA-Seq_HPWtest/FangLan/rsem2/N";
		MapRsem mapRsem = new MapRsem();
		Species species = new Species(10090);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		mapRsem.setExePath("", "");
		mapRsem.setThreadNum(4);
		mapRsem.setLeftFq(fastqFile);
		mapRsem.setGffChrAbs(gffChrAbs);
		mapRsem.setOutPathPrefix(outFile);
		mapRsem.mapReads();
	}
	private static Logger logger = Logger.getLogger(MapRsem.class);
	
	GffChrSeq gffChrSeq = null;
	GffChrAnno gffChrAnno = null;
	GffChrAbs gffChrAbs = null;
	/** 由GffFile自动生成 */
	String gene2isoFile = "";
	/** refgene的文件 FileOperate.changeFileSuffix(chrFile, "_RefGene", "fastq"); */
	String refFile = "";
	/** FileOperate.changeFileSuffix(refFile, "_rsemIndex", "") */
	String rsemIndex = "";
	/** rsem的路径 */
	String exePathRsem = "";
	/** bowtie的路径 */
	String exePathBowtie = "";
	/** 线程数 */
	int threadNum = 4;
	List<FastQ> lsLeftFq;
	List<FastQ> lsRightFq;
	boolean pairend = false;
	/** 输出文件夹以及前缀 */
	String outPathPrefix = "";
	
	public MapRsem() {
		// TODO Auto-generated constructor stub
	}
	/** 直接输入过滤好的fastq，线程和输出路径，就可以开始运行，其他啥也不用管了 */
	public MapRsem(Species species) {
		gffChrSeq = new GffChrSeq();
		gffChrSeq.setSpecies(species);
		SoftWareInfo softWareInfoRsem = new SoftWareInfo();
		softWareInfoRsem.setName(SoftWare.rsem);
		SoftWareInfo softWareInfoBowtie = new SoftWareInfo();
		softWareInfoBowtie.setName(SoftWare.bowtie);
		
		setExePath(softWareInfoRsem.getExePath(), softWareInfoBowtie.getExePath());
	}

	/**
	 * 设定Gff文件和chrFile
	 * @param gffFile
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		gffChrSeq = new GffChrSeq(gffChrAbs);
	}
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePathRsem rsem的路径 如果在根目录下则设置为""或null
	 * @param exePathBowtie bowtie的路径，什么时候rsem支持bowtie2了，那么再修正
	 */
	public void setExePath(String exePathRsem, String exePathBowtie) {
		if (exePathRsem == null || exePathRsem.trim().equals(""))
			this.exePathRsem = "";
		else
			this.exePathRsem = FileOperate.addSep(exePathRsem);
		
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.exePathBowtie = "";
		else
			this.exePathBowtie = FileOperate.addSep(exePathBowtie);
	}
	/**
	 * 设定Gff文件和refFile
	 * @param gffFile
	 */
	public void setFileRef(String refFile) {
		this.refFile = refFile;
	}
	public void setThreadNum(int threadNum) {
		if (threadNum > 0 && threadNum < 20) {
			this.threadNum = threadNum;
		}
	}
	private void createGene2IsoAndRefSeq() {
		if (!FileOperate.isFileExist(refFile)) {
			refFile = FileOperate.changeFileSuffix(gffChrAbs.getSeqHash().getChrFile(), "_RefGene", "fastq");
			if (!FileOperate.isFileExist(refFile))
				gffChrSeq.writeIsoFasta(refFile);
		}
		gene2isoFile = FileOperate.changeFileSuffix(gffChrAbs.getGffHashGene().getGffFilename(),"_gene2iso","txt");
		if (!FileOperate.isFileExist(gene2isoFile)) {
			gffChrAbs.getGffHashGene().writeGene2Iso(gene2isoFile);
		}
	}
	private String getThreadNum() {
		return "-p " + threadNum + " ";
	}
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(String... fqFile) {
		lsLeftFq = new ArrayList<FastQ>();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string);
			lsLeftFq.add(fastQ);
		}
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(String... fqFile) {
		lsRightFq = new ArrayList<FastQ>();
		for (String string : fqFile) {
			FastQ fastQ = new FastQ(string);
			lsRightFq.add(fastQ);
		}
	}
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/**
	 * 制作索引，输入是用bowtie1还是bowtie2做索引
	 * @param bowtie2
	 */
	private void IndexMakeBowtie() {
		createGene2IsoAndRefSeq();
		rsemIndex = FileOperate.changeFileSuffix(refFile, "_rsemIndex", "");
		if (FileOperate.isFileExist(rsemIndex + ".3.ebwt") == true)
			return;
		String cmd = exePathRsem + "rsem-prepare-reference  --transcript-to-gene-map ";
		//TODO :考虑是否自动判断为solid
		cmd = cmd + gene2isoFile + " " + refFile + " " + rsemIndex;
		CmdOperate cmdOperate = new CmdOperate(cmd,"RsemMakeIndex");
		cmdOperate.run();
	}
	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return " --phred64-quals ";
		}
		return " --phred33-quals ";
	}
	private String getBowtiePath() {
		if (exePathBowtie != null && !exePathBowtie.equals("")) {
			return "--bowtie-path " + exePathBowtie + "bowtie ";
		}
		return "";
	}
	private String getPairend() {
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0)
			pairend = true;
		else
			pairend = false;
		
		if (pairend) {
			return "--paired-end ";
		}
		return "";
	}
	/**
	 * 比对序列并计算表达
	 * @return
	 */
	public SamFile mapReads() {
		IndexMakeBowtie();
		String cmd = exePathRsem + "rsem-calculate-expression " + getBowtiePath();
		cmd = cmd + getOffset() + getPairend() + getThreadNum();
		
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
		cmd = cmd + " " + rsemIndex + " " + outPathPrefix;
		logger.info(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd,"bwaMapping");
		cmdOperate.run();
		return null;//最后考虑返回一个bam文件
	}
}
