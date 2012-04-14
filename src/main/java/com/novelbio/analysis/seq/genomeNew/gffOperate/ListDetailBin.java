package com.novelbio.analysis.seq.genomeNew.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

public class ListDetailBin extends ListDetailAbs{

	public ListDetailBin(String chrID, String ItemName, Boolean cis5to3) {
		super(chrID, ItemName, cis5to3);
		// TODO Auto-generated constructor stub
	}
	
	public ListDetailBin()
	{}
	
	double score = 0;
	public void setScore(double score) {
		this.score = score;
	}
	public double getScore() {
		return score;
	}
	
	public ListDetailBin clone() {
		ListDetailBin result = null;
		result = (ListDetailBin) super.clone();
		result.score = score;
		return result;
	}
}
