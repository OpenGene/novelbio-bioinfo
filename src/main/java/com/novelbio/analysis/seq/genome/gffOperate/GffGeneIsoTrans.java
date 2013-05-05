package com.novelbio.analysis.seq.genome.gffOperate;

import org.apache.log4j.Logger;

import com.novelbio.database.model.modgeneid.GeneType;

public class GffGeneIsoTrans extends GffGeneIsoInfo {
	private static final long serialVersionUID = -3187469258704218874L;
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	
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
			logger.error(getName() + "没有元素");
		}
		return get(size() - 1).getEndCis();
	}
	@Override
	public int getEndAbs() {
		return get(0).getStartCis();
	}

	@Override
	protected String getGTFformatExon(String title, String strand) {
		String geneExon = "";
		for (int i = size() - 1; i >= 0; i--) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getRefID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis()
			     + "\t" + "." + "\t" + strand + "\t.\t" + "gene_id \"" + getParentGeneName() + "\"; transcript_id " + "\"" + getName() + "\"; \r\n";
		}
		return geneExon;
	}
	@Override
	protected String getGFFformatExonMISO(String title, String strand) {
		String geneExon = "";
		for (int i = 0;  i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getRefID() + "\t" + title + "\texon\t" + exons.getEndCis() + "\t" + exons.getStartCis() + "\t" + "." + "\t"
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
