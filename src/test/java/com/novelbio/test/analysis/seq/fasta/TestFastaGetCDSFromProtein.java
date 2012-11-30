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
	/** protein û�ж˵㲢���д���Ĳ��� */
	private void assertStartEndNoStartEnd() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCCGAGGCATCATGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTTAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //����û�������յ㣬�»��߱�ʾgap��X��ʾ����
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(21, gffGeneIsoInfo.getATGsite());
		assertEquals(113, gffGeneIsoInfo.getUAGsite());
	}
	
	/** protein û�ж˵㲢���д���
	 * ����û��ATG��UAG
	 */
	private void assertStartEndNoStartEndAndNoATGUAG() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCCGAGGCATCCTGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTAAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //����û�������յ㣬�»��߱�ʾgap��X��ʾ����
		
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		fastaGetCDSFromProtein.setGetBlastIso(true);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(3, gffGeneIsoInfo.getATGsite());
		assertEquals(134, gffGeneIsoInfo.getUAGsite());
	}
	
	/** protein û�ж˵㲢���д���
	 * ����û��ATG��UAG
	 * ���ǵ��׵���ǰ���и�UAG����ô�趨ATGλ��Ӧ����UAGǰ��
	 */
	private void assertStartEndNoStartEndAndNoATGUAG2() {
		SeqFasta seqFasta = new SeqFasta("testaaa", "TCCTCCGCTGCTAAGGCATCCTGGCCGCTAAGTCAGACGGGAGGCTGAAGATGAA" +
				"GAAGAGCAGCGACGTGGCGTTCACCCCGCTGCAGAACTCGGACAATTCGGGCTCTAAGCAAGGACTGGCTCCAGGCTTGCC");
		String proteinSeq = "XKSDGRLKMK_KSSDVAFTPLQNXDNS"; //����û�������յ㣬�»��߱�ʾgap��X��ʾ����
		fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFasta, proteinSeq);
		GffGeneIsoInfo gffGeneIsoInfo = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		assertEquals(15, gffGeneIsoInfo.getATGsite());
		assertEquals(134, gffGeneIsoInfo.getUAGsite());
	}
}
