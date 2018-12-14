package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.gwas.convertformat.PlinkPedFilterHete;

/**
 * 
 * @author zongjie
 *
 */
public class TestPlinkPedFilterHete {
	String plinkBim = "/tmp/test.plink.mid";
	String plinkPed = "/tmp/test.plink.ped";
	
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

		txtWritePed.writefileln("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tA T\tT T\tT T\tG G");
		txtWritePed.writefileln("s2\ts1\t0\t0\t0\t-9\tG G\tA A\tA T\tT T\tT T\tG G");
		txtWritePed.writefileln("s3\ts1\t0\t0\t0\t-9\tC C\tA A\tA T\tT T\tT T\tG G");
		//delete
		txtWritePed.writefileln("s4\ts1\t0\t0\t0\t-9\tC C\tT A\tA T\tA A\tA A\tA A");
		
		txtWritePed.writefileln("s5\ts1\t0\t0\t0\t-9\tC C\tT T\tT T\tA A\tA A\tA A");
		txtWritePed.writefileln("s6\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tA A\tT T\tA A");
		//delete
		txtWritePed.writefileln("s7\ts1\t0\t0\t0\t-9\tG C\tA A\tA A\tA A\tT T\tA A");
		
		txtWritePed.writefileln("s8\ts1\t0\t0\t0\t-9\tG G\tT T\tT T\tT T\tT T\tG G");
		txtWritePed.writefileln("s9\ts1\t0\t0\t0\t-9\tG G\tT T\tT T\tT T\tT T\tG G");
		txtWritePed.writefileln("s10\ts1\t0\t0\t0\t-9\tC C\tA A\tA T\tT T\tT T\tG G");
		txtWritePed.close();
	}
	protected static List<String> getLsMapInfo(String chrId) {
		List<String> lsTmp = new ArrayList<>();
		lsTmp.add(chrId + "\ta\tb\t15\tG\tC");
		lsTmp.add(chrId + "\ta\tb\t25\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t500\tA\tT");//delete
		lsTmp.add(chrId + "\ta\tb\t550\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t1000\tT\tA");
		lsTmp.add(chrId + "\ta\tb\t1100\tA\tG");
		return lsTmp;
	}
	
	@Test
	public void test() {
		PlinkPedFilterHete pedFilter = new PlinkPedFilterHete();
		pedFilter.setPedMidRead(plinkPed, plinkBim);
		pedFilter.setPedMidWrite(plinkPed+"new", plinkBim+"new");
		pedFilter.setHeteProp(0.2);
		pedFilter.readPed();
		pedFilter.fillFilterInfo();
		Assert.assertEquals(Sets.newHashSet(2), pedFilter.setSiteNeedDelete);
		Assert.assertEquals(Sets.newHashSet("s4","s7"), pedFilter.setStrainNeedDelete);
		pedFilter.filter();
		
		TxtReadandWrite txtReadPed = new TxtReadandWrite(plinkPed+"new");
		List<String> lsPed = txtReadPed.readfileLs();
		txtReadPed.close();
		List<String> lsPedExp = new ArrayList<>();
		lsPedExp.add("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tT T\tT T\tG G");
		lsPedExp.add("s2\ts1\t0\t0\t0\t-9\tG G\tA A\tT T\tT T\tG G");
		lsPedExp.add("s3\ts1\t0\t0\t0\t-9\tC C\tA A\tT T\tT T\tG G");
		
		lsPedExp.add("s5\ts1\t0\t0\t0\t-9\tC C\tT T\tA A\tA A\tA A");
		lsPedExp.add("s6\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tA A");
		
		lsPedExp.add("s8\ts1\t0\t0\t0\t-9\tG G\tT T\tT T\tT T\tG G");
		lsPedExp.add("s9\ts1\t0\t0\t0\t-9\tG G\tT T\tT T\tT T\tG G");
		lsPedExp.add("s10\ts1\t0\t0\t0\t-9\tC C\tA A\tT T\tT T\tG G");
		Assert.assertArrayEquals(lsPedExp.toArray(new String[0]), lsPed.toArray(new String[0]));
		
		TxtReadandWrite txtReadMid = new TxtReadandWrite(plinkBim+"new");
		List<String> lsMid = txtReadMid.readfileLs();
		txtReadMid.close();
		List<String> lsMidExp = new ArrayList<>();
		lsMidExp.add("chr1" + "\ta\tb\t15\tG\tC");
		lsMidExp.add("chr1" + "\ta\tb\t25\tT\tA");
		lsMidExp.add("chr1" + "\ta\tb\t550\tT\tA");
		lsMidExp.add("chr1" + "\ta\tb\t1000\tT\tA");
		lsMidExp.add("chr1" + "\ta\tb\t1100\tA\tG");
		Assert.assertArrayEquals(lsMidExp.toArray(new String[0]), lsMid.toArray(new String[0]));
	}
	
}
