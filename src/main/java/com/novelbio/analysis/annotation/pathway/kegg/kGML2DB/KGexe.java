package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;


public class KGexe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {			
//			KGML2DB.readKGML("/media/winE/OutMrd1.mrd/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winE/OutMrd1.mrd/ssc/ssc_ncbi-geneid.list");
			
			KGML2DB.readKGML("/media/winE/OutMrd1.mrd/mmu");
			KeggIDcvt.upDateGen2Keg("/media/winE/OutMrd1.mrd/mmu/mmu_ncbi-geneid.list");
			
//			KGML2DB.readKGML("/media/winE/NBCplatform/database/kegg/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winE/NBCplatform/database/kegg/ssc_ncbi-geneid.list");
			
//			KGML2DB.readKGML("/media/winD/NBC/KGMLNew/bta");
//			KeggIDcvt.upDateGen2Keg("/media/winD/NBC/KGMLNew/bta/bta_ncbi-geneid.list");
//
//			KGML2DB.readKGML("/media/winD/NBC/KGMLNew/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winD/NBC/KGMLNew/bta/ssc_ncbi-geneid.list");

			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
