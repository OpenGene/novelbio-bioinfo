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
		aGeneInfoInDB.setDescrp("test raw gene info");
		aGeneInfoInDB.setGeneUniID("12345");
		aGeneInfoInDB.setSymb("symbol Test1");
		aGeneInfoInDB.addSymNom("Nomsy Test1");
		aGeneInfoInDB.addSynonym("NonSy Test1");
		
		aGeneInfoInAddSamDB.setDBinfo("NCBI");
		aGeneInfoInAddSamDB.setDescrp("test raw gene info Anoter");
		aGeneInfoInAddSamDB.addFullName("full name test2");
		aGeneInfoInAddSamDB.setGeneUniID("12345");
		aGeneInfoInAddSamDB.setSymb("symbol Test2");
		aGeneInfoInAddSamDB.addSymNom("Nomsy Test1");
		aGeneInfoInAddSamDB.addSynonym("NonSy Test1");
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
