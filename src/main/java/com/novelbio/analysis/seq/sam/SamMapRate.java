package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.SepSign;

/** 级联的mapping率统计
 * 譬如要做4-5个级联mapping，先做第一个，mapping完了将没有mapping上的比对第二个，
 * 依次下去，统计每一个的mapping率和总mapping率
 * @author zong0jie
 *
 */
public class SamMapRate {
	GeneExpTable geneExpTable = new GeneExpTable("Reference");
	
	/** 最早就要设定 */
	public void setAllReads(String condition, long allReadsNum) {
		geneExpTable.setCurrentCondition(condition);
		geneExpTable.setAllReads(allReadsNum);
	}
	/**
	 * @param refName 比对到序列的名称，显示时使用
	 * @param samFileStatistics 其prefix就是condition
	 */
	public void addMapInfo(String refName, SamFileStatistics samFileStatistics) {
		geneExpTable.setCurrentCondition(samFileStatistics.getPrefix());
		if (geneExpTable.getCurrentAllReads() == 0) {
			geneExpTable.setAllReads(samFileStatistics.getReadsNum(MappingReadsType.allReads));
		}
		List<String> lsGene = new ArrayList<>();
		lsGene.add(refName);
		geneExpTable.addLsGeneName(lsGene);
		geneExpTable.addGeneExp(refName, samFileStatistics.getReadsNum(MappingReadsType.allMappedReads));
	}
	
	/**
	 * 添加新miRNA的mapping统计结果，将novel miRNA的统计分成有注释--比对到其他物种上，和没注释两类
	 * @param splitSymbol 用什么符号来切分mirName和mirAnnotation的，一般是@@，譬如 chr1_2345@@hsa-let-7a
	 * @param samFileStatistics 其prefix就是condition
	 */
	public void addMapInfoNovelMiRNA(String splitSymbol, SamFileStatistics samFileStatistics) {
		geneExpTable.setCurrentCondition(samFileStatistics.getPrefix());
		if (geneExpTable.getCurrentAllReads() == 0) {
			geneExpTable.setAllReads(samFileStatistics.getReadsNum(MappingReadsType.allReads));
		}
		List<String> lsGene = new ArrayList<>();
		lsGene.add("NovelMiRNA_Anno");
		lsGene.add("NovelMiRNA_NoAnno");
		geneExpTable.addLsGeneName(lsGene);
		long numberAnno = 0;
		for (String chrID : samFileStatistics.getMapChrID2MappedNumber().keySet()) {
			if (chrID.contains(splitSymbol)) {
				numberAnno += samFileStatistics.getMapChrID2MappedNumber().get(chrID);
			}
		}
		
		geneExpTable.addGeneExp("NovelMiRNA_Anno", numberAnno);
		geneExpTable.addGeneExp("NovelMiRNA_NoAnno", samFileStatistics.getReadsNum(MappingReadsType.allMappedReads) - numberAnno);
	}
	
	/** 最后一个unmap信息 */
	public void addUnmapInfo(SamFileStatistics samFileStatistics) {
		String itemName = "Unmapped";
		geneExpTable.setCurrentCondition(samFileStatistics.getPrefix());
		List<String> lsGene = new ArrayList<>();
		lsGene.add(itemName);
		geneExpTable.addLsGeneName(lsGene);
		geneExpTable.addGeneExp(itemName, samFileStatistics.getReadsNum(MappingReadsType.unMapped));
	}
	
	public List<String[]> getLsResult() {
		return geneExpTable.getLsAllCountsNum2Ratio(EnumExpression.Counts);
	}
}
