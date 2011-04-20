package com.novelBio.annotation.pathway.kegg.kGML2DB;


public class KGexe {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/dre");
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/non-metabolic/organisms/bta");
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/bta");
		//	KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/KGML/kgml/metabolic/organisms/mmu");
				/**
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/mmu");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/rno");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic organisms hsa rno ssc bta mmu/ssc");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/non-metabolic ko/ko");
			
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/ec");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/ko");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic ec ko rn/rn");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/bta");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/hsa");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/mmu");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/rno");
			KGML2DB.readKGML("/media/winE/Bioinformatics/Kegg/xml/kgml/新建文件夹/metabolic,organisms hsa  rno   ssc   bta   mmu/ssc");
			**/
			//KeggIDcvt.upDateGen2Keg("/media/winE/Bioinformatics/Kegg/genes/bta/bta_ncbi-geneid.list");

			KeggIDcvt.upDateGen2Keg("/home/zong0jie/桌面/dre_ncbi-geneid.list");
		//	KeggIDcvt.upDateKegCompound("/media/winE/Bioinformatics/Kegg/compound/compound");
			KeggIDcvt.upDateKeg2Ko("/home/zong0jie/桌面/dre_ko.list");

			System.out.println("ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
