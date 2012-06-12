package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mirna.MappingMiRNA;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.analysis.seq.mirna.NovelMiRNAReap;
import com.novelbio.analysis.seq.mirna.ReadsOnNCrna;
import com.novelbio.analysis.seq.mirna.ReadsOnRepeatGene;
import com.novelbio.analysis.seq.mirna.RfamStatistic;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * microRNA测序分析
 * 内部设定下环境变量
 * @author zong0jie
 *
 */
public class CtrlMiRNA {
	/** mapping 序列 */
	MappingMiRNA mappingMiRNA = new MappingMiRNA();
	/** 计算小RNA表达 */
	MiRNACount miRNACount = new MiRNACount();
	/** Repeat的情况 */
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	/** refseq的ncRNA的序列 */
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/** 输出文件夹以及前缀 */
	String outPathPrefix = "";
	
	/** gffType */
	String gffType = "";
	/** hg19的gff之类，用于小RNA定位等 */
	String gffFile = "";
	/** 染色体文件 */
	String chrFa = "";
	/** 设定gff和chrome */
	GffChrAbs gffChrAbs = null;
	/** miRNA前体 */
	String hairpinMiRNA = "";
	/** miRNA成熟体 */
	String matureMiRNA = "";
	
	/** repeat 的gff文件 */
	String repeatFile = null;
	/** Rfam比对 */
	RfamStatistic rfamStatistic = new RfamStatistic();
	String rfamFile = "";
	String mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_rfam.bed";
	/**新的miRNA预测 
	 * 未来考虑用list来放置多个如mireap和mirdeep等预测方法
	 * */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	/** 预测新miRNA所需的reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	
	
	public CtrlMiRNA() {
		mappingMiRNA.setExePath("");
	}
	/**
	 * 设定待比对的序列
	 * @param miRNAseq 前体序列
	 * @param rfamSeq
	 * @param refNcRnaSeq
	 */
	public void setRefFile(String miRNAseq, String rfamSeq, String refNcRnaSeq) {
		this.hairpinMiRNA = miRNAseq;
		mappingMiRNA.setMiRNAseq(miRNAseq);
		mappingMiRNA.setNcRNAseq(refNcRnaSeq);
		mappingMiRNA.setRfamSeq(rfamSeq);
	}
	/**
	 * 设定基因组的序列，必须是一个文件
	 * @param mappingAll2Genome 是否将全体reads mapping至基因组上去
	 * @param genomeSeq 序列文件，最好是index
	 */
	public void setGenome(boolean mappingAll2Genome, String genomeSeq) {
		mappingMiRNA.setGenome(genomeSeq);
		mappingMiRNA.setMapping2Genome(mappingAll2Genome);
	}
	/** 设定输入的测序文件 */
	public void setFastqFile(String fastqFile) {
		mappingMiRNA.setSample(fastqFile);
	}
	/** 设定输出文件夹和前缀 */
	public void setOutPathPrix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
		mappingMiRNA.setOutPath(outPathPrefix);
	}
	/**
	 * 设定预测新miRNA所需的信息
	 * @param gffType gff类型
	 * @param geneGffFile gff文件
	 * @param chromFa 该gff所对应的序列
	 */
	public void setGffInfo(String gffType, String geneGffFile, String chromFa) {
		this.gffType = gffType;
		this.gffFile = geneGffFile;
		this.chrFa = chromFa;
	}
	/** 设定repeat的gff文件 */
	public void setRepeat(String repeatFile) {
		this.repeatFile = repeatFile;
	}
	/** 设定rfam的文件 */
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
	}
	/** 
	 * miRNA计算表达使用
	 * 设定miRNA的前体序列和成熟序列 
	 * */
	public void setMirnaFile(String matureMirna) {
		this.matureMiRNA = matureMirna;
	}
	/**
	 * miRNA计算表达使用
	 * 给定miRNA文件和物种名
	 * @param fileType 读取的是miReap的文件还是RNA.dat ListMiRNALocation.TYPE_RNA_DATA 或 ListMiRNALocation.TYPE_MIREAP
	 * @param Species 为miRNA.dat中的物种名，如果文件不是miRNA.dat，那就不用写了
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, String species, String rnadatFile) {
		miRNACount.setMiRNAinfo(fileType, species, rnadatFile);
	}
	public void setLsBedFile(ArrayList<String> lsBedFileNovelMiRNA) {
		this.lsBedFileNovelMiRNA = lsBedFileNovelMiRNA;
	}
	/**
	 * 开始比对
	 */
	public void mapping() {
		mappingMiRNA.mappingPipeline();
	}
	public void exeRunning() {
		countMiRNA();
		countRfam();
		if (!FileOperate.isFileExist(gffFile)) {
			gffChrAbs = new GffChrAbs(gffType, gffFile, chrFa, null, 0);
			readGffInfo();
			countRepeatGene();
			miRNApredict();
		}
	}
	/**
	 * 将指定的bed文件输入
	 * @param bedSeqFile
	 */
	private void miRNApredict() {
		//////////新建文件夹
		String novelMiRNAPath = outPathPrefix + "miRNApredictReap/";
		if (!FileOperate.createFolders(novelMiRNAPath)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPath, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		//////////
		novelMiRNAReap.setBedSeq(novelMiRNAPath + "_", lsBedFileNovelMiRNA);
		novelMiRNAReap.setGffChrAbs(gffChrAbs);
		novelMiRNAReap.setNovelMiRNAMiReapInputFile(novelMiRNAPath + "mireapSeq.fa", novelMiRNAPath + "mireapMap.txt");
		novelMiRNAReap.runBedFile();
	}
	/** 计算miRNA表达 */
	private void countMiRNA() {
		miRNACount.setMiRNAfile(hairpinMiRNA, matureMiRNA);
		if (FileOperate.isFileExist(mappingMiRNA.getOutMiRNAbed()) && FileOperate.getFileSize(mappingMiRNA.getOutMiRNAbed()) > 1000) {
			miRNACount.setBedSeqMiRNA(mappingMiRNA.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
	}
	/** 读取信息 */
	private void readGffInfo() {
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(repeatFile);
	}
	/** 读取repeat和gene信息 */
	private void countRepeatGene() {
		if (FileOperate.isFileExist(mappingMiRNA.getOutGenomebed()) && FileOperate.getFileSize(mappingMiRNA.getOutGenomebed()) > 1000) {
			readsOnRepeatGene.countReadsInfo(mappingMiRNA.getOutGenomebed());
			readsOnRepeatGene.writeToFileGeneProp(outPathPrefix + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPathPrefix + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPathPrefix + "_RepeatName.txt");
		}
	}
	
	private void countRfam() {
		if (FileOperate.isFileExist(mappingMiRNA.getOutRfambed()) && FileOperate.getFileSize(mappingMiRNA.getOutRfambed()) > 1000) {
			rfamStatistic.countRfamInfo(rfamFile, mappingMiRNA.getOutRfambed(), outPathPrefix + "_RfamStatistics.txt");
		}
	}
}