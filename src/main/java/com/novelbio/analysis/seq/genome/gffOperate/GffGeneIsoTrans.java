package com.novelbio.analysis.seq.genome.gffOperate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.genome.ExceptionNbcGFF;
import com.novelbio.database.domain.modgeneid.GeneType;
public class GffGeneIsoTrans extends GffGeneIsoInfo {
	private static final long serialVersionUID = -3187469258704218874L;
	
	private static final Logger logger = LoggerFactory.getLogger(GffGeneIsoTrans.class);
	
	public GffGeneIsoTrans() {}
	
	public GffGeneIsoTrans(String IsoName, String geneParentName, GeneType geneType) {
		super(IsoName, geneParentName, geneType);
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, String geneParentName, GffDetailGene gffDetailGene, GeneType geneType) {
		super(IsoName, geneParentName, gffDetailGene, geneType);
		super.setCis5to3(false);
	}
	@Override
	public int getStartAbs() {
		if (size() == 0) {
			throw new ExceptionNbcGFF(getName() + " has no exon infomation please check!");
		}
		return get(size() - 1).getEndCis();
	}
	@Override
	public int getEndAbs() {
		return get(0).getStartCis();
	}

	@Override
	public GffGeneIsoTrans clone() {
		GffGeneIsoTrans result = null;
		result = (GffGeneIsoTrans) super.clone();
		return result;
	
	}
	
}
