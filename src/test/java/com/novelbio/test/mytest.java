package com.novelbio.test;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;


public class mytest {

	private static Logger logger = Logger.getLogger(mytest.class);
	
	public static void main(String[] args) {
//		MiRNACount miRNACount = new MiRNACount();
//		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_RNA_DATA, new Species(10090), "/media/winE/Bioinformatics/genome/sRNA/miRNA.dat");
		
		Species species = new Species(3702);
//		species.setVersion("rnor4");
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		HashSet<String> setGeneID = gffChrAbs.getGffHashGene().getSetIsoID();
		TxtReadandWrite txtOut = new TxtReadandWrite("/home/zong0jie/×ÀÃæ/atBG.txt", true);
		for (String geneID : setGeneID) {
			txtOut.writefileln(geneID);
		}
		txtOut.close();
	}
	
	private static void HG18() {
		SnpAnnotation snpAnnotation = new SnpAnnotation();
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile("/media/winE/Bioinformatics/genome/rice/tigr6.0/all.con", null);
		gffChrAbs.setGffFile(39947, NovelBioConst.GENOME_GFF_TYPE_TIGR, "/media/winE/Bioinformatics/genome/rice/tigr6.0/all.gff3");
		

		snpAnnotation.setGffChrAbs(gffChrAbs);
		snpAnnotation.addTxtSnpFile("/home/zong0jie/×ÀÃæ/geneID.txt", "/home/zong0jie/×ÀÃæ/geneID_Anno");
		snpAnnotation.setCol(1, 2, 3, 4);
		snpAnnotation.run();
//		
	}
}