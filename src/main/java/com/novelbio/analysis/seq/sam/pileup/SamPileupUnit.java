package com.novelbio.analysis.seq.sam.pileup;

import java.util.LinkedList;

import com.novelbio.analysis.seq.sam.SamErrorException;
import com.novelbio.analysis.seq.sam.SamRecord;


/**
 * pileup的一个单元
 * @author zomg0jie
 */
public class SamPileupUnit {
	int startRegion;
	int endRegion;
	LinkedList<SamRecord> lsSamRecord = new LinkedList<>();
	
	/**
	 * 给定指定的位点，获得其pileup信息
	 * @param site
	 */
	public void getSite(int site) {
		checkRegion(site);
	}
	
	private void checkRegion(int site) {
		if (site < startRegion || site > endRegion) {
			throw new SamErrorException("site out of region:" + site + " " + startRegion + " " + endRegion);
		}
	}
}
