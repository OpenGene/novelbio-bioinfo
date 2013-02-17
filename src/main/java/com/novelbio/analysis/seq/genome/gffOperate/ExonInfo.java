package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
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
public class ExonInfo extends ListDetailAbs {
	public ExonInfo() {}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(String IsoName, boolean cis, int start, int end) {
		super(IsoName, start + "_" +end, cis);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start 从1开始记数
	 * @param end 从1开始记数
	 * @param cis
	 */
	public ExonInfo(GffGeneIsoInfo gffGeneIsoInfo, boolean cis, int start, int end) {
		super(gffGeneIsoInfo, start + "_" +end, cis);
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
			if (getParent().getChrID().equalsIgnoreCase(element.getParent().getChrID())) {
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
		return numberstart * 100000 + numberend * i + getParent().getChrID().hashCode();
	}
}

