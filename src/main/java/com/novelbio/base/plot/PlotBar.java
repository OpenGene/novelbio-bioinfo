package com.novelbio.base.plot;

import java.awt.Font;
import java.util.List;

import com.novelbio.base.plot.java.BarInfo;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.BarPlot;

public class PlotBar extends PlotNBCInteractive{
	public static void main(String[] args) {
		BarInfo barInfo = new BarInfo();
		barInfo.
	}
	PlotScatter plotScatter = new PlotScatter();
    /**
     * using data to plot the Bar figure, 直接加入plot，不进入hash表
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void addBarPlot(List<BarInfo> lsBarInfos, BarStyle dotStyle) {
    	DataTable data = new DataTable(Double.class, Integer.class, String.class);
    	for (BarInfo barInfo : lsBarInfos) {
			data.ad
		}
    	
    	
    	if (plotScatter.plot == null) {
    		plotScatter.plot = new BarPlot(dataTable);
		}
    	else {
    		plotScatter.plot.add(dataTable);
		}
    }
	
    public void changeDotStyle(DotStyle dotStyle) {
		plotScatter.changeDotStyle(dotStyle);
	}
	@Override
	public Drawable getPlot() {
		return plotScatter.getPlot();
	}
	@Override
	protected void draw(int width, int heigh) {
		plotScatter.draw(width, heigh);
	}
	  /**
     * 设定图片坐标轴到图片边缘的距离,这个一般走默认就好
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setInsets(int left, int top, int right, int bottom) {
    	plotScatter.setInsets(left, top, right, bottom);
    }
    /**
     * set the marge size of a figure, the bigger the marge be, the font of the tile will also bigger
     * @param int size 
     */
    public void setInsets(int size) {
    	plotScatter.setInsets(size);
    }
}
