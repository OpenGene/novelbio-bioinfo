package com.novelbio.bioinfo.emboss.motif;

import com.novelbio.bioinfo.emboss.motif.MotifEmboss;
import com.novelbio.bioinfo.emboss.motif.MotifEmboss.MotifEmbossScanAlgorithm;
import com.novelbio.bioinfo.fasta.SeqHash;

public class TestMotifEmboss {
	public static void main(String[] args) {
		String parentPath = "/home/novelbio/NBCsource/test/motif/";
		String fileName = parentPath + "sequence.fa";
		String motifMetrix = parentPath + "motifMetrix.fa";
		SeqHash seqHashMotif = new SeqHash(motifMetrix);

		MotifEmboss motifEmboss = new MotifEmboss();
		motifEmboss.setColAlignedMotifFasta(motifMetrix);
		motifEmboss.setSeqFilePath(fileName);
		motifEmboss.setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm.Frequency);
		motifEmboss.generateMatrix();
		
		motifEmboss.setOutFile(parentPath + "result.txt");
		motifEmboss.scanMotif();
	}
}
