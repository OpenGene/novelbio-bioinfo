package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class ExonInfo extends ListDetailAbs {
	public ExonInfo() {}
	/**
	 * �����������Զ��趨�����յ�
	 * @param start
	 * @param end
	 * @param cis
	 */
	public ExonInfo(String IsoName, boolean cis, int start, int end) {
		super(IsoName, start + "_" +end, cis);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	
	public void setStartCis(int startLoc)
	{
		if (cis5to3) {
			numberstart = startLoc;
		}
		else {
			numberend = startLoc;
		}
	}
	public void setEndCis(int endLoc)
	{
		if (cis5to3) {
			numberend = endLoc;
		}
		else {
			numberstart = endLoc;
		}
	}
	public ExonInfo clone()
	{
		ExonInfo result = null;
		result = (ExonInfo) super.clone();
		return result;
	}
	/**
	 * �����жϲ�ͬȾɫ������ͬ������λ��
	 * ���Ƚ�����exon����ת¼��������
	 */
	public boolean equals(Object elementAbs) {
		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		ExonInfo element = (ExonInfo)elementAbs;
		//�Ȳ��Ƚ�����exon����ת¼��������
//		if (exon[0] == element.exon[0] && exon[1] == element.exon[1] && element.getParentName().equals(element.getParentName()) )
		if (numberstart == element.numberstart && numberend == element.numberend && super.cis5to3 == element.cis5to3 ) {
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		int i = 1;
		if (cis5to3) {
			i = -1;
		}
		return numberstart * 100000 + numberend * i;
	}
	
	public static class ExonCluster {
		Boolean sameExon = null;
		String chrID;
		int startLoc = 0;
		int endLoc = 0;
		ArrayList<ExonInfo> lsCombExon;
		/**
		 * list--����isoform
		 * list--ÿ��isoform�и��������exon
		 */
		ArrayList<ArrayList<ExonInfo>> lsExonCluster = new ArrayList<ArrayList<ExonInfo>>();
		ArrayList<String> lsIsoName = new ArrayList<String>();
		
		/**
		 * �洢��Щ����exon��ת¼������¼����������һ��exon��ֻ��¼ǰһ��exon��λ�� 
		 */
		HashMap<String, Integer> hashIsoExonNum = new HashMap<String, Integer>();
		
		public ExonCluster(String chrID, int start, int end) {
			this.chrID = chrID;
			this.startLoc = Math.min(start, end);
			this.endLoc = Math.max(start, end);
		}
		public String getLocInfo() {
			return chrID + ":" + startLoc + "-" + endLoc;
		}
		
		public void addExonCluster(String isoName, ArrayList<ExonInfo> lsExon) {
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
			ExonInfo exonOld = lsExonCluster.get(0).get(0);
			for (int i = 1; i < lsExonCluster.size(); i++) {
				if (lsExonCluster.get(i).size() != 1) {
					sameExon = false;
					break;
				}
				ExonInfo exon = lsExonCluster.get(i).get(0);
				if (!exon.equals(exonOld)) {
					sameExon = false;
					break;
				}
			}
			return sameExon;
		}
		/** ���ظ�exonCluster�е�����exon */
		public ArrayList<ExonInfo> getAllExons() {
			if (lsCombExon != null) {
				return lsCombExon;
			}
			combExon();
			return lsCombExon;
		}
		
		private void combExon() {
			lsCombExon = new ArrayList<ExonInfo>();
			//����ȥ�ظ���hash��
			HashSet<ExonInfo> hashExon = new HashSet<ExonInfo>();
			for (ArrayList<ExonInfo> lsExon : lsExonCluster) {
				for (ExonInfo is : lsExon) {
					hashExon.add( is);
				}
			}
			for (ExonInfo exonInfo : hashExon) {
				lsCombExon.add(exonInfo);
			}
		}
		/**
		 * @param Isoname
		 * @param exonNumStart
		 */
		public void setIso2JunctionStartExonNum(String Isoname, int exonNumStart) {
			hashIsoExonNum.put(Isoname, exonNumStart);
		}
		
		public HashMap<String, Integer> getHashIsoExonNum() {
			return hashIsoExonNum;
		}

		
	}
}

