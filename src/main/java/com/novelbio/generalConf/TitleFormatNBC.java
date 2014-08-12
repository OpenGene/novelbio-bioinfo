package com.novelbio.generalConf;

import java.util.HashMap;
import java.util.Map;


public enum TitleFormatNBC {
	/** 公司名缩写 */
	CompanyNameAbbr("NovelBio"),
	GeneID("GeneID"), MirName("MirName"),
	QueryID("QueryID"), AccID("AccID"), Symbol("Symbol"), Pvalue("P-Value"), Log2Pnegative("(-log2P)"), Adjusted_PValue("Adjusted_PValue "),
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
	KEGGID("KeggID"), KEGGTerm("KeggTerm"),
	PathwayID("PathwayID"), PathwayTerm("PathwayTerm"),
	COGAbbr("COGFunction"), COGID("COGID"), COGTerm("COGTerm"),
	/** mapping的临时文件夹 */
	TmpMapping("TmpMapping"),
	/** 样本文件夹 */
	Samples("Samples");
	
	String item;
	static Map<String, TitleFormatNBC> mapPvalueFdr;
	TitleFormatNBC(String item) {
		this.item = item;
	}
	
	@Override
	public String toString() {
		return item;
	}
	
	public static Map<String, TitleFormatNBC> getPvalueFdr() {
		if (mapPvalueFdr == null) {
			mapPvalueFdr = new HashMap<>();
			mapPvalueFdr.put(Pvalue.name(), Pvalue);
			mapPvalueFdr.put(FDR.name(), FDR);
		}
		return mapPvalueFdr;
	}
	
}
