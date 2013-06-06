package com.novelbio.aoplog;

import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
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
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlGOPath;
import com.novelbio.nbcgui.controltest.CtrlPath;

/**
 * 给GOPath添加report相关的参数说明
 * 
 * @author zong0jie
 * 
 */
@Component
@Aspect
public class AopGOPath {
	private static Logger logger = Logger.getLogger(AopGOPath.class);

	/**
	 * 用来拦截GOPath的生成excel的方法，在生成excel之前，先画一幅图，并向配置文件params.txt中加入生成报告所需的参数
	 * 
	 * @param excelPath
	 * @param ctrlGOPath
	 */
	@Before("execution (* com.novelbio.nbcgui.controltest.CtrlGOPath.saveExcel(*)) && args(excelPath) && target(ctrlGOPath)")
	public void goPathPoint(String excelPath, CtrlGOPath ctrlGOPath) {
		ReportBuilder goPathBuilder = new GoPathBuilder(excelPath, ctrlGOPath);
		if (goPathBuilder.buildExcels() && goPathBuilder.buildImages() && goPathBuilder.buildDescFile())
			return;
		logger.error("aopGoPath生成报告图表参数出现异常！");
	}

	/**
	 * gopath报告参数生成器
	 * 
	 * @author novelbio
	 * 
	 */
	private class GoPathBuilder extends ReportBuilder {
		/** 画的柱状图的柱的数量上限 */
		private static final int barMaxNum = 20;
		/** 拦截的excel的存放路径 */
		private String excelPath;
		/** 拦截的对象 */
		private CtrlGOPath ctrlGOPath;
		/** 筛选条件 */
		private String finderCondition = null;
		/** 是否是cluster */
		private boolean isCluster = false;
		/** 是否是pathway */
		private boolean isPathway = false;
		/** 结果标题 */
		private String title = "";

		/**
		 * 
		 * @param excelPath
		 *            拦截的excel的存放路径
		 * @param ctrlGOPath
		 *            拦截的对象
		 */
		public GoPathBuilder(String excelPath, CtrlGOPath ctrlGOPath) {
			this.excelPath = excelPath;
			this.ctrlGOPath = ctrlGOPath;
			this.isCluster = ctrlGOPath.isCluster();
			this.isPathway = ctrlGOPath instanceof CtrlPath;
			this.title = ctrlGOPath.getResultBaseTitle();
			if (!isPathway) {
				testMethodParam += ((CtrlGO) ctrlGOPath).getGoAlgorithm();
			}
		}

