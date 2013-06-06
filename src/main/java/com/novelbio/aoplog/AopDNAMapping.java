package com.novelbio.aoplog;

import java.awt.Color;
import java.awt.Font;
import java.io.FileOutputStream;
import java.util.ArrayList;
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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.springframework.stereotype.Component;

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

	/**
	 * mapping报告参数生成器
	 * @author novelbio
	 *
	 */
	private class MappingBuilder extends ReportBuilder{
		/** 拦截到的返回值sam文件 */
		private SamFile samFile;
		/** 拦截到的对象 */
		private MapDNAint mapDNAint;
		/** sam文件的全路径，不包含后缀 */
		private String pathAndName;
		/** 运行的cmd命令 */
		private String cmdMapping;
		private List<String[]> lsReport = new ArrayList<String[]>();
		public MappingBuilder(SamFile samFile, MapDNAint mapDNAint) {
			this.samFile = samFile;
			this.mapDNAint = mapDNAint;
		}
		
		public boolean saveCmdMapping(){
			// TODO 把命令持久化起来
			cmdMapping = mapDNAint.getCmdMapping();
			System.out.println(cmdMapping);
			return true;
		}
		
		@Override
		public boolean buildExcels() {
			TxtReadandWrite txtReadandWrite = null;
			try {
				pathAndName = FileOperate.getParentPathName(samFile.getFileName()) + FileOperate.getFileNameSep(samFile.getFileName())[0];
				lsReport.add(new String[] { "Statistics Term", "Result(" + FileOperate.getFileNameSep(samFile.getFileName())[0] + ")" });
				for (AlignmentRecorder alignmentRecorder : mapDNAint.getLsAlignmentRecorders()) {
					if (alignmentRecorder instanceof SamFileStatistics) {
						SamFileStatistics samFileStatistics = (SamFileStatistics) alignmentRecorder;
						double allReads = samFileStatistics.getReadsNum(MappingReadsType.allReads);
						double unMapped = samFileStatistics.getReadsNum(MappingReadsType.unMapped);
						double allMappedReads = samFileStatistics.getReadsNum(MappingReadsType.allMappedReads);
						double uniqueMapping = samFileStatistics.getReadsNum(MappingReadsType.uniqueMapping);
						double repeatMapping = samFileStatistics.getReadsNum(MappingReadsType.repeatMapping);
						double junctionAllMappedReads = samFileStatistics.getReadsNum(MappingReadsType.junctionAllMappedReads);
						double junctionUniqueMapping = samFileStatistics.getReadsNum(MappingReadsType.junctionUniqueMapping);

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
						
					}
				}
				excelParam += FileOperate.getFileName(pathAndName) + "_report.xls;";
				txtReadandWrite = new TxtReadandWrite(pathAndName + "_report.xls", true);
				txtReadandWrite.ExcelWrite(lsReport);
			} catch (Exception e) {
				logger.error("aopRNAMapping生成表格出错啦！");
				return false;
			} finally{
				try {
					txtReadandWrite.close();
				} catch (Exception e2) {
				}
			}
			return true;
		}

		@Override
		public boolean buildImages() {
			try {
				for (AlignmentRecorder alignmentRecorder : mapDNAint.getLsAlignmentRecorders()) {
					if (alignmentRecorder instanceof SamFileStatistics) {
						SamFileStatistics samFileStatistics = (SamFileStatistics) alignmentRecorder;
						String picName = pathAndName + ".png";
						if (drawMappingImage(picName, samFileStatistics.getMapChrID2Len())) {
							picParam += FileOperate.getFileName(picName) + ";";
						}
					}
				}
			} catch (Exception e) {
				logger.error("aopRNAMapping画图表出错啦！");
				return false;
			}
			return true;
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
		private boolean drawMappingImage(String picName, Map<String, Long> data) {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (String key : data.keySet()) {
				dataset.addValue(data.get(key), "", key);
			}
			JFreeChart chart = ChartFactory.createBarChart("Mapping Result", null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
			// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			// 设置图标题的字体
			Font font = new Font("黑体", Font.BOLD, 30);
			chart.getTitle().setFont(font);
			RectangleInsets titlePosition = chart.getTitle().getPadding();
			chart.getTitle().setPadding(titlePosition.getTop() + 60, titlePosition.getLeft(), titlePosition.getBottom(), titlePosition.getRight());
			chart.setBorderVisible(true);
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.white);
			CategoryAxis cateaxis = plot.getDomainAxis();
			BarRenderer renderer = new BarRenderer();// 设置柱子的相关属性
			// 设置柱子宽度
			renderer.setMaximumBarWidth(0.05);
			renderer.setMinimumBarLength(0.01000000000000001D); // 宽度
			// 设置柱子高度
			renderer.setMinimumBarLength(0.1);
			// 设置柱子类型
			BarPainter barPainter = new StandardBarPainter();
			renderer.setBarPainter(barPainter);
			renderer.setSeriesPaint(0, new Color(23, 200, 200));
			// 是否显示阴影
			renderer.setShadowVisible(false);
			renderer.setItemMargin(0.2);

			plot.setRenderer(renderer);
			// 设置横轴的标题
			cateaxis.setTickLabelFont(new Font("粗体", Font.BOLD, 16));
			// 让标尺以30度倾斜
			cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
			// 纵轴
			NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
			numaxis.setLabelFont(new Font("宋体", Font.BOLD, 20));
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
