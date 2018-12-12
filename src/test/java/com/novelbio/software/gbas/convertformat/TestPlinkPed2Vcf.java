package com.novelbio.software.gbas.convertformat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestPlinkPed2Vcf {
	String plinkBim = "/tmp/test.plink.mid";
	String plinkPed = "/tmp/test.plink.ped";
	String plinkVcf = "/tmp/test.plink.vcf";

	@Before
	public void prepare() {
		TxtReadandWrite txtWriteBim = new TxtReadandWrite(plinkBim, true);
		List<String> lsTmp = getLsMapInfo("chr1");
		for (String content : lsTmp) {
			txtWriteBim.writefileln(content);
		}
		txtWriteBim.close();
		
		TxtReadandWrite txtWritePed = new TxtReadandWrite(plinkPed, true);
//		txtWritePed.writefileln("s1\ts1\t0\t0\t0\t-9\tG C\tT A\tA T\tT T\tT A\tG G");

		txtWritePed.writefileln("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tA G\tT T\tT T\tG G");
		txtWritePed.writefileln("s2\ts2\t0\t0\t0\t-9\tG C\tA A\tA T\tT T\tT C\tG G");
		txtWritePed.writefileln("s3\ts3\t0\t0\t0\t-9\tC A\tA C\tA G\t0 0\tT T\tG G");
		txtWritePed.writefileln("s4\ts4\t0\t0\t0\t-9\tA A\tT A\tG T\tA A\tG G\tA A");		
		txtWritePed.writefileln("s5\ts5\t0\t0\t0\t-9\tC G\tT C\tT T\tA A\tG C\tA A");
		txtWritePed.writefileln("s6\ts6\t0\t0\t0\t-9\tG A\tC C\tA A\tA A\tT T\tA A");
		
		txtWritePed.close();
	}
	private static List<String> getLsMapInfo(String chrId) {
		List<String> lsTmp = new ArrayList<>();
		lsTmp.add(chrId + "\ta\tb\t15\tG\tC");
		lsTmp.add(chrId + "\ta\tb\t25\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t50\tA\tT");//delete
		lsTmp.add(chrId + "\ta\tb\t80\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t100\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t200\tA\tG");
		return lsTmp;
	}
	
	@Test
	public void testConvert() {
		PlinkPed2Vcf ped2Vcf = new PlinkPed2Vcf();
		ped2Vcf.convertPed2Vcf(plinkPed, plinkBim, plinkVcf);
		TxtReadandWrite txtReadVcf = new TxtReadandWrite(plinkVcf);
		List<String> lsResult = new ArrayList<>();
		for (String content : txtReadVcf.readlines()) {
			if (content.startsWith("#") && !content.startsWith("#CHROM")) {
				continue;
			}
			lsResult.add(content);
		}
		txtReadVcf.close();
		
		List<String> lsExp = new ArrayList<>();
		lsExp.add("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	s1	s2	s3	s4	s5	s6");
		lsExp.add("chr1	15	.	G	C,A	.	PASS	.	GT	0|0	0|1	1|2	2|2	1|0	0|2");
		lsExp.add("chr1	25	.	T	A,C	.	PASS	.	GT	0|0	1|1	1|2	0|1	0|2	2|2");
		lsExp.add("chr1	50	.	A	G,T	.	PASS	.	GT	0|1	0|2	0|1	1|2	2|2	0|0");
		lsExp.add("chr1	80	.	T	A	.	PASS	.	GT	0|0	0|0	.|.	1|1	1|1	1|1");
		lsExp.add("chr1	100	.	T	C,G	.	PASS	.	GT	0|0	0|1	0|0	2|2	2|1	0|0");
		lsExp.add("chr1	200	.	A	G	.	PASS	.	GT	1|1	1|1	1|1	0|0	0|0	0|0");
		Assert.assertArrayEquals(lsExp.toArray(new String[0]), lsResult.toArray(new String[0]));
		
		ped2Vcf.convertVcf2Ped(plinkVcf, plinkPed);
		TxtReadandWrite txtReadPed = new TxtReadandWrite(plinkPed);
		lsResult = new ArrayList<>();
		for (String content : txtReadPed.readlines()) {
			if (content.startsWith("#") && !content.startsWith("#CHROM")) {
				continue;
			}
			lsResult.add(content);
		}
		txtReadPed.close();
		
		List<String> lsPed = new ArrayList<>();
		lsPed.add("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tA G\tT T\tT T\tG G");
		lsPed.add("s2\ts2\t0\t0\t0\t-9\tG C\tA A\tA T\tT T\tT C\tG G");
		lsPed.add("s3\ts3\t0\t0\t0\t-9\tC A\tA C\tA G\t0 0\tT T\tG G");
		lsPed.add("s4\ts4\t0\t0\t0\t-9\tA A\tT A\tG T\tA A\tG G\tA A");
		lsPed.add("s5\ts5\t0\t0\t0\t-9\tC G\tT C\tT T\tA A\tG C\tA A");
		lsPed.add("s6\ts6\t0\t0\t0\t-9\tG A\tC C\tA A\tA A\tT T\tA A");
		Assert.assertArrayEquals(lsPed.toArray(new String[0]), lsResult.toArray(new String[0]));

	}
	
}
