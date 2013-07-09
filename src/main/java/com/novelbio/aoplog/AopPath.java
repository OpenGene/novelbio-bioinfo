package com.novelbio.aoplog;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.jfree.chart.ChartFactory;
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
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.base.SepSign;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.base.plot.PlotBar;
import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlTestPathInt;

/**
 * 给GOPath添加report相关的参数说明
 * 
 * @author zong0jie
 * 
 */
public class AopPath {
	private static Logger logger = Logger.getLogger(AopPath.class);

	/**
	 * 用来拦截GOPath的生成excel的方法，在生成excel之前，先画一幅图，并向配置文件params.txt中加入生成报告所需的参数
	 * @param excelPath
	 * @param ctrlTestPathInt
	 */
	@After("execution (* com.novelbio.nbcgui.controltest.CtrlTestPathInt.saveExcel(*)) && target(ctrlTestPathInt)")
	public void goPathPoint(CtrlTestPathInt ctrlTestPathInt) {
		ReportBuilder goPathBuilder = new PathBuilder(ctrlTestPathInt);
		goPathBuilder.writeInfo();
	}

	/**
	 * gopath报告参数生成器
	 * 
	 * @author novelbio
	 * 
	 */
	public static class PathBuilder extends ReportBuilder {
		/** 画的柱状图的柱的数量上限 */
		private static final int barMaxNumVertical = 25;
		/** 画的柱状图的柱的数量上限 */
		private static final int barMaxNumHorizon = 15;
		/** 拦截的对象 */
		private CtrlTestPathInt ctrlTestPathInt;
		/** 筛选条件 */
		private String finderCondition = null;

		/**
		 * 
		 * @param excelPath
		 *            拦截的excel的存放路径
		 * @param ctrlTestPathInt
		 *            拦截的对象
		 */
		public PathBuilder( CtrlTestPathInt ctrlTestPathInt) {
			this.ctrlTestPathInt = ctrlTestPathInt;
			setParamPath(FileOperate.getParentPathName(ctrlTestPathInt.getSaveExcelPrefix()));
		}

		@Override
		public boolean buildExcels() {
			try {
				// 拦截到对象中的结果集
				Map<String, FunctionTest> map = ctrlTestPathInt.getMapResult_Prefix2FunTest();
				for (Entry<String, FunctionTest> entry : map.entrySet()) {
					// excel中的testResult对象结果集
					List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
					FunctionTest functionTest = entry.getValue();
					String prix = entry.getKey();
					finderCondition = getfindCondition(lsTestResults, finderCondition);

					// 参数开始赋值
					if (prix.equalsIgnoreCase("up")) {
						addParamInfo(Param.upRegulationParam, functionTest.getAllDifGeneNum() + "");
					}
					if (prix.equalsIgnoreCase("down")) {
						addParamInfo(Param.downRegulationParam, functionTest.getAllDifGeneNum() + "");
					}

					// 赋值excel
					Set<String> setSheetName = functionTest.getMapWriteToExcel().keySet();
					String excelPathOut = ctrlTestPathInt.getSaveExcelPrefix();

					for (String sheetName : setSheetName) {
						if (ctrlTestPathInt.isCluster()) {
							addParamInfo(Param.excelParam1, FileOperate.getFileName(excelPathOut) + "_" + prix + SepSign.SEP_INFO_SAMEDB + sheetName);
						} else {
							addParamInfo(Param.excelParam, FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + prix + sheetName);
						}
						// TODO excel的说明文件在这里写
						//String descFile = FileOperate.changeFileSuffix(excelPathOut, "_" + prix + sheetName + "_xls", ".txt");
					}
				}
			} catch (Exception e) {
				logger.error("aopGoPath生成excel出错！");
				return false;
			}
			return true;
		}

		@Override
		public boolean buildImages() {
			try {
				Map<String, FunctionTest> map = ctrlTestPathInt.getMapResult_Prefix2FunTest();
				for (Entry<String, FunctionTest> entry : map.entrySet()) {
					List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
					String prix = entry.getKey();
					String excelPath = ctrlTestPathInt.getSaveParentPath();
					String picNameLog2P = FileOperate.addSep(excelPath) + "Path-Analysis-Log2P_" + prix + "_" + ctrlTestPathInt.getSavePrefix() + ".png";
					BufferedImage bfImageLog2Pic = drawLog2PvaluePicture(lsTestResults, ctrlTestPathInt.getResultBaseTitle());
					if (bfImageLog2Pic == null) return false;
					
					ImageIO.write(bfImageLog2Pic, "png", new File(picNameLog2P));
					if (ctrlTestPathInt.isCluster()) {
						addParamInfo(Param.picParam1, FileOperate.getFileName(picNameLog2P));
					} else {
						addParamInfo(Param.picParam, FileOperate.getFileName(picNameLog2P));
					}
					
					String picNameEnrichment = FileOperate.addSep(excelPath) + "Path-Analysis-Enrichment_" + prix + "_" + ctrlTestPathInt.getSavePrefix() + ".png";
					BufferedImage bfImageEnrichment = drawEnrichmentPicture(lsTestResults, ctrlTestPathInt.getResultBaseTitle());
					if (bfImageEnrichment == null) return false;
					
					ImageIO.write(bfImageEnrichment, "png", new File(picNameEnrichment));
					if (ctrlTestPathInt.isCluster()) {
						addParamInfo(Param.picParam1, FileOperate.getFileName(picNameEnrichment));
					} else {
						addParamInfo(Param.picParam, FileOperate.getFileName(picNameEnrichment));
					}
					
					// TODO image的说明文件在这里写
					// String descFile = FileOperate.changeFileSuffix(picName,
					// "_pic", "txt");
				}
			} catch (Exception e) {
				logger.error("aopGoPath生成图表出错！");
				return false;
			}
			return true;
		}

