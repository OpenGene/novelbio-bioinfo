package com.novelbio.base.plot;

import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.XYPlot;

/**
 * 互动的plot，调用GRAL包实现
 * @author zong0jie
 *
 */
public abstract class PlotNBCInteractive extends PlotNBC{
	boolean plotareaAll = false;
	/**
	 * check whether just figure the figure or all the picture area(include the axis region)
	 * @param plotareaAll
	 */
	public void setPlotareaAll(boolean plotareaAll) {
		this.plotareaAll = plotareaAll;
	}
	public boolean isPlotareaAll() {
		return plotareaAll;
	}
	/**
	 * needs check
	 * @return
	 */
	public abstract Drawable getPlot();
}
