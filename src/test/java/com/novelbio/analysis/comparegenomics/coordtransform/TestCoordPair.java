package com.novelbio.analysis.comparegenomics.coordtransform;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.mapping.Align;

public class TestCoordPair {
	
	@Test
	public void testConstruct() {
		CoordPair coordPair = new CoordPair("    2533     2927  | 25917838 25917435  |      395      404  |    90.89  | 1	5");
		assertEquals(new Align("1:2533-2927"), coordPair.getAlignRef());
		assertEquals(new Align("5:25917838-25917435"), coordPair.getAlignAlt());
		assertEquals( 90.89, coordPair.getIdentity(), 0.01);
		
	}
	
	/**
	 * 
	 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg
	 * 45025486   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg
 	 *                                                         ^  ^                                                                       ^   
	 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa
	 * 45025437   ......t...........aagtccaaaagcatgtagccccatcatagaa
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               
	 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa
	 * 45025405   atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa
	 *                                           ^^^^^^^^^^^  ^^^^^                      
	 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat
	 * 45025356   tgaaacccaatacaagaa..........tggcttgtcaggagctactat
  	 *                                                                  ^^^^^^^^^^                     
	 */
	@Test
	public void testIndel() {
		CoordPair coordPair = new CoordPair("    9285     9834  | 45025486 45024948  |      550      539  |    85.49  | 1	1");
		coordPair.addIndelMummer(-13);
		
		coordPair.addIndelMummer(37);
		addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addIndelMummer(-41);
		addCoordPairNum(coordPair, 10, -1);
		
		coordPair.addIndelMummer(-3);
		addCoordPairNum(coordPair, 4, -1);
		
		coordPair.addIndelMummer(41);
		addCoordPairNum(coordPair, 9, 1);
		
		List<IndelForRef> lsIndelForRefs = coordPair.getLsIndel();
		assertEquals(6, lsIndelForRefs.size());
		assertEquals("d:9296-9297:1", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9333-9338:6", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9340-9350:11", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:11", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:5", getIndelInfo(lsIndelForRefs.get(4)));
		assertEquals("i:9433-9442:10", getIndelInfo(lsIndelForRefs.get(5)));
	}
	
	private void addCoordPairNum(CoordPair coordPair, int num, int value) {
		for (int i = 0; i < num; i++) {
			coordPair.addIndelMummer(value);
		}
	}
	private static String getIndelInfo(IndelForRef indelForRef) {
		String insert = indelForRef.isRefInsertion() ? "i" : "d";
		String info = insert + ":" + indelForRef.getStartAbs() + "-" +indelForRef.getEndAbs();
		int len = indelForRef.isRefInsertion() ? indelForRef.getLength() : indelForRef.getDelLen();
		info = info + ":" + len;

		return info;
	}
}
