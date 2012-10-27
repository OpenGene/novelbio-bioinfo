package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

/** 
 * ����һϵ��fastq�ļ������miRNA��bed�ļ�
 * @author zong0jie
 */
public class CtrlMiRNAfastq {
	Logger logger = Logger.getLogger(CtrlMiRNAfastq.class);
	
	Species species;
	/** �趨gff��chrome */
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	/** mapping ���� */
	MiRNAmapPipline miRNAmappingPipline = new MiRNAmapPipline();
	
	MiRNACount miRNACount = new MiRNACount();
	RfamStatistic rfamStatistic = new RfamStatistic();
	ReadsOnRepeatGene readsOnRepeatGene = new ReadsOnRepeatGene();
	ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
	/**�µ�miRNAԤ��   δ��������list�����ö����mireap��mirdeep��Ԥ�ⷽ��  */
	NovelMiRNAReap novelMiRNAReap = new NovelMiRNAReap();
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();
	
	/////�����ļ� ///////
	String rfamFile;
	
	/** fastqFile--prefix */
	ArrayList<String[]> lsFastqFile2Prefix;
	
	///////����ļ��� //////////
	String outPath;
	String outPathTmpMapping;
	String outPathTmpBed;
	///////������� ///////////
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAPre = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAmature = new HashMap<String, HashMap<String,Double>>();
	
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNArfam = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2MiRNAncrna = new HashMap<String, HashMap<String,Double>>();
	
	HashMap<String, HashMap<String, Double>> mapPrefix2RepeatName = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2RepeatFamily = new HashMap<String, HashMap<String,Double>>();
	HashMap<String, HashMap<String, Double>> mapPrefix2GeneInfo = new HashMap<String, HashMap<String,Double>>();
	
