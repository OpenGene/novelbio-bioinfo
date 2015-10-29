package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.listOperate.ListDetailAbs;
/**
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode 仅比较ChrID + "//" + numberstart + "//" + numberstart;
 * 
 * 不比较两个exon所在转录本的名字<br>
 * 包括<br>
 * 条目起点 numberstart<br>
 * 条目终点 numberend<br>
 * 条目方向 cis5to3
 * @author zong0jie
 *
 */
public class ExonInfo extends ListDetailAbs implements Comparable<ExonInfo> {
	public ExonInfo() {}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(GffGeneIsoInfo gffGeneIsoInfo, boolean cis, int start, int end) {
		super("", start + "_" +end, cis);
		super.setParentListAbs(gffGeneIsoInfo);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(boolean cis, int start, int end) {
		super("", start + "_" +end, cis);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	public void setStartCis(int startLoc) {
		if (cis5to3) {
			numberstart = startLoc;
		} else {
			numberend = startLoc;
		}
	}
	public void setEndCis(int endLoc) {
		if (cis5to3) {
			numberend = endLoc;
		} else {
			numberstart = endLoc;
		}
	}
	public ExonInfo clone() {
		ExonInfo result = null;
		result = (ExonInfo) super.clone();
		return result;
	}
	public GffGeneIsoInfo getParent() {
		return (GffGeneIsoInfo) listAbs;
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
		if (numberstart == element.numberstart && numberend == element.numberend && super.cis5to3 == element.cis5to3 ) {
			if (getParent().getRefIDlowcase().equalsIgnoreCase(element.getParent().getRefIDlowcase())) {
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		int i = 1;
		if (cis5to3) {
			i = -1;
		}
		return numberstart * 100000 + numberend * i + getParent().getRefIDlowcase().hashCode();
	}
	
	public String toString() {
		return getRefID() + "\t" + getStartCis() + "\t" + getEndCis();
	}
	@Override
    public int compareTo(ExonInfo o) {
		Integer o1startCis = getStartCis(); Integer o1endCis = getEndCis();
		Integer o2startCis = o.getStartCis(); Integer o2endCis = o.getEndCis();
		
		if (isCis5to3() == null || isCis5to3()) {
			int result = o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return o1endCis.compareTo(o2endCis);
			}
			return result;
		} else {
			int result = - o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return - o1endCis.compareTo(o2endCis);
			}
			return result;
		}
    }
}

