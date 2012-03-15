package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
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
    public Styling() {
        // Create data
        DataTable data = new DataTable(Double.class, Double.class, Double.class);
        DataTable data2 = new DataTable(Double.class, Double.class);
        final int POINT_COUNT = 1000;
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < POINT_COUNT; i++) {
            double x = rand.nextGaussian();
            double y1 = rand.nextGaussian() + x;
            double y2 = rand.nextGaussian() - x;
            data.add(x, y1, y2);
        }

        for (int i = 0; i < 20; i++) {
            double x = rand.nextGaussian();
            double y1 = rand.nextGaussian() + x;
            data2.add(x, y1);
        }

        // Create series
        DataSeries series1 = new DataSeries("Series 1", data, 0, 1);
        DataSeries series2 = new DataSeries("Series 2", data2, 0, 1);
        XYPlot plot = new XYPlot(series1);
      
        // Style the plot
        int insetsTop = 30,
               insetsLeft = 60,
               insetsBottom = 60,
               insetsRight = 40;
        plot.setInsets(new Insets2D.Double(
                insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.setBounds(1000, 1000, 2, 3);
        Axis axis = new Axis(0, 2);
        plot.setAxis(XYPlot.AXIS_X, axis);//设置坐标轴
        plot.setSetting(BarPlot.TITLE, "Nice scatter");
        // Style the plot area
//        plot.getPlotArea().setSetting(
//                PlotArea.COLOR, new Color(0.0f, 0.3f, 1.0f));
//        plot.getPlotArea().setSetting(PlotArea.BORDER, new BasicStroke(2f));
        plot.getPlotArea().setSetting(
                PlotArea.COLOR, Color.BLUE);
        plot.getPlotArea().setSetting(PlotArea.BORDER, new BasicStroke(2f));
        plot.getPlotArea().setSetting(
                PlotArea.BACKGROUND, new Color(255, 255, 255, 255));
        // Style data series
        PointRenderer points1 = new DefaultPointRenderer2D();
        points1.setSetting(PointRenderer.SHAPE, new Ellipse2D.Double(-0.0, -0.0, 2.0, 1006.0));
        points1.setSetting(PointRenderer.COLOR, new Color(0.0f, 0.3f, 1.0f, 0.3f));
        plot.setPointRenderer(series1, points1);
        plot.add(series2);
        PointRenderer points2 = new DefaultPointRenderer2D();
        Shape circle = new Rectangle2D.Double(-2.5, -2.5, 5, 5);
        points2.setSetting(PointRenderer.SHAPE, circle);
        points2.setSetting(PointRenderer.COLOR, new Color(0.0f, 0.0f, 0.0f, 0.3f));
        plot.setPointRenderer(series2, points2);
        // Style axes
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, "X");
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, "Y");
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, 1.0);//坐标轴刻度
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, 2.0);//坐标轴刻度
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        DrawableWriter drawableWriter = DrawableWriterFactory.getInstance().get("image/png");
        

		BufferedImage image = new BufferedImage(2000, 2000, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = image.createGraphics();
		//fill the left region
//		graphics.fillRect(0, 0, insetsLeft, 2000);//fill the left region
//		graphics.fillRect(insetsLeft, 0, 2000-insetsLeft, insetsTop);//fill the upon region
//		graphics.fillRect(insetsLeft, 2000-insetsBottom, 2000-insetsLeft, insetsBottom); //fill the bottom region
//		graphics.fillRect(2000-insetsRight, insetsTop, insetsRight, 2000 - insetsTop - insetsBottom); //fill the right region
		
		DrawingContext context = new DrawingContext(graphics);
		plot.draw(context);
		Graphics2D graphics2 = image.createGraphics();
		graphics2.setColor(Color.black);
		graphics2.fillRect(0, 0, 2000, 2000);
		File file = new File("/home/zong0jie/桌面/sfsefe.png");
        try {
			drawableWriter.write(plot, new FileOutputStream(file), 2000, 2000);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        JPanel jPanel = new JPanel();
        // Display on screen
        getContentPane().add(new InteractivePanel(plot), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(getContentPane().getMinimumSize());
        setSize(504, 327);
    }

    public static void main(String[] args) {
        Styling df = new Styling();
        df.setVisible(true);
    }
}
