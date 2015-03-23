package com.novelbio.analysis.seq.fasta.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;

import com.hg.doc.ch;
import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestChrFileFormat {
	public void testChrSeqWithOutGff() {
		String refSeq = "/hdfs:/nbCloud/testJava/NBCplatform/testChrFileFormat/ChromFa/chi_ref_CHIR_1.0_chrall2.fa";
		String refSeqResult = "/hdfs:/nbCloud/testJava/NBCplatform/testChrFileFormat/ChromFa/chi_ref_CHIR_1.0_chrall2_format.fa";
		int minLen = 2000;
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setMinLen(minLen);
		chrFileFormat.rebuild();
//		chrFileFormat.movFile();
		
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
	}
	
	public void testChrSeqWithGff() {
		String gffFile = "";
		String refSeq = "/hdfs:/nbCloud/testJava/NBCplatform/testChrFileFormat/ChromFa/chi_ref_CHIR_1.0_chrall2.fa";
		String refSeqResult = "/hdfs:/nbCloud/testJava/NBCplatform/testChrFileFormat/ChromFa/chi_ref_CHIR_1.0_chrall2_format.fa";
		int minLen = 2000;
		
		Set<String> setChrId = readGffFile(gffFile);
		
		ChrFileFormat chrFileFormat = new ChrFileFormat();
		chrFileFormat.setIncludeChrId(setChrId);
		chrFileFormat.setRefSeq(refSeq);
		chrFileFormat.setMinLen(minLen);
		chrFileFormat.rebuild();
//		chrFileFormat.movFile();
		
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
	}
	
	private Set<String> readGffFile(String gffFile) {
		Set<String> setGff = new HashSet<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(gffFile);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			setGff.add(ss[0].toLowerCase());
		}
		return setGff;
	}
	
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
}
