package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class TestMirPipeline {
	private static final Logger logger = Logger.getLogger(TestMirPipeline.class);
	Species species = new Species(9940);
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.miRNAName);
	CtrlMiRNAfastq ctrlMiRNAfastq;
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	Map<String, String> mapfastqFile2Prefix;
	List<Species> lsSpeciesBlastTo;
	String outPath;
	
	Map<String, AlignSeq> mapPrefix2GenomeSam;

	public static void main(String[] args) {
		TestMirPipeline testMirPipeline = new TestMirPipeline();
		testMirPipeline.preperData();
		testMirPipeline.running();
	}

	/** 设定输出路径 */
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	private void preperData() {
		mapfastqFile2Prefix = new LinkedHashMap<>();
		mapfastqFile2Prefix.put("H1", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_H/Sheep_H.clean.fa.fastq");
		mapfastqFile2Prefix.put("H2", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_Q/Sheep_Q.clean.fa.fastq");
		
		lsSpeciesBlastTo = new ArrayList<>();
		lsSpeciesBlastTo.add(new Species(9913));
		lsSpeciesBlastTo.add(new Species(9606));
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtestcow/";
	}
	
	private void running() {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		ctrlMiRNAfastq = new CtrlMiRNAfastq();
		ctrlMiRNAfastq.setMiRNAexp(expMirPre, expMirMature);
		if (mapMirna) {
			runMapping(gffChrAbs, species, mapfastqFile2Prefix);
			mapPrefix2GenomeSam = ctrlMiRNAfastq.getMapPrefix2GenomeSam();
		}
		if (predictMirna) {
			runPredict(mapPrefix2GenomeSam, gffChrAbs, species);
		}
		if (lsSpeciesBlastTo.size() > 0) {
			blastToOtherSpecies();
		}
	
	}
	private void runMapping(GffChrAbs gffChrAbs, Species species, Map<String, String> mapPrefix2Fastq) {
		ctrlMiRNAfastq.setMappingAll2Genome(false);
		ctrlMiRNAfastq.setRfamSpeciesSpecific(false);
		ctrlMiRNAfastq.setSpecies(species);
		ctrlMiRNAfastq.setOutPath(outPath);
		ctrlMiRNAfastq.setGffChrAbs(gffChrAbs);
		ctrlMiRNAfastq.setMapPrefix2Fastq(mapPrefix2Fastq);
		ctrlMiRNAfastq.setMiRNAinfo(PathDetailNBC.getMiRNADat());
		ctrlMiRNAfastq.setRfamFile(PathDetailNBC.getRfamTab());
		ctrlMiRNAfastq.setMapAll2Rfam(true);
		ctrlMiRNAfastq.mappingAndCounting();
		ctrlMiRNAfastq.writeToFile();
		logger.info("finish mapping");
	}
	/** 从头预测 */
	private void runPredict(Map<String, AlignSeq> mapPrefix2BedFile, GffChrAbs gffChrAbs, Species species) {
		ctrlMiRNApredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNApredict.setSpecies(species);
		ctrlMiRNApredict.setMapPrefix2GenomeSamFile(mapPrefix2BedFile);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
		ctrlMiRNApredict.predictAndCalculate();
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	/** 比对到别的物种 */
	private void blastToOtherSpecies() {
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		mirSpeciesPipline.setExpMir(expMirPre, expMirMature);
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat());
		mirSpeciesPipline.writeToFile(outPath);
	}
}
