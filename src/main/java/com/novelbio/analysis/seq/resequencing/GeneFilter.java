package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

/**
 * 因为snp一般不会发生在相同的位点，而更可能发生在相同的基因。
 * 也就是说一个snp可能只有一个样本突变，
 * 而一个基因可能有多个样本发生突变。
 * 所以用这个做一个过滤器
 * 用来筛选出大部分样本都发生突变的那个基因
 * @author zong0jie
 */
public class GeneFilter {
	private final static Logger logger = Logger.getLogger(GeneFilter.class);
	GffChrAbs gffChrAbs;
	
	/**
	 * 这个是输入的RefSiteSnpIndel
	 * key: site转化为string
	 * value: snp位点
	 */
	HashMap<String, RefSiteSnpIndel> mapSiteInfo2SnpIndel = new HashMap<String, RefSiteSnpIndel>();
	ArrayListMultimap<GeneID, RefSiteSnpIndel> mapGeneID2LsRefSiteSnpIndel = ArrayListMultimap.create();
	/**
	 * 结果map，只有当结果map为null的时候，才会启动过滤
	 * key：发生突变的样本数量，倒序排列，也就是数量从大到小排列
	 * value：具体发生突变的一系列RefSiteSnpIndel
	 */
	TreeMap<Integer, List<RefSiteSnpIndel>> mapNum2LsMapSnpIndelInfo;
	
	//样本名
	Set<String> setSampleName = new HashSet<String>();
	
	/** 实验组通过过滤的最少数目
	 * 就是说只要某个基因有>=该数量的样本通过质检，就认为该gene合格
	 *  */
	int sampleFilteredMinNum = 0;
	
	/** 过滤单个snpSite位点的过滤器 */
	SnpFilter snpFilterSingleSite = new SnpFilter();
	
