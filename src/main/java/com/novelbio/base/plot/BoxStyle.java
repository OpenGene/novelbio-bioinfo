package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Paint;

public class BoxStyle extends DotStyle {
	
	Paint colorBoxEdge;
	Paint colorBoxWhisker;
	Paint colorBoxCenter;
	/**
	 *  paint the outline of the point shape.
	 */
	BasicStroke basicStroke = null;
	
	
	
	/** always return BOXs */
	public int getStyle() {
		return STYLE_BOX;
	}
	
	public void setColorBoxCenter(Paint colorBoxCenter) {
		this.colorBoxCenter = colorBoxCenter;
	}
	
	public void setColorBoxEdge(Paint colorBoxEdge) {
		this.colorBoxEdge = colorBoxEdge;
	}
	
	/**
	 * 就那个连接最上面的横线和box的那条竖线
	 * @param colorBoxWhisker
	 */
	public void setColorBoxWhisker(Paint colorBoxWhisker) {
		this.colorBoxWhisker = colorBoxWhisker;
	}
	public Paint getColorBoxEdge() {
		return colorBoxEdge;
	}
	public Paint getColorBoxWhisker() {
		return colorBoxWhisker;
	}
	public Paint getColorBoxCenter() {
		return colorBoxCenter;
	}
	/**
	 *  paint the outline of the point shape.
	 */
	public void setBasicStroke(float width) {
		this.basicStroke = new BasicStroke(width);
	}
	
}
