package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;

public class TestMirPipeline {
	private static final Logger logger = Logger.getLogger(TestMirPipeline.class);
	Species species = new Species(9940);
	CtrlMiRNAfastq ctrlMiRNAfastq;
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	List<String[]> lsfastqFile2Prefix;
	List<Species> lsSpeciesBlastTo;
	String outPath;
	
	Map<AlignSeq, String> mapBedFile2Prefix;

	public static void main(String[] args) {
		TestMirPipeline testMirPipeline = new TestMirPipeline();
		testMirPipeline.preperData();
		testMirPipeline.runningFromHead();
	}
	
	private void preperData() {
		lsfastqFile2Prefix = new ArrayList<>();
		lsfastqFile2Prefix.add(new String[]{ "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_H/Sheep_H.clean.fa.fastq", "H1"});
		lsfastqFile2Prefix.add(new String[]{ "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_Q/Sheep_Q.clean.fa.fastq", "H2"});
		
		lsSpeciesBlastTo = new ArrayList<>();
		lsSpeciesBlastTo.add(new Species(9913));
		lsSpeciesBlastTo.add(new Species(9606));
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/";
	}
	
	private void runningFromHead() {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		ctrlMiRNAfastq = new CtrlMiRNAfastq();
		if (mapMirna) {
			runMapping(gffChrAbs, species, lsfastqFile2Prefix);
			mapBedFile2Prefix = ctrlMiRNAfastq.getMapGenomeSam2Prefix();
		}
		if (predictMirna) {
			runPredict(mapBedFile2Prefix, gffChrAbs, species);
		}
		blastToOtherSpecies();
	}
	private void runMapping(GffChrAbs gffChrAbs, Species species, List<String[]> lsfastqFile2Prefix) {
		ctrlMiRNAfastq.setMappingAll2Genome(false);
		ctrlMiRNAfastq.setRfamSpeciesSpecific(false);
		ctrlMiRNAfastq.setSpecies(species);
		ctrlMiRNAfastq.setOutPath(outPath);
		ctrlMiRNAfastq.setGffChrAbs(gffChrAbs);
		ctrlMiRNAfastq.setLsFastqFile(lsfastqFile2Prefix);
		ctrlMiRNAfastq.setMiRNAinfo(PathDetailNBC.getMiRNADat());
		ctrlMiRNAfastq.setRfamFile(PathDetailNBC.getRfamTab());
		ctrlMiRNAfastq.setMapAll2Rfam(true);
		ctrlMiRNAfastq.mappingAndCounting();
		ctrlMiRNAfastq.writeToFile();
		logger.info("finish mapping");
	}
	/** 从头预测 */
	private void runPredict(Map<AlignSeq, String> mapBedFile2Prefix, GffChrAbs gffChrAbs, Species species) {
		ctrlMiRNApredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNApredict.setSpecies(species);
		ctrlMiRNApredict.setLsSamFile2Prefix(mapBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		if (mapMirna) {
			ctrlMiRNApredict.setExpMir(ctrlMiRNAfastq.getExpMirPre(), ctrlMiRNAfastq.getExpMirMature());
		}
		ctrlMiRNApredict.predictAndCalculate();
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	private void blastToOtherSpecies() {
		if (lsSpeciesBlastTo.size() == 0) {
			return;
		}
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		if (predictMirna) {
			mirSpeciesPipline.setExpMir(ctrlMiRNApredict.getExpMirPre(), ctrlMiRNApredict.getExpMirMature());
		} else {
			mirSpeciesPipline.setExpMir(ctrlMiRNAfastq.getExpMirPre(), ctrlMiRNApredict.getExpMirMature());
		}
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat());
		mirSpeciesPipline.writeToFile(outPath);
	}
}
