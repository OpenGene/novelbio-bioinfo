package com.novelbio.test.base.dataStructure.listOperate;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.diffexpress.DiffExpDEGseq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoTrans;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.modgeneid.GeneType;
/**
 * @author zong0jie
 *
 */
public class TestListAbs extends TestCase {
	@Before
	public void setUp() {
		
	}
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	/**
	 * ≤‚ ‘ListAbs.getCombSep
	 */
	@Test
	public void testTransIsoNorm() {
		GffGeneIsoTrans isoTrans1 = new GffGeneIsoTrans("Iso1", GeneType.mRNA);
		GffGeneIsoTrans isoTrans2 = new GffGeneIsoTrans("Iso2", GeneType.mRNA);
		//<----------20--30--------------40-50------------------60-70--------------80-90---<
		//<----------20---33--------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(isoTrans1, false, 80, 90));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 20, 30));
		
		isoTrans2.add(new ExonInfo(isoTrans2, false, 80, 92));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 20, 33));
		ArrayList<GffGeneIsoTrans> lsIso = new ArrayList<GffGeneIsoTrans>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<int[]> lsSep = ListAbs.getCombSep(false, lsIso, false);
		int num = 0;
		assertEquals(80, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(50, lsSep.get(num)[1]);
		num++;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
	}
	/**
	 * ≤‚ ‘ListAbs.getCombSep
	 */
	@Test
	public void testTransIsoCasstteDouble() {
		GffGeneIsoTrans isoTrans1 = new GffGeneIsoTrans("Iso1", GeneType.mRNA);
		GffGeneIsoTrans isoTrans2 = new GffGeneIsoTrans("Iso2", GeneType.mRNA);
		//<----------20--30--------------40-50----52-54--------------60-70--------------80-90---<
		//<----------20---33-----------------------------------------------55-69---------------80--92---<
		isoTrans1.add(new ExonInfo(isoTrans1, false, 80, 90));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 52, 54));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 20, 30));
		
		isoTrans2.add(new ExonInfo(isoTrans2, false, 80, 92));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 20, 33));
		ArrayList<GffGeneIsoTrans> lsIso = new ArrayList<GffGeneIsoTrans>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<int[]> lsSep = ListAbs.getCombSep(false, lsIso, false);
		int num = 0;
		assertEquals(80, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
	}
	
	/**
	 * ≤‚ ‘ListAbs.getCombSep
	 */
	@Test
	public void testCisIsoCasstteDouble() {
		GffGeneIsoCis isoTrans1 = new GffGeneIsoCis("Iso1", GeneType.mRNA);
		GffGeneIsoCis isoTrans2 = new GffGeneIsoCis("Iso2", GeneType.mRNA);
		//<----------20--30-----------------40-50----52-54--------------60----70----------80-------90---<
		//<----------20---33----35-36----------------------------------55------69-------71---82---84--92---<
		isoTrans1.add(new ExonInfo(isoTrans1, false, 20, 30));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 40, 50));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 52, 54));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 60, 70));
		isoTrans1.add(new ExonInfo(isoTrans1, false, 80, 90));
		
		isoTrans2.add(new ExonInfo(isoTrans2, false, 20, 33));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 35, 36));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 55, 69));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 71, 82));
		isoTrans2.add(new ExonInfo(isoTrans2, false, 84, 92));
		
		ArrayList<GffGeneIsoCis> lsIso = new ArrayList<GffGeneIsoCis>();
		lsIso.add(isoTrans1); lsIso.add(isoTrans2);
		
		ArrayList<int[]> lsSep = ListAbs.getCombSep(true, lsIso, false);
		int num = 0;
		assertEquals(20, lsSep.get(num)[0]);
		assertEquals(33, lsSep.get(num)[1]);
		num++;
		assertEquals(35, lsSep.get(num)[0]);
		assertEquals(36, lsSep.get(num)[1]);
		num++;
		assertEquals(40, lsSep.get(num)[0]);
		assertEquals(54, lsSep.get(num)[1]);
		num++;
		assertEquals(55, lsSep.get(num)[0]);
		assertEquals(70, lsSep.get(num)[1]);
		num++;
		assertEquals(71, lsSep.get(num)[0]);
		assertEquals(92, lsSep.get(num)[1]);
	}
}
