package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mirna.MiRNAmapPipline;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.analysis.seq.mirna.NovelMiRNADeep;
import com.novelbio.analysis.seq.mirna.NovelMiRNAReap;
import com.novelbio.analysis.seq.mirna.ReadsOnNCrna;
import com.novelbio.analysis.seq.mirna.ReadsOnRepeatGene;
import com.novelbio.analysis.seq.mirna.RfamStatistic;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/**
 * microRNA测序分析
 * 内部设定下环境变量
 * @author zong0jie
 */
public class CtrlMiRNA {
	public static void main(String[] args) {
		SeqFastaHash seqFastaHash = new SeqFastaHash("/media/winE/Bioinformatics/GenomeData/CriGri/rna_Cope.fa");
		seqFastaHash.writeToFile("/media/winE/Bioinformatics/GenomeData/CriGri/rna_CopeNew.fa");
	}
	Species species = new Species();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	/** 输入的待比对序列 */
	String fastqFile = "";

	/** 输出文件的前缀 */
	String outputPrefix;
	String outPath = "";
	/** 输出文件夹的子文件夹，是临时文件夹 */
	String outPathTmpMapping = "";
	String outPathTmpBed = "";
	
	/** rfam的信息比较文件，类似一个键值表 */
	String rfamFile = "";
	String mapBedFile = "";
	/** 预测新miRNA所需的reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	boolean changeSpecies = true;
	
	String miRNAcountMiRNAbed = "";
	String readsOnRepeatGeneGenomebed = "";
	String rfamStatisticRfambed = "";
	String readsOnNCrnaBed = "";
	
	/** 设定gff和chrome */
	GffChrAbs gffChrAbs = null;
	
	/** mapping 序列 */
	MiRNAmapPipline miRNAmappingPipline = new MiRNAmapPipline();
	
