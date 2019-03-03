package com.novelbio.bioinfo.gwas.convertformat;

/**
 * chr1	10100014147	0	14147	C	T
 * chr1	10100025983	0	25983	C	T
 * @author novelbio
 *
 */
public class PlinkMid {
	String chrId;
	String varId;
	int position;
	String ref;
	String alt;
	
	public PlinkMid(String content) {
		String[] ss = content.split("\t");
		chrId = ss[0];
		varId = ss[1];
		position = Integer.parseInt(ss[3]);
		ref = ss[4];
		alt = ss[5];
	}
	
	public String getChrId() {
		return chrId;
	}
	public String getVarId() {
		return varId;
	}
	public int getPosition() {
		return position;
	}
	public String getRef() {
		return ref;
	}
	public String getAlt() {
		return alt;
	}
	
}