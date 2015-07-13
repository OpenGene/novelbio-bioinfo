package com.novelbio.analysis.seq.denovo;

import ch.ethz.ssh2.log.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TaxClassify {
	private static final Logger logger = Logger.getLogger(TaxClassify.class);
	
	public static void main(String[] args) {
//		String taxDivFile = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\division.dmp";
//		TxtReadandWrite txtRead = new TxtReadandWrite(taxDivFile);
//		for (String context:txtRead.readlines()) {			
//			TaxDivision taxDivision = TaxDivision.generateTaxDivision(context);
//			logger.info(taxDivision.getTaxId() + "\t" + taxDivision.getTaxDivCode() + "\t" + taxDivision.getTaxDivName());
//		}
		
		String giToTaxIdFile = "E:\\上海烈冰\\数据库\\Tax_Database\\taxdump\\gi_taxid_prot.zip";
		TxtReadandWrite txtRead = new TxtReadandWrite(giToTaxIdFile);
		int i = 0;
		for (String context:txtRead.readlines()) {			
			GiToTaxId giToTaxId = GiToTaxId.generateGiToTaxId(context);
			System.out.println(giToTaxId.getGi()+ "\t" + giToTaxId.getTaxId());
			
			if (i++>10) {
				break;
			}
//			logger.info(giToTaxId.getGi()+ "\t" + giToTaxId.getTaxId());
		}
		
	}
	
	
}
