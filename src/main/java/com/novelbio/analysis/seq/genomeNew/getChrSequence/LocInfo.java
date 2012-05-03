package com.novelbio.analysis.seq.genomeNew.getChrSequence;
/**
 * 专门保存坐标信息的一个小类，可以当作结构体
 * @author zong0jie
 *
 */
public class LocInfo {
	/**
	 * 
	 * @param chrID
	 * @param startLoc 实际起点闭区间
	 * @param endLoc 实际终点闭区间
	 */
	public LocInfo(String chrID, int startLoc, int endLoc)
	{
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}
	public LocInfo(String chrID, int startLoc, int endLoc, boolean cis5to3)
	{
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.cis5to3 = cis5to3;
	}
	public LocInfo(String locName, String chrID, int startLoc, int endLoc, boolean cis5to3)
	{
		this.chrID = chrID;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.cis5to3 = cis5to3;
		this.locName = locName;
	}
	private boolean cis5to3 = true;
	private String chrID = "";
	private int startLoc = -1;
	private int endLoc = -1;
	private String locName = "";
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
		return locName;
	}
	public boolean isCis5to3() {
		return cis5to3;
	}
	public int getLength()
	{
		return endLoc - startLoc + 1;
	}
	/**
	 * return locName +"\t"+ chrID +"\t"+ startLoc +"\t"+ endLoc;
	 */
	public String toString() {
		String mylocName = "", mychrID = "";
		if (locName != null && !locName.equals("")) {
			mylocName = locName + "\t";
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
