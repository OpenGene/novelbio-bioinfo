package com.novelbio.bioinfo.gff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.database.domain.modgeneid.GeneType;
public class GffIsoTrans extends GffIso {
	private static final long serialVersionUID = -3187469258704218874L;
	
	private static final Logger logger = LoggerFactory.getLogger(GffIsoTrans.class);
	
	public GffIsoTrans() {}
	
	public GffIsoTrans(String IsoName, String geneParentName, GeneType geneType) {
		super(IsoName, geneParentName, geneType);
		setCis5to3(false);
	}
	public GffIsoTrans(String IsoName, String geneParentName, GffGene gffDetailGene, GeneType geneType) {
		super(IsoName, geneParentName, gffDetailGene, geneType);
		setCis5to3(false);
	}

	@Override
	public GffIsoTrans clone() {
		GffIsoTrans result = null;
		result = (GffIsoTrans) super.clone();
		return result;
	
	}

}
