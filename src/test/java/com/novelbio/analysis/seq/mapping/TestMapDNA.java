package com.novelbio.analysis.seq.mapping;

import htsjdk.samtools.SAMFileHeader.SortOrder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class TestMapDNA {
	String parentPath = "/hdfs:/nbCloud/testJava/NBCplatform/testDNAmap/";
	MapDNAint mapDNA;

	
//	@Test
	public void testBowtie() {
		CmdOperate.setTmpPath("/home/novelbio/tmp");
		mapDNA = new MapBowtie();
		String leftFqName = parentPath + "HumanDNA_2A_1_Small.fastq.gz";
		String rightFqName = parentPath + "HumanDNA_2A_2_Small.fastq.gz";
		Species species = new Species(9913);
		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bowtie2));
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(true);
		mapDNA.setOutFileName(parentPath + "resultBowtie");
		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
	
//	@Test
	public void testBwa() {
		MapBwaAln mapDNA = new MapBwaAln();
//		mapDNA.setExePath("/home/novelbio/software/bwa/bwa/");
		String leftFqName = parentPath + "HumanDNA_2A_1_Small.fastq.gz";
		String rightFqName = parentPath + "HumanDNA_2A_2_Small.fastq.gz";
		Species species = new Species(9913);
		mapDNA.setChrIndex(species.getIndexChr(SoftWare.bwa_aln));
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(false);
		mapDNA.setOutFileName(parentPath + "/bwaResult/resultBWA3");
		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
	
//	@Test
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
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
	
	@Test
	public void testBwaMemMaize() {
		CmdOperate.setTmpPath("/home/novelbio/tmp");
		MapBwaMem mapDNA = new MapBwaMem();
		String parentPath = "/media/nbfs/nbCloud/testJava/NBCplatform/testDNAmap/maize/";
		String leftFqName = parentPath + "testFastq1.fq";
		String rightFqName = parentPath + "testFastq2.fq";
		mapDNA.setChrIndex("/media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/4577/zmb73_ensembl/Chr_Index/chrAll.fa");
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(false);
		mapDNA.setOutFileName(parentPath + "/bwaResult/resultMaize");
		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
	}
	
//	@Test
//	public void test() throws InterruptedException {
//		List<String> lsCmd = new ArrayList<>();
//		lsCmd.add("bwa");
//		lsCmd.add("mem");
//		lsCmd.add("-P");
//		lsCmd.add("/media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/4577/zmb73_ensembl/Chr_Index/chrAll.fa");
//		lsCmd.add("/media/nbfs/nbCloud/testJava/NBCplatform/testDNAmap/HumanDNA_2A_2_Small.fastq.gz");
//		lsCmd.add("/media/nbfs/nbCloud/testJava/NBCplatform/testDNAmap/HumanDNA_2A_1_Small.fastq.gz");
//		lsCmd.add(">");
//		lsCmd.add("/home/novelbio/maize");
//		
//		CmdOperate cmdOperate = new CmdOperate(lsCmd);
//		Thread thread = new Thread(cmdOperate);
//		thread.start();
//		Thread.sleep(5000);
//		cmdOperate.threadStop();
//		thread.join();
//	}
	
}
