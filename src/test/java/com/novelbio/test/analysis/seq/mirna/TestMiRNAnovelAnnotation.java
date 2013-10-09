package com.novelbio.test.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.mirna.MiRNAnovelAnnotaion;
import com.novelbio.database.model.species.Species;

public class TestMiRNAnovelAnnotation {
	MiRNAnovelAnnotaion miRNAnovelAnnotaion = new MiRNAnovelAnnotaion();
	
	
	@Test
	public void test() {
		miRNAnovelAnnotaion.setMiRNAthis("/media/hdfs/nbCloud/public/test/miRNApredict/miRNA.fa");
		List<Species> lsSpecies = new ArrayList<>();
		lsSpecies.add(new Species(10090));
		miRNAnovelAnnotaion.setLsMiRNAblastTo(lsSpecies);
		miRNAnovelAnnotaion.annotation();
	}
}