		@Override
		protected boolean fillDescFile() {
			return true;
		}
		
		/**
		 * 统计结果，返回筛选条件
		 * 
		 * @param lsTestResults
		 * @param knownCondition
		 *            已知条件，用来比较返回更宽松的条件
		 * @return
		 */
		private String getfindCondition(List<StatisticTestResult> lsTestResults, String knownCondition) {
			int fdrSum1 = 0;
			int fdrSum5 = 0;
			int pValueSum1 = 0;
			String[] result = { "FDR&lt;0.01", "FDR&lt;0.05", "P-value&lt;0.01", "P-value&lt;0.05" };
			int conditionNum = 0;
			if (knownCondition != null) {
				for (int i = 0; i < result.length; i++) {
					if (knownCondition.equals(result[i]))
						conditionNum = i;
				}
			}
			for (StatisticTestResult testResult : lsTestResults) {
				if (testResult.getPvalue() < 0.01) {
					pValueSum1++;
				}
				if (testResult.getPvalue() < 0.05) {
				}
				if (testResult.getFdr() < 0.01) {
					fdrSum1++;
				}
				if (testResult.getFdr() < 0.05) {
					fdrSum5++;
				}
			}
			int currentConditionNum = fdrSum1 > 8 ? 0 : (fdrSum5 > 8 ? 1 : (pValueSum1 > 8 ? 2 : 3));
			conditionNum = currentConditionNum > conditionNum ? currentConditionNum : conditionNum;
			return result[conditionNum];
		}

		/**
		 * 根据参数画gopath的柱状图
		 * 
		 * @return　是否成功
		 */
		public static BufferedImage drawLog2PvaluePicture(List<StatisticTestResult> lsTestResults, String title) throws Exception {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (int i = 0; i < barMaxNumHorizon; i++) {
				if (i < lsTestResults.size())
					dataset.addValue(lsTestResults.get(i).getLog2Pnegative(), "", lsTestResults.get(i).getItemTerm());
			}
			JFreeChart chart = ChartFactory.createBarChart(title, null, "-Log2P", dataset, PlotOrientation.HORIZONTAL, false, false, false);
			// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			// 设置图标题的字体
			chart.getTitle().setFont(new Font("黑体", Font.BOLD, 30));
			/** title永远是居中的，但是我们想要让title靠上或者靠边怎么办呢，
			 * 就要将title包装成一个矩形，然后jfreechart会将这个矩形居中
			 * 所以第一个就是矩形的上边，这样上边设置越大，title与上边框的距离就越大
			 * 第二个是左边，左边设置越大，title与左边界的距离也就越大
			 * 第三个是下边，下边越大，title与下边图片的距离也越大
			 */
			chart.getTitle().setPadding(20,0,20,0);
			// TextTitle title = new TextTitle("直方图测试");
			// 设置图例中的字体
			// LegendTitle legend = chart.getLegend();
			// legend.setItemFont(new Font("宋体", Font.BOLD, 16));
			// chart.setBorderPaint(Color.white);
			chart.setBorderVisible(true);
			// chart.setBackgroundPaint(Color.WHITE);
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.white);
			plot.setOutlinePaint(Color.WHITE); // 设置绘图面板外边的填充颜色
			// CategoryPlot plot = (CategoryPlot) chart.getCategoryPlot();
			// plot.setRenderer(render);//使用我们设计的效果
			CategoryAxis cateaxis = plot.getDomainAxis();
			// BarRenderer render = new BarRenderer();
			// render.setBaseFillPaint(Color.pink);
			// plot.setRenderer(render);
			
			JFreeChartBarRender renderer = getJfreechartBarRender(lsTestResults);
			// 设置柱子宽度
			renderer.setMaximumBarWidth(0.03);
			renderer.setMinimumBarLength(0.01000000000000001D); // 宽度
			// 设置柱子高度
			renderer.setMinimumBarLength(0.1);
			// 设置柱子类型
			BarPainter barPainter = new StandardBarPainter();
			renderer.setBarPainter(barPainter);
			
			
			
			
			
			
//			renderer.setSeriesPaint(0, new Color(51, 102, 153));
			// 是否显示阴影
			renderer.setShadowVisible(false);
			// 阴影颜色
			// renderer1.setShadowPaint(Color.white);
			// 设置柱子边框的渐变色
			// renderer1.setBarPainter(new GradientBarPainter(1,1,1));
			// 设置柱子边框颜色
			// renderer1.setBaseOutlinePaint(Color.BLACK);
			// 设置柱子边框可见
			// renderer1.setDrawBarOutline(true);
			// 设置每个地区所包含的平行柱的之间距离，数值越大则间隔越大，图片大小一定的情况下会影响柱子的宽度，可以为负数
			renderer.setItemMargin(0.4);

