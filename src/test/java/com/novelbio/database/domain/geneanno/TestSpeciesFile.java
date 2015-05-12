package com.novelbio.database.domain.geneanno;

import junit.framework.TestCase;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class TestSpeciesFile extends TestCase {
	SpeciesFile speciesFile = new SpeciesFile();
	
	public void test() {
		Species species = new Species(9606);
		FileOperate.linkFile("/media/winE/NBCsource/species/9606/hg19_GRCh37/ChromFa/chrAll.fa",
				"/media/winE/test/speciesFile_genome/species/9606/hg19_GRCh37/ChromFa/chrAll.fa", true);
		
		SpeciesFile.pathParent = "/media/winE/test/speciesFile_genome/";
		SpeciesFile speciesFile = species.getMapVersion2Species().get("hg19_grch37");
		String seqName = speciesFile.getRefSeqFile(true, false);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refrna_all_iso/rna_modify.fa", seqName);
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(seqName, 0));
	
		seqName = speciesFile.getRefSeqFile(false, false);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refrna_one_iso/rnaOneIso_hg19_GRCh37.fa", seqName);
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(seqName, 0));
		
		seqName = speciesFile.getRefSeqFile(false, true);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refprotein_one_iso/proteinOneIso_hg19_GRCh37.fa", seqName);
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(seqName, 0));
		
		seqName = speciesFile.getRefSeqFile(true, true);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refprotein_all_iso/proteinAllIso_hg19_GRCh37.fa", seqName);
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(seqName, 0));
		
		String indexBwa = speciesFile.getIndexRefseqAndCp(SoftWare.bwa_aln, true);
		assertEquals(SpeciesFile.pathParent + "index/bwa/9606/hg19_GRCh37/Ref_AllIso_Index/rna_modify.fa", indexBwa);

//		speciesFile.deleteChromAll();
		
		
	}
	
}
