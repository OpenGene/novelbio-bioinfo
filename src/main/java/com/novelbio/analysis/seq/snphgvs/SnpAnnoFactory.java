package com.novelbio.analysis.seq.snphgvs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public class SnpAnnoFactory {
	GffChrAbs gffChrAbs;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	public SnpInfo generateSnpInfo(String refId, int position, String seqRef, String seqAlt) {
		SnpInfo snpInfo = new SnpInfo(refId, position, seqRef, seqAlt);
		snpInfo.setGffHashGene(gffChrAbs.getGffHashGene());
		snpInfo.initial(gffChrAbs.getSeqHash());
		return snpInfo;
	}
	
	public SnpInfo generateSnpInfo(String refId, int position, String seqRef, String seqAlt, GffDetailGene gene) {
		SnpInfo snpInfo = new SnpInfo(refId, position, seqRef, seqAlt);
		snpInfo.setGene(gene);
		snpInfo.initial(gffChrAbs.getSeqHash());
		return snpInfo;
	}
	
	/**
	 * 将获得的snp信息解析成文字并返回
	 * 一行一个转录本
	 * @param snpInfo
	 * @return
	 */
	public List<List<String>> getLsAnnotation(SnpInfo snpInfo) {
		List<List<String>> lsResult = new ArrayList<>();
		for (GffGeneIsoInfo iso : snpInfo.getLsIsos()) {
			lsResult.add(getIsoAnno(snpInfo, iso));
		}
		return lsResult;
	}
	
	private List<String> getIsoAnno(SnpInfo snpInfo, GffGeneIsoInfo iso) {
		List<String> lsResult = new ArrayList<>();
		SnpIsoHgvsc snpIsoHgvsc = snpInfo.getMapIso2Hgvsc().get(iso);
		SnpIsoHgvsp snpIsoHgvsp = snpInfo.getMapIso2Hgvsp().get(iso);
		lsResult.add(iso.getName());
		lsResult.add(iso.getParentGeneName());
		lsResult.add(snpIsoHgvsc.getHgvsc());
		String hgvsp = snpIsoHgvsp.isNeedHgvsp() ? snpIsoHgvsp.getHgvsp() : "";
		lsResult.add(hgvsp);
		Set<EnumVariantClass> setVar = VariantTypeDetector.getSetVarType(iso, snpInfo);
		setVar.addAll(snpIsoHgvsp.getSetVarType());
		lsResult.add(VariantTypeDetector.mergeVars(setVar));
		return lsResult;
	}
	
	public static List<String> getLsTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add("IsoName");
		lsTitle.add("GeneName");
		lsTitle.add("HGVS.c");
		lsTitle.add("HGVS.p");
		lsTitle.add("VariationType");
		return lsTitle;
	}
	
}
