package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JPanel;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;

public class PlotScatter extends PlotNBC{
	HashMap<DotStyle, DataTable> hashDataTable = new HashMap<DotStyle, DataTable>();
	
	XYPlot plot;
	String title = null, titleX = null, titleY = null;
	Double spaceX = null, spaceY = null;
    Font fontTitle = new Font(Font.SANS_SERIF, Font.PLAIN, 15), fontX = null, fontY = null;
    int InsetsSize = 200;
    public static final int INSETS_SIZE_S = 100;
    public static final int INSETS_SIZE_SM = 200;
    public static final int INSETS_SIZE_M = 300;
    public static final int INSETS_SIZE_ML = 400;
    public static final int INSETS_SIZE_L = 500;
    /**
     * 坐标轴边界
     */
    Axis axisX = null, axisY = null;
    /**
     * TODO: if style is line and y is much bigger than the Y axis length, than the point may not draw complete.
     * so we should set the point just near the bound of figure. so that the line can be drawn on the figure
     * add point
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
    	if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
			if (y > dotStyle.getLineLength()) {
				dotStyle.setLineLength(y);
			}
		}
    }
    /**
     * add point array, x.length must equals y.length
     * @param x
     * @param y
     */
    public void addXY(double[] x, double[] y, DotStyle dotStyle) {
    	if (x.length != y.length) {
			return;
		}
    	DataTable dataTable = null;
    	if (!hashDataTable.containsKey(dotStyle)) {
    		dataTable = new DataTable(Double.class, Double.class);
			hashDataTable.put(dotStyle, dataTable);
		}
    	else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	for (int i = 0; i < x.length; i++) {
    		dataTable.add(x[i],y[i]);
    		if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
				if (y[i] > dotStyle.getLineLength()) {
					dotStyle.setLineLength(y[i]);
				}
			}
		}
    }
    /**
     * add lsXY, double[0]: x  double[1]: y
     * @param x
     * @param y
     */
    public void addXY(Collection<double[]> lsXY, DotStyle dotStyle) {
    	DataTable dataTable = null;
    	if (!hashDataTable.containsKey(dotStyle)) {
    		dataTable = new DataTable(Double.class, Double.class);
			hashDataTable.put(dotStyle, dataTable);
		}
    	else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	for (double[] ds : lsXY) {
			dataTable.add(ds[0],ds[1]);
			if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
				if (ds[1] > dotStyle.getLineLength()) {
					dotStyle.setLineLength(ds[1]);
				}
			}
		}
    }
    /**
     * check the method
     * add lsX, lsY
     * @param x
     * @param y
     */
    public void addXY(Collection<? extends Number> lsX, Collection<? extends Number> lsY,DotStyle dotStyle) {
    	if (lsX.size() != lsY.size()) {
			return;
		}
    	DataTable dataTable = null;
    	if (!hashDataTable.containsKey(dotStyle)) {
    		dataTable = new DataTable(Double.class, Double.class);
			hashDataTable.put(dotStyle, dataTable);
		}
    	else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	for (Number numberX : lsX) {
			Number numberY = lsY.iterator().next();
			dataTable.add(numberX.doubleValue(), numberY.doubleValue());
			if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
				if (numberY.doubleValue() > dotStyle.getLineLength()) {
					dotStyle.setLineLength(numberY.doubleValue());
				}
			}
		}
    }
    

    /**
     *  设定坐标轴边界
     * @param x1
     * @param x2
     */
    public void setAxisX(double x1, double x2) {
    	axisX = new Axis(x1, x2);
    }
    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title, Font fontTitle)  {
    	this.title = title;
    	this.fontTitle = fontTitle;
    }
    
    /**
     * 设置标题
     * @param title
     */
    public void setTitleX(String titleX, Font fontX, double spaceX)  {
    	this.titleX = titleX;
    	this.fontX = fontX;
    	this.spaceX = spaceX;
    }
    
    /**
     * 设置标题
     * @param title
     */
    public void setTitleY(String titleY, Font fontY, double spaceY)  {
    	this.titleY = titleY;
    	this.fontY = fontY;
    	this.spaceY = spaceY;
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
    int insetsTop = 30, insetsLeft = 60, insetsBottom = 60, insetsRight = 40;
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
    /**
     * 设定图片坐标轴到图片边缘的距离
     * @param int size 
     */
    public void setInsets(int size) {
    	int insetsTop = 20, insetsLeft = 60, insetsBottom = 60, insetsRight = 40;
    	double scale = 1;
    	if (size == INSETS_SIZE_S) {
    		scale = 0.8;
    	}
    	else if (size == INSETS_SIZE_SM) {
			scale = 1;
		}
    	else if (size == INSETS_SIZE_M) {
			scale = 1.5;
		}
    	else if (size == INSETS_SIZE_ML) {
			scale = 2;
		}
    	else if (size == INSETS_SIZE_L) {
			scale = 3;
		}
    	this.insetsTop = (int) (insetsTop * scale); this.insetsLeft = (int) (insetsLeft * scale);
		this.insetsBottom = (int) (insetsBottom * scale); this.insetsRight = (int) (insetsRight * scale);
    }
    /**
     * 因为无法绘制每个点都为点到x轴的直线，会画的参差不齐，这时候就需要绘制最长的线
     * 效率还不错
     * 给定图片高度，给定最高的点的高度，返回每条线的长度
     * @param width
     * @param heigh
     * @param lineLength
     * @return
     */
    private double getLineLen(int heigh, double lineLength)
    {
    	double start = axisY.getMin().doubleValue();
    	//1.02 means the result should some what long than the result
    	return (lineLength - start) * heigh* 1.02 / axisY.getRange();
    }

//	@Override
	protected void draw(int width, int heigh) {
		int foldchange = 1;
		width = width * foldchange;
		heigh = heigh * foldchange;
		plot = new XYPlot();
		for (Entry<DotStyle, DataTable> entry : hashDataTable.entrySet()) {
			DotStyle dotStyle = entry.getKey();
			if (dotStyle.getStyle() == DotStyle.STYLE_LINE) {
				dotStyle.setLineLength(getLineLen(heigh, dotStyle.getLineLength()));
			}
			DataTable dataTable = entry.getValue();
			DataSeries dataSeries = new DataSeries(dotStyle.getGroup(), dataTable,0,1);
			plot.add(dataSeries);
			
			if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
                AreaRenderer area = new DefaultAreaRenderer2D();
//              areaUpper.setSetting(AreaRenderer.COLOR, GraphicsUtils.deriveWithAlpha(colorUpper, 64));
                area.setSetting(AreaRenderer.COLOR, dotStyle.getColor());
                plot.setAreaRenderer(dataSeries, area);
                plot.setPointRenderer(dataSeries, null);
			}
			else {
				  // Style data series
		        PointRenderer points = new DefaultPointRenderer2D();
		        points.setSetting(PointRenderer.SHAPE, dotStyle.getShape(foldchange));
		        points.setSetting(PointRenderer.COLOR, dotStyle.getColor());
			}
		}

        // Style the plot area
//        plot.getPlotArea().setSetting(PlotArea.BORDER, new BasicStroke(2f));
    	//background color
        plot.getPlotArea().setSetting(PlotArea.BACKGROUND, new Color(0, 0, 0, 0));
        // Style data series

		   // set the distance between the figure and picture edge, 设定图片坐标轴到图片边缘的距离
		plot.setInsets(new Insets2D.Double( insetsTop, insetsLeft, insetsBottom, insetsRight));
		
        plot.getAxis(XYPlot.AXIS_X).setRange(axisX.getMin(), axisX.getMax());//设置坐标轴
        plot.getAxis(XYPlot.AXIS_Y).setRange(axisY.getMin(), axisY.getMax());//设置坐标轴
        
        setAxisAndTitle();
        //坐标轴在figure最下方
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        
		int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
    	bufferedImage = new BufferedImage(width, heigh, imageType);
    	if (bg != null && !bg.equals(new Color(0,0,0,0))) {
			setBG(width, heigh);
		}
		DrawingContext context = new DrawingContext((Graphics2D) bufferedImage.getGraphics());
		plot.setBounds(0, 0, width, heigh);
		plot.draw(context);
	}
	private void setAxisAndTitle()
	{
		// Style axes
		if (titleX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, titleX);
		}
		if (titleY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, titleY);
		}
		if (title != null) {
			plot.setSetting(BarPlot.TITLE, title);
		}
		
		if (spaceX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, spaceX);//坐标轴刻度
		}
		if (spaceY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, spaceY);//坐标轴刻度
		}

		if (fontX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL_FONT, fontX);
		}
		if (fontY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_FONT, fontY);
		}
		if (fontTitle != null) {
			plot.setSetting(BarPlot.TITLE_FONT, fontTitle);
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
		graphics.fillRect(0, 0, width, heigh);//fill the left region
//		graphics.fillRect(insetsLeft, 0, width-insetsLeft, insetsTop);//fill the upon region
//		graphics.fillRect(insetsLeft, heigh-insetsBottom, width-insetsLeft, insetsBottom); //fill the bottom region
//		graphics.fillRect(width-insetsRight, insetsTop, insetsRight, heigh - insetsTop - insetsBottom); //fill the right region
//		if (fontTitle != null) {
//			graphics.fillRect(insetsLeft, insetsTop, width-insetsLeft-insetsRight, (int)(fontTitle.getSize()*1.3)); //fill the bottom region
//		}
	}
