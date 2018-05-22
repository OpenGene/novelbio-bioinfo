package com.novelbio.analysis.seq.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class TestMapRNA {
	String parentPath = "src/test/resources/test_file/reference/arabidopsis_sub/";
	String referenceFileRaw = parentPath + "chrAll.fa";
	String referenceTmpPath = FileOperate.getParentPathNameWithSep(referenceFileRaw) + "tmpPath2/";
	String gtfTmpPath = FileOperate.getParentPathNameWithSep(referenceFileRaw) + "tmpPathGtf/";

	String referenceFile =  referenceTmpPath + "ref2.fasta";
	String outPath = parentPath + "rnamapresult/";
	String gtfFileRaw = parentPath + "TAIR10_Gff3_simple_with_contig.gtf.gz";
	String gtfFile = gtfTmpPath + "TAIR10_Gff3_simple_with_contig.gtf";
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
	
		gtfFileRaw = getAbsolutePath(gtfFileRaw);
		gtfFile = getAbsolutePath(gtfFile);
		
		FileOperate.createFolders(gtfTmpPath);

		TxtReadandWrite txtRead = new TxtReadandWrite(gtfFileRaw);
		TxtReadandWrite txtWrite = new TxtReadandWrite(gtfFile, true);
		for (String content : txtRead.readlines()) {
			txtWrite.writefileln(content);
		}
		txtRead.close();
		txtWrite.close();
		
		fq1 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_1.fq.gz";
		fq2 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_2.fq.gz";
		
		fq1 = getAbsolutePath(fq1);
		fq2 = getAbsolutePath(fq2);
		
		lsLeftFq.add(new FastQ(fq1));
		lsRightFq.add(new FastQ(fq2));
		gffChrAbs = new GffChrAbs();
		gffChrAbs.setGffHash(new GffHashGene(gtfFileRaw));
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
		FileOperate.deleteFileFolder(referenceTmpPath);
		FileOperate.deleteFileFolder(gtfTmpPath);
		FileOperate.deleteFileFolder(outPath);
	}
	
//	@Test
	public void testMapSplice() {
		copyFile();

		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.mapsplice);
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.setRefIndex(referenceFile);
		mapRNA.setGtfFiles(gtfFile);
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(8);
		mapRNA.setOutPathPrefix(outPath + "mapsplice");
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		mapRNA.mapReads();
		lsLeftFq.clear();
		lsRightFq.clear();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		deleteFolder();
	}
	
	@Test
	public void testTophat() {
		copyFile();
		
		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat);
		((MapTophat)mapRNA).setBowtieVersion(SoftWare.bowtie);

		mapRNA.setRefIndex(referenceFile);
		mapRNA.setGtfFiles(gtfFile.replace(".gz", ""));
		((MapTophat)mapRNA).setMoveGtfToChr(false);
		
		mapRNA.setOutPathPrefix(outPath + "tophat");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.mapReads();
		
		lsLeftFq.clear();
		lsRightFq.clear();
		
		String gtfFileAssert = FileOperate.getPathName(referenceFile) + FileOperate.getFileName(gtfFile.replace(".gz", ""));
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(gtfFileAssert));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		deleteFolder();
		FileOperate.deleteFileFolder(gtfFile.replace(".gz", ""));
	}
	
	@Test
	public void testTophatWithGffChrAbs() {
		copyFile();
		
		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat);		
		((MapTophat)mapRNA).setBowtieVersion(SoftWare.bowtie);
		mapRNA.setGtfFiles(gtfFile.replace(".gz", ""));
		((MapTophat)mapRNA).setMoveGtfToChr(true);
		
		mapRNA.setRefIndex(referenceFile);
		mapRNA.setOutPathPrefix(outPath + "tophat");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.mapReads();
		
		lsLeftFq.clear();
		lsRightFq.clear();
		
		String gtfFileAssert = FileOperate.getPathName(referenceFile) + FileOperate.getFileName(gtfFile);
		Assert.assertTrue(FileOperate.isFileExistAndBigThanSize(gtfFileAssert, 0));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		deleteFolder();
		FileOperate.deleteFileFolder(gtfFile.replace(".gz", ""));
	}
	
	@Test
	public void testTophat_without_GTF() {
		copyFile();
		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat);
		((MapTophat)mapRNA).setBowtieVersion(SoftWare.bowtie);
		mapRNA.setRefIndex(referenceFile);
		mapRNA.setOutPathPrefix(outPath + "tophat");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.mapReads();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		lsLeftFq.clear();
		lsRightFq.clear();
		deleteFolder();
	}
	
	@Test
	public void testTophat2() {
		copyFile();

		mapRNA = MapRNAfactory.generateMapRNA(SoftWare.tophat);

		mapRNA.setRefIndex(referenceFile);
		mapRNA.setGtfFiles(gtfFile.replace(".gz", ""));
		((MapTophat)mapRNA).setMoveGtfToChr(true);
		mapRNA.setOutPathPrefix(outPath + "tophat2");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.mapReads();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		lsLeftFq.clear();
		lsRightFq.clear();
		deleteFolder();
	}
	
	@Test
	public void testMapHisat() {
		copyFile();

		MapHisat mapRNA = (MapHisat)MapRNAfactory.generateMapRNA(SoftWare.hisat2);
		mapRNA.setExePathHist("/home/novelbio/下载/hisat2-2.0.1-beta/");
		mapRNA.setRefIndex(referenceFile);
		mapRNA.setGtfFiles(gtfFile.replace(".gz", ""));
		mapRNA.setOutPathPrefix(outPath + SoftWare.hisat2);
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.mapReads();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		//TODO 看mapping率和uniquemapping率之类的对不对
		lsLeftFq.clear();
		lsRightFq.clear();
		deleteFolder();
	}
	
}
