package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;


/**
 * ×î¼òµ¥µÄalign
 * @author zong0jie
 *
 */
public class Align implements Alignment{
	int start, end;
	Boolean cis5to3;
	public Align(int start, int end) {
		this.start = start;
		this.end = end;
	}
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	@Override
	public int getStartAbs() {
		return Math.min(start, end);
	}

	@Override
	public int getEndAbs() {
		return Math.max(start, end);
	}

	@Override
	public Boolean isCis5to3() {
		return cis5to3;
	}

	@Override
	public int Length() {
		return Math.abs(start - end + 1);
	}
	
}
