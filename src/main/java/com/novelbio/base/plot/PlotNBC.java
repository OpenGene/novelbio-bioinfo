package com.novelbio.base.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

import de.erichseifert.gral.graphics.DrawingContext;

public abstract  class PlotNBC{

	Logger logger = Logger.getLogger(PlotNBC.class);
	/**
	 *	最后生成的bufferedImage
	 */
	protected BufferedImage bufferedImage;
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}
	protected Color bg = new Color(255, 255, 255, 0);
	protected Color fg = Color.black;
    public void setFg(Color fg) {
		this.fg = fg;
	}
    public void setBg(Color bg) {
		this.bg = bg;
	}
    public Color getFg() {
		return fg;
	}
    public Color getBg() {
		return bg;
	}
	boolean painted = false;
    /**
     * 画图，必须调用了该方法后才能保存图片
     */
    public void drawData(int width, int heigh)
    {
    	painted = true;
    	draw(width,heigh);
    }
	/**
	 * 透明度
	 */
	protected boolean alpha = true;
	/**
	 * 设定是否透明，默认透明
	 * @param alpha
	 */
	public void setAlpha(boolean alpha) {
		this.alpha = alpha;
	}
	/**
	 * 将bufferedImage填充完毕
	 */
    protected abstract void draw( int width, int heigh);
    

    /**
     * 默认保存为jpg格式
	 * Generates a new chart <code>Image</code> based upon the currently held 
	 * settings and then attempts to save that image to disk, to the location 
	 * provided as a File parameter. The image type of the saved file will 
	 * equal the extension of the filename provided, so it is essential that a 
	 * suitable extension be included on the file name.
	 * 
	 * <p>
	 * All supported <code>ImageIO</code> file types are supported, including 
	 * PNG, JPG and GIF.
	 * <p>
	 * No chart will be generated until this or the related 
	 * <code>getChartImage()</code> method are called. All successive calls 
	 * will result in the generation of a new chart image, no caching is used.
     * @param outputFileName the file location that the generated image file should 
	 * be written to. The File must have a suitable filename, with an extension
	 * of a valid image format (as supported by <code>ImageIO</code>).
     * @param Width
     * @param Height
     * @param transpreat 是否透明
     * @throws IOException if the output file's filename has no extension or 
	 * if there the file is unable to written to. Reasons for this include a 
	 * non-existant file location (check with the File exists() method on the 
	 * parent directory), or the permissions of the write location may be 
	 * incorrect.
	 */
	public void saveToFile(String outputFileName, int Width, int Height) {
		if (!painted) {
			drawData(Width, Height);
		}
		String filename = FileOperate.getFileNameSep(outputFileName)[1];
		if (filename.equals("")) {
			outputFileName = FileOperate.changeFileSuffix(outputFileName, null, ".jpg");
		}
    	
    	BufferedImage bufferedImageResult = paintGraphicOut( Width, Height);
    	saveGraphic(bufferedImageResult, outputFileName, 1.0f);
	}
    
    private void saveGraphic(BufferedImage chart, String outputFile, float quality) {
    	String ext = FileOperate.getFileNameSep(outputFile)[1];
    	File fileOut = new File(outputFile);
		// Handle jpg without transparency.
		if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
			// Setup correct compression for jpeg.
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = (ImageWriter) iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(quality);
			try {
				// Output the image.
				FileImageOutputStream output = new FileImageOutputStream(fileOut);
				writer.setOutput(output);
				IIOImage image = new IIOImage(chart, null, null);
				writer.write(null, image, iwp);
			} catch (Exception e) {
			}
			writer.dispose();
		} else {
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
				Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/png");
				while (writers.hasNext()) {
					ImageWriter writer = writers.next();
					ImageOutputStream ios = ImageIO.createImageOutputStream(fileOutputStream);
					writer.setOutput(ios);
					try {
						writer.write(chart);
					} finally {
						ios.close();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    /**
     * 最后保存在Graphics g中所对应的另一个BufferedImage中
     * 如果保存的图案的大小和显示图案大小一致，则赋值：bufferedImageResult = bufferedImage;
     * @param bufferedImage
     * @param g
     * @param width
     * @param height
     */
    private BufferedImage paintGraphicOut( int Width, int Height)
    {
    	if (bufferedImage.getWidth() == Width && bufferedImage.getHeight() == Height) {
			return bufferedImage;
		}
    	BufferedImage bufferedImageResult = GraphicCope.resizeImage(bufferedImage, Width, Height);
    	Graphics2D graphics2d = (Graphics2D) bufferedImageResult.createGraphics();
    	// ----------   增加下面的代码使得背景透明   -----------------  
    	if (alpha) {
    		bufferedImageResult = graphics2d.getDeviceConfiguration().createCompatibleImage(bufferedImageResult.getWidth(), bufferedImageResult.getHeight(), Transparency.TRANSLUCENT);  
    		graphics2d.dispose();
    		graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
    		graphics2d = (Graphics2D) bufferedImageResult.createGraphics();  
//       	graphics2d.setColor(new Color(255,0,0));  
//       	graphics2d.setStroke(new BasicStroke(1));  
//       	graphics2d.setColor(Color.white);  
		}
    	 graphics2d.setColor(fg);
    	 graphics2d.drawImage(bufferedImageResult,  0, 0, null);
    	 return bufferedImageResult;
    }


}
