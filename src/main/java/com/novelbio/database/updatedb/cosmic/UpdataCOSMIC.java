package com.novelbio.database.updatedb.cosmic;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.cosmic.CancerGene;
import com.novelbio.database.domain.omim.GeneMIM;
import com.novelbio.database.model.modcosmic.MgmtCancerGene;
import com.novelbio.database.model.modomim.MgmtGeneMIMInfo;

public class UpdataCOSMIC {
	static String cancerGenePath = "/home/novelbio/bianlianle/tmp/cancer_gene_census.csv.test2.txt";	
	
	public static void main(String[] args) {
		UpdataCOSMIC updataCOSMIC = new UpdataCOSMIC();
		updataCOSMIC.creatCancerGene(cancerGenePath);
	}
	
	public void creatCancerGene(String inFile) {
		
		TxtReadandWrite txtCancerGene = new TxtReadandWrite(cancerGenePath);
		MgmtCancerGene mgmtCancerGene = MgmtCancerGene.getInstance();
		for (String content : txtCancerGene.readlines()) {
			CancerGene cancerGene = CancerGene.getInstanceFromCancerGene(content);			
			if (!(cancerGene == null)) {
			mgmtCancerGene.save(cancerGene);
			}	
		}
		txtCancerGene.close();
	}
}
