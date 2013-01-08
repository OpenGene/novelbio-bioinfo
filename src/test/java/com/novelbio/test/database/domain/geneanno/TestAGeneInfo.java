package com.novelbio.test.database.domain.geneanno;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;


import junit.framework.TestCase;

public class TestAGeneInfo extends TestCase {
	AGeneInfo aGeneInfoInDB = new GeneInfo();
	AGeneInfo aGeneInfoInAddSamDB = new GeneInfo();
	AGeneInfo aGeneInfoInAddDifDB = new GeneInfo();
	@Before
	public void setUp() {
		aGeneInfoInDB.setDBinfo("NCBI");
		aGeneInfoInDB.setChrm("1234-5678");
		aGeneInfoInDB.setDescrp("test raw gene info");
		aGeneInfoInDB.setFullName("full name test1");
		aGeneInfoInDB.setChrm("chr1");
		aGeneInfoInDB.setGeneUniID("12345");
		aGeneInfoInDB.setSymb("symbol Test1");
		aGeneInfoInDB.setSymNom("Nomsy Test1");
		aGeneInfoInDB.setSynonym("NonSy Test1");
		
		aGeneInfoInAddSamDB.setDBinfo("NCBI");
		aGeneInfoInAddSamDB.setChrm("1234-5678");
		aGeneInfoInAddSamDB.setDescrp("test raw gene info Anoter");
		aGeneInfoInAddSamDB.setFullName("full name test2");
		aGeneInfoInAddSamDB.setChrm("chr1");
		aGeneInfoInAddSamDB.setGeneUniID("12345");
		aGeneInfoInAddSamDB.setSymb("symbol Test2");
		aGeneInfoInAddSamDB.setSymNom("Nomsy Test1");
		aGeneInfoInAddSamDB.setSynonym("NonSy Test1");
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testAddSame() {
		AGeneInfo aGeneInfoRaw = new GeneInfo();
		aGeneInfoRaw.copeyInfo(aGeneInfoInAddDifDB);
		
		boolean isadd = aGeneInfoRaw.addInfo(aGeneInfoInAddSamDB);
		assertEquals(true, isadd);
		assertEquals("test raw gene info Anoter", aGeneInfoRaw.getDescrp());
		assertEquals("full name test2", aGeneInfoRaw.getFullName());
		assertEquals("symbol Test2", aGeneInfoRaw.getSymb());

		assertEquals("NCBI", aGeneInfoInDB.getDbInfo());
	}

	@Test
	public void testAddDif() {
		AGeneInfo aGeneInfoRaw = new GeneInfo();
		aGeneInfoRaw.copeyInfo(aGeneInfoInAddDifDB);
		
		boolean isadd = aGeneInfoRaw.addInfo(aGeneInfoInAddSamDB);
		assertEquals(true, isadd);
		assertEquals("test raw gene info Anoter", aGeneInfoRaw.getDescrp());
		assertEquals("full name test2", aGeneInfoRaw.getFullName());
		assertEquals("symbol Test2", aGeneInfoRaw.getSymb());

		assertEquals("NCBI", aGeneInfoInDB.getDbInfo());
	}
}
