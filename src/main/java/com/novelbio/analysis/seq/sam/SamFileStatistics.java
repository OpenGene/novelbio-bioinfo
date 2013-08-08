package com.novelbio.analysis.seq.sam;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
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
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.fasta.ChrStringHash.CompareChrID;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.base.plot.PlotBar;

/** <>仅用于分析sambam文件<>
 * 根据需求判定是否需要执行{@link #initial()}
 *  */
public class SamFileStatistics implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(SamFileStatistics.class);
	SamFile samFile;
	boolean countReadsNum;
	
	double allReadsNum = 0;
	double unmappedReadsNum = 0;
	double mappedReadsNum = 0;
	double uniqMappedReadsNum = 0;
	double repeatMappedReadsNum = 0;
	double junctionUniReads = 0;
	double junctionAllReads = 0;
	
	/** 用于画图和生成表格的参数 */
	HashMap<String, double[]> mapChrID2LenProp = null;
	/** standardData 染色体长度的map */
	Map<String, Long> standardData;
	/** 超过50条染色体就不画这个图了 */
	private static int chrNumMax = 50;
	String prefix = "";
	
	Map<String, double[]> mapChrID2ReadsNum = new TreeMap<String, double[]>(new Comparator<String>() {
		PatternOperate patternOperate = new PatternOperate("\\d+", false);
		@Override
		public int compare(String arg0, String arg1) {
			String str1 = patternOperate.getPatFirst(arg0);
			String str2 = patternOperate.getPatFirst(arg1);
			if (str1 == null || str1.equals("") || str2 == null || str2.equals("")) {
				 return arg0.compareTo(arg1);
			}
			Integer num1 = Integer.parseInt(str1);
			Integer num2 = Integer.parseInt(str2);
			return num1.compareTo(num2);
		}
	});
	
	public SamFileStatistics(String prefix) {
		this.prefix = prefix;
	}
	public void setStandardData(Map<String, Long> standardData) {
		this.standardData = standardData;
	}
	protected void setSamFile(SamFile samFile) {
		this.samFile = samFile;
		countReadsNum = false;
	}
	public SamFile getSamFile() {
		return samFile;
	}
	/**
	 * 返回readsNum
	 * @param mappingType MAPPING_ALLREADS等
	 * @return -1表示错误
	 */
	public long getReadsNum(MappingReadsType mappingType) {
		if (mappingType == MappingReadsType.allReads) {
			return (long)allReadsNum;
		}
		if (mappingType == MappingReadsType.allMappedReads) {
			return (long)mappedReadsNum;
		}
		if (mappingType == MappingReadsType.unMapped) {
			return (long)unmappedReadsNum;
		}
		
		if (mappingType == MappingReadsType.uniqueMapping) {
			return (long)uniqMappedReadsNum;
		}

		if (mappingType == MappingReadsType.repeatMapping) {
			return (long)repeatMappedReadsNum;
		}
		
		if (mappingType == MappingReadsType.junctionUniqueMapping) {
			return (long)junctionUniReads;
		}
		if (mappingType == MappingReadsType.junctionAllMappedReads) {
			return (long)junctionAllReads;
		}
		return -1;
	}
	
	/** 把结果写入文本，首先要运行 statistics */
	public void writeToFile(String outFileName) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		txtWrite.ExcelWrite(getMappingInfo());
		txtWrite.close();
	}
	
	/**
	 * 获取每条染色体所对应的reads数量
	 * key都为小写
	 * @return
	 */
	public Map<String, Long> getMapChrID2Len() {
		Map<String, Long> mapChrID2Len = new LinkedHashMap<String, Long>();
		for (String chrID : mapChrID2ReadsNum.keySet()) {
			mapChrID2Len.put(chrID.toLowerCase(), (long)mapChrID2ReadsNum.get(chrID)[0]);
		}
		return mapChrID2Len;
	}
	
	/**
	 * 首先要运行 statistics
	 * @return
	 */
	public ArrayList<String[]> getMappingInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"allReadsNum", (long)allReadsNum + ""});
		lsResult.add(new String[]{"mappedReadsNum", (long)mappedReadsNum + ""});
		lsResult.add(new String[]{"uniqMappedReadsNum", (long)uniqMappedReadsNum + ""});
		lsResult.add(new String[]{"repeatMappedReadsNum", (long)repeatMappedReadsNum + ""});
		lsResult.add(new String[]{"junctionAllReads", (long)junctionAllReads + ""});
		lsResult.add(new String[]{"junctionUniReads", (long)junctionUniReads + ""});
		lsResult.add(new String[]{"unmappedReadsNum", (long)unmappedReadsNum + ""});

		lsResult.add(new String[]{"mappringRates", (double)mappedReadsNum/allReadsNum + ""});
		lsResult.add(new String[]{"uniqMappingRates", (double)uniqMappedReadsNum/allReadsNum + ""});
		
		lsResult.add(new String[]{"Reads On Chromosome",  ""});
		try {
			lsResult.addAll(getLsChrID2Num());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lsResult;
	}
	
	private ArrayList<String[]> getLsChrID2Num() {
		ArrayList<String[]> lsChrID2Num = new ArrayList<String[]>();
		for (Entry<String, double[]> entry : mapChrID2ReadsNum.entrySet()) {
			String[] tmp = new String[]{entry.getKey(), (long)entry.getValue()[0] + ""};
			lsChrID2Num.add(tmp);
		}
		Collections.sort(lsChrID2Num, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				return o1[0].compareTo(o2[0]);
			}
		});
		return lsChrID2Num;
	}
	
	public void statistics() {
		if (countReadsNum) {
			return;
		}
		countReadsNum = true;
		 initial();
		readSamFile();
	}
	
	/** 初始化 */
	public void initial() {
		allReadsNum = 0;
		unmappedReadsNum = 0;
		mappedReadsNum = 0;
		uniqMappedReadsNum = 0;
		repeatMappedReadsNum = 0;
		junctionAllReads = 0;
		junctionUniReads = 0;
		mapChrID2ReadsNum.clear();
	}
	
	private void readSamFile() {
		for (SamRecord samRecord : samFile.readLines()) {
			addAlignRecord(samRecord);
		}
		summary();
		samFile.close();
	}

	@Override
	public void addAlignRecord(AlignRecord samRecord) {
		int readsMappedWeight = samRecord.getMappedReadsWeight();
		allReadsNum = allReadsNum + (double)1/readsMappedWeight;
		if (samRecord.isMapped()) {
			mappedReadsNum = mappedReadsNum + (double)1/readsMappedWeight;
			setChrReads(readsMappedWeight, samRecord);
			if (samRecord.isUniqueMapping()) {
				uniqMappedReadsNum ++;
				if (samRecord.isJunctionCovered()) {
					junctionUniReads ++;
				}
			}
			else {
				repeatMappedReadsNum = repeatMappedReadsNum + (double)1/readsMappedWeight;
			}
			if (samRecord.isJunctionCovered()) {
				junctionAllReads = junctionAllReads + (double)1/readsMappedWeight;
			}
		}
		else {
			unmappedReadsNum = unmappedReadsNum + (double)1/readsMappedWeight;
		}
	}

	
	private void setChrReads(int readsWeight, AlignRecord samRecord) {
		String chrID = samRecord.getRefID();
		double[] chrNum;
		if (mapChrID2ReadsNum.containsKey(chrID)) {
			chrNum = mapChrID2ReadsNum.get(chrID);
		}
		else {
			chrNum = new double[1];
			mapChrID2ReadsNum.put(chrID, chrNum);
		}
		chrNum[0] = chrNum[0] + (double)1/readsWeight;
	}

	@Override
	public void summary() {
		summeryReadsNum();
		modifyChrReadsNum();
	}
	
	/** 将所有reads数量四舍五入转变为long，同时矫正由double转换为long时候可能存在的偏差 */
	private void summeryReadsNum() {
		allReadsNum = Math.round(allReadsNum);
		unmappedReadsNum = Math.round(unmappedReadsNum);
		mappedReadsNum = Math.round(mappedReadsNum);
		//double 转换可能会有1的误差
		if (allReadsNum != mappedReadsNum + unmappedReadsNum) {
			if (Math.abs(mappedReadsNum + unmappedReadsNum - allReadsNum) > 10) {
				logger.error("统计出错，mappedReadsNum:" + mappedReadsNum + " unmappedReadsNum:" + unmappedReadsNum + " allReadsNum:" + allReadsNum );
			}
			unmappedReadsNum = allReadsNum - mappedReadsNum;
		}
		
		uniqMappedReadsNum = Math.round(uniqMappedReadsNum);
		repeatMappedReadsNum = Math.round(repeatMappedReadsNum);
		if (mappedReadsNum != uniqMappedReadsNum + repeatMappedReadsNum) {
			if (Math.abs(mappedReadsNum - uniqMappedReadsNum - repeatMappedReadsNum) > 10) {
				logger.error("统计出错，mappedReadsNum:" + mappedReadsNum + " uniqMappedReadsNum:" + uniqMappedReadsNum + " repeatMappedReadsNum:" + repeatMappedReadsNum );
			}
			repeatMappedReadsNum = mappedReadsNum - uniqMappedReadsNum;
		}
		
		junctionAllReads = Math.round(junctionAllReads);
		junctionUniReads = Math.round(junctionUniReads);
		for (double[] readsNum : mapChrID2ReadsNum.values()) {
			readsNum[0] = Math.round(readsNum[0]);
		}
	}
	/** 因为用double来统计reads数量，
	 * 所以最后所有染色体上的reads之
	 * 和与总reads数相比会有一点点的差距 */
	private void modifyChrReadsNum() {
		long numAllChrReads = 0;
		for (double[] readsNum : mapChrID2ReadsNum.values()) {
			numAllChrReads += (long)readsNum[0];
		}
		long numLess = (long)allReadsNum - numAllChrReads;
		long numAddAVG = numLess/mapChrID2ReadsNum.size();
		long numAddSub = numLess%mapChrID2ReadsNum.size();
		for (double[] readsNum : mapChrID2ReadsNum.values()) {
			readsNum[0] = readsNum[0] + numAddAVG;
			if (numAddSub > 0) {
				readsNum[0] = readsNum[0] + 1;
				numAddSub --;
			}
		}
	}
	/**
	 * 取得最终的表格数据
	 * @return
	 */
	public Map<String, List<String[]>> getMapSheetName2Data() {
		getMapChrID2PropAndLen();
		List<String[]> lsSamTable = writeSamTable();
		List<String[]> lsChrReports = writeChrInfoTable();
		Map<String, List<String[]>> mapSheetName2Data = new LinkedHashMap<String, List<String[]>>();
		mapSheetName2Data.put("statisticsTerm", lsSamTable);
		mapSheetName2Data.put("chrDistribution", lsChrReports);
		return mapSheetName2Data;
	}
	
	/**
	 * 返回reads的分布情况统计
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 * key: chrID<br>
	 * value: double[4] 0: readsNum 1: readsProp 2: chrLen 3: chrProp
	 */
	public HashMap<String, double[]> getMapChrID2PropAndLen() {
		if (mapChrID2LenProp != null) {
			return mapChrID2LenProp;
		}
		mapChrID2LenProp = new LinkedHashMap<String, double[]>();
		Map<String, Long> resultData = getMapChrID2Len();
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
		return mapChrID2LenProp;
	}
	
	/** 写每条染色体上 reads的覆盖度等数据 */
	private List<String[]> writeChrInfoTable() {
		List<String[]> lsTable = new ArrayList<String[]>();
		try {
			lsTable.add(new String[]{"ChrID","MappedReadsNum","MappedReadsProp","ChrLen","ChrLenProp"});
			for (String chrID : mapChrID2LenProp.keySet()) {
				String[] info = new String[5];
				info[0] = chrID;
				double[] resultTmp = mapChrID2LenProp.get(chrID);
				info[1] = (long)resultTmp[0] + "";
				info[2] = resultTmp[1] + "";
				info[3] = (long)resultTmp[2] + "";
				info[4] = resultTmp[3] + "";
				lsTable.add(info);
			}
		} catch (Exception e) {
			logger.error("mapping生成表格出错啦！");
			return new ArrayList<String[]>();
		}
		return lsTable;
	}
	
	/** 写junction reads等统计数据 */
	private List<String[]> writeSamTable() {
		List<String[]> lsTable = new ArrayList<String[]>();
		try {
			lsTable.add(new String[] { "Statistics Term", "Result(" + FileOperate.getFileNameSep(samFile.getFileName())[0] + ")" });
			long allReads = getReadsNum(MappingReadsType.allReads);
			long unMapped = getReadsNum(MappingReadsType.unMapped);
			long allMappedReads = getReadsNum(MappingReadsType.allMappedReads);
			long uniqueMapping = getReadsNum(MappingReadsType.uniqueMapping);
			long repeatMapping = getReadsNum(MappingReadsType.repeatMapping);
			long junctionAllMappedReads = getReadsNum(MappingReadsType.junctionAllMappedReads);
			long junctionUniqueMapping = getReadsNum(MappingReadsType.junctionUniqueMapping);

			lsTable.add(new String[] { "allReads", allReads + "" });
			lsTable.add(new String[] { "unMapped", unMapped + "" });

			lsTable.add(new String[] { "allMappedReads", allMappedReads + "" });
			if (!(allMappedReads == repeatMapping && repeatMapping < 1)) {
				lsTable.add(new String[] { "uniqueMapping", uniqueMapping + "" });
				lsTable.add(new String[] { "repeatMapping", repeatMapping + "" });
			}
			if (junctionAllMappedReads != 0 && junctionUniqueMapping != 0) {
				lsTable.add(new String[] { "junctionAllMappedReads", junctionAllMappedReads + "" });
				lsTable.add(new String[] { "junctionUniqueMapping", junctionUniqueMapping + "" });
			}
		} catch (Exception e) {
			logger.error("mapping生成表格出错啦！");
			return new ArrayList<String[]>();
		}
		return lsTable;
	}
	/**
	 * 取得最终的图片流
	 * @return
	 */
	public BufferedImage getBufferedImages() {
		List<SamFileStatistics> lsSamFileStatistics = new ArrayList<SamFileStatistics>();
		lsSamFileStatistics.add(this);
		return drawMappingImage(lsSamFileStatistics);
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
	public static BufferedImage drawMappingImage(List<SamFileStatistics> lsSamFileStatistics) {
		if (lsSamFileStatistics.get(0).getMapChrID2PropAndLen().size() > chrNumMax) {
			logger.error("大于最大chr数量,不支持生成图片");
			return null;
		}
		Color barColor1 = new Color(23, 200, 200); 
		Color barColor2 = new Color(100, 100, 100);
		double[][] allData = getResultProp(lsSamFileStatistics);
		List<String> lsRowkeys = new ArrayList<String>();
		for (SamFileStatistics samFileStatistics : lsSamFileStatistics) {
			lsRowkeys.add(samFileStatistics.prefix);
		}
		lsRowkeys.add("Genome");
		String [] rowkeys = lsRowkeys.toArray(new String[lsRowkeys.size()]);
		String[] columnKeys = lsSamFileStatistics.get(0).getMapChrID2PropAndLen().keySet().toArray(new String[0]);
//		int width = 50 * (lsSamFileStatistics.get(0).getMapChrID2PropAndLen().size() * (lsSamFileStatistics.size() + 1));
//		float rate = width/1500-1;
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset(rowkeys,columnKeys, allData);
		JFreeChart chart = ChartFactory.createBarChart("Mapping Result", null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		//设置图例的位置
		LegendTitle legend = chart.getLegend();
		legend.setItemFont(new Font("宋体", Font.PLAIN, 20));
		legend.setPadding(20,20,20,20);
		legend.setPosition(RectangleEdge.RIGHT);
		legend.setMargin(0, 0, 0, 50);
		
		// 设置图标题的字体
		Font font = new Font("黑体", Font.BOLD,30);
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
		return chart.createBufferedImage(1500, 700);
	}
	
	/**
	 * 返回染色体高度的数据用于画图，染色体根据染色体编号进行排序
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 */
	private static double[][] getResultProp(List<SamFileStatistics> lsSamFileStatistics) {
		double[][] dataInfo = new double[lsSamFileStatistics.size()+1][lsSamFileStatistics.get(0).getMapChrID2PropAndLen().size()];
		
		for (int j = 0; j < lsSamFileStatistics.size(); j++) {
			int i = 0;
			for (String key : lsSamFileStatistics.get(0).getMapChrID2PropAndLen().keySet()) {
				dataInfo[j][i] = lsSamFileStatistics.get(j).getMapChrID2PropAndLen().get(key)[1];
				i++;
			}
		}
		int k = 0;
		for (String key : lsSamFileStatistics.get(0).getMapChrID2PropAndLen().keySet()) {
			dataInfo[lsSamFileStatistics.size()][k] = lsSamFileStatistics.get(0).getMapChrID2PropAndLen().get(key)[3];
			k++;
		}
		return dataInfo;
	}
	
	public static void saveInfo(String pathAndName, SamFileStatistics samFileStatistics) {
		String pathChrPic = "";
		if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
			pathChrPic = pathAndName + "ChrDistribution.png";
		} else {
			pathChrPic = FileOperate.changeFilePrefix(pathAndName, "ChrDistribution_", "png");
		}
		ImageUtils.saveBufferedImage(samFileStatistics.getBufferedImages(), pathChrPic);
		
		ExcelOperate excelOperate = new ExcelOperate(FileOperate.changeFileSuffix(pathAndName, "_MappingStatistic", "xls"));
		Map<String, List<String[]>> mapSheetName2Info = samFileStatistics.getMapSheetName2Data();
		for (String sheetName : mapSheetName2Info.keySet()) {
			excelOperate.WriteExcel(sheetName, 1, 1, mapSheetName2Info.get(sheetName));
		}
		excelOperate.Close();
	}
}
