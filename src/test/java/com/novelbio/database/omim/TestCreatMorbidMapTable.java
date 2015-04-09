package com.novelbio.database.omim;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import com.novelbio.database.domain.omim.MorbidMap;

public class TestCreatMorbidMapTable {

	public static void main(String[] args) {
		TestCreatMorbidMapTable testCreatMorbidMapTable = new TestCreatMorbidMapTable();
		testCreatMorbidMapTable.testCreatMorbidMapTable();
	}
	
	public void testCreatMorbidMapTable() {
		String content = "17,20-lyase deficiency, isolated, 202110 (3)|CYP17A1, CYP17, P450C17|609300|10q24.32";
		String content2 = "17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)|CYP17A1, CYP17, P450C17|609300|10q24.32";
		MorbidMap morbidMap = MorbidMap.getInstanceFromOmimRecord(content2);
		Assert.assertEquals(0, morbidMap.getGeneId());
		Assert.assertEquals(609300, morbidMap.getGeneMimId());
		Assert.assertEquals(202110, morbidMap.getPhenMimId());
		Assert.assertEquals("10q24.32", morbidMap.getCytLoc());
		
		List<String> liDis = new ArrayList<>();
		liDis.add("17-alpha-hydroxylase/17");
		liDis.add("20-lyase deficiency");
		Assert.assertEquals(liDis, morbidMap.getListDis());
	}
}
