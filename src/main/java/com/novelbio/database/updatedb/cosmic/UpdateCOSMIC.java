package com.novelbio.database.updatedb.cosmic;

import java.util.ArrayList;
import java.util.List;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.cosmic.CompleteExport;
import com.novelbio.database.domain.cosmic.CosmicAbb;
import com.novelbio.database.domain.cosmic.CosmicAllSNVs;
import com.novelbio.database.domain.cosmic.CosmicCNV;
import com.novelbio.database.domain.cosmic.NonCodingVars;
import com.novelbio.database.service.servcosmic.MgmtCancerGene;
import com.novelbio.database.service.servcosmic.MgmtCodingMuts;
import com.novelbio.database.service.servcosmic.MgmtCompleteExport;
import com.novelbio.database.service.servcosmic.MgmtCosmicAbb;
import com.novelbio.database.service.servcosmic.MgmtCosmicAllSNVs;
import com.novelbio.database.service.servcosmic.MgmtNCV;
import com.novelbio.database.service.servcosmic.MgmtNonCodingVars;

public class UpdateCOSMIC {
	static String cancerGenePath = "/home/novelbio/bianlianle/tmp/cosmic/cancer_gene_census_change.txt";	
	static String codingMutsPath = "/home/novelbio/bianlianle/tmp/cosmic/CosmicCodingMuts.vcf.gz";	
	static String completeExportPath = "/home/novelbio/bianlianle/tmp/cosmic/CosmicCompleteExport.tsv";
	static String nCVPath = "/home/novelbio/bianlianle/tmp/CosmicNCV.tsv.10.txt";
	static String nonCodingVarsPath = "/home/novelbio/bianlianle/tmp/cosmic/CosmicNonCodingVariants.1.vcf.gz";
	static String abbFile = "/home/novelbio/bianlianle/tmp/Abbreviation.txt";	
	
	public static void main(String[] args) {
		UpdateCOSMIC updateCOSMIC = new UpdateCOSMIC();
//		updateCOSMIC.creatCancerGene(cancerGenePath);
		updateCOSMIC.creatCodingMuts(codingMutsPath);
//		updateCOSMIC.creatCompleteExport(completeExportPath);
//		updateCOSMIC.creatNCV(nCVPath);
//		updateCOSMIC.creatNonCodingVars(nonCodingVarsPath);
//		updateCOSMIC.creatCosmicAllSNVs(codingMutsPath, true);
//		updateCOSMIC.creatCosmicAllSNVs(nonCodingVarsPath, false);
//		updateCOSMIC.creatCosmicAbb(abbFile);
		System.out.println("finished!");
	}
	
	public void creatCancerGene(String inFile) {
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(inFile);
		MgmtCancerGene mgmtCancerGene = MgmtCancerGene.getInstance();
		for (String content : txtCancerGene.readlines()) {
			CancerGene cancerGene = CancerGene.getInstanceFromCancerGene(content);			
			if (!(cancerGene == null)) {
			mgmtCancerGene.save(cancerGene);
			}	
		}
		txtCancerGene.close();
	}
	public void creatCodingMuts(String inFile) {
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(inFile);
		MgmtCodingMuts mgmtCodingMuts = MgmtCodingMuts.getInstance();
		for (String content : txtCancerGene.readlines()) {
			if (!content.startsWith("#")) {
				CodingMuts codingMuts = CodingMuts.getInstanceFromCodingMuts(content);			
				if (!(codingMuts == null)) {
					mgmtCodingMuts.save(codingMuts);
				}
			}
		}
		txtCancerGene.close();
	}
	public void creatCompleteExport(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtCompleteExport mgmtCompleteExport = MgmtCompleteExport.getInstance();
		List<CompleteExport> lsCompleteExport = new ArrayList<>();
		int rowCount = txtCompleteExport.ExcelRows()-1;
		int mod = rowCount % 10000;
		int mulriple = (int)Math.floor(rowCount/10000);
		int circulation = 0;
//		long start = System.currentTimeMillis();
		for (String content : txtCompleteExport.readlines()) {
			CompleteExport completeExport = CompleteExport.getInstanceFromCodingMuts(content);
			if (completeExport != null) {
//				mgmtCompleteExport.save(completeExport);
				lsCompleteExport.add(completeExport);
			}
			if (lsCompleteExport.size()==10000) {
//				long end = System.currentTimeMillis();
//				System.out.println("time1= " + (end - start));
				mgmtCompleteExport.save(lsCompleteExport);
//				long end2 = System.currentTimeMillis();
//				System.out.println("time1= " + (end2 - end));
				lsCompleteExport.clear();
				circulation ++;
//				System.out.println("saved line is " + circulation);
//				start = System.currentTimeMillis();
			} else if ((circulation == mulriple) && (lsCompleteExport.size()==mod)) {
				mgmtCompleteExport.save(lsCompleteExport);
				lsCompleteExport.clear();
			}
		}
		txtCompleteExport.close();
	}
	
	public void creatNCV(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtNCV mgmtNCV = MgmtNCV.getInstance();
		List<CosmicCNV> lsCosmicCNV= new ArrayList<>();
		int rowCount = txtCompleteExport.ExcelRows()-1;
		int mod = rowCount % 10000;
		int mulriple = (int)Math.floor(rowCount/10000);
		int circulation = 0;
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				CosmicCNV cosmicCNV = CosmicCNV.getInstanceFromNCV(content);
				if (!(cosmicCNV == null)) {
					lsCosmicCNV.add(cosmicCNV);
//					mgmtNCV.save(cosmicCNV);
				}
				if (lsCosmicCNV.size()==10000) {
					mgmtNCV.save(lsCosmicCNV);
					lsCosmicCNV.clear();
					circulation ++;
				} else if ((circulation == mulriple) && (lsCosmicCNV.size()==mod)) {
					mgmtNCV.save(lsCosmicCNV);
					lsCosmicCNV.clear();
				}
			}
		}
		txtCompleteExport.close();
	}
	public void creatNonCodingVars(String inFile) {
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inFile));
		MgmtNonCodingVars mgmtNonCodingVars = MgmtNonCodingVars.getInstance();
		for (VariantContext variantContext : vcfFileReader) {
				NonCodingVars nonCodingVars = NonCodingVars.getInstanceFromNonCodingVars(variantContext);
				if (!(nonCodingVars == null)) {
					mgmtNonCodingVars.save(nonCodingVars);
				}
		}
	}
	
	public void creatCosmicAllSNVs(String inFile, boolean isCodingVars) {
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inFile));
		MgmtCosmicAllSNVs mgmtCosmicAllSNVs = MgmtCosmicAllSNVs.getInstance();
		for (VariantContext variantContext : vcfFileReader) {
			CosmicAllSNVs cosmicAllSNVs = CosmicAllSNVs.getInstanceFromNonCodingVars(variantContext,isCodingVars);
			if (!(cosmicAllSNVs == null)) {
				mgmtCosmicAllSNVs.save(cosmicAllSNVs);
			}			
		}
	}
	
	
	public void creatCosmicAbb(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtCosmicAbb mgmtCosmicAbb = MgmtCosmicAbb.getInstance();
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				CosmicAbb cosmicAbb = CosmicAbb.getInstanceFromCosmicAbb(content);
				if (!(cosmicAbb == null)) {
					mgmtCosmicAbb.save(cosmicAbb);
				}
			}
		}
		txtCompleteExport.close();
	}	
}
