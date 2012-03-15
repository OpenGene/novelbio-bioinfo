package com.novelbio.base.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map.Entry;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.util.Insets2D;

public class PlotScatter extends PlotNBC{
	HashMap<DotStyle, DataTable> hashDataTable = new HashMap<DotStyle, DataTable>();
	
	XYPlot plot;
	
	/**
	 * 如果点的格式是竖线的话，设定线的长度，这个最好能自动设定，从坐标范围内获取
	 */
	int lineLength =100;
    /**
     * 增加点
     * @param x
     * @param y
     */
    public void addXY(double x, double y, DotStyle dotStyle) {
    	DataTable dataTable = null;
    	if (!hashDataTable.containsKey(dotStyle)) {
    		dataTable = new DataTable(Double.class, Double.class);
			hashDataTable.put(dotStyle, dataTable);
		}
    	else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	dataTable.add(x,y);
    }
    /**
     * 坐标轴边界
     */
    Axis axisX = null, axisY = null;
    /**
     *  设定坐标轴边界
     * @param x1
     * @param x2
     */
    public void setAxisX(double x1, double x2) {
    	axisX = new Axis(x1, x2);
    }
    
    public void setTitle(String title)  {
    	 plot.setSetting(BarPlot.TITLE, title);
    }
    /**
     * 设定坐标轴边界
     * @param y1
     * @param y2
     */
    public void setAxisY(double y1, double y2) {
    	axisY = new Axis(y1, y2);
    }
    
    /**
     * 图片坐标轴到图片边缘的距离
     */
    int insetsTop = 30,
           insetsLeft = 60,
           insetsBottom = 60,
           insetsRight = 40;
    /**
     * 设定图片坐标轴到图片边缘的距离,这个一般走默认就好
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setInsets(int left, int top, int right, int bottom) {
    	this.insetsTop = top; this.insetsLeft = left;
    	this.insetsBottom = bottom; this.insetsRight = right;
    }
	@Override
	protected void draw(int width, int heigh) {
		plot = new XYPlot();
		//设定图片坐标轴到图片边缘的距离
		plot.setInsets(new Insets2D.Double( insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.setAxis(XYPlot.AXIS_X, axisX);//设置坐标轴
        plot.setAxis(XYPlot.AXIS_Y, axisY);//设置坐标轴
        
		int i = 0;
		for (Entry<DotStyle, DataTable> entry : hashDataTable.entrySet()) {
			DotStyle dotStyle = entry.getKey();
			if (dotStyle.getStyle() == DotStyle.STYLE_LINE) {
				dotStyle.setLineLength(lineLength);
			}
			DataTable dataTable = entry.getValue();
			DataSeries dataSeries = new DataSeries(dotStyle.getGroup(), dataTable,0,1);
			plot.add(dataSeries);
			   // Style data series
	        PointRenderer points = new DefaultPointRenderer2D();
	        points.setSetting(PointRenderer.SHAPE, dotStyle.getShape());
	        points.setSetting(PointRenderer.COLOR, dotStyle.getColor());
	        plot.setPointRenderer(dataSeries, points);
		}
		plot.getPlotArea().setSetting(PlotArea.BACKGROUND, bg);
		
		int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
    	bufferedImage = new BufferedImage(width, heigh, imageType);
    	
		DrawingContext context = new DrawingContext((Graphics2D) bufferedImage.getGraphics());
		if (bg == null && bg.equals(new Color(0,0,0,0))) {
			plot.draw(context);
			setBG(width, heigh);
		}
	}
	/**
	 * fill the insets with BG color, means fill the marginal area between the frame and the plot border.<br>
	 * because gral will not color the marginal area, so we should color the marginal area using this method
	 * @param width
	 * @param heigh
	 */
	private void setBG(int width, int heigh) {
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.setColor(bg);
		graphics.fillRect(0, 0, insetsLeft, heigh);//fill the left region
		graphics.fillRect(insetsLeft, 0, width-insetsLeft, insetsTop);//fill the upon region
		graphics.fillRect(insetsLeft, heigh-insetsBottom, width-insetsLeft, insetsBottom); //fill the bottom region
		graphics.fillRect(width-insetsRight, insetsTop, insetsRight, heigh - insetsTop - insetsBottom); //fill the right region
		
	}
}
