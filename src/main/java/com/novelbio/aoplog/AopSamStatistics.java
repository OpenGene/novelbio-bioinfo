package com.novelbio.aoplog;

import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fasta.ChrFoldHash.CompareChrID;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.PlotBar;
import com.novelbio.nbcgui.controlseq.CtrlSamPPKMint;

public class AopSamStatistics {
	private static final Logger logger = Logger.getLogger(AopDNAMapping.class);
	/** 超过50条染色体就不画这个图了 */
	private static int chrNumMax = 50;
	@After("execution (* com.novelbio.nbcgui.controlseq.CtrlSamPPKMint.aop(..)) && target(ctrlSamPPKMint)")
	public void plotPic(CtrlSamPPKMint ctrlSamPPKMint) {
		Map<String, SamFileStatistics> mapPrefix2SamFileStatistics = ctrlSamPPKMint.getMapPrefix2Statistics();
		for (String prefix : mapPrefix2SamFileStatistics.keySet()) {
			SamFileStatistics samFileStatistics = mapPrefix2SamFileStatistics.get(prefix);
			aopSamFileStatistics(prefix, ctrlSamPPKMint, samFileStatistics);
		}
	}
	
	private void aopSamFileStatistics(String prefix, CtrlSamPPKMint ctrlSamPPKMint, SamFileStatistics samFileStatistics) {
		SamStatisticsBuilder samStatisticsBuilder = new SamStatisticsBuilder(chrNumMax);
		samStatisticsBuilder.setSamFileStatistics(ctrlSamPPKMint.getResultPrefix() +prefix, ctrlSamPPKMint.getMapChrID2MappedNumber(), samFileStatistics);
		samStatisticsBuilder.writeInfo();
	}

}


/**
 * mapping报告参数生成器
 * @author novelbio
 */
class SamStatisticsBuilder extends ReportBuilder {
	private static Logger logger = Logger.getLogger(SamStatisticsBuilder.class);
	private final Color barColor1 = new Color(23, 200, 200); 
	private final Color barColor2 = new Color(100, 100, 100); 
	/**  */
	String fileName;
	/** 拦截到的对象 */
	private SamFileStatistics samFileStatistics;
	/** sam文件的全路径，不包含后缀 */
	private String pathAndName;

	private List<String[]> lsReport = new ArrayList<String[]>();
	
	/** 超过这个数据就不画图 */
	int chrNumMax = 50;
	
	/**
	 * reads的分布情况统计
	 * key: chrID<br>
	 * value: double[4] 0: readsNum 1: readsProp 2: chrLen 3: chrProp
	 */
	private Map<String, double[]> mapChrID2LenProp = new LinkedHashMap<String, double[]>();
	
	public SamStatisticsBuilder(int chrNumMax) {
		this.chrNumMax = chrNumMax;
	}
	public void setSamFileStatistics(String fileName, Map<String, Long> mapChrID2Length, SamFileStatistics samFileStatistics) {
		this.samFileStatistics = samFileStatistics;
		this.fileName = fileName;
		setParamPath(fileName);
		setMapChrID2PropAndLen(samFileStatistics.getMapChrID2MappedNumber(), mapChrID2Length);
		pathAndName = FileOperate.getPathName(fileName) + FileOperate.getFileNameSep(fileName)[0];
	}
	
	public void setSamFileStatistics(SamFile samFile, SamFileStatistics samFileStatistics) {
		this.samFileStatistics = samFileStatistics;
		this.fileName = samFile.getFileName();
		setParamPath(fileName);
		setMapChrID2PropAndLen(samFileStatistics.getMapChrID2MappedNumber(), samFile.getMapChrIDLowcase2Length());
		pathAndName = FileOperate.getParentPathName(samFile.getFileName()) + FileOperate.getFileNameSep(samFile.getFileName())[0];
	}
	
	/**
	 * 返回reads的分布情况统计
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 * key: chrID<br>
	 * value: double[4] 0: readsNum 1: readsProp 2: chrLen 3: chrProp
	 */
	private void setMapChrID2PropAndLen(Map<String, Long> resultData, Map<String, Long> standardData) {
		mapChrID2LenProp = new LinkedHashMap<String, double[]>();
		long readsNumAll = 0, chrLenAll = 0;
		List<String> lsChrID = new ArrayList<String>(standardData.keySet());
		Collections.sort(lsChrID, new CompareChrID());
		for (String chrID : lsChrID) {
			if (resultData.containsKey(chrID)) {
				readsNumAll += resultData.get(chrID);
			}
			chrLenAll += standardData.get(chrID);
		}
		
		for (String key : lsChrID) {
			double[] data = new double[4];
			data[0] = resultData.get(key) ==null? 0 : resultData.get(key);
			data[1] =  (double)data[0]/readsNumAll;
			data[2] = standardData.get(key);
			data[3] = (double)data[2]/chrLenAll;
			mapChrID2LenProp.put(key, data);
		}
	}


