package com.novelbio.analysis.seq.resequencing;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;

public class SiteSnpIndelInfoFactory {
	private static Logger logger = Logger.getLogger(SiteSnpIndelInfoFactory.class);
	
	public static SnpRefAltInfo creatSiteSnpIndelInfo(RefSiteSnpIndel refSiteSnpIndel, String referenceSeq, String thisSeq) {
		 referenceSeq = referenceSeq.toUpperCase().trim(); thisSeq = thisSeq.toUpperCase().trim();
		 
		 if (referenceSeq.length() > 1 && thisSeq.length() > 1) {
			logger.error("出现奇怪的位置，请确认：" + refSiteSnpIndel.getRefID() + "\t" + refSiteSnpIndel.getRefSnpIndelStart());
			return null;
		}
		 else if (referenceSeq.equals(thisSeq)) {
			 SnpRefAltInfo siteSnpIndelInfo = new SiteSnpIndelInfoNoSnp(refSiteSnpIndel, referenceSeq, thisSeq);
			 return siteSnpIndelInfo;
		}
		else if (referenceSeq.length() == 1 && thisSeq.length() == 1) {
			SnpRefAltInfo siteSnpIndelInfo = new SiteSnpIndelInfoSnp(refSiteSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		else if (referenceSeq.length() == 1 && thisSeq.length() > 1) {
			SnpRefAltInfo siteSnpIndelInfo = new SiteSnpIndelInfoInsert(refSiteSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		else if (referenceSeq.length() > 1 && thisSeq.length() == 1) {
			SnpRefAltInfo siteSnpIndelInfo = new SiteSnpIndelInfoDeletion(refSiteSnpIndel, referenceSeq, thisSeq);
			return siteSnpIndelInfo;
		}
		logger.error("出现奇怪的位置，请确认：" + refSiteSnpIndel.getRefID() + "\t" + refSiteSnpIndel.getRefSnpIndelStart());
		return null;
	}
}
