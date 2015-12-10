package com.novelbio.database.updatedb.cosmic;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.cosmic.CompleteExport;
import com.novelbio.database.domain.cosmic.CosmicCNV;
import com.novelbio.database.domain.cosmic.NonCodingVars;
import com.novelbio.database.service.servcosmic.MgmtCancerGene;
import com.novelbio.database.service.servcosmic.MgmtCodingMuts;
import com.novelbio.database.service.servcosmic.MgmtCompleteExport;
import com.novelbio.database.service.servcosmic.MgmtNCV;
import com.novelbio.database.service.servcosmic.MgmtNonCodingVars;

public class UpdataCOSMIC {
	static String cancerGenePath = "/home/novelbio/bianlianle/tmp/cancer_gene_census.csv.test2.txt";	
	static String codingMutsPath = "/home/novelbio/bianlianle/tmp/CosmicCodingMuts.vcf.20.txt";	
	static String completeExportPath = "/home/novelbio/bianlianle/tmp/CosmicCompleteExport.part.20.tsv.xls";
	static String nCVPath = "/home/novelbio/bianlianle/tmp/CosmicNCV.tsv.10.txt";
	static String nonCodingVarsPath = "/home/novelbio/bianlianle/tmp/CosmicNonCodingVariants.20.vcf";
//	CosmicNCV.tsv.10.txt
	
	public static void main(String[] args) {
		UpdataCOSMIC updataCOSMIC = new UpdataCOSMIC();
//		updataCOSMIC.creatCancerGene(cancerGenePath);
//		updataCOSMIC.creatCodingMuts(codingMutsPath);
		updataCOSMIC.creatCompleteExport(completeExportPath);
		
//		updataCOSMIC.creatNCV(nCVPath);
//		updataCOSMIC.creatNonCodingVars(nonCodingVarsPath);
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
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				CompleteExport completeExport = CompleteExport.getInstanceFromCodingMuts(content);
				if (!(completeExport == null)) {
					mgmtCompleteExport.save(completeExport);
				}
			}
		}
		txtCompleteExport.close();
	}
	
	public void creatNCV(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtNCV mgmtNCV = MgmtNCV.getInstance();
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				CosmicCNV cosmicCNV = CosmicCNV.getInstanceFromNCV(content);
				if (!(cosmicCNV == null)) {
					mgmtNCV.save(cosmicCNV);
				}
			}
		}
		txtCompleteExport.close();
	}
	public void creatNonCodingVars(String inFile) {
		TxtReadandWrite txtCompleteExport = new TxtReadandWrite(inFile);
		MgmtNonCodingVars mgmtNonCodingVars = MgmtNonCodingVars.getInstance();
		for (String content : txtCompleteExport.readlines()) {
			if (!content.startsWith("#")) {
				NonCodingVars nonCodingVars = NonCodingVars.getInstanceFromNonCodingVars(content);
				if (!(nonCodingVars == null)) {
					mgmtNonCodingVars.save(nonCodingVars);
				}
			}
		}
		txtCompleteExport.close();
	}
}
