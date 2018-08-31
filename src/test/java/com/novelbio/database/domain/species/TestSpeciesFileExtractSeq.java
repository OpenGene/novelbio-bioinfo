package com.novelbio.database.domain.species;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.database.domain.species.SpeciesFileExtract;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;

public class TestSpeciesFileExtractSeq {
	private static Logger logger = LoggerFactory.getLogger(TestSpeciesFileExtractSeq.class);
	
	SpeciesFile speciesFile = new SpeciesFile();
	String chrFile;
	String gffFile;
	String parentPath = "src/test/resources/test_file/reference/";
	
	int saveNum = 0;
	@Before
	public void initialSpeciesFile() {
		chrFile = parentPath + "species/9606/GRCh38/" + EnumSpeciesFile.chromSeqFile.getPathNode() + "/aaa.fa";
		gffFile = parentPath + "species/9606/GRCh38/" + EnumSpeciesFile.gffGeneFile.getPathNode() + "/aaa.gff.gz";
		SpeciesFile.pathParent = parentPath;
		FileOperate.createFolders(FileOperate.getPathName(chrFile));
		FileOperate.createFolders(FileOperate.getPathName(gffFile));

		FileOperate.copyFile("src/test/resources/test_file/reference/arabidopsis_sub/chrAll.fa", chrFile, true);
		FileOperate.copyFile("src/test/resources/test_file/reference/arabidopsis_sub/TAIR10_Gff3_simple.gff.gz", gffFile, true);

		speciesFile.setChromSeq("aaa.fa");
		speciesFile.addGffDB2TypeFile("test", GffType.GFF3, FileOperate.getFileName(gffFile));
		speciesFile.setTaxID(9606);
		speciesFile.setVersion("GRCh38");
		
		speciesFile = PowerMockito.spy(speciesFile);
		try {
			PowerMockito.doAnswer(new Answer<String>() {
				@Override
				public String answer(InvocationOnMock invocation) throws Throwable {
					saveNum++;
					logger.info("saveNum {}", saveNum);
					return null;
                }
			}).when(speciesFile, "save");
		} catch (Exception e) {
			
        }
	}
	
	@Test
	public void testExtractGtf() {
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.extractGtf();
		String gtfFile = GffHashGene.convertNameToOtherFile(gffFile, GffType.GTF);
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(gtfFile));
		GffHashGene gffHashGene = new GffHashGene(gtfFile);
		GffHashGene gffHashGene2 = new GffHashGene(gffFile);
		Assert.assertEquals(gffHashGene.getChrID2LengthForRNAseq().keySet(), gffHashGene2.getChrID2LengthForRNAseq().keySet());
	}
	
	@Test
	public void testExtractRefAndGene2Iso() {
		saveNum = 0;
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.extractRefSeq();
		Assert.assertEquals(4, saveNum);
		String fileProAll = speciesFile.getRefSeqFile(true, true);
		String fileRefAll = speciesFile.getRefSeqFile(true, false);
		String fileProOne = speciesFile.getRefSeqFile(false, true);
		String fileRefOne = speciesFile.getRefSeqFile(false, false);

		String parentPathRef = parentPath + "species/9606/GRCh38/"; 
		Assert.assertEquals(parentPathRef + FileOperate.addSep(EnumSpeciesFile.refseqAllIsoPro.getPathNode()) 
				+ SpeciesFileExtract.getRefSeqRegularName(speciesFile.getVersion(), true, true), fileProAll);
		Assert.assertEquals(parentPathRef + FileOperate.addSep(EnumSpeciesFile.refseqAllIsoRNA.getPathNode()) 
				+ SpeciesFileExtract.getRefSeqRegularName(speciesFile.getVersion(), true, false), fileRefAll);
		Assert.assertEquals(parentPathRef + FileOperate.addSep(EnumSpeciesFile.refseqOneIsoPro.getPathNode())
				+ SpeciesFileExtract.getRefSeqRegularName(speciesFile.getVersion(), false, true), fileProOne);
		Assert.assertEquals(parentPathRef + FileOperate.addSep(EnumSpeciesFile.refseqOneIsoRNA.getPathNode())
				+ SpeciesFileExtract.getRefSeqRegularName(speciesFile.getVersion(), false, false), fileRefOne);
		
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(fileProAll));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(fileRefAll));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(fileProOne));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(fileRefOne));
		
		//TODO 初步就判断了文件是否存在，回头需要补上里面的内容
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(SpeciesFileExtract.getRefrna_Gene2Iso(fileRefAll)));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(SpeciesFileExtract.getRefrna_Gene2Iso(fileRefOne)));
	}
	
//	@Test
	public void testRfamFile() {
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.setRfamSeqFile("/home/novelbio/NBCresource/test/Rfam.fasta");
		speciesFileExtract.extractRfamFile();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(speciesFile.getRfamFile(true)));
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(speciesFile.getRfamFile(false)));
	}
	
	@After
	public void delete() {
		FileOperate.deleteFileFolder(parentPath + "species");
	}
}
