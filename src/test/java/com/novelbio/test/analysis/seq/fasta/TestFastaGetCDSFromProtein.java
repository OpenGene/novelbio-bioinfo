package com.novelbio.test.analysis.seq.fasta;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.fasta.FastaGetCDSFromProtein;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public class TestFastaGetCDSFromProtein extends TestCase{
//	public static void main(String[] args) {
//		SeqFasta seqFasta = new SeqFasta("TCCTCCGCTGCCGAGGCATCATGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
//				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTTAGCAAGGACTGGCTCCAGGCTTGCC");
//		System.out.println(seqFasta.toStringAA(true, 0));
//		System.out.println(seqFasta.toStringAA(true, 1));
//		System.out.println(seqFasta.toStringAA(true, 2));
//	}
	
	
	FastaGetCDSFromProtein fastaGetCDSFromProtein;
	protected void setUp() throws Exception {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void test() {
		assertStartEndUsual();
//		assertStartEndNoStartEnd();
		assertStartEndNoStartEndAndNoATGUAG();
		assertStartEndNoStartEndAndNoATGUAG2();
	}
	
	private void assertStartEndUsual() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCCGAGGCATCATGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTTAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "MAAKSDGRLKMKKSSDVAFTPLQNSDNSGS";
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(21, gffGeneIsoInfo.getATGsite());
		assertEquals(113, gffGeneIsoInfo.getUAGsite());
	}
	/** protein 没有端点并且有错配的测试 */
	private void assertStartEndNoStartEnd() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCCGAGGCATCATGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTTAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //蛋白没有起点和终点，下划线表示gap，X表示错配
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(21, gffGeneIsoInfo.getATGsite());
		assertEquals(113, gffGeneIsoInfo.getUAGsite());
	}
	
	/** protein 没有端点并且有错配
	 * 序列没有ATG和UAG
	 */
	private void assertStartEndNoStartEndAndNoATGUAG() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCCGAGGCATCCTGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTAAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //蛋白没有起点和终点，下划线表示gap，X表示错配
		
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		fastaGetCDSFromProtein.setGetBlastIso(true);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(3, gffGeneIsoInfo.getATGsite());
		assertEquals(134, gffGeneIsoInfo.getUAGsite());
	}
	
	/** protein 没有端点并且有错配
	 * 序列没有ATG和UAG
	 * 但是蛋白的最前面有个UAG，那么设定ATG位点应该在UAG前面
	 */
	private void assertStartEndNoStartEndAndNoATGUAG2() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCTAAGGCATCCTGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTAAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //蛋白没有起点和终点，下划线表示gap，X表示错配
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(15, gffGeneIsoInfo.getATGsite());
		assertEquals(134, gffGeneIsoInfo.getUAGsite());
	}
}
