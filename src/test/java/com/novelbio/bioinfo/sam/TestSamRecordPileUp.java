package com.novelbio.bioinfo.sam;

import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordFactory;

import java.util.ArrayList;

import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;
import com.novelbio.bioinfo.sam.SamRecordPileUp;

import junit.framework.TestCase;

public class TestSamRecordPileUp extends TestCase{
	SamFile samFile;
	ArrayList<SamRecord> lsSamRecords;
	SamRecordPileUp samRecordPileUp = new SamRecordPileUp();
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		samFile = new SamFile("/media/winF/NBC/Project/RNASeq_Snp_WJ120725/test.txt");
		lsSamRecords = new ArrayList<SamRecord>();
		int i = 0;
		for (SamRecord samRecord : samFile.readLines()) {
			lsSamRecords.add(samRecord);
			i++;
			if (i > 100) {
				break;
			}
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testRecordEquals() {
		samRecordPileUp = new SamRecordPileUp();
		
		SamRecord samRecord = lsSamRecords.get(0);
		samRecordPileUp.setSamRecord(samRecord);
		samRecordPileUp.addBase("ACTGTTGGGGAGGCAGCTGTAACTCAAAGCCTTAGCCTCTGTTCCCACGAAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTG");
		System.out.println(samRecord.isCis5to3());
		assertEquals("^!.", samRecordPileUp.getSequence(13169));
		samRecordPileUp.pollBase();
		assertEquals(".", samRecordPileUp.getSequence(13170));
	}
	public void testRecordDeletion() {
		samRecordPileUp = new SamRecordPileUp();
		
		SamRecord samRecord = lsSamRecords.get(0);
		samRecordPileUp.setSamRecord(samRecord);
		samRecordPileUp.addBase("ACTGTTGGGGAGGCAGCTGTAACTCAAAGCCTTAGCCTCTGTTCCCACGAAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTG");
		assertEquals("^!.", samRecordPileUp.getSequence(13169));
		for (int i = 0; i < 9; i++) {
			samRecordPileUp.pollBase();
		}
		assertEquals(".-3AGG", samRecordPileUp.getSequence(13178));
	}
	public void testRecordInsert() {
		samRecordPileUp = new SamRecordPileUp();
		
		SamRecord samRecord = lsSamRecords.get(0);
		samRecordPileUp.setSamRecord(samRecord);
		samRecordPileUp.addBase("ACTGTTGGGGAGGCAGCTGTAACTCAAAGCCTTAGCCTCTGTTCCCACGAAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTG");
		assertEquals("^!.", samRecordPileUp.getSequence(13169));
		for (int i = 0; i < 18; i++) {
			samRecordPileUp.pollBase();
		}
		assertEquals("C+4TATT", samRecordPileUp.getSequence(13187));
		
		samRecordPileUp.pollBase();
		assertEquals("C", samRecordPileUp.getSequence(13188));
	}
	
	public void testRecordMisMatch() {
		samRecordPileUp = new SamRecordPileUp();
		
		SamRecord samRecord = lsSamRecords.get(0);
		samRecordPileUp.setSamRecord(samRecord);
		samRecordPileUp.addBase("ACTGTTGGGGAGGCAGCTGTAACTCAAAGCCTTAGCCTCTGTTCCCACGAAGGCAGGGCCATCAGGCACCAAAGGGATTCTGCCAGCATAGTGCTCCTG");
		assertEquals("^!.", samRecordPileUp.getSequence(13169));
		for (int i = 0; i < 23; i++) {
			samRecordPileUp.pollBase();
		}
		assertEquals("A", samRecordPileUp.getSequence(13192));
		samRecordPileUp.pollBase();
		samRecordPileUp.pollBase();
	}
}
