package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.stream.IIOByteBuffer;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.directive.Foreach;

import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictME;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.database.domain.geneanno.SepSign;

public class ExonCluster {
	private static Logger logger = Logger.getLogger(ExonCluster.class);
	
	/** 全体父亲ISO */
	Collection<GffGeneIsoInfo> colGeneIsoInfosParent;
	ExonCluster exonClusterBefore;
	ExonCluster exonClusterAfter;
	
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	ArrayList<ExonInfo> lsCombExon;
	/**
	 * list--所有isoform
	 * list--每个isoform中该组的所有exon
	 * 如果该iso跳过了这个exon，则里面装空的list
	 * 如果该iso根本不在这个范围内,则里面就没有这个list
	 */
	ArrayList<ArrayList<ExonInfo>> lsIsoExon = new ArrayList<ArrayList<ExonInfo>>();
	/** 该iso跳过了这个exon，则里面装空的list
	 *  如果该iso根本不在这个范围内,则里面就没有这个list
	 */
	HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> mapIso2LsExon = new HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>>();
	/**
	 * 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号<br>
	 */
	HashMap<GffGeneIsoInfo, Integer> mapIso2ExonNumSkipTheCluster = new HashMap<GffGeneIsoInfo, Integer>();
	
	PredictME predictME;
	
