package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 因为snp一般不会发生在相同的位点，而更可能发生在相同的基因，所以用这个做一个过滤器
 * 用来筛选出大部分样本都发生突变的那个基因
 * @author zong0jie
 */
public class GeneFilter {
	
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
	Set<String> setTreat = new HashSet<String>();
	
	/** 实验组通过过滤的最少数目
	 * 就是说只要某个基因有>=该数量的样本通过质检，就认为该gene合格
	 *  */
	int treatFilteredMinNum = 0;
	
	/** 过滤单个snpSite位点的过滤器 */
	SnpFilter snpFilterSingleSite = new SnpFilter();
	
	int snpLevel = SnpGroupFilterInfo.Heto;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/**
	 * 设置基因过滤器的level
	 * @param snpLevel  SnpGroupFilterInfo.HetoLess 等
	 */
	public void setSnpLevel(int snpLevel) {
		snpFilterSingleSite.setSampleFilterInfoSingle(snpLevel);
		mapNum2LsMapSnpIndelInfo = null;
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
	
	public void addTreatName(String treatName) {
		setTreat.add(treatName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	public void addTreatName(Collection<String> colTreatName) {
		setTreat.addAll(colTreatName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/** 超过几个treat含有该gene就认为通过了 */
	public void setTreatFilteredNum(int treatFilteredNum) {
		this.treatFilteredMinNum = treatFilteredNum;
	}
	
	public ArrayList<RefSiteSnpIndel> filterSnpInGene() {
		ArrayList<RefSiteSnpIndel> lsResult = new ArrayList<RefSiteSnpIndel>();
		if (mapNum2LsMapSnpIndelInfo == null) {
			setMapGeneID2LsRefSiteSnpIndel();
			sortByTreatSampleNum();
		}
		for (Integer filteredTreatNum : mapNum2LsMapSnpIndelInfo.keySet()) {
			if (filteredTreatNum < treatFilteredMinNum) {
				break;
			}
			lsResult.addAll(mapNum2LsMapSnpIndelInfo.get(filteredTreatNum));
		}
		return lsResult;
	}
	
	
	/** 把输入的refInfoSnpIndel按照基因名字整理起来 */
	private void setMapGeneID2LsRefSiteSnpIndel() {
		for (RefSiteSnpIndel refInfoSnpIndel : mapSiteInfo2SnpIndel.values()) {
			GeneID geneID = refInfoSnpIndel.getGffIso().getGeneID();
			mapGeneID2LsRefSiteSnpIndel.put(geneID, refInfoSnpIndel);
		}
	}
	
	/** 按照treat的样本数量排序，就是越多样本含有该基因就把他跳出来，然后保存进入treemap */
	private TreeMap<Integer, List<RefSiteSnpIndel>> sortByTreatSampleNum() {
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
			int filteredSampleNum = getTreatNum(lsSnpIndels);
			mapNum2LsMapSnpIndelInfo.put(filteredSampleNum, lsSnpIndels);
		}
		return mapNum2LsMapSnpIndelInfo;
	}
	
	/** 输入的必须是过滤后只剩下causal snp的RefSiteSnpIndel */
	private int getTreatNum(List<RefSiteSnpIndel> lsSnpIndels) {
		HashSet<String> setTreatName = new HashSet<String>();
		for (RefSiteSnpIndel refSiteSnpIndel : lsSnpIndels) {

			//第一 我们只能找哪些筛选出来的snp，没有筛选出来的就不要考虑。
			//第二 对于筛选出来的snp，我们依然要遍历每一个样本，看该snp是否超过阈值
			//获得前面筛选通过的snp类型
			for (SiteSnpIndelInfo siteSnpIndelInfo : refSiteSnpIndel.mapAllen2Num.values()) {
				for (String treatName : setTreatName) {
					siteSnpIndelInfo.setSampleName(treatName);
					if (snpFilterSingleSite.isFilterdSnp(siteSnpIndelInfo)) {
						setTreatName.add(treatName);
					}
				}
			}
		}
		return setTreatName.size();
	}
	
	/** 给定MapInfoSnpIndel，返回其坐标所对应的string，用于做hashmap的key */
	private static String getRefSiteSnpIndelStr(RefSiteSnpIndel refSiteSnpIndel) {
		return refSiteSnpIndel.getRefID() + SepSign.SEP_ID + refSiteSnpIndel.getRefSnpIndelStart();
	}

}
