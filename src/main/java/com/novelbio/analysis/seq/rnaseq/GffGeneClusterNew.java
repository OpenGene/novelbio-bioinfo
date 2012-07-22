package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.omg.CORBA.FREE_MEM;
import org.w3c.dom.ls.LSException;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.SepSign;

public class GffGeneClusterNew {
	/** 是否含有reference的GffDetailGene */
	boolean isContainsRef = true;
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	
	/** 最后比完的结果放在这里 */
	ArrayList<GffDetailGene> lsCombGenesResult;
	/**
	 * 挑选出需要进行修正的GffDetailGeneList，首先选择RefGffGene
	 * 设定的时候会将lsGeneCluster里面的对应项目删除
	 */
	ArrayList<GffDetailGene> lsGenesRef;
	/**
	 * list--所有不同来源的Gffhash<br>
	 * list--每个GffHash的GffDetail<br>
	 * <br>
	 * 如果本组中有RefGffGene，则放在第一个位置。那么就用第一个位置的GffDetailGene进行比较
	 * 如果本组没有RefGffGene，则选择本组中最长的一个GffDetailGene进行比较<br>
	 * <br>
	 * 如果存在reference<b>其中第一个一定是Reference的GffGene</b>
	 */
	ArrayList<ArrayList<GffDetailGene>> lsGeneCluster = new ArrayList<ArrayList<GffDetailGene>>();
	ArrayList<String> lsIsoName = new ArrayList<String>();
	
	double likelyhood = 0.5;
	/**
	 * 是否含有refGffDetailGene，没有说明Ref跳过了这个loc
	 * @param isContainsRef
	 */
	public void setIsContainsRef(boolean isContainsRef) {
		this.isContainsRef = isContainsRef;
	}
	public void addLsGffDetailGene(String isoName, ArrayList<GffDetailGene> lsGffDetailGenes) {
		lsGeneCluster.add(lsGffDetailGenes);
		lsIsoName.add(isoName);
	}
	
	private void combineRffInfo() {
		setRefGffGene();
	}
	
	
	/** 设定lsGenesRef，同时将lsGeneCluster中对应项目删除
	 * 为合并做好准备
	 */
	private void setRefGffGene() {
		if (isContainsRef) {
			lsGenesRef = lsGeneCluster.get(0);
			lsGeneCluster.remove(0);
		}
		else {
			int longestGffGene = getLongestGffGene_In_LsGeneCluster();
			lsGenesRef = lsGeneCluster.get(longestGffGene);
			lsGeneCluster.remove(longestGffGene);
		}
	}
	private int getLongestGffGene_In_LsGeneCluster() {
		int lengthGffGene = 0;
		int index = 0;
		for (int i = 0; i < lsGeneCluster.size(); i++) {
			for (GffDetailGene gffDetailGene : lsGeneCluster.get(i)) {
				if (gffDetailGene.getLen() > lengthGffGene) {
					lengthGffGene = gffDetailGene.getLen();
					index = i;
				}
			}
		}
		return index;
	}
	
	private void compareGffGene() {
		for (GffDetailGene gffDetailGeneRef : lsGenesRef) {//遍历每个GffDetail
			for (ArrayList<GffDetailGene> lsgffArrayList : lsGeneCluster) {//获得GffCluster里面每个GffHash的list，这个一般只有一个GffHash
				for (GffDetailGene gffDetailGeneCalculate : lsgffArrayList) {//获得另一个GffHash里面的GffDetailGene
					for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//遍历该GffDetailGene的转录本，并挑选出最接近的进行比较
						GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, likelyhood).clone();
						//TODO 新建一个gffDetail然后放进去可能比较合适把
					}
				}
			}
		}
	}
	/**
	 * 比较两个方向相同，有交集的GffGeneIso的信息，修正gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoThis
	 */
	@SuppressWarnings("unused")
	private void compareIso(String IsoName, GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		GffGeneIsoInfo gffGeneIsoInfoRefDuplicate = gffGeneIsoInfoRef.clone();
		gffGeneIsoInfoRef.clear();//最后装在这个里面，所以现在清空
		
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRefDuplicate); 
		lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), gffGeneIsoInfoRef.getChrID(), lsGffGeneIsoInfos);
