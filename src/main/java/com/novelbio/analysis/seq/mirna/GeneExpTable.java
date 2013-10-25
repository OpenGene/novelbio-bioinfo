package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 将一系列基因表达的表整理到一起，方便归类等工作
 * @author zong0jie
 */
public class GeneExpTable {
	String geneTitleName;
	/** 基因annotation的title */
	List<String> lsGeneAnnoTitle = new ArrayList<>();
	List<String> lsGeneName = new ArrayList<>();
	/** 基因名和注释的对照表 */
	ArrayListMultimap<String, String> mapGene2Anno = ArrayListMultimap.create();
	/** 时期信息 */
	Set<String> setCondition = new LinkedHashSet<>();
	/** 具体存储表达信息的表 */
	Map<String, Map<String, Double>> mapGene_2_Cond2Exp = new LinkedHashMap<>();
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
	/**  最早就要设定 */
	public void addLsGeneName(Collection<String> colGeneName) {
		setLsGeneName(colGeneName);
		lsGeneName = new ArrayList<>(mapGene_2_Cond2Exp.keySet());
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
		setCondition.add(currentCondition);
	}
	public String getCurrentCondition() {
		return currentCondition;
	}
	/**
	 * @param mapGene2Anno 如果某个基因没有anno，也要标记为null，否则后面会出错
	 */
	public void addAnnotation(Map<String, String> mapGene2Anno) {
		for (String geneName : mapGene2Anno.keySet()) {
			String anno = mapGene2Anno.get(geneName);
			if (anno == null) anno = "";
			this.mapGene2Anno.put(geneName, anno);
		}
	}
	
	/** 初始化基因列表 */
	private void setLsGeneName(Collection<String> lsGeneName) {
		for (String geneName : lsGeneName) {
			if (mapGene_2_Cond2Exp.containsKey(geneName)) continue;
			
			Map<String, Double> mapCond2Exp = new HashMap<>();
			mapGene_2_Cond2Exp.put(geneName, mapCond2Exp);
		}
	}
	
	/** 设置当前时期所有mapping上的reads */
	public void addAllReads(long allReads) {
		if (mapCond2AllReads.containsKey(currentCondition)) {
			allReads += mapCond2AllReads.get(currentCondition);
		}
		mapCond2AllReads.put(currentCondition, allReads);
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
	 * 设定某个时期的全体表达值
	 * 在添加表达信息之前，先添加 {@link #addLsGeneName(Map)}*/
	public void addGeneExp(Map<String, ? extends Number> mapGene2Exp) {
		for (String geneName : mapGene2Exp.keySet()) {
			Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
			mapCond2Exp.put(currentCondition, mapCond2Exp.get(geneName).doubleValue());
		}
	}
	/** 在添加表达信息之前，先添加 {@link #addLsGeneName(Map)}
	 * @param condition
	 * @param geneName
	 * @param value 本基因需要加上的值
	 */
	public void addGeneExp(String geneName, double value) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
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
		List<String[]> lsResult = new ArrayList<>();
		lsResult.add(getCurrentTitle());
		double uq = 0;
		if (enumExpression == EnumExpression.UQPM || enumExpression == EnumExpression.UQRPKM) {
			uq = getUQ(currentCondition);
		}
		for (String geneName : lsGeneName) {
			List<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			if (!mapGene2Anno.isEmpty()) {
				lsTmpResult.addAll(mapGene2Anno.get(geneName));
			}
			
			lsTmpResult.add(getValue(geneName, enumExpression, uq));
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/**
	 * 获得全体时期的表达情况
	 * @param enumExpression
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public List<String[]> getLsCond2CountsNum(EnumExpression enumExpression) {
		List<String[]> lsResult = new ArrayList<>();
		lsResult.add(getTitle());
		Map<String, Double> mapCondition2UQ = null; 
		if (enumExpression == EnumExpression.UQPM || enumExpression == EnumExpression.UQRPKM) {
			mapCondition2UQ = getMapCond2UQ();
		}
		for (String geneName : lsGeneName) {
			List<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			if (!mapGene2Anno.isEmpty()) {
				lsTmpResult.addAll(mapGene2Anno.get(geneName));
			}
			
			lsTmpResult.addAll(getLsValue(geneName, enumExpression, mapCondition2UQ));
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/** @return 返回每个时期的UQreads */
	private Map<String, Double> getMapCond2UQ() {
		Map<String, Double> mapGene2UQ = new HashMap<>();
		for (String condition : setCondition) {
			mapGene2UQ.put(condition, getUQ(condition));
		}
		return mapGene2UQ;
	}
	
	/** @return 返回指定时期的UQreads */
	private double getUQ(String condition) {
		List<Double> lsValues = new ArrayList<>();
		for (String geneName : lsGeneName) {
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
	
	/** 获得全体时期的基因表达情况 */
	private List<String> getLsValue(String geneName, EnumExpression enumExpression, Map<String, Double> mapCondition2UQ) {
		List<String> lsValue = new ArrayList<>();
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		for (String condition : setCondition) {
			Double value = mapCond2Exp.get(condition);			
			double uq = (mapCondition2UQ != null) ? mapCondition2UQ.get(condition) : 0;
			
			double allReadsNum = mapCond2AllReads.get(condition);
			int geneLen = mapGene2Len.get(geneName);
			String geneValue = getValue(enumExpression, value, allReadsNum, uq, geneLen);
			lsValue.add(geneValue);
		}
		return lsValue;
	}
	
	/**
	 * 获得单个时期的基因表达情况
	 * @param geneName
	 * @param enumExpression
	 * @return
	 */
	private String getValue(String geneName, EnumExpression enumExpression, double uq) {
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		Double value = mapCond2Exp.get(currentCondition);
		double allReadsNum = mapCond2AllReads.get(currentCondition);
		int geneLen = mapGene2Len.get(geneName);
		String geneValue = getValue(enumExpression, value, allReadsNum, uq, geneLen);
		return geneValue;
	}
	
	/** 计算单个基因的表达值 */
	private String getValue(EnumExpression enumExpression, Double readsCount, double allReadsNum, double upQuerterNum, int geneLen) {
		String resultValue = null;
		if (readsCount == null) {
			return "0";
		}
		if (enumExpression == EnumExpression.Counts) {
			resultValue = readsCount.intValue() + "";
		} else if (enumExpression == EnumExpression.TPM) {
			resultValue = (int)(readsCount*mapreadsNum/allReadsNum) + "";
		} else if (enumExpression == EnumExpression.RPKM) {
			resultValue = readsCount*mapreadsNum*1000/allReadsNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQRPKM) {
			resultValue = readsCount*geneExp*1000/upQuerterNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQPM) {
			resultValue = readsCount*geneExp/upQuerterNum + "";
		}
		return resultValue;
	}
	
	private String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		for (String annoTitle : lsGeneAnnoTitle) {
			lsTitle.add(annoTitle);
		}
		for (String condition : setCondition) {
			lsTitle.add(condition);
		}
		return lsTitle.toArray(new String[0]);
	}
	
	private String[] getCurrentTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		for (String annoTitle : lsGeneAnnoTitle) {
			lsTitle.add(annoTitle);
		}
		lsTitle.add(currentCondition);
		return lsTitle.toArray(new String[0]);
	}
}
