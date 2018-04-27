package com.novelbio.analysis.tools;

import htsjdk.samtools.reference.FastaSequenceFile;

import java.util.List;

import com.novelbio.analysis.seq.denovo.N50AndSeqLen;
import com.novelbio.analysis.seq.denovo.N50statistics;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.modgeneid.GeneType;

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
		

//		String chrFile = "/home/novelbio/bianlianle/bin/Trinity_mod_1cdhit_0.95.2cap3_0.95_3cdhit_0.95.4cap3_0.95_5cdhit_0.95_6cap3_0.95.fa";
//		String geneNameFile = "/home/novelbio/bianlianle/bin/Ta_blast-2-200.list.txt";
//		String outputFaFile = "/home/novelbio/bianlianle/bin/Ta_blast-2-200.list.txt.fa";
		
//		String chrFile = args[0];
//		String geneNameFile = args[1];
//		String outputFaFile =args[2];
		
//		GffChrAbs gffChrAbs = new GffChrAbs();
//		gffChrAbs.setSeqHash(new SeqHash(chrFile));
//		SeqHash seqHash = gffChrAbs.getSeqHash();
//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(geneNameFile);
//		TxtReadandWrite txtOutput = new TxtReadandWrite(outputFaFile,true);
//		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
//		for (String gene:txtReadandWrite.readlines()) {
//			SeqFasta seq=seqHash.getSeq(gene);
//			
//			txtOutput.writefileln(seq.toStringNRfasta());
//		}
//		txtReadandWrite.close();
//		txtOutput.close();
		
		
		
		/**
		 * 统计序列的N50
		 */
		
//		String chrFile = "/run/media/novelbio/A/bianlianle/project/software_test/transdecoder_withstep2.cds.fa";
//		N50statistics n50Statistics = new N50statistics(chrFile);
//		n50Statistics.doStatistics();
//		n50Statistics.getLsNinfo();
//		String faStatResult = chrFile + ".stat.xls";
//		TxtReadandWrite txtWrite = new TxtReadandWrite(faStatResult, true);
//		int allContigsNum = n50Statistics.getAllContigsNum();
//		long allContigsLen = n50Statistics.getAllContigsLen();
//		int minContigsLen = n50Statistics.getRealMinConLen();
//		int maxContigsLen = n50Statistics.getRealMaxConLen();
//		int averageLen = n50Statistics.getLenAvg();
//		int N50Len = n50Statistics.getN50Len();
//		int medianLen = n50Statistics.getMedianLen();
//		txtWrite.writefileln("Number of contigs\t" + allContigsNum + "\nNumber of characters(bp)\t" + allContigsLen + "\nAverage Length(bp)\t" + averageLen + "\nMinimum Contigs Length\t" + minContigsLen + "\nMaximum Contigs Length\t" + maxContigsLen + "\nN50 Length\t" + N50Len + "\nMedian Length\t" + medianLen);
//		txtWrite.close();
		
		
	
//		String gffFile = "/run/media/novelbio/A/bianlianle/project/software_test/hisat2/hg19_p13_chr20.gtf";
//		String proteinFile = "/run/media/novelbio/A/bianlianle/project/software_test/hisat2/chr20_part.fa";
//		GffChrAbs gffChrAbs = new GffChrAbs();
//		gffChrAbs.setSeqHash(new SeqHash(chrFile));
		
		
		
//		gffChrAbs.setGffHash(new GffHashGene(gffFile));
//		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
//		gffChrSeq.setGeneStructure(GeneStructure.CDS);
//		gffChrSeq.setGetAllIso(false);
//		gffChrSeq.setGeneType(GeneType.mRNA);
//		gffChrSeq.setGetSeqGenomWide();
//		gffChrSeq.setIsSaveToFile(true);
//		gffChrSeq.setGetAAseq(true);
//		List<SeqFasta> seqString = gffChrSeq.getSeq("chr20", 23441943, 2344210, true);
//		System.out.println(seqString.toString());
		
//		gffChrSeq.writeIsoFasta(proteinFile);
		
		
		}
}
