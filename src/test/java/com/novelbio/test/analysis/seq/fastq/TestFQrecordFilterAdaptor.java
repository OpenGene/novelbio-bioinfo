package com.novelbio.test.analysis.seq.fastq;

import com.novelbio.analysis.seq.fastq.FQrecordFilterAdaptor;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;

import junit.framework.TestCase;

public class TestFQrecordFilterAdaptor extends TestCase {
	FastQRecord fastQRecord;
	FQrecordFilterAdaptor fQrecordFilterAdaptor = new FQrecordFilterAdaptor();
	@Override
	protected void setUp() throws Exception {
		fastQRecord = new FastQRecord();
		fastQRecord.setName("novelbio");
		fastQRecord.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fastQRecord.setSeq("CCTTCGATAGCTCAGCTGGTAGAGCCTGTAGGCACC");
		fastQRecord.setFastaQuality("CCCFFFFFHHHGHJGGIJJJHHIFHIIJIJIIIIJJ");
		fQrecordFilterAdaptor.setTrimMinLen(10);
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testFilterRightAdaptor() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorR("CTGTAGGCACCATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterRightAdaptorMisMatch() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorR("CTGTTGGGACGATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterRightAdaptorGap() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorR("CTGAGGCACCATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterLeftAdaptor() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorL("AGTCCTTCGATAGCTCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterLeftAdaptorMisMatch() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorL("AGTCCTTCGTTACCTCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterLeftAdaptorGap() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorL("AGTCCTTCGAAGCCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testFilterAdaptor() {
		fQrecordFilterAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordFilterAdaptor.setSeqAdaptorL("AGTCCTTCGAAGCCAGC");
		fQrecordFilterAdaptor.setSeqAdaptorR("CTGTAGGCACCATCAAT");
		fQrecordFilterAdaptor.setTrimMinLen(6);
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean isFiltered = fQrecordFilterAdaptor.filter(fastQRecord2);
		assertEquals(isFiltered, true);
		assertEquals("TGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFH", fastQRecord2.getSeqQuality());
	}
}
