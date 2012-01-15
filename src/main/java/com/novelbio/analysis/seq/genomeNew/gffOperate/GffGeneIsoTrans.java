package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

public class GffGeneIsoTrans extends GffGeneIsoInfo{
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	public GffGeneIsoTrans(String IsoName, GffDetailGene gffDetailGene, String geneTpye) {
		super(IsoName, gffDetailGene, geneTpye);
	}
	public GffGeneIsoTrans(String IsoName, String chrID, int coord, String geneTpye) {
		super(IsoName, chrID, coord, geneTpye);
	}
	public GffGeneIsoTrans(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
	}
	
	@Override
	public Boolean isCis5to3() {
		return false;
	}
	@Override
	public GffGeneIsoTrans clone() {
		GffGeneIsoTrans gffGeneIsoTrans = new GffGeneIsoTrans(IsoName, chrID, coord, getGeneType());
		this.clone(gffGeneIsoTrans);
		gffGeneIsoTrans.setCoord(getCoord());
		return gffGeneIsoTrans;
	}
	@Override
	public GffGeneIsoTrans cloneDeep() {
		GffGeneIsoTrans gffGeneIsoTrans = new GffGeneIsoTrans(IsoName, chrID, coord, getGeneType());
		this.cloneDeep(gffGeneIsoTrans);
		gffGeneIsoTrans.setCoord(getCoord());
		return gffGeneIsoTrans;
	}
	@Override
	public int getStartAbs() {
		return get(size() - 1).getEndCis();
		
	}

	@Override
	public int getEndAbs() {
		// TODO Auto-generated method stub
		return get(0).getStartCis();
	}

	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = getIsoInfo().size() - 1; i >= 0; i--) {
			ExonInfo exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "gene_id \"" + geneID + "\"; transcript_id " + getIsoName() + "\"; \r\n";
		}
		return geneExon;
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = 0;  i < getIsoInfo().size(); i++) {
			ExonInfo exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "ID=exon:" + getIsoName()  + ":" + (i+1) +";"+ "Parent=" + getIsoName() + " \r\n";
		}
		return geneExon;
	}
	
}
