package com.novelbio.database.model.modgeneid;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;

public class TestGeneID extends TestCase {
	ManageNCBIUniID manageNCBIUniID = new ManageNCBIUniID();
	int taxID = 123456;
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
	}
	
	@Override
	protected void tearDown() throws Exception {
//		AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
//		ageneUniID.setAccID("Test1");
//		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
//		ageneUniID.setGenUniID("1234567890");
//		ageneUniID.setTaxID(taxID);
//		manageNCBIUniID.delete(ageneUniID);
//		
//		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
//		ageneUniID.setAccID("Test2");
//		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
//		ageneUniID.setGenUniID("12345678902");
//		ageneUniID.setTaxID(taxID);
//		manageNCBIUniID.delete(ageneUniID);
//		
//		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
//		ageneUniID.setAccID("Test2");
//		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
//		ageneUniID.setGenUniID("12345678903");
//		ageneUniID.setTaxID(taxID);
//		manageNCBIUniID.delete(ageneUniID);
//		
//		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
//		ageneUniID.setAccID("Test4");
//		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
//		ageneUniID.setGenUniID("1234567890");
//		ageneUniID.setTaxID(taxID);
//		manageNCBIUniID.delete(ageneUniID);
//		
//		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
//		ageneUniID.setAccID("Test5");
//		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
//		ageneUniID.setGenUniID("1234567890");
//		ageneUniID.setTaxID(taxID);
//		manageNCBIUniID.delete(ageneUniID);
//		
		super.tearDown();
	}
	@Test
	public void test() {
		testGoInsert();
	}
		
	
	private void testInsert() {
		AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
		ageneUniID.setAccID("Test1");
		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
		ageneUniID.setGenUniID("1234567890");
		ageneUniID.setTaxID(taxID);
		manageNCBIUniID.saveNCBIUniID(ageneUniID);

		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
		ageneUniID.setAccID("Test2");
		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
		ageneUniID.setGenUniID("12345678902");
		ageneUniID.setTaxID(taxID);
		manageNCBIUniID.saveNCBIUniID(ageneUniID);
		
		ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
		ageneUniID.setAccID("Test2");
		ageneUniID.setDataBaseInfo(DBAccIDSource.EMBL.name());
		ageneUniID.setGenUniID("12345678903");
		ageneUniID.setTaxID(taxID);
		manageNCBIUniID.saveNCBIUniID(ageneUniID);
		
		AgeneUniID ageneUniIDGet = manageNCBIUniID.findByGeneUniIDAndAccIDAndTaxID(GeneID.IDTYPE_GENEID, "1234567890", "Test1", taxID);
		assertEquals("Test1", ageneUniIDGet.getAccID());
		assertEquals("1234567890", ageneUniIDGet.getGenUniID());
		assertEquals(taxID, ageneUniIDGet.getTaxID());
		
		ageneUniIDGet = manageNCBIUniID.findByGeneUniIDAndAccIDAndTaxID(GeneID.IDTYPE_GENEID, "12345678902", "Test2", taxID);
		assertEquals("Test2", ageneUniIDGet.getAccID());
		assertEquals("12345678902", ageneUniIDGet.getGenUniID());
		assertEquals(taxID, ageneUniIDGet.getTaxID());
		
		ageneUniIDGet = manageNCBIUniID.findByGeneUniIDAndAccIDAndTaxID(GeneID.IDTYPE_GENEID, "12345678903", "Test2", taxID);
		assertEquals("Test2", ageneUniIDGet.getAccID());
		assertEquals("12345678903", ageneUniIDGet.getGenUniID());
		assertEquals(taxID, ageneUniIDGet.getTaxID());
	}
	
	private void testGeneIDInsert() {
		GeneID geneID = new GeneID("", taxID);
		geneID.addUpdateRefAccID("Test2");
		geneID.addUpdateRefAccID("test1");
		geneID.setUpdateAccID("Test4");
		geneID.setUpdateDBinfo(DBAccIDSource.EMBL.name(), false);
		geneID.update(false);
		
		geneID.setUpdateAccID("Test5");
		geneID.setUpdateDBinfo(DBAccIDSource.EMBL.name(), false);
		geneID.update(false);
		
		
		List<AgeneUniID> lsageneUniIDGet = manageNCBIUniID.findByAccID(GeneID.IDTYPE_GENEID, "Test4", taxID);
		assertEquals(1, lsageneUniIDGet.size());
		assertEquals("1234567890", lsageneUniIDGet.get(0).getGenUniID());
	
		lsageneUniIDGet = manageNCBIUniID.findByAccID(GeneID.IDTYPE_GENEID, "Test5", taxID);
		assertEquals(1, lsageneUniIDGet.size());
		assertEquals("1234567890", lsageneUniIDGet.get(0).getGenUniID());
	}
	
	private void testGeneInfoInsert() {
		ManageDBInfo manageDBInfo = new ManageDBInfo();
		GeneID geneID = new GeneID("", taxID);
		geneID.addUpdateRefAccID("Test2");
		geneID.addUpdateRefAccID("test1");
		geneID.setUpdateAccID("Test5");
		geneID.setUpdateDBinfo(DBAccIDSource.EMBL.name(), false);
		
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.EMBL.name()));
		geneInfo.setDescrp("test1sefsef");
		geneInfo.setSymb("sese");
		geneInfo.setTaxID(taxID);
		geneInfo.addDbXref("serfser");
		geneID.setUpdateGeneInfo(geneInfo);
		geneID.update(false);
		
		GeneID geneID2 = new GeneID("Test5", taxID);
		assertEquals(0, geneID2.getGeneInfo().getPubmedIDs().size());
		
		geneInfo = new GeneInfo();
		geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.EMBL.name()));
		geneInfo.setDescrp("test1sefsef");
		geneInfo.setSymb("sese");
		geneInfo.addDbXref("serfser22");
		geneInfo.addPubID("232533");
		geneInfo.setTaxID(taxID);
		geneID.setUpdateGeneInfo(geneInfo);
		geneID.update(false);
		
		List<AgeneUniID> lsageneUniIDGet = manageNCBIUniID.findByAccID(GeneID.IDTYPE_GENEID, "Test5", taxID);
		assertEquals(1, lsageneUniIDGet.size());
		assertEquals("1234567890", lsageneUniIDGet.get(0).getGenUniID());
	
		geneID2 = new GeneID("Test5", taxID);
		assertEquals("sese", geneID2.getGeneInfo().getSymb());
		assertEquals(1, geneID2.getGeneInfo().getPubmedIDs().size());
		assertEquals("232533", geneID2.getGeneInfo().getPubmedIDs().iterator().next());
	}
	
	private void testGoInsert() {
		GeneID geneID = new GeneID("eee", taxID);
		geneID.addUpdateRefAccID("Test2");
		
		geneID.addUpdateRefAccID("test1");
		geneID.setUpdateAccID("Test5");
		geneID.setUpdateDBinfo(DBAccIDSource.EMBL.name(), false);
		List<String> lsList = new ArrayList<String>();
		lsList.add("PMID:1234");
		geneID.addUpdateGO("GO:0000105", DBAccIDSource.EMBL, "IEA", lsList, "NOT");
		assertEquals(1, geneID.getGene2GO(GOtype.BP).size());
		assertEquals("NOT", geneID.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(1, geneID.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(1, geneID.getGene2GO(GOtype.BP).get(0).getReference().size());
		assertEquals("PMID:1234", geneID.getGene2GO(GOtype.BP).get(0).getReference().iterator().next());
		geneID.update(false);
		
		GeneID geneID2 = new GeneID("Test5", taxID);
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).size());
		assertEquals("NOT", geneID2.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).get(0).getReference().size());
		assertEquals("PMID:1234", geneID2.getGene2GO(GOtype.BP).get(0).getReference().iterator().next());
		
		lsList = new ArrayList<String>();
		lsList.add("PMID:3456");
		lsList.add("PMID:4567");
		geneID = new GeneID("Test5", taxID);		
		geneID.addUpdateGO("GO:0000105", DBAccIDSource.EMBL, "ISA", lsList, "AAAA");
		assertEquals(1, geneID.getGene2GO(GOtype.BP).size());
		assertEquals("AAAA", geneID.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(2, geneID.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(3, geneID.getGene2GO(GOtype.BP).get(0).getReference().size());
		
		geneID.update(false);
		
		geneID2 = new GeneID("Test5", taxID);
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).size());
		assertEquals("AAAA", geneID2.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(2, geneID2.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(3, geneID2.getGene2GO(GOtype.BP).get(0).getReference().size());
	}
	
	private void testBlastInsert() {
		GeneID geneID = new GeneID("ffff", taxID);
		geneID.addUpdateRefAccID("Test2");
		
		geneID.addUpdateRefAccID("test1");
		geneID.setUpdateAccID("Test5");
		geneID.setUpdateDBinfo(DBAccIDSource.EMBL.name(), false);
		geneID.addUpdateBlastInfo(blastInfo);
		List<String> lsList = new ArrayList<String>();
		lsList.add("PMID:1234");

		assertEquals(1, geneID.getGene2GO(GOtype.BP).size());
		assertEquals("NOT", geneID.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(1, geneID.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(1, geneID.getGene2GO(GOtype.BP).get(0).getReference().size());
		assertEquals("PMID:1234", geneID.getGene2GO(GOtype.BP).get(0).getReference().iterator().next());
		geneID.update(false);
		
		GeneID geneID2 = new GeneID("Test5", taxID);
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).size());
		assertEquals("NOT", geneID2.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).get(0).getReference().size());
		assertEquals("PMID:1234", geneID2.getGene2GO(GOtype.BP).get(0).getReference().iterator().next());
		
		lsList = new ArrayList<String>();
		lsList.add("PMID:3456");
		lsList.add("PMID:4567");
		geneID = new GeneID("Test5", taxID);		
		geneID.addUpdateGO("GO:0000105", DBAccIDSource.EMBL, "ISA", lsList, "AAAA");
		assertEquals(1, geneID.getGene2GO(GOtype.BP).size());
		assertEquals("AAAA", geneID.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(2, geneID.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(3, geneID.getGene2GO(GOtype.BP).get(0).getReference().size());
		
		geneID.update(false);
		
		geneID2 = new GeneID("Test5", taxID);
		assertEquals(1, geneID2.getGene2GO(GOtype.BP).size());
		assertEquals("AAAA", geneID2.getGene2GO(GOtype.BP).get(0).getQualifier());
		assertEquals(2, geneID2.getGene2GO(GOtype.BP).get(0).getEvidence().size());
		assertEquals(3, geneID2.getGene2GO(GOtype.BP).get(0).getReference().size());
	}
	
	
	
}
