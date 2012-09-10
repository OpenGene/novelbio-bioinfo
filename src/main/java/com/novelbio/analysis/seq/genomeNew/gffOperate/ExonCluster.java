package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;

public class ExonCluster {
	ExonCluster exonClusterBefore;
	ExonCluster exonClusterAfter;
	
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	ArrayList<ExonInfo> lsCombExon;
	/**
	 * list--所有isoform
	 * list--每个isoform中该组的所有exon
	 * 如果该iso跳过了这个exon，则里面装空的list
	 */
	ArrayList<ArrayList<ExonInfo>> lsIsoExon = new ArrayList<ArrayList<ExonInfo>>();
	ArrayList<GffGeneIsoInfo> lsIsoParent = new ArrayList<GffGeneIsoInfo>();
	HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> mapIso2LsExon = new HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>>();
	/**
	 * 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号<br>
	 */
	HashMap<String, Integer> mapIso2ExonNumSkipTheCluster = new HashMap<String, Integer>();
	
	public ExonCluster(String chrID, int start, int end) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
	}
	public void setExonClusterBefore(ExonCluster exonClusterBefore) {
		this.exonClusterBefore = exonClusterBefore;
	}
	public void setExonClusterAfter(ExonCluster exonClusterAfter) {
		this.exonClusterAfter = exonClusterAfter;
	}
	public String getLocInfo() {
		return chrID + ":" + startLoc + "-" + endLoc;
	}
	public int getLength() {
		return Math.abs(endLoc - startLoc);
	}
	/** 返回其所在的GffGene */
	public GffDetailGene getParentGene() {
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).getParent().getParentGffDetailGene();
			}
		}
		return null;
	}
	
	/**
	 * 如果该iso跳过了这个exon，则里面装空的list
	 * @param gffGeneIsoInfo
	 * @param lsExon
	 */
	public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, ArrayList<ExonInfo> lsExon) {
		lsIsoExon.add(lsExon);
		mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
	}
	public HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> getMapIso2LsExon() {
		return mapIso2LsExon;
	}
	/**
	 * list--所有isoform
	 * list--每个isoform中该组的所有exon
	 * 如果该iso跳过了这个exon，则里面装空的list
	 */
	public ArrayList<ArrayList<ExonInfo>> getLsIsoExon() {
		return lsIsoExon;
	}
	/**
	 * 本组中是否为相同的exon，如果相同了那么也就没有可变剪接的说法了
	 * @return
	 */
	public boolean isSameExon() {
		if (sameExon != null) {
			return sameExon;
		}
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (lsIsoExon.size() >= 1 && mapIso2ExonNumSkipTheCluster.size() >= 1) {
			sameExon = false;
			return false;
		}
		sameExon = true;
		if (lsIsoExon.get(0).size() != 1) {
			sameExon = false;
			return false;
		}
		ExonInfo exonOld = lsIsoExon.get(0).get(0);
		for (int i = 1; i < lsIsoExon.size(); i++) {
			if (lsIsoExon.get(i).size() != 1) {
				sameExon = false;
				break;
			}
			//比较第一个就行了，因为如果有两个直接就返回false了
			ExonInfo exon = lsIsoExon.get(i).get(0);
			if (!exon.equals(exonOld)) {
				sameExon = false;
				break;
			}
		}
		return sameExon;
	}
	/** 返回该exonCluster中的所有exon */
	public ArrayList<ExonInfo> getAllExons() {
		if (lsCombExon != null) {
			return lsCombExon;
		}
		combExon();
		return lsCombExon;
	}
	
	private void combExon() {
		lsCombExon = new ArrayList<ExonInfo>();
		//用来去重复的hash表
		HashSet<ExonInfo> hashExon = new HashSet<ExonInfo>();
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			for (ExonInfo is : lsExon) {
				hashExon.add( is);
			}
		}
		for (ExonInfo exonInfo : hashExon) {
			lsCombExon.add(exonInfo);
		}
	}
	/**
	 * 记录IsoName和所对应的第一个exonNum的编号<br>
	 * 如果本组中该IsoName的转录本正好没有exon落在组中，也就是跳过去了，那么记录该Iso在本组的前一个exon的Num
	 * @param Isoname
	 * @param exonNumStart
	 */
	public void setIso2ExonNumSkipTheCluster(String Isoname, int exonNumStart) {
		mapIso2ExonNumSkipTheCluster.put(Isoname, exonNumStart);
	}
	/**
	 * 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号<br>
	 */
	public HashMap<String, Integer> getMapIso2ExonIndexSkipTheCluster() {
		return mapIso2ExonNumSkipTheCluster;
	}
	
	public ExonSplicingType getExonSplicingType() {
		HashSet<ExonSplicingType> setSplicingTypes = getExonSplicingTypeSet();
		if (setSplicingTypes.size() == 0) {
			return ExonSplicingType.unknown;
		}
		return setSplicingTypes.iterator().next();
	}
	
	
	public HashSet<ExonSplicingType> getExonSplicingTypeSet() {
		HashSet<ExonSplicingType> setSplicingTypes = new HashSet<ExonSplicingType>();
		if (isSameExon()) {
			return setSplicingTypes;
		}
		
		ArrayList<ExonInfo> lsSingleExonInfo = getExonInfoSingleLs();
		if (isRetainExon()) {
			setSplicingTypes.add(ExonSplicingType.retain_intron);
		}
		
		//前后的exon一样
		if (exonClusterBefore != null && exonClusterBefore.isSameExon()
				&& exonClusterAfter != null && exonClusterAfter.isSameExon()
				) 
		{
			if (mapIso2ExonNumSkipTheCluster.size() > 0) {
				setSplicingTypes.add(ExonSplicingType.cassette);
			}
		}
		//前面的exon不一样
		if (exonClusterBefore != null && !exonClusterBefore.isSameExon()) {
			if (isAltStart(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altstart);
			}
			if (isAltEnd(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altend);
			}
			else {
				setSplicingTypes.addAll(searchBeforeExon(lsSingleExonInfo));
			}
		}
		//后面的exon不一样
		if (exonClusterAfter != null && !exonClusterAfter.isSameExon()) {
			if (isAltStart(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altstart);
			}
			if (isAltEnd(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altend);
			}
			else {
				setSplicingTypes.addAll(searchAfterExon(lsSingleExonInfo));
			}
		}
		setSplicingTypes.addAll(getSingleSiteSpliteType(lsSingleExonInfo));
		
		if (setSplicingTypes.contains(ExonSplicingType.altstart) || setSplicingTypes.contains(ExonSplicingType.altend)) {
			if (setSplicingTypes.size() > 1) {
				ExonSplicingType exonSplicingType = setSplicingTypes.iterator().next();
				setSplicingTypes = new HashSet<ExonCluster.ExonSplicingType>();
				setSplicingTypes.add(exonSplicingType);
			}
		}
		if (setSplicingTypes.size() == 0) {
			setSplicingTypes.add(ExonSplicingType.unknown);
		}
		return setSplicingTypes;
	}
	
	private boolean isRetainExon() {
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				return true;
			}
		}
		return false;
	}
	
	/** 获得本exoncluster中存在的单个exon
	 * 如果是连续两个exon，就合并为一个
	 *  **/
	public ArrayList<ExonInfo> getExonInfoSingleLs() {
		//仅保存该位置每个iso只有一个exon的那种信息，就是说不保存 retain intron这种含有两个exon的信息
		//用来判断cassette和alt5，alt3这几类
		ArrayList<ExonInfo> lsExonTmp = new ArrayList<ExonInfo>();
		HashSet<ExonInfo> setRemoveSameExon = new HashSet<ExonInfo>();
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() == 0)
				continue;
			
			ExonInfo exonInfo = lsExon.get(0).clone();
			if (lsExon.size() > 1) {
				exonInfo.setEndCis(lsExon.get(lsExon.size() - 1).getEndCis());
			}
			
			if (setRemoveSameExon.contains(exonInfo))
				continue;
			else
				setRemoveSameExon.add(exonInfo);

			lsExonTmp.add(exonInfo);
		}
		return lsExonTmp;
	}
	
	/** 仅判断本位点的可变剪接情况
	 * 也就是仅判断alt5，alt3
	 */
	private LinkedList<ExonSplicingType> getSingleSiteSpliteType(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();
		if (lsExonInfo.size() <= 1) {
			return setSplicingTypes;
		}
		int start = lsExonInfo.get(0).getStartCis(), end = lsExonInfo.get(0).getEndCis();
		for (int i = 1; i < lsExonInfo.size(); i++) {
			ExonInfo exonInfo = lsExonInfo.get(i);
			if (exonInfo.getItemNum() != 0 && start != exonInfo.getStartCis()) {
				setSplicingTypes.add(ExonSplicingType.alt3);
			}
			if (exonInfo.getItemNum() != exonInfo.getParent().size() - 1 && end != exonInfo.getEndCis()) {
				setSplicingTypes.add(ExonSplicingType.alt5);
			}
		}
		return setSplicingTypes;
	}
	private boolean isAltStart(List<ExonInfo> lsExonInfo) {
		if (lsExonInfo.size() == 1) {
			ExonInfo exonInfo = lsExonInfo.get(0);
			if (exonInfo.getItemNum() == 0) {
				return true;
			}
		}
		return false;
	}
	/** 查看前一个exoncluster的信息 */
	private LinkedList<ExonSplicingType> searchBeforeExon(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();

		for (ExonInfo exonInfo : lsExonInfo) {
			if (!exonClusterBefore.getMapIso2LsExon().containsKey(exonInfo.getParent())) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon); 
			}
			else {
				setSplicingTypes.add(ExonSplicingType.cassette);
			}
		}
		
		return setSplicingTypes;
	}
	
	private boolean isAltEnd(List<ExonInfo> lsExonInfo) {
		if (lsExonInfo.size() == 1) {
			ExonInfo exonInfo = lsExonInfo.get(0);
			if (exonInfo.getItemNum() == exonInfo.getParent().size() - 1) {
				return true;
			}
		}
		return false;
	}
	/** 查看前一个exoncluster的信息 */
	private LinkedList<ExonSplicingType> searchAfterExon(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();

		for (ExonInfo exonInfo : lsExonInfo) {
			if (!exonClusterAfter.getMapIso2LsExon().containsKey(exonInfo.getParent())) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon);
			}
		}
		return setSplicingTypes;
	}
	
	/** 获取有变化的区域，用于提取该区域的表达值，计算是否为差异的可变剪接
	 * 譬如cassttet就直接提取整个exon
	 * 而alt5等就提取差异的哪个片段 
	 */
	public SiteInfo getDifSite() {
		HashSet<ExonSplicingType> setExonSplicingTypes = getExonSplicingTypeSet();
		SiteInfo siteInfo = null;
		if (setExonSplicingTypes.size() == 1) {
			if (setExonSplicingTypes.contains(ExonSplicingType.alt5)) {
				siteInfo = getAlt5Site();
			}
			else if (setExonSplicingTypes.contains(ExonSplicingType.alt3)) {
				siteInfo = getAlt3Site();
			}
		}
		if (setExonSplicingTypes.iterator().next() == ExonSplicingType.retain_intron) {
			siteInfo = getRetainIntronSite();
		}
		if (siteInfo == null) {
			siteInfo = new SiteInfo(chrID, startLoc, endLoc);
		}
		return siteInfo;
	}
	/** 获得alt5， alt3的差异位点 */
	private SiteInfo getAlt3Site() {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		//按照长度排序
		Collections.sort(lsExonInfo, new Comparator<ExonInfo>() {
			public int compare(ExonInfo o1, ExonInfo o2) {
				Integer start1 = o1.getStartCis();
				Integer start2 = o2.getStartCis();
				return start1.compareTo(start2);
			}
		});
		int start = lsExonInfo.get(0).getStartCis();
		int end = lsExonInfo.get(lsExonInfo.size() - 1).getStartCis();
		SiteInfo siteInfo = new SiteInfo(chrID, start, end);
		return siteInfo;
	}
	/** 获得alt5， alt3的差异位点 */
	private SiteInfo getAlt5Site() {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		//按照长度排序
		Collections.sort(lsExonInfo, new Comparator<ExonInfo>() {
			public int compare(ExonInfo o1, ExonInfo o2) {
				Integer start1 = o1.getEndCis();
				Integer start2 = o2.getEndCis();
				return start1.compareTo(start2);
			}
		});
		int start = lsExonInfo.get(0).getEndCis();
		int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
		SiteInfo siteInfo = new SiteInfo(chrID, start, end);
		return siteInfo;
	}
	private SiteInfo getRetainIntronSite() {
		for (ArrayList<ExonInfo> lsExonInfo : lsIsoExon) {
			if (lsExonInfo.size() > 1) {
				int start = lsExonInfo.get(0).getEndCis();
				int end = lsExonInfo.get(1).getStartCis();
				return new SiteInfo(chrID, start, end);
			}
		}
		return null;
	}
	public static enum ExonSplicingType {
		cassette, alt5, alt3, altend, altstart, mutually_exon, retain_intron, unknown
	}
}

