package com.novelbio.analysis.tools;

import java.util.Map;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import com.novelbio.base.fileOperate.FileOperate;

public class LumpyResultChange {

	String inputVCFFile;
	String faSeqFile;
	String outputVCFFile;
	String refBase;
	
	public void setInputVCFFile(String inputVCFFile) {
		this.inputVCFFile = inputVCFFile;
	}
	public void setFaSeqFile(String faSeqFile) {
		this.faSeqFile = faSeqFile;
	}
	
	public void changeLumpyVCF (String inputVCFFile) {
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(inputVCFFile),false);
		for (VariantContext variantContext : vcfFileReader) {			
			Map<String, Object> attr = variantContext.getAttributes();
			
//			type = attr.get("SVTYPE").toString();
		}
	}
	
	public boolean isBNDType (String string) {
		return false;
	}
	
	public String getBase(String info) {
		
		String base = "";
		return base;
	}
}
