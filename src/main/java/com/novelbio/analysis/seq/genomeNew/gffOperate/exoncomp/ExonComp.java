package com.novelbio.analysis.seq.genomeNew.gffOperate.exoncomp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;

public class ExonComp {
	
	GffDetailGene gffDetailGene = null;
	public void setGffDetailGene(GffDetailGene gffDetailGene) {
		this.gffDetailGene = gffDetailGene;
	}
	/**
	 * ���澭�������exon
	 */
	ArrayList<ExonCluster> lsExonClusters = new ArrayList<ExonCluster>();
	
	/**
	 * ��һ��gffDetailGene�е�ת¼���ϲ�����
	 * @param gffDetailGene
	 */
	private ArrayList<ExonInfo> compIso() {
		gffDetailGene.removeDupliIso();
		ArrayList<GffGeneIsoInfo> lsIsos = gffDetailGene.getLsCodSplit();
		ArrayList<ExonInfo> lsExonAll = new ArrayList<ExonInfo>();
		//��ȫ����exon����һ��list���沢������
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
			if (gffGeneIsoInfo.isCis5to3() != gffDetailGene.isCis5to3()) {
				continue;
			}
			lsExonAll.addAll(gffGeneIsoInfo);
		}
		if (gffDetailGene.isCis5to3()) {
			Collections.sort(lsExonAll);
		}
		else {
			Collections.sort(lsExonAll);
		}
		return lsExonAll;
	}
	ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
	
	/**
	 * �����������exonlist�ϲ�����ü���������exon�����ڷֶ�
	 */
	private void combExon(boolean cis5to3, ArrayList<int[]> lsAllExon)
	{
		lsExonBounder.clear();
		int[] exonOld = lsAllExon.get(0).clone(); boolean allFinal = false;//���һ��exon�Ƿ���Ҫ�����list��
		lsExonBounder.add(exonOld);
		for (int i = 1; i < lsAllExon.size(); i++) {
			int[] exon = lsAllExon.get(i);
			if (cis5to3 )
			{
				if (exon[0] <= exonOld[1]) {
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				}
				else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
			else {
				if (exon[0] >= exonOld[1]) {
					if (exon[1] < exonOld[1]) {
						exonOld[1] = exon[1];
					}
				}
				else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
		}
	}
	/**
	 * ���շ���õı߽�exon����ÿ��ת¼�����л���
	 */
	private void setExonCluster()
	{
		ArrayList<GffGeneIsoInfo> lsIsos = gffDetailGene.getLsCodSplit();
		for (int[] exonBound : lsExonBounder) {
			ExonCluster exonCluster = new ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (GffGeneIsoInfo gffGeneIsoInfo : lsIsos) {
				if (gffGeneIsoInfo.isCis5to3() != gffDetailGene.isCis5to3()) {
					continue;
				}
				
				ArrayList<ExonInfo> lsExonClusterTmp = new ArrayList<ExonInfo>();
				ArrayList<ExonInfo> lsExon = gffGeneIsoInfo;
				int beforeExonNum = 0;//�����isoform����û������bounder���е�exon����ô��Ҫ��¼��isoform��ǰ������exon��λ�ã����ڲ��ҿ����û�п����exon
				boolean junc = false;//�����isoform����û������bounder���е�exon����ô����Ҫ��¼������exon��λ�ã��ͽ����flag����Ϊtrue
				for (int i = 0; i < lsExon.size(); i++) {
					ExonInfo exon = lsExon.get(i);
					if (gffDetailGene.isCis5to3()) {
						if (exon.getEndCis() < exonBound[0]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getStartCis() >= exonBound[0] && exon.getEndCis() <= exonBound[1]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getStartCis() > exonBound[1]) {
							break;
						}
					}
					else {
						if (exon.getEndCis() > exonBound[0]) {
							junc = true;
							beforeExonNum = i;
							continue;
						}
						else if (exon.getStartCis() <= exonBound[0] && exon.getEndCis() >= exonBound[1]) {
							lsExonClusterTmp.add(exon);
							junc = false;
						}
						else if (exon.getStartCis() < exonBound[1]) {
							break;
						}
					}
				}
				if (lsExonClusterTmp.size() > 0) {
					exonCluster.addExonCluster(gffGeneIsoInfo.getName(), lsExonClusterTmp);
				}
				if (junc && beforeExonNum < lsExon.size()-1) {
					exonCluster.setIsoJun(gffGeneIsoInfo.getName(), beforeExonNum);
				}
			}
			lsExonClusters.add(exonCluster);
		}
	}
	/**
	 * �����в����exonϵ��
	 * @return
	 */
	public ArrayList<ExonCluster> getDifExonCluster() {
		ArrayList<int[]> lsAllExon = compIso();
		combExon(gffDetailGene.isCis5to3(), lsAllExon);
		setExonCluster();
		ArrayList<ExonCluster> lsDifExon = new ArrayList<ExonCluster>();
		for (ExonCluster exonClusters : lsExonClusters) {
			if (exonClusters.isSameExon()) {
				continue;
			}
			lsDifExon.add(exonClusters);
		}
		return lsDifExon;
	}
}

class ExonCluster
{
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	public ExonCluster(String chrID, int start, int end) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
	}
	public String getLocInfo()
	{
		return chrID + ":" + startLoc + "-" + endLoc;
	}
	/**
	 * list--����isoform
	 * list--ÿ��isoform�и��������exon
	 */
	ArrayList<ArrayList<int[]>> lsExonCluster = new ArrayList<ArrayList<int[]>>();
	ArrayList<String> lsIsoName = new ArrayList<String>();
	
	/**
	 * �洢��Щ����exon��ת¼������¼����������һ��exon��ֻ��¼ǰһ��exon��λ�� 
	 */
	HashMap<String, Integer> hashIsoExonNum = new HashMap<String, Integer>();
	
	public void addExonCluster(String isoName, ArrayList<int[]> lsExon) {
		lsExonCluster.add(lsExon);
		lsIsoName.add(isoName);
	}

	
	public boolean isSameExon() {
		if (sameExon != null) {
			return sameExon;
		}
		//���������û��exon����Ҳû�п�Խ��junction��˵������û�пɱ��exon
		if (lsExonCluster.size() >= 1 && hashIsoExonNum.size() >= 1) {
			sameExon = false;
			return false;
		}
		sameExon = true;
		if (lsExonCluster.get(0).size() != 1) {
			sameExon = false;
			return false;
		}
		int[] exonOld = lsExonCluster.get(0).get(0);
		for (int i = 1; i < lsExonCluster.size(); i++) {
			if (lsExonCluster.get(i).size() != 1) {
				sameExon = false;
				break;
			}
			int[] exon = lsExonCluster.get(i).get(0);
			if (exonOld[0] != exon[0] || exonOld[1] != exon[1]) {
				sameExon = false;
				break;
			}
		}
		return sameExon;
	}
	
	public ArrayList<int[]> getCombExon()
	{
		if (lsCombExon != null) {
			return lsCombExon;
		}
		combExon();
		return lsCombExon;
	}
	
	ArrayList<int[]> lsCombExon;
	private void combExon()
	{
		lsCombExon = new ArrayList<int[]>();
		lsCombExon.clear();
		String sep = "@@";
		//����ȥ�ظ���hash��
		HashMap<String, int[]> hashExon = new HashMap<String, int[]>();
		for (ArrayList<int[]> lsExon : lsExonCluster) {
			for (int[] is : lsExon) {
				String key = is[0] + sep + is[1];
				hashExon.put(key, is);
			}
		}
		for (int[] exon : hashExon.values()) {
			lsCombExon.add(exon);
		}
	}
	/**
	 * 
	 * @param Isoname
	 * @param exonNumStart
	 */
	public void setIsoJun(String Isoname, int exonNumStart) {
		hashIsoExonNum.put(Isoname, exonNumStart);
	}
	
	public HashMap<String, Integer> getHashIsoExonNum()
	{
		return hashIsoExonNum;
	}

	
}