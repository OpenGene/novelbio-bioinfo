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
 * microRNA�������
 * �ڲ��趨�»�������
 * @author zong0jie
 */
public class CtrlMiRNA {
	public static void main(String[] args) {
		SeqFastaHash seqFastaHash = new SeqFastaHash("/media/winE/Bioinformatics/GenomeData/CriGri/rna_Cope.fa");
		seqFastaHash.writeToFile("/media/winE/Bioinformatics/GenomeData/CriGri/rna_CopeNew.fa");
	}
	Species species = new Species();
	SoftWareInfo softWareInfo = new SoftWareInfo();
	/** ����Ĵ��ȶ����� */
	String fastqFile = "";

	/** ����ļ���ǰ׺ */
	String outputPrefix;
	String outPath = "";
	/** ����ļ��е����ļ��У�����ʱ�ļ��� */
	String outPathTmpMapping = "";
	String outPathTmpBed = "";
	
	/** rfam����Ϣ�Ƚ��ļ�������һ����ֵ�� */
	String rfamFile = "";
	String mapBedFile = "";
	/** Ԥ����miRNA�����reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	boolean changeSpecies = true;
	
	String miRNAcountMiRNAbed = "";
	String readsOnRepeatGeneGenomebed = "";
	String rfamStatisticRfambed = "";
	String readsOnNCrnaBed = "";
	
	/** �趨gff��chrome */
	GffChrAbs gffChrAbs = null;
	
	/** mapping ���� */
	MiRNAmapPipline miRNAmappingPipline = new MiRNAmapPipline();
	
	MiRNACount miRNACount = new MiRNACount();
	RfamStatistic rfamStatistic = new RfamStatistic();
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/**�µ�miRNAԤ��   δ��������list�����ö����mireap��mirdeep��Ԥ�ⷽ��  */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	
	/**
	 * �趨miRNA���ݼ����bed�ļ����Ǵ�mapping��õ�
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
	 * �趨����������У�������һ���ļ�
	 * @param mappingAll2Genome �Ƿ�ȫ��reads mapping����������ȥ
	 * @param genomeSeq �����ļ��������index
	 */
	public void setGenome(boolean mappingAll2Genome) {
		miRNAmappingPipline.setMappingAll2Genome(mappingAll2Genome);
	}
	/** �趨����Ĳ����ļ� */
	public void setFastqFile(String fastqFile) {
		this.fastqFile = fastqFile;
	}
	/** �趨����ļ��� */
	public void setOutPath(String outputPrefix, String outPath) {
		this.outputPrefix = outputPrefix;
		this.outPath = FileOperate.addSep(outPath);
		this.outPathTmpMapping = FileOperate.addSep(outPath) + "tmpMapping";
		FileOperate.createFolders(outPathTmpMapping);
		this.outPathTmpBed = FileOperate.addSep(outPath) + "tmpBed";
		FileOperate.createFolders(outPathTmpBed);
	}
	/** rfam����Ϣ�Ƚ��ļ�������һ����ֵ�� */
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
	}
	/**
	 * miRNA������ʹ��
	 * ����miRNA�ļ���������
	 * @param fileType ��ȡ����miReap���ļ�����RNA.dat ListMiRNALocation.TYPE_RNA_DATA �� ListMiRNALocation.TYPE_MIREAP
	 * @param Species ΪmiRNA.dat�е�������������ļ�����miRNA.dat���ǾͲ���д��
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, String rnadatFile) {
		miRNACount.setMiRNAinfo(fileType, species, rnadatFile);
	}
	public void setLsBedFile(ArrayList<String> lsBedFileNovelMiRNA) {
		this.lsBedFileNovelMiRNA = lsBedFileNovelMiRNA;
	}
	/** ��ʼ�ȶ� */
	public void mapping() {
		setConfigFile();
		miRNAmappingPipline.setSample(outputPrefix, fastqFile);
		miRNAmappingPipline.setOutPath(outPath, outPathTmpMapping, outPathTmpBed);
		miRNAmappingPipline.mappingPipeline();
	}
	/** �趨���ȶԵ����� */
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
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bwa));//Ĭ��bwa��mapping
	}
	/**
	 * ����mapping���������ϵ�bed�ļ�
	 * ����Ҫ��mapping�����ܻ�ȡ
	 */
	public String getGenomeBed() {
		return miRNAmappingPipline.getOutGenomebed();
	}
	/** ����miRNA��� */
	public void exeRunning(boolean solo) {
		countMiRNA(solo);
		countRfam(solo);
		countNCrna(solo);
		countRepeatGene(solo);
	}
	/**
	 * ��ָ����bed�ļ�����
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
		//////////�½��ļ���
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
	/** ����miRNA��� */
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
	/** ��ȡ��Ϣ */
	private void readGffInfo() {
		if (gffChrAbs != null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
	}
	/** ��ȡrepeat��gene��Ϣ������
	 * @param solo ��������
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
	/** ��ȡrfam��Ϣ������
	 * @param solo ��������
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
	/** ��ȡncRNA����Ϣ������
	 * @param solo ��������
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