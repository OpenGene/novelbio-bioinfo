package com.novelbio.test.analysis.seq.genomeNew.getChrSequence;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqfastaStatisticsCDS;
import com.novelbio.analysis.seq.fastq.FastQRecord;

public class TestFastQRecord extends TestCase{
	FastQRecord fastQRecord;
	protected void setUp() throws Exception {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertSeq();
	}
	
	private void assertSeq() {
		fastQRecord = new FastQRecord();
		fastQRecord.setFastqOffset(33);
		fastQRecord.setName("@HWI-ST1033:92:D0VACACXX:3:1101:1372:1938 2:N:0:GTGAAA");
		fastQRecord.setSeq("AGGATTTGCACTTTTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTTTAGTCACCAAACAA");
		fastQRecord.setFastaQuality("@@@;DD>DD>FFHGI<CFAF<AHGFHFACIHIBHIGDFHCGDB?D9D:BDCGCGEGGIFHCHGGIF@G@CC>=ACEFDFE>AC3@CC;;;>?<ABB");
		//Æ¥Åä
		fastQRecord.trimAdaptor("AGGATTTGCACTT", "TAGTCACCAAACAA", -1, -1, 3, 2, 30);
		assertEquals("TTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTT", fastQRecord.getSeqFasta().toString());
		
		
		fastQRecord = new FastQRecord();
		fastQRecord.setFastqOffset(33);
		fastQRecord.setName("@HWI-ST1033:92:D0VACACXX:3:1101:1372:1938 2:N:0:GTGAAA");
		fastQRecord.setSeq("AGGATTTGCACTTTTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTTTAGTCACCAAACAA");
		fastQRecord.setFastaQuality("@@@;DD>DD>FFHGI<CFAF<AHGFHFACIHIBHIGDFHCGDB?D9D:BDCGCGEGGIFHCHGGIF@G@CC>=ACEFDFE>AC3@CC;;;>?<ABB");
		//´íÅä
		fastQRecord.trimAdaptor("AGGAGTTGTACTT", "TAGTCGCCAAACAA", -1, -1, 3, 2, 30);
		assertEquals("TTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTT", fastQRecord.getSeqFasta().toString());
		
		fastQRecord = new FastQRecord();
		fastQRecord.setFastqOffset(33);
		fastQRecord.setName("@HWI-ST1033:92:D0VACACXX:3:1101:1372:1938 2:N:0:GTGAAA");
		fastQRecord.setSeq("AGGATTTGCACTTTTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTTTAGTCACCAAACAA");
		fastQRecord.setFastaQuality("@@@;DD>DD>FFHGI<CFAF<AHGFHFACIHIBHIGDFHCGDB?D9D:BDCGCGEGGIFHCHGGIF@G@CC>=ACEFDFE>AC3@CC;;;>?<ABB");
		//²åÈëÈ±Ê§
		fastQRecord.trimAdaptor("ACGATTGCACTTG", "TAGTCACACAACAA", -1, -1, 3, 2, 30);
		assertEquals("TTAGGAGTGATTGTCAAGGCATATTTCATATGAGTGATATGGATAAACCGATAAACCCTCCAGCAATTT", fastQRecord.getSeqFasta().toString());
		
	}
}
