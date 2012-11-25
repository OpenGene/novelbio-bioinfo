package com.novelbio.base.plot;

import java.util.HashMap;
import java.util.List;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BoxPlot;
import de.erichseifert.gral.plots.BoxPlot.BoxWhiskerRenderer;
/**
 * boxplot��ͳ��ͼ
 * @author zong0jie
 *
 */
public class PlotBox extends PlotScatter {
	   /**
     * using data to plot the Bar figure, ֱ�Ӽ���plot��������hash��
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void setBoxPlot(List<BoxInfo> lsBoxInfo, BoxStyle boxStyle) {
		DataTable stats = new DataTable(Integer.class, Double.class,
			Double.class, Double.class, Double.class, Double.class);
		HashMap<Double, String> mapInfo2Name = new HashMap<Double, String>();
		// Generate statistical values for each column
		for (int c = 0; c < lsBoxInfo.size(); c++) {
			BoxInfo boxInfo = lsBoxInfo.get(c);
			stats.add(
				c + 1,
				boxInfo.info50,
				boxInfo.info1,
				boxInfo.info25,
				boxInfo.info75,
				boxInfo.info99
			);
			mapInfo2Name.put((double) (c + 1), boxInfo.getBoxName());
		}
		plot = new BoxPlot(stats);
		setRender(stats, boxStyle);
		setAxisTicksXMap(mapInfo2Name);
    }
    
    private void setRender(DataSource boxData, BoxStyle boxStyle) {
    	BoxPlot plot = (BoxPlot)this.plot;
    	plot.getPointRenderer(boxData).setSetting(
				BoxWhiskerRenderer.WHISKER_STROKE, boxStyle.getBasicStroke());
		plot.getPointRenderer(boxData).setSetting(
				BoxWhiskerRenderer.BOX_BORDER, boxStyle.getBasicStroke());
		plot.getPointRenderer(boxData).setSetting(
				BoxWhiskerRenderer.BOX_BACKGROUND, boxStyle.getColor());
		plot.getPointRenderer(boxData).setSetting(BoxWhiskerRenderer.BOX_COLOR, boxStyle.getColorBoxEdge());
		plot.getPointRenderer(boxData).setSetting(
				BoxWhiskerRenderer.WHISKER_COLOR, boxStyle.getColorBoxWhisker());
		plot.getPointRenderer(boxData).setSetting(
				BoxWhiskerRenderer.BAR_CENTER_COLOR, boxStyle.getColorBoxCenter());
    }
    
    
    public static class BoxInfo {
    	double info99;
    	double info95;
    	double info75;
    	double info50;
    	double info25;
    	double info5;
    	double info1;
    	
    	String boxName = "";
    	public BoxInfo(String boxName) {
    		this.boxName = boxName;
    	}
    	
    	public String getBoxName() {
			return boxName;
		}
    	public void setInfoMedian(double info50) {
    		this.info50 = info50;
    	}
    	
    	public void setInfo25And75(double info25, double info75) {
    		this.info25 = info25;
    		this.info75 = info75;
    	}
    	
    	public void setInfoMinAndMax(double info1, double info99) {
			this.info1 = info1;
			this.info99 = info99;
		}
    }
}