			plot.setRenderer(renderer);

			// 设置横轴的标题
			// cateaxis.setLabelFont(new Font("粗体", Font.BOLD, 16));
			// 设置横轴的标尺
			cateaxis.setTickLabelFont(new Font(Font.SERIF, Font.BOLD, 22));
			cateaxis.setMaximumCategoryLabelWidthRatio(0.45f);
			// 让标尺以30度倾斜
//			cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));
			// 纵轴
			NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
			numaxis.setTickUnit(new NumberTickUnit(PlotBar.getSpace(numaxis.getRange().getUpperBound(), 10)));
			numaxis.setLabelFont(new Font("宋体", Font.BOLD, 25));
			numaxis.setLabelInsets(new RectangleInsets(0, 500, 10, 0));
			return chart.createBufferedImage(1000, 1000);
			
			
//			FileOutputStream fosPng = null;
//			try {
//				fosPng = new FileOutputStream(picName);
//				ChartUtilities.writeChartAsPNG(fosPng, chart, 1200, 1000);
//			} catch (Exception e) {
//				logger.error(e.getMessage());
//				return false;
//			} finally {
//				try {
//					fosPng.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			return true;
		}
		/**
		 * 根据参数画gopath的柱状图
		 * 
		 * @return　是否成功
		 */
		public static BufferedImage drawEnrichmentPicture(List<StatisticTestResult> lsTestResults, String title) throws Exception {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (int i = 0; i < barMaxNumVertical; i++) {
				if (i < lsTestResults.size())
					dataset.addValue(lsTestResults.get(i).getEnrichment(), "", lsTestResults.get(i).getItemTerm());
			}
			JFreeChart chart = ChartFactory.createBarChart(title, null, "Enrichment", dataset, PlotOrientation.VERTICAL, false, false, false);
			// 设置图标题的字体
			chart.getTitle().setFont(new Font("黑体", Font.BOLD, 30));
			chart.getTitle().setPadding(20,0,20,0);
			chart.setBorderVisible(true);
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.white);
			plot.setOutlinePaint(Color.WHITE);
			
			NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
			numaxis.setLabelFont(new Font("宋体", Font.BOLD, 25));
			numaxis.setTickUnit(new NumberTickUnit(PlotBar.getSpace(numaxis.getRange().getUpperBound(), 10)));
			numaxis.setLabelInsets(new RectangleInsets(60, 15, 0, 30));
			
			CategoryAxis cateaxis = plot.getDomainAxis();
			cateaxis.setTickLabelFont(new Font(Font.SERIF, Font.BOLD, 22));
			cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));// 让标尺以30度倾斜
			
			BarRenderer renderer =getJfreechartBarRender(lsTestResults);
			// 分类柱子之间的宽度
			renderer.setItemMargin(0.02);
			// 设置柱子宽度
			renderer.setMaximumBarWidth(0.028);
			renderer.setMinimumBarLength(0.01000000000000001D); // 宽度
			// 设置柱子高度
			renderer.setMinimumBarLength(0.1);
			// 设置柱子类型
			BarPainter barPainter = new StandardBarPainter();
			renderer.setBarPainter(barPainter);
			// 是否显示阴影
			renderer.setShadowVisible(false);
			// 设置每个地区所包含的平行柱的之间距离，数值越大则间隔越大，图片大小一定的情况下会影响柱子的宽度，可以为负数
			renderer.setItemMargin(0.4);
			plot.setRenderer(renderer);

			return chart.createBufferedImage(1200, 1000);
			
//			FileOutputStream fosPng = null;
//			try {
//				fosPng = new FileOutputStream(picName);
//				ChartUtilities.writeChartAsPNG(fosPng, chart, 1000, 1000);
//			} catch (Exception e) {
//				logger.error(e.getMessage());
//			} finally {
//				try {
//					fosPng.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			return true;
		}
	}
	
	/**
	 * 根据fdr的值，来对bar进行染色
	 * @param lsTestResults
	 * @return
	 */
	private static JFreeChartBarRender getJfreechartBarRender(List<StatisticTestResult> lsTestResults) {
		JFreeChartBarRender jFreeChartBarRender = new JFreeChartBarRender();
		BarColor barColorRed = new BarColor(new Color(228, 55, 18));
		BarColor barColorBlue = new BarColor(new Color(51, 102, 153));

		int i = 0;
		for (StatisticTestResult statisticTestResult : lsTestResults) {
			if (statisticTestResult.getPvalue() < 0.05) {
				barColorRed.addBarNum(i);
			} else {
				barColorBlue.addBarNum(i);
			}
			i++;
		}
		
		jFreeChartBarRender.addBarColor(barColorRed);
		jFreeChartBarRender.addBarColor(barColorBlue);
		return jFreeChartBarRender;
	}
	
}
