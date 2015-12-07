package com.novelbio.analysis.tools;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.database.model.modgeneid.GeneType;

public class ExtractSeq {
	public static void main(String[] args) {
		String chrFile = "C:\\Users\\Novelbio\\Desktop\\Triticum_aestivum.IWGSC1.0_popseq.28.dna_sm.integration_v2\\Triticum_aestivum.IWGSC1.0_popseq.28.dna_sm.integration.fa";
		String gffFile = "C:\\Users\\Novelbio\\Desktop\\Triticum_aestivum.IWGSC1.0_popseq.28.dna_sm.integration_v2\\Triticum_aestivum.IWGSC1.0_popseq.28.integration.v3.gtf";
		String proteinFile = "C:\\Users\\Novelbio\\Desktop\\Triticum_aestivum.IWGSC1.0_popseq.28.dna_sm.integration_v2\\Sequence.v3.fa";
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setSeqHash(new SeqHash(chrFile));
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		gffChrSeq.setGeneStructure(GeneStructure.CDS);
		gffChrSeq.setGetAllIso(false);
		gffChrSeq.setGeneType(GeneType.mRNA);
		gffChrSeq.setGetSeqGenomWide();
		gffChrSeq.setIsSaveToFile(true);
		gffChrSeq.setGetAAseq(true);
		gffChrSeq.writeIsoFasta(proteinFile);
		}
}
