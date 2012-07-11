package com.novelbio.test.junit.seq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.generalConf.NovelBioConst;

public class GffChrAnnoTest {
	public static void main(String[] args) {
		test();
	}
	public static void test() {
		
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE);
		gffChrAnno.setFilterTssTes(new int[]{-1500,0}, null);
		ArrayList<String[]> ls = gffChrAnno.getGenInfoFilterPeakSingle("chr8",6571400,6572799);
		System.out.println(ls.get(0)[0]);
	}
}
