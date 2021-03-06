package com.novelbio.database.domain.species;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.SpeciesFileExtract;
import com.novelbio.database.domain.species.SpeciesIndexMappingMaker;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexMapSplice;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexTophat;
import com.novelbio.database.model.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class TestSpeciesIndexMappingMaker {
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
		
		FileOperate.copyFile("src/test/resources/test_file/reference/arabidopsis_sub/chrAll.fa", chrFile, true);
		FileOperate.copyFile("src/test/resources/test_file/reference/testTrinity.fa", refseqAllIso, true);
		FileOperate.copyFile("src/test/resources/test_file/reference/testTrinitySub.fa", refseqOneIso, true);

		speciesFile.setChromSeq("aaa.fa");
		speciesFile.setTaxID(9606);
		speciesFile.setVersion("GRCh38");
		speciesFile.setRefSeqFileName("refAllIso.fa", true, false);
		speciesFile.setRefSeqFileName("refOneIso.fa", false, false);
		speciesFile.setRefSeqFileName("proteinAllIso_GRCh38.fa", true, true);
		speciesFile.setRefSeqFileName("proteinOneIso_GRCh38.fa", false, true);
		SpeciesFileExtract speciesFileExtract = new SpeciesFileExtract(speciesFile);
		speciesFileExtract.indexChrFile();
		speciesFileExtract.indexRefseqFile();
	}
	
	@Test
	public void testIndexChrMake() {
		SpeciesIndexMappingMaker speciesIndexMappingMaker = new SpeciesIndexMappingMaker(speciesFile);
		speciesIndexMappingMaker.setGenomePath(parentPath);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.bowtie);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.bowtie2);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.bwa_aln);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.tophat);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.mapsplice);
		testIndexChrMake(speciesIndexMappingMaker, SoftWare.hisat2);
	}
	
	private void testIndexChrMake(SpeciesIndexMappingMaker speciesIndexMappingMaker, SoftWare software) {
		speciesIndexMappingMaker.makeIndexChr(software.toString());
		
		String chrFile = speciesIndexMappingMaker.getSequenceIndex(EnumSpeciesFile.chromSeqFile, software.toString());
		
		String softwareName = software.toString();
		if (softwareName.startsWith("bwa_")) {
			softwareName = "bwa";
        }
		Assert.assertEquals(chrFile, "src/test/resources/test_file/reference/" +
				SpeciesIndexMappingMaker.indexPath + softwareName + "/" + speciesFile.getTaxID() + "/" + speciesFile.getVersion() + "/" +
				FileOperate.addSep(SpeciesIndexMappingMaker.mapFile2IndexPath.get(EnumSpeciesFile.chromSeqFile)) + 
				FileOperate.getFileName(speciesFile.getChromSeqFile()));
		if (software == SoftWare.mapsplice) {
			Assert.assertTrue(FileOperate.isFileExistAndBigThan0(IndexMapSplice.modifyChrSeqName(chrFile)));
		} else {
			Assert.assertTrue(FileOperate.isFileExistAndBigThan0(chrFile));
		}

		IndexMappingMaker indexMappingMaker = IndexMappingMaker.createIndexMaker(software);
		if (software == SoftWare.tophat) {
			((IndexTophat)indexMappingMaker).setBowtieVersion(SoftWare.bowtie);
			indexMappingMaker.setChrIndex(chrFile);
			Assert.assertTrue(indexMappingMaker.isIndexFinished());
			((IndexTophat)indexMappingMaker).setBowtieVersion(SoftWare.bowtie2);
			indexMappingMaker.setChrIndex(chrFile);
			Assert.assertTrue(indexMappingMaker.isIndexFinished());
		} else {
			indexMappingMaker.setChrIndex(chrFile);
			Assert.assertTrue(indexMappingMaker.isIndexFinished());
		}
	}
	
	@After
	public void delete() {
		FileOperate.deleteFileFolder(parentPath + "species");
		FileOperate.deleteFileFolder(parentPath + "index");

	}
}
