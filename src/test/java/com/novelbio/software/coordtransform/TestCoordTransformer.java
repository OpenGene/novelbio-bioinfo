package com.novelbio.software.coordtransform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.software.coordtransform.CoordPair;
import com.novelbio.software.coordtransform.CoordTransformer;
import com.novelbio.software.coordtransform.IndelForRef;
import com.novelbio.software.coordtransform.VarInfo;
import com.novelbio.software.snpanno.SeqHashStub;
import com.novelbio.software.snpanno.SnpInfo;

public class TestCoordTransformer {
	
	/**
	 * 1           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
	 * 1   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
 	 *                                                         ^  ^                                                                       ^   <br>
	 * 49       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
	 * 50       ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
	 * 98            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
	 * 82            atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
	 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
	 * 131           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
	 * 131   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
  	 *                                                                  ^^^^^^^^^^                     <br>
	 */
	@Test
	public void testTransformCis() {
		String seq = "gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg"
				+"taagtccaaaagcatgtagccccatcatagaa"
				+"atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa"
				+"tgaaacccaatacaagaatggcttgtcaggagctactat";
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq(seq);
		
		CoordPair coord = getCoordPairCis1();
		List<CoordPair> lsCoordPairs = Lists.newArrayList(coord);
		Map<String, List<CoordPair>> mapChrId2LsCoord = new HashMap<>();
		mapChrId2LsCoord.put("1", lsCoordPairs);
		CoordTransformer coordTransformer = new CoordTransformer();
		coordTransformer.setMapChrId2LsCoorPairs(mapChrId2LsCoord);
		coordTransformer.setSeqHashAlt(seqHashStub);
		
		SnpInfo snpInfo = new SnpInfo("1", 14, "a", "g");
		SnpInfo snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	15	t	g", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 132, "g", "c");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	132	g	c", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 54, "ttacacaagaagaa", "t");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	49	gta	g", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 106, "a", "aac");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	90	a	aac", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 98, "atggtg", "catac");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	82	atggtg	catac", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 98, "atggtg", "catac");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	82	atggtg	catac", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 106, "atgc", "cag");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	90	atacagtttcactgaaatac	cag", snpInfoAlt.toString());
	}
	
	
	/**
	 * 1           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
	 * 539   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
 	 *                                                         ^  ^                                                                       ^   <br>
	 * 49       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
	 * 490   ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
	 * 98            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
	 * 458          atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
	 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
	 * 131           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
	 * 409   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
  	 *                                                                  ^^^^^^^^^^                     <br>
	 */
	@Test
	public void testTransformTrans() {
		String seq = "gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg"
				+"taagtccaaaagcatgtagccccatcatagaa"
				+"atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa"
				+"tgaaacccaatacaagaatggcttgtcaggagctactatg"
		+"tgaaacccaatacaagaatggcttgtcaggagctactatctgaaacccaatacaagaatggcttgtcaggagctactatc"
		+"tgaaacccaatacaagaatggcttgtcaggagctactatctgaaacccaatacaagaatggcttgtcaggagctactatc"
		+"tgaaacccaatacaagaatggcttgtcaggagctactatctgaaacccaatacaagaatggcttgtcaggagctactatc"
		+"tgaaacccaatacaagaatggcttgtcaggagctactatctgaaacccaatacaagaatggcttgtcaggagctactatc"
		+"tgaaacccaatacaagaatggcttgtcagtgaaacccaatacaagaatg";

		seq = SeqFasta.reverseComplement(seq);
		SeqHashStub seqHashStub = new SeqHashStub();
		seqHashStub.setSeq(seq);
		
		CoordPair coord = getCoordPairTrans1();
		List<CoordPair> lsCoordPairs = Lists.newArrayList(coord);
		Map<String, List<CoordPair>> mapChrId2LsCoord = new HashMap<>();
		mapChrId2LsCoord.put("1", lsCoordPairs);
		CoordTransformer coordTransformer = new CoordTransformer();
		coordTransformer.setMapChrId2LsCoorPairs(mapChrId2LsCoord);
		coordTransformer.setSeqHashAlt(seqHashStub);
		
		SnpInfo snpInfo = new SnpInfo("1", 14, "a", "g");
		SnpInfo snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	525	a	c", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 132, "g", "c");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	408	c	g", snpInfoAlt.toString());
		snpInfo = new SnpInfo("1", 54, "ttacacaagaagaa", "t");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	488	tta	t", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 55, "t", "tac");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	489	t	tgt", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 98, "atggtg", "catac");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	453	caccat	gtatg", snpInfoAlt.toString());
		
		snpInfo = new SnpInfo("1", 106, "atgc", "cag");
		snpInfoAlt = coordTransformer.coordTransform(snpInfo);
		assertEquals("1	431	gtatttcagtgaaactgtat	ctg", snpInfoAlt.toString());
	}
	
	
	
	@Test
	public void testSearchCis() {
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
		alignRef = new Align("1:9347-9392");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024998-45025037", 4, 2, null, true);
		assertEqualsVar(varInfoExp, varInfo);
		
		coord = getCoordPairCis();
		coord.lsIndel = new ArrayList<>();
		lsCoordPairs = Lists.newArrayList(coord);
		alignRef = new Align("1:9283-9296");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45024948-45024959", 2, 0, null, true);
		assertEqualsVar(varInfoExp, varInfo);
	}
	
	@Test
	public void testSearchTrans() {
		/**
		 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
		 * 45025486   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
	 	 *                                                         ^  ^                                                                       ^   <br>
		 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
		 * 45025437   ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
	 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
		 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
		 * 45025405    atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
		 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
		 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
		 * 45025356   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
	  	 *                                                                  ^^^^^^^^^^                     <br>
		 */
		CoordPair coord = getCoordPairTrans();
		List<CoordPair> lsCoordPairs = Lists.newArrayList(coord);
		Align alignRef = new Align("1:9283-9296");
		VarInfo varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		VarInfo varInfoExp = generateVarInfo("1:45025486-45025475", 2, 0, null, false);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9283-9297");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45025486-45025473", 2, 0, coord.getLsIndel().subList(0, 1), false);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9333-9338");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		assertNull(varInfo);
		
		alignRef = new Align("1:9333-9339");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45025437-45025437", 6, 0, null, false);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9333-9341");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45025437-45025437", 6, 2, null, false);
		assertEqualsVar(varInfoExp, varInfo);
		
		alignRef = new Align("1:9347-9392");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45025436-45025397", 4, 2, null, false);
		assertEqualsVar(varInfoExp, varInfo);
		
		coord = getCoordPairTrans();
		coord.lsIndel = new ArrayList<>();
		lsCoordPairs = Lists.newArrayList(coord);
		alignRef = new Align("1:9283-9296");
		varInfo = CoordTransformer.coordTransform(lsCoordPairs, alignRef);
		varInfoExp = generateVarInfo("1:45025486-45025475", 2, 0, null, false);
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
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    9285     9834  | 45024948 45025486   |      550      539  |    85.49  | 1	1");
		coordPair.addIndelMummer(-13);
		
		coordPair.addIndelMummer(37);
		addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);
		
		coordPair.addIndelMummer(41);
		addCoordPairNum(coordPair, 9, 1);
		return coordPair;
	}	
	/**
	 * 9285           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
	 * 45025486   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
 	 *                                                         ^  ^                                                                       ^   <br>
	 * 9333       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
	 * 45025437   ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
	 * 9382            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
	 * 45025405   atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
	 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
	 * 9415           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
	 * 45025356   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
  	 *                                                                  ^^^^^^^^^^                     <br>
	 */
	private CoordPair getCoordPairTrans() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    9285     9834  | 45025486 45024948  |      550      539  |    85.49  | 1	1");

		coordPair.addIndelMummer(-13);
		
		coordPair.addIndelMummer(37);
		addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);
		
		coordPair.addIndelMummer(41);
		addCoordPairNum(coordPair, 9, 1);
		return coordPair;
	}
	
	/**
	 * 1           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
	 * 1   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
 	 *                                                         ^  ^                                                                       ^   <br>
	 * 49       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
	 * 50   ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
	 * 98            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
	 * 82   atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
	 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
	 * 131           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
	 * 131   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
  	 *                                                                  ^^^^^^^^^^                     <br>
	 */
	private CoordPair getCoordPairCis1() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    1     550  | 1 539   |      550      539  |    85.49  | 1	1");

		coordPair.addIndelMummer(-13);
		
		coordPair.addIndelMummer(37);
		addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);

		coordPair.addIndelMummer(41);
		addCoordPairNum(coordPair, 9, 1);
		return coordPair;
	}	
	/**
	 * 1           gatatgaggaat-gatttatcatgtgaggtgaaagaagagcaccgggtg<br>
	 * 539   gatatgaggaatggttttatcatgtgaggtgaaagaagagcaccgtgtg<br>
 	 *                                                         ^  ^                                                                       ^   <br>
	 * 49       aacagttacacaagaagaaagtccaaaagcaggaagccccatcatagaa<br>
	 * 490   ......t...........aagtccaaaagcatgtagccccatcatagaa<br>
 	 *           ^^^^^^ ^^^^^^^^^^^             ^ ^               <br>
	 * 98            atggtgaaa...........tg.....cgtgtagcttgcaaccttggaa<br>
	 * 458   atggtgaaatacagtttcactgaaatacgtgtagcttgcaaccttggaa<br>
	 *                                           ^^^^^^^^^^^  ^^^^^                      <br>
	 * 131           tgaaacccaatacaagaaatcccctctgtggcttgtcaggagctactat<br>
	 * 409   tgaaacccaatacaagaa..........tggcttgtcaggagctactat<br>
  	 *                                                                  ^^^^^^^^^^                     <br>
	 */
	private CoordPair getCoordPairTrans1() {
		CoordPair coordPair = new CoordPair();
		coordPair.initialMummer("    1     550  | 539  1  |      550      539  |    85.49  | 1	1");

		coordPair.addIndelMummer(-13);
		
		coordPair.addIndelMummer(37);
		addCoordPairNum(coordPair, 5, 1);
		
		coordPair.addIndelMummer(2);
		addCoordPairNum(coordPair, 10, 1);
		
		coordPair.addChainLiftover(40, 2, 18);
		
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
