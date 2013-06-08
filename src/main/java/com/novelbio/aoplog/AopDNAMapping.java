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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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

import com.novelbio.analysis.seq.fasta.ChrStringHash.CompareChrID;
import com.novelbio.analysis.seq.mapping.MapDNAint;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamFileStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

@Component
@Aspect
public class AopDNAMapping {
	private static Logger logger = Logger.getLogger(AopDNAMapping.class);
	/** 超过50条染色体就不画这个图了 */
	private static int chrNumMax = 50;
	@Around("execution (* com.novelbio.analysis.seq.mapping.MapDNAint.mapReads(..)) && target(mapDNAint)")
	public void aroundMapping(ProceedingJoinPoint pjp, MapDNAint mapDNAint) {
		SamFile samFile = null;
		try {
			samFile = (SamFile) pjp.proceed();
		} catch (Throwable e) {
			logger.error("aopDNAMapping拦截失败！");
		}
		if (!(samFile != null && FileOperate.isFileExistAndBigThanSize(samFile.getFileName(), 0))) {
			logger.error("samFile不存在，aopDNAMapping拦截失败！");
			return;
		}
		
		MappingBuilder mappingBuilder = new MappingBuilder(samFile,mapDNAint);
		if (!(mappingBuilder.buildExcels() && mappingBuilder.buildImages() && mappingBuilder.buildDescFile())) 
			logger.error("AopDNAMapping拦截不成功，有部分内容没有成功生成！");
		
		//TODO 保存执行的命令
		mappingBuilder.saveCmdMapping();
	}
	
	/** 仅用于单元测试 */
	public void test(SamFile samFile, MapDNAint mapDNAint) {
		if (!(samFile != null && FileOperate.isFileExistAndBigThanSize(samFile.getFileName(), 0))) {
			logger.error("samFile不存在，aopDNAMapping拦截失败！");
			return;
		}
		
		MappingBuilder mappingBuilder = new MappingBuilder(samFile,mapDNAint);
		if (!(mappingBuilder.buildExcels() && mappingBuilder.buildImages() && mappingBuilder.buildDescFile())) 
			logger.error("AopDNAMapping拦截不成功，有部分内容没有成功生成！");
		
		//TODO 保存执行的命令
		mappingBuilder.saveCmdMapping();
	}
	
	/**
	 * mapping报告参数生成器
	 * @author novelbio
	 *
	 */
	private class MappingBuilder extends ReportBuilder {
		private final Color barColor1 = new Color(23, 200, 200); 
		private final Color barColor2 = new Color(100, 100, 100); 
		/** 拦截到的返回值sam文件 */
		private SamFile samFile;
		/** 拦截到的对象 */
		private SamFileStatistics samFileStatistics;
		/** sam文件的全路径，不包含后缀 */
		private String pathAndName;
		/** 运行的cmd命令 */
		private String cmdMapping;
		private List<String[]> lsReport = new ArrayList<String[]>();
		
		/**
		 * reads的分布情况统计
		 * key: chrID<br>
		 * value: double[4] 0: readsNum 1: readsProp 2: chrLen 3: chrProp
		 */
		private Map<String, double[]> mapChrID2LenProp = new LinkedHashMap<String, double[]>();
		
		
		public MappingBuilder(SamFile samFile, MapDNAint mapDNAint) {
			this.samFile = samFile;
			for (AlignmentRecorder alignmentRecorder : mapDNAint.getLsAlignmentRecorders()) {
				if (alignmentRecorder instanceof SamFileStatistics) {
					samFileStatistics = (SamFileStatistics) alignmentRecorder;
					break;
				}
			}
			pathAndName = FileOperate.getParentPathName(samFile.getFileName()) + FileOperate.getFileNameSep(samFile.getFileName())[0];
			setMapChrID2PropAndLen(samFileStatistics.getMapChrID2Len(), samFile.getChrID2LengthMap());
			cmdMapping = mapDNAint.getCmdMapping();
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
		
		public boolean saveCmdMapping() {
			// TODO 把命令持久化起来
			System.out.println(cmdMapping);
			return true;
		}
		
		@Override
		public boolean buildExcels() {
			if (mapChrID2LenProp == null || mapChrID2LenProp.size() == 0) {
				return true;
			}
			boolean samReprot = writeSamReport();
			boolean chrReport = writeChrInfo();
			return samReprot && chrReport;
		}

		/** 写junction reads等统计数据 */
		private boolean writeSamReport() {
			try {
				lsReport.add(new String[] { "Statistics Term", "Result(" + FileOperate.getFileNameSep(samFile.getFileName())[0] + ")" });
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
				
				excelParam += FileOperate.getFileName(pathAndName) + "_MappingReport.xls;";
				TxtReadandWrite txtReadandWrite = new TxtReadandWrite(pathAndName + "_MappingReport.xls", true);
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
				TxtReadandWrite txtWrite = new TxtReadandWrite(pathAndName + "_ChrReport.xls", true);
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
		public boolean buildImages() {
			try {
				String picName = pathAndName + ".png";
				picParam += FileOperate.getFileName(picName) + ";";
				return drawMappingImage(picName);
			} catch (Exception e) {
				logger.error("aopRNAMapping画图表出错啦！");
				return false;
			}
		}

		@Override
		public boolean buildDescFile() {
			TxtReadandWrite txtReadandWrite = null;
			try {
				txtReadandWrite = getParamsTxt(samFile.getFileName());
				// 把参数写入到params.txt
				txtReadandWrite.writefileln(picParam);
				txtReadandWrite.writefileln(excelParam);
			} catch (Exception e) {
				logger.error("aopRNAMapping生成参数文件出错啦！");
				return false;
			} finally{
				try {
					txtReadandWrite.close();
				} catch (Exception e2) {
				}
			}
			return true;
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
			if (mapChrID2LenProp.size() > chrNumMax) {
				return true;
			}
			
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
			numaxis.setTickUnit(new NumberTickUnit(0.02));
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

	
	
}
