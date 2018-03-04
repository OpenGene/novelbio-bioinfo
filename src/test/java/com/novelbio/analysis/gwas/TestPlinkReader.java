package com.novelbio.analysis.gwas;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestPlinkReader {
	
	static String testPlinkPed = PathDetail.getTmpPathWithSep() + "testPlinkPed";
	
	@BeforeClass
	public static void BeforeTest() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(testPlinkPed, true);
		txtWrite.writefile("IRIS_-43 IR_3590 0 0 0 -9 G G T T A A T T T T A A C A\n");
		txtWrite.writefile("IRI910 IRI3-11 0 0 0 -9 G G T T A A T G C T C A G G\r\n");
		txtWrite.writefile("IR23S_0 IRIS_31910 0 0 0 -9 G G T T A A T T T T A A C A\r");
		txtWrite.writefile("IRIS_313-10 IR10 0 0 0 -9 G G T T A A T T T T A A C A\n");
		txtWrite.writefile("IR_3-1r10 IrRIS_310 0 0 0 -9 G G T T A A T T T T A A C A\n");
		txtWrite.close();
	}
	@AfterClass
	public static void AfterTest() {
		FileOperate.deleteFileFolder(testPlinkPed);
	}
	
	@Test
	public void testRead() throws IOException {
		List<String[]> lsIndexes = PlinkPedReader.createPlinkPedIndexLs(testPlinkPed);
		PlinkPedReader plinkPedReader = new PlinkPedReader(testPlinkPed);		
		plinkPedReader.readIndex(lsIndexes);
		
		List<Allele> lsAlleles = plinkPedReader.readAllelsFromSample("IRI910", 4, 7);
		Assert.assertEquals(4, lsAlleles.size());
		Assert.assertEquals("T", lsAlleles.get(0).getAllele1());
		Assert.assertEquals("G", lsAlleles.get(0).getAllele2());

		Assert.assertEquals("C", lsAlleles.get(1).getAllele1());
		Assert.assertEquals("T", lsAlleles.get(1).getAllele2());
		
		Assert.assertEquals("C", lsAlleles.get(2).getAllele1());
		Assert.assertEquals("A", lsAlleles.get(2).getAllele2());
		
		Assert.assertEquals("G", lsAlleles.get(3).getAllele1());
		Assert.assertEquals("G", lsAlleles.get(3).getAllele2());
		plinkPedReader.close();
	}
	
	@Test
	public void testReadIt() throws IOException {
		List<String[]> lsIndexes = PlinkPedReader.createPlinkPedIndexLs(testPlinkPed);
		PlinkPedReader plinkPedReader = new PlinkPedReader(testPlinkPed);
		plinkPedReader.readIndex(lsIndexes);
		
		List<Allele> lsAlleles = new ArrayList<>();
		for (Allele allele : plinkPedReader.readAllelsFromSample("IRI910", 4)) {
			lsAlleles.add(allele);
		}
		Assert.assertEquals(4, lsAlleles.size());
		Assert.assertEquals("T", lsAlleles.get(0).getAllele1());
		Assert.assertEquals("G", lsAlleles.get(0).getAllele2());
		Assert.assertEquals(4, lsAlleles.get(0).getIndex());

		Assert.assertEquals("C", lsAlleles.get(1).getAllele1());
		Assert.assertEquals("T", lsAlleles.get(1).getAllele2());
		Assert.assertEquals(5, lsAlleles.get(1).getIndex());

		Assert.assertEquals("C", lsAlleles.get(2).getAllele1());
		Assert.assertEquals("A", lsAlleles.get(2).getAllele2());
		Assert.assertEquals(6, lsAlleles.get(2).getIndex());

		Assert.assertEquals("G", lsAlleles.get(3).getAllele1());
		Assert.assertEquals("G", lsAlleles.get(3).getAllele2());
		Assert.assertEquals(7, lsAlleles.get(3).getIndex());

		plinkPedReader.close();
	}
	@Test
	public void testCreatePlinkPedIndex() throws IOException {
		List<String[]> lsIndexes = PlinkPedReader.createPlinkPedIndexLs(testPlinkPed);
		Assert.assertArrayEquals(new String[]{"IRIS_-43", "0", "26", "7"}, lsIndexes.get(0));
		Assert.assertArrayEquals(new String[]{"IRI910", "54", "78", "7"}, lsIndexes.get(1));
		Assert.assertArrayEquals(new String[]{"IR23S_0", "107", "135", "7"}, lsIndexes.get(2));
		Assert.assertArrayEquals(new String[]{"IRIS_313-10", "163", "189", "7"}, lsIndexes.get(3));
		Assert.assertArrayEquals(new String[]{"IR_3-1r10", "217", "246", "7"}, lsIndexes.get(4));
	}
	
	@Test
	public void testAlleleGetFrq() {
		Allele allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("A"); allele.setAllele2("A");
		allele.setIsRefMajor(true);
		assertEquals(1, allele.getFrq());
		
		allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("T"); allele.setAllele2("T");
		allele.setIsRefMajor(true);
		assertEquals(-1, allele.getFrq());
		
		allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("A"); allele.setAllele2("A");
		allele.setIsRefMajor(false);
		assertEquals(-1, allele.getFrq());
		
		allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("T"); allele.setAllele2("T");
		allele.setIsRefMajor(false);
		assertEquals(1, allele.getFrq());
		
		allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("A"); allele.setAllele2("T");
		allele.setIsRefMajor(true);
		assertEquals(0, allele.getFrq());
		
		allele = new Allele();
		allele.setRef("A"); allele.setAlt("T");
		allele.setAllele1("A"); allele.setAllele2("T");
		allele.setIsRefMajor(false);
		assertEquals(0, allele.getFrq());
	}
	
}
