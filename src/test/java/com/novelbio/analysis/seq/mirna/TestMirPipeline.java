package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

public class TestMirPipeline {
	private static final Logger logger = Logger.getLogger(TestMirPipeline.class);
	Species species = new Species(9940);
	Map<String, String> mapPrefix2Fastq;
	Map<String, AlignSeq> mapBedFile2Prefix;
	List<Species> lsSpeciesBlastTo;
	String mirPreAll;
	String mirMatureAll;
	String outPath;
	private void preperData() {
		mapPrefix2Fastq = new LinkedHashMap<>();
		mapPrefix2Fastq.put("H1", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_H/Sheep_H.clean.fa.fastq");
		mapPrefix2Fastq.put("H2", "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA" +
				"-Data/Sheep_Q/Sheep_Q.clean.fa.fastq");
		
		mapBedFile2Prefix = new HashMap<>();
		mapBedFile2Prefix.put("H1", new SamFile("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCA" +
				"AS_sheep_RNA_20130925/miRNAtest/tmpMapping/H1_Genome.bam"));
		mapBedFile2Prefix.put("H2", new SamFile("/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAA" +
				"S_sheep_RNA_20130925/miRNAtest/tmpMapping/H2_Genome.bam"));
		
		
		mirPreAll = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirPreAll_Counts.txt";
		mirMatureAll = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/mirMatureAll_Counts.txt";
		
		lsSpeciesBlastTo = new ArrayList<>();
		lsSpeciesBlastTo.add(new Species(9913));
		lsSpeciesBlastTo.add(new Species(9606));
		outPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNAtest/";
	}
	
//	@Test
	public void runMirna() {
		preperData();
		CtrlMiRNApipeline ctrlMiRNApipeline = new CtrlMiRNApipeline(species);
		ctrlMiRNApipeline.setMapPrefix2Fastq(mapPrefix2Fastq);
		ctrlMiRNApipeline.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApipeline.setOutPath(outPath);
		ctrlMiRNApipeline.run();
	}
	@Test
	public void runMirnaWithoutPredict() {
		preperData();
		CtrlMiRNApipeline ctrlMiRNApipeline = new CtrlMiRNApipeline(species);
		ctrlMiRNApipeline.setMapPrefix2Fastq(mapPrefix2Fastq);
		ctrlMiRNApipeline.setMapPrefix2AlignFile(mapBedFile2Prefix);
		ctrlMiRNApipeline.setMapMirna(false);
		ctrlMiRNApipeline.setPredictAlready(true);
		ctrlMiRNApipeline.readExistMiRNA(mirPreAll, mirMatureAll);
		ctrlMiRNApipeline.setLsSpeciesBlastTo(lsSpeciesBlastTo);
		ctrlMiRNApipeline.setOutPath(outPath);
 		ctrlMiRNApipeline.run();
	}
}
