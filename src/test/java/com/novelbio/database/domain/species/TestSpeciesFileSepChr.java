package com.novelbio.database.domain.species;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.fasta.format.ChrFileFormat;
import com.novelbio.analysis.seq.fasta.format.TestChrFileFormat;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.SpeciesFileSepChr;
import com.novelbio.database.model.geneanno.SpeciesFile;

public class TestSpeciesFileSepChr {
	String parentPath = "src/test/resources/test_file/reference/arabidopsis_sub/";
	String gffFile = parentPath + "TAIR10_Gff3_simple_with_contig.gtf.gz";
	String refSeq = parentPath + "chrAll.fa";
	
	@After
	public void deletePath() {
		FileOperate.deleteFileFolder(parentPath + "genome/");
	}
	
	@Test
	public void testSplitSeqWithSpeciesFile1() {
		int minLen = 1000;
		int maxNum = 3000;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setGffChrId = TestChrFileFormat.readGffFile(gffFile);
		
		SpeciesFile.setPathParent(parentPath + "genome/");
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(3702);
		speciesFile.setVersion("test");
		speciesFile.addGffDB2TypeFile("testdb", GffType.GTF, gffFile);
		speciesFile.setChromSeq(refSeq);
		
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setSpeciesFile(speciesFile);
		sepChr.setGenomePath(parentPath + "genome/");
		sepChr.setSetChrId(setGffChrId);
		sepChr.setChrSeq(refSeq);
		sepChr.setMaxSeqNum(maxNum);
		sepChr.setMinLen(minLen);
		
		sepChr.generateChrSepFiles();

		String resultFileOne = parentPath + "genome/chrSep/3702/test/chrAll.fa";
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne + ".fai"));
		Map<String, Long> mapChrId2LenNew = SamIndexRefsequence.getMapChrId2Len(resultFileOne + ".fai");
		
		Assert.assertEquals(14, mapChrId2LenNew.size());
		// 选中的染色体是否都在gff中或者比指定的长

	}
	
	@Test
	public void testSplitSeqWithSpeciesFile2() {
		int minLen = 1000;
		int maxNum = 5;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setGffChrId = TestChrFileFormat.readGffFile(gffFile);
		
		SpeciesFile.setPathParent(parentPath + "genome/");
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(3702);
		speciesFile.setVersion("test");
		speciesFile.addGffDB2TypeFile("testdb", GffType.GTF, gffFile);
		speciesFile.setChromSeq(refSeq);
		
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setSpeciesFile(speciesFile);		sepChr.setGenomePath(parentPath + "genome/");
		sepChr.setSetChrId(setGffChrId);
		sepChr.setChrSeq(refSeq);
		sepChr.setMaxSeqNum(maxNum);
		sepChr.setMinLen(minLen);
		
		sepChr.generateChrSepFiles();

		String resultFileOne = parentPath + "genome/chrSep/3702/test/chrAll.fa";
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne + ".fai"));
		Map<String, Long> mapChrId2LenNew = SamIndexRefsequence.getMapChrId2Len(resultFileOne + ".fai");
		

		
		Assert.assertEquals(5, mapChrId2LenNew.size());
		// 选中的染色体是否都在gff中或者比指定的长
		for (String chrId : mapChrId2LenNew.keySet()) {
			Assert.assertEquals(true, setGffChrId.contains(chrId) || mapChrId2Len.get(chrId) >= minLen);
		}

	}
	
	@Test
	public void testSplitSeqWithSpeciesFile3() {
		int minLen = 0;
		int maxNum = 0;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setChrId = new HashSet<>();
		setChrId.add("chr2"); setChrId.add("contig1"); setChrId.add("contig3");
		
		SpeciesFile.setPathParent(parentPath + "genome/");
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(3702);
		speciesFile.setVersion("test");
		speciesFile.addGffDB2TypeFile("testdb", GffType.GTF, gffFile);
		speciesFile.setChromSeq(refSeq);
		
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setSpeciesFile(speciesFile);
		sepChr.setGenomePath(parentPath + "genome/");
		sepChr.setSetChrId(setChrId);
		sepChr.setChrSeq(refSeq);
		sepChr.setMaxSeqNum(maxNum);
		sepChr.setMinLen(minLen);
		
		sepChr.generateChrSepFiles();
		
		String resultFileOne = parentPath + "genome/chrSep/3702/test/chrAll.fa";
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne + ".fai"));
		Map<String, Long> mapChrId2LenNew = SamIndexRefsequence.getMapChrId2Len(resultFileOne + ".fai");
		

		
		Assert.assertEquals(setChrId.size(), mapChrId2LenNew.size());
		for (String chrId : mapChrId2LenNew.keySet()) {
			Long len = mapChrId2LenNew.get(chrId);
			Assert.assertEquals(mapChrId2Len.get(chrId), len);
			Assert.assertEquals(true, setChrId.contains(chrId));
		}
	}
	
	@Test
	public void testSplitSeqWithChrSeq() {
		int minLen = 0;
		int maxNum = 0;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setChrId = new HashSet<>();
		setChrId.add("chr2"); setChrId.add("contig1"); setChrId.add("contig3");
		
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setGenomePath(parentPath + "genome/");
		sepChr.setSetChrId(setChrId);
		sepChr.setChrSeq(refSeq, parentPath + "testSep");
		sepChr.setMaxSeqNum(maxNum);
		sepChr.setMinLen(minLen);
		
		sepChr.generateChrSepFiles();
		
		String resultFileOne = parentPath + "testSep/chrAll.fa";
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(resultFileOne + ".fai"));
		Map<String, Long> mapChrId2LenNew = SamIndexRefsequence.getMapChrId2Len(resultFileOne + ".fai");
		

		
		Assert.assertEquals(setChrId.size(), mapChrId2LenNew.size());
		for (String chrId : mapChrId2LenNew.keySet()) {
			Long len = mapChrId2LenNew.get(chrId);
			Assert.assertEquals(mapChrId2Len.get(chrId), len);
			Assert.assertEquals(true, setChrId.contains(chrId));
		}
	}
	
	@Test
	public void testGetSepChrOne() {
		SpeciesFile.setPathParent(parentPath + "genome/");
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(3702);
		speciesFile.setVersion("test");
		speciesFile.addGffDB2TypeFile("testdb", GffType.GTF, gffFile);
		speciesFile.setChromSeq(refSeq);
		
		SpeciesFileSepChr sepChr = new SpeciesFileSepChr();
		sepChr.setGenomePath(parentPath + "genome/");
		sepChr.setSpeciesFile(speciesFile);
		String chrFile = sepChr.getChrSepFileOne();
		String resultFileOne = parentPath + "genome/chrSep/3702/test/chrAll.fa";
		Assert.assertEquals(resultFileOne, chrFile);
	}
}
