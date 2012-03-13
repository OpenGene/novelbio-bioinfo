package com.novelbio.base.plot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map.Entry;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.util.Insets2D;

public class PlotScatter extends PlotNBC{
	HashMap<DotStyle, DataTable> hashDataTable = new HashMap<DotStyle, DataTable>();
	
	XYPlot plot;
	/**
	 * �����ĸ�ʽ�����ߵĻ����趨�ߵĳ��ȣ����������Զ��趨�������귶Χ�ڻ�ȡ
	 */
	int lineLength =100;
    /**
     * ���ӵ�
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
     * ������߽�
     */
    Axis axisX = null, axisY = null;
    /**
     *  �趨������߽�
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
     * �趨������߽�
     * @param y1
     * @param y2
     */
    public void setAxisY(double y1, double y2) {
    	axisY = new Axis(y1, y2);
    }
    
    /**
     * ͼƬ�����ᵽͼƬ��Ե�ľ���
     */
    double insetsTop = 30.0,
           insetsLeft = 60.0,
           insetsBottom = 60.0,
           insetsRight = 40.0;
    /**
     * �趨ͼƬ�����ᵽͼƬ��Ե�ľ���
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setInsets(double left, double top, double right, double bottom) {
    	this.insetsTop = top; this.insetsLeft = left;
    	this.insetsBottom = bottom; this.insetsRight = right;
    }
	@Override
	protected void draw(int width, int heigh) {
		plot = new XYPlot();
		//�趨ͼƬ�����ᵽͼƬ��Ե�ľ���
		plot.setInsets(new Insets2D.Double( insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.setAxis(XYPlot.AXIS_X, axisX);//����������
        plot.setAxis(XYPlot.AXIS_Y, axisY);//����������
        
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
		
		int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
    	bufferedImage = new BufferedImage(width, heigh, imageType);
    	
		DrawingContext context =
			new DrawingContext((Graphics2D) bufferedImage.getGraphics());
		plot.draw(context);
	}
	
	public void setCondition() {
		
	}
}
