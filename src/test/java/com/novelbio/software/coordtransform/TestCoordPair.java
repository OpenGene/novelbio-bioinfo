package com.novelbio.software.coordtransform;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.novelbio.bioinfo.base.Align;
import com.novelbio.software.coordtransform.CoordPair;
import com.novelbio.software.coordtransform.IndelForRef;

public class TestCoordPair {
	
	@Test
	public void testConstruct() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    2533     2927  | 25917838 25917435  |      395      404  |    90.89  | 1	5");
		assertEquals(new Align("1:2533-2927"), coordPair.getAlignRef());
		assertEquals(new Align("5:25917838-25917435"), coordPair.getAlignAlt());
		assertEquals( 90.89, coordPair.getIdentity(), 0.01);
		
		coordPair = new CoordPair();
		coordPair.initialChainLiftover("chain 20849626768 chr1 248956422 + 10000 248946422 chr1 249250621 + 10000 249240621 2");
		assertEquals(new Align("chr1:10001-248946422"), coordPair.getAlignRef());
		assertEquals(new Align("chr1:10001-249240621"), coordPair.getAlignAlt());
		assertEquals( 20849626768l, coordPair.getIdentity(), 0.01);
	}
	
	@Test
	public void testAddCoordPairTrans() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    2533     2927  | 25917838 25917435  |      395      404  |    90.89  | 1	5");
		CoordPair coordPair2 = new CoordPair();
		coordPair2.initialMummer("    2998     3015  | 25917420 25917390  |      8      21  |    90.89  | 1	5");
		coordPair2.addChainLiftover(10, 3, 2);
		coordPair.addCoordPair(coordPair2);
		
		assertEquals(new Align("1:2533-3015"), coordPair.getAlignRef());
		assertEquals(new Align("5:25917838-25917390"), coordPair.getAlignAlt());
		List<IndelForRef> lsIndels = coordPair.getLsIndel();
		assertEquals(2, lsIndels.size());
		assertEquals("i:2927-2998:70|i:25917435-25917420:14", getIndelInfo(lsIndels.get(0)));
		assertEquals("i:3007-3011:3|i:25917411-25917408:2", getIndelInfo(lsIndels.get(1)));
		
		assertEquals( 90.89, coordPair.getIdentity(), 0.01);
	}
	
	@Test
	public void testAddCoordPairCis() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    2533     2927  | 25917435 25917838  |      395      404  |    90.89  | 1	5");
		CoordPair coordPair2 = new CoordPair();
		coordPair2.initialMummer("    2998     3015  | 25917990 25918020  |      8      21  |    90.89  | 1	5");
		coordPair2.addChainLiftover(10, 3, 2);
		coordPair.addCoordPair(coordPair2);
		
		assertEquals(new Align("1:2533-3015"), coordPair.getAlignRef());
		assertEquals(new Align("5:25917435-25918020"), coordPair.getAlignAlt());
		List<IndelForRef> lsIndels = coordPair.getLsIndel();
		assertEquals(2, lsIndels.size());
		assertEquals("i:2927-2998:70|i:25917838-25917990:151", getIndelInfo(lsIndels.get(0)));
		assertEquals("i:3007-3011:3|i:25917999-25918002:2", getIndelInfo(lsIndels.get(1)));
		
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
		assertEquals("d:9296-9297:0|i:45025475-45025473:1", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45025438-45025437:0", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45025437-45025436:0", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:0|i:45025397-45025385:11", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:0|i:45025384-45025378:5", getIndelInfo(lsIndelForRefs.get(4)));
		assertEquals("i:9432-9443:10|d:45025339-45025338:0", getIndelInfo(lsIndelForRefs.get(5)));
		
		coordPair.setStart(9335);
		coordPair.setEnd(9435);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45025437, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025339, coordPair.getAlignAlt().getEndCis());

		assertEquals(3, lsIndelForRefs.size());
		assertEquals("i:9339-9351:11|d:45025437-45025436:0", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9390-9391:0|i:45025397-45025385:11", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("d:9392-9393:0|i:45025384-45025378:5", getIndelInfo(lsIndelForRefs.get(2)));
		
		coordPair = getCoordPairTrans();
		coordPair.setStart(9296);
		coordPair.setEnd(9393);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9296, coordPair.getStartAbs());
		assertEquals(9393, coordPair.getEndAbs());
		assertEquals(45025475, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025378, coordPair.getAlignAlt().getEndCis());

		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:0|i:45025475-45025473:1", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45025438-45025437:0", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45025437-45025436:0", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:0|i:45025397-45025385:11", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:0|i:45025384-45025378:5", getIndelInfo(lsIndelForRefs.get(4)));
		
		coordPair = getCoordPairTrans();
		coordPair.setStart(9353);
		coordPair.setEnd(9400);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9353, coordPair.getStartAbs());
		assertEquals(9400, coordPair.getEndAbs());
		assertEquals(45025434, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025371, coordPair.getAlignAlt().getEndCis());

		assertEquals(2, lsIndelForRefs.size());
		assertEquals("d:9390-9391:0|i:45025397-45025385:11", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9392-9393:0|i:45025384-45025378:5", getIndelInfo(lsIndelForRefs.get(1)));
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
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    9285     9834  | 45025486 45024948  |      550      539  |    85.49  | 1	1");
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
		assertEquals("d:9296-9297:0|i:45024959-45024961:1", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45024996-45024997:0", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45024997-45024998:0", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:0|i:45025037-45025049:11", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:0|i:45025050-45025056:5", getIndelInfo(lsIndelForRefs.get(4)));
		assertEquals("i:9432-9443:10|d:45025095-45025096:0", getIndelInfo(lsIndelForRefs.get(5)));
		
		//=================================

		coordPair.setStart(9335);
		coordPair.setEnd(9435);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45024997, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025095, coordPair.getAlignAlt().getEndCis());

		assertEquals(3, lsIndelForRefs.size());
		assertEquals("i:9339-9351:11|d:45024997-45024998:0", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9390-9391:0|i:45025037-45025049:11", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("d:9392-9393:0|i:45025050-45025056:5", getIndelInfo(lsIndelForRefs.get(2)));
		

		coordPair = getCoordPairCis();
		coordPair.setStart(9296);
		coordPair.setEnd(9393);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9296, coordPair.getStartAbs());
		assertEquals(9393, coordPair.getEndAbs());
		assertEquals(45024959, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025056, coordPair.getAlignAlt().getEndCis());

		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:0|i:45024959-45024961:1", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45024996-45024997:0", getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45024997-45024998:0", getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("d:9390-9391:0|i:45025037-45025049:11", getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("d:9392-9393:0|i:45025050-45025056:5", getIndelInfo(lsIndelForRefs.get(4)));

		
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
		assertEquals("d:9390-9391:0|i:45025037-45025049:11", getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("d:9392-9393:0|i:45025050-45025056:5", getIndelInfo(lsIndelForRefs.get(1)));
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
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    9285     9834  | 45024948 45025486   |      550      539  |    85.49  | 1	1");

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
	
	public static void addCoordPairNum(CoordPair coordPair, int num, int value) {
		for (int i = 0; i < num; i++) {
			coordPair.addIndelMummer(value);
		}
	}
	public static String getIndelInfo(IndelForRef indelForRef) {
		String insertRef = indelForRef.isRefInsertion() ? "i" : "d";
		String insertAlt = indelForRef.isAltInsertion() ? "i" : "d";

		String info = insertRef + ":" + indelForRef.getStartAbs() + "-" +indelForRef.getEndAbs() + ":" + indelForRef.getRefLen();
		String info2 = "|"+ insertAlt + ":" +indelForRef.getStartCisAlt() + "-" +indelForRef.getEndCisAlt() + ":" + indelForRef.getAltLen();
		info = info + info2;
		
		return info;
	}
}
