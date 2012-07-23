package com.novelbio.analysis.seq.rnaseq;

import java.lang.annotation.Retention;
import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.base.dataStructure.ArrayOperate;

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
	public GffGeneIsoInfo compareIso(String IsoName, GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneIsoInfoRef.clone();
		gffGeneIsoInfoResult.clear();
		
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); 
		lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), lsGffGeneIsoInfos);

		int[] boundBoth = getBothStartNumEndNum(lsExonClusters);
		
		int[] select = null;//�Ƿ���ӵ���Ref��lsexoninfo
		int[] boundInfo = new int[]{0, 0};;//�Ƚ�����exon�ı߽�0����ʾһ�� 1��ʾ��һ��
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRef);
			ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
			//��ͬ�ľ�ֱ��װ��ȥ
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
				if (boundBoth[1] == i)//TODO ���right�߽����Ϣ
					select = compareExonEnd(boundInfo,select,gffGeneIsoInfoRef, lsExonInfosRef, gffGeneIsoInfoThis, lsExonInfosThis);
				else
					select = compExonMidSelect(boundInfo, select, lsExonInfosRef, lsExonInfosThis);
			}
			else if (boundBoth[1] < i) {
				select = (lsExonInfosRef.size() == 1? new int[]{0, 0} : new int[]{1, 1});
			}
			//��boundInfo[1] != 0ʱ��������һ����ӵľͺ�
			lsExonInfos = getLsExonInfo(select, lsExonInfosRef, lsExonInfosThis);
			for (ExonInfo exonInfo : lsExonInfos) {
				gffGeneIsoInfoResult.add(exonInfo);
			}
			boundInfo = compareLsExonInfoBound(exonCluster);
		}
		return gffGeneIsoInfoResult;
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
	 * @param lastBound
	 * @param lastSelect
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return int[2]
	 * 0��0 ref���߽� 1 this���߽�
	 * 1��0 ref�յ�߽� 1 this�յ�߽�
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
	 * �������
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis ���벻Ϊ0
	 * @return true RefExon
	 * false ThisExon
	 */
	private int[] compareExonStart(GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		int[] result = new int[]{1, 1};
		//����������
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
	 * ����ĩ��
	 * @param lastBound
	 * @param lastSelectInfo
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis ���벻Ϊ0
	 * @param lsExonInfosThis
	 * @return
	 * 0��0 ref���߽� 1 this���߽�<br>
	 * 1��0 ref�յ�߽� 1 this�յ�߽�<br>
	 * @return
	 */
	private int[] compareExonEnd(int[] lastBound, int[] lastSelectInfo, GffGeneIsoInfo gffGeneIsoInfoRef, ArrayList<ExonInfo> lsExonInfosRef, GffGeneIsoInfo gffGeneIsoInfoThis, ArrayList<ExonInfo> lsExonInfosThis) {
		int[] result = new int[]{1, 1};
		if (lastBound[1] == 1 && lastSelectInfo[1] == 0) {
			result[0] = 0;
		}
		
		//��������յ�
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
	 * ��ref��this��exon���ڱ߽�ʱ�����س��ı߽磬Ȼ���ڲ�����this��λ��
	 * @param selectInfo ����ѡ���λ�㣬��������
	 * 0��0 ref���߽� 1 this���߽�<br>
	 * 1��0 ref�յ�߽� 1 this�յ�߽�<br>
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
	 * ����select����Ϣ���������
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
	 * �Ƚϱ���exon�ı߽�
	 * @return int[2] 0һ�� 1��һ��
	 * 0: 0���һ�� 1��˲�һ��
	 * 1: 0�Ҷ�һ�� 1�Ҷ˲�һ��
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