	/** ��������趨 */
	public void setSpecies(Species species) {
		this.species = species;
		gffChrAbs.setSpecies(species);
	}
	/** ���Ȼ��ж�����gffChrAbs��species�Ƿ�һ�� */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (this.gffChrAbs.getSpecies().equals(gffChrAbs.getSpecies())) {
			return;
		}
		this.gffChrAbs = gffChrAbs;
	}
	
	public void setLsFastqFile(ArrayList<String[]> lsFastqFile2Prefix) {
		this.lsFastqFile2Prefix = lsFastqFile2Prefix;
	}
	
	/** �趨����ļ��� */
	public void setOutPath(String outPath) {
		this.outPath = FileOperate.addSep(outPath);
		FileOperate.createFolders(this.outPath);
		this.outPathTmpMapping = this.outPath + "tmpMapping";
		this.outPathTmpBed = this.outPath + "tmpBed";
	}
	
	/** rfam����Ϣ�Ƚ��ļ�������һ����ֵ�� */
	public void setRfamFile(String rfamFile) {
		this.rfamFile = rfamFile;
	}
	
	/**
	 * miRNA������ʹ��
	 * @param rnadatFile miRNA.dat�ļ�
	 */
	public void setMiRNAinfo(String rnadatFile) {
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_RNA_DATA, species, rnadatFile);
	}
	
	/** �ȶԺͼ�����ÿ�ȶ�һ�ξͼ�������Ҫ��Ϊ�˷�ֹ���� */
	public void mappingAndCounting() {
		mapPrefix2MiRNAPre.clear();
		mapPrefix2MiRNAmature.clear();
		mapPrefix2MiRNArfam.clear();
		mapPrefix2MiRNAncrna.clear();
		
		FileOperate.createFolders(outPathTmpMapping);
		FileOperate.createFolders(outPathTmpBed);
		setConfigFile();
		for (String[] fastq2Prefix : lsFastqFile2Prefix) {
			//�ļ���Ϊ����ļ���+�ļ�ǰ׺
			miRNAmappingPipline.setSample(outPath + fastq2Prefix[1], fastq2Prefix[0]);
			miRNAmappingPipline.setOutPathTmp(outPathTmpMapping, outPathTmpBed);
			miRNAmappingPipline.mappingPipeline();
		}
	}
	
	/** �趨���ȶԵ����� */
	private void setConfigFile() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.bwa.toString());
		
		miRNAmappingPipline.setExePath(softWareInfo.getExePath());
		miRNAmappingPipline.setMiRNApreSeq(species.getMiRNAhairpinFile());
		miRNAmappingPipline.setNcRNAseq(species.getRefseqNCfile());
		miRNAmappingPipline.setRfamSeq(species.getRfamFile());
		miRNAmappingPipline.setGenome(species.getIndexChr(SoftWare.bwa));//Ĭ��bwa��mapping
	}
	
	
	/** ����miRNA���
	 * @param solo ǰ���Ƿ���mapping
	 */
	public void countSmallRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		countMiRNA(outPath, prefix, miRNAmappingPipline);
		countRfam(outPath, prefix, miRNAmappingPipline);
		countNCrna(outPath, prefix, miRNAmappingPipline);
		countRepeatGene(outPath, prefix, miRNAmappingPipline);
	}

	/**
	* ����miRNA���
	* @param outPath
	* @param prefix
	* @param miRNAmappingPipline
	*/
	private void countMiRNA(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		miRNACount.setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
		if (FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutMiRNAbed(),1)) {
			miRNACount.setBedSeqMiRNA(miRNAmappingPipline.getOutMiRNAbed());
			miRNACount.countMiRNA();
			miRNACount.writeResultToOut(outPath + prefix);
			mapPrefix2MiRNAmature.put(prefix, miRNACount.getMapMirMaturename2Value());
			mapPrefix2MiRNAPre.put(prefix, miRNACount.getMapMiRNAvalue());
		}
	}
	
	private void readRepeatGff() {
		if (gffChrAbs != null) {
			gffChrAbs = new GffChrAbs(species);
		}
		readsOnRepeatGene.setGffGene(gffChrAbs);
		readsOnRepeatGene.readGffRepeat(species.getGffRepeat());
	}

	/** ��ȡrfam��Ϣ������
	 * @param solo ��������
	 *  */
	private void countRfam(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		rfamStatistic.setOutputFile(outPath + prefix + "_RfamStatistics.txt");
		if (FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutRfambed(), 10)) {
			rfamStatistic.countRfamInfo(rfamFile, miRNAmappingPipline.getOutRfambed());
			mapPrefix2MiRNArfam.put(prefix, rfamStatistic.getMapRfam2Counts());
		}
	}
	/** ��ȡncRNA����Ϣ������
	 * @param solo ��������
	 *  */
	private void countNCrna(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		if (FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutNCRNAbed(), 10)) {
			readsOnNCrna.setBedSed(miRNAmappingPipline.getOutNCRNAbed());
			readsOnNCrna.searchNCrna();
			readsOnNCrna.writeToFile(outPath + prefix + "_NCrnaStatistics.txt");
			mapPrefix2MiRNAncrna.put(prefix, readsOnNCrna.getMapNCrnaID_2_nameDescripValue());
		}
	}
	
	/** ��ȡrepeat��gene��Ϣ������
	 * @param solo ��������
	 *  */
	private void countRepeatGene(String outPath, String prefix, MiRNAmapPipline miRNAmappingPipline) {
		String outFinal = outPath + prefix;
		if (FileOperate.isFileExistAndBigThanSize(miRNAmappingPipline.getOutGenomebed(), 10) ) {
			readRepeatGff();
			readsOnRepeatGene.countReadsInfo(miRNAmappingPipline.getOutGenomebed());
			readsOnRepeatGene.writeToFileGeneProp(outFinal + "_geneProp.txt");
			readsOnRepeatGene.writeToFileRepeatFamily(outFinal + "_RepeatFamily.txt");
			readsOnRepeatGene.writeToFileRepeatName(outFinal + "_RepeatName.txt");
			
			mapPrefix2RepeatFamily.put(prefix, readsOnRepeatGene.hashRepeatFamily);
			mapPrefix2RepeatName.put(prefix, readsOnRepeatGene.hashRepeatName);
			mapPrefix2GeneInfo.put(prefix, readsOnRepeatGene.hashGeneInfo);
		}
	}
	
	public void writeToFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath + "mirPreAll.txt", true);
		ArrayList<String[]> lsMirPre = miRNACount.combMapMir2Value(mapPrefix2MiRNAPre);
		txtWrite.ExcelWrite(lsMirPre);
		
		ArrayList<String[]> lsMirMature = miRNACount.combMapMir2Value(mapPrefix2MiRNAmature);
		txtWrite = new TxtReadandWrite(outPath + "mirMatureAll.txt", true);
		txtWrite.ExcelWrite(lsMirMature);
		
		
		
		
	}
	
	/** ��մ洢����Ϣ */
	public void clear() {
		 mapPrefix2MiRNAPre.clear();
		 mapPrefix2MiRNAmature.clear();
		
		 mapPrefix2MiRNArfam.clear();
		 mapPrefix2MiRNAncrna.clear();
		
		 mapPrefix2RepeatName.clear();
		 mapPrefix2RepeatFamily.clear();
		 mapPrefix2GeneInfo.clear();
	}
}
