package com.novelbio.generalConf;

public enum TitleFormatNBC {
	QueryID("QueryID"), AccID("AccID"), Symbol("Symbol"), Pvalue("P-value"), 
	
	FDR("FDR"), FoldChange("FoldChange"), Log2FC("Log2FC"),
	Log10FC("Log10FC"), ChrID("ChrID"), LocStart("LocStart"), LocEnd("LocEnd"),
	Location("Location"), Description("Description"), 
	
	miRNAName("miRNAName"), miRNApreName("miRNApreName"),
	mirSequence("mirSequence"), mirPreSequence("mirPreSequence"),
	RfamID("RfamID");
	
	String item;
	TitleFormatNBC(String item) {
		this.item = item;
	}
	@Override
	public String toString() {
		return item;
	}
}
