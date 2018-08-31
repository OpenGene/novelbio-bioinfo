package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
/**
 * ID转换，已知两个gff文件，将一个gff与另一个gff进行比对，找到相同的基因然后做ID转换。
 * @author zong0jie
 * 
 */
public class GffIDconvert {
	/** 待转化的gff */
	GffHashGene gffHashGeneQuery = null;
	/** 目的gff */
	GffHashGene gffHashGeneSub= null;
	/** 输出结果，query geneId 和 subject geneId的对照表
	 * key query2subject，意思一对基因只能出现一次
	 * value 0: queryGene 1: subjuectGene
	 */
	Map<String, String[]> mapQgene2Sgene = new HashMap<>();
	/**
	 * 首先用QueryHash查找SubjectHash，找完之后再用SubjectHash，在本set中不存在的geneName，找QueryHash
	 * 这样保证找全
	 */
	Set<String> setGeneAlreadFind = new HashSet<String>();
	public void setGffHashGeneQuery(GffHashGene gffHashGeneQuery) {
		this.gffHashGeneQuery = gffHashGeneQuery;
	}
	public void setGffHashGeneSub(GffHashGene gffHashGeneSub) {
		this.gffHashGeneSub = gffHashGeneSub;
	}
	
	public void convert() {
		setGeneAlreadFind.clear();
		mapQgene2Sgene.clear();
		for (GffGene gffDetailGene : gffHashGeneQuery.getLsGffDetailGenes()) {
			searchGffGene(gffDetailGene, gffHashGeneSub, true);
		}
		
		for (GffGene gffDetailGene : gffHashGeneSub.getLsGffDetailGenes()) {
			if (setGeneAlreadFind.contains(gffDetailGene.getNameSingle())) {
				continue;
			}
			searchGffGene(gffDetailGene, gffHashGeneQuery, false);
		}
	}
	
	/**
	 * 给定query的geneIso的信息，和查找Destination的结果，将结果进行升级
	 * @param gffGeneIsoInfo
	 * @param gffCodGeneDU
	 * @param q2s 是query 到 subject 还是 subject 到 query
	 */
	private void searchGffGene(GffGene gffDetailGene, GffHashGene gffHash, boolean q2s) {
		String chrId = gffDetailGene.getRefID();
		int start = gffDetailGene.getStartAbs(), end = gffDetailGene.getEndAbs();
		String keyQ = gffDetailGene.getNameSingle();
		setGeneAlreadFind.add(keyQ);
		GffCodGeneDU gffCodGeneDU = gffHash.searchLocation(chrId, start, end);
		if (gffCodGeneDU == null) {
			return;
		}
		Set<GffGene> setGenes = gffCodGeneDU.getCoveredOverlapGffGene();

		if (!setGenes.isEmpty()) {
			for (GffGene gffDetailGeneSub : setGenes) {
				for (GffGene gffDetailGeneSubFinal : gffDetailGeneSub.getlsGffDetailGenes()) {
					String keyS = gffDetailGeneSubFinal.getNameSingle();
					if (q2s) {
						String keyQ2S = keyQ + SepSign.SEP_ID + keyS;
						mapQgene2Sgene.put(keyQ2S, new String[]{keyQ, keyS});
					} else {
						String keyQ2S = keyS + SepSign.SEP_ID + keyQ;
						mapQgene2Sgene.put(keyQ2S, new String[]{keyS, keyQ});
					}
					setGeneAlreadFind.add(keyS);
				}
			}
		} else {
			if (q2s) {
				mapQgene2Sgene.put(keyQ, new String[]{keyQ, ""});
			} else {
				mapQgene2Sgene.put(keyQ, new String[]{"", keyQ});
			}
		}
	}
	
	public List<String[]> getLsResultTab() {
		List<String[]> lsResultTab = new ArrayList<>();
		for (String[] q2s : mapQgene2Sgene.values()) {
			lsResultTab.add(q2s);
		}
		return lsResultTab;
	}
	
}
