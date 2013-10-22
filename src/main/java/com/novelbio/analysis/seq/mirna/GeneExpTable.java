package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 将一系列基因表达的表整理到一起，方便归类等工作
 * @author zong0jie
 */
public class GeneExpTable {
	String tableName;
	String geneTitleName;
	/** 基因annotation的title */
	List<String> lsGeneAnnoTitle = new ArrayList<>();
	List<String> lsGeneName = new ArrayList<>();
	/** 基因名和注释的对照表 */
	ArrayListMultimap<String, String> mapGene2Anno = ArrayListMultimap.create();
	/** 时期信息 */
	List<String> lsCondition = new ArrayList<>();
	/** 具体存储表达信息的表 */
	Map<String, Map<String, Double>> mapGene_2_Cond2Exp = new HashMap<>();
	
	/** 基因长度 */
	Map<String, Integer> mapGene2Len;
	/** 某个时期的全体reads信息，用来做标准化 */
	Map<String, Long> mapCondition2Reads;
	
	/**
	 * @param tableName  该表的名字
	 * @param geneAccIDName 表示AccID一列的具体名字，譬如symbol或者miRNAname等
	 */
	public GeneExpTable(String tableName, TitleFormatNBC geneAccIDName) {
		this.tableName = tableName;
		this.geneTitleName = geneAccIDName.toString();
	}
	public void addLsGeneName(Collection<String> colGeneName) {
		setLsGeneName(colGeneName);
		lsGeneName = new ArrayList<>(mapGene_2_Cond2Exp.keySet());
	}
	
	/** 最早就要设定
	 * @param mapGene2Anno 如果某个基因没有anno，也要标记为null，否则后面会出错
	 */
	public void addAnnotation(Map<String, String> mapGene2Anno) {
		for (String geneName : mapGene2Anno.keySet()) {
			String anno = mapGene2Anno.get(geneName);
			if (anno == null) anno = "";
			this.mapGene2Anno.put(geneName, anno);
		}
		setLsGeneName(mapGene2Anno.keySet());
	}
	
	/** 初始化基因列表 */
	private void setLsGeneName(Collection<String> lsGeneName) {
		for (String geneName : lsGeneName) {
			if (mapGene_2_Cond2Exp.containsKey(geneName)) continue;
			
			Map<String, Double> mapCond2Exp = new HashMap<>();
			mapGene_2_Cond2Exp.put(geneName, mapCond2Exp);
		}
	}
	
	/** 设置本时期mapping上的reads */
	public void setMapCondition2Reads(Map<String, Long> mapCondition2Reads) {
		this.mapCondition2Reads = mapCondition2Reads;
	}
	/** 设置基因长度信息 */
	public void setMapGene2Len(Map<String, Integer> mapGene2Len) {
		this.mapGene2Len = mapGene2Len;
	}
	
	/** 在添加表达信息之前，先添加 {@link #addAnnotation(Map)}*/
	public void addGeneExp(String condition, Map<String, ? extends Number> mapGene2Exp) {
		lsCondition.add(condition);
		for (String geneName : mapGene2Exp.keySet()) {
			Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
			mapCond2Exp.put(condition, mapCond2Exp.get(geneName).doubleValue());
		}
	}
	
	/**
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
		}
		return lsResult;
	}
	
	/** @return 返回每个时期的UQreads */
	private Map<String, Double> getMapCond2UQ() {
		Map<String, Double> mapGene2UQ = new HashMap<>();
		for (String condition : lsCondition) {
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
				double uq = MathComput.median(lsValues, 75);
				mapGene2UQ.put(condition, uq);
			}
		}
		return mapGene2UQ;
	}

	private List<String> getLsValue(String geneName, EnumExpression enumExpression, Map<String, Double> mapCondition2UQ) {
		List<String> lsValue = new ArrayList<>();
		Map<String, Double> mapCond2Exp = mapGene_2_Cond2Exp.get(geneName);
		for (String condition : lsCondition) {
			Double value = mapCond2Exp.get(condition);
			if (value == null) lsValue.add(0 + "");
			
			double uq = (mapCondition2UQ != null) ? mapCondition2UQ.get(condition) : 0;
			
			double allReadsNum = mapCondition2Reads.get(condition);
			int geneLen = mapGene2Len.get(geneName);
			String geneValue = getValue(enumExpression, value, allReadsNum, uq, geneLen);
			lsValue.add(geneValue);
		}
		return lsValue;
	}
	
	/** 计算单个基因的表达值 */
	public static String getValue(EnumExpression enumExpression, double readsCount, double allReadsNum, double upQuerterNum, int geneLen) {
		String resultValue = null;
		if (enumExpression == EnumExpression.Counts) {
			resultValue = (int)readsCount + "";
		} else if (enumExpression == EnumExpression.TPM) {
			resultValue = (int)(readsCount*1000000/allReadsNum) + "";
		} else if (enumExpression == EnumExpression.RPKM) {
			resultValue = readsCount*1000000*1000/allReadsNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQRPKM) {
			resultValue = readsCount*100*1000/upQuerterNum/geneLen + "";
		} else if (enumExpression == EnumExpression.UQPM) {
			resultValue = readsCount*10/upQuerterNum + "";
		}
		return resultValue;
	}
	
	private String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneTitleName);
		for (String annoTitle : lsGeneAnnoTitle) {
			lsTitle.add(annoTitle);
		}
		for (String condition : lsCondition) {
			lsTitle.add(condition);
		}
		return lsTitle.toArray(new String[0]);
	}
}
