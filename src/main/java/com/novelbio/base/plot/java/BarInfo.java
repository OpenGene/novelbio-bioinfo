package com.novelbio.base.plot.java;
/**
 * ר�Ż�bar���࣬����bar������
 * @author zong0jie
 *
 */
public class BarInfo {
	double x = 0;
	double y = 0;
	String barName = "";
	public BarInfo(double x, double high, String name) {
		this.x = x;
		this.y = high;
		this.barName = name;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
	public void setBarName(String barName) {
		this.barName = barName;
	}
}
