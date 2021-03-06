package com.novelbio.bioinfo.fastq;

import com.novelbio.bioinfo.fastq.FQrecordFilterAdaptor;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQRecord;

import junit.framework.TestCase;

public class TestFQrecordFilterAdaptor extends TestCase {
	FastQRecord fastQRecord;
	FQrecordFilterAdaptor fQrecordcopeReadsAdaptor = new FQrecordFilterAdaptor();
	@Override
	protected void setUp() throws Exception {
		fastQRecord = new FastQRecord();
		fastQRecord.setName("novelbio");
		fastQRecord.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fastQRecord.setSeq("CCTTCGATAGCTCAGCTGGTAGAGCCTGTAGGCACC");
		fastQRecord.setFastaQuality("CCCFFFFFHHHGHJGGIJJJHHIFHIIJIJIIIIJJ");
		fQrecordcopeReadsAdaptor.setTrimMinLen(10);
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testcopeReadsRightAdaptorShort() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CACCCCTGTA");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGCCTGTAGG", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFHIIJIJII", fastQRecord2.getSeqQuality());
		
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("ACCCCTGTAC");
		fastQRecord2 = fastQRecord.clone();
		iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsRightAdaptor() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CTGTAGGCACCATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsRightAdaptorMisMatch() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CTGTTGGGACGATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsRightAdaptorGap() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CTGAGGCACCATCAAT");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
		
		
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CTGAGGCACC");
		fastQRecord2 = fastQRecord.clone();
		iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("CCTTCGATAGCTCAGCTGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("CCCFFFFFHHHGHJGGIJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsLeftAdaptor() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorL("AGTCCTTCGATAGCTCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsLeftAdaptorMisMatch() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorL("AGTCCTTCGTTACCTCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsLeftAdaptorGap() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorL("AGTCCTTCGAAGCCAGC");
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("TGGTAGAGCCTGTAGGCACC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFHIIJIJIIIIJJ", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsAdaptor() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorL("AGTCCTTCGAAGCCAGC");
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("CTGTAGGCACCATCAAT");
		fQrecordcopeReadsAdaptor.setTrimMinLen(6);
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("TGGTAGAGC", fastQRecord2.getSeqFasta().toString());
		assertEquals("IJJJHHIFH", fastQRecord2.getSeqQuality());
	}
	
	public void testcopeReadsAdaptorNNN() {
		fQrecordcopeReadsAdaptor.setFastqOffset(FastQ.FASTQ_SANGER_OFFSET);
		fQrecordcopeReadsAdaptor.setSeqAdaptorL("NNNNNN");
		fQrecordcopeReadsAdaptor.setSeqAdaptorR("NNNN");
		fQrecordcopeReadsAdaptor.setTrimMinLen(6);
		FastQRecord fastQRecord2 = fastQRecord.clone();
		boolean iscopeReadsed = fQrecordcopeReadsAdaptor.copeReads(fastQRecord2);
		assertEquals(iscopeReadsed, true);
		assertEquals("ATAGCTCAGCTGGTAGAGCCTGTAGG", fastQRecord2.getSeqFasta().toString());
		assertEquals("FFHHHGHJGGIJJJHHIFHIIJIJII", fastQRecord2.getSeqQuality());
	}
}
