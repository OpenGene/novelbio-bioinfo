package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servkegg.ServKEntry;


public class KGexe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//			KGML2DB.readKGML("/media/winE/OutMrd1.mrd/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winE/OutMrd1.mrd/ssc/ssc_ncbi-geneid.list");
			
			GeneID genId = new GeneID("YP_006516293.1", 0);
			System.out.println(genId.getGeneUniID());
			
//			KGML2DB.readKGML("/home/novelbio/NBCsource/database/kegg/mtu");
//			KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/database/kegg/mtu/mtu_ncbi-geneid.list");
//			
////			KGML2DB.readKGML("/media/winE/NBCplatform/database/kegg/ssc");
////			KeggIDcvt.upDateGen2Keg("/media/winE/NBCplatform/database/kegg/ssc_ncbi-geneid.list");
//			
////			KGML2DB.readKGML("/media/winD/NBC/KGMLNew/bta");
////			KeggIDcvt.upDateGen2Keg("/media/winD/NBC/KGMLNew/bta/bta_ncbi-geneid.list");
////
////			KGML2DB.readKGML("/media/winD/NBC/KGMLNew/ssc");
////			KeggIDcvt.upDateGen2Keg("/media/winD/NBC/KGMLNew/bta/ssc_ncbi-geneid.list");
//
//			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
