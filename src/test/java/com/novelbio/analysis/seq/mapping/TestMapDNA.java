package com.novelbio.analysis.seq.mapping;

import htsjdk.samtools.SAMFileHeader.SortOrder;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class TestMapDNA {
	String parentPath = "src/test/resources/test_file/";
	String referenceFileRaw = parentPath + "reference/testTrinity.fa";
	String referenceTmpPath = FileOperate.getParentPathNameWithSep(referenceFileRaw) + "tmpPath2/";
	String referenceFile =  referenceTmpPath + "ref2.fasta";

	String outPath = parentPath + "dnamapresult/";
	MapDNA mapDNA;
	
	@Before
	public void before() {
		parentPath = FileOperate.addSep(getAbsolutePath(parentPath));
		referenceFile = getAbsolutePath(referenceFile);
		referenceTmpPath = FileOperate.addSep(getAbsolutePath(referenceTmpPath));
		outPath = FileOperate.addSep(getAbsolutePath(outPath));
		FileOperate.createFolders(outPath);
	}
	
	private String getAbsolutePath(String path) {
		File file = new File(path);
		return file.getAbsolutePath();
	}
	
	private void copyFile() {
		FileOperate.createFolders(referenceTmpPath);
		FileOperate.copyFile(referenceFileRaw, referenceFile, true);
	}
	
	private void deleteFolder() {
		FileOperate.deleteFileFolder(referenceTmpPath);
		FileOperate.deleteFileFolder(outPath);
	}
	
	@Test
	public void testBowtie() {
		copyFile();
		
		mapDNA = new MapBowtie2();
		mapDNA.getIndexMaker().setLock(false);
		String leftFqName = parentPath + "fastq/PE/L_correct.1.fq.gz";
		String rightFqName = parentPath + "fastq/PE/R_correct.2.fq.gz";
		mapDNA.setChrIndex(referenceFile);
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(true);
		mapDNA.setOutFileName(outPath + "resultBowtie2");
		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
		
		deleteFolder();
	}
	
	@Test
	public void testBwa() {
		copyFile();
		
		MapBwaAln mapDNA = new MapBwaAln();
		String leftFqName = parentPath + "fastq/PE/L_correct.1.fq.gz";
		String rightFqName = parentPath + "fastq/PE/R_correct.2.fq.gz";
		mapDNA.setChrIndex(referenceFile);
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(false);
		mapDNA.setOutFileName(outPath + "resultBWAaln");
		mapDNA.getIndexMaker().setLock(false);
		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(false, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
		
		deleteFolder();
	}
	
	@Test
	public void testBwaMem() {
		copyFile();
		
		MapBwaMem mapDNA = new MapBwaMem();
		String leftFqName = parentPath + "fastq/PE/L_correct.1.fq.gz";
		String rightFqName = parentPath + "fastq/PE/R_correct.2.fq.gz";
		mapDNA.setChrIndex(referenceFile);
		FastQ leftFq = new FastQ(leftFqName);
		FastQ rightFq = new FastQ(rightFqName);
		mapDNA.setFqFile(leftFq, rightFq);
		mapDNA.setSortNeed(true);
		mapDNA.setOutFileName(outPath + "resultBWA5");
		mapDNA.getIndexMaker().setLock(false);

		SamFile samFile = mapDNA.mapReads();
		Assert.assertEquals(true, SamFile.isSamBamFile(samFile.getFileName()) == FormatSeq.BAM);
		Assert.assertEquals(true, samFile.getHeader().getSortOrder() == SortOrder.coordinate);
		
		deleteFolder();
	}
	
}