	MiRNACount miRNACount = new MiRNACount();
	RfamStatistic rfamStatistic = new RfamStatistic();
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/**新的miRNA预测   未来考虑用list来放置多个如mireap和mirdeep等预测方法  */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	
	/**
	 * 设定miRNA数据计算的bed文件，是从mapping获得的
	 * @param miRNAcountMiRNAbed
	 * @param readsOnRepeatGeneGenomebed
	 * @param rfamStatisticRfambed
	 */
	public void setBedFileCountMiRNA(String miRNAcountMiRNAbed, String readsOnRepeatGeneGenomebed, String rfamStatisticRfambed, String readsOnNCrnaBed) {
		this.miRNAcountMiRNAbed = miRNAcountMiRNAbed;
		this.readsOnRepeatGeneGenomebed = readsOnRepeatGeneGenomebed;
		this.rfamStatisticRfambed = rfamStatisticRfambed;
		this.readsOnNCrnaBed = readsOnNCrnaBed;
	}
	public void setSpecies(Species species) {
		changeSpecies = true;
		this.species = species;
	}
	public ArrayList<String> getVersion() {
		return species.getVersionAll();
	}
	public void setVersion(String version) {
		species.setVersion(version);
	}
	/**
	 * 设定基因组的序列，必须是一个文件
	 * @param mappingAll2Genome 是否将全体reads mapping至基因组上去
	 * @param genomeSeq 序列文件，最好是index
	 */
	public void setGenome(boolean mappingAll2Genome) {
		miRNAmappingPipline.setMappingAll2Genome(mappingAll2Genome);
	}
	/** 设定输入的测序文件 */
	public void setFastqFile(String fastqFile) {
		this.fastqFile = fastqFile;
	}
	/** 设定输出文件夹 */
	public void setOutPath(String outputPrefix, String outPath) {
		this.outputPrefix = outputPrefix;
		this.outPath = FileOperate.addSep(outPath);
		this.outPathTmpMapping = FileOperate.addSep(outPath) + "tmpMapping";
		FileOperate.createFolders(outPathTmpMapping);
		this.outPathTmpBed = FileOperate.addSep(outPath) + "tmpBed";
		FileOperate.createFolders(outPathTmpBed);
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
	public void setMiRNAinfo(int fileType, String rnadatFile) {
		miRNACount.setMiRNAinfo(fileType, species, rnadatFile);
	}
	public void setLsBedFile(ArrayList<String> lsBedFileNovelMiRNA) {
		this.lsBedFileNovelMiRNA = lsBedFileNovelMiRNA;
	}
	/** 开始比对 */
	public void mapping() {
		setConfigFile();
		miRNAmappingPipline.setSample(outputPrefix, fastqFile);
		miRNAmappingPipline.setOutPath(outPath, outPathTmpMapping, outPathTmpBed);
		miRNAmappingPipline.mappingPipeline();
	}
	/** 设定待比对的序列 */
	private void setConfigFile() {
		if (!changeSpecies) {
			return;
		}
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa.toString());
		changeSpecies = false;
		miRNAmappingPipline.setExePath(softWareInfo.getExePath());
		miRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		miRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		miRNAmappingPipline.setRfamSeq(species.getRfamFile());
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bwa));//默认bwa做mapping
	}
	/**
	 * 返回mapping至基因组上的bed文件
	 * 必须要等mapping完后才能获取
	 */
	public String getGenomeBed() {
		return miRNAmappingPipline.getOutGenomebed();
	}
	/** 计算miRNA表达 */
	public void exeRunning(boolean solo) {
		countMiRNA(solo);
		countRfam(solo);
		countNCrna(solo);
		countRepeatGene(solo);
	}
	/**
	 * 将指定的bed文件输入
	 * @param bedSeqFile
	 */
	public void runMiRNApredict() {
		if (gffChrAbs != null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readGffInfo();
		if (lsBedFileNovelMiRNA.size() <= 0) {
			return;
		}
		//////////新建文件夹
		String novelMiRNAPathReap = outPath + outputPrefix + "miRNApredictReap/";
		if (!FileOperate.createFolders(novelMiRNAPathReap)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPathReap, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		String novelMiRNAPathDeep = outPath + outputPrefix + "miRNApredictDeep/";
		if (!FileOperate.createFolders(novelMiRNAPathDeep)) {
			JOptionPane.showMessageDialog(null, "cannot create fold: " + novelMiRNAPathDeep, "fold create error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		//////////
		novelMiRNAReap.setBedSeqInput(novelMiRNAPathReap + "allSample.bed", lsBedFileNovelMiRNA);
		novelMiRNAReap.setGffChrAbs(gffChrAbs);
		novelMiRNAReap.setNovelMiRNAMiReapInputFile(novelMiRNAPathReap + "mireapSeq.fa", novelMiRNAPathReap + "mireapMap.txt");
		novelMiRNAReap.runBedFile();
		
		novelMiRNADeep.setBedSeqInput(novelMiRNAPathReap + "allSample.bed", lsBedFileNovelMiRNA);
		softWareInfo.setName(SoftWare.mirDeep);
		novelMiRNADeep.setExePath(softWareInfo.getExePath(), species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setGffChrAbs(gffChrAbs);
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setSpecies(species.getCommonName());
		novelMiRNADeep.setOutPath(novelMiRNAPathDeep);
		novelMiRNADeep.predict();
		novelMiRNADeep.getMirCount();
	}
	/** 计算miRNA表达 */
	private void countMiRNA(boolean solo) {
		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		if (!solo && FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutMiRNAbed(),1)) {
			miRNACount.setBedSeqMiRNA(miRNAmappingPipline.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.writeResultToOut(outPath + outputPrefix);
		}
		else if (solo && FileOperate.isFileExistAndBigThanSize(miRNAcountMiRNAbed,1)) {
			miRNACount.setBedSeqMiRNA(miRNAcountMiRNAbed);
			miRNACount.countMiRNA();
			miRNACount.writeResultToOut(outPath + outputPrefix);
		}
	}
	/** 读取信息 */
	private void readGffInfo() {
		if (gffChrAbs != null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
	}
	/** 读取repeat和gene信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRepeatGene(boolean solo) {
		String outFinal = outPath + outputPrefix;
		if (!solo && FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutGenomebed(), 10) ) {
			readGffInfo();
			readsOnRepeatGene.countReadsInfo(miRNAmappingPipline.getOutGenomebed());
			readsOnRepeatGene.writeToFileGeneProp(outFinal + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outFinal + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outFinal + "_RepeatName.txt");
		}
		else if (solo && FileOperate.isFileExistAndBigThanSize(readsOnRepeatGeneGenomebed, 10)) {
			readGffInfo();
			readsOnRepeatGene.countReadsInfo(readsOnRepeatGeneGenomebed);
			readsOnRepeatGene.writeToFileGeneProp(outFinal + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outFinal + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outFinal + "_RepeatName.txt");
		}
	}
	/** 读取rfam信息并计数
	 * @param solo 单独计数
	 *  */
	private void countRfam(boolean solo) {
		rfamStatistic.setOutputFile(outPath + outputPrefix + "_RfamStatistics.txt");
		if (!solo && FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutRfambed(), 10)) {
			rfamStatistic.countRfamInfo(rfamFile, miRNAmappingPipline.getOutRfambed());
		}
		else if (solo && FileOperate.isFileExistAndBigThanSize(rfamStatisticRfambed,10)) {
			rfamStatistic.countRfamInfo(rfamFile, rfamStatisticRfambed);
		}
	}
	/** 读取ncRNA的信息并计数
	 * @param solo 单独计数
	 *  */
	private void countNCrna(boolean solo) {
		if (!solo && FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutNCRNAbed(), 10)) {
			readsOnNCrna.setBedSed(miRNAmappingPipline.getOutNCRNAbed());
		}
		else if (solo && FileOperate.isFileExist(readsOnNCrnaBed) && FileOperate.getFileSize(readsOnNCrnaBed) > 1000) {
			readsOnNCrna.setBedSed(readsOnNCrnaBed);
		}
		else {
			return;
		}
		readsOnNCrna.searchNCrna();
		readsOnNCrna.writeToFile(outPath + outputPrefix + "_NCrnaStatistics.txt");
	}
}