package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class ExonInfo extends ListDetailAbs {
	public ExonInfo() {}
	/**
	 * 根据正反向自动设定起点和终点
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
	 * 不能判断不同染色体上相同的坐标位点
	 * 不比较两个exon所在转录本的名字
	 */
	public boolean equals(Object elementAbs) {
		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		ExonInfo element = (ExonInfo)elementAbs;
		//先不比较两个exon所在转录本的名字
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
		 * list--所有isoform
		 * list--每个isoform中该组的所有exon
		 */
		ArrayList<ArrayList<ExonInfo>> lsExonCluster = new ArrayList<ArrayList<ExonInfo>>();
		ArrayList<String> lsIsoName = new ArrayList<String>();
		
		/**
		 * 存储那些跳过exon的转录本，记录跳过的是哪一个exon，只记录前一个exon的位置 
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
			//如果本组中没有exon并且也没有跨越的junction，说明本组没有可变的exon
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

