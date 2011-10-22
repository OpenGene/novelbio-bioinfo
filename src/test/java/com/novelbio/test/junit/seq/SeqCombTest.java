package com.novelbio.test.junit.seq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.SeqComb;

import junit.framework.TestCase;

public class SeqCombTest extends TestCase{
	String seqIn = "";
	@Before
	public void setUp() throws Exception
	{
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCAAANCANACCAAAA";
		
		
	}
	@Test
	public void testTrimPolyA()
	{
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCAAANCANACCAAAA";
		int i = SeqComb.trimPolyA(seqIn, 7, 2);
		assertEquals(25, i);
		
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCAAANCANACCCAAAA";
		i = SeqComb.trimPolyA(seqIn, 7, 2);
		assertEquals(36, i);
		
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCAAANCCANACCAAAA";
		i = SeqComb.trimPolyA(seqIn, 7, 2);
		assertEquals(25, i);
		
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCAAANCCANACCAAAA";
		i = SeqComb.trimPolyA(seqIn, 3, 2);
		assertEquals(31, i);
		
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCACACANCANACAACAA";
		i = SeqComb.trimPolyA(seqIn, 3, 2);
		assertEquals(29, i);//should be 29
		
		seqIn = "CCCCCCCCCCCCTTTTGGGAAACCCACACANCANACAACAACCC";
		i = SeqComb.trimPolyA(seqIn, 3, 2);
		assertEquals(44, i);
		seqIn = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		i = SeqComb.trimPolyA(seqIn, 3, 2);
		assertEquals(0, i);
	}
	@Test
	public void testTrimPolyT()
	{
		seqIn = "TTTTTTCCCCCCCCCCCC";
		int i = SeqComb.trimPolyT(seqIn, 7, 2);
		assertEquals(6, i);
		
		seqIn = "TTTNNTTTTCCTTNTTTCTTCTTTTNTTCCCTTCCCCCCCCCCCCTTTTGGGAAACCCAAANCANACCCAAAA";
		i = SeqComb.trimPolyT(seqIn, 7, 2);
		assertEquals(28, i);
		
		seqIn = "TTTTTTCCTTTNNTCCTTTTCCTTTTTCCTTTTCCCCCCCCCCCCTTTTGGGAAACCCAAANCCANACCAAAA";
		i = SeqComb.trimPolyT(seqIn, 7, 2);
		assertEquals(27, i);

		seqIn = "TTTTTTTTTTTTTTT";
		i = SeqComb.trimPolyT(seqIn, 3, 2);
		assertEquals(15, i);
		

	}
	
	
	@After
	public void  clear() {
		
	}
	
	
}
