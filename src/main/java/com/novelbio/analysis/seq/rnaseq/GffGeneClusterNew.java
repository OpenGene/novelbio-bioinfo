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
	/** �Ƿ���reference��GffDetailGene */
	boolean isContainsRef = true;
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	
	/** ������Ľ���������� */
	ArrayList<GffDetailGene> lsCombGenesResult;
	/**
	 * ��ѡ����Ҫ����������GffDetailGeneList������ѡ��RefGffGene
	 * �趨��ʱ��ὫlsGeneCluster����Ķ�Ӧ��Ŀɾ��
	 */
	ArrayList<GffDetailGene> lsGenesRef;
	/**
	 * list--���в�ͬ��Դ��Gffhash<br>
	 * list--ÿ��GffHash��GffDetail<br>
	 * <br>
	 * �����������RefGffGene������ڵ�һ��λ�á���ô���õ�һ��λ�õ�GffDetailGene���бȽ�
	 * �������û��RefGffGene����ѡ���������һ��GffDetailGene���бȽ�<br>
	 * <br>
	 * �������reference<b>���е�һ��һ����Reference��GffGene</b>
	 */
	ArrayList<ArrayList<GffDetailGene>> lsGeneCluster = new ArrayList<ArrayList<GffDetailGene>>();
	ArrayList<String> lsIsoName = new ArrayList<String>();
	
	double likelyhood = 0.5;
	/**
	 * �Ƿ���refGffDetailGene��û��˵��Ref���������loc
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
	
	
	/** �趨lsGenesRef��ͬʱ��lsGeneCluster�ж�Ӧ��Ŀɾ��
	 * Ϊ�ϲ�����׼��
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
		for (GffDetailGene gffDetailGeneRef : lsGenesRef) {//����ÿ��GffDetail
			for (ArrayList<GffDetailGene> lsgffArrayList : lsGeneCluster) {//���GffCluster����ÿ��GffHash��list�����һ��ֻ��һ��GffHash
				for (GffDetailGene gffDetailGeneCalculate : lsgffArrayList) {//�����һ��GffHash�����GffDetailGene
					for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//������GffDetailGene��ת¼��������ѡ����ӽ��Ľ��бȽ�
						GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, likelyhood).clone();
						gffDetailGeneRef.removeIso(gffIsoRef.getName());
						//TODO �½�һ��gffDetailȻ��Ž�ȥ���ܱȽϺ��ʰ�
					}
				}
			}
		}
	}
	/**
	 * �Ƚ�����������ͬ���н�����GffGeneIso����Ϣ������gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoThis
	 */
	private void compareIso(String IsoName, GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); 
		lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), gffGeneIsoInfoRef.getChrID(), lsGffGeneIsoInfos);
		
		//װ�����Ľ��iso
		GffGeneIsoInfo gffGeneIsoInfo = new GffGeneIsoCis(IsoName, gffGeneIsoInfoRef.getGffDetailGeneParent(), gffGeneIsoInfoRef.getGeneType());
		
		boolean leftBoth = false;
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRef);
			ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
			
			if (lsExonInfosRef.size() > 0 || lsExonInfosThis.size() > 0 ) {
				leftBoth = true;
			}
			
		}
		
	}
	private ArrayList<ExonInfo> compExonMid() {
		
		
		return null;
	}
	/**
	 * �Ƚϱ���
	 * @return int[2]
	 * 0: ���һ��
	 * 1: �Ҷ�һ��
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
	 * �������
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis
	 * @return
	 */
	private ArrayList<ExonInfo> compareExonLeft(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//����������
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) == 0 && gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(0)) == 0) {
			return getBoundListExonInfo(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) != 0) {
				return lsExonInfosRef;
			}
			else {
				return lsExonInfosThis;
			}
		}
	}
	/**
	 * �����Ҷ�
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis
	 * @return
	 */
	private ArrayList<ExonInfo> compareExonRight(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//��������յ�
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) == gffGeneIsoInfoRef.size() - 1 
				&& gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(lsExonInfosThis.size() - 1)) == gffGeneIsoInfoThis.size() - 1 ) {
			return getBoundListExonInfo(gffGeneIsoInfoRef.isCis5to3(), false, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) != gffGeneIsoInfoRef.size() - 1 ) {
				return lsExonInfosRef;
			}
			else {
				return lsExonInfosThis;
			}
		}
	}
	/**
	 * ��ref��this��exon���ڱ߽�ʱ�����س���
	 * @param cis
	 * @param start
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return
	 */
	private ArrayList<ExonInfo> getBoundListExonInfo(boolean cis, boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
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
				return lsExonInfosRef;
			}
			else {
				return lsExonInfosThis;
			}
		}
		else {
			if (refBound >= thisBound) {
				return lsExonInfosRef;
			}
			else {
				return lsExonInfosThis;
			}
		}
	}
	/**
	 * �ؽ�ת¼��ʱ�õ����Ƚ������㷨��ת¼��֮��Ĳ���
	 * ����gffHashGeneӦ����ͬһ������
	 * @param gffHashGene ��һ��ת¼��������������--��һ�����ø÷����õ��Ľ��һ��
	 * @return
	 */
	public static GffHashGene compHashGene(GffHashGene gffHashThis, GffHashGene gffHashGene, String chrLen, String gffHashGeneBed, int highExpReads)
	{
		
		GffHashGene gffHashGeneResult = new GffHashGene();
		//����ͬһ�����־Ͳ�����
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
				//�Ƚ�ÿһ�������this��comp��GffDetailGene
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
