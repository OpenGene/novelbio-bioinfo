package com.novelbio.analysis.seq.fasta;

/**
 * 单个碱基
 * @author zongjie
 *
 */
public class Base {
	String refId;
	int position;
	char base;
	
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public void setBase(char base) {
		this.base = base;
	}

	public String getRefId() {
		return refId;
	}
	public int getPosition() {
		return position;
	}
	public char getBase() {
		return base;
	}
}
