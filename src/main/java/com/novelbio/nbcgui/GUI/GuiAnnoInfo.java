package com.novelbio.nbcgui.GUI;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 * 给GUI界面信息的类
 * @author jie
 *
 */
public class GuiAnnoInfo {
	/** 数量信息 */
	double num;
	/** 文字信息 */
	String info;
	/** 列表信息 */
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
	/** 数量信息 */
	public double getNumDouble() {
		return num;
	}
	/** 数量信息 */
	public int getNumInt() {
		return (int) num;
	}
	/** 文字信息 */
	public String getInfo() {
		return info;
	}
	/** 列表信息 */
	public ArrayList<String[]> getLsInfo() {
		return lsInfo;
	}
}
