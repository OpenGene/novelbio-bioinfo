package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.resequencing.RefSiteSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class PlinkMapReader {
	
	/**
	 * 每个位点的坐标信息<br>
	 * chrId name	other	location<br>
	 * 1	10100001579	0	1579<br>
	 * 1	10100003044	0	3044<br>
	 * 
	 * 其中第二列和第三列不用管，只需要根据第一列和第四列去提取信息即可
	 */
	String plinkMap;
	
	/** 从第几位的Snp开始读 */
	int startPlinkPedSample;
	
	/** 读到第几位的Snp */
	int endPlinkPedSample;
	
	/** 缓存队列，用于 */
	Queue<Allele> lsAllele = new LinkedList<>();
	
	GffChrAbs gffChrAbs;
	public void setGffChrAbs(String chrFile, String gffFile) {
		this.gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
	}

	public void extractSnp() {
		TxtReadandWrite txtReadSite = new TxtReadandWrite(plinkMap);
		//用队列做一个位点的缓存
		
		
		
		for (String content : txtReadSite.readlines()) {
			Allele allele = new Allele(content);
			
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, allele.getRefID(), allele.getStartAbs());
			SiteSnpIndelInfo siteSnpIndelInfo = refSiteSnpIndel.getAndAddAllenInfo(allele.getRef(), allele.getAlt());
			GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndel.getGffIso();
			if (siteSnpIndelInfo == null || gffGeneIsoInfo == null) {
				GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(lsInfo.get(colChrID), refStartSite);
				if (gffCodGene == null) {
					return input;
				}
				//TODO 5000bp以内的基因都注释起来
				GffDetailGene gffDetailGene = gffCodGene.getNearestGffGene(5000);
				if (gffDetailGene == null) {
					return input;
				}
				gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
			}
		}
	}
	
	private void fillCach() {
		
	}
	
	private GffGeneIsoInfo getSiteInIso = 
	
	private GffGeneIsoInfo getNearestIso(Allele allele) {
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(allele.getRefID(), allele.getStartAbs());
		if (gffCodGene == null) {
			return null;
		}
		//TODO 5000bp以内的基因都注释起来
		GffDetailGene gffDetailGene = gffCodGene.getNearestGffGene(5000);
		if (gffDetailGene == null) {
			return null;
		}
		gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
	
	}
	
}

class Allele extends Align {
	String ref;
	String alt;
	
	public Allele() {}
	
	public Allele(String chrInfo) {
		String[] ss = chrInfo.split("\t");
		setChrID(ss[0]);
		setEndAbs(Integer.parseInt(ss[3]));
		setStartAbs(Integer.parseInt(ss[3]));
		if (ss.length > 6) {
			ref = ss[4];
			alt = ss[5];
		}
	}
	
	public String getRef() {
		return ref;
	}
	public String getAlt() {
		return alt;
	}
	
	public int getPosition() {
		return getStartAbs();
	}
	
}