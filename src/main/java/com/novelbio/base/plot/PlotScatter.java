package com.novelbio.base.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.base.dataStructure.Equations;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.util.Insets2D;

public class PlotScatter extends PlotNBCInteractive{
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
     * custom axis X's ticks
     */
    Map<Double, String> mapAxisX = null;
    Font fontTicksX = null;
    /**
     * custom axis Y's ticks
     */
    Map<Double, String> mapAxisY = null;
    Font fontTicksY = null;
    /**
     * 坐标轴边界
     */
    Axis axisX = null, axisY = null;
    
    ArrayList<String> lsAxisNotMove = new ArrayList<String>();
    /**
     * set which axis is not move when moving or zooming
     * @param AxisNotMove null will clean all the settings
     */
    public void setAxisNotMove(String... AxisNotMove) {
    	if (AxisNotMove == null || AxisNotMove.length == 0) {
			lsAxisNotMove.clear();
		}
    	for (String string : AxisNotMove) {
    		this.lsAxisNotMove.add(string);
    	}
	}
    
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
     * @param title main title
     * @param fontTitle font of the title
     * @param title
     */
    public void setTitle(String title, Font fontTitle)  {
    	if (title != null)
    		this.title = title;
    	if (fontTitle != null)
    		this.fontTitle =fontTitle;
    }
    
    /**
     * 
     * 设置标题
     * @param titleX tile on axis x
     * @param fontX font of the title
     * @param spaceX ticks interval, 0 means not set the space
     */
    public void setTitleX(String titleX, Font fontX, double spaceX)  {
    	if (titleX != null)
    		this.titleX = titleX;
    	if (fontX != null)
    		this.fontX = fontX;
    	if (spaceX != 0)
    		this.spaceX = spaceX;
    }
    
    /**
     * 设置标题
     * @param titleY tile on axis y
     * @param fontY font of the title
     * @param spaceY ticks interval, 0 means not set the space
     */
    public void setTitleY(String titleY, Font fontY, double spaceY)  {
    	if (titleY != null)
    		this.titleY = titleY;
    	if (fontY != null)
    		this.fontY = fontY;
    	if (spaceY != 0)
    		this.spaceY = spaceY;
    }
    
