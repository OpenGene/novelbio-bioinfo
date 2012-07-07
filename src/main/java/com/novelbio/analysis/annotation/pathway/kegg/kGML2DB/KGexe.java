package com.novelbio.analysis.annotation.pathway.kegg.kGML2DB;


public class KGexe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		
			
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/dre");
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/bta");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/ath");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/ath");

//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/mmu");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/rno");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/ssc");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic ko/ko");
//			
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/ec");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/ko");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/rn");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/bta");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/hsa");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/mmu");
			
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/bta");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/bta");
			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/organisms/bta/bta_ncbi-geneid.list");
			KeggIDcvt.upDateKeg2Ko("/media/winE/Bioinformatics/Kegg/genes/organisms/bta/bta_ko.list");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/bta");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/bta");
//			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/organisms/bta/bta_ncbi-geneid.list");
//			KeggIDcvt.upDateKeg2Ko("/media/winE/Bioinformatics/Kegg/genes/organisms/bta/bta_ko.list");
			
//			KeggIDcvt.upDateGen2Keg("/home/zong0jie/桌面/yeast/sce_ncbi-geneid.list");
//			KeggIDcvt.upDateKeg2Ko("/home/zong0jie/桌面/mmu_ko.list");
//			
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/mmu");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/mmu");
//			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/organisms/mmu/mmu_ncbi-geneid.list");
//			
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/ssc");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/ssc");
//			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/organisms/ssc/ssc_ncbi-geneid.list");
//			KeggIDcvt.upDateKeg2Ko("/media/winE/Bioinformatics/Kegg/genes/organisms/ssc/ssc_ko.list");
			
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/gmx");
//			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/xml/gmx_ncbi-geneid.list");
			
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/gga");
//			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/gga");
//			KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/organisms/gga/gga_ncbi-geneid.list");
//			KeggIDcvt.upDateKeg2Ko("/media/winE/Bioinformatics/Kegg/genes/organisms/gga/gga_ko.list");
			
			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
