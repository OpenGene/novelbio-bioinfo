package com.novelbio.analysis.seq.rnaseq;

import java.lang.annotation.Retention;
import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.base.dataStructure.ArrayOperate;

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
	public GffGeneIsoInfo compareIso(String IsoName, GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneIsoInfoRef.clone();
		gffGeneIsoInfoResult.clear();
		
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); 
		lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), lsGffGeneIsoInfos);

		int[] boundBoth = getBothStartNumEndNum(lsExonClusters);
		
		int[] select = null;//是否添加的是Ref的lsexoninfo
		int[] boundInfo = new int[]{0, 0};;//比较上组exon的边界0，表示一致 1表示不一致
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRef);
			ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
			//相同的就直接装进去
			if (exonCluster.isSameExon()) {
				gffGeneIsoInfoResult.add(lsExonInfosRef.get(0));
				boundInfo = new int[]{0, 0};
				continue;
			}
			
			ArrayList<ExonInfo> lsExonInfos = null;
			if (boundBoth[0] > i) {
				select = (lsExonInfosRef.size() == 1? new int[]{0, 0} : new int[]{1, 1});
			}
			else if (boundBoth[0] == i) {
				select = compareExonStart(gffGeneIsoInfoRef, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
			} 
			else if (boundBoth[1] >= i) {
				if (boundBoth[1] == i)//TODO 添加right边界的信息
					select = compareExonEnd(boundInfo,select,gffGeneIsoInfoRef, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
				else
					select = compExonMidSelect(boundInfo, select, lsExonInfosRef, lsExonInfosThis);
			}
			else if (boundBoth[1] < i) {
				select = (lsExonInfosRef.size() == 1? new int[]{0, 0} : new int[]{1, 1});
			}
			//当boundInfo[1] != 0时，紧跟上一次添加的就好
			lsExonInfos = getLsExonInfo(select, lsExonInfosRef, lsExonInfosThis);
			for (ExonInfo exonInfo : lsExonInfos) {
				gffGeneIsoInfoResult.add(exonInfo);
			}
			boundInfo = compareLsExonInfoBound(exonCluster);
		}
		return gffGeneIsoInfoResult;
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
	 * @param lastBound
	 * @param lastSelect
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return int[2]
	 * 0：0 ref起点边界 1 this起点边界
	 * 1：0 ref终点边界 1 this终点边界
	 * @return
	 */
	private int[] compExonMidSelect(int[] lastBound, int[] lastSelect, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		int[] result = new int[]{1, 1};
		if (lastBound[1] != 0 && lastSelect[1] == 0) {
			result[0] = 0;
			if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
				result[1] = 0;
			}
		}
		return result;
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
	private int[] compareExonStart(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		int[] result = new int[]{1, 1};
		//如果都是起点
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) == 0 && gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(0)) == 0) {
			getBoundListExonInfo(result, gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) != 0) {
				result[0] = 0;
			}
			else {
				result[0] = 1;
			}
		}
		return result;
	}
	/**
	 * 
	 * 返回末端
	 * @param lastBound
	 * @param lastSelectInfo
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef 必须不为0
	 * @param gffGeneIsoInfoThis 必须不为0
	 * @param lsExonInfosThis
	 * @return
	 * 0：0 ref起点边界 1 this起点边界<br>
	 * 1：0 ref终点边界 1 this终点边界<br>
	 * @return
	 */
	private int[] compareExonEnd(int[] lastBound, int[] lastSelectInfo, GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		int[] result = new int[]{1, 1};
		if (lastBound[1] == 1 && lastSelectInfo[1] == 0) {
			result[0] = 0;
		}
		
		//如果都是终点
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) == gffGeneIsoInfoRef.size() - 1 
				&& gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(lsExonInfosThis.size() - 1)) == gffGeneIsoInfoThis.size() - 1 ) {
			getBoundListExonInfo(result, gffGeneIsoInfoRef.isCis5to3(), false, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) != gffGeneIsoInfoRef.size() - 1 ) {
				result[1] = 0;
			}
			else {
				result[1] = 1;
			}
		}
		return result;
	}
	/**
	 * 当ref和this的exon都在边界时，返回长的边界，然后内部返回this的位点
	 * @param selectInfo 输入选择的位点，进行修正
	 * 0：0 ref起点边界 1 this起点边界<br>
	 * 1：0 ref终点边界 1 this终点边界<br>
	 * @param cis
	 * @param start
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return 	
	 */
	private void getBoundListExonInfo(int[] selectInfo, boolean cis, boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {		
		int refBound = 0, thisBound = 0;
		if (start) {
			refBound = lsExonInfosRef.get(0).getStartCis();
			thisBound = lsExonInfosThis.get(0).getStartCis();
		}
		else {
			refBound = lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis();
			thisBound = lsExonInfosThis.get(lsExonInfosThis.size() - 1).getEndCis();
		}
		
		if (cis && start)
			selectInfo[0] = (refBound < thisBound? 0:1);
		else if (cis && !start)
			selectInfo[1] = (refBound > thisBound? 0:1);
		else if (!cis && start)
			selectInfo[0] = (refBound > thisBound? 0:1);
		else if (!cis && !start)
			selectInfo[1] = (refBound < thisBound? 0:1);
	}
	
	/**
	 * 根据select的信息添加外显子
	 * @param select
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return
	 */
	private ArrayList<ExonInfo> getLsExonInfo(int[] select, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		ArrayList<ExonInfo> lsResult = new ArrayList<ExonInfo>();
		if (select[0] == 0) {
			if (select[1] == 0) {
				return lsExonInfosRef;
			}
			
			if (lsExonInfosRef.size() == 0) {
				select[1] = 0;
				return new ArrayList<ExonInfo>();
			}
			else {
				if (lsExonInfosThis.size() == 0) {
					select[1] = 0;
					return lsExonInfosRef;
				}
				else if (lsExonInfosThis.size() >= 1) {
					ExonInfo exonInfo = new ExonInfo();
					exonInfo.setCis5to3(lsExonInfosThis.get(0).isCis5to3());
					exonInfo.setStartCis(lsExonInfosRef.get(0).getStartCis());
					exonInfo.setEndCis(lsExonInfosThis.get(0).getEndCis());
					lsResult.add(exonInfo);
					for (int i = 1; i < lsExonInfosThis.size(); i++) {
						lsResult.add(lsExonInfosThis.get(i));
					}
				}
			}
		}
		else {
			if (select[1] == 1) {
				return lsExonInfosThis;
			}
			else {
				if (lsExonInfosThis.size() == 0 || lsExonInfosRef.size() == 0) {
					select[1] = 1;
					return lsExonInfosThis;
				}
				for (int i = 0; i < lsExonInfosThis.size() - 1; i++) {
					lsResult.add(lsExonInfosThis.get(i));
				}
				ExonInfo exonInfo = new ExonInfo();
				exonInfo.setCis5to3(lsExonInfosThis.get(0).isCis5to3());
				exonInfo.setStartCis(lsExonInfosThis.get(lsExonInfosThis.size() - 1).getStartCis());
				exonInfo.setEndCis(lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis());
				lsResult.add(exonInfo);
			}
		}
		return lsResult;
	}
	
	/**
	 * 比较本组exon的边界
	 * @return int[2] 0一致 1不一致
	 * 0: 0左端一致 1左端不一致
	 * 1: 0右端一致 1右端不一致
	 */
	private int[] compareLsExonInfoBound(ExonCluster exonCluster) {
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
}
