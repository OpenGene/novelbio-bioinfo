package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.methods.multipart.FilePart;
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
import com.novelbio.database.domain.information.SoftWareInfo.SoftMapping;
import com.novelbio.database.model.species.Species;

/**
 * microRNA测序分析
 * 内部设定下环境变量
 * @author zong0jie
 *
 */
public class CtrlMiRNA {
	Species species = new Species();
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
	/** 输出文件夹的子文件夹，是临时文件夹 */
	String outPathPrefixTmp = "";
	/** 设定gff和chrome */
	GffChrAbs gffChrAbs = null;
	/** repeat 的gff文件 */
	String repeatFile = null;
	/** Rfam比对 */
	RfamStatistic rfamStatistic = new RfamStatistic();
	/** rfam的信息比较文件，类似一个键值表 */
	String rfamFile = "";
	String mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_rfam.bed";
	/**新的miRNA预测 
	 * 未来考虑用list来放置多个如mireap和mirdeep等预测方法
	 * */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	/** 预测新miRNA所需的reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	boolean changeSpecies = true;
	String miRNAcountMiRNAbed = "";
	String readsOnRepeatGeneGenomebed = "";
	String rfamStatisticRfambed = "";
	
	public void setBedFileCountMiRNA(String miRNAcountMiRNAbed, String readsOnRepeatGeneGenomebed, String rfamStatisticRfambed) {
		this.miRNAcountMiRNAbed = miRNAcountMiRNAbed;
		this.readsOnRepeatGeneGenomebed = readsOnRepeatGeneGenomebed;
		this.rfamStatisticRfambed = rfamStatisticRfambed;
	}
	public CtrlMiRNA() {
		mappingMiRNA.setExePath("");
	}
	public void setTaxID(int taxID) {
		changeSpecies = true;
		species.setTaxID(taxID);
	}
	public ArrayList<String> getVersion() {
		return species.getVersion();
	}
	public void setVersion(String version) {
		species.setVersion(version);
	}
	/** 设定待比对的序列 */
	private void setFile() {
		if (!changeSpecies) {
			return;
		}
		changeSpecies = false;
		mappingMiRNA.setMiRNApreSeq(species.getMiRNAhairpinFile());
		mappingMiRNA.setNcRNAseq(species.getRefseqNCfile());
		mappingMiRNA.setRfamSeq(species.getRfamFile());
		mappingMiRNA.setGenome(species.getIndexChr(SoftMapping.bwa));//默认bwa做mapping
	}

	/**
	 * 设定基因组的序列，必须是一个文件
	 * @param mappingAll2Genome 是否将全体reads mapping至基因组上去
	 * @param genomeSeq 序列文件，最好是index
	 */
	public void setGenome(boolean mappingAll2Genome) {
		mappingMiRNA.setMapping2Genome(mappingAll2Genome);
	}
	/** 设定输入的测序文件 */
	public void setFastqFile(String fastqFile) {
		mappingMiRNA.setSample(fastqFile);
	}
	/** 设定输出文件夹和前缀 */
	public void setOutPathPrix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
		this.outPathPrefixTmp = FileOperate.getParentPathName(outPathPrefix) + "tmpMapping";
		FileOperate.createFolders(outPathPrefixTmp);
		mappingMiRNA.setOutPath(outPathPrefixTmp);
	}
	/** rfam的信息比较文件，类似一个键值表 */
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
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
	/**
	 * 返回mapping至基因组上的bed文件
	 * 必须要等mapping完后才能获取
	 */
	public String getGenomeBed() {
		return mappingMiRNA.getOutGenomebed();
	}
	/** 计算miRNA表达 */
	public void exeRunning(boolean solo) {
		countMiRNA(solo);
		countRfam(solo);
		gffChrAbs = new GffChrAbs(species.getGffFile()[0], species.getGffFile()[1], species.getChrPath()[1], species.getChrPath()[0], null, 0);
		if (gffChrAbs.getGffHashGene() == null) {
			readGffInfo();
			countRepeatGene(solo);
		}
	}
	/**
	 * 将指定的bed文件输入
	 * @param bedSeqFile
	 */
	public void runMiRNApredict() {
		gffChrAbs = new GffChrAbs(species.getGffFile()[0], species.getGffFile()[1], species.getChrPath()[1], species.getChrPath()[0], null, 0);
		readGffInfo();
		if (lsBedFileNovelMiRNA.size() <= 0) {
			return;
		}
		
		//////////新建文件夹
		String novelMiRNAPath = outPathPrefix + "miRNApredictReap/";
		if (!FileOperate.createFolders(novelMiRNAPath)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPath, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		//////////
		novelMiRNAReap.setBedSeq(novelMiRNAPath + "allSample.bed", lsBedFileNovelMiRNA);
		novelMiRNAReap.setGffChrAbs(gffChrAbs);
		novelMiRNAReap.setNovelMiRNAMiReapInputFile(novelMiRNAPath + "mireapSeq.fa", novelMiRNAPath + "mireapMap.txt");
		novelMiRNAReap.runBedFile();
	}
	/** 计算miRNA表达 */
	private void countMiRNA(boolean solo) {
		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		if (!solo && FileOperate.isFileExist(mappingMiRNA.getOutMiRNAbed()) && FileOperate.getFileSize(mappingMiRNA.getOutMiRNAbed()) > 1000) {
			miRNACount.setBedSeqMiRNA(mappingMiRNA.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
		else if (solo && FileOperate.isFileExist(miRNAcountMiRNAbed) && FileOperate.getFileSize(miRNAcountMiRNAbed) > 1000) {
			miRNACount.setBedSeqMiRNA(miRNAcountMiRNAbed);
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
	}
	/** 读取信息 */
	private void readGffInfo() {
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(repeatFile);
	}
	/** 读取repeat和gene信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRepeatGene(boolean solo) {
		if (!solo && FileOperate.isFileExist(mappingMiRNA.getOutGenomebed()) && FileOperate.getFileSize(mappingMiRNA.getOutGenomebed()) > 1000) {
			readsOnRepeatGene.countReadsInfo(mappingMiRNA.getOutGenomebed());
			readsOnRepeatGene.writeToFileGeneProp(outPathPrefix + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPathPrefix + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPathPrefix + "_RepeatName.txt");
		}
		else if (solo && FileOperate.isFileExist(readsOnRepeatGeneGenomebed) && FileOperate.getFileSize(readsOnRepeatGeneGenomebed) > 1000) {
			readsOnRepeatGene.countReadsInfo(readsOnRepeatGeneGenomebed);
			readsOnRepeatGene.writeToFileGeneProp(outPathPrefix + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPathPrefix + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPathPrefix + "_RepeatName.txt");
		}
	}
	/** 读取rfam信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRfam(boolean solo) {
		if (!solo && FileOperate.isFileExist(mappingMiRNA.getOutRfambed()) && FileOperate.getFileSize(mappingMiRNA.getOutRfambed()) > 1000) {
			rfamStatistic.countRfamInfo(rfamFile, mappingMiRNA.getOutRfambed(), outPathPrefix + "_RfamStatistics.txt");
		}
		else if (solo && FileOperate.isFileExist(rfamStatisticRfambed) && FileOperate.getFileSize(rfamStatisticRfambed) > 1000) {
			rfamStatistic.countRfamInfo(rfamFile, rfamStatisticRfambed, outPathPrefix + "_RfamStatistics.txt");
		}
	}
}