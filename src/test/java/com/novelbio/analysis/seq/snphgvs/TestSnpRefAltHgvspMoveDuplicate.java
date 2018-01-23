package com.novelbio.analysis.seq.snphgvs;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.model.modgeneid.GeneType;

import junit.framework.Assert;

public class TestSnpRefAltHgvspMoveDuplicate {
	
	public void testIsNeedMoveDuplicateBefore() {
		GffGeneIsoInfo isoCis = getIsoCis();
		testIsNeedMoveDuplicateBefore(isoCis);
		GffGeneIsoInfo isoTrans = getIsoTrans();
		testIsNeedMoveDuplicateBefore(isoTrans);
	}
	
	protected void testIsNeedMoveDuplicateBefore(GffGeneIsoInfo iso) {
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 173470236, "A", "AC");
		snpRefAltInfo.isDup = true;
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:31-33"));
		SnpRefAltHgvsp snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:32-33"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:33-33"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertFalse(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		snpRefAltInfo.setAlignRef(new Align("chr1:31-33"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		snpRefAltInfo.setAlignRef(new Align("chr1:32-33"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertFalse(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		//==================================================
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-39"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-38"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Deletions;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-37"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertFalse(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-40"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-39"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertTrue(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
		
		snpRefAltInfo.varType = EnumHgvsVarType.Duplications;
		snpRefAltInfo.setAlignRef(new Align("chr1:36-38"));
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo, iso);
		Assert.assertFalse(snpRefAltHgvsp.isNeedMoveDuplicateBefore());
	}
		
	private GffGeneIsoInfo getIsoCis() {
		GffGeneIsoInfo isoCis = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneCis", null, GeneType.mRNA, true);
		//<---20-30------40-50-------60-70-------80-90-----100-110-----120-125<
		isoCis.add(new ExonInfo( true, 20, 30));
		isoCis.add(new ExonInfo( true, 40, 50));
		isoCis.add(new ExonInfo( true, 60, 70));
		isoCis.add(new ExonInfo( true, 80, 90));
		isoCis.add(new ExonInfo( true, 100, 110));
		isoCis.add(new ExonInfo( true, 120, 125));
		isoCis.setATG(65);
		isoCis.setUAG(85);
		return isoCis;
	}
	private GffGeneIsoInfo getIsoTrans() {
		GffGeneIsoInfo isoTrans = GffGeneIsoInfo.createGffGeneIso("Iso1", "geneTrans", null, GeneType.mRNA, false);
		//>---20-30------40-50-------60-65-70-------80-85-90-----100-110-----120-125-->
		//>---125-120------110-100-------90-85-80-------70-65-60----50-40-----30-20-->
		isoTrans.add(new ExonInfo( false, 120, 125));
		isoTrans.add(new ExonInfo( false, 100, 110));
		isoTrans.add(new ExonInfo( false, 80, 90));
		isoTrans.add(new ExonInfo( false, 60, 70));
		isoTrans.add(new ExonInfo( false, 40, 50));
		isoTrans.add(new ExonInfo( false, 20, 30));
		isoTrans.setATG(85);
		isoTrans.setUAG(65);
		return isoTrans;
	}
}
