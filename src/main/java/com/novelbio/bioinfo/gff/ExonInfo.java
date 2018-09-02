package com.novelbio.bioinfo.gff;

import com.novelbio.bioinfo.base.AlignExtend;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.base.binarysearch.ListEle;

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
public class ExonInfo extends AlignExtend implements Comparable<ExonInfo> {
	
	public ExonInfo() {}
	
	GffIso isoParent;
	
	@Override
	public String getName() {
		return isoParent.getName() +":"+ super.toString();
	}
	/**
	 * @param parent 必须是 {@link GffIso}
	 */
	@Override
	public void setParent(ListEle<? extends AlignExtend> parent) {
		isoParent = (GffIso)parent;
		setChrId(isoParent.getRefID());
	}
	
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(GffIso isoParent, boolean cis, int start, int end) {
		setStartEndLoc(start, end);
		setCis5to3(cis);
		this.isoParent = isoParent;
		setChrId(isoParent.getRefID());
	}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(boolean cis, int start, int end) {
		setStartEndLoc(start, end);
		setCis5to3(cis);
	}

	public ExonInfo clone() {
		ExonInfo result = null;
		result = (ExonInfo) super.clone();
		return result;
	}
	public GffIso getParent() {
		return isoParent;
	}
	public int getItemNum() {
		return getParent().indexOf(this);
	}
	/**
	 * 不能判断不同染色体上相同的坐标位点
	 * 不比较两个exon所在转录本的名字
	 * 也不比较他们自己的名字
	 * 仅比较坐标,方向和parentName
	 */
	public boolean equals(Object elementAbs) {
		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		ExonInfo element = (ExonInfo)elementAbs;
		if (this.isCis5to3() != element.isCis5to3()) {
			return false;
		}
		//先不比较两个exon所在转录本的名字
		return super.equalsRefAndLoc(element);
	}
	
	/**
	 * 不能判断不同染色体上相同的坐标位点
	 * 不比较两个exon所在转录本的名字
	 * 也不比较他们自己的名字
	 * 仅比较坐标和方向
	 */
	public boolean equalsLoc(ExonInfo element) {
		//先不比较两个exon所在转录本的名字
		return (getStartAbs() == element.getStartAbs() && getEndAbs() == element.getEndAbs() 
				&& Alignment.isEqual(super.cis5to3, element.cis5to3));
	}
	
	@Override
	public int hashCode() {
		int i = 1;
		if (cis5to3) {
			i = -1;
		}
		return getStartAbs() * 100000 + getEndAbs() * i + getParent().getRefIDlowcase().hashCode();
	}
	
	public String toString() {
		return getChrId() + "\t" + getStartCis() + "\t" + getEndCis();
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

