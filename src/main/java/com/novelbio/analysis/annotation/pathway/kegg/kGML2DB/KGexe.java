package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;


public class KGexe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
//			KGML2DB.readKGML("/media/winE/OutMrd1.mrd/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winE/OutMrd1.mrd/ssc/ssc_ncbi-geneid.list");

//			GeneID geneID = new GeneID("ATCC25795GL000001", 1773);
//			System.out.println(geneID.getTaxID());
//			geneID.setBlastInfo(1e-5, 83332);
//			System.out.println(ArrayOperate.cmbString(geneID.getAnno(true),"\t"));
			
//			RepoBlastInfo repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
//			repoBlastInfo.deleteAll();
//			RepoBlastFileInfo repoBlastFileInfo = (RepoBlastFileInfo)SpringFactory.getFactory().getBean("repoBlastFileInfo");
//			repoBlastFileInfo.deleteAll();
			
//			RepoBlastInfo repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
//			
//			List<BlastInfo> lt = repoBlastInfo.findBySubTaxID(83332);
//			System.out.println(lt.size());
//			Set<Integer> setId = new HashSet<>();
//			for (BlastInfo blastInfo : lt) {
//				System.out.println(blastInfo.getSubjectTax());
//				setId.add(blastInfo.getSubjectTax());
//				if (blastInfo.getQueryID().equalsIgnoreCase("ATCC25795GL000001")) {
//					System.out.println();
//				}
//				if (blastInfo.getSubjectTax() == 83332) {
//					ManageBlastInfo.getInstance().removeBlastInfo(blastInfo.getId());
//				}
//			}
//			for (Integer integer : setId) {
//				System.out.println(integer);
//			}
			
			
			
			
			KGML2DB.readKGML("/home/novelbio/NBCsource/database/kegg/eco");
			KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/database/kegg/eco/eco_ncbi-geneid.list");
//			
//			KGML2DB.readKGML("/home/novelbio/NBCsource/database/kegg/mmu");
//			KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/database/kegg/mmu/mmu_ncbi-geneid.list");
//			
//			KGML2DB.readKGML("/home/novelbio/NBCsource/database/kegg/ath");
//			KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/database/kegg/ath/ath_ncbi-geneid.list");
//
//			KGML2DB.readKGML("/home/novelbio/NBCsource/database/kegg/osa");
//			KeggIDcvt.upDateGen2Keg("/home/novelbio/NBCsource/database/kegg/osa/osa_ncbi-geneid.list");
//
//			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
