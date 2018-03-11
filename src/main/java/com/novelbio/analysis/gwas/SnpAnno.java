package com.novelbio.analysis.gwas;

import java.util.HashSet;
import java.util.Set;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.snphgvs.EnumVariantClass;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.analysis.seq.snphgvs.SnpIsoHgvsp;
import com.novelbio.analysis.seq.snphgvs.VariantTypeDetector;

public class SnpAnno {
	SnpAnnoFactory snpAnnoFactory = new SnpAnnoFactory();
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		snpAnnoFactory.setGffChrAbs(gffChrAbs);
	}
	
	/**
	 * 获得改变iso的snp，以及相应的iso的名字
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	public Set<String> getSetIsoName(Allele allele, GffDetailGene gene) {
		if (!isAlleleInGene(allele, gene)) {
			return new HashSet<>();
		}
		SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo(allele.getRefID(), allele.getPosition(), allele.getRefBase(), allele.getAltBase(), gene);
		return getSetIsoName(snpInfo);
	}
	/**
	 * 获得改变iso的snp，以及相应的iso的名字
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	public Set<String> getSetIsoName(Allele allele) {
		SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo(allele.getRefID(), allele.getPosition(), allele.getRefBase(), allele.getAltBase());
		return getSetIsoName(snpInfo);
	}
	/**
	 * 获得改变iso的snp，以及相应的iso的名字
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	private Set<String> getSetIsoName(SnpInfo snpInfo) {
		Set<String> setIsoName = new HashSet<>();
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
				setIsoName.add(iso.getName());
			}
		}
		return setIsoName;
	}
	
	private boolean isAlleleInGene(Allele allele, GffDetailGene gene) {
		if (allele == null || !allele.getRefID().equals(gene.getRefID())) {
			return false;
		}
		return allele.getPosition() >= gene.getStartAbs() && allele.getPosition() <= gene.getEndAbs();
	}
}
