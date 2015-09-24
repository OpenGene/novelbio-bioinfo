package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listOperate.ListAbs;
import com.novelbio.listOperate.ListCodAbs;
import com.novelbio.listOperate.ListCodAbsDu;

/**
 * 由于GffGeneIsoInfo重写了hashcode
 * 所以本类无法两个hashcode相同的GffGeneIsoInfo，会将其合并为一个GffGeneIsoInfo
 * @author zong0jie
 *
 */
public class ExonCluster implements Alignment {
	private static final Logger logger = Logger.getLogger(ExonCluster.class);
	
	ExonClusterSite exonClusterSite;
	
	/** 全体父亲ISO */
	List<GffGeneIsoInfo> colGeneIsoInfosParent;
	ExonCluster exonClusterBefore;
	ExonCluster exonClusterAfter;
	
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	List<ExonInfo> lsCombExon;
	Boolean isCis5To3;
	/**
	 * 某个iso在该exonCluster中所对应的lsExon
	 * 该iso跳过了这个exon，则里面装空的list
	 *  如果该iso根本不在这个范围内,则里面就没有这个list
	 */
	Map<GffGeneIsoInfo, List<ExonInfo>> mapIso2LsExon = new LinkedHashMap<GffGeneIsoInfo, List<ExonInfo>>();
	/** 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号 */
	Map<GffGeneIsoInfo, Integer> mapIso2ExonNumSkipTheCluster = new HashMap<GffGeneIsoInfo, Integer>();
	
	List<SpliceTypePredict> lsSpliceTypePredicts;
	
