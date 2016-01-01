package com.novelbio.analysis.seq;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 将一系列基因表达的表整理到一起，方便归类等工作
 * @author zong0jie
 */
public class GeneExpTable {
	private static final Logger logger = LoggerFactory.getLogger(GeneExpTable.class);
	
	public enum EnumAddAnnoType {
		/** 不添加anno，如果第一次anno已经都加好了就可以用这个 */
		notAdd,
		/** 将全部anno都添加进去，当遇到一个全新的文本可以这么做 */
		addAll,
		/** 如果发现已经存在的geneName，则跳过；如果发现全新的geneName，就把该Anno添加进去 */
		addNew
	}
	String geneTitleName;
	/** 基因annotation的title */
	Set<String> setGeneAnnoTitle = new LinkedHashSet<>();
	/** 基因名和注释的对照表 */
	protected ArrayListMultimap<String, String> mapGene2Anno = ArrayListMultimap.create();
	/** 时期信息 */
	Set<String> setCondition = new LinkedHashSet<>();
	/** 具体存储表达信息的表 */
	protected Map<String, Map<String, Double>> mapGene_2_Cond2Exp = new LinkedHashMap<>();
	/** 大致测序量，譬如rna-seq就是百万级别，小RNAseq就是可能就要变成十万级别 */
	int mapreadsNum = 1000000;
	/** 单个基因的平均表达量，用于UQ的时候除以uq的数量 */
	int geneExp = 100;
	/** 基因长度 */
	Map<String, Integer> mapGene2Len;
	/** 某个时期的全体reads信息，用来做标准化 */
	Map<String, Long> mapCond2AllReads = new HashMap<>();
	String currentCondition;
	/**
	 * @param geneAccIDName 表示AccID一列的具体名字，譬如symbol或者miRNAname等
	 */
	public GeneExpTable(TitleFormatNBC geneAccIDName) {
		this.geneTitleName = geneAccIDName.toString();
	}
	public GeneExpTable(String geneAccIDName) {
		this.geneTitleName = geneAccIDName;
	}

	
	/**
	 * <b>添加前需要指定condition</b>
	 * 
	 *  添加counts文本，将其加入mapGene_2_Cond2Exp中
	 * 同时添加注释信息并设定allCountsNumber为全体reads的累加
	 *
	 * @param file 读取的文件
	 * @param enumAddAnnoType 是否添加注释，如果本对象已经有了注释，就不可以添加了，否则会出错
	 */
	public void read(String file, EnumAddAnnoType enumAddAnnoType) {
		TxtReadandWrite txtRead = new TxtReadandWrite(file);
		List<String> lsFirst3Lines = txtRead.readFirstLines(3);
		Map<Integer, String> mapCol2Sample = getMapCol2Sample(lsFirst3Lines);
		txtRead.close();
		read(file, enumAddAnnoType, mapCol2Sample);
	}
	
	public void read(String file, EnumAddAnnoType addAnno, Map<Integer, String> mapCol2Sample) {
		logger.info("file is " + file);
		TxtReadandWrite txtRead = new TxtReadandWrite(file);
		if (mapCol2Sample == null) {
			txtRead.close();
			return;
		}
		for (String string : mapCol2Sample.values()) {
			setCondition.add(string);
		}
		String[] title = txtRead.readFirstLine().split("\t");
		geneTitleName = title[0];
		if (addAnno != EnumAddAnnoType.notAdd) {
			setLsAnnoTitle(title, mapCol2Sample.keySet());
		}
	
		for (String content : txtRead.readlines(2)) {
			if (content.trim().equals("")) {
				continue;
			}
			addValue(content.split("\t"), mapCol2Sample, addAnno);
		}
		setAllreadsPerConditon();
		txtRead.close();
	}
	
