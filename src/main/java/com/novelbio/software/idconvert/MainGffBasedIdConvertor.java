package com.novelbio.software.idconvert;

public class MainGffBasedIdConvertor {
	
	public static void main(String[] args) {
		GffBasedIdConvertor gffBasedIdConvertor = new GffBasedIdConvertor();
		gffBasedIdConvertor.setGffRef("/home/novelbio/mywork/nongkeyuan/rice_anno/ref_IRGSP-1.0_top_level.gff3", "Dbxref=GeneID:");
		gffBasedIdConvertor.setGffAlt("/home/novelbio/mywork/nongkeyuan/rice_anno/tigr-msu-all.gff3");
		gffBasedIdConvertor.setIdConvertResult("/home/novelbio/mywork/nongkeyuan/rice_anno/ncbi2tigr.4.txt");
		gffBasedIdConvertor.convert();
	}
	
}
