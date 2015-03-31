package com.novelbio.analysis.seq.mapping;

import htsjdk.samtools.SAMFileHeader.SortOrder;

import org.junit.Test;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.MapBowtie;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

import junit.framework.TestCase;

public class TestMapDNA extends TestCase {
	String parentPath = "/hdfs:/nbCloud/testJava/NBCplatform/testDNAmap/";
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
//		CmdOperate.setTmpPath("/home/novelbio/tmp");
//		mapDNA = new MapBowtie();
//		String leftFqName = parentPath + "HumanDNA_2A_1_Small.fastq.gz";
//		String rightFqName = parentPath + "HumanDNA_2A_2_Small.fastq.gz";
//		Species species = new Species(9913);
//		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bowtie2));
//		FastQ leftFq = new FastQ(leftFqName);
//		FastQ rightFq = new FastQ(rightFqName);
//		mapDNA.setFqFile(leftFq, rightFq);
//		mapDNA.setSortNeed(true);
//		mapDNA.setOutFileName(parentPath + "resultBowtie");
//		SamFile samFile = mapDNA.mapReads();
//		assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
//		assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
//	}
	
//	@Test
//	public void testBwa() {
//		MapBwaAln mapDNA = new MapBwaAln();
////		mapDNA.setExePath("/home/novelbio/software/bwa/bwa/");
//		String leftFqName = parentPath + "HumanDNA_2A_1_Small.fastq.gz";
//		String rightFqName = parentPath + "HumanDNA_2A_2_Small.fastq.gz";
//		Species species = new Species(9913);
//		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bwa_aln));
//		FastQ leftFq = new FastQ(leftFqName);
//		FastQ rightFq = new FastQ(rightFqName);
//		mapDNA.setFqFile(leftFq, rightFq);
//		mapDNA.setSortNeed(false);
//		mapDNA.setOutFileName(parentPath + "/bwaResult/resultBWA3");
//		SamFile samFile = mapDNA.mapReads();
//		assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
//		assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
//	}
	
	@Test
	public void testBwaMem() {
		CmdOperate.setTmpPath("/home/novelbio/tmp");
		MapBwaMem mapDNA = new MapBwaMem();
		String leftFqName = parentPath + "HumanDNA_2A_1_Small.fastq.gz";
		String rightFqName = parentPath + "HumanDNA_2A_2_Small.fastq.gz";
		Species species = new Species(9913);
		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bwa_aln));
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(false);
		mapDNA.setOutFileName(parentPath + "/bwaResult/resultBWA5");
		SamFile samFile = mapDNA.mapReads();
		assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
}
