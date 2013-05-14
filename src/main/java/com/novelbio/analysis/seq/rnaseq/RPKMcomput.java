package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 根据tophat或者其他mapping至genome的bam结果，计算基因的rpkm
 * 如果是链特异性测序，那就需要考虑方向，可以设定{@link #setConsiderStrand}
 * @author zong0jie
 */
public class RPKMcomput implements AlignmentRecorder {
	/** 默认不考虑方向 */
	boolean considerStrand = false;
	
	GffHashGene gffHashGene;
	
	/**
	 * key：基因名<br>
	 * value：<br>
	 * key:condiiton<br>
	 *         value: 0 counts<br>
	 */
	Map<String, Map<String, double[]>> mapGeneName2Cond2ReadsCounts = new HashMap<String, Map<String,double[]>>();
	/**
	 * key: geneName
	 * value: geneLength
	 */
	Map<String, Integer> mapGeneName2Length = new TreeMap<String, Integer>();
	/**
	 * key: geneName
	 * value: geneType
	 */
	Map<String, GeneType> mapGeneName2Type = new HashMap<String, GeneType>();
	
	/** 样本时期 和 样本reads num 用来算rpkm */
	Map<String, Double> mapCond2CountsNum = new LinkedHashMap<String, Double>();
	/** 设定当前condition */
	String currentCondition;
	/** 计数器，获得当前样本的总体 reads数， 用来算rpkm的 */
	double currentReadsNum = 0;
	
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		initial();
	}
	public void setGffChrAbs(GffHashGeneAbs gffHashGeneAbs) {
		this.gffHashGene = new GffHashGene(gffHashGeneAbs);
		initial();
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffHashGene = gffChrAbs.getGffHashGene();
		initial();
	}

	private void initial() {
		ArrayList<GffDetailGene> lsGffDetailGene = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String geneName = gffGeneIsoInfo.getParentGeneName();
				int isoLength = gffGeneIsoInfo.getLenExon(0);
				//获得一个基因中最长转录本的名字
				if (!mapGeneName2Length.containsKey(geneName) || mapGeneName2Length.get(geneName) < isoLength) {
					mapGeneName2Length.put(geneName, isoLength);
					mapGeneName2Type.put(geneName, gffGeneIsoInfo.getGeneType());
				}
			}
		}
	}
	
	/** 是否考虑reads方向，只有链特异性测序才使用 */
	public void setConsiderStrand(boolean considerStrand) {
		this.considerStrand = considerStrand;
	}
	
	/**
	 * 设定样本名，同时清空currentReadsNum这个计数器
	 * 区分大小写
	 * @param currentCondition
	 */
	public void setCurrentCondition(String currentCondition) {
		this.currentCondition = currentCondition;
		this.currentReadsNum = 0;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		boolean cis5to3 = alignRecord.isCis5to3();
		//TODO 待改进，如何能够更好的区分iso的表达
		List<Align> lsAligns = alignRecord.getAlignmentBlocks();
		Set<String> setGeneName = new HashSet<String>(); 
		for (Align align : lsAligns) {
			GffCodGene gffCodGene = gffHashGene.searchLocation(align.getRefID(), align.getMidSite());
			if (gffCodGene == null) {
				continue;
			}
			setGeneName.addAll(getSetGeneName(cis5to3, gffCodGene));
		}
		if (setGeneName.size() == 0) {
			return;
		}
		for (String geneName : setGeneName) {
			//同时要考虑mapping至多个位置，以及两个不同的gene Overlap
			addInMapGeneName2Cond2ReadsCounts(geneName, alignRecord.getMappedReadsWeight()*setGeneName.size());
		}
		currentReadsNum += (double)1/alignRecord.getMappedReadsWeight();
	}
	
	/**
	 * 返回落到的基因名和基因类型
	 * 没有落在基因内部就返回null
	 * @param readsCis5to3
	 * @param gffCodGene
	 * @return
	 */
	private Set<String> getSetGeneName(boolean readsCis5to3, GffCodGene gffCodGene) {
		//判定是否落在exon里面，落在intron里面不算
		if (!gffCodGene.isInsideLoc()) {
			return new HashSet<String>();
		}
		
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		Set<String> setGeneName = new HashSet<String>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOC_EXON) {
				
				if (!considerStrand || (considerStrand && readsCis5to3 == gffGeneIsoInfo.isCis5to3())) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
				}
			}
		}
		return setGeneName;
	}
	
	/**
	 * @param geneName 基因名
	 * @param mapNum 如果是非unique mapping，那么mapping到几个位置上
	 */
	private void addInMapGeneName2Cond2ReadsCounts(String geneName, int mapNum) {
		Map<String, double[]>mapCond2ReadsCounts = null;
		if (mapGeneName2Cond2ReadsCounts.containsKey(geneName)) {
			mapCond2ReadsCounts = mapGeneName2Cond2ReadsCounts.get(geneName);
		} else {
			mapCond2ReadsCounts = new LinkedHashMap<String, double[]>();
			mapGeneName2Cond2ReadsCounts.put(geneName, mapCond2ReadsCounts);
		}
		
		double[] countsNum = null;
		if (mapCond2ReadsCounts.containsKey(currentCondition)) {
			countsNum = mapCond2ReadsCounts.get(currentCondition);
		} else {
			countsNum = new double[1];
			mapCond2ReadsCounts.put(currentCondition, countsNum);
		}
		countsNum[0] += (double)1/mapNum;
	}
	
	@Override
	public void summary() {
		mapCond2CountsNum.put(currentCondition, currentReadsNum);
	}
	
	/** 返回计算得到的rpm值 */
	public ArrayList<String[]> getLsTPMs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = ArrayOperate.getArrayListKey(mapCond2CountsNum);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsConditions.add(1, TitleFormatNBC.GeneType.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		lsConditions.remove(0);
		lsConditions.remove(0);
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] readsCounts = mapCond2Counts.get(conditions);
					if (readsCounts == null) {
						lsTmpResult.add(0 + "");
					} else {
						lsTmpResult.add(readsCounts[0]*1000000/mapCond2CountsNum.get(conditions) + "");
					}
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public ArrayList<String[]> getLsCounts() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = ArrayOperate.getArrayListKey(mapCond2CountsNum);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsConditions.add(1, TitleFormatNBC.GeneType.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		lsConditions.remove(0);
		lsConditions.remove(0);
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] readsCounts = mapCond2Counts.get(conditions);
					if (readsCounts == null) {
						lsTmpResult.add(0 + "");
					} else {
						lsTmpResult.add((int)readsCounts[0]+ "");
					}
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public ArrayList<String[]> getLsRPKMs() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = ArrayOperate.getArrayListKey(mapCond2CountsNum);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsConditions.add(1, TitleFormatNBC.GeneType.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		lsConditions.remove(0);
		lsConditions.remove(0);
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] readsCounts = mapCond2Counts.get(conditions);
					if (readsCounts == null) {
						lsTmpResult.add(0 + "");
					} else {
						lsTmpResult.add(readsCounts[0]*1000000*1000/mapCond2CountsNum.get(conditions)/mapGeneName2Length.get(geneName) + "");
					}
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	
	/** 返回当前时期的rpm值 */
	public ArrayList<String[]> getLsTPMsCurrent() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{TitleFormatNBC.GeneName.toString(), TitleFormatNBC.GeneType.toString(), currentCondition});
		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			if (mapCond2Counts == null) {
				lsTmpResult.add(0 + "");
			} else {
				double[] readsCounts = mapCond2Counts.get(currentCondition);
				if (readsCounts == null) {
					lsTmpResult.add(0 + "");
				} else {
					lsTmpResult.add(readsCounts[0]*1000000/mapCond2CountsNum.get(currentCondition) + "");
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public ArrayList<String[]> getLsCountsCurrent() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{TitleFormatNBC.GeneName.toString(), TitleFormatNBC.GeneType.toString(), currentCondition});

		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			if (mapCond2Counts == null) {
				lsTmpResult.add(0 + "");
			} else {
				double[] readsCounts = mapCond2Counts.get(currentCondition);
				if (readsCounts == null) {
					lsTmpResult.add(0 + "");
				} else {
					lsTmpResult.add((int)readsCounts[0]+ "");
				}
			}
			
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public ArrayList<String[]> getLsRPKMsCurrent() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{TitleFormatNBC.GeneName.toString(), TitleFormatNBC.GeneType.toString(), currentCondition});

		for (String geneName : mapGeneName2Length.keySet()) {
			ArrayList<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			if (mapCond2Counts == null) {
				lsTmpResult.add(0 + "");
			} else {
				double[] readsCounts = mapCond2Counts.get(currentCondition);
				if (readsCounts == null) {
					lsTmpResult.add(0 + "");
				} else {
					lsTmpResult.add(readsCounts[0]*1000000*1000/mapCond2CountsNum.get(currentCondition)/mapGeneName2Length.get(geneName) + "");
				}
			}
			
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFile(String fileNamePrefix) {
		TxtReadandWrite txtWriteTpm = new TxtReadandWrite(fileNamePrefix + "_TPM", true);
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(fileNamePrefix + "_RawCounts", true);
		TxtReadandWrite txtWriteRPKM = new TxtReadandWrite(fileNamePrefix + "_RPKM", true);
		txtWriteCounts.ExcelWrite(getLsCounts());
		txtWriteRPKM.ExcelWrite(getLsRPKMs());
		txtWriteTpm.ExcelWrite(getLsTPMs());
		txtWriteCounts.close();
		txtWriteRPKM.close();
		txtWriteTpm.close();
	}
	
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFileCurrent(String fileNamePrefix) {
		TxtReadandWrite txtWriteTpm = new TxtReadandWrite(fileNamePrefix + currentCondition + "_TPM", true);
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(fileNamePrefix + currentCondition + "_RawCounts", true);
		TxtReadandWrite txtWriteRPKM = new TxtReadandWrite(fileNamePrefix + currentCondition + "_RPKM", true);
		txtWriteCounts.ExcelWrite(getLsCountsCurrent());
		txtWriteRPKM.ExcelWrite(getLsRPKMsCurrent());
		txtWriteTpm.ExcelWrite(getLsTPMsCurrent());
		txtWriteCounts.close();
		txtWriteRPKM.close();
		txtWriteTpm.close();
	}
}
