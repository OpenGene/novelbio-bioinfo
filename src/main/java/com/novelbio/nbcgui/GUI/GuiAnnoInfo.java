package com.novelbio.nbcgui.GUI;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 * ��GUI������Ϣ����
 * @author jie
 *
 */
public class GuiAnnoInfo {
	/** ������Ϣ */
	double num;
	/** ������Ϣ */
	String info;
	/** �б���Ϣ */
	ArrayList<String[]> lsInfo = new ArrayList<String[]>();
	
	public void setNum(double num) {
		this.num = num;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public void setLsInfo(ArrayList<String[]> lsInfo) {
		this.lsInfo = lsInfo;
	}
	public void addInfo(String[] info) {
		lsInfo.add(info);
	}
	/** ������Ϣ */
	public double getNumDouble() {
		return num;
	}
	/** ������Ϣ */
	public int getNumInt() {
		return (int) num;
	}
	/** ������Ϣ */
	public String getInfo() {
		return info;
	}
	/** �б���Ϣ */
	public ArrayList<String[]> getLsInfo() {
		return lsInfo;
	}
}
