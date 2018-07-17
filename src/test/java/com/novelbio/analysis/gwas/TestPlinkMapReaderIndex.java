package com.novelbio.analysis.gwas;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestPlinkMapReaderIndex {
	String plinkBim = "/tmp/test.plink.bim";
	
	@Before
	public void prepare() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkBim, true);
		List<String> lsTmp = getLsMapInfo("chr2");
		for (String content : lsTmp) {
			txtWrite.writefileln(content);
		}
		lsTmp = getLsMapInfo("chr1");
		for (String content : lsTmp) {
			txtWrite.writefileln(content);
		}
		txtWrite.close();
	}
	protected static List<String> getLsMapInfo(String chrId) {
		List<String> lsTmp = new ArrayList<>();
		lsTmp.add(chrId + "\ta\tb\t15\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t25\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t50\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t80\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t100\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t200\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t250\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t280\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t300\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t330\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t720\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t850\tA\tG");
		return lsTmp;
	}
	
	@Test
	public void testIndex() {
		List<String[]> lsIndexs = PlinkMapReader.createPlinkMapIndexLs(plinkBim);
		assertEquals(lsIndexs.size(), 2);
		assertStringArray(lsIndexs.get(0), new String[]{"chr2", "0"});
		assertStringArray(lsIndexs.get(1), new String[]{"chr1", "200"});
	}
	
	private void assertStringArray(String[] array1, String[] array2) {
		assertEquals(array1.length, array2.length);
		for (int i = 0; i < array1.length; i++) {
			assertEquals(array1[i], array2[i]);
		}
	}
}
