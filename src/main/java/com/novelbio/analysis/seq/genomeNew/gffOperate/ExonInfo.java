package com.novelbio.analysis.seq.genomeNew.gffOperate;

import org.apache.poi.ss.formula.functions.NumericFunction;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;


public class ExonInfo extends ListDetailAbs implements Comparable<ExonInfo>
{
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
	/**
	 * �����������Զ��趨�����յ�
	 * @param start
	 * @param end
	 * @param cis
	 */
	public ExonInfo() {
		super(null, null, null);
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
	/**
	 * �����жϲ�ͬȾɫ������ͬ������λ��
	 */
	public boolean equals(Object elementAbs)
	{
		if (this == elementAbs) return true;
		
		if (elementAbs == null) return false;
		
		if (getClass() != elementAbs.getClass()) return false;
		ExonInfo element = (ExonInfo)elementAbs;
		//�Ȳ��Ƚ�����exon����ת¼��������
//		if (exon[0] == element.exon[0] && exon[1] == element.exon[1] && element.getParentName().equals(element.getParentName()) )
		if (numberstart == element.numberstart && numberend == element.numberend && super.cis5to3 == element.cis5to3 )
		{
			return true;
		}
		return false;
	}
	@Override
	public int compareTo(ExonInfo o) {
		Integer o1start = getStartCis(); Integer o1end = getEndCis();
		Integer o2start = o.getStartCis(); Integer o2end = o.getEndCis();
		if (isCis5to3()) {
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
}