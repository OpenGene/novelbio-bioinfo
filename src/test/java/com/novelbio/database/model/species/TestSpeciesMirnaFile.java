package com.novelbio.database.model.species;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.TaxInfo;

public class TestSpeciesMirnaFile {
	String parentPath = "src/test/resources/test_file/reference/";
	TaxInfo taxInfo;
	
	@Before
	public void initial() {
		taxInfo = new TaxInfo();
		taxInfo.setTaxID(9606);
		taxInfo.setLatin("Homo sapiens");
	}
	
	@Test
	public void testGetMiRNA() {
		taxInfo.setIsHaveMiRNA(true);
		SpeciesMirnaFile speciesMirnaFile = new SpeciesMirnaFile(taxInfo);
		speciesMirnaFile.setParentPathAndMirnaFile(parentPath, "/home/novelbio/NBCresource/test/miRNA.dat");
		String mirHairpin = speciesMirnaFile.getMiRNAhairpinFile();
		String mirMature = speciesMirnaFile.getMiRNAmatureFile();
		Assert.assertEquals(parentPath + "miRNA/9606/miRNA.fa", mirMature);
		Assert.assertEquals(parentPath + "miRNA/9606/miRNAhairpin.fa", mirHairpin);
	}
	
	@Test
	public void testExtractMiRNA() {
		SpeciesMirnaFile speciesMirnaFile = new SpeciesMirnaFile(taxInfo);
		speciesMirnaFile.setParentPathAndMirnaFile(parentPath, "/home/novelbio/NBCresource/test/miRNA.dat");
		speciesMirnaFile.isHaveAndExtractMiRNA();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(parentPath + "miRNA/9606/miRNA.fa"));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(parentPath + "miRNA/9606/miRNAhairpin.fa"));
		Assert.assertTrue(taxInfo.isHaveMiRNA());
	}
	
	@After
	public void delete() {
		FileOperate.deleteFileFolder(parentPath + "miRNA");
	}
}
