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
	private void compareIso(GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		//�趨���֣���ֹgffGeneIsoInfoRef��gffGeneIsoInfoThisһ������
		//����������ֺ󣬾ͻ�����
		gffGeneIsoInfoRef.setName("REF" + SepSign.SEP_ID + gffGeneIsoInfoRef.getName());
		gffGeneIsoInfoThis.setName("THIS" + SepSign.SEP_ID + gffGeneIsoInfoRef.getName());
		
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), gffGeneIsoInfoRef.getChrID(), lsGffGeneIsoInfos);
		//TODO ��ʼ�Ƚ�ÿ��exon������������
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
