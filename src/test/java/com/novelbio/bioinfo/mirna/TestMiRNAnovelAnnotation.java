package com.novelbio.bioinfo.mirna;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.bioinfo.mirna.MiRNAnovelAnnotaion;
import com.novelbio.database.domain.species.Species;

public class TestMiRNAnovelAnnotation {
	MiRNAnovelAnnotaion miRNAnovelAnnotaion = new MiRNAnovelAnnotaion();
	
	
	@Test
	public void test() {
		miRNAnovelAnnotaion.setMiRNAthis("/media/hdfs/nbCloud/public/test/miRNApredict/miRNA.fa");
		List<Species> lsSpecies = new ArrayList<>();
		lsSpecies.add(new Species(10090));
		miRNAnovelAnnotaion.setLsMiRNAblastTo(lsSpecies, "/media/hdfs/nbCloud/public/test/miRNApredict/blastto.fa");
		miRNAnovelAnnotaion.annotation();
	}
}
