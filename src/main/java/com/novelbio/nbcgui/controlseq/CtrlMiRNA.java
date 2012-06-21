package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mirna.MiRNAmapPipline;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.analysis.seq.mirna.NovelMiRNAReap;
import com.novelbio.analysis.seq.mirna.ReadsOnNCrna;
import com.novelbio.analysis.seq.mirna.ReadsOnRepeatGene;
import com.novelbio.analysis.seq.mirna.RfamStatistic;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftMapping;
import com.novelbio.database.model.species.Species;

/**
 * microRNA�������
 * �ڲ��趨�»�������
 * @author zong0jie
 */
public class CtrlMiRNA {
	Species species = new Species();
	/** mapping ���� */
	MiRNAmapPipline MiRNAmappingPipline = new MiRNAmapPipline();
	/** ����СRNA��� */
	MiRNACount miRNACount = new MiRNACount();
	/** Repeat����� */
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	/** refseq��ncRNA������ */
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/** ����ļ����Լ�ǰ׺ */
	String outPathPrefix = "";
	/** ����ļ��е����ļ��У�����ʱ�ļ��� */
	String outPathPrefixTmp = "";
	/** �趨gff��chrome */
	GffChrAbs gffChrAbs = null;
	/** Rfam�ȶ� */
	RfamStatistic rfamStatistic = new RfamStatistic();
	/** rfam����Ϣ�Ƚ��ļ�������һ����ֵ�� */
	String rfamFile = "";
	String mapBedFile = "";
	/**�µ�miRNAԤ�� 
	 * δ��������list�����ö����mireap��mirdeep��Ԥ�ⷽ��
	 * */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	/** Ԥ����miRNA�����reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	boolean changeSpecies = true;
	String miRNAcountMiRNAbed = "";
	String readsOnRepeatGeneGenomebed = "";
	String rfamStatisticRfambed = "";
	String readsOnNCrnaBed = "";
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
	/** �趨���ȶԵ����� */
	private void setFile() {
		if (!changeSpecies) {
			return;
		}
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftMapping.bwa.toString());
		changeSpecies = false;
		MiRNAmappingPipline.setExePath(softWareInfo.getExePath());
		MiRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		MiRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		MiRNAmappingPipline.setRfamSeq(species.getRfamFile());
		MiRNAmappingPipline.setGenome(species.getIndexChr(SoftMapping.bwa));//Ĭ��bwa��mapping
	}

	/**
	 * �趨����������У�������һ���ļ�
	 * @param mappingAll2Genome �Ƿ�ȫ��reads mapping����������ȥ
	 * @param genomeSeq �����ļ��������index
	 */
	public void setGenome(boolean mappingAll2Genome) {
		MiRNAmappingPipline.setMapping2Genome(mappingAll2Genome);
	}
	/** �趨����Ĳ����ļ� */
	public void setFastqFile(String fastqFile) {
		MiRNAmappingPipline.setSample(fastqFile);
	}
	/** �趨����ļ��к�ǰ׺ */
	public void setOutPathPrix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
		this.outPathPrefixTmp = FileOperate.getParentPathName(outPathPrefix) + "tmpMapping";
		FileOperate.createFolders(outPathPrefixTmp);
		MiRNAmappingPipline.setOutPath(outPathPrefixTmp);
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
	public void setMiRNAinfo(int fileType, int taxID, String rnadatFile) {
		miRNACount.setMiRNAinfo(fileType, taxID, rnadatFile);
	}
	public void setLsBedFile(ArrayList<String> lsBedFileNovelMiRNA) {
		this.lsBedFileNovelMiRNA = lsBedFileNovelMiRNA;
	}
	/**
	 * ��ʼ�ȶ�
	 */
	public void mapping() {
		setFile();
		MiRNAmappingPipline.mappingPipeline();
	}
	/**
	 * ����mapping���������ϵ�bed�ļ�
	 * ����Ҫ��mapping�����ܻ�ȡ
	 */
	public String getGenomeBed() {
		return MiRNAmappingPipline.getOutGenomebed();
	}
	/** ����miRNA��� */
	public void exeRunning(boolean solo) {
		countMiRNA(solo);
		countRfam(solo);
		countNCrna(solo);
		gffChrAbs = new GffChrAbs(species.getGffFile()[0], species.getGffFile()[1], species.getChrPath()[1], species.getChrPath()[0], null, 0);
		if (gffChrAbs.getGffHashGene() != null) {
			readGffInfo();
			countRepeatGene(solo);
		}
	}
	/**
	 * ��ָ����bed�ļ�����
	 * @param bedSeqFile
	 */
	public void runMiRNApredict() {
		gffChrAbs = new GffChrAbs(species.getGffFile()[0], species.getGffFile()[1], species.getChrPath()[1], species.getChrPath()[0], null, 0);
		readGffInfo();
		if (lsBedFileNovelMiRNA.size() <= 0) {
			return;
		}
		//////////�½��ļ���
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
	/** ����miRNA��� */
	private void countMiRNA(boolean solo) {
		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		if (!solo && FileOperate.isFileExist(MiRNAmappingPipline.getOutMiRNAbed()) && FileOperate.getFileSize(MiRNAmappingPipline.getOutMiRNAbed()) > 1000) {
			miRNACount.setBedSeqMiRNA(MiRNAmappingPipline.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
		else if (solo && FileOperate.isFileExist(miRNAcountMiRNAbed) && FileOperate.getFileSize(miRNAcountMiRNAbed) > 1000) {
			miRNACount.setBedSeqMiRNA(miRNAcountMiRNAbed);
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
	}
	/** ��ȡ��Ϣ */
	private void readGffInfo() {
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
	}
	/** ��ȡrepeat��gene��Ϣ������
	 * @param solo ��������
	 *  */
	private void countRepeatGene(boolean solo) {
		if (!solo && FileOperate.isFileExistAndBigThanSize(MiRNAmappingPipline.getOutGenomebed(), 10) ) {
			readsOnRepeatGene.countReadsInfo(MiRNAmappingPipline.getOutGenomebed());
			readsOnRepeatGene.writeToFileGeneProp(outPathPrefix + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPathPrefix + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPathPrefix + "_RepeatName.txt");
		}
		else if (solo && FileOperate.isFileExistAndBigThanSize(readsOnRepeatGeneGenomebed, 10)) {
			readsOnRepeatGene.countReadsInfo(readsOnRepeatGeneGenomebed);
			readsOnRepeatGene.writeToFileGeneProp(outPathPrefix + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outPathPrefix + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outPathPrefix + "_RepeatName.txt");
		}
	}
	/** ��ȡrfam��Ϣ������
	 * @param solo ��������
	 *  */
	private void countRfam(boolean solo) {
		rfamStatistic.setOutputFile(outPathPrefix + "_RfamStatistics.txt");
		if (!solo && FileOperate.isFileExistAndBigThanSize(MiRNAmappingPipline.getOutRfambed(), 10)) {
			rfamStatistic.countRfamInfo(rfamFile, MiRNAmappingPipline.getOutRfambed());
		}
		else if (solo && FileOperate.isFileExistAndBigThanSize(rfamStatisticRfambed,10)) {
			rfamStatistic.countRfamInfo(rfamFile, rfamStatisticRfambed);
		}
	}
	/** ��ȡrfam��Ϣ������
	 * @param solo ��������
	 *  */
	private void countNCrna(boolean solo) {
		if (!solo && FileOperate.isFileExistAndBigThanSize(MiRNAmappingPipline.getOutNCRNAbed(), 10)) {
			readsOnNCrna.setBedSed(MiRNAmappingPipline.getOutNCRNAbed());
		}
		else if (solo && FileOperate.isFileExist(readsOnNCrnaBed) && FileOperate.getFileSize(readsOnNCrnaBed) > 1000) {
			readsOnNCrna.setBedSed(readsOnNCrnaBed);
		}
		readsOnNCrna.searchNCrna();
		readsOnNCrna.writeToFile(outPathPrefix + "_NCrnaStatistics.txt");
	}
}