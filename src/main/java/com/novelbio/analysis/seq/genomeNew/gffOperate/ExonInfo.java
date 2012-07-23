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
	
	public void setStartCis(int startLoc) {
		if (cis5to3) {
			numberstart = startLoc;
		}
		else {
			numberend = startLoc;
		}
	}
	public void setEndCis(int endLoc) {
		if (cis5to3) {
			numberend = endLoc;
		}
		else {
			numberstart = endLoc;
		}
	}
	public ExonInfo clone() {
		ExonInfo result = null;
		result = (ExonInfo) super.clone();
		return result;
	}
	/**
	 * 不能判断不同染色体上相同的坐标位点
	 * 不比较两个exon所在转录本的名字
	 * 也不比较他们自己的名字
	 * 仅比较坐标和方向
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
		 * 如果该iso跳过了这个exon，则里面装空的list
		 */
		ArrayList<ArrayList<ExonInfo>> lsExonCluster = new ArrayList<ArrayList<ExonInfo>>();
		ArrayList<GffGeneIsoInfo> lsIsoParent = new ArrayList<GffGeneIsoInfo>();
		HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> mapIso2LsExon = new HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>>();
		/**
		 * 如果本组中该IsoName的转录本正好没有exon落在组中，也就是跳过去了，那么记录该Iso在本组的前一个exon的Num
		 */
		HashMap<String, Integer> hashIsoName2ExonNum = new HashMap<String, Integer>();
		
		public ExonCluster(String chrID, int start, int end) {
			this.chrID = chrID;
			this.startLoc = Math.min(start, end);
			this.endLoc = Math.max(start, end);
		}
		public String getLocInfo() {
			return chrID + ":" + startLoc + "-" + endLoc;
		}
		/**
		 * 如果该iso跳过了这个exon，则里面装空的list
		 * @param gffGeneIsoInfo
		 * @param lsExon
		 */
		public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, ArrayList<ExonInfo> lsExon) {
			lsExonCluster.add(lsExon);
			mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
		}
		public HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> getMapIso2LsExon() {
			return mapIso2LsExon;
		}
		/**
		 * 本组中是否为相同的exon，如果相同了那么也就没有可变剪接的说法了
		 * @return
		 */
		public boolean isSameExon() {
			if (sameExon != null) {
				return sameExon;
			}
			//如果本组中有不止一个exon的转录本，并且还有跨越的junction，说明本组有可变的exon
			if (lsExonCluster.size() >= 1 && hashIsoName2ExonNum.size() >= 1) {
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
				//比较第一个就行了，因为如果有两个直接就返回false了
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
		 * 如果本组中该IsoName的转录本正好没有exon落在组中，也就是跳过去了，那么记录该Iso在本组的前一个exon的Num
		 * @param Isoname
		 * @param exonNumStart
		 */
		public void setIso2JunctionStartExonNum(String Isoname, int exonNumStart) {
			hashIsoName2ExonNum.put(Isoname, exonNumStart);
		}
		/**
		 * 如果本组中该IsoName的转录本正好没有exon落在组中，也就是跳过去了，那么记录该Iso在本组的前一个exon的Num
		 * @return
		 */
		public HashMap<String, Integer> getHashIsoName2ExonNum() {
			return hashIsoName2ExonNum;
		}

		
	}
}

