package baobao;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.novelbio.listOperate.HistList;

public class HistogramEqualization {
	
	/**
	 * Main method
	 * 
	 * If the file is not found, print "file not found" to the command line, otherwise print nothing
	 * 
	 * The equalized image should be printed to a file that has '-equalized' after the file name and before the extension.
	 * The equalized image should have the same extension and encoding as the source.
	 * 
	 * @param args - program arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		BufferedImage src_biao = ImageIO.read(new File("/home/zong0jie/桌面/800px-Unequalized_Hawkes_Bay_NZ.jpg"));
		BufferedImage result = equalizeImage(src_biao);
		ImageIO.write(result, "png", new File("/home/zong0jie/桌面/800px-Unequalized_Hawkes_Bay_NZ_Norm.png"));
	}

	
	
	/**
	 * Method to equalize an image
	 * 
	 * @param source - the original image
	 * @return the equalized image
	 * @return null - the input is null
	 */
	protected static BufferedImage equalizeImage(BufferedImage source){
		HistogramEqualization histogramEqualization = new HistogramEqualization();
		return histogramEqualization.norm(source);
	}
	
	private BufferedImage norm(BufferedImage source) {
		HistList histGreen = createHisColor("green");
		HistList histBlue = createHisColor("blue");
		HistList histRed = createHisColor("red");

		for (int i = 0; i < source.getWidth(); i++) {
			for (int j = 0; j < source.getHeight(); j++) {
				int[] rgb = decompressRGB(source.getRGB(i, j));
				histRed.addNum(rgb[0]);
				histGreen.addNum(rgb[1]);
				histBlue.addNum(rgb[2]);
			}
		}
		
		BufferedImage bufferedImageResult = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		
		for (int i = 0; i < source.getWidth(); i++) {
			for (int j = 0; j < source.getHeight(); j++) {
				int[] rgb = decompressRGB(source.getRGB(i, j));
				int result = getRGBnorm(rgb, histRed, histGreen, histBlue);
				bufferedImageResult.setRGB(i, j, result);
			}
		}
		
		return bufferedImageResult;
	}
	
	
	
	
	private HistList createHisColor(String colorName) {
		HistList histList = HistList.creatHistList(colorName, true);
		histList.setBinAndInterval(256, 1, 255);
		return histList;
	}
	
	
	private int getRGBnorm(int[] rgb, HistList histR, HistList histG, HistList histB) {
		int r = getNormValue(rgb[0], histR);
		int g = getNormValue(rgb[1], histG);
		int b = getNormValue(rgb[2], histB);
		return compressRGB(r, g, b);
	}
	
	private int getNormValue(int num, HistList histList) {
		double[] values = histList.getIntegral(num, true);
		return (int)(values[1] * 255);
	}
	
	
	
	/**
	 * Decompress the single RGB value into the separate red, green and blue
	 * channels
	 * 
	 * @param rgb - the single rgb value
	 * @return array(red, green, blue) - the channels in an array (position 0 for red, position 1 for green, position 2 for blue)
	 */
	protected static int[] decompressRGB(int rgb){
		Color color = new Color(rgb);
		return new int[]{color.getRed(), color.getGreen(), color.getBlue()};
	}
	
	/**
	 * Compress the separate RGB values into one value that will be stored
	 * in the image
	 * 
	 * @param red - the red channel value
	 * @param green - the green channel value
	 * @param blue - the blue channel value
	 * @return rgb - the compressed value
	 */
	protected static int compressRGB(int red, int green, int blue){
		Color color = new Color(red, green, blue);
		return color.getRGB();
	}
	
}