	public ExonCluster(String chrID, int start, int end, List<GffGeneIsoInfo> colGeneIsoInfosParent, Boolean isCis5To3) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
		this.colGeneIsoInfosParent = colGeneIsoInfosParent;
		this.isCis5To3 = isCis5To3;
	}
	
	public void initail() {
		for (GffGeneIsoInfo gffGeneIsoInfo : colGeneIsoInfosParent) {
			if (gffGeneIsoInfo.isCis5to3() != isCis5To3
					|| gffGeneIsoInfo.getEndAbs() < startLoc
					|| gffGeneIsoInfo.getStartAbs() > endLoc
				) {
				continue;
			}
			ListCodAbsDu<ExonInfo, ListCodAbs<ExonInfo>> lsDu = gffGeneIsoInfo.searchLocationDu(startLoc, endLoc);
			List<ExonInfo> lsExon = lsDu.getCoveredElement();
			Collections.sort(lsExon);
			boolean junc = false;//如果本isoform正好没有落在bounder组中的exon，那么就需要记录跳过的exon的位置，就将这个flag设置为true
			int beforeExonNum = 0;//如果本isoform正好没有落在bounder组中的exon，那么就要记录该isoform的前后两个exon的位置，用于查找跨过和没有跨过的exon

			if (lsExon.size() == 0) junc = true;
			ListCodAbs<ExonInfo> codBefore = gffGeneIsoInfo.isCis5to3() ? lsDu.getGffCod1() : lsDu.getGffCod2();
			beforeExonNum = codBefore.getItemNumUp();

			addExonCluster(gffGeneIsoInfo, lsExon);
			if (junc && beforeExonNum < gffGeneIsoInfo.size()-1) {
				if (beforeExonNum == -1) {
					logger.debug("error");
				}
				setIso2ExonNumSkipTheCluster(gffGeneIsoInfo, beforeExonNum);
			}
			if (junc && beforeExonNum >= gffGeneIsoInfo.size()-1) {
				logger.error("出错拉，请检查该基因：" + gffGeneIsoInfo.getName() );
			}
		}
	}
	
	public String getRefID() {
		return chrID;
	}
	public int getStartAbs() {
		return startLoc;
	}
	public int getEndAbs() {
		return endLoc;
	}
	public int getStartCis() {
		if (isCis5to3()) {
			return startLoc;
		} else {
			return endLoc;
		}
	}
	public int getEndCis() {
		if (isCis5to3()) {
			return endLoc;
		} else {
			return startLoc;
		}
	}
	
	public Boolean isCis5to3() {
		if (isCis5To3 != null) {
			return isCis5To3;
		}
		for (List<ExonInfo> lsExonInfos : mapIso2LsExon.values()) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).isCis5to3();
			}
		}
		logger.error("本exoncluster为空");
		return true;
	}
	public void setExonClusterBefore(ExonCluster exonClusterBefore) {
		this.exonClusterBefore = exonClusterBefore;
	}
	public void setExonClusterAfter(ExonCluster exonClusterAfter) {
		this.exonClusterAfter = exonClusterAfter;
	}
	public ExonCluster getExonClusterAfter() {
		return exonClusterAfter;
	}
	public ExonCluster getExonClusterBefore() {
		return exonClusterBefore;
	}
	public String getLocInfo() {
		return chrID + ":" + startLoc + "-" + endLoc;
	}
	public int getLength() {
		return Math.abs(endLoc - startLoc);
	}
	/** 返回其所在的GffGene */
	public GffDetailGene getParentGene() {
		for (List<ExonInfo> lsExonInfos : mapIso2LsExon.values()) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).getParent().getParentGffDetailGene();
			}
		}
		return null;
	}
	
	/** 该iso跳过了这个exon，则里面装空的list
	 * 如果该iso根本不在这个范围内,则里面就没有这个list
	 */
	public Map<GffGeneIsoInfo, List<ExonInfo>> getMapIso2LsExon() {
		return mapIso2LsExon;
	}
	/**
	 * list--所有isoform
	 * list--每个isoform中该组的所有exon
	 * 如果该iso跳过了这个exon，则里面装空的list
	 */
	public List<List<ExonInfo>> getLsIsoExon() {
		return ArrayOperate.getArrayListValue(mapIso2LsExon);
	}
	/** 
	 * 该iso是否覆盖了该exoncluster
	 * 哪怕该iso没有这个exon，但是只要覆盖了，譬如跨过该exon，就返回true
	 */
	public boolean isIsoCover(GffGeneIsoInfo gffGeneIsoInfo) {
		if (mapIso2LsExon.containsKey(gffGeneIsoInfo)) {
			return true;
		}
		return false;
	}
	/** 返回某个iso所对应的exon */
	public List<ExonInfo> getIsoExon(GffGeneIsoInfo gffGeneIsoInfo) {
		return mapIso2LsExon.get(gffGeneIsoInfo);
	}
	
	/**
	 * 如果该iso跳过了这个exon，则里面装空的list
	 * 如果该iso根本不在这个范围内,则里面就没有这个list
	 * @param gffGeneIsoInfo
	 * @param lsExon
	 */
	public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, List<ExonInfo> lsExon) {
		mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
	}

	/**
	 * 有时候会出现这种情况，两个iso不是同一个tss，
	 * 那么一头就会露在外面，这种情况不是我们想要的，不做差异可变剪接分析
	 * 譬如:<br>
	 * 0-1-----2-3-----4-5-----------<br>
	 *  -----------------4'-5'----------<br>
	 *  那么0-1-----2-3这两个exon就不是我们想要的东西，返回true<br>
	 * @return
	 */
	public boolean isAtEdge() {
		if (exonClusterBefore == null || exonClusterAfter == null ) {
			int thisExistIso = 0;
			for (List<ExonInfo> lsexons : mapIso2LsExon.values()) {
				if (lsexons.size() > 0) {
					thisExistIso++;
				}
			}
			if (thisExistIso == 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 有时候会出现这种情况，两个iso不是同一个tss，但结尾相同
	 * 这种情况不是我们想要的，不做差异可变剪接分析
	 * 譬如:<br>
	 *1-------2-----------<br>
	 *    1'--2'----------<br>
	 *  这两个exon就不是我们想要的东西，返回true<br>
	 * @return
	 */
	public boolean isNotSameTss_But_SameEnd() {
		List<ExonInfo> lsExonEdge = new ArrayList<ExonInfo>();
		if (exonClusterBefore != null && exonClusterAfter != null) {
			return false;
		}
		for (List<ExonInfo> lsexons : mapIso2LsExon.values()) {
			if (lsexons.size() >= 2) {
				return false;
			}
			if (lsexons.size() == 1) {
				lsExonEdge.add(lsexons.get(0));
			}
		}
		if (lsExonEdge.size() <= 1) {
			return false;
		}
		
		if (exonClusterBefore == null ) {
			int end = lsExonEdge.get(0).getEndCis();
			for (int i = 1; i < lsExonEdge.size(); i++) {
				if (end != lsExonEdge.get(i).getEndCis()) {
					return false;
				}
			}
			return true;
		}
		else if (exonClusterAfter == null) {
			int start = lsExonEdge.get(0).getStartCis();
			for (int i = 1; i < lsExonEdge.size(); i++) {
				if (start != lsExonEdge.get(i).getStartCis()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * <b>只能比较两组</b><br>
	 * 几种情况<br>
	 * 1. 两个exon都在iso的最边界，两头不同<br>
	 * this            10---20-----<br>
	 * ref可以是     15--20------<br>
	 * ref可以是   8------20------<br><br>
	 * 2. 一个exon在iso边界，该exon的靠边端长度小于另一个的长度
	 * @return
	 */
	public boolean isEdgeSmaller(GffGeneIsoInfo gffRef) {
		for (List<ExonInfo> lsexons : mapIso2LsExon.values()) {
			if (lsexons.size() > 2 || lsexons.size() == 0) {
				return false;
			}
		}

		if (mapIso2LsExon.size() <= 1) {
			return false;
		}

		
		ExonInfo exonInfoRef = mapIso2LsExon.get(gffRef).get(0);
		ExonInfo exonInfoThis = null;
		for (List<ExonInfo> lsExonInfos : mapIso2LsExon.values()) {
			if (exonInfoRef != lsExonInfos.get(0)) {
				exonInfoThis = lsExonInfos.get(0);
				break;
			}
		}
		
		if (exonClusterBefore == null && exonClusterAfter != null) {
			if (exonInfoRef.getStartCis() != exonInfoThis.getStartCis() && exonInfoRef.getEndCis() == exonInfoThis.getEndCis()
					) {
				return true;
			}
		}
		if (exonClusterBefore != null && exonClusterAfter == null) {
			if (exonInfoRef.getStartCis() == exonInfoThis.getStartCis() && exonInfoRef.getEndCis() != exonInfoThis.getEndCis()
					) {
				return true;
			}
		}
		if (exonClusterBefore == null && exonClusterAfter == null) {
			if (exonInfoRef.getStartCis() != exonInfoThis.getStartCis() || exonInfoRef.getEndCis() != exonInfoThis.getEndCis()
					) {
				return true;
			}
		}
		
		
		if (exonClusterBefore != null && exonClusterBefore.mapIso2LsExon.get(gffRef) == null) {
			if ((isCis5to3() && exonInfoRef.getStartCis() > exonInfoThis.getStartCis()
					||
					!isCis5to3() && exonInfoRef.getStartCis() < exonInfoThis.getStartCis())
					&&
					exonInfoRef.getEndCis() == exonInfoThis.getEndCis()
					) {
				return true;
			}
		}
		
		if (exonClusterAfter != null && exonClusterAfter.mapIso2LsExon.get(gffRef) == null) {
			if ((isCis5to3() && exonInfoRef.getEndCis() < exonInfoThis.getEndCis()
					||
					!isCis5to3() && exonInfoRef.getEndCis() > exonInfoThis.getEndCis())
										&&
					exonInfoRef.getStartCis() == exonInfoThis.getStartCis()
					) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * 在已经存在的iso中是否是一致的
	 * 那么如果iso不在这个exoncluster范围内，就不进行统计
	 * @return
	 */
	public boolean isSameExonInExistIso() {
		return isSameExon(false);
	}
	
	/** 本exoncluster所在的exon的位置，如果同时存在retain_intron可能会不准
	 * 
	 * @param setIsoName_No_Reconstruct 没有重建转录本的老iso的名字，这样仅选取最长的没有重建过的转录本来计算exon的所在位置
	 * @return
	 */
	public String getExonNum(Set<String> setIsoName_No_Reconstruct) {
		if (setIsoName_No_Reconstruct == null) {
			setIsoName_No_Reconstruct = new HashSet<>();
		}
		
		//获得本exonCluster所对应的gffGene的全体iso
		GffDetailGene gffDetailGeneParent = colGeneIsoInfosParent.get(0).getParentGffGeneSame();
		List<GffGeneIsoInfo> lsGeneIsoInfos = new ArrayList<>();
		for (GffGeneIsoInfo iso : gffDetailGeneParent.getLsCodSplit()) {
			if (!setIsoName_No_Reconstruct.isEmpty() && !setIsoName_No_Reconstruct.contains(iso.getName())) {
				continue;
			}
			lsGeneIsoInfos.add(iso);
		}
		
		boolean isOnExon = false;
		for (GffGeneIsoInfo gffGeneIsoInfo : mapIso2LsExon.keySet()) {
			if (!setIsoName_No_Reconstruct.isEmpty() && !setIsoName_No_Reconstruct.contains(gffGeneIsoInfo.getName())) {
				continue;
			}
			
			List<ExonInfo> lsExon = mapIso2LsExon.get(gffGeneIsoInfo);
			if (!isOnExon && !lsExon.isEmpty()) {
				isOnExon = true;
				break;
			}
		}

		List<int[]> lsBound = ListAbs.getSep(isCis5To3, lsGeneIsoInfos);
		int exonNum = getExonNumInfo(isCis5To3, getStartAbs(), getEndAbs(), lsBound);
		return decodeExonLocate(exonNum, lsBound.size(), isOnExon);
	}
	
	/**
	 * 给定一系列的转录本，和指定的起点终点，返回该坐标对所在exon的位置
	 * @param isCis5To3 方向
	 * @param startAbs 坐标对的起点
	 * @param endAbs 坐标对的终点
	 * @param lsGeneIsoInfos 一系列的iso
	 * @param isOnExon 是否处于exon上
	 * @return
	 */
	protected static int getExonNumInfo(Boolean isCis5To3, int startAbs, int endAbs, List<int[]> lsBound ) {
		
		int num = 1;
		int resultExonNum = -100;
		if (isCis5To3 == null || isCis5To3) {
			//       ->------->------->-
			//-----10---20-------------30---40---------------50--60
			for (int[] is : lsBound) {
				if (startAbs > is[1]) {
				} else if (endAbs < is[0]) {
					resultExonNum = -(num-1);
					break;
				} else if (endAbs >= is[0]) {
					resultExonNum = num;
					break;
				}
				num++;
			}
		} else {
			//       <-------<-------<-
			//-----10---20-------------30---40---------------50--60
			for (int[] is : lsBound) {
				if (endAbs < is[0]) {
				} else if (startAbs > is[1]) {
					resultExonNum = -(num-1);
					break;
				} else if (startAbs <= is[1]) {
					resultExonNum = num;
					break;
				}
				num++;
			}
		}
		if (resultExonNum == -100) {
			resultExonNum = num;
		}
		return resultExonNum;
	}
	
	protected static String decodeExonLocate(int exonNum, int allExonNum, boolean isOnExon) {
		if (exonNum > 0 || isOnExon) {
			return Math.abs(exonNum) + "";
		} else {
			if (Math.abs(exonNum) < 1) {
				return "before first";
			} else if (exonNum > allExonNum) {
				return "after end";
			} else if (exonNum < 0) {
				return Math.abs(exonNum) +"-"+(Math.abs(exonNum) + 1);
			} else {
				return exonNum + "";
			}
		}
	}
	
	/** 本cluster中，最多的转录本含有多少exon  */
	protected int getMaxExonNumInCluster() {
		int maxNum = 0;
		for (List<ExonInfo> lsExonInfos : mapIso2LsExon.values()) {
			if (lsExonInfos.size() > maxNum) {
				maxNum = lsExonInfos.size();
			}
		}
		return maxNum;
	}
	
	/**
	 * 本组中是否为相同的exon，如果相同了那么也就没有可变剪接的说法了
	 * @return
	 */
	public boolean isSameExon() {
		return isSameExon(true);
	}
	
	/**
	 * 本组中的exon是否起点相同
	 * @return
	 */
	public boolean isSameStartCis() {
		List<List<ExonInfo>> lsIsoExon = ArrayOperate.getArrayListValue(mapIso2LsExon);
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (!isMaybeSameExon(lsIsoExon, true)) {
			return false;
		}
		boolean sameStartCis = true;
		ExonInfo exonOld = lsIsoExon.get(0).get(0);
		for (int i = 1; i < lsIsoExon.size(); i++) {
			//比较第一个就行了，因为如果有两个直接就返回false了
			ExonInfo exon = lsIsoExon.get(i).get(0);
			if (exon.getStartCis() != exonOld.getStartAbs()) {
				sameStartCis = false;
				break;
			}
		}
		return sameStartCis;
	}
	/**
	 * 本组中的exon是否终点相同
	 * @return
	 */
	public boolean isSameEndCis() {
		List<List<ExonInfo>> lsIsoExon = ArrayOperate.getArrayListValue(mapIso2LsExon);
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (!isMaybeSameExon(lsIsoExon, true)) {
			return false;
		}
		boolean sameEndCis = true;
		ExonInfo exonOld = lsIsoExon.get(0).get(0);
		for (int i = 1; i < lsIsoExon.size(); i++) {
			//比较第一个就行了，因为如果有两个直接就返回false了
			ExonInfo exon = lsIsoExon.get(i).get(0);
			if (exon.getEndAbs() != exonOld.getEndAbs()) {
				sameEndCis = false;
				break;
			}
		}
		return sameEndCis;
	}
	private boolean isSameExon(boolean considerIsoNotInRegion) {
		List<List<ExonInfo>> lsIsoExon = ArrayOperate.getArrayListValue(mapIso2LsExon);
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (!isMaybeSameExon(lsIsoExon, considerIsoNotInRegion)) {
			return false;
		}
		
		boolean sameExon = true;
		ExonInfo exonOld = lsIsoExon.get(0).get(0);
		for (int i = 1; i < lsIsoExon.size(); i++) {
			//比较第一个就行了，因为如果有两个直接就返回false了
			ExonInfo exon = lsIsoExon.get(i).get(0);
			if (!exon.equals(exonOld)) {
				sameExon = false;
				break;
			}
		}
		return sameExon;
	}
	/** 是相同exon的必要条件，譬如不包含junction exon等
	 * 
	 * @param considerIsoNotInRegion true：如果iso不在这个exoncluster范围内，就认为是不同
	 *  false：如果iso不在这个exoncluster范围内，就不进行统计
	 * @return true:表示有可能是相同的exon，但是还需要后续判定
	 */
	private boolean isMaybeSameExon(List<List<ExonInfo>> lsIsoExon, boolean considerIsoNotInRegion) {
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (lsIsoExon.size() >= 1 && mapIso2ExonNumSkipTheCluster.size() >= 1	) {
			return false;
		}
		if (considerIsoNotInRegion && lsIsoExon.size() < colGeneIsoInfosParent.size()) {
			return false;
		}
		boolean isMaybeSame = true;
		for (List<ExonInfo> list : lsIsoExon) {
			if (list.size() != 1) {
				isMaybeSame = false;
				break;
			}
		}
		return isMaybeSame;
	}
	
	/** 返回该exonCluster中的所有exon */
	public List<ExonInfo> getAllExons() {
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
		for (List<ExonInfo> lsExon : mapIso2LsExon.values()) {
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
	 * @param exonNumStart 从0开始记数
	 */
	public void setIso2ExonNumSkipTheCluster(GffGeneIsoInfo gffGeneIsoInfo, int exonNumStart) {
		mapIso2ExonNumSkipTheCluster.put(gffGeneIsoInfo, exonNumStart);
	}
	/**
	 * 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号<br>
	 * 编号从0开始记数
	 */
	public Map<GffGeneIsoInfo, Integer> getMapIso2ExonIndexSkipTheCluster() {
		return mapIso2ExonNumSkipTheCluster;
	}

	/**
	 * 输入的iso在该位点是否存在exon
	 * @param gffGeneIsoInfo 注意gffgeneIsoInfo重写过hashcode
	 * @return
	 */
	protected boolean isIsoHaveExon(GffGeneIsoInfo gffGeneIsoInfo) {
		List<ExonInfo> lsExonInfos = getMapIso2LsExon().get(gffGeneIsoInfo);
		if (lsExonInfos != null && lsExonInfos.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获得本exoncluster中存在的单个exon
	 * 如果是连续两个exon，就合并为一个
	 * 如果iso在该位点没有exon，就不加入list
	 */
	public List<ExonInfo> getExonInfoSingleLs() {
		//仅保存该位置每个iso只有一个exon的那种信息，就是说不保存 retain intron这种含有两个exon的信息
		//用来判断cassette和alt5，alt3这几类
		List<ExonInfo> lsExonTmp = new ArrayList<ExonInfo>();
		HashSet<ExonInfo> setRemoveSameExon = new HashSet<ExonInfo>();
		for (List<ExonInfo> lsExon : mapIso2LsExon.values()) {
			if (lsExon.size() == 0) {
				continue;
			}
			ExonInfo exonInfo = lsExon.get(0).clone();
			//将该exon扩展为一个大的exon
			if (lsExon.size() > 1) {
				exonInfo.setEndCis(lsExon.get(lsExon.size() - 1).getEndCis());
			}
			
			if (setRemoveSameExon.contains(exonInfo)) {
				continue;
			} else {
				setRemoveSameExon.add(exonInfo);
			}
			lsExonTmp.add(exonInfo);
		}
		return lsExonTmp;
	}
	/** 返回所有SpliceType的类型 */
	public Set<SplicingAlternativeType> getSplicingTypeSet() {
		Set<SplicingAlternativeType> setSpliceTypePredicts = new HashSet<SplicingAlternativeType>();
		List<SpliceTypePredict> lsSpliceTypePredicts = getSplicingTypeLs();
		for (SpliceTypePredict spliceTypePredict : lsSpliceTypePredicts) {
			setSpliceTypePredicts.add(spliceTypePredict.getType());
		}
		return setSpliceTypePredicts;
	}
	
	/**
	 * 获得本exoncluster的剪接类型
	 * 如果返回空的list，说明不能做差异可变剪接分析
	 * @return
	 */
	public List<SpliceTypePredict> getSplicingTypeLs() {
		if (lsSpliceTypePredicts == null) {
			if (getStartAbs() == 70329988) {
				logger.debug("");
			}
			lsSpliceTypePredicts = SpliceTypePredict.getSplicingTypeLs(this);
		}
		return lsSpliceTypePredicts;
	}
	
	/** 根据坐标设定一个key */
	public String getHashKey() {
		return getRefID() + "_" +getStartAbs() + "_" + getEndAbs();
	}

	/** 根据坐标设定一个key */
	public String toString() {
		return getRefID() + " " +getStartAbs() + " " + getEndAbs() + " " + getParentGene().getName();
	}
	
	
	public void setExonClusterSite(ExonClusterSite exonClusterSite) {
		this.exonClusterSite = exonClusterSite;
	}
	public ExonClusterSite getExonClusterSite() {
		return exonClusterSite;
	}
}
