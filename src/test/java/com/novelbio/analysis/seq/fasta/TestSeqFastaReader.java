package com.novelbio.analysis.seq.fasta;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;


public class TestSeqFastaReader {
	
	@Test
	public void testCreatSeqFasta() {
		List<String> lsSeqFasta = new ArrayList<>();
		lsSeqFasta.add(">fastaTest");
		lsSeqFasta.add("fasrterse234rfds");
		lsSeqFasta.add("nny\t\nadtdc");
		lsSeqFasta.add("");
		lsSeqFasta.add("fse aser  ser   ");
		lsSeqFasta.add("");
		
		SeqFasta seqFasta = SeqFastaReader.creatSeqFasta(lsSeqFasta);
		
		assertEquals("fastaTest", seqFasta.getSeqName());
		assertEquals("fasrterserfdsnnyadtdcfseaserser", seqFasta.toString());
	}
}
