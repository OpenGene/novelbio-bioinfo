package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class ExonInfo extends ListDetailAbs {
	public ExonInfo() {}
	/**
	 * �����������Զ��趨�����յ�
	 * @param start ��1��ʼ����
	 * @param end ��1��ʼ����
	 * @param cis
	 */
	public ExonInfo(String IsoName, boolean cis, int start, int end) {
		super(IsoName, start + "_" +end, cis);
		numberstart = Math.min(start, end);
		numberend = Math.max(start, end);
	}
	/**
	 * �����������Զ��趨�����յ�
	 * @param start ��1��ʼ����
	 * @param end ��1��ʼ����
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
	 * �����жϲ�ͬȾɫ������ͬ������λ��
	 * ���Ƚ�����exon����ת¼��������
	 * Ҳ���Ƚ������Լ�������
	 * ���Ƚ�����ͷ���
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
	


}

