package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class TestMirPipelineAfterPredict {
	private static final Logger logger = Logger.getLogger(TestMirPipelineAfterPredict.class);
	Species species = new Species(9940);
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.MirName);

	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	List<String[]> lsfastqFile2Prefix;
	List<Species> lsSpeciesBlastTo;
	String outPath;
	
	Map<AlignSeq, String> mapBedFile2Prefix;

	public static void main(String[] args) {
		TestMirPipelineAfterPredict testMirPipeline = new TestMirPipelineAfterPredict();
		testMirPipeline.preperData();
		testMirPipeline.runningAfterPredict();
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
		
		expMirPre.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirPreAll_Counts.txt");
		expMirMature.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirMatureAll_Counts.txt");
		
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/";
	}
	
	private void runningAfterPredict() {
		if (predictMirna) {
			runPredictAlready(mapBedFile2Prefix, expMirPre, expMirMature);
		}
		blastToOtherSpecies();
	}
	
	/** 已经预测好了算下表达就行了 */
	private void runPredictAlready(Map<AlignSeq, String> mapBedFile2Prefix, GeneExpTable expMirPre, GeneExpTable expMirMature) {
		ctrlMiRNApredict.setLsSamFile2Prefix(mapBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		if (mapMirna) {
			ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
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
