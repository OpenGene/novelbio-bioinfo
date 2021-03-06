package com.novelbio.software.snpanno;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gffchr.GffChrAbs;

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
	
	public SnpInfo generateSnpInfo(String refId, int position, String seqRef, String seqAlt, GffGene gene) {
		SnpInfo snpInfo = new SnpInfo(refId, position, seqRef, seqAlt);
		snpInfo.setGene(gene);
		snpInfo.initial(gffChrAbs.getSeqHash());
		return snpInfo;
	}
	public static List<String[]> getLsAnnotationFinal(SnpInfo snpInfo) {
		List<List<String>> lsLsAnno = getLsAnnotation(snpInfo);
		List<String[]> lsResult = lsLsAnno.stream().map((lsAnno)->lsAnno.toArray(new String[0])).collect(Collectors.toList());
		return lsResult;
	}

	/**
	 * 将获得的snp信息解析成文字并返回
	 * 一行一个转录本
	 * @param snpInfo
	 * @return
	 */
	public static List<List<String>> getLsAnnotation(SnpInfo snpInfo) {
		List<List<String>> lsResult = new ArrayList<>();
		for (GffIso iso : snpInfo.getLsIsos()) {
			lsResult.add(getIsoAnno(snpInfo, iso));
		}
		return lsResult;
	}
	
	public static List<String> getIsoAnno(SnpInfo snpInfo, GffIso iso) {
		List<String> lsResult = new ArrayList<>();
		SnpIsoHgvsc snpIsoHgvsc = snpInfo.getMapIso2Hgvsc().get(iso);
		SnpIsoHgvsp snpIsoHgvsp = snpInfo.getMapIso2Hgvsp().get(iso);
		lsResult.add(iso.getName());
		lsResult.add(iso.getParentGeneName());
		lsResult.add(snpIsoHgvsc.getHgvsc());
		String hgvsp = snpIsoHgvsp.fillAndGetHgvsp();
		lsResult.add(hgvsp);
		Set<EnumVariantClass> setVar = VariantTypeDetector.getSetVarType(iso, snpInfo);
		setVar.addAll(snpIsoHgvsp.getSetVarType());
		lsResult.add(VariantTypeDetector.mergeVars(setVar));
		if (snpIsoHgvsp.isNeedHgvsp()) {
			lsResult.add(snpIsoHgvsp.getAAattrConvert());
		}
		return lsResult;
	}
	
	public static List<String> getLsTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add("IsoName");
		lsTitle.add("GeneName");
		lsTitle.add("HGVS.c");
		lsTitle.add("HGVS.p");
		lsTitle.add("VariationType");
		lsTitle.add("ChamicalConvert");
		return lsTitle;
	}
	
}
