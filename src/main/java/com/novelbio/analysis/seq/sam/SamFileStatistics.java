package com.novelbio.analysis.seq.sam;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.fasta.ChrSeqHash.CompareChrID;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.base.plot.PlotBar;

/** <>仅用于分析sambam文件<>
 * 根据需求判定是否需要执行{@link #initial()}
 *  */
public class SamFileStatistics implements AlignmentRecorder {
	private static final Logger logger = LoggerFactory.getLogger(SamFileStatistics.class);
	/** 写入文本中InsertSize的Item  */
	private static final String INSERTSIZE = "InsertSize";
	private static final String title = "Statistics";
	
	GeneExpTable expStatistics;
	GeneExpTable expChrDist;
	
	double allReadsNum = 0;
	double allReadsBase = 0;
	
	double mappingRate = 0;
	double uniqueMappingRate = 0;
	
	int insertSize = 0;
	double insertSizeAll = 0;
	double insertNum = 0;
	
	/** 用于画图和生成表格的参数, key是真实的ChrID */
	HashMap<String, double[]> mapChrID2LenProp = null;
	/** standardData 染色体长度的map, key是真实的ChrID */
	Map<String, Long> standardData;
	/** 超过50条染色体就不画这个图了 */
	private static int chrNumMax = 50;
	String prefix = "";
	/** 是否经过了计算，也就是有东西了 */
	boolean isCalculated = false;
	/**
	 * 由于非unique mapped reads的存在，为了精确统计reads在染色体上的分布，每个染色体上的reads数量用double来记数<br>
	 * 这样如果一个reads在bam文本中出现多次--也就是mapping至多个位置，就会将每个记录(reads)除以其mapping number,<br>
	 * 从而变成一个小数，然后加到染色体上。
	 * 
	 *  因为用double来统计reads数量，所以最后所有染色体上的reads之和与总reads数相比会有一点点的差距<br>
	 * 选择correct就会将这个误差消除。意思就是将所有染色体上的reads凑出总reads的数量。<br>
	 * 算法是  每条染色体reads(结果) = 每条染色体reads数量(原始)  + (总mapped reads数 - 染色体总reads数)/染色体数量<p>
	 * 
	 *  Because change double to long will lose some accuracy, for example double 1.2 convert to int will be 1,<br> 
	 *   so the result "All Chr Reads Number" will not equal to "All Map Reads Number",
		so we make a correction here.
	 */
	@Deprecated
	boolean correctChrReadsNum = false;
	
//	Map<String, double[]> mapChrID2ReadsNum = new HashMap<>();
	
	public SamFileStatistics(String prefix) {
		this.prefix = prefix;
		initial();
	}
	public String getPrefix() {
		return prefix;
	}
	/** 是否经过了计算，也就是有东西了 */
	public boolean isCalculated() {
		return isCalculated;
	}
	/**
	 * 由于非unique mapped reads的存在，为了精确统计reads在染色体上的分布，每个染色体上的reads数量用double来记数<br>
	 * 这样如果一个reads在bam文本中出现多次--也就是mapping至多个位置，就会将每个记录(reads)除以其mapping number,<br>
	 * 从而变成一个小数，然后加到染色体上。
	 * 
	 *  因为用double来统计reads数量，所以最后所有染色体上的reads之和与总reads数相比会有一点点的差距<br>
	 * 选择correct就会将这个误差消除。意思就是将所有染色体上的reads凑出总reads的数量。<br>
	 * 算法是  每条染色体reads(结果) = 每条染色体reads数量(原始)  + (总mapped reads数 - 染色体总reads数)/染色体数量<p>
	 * 
	 *  Because change double to long will lose some accuracy, for example double 1.2 convert to int will be 1,<br> 
	 *   so the result "All Chr Reads Number" will not equal to "All Map Reads Number",
		so we make a correction here.
	 */
	@Deprecated
	public void setCorrectChrReadsNum(boolean correctChrReadsNum) {
		this.correctChrReadsNum = correctChrReadsNum;
	}
	/** 染色体长度的map
	 * 无所谓大小写，应该是真实的ChrID
	 * @param standardData
	 */
	public void setStandardData(Map<String, Long> standardData) {
		this.standardData = standardData;
		expChrDist.addLsGeneName(standardData.keySet());
	}
	/**
	 * 返回readsNum
	 * @param mappingType MAPPING_ALLREADS等，注意不要重这个方法获得rate
	 * @return -1表示错误
	 */
	public long getReadsNum(MappingReadsType mappingType) {
		Double value = getReadsNumRaw(mappingType);
		if (value == null) {
			return 0;
		}
		return value.longValue();
	}
	/**
	 * 返回readsNum
	 * @param mappingType MAPPING_ALLREADS等，注意不要重这个方法获得rate
	 * @return -1表示错误
	 */
	private Double getReadsNumRaw(MappingReadsType mappingType) {
		if (mappingType == MappingReadsType.All) {
			return allReadsNum;
		} else if (mappingType == MappingReadsType.AllBase) {
			return allReadsBase;
		}
		return expStatistics.getGeneExp(mappingType.toString(), EnumExpression.Counts, prefix);
	}
	
