package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.base.plot.java.BarInfo;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.points.PointRenderer;

public class PlotBar extends PlotNBCInteractive{
	public static void main(String[] args) {
		BarInfo barInfo = new BarInfo(1, 10, "aaa");
		BarInfo barInfo2 = new BarInfo(2, 12, "bbb");
		PlotBar plotBar = new PlotBar();
		ArrayList<BarInfo> lsBarInfos = new ArrayList<BarInfo>();
		lsBarInfos.add(barInfo); lsBarInfos.add(barInfo2);
		BarStyle barStyle = new BarStyle();
		barStyle.setBarWidth(1);
		barStyle.setBasicStroke(new BasicStroke(0.2f));
		barStyle.setColor(BarStyle.getGridentColorBrighter(Color.BLUE));
		barStyle.setColorEdge(BarStyle.getGridentColorDarker(Color.BLUE));
		plotBar.addBarPlot(lsBarInfos, barStyle);
		plotBar.setAxisX(0, 3);
		plotBar.setAxisY(0, 20);
		plotBar.setInsets(PlotScatter.INSETS_SIZE_ML);
		plotBar.saveToFile("/home/zong0jie/桌面/aaa.jpg", 1000, 1000);
	}
	PlotScatter plotScatter = new PlotScatter();
    /**
     * using data to plot the Bar figure, 直接加入plot，不进入hash表
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void addBarPlot(List<BarInfo> lsBarInfos, BarStyle barStyle) {
    	DataTable data = new DataTable(Double.class, Double.class, String.class);
    	for (BarInfo barInfo : lsBarInfos) {
			data.add(barInfo.getX(), barInfo.getHeigth(), barInfo.getBarName());
		}    	
    	if (plotScatter.plot == null) {
    		plotScatter.plot = new BarPlot(data);
		}
    	else {
    		plotScatter.plot.add(data);
		}
    	
    	
    	if (barStyle.getBarWidth() != 0) {
    		plotScatter.plot.setSetting(BarPlot.BAR_WIDTH, barStyle.getBarWidth());
			}
			PointRenderer pointRenderer = plotScatter.plot.getPointRenderer(data);
			pointRenderer.setSetting(PointRenderer.COLOR, barStyle.getColor());
			pointRenderer.setSetting(BarPlot.BarRenderer.STROKE, barStyle.getBasicStroke());
			pointRenderer.setSetting(BarPlot.BarRenderer.STROKE_COLOR, barStyle.getEdgeColor());
		//规定，dotname在第3列，dotvalue也就是常规value在第二列
			//the third column is the name column
			pointRenderer.setSetting(PointRenderer.VALUE_COLUMN, 2);
			pointRenderer.setSetting(PointRenderer.VALUE_DISPLAYED, barStyle.isValueVisible());
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
    /**
     *  设定坐标轴边界
     * @param x1
     * @param x2
     */
    public void setAxisX(double x1, double x2) {
    	plotScatter.setAxisX(x1, x2);
    }
    /**
     * 设定坐标轴边界
     * @param y1
     * @param y2
     */
    public void setAxisY(double y1, double y2) {
    	plotScatter.setAxisY(y1, y2);
    }
    /**
     * 设置标题
     * @param title main title
     * @param fontTitle font of the title
     * @param title
     */
    public void setTitle(String title, Font fontTitle)  {
    	plotScatter.setTitle(title, fontTitle);
    }
    /**
     * 设置标题
     * @param titleX tile on axis x
     * @param fontX font of the title
     * @param spaceX ticks interval, 0 means not set the space
     */
    public void setTitleX(String titleX, Font fontX, double spaceX)  {
    	plotScatter.setTitleX(titleX, fontX, spaceX);
    }
    /**
     * 设置标题
     * @param titleY tile on axis y
     * @param fontY font of the title
     * @param spaceY ticks interval, 0 means not set the space
     */
    public void setTitleY(String titleY, Font fontY, double spaceY)  {
    	plotScatter.setTitleY(titleY, fontY, spaceY);
    }
    public void setAxisTicksX(Map<Double, String> mapTicks, Font fontTicks) {
    	plotScatter.setAxisTicksX(mapTicks, fontTicks);
	}
    public void setAxisTicksY(Map<Double, String> mapTicks, Font fontTicks) {
    	plotScatter.setAxisTicksY(mapTicks, fontTicks);
	}
    public void saveToFile(String outputFileName, int Width, int Height) {
    	plotScatter.saveToFile(outputFileName, Width, Height);
    }
}
