package com.novelbio.analysis.seq.genome.gffOperate.exoncluster;

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

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
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
	
	ArrayList<SpliceTypePredict> lsSpliceTypePredicts;
	
	public ExonCluster(String chrID, int start, int end, Collection<GffGeneIsoInfo> colGeneIsoInfosParent) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
		this.colGeneIsoInfosParent = colGeneIsoInfosParent;

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
	 * 返回某个iso所对应的exon
	 * @return
	 */
	public ArrayList<ExonInfo> getIsoExon(GffGeneIsoInfo gffGeneIsoInfo) {
		return mapIso2LsExon.get(gffGeneIsoInfo);
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
	 * 获得本exoncluster的剪接类型
	 * 如果返回空的list，说明不能做差异可变剪接分析
	 * @return
	 */
	public ArrayList<SpliceTypePredict> getSplicingTypeLs() {
		if (lsSpliceTypePredicts != null) {
			return lsSpliceTypePredicts;
		}
		ArrayList<SpliceTypePredict> lsSpliceTypePredictsTmp = new ArrayList<SpliceTypePredict>();
		SpliceTypePredict spliceTypeME = new PredictME(this);
		SpliceTypePredict spliceTypeAS = new PredictAltStart(this);
		SpliceTypePredict spliceTypeAE = new PredictAltEnd(this);
		SpliceTypePredict spliceTypeRI = new PredictAltEnd(this);
		SpliceTypePredict spliceTypeCS = new PredictAltEnd(this);
		SpliceTypePredict spliceTypeA5 = new PredictAltEnd(this);
		SpliceTypePredict spliceTypeA3 = new PredictAltEnd(this);
		lsSpliceTypePredictsTmp.add(spliceTypeME);
		lsSpliceTypePredictsTmp.add(spliceTypeAS);
		lsSpliceTypePredictsTmp.add(spliceTypeAE);
		lsSpliceTypePredictsTmp.add(spliceTypeRI);
		lsSpliceTypePredictsTmp.add(spliceTypeCS);
		lsSpliceTypePredictsTmp.add(spliceTypeA5);
		lsSpliceTypePredictsTmp.add(spliceTypeA3);
		
		
		if (isSameExonInExistIso() || isNotSameTss_But_SameEnd() || isAtEdge()) {
			lsSpliceTypePredicts = new ArrayList<SpliceTypePredict>();
			return lsSpliceTypePredicts;
		}
		
		for (SpliceTypePredict spliceTypePredict : lsSpliceTypePredictsTmp) {
			if (spliceTypePredict.isSpliceType()) {
				lsSpliceTypePredicts.add(spliceTypePredict);
			}
		}
		if (lsSpliceTypePredicts.size() == 0) {
			lsSpliceTypePredicts.add(new PredictUnKnown(this));
		}
		return lsSpliceTypePredicts;
	}
	
	/** 根据坐标设定一个key */
	protected String getHashKey() {
		return getChrID() + "_" +getStartLocAbs() + getEndLocAbs();
	}
	

}


