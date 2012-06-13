package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class GffGeneIsoTrans extends GffGeneIsoInfo{
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	public GffGeneIsoTrans(String IsoName, GffDetailGene gffDetailGene, int geneType) {
		super(IsoName, gffDetailGene, geneType);
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, String chrID, int coord, int geneType) {
		super(IsoName, chrID, geneType);
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, String ChrID, int geneType) {
		super(IsoName, ChrID, geneType);
		super.setCis5to3(false);
	}
	
	@Override
	public int getStartAbs() {
		return get(size() - 1).getEndCis();
		
	}

	@Override
	public int getEndAbs() {
		return get(0).getStartCis();
	}

	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = size() - 1; i >= 0; i--) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "gene_id \"" + geneID + "\"; transcript_id " + getName() + "\"; \r\n";
		}
		return geneExon;
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = 0;  i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
					+ strand + "\t.\t" + "ID=exon:" + getName()  + ":" + (i+1) +";"+ "Parent=" + getName() + " \r\n";
		}
		return geneExon;
	}
	@Override
	public GffGeneIsoTrans clone() {
		GffGeneIsoTrans result = null;
		result = (GffGeneIsoTrans) super.clone();
		return result;
	
	}
	
}
