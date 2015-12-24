package com.novelbio.database.updatedb.cosmic;

import java.util.ArrayList;
import java.util.Date;
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
	static String nCVPath = "/home/novelbio/bianlianle/tmp/cosmic/CosmicNCV.tsv";
	static String nonCodingVarsPath = "/home/novelbio/bianlianle/tmp/cosmic/CosmicNonCodingVariants.vcf.gz";
	static String abbFile = "/home/novelbio/bianlianle/tmp/Abbreviation.txt";	
	
	public static void main(String[] args) {
		UpdateCOSMIC updateCOSMIC = new UpdateCOSMIC();
//		updateCOSMIC.creatCancerGene(cancerGenePath);
//		updateCOSMIC.creatCodingMuts(codingMutsPath);
//		updateCOSMIC.creatCompleteExport(completeExportPath);
//		updateCOSMIC.creatNCV(nCVPath);
//		updateCOSMIC.creatNonCodingVars(nonCodingVarsPath);
		updateCOSMIC.creatCosmicAllSNVs(codingMutsPath, true);
		System.out.println("codingMutsPath finished!" + (new Date(System.currentTimeMillis())));
		updateCOSMIC.creatCosmicAllSNVs(nonCodingVarsPath, false);
		System.out.println("nonCodingVarsPath finished!" + (new Date(System.currentTimeMillis())));
//		updateCOSMIC.creatCosmicAbb(abbFile);
		System.out.println("All finished!");
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
		for (String content : txtCompleteExport.readlines()) {
			CompleteExport completeExport = CompleteExport.getInstanceFromCodingMuts(content);
			if (completeExport != null) {
				lsCompleteExport.add(completeExport);
			}
			if (lsCompleteExport.size()==10000) {
				mgmtCompleteExport.save(lsCompleteExport);
				lsCompleteExport.clear();
				circulation ++;
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
	
//	public void creatNonCodingVars(String inFile) {
//		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inFile));
//		MgmtNonCodingVars mgmtNonCodingVars = MgmtNonCodingVars.getInstance();
//		for (VariantContext variantContext : vcfFileReader) {
//				NonCodingVars nonCodingVars = NonCodingVars.getInstanceFromNonCodingVars(variantContext);
//				if (!(nonCodingVars == null)) {
//					mgmtNonCodingVars.save(nonCodingVars);
//				}
//		}
//	}
//	
//	public void creatCosmicAllSNVs(String inFile, boolean isCodingVars) {
//		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inFile));
//		MgmtCosmicAllSNVs mgmtCosmicAllSNVs = MgmtCosmicAllSNVs.getInstance();
//		for (VariantContext variantContext : vcfFileReader) {
//			CosmicAllSNVs cosmicAllSNVs = CosmicAllSNVs.getInstanceFromNonCodingVars(variantContext,isCodingVars);
//			if (!(cosmicAllSNVs == null)) {
//				mgmtCosmicAllSNVs.save(cosmicAllSNVs);
//			}			
//		}
//	}
	
	
	public void creatNonCodingVars(String inFile) {
		TxtReadandWrite txtNonCodingVars= new TxtReadandWrite(inFile);
//		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inFile));
		MgmtNonCodingVars mgmtNonCodingVars = MgmtNonCodingVars.getInstance();
		List<NonCodingVars> lsNonCodingVars= new ArrayList<>();
		int rowCount = txtNonCodingVars.ExcelRows();
		int mod = rowCount % 10000;
		int mulriple = (int)Math.floor(rowCount/10000);
		int circulation = 0;
		int number = 0;
		for (String content : txtNonCodingVars.readlines()) {
			if (!content.startsWith("#")) {
				NonCodingVars nonCodingVars = NonCodingVars.getInstanceFromNonCodingVars(content);
				if (!(nonCodingVars == null)) {
					lsNonCodingVars.add(nonCodingVars);
				}
				if (lsNonCodingVars.size() == 10000) {
					mgmtNonCodingVars.save(lsNonCodingVars);
					System.out.println("finished" + circulation);
					lsNonCodingVars.clear();
					circulation ++;
				}else if ((circulation == mulriple) && (lsNonCodingVars.size()==(mod-number))) {
					mgmtNonCodingVars.save(lsNonCodingVars);
					System.out.println("finished the end !");
					lsNonCodingVars.clear();
				}
			}else {
				number ++;
			}
		}
		txtNonCodingVars.close();
	}
	
	public void creatCosmicAllSNVs(String inFile, boolean isCodingVars) {
		TxtReadandWrite txtVars= new TxtReadandWrite(inFile);
		MgmtCosmicAllSNVs mgmtCosmicAllSNVs = MgmtCosmicAllSNVs.getInstance();
		List<CosmicAllSNVs> lsCosmicAllSNVs= new ArrayList<>();
		int rowCount = txtVars.ExcelRows();
		int mod = rowCount % 10000;
		int mulriple = (int)Math.floor(rowCount/10000);
		int circulation = 0;
		int number = 0;
		for (String content : txtVars.readlines()) {
			if (!content.startsWith("#")) {
				CosmicAllSNVs cosmicAllSNVs = CosmicAllSNVs.getInstanceFromNonCodingVars(content,isCodingVars);
				if (!(cosmicAllSNVs == null)) {
					lsCosmicAllSNVs.add(cosmicAllSNVs);				
				}	
				if (lsCosmicAllSNVs.size() == 10000) {
					mgmtCosmicAllSNVs.save(lsCosmicAllSNVs);
					System.out.println("finished time is  " + (new Date(System.currentTimeMillis())) + "\t"+ circulation);
					lsCosmicAllSNVs.clear();
					circulation ++;
				} else if ((circulation == mulriple) && (lsCosmicAllSNVs.size()==(mod-number))) {
					mgmtCosmicAllSNVs.save(lsCosmicAllSNVs);
					System.out.println("finished the end time is!" + (new Date(System.currentTimeMillis())));
					lsCosmicAllSNVs.clear();
				}
			}else {
				number ++;
			}
		}
	}

	public void creatCosmicAbb(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtCosmicAbb mgmtCosmicAbb = MgmtCosmicAbb.getInstance();
		List<CosmicAbb>  lsCosmicAbb = new ArrayList<>();
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				CosmicAbb cosmicAbb = CosmicAbb.getInstanceFromCosmicAbb(content);
				if (cosmicAbb != null) {
					lsCosmicAbb.add(cosmicAbb);
				}
			}
		}
		mgmtCosmicAbb.save(lsCosmicAbb);
		txtCompleteExport.close();
	}	
}
