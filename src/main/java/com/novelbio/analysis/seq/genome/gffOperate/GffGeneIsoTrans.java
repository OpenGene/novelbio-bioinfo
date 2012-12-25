package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.database.model.modgeneid.GeneType;

public class GffGeneIsoTrans extends GffGeneIsoInfo{
	private static final long serialVersionUID = -3187469258704218874L;
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	
	public GffGeneIsoTrans(String IsoName, GeneType geneType) {
		super(IsoName, geneType);
		super.setCis5to3(false);
	}
	public GffGeneIsoTrans(String IsoName, GffDetailGene gffDetailGene, GeneType geneType) {
		super(IsoName, gffDetailGene, geneType);
		super.setCis5to3(false);
	}
	@Override
	public int getStartAbs() {
		if (size() == 0) {
			logger.error(getName() + "Ã»ÓÐÔªËØ");
		}
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
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis()
			     + "\t" + "." + "\t" + strand + "\t.\t" + "gene_id \"" + geneID + "\"; transcript_id " + "\"" + getName() + "\"; \r\n";
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