//	@Override
	protected void draw2(int width, int heigh) {
	  // Create data
    DataTable data = new DataTable(Double.class, Double.class);
    DataTable data2 = new DataTable(Double.class, Double.class);
    final int POINT_COUNT = 2;
    java.util.Random rand = new java.util.Random();
    for (int i = 0; i < POINT_COUNT; i++) {
        double x = rand.nextGaussian();
        double y1 = rand.nextGaussian() + x;
        data.add(x, y1);
    }

    for (int i = 0; i < 20; i++) {
        double x = rand.nextGaussian();
        double y1 = rand.nextGaussian() + x;
        data2.add(x, y1);
    }

    // Create series
    DataSeries series1 = new DataSeries("Series 1", data, 0, 1);
    DataSeries series2 = new DataSeries("Series 2", data2, 0, 1);
    plot = new XYPlot();
    plot.add(series1);

 
  
 
    for (Entry<DotStyle, DataTable> entry : hashDataTable.entrySet()) {
		DotStyle dotStyle = entry.getKey();
		if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
			dotStyle.setLineLength(getLineLen(heigh, dotStyle.getLineLength()));
		}
		DataTable dataTable = entry.getValue();
		DataSeries dataSeries = new DataSeries(dotStyle.getGroup(), dataTable,0,1);
		plot.add(dataSeries);
		   // Style data series
        PointRenderer points = new DefaultPointRenderer2D();
        points.setSetting(PointRenderer.SHAPE, dotStyle.getShape(5));
        points.setSetting(PointRenderer.COLOR, dotStyle.getColor());
        plot.setPointRenderer(dataSeries, points);
	}
 
    plot.getPlotArea().setSetting(
            PlotArea.COLOR, new Color(0, 0, 0, 0));
  
    int insetsTop = 30,
           insetsLeft = 60,
           insetsBottom = 60,
           insetsRight = 40;
    plot.setInsets(new Insets2D.Double(
            insetsTop, insetsLeft, insetsBottom, insetsRight));
