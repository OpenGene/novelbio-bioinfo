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
		CoordPair coordPair = getCoordPairTrans();
		
		List<IndelForRef> lsIndelForRefs = coordPair.getLsIndel();
		assertEquals(6, lsIndelForRefs.size());
		assertEquals("d:9296-9297:1|45025474-45025474", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9333-9338:6|45025438-45025437", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9340-9350:11|45025437-45025436", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:11|45025396-45025386", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:5|45025383-45025379", getIndelInfo(lsIndelForRefs.get(4)));
		assertEquals("i:9433-9442:10|45025339-45025338", getIndelInfo(lsIndelForRefs.get(5)));
		
		coordPair.setStart(9335);
		coordPair.setEnd(9435);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45025437, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025339, coordPair.getAlignAlt().getEndCis());

		assertEquals(3, lsIndelForRefs.size());
		assertEquals("i:9340-9350:11|45025437-45025436", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9390-9391:11|45025396-45025386", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("d:9392-9393:5|45025383-45025379", getIndelInfo(lsIndelForRefs.get(2)));
		
		coordPair = getCoordPairTrans();
		coordPair.setStart(9296);
		coordPair.setEnd(9393);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9296, coordPair.getStartAbs());
		assertEquals(9393, coordPair.getEndAbs());
		assertEquals(45025475, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025378, coordPair.getAlignAlt().getEndCis());

		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:1|45025474-45025474", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9333-9338:6|45025438-45025437", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9340-9350:11|45025437-45025436", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:11|45025396-45025386", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:5|45025383-45025379", getIndelInfo(lsIndelForRefs.get(4)));
		
		coordPair = getCoordPairTrans();
		coordPair.setStart(9353);
		coordPair.setEnd(9400);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9353, coordPair.getStartAbs());
		assertEquals(9400, coordPair.getEndAbs());
		assertEquals(45025434, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025371, coordPair.getAlignAlt().getEndCis());

		assertEquals(2, lsIndelForRefs.size());
		assertEquals("d:9390-9391:11|45025396-45025386", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9392-9393:5|45025383-45025379", getIndelInfo(lsIndelForRefs.get(1)));
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
	private CoordPair getCoordPairTrans() {
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
		return coordPair;
	}
	
	/**
	 * 
	 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg
	 * 45024948   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg
 	 *                                                         ^  ^                                                                       ^   
	 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa
	 * 45024997   ......t...........aagtccaaaagcatgtagccccatcatagaa
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               
	 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa
	 * 45025029    atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa
	 *                                           ^^^^^^^^^^^  ^^^^^                      
	 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat
	 * 45025078   tgaaacccaatacaagaa..........tggcttgtcaggagctactat
  	 *                                                                  ^^^^^^^^^^                     
	 */
	@Test
	public void testIndel2() {
		CoordPair coordPair = getCoordPairCis();

		
		List<IndelForRef> lsIndelForRefs = coordPair.getLsIndel();
		assertEquals(6, lsIndelForRefs.size());
		assertEquals("d:9296-9297:1|45024960-45024960", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9333-9338:6|45024996-45024997", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9340-9350:11|45024997-45024998", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:11|45025038-45025048", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:5|45025051-45025055", getIndelInfo(lsIndelForRefs.get(4)));
		assertEquals("i:9433-9442:10|45025095-45025096", getIndelInfo(lsIndelForRefs.get(5)));
		
		//=================================

		coordPair.setStart(9335);
		coordPair.setEnd(9435);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45024997, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025095, coordPair.getAlignAlt().getEndCis());

		assertEquals(3, lsIndelForRefs.size());
		assertEquals("i:9340-9350:11|45024997-45024998", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9390-9391:11|45025038-45025048", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("d:9392-9393:5|45025051-45025055", getIndelInfo(lsIndelForRefs.get(2)));
		

		coordPair = getCoordPairCis();
		coordPair.setStart(9296);
		coordPair.setEnd(9393);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9296, coordPair.getStartAbs());
		assertEquals(9393, coordPair.getEndAbs());
		assertEquals(45024959, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025056, coordPair.getAlignAlt().getEndCis());

		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:1|45024960-45024960", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9333-9338:6|45024996-45024997", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9340-9350:11|45024997-45024998", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:11|45025038-45025048", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:5|45025051-45025055", getIndelInfo(lsIndelForRefs.get(4)));
		
		/**
		 * 
		 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg
		 * 45024948   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg
	 	 *                                                         ^  ^                                                                       ^   
		 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa
		 * 45024997   ......t...........aagtccaaaagcatgtagccccatcatagaa
	 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               
		 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa
		 * 45025029    atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa
		 *                                           ^^^^^^^^^^^  ^^^^^                      
		 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat
		 * 45025078   tgaaacccaatacaagaa..........tggcttgtcaggagctactat
	  	 *                                                                  ^^^^^^^^^^                     
		 */
		coordPair = getCoordPairCis();
		coordPair.setStart(9353);
		coordPair.setEnd(9400);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9353, coordPair.getStartAbs());
		assertEquals(9400, coordPair.getEndAbs());
		assertEquals(45025000, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025063, coordPair.getAlignAlt().getEndCis());

		assertEquals(2, lsIndelForRefs.size());
		assertEquals("d:9390-9391:11|45025038-45025048", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9392-9393:5|45025051-45025055", getIndelInfo(lsIndelForRefs.get(1)));
	}
	/**
	 * 
	 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg
	 * 45024948   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg
 	 *                                                         ^  ^                                                                       ^   
	 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa
	 * 45024997   ......t...........aagtccaaaagcatgtagccccatcatagaa
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               
	 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa
	 * 45025029    atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa
	 *                                           ^^^^^^^^^^^  ^^^^^                      
	 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat
	 * 45025078   tgaaacccaatacaagaa..........tggcttgtcaggagctactat
  	 *                                                                  ^^^^^^^^^^                     
	 */
	private CoordPair getCoordPairCis() {
		CoordPair coordPair = new CoordPair("    9285     9834  | 45024948 45025486   |      550      539  |    85.49  | 1	1");
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
		return coordPair;
	}
	
	private void addCoordPairNum(CoordPair coordPair, int num, int value) {
		for (int i = 0; i < num; i++) {
			coordPair.addIndelMummer(value);
		}
	}
	private static String getIndelInfo(IndelForRef indelForRef) {
		String insert = indelForRef.isRefInsertion() ? "i" : "d";
		String info = insert + ":" + indelForRef.getStartAbs() + "-" +indelForRef.getEndAbs();
		int len = indelForRef.isRefInsertion() ? indelForRef.getLength() : indelForRef.getAltLen();
		info = info + ":" + len;

		String info2 = "|"+ indelForRef.getStartCisAlt() + "-" +indelForRef.getEndCisAlt();
		info = info + info2;
		
		return info;
	}
}
