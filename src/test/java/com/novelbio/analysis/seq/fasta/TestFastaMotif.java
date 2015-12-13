package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.analysis.seq.fasta.MotifFastaSearch.MotifLoc;

public class TestFastaMotif {
	
	@Test
	public void testFasta() {
		MotifFastaSearch fastaMotif = new MotifFastaSearch("src/test/resources/test_file/motif/seqMotif.fa");
		fastaMotif.setMotif("CGTCGCGC");
		fastaMotif.bufferSize = 45;
		fastaMotif.overlapSize = 9;
		
		fastaMotif.readSeq();
		Assert.assertEquals(60, fastaMotif.nowSize);
		
		List<MotifLoc> lsList = fastaMotif.searchMotif();
		Assert.assertEquals(18, lsList.get(0).getStart());
		Assert.assertEquals(1, lsList.size());
		Assert.assertEquals(35, fastaMotif.nowSize);
		Assert.assertEquals(25, fastaMotif.startSite);
		Assert.assertEquals("TTTTTTTTTTTTTTT", fastaMotif.lsSeq.get(0));
		Assert.assertEquals("TTTTTTTTTTTTTTTTTTTT", fastaMotif.lsSeq.get(1));
		
		//==
		fastaMotif.readSeq();
		Assert.assertEquals(49, fastaMotif.nowSize);
		lsList = fastaMotif.searchMotif();
		Assert.assertEquals(0, lsList.size());
		
		//==
		fastaMotif.readSeq();
		Assert.assertEquals(49, fastaMotif.nowSize);
		lsList = fastaMotif.searchMotif();
		Assert.assertEquals(107, lsList.get(0).getStart());
		Assert.assertEquals(132, lsList.get(1).getStart());
		Assert.assertEquals("Contig1", lsList.get(0).getRefId());
		Assert.assertEquals(1, fastaMotif.lsSeq.size());
		Assert.assertEquals("T", fastaMotif.lsSeq.get(0));
		
		//==
		fastaMotif.readSeq();
		Assert.assertEquals(61, fastaMotif.nowSize);
		lsList = fastaMotif.searchMotif();
		Assert.assertEquals(1, lsList.size());
		Assert.assertEquals(143, lsList.get(0).getStart());
		Assert.assertEquals(3, fastaMotif.lsSeq.size());
		Assert.assertEquals("TTTTTTTTTT", fastaMotif.lsSeq.get(0));
		Assert.assertEquals(50, fastaMotif.nowSize);

		//==
		fastaMotif.readSeq();
		Assert.assertEquals(49, fastaMotif.nowSize);
		lsList = fastaMotif.searchMotif();
		Assert.assertEquals(2, lsList.size());
		Assert.assertEquals(197, lsList.get(0).getStart());
		Assert.assertEquals(203, lsList.get(1).getStart());
		Assert.assertEquals(2, fastaMotif.lsSeq.size());
		Assert.assertEquals(30, fastaMotif.nowSize);
	}
	
	
	@Test
	public void testFasta2() {
		MotifFastaSearch fastaMotif = new MotifFastaSearch("src/test/resources/test_file/motif/seqMotif.fa");
		fastaMotif.setMotif("CGTCGCGC");
		fastaMotif.bufferSize = 45;
		fastaMotif.overlapSize = 9;
		
		List<MotifLoc> lsMotifLoc = new ArrayList<>();
		while (fastaMotif.isNotFinish()) {
			fastaMotif.readSeq();
			lsMotifLoc.addAll(fastaMotif.searchMotif());
		}
		int i = 0;
		Assert.assertEquals(12, lsMotifLoc.size());
		MotifLoc motifLoc = lsMotifLoc.get(i++);
		Assert.assertEquals("Contig1", motifLoc.getRefId());
		Assert.assertEquals(18, motifLoc.getStart());
		Assert.assertEquals(107, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(132, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(143, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(197, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(203, lsMotifLoc.get(i++).getStart());
		
		motifLoc = lsMotifLoc.get(i++);
		Assert.assertEquals("Contig2", motifLoc.getRefId());
		Assert.assertEquals(18, motifLoc.getStart());
		Assert.assertEquals(107, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(132, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(143, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(197, lsMotifLoc.get(i++).getStart());
		Assert.assertEquals(203, lsMotifLoc.get(i++).getStart());

		
	}
	
}
