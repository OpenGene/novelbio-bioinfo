package com.novelbio.analysis.seq.genome.gffOperate.trfrna;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestTrfRNAList {
	
	String trfFile = PathDetail.getTmpPathWithSep() + "trfRNA_test_tmp.txt";
	@Before
	public void prepareFiles() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(trfFile, true);
		txtWrite.writefileln("5001, c.elegans, trf-5, chrI-2272439-2272369, chrI.trna75-GlyGCC, GSM632211-0-Heatshock-WT-Day1;GSM632210-0-WT-day1;GSM632212-0-PA-WT-Day1;GSM632213-1-L1-arrest-WT-Day1;, 5' GCATCGGTGGTTCAGTGGTAGAATGCTCGCCTGCCACGCGGGCGGCCCGGGTTCGATTCCCGGTCGATGCACCA 3', Start:1 - End:26, GCATCGGTGGTTCAGTGGTAGAATGC ");
		txtWrite.writefileln("5001a, mouse, trf-5, chr13-21998980-21999051, chr13.trna82-MetCAT, GSM849855-6-GSM849855_TotalRNA_withoutAgo;GSM849857-0-GSM849857_TotalRNA_withAgo2;, 5' AGCAGAGTGGCGCAGCGGAAGCGTGCTGGGCCCATAACCCAGAGGTCGATAGATCGAAACCATCCTCTGCTACCA 3', Start:1 - End:19, AGCAGAGTGGCGCAGCGGA ");
		txtWrite.writefileln("5001a, human, trf-5, chr8-67026223-67026311, chr8.trna5-TyrGTA, SRR207113-0-HeLa-WholeCell-AGO12IP;SRR207116-0-HeLa-Nucleus;, 5' CCTTCGATAGCTCAGCTGGTAGAGCGGAGGACTGTAGGCGCGCGCCCGTGGCCATCCTTAGGTCGCTGGTTCGATTCCGGCTCGAAGGACCA 3', Start:1 - End:15, CCTTCGATAGCTCAG ");
		txtWrite.writefileln("5001a, human, trf-5, chr6-26577332-26577420, chr6.trna16-TyrGTA, SRR207113-0-HeLa-WholeCell-AGO12IP;SRR207116-0-HeLa-Nucleus;, 5' CCTTCGATAGCTCAGTTGGTAGAGCGGAGGACTGTAGGCTCATTAAGCAAGGTATCCTTAGGTCGCTGGTTCGAATCCGGCTCGGAGGACCA 3', Start:1 - End:15, CCTTCGATAGCTCAG ");
		txtWrite.writefileln("5001a, human, trf-5, chr14-21151432-21151520, chr14.trna5-TyrGTA, SRR207113-0-HeLa-WholeCell-AGO12IP;SRR207116-0-HeLa-Nucleus;, 5' CCTTCGATAGCTCAGCTGGTAGAGCGGAGGACTGTAGTACTTAATGTGTGGTCATCCTTAGGTCGCTGGTTCGATTCCGGCTCGAAGGACCA 3', Start:1 - End:15, CCTTCGATAGCTCAG ");
		txtWrite.close();
	}
	
	@After
	public void deleteTrfFile() {
		FileOperate.deleteFileFolder(trfFile);
	}
	
	@Test
	public void testMiRNAList() {
		TrfRNAList trfRNAList = new TrfRNAList();
		trfRNAList.setSpeciesName("human");
		trfRNAList.ReadGffarray(trfFile);
		
		Assert.assertEquals(3, trfRNAList.getMapChrID2LsGff().size());

		Assert.assertEquals("chr14.trna5-TyrGTA", trfRNAList.searchElement("chr14.trna5-TyrGTA", 1, 15).getNameSingle());
		Assert.assertEquals(null, trfRNAList.searchElement("chr14.trna5-TyrGTA", 16, 18));
		Assert.assertEquals("CCTTCGATAGCTCAG", trfRNAList.searchLOC("chr6.trna16-TyrGTA").getSeq().toString());

	}
	
}
