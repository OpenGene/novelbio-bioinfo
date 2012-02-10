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
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, String chrID, int coord, String geneTpye) {
		super(IsoName, chrID, coord, geneTpye);
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
		super.setCis5to3(false);
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
		for (int i = size() - 1; i >= 0; i--) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "gene_id \"" + geneID + "\"; transcript_id " + getIsoName() + "\"; \r\n";
		}
		return geneExon;
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = 0;  i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "ID=exon:" + getIsoName()  + ":" + (i+1) +";"+ "Parent=" + getIsoName() + " \r\n";
		}
		return geneExon;
	}
	@Override
	protected void setCod2SiteAbs() {
		cod2ATG =  ATGsite - coord; //CnnnATG    AtgnC
		cod2UAG = UAGsite - coord; //CnuaG    UAGnnnC
		cod2TSS = getTSSsite() - coord;
		cod2TES = getTESsite() - coord;
		
	}
	
}