	public ExonCluster(String chrID, int start, int end, Collection<GffGeneIsoInfo> colGeneIsoInfosParent) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
		this.colGeneIsoInfosParent = colGeneIsoInfosParent;
		predictME = new PredictME(this);
	}
	public String getChrID() {
		return chrID;
	}
	public int getStartLocAbs() {
		return startLoc;
	}
	public int getEndLocAbs() {
		return endLoc;
	}
	public int getStartCis() {
		if (isCis5To3()) {
			return startLoc;
		} else {
			return endLoc;
		}
	}
	public int getEndCis() {
		if (isCis5To3()) {
			return endLoc;
		} else {
			return startLoc;
		}
	}
	public boolean isCis5To3() {
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
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
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).getParent().getParentGffDetailGene();
			}
		}
		return null;
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
	
	/**
	 * 如果该iso跳过了这个exon，则里面装空的list
	 * 如果该iso根本不在这个范围内,则里面就没有这个list
	 * @param gffGeneIsoInfo
	 * @param lsExon
	 */
	public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, ArrayList<ExonInfo> lsExon) {
		lsIsoExon.add(lsExon);
		mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
	}
	
	/** 该iso跳过了这个exon，则里面装空的list
	 * 如果该iso根本不在这个范围内,则里面就没有这个list
	 */
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
			for (ArrayList<ExonInfo> lsexons : lsIsoExon) {
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
	 *4----5-----------<br>
	 *  -4'-5'----------<br>
	 *  这两个exon就不是我们想要的东西，返回true<br>
	 * @return
	 */
	public boolean isNotSameTss_But_SameEnd() {
		ArrayList<ExonInfo> lsExonEdge = new ArrayList<ExonInfo>();
		if (exonClusterBefore != null && exonClusterAfter != null) {
			return false;
		}
		for (ArrayList<ExonInfo> lsexons : lsIsoExon) {
			if (lsexons.size() > 2) {
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
	 * 在已经存在的iso中是否是一致的
	 * 那么如果iso不在这个exoncluster范围内，就不进行统计
	 * @return
	 */
	public boolean isSameExonInExistIso() {
		return isSameExon(false);
	}
	
	/**
	 * 本组中是否为相同的exon，如果相同了那么也就没有可变剪接的说法了
	 * @return
	 */
	public boolean isSameExon() {
		return isSameExon(true);
	}
	
	private boolean isSameExon(boolean considerIsoNotInRegion) {
		//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
		if (lsIsoExon.size() >= 1 && mapIso2ExonNumSkipTheCluster.size() >= 1	) {
			return false;
		}
		if (considerIsoNotInRegion && lsIsoExon.size() < colGeneIsoInfosParent.size()) {
			return false;
		}
		boolean sameExon = true;
		if (lsIsoExon.get(0).size() != 1) {
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
	public void setIso2ExonNumSkipTheCluster(GffGeneIsoInfo gffGeneIsoInfo, int exonNumStart) {
		mapIso2ExonNumSkipTheCluster.put(gffGeneIsoInfo, exonNumStart);
	}
	/**
	 * 记录跳过该exoncluster的Iso，和跨过该exoncluster的那对exon的，前一个exon的编号<br>
	 */
	public HashMap<GffGeneIsoInfo, Integer> getMapIso2ExonIndexSkipTheCluster() {
		return mapIso2ExonNumSkipTheCluster;
	}
	
	/**
	 * 返回本位点第一个获得的剪接方式
	 * @return
	 */
	public SplicingAlternativeType getExonSplicingType() {
		HashSet<SplicingAlternativeType> setSplicingTypes = getExonSplicingTypeSet();
		if (setSplicingTypes.size() == 0) {
			return SplicingAlternativeType.unknown;
		}
		return setSplicingTypes.iterator().next();
	}
	public PredictME getPredictME() {
		return predictME;
	}
	/**
	 * 获得本exoncluster的剪接类型
	 * TODO 还有一些识别不出来
	 * @return
	 */
	public HashSet<SplicingAlternativeType> getExonSplicingTypeSet() {
		HashSet<SplicingAlternativeType> setSplicingTypes = new HashSet<SplicingAlternativeType>();
		if (isSameExon()) {
			setSplicingTypes.add(SplicingAlternativeType.sam_exon);
			return setSplicingTypes;
		}
		
		if (isRetainIntron()) {
			setSplicingTypes.add(SplicingAlternativeType.retain_intron);
		}
		
		SplicingAlternativeType splicingType = getIfCassette();
		if (splicingType != null) {
			setSplicingTypes.add(splicingType);
		}
		
		ArrayList<ExonInfo> lsSingleExonInfo = getExonInfoSingleLs();
		//前面或后面的exon不一样
		boolean beforeIsNotSame = (exonClusterBefore != null && !exonClusterBefore.isSameExon());
		boolean afterIsNotSame = (exonClusterAfter != null && !exonClusterAfter.isSameExon());
		if (beforeIsNotSame) {
			if (isAltStart(lsSingleExonInfo)) {
				setSplicingTypes.add(SplicingAlternativeType.altstart);
			}
		}
		
		if (afterIsNotSame) {
			if (isAltEnd(lsSingleExonInfo)) {
				setSplicingTypes.add(SplicingAlternativeType.altend);
			}
		}
		
		if (predictME.isMutuallyExclusive()) {
			setSplicingTypes.add(SplicingAlternativeType.mutually_exclusive);
		}
		
		setSplicingTypes.addAll(getSpliteTypeAlt5Alt3(lsSingleExonInfo));
		
		if (setSplicingTypes.size() == 0 && isIsosHaveSameBeforeAfterExon(mapIso2LsExon.keySet(), mapIso2ExonNumSkipTheCluster.keySet())) {
			setSplicingTypes.add(SplicingAlternativeType.cassette);
		}
		
		if (setSplicingTypes .size() == 0) {
			setSplicingTypes.add(SplicingAlternativeType.unknown);
		}
		return setSplicingTypes;
	}
	
	private boolean isRetainIntron() {
		//retainIntron有两个条件：1：存在一个长的exon，2：存在两个短的exon
		boolean twoExon = false;
		boolean oneExon = false;
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				twoExon = true;
			}
			else if (lsExon.size() == 1) {
				oneExon = true;
			}
		}
		return twoExon && oneExon;
	}

	/**
	 * 当为Cassette时，设定为单个Cassette还是Cassette_Multi
	 * 如果不符合Cassette的条件，则返回null
	 */
	private SplicingAlternativeType getIfCassette() {
		SplicingAlternativeType splicingType = null;
		//获得本位点存在有exon的iso
		ArrayList<GffGeneIsoInfo> lsIso_ExonExist = new ArrayList<GffGeneIsoInfo>();
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				lsIso_ExonExist.add(lsExonInfos.get(0).getParent());
			}
		}
		//看跳过和存在的Iso在前后位置里面是否都存在
		//也就是说本位点跳过的iso在前后必须都有exon
		//本位点有exon的iso在前后也必须都有exon
		//这样才是casstte的类型
		if (!isOneIsoHaveExonBeforeAndAfter(lsIso_ExonExist) || !isOneIsoHaveExonBeforeAndAfter(mapIso2ExonNumSkipTheCluster.keySet())) {
			return null;
		}
		
		boolean casstteMulti = false;
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				casstteMulti = true;
				break;
			}
		}
		if (casstteMulti) {
			splicingType = SplicingAlternativeType.cassette_multi;
		} else {
			splicingType = SplicingAlternativeType.cassette;
		}
		return splicingType;
	}
	/**
	 * 是否存在某个转录本，该转录本在本exon前后都含有exon
	 * 只要有一个存在就判定为true
	 * 主要是用来判定casstte的
	 * @return
	 */
	private boolean isOneIsoHaveExonBeforeAndAfter(Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		boolean isbeforeAndAfterContainHaveIso = false;
		if (exonClusterBefore == null || exonClusterAfter == null) {
			return false;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			List<ExonInfo> lsExonInfosBefore = exonClusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo);
			List<ExonInfo> lsExonInfosAfter = exonClusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
			if (lsExonInfosBefore != null && lsExonInfosBefore.size() > 0
					&&
					lsExonInfosAfter != null && lsExonInfosAfter.size() > 0
					) {
				isbeforeAndAfterContainHaveIso = true;
				break;
			}
		}
		return isbeforeAndAfterContainHaveIso;
	}
	
	/**
	 * 看包含该exon和不包含该exon的iso是否有相同的前exon和后exon
	 * @param lsIso_ExonExist 包含该exon的iso
	 * @param lsIso_ExonSkip 不包含该exon的iso
	 */
	private boolean isIsosHaveSameBeforeAfterExon(Collection<GffGeneIsoInfo> lsIso_ExonExist, Collection<GffGeneIsoInfo> lsIso_ExonSkip) {
		int initialNum = -1000;
		Set<String> setBeforAfterExist = getIsoHaveBeforeAndAfterExon(initialNum, lsIso_ExonExist);
		Set<String> setBeforAfterSkip = getIsoHaveBeforeAndAfterExon(initialNum, lsIso_ExonSkip);
		for (String string : setBeforAfterSkip) {
			if (string.contains(initialNum + "")) {
				continue;
			}
			if (setBeforAfterExist.contains(string)) {
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * 获得某个iso的前后的 exon的相对位置
	 * 譬如某个iso在前面有一个exon，后面有一个exon
	 * 则统计为0sepsign0
	 * 如果前面的前面有一个exon，后面的后面的后面有一个exon
	 * 则统计为
	 * -1sepSign2
	 * @param initialNum 初始化数字，设定为一个比较大的负数就好，随便设定，譬如-1000
	 * @param lsIso_ExonExist
	 * @return
	 */
	private HashSet<String> getIsoHaveBeforeAndAfterExon(int initialNum, Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		HashSet<String> setBeforeAfter = new HashSet<String>();
		ExonCluster clusterBefore = exonClusterBefore;
		ExonCluster clusterAfter = exonClusterAfter;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			int[] beforeAfter = new int[]{initialNum, initialNum};//初始化为负数
			int numBefore = 0, numAfter = 0;//直接上一位的exon标记为0，再向上一位标记为-1
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[0] = numBefore;
					break;
				}
				clusterBefore = clusterBefore.exonClusterBefore;
				numBefore--;
			}
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[1] = numAfter;
					break;
				}
				clusterAfter = clusterAfter.exonClusterAfter;
				numAfter++;
			}
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.add(tmpBeforeAfter);
		}
		return setBeforeAfter;
	}
	
	/**
	 * @param gffGeneIsoInfo 注意gffgeneIsoInfo重写过hashcode
	 * @return
	 */
	private boolean isIsoHaveExon(GffGeneIsoInfo gffGeneIsoInfo) {
		List<ExonInfo> lsExonInfos = getMapIso2LsExon().get(gffGeneIsoInfo);
		if (lsExonInfos != null && lsExonInfos.size() > 0) {
			return true;
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
	/**
	 * 输入由getExonInfoSingleLs合并后的lsExonInfo，其中每个ExonInfo来源于不同的Iso
	 * @param lsExonInfo
	 * @return
	 */
	private boolean isAltStart(List<ExonInfo> lsExonInfo) {
		for (ExonInfo exonInfo : lsExonInfo) {
			if (exonInfo.getItemNum() == 0) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 输入由getExonInfoSingleLs合并后的lsExonInfo，其中每个ExonInfo来源于不同的Iso
	 * @param lsExonInfo
	 * @return
	 */
	private boolean isAltEnd(List<ExonInfo> lsExonInfo) {
		for (ExonInfo exonInfo : lsExonInfo) {
			if (exonInfo.getItemNum() == exonInfo.getParent().size() - 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 仅判断本位点的可变剪接情况
	 * 也就是仅判断alt5，alt3
	 */
	private LinkedList<SplicingAlternativeType> getSpliteTypeAlt5Alt3(List<ExonInfo> lsExonInfo) {
		LinkedList<SplicingAlternativeType> setSplicingTypes = new LinkedList<SplicingAlternativeType>();
		if (lsExonInfo.size() <= 1) {
			return setSplicingTypes;
		}
		int start = 0, end = 0;
		if (exonClusterBefore != null) {
			for (ExonInfo exonInfo : lsExonInfo) {
				if (exonClusterBefore.isIsoCover(exonInfo.getParent())) {
					start = exonInfo.getStartCis();
					break;
				}
			}
			for (ExonInfo exonInfo : lsExonInfo) {
				if (exonInfo.getStartCis() != start && exonClusterBefore.isIsoCover(exonInfo.getParent())) {
					setSplicingTypes.add(SplicingAlternativeType.alt3);
					break;
				}
			}
		}

		if (exonClusterAfter != null) {
			for (ExonInfo exonInfo : lsExonInfo) {
				if (exonClusterAfter.isIsoCover(exonInfo.getParent())) {
					end = exonInfo.getEndCis();
					break;
				}
			}
			for (ExonInfo exonInfo : lsExonInfo) {
				if (exonInfo.getEndCis() != end && exonClusterAfter.isIsoCover(exonInfo.getParent())) {
					setSplicingTypes.add(SplicingAlternativeType.alt5);
					break;
				}
			}
		}
		
		return setSplicingTypes;
	}
	
	/** 获取有变化的区域，用于提取该区域的表达值，计算是否为差异的可变剪接
	 * 譬如cassttet就直接提取整个exon
	 * 而alt5等就提取差异的哪个片段
	 * @param exonSplicingType 为了方便提取那种复杂转录本才需要的，目前只有alt5，alt3，retain_intron这几种类型
	 * @param tophatJunction 主要用tophatJunction来判定到alt5和alt3的边界
	 */
	public SiteInfo getDifSite(SplicingAlternativeType exonSplicingType, TophatJunction tophatJunction) {
		HashSet<SplicingAlternativeType> setExonSplicingTypes = getExonSplicingTypeSet();
		SiteInfo siteInfo = null;
		if (exonSplicingType == SplicingAlternativeType.alt5 && setExonSplicingTypes.contains(SplicingAlternativeType.alt5)) {
			siteInfo = getAlt5Site(tophatJunction);
		}
		else if (exonSplicingType == SplicingAlternativeType.alt3 && setExonSplicingTypes.contains(SplicingAlternativeType.alt3)) {
			siteInfo = getAlt3Site(tophatJunction);
		}
		else if (exonSplicingType == SplicingAlternativeType.retain_intron && setExonSplicingTypes.contains(SplicingAlternativeType.retain_intron)) {
			siteInfo = getRetainIntronSite(tophatJunction);
		}
		else {
			siteInfo = new SiteInfo(chrID, startLoc, endLoc);
		}
		return siteInfo;
	}

	/** 获得alt5， alt3的差异位点 */
	private SiteInfo getAlt5Site(TophatJunction tophatJunction) {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		Set<Integer> setBound = new HashSet<Integer>();
		for (ExonInfo exonInfo : lsExonInfo) {
			setBound.add(exonInfo.getEndCis());
		}
		return sortByJunctionReads(tophatJunction, setBound);
	}
	
	/** 获得alt5， alt3的差异位点 */
	private SiteInfo getAlt3Site(TophatJunction tophatJunction) {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		Set<Integer> setBound = new HashSet<Integer>();
		for (ExonInfo exonInfo : lsExonInfo) {
			setBound.add(exonInfo.getStartCis());
		}
		return sortByJunctionReads(tophatJunction, setBound);
	}
	
	/**
	 * 根据junction reads的数量从大到小排序
	 * 并返回topjunction中reads支持最多的两个点
	 */
	private SiteInfo sortByJunctionReads(final TophatJunction tophatJunction, Collection<Integer> colBount) {
		ArrayList<Integer> lsBount = new ArrayList<Integer>(colBount);
		Collections.sort(lsBount, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				Integer start1 = tophatJunction.getJunctionSite(chrID, o1);
				Integer start2 = tophatJunction.getJunctionSite(chrID, o2);
				return -start1.compareTo(start2);
			}
		});
		int start = lsBount.get(0);
		int end = lsBount.get(1);
		SiteInfo siteInfo = new SiteInfo(chrID, Math.min(start, end), Math.max(start, end));
		return siteInfo;
	}
	
	private SiteInfo getRetainIntronSite(TophatJunction tophatJunction) {
		int maxReadsNum = -1;
		SiteInfo siteInfoMaxReads = null;
		for (ArrayList<ExonInfo> lsExonInfo : lsIsoExon) {
			if (lsExonInfo.size() > 1) {
				for (int i = 0; i < lsExonInfo.size() - 1; i++) {
					int startLoc =  lsExonInfo.get(i).getEndCis();
					int endLoc = lsExonInfo.get(i+1).getStartCis();
					int readsNum = tophatJunction.getJunctionSite(chrID, startLoc, endLoc);
					if (readsNum > maxReadsNum) {
						maxReadsNum = readsNum;
						siteInfoMaxReads = new SiteInfo(chrID, startLoc, endLoc);
					}
				}
			}
		}
		return siteInfoMaxReads;
	}
	
	/** 根据坐标设定一个key */
	protected String getHashKey() {
		return getChrID() + "_" +getStartLocAbs() + getEndLocAbs();
	}
	
	public static enum SplicingAlternativeType {
		cassette, cassette_multi, alt5, alt3, altend, altstart, mutually_exclusive, retain_intron, unknown, sam_exon;
		
		static HashMap<String, SplicingAlternativeType> mapName2Events = new LinkedHashMap<String, ExonCluster.SplicingAlternativeType>();
		public static HashMap<String, SplicingAlternativeType> getMapName2SplicingEvents() {
			if (mapName2Events.size() == 0) {
				mapName2Events.put("cassette", cassette);
				mapName2Events.put("cassette_multi", cassette_multi);
				mapName2Events.put("alt5", alt5);
				mapName2Events.put("alt3", alt3);
				mapName2Events.put("altend", altend);
				mapName2Events.put("altstart", altstart);
				mapName2Events.put("mutually_exon", mutually_exclusive);
				mapName2Events.put("retain_intron", retain_intron);
				mapName2Events.put("unknown", unknown);
				mapName2Events.put("sam_exon", sam_exon);
			}
			return mapName2Events;
		}
	}
}


