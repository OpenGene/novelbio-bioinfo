package com.novelbio.analysis.seq.fasta;

import junit.framework.TestCase;

import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.domain.species.Species;

public class TestSeqHash extends TestCase {
	SeqHash seqHash;
	GffDetailGene gffDetailGene;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Species species = new Species(9606);
		seqHash = new SeqHash(FileHadoop.addHdfsHeadSymbol("/nbCloud/nbcplatform/genome/human/hg19_GRCh37/ChromFa/chrAll.fa"), " ");
		gffDetailGene = new GffDetailGene("chr1", "test", true);
	}
	
	@Test
	public void testGetSeqCis() {
		
		GffGeneIsoInfo gffGeneIsoInfoCis = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, true);
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50000, 50006));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50012, 50018));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50024, 50028));
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, true);
		assertEquals("TAAACAGgttaaTCGCCACgacatAGTAG", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, false);
		assertEquals("TAAACAGTCGCCACAGTAG", seqFasta.toString());
		assertEquals(4, gffGeneIsoInfoCis.getCod2TSSmRNA(50004));
		assertEquals(9, gffGeneIsoInfoCis.getCod2TSSmRNA(50014));
		assertEquals(17, gffGeneIsoInfoCis.getCod2TSSmRNA(50027));
		System.out.println("testGetSeqCis");
	}
	@Test
	public void testGetSeqTrans() {
		GffGeneIsoInfo gffGeneIsoInfoTrans = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, false);
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50024, 50028));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50012, 50018));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50000, 50006));
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, true);
		assertEquals("CTACTatgtcGTGGCGAttaacCTGTTTA", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, false);
		assertEquals("CTACTGTGGCGACTGTTTA", seqFasta.toString());
		assertEquals(14, gffGeneIsoInfoTrans.getCod2TSSmRNA(50004));
		assertEquals(9, gffGeneIsoInfoTrans.getCod2TSSmRNA(50014));
		assertEquals(2, gffGeneIsoInfoTrans.getCod2TSSmRNA(50026));
		System.out.println("testGetSeqTrans");
	}
	
	@Test
	public void testGetSeqCisUnNormal() {
		GffGeneIsoInfo gffGeneIsoInfoCis = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, true);
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50000, 50006));
		gffGeneIsoInfoCis.add(new ExonInfo(false, 50012, 50018));
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50024, 50028));
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, true);
		assertEquals("TAAACAGgttaaGTGGCGAgacatAGTAG", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, false);
		assertEquals("TAAACAGGTGGCGAAGTAG", seqFasta.toString());
		assertEquals(4, gffGeneIsoInfoCis.getCod2TSSmRNA(50004));
		assertEquals(11, gffGeneIsoInfoCis.getCod2TSSmRNA(50014));
		assertEquals(17, gffGeneIsoInfoCis.getCod2TSSmRNA(50027));
		
		
		gffGeneIsoInfoCis = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, true);
		gffGeneIsoInfoCis.add(new ExonInfo(true, 50000, 50006));
		gffGeneIsoInfoCis.add(new ExonInfo(false, 50012, 50018));
		gffGeneIsoInfoCis.add(new ExonInfo(false, 50024, 50028));
		seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, true);
		assertEquals("TAAACAGgttaaGTGGCGAgacatCTACT", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoCis, false);
		assertEquals("TAAACAGGTGGCGACTACT", seqFasta.toString());
		assertEquals(4, gffGeneIsoInfoCis.getCod2TSSmRNA(50004));
		assertEquals(11, gffGeneIsoInfoCis.getCod2TSSmRNA(50014));
		assertEquals(16, gffGeneIsoInfoCis.getCod2TSSmRNA(50026));
		
		
		System.out.println("testGetSeqCisUnNormal");
	}
	
	@Test
	public void testGetSeqTransUnNormal() {
		GffGeneIsoInfo gffGeneIsoInfoTrans = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, false);
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50024, 50028));
		gffGeneIsoInfoTrans.add(new ExonInfo(true, 50012, 50018));
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50000, 50006));
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, true);
		assertEquals("CTACTatgtcTCGCCACttaacCTGTTTA", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, false);
		assertEquals("CTACTTCGCCACCTGTTTA", seqFasta.toString());
		assertEquals(14, gffGeneIsoInfoTrans.getCod2TSSmRNA(50004));
		assertEquals(7, gffGeneIsoInfoTrans.getCod2TSSmRNA(50014));
		assertEquals(2, gffGeneIsoInfoTrans.getCod2TSSmRNA(50026));
		
		System.out.println("testGetSeqTransUnNormal");
		
		gffGeneIsoInfoTrans = GffGeneIsoInfo.createGffGeneIso("", "test", gffDetailGene, GeneType.mRNA, false);
		gffGeneIsoInfoTrans.add(new ExonInfo(false, 50024, 50028));
		gffGeneIsoInfoTrans.add(new ExonInfo(true, 50012, 50018));
		gffGeneIsoInfoTrans.add(new ExonInfo(true, 50000, 50006));
		
		seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, true);
		assertEquals("CTACTatgtcTCGCCACttaacTAAACAG", seqFasta.toString());
		seqFasta = seqHash.getSeq(gffGeneIsoInfoTrans, false);
		assertEquals("CTACTTCGCCACTAAACAG", seqFasta.toString());
		assertEquals(16, gffGeneIsoInfoTrans.getCod2TSSmRNA(50004));
		assertEquals(7, gffGeneIsoInfoTrans.getCod2TSSmRNA(50014));
		assertEquals(2, gffGeneIsoInfoTrans.getCod2TSSmRNA(50026));
		System.out.println("testGetSeqTransUnNormal");
	}
}