	/**
	 * <b>添加前需要指定condition</b>
	 * 
	 *  添加counts文本，将其加入mapGene_2_Cond2Exp中
	 * 同时添加注释信息并设定allCountsNumber为全体reads的累加
	 *
	 * @param file 读取的文件
	 * @param addAnno 是否添加注释，如果本对象已经有了注释，就不可以添加了，否则会出错
	 */
	public void read(List<String> lsInfo, EnumAddAnnoType addAnno) {
		List<String> lsTitles = new ArrayList<>();
		for (int i = 0; i < Math.min(3, lsInfo.size()); i++) {
			lsTitles.add(lsInfo.get(i));
		}
		if (lsTitles.size() < 3) {
			lsTitles.add(lsTitles.get(1));
		}
		Map<Integer, String> mapCol2Sample = getMapCol2Sample(lsTitles);
		read(lsInfo, addAnno, mapCol2Sample);
	}
	
	/**
	 * 
	 * @param lsInfo 第一行是title
	 * @param addAnno
	 * @param mapCol2Sample
	 */
	public void read(List<String> lsInfo, EnumAddAnnoType addAnno, Map<Integer, String> mapCol2Sample) {
		if (mapCol2Sample == null) {
			return;
		}
		for (String string : mapCol2Sample.values()) {
			setCondition.add(string);
		}
		String[] title = lsInfo.get(0).split("\t");
		if (title[0].startsWith("#")) {
			title[0] = title[0].replaceFirst("#", "");
		}
		
		
		geneTitleName = title[0];
		if (addAnno != EnumAddAnnoType.notAdd) {
			setLsAnnoTitle(title, mapCol2Sample.keySet());
		}
		for (int m = 1; m < lsInfo.size(); m++) {
			String content = lsInfo.get(m);
			if (content.trim().equals("")) {
				continue;
			}
			addValue(content.split("\t"), mapCol2Sample, addAnno);
		}
		setAllreadsPerConditon();   //bll
	}
	
	/**
	 * @param lsFirst3Lines 前三行
	 * @return 从0开始计算的col --- sampleName
	 */
	private Map<Integer, String> getMapCol2Sample(List<String> lsFirst3Lines) {
		if (lsFirst3Lines.size() < 2) {
			return null;
		}
		Map<Integer, String> mapCol2Sample = new LinkedHashMap<>();
		String[] titleArray = lsFirst3Lines.get(0).split("\t");
		String[] dataArray1 = lsFirst3Lines.get(1).split("\t");
		String[] dataArray2 = null;
		if (lsFirst3Lines.size() > 2) {
			dataArray2 = lsFirst3Lines.get(2).split("\t");
		}
		for (int i = 1; i < dataArray1.length; i++) {
			try {
				Double.parseDouble(dataArray1[i]);
				if (dataArray2 != null) {
					Double.parseDouble(dataArray2[i]);
				}
//				System.out.println( i +"*********\t" + titleArray[i]);
				mapCol2Sample.put(i, titleArray[i]);
			} catch (Exception e) {
			}
		}
		return mapCol2Sample;
	}
	
	private void setLsAnnoTitle(String[] title, Set<Integer> colCol) {
		if (setGeneAnnoTitle.size() > 0) {
			return;
		}
		for (int i = 1; i < title.length; i++) {
			if (colCol.contains(i)) {
				continue;
			}
			if (!setGeneAnnoTitle.contains(title[i])) {
				setGeneAnnoTitle.add(title[i]);
			}
		}
	}
	
	private void addValue(String[] data, Map<Integer, String> mapCol2Sample, EnumAddAnnoType enumAddAnnoType) {
		String geneName = data[0];
		Map<String, Double> mapSample2Value = null;
		if (mapGene_2_Cond2Exp.containsKey(geneName)) {
			mapSample2Value = mapGene_2_Cond2Exp.get(geneName);
		} else {
			mapSample2Value = new HashMap<>();
			mapGene_2_Cond2Exp.put(geneName, mapSample2Value);
		}
		boolean addAnno = false;
		if (enumAddAnnoType == EnumAddAnnoType.addAll 
				|| (enumAddAnnoType == EnumAddAnnoType.addNew && !mapGene2Anno.containsKey(geneName))) {
			addAnno = true;
		}
		
		for (int i = 1; i < data.length; i++) {
			if (mapCol2Sample.containsKey(i)) {
				String sampleName = mapCol2Sample.get(i);
				Double value = Double.parseDouble(data[i]);
				mapSample2Value.put(sampleName, value);
			} else {
				if (addAnno) {
					mapGene2Anno.put(geneName, data[i]);
				}
			}
		}
	}
	
