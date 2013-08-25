package com.novelbio.analysis.seq.genome.gffOperate;


import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneType;
/**
 * 名字通通小写
 * 计算距离时，SnnnC<br>
 * S距离C为5，S和C重合时距离为0<br>
 * CnnnATG<br>
 * C到UTRend的距离: ATGsite - coord - 1;//CnnnATG<br>
 * C到ATG的距离: coord - ATGsite//CnnnATG<br>
 * 距离本外显子起始 nnnCnn为3个碱基，距离终点为2个碱基<br>
 * 距离本外显子起始 Cnn为0个碱基<br>
 * @author zong0jie
 *
 */
public class GffGeneIsoCis extends GffGeneIsoInfo {
	private static final long serialVersionUID = 8473636267008365629L;
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, String geneParentName, GeneType geneType) {
		super(IsoName, geneParentName, geneType);
		super.setCis5to3(true);
	}
	public GffGeneIsoCis(String IsoName, String geneParentName, GffDetailGene gffDetailGene, GeneType geneType) {
		super(IsoName, geneParentName, gffDetailGene, geneType);
		super.setCis5to3(true);
	}
	
	@Override
	public int getStartAbs() {
		try {
			return get(0).getStartCis();
		} catch (Exception e) {
			return get(0).getStartCis();
		}
		
	}
	@Override
	public int getEndAbs() {
		return get(size() - 1).getEndCis();
	}
	
	@Override
	protected String getGTFformatExon(String title, String strand) {
		String geneExon = "";
		String prefixInfo = getRefID() + "\t" + title + "\t";
		String suffixInfo = "\t" + "." + "\t" + strand + "\t.\t" + "gene_id \"" + getParentGeneName() + "\"; transcript_id " + "\"" + getName()+"\"; " + TxtReadandWrite.ENTER_LINUX;
		int[] atg = getATGLoc();
		int[] uag = getUAGLoc();
		
		for (ExonInfo exons : this) {
			if (atg != null && ATGsite >= exons.getStartAbs() && ATGsite <= exons.getEndAbs()) {
				geneExon = geneExon + prefixInfo + GffHashGTF.startCodeFlag + "\t" + atg[0] + "\t" + atg[1] + suffixInfo;
			}
			geneExon = geneExon + getRefID() + "\t" +title + "\texon\t" + exons.getStartAbs()  + "\t" + exons.getEndAbs() 
		         + suffixInfo;
			if (uag != null && UAGsite >= exons.getStartAbs() && UAGsite <= exons.getEndAbs()) {
				geneExon = geneExon + prefixInfo + GffHashGTF.stopCodeFlag + "\t" + uag[0] + "\t" + uag[1] + suffixInfo;
			}
		}
		return geneExon;
	}
	@Override
	protected String getGFFformatExonMISO(String title, String strand) {
		String geneExon = "";
		for (int i = 0; i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getRefID() + "\t" +title + "\texon\t" + exons.getStartAbs() + "\t" + exons.getEndAbs()
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=exon:" + getName()  + ":" + (i+1) +";Parent=" + getName() + " "+TxtReadandWrite.ENTER_LINUX;
		}
		return geneExon;
	}
	
	private int[] getATGLoc() {
		int[] atginfo = null;
		if (ATGsite > 0) {
			atginfo = new int[2];
			if (isCis5to3()) {
				atginfo[0] = ATGsite;
				atginfo[1] = ATGsite + 2;
			} else {
				atginfo[0] = ATGsite - 2;
				atginfo[1] = ATGsite;
			}
		}
		return atginfo;
	}
	private int[] getUAGLoc() {
		int[] atginfo = null;
		if (UAGsite > 0) {
			atginfo = new int[2];
			if (isCis5to3()) {
				atginfo[0] = UAGsite;
				atginfo[1] = UAGsite + 2;
			} else {
				atginfo[0] = UAGsite - 2;
				atginfo[1] = UAGsite;
			}
		}
		return atginfo;
	}
	
	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis result = null;
		result = (GffGeneIsoCis) super.clone();
		return result;
	
	}

}
