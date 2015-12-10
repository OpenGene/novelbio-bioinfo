package com.novelbio.database.cosmic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.cosmic.CompleteExport;

public class TestCosmicData {
	
	public void testCreatCodingMuts() {
		String codingMutsPath = "src/test/resources/test_file/cosmic/CosmicCodingMuts.vcf";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(codingMutsPath);
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CodingMuts codingMuts = CodingMuts.getInstanceFromCodingMuts(content);	
				Assert.assertEquals(69224,codingMuts.getPos());  
				Assert.assertEquals("COSM3677745",codingMuts.getCosmicId());
				Assert.assertEquals("C",codingMuts.getAlt());
				Assert.assertEquals("A",codingMuts.getRef());
				Assert.assertEquals("c.134A>C",codingMuts.getCdsChange());
				Assert.assertEquals("p.D45A",codingMuts.getAAChange());
			}
		}
		txtCancerGene.close();
	}
	
	public void testCreatCancerGene() {
		String codingMutsPath = "src/test/resources/test_file/cosmic/cancer_gene_census.csv.txt";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(codingMutsPath);
		List<String> lsTumTypeSom = new ArrayList<>();
		lsTumTypeSom.add("AML");
		List<String> lsTissueType = new ArrayList<>();
		lsTissueType.add("L");
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CancerGene cancerGene = CancerGene.getInstanceFromCancerGene(content);	
				Assert.assertEquals(10006,cancerGene.getGeneId());  
				Assert.assertEquals("10p11.2",cancerGene.getChrBand());
				Assert.assertEquals("10:26748570-26860863",cancerGene.getGenomeLocation());
				Assert.assertEquals(lsTumTypeSom,cancerGene.getLsTumTypeSom());
				Assert.assertEquals(lsTissueType,cancerGene.getLsTissueType());
				Assert.assertEquals("Dom",cancerGene.getMoleGenetics());
			}
		}
		txtCancerGene.close();
	}
	@Test
	public void testCreatCompleteExport() {
		String codingMutsPath = "src/test/resources/test_file/cosmic/CosmicCompleteExport.tsv";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(codingMutsPath);
		List<String> lsTumTypeSom = new ArrayList<>();
		lsTumTypeSom.add("AML");
		List<String> lsTissueType = new ArrayList<>();
		lsTissueType.add("L");
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CompleteExport completeExport = CompleteExport.getInstanceFromCodingMuts(content);	
				Assert.assertEquals(673,completeExport.getGeneId());  
				Assert.assertEquals("ENST00000288602",completeExport.getAccessionNum());
				Assert.assertEquals(2301,completeExport.getcDSLength());
				Assert.assertEquals(1097,completeExport.gethGNCId());
				Assert.assertEquals("E16970",completeExport.getSampleName());
				Assert.assertEquals(686393,completeExport.getSampleID());
				Assert.assertEquals(614553,completeExport.getTumourID());
				Assert.assertEquals("lung",completeExport.getPriSiteCos());
				Assert.assertEquals("NS",completeExport.getSiteSubtype1());
				Assert.assertEquals("NS",completeExport.getSiteSubtype2());
				Assert.assertEquals("NS",completeExport.getSiteSubtype3());
				Assert.assertEquals("carcinoma",completeExport.getPrihistCos());
				Assert.assertEquals("adenocarcinoma",completeExport.getHistSubtype1());
				Assert.assertEquals("NS",completeExport.getHistSubtype2());
				Assert.assertEquals("NS",completeExport.getHistSubtype3());
				Assert.assertEquals("COSM444",completeExport.getMutationID());
				Assert.assertEquals("Substitution - Missense",completeExport.getMutationDes());
				Assert.assertEquals("",completeExport.getMutationZyg());
				Assert.assertEquals("Variant of unknown origin",completeExport.getMutSomSta());
				Assert.assertEquals("y",completeExport.getlOH());
				Assert.assertEquals("PATHOGENIC",completeExport.getFathmmPre());
				Assert.assertEquals("0.97787",completeExport.getFathmmScore() + "");
				Assert.assertEquals("12460918",completeExport.getPubmedPMID() + "");
				Assert.assertEquals(0,completeExport.getStudyId());
				Assert.assertEquals("primary",completeExport.getTumourOrigin());
				Assert.assertEquals("0.0",completeExport.getAge() + "");
			}
		}
		txtCancerGene.close();
	}
	
	public void testCreatNCV() {
		String codingMutsPath = "src/test/resources/test_file/cosmic/cancer_gene_census.csv.txt";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(codingMutsPath);
		List<String> lsTumTypeSom = new ArrayList<>();
		lsTumTypeSom.add("AML");
		List<String> lsTissueType = new ArrayList<>();
		lsTissueType.add("L");
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CancerGene cancerGene = CancerGene.getInstanceFromCancerGene(content);	
				Assert.assertEquals(10006,cancerGene.getGeneId());  
				Assert.assertEquals("10p11.2",cancerGene.getChrBand());
				Assert.assertEquals("10:26748570-26860863",cancerGene.getGenomeLocation());
				Assert.assertEquals(lsTumTypeSom,cancerGene.getLsTumTypeSom());
				Assert.assertEquals(lsTissueType,cancerGene.getLsTissueType());
				Assert.assertEquals("Dom",cancerGene.getMoleGenetics());
			}
		}
		txtCancerGene.close();
	}
	
	
	
}