	/** 返回一系列基因的名称 */
	public Set<String> getSetGeneName() {
		return mapGene_2_Cond2Exp.keySet();
	}
	
	/** 看是否存在该基因名*/
	public boolean isContainGeneName(String geneName) {
		return mapGene_2_Cond2Exp.containsKey(geneName);
	}
	
	public void addGeneName(String geneName) {
		if (mapGene_2_Cond2Exp.containsKey(geneName)) return;
		
		Map<String, Double> mapCond2Exp = new HashMap<>();
		mapGene_2_Cond2Exp.put(geneName, mapCond2Exp);
	}
	
	/**  最早就要设定 */
	public void addLsGeneName(Collection<String> colGeneName) {
		setLsGeneName(colGeneName);
	}
	/** 初始化基因列表 */
	private void setLsGeneName(Collection<String> lsGeneName) {
		for (String geneName : lsGeneName) {
			if (mapGene_2_Cond2Exp.containsKey(geneName)) continue;
			
			Map<String, Double> mapCond2Exp = new HashMap<>();
			mapGene_2_Cond2Exp.put(geneName, mapCond2Exp);
		}
	}
	/**
	 * 大致测序量，譬如rna-seq就是百万级别，小RNAseq就是可能就要变成十万级别
	 * 就是tpm和rpkm那块，要乘的测序量
	 * @param mapreadsNum 默认为1百万 
	 */
	public void setMapreadsNum(int mapreadsNum) {
		this.mapreadsNum = mapreadsNum;
	}
	/**
	 * uq基因的大致表达量，用于UQ的时候除以uq的数量
	 * 就是uq那块，要乘的测序量
	 * @param geneExp 默认为100
	 */
	public void setGeneExp(int geneExp) {
		this.geneExp = geneExp;
	}
	public void setCurrentCondition(String currentCondition) {
		this.currentCondition = currentCondition;
		if (!setCondition.contains(currentCondition)) {
			setCondition.add(currentCondition);
		}
	}
	public String getCurrentCondition() {
		return currentCondition;
	}
	/** 返回全体condition */
	public Set<String> getSetCondition() {
		return setCondition;
	}
	/** 返回全体condition和对应的allReads */
	public Map<String, Long> getMapCond2AllReads() {
		return mapCond2AllReads;
	}
	
	/**
	 * mapGene2Anno中务必含有全体geneName
	 * 已经添加过一次的Gene不能再添加第二次 
	 */
	public void addAnnotation(String geneName, String annotation) {
		if (mapGene2Anno.containsKey(geneName)) {
			throw new ExceptionNullParam("Cannot add same geneName twice");
		}
		this.mapGene2Anno.put(geneName, annotation);
	}
	/**
	 * mapGene2Anno中务必含有全体geneName
	 * 已经添加过一次的Gene不能再添加第二次 
	 */
	public void addAnnotation(String geneName, String[] annotation) {
		if (mapGene2Anno.containsKey(geneName)) {
			throw new ExceptionNullParam("Cannot add same geneName twice");
		}
		for (String string : annotation) {
			if (string == null) string = "";
			this.mapGene2Anno.put(geneName, string);
		}
	}
	/** mapGene2Anno中务必含有全体geneName
	 * key geneName
	 * value annotation
	 * @param mapGene2Anno
	 */
	public void addAnnotation(Map<String, String> mapGene2Anno) {
		for (String geneName : mapGene2Anno.keySet()) {
			String anno = mapGene2Anno.get(geneName);
			if (anno == null) anno = "";
			this.mapGene2Anno.put(geneName, anno);
		}
	}
	/** mapGene2Anno中务必含有全体geneName
	 * key geneName
	 * value annotation 是个数组，可以有好多项
	 * @param mapGene2Anno
	 */
	public void addAnnotationArray(Map<String, String[]> mapGene2Anno) {
		for (String geneName : mapGene2Anno.keySet()) {
			String[] anno = mapGene2Anno.get(geneName);
			for (String string : anno) {
				if (string == null) string = "";
				this.mapGene2Anno.put(geneName, string);
			}
		}
	}
	/** 会自动去重复 */
	public void addLsTitle(Collection<String> colTitle) {
		setGeneAnnoTitle.addAll(colTitle);
	}

	
	/** 设置当前时期所有mapping上的reads */
	public void addAllReads(double allReads) {
		if (mapCond2AllReads.containsKey(currentCondition)) {
			allReads += mapCond2AllReads.get(currentCondition);
		}
		mapCond2AllReads.put(currentCondition, (long)allReads);
	}
	/** 设置当前时期所有mapping上的reads */
	public void setAllReads(double allReads) {
		mapCond2AllReads.put(currentCondition, (long)allReads);
	}
	/** 设置基因长度信息 */
	public void setMapGene2Len(Map<String, Integer> mapGene2Len) {
		this.mapGene2Len = mapGene2Len;
	}
	