	/**
	 * 返回比对比率
	 * @param mappingType mappingRate等
	 * @return -1表示错误
	 */
	public double getMappingRate(MappingReadsType mappingType) {
		if (mappingType == MappingReadsType.MappedRate) {
			mappingRate = getReadsNum(MappingReadsType.Mapped)/getReadsNum(MappingReadsType.All);
			return mappingRate;
		}
		if (mappingType == MappingReadsType.UniqueMappedRate) {
			uniqueMappingRate = getReadsNum(MappingReadsType.UniqueMapped)/getReadsNum(MappingReadsType.All);
			return uniqueMappingRate;
		}
		return -1;
	}
	/**
	 * 获取每条染色体所对应的reads数量
	 * key都为小写
	 * @return
	 */
	public Map<String, Long> getMapChrID2MappedNumber() {
		Map<String, Long> mapChrID2MappedNumber = new LinkedHashMap<String, Long>();
		List<String> lsChrID = new ArrayList<>(expChrDist.getSetGeneName());
		Collections.sort(lsChrID, new CompareChrID());
		for (String chrID : lsChrID) {
			mapChrID2MappedNumber.put(chrID.toLowerCase(), (long)expChrDist.getGeneExpRaw(chrID));
		}
		
		return mapChrID2MappedNumber;
	}
		
	/** 初始化 */
	public void initial() {
		expStatistics = new GeneExpTable("Item");
		expStatistics.setCurrentCondition(prefix);
		expStatistics.addLsGeneName(MappingReadsType.getLsReadsInfoType());
		
		for (String type : MappingReadsType.getLsReadsInfoType()) {
			expStatistics.addGeneExp(type, 0);
		}

		expChrDist = new GeneExpTable("ChrId");
		expChrDist.setCurrentCondition(prefix);
		if (standardData != null) {
			expChrDist.addLsGeneName(standardData.keySet());
		}
		mappingRate = 0;
		uniqueMappingRate = 0;
	}

	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		SamRecord samRecord = (SamRecord)alignRecord;
		if (!samRecord.isMapped()) {
			allReadsNum ++;
			double baseNum = samRecord.getLengthReal();
			allReadsBase += baseNum;
			expStatistics.addGeneExp(MappingReadsType.UnMapped.toString(), 1);
			expStatistics.addGeneExp(MappingReadsType.UnMappedBase.toString(), baseNum);
			return;
		}
		
		int readsMappedWeight = samRecord.getMappedReadsWeight();
		if (readsMappedWeight == 0) {
			readsMappedWeight = 1;
			logger.debug("reads mapped weight = 0: " + samRecord.toString());
		}
		
