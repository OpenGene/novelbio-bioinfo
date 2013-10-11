package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.model.species.Species;

public class TestCtrlMiRNApredict {
	CtrlMiRNApredict ctrlMiRNApredict = new CtrlMiRNApredict();
	String parentPath = "/media/hdfs/nbCloud/public/customer/gaohongmei_IASCAAS_sheep_RNA_20130925/miRNA-Data/mapping2cow/tmpMapping/";
	public void setUp() {
		ctrlMiRNApredict.novelMiRNADeep.novelMiRNAdeepMrdFile = parentPath + "test/miRNApredictDeep/run/output.mrd";
		ctrlMiRNApredict.novelMiRNADeep.novelMiRNAhairpin = parentPath + "test/miRNApredictDeep/novelMiRNA/hairpin.fa";
		ctrlMiRNApredict.novelMiRNADeep.novelMiRNAmature = parentPath + "test/miRNApredictDeep/novelMiRNA/mature.fa";
		
		Map<SamFile, String> map = new HashMap<>();
		map.put(new SamFile(parentPath + "H_Genome.bam"), "H");
		map.put(new SamFile(parentPath + "Q_Genome.bam"), "Q");
		ctrlMiRNApredict.setLsSamFile2Prefix(map);
		
		List<Species> lsSpecies = new ArrayList<>();
		lsSpecies.add(new Species(9606));
		ctrlMiRNApredict.setLsSpeciesBlastTo(lsSpecies);
	}
	
	@Test
	public void testAnno() {
		setUp();
		ctrlMiRNApredict.setOutPath(parentPath + "test/miRNAanno");
		ctrlMiRNApredict.calculateExp();
		ctrlMiRNApredict.writeToFile();
	}
}
