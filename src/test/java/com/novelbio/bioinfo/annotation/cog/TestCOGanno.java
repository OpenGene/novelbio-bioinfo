package com.novelbio.bioinfo.annotation.cog;

import com.novelbio.bioinfo.annotation.cog.COGanno;
import com.novelbio.bioinfo.annotation.cog.EnumCogType;
import com.novelbio.generalconf.PathDetailNBC;
import com.novelbio.listoperate.HistList;

public class TestCOGanno {
	public static void main(String[] args) {
		String cogId2Anno = "/media/winE/NBCsource/otherResource/cog/COG0303/cogs.csv";
		String pro2cogFile = "/media/winE/NBCsource/otherResource/cog/COG0303/prot2COG.tab";
		String cogAbbr2FunFile = "/media/winE/NBCsource/otherResource/cog/COG0303/fun.txt";
		String seqFastaFile = "/media/winE/NBCsource/otherResource/cog/COG0303/test/tair10_protein_modify.fa";
		COGanno cogAnno = new COGanno(EnumCogType.COG);
		cogAnno.setCogId2AnnoFile(cogId2Anno);
		cogAnno.setSeqFastaFile(seqFastaFile);
		cogAnno.setPro2cogFile(pro2cogFile);
		cogAnno.setCogAbbr2FunFile(cogAbbr2FunFile);
		cogAnno.initial();
		COGanno.getModifiedSeq(PathDetailNBC.getCogFasta(EnumCogType.COG), "/media/winE/NBCsource/otherResource/cog/COG0303/test22", pro2cogFile);
//		HistList histList = HistList.creatHistList(name, cisList);
//		histList.setStartBin(number, name, start, end);
//		histList.addHistBin(number, name, thisNum);
				
	}
}
