package com.novelbio.analysis.seq.genome.gffoperate;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.genome.gffoperate.GffHashGTF;

public class TestGffHashGTF {
	@Test
	public void testGetIsoName2GeneNameStr() {
		String[] iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5DL_91B56C21D\";transcript_id \"Traes_5DL_91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL_91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5DL_91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5D; L; _91B56C21D\";transcript_id \"Traes_5DL _91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL _91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5D; L; _91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("gene_id \"Traes_5D; L; _91B56C21D\";\texonnum \"1\"transcript_id \"Traes_5DL _91B56C21D.1\";", null);
		Assert.assertEquals("Traes_5DL _91B56C21D.1", iso2Gene[0]);
		Assert.assertEquals("Traes_5D; L; _91B56C21D", iso2Gene[1]);
		
		iso2Gene = GffHashGTF.getIsoName2GeneName("\"gene_id\"\"Contig10000\"; \"transcript_id\"\"Contig100001\"", null);
		Assert.assertEquals("Contig100001", iso2Gene[0]);
		Assert.assertEquals("Contig10000", iso2Gene[1]);
	}
	
	@Test
	public void testGetMapId2ValueSS8() {
		Map<String, String> mapId2Value = GffHashGTF.getMapId2ValueSS8("gene_id \"Traes_5DL_91B56C21D\";transcript_id \"Traes_5DL_91B56C21D.1\";");
		Map<String, String> mapId2ValueExp = new HashMap<>();
		mapId2ValueExp.put("gene_id", "Traes_5DL_91B56C21D");
		mapId2ValueExp.put("transcript_id", "Traes_5DL_91B56C21D.1");
		assertEquals(mapId2ValueExp, mapId2Value);

		mapId2Value = GffHashGTF.getMapId2ValueSS8("gene_id \"Traes_5D; L; _91B56C21D\";transcript_id \"Traes_5DL _91B56C21D.1\";");
		mapId2ValueExp = new HashMap<>();
		mapId2ValueExp.put("gene_id", "Traes_5D; L; _91B56C21D");
		mapId2ValueExp.put("transcript_id", "Traes_5DL _91B56C21D.1");
		assertEquals(mapId2ValueExp, mapId2Value);
		
		mapId2Value = GffHashGTF.getMapId2ValueSS8("\"gene_id\" \"Traes_5D; L; _91B56C21D\";\texonnum \"1\" transcript_id Traes_5DL_91B56C21D.1;");
		mapId2ValueExp = new HashMap<>();
		mapId2ValueExp.put("gene_id", "Traes_5D; L; _91B56C21D");
		mapId2ValueExp.put("exonnum", "1");
		mapId2ValueExp.put("transcript_id", "Traes_5DL_91B56C21D.1");
		assertEquals(mapId2ValueExp, mapId2Value);
		
		mapId2Value = GffHashGTF.getMapId2ValueSS8Commo("gene_id \"Os02g0731700\"; gene_name \"B-C1) domrotein 9, \"Grait, and; hea da2\", 1 protein\"; gene_source \"ensembl\"; gene_biotype \"protein_coding\";");
		mapId2ValueExp = new HashMap<>();
		mapId2ValueExp.put("gene_id", "Os02g0731700");
		mapId2ValueExp.put("gene_name", "B-C1) domrotein 9, \"Grait, and hea da2\", 1 protein");
		mapId2ValueExp.put("gene_source", "ensembl");
		mapId2ValueExp.put("gene_biotype", "protein_coding");
		assertEquals(mapId2ValueExp, mapId2Value);

		mapId2Value = GffHashGTF.getMapId2ValueSS8("gene_name \"\"chloroplastic fructose 1, 6-bisphosphatase\", chloroplastic FruP2ase,"
				+ " \"fructose-1, 6-bisphosphatase plastidic isoform\", \"Plastidic Fructose-1, 6-Bisphosphatase\"\"; gene_source \"ensembl\"; gene_biotype \"protein_coding\";");
		mapId2ValueExp = new HashMap<>();
		mapId2ValueExp.put("gene_name", "\"chloroplastic fructose 1, 6-bisphosphatase\", chloroplastic FruP2ase, \"fructose-1, 6-bisphosphatase plastidic isoform\", \"Plastidic Fructose-1, 6-Bisphosphatase\"");
		mapId2ValueExp.put("gene_source", "ensembl");
		mapId2ValueExp.put("gene_biotype", "protein_coding");
		assertEquals(mapId2ValueExp, mapId2Value);
	}
}
