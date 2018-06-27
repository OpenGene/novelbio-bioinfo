package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.analysis.seq.snphgvs.EnumVariantClass;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.analysis.seq.snphgvs.SnpIsoHgvsp;
import com.novelbio.analysis.seq.snphgvs.VariantTypeDetector;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 按照基因读取PlinkMap
 * 一次读取一个基因中的全体snp
 * @author zong0jie
 * @data 2018年3月10日
 */
public class PlinkMapReader {
	private static final Logger logger = LoggerFactory.getLogger(PlinkMapReader.class);
	
	int tss = 1500;
	
	/**
	 * 每个位点的坐标信息<br>
	 * chrId name	other	location<br>
	 * 1	10100001579	0	1579<br>
	 * 1	10100003044	0	3044<br>
	 * 
	 * 其中第二列和第三列不用管，只需要根据第一列和第四列去提取信息即可
	 */
	String plinkMap;
	
	Map<String, List<GffDetailGene>> mapChrId2LsGenes = new HashMap<>();
	
	TxtReadandWrite txtReadPlinkMap;
	Iterator<String> itPlinkMap;
	Iterator<GffDetailGene> itGenes;
	Allele alleleLast;
	String chrIdTmp;
	int snpIndex = 1;
	
	boolean isFinish = false;
	
	GffDetailGene geneCurrent;
	List<Allele> lsAlleleTmp = new ArrayList<>();
	List<Allele> lsAlleleCurrent = new ArrayList<>();
	
	Map<Allele, Set<String>> mapCurrentSnp2SetIsoName = new LinkedHashMap<>();
	
