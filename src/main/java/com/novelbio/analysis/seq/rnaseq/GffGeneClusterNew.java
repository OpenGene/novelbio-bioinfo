package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import org.omg.CORBA.FREE_MEM;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
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
						gffDetailGeneRef.removeIso(gffIsoRef.getName());
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
	private void compareIso(GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		//设定名字，防止gffGeneIsoInfoRef和gffGeneIsoInfoThis一个名字
		//但是添加名字后，就会变掉了
		gffGeneIsoInfoRef.setName("REF" + SepSign.SEP_ID + gffGeneIsoInfoRef.getName());
		gffGeneIsoInfoThis.setName("THIS" + SepSign.SEP_ID + gffGeneIsoInfoRef.getName());
		
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), gffGeneIsoInfoRef.getChrID(), lsGffGeneIsoInfos);
		//TODO 开始比较每个exon，并进行修正
	}
	
	/**
	 * 重建转录本时用到，比较两个算法的转录本之间的差异
	 * 两个gffHashGene应该是同一个物种
	 * @param gffHashGene 另一个转录本，本方法可逆--另一个调用该方法得到的结果一样
	 * @return
	 */
	public static GffHashGene compHashGene(GffHashGene gffHashThis, GffHashGene gffHashGene, String chrLen, String gffHashGeneBed, int highExpReads)
	{
		
		GffHashGene gffHashGeneResult = new GffHashGene();
		//不是同一个物种就不比了
		if (gffHashGene.getTaxID() != gffHashThis.getTaxID()) {
			return null;
		}
		GffGeneCluster.setHighExpReads(highExpReads);
		GffGeneCluster.setMapReads(chrLen, gffHashGeneBed);
		for (Entry<String, ArrayList<GffDetailGene>> entry : gffHashThis.getChrhash().entrySet()) {
			String chrID = entry.getKey();
			System.out.println(chrID);
			ArrayList<GffDetailGene> lsThisGffDetail = entry.getValue();
			ArrayList<GffDetailGene> lsCmpGffDetail = gffHashGene.getChrhash().get(chrID);
			if (lsCmpGffDetail == null) {
				for (GffDetailGene gffDetailGene : lsThisGffDetail) {
					gffHashGeneResult.addGffDetailGene(chrID, gffDetailGene);
				}
				continue;
			}
			ArrayList<CompSubArrayCluster>  lstmpArrayClusters = ArrayOperate.compLs2(lsThisGffDetail, lsCmpGffDetail,true);
			for (CompSubArrayCluster compSubArrayCluster : lstmpArrayClusters) {
				//比较每一组里面的this和comp的GffDetailGene
				ArrayList<CompSubArrayInfo> lsThis = compSubArrayCluster.getLsCompSubArrayInfosThis();
				ArrayList<GffDetailGene> lsGffGeneThis = new ArrayList<GffDetailGene>();
				for (CompSubArrayInfo compSubArrayInfo : lsThis) {
					GffDetailGene gene =(GffDetailGene)compSubArrayInfo.cmp;
					lsGffGeneThis.add((GffDetailGene)compSubArrayInfo.cmp);
				}
				ArrayList<CompSubArrayInfo> lsComp = compSubArrayCluster.getLsCompSubArrayInfosComp();
				ArrayList<GffDetailGene> lsGffGeneComp = new ArrayList<GffDetailGene>();
				for (CompSubArrayInfo compSubArrayInfo : lsComp) {
					lsGffGeneComp.add((GffDetailGene)compSubArrayInfo.cmp);
				}
				GffGeneCluster gffGeneCluster = new GffGeneCluster(gffHashThis, gffHashGene, lsGffGeneThis, lsGffGeneComp);

				GffDetailGene gffdetail = gffGeneCluster.getCombGffDetail();
				if (gffdetail == null) {
					continue;
				}
				gffHashGeneResult.addGffDetailGene(chrID, gffdetail);
			}
		}
		return gffHashGeneResult;
	}
}
