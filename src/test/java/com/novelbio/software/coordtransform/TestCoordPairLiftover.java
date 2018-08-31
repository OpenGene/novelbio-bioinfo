package com.novelbio.software.coordtransform;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.software.coordtransform.CoordPair;
import com.novelbio.software.coordtransform.IndelForRef;

public class TestCoordPairLiftover {
	@Test
	public void testConstruct() {
		CoordPair coordPair = getCoordPairCisLiftover();
		List<IndelForRef> lsIndelForRefs = coordPair.getLsIndel();
		assertEquals(3, lsIndelForRefs.size());
		assertEquals("i:177417-257667:80249|i:177417-227418:50000", TestCoordPair.getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:297968-585989:288020|i:267719-521369:253649", TestCoordPair.getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:1630687-1630690:2|i:1566067-1566069:1", TestCoordPair.getIndelInfo(lsIndelForRefs.get(2)));
		
		assertEquals("chain 20849626768 chr1 248956422 + 10000 248946422 chr1 249250621 + 10000 249240621 2", coordPair.toStringHead());
		List<String> lsChain = new ArrayList<>();
		for (String content : coordPair.readPerIndel()) {
			lsChain.add(content);
		}
		assertEquals(4, lsChain.size());
		assertEquals("167417	80249	50000", lsChain.get(0));
		assertEquals("40302	288020	253649", lsChain.get(1));
		assertEquals("1044699	2	1", lsChain.get(2));

	}
	
	private CoordPair getCoordPairCisLiftover() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialChainLiftover("chain 20849626768 chr1 248956422 + 10000 248946422 chr1 249250621 + 10000 249240621 2");
		coordPair.addChainLiftover("167417	80249	50000");
		coordPair.addChainLiftover("40302	288020	253649");
		coordPair.addChainLiftover("1044699	2	1");
		return coordPair;
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
	 * 45025405    atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa
	 *                                           ^^^^^^^^^^^  ^^^^^                      
	 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat
	 * 45025356   tgaaacccaatacaagaa..........tggcttgtcaggagctactat
  	 *                                                                  ^^^^^^^^^^                     
	 */
	@Test
	public void testIndel() {
		CoordPair coordPair = getCoordPairTrans();
		
		List<IndelForRef> lsIndelForRefs = coordPair.getLsIndel();
		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:0|i:45025475-45025473:1", TestCoordPair.getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45025438-45025437:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45025437-45025436:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("i:9390-9393:2|i:45025397-45025378:18", TestCoordPair.getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("i:9432-9443:10|d:45025339-45025338:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(4)));
		
		coordPair.setStart(9335);
		coordPair.setEnd(9392);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9390, coordPair.getEndAbs());
		assertEquals(45025437, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025397, coordPair.getAlignAlt().getEndCis());

		assertEquals(1, lsIndelForRefs.size());
		assertEquals("i:9339-9351:11|d:45025437-45025436:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(0)));
		
		coordPair = getCoordPairTrans();
		coordPair.setStart(9392);
		coordPair.setEnd(9432);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9393, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45025378, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025339, coordPair.getAlignAlt().getEndCis());

		assertEquals(0, lsIndelForRefs.size());
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
		TestCoordPair.addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		TestCoordPair.addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);
		
		coordPair.addIndelMummer(41);
		TestCoordPair.addCoordPairNum(coordPair, 9, 1);
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
		assertEquals(5, lsIndelForRefs.size());
		assertEquals("d:9296-9297:0|i:45024959-45024961:1", TestCoordPair.getIndelInfo(lsIndelForRefs.get(0)));
		assertEquals("i:9332-9339:6|d:45024996-45024997:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(1)));
		assertEquals("i:9339-9351:11|d:45024997-45024998:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(2)));
		assertEquals("i:9390-9393:2|i:45025037-45025056:18", TestCoordPair.getIndelInfo(lsIndelForRefs.get(3)));
		assertEquals("i:9432-9443:10|d:45025095-45025096:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(4)));
		
		//=================================
		coordPair.setStart(9335);
		coordPair.setEnd(9392);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9339, coordPair.getStartAbs());
		assertEquals(9390, coordPair.getEndAbs());
		assertEquals(45024997, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025037, coordPair.getAlignAlt().getEndCis());

		assertEquals(1, lsIndelForRefs.size());
		assertEquals("i:9339-9351:11|d:45024997-45024998:0", TestCoordPair.getIndelInfo(lsIndelForRefs.get(0)));
		
		coordPair = getCoordPairCis();
		coordPair.setStart(9392);
		coordPair.setEnd(9432);
		lsIndelForRefs = coordPair.getLsIndel();

		assertEquals(9393, coordPair.getStartAbs());
		assertEquals(9432, coordPair.getEndAbs());
		assertEquals(45025056, coordPair.getAlignAlt().getStartCis());
		assertEquals(45025095, coordPair.getAlignAlt().getEndCis());

		assertEquals(0, lsIndelForRefs.size());
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
		TestCoordPair.addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		TestCoordPair.addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);
		
		coordPair.addIndelMummer(41);
		TestCoordPair.addCoordPairNum(coordPair, 9, 1);
		return coordPair;
	}

}
