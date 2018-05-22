package com.novelbio.analysis.seq.snphgvs;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.database.domain.species.Species;

import junit.framework.Assert;

public class TestSnpHgvspMove {
	// static GffChrAbs gffchrAbs;
	static GffHashGene gffHashGene;
	static SeqHashStub seqHash;

	@Test
	public void testMoveCisInsert() {
		setSeqHashInsertion();
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 35, "T", "TAT");
		snpRefAltInfo.initial(seqHash);
		SnpIsoHgvsp snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		int moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 53, "A", "AA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 53, "A", "AAAA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 74, "A", "AA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GGTG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GACG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GTG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 48, "A", "ACA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 42, "T", "TC");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
	}
	@Test
	public void testMoveTransInsert() {
		setSeqHashInsertion();
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 35, "T", "TAT");
		snpRefAltInfo.initial(seqHash);
		SnpIsoHgvsp snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		int moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 53, "A", "AA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 53, "A", "AAAA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 74, "A", "AA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GGTG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GACG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 41, "G", "GTG");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 48, "A", "ACA");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 42, "T", "TC");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(0, moveNum);
	}
	private static void setSeqHashInsertion() {
		seqHash = new SeqHashStub();
		String seq = "AAATGATTAT"//10
				+ "ATAAAGATTG"//20
				+ "TTACTGTAAA"//30
				+ "GGT ATG TTT C"//40
				+ "GTGGTGCAG G"
				+ "G AAG GAA AGC"
				+ "CAATGCGCA G"
				+ "GT TAG ATTTA"
				+ "AGGAAGAAAA"
				+ "CCTTTGGTTT";
		seqHash.setSeq(seq.replace(" ", ""));
	}
	
	private static void setSeqHashDeletion() {
		seqHash = new SeqHashStub();
		String seq = "AAATGATTAT"//10
				+ "ATAAAGATTG"//20
				+ "TTACTGTAAA"//30
				+ "GAT ATG GGT G"//40
				+ "GTGCCAGAG C"//50
				+ "G AAG AAG AGC"//60
				+ "GTAAGCTAG C"//70
				+ "TT TAG ATTTA"//80
				+ "AGGAAGAAAA"//90
				+ "CCTTTGGTTT";//100
		seqHash.setSeq(seq.replace(" ", ""));
	}
	@Test
	public void testMoveCisDeletion() {
		setSeqHashDeletion();
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 46, "AGA", "A");
		snpRefAltInfo.initial(seqHash);
		SnpIsoHgvsp snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		int moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 67, "TAGCT", "T");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(4, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 40, "GGTG", "G");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(4, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 55, "AAGA", "A");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 33, "TAT", "T");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoCis());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
	}
	@Test
	public void testMoveTransDeletion() {
		setSeqHashDeletion();
		SnpInfo snpRefAltInfo = new SnpInfo("chr1", 46, "AGA", "A");
		snpRefAltInfo.initial(seqHash);
		SnpIsoHgvsp snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		int moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 67, "TAGCT", "T");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(4, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 40, "GGTG", "G");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(4, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 55, "AAGA", "A");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(1, moveNum);
		
		snpRefAltInfo = new SnpInfo("chr1", 33, "TAT", "T");
		snpRefAltInfo.initial(seqHash);
		snpIsoHgvsp = SnpIsoHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, getIsoTrans());
		moveNum = snpIsoHgvsp.moveBeforeNum();
		assertEquals(2, moveNum);
	}
	private GffGeneIsoInfo getIsoCis() {
		GffGeneIsoInfo isoCis = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", null, GeneType.mRNA, true);
		//>---10-20------30-40-------50-60-------70-80----90-100-->
		isoCis.add(new ExonInfo( true, 10, 20));
		isoCis.add(new ExonInfo( true, 30, 40));
		isoCis.add(new ExonInfo( true, 50, 60));
		isoCis.add(new ExonInfo( true, 70,80));
		isoCis.add(new ExonInfo( true, 90,100));
		isoCis.setATG(34);
		isoCis.setUAG(75);
		return isoCis;
	}
	
	private GffGeneIsoInfo getIsoTrans() {
		GffGeneIsoInfo isoTrans = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneTrans", null, GeneType.mRNA, false);
		//<---10-20------30-40-------50-60-------70-80----90-100<
		isoTrans.add(new ExonInfo( false,90, 100));
		isoTrans.add(new ExonInfo( false, 70, 80));
		isoTrans.add(new ExonInfo( false, 50, 60));
		isoTrans.add(new ExonInfo( false, 30, 40));
		isoTrans.add(new ExonInfo( false, 10, 20));
		isoTrans.setATG(75);
		isoTrans.setUAG(34);
		return isoTrans;
	}
	

}
