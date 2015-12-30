package com.novelbio.database.model.species;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.EnumSpeciesFile;
import com.novelbio.database.domain.geneanno.SpeciesFile;

public class TestSpeciesFileFaidx {
	SpeciesFile speciesFile = new SpeciesFile();
	String chrFile;
	String refseqAllIso;
	String refseqOneIso;
	String parentPath = "src/test/resources/test_file/reference/";
	@Before
	public void initialSpeciesFile() {
		chrFile = parentPath + "species/9606/GRCh38/" + EnumSpeciesFile.chromSeqFile.getPathNode() + "/aaa.fa";
		refseqAllIso = parentPath + "species/9606/GRCh38/" + EnumSpeciesFile.refseqAllIsoRNA.getPathNode() + "/refAllIso.fa";
		refseqOneIso = parentPath + "species/9606/GRCh38/" + EnumSpeciesFile.refseqOneIsoRNA.getPathNode() + "/refOneIso.fa";

		SpeciesFile.pathParent = parentPath;
		FileOperate.createFolders(FileOperate.getPathName(chrFile));
		FileOperate.createFolders(FileOperate.getPathName(refseqAllIso));
		FileOperate.createFolders(FileOperate.getPathName(refseqOneIso));
		
		FileOperate.copyFile("src/test/resources/test_file/reference/ara/chrAll.fa", chrFile, true);
		FileOperate.copyFile("src/test/resources/test_file/reference/testTrinity.fa", refseqAllIso, true);
		FileOperate.copyFile("src/test/resources/test_file/reference/testTrinitySub.fa", refseqOneIso, true);

		speciesFile.setChromSeq("aaa.fa");
		speciesFile.setTaxID(9606);
		speciesFile.setVersion("GRCh38");
		speciesFile.setRefSeqFileName("refAllIso.fa", true, false);
		speciesFile.setRefSeqFileName("refOneIso.fa", false, false);
		speciesFile.setRefSeqFileName("proteinAllIso_GRCh38.fa", true, true);
		speciesFile.setRefSeqFileName("proteinOneIso_GRCh38.fa", false, true);
		
		speciesFile = PowerMockito.spy(speciesFile);
		try {
			PowerMockito.doAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocation) throws Throwable {
					System.out.println("test");
					return null;
                }
			}).when(speciesFile, "save");
		} catch (Exception e) {
			
        }
		
	}
	
	@Test
	public void testIndexMake() {
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.indexChrFile();
		speciesFileExtract.indexRefseqFile();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(chrFile + ".fai"));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(FileOperate.changeFileSuffix(chrFile, "", "dict")));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(refseqAllIso + ".fai"));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(refseqOneIso + ".fai"));
	}
	
	@After
	public void delete() {
		FileOperate.DeleteFileFolder(parentPath + "species");
	}
}