	public void addMapGene2Len(Map<String, Integer> mapGene2Len) {
		if (this.mapGene2Len == null) {
			this.mapGene2Len = mapGene2Len;
		} else {
			this.mapGene2Len.putAll(mapGene2Len);
		}
	}
	/**
	 * 累加某个时期的全体表达值
	 * 在添加表达信息之前，先添加 {@link #addLsGeneName(Map)}*/
	public void addGeneExp(Map<String, ? extends Number> mapGene2Exp) {
		for (String geneName : mapGene2Exp.keySet()) {
			addGeneExp(geneName, mapGene2Exp.get(geneName).doubleValue());
		}
	}
	/** 在添加表达信息之前，先添加 {@link #addLsGeneName(Map)}
	 * @param condition
	 * @param geneName
	 * @param value 本基因需要加上的值
	 */
	public void addGeneExp(String geneName, double value) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		if (mapCond2Exp == null) {
			throw new ExceptionNullParam(geneName + " is not exist");
		}
		if (mapCond2Exp.containsKey(currentCondition)) {
			double lastValue = mapCond2Exp.get(currentCondition);
			value += lastValue;
		}
		mapCond2Exp.put(currentCondition, value);
	}
	
	/**
	 * 获得当前时期的表达情况
	 * @param enumExpression
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public List<String[]> getLsCountsNum(EnumExpression enumExpression) {
		setConditionAllreads(currentCondition);

		List<String[]> lsResult = new ArrayList<>();
		lsResult.add(getCurrentTitle());
		double uq = 0;
		if (enumExpression == EnumExpression.UQPM || enumExpression == EnumExpression.UQRPKM) {
			uq = getUQ(currentCondition);
		}
		for (String geneName : mapGene_2_Cond2Exp.keySet()) {
			List<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			if (!mapGene2Anno.isEmpty()) {
				lsTmpResult.addAll(mapGene2Anno.get(geneName));
			}
			
			lsTmpResult.add(getValueCondition(geneName, enumExpression, uq));
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	public long getCurrentAllReads() {
		if (currentCondition == null || !mapCond2AllReads.containsKey(currentCondition)) {
			return 0;
		}
		return mapCond2AllReads.get(currentCondition);
	}
	
	public double getGeneExpRaw(String geneName) {
		return getGeneExp(geneName, EnumExpression.Counts, currentCondition);
	}
	
	/**
	 * 获得全体时期的表达情况和ratio信息，用于mapping率
	 * @param enumExpression
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public Double getGeneExp(String geneName, EnumExpression enumExpression, String condition) {
		setAllreadsPerConditon();
		Map<String, Double> mapCondition2UQ = null; 
		if (enumExpression == EnumExpression.UQPM || enumExpression == EnumExpression.UQRPKM) {
			mapCondition2UQ = getMapCond2UQ();
		}
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		if (mapCond2Exp == null) return null;
		Double value = mapCond2Exp.get(condition);
		logger.debug(geneName + "\t"+ value);
		if (value == null) return 0.0;
		
		double uq = (mapCondition2UQ != null) ? mapCondition2UQ.get(condition) : 0;
		
		Long allReadsNum = mapCond2AllReads.get(condition);
		Integer geneLen = mapGene2Len == null ? 0 : mapGene2Len.get(geneName);
		Double geneValue = getValue(enumExpression, value, allReadsNum, uq, geneLen);
		return geneValue;
	}
	/**
	 * 设定基因的表达
	 * @param geneName
	 * @param condition
	 * @param exp
	 */
	public void setGeneExp(String geneName, String condition, double exp) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		if (mapCond2Exp == null) return;
		mapCond2Exp.put(condition, exp);
	}
	/**
	 * 设定当前currentCondition下基因的表达
	 * @param geneName
	 * @param exp
	 */
	public void setGeneExp(String geneName, double exp) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		if (mapCond2Exp == null) return;
		mapCond2Exp.put(currentCondition, exp);
	}
	/**
	 * 获得全体时期的表达情况
	 * @param enumExpression
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public List<String[]> getLsAllCountsNum(EnumExpression enumExpression) {
		return getLsAllCountsNum2Ratio(false, enumExpression);
	}
	/**
	 * 获得全体时期的表达情况和ratio信息，用于mapping率
	 * @param enumExpression
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public List<String[]> getLsAllCountsNum2Ratio(EnumExpression enumExpression) {
		return getLsAllCountsNum2Ratio(true, enumExpression);
	}
	
	protected List<String[]> getLsAllCountsNum2Ratio(boolean isGetRatio, EnumExpression enumExpression) {
		setAllreadsPerConditon();

		List<String[]> lsResult = new ArrayList<>();
		if (isGetRatio) {
			lsResult.add(getTitle2Ratio());
		} else {
			lsResult.add(getTitle());
		}
		
		Map<String, Double> mapCondition2UQ = null; 
		if (enumExpression == EnumExpression.UQPM || enumExpression == EnumExpression.UQRPKM) {
			mapCondition2UQ = getMapCond2UQ();
		}
		for (String geneName : getSetGeneName()) {
			List<String> lsTmpResult = getLsGeneResult2Anno(isGetRatio, geneName, enumExpression, mapCondition2UQ);
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/** @return 返回每个时期的UQreads */
	protected Map<String, Double> getMapCond2UQ() {
		Map<String, Double> mapGene2UQ = new HashMap<>();
		for (String condition : setCondition) {
			mapGene2UQ.put(condition, getUQ(condition));
		}
		return mapGene2UQ;
	}
	
	/** @return 返回指定时期的UQreads */
	private double getUQ(String condition) {
		List<Double> lsValues = new ArrayList<>();
		for (String geneName : mapGene_2_Cond2Exp.keySet()) {
			Map<String, Double> mapCond2Counts = mapGene_2_Cond2Exp.get(geneName);
			if (mapCond2Counts == null) {
				lsValues.add(0.0);
			} else {
				Double readsCounts = mapCond2Counts.get(condition);
				if (readsCounts == null) {
					lsValues.add(0.0);
				} else {
					lsValues.add(readsCounts);
				}
			}
		}
		double uq = MathComput.median(lsValues, 75);
		return uq;
	}
	
	/** 获得全体时期的基因表达情况，ratio，以及annotation  */
	private List<String> getLsGeneResult2Anno(boolean isGetRatio, String geneName, EnumExpression enumExpression, Map<String, Double> mapCondition2UQ) {
		List<String> lsTmpResult = new ArrayList<String>();
		lsTmpResult.add(geneName);
		if (!mapGene2Anno.isEmpty()) {
			lsTmpResult.addAll(mapGene2Anno.get(geneName));
		}
		lsTmpResult.addAll(getLsValue2Ratio(isGetRatio, geneName, enumExpression, mapCondition2UQ));
			
		return lsTmpResult;
	}
	
	/** 获得全体时期的基因表达情况以及其在allreads中的比例 */
	private List<String> getLsValue2Ratio(boolean isGetRatio, String geneName, EnumExpression enumExpression, Map<String, Double> mapCondition2UQ) {
		List<String> lsValue = new ArrayList<>();
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		for (String condition : setCondition) {
			Double value = mapCond2Exp.get(condition);			
			double uq = (mapCondition2UQ != null) ? mapCondition2UQ.get(condition) : 0;
			
			Long allReadsNum = mapCond2AllReads.get(condition);
			Integer geneLen = mapGene2Len == null ? 0 : mapGene2Len.get(geneName);
			String geneValue = getValueStr(enumExpression, value, allReadsNum, uq, geneLen);
			lsValue.add(geneValue);
			if (isGetRatio) {
				lsValue.add(getValueStr(EnumExpression.Ratio, value, allReadsNum, uq, geneLen));
			}
		}
		return lsValue;
	}
		
	/** 如果allReads没有设定，则设定每个时期的allReads数量为该时期counts数加和 */
	protected void setAllreadsPerConditon() {
		for (String condition : setCondition) {
			setConditionAllreads(condition);
		}
	}
	/** 如果allReads没有设定，则设定每个时期的allReads数量为该时期counts数加和 */
	private void setConditionAllreads(String condition) {
		if (mapCond2AllReads.containsKey(condition)) {
			return;
		}
		double number = getConditionSumReads(condition);
		mapCond2AllReads.put(condition, (long) number);
	}
	
	/** 返回指定时期，全体基因表达量之和 */
	private double getConditionSumReads(String condition) {
		double number = 0;
		for (String geneName : mapGene_2_Cond2Exp.keySet()) {
			Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
			Double value = mapCond2Exp.get(condition);
			if (value == null) value = 0.0;
			number += value;
		}
		return number;
	}
	
	/**
	 * 获得单个时期的基因表达情况
	 * @param geneName
	 * @param enumExpression
	 * @return
	 */
	private String getValueCondition(String geneName, EnumExpression enumExpression, double uq) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		Double value = mapCond2Exp.get(currentCondition);
		Long allReadsNum = mapCond2AllReads.get(currentCondition);
		Integer geneLen = mapGene2Len == null ? 0 : mapGene2Len.get(geneName);
		String geneValue = getValueStr(enumExpression, value, allReadsNum, uq, geneLen);
		return geneValue;
	}
	
	/** 计算单个基因的表达值 */
	private String getValueStr(EnumExpression enumExpression, Double value, Long allReadsNum, double upQuerterNum, Integer geneLen) {
		String resultValue = null;
		if (geneLen == null) geneLen = 1000;
		if (allReadsNum == null) allReadsNum = (long) mapreadsNum;
		DecimalFormat df = new DecimalFormat("0.##"); 
		if (value == null) return "0";
		if (enumExpression == EnumExpression.RawValue) {
			resultValue = value + "";
		} else if (enumExpression == EnumExpression.Counts) {
			resultValue = value.intValue() + "";
		} else if (enumExpression == EnumExpression.TPM) {
			resultValue = value*mapreadsNum/allReadsNum + "";
		} else if (enumExpression == EnumExpression.RPKM) {
			resultValue = value*mapreadsNum*1000/allReadsNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQRPKM) {
			resultValue = value*geneExp*1000/upQuerterNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQPM) {
			resultValue = value*geneExp/upQuerterNum + "";
		} else if (enumExpression == EnumExpression.Ratio) {
			resultValue = df.format(value/allReadsNum) + "";
		}
		return resultValue;
	}
	
	/** 计算单个基因的表达值，想要什么类型如int等，自己做类型转换，<br>
	 * 如输入查找counts，返回的也可能带小数点，这时候就要自己做类型转换 */
	private Double getValue(EnumExpression enumExpression, Double value, Long allReadsNum, double upQuerterNum, Integer geneLen) {
		Double resultValue = null;
		if (geneLen == null) geneLen = 1000;
		if (allReadsNum == null) allReadsNum = (long) mapreadsNum;
		DecimalFormat df = new DecimalFormat("0.##"); 
		if (value == null) return 0.0;
		if (enumExpression == EnumExpression.RawValue) {
			resultValue = value;
		} else if (enumExpression == EnumExpression.Counts) {
			resultValue = value;
		} else if (enumExpression == EnumExpression.TPM) {
			resultValue = value*mapreadsNum/allReadsNum;
		} else if (enumExpression == EnumExpression.RPKM) {
			resultValue = value*mapreadsNum*1000/allReadsNum/geneLen;
		} else if (enumExpression == EnumExpression.UQRPKM) {
			resultValue = value*geneExp*1000/upQuerterNum/geneLen;
		} else if (enumExpression == EnumExpression.UQPM) {
			resultValue = value*geneExp/upQuerterNum;
		} else if (enumExpression == EnumExpression.Ratio) {
			resultValue = value/allReadsNum;
		}
		return resultValue;
	}
	
	protected String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		lsTitle.addAll(setGeneAnnoTitle);

		lsTitle.addAll(setCondition);
		
		return lsTitle.toArray(new String[0]);
	}
	private String[] getTitle2Ratio() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		lsTitle.addAll(setGeneAnnoTitle);
		for (String condition : setCondition) {
			lsTitle.add(condition);
			lsTitle.add(condition + "_Rate");
		}		
		return lsTitle.toArray(new String[0]);
	}
	private String[] getCurrentTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		lsTitle.addAll(setGeneAnnoTitle);

		lsTitle.add(currentCondition);
		return lsTitle.toArray(new String[0]);
	}
	
	/**
	 * 如果文件夹不存在，会新建文件夹
	 * @param writeAllCondition
	 * @param fileName
	 * @param expTable
	 * @param enumExpression
	 */
	public void writeFile(boolean writeAllCondition, String fileName, EnumExpression enumExpression) {
		List<String[]> lsValues = null;
		if (writeAllCondition) {
			lsValues = getLsAllCountsNum(enumExpression);
		} else {
			lsValues = getLsCountsNum(enumExpression);
		}
		
		if (lsValues == null || lsValues.size() <= 1) {
			return;
		}
		FileOperate.createFolders(FileOperate.getPathName(fileName));
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		txtWrite.ExcelWrite(lsValues);
		txtWrite.close();
	}
	
	/**
	 * 使用场景:<p>
	 * 由于非unique mapped reads的存在，为了精确统计reads在染色体上的分布，每个染色体上的reads数量用double来记数<br>
	 * 这样如果一个reads在bam文本中出现多次--也就是mapping至多个位置，就会将每个记录(reads)除以其mapping number,
	 * 从而变成一个小数，然后加到染色体上。<p>
	 * 
	 *  因为用double来统计reads数量，所以最后所有染色体上的reads之和与总reads数相比会有一点点的差距<br>
	 * 选择correct就会将这个误差消除。意思就是将所有染色体上的reads凑出总reads的数量。<br>
	 * 算法是  每条染色体reads(结果) = 每条染色体reads数量(原始)  + (总mapped reads数 - 染色体总reads数)/染色体数量<p>
	 * 
	 *  Because change double to long will lose some accuracy, for example double 1.2 convert to int will be 1,<br> 
	 *   so the result "All Chr Reads Number" will not equal to "All Map Reads Number",
		so we make a correction here.
	 */
	public void modifyByAllReadsNum() {
		for (String condition : mapCond2AllReads.keySet()) {
			long allReadsNum = mapCond2AllReads.get(condition);
			long allSumGeneReadsNum = (long) getConditionSumReads(condition);
			long numLess = allReadsNum - allSumGeneReadsNum;
			if (numLess > 0.0001 * allSumGeneReadsNum && numLess > 100) {
				logger.error("statistic error: GeneReadsNum:" + allSumGeneReadsNum + " is not equal to allReadsNum:"  + (long)allReadsNum);
			}
			
			//Because change double to long will lose some accuracy and the result "All Chr Reads Number" will not equal to "All Map Reads Number"
			//so we make a correction here.
			long numAddAVG = numLess/mapGene2Anno.size();
			long numAddSub = numLess%mapGene2Anno.size();
			for (Map<String, Double> mapCond2Exp : mapGene_2_Cond2Exp.values()) {
				Double expValue = mapCond2Exp.get(condition);
				if (expValue < numAddAVG * 500) {
					continue;
				}
				expValue = expValue + numAddAVG;
				if (numAddSub > 0) {
					expValue = expValue + 1;
					numAddSub --;
				}
				mapCond2Exp.put(condition, expValue);
				if (numAddAVG == 0 && numAddSub <= 0) {
					break;
				}
			}
		}
	}
}
