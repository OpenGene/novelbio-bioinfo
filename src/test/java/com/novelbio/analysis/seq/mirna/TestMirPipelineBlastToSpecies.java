package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class TestMirPipelineBlastToSpecies {
	private static final Logger logger = Logger.getLogger(TestMirPipelineBlastToSpecies.class);
	Species species = new Species(9940);
	GeneExpTable expMirPre = new GeneExpTable(TitleFormatNBC.miRNApreName);
	GeneExpTable expMirMature = new GeneExpTable(TitleFormatNBC.MirName);

	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	boolean mapMirna = true;
	boolean predictMirna = true;
	
	List<String[]> lsfastqFile2Prefix;
	List<Species> lsSpeciesBlastTo;
	String outPath;
	
	Map<String, String> mapPrefix2Fastq;

	public static void main(String[] args) {
		TestMirPipelineBlastToSpecies testMirPipeline = new TestMirPipelineBlastToSpecies();
		testMirPipeline.preperData();
		testMirPipeline.runningAfterPredict();
	}
	
	private void preperData() {
		mapPrefix2Fastq = new HashMap<>();
		mapPrefix2Fastq.put("H1", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/tmpMapping/H1novelMiRNAunmapped.fq.gz");
		mapPrefix2Fastq.put("H2", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/tmpMapping/H2novelMiRNAunmapped.fq.gz");

		lsSpeciesBlastTo = new ArrayList<>();
		lsSpeciesBlastTo.add(new Species(9913));
		lsSpeciesBlastTo.add(new Species(9606));
		
		expMirPre.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/NovelMirPreAll_Counts.txt", true);
		expMirMature.read("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/NovelMirMatureAll_Counts.txt", true);
		
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/";
	}
	
	private void runningAfterPredict() {
		blastToOtherSpecies();
	}

	private void blastToOtherSpecies() {
		if (lsSpeciesBlastTo.size() == 0) {
			return;
		}
		MirSpeciesPipline mirSpeciesPipline = new MirSpeciesPipline();
		for (String prefix : mapPrefix2Fastq.keySet()) {
			mirSpeciesPipline.addSample(prefix, mapPrefix2Fastq.get(prefix));
		}
		mirSpeciesPipline.setExpMir(ctrlMiRNApredict.getExpMirPre(), ctrlMiRNApredict.getExpMirMature());
		mirSpeciesPipline.setLsSpecies(lsSpeciesBlastTo); 
		mirSpeciesPipline.setOutPathTmp(outPath);
		mirSpeciesPipline.setThreadNum(4);
		mirSpeciesPipline.mappingPipeline(PathDetailNBC.getMiRNADat());
		mirSpeciesPipline.writeToFile(outPath);
	}
}
