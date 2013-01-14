package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.domain.geneanno.SepSign;

public abstract class PredictAltStartEnd extends SpliceTypePredict {
	/** exon与前面一个exon尾巴的坐标 */
	ArrayList<Align> lsSite;

	/** 判定为altStartEnd的listexon */
	ArrayList<ArrayList<ExonInfo>> lslsExonInfos;
	
	public PredictAltStartEnd(ExonCluster exonCluster) {
		super(exonCluster);
	}
 
	protected boolean isType() {
		boolean istype = false;
		if (isBeforeOrAfterNotSame()) {
			find();
		}
		
		if (lsSite == null || lsSite.size() == 0) {
			istype = false;
		} else {
			istype = true;
		}
		return istype;
	}
	
	/**
	 * altStart返回Before
	 * altEnd 返回after
	 * @return
	 */
	protected abstract boolean isBeforeOrAfterNotSame();
	
	/**
	 * 看本位点是否能和前一个exon组成mutually exclusivelsIsoExon
	 * 并且填充lsExonThisBefore和lsExonBefore
	 */
	protected abstract void find();
	
}