	@Override
	protected boolean fillDescFile() {
		return true;
	}
	
	@Override
	protected boolean buildExcels() {
		if (mapChrID2LenProp == null || mapChrID2LenProp.size() == 0) {
			return true;
		}
		boolean samReprot = writeSamReport();
		boolean chrReport = true;
		if (mapChrID2LenProp.size() <= chrNumMax) {
			chrReport = writeChrInfo();
		}
		return samReprot && chrReport;
	}

	/** 写junction reads等统计数据 */
	private boolean writeSamReport() {
		try {
			lsReport.add(new String[] { "Statistics Term", "Result(" + FileOperate.getFileNameSep(fileName)[0] + ")" });
			long allReads = samFileStatistics.getReadsNum(MappingReadsType.allReads);
			long unMapped = samFileStatistics.getReadsNum(MappingReadsType.unMapped);
			long allMappedReads = samFileStatistics.getReadsNum(MappingReadsType.allMappedReads);
			long uniqueMapping = samFileStatistics.getReadsNum(MappingReadsType.uniqueMapping);
			long repeatMapping = samFileStatistics.getReadsNum(MappingReadsType.repeatMapping);
			long junctionAllMappedReads = samFileStatistics.getReadsNum(MappingReadsType.junctionAllMappedReads);
			long junctionUniqueMapping = samFileStatistics.getReadsNum(MappingReadsType.junctionUniqueMapping);

			lsReport.add(new String[] { "allReads", allReads + "" });
			lsReport.add(new String[] { "unMapped", unMapped + "" });

			lsReport.add(new String[] { "allMappedReads", allMappedReads + "" });
			if (!(allMappedReads == repeatMapping && repeatMapping < 1)) {
				lsReport.add(new String[] { "uniqueMapping", uniqueMapping + "" });
				lsReport.add(new String[] { "repeatMapping", repeatMapping + "" });
			}
			if (junctionAllMappedReads != 0 && junctionUniqueMapping != 0) {
				lsReport.add(new String[] { "junctionAllMappedReads", junctionAllMappedReads + "" });
				lsReport.add(new String[] { "junctionUniqueMapping", junctionUniqueMapping + "" });
			}
			
			String pathMapReport ="";
			if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
				pathMapReport = pathAndName + "MappingReport.xls";
			} else {
				pathMapReport = FileOperate.changeFilePrefix(pathAndName, "MappingReport_", "xls");
			}
					
			addParamInfo(Param.excelParam,  FileOperate.getFileName(pathMapReport));
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(pathMapReport, true);
			txtReadandWrite.ExcelWrite(lsReport);
			txtReadandWrite.close();
		} catch (Exception e) {
			logger.error("aopRNAMapping生成表格出错啦！");
			return false;
		}
		return true;
	}
	
	/** 写每条染色体上 reads的覆盖度等数据 */
	private boolean writeChrInfo() {
		try {
			String pathChrReport = "";
			if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
				pathChrReport = pathAndName + "ChrDistribution.xls";
			} else {
				pathChrReport = FileOperate.changeFilePrefix(pathAndName, "ChrDistribution_", "xls");
			}
			TxtReadandWrite txtWrite = new TxtReadandWrite(pathChrReport, true);
			txtWrite.writefileln("ChrID\tMappedReadsNum\tMappedReadsProp\tChrLen\tChrLenProp");
			for (String chrID : mapChrID2LenProp.keySet()) {
				String[] info = new String[5];
				info[0] = chrID;
				double[] resultTmp = mapChrID2LenProp.get(chrID);
				info[1] = (long)resultTmp[0] + "";
				info[2] = resultTmp[1] + "";
				info[3] = (long)resultTmp[2] + "";
				info[4] = resultTmp[3] + "";
				txtWrite.writefileln(info);
			}
			txtWrite.close();
		} catch (Exception e) {
			logger.error("aopRNAMapping生成表格出错啦！");
			return false;
		}
		return true;
	}
	@Override
	protected boolean buildImages() {
		if (mapChrID2LenProp.size() > chrNumMax) {
			return true;
		}
		try {
			String pathChrPic = "";
			if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
				pathChrPic = pathAndName + "ChrDistribution.png";
			} else {
				pathChrPic = FileOperate.changeFilePrefix(pathAndName, "ChrDistribution_", "png");
			}
			addParamInfo(Param.picParam, FileOperate.getFileName(pathChrPic));
			return drawMappingImage(pathChrPic);
		} catch (Exception e) {
			logger.error("aopRNAMapping画图表出错啦！");
			return false;
		}
	}

	/**
	 * 画mapping的结果图
	 * 
	 * @param picName
	 *            图片的全路径及名称
	 * @param data
	 *            作图的数据
	 * @return 是否成功
	 */
	private boolean drawMappingImage(String picName) {
		double[][] allData = getResultProp();
		String[] rowkeys = {"Sequencing","Genome"};
		String[] columnKeys = mapChrID2LenProp.keySet().toArray(new String[0]);
		
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset( rowkeys,columnKeys, allData);
		JFreeChart chart = ChartFactory.createBarChart("Mapping Result", null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		//设置图例的位置
		LegendTitle legend = chart.getLegend();
		legend.setItemFont(new Font("宋体", Font.PLAIN, 20));
		legend.setPadding(20, 20, 20, 20);
		legend.setPosition(RectangleEdge.RIGHT);
		legend.setMargin(0, 0, 0, 50);
		
		// 设置图标题的字体
		Font font = new Font("黑体", Font.BOLD, 30);
		chart.getTitle().setFont(font);
		RectangleInsets titlePosition = chart.getTitle().getPadding();
		chart.getTitle().setPadding(titlePosition.getTop() + 30, titlePosition.getLeft(), titlePosition.getBottom(), titlePosition.getRight());
		chart.getTitle().setText("Reads Distribution On Chromosomes");
		chart.setBorderVisible(true);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		CategoryAxis cateaxis = plot.getDomainAxis();
		
		BarRenderer renderer = new BarRenderer();// 设置柱子的相关属性
		// 分类柱子之间的宽度
		renderer.setItemMargin(0.02);
		// 设置柱子宽度
		renderer.setMaximumBarWidth(0.03);
		renderer.setMinimumBarLength(0.1); //最短的BAR长度
		// 设置柱子类型
		BarPainter barPainter = new StandardBarPainter();
		renderer.setBarPainter(barPainter);
		renderer.setSeriesPaint(0, barColor1);
		renderer.setSeriesPaint(1, barColor2);
		// 是否显示阴影
		renderer.setShadowVisible(false);

		plot.setRenderer(renderer);
		// 设置横轴的标题
		cateaxis.setTickLabelFont(new Font("粗体", Font.BOLD, 16));
		// 让标尺以30度倾斜
		cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
		cateaxis.setLabel("Chromosome Distribution");
		cateaxis.setLabelFont(new Font("粗体", Font.BOLD, 20));
		//在lable和坐标轴之间插一个矩形，所以如果是下标签，设定该矩形的高度即可
		cateaxis.setLabelInsets(new RectangleInsets(10,0,10,0));
		// 纵轴
		NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
		numaxis.setTickLabelFont(new Font("宋体", Font.BOLD, 10));
		//纵轴标尺的间距
		numaxis.setTickUnit(new NumberTickUnit(PlotBar.getSpace(numaxis.getRange().getUpperBound(), 5)));
		numaxis.setLabelFont(new Font("粗体", Font.BOLD, 20));
		numaxis.setLabel("Proportion");
		//20表示左边marge，10表示lable与y轴的距离
		numaxis.setLabelInsets(new RectangleInsets(0,10,10,10));
		FileOutputStream fosPng = null;
		try {
			fosPng = new FileOutputStream(picName);
			ChartUtilities.writeChartAsPNG(fosPng, chart,1500, 700);
		} catch (Exception e) {
			return false;
		} finally {
			try {
				fosPng.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * 返回染色体高度的数据用于画图，染色体根据染色体编号进行排序
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 */
	private double[][] getResultProp() {
		double[][] dataInfo = new double[2][mapChrID2LenProp.size()];
		int i = 0;
		for (double[] ds : mapChrID2LenProp.values()) {
			dataInfo[0][i] = ds[1];
			dataInfo[1][i] = ds[3];
			i++;
		}
		return dataInfo;
	}
}
