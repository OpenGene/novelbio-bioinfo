package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;


/**
 * ��򵥵�align
 * @author zong0jie
 *
 */
public class Align implements Alignment{
	int start, end;
	Boolean cis5to3;
	public Align(int start, int end) {
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
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
		return start;
	}

	@Override
	public int getEndAbs() {
		return end;
	}

	@Override
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	@Override
	public int Length() {
		return Math.abs(start - end) + 1;
	}
	@Override
	public int getStartCis() {
		if (cis5to3 == null || isCis5to3()) {
			return Math.min(start, end);
		}
		return Math.max(start, end);
	}
	@Override
	public int getEndCis() {
		if (cis5to3 == null || isCis5to3()) {
			return Math.max(start, end);
		}
		return Math.min(start, end);
	}
	
}