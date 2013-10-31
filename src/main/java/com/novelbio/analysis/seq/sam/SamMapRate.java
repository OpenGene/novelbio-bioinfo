package com.novelbio.analysis.seq.sam;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.generalConf.TitleFormatNBC;

/** 级联的mapping率统计
 * 譬如要做4-5个级联mapping，先做第一个，mapping完了将没有mapping上的比对第二个，
 * 依次下去，统计每一个的mapping率和总mapping率
 * @author zong0jie
 *
 */
public class SamMapRate {
	GeneExpTable geneExpTable = new GeneExpTable("KindOfSequence");
	
	/**
	 * @param refName 比对到序列的名称，显示时使用
	 * @param samFileStatistics
	 */
	public void addMapInfo(String refName, SamFileStatistics samFileStatistics) {
		geneExpTable.addGeneExp(refName, samFileStatistics.get);
	}
	
}
