package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
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
	
	Map<String, AlignSeq> mapBedFile2Prefix;
	SamMapRate samMapMiRNARate = new SamMapRate();

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
		
		mapBedFile2Prefix = new HashMap<>();
		mapBedFile2Prefix.put("H1", new SamFile("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCA" +
				"AS_sheep_RNA_20130925/miRNAtest/tmpMapping/H1_Genome.bam"));
		mapBedFile2Prefix.put("H2", new SamFile("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAA" +
				"S_sheep_RNA_20130925/miRNAtest/tmpMapping/H2_Genome.bam"));
		
		lsSpeciesBlastTo = new ArrayList<>();
		lsSpeciesBlastTo.add(new Species(9913));
		lsSpeciesBlastTo.add(new Species(9606));
		
		expMirPre.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirPreAll_Counts.txt", true);
		expMirMature.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirMatureAll_Counts.txt", true);
		
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/";
	}
	
	private void runningAfterPredict() {
		if (predictMirna) {
			runPredictAlready(mapBedFile2Prefix, expMirPre, expMirMature);
		}
		blastToOtherSpecies();
		for (String condition : expMirPre.getSetCondition()) {
			System.out.println("pre:" + expMirPre.getMapCond2AllReads().get(condition) + "   mature:" + expMirMature.getMapCond2AllReads().get(condition));
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath + "miRNAstatistics.txt", true);
		txtWrite.ExcelWrite(samMapMiRNARate.getLsResult());
		txtWrite.close();
	}
	
	/** 已经预测好了算下表达就行了 */
	private void runPredictAlready(Map<String, AlignSeq> mapBedFile2Prefix, GeneExpTable expMirPre, GeneExpTable expMirMature) {
		ctrlMiRNApredict.setMapPrefix2GenomeSamFile(mapBedFile2Prefix);
		ctrlMiRNApredict.setOutPath(outPath);
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		if (mapMirna) {
			ctrlMiRNApredict.setExpMir(expMirPre, expMirMature);
		}
		ctrlMiRNApredict.runMiRNApredict(samMapMiRNARate);
		ctrlMiRNApredict.writeToFile();
		logger.info("finish predict");
	}
	
	private void blastToOtherSpecies() {
		if (lsSpeciesBlastTo.size() == 0) {
			return;
		}
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		for (String prefix : ctrlMiRNApredict.getMapPrefix2UnmapFq().keySet()) {
			mirSpeciesPipline.addSample(prefix, ctrlMiRNApredict.getMapPrefix2UnmapFq().get(prefix));
		}
		mirSpeciesPipline.setExpMir(ctrlMiRNApredict.getExpMirPre(), ctrlMiRNApredict.getExpMirMature());
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat(), samMapMiRNARate);
		mirSpeciesPipline.writeToFile();
	}
	
}
