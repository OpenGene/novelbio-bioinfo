package com.novelbio.analysis.comparegenomics.coordtransform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.mapping.Align;

public class TestCoordTransformer {
	
	@Test
	public void testSearch() {
		CoordPair coord = getCoordPairCis();
		List<CoordPair> lsCoordPairs = Lists.newArrayList(coord);
		Align alignRef = new Align("1:9283-9296");
		VarInfo varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		VarInfo varInfoExp = generateVarInfo("1:45024948-45024959", 2, 0, null, true);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9283-9297");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024948-45024961", 2, 0, coord.getLsIndel().subList(0, 1), true);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9333-9338");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		assertNull(varInfo);
		
		alignRef = new Align("1:9333-9339");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024997-45024997", 6, 0, null, true);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9333-9341");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024997-45024997", 6, 2, null, true);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9347-9392");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024998-45025050", 4, 0, coord.getLsIndel().subList(3, 4), true);
		assertEqualsVar(varInfoExp, varInfo);
	}
	
	private VarInfo generateVarInfo(String chrInfo, int startBias, int endBias, List<IndelForRef> lsIndelForRefs, Boolean isCis5To3) {
		VarInfo varInfo = new VarInfo(chrInfo);
		if (isCis5To3 != null) {
			varInfo.setCis5to3(isCis5To3);
		}
		varInfo.setStartBias(startBias);
		varInfo.setEndBias(endBias);
		varInfo.setLsIndelForRefs(lsIndelForRefs);
		return varInfo;
	}
	
	private void assertEqualsVar(VarInfo varExp, VarInfo varReal) {
		assertEquals(varExp.isCis5to3(), varReal.isCis5to3());
		assertEquals(varExp.getStartAbs(), varReal.getStartAbs());
		assertEquals(varExp.getEndAbs(), varReal.getEndAbs());
		assertEquals(varExp.getStartBias(), varReal.getStartBias());
		assertEquals(varExp.getEndBias(), varReal.getEndBias());
		assertEquals(varExp.getLsIndelForRefs(), varReal.getLsIndelForRefs());
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
	/**
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
	

	
	private void addCoordPairNum(CoordPair coordPair, int num, int value) {
		for (int i = 0; i < num; i++) {
			coordPair.addIndelMummer(value);
		}
	}
}