		@Override
		public boolean buildExcels() {
			try {
				// 拦截到对象中的结果集
				Map<String, FunctionTest> map = ctrlGOPath.getMapResult_Prefix2FunTest();
				for (Entry<String, FunctionTest> entry : map.entrySet()) {
					// excel中的testResult对象结果集
					List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
					FunctionTest functionTest = entry.getValue();
					String prix = entry.getKey();
					finderCondition = getfindCondition(lsTestResults, finderCondition);

					// 参数开始赋值
					if (prix.equalsIgnoreCase("up")) {
						upRegulationParam += functionTest.getAllDifGeneNum();
					}
					if (prix.equalsIgnoreCase("down")) {
						downRegulationParam += functionTest.getAllGeneNum();
					}

					// 赋值excel
					Map<String, List<String[]>> mapSheetName2LsInfo = functionTest.getMapWriteToExcel();
					// 加上前缀名
					String excelPathOut = FileOperate.changeFilePrefixReal(excelPath, title + "_", null);

					for (String sheetName : mapSheetName2LsInfo.keySet()) {
						if (isCluster) {
							excelParam1 += FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + prix + sheetName + ";";
						} else {
							excelParam += FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + prix + sheetName + ";";
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
				// 拦截到对象中的结果集
				Map<String, FunctionTest> map = ctrlGOPath.getMapResult_Prefix2FunTest();
				for (Entry<String, FunctionTest> entry : map.entrySet()) {
					// excel中的testResult对象结果集
					List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
					String prix = entry.getKey();

					// 赋值picture
					// excel中testResult对应的sheet的名字，将作为画的图的名字
					String picName = FileOperate.addSep(FileOperate.getParentPathName(excelPath)) + title + "_" + prix + ".png";
					// 画一张testResult的图
					if (drawPicture(picName, lsTestResults, title)) {
						if (isCluster) {
							picParam1 += FileOperate.getFileName(picName) + ";";
						} else {
							picParam += FileOperate.getFileName(picName) + ";";
						}
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
		public boolean buildDescFile() {
			finderConditionParam += finderCondition;
			TxtReadandWrite txtReadandWrite = null;
			try {
				txtReadandWrite = getParamsTxt(excelPath);
				// 把参数写入到params.txt
				txtReadandWrite.writefileln(picParam);
				txtReadandWrite.writefileln(excelParam);
				txtReadandWrite.writefileln(picParam1);
				txtReadandWrite.writefileln(excelParam1);
				txtReadandWrite.writefileln(testMethodParam);
				txtReadandWrite.writefileln(finderConditionParam);
				txtReadandWrite.writefileln(upRegulationParam);
				txtReadandWrite.writefileln(downRegulationParam);
				txtReadandWrite.flash();
			} catch (Exception e) {
				logger.error("GOPath生成自动化报告参数文件param.txt出错！");
				return false;
			} finally {
				try {
					txtReadandWrite.close();
				} catch (Exception e2) {
					logger.error("GOPath生成自动化报告参数文件param.txt出错！");
					return false;
				}
			}
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
		private boolean drawPicture(String picName, List<StatisticTestResult> lsTestResults, String title) throws Exception {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (int i = 0; i < barMaxNum; i++) {
				if (i < lsTestResults.size())
					dataset.addValue(lsTestResults.get(i).getLog2Pnegative(), "", lsTestResults.get(i).getItemTerm());
			}
			JFreeChart chart = ChartFactory.createBarChart(title, null, "-Log2P", dataset, PlotOrientation.VERTICAL, false, false, false);
			// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			// 设置图标题的字体
			Font font = new Font("黑体", Font.BOLD, 30);
			chart.getTitle().setFont(font);
			RectangleInsets titlePosition = chart.getTitle().getPadding();
			chart.getTitle().setPadding(titlePosition.getTop() + 60, titlePosition.getLeft(), titlePosition.getBottom(), titlePosition.getRight());
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
			BarRenderer renderer = new BarRenderer();// 设置柱子的相关属性
			// 设置柱子宽度
			renderer.setMaximumBarWidth(0.02);
			renderer.setMinimumBarLength(0.01000000000000001D); // 宽度
			// 设置柱子高度
			renderer.setMinimumBarLength(0.1);
			// 设置柱子类型
			BarPainter barPainter = new StandardBarPainter();
			renderer.setBarPainter(barPainter);
			renderer.setSeriesPaint(0, new Color(51, 102, 153));
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
			cateaxis.setTickLabelFont(new Font("粗体", Font.BOLD, 16));
			// 让标尺以30度倾斜
			cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
			// 纵轴
			NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
			numaxis.setLabelFont(new Font("宋体", Font.BOLD, 20));
			RectangleInsets titleYPosition = numaxis.getLabelInsets();
			numaxis.setLabelInsets(new RectangleInsets(titleYPosition.getTop() + 80, titleYPosition.getLeft(), titleYPosition.getBottom(), titleYPosition.getRight()));
			FileOutputStream fosPng = null;
			try {
				fosPng = new FileOutputStream(picName);
				ChartUtilities.writeChartAsPNG(fosPng, chart, 1000, 1000);
			} catch (Exception e) {
				logger.error(e.getMessage());
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
	}

}
