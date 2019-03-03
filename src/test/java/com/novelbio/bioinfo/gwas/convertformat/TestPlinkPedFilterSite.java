package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.gwas.convertformat.PlinkPedFilterSite;

/**
 * 
 * @author zongjie
 *
 */
public class TestPlinkPedFilterSite {
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

		txtWritePed.writefileln("s1\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tT T\tG G");
		txtWritePed.writefileln("s2\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tT T\tG G");
		txtWritePed.writefileln("s3\ts1\t0\t0\t0\t-9\tC C\tA A\tC C\tT T\tT T\tG G");
		//delete
		txtWritePed.writefileln("s4\ts1\t0\t0\t0\t-9\tC C\tA A\tC C\tA A\tA A\tA A");
		
		txtWritePed.writefileln("s5\ts1\t0\t0\t0\t-9\tC C\tA A\tC C\tA A\tA A\tA A");
		txtWritePed.writefileln("s6\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tA A\tT T\tA A");
		//delete
		txtWritePed.writefileln("s7\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tA A\tT T\tA A");
		
		txtWritePed.writefileln("s8\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tT T\tG G");
		txtWritePed.writefileln("s9\ts1\t0\t0\t0\t-9\tG G\tT T\tA A\tT T\tT T\tG G");
		txtWritePed.writefileln("s10\ts1\t0\t0\t0\t-9\tC C\tA A\tC C\tT T\tT T\tG G");
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
		PlinkPedFilterSite pedFilter = new PlinkPedFilterSite();
		pedFilter.setPedMidRead(plinkPed, plinkBim);
		pedFilter.setPedMidWrite(plinkPed+"new", plinkBim+"new");
		pedFilter.readPed();
		pedFilter.fillFilterInfo();
		Assert.assertEquals(Sets.newHashSet(1), pedFilter.setSiteNeedDelete);
	}
	
}
