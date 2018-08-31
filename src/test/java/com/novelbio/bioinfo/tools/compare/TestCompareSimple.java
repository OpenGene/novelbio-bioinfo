package com.novelbio.bioinfo.tools.compare;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.tools.compare.CompareSimple;

public class TestCompareSimple {
	static String file1 = "/tmp/test/compare1.txt";
	static String file2 = "/tmp/test/compare2.txt";
	static String file3 = "/tmp/test/compare3.txt";

	@BeforeClass
	public static void before() {
		FileOperate.createFolders(FileOperate.getPathName(file1));
		TxtReadandWrite txtWrite1 = new TxtReadandWrite(file1, true);
		txtWrite1.writefileln(new String[]{"accId", "value1", "value2", "value3"});
		txtWrite1.writefileln(new String[]{"A", "1", "2", "3"});
		txtWrite1.writefileln(new String[]{"B", "2", "3", "4"});
		txtWrite1.writefileln(new String[]{"A", "1", "2", "4"});
		txtWrite1.writefileln(new String[]{"C", "3", "4", "5"});
		txtWrite1.writefileln(new String[]{"D", "5", "6", "7"});
		txtWrite1.writefileln(new String[]{"E", "9", "0", "1"});
		txtWrite1.close();
		
		TxtReadandWrite txtWrite2 = new TxtReadandWrite(file2, true);
		txtWrite2.writefileln(new String[]{"accId", "value21", "value22", "value23"});
		txtWrite2.writefileln(new String[]{"A", "11", "22", "33"});
		txtWrite2.writefileln(new String[]{"B", "22", "33", "44"});
		txtWrite2.writefileln(new String[]{"A", "11", "22", "44"});
		txtWrite2.writefileln(new String[]{"F", "33", "44", "55"});
		txtWrite2.writefileln(new String[]{"G", "55", "66", "77"});
		txtWrite2.writefileln(new String[]{"E", "99", "00", "11"});
		txtWrite2.close();
		
		TxtReadandWrite txtWrite3 = new TxtReadandWrite(file3, true);
		txtWrite3.writefileln(new String[]{"value21", "accId", "value22", "value23"});
		txtWrite3.writefileln(new String[]{"11", "A", "22", "33"});
		txtWrite3.writefileln(new String[]{"22", "B", "33", "44"});
		txtWrite3.writefileln(new String[]{"11", "A", "22", "44"});
		txtWrite3.writefileln(new String[]{"33", "F", "44", "55"});
		txtWrite3.writefileln(new String[]{"55", "G", "66", "77"});
		txtWrite3.writefileln(new String[]{"99", "E", "00", "11"});
		txtWrite3.close();
	}
	
	@AfterClass
	public static void After() {
		FileOperate.deleteFileFolder(file1);
		FileOperate.deleteFileFolder(file2);
		FileOperate.deleteFileFolder(file3);
	}
	
	@Test
	public void testOverlap() {
		CompareSimple compareSimple = new CompareSimple();
		compareSimple.setFile1(file1, "p1");
		compareSimple.setFile2(file2, "p2");
		compareSimple.setCompareColNum(1);

		compareSimple.readFiles();
		List<String[]> lsResult = compareSimple.getLsOverlapInfoWithTitle();
		
		List<String[]> lsResultExpect = new ArrayList<>();
		lsResultExpect.add(new String[]{"accId", "p1_value1", "p1_value2", "p1_value3", "p2_value21", "p2_value22", "p2_value23"});
		lsResultExpect.add(new String[]{"A", "1", "2", "3", "11", "22", "33"});
		lsResultExpect.add(new String[]{"A", "1", "2", "3", "11", "22", "44"});
		lsResultExpect.add(new String[]{"A", "1", "2", "4", "11", "22", "33"});
		lsResultExpect.add(new String[]{"A", "1", "2", "4", "11", "22", "44"});
		lsResultExpect.add(new String[]{"B", "2", "3", "4", "22", "33", "44"});
		lsResultExpect.add(new String[]{"E", "9", "0", "1", "99", "00", "11"});
		
		Assert.assertEquals(lsResultExpect.size(), lsResult.size());
		for (int i = 0; i < lsResult.size(); i++) {
			Assert.assertArrayEquals(lsResultExpect.get(i), lsResult.get(i));
		}
		
		compareSimple = new CompareSimple();
		compareSimple.setFile1(file1, "p1");
		compareSimple.setFile2(file3, "p2");
		compareSimple.setCompareColNum(1);
		compareSimple.setCompareColNum2(2);

		compareSimple.readFiles();
		lsResult = compareSimple.getLsOverlapInfoWithTitle();
		Assert.assertEquals(lsResultExpect.size(), lsResult.size());
		for (int i = 0; i < lsResult.size(); i++) {
			Assert.assertArrayEquals(lsResultExpect.get(i), lsResult.get(i));
		}
		
	}
}
