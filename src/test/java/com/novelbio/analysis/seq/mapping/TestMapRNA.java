package com.novelbio.analysis.seq.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class TestMapRNA {
	String parentPath = "src/test/resources/test_file/reference/ara/";
	String referenceFileRaw = parentPath + "chrAll.fa";
	String referenceTmpPath = FileOperate.getParentPathNameWithSep(referenceFileRaw) + "tmpPath2/";
	String referenceFile =  referenceTmpPath + "ref2.fa";
	String outPath = parentPath + "rnamapresult/";
	String gtfFile = parentPath + "TAIR10_Gff3_simple.gtf.gz";
	
	GffChrAbs gffChrAbs;
	
	String fq1;
	String fq2;
	
	MapRNA mapRNA;
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();

	@Before
	public void before() {
		parentPath = FileOperate.addSep(getAbsolutePath(parentPath));
		referenceFile = getAbsolutePath(referenceFile);
		referenceTmpPath = FileOperate.addSep(getAbsolutePath(referenceTmpPath));
		outPath = FileOperate.addSep(getAbsolutePath(outPath));
		gtfFile = getAbsolutePath(gtfFile);
		
//		fq1 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_1.fq.gz";
//		fq2 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_2.fq.gz";
		
//		fq1 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_1_2.fq";
//		fq2 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_2_2.fq";
		
		fq1 = "src/test/resources/test_file/fastq/PE/L_correct.1.fq";
		fq2 = "src/test/resources/test_file/fastq/PE/R_correct.2.fq";
		
		fq1 = getAbsolutePath(fq1);
		fq2 = getAbsolutePath(fq2);
		
		lsLeftFq.add(new FastQ(fq1));
		lsRightFq.add(new FastQ(fq2));
		gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(new GffHashGene(gtfFile));
		gffChrAbs.setSeqHash(new SeqHash(referenceFile));
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
		FileOperate.DeleteFileFolder(referenceTmpPath);
		FileOperate.DeleteFileFolder(outPath);
	}
	
	@Test
	public void testMapSplice() {
		copyFile();
		
		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.mapsplice, gffChrAbs);
		
		mapRNA.setRefIndex(referenceFile);
		mapRNA.setGtf_Gene2Iso(gtfFile);
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(8);
		mapRNA.setOutPathPrefix(outPath + "mapsplice");
		mapRNA.mapReads();
		lsLeftFq.clear();
		lsRightFq.clear();
		
		deleteFolder();
	}
	
//	@Test
//	public void testTophat() {
//		copyFile();
//		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
//
//		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat, null);
//		((MapTophat)mapRNA).setBowtieVersion(SoftWare.bowtie);
//		mapRNA.setRefIndex(referenceFile);
////		mapRNA.setGtf_Gene2Iso(gtfFile);
//		mapRNA.setOutPathPrefix(outPath + "tophat");
//		mapRNA.setLeftFq(lsLeftFq);
//		mapRNA.setRightFq(lsRightFq);
//		mapRNA.setThreadNum(3);
//		mapRNA.mapReads();
//		
//		lsLeftFq.clear();
//		lsRightFq.clear();
//		deleteFolder();
//	}
//	
//	@Test
//	public void testTophat_without_GTF() {
//		copyFile();
//		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
//
//		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat, null);
//		((MapTophat)mapRNA).setBowtieVersion(SoftWare.bowtie);
//		mapRNA.setRefIndex(referenceFile);
//		mapRNA.setOutPathPrefix(outPath + "tophat");
//		mapRNA.setLeftFq(lsLeftFq);
//		mapRNA.setRightFq(lsRightFq);
//		mapRNA.setThreadNum(3);
//		mapRNA.mapReads();
//		
//		lsLeftFq.clear();
//		lsRightFq.clear();
//		deleteFolder();
//	}
//	
//	@Test
//	public void testTophat2() {
//		copyFile();
//		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
//
//		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat, gffChrAbs);
//	
//		mapRNA.setRefIndex(referenceFile);
//		mapRNA.setGtf_Gene2Iso(gtfFile);
//		mapRNA.setOutPathPrefix(outPath + "tophat2");
//		mapRNA.setLeftFq(lsLeftFq);
//		mapRNA.setRightFq(lsRightFq);
//		mapRNA.setThreadNum(3);
//		mapRNA.mapReads();
//		
//		lsLeftFq.clear();
//		lsRightFq.clear();
//		deleteFolder();
//	}
	
	
}
