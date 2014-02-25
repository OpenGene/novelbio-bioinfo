package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.mapping.MappingReadsType;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;

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
	/** 设定当前时期 */
	public void setCurrentCondition(String condition) {
		geneExpTable.setCurrentCondition(condition);
	}
	/**
	 * 根据samFileStatistics的prefix自动设定condition，覆盖{@link #setCurrentCondition(String)}
	 * @param refName 比对到reference的名称，用于显示最后有多少reads比对到了该reference上
	 * @param samFileStatistics 其prefix就是condition
	 */
	public void addMapInfo(String refName, SamFileStatistics samFileStatistics) {
		if (samFileStatistics.getPrefix() != null && !samFileStatistics.getPrefix().equals("")) {
			geneExpTable.setCurrentCondition(samFileStatistics.getPrefix());
		}
		
		if (geneExpTable.getCurrentAllReads() == 0) {
			geneExpTable.setAllReads(samFileStatistics.getReadsNum(MappingReadsType.allReads));
		}
		List<String> lsGene = new ArrayList<>();
		lsGene.add(refName);
		geneExpTable.addLsGeneName(lsGene);
		geneExpTable.addGeneExp(refName, samFileStatistics.getReadsNum(MappingReadsType.allMappedReads));
	}
	
	public List<String[]> getLsResult() {
		return geneExpTable.getLsAllCountsNum2Ratio(EnumExpression.Counts);
	}
	
	/** 仅用于级联mapping率统计<br>
	 * 最后一个unmap信息
	 */
	public void addUnmapInfo(SamFileStatistics samFileStatistics) {
		String itemName = "Unmapped";
		geneExpTable.setCurrentCondition(samFileStatistics.getPrefix());
		List<String> lsGene = new ArrayList<>();
		lsGene.add(itemName);
		geneExpTable.addLsGeneName(lsGene);
		geneExpTable.addGeneExp(itemName, samFileStatistics.getReadsNum(MappingReadsType.unMapped));
	}
	
	/** 仅用于miRNA */
	public void setNovelMiRNAInfo() {
		List<String> lsGene = new ArrayList<>();
		lsGene.add("NovelMiRNA_Anno");
		lsGene.add("NovelMiRNA_NoAnno");
		geneExpTable.addLsGeneName(lsGene);
	}

	/**
	 * 仅用于miRNA<br>
	 * <b>注意设定{@link #setCurrentCondition(String)}</b><br>
	 * 添加新miRNA的mapping统计结果，将novel miRNA的统计分成有注释--比对到其他物种上，和没注释两类
	 * @param splitSymbol 用什么符号来切分mirName和mirAnnotation的，一般是@@，譬如 chr1_2345@@hsa-let-7a
	 * @param alignRecord 具体的mapping序列
	 */
	public void addMapInfoNovelMiRNA(String splitSymbol, String mirName, int weight) {
		if (mirName != null && mirName.contains(splitSymbol)) {
			geneExpTable.addGeneExp("NovelMiRNA_Anno", (double)1/weight);
		} else {
			geneExpTable.addGeneExp("NovelMiRNA_NoAnno", (double)1/weight);
		}
	}
}
