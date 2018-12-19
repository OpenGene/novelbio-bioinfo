package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestPlinkPedFastaConvertor {
	String plinkPed = "/tmp/test.plink.ped";
	String fastaFile = "/tmp/test.plink.fasta";
	
	List<String> lsPed = new ArrayList<>();
	List<String> lsFasta = new ArrayList<>();
	
	@Before
	public void prepare() {
		TxtReadandWrite txtWritePed = new TxtReadandWrite(plinkPed, true);
		lsPed.clear();
		lsPed.add("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tC C\tG G");
		lsPed.add("s2\ts2\t0\t0\t0\t-9\tG G\tA A\tA A\tT T\tC C\tG G");
		lsPed.add("s3\ts3\t0\t0\t0\t-9\tC C\tA A\tA A\tT T\tC C\tG G");
		lsPed.add("s4\ts4\t0\t0\t0\t-9\tC C\tT T\tA A\tA A\tA A\tA A");
		lsPed.add("s5\ts5\t0\t0\t0\t-9\tC C\tT T\tT T\tA A\tA A\tA A");
		lsPed.add("s6\ts6\t0\t0\t0\t-9\tG G\tT T\tA A\tA A\tC C\tA A");
		lsPed.add("s7\ts7\t0\t0\t0\t-9\tG G\tA A\tA A\tA A\tC C\tA A");
		
		lsFasta.clear();
		lsFasta.add(">s1");
		lsFasta.add("GTATCG");
		lsFasta.add(">s2");
		lsFasta.add("GAATCG");
		lsFasta.add(">s3");
		lsFasta.add("CAATCG");
		lsFasta.add(">s4");
		lsFasta.add("CTAAAA");
		lsFasta.add(">s5");
		lsFasta.add("CTTAAA");
		lsFasta.add(">s6");
		lsFasta.add("GTAACA");
		lsFasta.add(">s7");
		lsFasta.add("GAAACA");
		for (String content : lsPed) {
			txtWritePed.writefileln(content);
		}
		txtWritePed.close();
		
	}
	
	@Test
	public void testPlinkPed2Fasta() {
		PlinkPedFastaConvertor.convertPed2Fasta(plinkPed, fastaFile);
		List<String> lsReal = TxtReadandWrite.readfileLs(fastaFile);
		Assert.assertArrayEquals(lsFasta.toArray(new String[0]), lsReal.toArray(new String[0]));
	}
	
	@Test
	public void testPlinkFasta2Ped() {
		PlinkPedFastaConvertor.convertFasta2Ped(fastaFile, plinkPed);
		List<String> lsReal = TxtReadandWrite.readfileLs(plinkPed);
		Assert.assertArrayEquals(lsPed.toArray(new String[0]), lsReal.toArray(new String[0]));
	}
}
