package com.novelbio.analysis.seq.fasta.format;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffGetChrId;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestChrFileFormat {
	String parentPath = "src/test/resources/test_file/reference/ara/";
	String gffFile = parentPath + "TAIR10_Gff3_simple_with_contig.gtf.gz";
	String refSeq = parentPath + "chrAll.fa";
	
	@Test
	public void testGetChrId() {
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		
		String chrId = ">chr290 nb|tge.iii";
		chrFileFormat.setRegex("chr\\w+");
		Assert.assertEquals("chr290", chrFileFormat.getChrId(chrId));
		
		chrId = ">chr290 nb|tge.iii";
		chrFileFormat.setRegex(" ");
		Assert.assertEquals("chr290", chrFileFormat.getChrId(chrId));
		
		chrId = ">chr290nb|tge.iii";
		chrFileFormat.setRegex("chr\\d+");
		Assert.assertEquals("chr290", chrFileFormat.getChrId(chrId));
		
		chrId = ">chr290 nb|tge.iii";
		chrFileFormat.setRegex("");
		Assert.assertEquals("chr290 nb|tge.iii", chrFileFormat.getChrId(chrId));
	}
	
	@Test
	public void testCutChrIdByLenAndGff() {
		int minLen = 1000;
		int maxNum = 3000;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setGffChrId = readGffFile(gffFile);
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setIncludeChrId(setGffChrId);
		chrFileFormat.setMaxNum(maxNum);
		chrFileFormat.setMinLen(minLen);
		
		Set<String> setChrIdGffAndLen = chrFileFormat.cutChrIdByLenAndGff(mapChrId2Len);
		Assert.assertEquals(6, setChrIdGffAndLen.size());
		// 选中的染色体是否都在gff中或者比指定的长
		for (String chrId : setChrIdGffAndLen) {
			Assert.assertEquals(true, setGffChrId.contains(chrId) || mapChrId2Len.get(chrId) >= minLen);
		}
		
		for (String chrId : mapChrId2Len.keySet()) {
			if (!setChrIdGffAndLen.contains(chrId)) {
				Assert.assertEquals(true, !setGffChrId.contains(chrId) && mapChrId2Len.get(chrId) < minLen);
			}
		}
	}
	
	@Test
	public void testCutChrIdByLenAndGffAndNum() {
		int minLen = 1000;
		int maxNum = 5;
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		chrSeqHash.close();
		Set<String> setGffChrId = readGffFile(gffFile);
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setIncludeChrId(setGffChrId);
		chrFileFormat.setMaxNum(maxNum);
		chrFileFormat.setMinLen(minLen);
		
		Set<String> setChrIdGffAndLen = chrFileFormat.cutChrIdByLenAndGff(mapChrId2Len);
		Set<String> setChrIdGffAndLenAndSort = chrFileFormat.cutChrIdBySort(mapChrId2Len, setChrIdGffAndLen);
		// 选中的染色体是否都在gff中或者比指定的长
		for (String chrId : setChrIdGffAndLenAndSort) {
			Assert.assertEquals(true, setGffChrId.contains(chrId) || mapChrId2Len.get(chrId) >= minLen);
		}
		Assert.assertEquals(true, setChrIdGffAndLenAndSort.size() == 5);
	}
	
	@Test
	public void testExtractSeq() {
		String refSeqResult = FileOperate.changeFileSuffix(refSeq, "_modify", null);
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
		
		Set<String> setChrId = new HashSet<>();
		setChrId.add("chr2"); setChrId.add("contig1"); setChrId.add("contig3");
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setResultSeq(refSeqResult);
		chrFileFormat.extractSeq(setChrId);
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		ChrSeqHash chrSeqHash2 = new ChrSeqHash(refSeqResult, "");
		
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		Map<String, Long> mapChrId2LenNew = chrSeqHash2.getMapChrLength();
		chrSeqHash.close();
		chrSeqHash2.close();
		for (String chrId : mapChrId2LenNew.keySet()) {
			Long len = mapChrId2LenNew.get(chrId);
			Assert.assertEquals(mapChrId2Len.get(chrId), len);
			Assert.assertEquals(true, setChrId.contains(chrId));
		}
		Assert.assertEquals(setChrId.size(), mapChrId2LenNew.size());
		
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
	}
	
	@Test
	public void testChrSeqWithOutGff() {
		String refSeqResult = FileOperate.changeFileSuffix(refSeq, "_modify", null);
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
		
		int minLen = 1000;
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setResultSeq(refSeqResult);
		chrFileFormat.setMinLen(minLen);
		chrFileFormat.rebuild();
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		ChrSeqHash chrSeqHash2 = new ChrSeqHash(refSeqResult, "");
		
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		Map<String, Long> mapChrId2LenNew = chrSeqHash2.getMapChrLength();
		for (String chrId : mapChrId2Len.keySet()) {
			long len = mapChrId2Len.get(chrId);
			if (len < minLen) {
				Assert.assertEquals(false, mapChrId2LenNew.containsKey(chrId));
			} else if (len >= minLen) {
				Assert.assertEquals(true, mapChrId2LenNew.containsKey(chrId));
			}			
		}
		chrSeqHash.close();
		chrSeqHash2.close();
		
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
	}
	
	@Test
	public void testChrSeqWithGffNoNumLimit() {
		String refSeqResult = FileOperate.changeFileSuffix(refSeq, "_modifyGffNoLimit", null);
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
		int minLen = 1000;
		Set<String> setChrId = readGffFile(gffFile);
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setIncludeChrId(setChrId);
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setResultSeq(refSeqResult);
		chrFileFormat.setMinLen(minLen);
		chrFileFormat.rebuild();
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		ChrSeqHash chrSeqHash2 = new ChrSeqHash(refSeqResult, "");
		
		
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		Map<String, Long> mapChrId2LenNew = chrSeqHash2.getMapChrLength();
		for (String chrId : mapChrId2Len.keySet()) {
			long len = mapChrId2Len.get(chrId);
			if (len < minLen && !setChrId.contains(chrId)) {
				Assert.assertEquals(false, mapChrId2LenNew.containsKey(chrId));
			} else if (len >= minLen) {
				Assert.assertEquals(true, mapChrId2LenNew.containsKey(chrId));
			}
		}
		
		chrSeqHash.close();
		chrSeqHash2.close();
		
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
	}
	
	@Test
	public void testChrSeqWithGffWithLimit() {
		String refSeqResult = FileOperate.changeFileSuffix(refSeq, "_modifyGffWithLimit", null);
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
		int minLen = 200;
		int maxNum = 4;
		int realNum = 5;//gff文件中有五条
		Set<String> setChrId = readGffFile(gffFile);
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setIncludeChrId(setChrId);
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setResultSeq(refSeqResult);
		chrFileFormat.setMinLen(minLen);
		chrFileFormat.setMaxNum(maxNum);
		chrFileFormat.rebuild();
		
		ChrSeqHash chrSeqHash = new ChrSeqHash(refSeq, "");
		ChrSeqHash chrSeqHash2 = new ChrSeqHash(refSeqResult, "");
		
		
		Map<String, Long> mapChrId2Len = chrSeqHash.getMapChrLength();
		Map<String, Long> mapChrId2LenNew = chrSeqHash2.getMapChrLength();
		for (String chrId : mapChrId2Len.keySet()) {
			long len = mapChrId2Len.get(chrId);
			if (len < minLen && !setChrId.contains(chrId)) {
				Assert.assertEquals(false, mapChrId2LenNew.containsKey(chrId));
			}		
		}
		Assert.assertEquals(realNum, mapChrId2LenNew.size());
		chrSeqHash.close();
		chrSeqHash2.close();
		
		FileOperate.deleteFileFolder(refSeqResult);
		FileOperate.deleteFileFolder(refSeqResult + ".fai");
	}
	
	private Set<String> readGffFile(String gffFile) {
		GffGetChrId gffGetChrId = new GffGetChrId();
		Set<String> setGff = new HashSet<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(gffFile);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			if (ss.length > 4 ) {
				gffGetChrId.getChrID(ss);
				ss[2] = ss[2].toLowerCase();
				if (ss[2].equals("gene") || ss[2].equals("exon") || ss[2].equals("cds")) {
					setGff.add(gffGetChrId.getChrID(ss).toLowerCase());
				}
			}
		}
		txtRead.close();
		return setGff;
	}

}
