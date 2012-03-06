package com.novelbio.base.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

public class PlotJpanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2731809645384427146L;
	Logger logger = Logger.getLogger(PlotJpanel.class);
	
	PlotNBC plotNBC;
	
	public PlotJpanel() {
		this.setDoubleBuffered(true);
	}
	
	/**
	 * 设定待画的图形
	 * @param plotNBC
	 */
	public void setPlotNBC(PlotNBC plotNBC) {
		this.plotNBC = plotNBC;
	}
	
	boolean painted = false;
 
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setOpaque(true);
        g.setColor(plotNBC.getBg());
        plotNBC.drawData(this.getWidth(), this.getHeight());
        g.drawImage(plotNBC.getBufferedImage(), 0, 0, this);
//        g.finalize();
      }
}