    public void setAxisTicksX(Map<Double, String> mapTicks, Font fontTicks) {
    	if (mapTicks != null) {
    		this.mapAxisX = mapTicks;
		}
		if (fontTicks != null) {
			this.fontTicksX = fontTicks;
		}
		
	}
    public void setAxisTicksY(Map<Double, String> mapTicks, Font fontTicks) {
    	if (mapTicks != null) {
    		this.mapAxisY = mapTicks;
		}
		if (fontTicks != null) {
			this.fontTicksY = fontTicks;
		}
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
     * set the marge size of a figure, the bigger the marge be, the font of the tile will also bigger
     * @param int size 
     */
    public void setInsets(int size) {
    	int insetsTop = 20, insetsLeft = 60, insetsBottom = 60, insetsRight = 40;
    	double scaleInsets = 1; double scaleFont = 1;
    	if (size == INSETS_SIZE_S) {
    		scaleInsets = 0.8;
    		scaleFont = 0.6;
    	}
    	else if (size == INSETS_SIZE_SM) {
    		scaleInsets = 1;
    		scaleFont = 0.8;
		}
    	else if (size == INSETS_SIZE_M) {
    		scaleInsets = 1.5;
    		scaleFont = 1.2;
		}
    	else if (size == INSETS_SIZE_ML) {
    		scaleInsets = 2;
    		scaleFont = 1.5;
		}
    	else if (size == INSETS_SIZE_L) {
    		scaleInsets = 3;
    		scaleFont = 2;
		}
    	this.insetsTop = (int) (insetsTop * scaleInsets); this.insetsLeft = (int) (insetsLeft * scaleInsets);
		this.insetsBottom = (int) (insetsBottom * scaleInsets); this.insetsRight = (int) (insetsRight * scaleInsets);		
    	this.fontX = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(20*scaleFont));
    	this.fontY = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(20*scaleFont));
		this.fontTicksX = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(15*scaleFont));
		this.fontTicksY = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(15*scaleFont));
		this.fontTitle =  new Font(Font.SANS_SERIF, Font.PLAIN, (int)(25*scaleFont));
		
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
		drawPlot(width, heigh);
		toImage(width, heigh);
	}
	/**
	 * needs check
	 * @return
	 */
	public Drawable getPlot() {
		drawPlot(10, 10);
		return plot;
	}
	/**
	 * @param width
	 * @param heigh
	 */
	protected void drawPlot(int width, int heigh) {
		int foldchange = 1;
		width = width * foldchange;
		heigh = heigh * foldchange;
		boolean plotFirst = true;
		for (Entry<DotStyle, DataTable> entry : hashDataTable.entrySet()) {
			DotStyle dotStyle = entry.getKey();
			if (dotStyle.getStyle() == DotStyle.STYLE_LINE) {
				dotStyle.setLineLength(getLineLen(heigh, dotStyle.getLineLength()));
			}
			DataTable dataTable = entry.getValue();
			DataSeries dataSeries = new DataSeries(dotStyle.getGroup(), dataTable,0,1);
			if (plot == null) {
				plot = new XYPlot(dataSeries);
				plotFirst = false;
			}
			else if (plotFirst) {
				plot.clear();
				plot.add(dataSeries);
			}
			else {
				plot.add(dataSeries);
			}
			
			
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
    
		// set the distance between the figure and picture edge, 设定图片坐标轴到图片边缘的距离
		plot.setInsets(new Insets2D.Double( insetsTop, insetsLeft, insetsBottom, insetsRight));
		
        plot.getAxis(XYPlot.AXIS_X).setRange(axisX.getMin(), axisX.getMax());//设置坐标轴
        plot.getAxis(XYPlot.AXIS_Y).setRange(axisY.getMin(), axisY.getMax());//设置坐标轴
        
        setAxisAndTitle();
        //坐标轴在figure最下方
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
	}
	
	
	protected void toImage(int width, int heigh) {
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
		if (mapAxisX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_CUSTOM, mapAxisX);//坐标轴刻度
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, (axisX.getMax().doubleValue() - axisX.getMin().doubleValue())*2);//坐标轴刻度
		}
		if (mapAxisY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_CUSTOM, mapAxisY);//坐标轴刻度
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, (axisY.getMax().doubleValue() - axisY.getMin().doubleValue())*2);//坐标轴刻度

		}
		
		if (fontTicksX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_FONT, fontTicksX);//坐标轴刻度
		}
		if (fontTicksY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_FONT, fontTicksY);//坐标轴刻度
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
		
		//background color
		if (!plotareaAll) {
			 plot.getPlotArea().setSetting(PlotArea.BACKGROUND, bg);
		}
		else {
			 plot.getPlotArea().setSetting(PlotArea.BACKGROUND, new Color(0, 0, 0, 0));
		}
		
		if (lsAxisNotMove.size() == 0) {
			plot.setAxisNotMove(null);
			plot.setAxisNotZoom(null);
		}
		else {
			for (String string : lsAxisNotMove) {
				plot.setAxisNotMove(string);
				plot.setAxisNotZoom(string);
			}
		}
	}
	/**
	 * fill the insets with BG color, means fill the marginal area between the frame and the plot border.<br>
	 * because gral will not color the marginal area, so we should color the marginal area using this method
	 * @param width
	 * @param heigh
	 */
	private void setBG(int width, int heigh) {
		if (plotareaAll) {
			Graphics2D graphics = bufferedImage.createGraphics();
			graphics.setColor(bg);
			graphics.fillRect(0, 0, width, heigh);
		}
	}
	/**
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	public void  mapNum2ChangeX(double startTick, double startResult, double endTick, double endResult, double intervalNumResult) {
		mapAxisX = mapNum2Change(startTick, startResult, endTick, endResult, intervalNumResult);
	}
	/**
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	public void  mapNum2ChangeY(double startTick, double startResult, double endTick, double endResult, double intervalNumResult) {
		mapAxisY = mapNum2Change(startTick, startResult, endTick, endResult, intervalNumResult);
	}
	/**
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	private Map<Double, String> mapNum2Change(double startTick, double startResult, double endTick, double endResult, double intervalNumResult)
	{
		HashMap<Double, String> mapAxis = new HashMap<Double, String>();
		Equations equations = new Equations();
		ArrayList<double[]> lsXY = new ArrayList<double[]>();
		lsXY.add(new double[]{startResult, startTick});
		lsXY.add(new double[]{endResult, endTick});
		equations.setXY(lsXY);
		boolean decimals = true;//whether the axis ticks have dot, means have decimals
		if (intervalNumResult >= 1 || intervalNumResult <= -1) {
			decimals = false;
		}
		for (double i = startResult; i < endResult; i = i + intervalNumResult) {
			double tick = equations.getY(i);
			String tmpResult = i + "";
			if (!decimals) {
				tmpResult = (int)i + "";
			}
			mapAxis.put(tick, tmpResult);
		}
		return mapAxis;
	}
	
}
