package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
/**
 * �̳� Comparable �ӿڣ�ֻ�Ƚ�score
 * @author zong0jie
 *
 */
public class ListDetailBin extends ListDetailAbs{

	public ListDetailBin(String chrID, String ItemName, Boolean cis5to3) {
		super(chrID, ItemName, cis5to3);
		// TODO Auto-generated constructor stub
	}
	
	public ListDetailBin()
	{}
	
	double score = 0;
	/**
	 * �趨������������Ҫ����doubleֵ
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}
	/**
	 * ��÷�����������Ҫ�����doubleֵ
	 * @return
	 */
	public double getScore() {
		return score;
	}
	String description = "";
	/**
	 * �趨������������Ҫ����stringֵ
	 * @param score
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * ���������������Ҫ�����stringֵ
	 * @param description
	 */
	public String getDescription() {
		return description;
	}
	public ListDetailBin clone() {
		ListDetailBin result = (ListDetailBin) super.clone();
		result.score = score;
		return result;
	}

	@Override
	public int compareTo(ListDetailAbs o) {
		ListDetailBin listDetailBin = (ListDetailBin)o;
		if (getScore() > listDetailBin.getScore()) {
			return 1;
		}
		else if (getScore() < listDetailBin.getScore()) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	
}