//    plot.setBounds(1, 1, 2, 3);
//    Axis axis = new Axis(0, 2);
//    plot.setAxis(XYPlot.AXIS_X, axis);//设置坐标轴ITALIC
    plot.setSetting(BarPlot.TITLE, "Nice scatter");
//    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
//    plot.setSetting(BarPlot.TITLE_FONT, font);
    plot.setAxis(XYPlot.AXIS_X, axisX);//设置坐标轴
    plot.setAxis(XYPlot.AXIS_Y, axisY);//设置坐标轴
    plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICK_LABELS, true);//坐标轴刻度
    plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, 20.0);//坐标轴刻度
    plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS, true);//坐标轴刻度
    plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_MINOR , true);//坐标轴刻度
    plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.SHAPE_VISIBLE , true);//坐标轴刻度

    if (title != null) {
    	plot.setSetting(BarPlot.TITLE, title);
        plot.setSetting(BarPlot.TITLE_FONT, fontTitle);
	}

	
	int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
	bufferedImage = new BufferedImage(width, heigh, imageType);
	DrawingContext context = new DrawingContext((Graphics2D) bufferedImage.getGraphics());

	plot.setBounds(0, 0, width, heigh);
	plot.draw(context);

	//fill the left region
//	graphics.fillRect(0, 0, insetsLeft, 2000);//fill the left region
//	graphics.fillRect(insetsLeft, 0, 2000-insetsLeft, insetsTop);//fill the upon region
//	graphics.fillRect(insetsLeft, 2000-insetsBottom, 2000-insetsLeft, insetsBottom); //fill the bottom region
//	graphics.fillRect(2000-insetsRight, insetsTop, insetsRight, 2000 - insetsTop - insetsBottom); //fill the right region
	
	
	
//	DrawingContext context = new DrawingContext(graphics);
//	plot.setBounds(0, 0, 1000, 1000);
//	plot.draw(context);



      

    
		
	}
}
