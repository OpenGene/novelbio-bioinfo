package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.snphgvs.EnumVariantClass;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.analysis.seq.snphgvs.SnpIsoHgvsp;
import com.novelbio.analysis.seq.snphgvs.VariantTypeDetector;
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
	/** 缓存队列，用于 */
	List<Allele> lsAllele = new ArrayList<>();
	SnpAnnoFactory snpAnnoFactory = new SnpAnnoFactory();
	
	Map<String, List<GffDetailGene>> mapChrId2LsGenes = new HashMap<>();
	
	TxtReadandWrite txtReadPlinkMap;
	Iterator<String> itPlinkMap;
	Iterator<GffDetailGene> itGenes;
	Allele alleleTmp;
	String chrIdTmp;
	int snpIndex = 1;
	
	boolean isFinish = false;
	
	GffDetailGene gene;
	
	
	public void setGffChrAbs(String chrFile, String gffFile) {
		GffChrAbs gffChrAbs = new GffChrAbs();
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
		snpAnnoFactory.setGffChrAbs(gffChrAbs);
	}
	
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
		txtReadPlinkMap = new TxtReadandWrite(plinkMap);
		itPlinkMap = txtReadPlinkMap.readlines().iterator();
	}
	
	public void initial() {
		lsAllele.clear();
		alleleTmp = new Allele(itPlinkMap.next());
		alleleTmp.setIndex(snpIndex++);
		chrIdTmp = alleleTmp.getRefID();
		itGenes = mapChrId2LsGenes.get(alleleTmp.getRefID()).iterator();
	}
	public GffDetailGene getGene() {
		return gene;
	}
	public boolean isFinish() {
		return isFinish;
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	public List<Allele> readLsAlleles() {
		if (!itGenes.hasNext()) {
			lsAllele.clear();
			while (itPlinkMap.hasNext()) {
				alleleTmp = new Allele(itPlinkMap.next());
				alleleTmp.setIndex(snpIndex++);
				if (!chrIdTmp.equals(alleleTmp.getRefID())) {
					chrIdTmp = alleleTmp.getRefID();
					itGenes = mapChrId2LsGenes.get(alleleTmp.getRefID()).iterator();
					break;
				}
			}
		}
		if (!itPlinkMap.hasNext()) {
			isFinish = true;
			return null;
		}
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
		if (gene == null && !isFinish) {
			return readLsAlleles();
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
			if (allele.getPosition() > gene.getEndAbs()) {
				alleleTmp = allele;
				break;
			}
			lsAlleleResult.add(allele);
		}
		lsAllele = lsAlleleResult;
		
		return getLsSnpsChangeAA(lsAllele, gene);
	}
	
	private List<Allele> getLsSnpsChangeAA(List<Allele> lsAllele, GffDetailGene gene) {
		List<Allele> lsAllelsChange = new ArrayList<>();
		for (Allele allele : lsAllele) {
			boolean isNeedSnp = false;
			SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo(allele.getRefID(), allele.getPosition(), allele.getRefBase(), allele.getAltBase(), gene);
			for (GffGeneIsoInfo iso : snpInfo.getLsIsos()) {
				SnpIsoHgvsp snpIsoHgvsp = snpInfo.getMapIso2Hgvsp().get(iso);
				if (snpIsoHgvsp.isNeedHgvsp()) {
					snpIsoHgvsp.getHgvsp();
				}
				Set<EnumVariantClass> setVar = VariantTypeDetector.getSetVarType(iso, snpInfo);
				setVar.addAll(snpIsoHgvsp.getSetVarType());
				if (setVar.contains(EnumVariantClass.Five_prime_UTR_variant)
						|| setVar.contains(EnumVariantClass.Three_prime_UTR_variant)
						|| setVar.contains(EnumVariantClass.missense_variant)
						|| setVar.contains(EnumVariantClass.splice_acceptor_variant)
						|| setVar.contains(EnumVariantClass.splice_donor_variant)
						) {
					isNeedSnp = true;
				}
			}
			if (isNeedSnp) {
				lsAllelsChange.add(allele);
			}
		}
		return lsAllelsChange;
	}
	
}

