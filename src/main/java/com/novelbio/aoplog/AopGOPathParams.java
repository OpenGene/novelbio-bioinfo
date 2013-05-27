package com.novelbio.aoplog;

import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.annotation.functiontest.FunctionTest;
import com.novelbio.analysis.annotation.functiontest.StatisticTestResult;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.DateUtil;
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
public class AopGOPathParams {
	private static Logger logger = Logger.getLogger(AopGOPathParams.class);
	/** 画的柱状图的柱的数量上限 */
	private static int barMaxNum = 20;
	
	/**
	 * 用来拦截GOPath的生成excel的方法，在生成excel之前，先画一幅图，并向配置文件params.txt中加入生成报告所需的参数
	 * 
	 * @param excelPath
	 * @param ctrlGOPath
	 */
	@Before("execution (* com.novelbio.nbcgui.controltest.CtrlGOPath.saveExcel(*)) && args(excelPath) && target(ctrlGOPath)")
	public void goPathPoint(String excelPath, CtrlGOPath ctrlGOPath) {

		TxtReadandWrite txtReadandWrite = getParamsTxt(excelPath);
		// 定义所有需要的参数
		// 分上下调的图和表
		String excelParam = "lsExcels" + SepSign.SEP_INFO + "EXCEL::";
		String picParam = "lsPictures" + SepSign.SEP_INFO + "PICTURE::";
		// 不分上下调的图和表
		String excelParam1 = "lsExcels1" + SepSign.SEP_INFO + "EXCEL::";
		String picParam1 = "lsPictures1" + SepSign.SEP_INFO + "PICTURE::";
		// 测试方法参数
		String testMethodParam = "testMethod" + SepSign.SEP_INFO;
		// 筛选条件参数
		String finderConditionParam = "finderCondition" + SepSign.SEP_INFO;
		// 上调数参数
		String upRegulationParam = "upRegulation" + SepSign.SEP_INFO;
		// 下调数参数
		String downRegulationParam = "downRegulation" + SepSign.SEP_INFO;
		String finderCondition = null;
		
		// 拦截到对象中的结果集
		Map<String, FunctionTest> map = ctrlGOPath.getMapResult_Prefix2FunTest();
		for (Entry<String, FunctionTest> entry : map.entrySet()) {
			// excel中的testResult对象结果集
			List<StatisticTestResult> lsTestResults = entry.getValue().getTestResult();
			FunctionTest functionTest = entry.getValue();
			String prix = entry.getKey();

			// 参数开始赋值
			if (entry.getKey().equalsIgnoreCase("up")) {
				upRegulationParam += functionTest.getAllDifGeneNum();
			}
			if (entry.getKey().equalsIgnoreCase("down")) {
				downRegulationParam += functionTest.getAllGeneNum();
			}
			finderCondition = getfindCondition(lsTestResults,finderCondition);
			// TODO 加上其它参数
			// 赋值excel
			Map<String,List<String[]>> mapSheetName2LsInfo = functionTest.getMapWriteToExcel();
			String excelPathOut = FileOperate.changeFileSuffix(excelPath, "_" + prix, null);
			
			for(int i = 1; i <= mapSheetName2LsInfo.size();i++) {
				if(ctrlGOPath.isCluster()){
					excelParam1 += FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + i + ";";
					continue;
				}
				excelParam += FileOperate.getFileName(excelPathOut) + SepSign.SEP_INFO_SAMEDB + i + ";";
			}
			
			// 赋值picture
			// excel中testResult对应的sheet的名字，将作为画的图的名字
			String testResultSheetName = "";
			String title = "";
			if (ctrlGOPath instanceof CtrlGO){
				testResultSheetName = prix + StatisticTestResult.titleGO;
				title = "GO-Analysis";
			}
			if (ctrlGOPath instanceof CtrlPath){
				testResultSheetName = prix + StatisticTestResult.titlePath;
				title = "Pathway-Analysis";
			}
			String picName = FileOperate.addSep(FileOperate.getParentPathName(excelPath)) + testResultSheetName + ".png";
			
			// 画一张testResult的图
			if (drawPicture(picName, lsTestResults,title)) {
				if(ctrlGOPath.isCluster()){
					picParam1 += FileOperate.getFileName(picName) + ";";
				}else {
					picParam += FileOperate.getFileName(picName) + ";";
				}
			}
		}
		if (ctrlGOPath instanceof CtrlGO){
			testMethodParam += ((CtrlGO) ctrlGOPath).getGoAlgorithm();
		}
		
		finderConditionParam += finderCondition;
		// 把参数写入到params*.txt
		txtReadandWrite.writefileln(picParam);
		txtReadandWrite.writefileln(excelParam);
		txtReadandWrite.writefileln(picParam1);
		txtReadandWrite.writefileln(excelParam1);
		txtReadandWrite.writefileln(testMethodParam);
		txtReadandWrite.writefileln(finderConditionParam);
		txtReadandWrite.writefileln(upRegulationParam);
		txtReadandWrite.writefileln(downRegulationParam);
		txtReadandWrite.flash();
		txtReadandWrite.close();
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
	 * 取得参数文件
	 * 
	 * @param excelPath
	 * @return
	 */
	private TxtReadandWrite getParamsTxt(String excelPath) {
		String paramsTxtPath = null;
		// 判断这些参数是放在本地还是hdfs上
		Boolean isHdfs = excelPath.substring(0, 3).equalsIgnoreCase("HDFS");

		if (isHdfs) {
			paramsTxtPath = FileOperate.addSep(excelPath) + "params" + DateUtil.getDateAndRandom() + ".txt";
		} else {
			// 在excelPath找params随.txt，没有就新建一个
			paramsTxtPath = FileOperate.getParentPathName(excelPath) + "params.txt";
			if (!FileOperate.isFileExist(paramsTxtPath)) {
				FileOperate.createFile(paramsTxtPath, null);
			}
		}
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(paramsTxtPath, true);
		return txtReadandWrite;
	}

	/**
	 * 根据参数画gopath的柱状图
	 * 
	 * @return　是否成功
	 */
	private boolean drawPicture(String picName, List<StatisticTestResult> lsTestResults,String title) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < barMaxNum; i++) {
			if(i<lsTestResults.size())
				dataset.addValue( lsTestResults.get(i).getLog2Pnegative(),"",lsTestResults.get(i).getItemTerm());
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
		//chart.setBorderPaint(Color.white);
		chart.setBorderVisible(true);
		// chart.setBackgroundPaint(Color.WHITE);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		// CategoryPlot plot = (CategoryPlot) chart.getCategoryPlot();
		// plot.setRenderer(render);//使用我们设计的效果
		CategoryAxis cateaxis = plot.getDomainAxis();
		// BarRenderer render = new BarRenderer();
		// render.setBaseFillPaint(Color.pink);
		// plot.setRenderer(render);
		BarRenderer renderer = new BarRenderer();// 设置柱子的相关属性
		// 设置柱子宽度
		renderer.setMaximumBarWidth(0.05);
		renderer.setMinimumBarLength(0.01000000000000001D); //宽度
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
		renderer.setItemMargin(0.2);
		
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
		} catch(Exception e){
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
