package com.novelbio.analysis.seq.denovo;

import htsjdk.variant.variantcontext.VariantContext;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.MAFRecord;

public class TaxDivision {
	private int taxId;
	private String taxDivCode;
	private String taxDivName;

	public int getTaxId() {
		return taxId;
	}
	public String getTaxDivCode() {
		return taxDivCode;
	}
	public String getTaxDivName() {
		return taxDivName;
	}
//	public static MAFRecord generateMafRecord(VariantContext variantContext, GffChrAbs gffChrAbs) {
	
	public static TaxDivision generateTaxDivision(String context) {
		String[] arrTaxDiv = context.split("	");
		TaxDivision taxDivision = new TaxDivision();
//		taxDivision.setTaxId(Integer.parseInt(arrTaxDiv[0]));
//		taxDivision.setTaxDivCode(arrTaxDiv[2]);
//		taxDivision.setTaxDivName(arrTaxDiv[4]);
		taxDivision.taxId = Integer.parseInt(arrTaxDiv[0]);
		taxDivision.taxDivCode = arrTaxDiv[2];
		taxDivision.taxDivName = arrTaxDiv[4];
		return taxDivision;
	}
	
}
