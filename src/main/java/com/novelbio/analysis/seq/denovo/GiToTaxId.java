package com.novelbio.analysis.seq.denovo;

public class GiToTaxId {
	private int gi;
	private int taxId;
	
	public int getGi() {
		return gi;
	}
	public int getTaxId() {
		return taxId;
	}
	
	public static GiToTaxId generateGiToTaxId (String context) {
		String[] giToTaxIdLine = context.split("\t");
		GiToTaxId giToTaxId = new GiToTaxId();
		giToTaxId.gi = Integer.parseInt(giToTaxIdLine[0]);
		giToTaxId.taxId = Integer.parseInt(giToTaxIdLine[1]);
		return giToTaxId;
	}
}
