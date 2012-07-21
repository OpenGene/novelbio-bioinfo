package com.novelbio.analysis.seq.genomeNew.getChrSequence;
/**
 * ר�ű���������Ϣ��һ��С�࣬���Ե����ṹ��
 * @author zong0jie
 *
 */
public class LocInfo {
	private boolean cis5to3 = true;
	private String chrID = "";
	private int startLoc = -1;
	private int endLoc = -1;
	private String type = "";
	/**
	 * @param chrID
	 * @param startLoc ʵ����������
	 * @param endLoc ʵ���յ������
	 */
	public LocInfo(String chrID, int startLoc, int endLoc) {
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}
	public LocInfo(String chrID, int startLoc, int endLoc, boolean cis5to3) {
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.cis5to3 = cis5to3;
	}
	public LocInfo(String locName, String chrID, int startLoc, int endLoc, boolean cis5to3) {
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.cis5to3 = cis5to3;
		this.type = locName;
	}

	public String getChrID() {
		return chrID;
	}
	public int getEndLoc() {
		return endLoc;
	}
	public int getStartLoc() {
		return startLoc;
	}
	public String getLocName() {
		return type;
	}
	public boolean isCis5to3() {
		return cis5to3;
	}
	public int getLength() {
		return endLoc - startLoc + 1;
	}
	/**
	 * return locName +"\t"+ chrID +"\t"+ startLoc +"\t"+ endLoc;
	 */
	public String toString() {
		String mylocName = "", mychrID = "";
		if (type != null && !type.equals("")) {
			mylocName = type + "\t";
		}
		else
			mylocName = "";
		if (chrID != null && !chrID.equals("")) {
			mychrID = chrID + "\t";
		}
		else {
			mychrID = "";
		}
		return mylocName +mychrID + startLoc +"\t"+ endLoc;
	}
}
