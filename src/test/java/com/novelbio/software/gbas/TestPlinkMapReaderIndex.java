package com.novelbio.software.gbas;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.software.gbas.PlinkMapReader;

public class TestPlinkMapReaderIndex {
	String plinkBim = "/tmp/test.plink.bim";
	
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
