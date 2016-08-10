package com.novelbio.database.domain.geneanno;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.mapping.IndexMappingMaker;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.Species.SeqType;
import com.novelbio.database.model.species.SpeciesIndexMappingMaker;

public class TestSpeciesFile extends TestCase {
	SpeciesFile speciesFile = new SpeciesFile();
	
	public void test() {
		speciesFile.setChromSeq("chrAll.fa");
		speciesFile.setTaxID(9606);
		speciesFile.setVersion("hg19_GRCh37");
		speciesFile.setRefSeqFileName("rnaAllIso_hg19_GRCh37.fa", true, false);
		speciesFile.setRefSeqFileName("rnaOneIso_hg19_GRCh37.fa", false, false);
		speciesFile.setRefSeqFileName("proteinAllIso_hg19_GRCh37.fa", true, true);
		speciesFile.setRefSeqFileName("proteinOneIso_hg19_GRCh37.fa", false, true);
		
		String seqName = speciesFile.getRefSeqFile(true, false);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refrna_all_iso/rnaAllIso_hg19_GRCh37.fa", seqName);
	
		seqName = speciesFile.getRefSeqFile(false, false);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refrna_one_iso/rnaOneIso_hg19_GRCh37.fa", seqName);
		
		seqName = speciesFile.getRefSeqFile(false, true);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refprotein_one_iso/proteinOneIso_hg19_GRCh37.fa", seqName);
		
		seqName = speciesFile.getRefSeqFile(true, true);
		assertEquals(SpeciesFile.pathParent + "species/9606/hg19_GRCh37/refprotein_all_iso/proteinAllIso_hg19_GRCh37.fa", seqName);
		
		SpeciesIndexMappingMaker speciesIndexMappingMake = new SpeciesIndexMappingMaker(speciesFile);
		String indexBwa = speciesIndexMappingMake.getSequenceIndex(EnumSpeciesFile.chromSeqFile, SoftWare.bwa_aln.toString());
		assertEquals(SpeciesFile.pathParent + "index/bwa/9606/hg19_GRCh37/Chr_Index/chrAll.fa", indexBwa);
		
		String indexBowtie = speciesIndexMappingMake.getSequenceIndex(EnumSpeciesFile.chromSeqFile, SoftWare.bowtie.toString());
		assertEquals(SpeciesFile.pathParent + "index/bowtie/9606/hg19_GRCh37/Chr_Index/chrAll.fa", indexBowtie);
		
		IndexMappingMaker indexBowtieMaker = IndexMappingMaker.createIndexMaker(SoftWare.bowtie);
		indexBowtieMaker.setChrIndex(indexBowtie);
		assertEquals(SpeciesFile.pathParent + "index/bowtie/9606/hg19_GRCh37/Chr_Index/chrAll", indexBowtieMaker.getIndexName());
		
		Species species = new Species();
		species.getMapVersion2Species().put("hg19_GRCh37".toLowerCase(), speciesFile);
		species.setVersion("hg19_GRCh37");
		assertEquals(SpeciesFile.pathParent + "index/bwa/9606/hg19_GRCh37/Chr_Index/chrAll.fa", species.getIndexChr(SoftWare.bwa_aln));
		assertEquals(SpeciesFile.pathParent + "index/bwa/9606/hg19_GRCh37/Ref_OneIso_Index/rnaOneIso_hg19_GRCh37.fa", species.getSeqIndex(SeqType.refseqOneIso, SoftWare.bwa_aln.toString()));
	}
	
}
