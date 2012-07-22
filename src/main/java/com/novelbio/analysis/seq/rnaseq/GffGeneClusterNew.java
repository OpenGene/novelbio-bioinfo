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
	@SuppressWarnings("unused")
	private void compareIso(String IsoName, GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		GffGeneIsoInfo gffGeneIsoInfoRefDuplicate = gffGeneIsoInfoRef.clone();
		gffGeneIsoInfoRef.clear();//���װ��������棬�����������
		
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRefDuplicate); 
		lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), gffGeneIsoInfoRef.getChrID(), lsGffGeneIsoInfos);
//		װ�����Ľ��iso
//		GffGeneIsoInfo gffGeneIsoInfo = new GffGeneIsoCis(IsoName, gffGeneIsoInfoRef.getGffDetailGeneParent(), gffGeneIsoInfoRef.getGeneType());
		int[] boundBoth = getBothStartNumEndNum(lsExonClusters);
		boolean addRefLs = true;//�Ƿ���ӵ���Ref��lsexoninfo
		int[] boundInfo = new int[]{0, 0};;//�Ƚ�����exon�ı߽�0����ʾһ�� 1��ʾ��һ��
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRefDuplicate);
			ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
			//��ͬ�ľ�ֱ��װ��ȥ
			if (exonCluster.isSameExon()) {
				gffGeneIsoInfoRef.add(lsExonInfosRef.get(0));
				boundInfo = new int[]{0, 0};
				continue;
			}
			
			ArrayList<ExonInfo> lsExonInfos = null;
			if (boundBoth[0] == i) {
				addRefLs = compareExonLeft(gffGeneIsoInfoRefDuplicate, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
			}
			else if (boundInfo[1] == 0) {//ǰ��ı���һ�µ�
				if (boundBoth[1] == i) {//TODO ���right�߽����Ϣ
					addRefLs = compareExonRight(gffGeneIsoInfoRefDuplicate, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
				}
				else {
					addRefLs = compExonMid(lsExonInfosRef, lsExonInfosThis);
				}
			}
			//��boundInfo[1] != 0ʱ��������һ����ӵľͺ�
			lsExonInfos = (addRefLs? lsExonInfosRef : lsExonInfosThis);
			for (ExonInfo exonInfo : lsExonInfos) {
				gffGeneIsoInfoRef.add(exonInfo);
			}
			boundInfo = compareLsExonInfo(exonCluster);
		}
		
	}
	/**
	 * lsExonClusters������ת¼���ȽϵĽ��������ͼ<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E ��һ��ת¼��<br>
	 * *----*-----a-----b �ڶ���ת¼��<br>
	 * �������н�����ֵ����ͽ�����ֵ��յ����꣬Ҳ����a��b�����꣬��0��ʼ
	 * ��ô���Ƿ���2��3
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
	 * �������������бȽ�ʱ��ֱ�ӷ���lsExonInfosThis
	 * Ҳ�������µ�ת¼������ɵ�
	 * �������lsExonInfosThisΪ�գ��Ǿͷ���lsExonInfosRef
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
	 * �Ƚϱ���exon�ı߽�
	 * @return int[2] 0һ�� 1��һ��
	 * 0: ���
	 * 1: �Ҷ�
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
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis ���벻Ϊ0
	 * @return true RefExon
	 * false ThisExon
	 */
	private boolean compareExonLeft(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//����������
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
	 * �����Ҷ�
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis ���벻Ϊ0
	 * @param lsExonInfosThis
	 * @return true RefExon
	 * false ThisExon
	 */
	private boolean compareExonRight(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		//��������յ�
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
	 * ��ref��this��exon���ڱ߽�ʱ�����س���
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
