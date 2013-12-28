package com.novelbio.generalConf;


public enum TitleFormatNBC {
	/** 公司名缩写 */
	CompanyNameAbbr("NovelBio"),
	GeneID("GeneID"), MirName("MirName"),
	QueryID("QueryID"), AccID("AccID"), Symbol("Symbol"), Pvalue("P-Value"), Log2Pnegative("(-log2P)"),
	GeneName("GeneName"), GeneType("GeneType"),
	SubjectID("SubjectID"),
	
	FDR("FDR"), FoldChange("FoldChange"), Log2FC("Log2FC"),
	Log10FC("Log10FC"), Evalue("E-Value"),
	
	Enrichment("Enrichment"),
	
	ChrID("ChrID"), LocStart("LocStart"), LocEnd("LocEnd"),
	Location("Location"), Description("Description"), Strand("Strand"),
	
	Style("Style"),
	
	miRNAName("miRNAName"), miRNApreName("miRNApreName"),
	mirSequence("mirSequence"), mirPreSequence("mirPreSequence"),
	mirMappingType("mirMappingType"),
	
	RfamID("RfamID"), RfamType("RfamType"), RfamAnnotaion("RfamAnnotaion"), RfamDescription("RfamDescription"), RfamClass("RfamClass"),
	NCRNAID("NCRNAID"),
	
	GeneStructure("GeneStructure"), RNAType("RNA_Type"),
	
	RepeatName("RepeatName"), RepeatFamily("RepeatFamily"),
	
	Energy("Energy"), Score("Score"), 
	GOID("GOID"), GOTerm("GOTerm"),
	PathwayID("PathwayID"), PathwayTerm("PathwayTerm"),
	
	/** mapping的临时文件夹 */
	TmpMapping("TmpMapping"),
	/** 样本文件夹 */
	Samples("Samples");
	
	String item;
	TitleFormatNBC(String item) {
		this.item = item;
	}
	
	@Override
	public String toString() {
		return item;
	}
	
//	public HashMap<String, String>
	
}