	SnpAnno snpAnno = new SnpAnno();
	public void setTss(int tss) {
		this.tss = tss;
	}
	public void setGffChrAbs(String chrFile, String gffFile) {
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		
		setGenes(gffChrAbs);
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		setGenes(gffChrAbs);
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	
	private void setGenes(GffChrAbs gffChrAbs) {
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLsGffDetailGenes()) {		
			List<GffDetailGene> lsGenes = mapChrId2LsGenes.get(gffDetailGene.getRefID());
			if (lsGenes == null) {
				lsGenes = new ArrayList<>();
				mapChrId2LsGenes.put(gffDetailGene.getRefID(), lsGenes);
			}
			GffDetailGene gffDetailGeneTss = new GffDetailGene();
			gffDetailGeneTss.setParentName(gffDetailGene.getRefID());
			gffDetailGeneTss.addItemName(gffDetailGene.getNameSingle() + ".tss");
			gffDetailGeneTss.setCis5to3(gffDetailGene.isCis5to3());
			if (gffDetailGene.isCis5to3()) {
				int start = gffDetailGene.getStartAbs()-tss;
				if (start < 1) start = 1;
				gffDetailGeneTss.setStartAbs(start);
				gffDetailGeneTss.setEndAbs(gffDetailGene.getStartAbs());
				if (tss>0) {
					lsGenes.add(gffDetailGeneTss);
				}
				lsGenes.add(gffDetailGene);
			} else {
				if (tss>0) {
					lsGenes.add(gffDetailGeneTss);
				}
				gffDetailGeneTss.setStartAbs(gffDetailGene.getEndAbs());
				gffDetailGeneTss.setEndAbs(gffDetailGene.getEndAbs() + tss);
				lsGenes.add(gffDetailGene);
			}
		
		}
		for (List<GffDetailGene> lsGenes : mapChrId2LsGenes.values()) {
			Collections.sort(lsGenes, (gene1, gene2) -> {return ((Integer)gene1.getStartAbs()).compareTo(gene2.getStartAbs());});
		}
	}
	
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
		txtReadPlinkMap = new TxtReadandWrite(plinkMap);
		itPlinkMap = txtReadPlinkMap.readlines().iterator();
	}
	
	public void initial() {
		lsAlleleTmp.clear();
		alleleLast = new Allele(itPlinkMap.next());
		alleleLast.setIndex(snpIndex++);
		chrIdTmp = alleleLast.getRefID();
		itGenes = mapChrId2LsGenes.get(alleleLast.getRefID()).iterator();
	}
	
	public GffDetailGene getGeneCurrent() {
		return geneCurrent;
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	public Map<Allele, Set<String>> getLsAllelesCurrentGene() {
		return mapCurrentSnp2SetIsoName;
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	protected List<Allele> getLsAllelesCurrent() {
		return lsAlleleCurrent;
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	public boolean readNext() {
		if (isFinish) {
			return false;
		}
		readNextLsAllele();
		mapCurrentSnp2SetIsoName = getMapSnp2SetIsoName(lsAlleleTmp, geneCurrent);
		return true;
	}
	
	@VisibleForTesting
	protected void readNextLsAllele() {
		lsAlleleCurrent.clear();
		//读完一条染色体后，根据PlinkMap的内容换下一条染色体
		if (!itGenes.hasNext()) {
			lsAlleleTmp.clear();
			while (itPlinkMap.hasNext()) {
				alleleLast = new Allele(itPlinkMap.next());
				alleleLast.setIndex(snpIndex++);
				if (!chrIdTmp.equals(alleleLast.getRefID())) {
					chrIdTmp = alleleLast.getRefID();
					itGenes = mapChrId2LsGenes.get(alleleLast.getRefID()).iterator();
					lsAlleleTmp.clear();
					alleleLast = null;
					break;
				}
			}
		}
		if (!itPlinkMap.hasNext()) {
			isFinish = true;
			return;
 		}
		List<Allele> lsAllelesResult = new ArrayList<>();
		
		while (itGenes.hasNext()) {
			geneCurrent = itGenes.next();
			if (!lsAlleleTmp.isEmpty() && lsAlleleTmp.get(lsAlleleTmp.size()-1).getPosition() > geneCurrent.getStartAbs()) {
				for (Allele allele : lsAlleleTmp) {
					if (isAlleleLargerThanGene(allele, geneCurrent)) {
						lsAllelesResult.add(allele);
					}
					if (isAlleleInGene(allele, geneCurrent)) {
						lsAlleleCurrent.add(allele);
					}
				}
				lsAlleleTmp.clear();
			}
			if (alleleLast == null 
					|| !alleleLast.getRefID().equals(geneCurrent.getRefID())
					|| alleleLast.getPosition() <= geneCurrent.getEndAbs()
					|| !lsAllelesResult.isEmpty()
					) {
				break;
			}
			geneCurrent = null;
		}
		//说明itGenes已经空了，alleleTmp.getPosition() > 最后一个gene的终点
		if (geneCurrent == null && !isFinish) {
			readNextLsAllele();
			return;
		}
		if (isAlleleInGene(alleleLast, geneCurrent)) {
			lsAllelesResult.add(alleleLast);
			lsAlleleCurrent.add(alleleLast);
		} else if (alleleLast != null && alleleLast.getPosition() > geneCurrent.getEndAbs()) {
			lsAlleleTmp = lsAllelesResult;
			return;
		}

		alleleLast = null;
		while (itPlinkMap.hasNext()) {
			String content = itPlinkMap.next();
			Allele allele = new Allele(content);
			allele.setIndex(snpIndex++);
			if (!allele.getRefID().equals(geneCurrent.getRefID())) {
				alleleLast = allele;
				chrIdTmp = alleleLast.getRefID();
				itGenes = mapChrId2LsGenes.get(alleleLast.getRefID()).iterator();
				if (!lsAllelesResult.isEmpty()) {
					lsAlleleTmp = lsAllelesResult;
					return;
				}
				readNextLsAllele();
				return;
			}
			if (allele.getPosition() < geneCurrent.getStartAbs()) {
				continue;
			}
			if (allele.getPosition() > geneCurrent.getEndAbs()) {
				alleleLast = allele;
				break;
			}
			lsAllelesResult.add(allele);
			lsAlleleCurrent.add(allele);
		}
		lsAlleleTmp = lsAllelesResult;
	}
	
	private boolean isAlleleInGene(Allele allele, GffDetailGene gene) {
		if (allele == null || !allele.getRefID().equals(gene.getRefID())) {
			return false;
		}
		return allele.getPosition() >= gene.getStartAbs() && allele.getPosition() <= gene.getEndAbs();
	}
	
	private boolean isAlleleLargerThanGene(Allele allele, GffDetailGene gene) {
		if (allele == null || !allele.getRefID().equals(gene.getRefID())) {
			return false;
		}
		return allele.getPosition() >= gene.getStartAbs();
	}
	
	/**
	 * 获得改变iso的snp，以及相应的iso的名字
	 * 落在内含子中的snp就不要了
	 * 注意这里如果是tss区域的snp，则都要
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	private Map<Allele, Set<String>> getMapSnp2SetIsoName(List<Allele> lsAllele, GffDetailGene gene) {
		Map<Allele, Set<String>> mapSnp2SetIsoName = new LinkedHashMap<>();
		for (Allele allele : lsAllele) {
			if (!isAlleleInGene(allele, gene)) {
				continue;
			}
			Set<String> setIsoName = null;
			if (gene.getNameSingle().endsWith(".tss")) {
				setIsoName = new HashSet<>();
				setIsoName.add(gene.getNameSingle());
			} else {
				setIsoName = snpAnno.getSetIsoName(allele, gene);
			}
			if (!setIsoName.isEmpty()) {
				mapSnp2SetIsoName.put(allele, setIsoName);
			}
		}
		return mapSnp2SetIsoName;
	}
	
}