//		装载最后的结果iso
//		GffGeneIsoInfo gffGeneIsoInfo = new GffGeneIsoCis(IsoName, gffGeneIsoInfoRef.getGffDetailGeneParent(), gffGeneIsoInfoRef.getGeneType());
		int[] boundBoth = getBothStartNumEndNum(lsExonClusters);
		boolean addRefLs = true;//是否添加的是Ref的lsexoninfo
		int[] boundInfo = new int[]{0, 0};;//比较上组exon的边界0，表示一致 1表示不一致
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRefDuplicate);
			ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
			//相同的就直接装进去
			if (exonCluster.isSameExon()) {
				gffGeneIsoInfoRef.add(lsExonInfosRef.get(0));
				boundInfo = new int[]{0, 0};
				continue;
			}
			
			ArrayList<ExonInfo> lsExonInfos = null;
			if (boundBoth[0] == i) {
				addRefLs = compareExonLeft(gffGeneIsoInfoRefDuplicate, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
			}
			else if (boundInfo[1] == 0) {//前面的边是一致的
				if (boundBoth[1] == i) {//TODO 添加right边界的信息
					addRefLs = compareExonRight(gffGeneIsoInfoRefDuplicate, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
				}
				else {
					addRefLs = compExonMid(lsExonInfosRef, lsExonInfosThis);
				}
			}
			//当boundInfo[1] != 0时，紧跟上一次添加的就好
			lsExonInfos = (addRefLs? lsExonInfosRef : lsExonInfosThis);
			for (ExonInfo exonInfo : lsExonInfos) {
				gffGeneIsoInfoRef.add(exonInfo);
			}
			boundInfo = compareLsExonInfo(exonCluster);
		}
		
	}
	/**
	 * lsExonClusters是两个转录本比较的结果，如下图<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E 第一个转录本<br>
	 * *----*-----a-----b 第二个转录本<br>
	 * 返回其中较晚出现的起点和较早出现的终点坐标，也就是a和b的坐标，从0开始
	 * 那么就是返回2和3
	 * @param lsExonClusters
	 * @return
	 */
	private int[] getBothStartNumEndNum(ArrayList<ExonCluster> lsExonClusters) {
		int start = -1, end = -1;
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ArrayList<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
			ArrayList<ExonInfo> lsExonInfo1 = lsLsExonInfo.get(0);
			ArrayList<ExonInfo> lsExonInfo2 = lsLsExonInfo.get(1);
			if (lsExonInfo1.size() > 0 && lsExonInfo2.size() > 0) {
				if (start == -1) {
					start = i;
				}
				end = i;
			}
		}
		return new int[]{start, end};
	}
	/**
	 * 当两个样本进行比较时，直接返回lsExonInfosThis
	 * 也就是用新的转录本替代旧的
	 * 但是如果lsExonInfosThis为空，那就返回lsExonInfosRef
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return
	 */
	private boolean compExonMid(ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		if (lsExonInfosThis.size() == 0) {
			return true;
		}
		return false;
	}
	/**
	 * 比较本组exon的边界
	 * @return int[2] 0一致 1不一致
	 * 0: 左端
	 * 1: 右端
	 */
	private int[] compareLsExonInfo(ExonCluster exonCluster) {
		ArrayList<ArrayList<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
		ArrayList<ExonInfo> lsExonInfosRef = lsLsExonInfo.get(0);
		ArrayList<ExonInfo> lsExonInfosThis = lsLsExonInfo.get(1);
		if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
			return new int[]{1, 1};
		}
		else {
			int[] result = new int[2]; result[0] = 1; result[1] = 1;
			if (lsExonInfosRef.get(0).getStartCis() == lsExonInfosThis.get(0).getStartCis()) {
				result[0] = 0;
			}
			if (lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis() == lsExonInfosThis.get(lsExonInfosThis.size() - 1).getEndCis())  {
				result[1] = 0;
			}
			return result;
		}
	}

	/**
	 * 返回左端
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef 必须不为0
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis 必须不为0
	 * @return true RefExon
	 * false ThisExon
	 */
	private boolean compareExonLeft(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//如果都是起点
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) == 0 && gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(0)) == 0) {
			return getBoundListExonInfo(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) != 0) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	/**
	 * 返回右端
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef 必须不为0
	 * @param gffGeneIsoInfoThis 必须不为0
	 * @param lsExonInfosThis
	 * @return true RefExon
	 * false ThisExon
	 */
	private boolean compareExonRight(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//如果都是终点
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) == gffGeneIsoInfoRef.size() - 1 
				&& gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(lsExonInfosThis.size() - 1)) == gffGeneIsoInfoThis.size() - 1 ) {
			return getBoundListExonInfo(gffGeneIsoInfoRef.isCis5to3(), false, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) != gffGeneIsoInfoRef.size() - 1 ) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	/**
	 * 当ref和this的exon都在边界时，返回长的
	 * @param cis
	 * @param start
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return  true RefExon
	 * false ThisExon
	 */
	private boolean getBoundListExonInfo(boolean cis, boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		int refBound = 0, thisBound = 0;
		if (start) {
			refBound = lsExonInfosRef.get(0).getStartCis();
			thisBound = lsExonInfosThis.get(0).getStartCis();
		}
		else {
			refBound = lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis();
			thisBound = lsExonInfosThis.get(lsExonInfosThis.size() - 1).getEndCis();
		}
		
		if ( (cis && start) || (!cis && !start)) {
			if (refBound <= thisBound) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (refBound >= thisBound) {
				return true;
			}
			else {
				return false;
			}
		}
	}
}
