package com.novelbio.database.updatedb.cosmic;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.cosmic.CodingMuts;
import com.novelbio.database.domain.omim.GeneMIM;
import com.novelbio.database.model.modcosmic.MgmCodingMuts;
import com.novelbio.database.model.modcosmic.MgmtCancerGene;
import com.novelbio.database.model.modomim.MgmtGeneMIMInfo;

public class UpdataCOSMIC {
	static String cancerGenePath = "/home/novelbio/bianlianle/tmp/cancer_gene_census.csv.test2.txt";	
	static String codingMutsPath = "/home/novelbio/bianlianle/tmp/CosmicCodingMuts.vcf.20.txt";	
	public static void main(String[] args) {
		UpdataCOSMIC updataCOSMIC = new UpdataCOSMIC();
//		updataCOSMIC.creatCancerGene(cancerGenePath);
		updataCOSMIC.creatCodingMuts(codingMutsPath);
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
		MgmCodingMuts mgmCodingMuts = MgmCodingMuts.getInstance();
//		MgmtCancerGene mgmtCancerGene = MgmtCancerGene.getInstance();
		for (String content : txtCancerGene.readlines()) {
			
			if (!content.startsWith("#")) {
				CodingMuts codingMuts = CodingMuts.getInstanceFromCodingMuts(content);			
				if (!(codingMuts == null)) {
					mgmCodingMuts.save(codingMuts);
//					System.out.println(codingMuts.getAAChange());
				
//				mgmtCancerGene.save(cancerGene);
				}
			}
	
		}
		txtCancerGene.close();
	}
	
	
}
