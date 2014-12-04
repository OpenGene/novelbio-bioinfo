package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoBlastFileInfo;
import com.novelbio.database.mongorepo.geneanno.RepoBlastInfo;
import com.novelbio.database.service.SpringFactory;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;
import com.novelbio.database.service.servkegg.ServKEntry;


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
			
			RepoBlastInfo repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
			repoBlastInfo.deleteAll();
			RepoBlastFileInfo repoBlastFileInfo = (RepoBlastFileInfo)SpringFactory.getFactory().getBean("repoBlastFileInfo");
			repoBlastFileInfo.deleteAll();
			
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
