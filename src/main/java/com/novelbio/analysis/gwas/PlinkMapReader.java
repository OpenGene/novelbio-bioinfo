package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.poi.hssf.record.PageBreakRecord.Break;

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
	GffChrAbs gffChrAbs;

	/** 缓存队列，用于 */
	List<Allele> lsAllele = new ArrayList<>();
	GffDetailGene gffGene = null;
	String chrId;
	
	Map<String, List<GffDetailGene>> mapChrId2LsGenes = new HashMap<>();
	
	TxtReadandWrite txtReadSite;
	Iterator<String> itPlinkMap;
	Iterator<String> itPlinkMapTmp;
	Iterator<GffDetailGene> itGenes;
	GffDetailGene geneTmp;
	Allele alleleTmp;
	String chrIdTmp;
	int snpIndex = 1;
	
	public void setGffChrAbs(String chrFile, String gffFile) {
		this.gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLsGffDetailGenes()) {
			List<GffDetailGene> lsGenes = mapChrId2LsGenes.get(gffDetailGene.getRefID());
			if (lsGenes == null) {
				lsGenes = new ArrayList<>();
				mapChrId2LsGenes.put(gffDetailGene.getRefID(), lsGenes);
			}
			lsGenes.add(gffDetailGene);
		}
		for (List<GffDetailGene> lsGenes : mapChrId2LsGenes.values()) {
			Collections.sort(lsGenes, (gene1, gene2) -> {return ((Integer)gene1.getStartAbs()).compareTo(gene2.getStartAbs());});
		}
	}
	
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
		txtReadSite = new TxtReadandWrite(plinkMap);
		itPlinkMap = txtReadSite.readlines().iterator();
	}
	
	public void initial(){
		lsAllele.clear();
		alleleTmp = new Allele(itPlinkMap.next());
		alleleTmp.setIndex(snpIndex++);
		chrIdTmp = alleleTmp.getRefID();
		itGenes = mapChrId2LsGenes.get(alleleTmp.getRefID()).iterator();
	}
	
	public List<Allele> getLsAllele() {
		return lsAllele;
	}
	
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	public void readLsAlleles() {
		if (!itGenes.hasNext()) {
			lsAllele.clear();
			while (itPlinkMap.hasNext()) {
				alleleTmp = new Allele(itPlinkMap.next());
				alleleTmp.setIndex(snpIndex++);
				if (!chrIdTmp.equals(alleleTmp.getRefID())) {
					chrIdTmp = alleleTmp.getRefID();
					itGenes = mapChrId2LsGenes.get(alleleTmp.getRefID()).iterator();
				}
			}
		}
		if (!itPlinkMap.hasNext()) {
			return;
		}
		GffDetailGene gene = null;
		while (itGenes.hasNext()) {
			gene = itGenes.next();
			if (alleleTmp.getPosition() > gene.getEndAbs()) {
				if (!lsAllele.isEmpty()) {
					lsAllele.clear();
				}
			} else {
				break;
			}
			gene = null;
		}
		//说明itGenes已经空了，alleleTmp.getPosition() > 最后一个gene的终点
		if (gene == null) {
			return;
		}
		lsAllele.add(alleleTmp);

		List<Allele> lsAlleleResult = new ArrayList<>();
		for (Allele allel : lsAllele) {
			if (allel.getPosition() >= gene.getStartAbs() && allel.getPosition() <= gene.getEndAbs()) {
				lsAlleleResult.add(allel);
			}
		}
		alleleTmp = null;
		while (itPlinkMap.hasNext()) {
			String content = itPlinkMap.next();
			Allele allele = new Allele(content);
			allele.setIndex(snpIndex++);
			if (!allele.getRefID().equals(gene.getRefID())) {
				alleleTmp = allele;
				chrIdTmp = alleleTmp.getRefID();
				itGenes = mapChrId2LsGenes.get(alleleTmp.getRefID()).iterator();
				break;
			}
			if (allele.getPosition() < gene.getStartAbs()) {
				continue;
			}
			if (allele.getPosition() > gene.getStartAbs()) {
				alleleTmp = allele;
				break;
			}
			lsAlleleResult.add(alleleTmp);
		}
		lsAllele = lsAlleleResult;
	}
	
	private void getSnpsChangeAA(List<Allele> lsAllele) {
		for (Allele allele : lsAllele) {
			//TODO 挑选出修改了aa的snp
		}
	}
	
	public void extractSnp() {
		TxtReadandWrite txtReadSite = new TxtReadandWrite(plinkMap);
		int i = 1;
		for (String content : txtReadSite.readlines()) {
			Allele allele = new Allele(content);
			allele.setIndex(i++);
			if (chrId == null || !allele.getRefID().equalsIgnoreCase(chrId)) {
				
			}
		}
	}
	
}

