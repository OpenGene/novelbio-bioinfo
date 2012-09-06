package com.novelbio.analysis.seq.resequencing;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;

public class SiteSnpIndelInfoFactory {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoFactory.class);
	
	public static SiteSnpIndelInfo creatSiteSnpIndelInfo(MapInfoSnpIndel mapInfoSnpIndel, GffChrAbs gffChrAbs, String referenceSeq, String thisSeq) {
		 referenceSeq = referenceSeq.toUpperCase(); thisSeq = thisSeq.toUpperCase();
		 
		if (referenceSeq.length() > 1 && thisSeq.length() > 1) {
			logger.error("出现奇怪的位置，请确认：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
			return null;
		}
		else if (referenceSeq.length() == 1 && thisSeq.length() == 1) {
			SiteSnpIndelInfo siteSnpIndelInfo = new SiteSnpIndelInfoSnp(mapInfoSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		else if (referenceSeq.length() == 1 && thisSeq.length() > 1) {
			SiteSnpIndelInfo siteSnpIndelInfo = new SiteSnpIndelInfoInsert(mapInfoSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		else if (referenceSeq.length() > 1 && thisSeq.length() == 1) {
			SiteSnpIndelInfo siteSnpIndelInfo = new SiteSnpIndelInfoDeletion(mapInfoSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		logger.error("出现奇怪的位置，请确认：" + mapInfoSnpIndel.getRefID() + "\t" + mapInfoSnpIndel.getRefSnpIndelStart());
		return null;
	}
}
