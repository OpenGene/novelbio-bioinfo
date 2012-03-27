package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.graphics.Layout;
import de.erichseifert.gral.graphics.StackedLayout;
import de.erichseifert.gral.io.plots.BitmapWriter;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.Orientation;

public class Styling extends JFrame {
    public Styling() throws Exception {
    	 // Create data
        DataTable data = new DataTable(Double.class, Double.class, Double.class);

        final int POINT_COUNT = 10;
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = rand.nextGaussian();
            double y1 = rand.nextGaussian() + x;
            double y2 = rand.nextGaussian() - x;
            data.add(x, y1, y2);
        }

        // Create series
        DataSeries series1 = new DataSeries("Series 1", data, 0, 1);
        DataSeries series2 = new DataSeries("Series 2", data, 0, 2);
        XYPlot plot = new XYPlot(series1);
        XYPlot plot2 = new XYPlot(series2);
        // Style the plot
        double insetsTop = 20.0,
               insetsLeft = 60.0,
               insetsBottom = 60.0,
               insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.setSetting(BarPlot.TITLE, "Nice scatter");

        // Style the plot area
        plot.getPlotArea().setSetting(
                PlotArea.COLOR, new Color(0.0f, 0.3f, 1.0f));
        plot.getPlotArea().setSetting(
                PlotArea.BORDER, new BasicStroke(2f));

        // Style data series
        PointRenderer points1 = new DefaultPointRenderer2D();
//        points1.setSetting(PointRenderer.SHAPE, new Rectangle.Double(0, 0, 100, 100));
//        points1.setSetting(PointRenderer.ERROR_DISPLAYED, false);
        points1.setSetting(PointRenderer.COLOR, Color.blue);
//        plot.setPointRenderer(series1, points1);

        PointRenderer points2 = new DefaultPointRenderer2D();
        points2.setSetting(PointRenderer.SHAPE, new Rectangle(10, 10));
        points2.setSetting(PointRenderer.COLOR, new Color(0.0f, 0.0f, 0.0f, 0.3f));
        plot2.setPointRenderer(series2, points2);
        plot2.getAxis(XYPlot.AXIS_X).setRange(-10, 10);
//        plot.setAxis(new Axis(-8, 8));
        // Style axes
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, "X");
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, "Y");
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, 1.0);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, 1.0);
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.add(plot2);
        // Display on screen
        getContentPane().add(new InteractivePanel(plot), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(getContentPane().getMinimumSize());
        setSize(504, 327);

        plot2.getPointRenderer(series2).setSetting(PointRenderer.VALUE_DISPLAYED,
                true);

		BufferedImage image = new BufferedImage(2000, 2000, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		//fill the left region
//		graphics.fillRect(0, 0, insetsLeft, 2000);//fill the left region
//		graphics.fillRect(insetsLeft, 0, 2000-insetsLeft, insetsTop);//fill the upon region
//		graphics.fillRect(insetsLeft, 2000-insetsBottom, 2000-insetsLeft, insetsBottom); //fill the bottom region
//		graphics.fillRect(2000-insetsRight, insetsTop, insetsRight, 2000 - insetsTop - insetsBottom); //fill the right region
		
		File file = new File("/home/zong0jie/桌面/sfsefe.png");
		File file2 = new File("/home/zong0jie/桌面/sfsefe2.png");
		
		
		DrawingContext context = new DrawingContext(graphics);
		plot.setBounds(0, 0, 1000, 1000);
		plot.draw(context);
		ImageIO.write(image, "image/png", file2);
		
		
		FileOutputStream fileOutputStream = new FileOutputStream(file);
//		ImageIO.write(image, "png", file2);
		Iterator<ImageWriter> writers =
				ImageIO.getImageWritersByMIMEType("image/png");
			while (writers.hasNext()) {
				ImageWriter writer = writers.next();
				ImageOutputStream ios =
					ImageIO.createImageOutputStream(fileOutputStream);
				writer.setOutput(ios);
//				Rectangle2D boundsOld = plot.getBounds();
				try {
					
//					plot.draw(context);
					writer.write(image);
					
				} finally {
//					plot.setBounds(boundsOld);
					ios.close();
				}
		
			}
 
    }

    public static void main(String[] args) throws Exception {
    	long start=System.currentTimeMillis(); //获取最初时间
        Styling df = new Styling();
        df.setVisible(true);
        long end=System.currentTimeMillis(); //获取运行结束时间
        System.out.println("程序运行时间： "+(end-start)+"ms"); 
    }
}
