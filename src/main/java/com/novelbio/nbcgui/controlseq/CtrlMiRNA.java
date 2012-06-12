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
 * microRNA�������
 * �ڲ��趨�»�������
 * @author zong0jie
 *
 */
public class CtrlMiRNA {
	/** mapping ���� */
	MappingMiRNA mappingMiRNA = new MappingMiRNA();
	/** ����СRNA��� */
	MiRNACount miRNACount = new MiRNACount();
	/** Repeat����� */
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	/** refseq��ncRNA������ */
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/** ����ļ����Լ�ǰ׺ */
	String outPathPrefix = "";
	
	/** gffType */
	String gffType = "";
	/** hg19��gff֮�࣬����СRNA��λ�� */
	String gffFile = "";
	/** Ⱦɫ���ļ� */
	String chrFa = "";
	/** �趨gff��chrome */
	GffChrAbs gffChrAbs = null;
	/** miRNAǰ�� */
	String hairpinMiRNA = "";
	/** miRNA������ */
	String matureMiRNA = "";
	
	/** repeat ��gff�ļ� */
	String repeatFile = null;
	/** Rfam�ȶ� */
	RfamStatistic rfamStatistic = new RfamStatistic();
	String rfamFile = "";
	String mapBedFile = "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/s_6_IDX8/H36_rfam.bed";
	/**�µ�miRNAԤ�� 
	 * δ��������list�����ö����mireap��mirdeep��Ԥ�ⷽ��
	 * */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	/** Ԥ����miRNA�����reads */
	ArrayList<String> lsBedFileNovelMiRNA = new ArrayList<String>();
	
	
	public CtrlMiRNA() {
		mappingMiRNA.setExePath("");
	}
	/**
	 * �趨���ȶԵ�����
	 * @param miRNAseq ǰ������
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
	 * �趨����������У�������һ���ļ�
	 * @param mappingAll2Genome �Ƿ�ȫ��reads mapping����������ȥ
	 * @param genomeSeq �����ļ��������index
	 */
	public void setGenome(boolean mappingAll2Genome, String genomeSeq) {
		mappingMiRNA.setGenome(genomeSeq);
		mappingMiRNA.setMapping2Genome(mappingAll2Genome);
	}
	/** �趨����Ĳ����ļ� */
	public void setFastqFile(String fastqFile) {
		mappingMiRNA.setSample(fastqFile);
	}
	/** �趨����ļ��к�ǰ׺ */
	public void setOutPathPrix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
		mappingMiRNA.setOutPath(outPathPrefix);
	}
	/**
	 * �趨Ԥ����miRNA�������Ϣ
	 * @param gffType gff����
	 * @param geneGffFile gff�ļ�
	 * @param chromFa ��gff����Ӧ������
	 */
	public void setGffInfo(String gffType, String geneGffFile, String chromFa) {
		this.gffType = gffType;
		this.gffFile = geneGffFile;
		this.chrFa = chromFa;
	}
	/** �趨repeat��gff�ļ� */
	public void setRepeat(String repeatFile) {
		this.repeatFile = repeatFile;
	}
	/** �趨rfam���ļ� */
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
	}
	/** 
	 * miRNA������ʹ��
	 * �趨miRNA��ǰ�����кͳ������� 
	 * */
	public void setMirnaFile(String matureMirna) {
		this.matureMiRNA = matureMirna;
	}
	/**
	 * miRNA������ʹ��
	 * ����miRNA�ļ���������
	 * @param fileType ��ȡ����miReap���ļ�����RNA.dat ListMiRNALocation.TYPE_RNA_DATA �� ListMiRNALocation.TYPE_MIREAP
	 * @param Species ΪmiRNA.dat�е�������������ļ�����miRNA.dat���ǾͲ���д��
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, String species, String rnadatFile) {
		miRNACount.setMiRNAinfo(fileType, species, rnadatFile);
	}
	public void setLsBedFile(ArrayList<String> lsBedFileNovelMiRNA) {
		this.lsBedFileNovelMiRNA = lsBedFileNovelMiRNA;
	}
	/**
	 * ��ʼ�ȶ�
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
	 * ��ָ����bed�ļ�����
	 * @param bedSeqFile
	 */
	private void miRNApredict() {
		//////////�½��ļ���
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
	/** ����miRNA��� */
	private void countMiRNA() {
		miRNACount.setMiRNAfile(hairpinMiRNA, matureMiRNA);
		if (FileOperate.isFileExist(mappingMiRNA.getOutMiRNAbed()) && FileOperate.getFileSize(mappingMiRNA.getOutMiRNAbed()) > 1000) {
			miRNACount.setBedSeqMiRNA(mappingMiRNA.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.outResult(outPathPrefix);
		}
	}
	/** ��ȡ��Ϣ */
	private void readGffInfo() {
		readsOnRepeatGene.readGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(repeatFile);
	}
	/** ��ȡrepeat��gene��Ϣ */
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