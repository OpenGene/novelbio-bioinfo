package com.novelbio.analysis.seq.fastq;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestRemoveDepReads {
	public static void main(String[] args) {


		
		String vcfFile = "/home/novelbio/tmp/HCV_6WC.vcf";
		String faFile = "/home/novelbio/tmp/chrAll.fa";
		String svType = "BND";
		String result = "/home/novelbio/tmp/HCV_6WC_final.vcf";
		SeqFastaHash seqFastaHash =  new SeqFastaHash(faFile);
		String newInfo = "";
		int i = 0;	
		 String regEx = "(N)*\\]*([a-zA-Z]*[0-9]*):([0-9]*)\\[*(N)*";  
		 Pattern pattern = Pattern.compile(regEx);  
		 String type = null;
		 String refBase = "W";
		 String altInfo = null;
		 String altBase = "X";
		 String chr = null;
		 long altStart =  0;
		 String[] value = new String[2];
		 String keyTmp = "";
		Map<String, String> hashBase = new HashMap<>();	
		VCFFileReader vcfFileReader = new VCFFileReader(FileOperate.getFile(vcfFile),false);

		 
		 
		for (VariantContext variantContext : vcfFileReader) {			
			Map<String, Object> attr = variantContext.getAttributes();
			type = attr.get("SVTYPE").toString();
			refBase = variantContext.getReference().toString().replaceAll("\\*", "");
			altInfo = variantContext.getAlternateAlleles().get(0).toString();   
			if (type.equals(svType)) {
				int start = variantContext.getStart();			
				refBase = seqFastaHash.getSeq(variantContext.getContig(), start, start).toString();				
				Matcher matcher = pattern.matcher(altInfo);  
				 if(matcher.find()){ 
					 chr =  matcher.group(2);
					 altStart = Long.parseLong(matcher.group(3));
					 altBase= seqFastaHash.getSeq(chr, altStart , altStart).toString();
					 keyTmp = variantContext.getContig() + "@" +variantContext.getStart() + "@" + variantContext.getID();
					 value[0] = refBase;
					 altInfo = altInfo.replace("N",altBase);
					 value[1]  = altInfo;
				       hashBase.put(keyTmp, refBase + "@" + altInfo);
				     
				 }	    
			}			
		}
		
		TxtReadandWrite txtVCF = new TxtReadandWrite(vcfFile);
		TxtReadandWrite txtWriteResule = new TxtReadandWrite(result,true);
		String newline = "";
		int x = 0;
		for (String content : txtVCF.readlines()) {
			if (!content.startsWith("#")) {
				String[] vcfLineinfo = content.split("\t");
				if (vcfLineinfo[2].contains("_")) {
					String keyValue = vcfLineinfo[0] + "@" + vcfLineinfo[1] + "@" + vcfLineinfo[2];				
					if (hashBase.containsKey(keyValue)) {
						String[] valueTmp = hashBase.get(keyValue).split("@");
						vcfLineinfo[3] = valueTmp[0];
						vcfLineinfo[4] = valueTmp[1];
					}
				}
				for (int j = 0; j<vcfLineinfo.length; j++) {
					newline += vcfLineinfo[j] + "\t";
				}
				txtWriteResule.writefileln(newline);
				newline = "";
			}
		}
		txtVCF.close();
		txtWriteResule.close();
	}	
}
