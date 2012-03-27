package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Paint;

public class BarStyle extends DotStyle{
	Paint barEdgeColor = null;
	/**
	 *  paint the outline of the point shape.
	 */
	BasicStroke basicStroke = null;
	/**
	 * if the style is bar, this value is the bar width info
	 */
	double barWidth = 0;
	/**
	 * always return BAR
	 */
	public int getStyle() {
		return STYLE_BAR;
	}
	
	/**
	 * if the style is bar, set the bar width info
	 * @param barWidth
	 */
	public void setBarWidth(double barWidth) {
		this.barWidth = barWidth;
	}
	/**
	 * if the style is bar, get the bar width info
	 * @return
	 */
	public double getBarWidth() {
		return barWidth;
	}
	
	/**
	 *  paint the outline of the point shape.
	 */
	public void setBasicStroke(BasicStroke basicStroke) {
		this.basicStroke = basicStroke;
	}
	/**
	 *  paint the outline of the point shape.
	 */
	public BasicStroke getBasicStroke() {
		return basicStroke;
	}
	public void setEdgeColor(Paint barEdgeColor) {
		this.barEdgeColor = barEdgeColor;
	}
	public Paint getEdgeColor() {
		return barEdgeColor;
	}
}
