package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;
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
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		super(IsoName, gffDetailGene, geneType);
	}

	public GffGeneIsoCis(String IsoName, String ChrID, int coord, String geneType) {
		super(IsoName, ChrID, coord, geneType);
	}
	public GffGeneIsoCis(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
	}
	
	


	
	




	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.clone(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}


	@Override
	public Boolean isCis5to3() {
		return true;
	}
	
	@Override
	public GffGeneIsoCis cloneDeep() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.cloneDeep(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}

	@Override
	public int getStartAbs() {
		return get(0).getStartCis();
	}

	@Override
	public int getEndAbs() {
		return get(size() - 1).getEndCis();
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title,
			String strand) {
		String geneExon = "";
		for (int i = 0; i < getIsoInfo().size(); i++) {
			ExonInfo exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs() + "\t" + exons.getEndAbs()
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=exon:" + getIsoName()  + ":" + (i+1) +";Parent=" + getIsoName() + " \r\n";
		}
		return geneExon;
	
	}
	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (ExonInfo exons : getIsoInfo()) {
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs()  + "\t" + exons.getEndAbs() 
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getIsoName()+"\"; \r\n";
		}
		return geneExon;
	}
	
}