		if (readsMappedWeight > 1 && samRecord.getMapIndexNum() != 1) {
			return;
		}
		readsMappedWeight = 1;
		double baseNum = samRecord.getLengthReal()/readsMappedWeight;
		double readsNum = (double)1/readsMappedWeight;
		allReadsBase += baseNum;
		allReadsNum += readsNum;
		
//		mappedReadsNum = mappedReadsNum + (double)1/readsMappedWeight;
		setChrReads(readsMappedWeight, samRecord);
		if (samRecord.isUniqueMapping()) {
			expStatistics.addGeneExp(MappingReadsType.UniqueMapped.toString(), 1);
			expStatistics.addGeneExp(MappingReadsType.UniqueMappedBase.toString(), baseNum);
			if (samRecord.isJunctionCovered()) {
				expStatistics.addGeneExp(MappingReadsType.JunctionUniqueMapped.toString(), 1);
			}
		}
		else {
			expStatistics.addGeneExp(MappingReadsType.RepeatMapped.toString(), readsNum);
			expStatistics.addGeneExp(MappingReadsType.RepeatMappedBase.toString(), baseNum);
		}
		if (samRecord.isJunctionCovered()) {
			expStatistics.addGeneExp(MappingReadsType.JunctionAllMapped.toString(), readsNum);
		}
		setMateSizeInfo(samRecord);
	}

	
	private void setChrReads(int readsWeight, AlignRecord samRecord) {
		String chrID = samRecord.getRefID();
		expChrDist.addGeneExp(chrID, (double)1/readsWeight);
	}
		
	private void setMateSizeInfo(AlignRecord samRecord) {
		if (samRecord instanceof SamRecord) {
			SamRecord samRecordThis = (SamRecord)samRecord;
			if (!samRecordThis.isMapped() || !samRecordThis.isFirstRead() ||  !samRecordThis.isUniqueMapping() 
					|| !samRecordThis.isMateMapped() || !samRecordThis.getRefID().equals(samRecordThis.getMateRefID()
					)) {
				int mateStart = samRecordThis.getMateAlignmentStart();
				int insertSizeThis = 0;
				if (mateStart >= samRecordThis.getStartAbs()) {
					insertSizeThis = mateStart - samRecordThis.getStartAbs() + samRecordThis.getLength();
				} else if (mateStart < samRecordThis.getStartAbs()) {
					insertSizeThis = samRecordThis.getEndAbs() - mateStart;
				}
				if (insertSizeThis > 0) {
					insertSizeAll += insertSizeThis;
					insertNum++;
				}
				
			}
		}
	}

	@Override
	public void summary() {
		isCalculated = true;
		summeryReadsNum();
		summeryBaseNum();
		if (correctChrReadsNum) {
			expChrDist.modifyByAllReadsNum();
		}
	}
	
	/** 将所有reads数量四舍五入转变为long，同时矫正由double转换为long时候可能存在的偏差 */
	private void summeryReadsNum() {
		allReadsNum = Math.round(allReadsNum);
		long unmappedReads = Math.round(getReadsNumRaw(MappingReadsType.UnMapped));
		
		long mappedReadsNum = Math.round(allReadsNum - unmappedReads);

		//double 转换可能会有1的误差
		if (allReadsNum != mappedReadsNum + unmappedReads) {
			if (Math.abs(mappedReadsNum + unmappedReads - allReadsNum) > 10) {
				logger.error("统计出错，mappedReadsNum:" + mappedReadsNum + " unmappedReadsNum:" + unmappedReads + " allReadsNum:" + allReadsNum );
			}
			unmappedReads = (long)allReadsNum - mappedReadsNum;
		}
		expStatistics.setGeneExp(MappingReadsType.UnMapped.toString(), unmappedReads);
		expStatistics.setGeneExp(MappingReadsType.Mapped.toString(), mappedReadsNum);

		long uniqMappedReadsNum = Math.round(getReadsNumRaw(MappingReadsType.UniqueMapped));
		long repeatMappedReadsNum = Math.round(getReadsNumRaw(MappingReadsType.RepeatMapped));

		if (mappedReadsNum != uniqMappedReadsNum + repeatMappedReadsNum) {
			if (Math.abs(mappedReadsNum - uniqMappedReadsNum - repeatMappedReadsNum) > 10) {
				logger.error("统计出错，mappedReadsNum:" + mappedReadsNum + " uniqMappedReadsNum:" + uniqMappedReadsNum + " repeatMappedReadsNum:" + repeatMappedReadsNum );
			}
			repeatMappedReadsNum = mappedReadsNum - uniqMappedReadsNum;
		}
		expStatistics.setGeneExp(MappingReadsType.UniqueMapped.toString(), uniqMappedReadsNum);
		expStatistics.setGeneExp(MappingReadsType.RepeatMapped.toString(), repeatMappedReadsNum);
		
		if (insertNum > mappedReadsNum * 0.1) {
			insertSize = (int) (insertSizeAll/insertNum);
		}
	}
	
	/** 将所有reads数量四舍五入转变为long，同时矫正由double转换为long时候可能存在的偏差 */
	private void summeryBaseNum() {
		allReadsBase = Math.round(allReadsBase);
		long unmappedBase = Math.round(getReadsNumRaw(MappingReadsType.UnMappedBase));
		
		long mappedBase = Math.round(allReadsBase - unmappedBase);

		//double 转换可能会有1的误差
		if (allReadsBase != mappedBase + unmappedBase) {
			if (Math.abs(mappedBase + unmappedBase - allReadsBase) > 1000) {
				logger.error("statistic error, mappedBaseNum: {} unmappedBaseNum: {} allBaseNum: " + allReadsBase , mappedBase + "", unmappedBase+ "" );
			}
			unmappedBase = (long)allReadsBase - mappedBase;
		}
		expStatistics.setGeneExp(MappingReadsType.UnMappedBase.toString(), unmappedBase);
		expStatistics.setGeneExp(MappingReadsType.MappedBase.toString(), mappedBase);

		long uniqMappedBaseNum = Math.round(getReadsNumRaw(MappingReadsType.UniqueMappedBase));
		long repeatMappedBaseNum = Math.round(getReadsNumRaw(MappingReadsType.RepeatMappedBase));

		if (mappedBase != uniqMappedBaseNum + repeatMappedBaseNum) {
			if (Math.abs(mappedBase - uniqMappedBaseNum - repeatMappedBaseNum) > 100) {
				logger.error("statistic error, mappedReadsNum: {} uniqMappedReadsNum: {} repeatMappedReadsNum: " + repeatMappedBaseNum, mappedBase,  uniqMappedBaseNum);
			}
			repeatMappedBaseNum = mappedBase - uniqMappedBaseNum;
		}
		expStatistics.setGeneExp(MappingReadsType.UniqueMappedBase.toString(), uniqMappedBaseNum);
		expStatistics.setGeneExp(MappingReadsType.RepeatMappedBase.toString(), repeatMappedBaseNum);
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
		if (standardData == null) {
			return new HashMap<>();
		}
		mapChrID2LenProp = new LinkedHashMap<String, double[]>();
		Map<String, Long> resultData = getMapChrID2MappedNumber();
		long readsNumAll = 0, chrLenAll = 0;
		List<String> lsChrID = new ArrayList<String>(standardData.keySet());
		Collections.sort(lsChrID, new CompareChrID());
		for (String chrID : lsChrID) {
			if (resultData.containsKey(chrID.toLowerCase())) {
				readsNumAll += resultData.get(chrID.toLowerCase());
			}
			chrLenAll += standardData.get(chrID);
		}
		
		for (String chrID : lsChrID) {
			String chrIDlowcase = chrID.toLowerCase();
			double[] data = new double[4];
			data[0] = resultData.get(chrIDlowcase) ==null? 0 : resultData.get(chrIDlowcase);
			data[1] =  (double)data[0]/readsNumAll;
			data[2] = standardData.get(chrID);
			data[3] = (double)data[2]/chrLenAll;
			mapChrID2LenProp.put(chrID, data);
		}
		return mapChrID2LenProp;
	}
	
	/** 写每条染色体上 reads的覆盖度等数据 */
	private List<String[]> getChrInfoTable() {
		if(getMapChrID2PropAndLen().isEmpty()) {
			return new ArrayList<String[]>();
		}
		
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
	private List<String[]> getSamTable() {
		List<String[]> lsTable = new ArrayList<String[]>();
		java.text.DecimalFormat df =new java.text.DecimalFormat("0.000");  

		try {
			lsTable.add(new String[]{title, "Result"});
			long allReads = getReadsNum(MappingReadsType.All);
			long unMapped = getReadsNum(MappingReadsType.UnMapped);
			long allMappedReads = getReadsNum(MappingReadsType.Mapped);
			long uniqueMapping = getReadsNum(MappingReadsType.UniqueMapped);
			long repeatMapping = getReadsNum(MappingReadsType.RepeatMapped);
			long junctionAllMappedReads = getReadsNum(MappingReadsType.JunctionAllMapped);
			long junctionUniqueMapping = getReadsNum(MappingReadsType.JunctionUniqueMapped);
			
			long allBases = getReadsNum(MappingReadsType.AllBase);
			long unMappedBase = getReadsNum(MappingReadsType.UnMappedBase);
			long mappedBase = getReadsNum(MappingReadsType.MappedBase);
			long uniqueMappingBase = getReadsNum(MappingReadsType.UniqueMappedBase);
			long repeatMappingBase = getReadsNum(MappingReadsType.RepeatMappedBase);


			lsTable.add(new String[] { MappingReadsType.All.toString(), allReads + "" });
			lsTable.add(new String[] { MappingReadsType.UnMapped.toString(), unMapped + "" });
			lsTable.add(new String[] { MappingReadsType.Mapped.toString(), allMappedReads + "" });
			lsTable.add(new String[] {MappingReadsType.MappedRate.toString(), df.format((double)allMappedReads/allReads) + "" });
			if (!(allMappedReads == repeatMapping && repeatMapping < 1)) {
				lsTable.add(new String[] {MappingReadsType.UniqueMapped.toString(), uniqueMapping + "" });
				lsTable.add(new String[] {MappingReadsType.UniqueMappedRate.toString(), df.format((double)uniqueMapping/allReads) + "" });
				lsTable.add(new String[] {MappingReadsType.RepeatMapped.toString(), repeatMapping + "" });
			}
			if (junctionAllMappedReads != 0 && junctionUniqueMapping != 0) {
				lsTable.add(new String[] {MappingReadsType.JunctionAllMapped.toString(), junctionAllMappedReads + "" });
				lsTable.add(new String[] {MappingReadsType.JunctionUniqueMapped.toString(), junctionUniqueMapping + "" });
			}
			
			lsTable.add(new String[] { MappingReadsType.AllBase.toString(), allBases + "" });
			lsTable.add(new String[] { MappingReadsType.UnMappedBase.toString(), unMappedBase + "" });
			lsTable.add(new String[] { MappingReadsType.MappedBase.toString(), mappedBase + "" });
			if (!(allBases == repeatMappingBase && repeatMappingBase < 1)) {
				lsTable.add(new String[] {MappingReadsType.UniqueMappedBase.toString(), uniqueMappingBase + "" });
				lsTable.add(new String[] {MappingReadsType.RepeatMappedBase.toString(), repeatMappingBase + "" });
			}

			if (insertSize > 0 && insertSize < 1000) {
				lsTable.add(new String[] { INSERTSIZE, insertSize + "" });
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("mapping生成表格出错啦！");
			return new ArrayList<String[]>();
		}
		return lsTable;
	}
	
	/** 读取文本表格并填充本类
	 * @param pathAndName 用 {@link #saveExcel(String, SamFileStatistics)} 这个方法写入的文件名 
	 */
	public void readTableShort(String pathAndName) {
		readTable(getSaveExcel(pathAndName));
	}
	
	/** 读取文本表格并填充本类 */
	public void readTable(String samStatisticFile) {
		isCalculated = true;
		List<String> lsReadsInfo = new ArrayList<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(samStatisticFile);
		
		boolean isReads = false;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#Item")) {//TODO 不要写字符串
				isReads = true;
				prefix = content.split("\t")[1];
				continue;
			} else if (content.startsWith("###")) {
				break;
			}
			if (content.equals("") || !isReads) continue;
			
			String[] ss = content.split("\t");
			if (ss[0].equals(title)) {
				ss[1] = prefix;
				content = ArrayOperate.cmbString(ss, "\t");
			}
			if (ss[0].equals(INSERTSIZE)) {
				insertSize = Integer.parseInt(ss[1]);
			}
			lsReadsInfo.add(content);
		}
		txtRead.close();
		expStatistics.read(lsReadsInfo, EnumAddAnnoType.notAdd);
		allReadsNum = (long)expStatistics.getGeneExpRaw(MappingReadsType.All.toString());
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
		Color barColor1 = new Color(23, 200, 200); 
		Color barColor2 = new Color(100, 100, 100);
		Set<String> setChrIdUsed = getSetChrId(lsSamFileStatistics.get(0));
		
		double[][] allData = getResultProp(setChrIdUsed, lsSamFileStatistics);
		List<String> lsRowkeys = new ArrayList<String>();
		for (SamFileStatistics samFileStatistics : lsSamFileStatistics) {
			lsRowkeys.add(samFileStatistics.prefix);
		}
		lsRowkeys.add("Genome");
		String [] rowkeys = lsRowkeys.toArray(new String[lsRowkeys.size()]);
		String[] columnKeys = getColumnKey(setChrIdUsed);
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
	
	/** 获得最后需要输出的染色体id */
	private static Set<String> getSetChrId(SamFileStatistics statistics) {
		boolean isChromosome = false;
		for (String chrId : statistics.getMapChrID2MappedNumber().keySet()) {
			if (chrId.toLowerCase().startsWith("chr")) {
				isChromosome = true;
				break;
			}
		}
		
		Set<String> setChrIdUse = new LinkedHashSet<String>();
		int chrCounts = 0;
		for (String chrId : statistics.getMapChrID2PropAndLen().keySet()) {
			if (isChromosome && !chrId.toLowerCase().startsWith("ch")) {
				continue;
			}
			if (chrCounts >= chrNumMax) {
				logger.info("大于最大chr数量,只画前" + chrNumMax + "条");
				break;
			}
			setChrIdUse.add(chrId);
			chrCounts++;
		}
		return setChrIdUse;
	}
	
	/**
	 * 返回染色体高度的数据用于画图，染色体根据染色体编号进行排序
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 */
	private static double[][] getResultProp(Set<String> setChrIdUsed, List<SamFileStatistics> lsSamFileStatistics) {
		double[][] dataInfo = new double[lsSamFileStatistics.size()+1][setChrIdUsed.size()];
		for (int i = 0; i <lsSamFileStatistics.size(); i++) {
			int j = 0;
			for (String chrId : setChrIdUsed) {
				dataInfo[i][j] = lsSamFileStatistics.get(i).getMapChrID2PropAndLen().get(chrId)[1];
				j++;
			}
		}
		int j = 0;
		for (String chrId : setChrIdUsed) {
			dataInfo[lsSamFileStatistics.size()][j] = lsSamFileStatistics.get(0).getMapChrID2PropAndLen().get(chrId)[3];
			j++;
		}
		return dataInfo;
	}
	
	/**
	 * 返回染色体高度的数据用于画图，染色体根据染色体编号进行排序
	 * @param resultData 实际reads在染色体上分布的map
	 * @param standardData 染色体长度的map
	 * @return
	 */
	private static String[] getColumnKey(Set<String> setChrIdUsed) {
		String[] columnKeys = new String[setChrIdUsed.size()];
		int i = 0;
		for (String chrID : setChrIdUsed) {
			columnKeys[i] = chrID;
			i++;
		}
		return columnKeys;
	}

	
	public static String saveExcel(String pathAndName, SamFileStatistics samFileStatistics) {
		
		String excelName = getSaveExcelStatisticsOnly(pathAndName);
		TxtReadandWrite txtWriteStatistics = new TxtReadandWrite(excelName, true);
		List<String[]> lsStatistics = samFileStatistics.getSamTable();
		
		for (String[] contents : lsStatistics) {
			txtWriteStatistics.writefileln(contents);
		}
		txtWriteStatistics.close();
		
		excelName = getSaveExcel(pathAndName);
		TxtReadandWrite excelOperate = new TxtReadandWrite(excelName, true);
		excelOperate.writefileln("#Mapping_Statistics");
		excelOperate.writefileln(new String[] { "#Item", samFileStatistics.getPrefix()});
		
		for (String[] contents : samFileStatistics.getSamTable()) {
			excelOperate.writefileln(contents);
		}
		excelOperate.writefileln();
		excelOperate.writefileln("########################");
		excelOperate.writefileln();
		excelOperate.writefileln("Chr_Distribution");
		for (String[] contents : samFileStatistics.getChrInfoTable()) {
			excelOperate.writefileln(contents);
		}
		excelOperate.close();
		

		
		return excelName;
	}
	
//bll
//	public static String getAllStatisticFile() {
//		String allStatisticsFile = null;
//		
//		return allStatisticsFile;
//	}
	
	
	public static String savePic(String pathAndName, SamFileStatistics samFileStatistics) {
		String pathChrPic = getSavePic(pathAndName);
		pathChrPic = ImageUtils.saveBufferedImage(samFileStatistics.getBufferedImages(), pathChrPic);
		return pathChrPic;
	}
	/** 预判会出现的文件名 */
	public static String getSavePic(String pathAndName) {
		String pathChrPic = null;
		if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
			pathChrPic = pathAndName + "chr_distribution.png";
		} else {
			pathChrPic = pathAndName + ".chr_distribution.png";
		}
		return pathChrPic;
	}
	/** 预判会出现的文件名 */
	public static String getSaveExcel(String pathAndName) {
		String excelName = null;
		if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
			excelName = pathAndName + "mapping_statistic.xls";
		} else if(FileOperate.getFileName(pathAndName).contains("mapping_statistics")) {
			return pathAndName;
		} else {
			excelName = pathAndName + ".mapping_statistics.xls";
		}
		return excelName;
	}
	/** 预判会出现的文件名 */
	private static String getSaveExcelStatisticsOnly(String pathAndName) {
		String excelName = null;
		if (pathAndName.endsWith("/") || pathAndName.endsWith("\\")) {
			excelName = pathAndName + "mapping.statistic.xls";
		} else if(FileOperate.getFileName(pathAndName).contains("mapping_statistics")) {
			return pathAndName;
		} else {
			excelName = pathAndName + ".mapping.statistics.xls";
		}
		return excelName;
	}
//bll
//	public static String getSaveAllStaExcel() {
//		String allStaExcel = null;
//		
//		return allStaExcel;
//	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
}
