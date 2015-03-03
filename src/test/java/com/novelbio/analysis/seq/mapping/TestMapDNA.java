package com.novelbio.analysis.seq.mapping;

import org.junit.Test;

import net.sf.samtools.SAMFileHeader.SortOrder;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

import junit.framework.TestCase;

public class TestMapDNA extends TestCase {
	MapDNAint mapDNA;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
//	@Test
//	public void testBowtie() {
//		mapDNA = new MapBowtie();
//		String leftFqName = "/media/nbfs/nbCloud/public/test/DNASeqMap/HumanDNA_2A_1_Small.fastq.gz";
//		String rightFqName = "/media/nbfs/nbCloud/public/test/DNASeqMap/HumanDNA_2A_2_Small.fastq.gz";
//		Species species = new Species(9606);
//		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bowtie2));
//		FastQ leftFq = new FastQ(leftFqName);
//		FastQ rightFq = new FastQ(rightFqName);
//		mapDNA.setFqFile(leftFq, rightFq);
//		mapDNA.setSortNeed(true);
//		mapDNA.setOutFileName("/media/nbfs/nbCloud/public/test/DNASeqMap/resultBowtie");
//		SamFile samFile = mapDNA.mapReads();
//		assertEquals(true, samFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
//		assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
//	}
	
	@Test
	public void testBwa() {
		MapBwaAln mapDNA = new MapBwaAln();
//		mapDNA.setExePath("/home/novelbio/software/bwa/bwa/");
		String leftFqName = "/hdfs:/nbCloud/public/test/DNASeqMap/HumanDNA_2A_1_Small.fastq.gz";
		String rightFqName = "/hdfs:/nbCloud/public/test/DNASeqMap/HumanDNA_2A_2_Small.fastq.gz";
		Species species = new Species(9606);
		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bwa_aln));
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(true);
		mapDNA.setOutFileName("/hdfs:/nbCloud/public/test/DNASeqMap/resultBWA");
		SamFile samFile = mapDNA.mapReads();
		assertEquals(true, samFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
}
