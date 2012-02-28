package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.analysis.seq.genomeNew.listOperate.ElementAbs;

public class ExonInfo implements ElementAbs, Comparable<ExonInfo>
{
	GffGeneIsoInfo gffGeneIsoInfo = null;
	int[] exon = new int[2];
	boolean cis;
	public ExonInfo(int[] exon) {
		this.exon = exon;
	}
	/**
	 * 根据正反向自动设定起点和终点
	 * @param start
	 * @param end
	 * @param cis
	 */
	public ExonInfo(GffGeneIsoInfo gffGeneIsoInfo, int start, int end, boolean cis) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
		this.cis = cis;
		if (cis) {
			exon[0] = Math.min(start, end);
			exon[1] = Math.max(start, end);
		}
		else {
			exon[0] = Math.max(start, end);
			exon[1] = Math.min(start, end);
		}
	}
	public ExonInfo() {
	}
	
	public void setCis5to3(boolean cis5to3)
	{
		this.cis = cis5to3;
	}
	@Override
	public Boolean isCis5to3() {
		return cis;
	}

	public void setStartCis(int startLoc)
	{
		exon[0] = (int)startLoc;
	}
	public void setEndCis(int endLoc)
	{
		exon[1] = (int)endLoc;
	}
	@Override
	public int getStartCis() {
		return exon[0];
	}
	@Override
	public int getEndCis() {
		return exon[1];
	}
	@Override
	public int getStartAbs() {
		return Math.min(exon[0], exon[1]);
	}
	@Override
	public int getEndAbs() {
		return Math.max(exon[0], exon[1]);
	}

	@Override
	public int getLen() {
		return Math.abs(exon[0] - exon[1]) + 1;
	}

	public ExonInfo clone()
	{
		ExonInfo exonInfo = new ExonInfo(gffGeneIsoInfo, getStartCis(), getEndCis(), cis);
		return exonInfo;
	}
	/**
	 * 不能判断不同染色体上相同的坐标位点
	 */
	public boolean equals(Object elementAbs)
	{

		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		ExonInfo element = (ExonInfo)elementAbs;
		//先不比较两个exon所在转录本的名字
//		if (exon[0] == element.exon[0] && exon[1] == element.exon[1] && element.getParentName().equals(element.getParentName()) )
		if (exon[0] == element.exon[0] && exon[1] == element.exon[1] )
		{
			return true;
		}
		return false;
	}
	/**
	 * 本外显子的   起点_终点
	 */
	@Override
	public String getName() {
		return exon[0] + "_" +  exon[1];
	}
	@Override
	public int compareTo(ExonInfo o) {
		Integer o1start = exon[0]; Integer o1end = exon[1];
		Integer o2start = o.getStartCis(); Integer o2end = o.getEndCis();
		if (cis) {
			int result = o1start.compareTo(o2start);
			if (result == 0) {
				return o1end.compareTo(o2end);
			}
			return result;
		}
		else {
				int result = - o1start.compareTo(o2start);
				if (result == 0) {
					return - o1end.compareTo(o2end);
				}
				return result;
			}
	}
	@Override
	public String getParentName() {
		return gffGeneIsoInfo.getName();
	}
}