	SnpLevel snpLevel = SnpLevel.HetoMid;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		mapNum2LsMapSnpIndelInfo = null;
	}
	public void setSpecies(Species species) {
		if (gffChrAbs == null) {
			gffChrAbs = new GffChrAbs();
		}
		gffChrAbs.setSpecies(species);
	}
	/**
	 * 设置基因过滤器的level
	 * @param snpLevel  SnpGroupFilterInfo.HetoLess 等
	 */
	public void setSnpLevel(SnpLevel snpLevel) {
		snpFilterSingleSite.setSampleFilterInfoSingle(snpLevel);
		mapNum2LsMapSnpIndelInfo = null;
	}
	/**
	 * 添加已经经过筛选的refSiteSnpIndel，里面只保留有通过过滤的snp。按照样本过滤
	 * @param refSiteSnpIndel 重复添加的话，后面的会覆盖前面的
	 */
	public void addLsRefSiteSnpIndel(Collection<RefSiteSnpIndel> colRefSiteSnpIndels) {
		for (RefSiteSnpIndel refSiteSnpIndel : colRefSiteSnpIndels) {
			addRefSiteSnpIndel(refSiteSnpIndel);
		}
	}
	/**
	 * 不要重复添加
	 * 添加已经经过筛选的refSiteSnpIndel，里面只保留有通过过滤的snp。按照样本过滤
	 * @param refSiteSnpIndel 重复添加的话，后面的会覆盖前面的
	 */
	public void addRefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		//TODO 最好添加refsitesnpindel中仅包含过滤好的site的map
		refSiteSnpIndel.setGffChrAbs(gffChrAbs);
		mapSiteInfo2SnpIndel.put(getRefSiteSnpIndelStr(refSiteSnpIndel), refSiteSnpIndel);
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/** 检验哪些样本名，样本名要和snp过滤的样本名一致 */
	public void addSampleName(String colSampleName) {
		setSampleName.add(colSampleName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	/** 检验哪些样本名，样本名要和snp过滤的样本名一致 */
	public void addSampleName(Collection<String> colSampleName) {
		setSampleName.addAll(colSampleName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	public void setSampleName(Collection<String> colSampleName) {
		setSampleName = new HashSet<String>(colSampleName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/** 超过几个treat含有该gene就认为通过了 */
	public void setSampleFilteredNum(int sampleFilteredNum) {
		this.sampleFilteredMinNum = sampleFilteredNum;
	}
	/** 获得设定的treatmentName */
	public Set<String> getSetSampleName() {
		return setSampleName;
	}
	public ArrayList<RefSiteSnpIndel> filterSnpInGene() {
		ArrayList<RefSiteSnpIndel> lsResult = new ArrayList<RefSiteSnpIndel>();
		if (mapNum2LsMapSnpIndelInfo == null) {
			setMapGeneID2LsRefSiteSnpIndel();
			sortBySampleNum();
		}
		for (Integer filteredTreatNum : mapNum2LsMapSnpIndelInfo.keySet()) {
			if (filteredTreatNum < sampleFilteredMinNum) {
				continue;
			}
			lsResult.addAll(mapNum2LsMapSnpIndelInfo.get(filteredTreatNum));
		}
		return lsResult;
	}
	
	
	/** 把输入的refInfoSnpIndel按照基因名字整理起来 */
	private void setMapGeneID2LsRefSiteSnpIndel() {
		for (RefSiteSnpIndel refInfoSnpIndel : mapSiteInfo2SnpIndel.values()) {
			if (refInfoSnpIndel.getGffIso() == null) {
				continue;
			}
			GeneID geneID = refInfoSnpIndel.getGffIso().getGeneID();
			mapGeneID2LsRefSiteSnpIndel.put(geneID, refInfoSnpIndel);
		}
	}
	
	/** 按照sample的样本数量排序，就是越多样本含有该基因就把他跳出来，然后保存进入treemap */
	private TreeMap<Integer, List<RefSiteSnpIndel>> sortBySampleNum() {
		//倒序排列的treemap
		mapNum2LsMapSnpIndelInfo =
				new TreeMap<Integer, List<RefSiteSnpIndel>>(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return -o1.compareTo(o2);
					}
		});
		
		for (GeneID geneID : mapGeneID2LsRefSiteSnpIndel.keySet()) {
			List<RefSiteSnpIndel> lsSnpIndels = mapGeneID2LsRefSiteSnpIndel.get(geneID);
			int filteredSampleNum = getFilteredSampleNum(lsSnpIndels);
			mapNum2LsMapSnpIndelInfo.put(filteredSampleNum, lsSnpIndels);
		}
		return mapNum2LsMapSnpIndelInfo;
	}
	
	/** 输入的必须是过滤后只剩下causal snp的RefSiteSnpIndel */
	private int getFilteredSampleNum(List<RefSiteSnpIndel> lsSnpIndels) {
		HashSet<String> setSampleName = new HashSet<String>();
		for (RefSiteSnpIndel refSiteSnpIndel : lsSnpIndels) {

			//第一 我们只能找那些筛选出来的snp，没有筛选出来的就不要考虑。
			//第二 对于筛选出来的snp，我们依然要遍历每一个样本，看该snp是否超过阈值
			//获得前面筛选通过的snp类型
			for (SiteSnpIndelInfo siteSnpIndelInfo : refSiteSnpIndel.mapAllen2Num.values()) {
				for (String treatName : setSampleName) {
					siteSnpIndelInfo.setSampleName(treatName);
					if (snpFilterSingleSite.isFilterdSnp(siteSnpIndelInfo)) {
						setSampleName.add(treatName);
					}
				}
			}
		}
		return setSampleName.size();
	}
	
	/** 给定MapInfoSnpIndel，返回其坐标所对应的string，用于做hashmap的key */
	private static String getRefSiteSnpIndelStr(RefSiteSnpIndel refSiteSnpIndel) {
		return refSiteSnpIndel.getRefID() + SepSign.SEP_ID + refSiteSnpIndel.getRefSnpIndelStart();
	}

}
