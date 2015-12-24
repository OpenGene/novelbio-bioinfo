package com.novelbio.database.cosmic;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.cosmic.CompleteExport;
import com.novelbio.database.domain.cosmic.CosmicAbb;
import com.novelbio.database.domain.cosmic.CosmicCNV;
import com.novelbio.database.domain.cosmic.NonCodingVars;
import com.novelbio.database.service.servcosmic.MgmtNonCodingVars;

public class TestCosmicData {
	
	public void testCreatCodingMuts() {
		String codingMutsPath = "src/test/resources/test_file/cosmic/CosmicCodingMuts.1.vcf.gz";
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
		String codingMutsPath = "src/test/resources/test_file/cosmic/CosmicNCV.tsv";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(codingMutsPath);
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CosmicCNV cosmicCNV = CosmicCNV.getInstanceFromNCV(content);
				Assert.assertEquals(1474918,cosmicCNV.getSampleId());  
				Assert.assertEquals("COSN158202",cosmicCNV.getcOSMICId());
				Assert.assertEquals("TCGA-25-1319-01",cosmicCNV.getSampleName());
				Assert.assertEquals("Heterozygous",cosmicCNV.getZygosity());
				Assert.assertEquals("Confirmed somatic variant",cosmicCNV.getMutaSomSta());
				Assert.assertEquals("ADB",cosmicCNV.getFathmmNonCodGroups());
				Assert.assertEquals("0.99726",cosmicCNV.getFathmmNonCodScore() + "");
				Assert.assertEquals("AEFDBI",cosmicCNV.getFathmmCodGroups());
				Assert.assertEquals("0.99857",cosmicCNV.getFathmmCodScore() + "");
				Assert.assertEquals("n",cosmicCNV.getSnp() + "");
				Assert.assertEquals(331,cosmicCNV.getStudyId());
				Assert.assertEquals(21720365,cosmicCNV.getPubmedPMID());
			}
		}
		txtCancerGene.close();
	}
	
	public void testCreatNonCodingVars() {
		String nonCodingVarsPath = "src/test/resources/test_file/cosmic/CosmicNonCodingVariants.1.vcf.gz";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(nonCodingVarsPath);
//		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(nonCodingVarsPath));
		for (String content : txtCancerGene.readlines()) {
			NonCodingVars nonCodingVars = NonCodingVars.getInstanceFromNonCodingVars(content);
			if (!(nonCodingVars == null)) {
				Assert.assertEquals("1",nonCodingVars.getChr());  
				Assert.assertEquals(10151,nonCodingVars.getPos());  
				Assert.assertEquals("COSN14661299",nonCodingVars.getCosmicId()); 
				Assert.assertEquals("T",nonCodingVars.getRef());  
				Assert.assertEquals("A",nonCodingVars.getAlt());  
			}
		}
	}	
	@Test
	public void testCreatCosmicAbb() {
		String abbreviationFile = "src/test/resources/test_file/cosmic/Abbreviation.1.txt";
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(abbreviationFile);
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CosmicAbb cosmicAbb = CosmicAbb.getInstanceFromCosmicAbb(content);
				Assert.assertEquals("A",cosmicAbb.getAbbreviation());  
				Assert.assertEquals("amplification",cosmicAbb.getTerm());
			}
		}
		txtCancerGene.close();
	}
	
}
