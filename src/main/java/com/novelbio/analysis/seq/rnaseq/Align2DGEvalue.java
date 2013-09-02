package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
/**
 * 从bam文件或bed文件转化为DGE的值
 * @author zong0jie
 *
 */
public class Align2DGEvalue {
	List<AlignSeq> lsAlignSeq;
	List<String> lsTitle;
	String resultFile;
	boolean allTags = true;
	/** dge的最短长度 */
	int dgeMinLength = 18;
	
	HashMap<String, String> mapAccID2GeneID;
	
	public void setSpecies(Species species) {
		mapAccID2GeneID = new HashMap<String, String>();
		if (species != null && species.getTaxID() != 0) {
			String gene2IsoFile = species.getGene2IsoFileFromRefSeq();
			TxtReadandWrite txtGene2Iso = new TxtReadandWrite(gene2IsoFile, false);
			for (String content : txtGene2Iso.readlines()) {
				String[] ss = content.split("\t");
				mapAccID2GeneID.put(ss[1], ss[0]);
			}
			txtGene2Iso.close();
		}
	}
	/** 设定sam，bam，或者bed文件 
	 * 这些文件必须是排过序的
	 * */
	public void setLsAlignSeq(List<AlignSeq> lsAlignSeq, List<String> lsTitle, String resultFile) {
		this.lsAlignSeq = lsAlignSeq;
		this.lsTitle = lsTitle;
		this.resultFile = resultFile;
	}
	
	/** 将输入的文件排序 */
	public void sort() {
		ArrayList<AlignSeq> lsSortedAlignSeq = new ArrayList<AlignSeq>();
		for (AlignSeq alignSeq : lsAlignSeq) {
			lsSortedAlignSeq.add(alignSeq.sort());
		}
		lsAlignSeq = lsSortedAlignSeq;
	}
	/**
	 * 一个基因会有多个位点有reads覆盖，就是说DGE的试验会造成在一个基因的多个位置有reads富集
	 * 是选择全部reads还是选择最高点的reads
	 * @param allTags
	 */
	public void setAllTags(boolean allTags) {
		this.allTags = allTags;
	}
	/**
	 * 无法设定compressType
	 * 将bed文件转化成DGE所需的信息，直接可以用DEseq分析的
	 * @param result
	 * @param sort 
	 * @param allTags 是否获得全部的正向tag，false的话，只选择最多的正向tag的数量
	 * @param bedFile
	 */
	public String dgeCal() {
		ArrayList<HashMap<String, Integer>> lsDGEvalue = new ArrayList<HashMap<String,Integer>>();
		for (AlignSeq alignSeq : lsAlignSeq) {
			try { lsDGEvalue.add(getGeneExpress(alignSeq)); } catch (Exception e) { }
		}
		HashMap<String, int[]> hashResult = combineHashDGEvalue(lsDGEvalue);
		TxtReadandWrite txtOut = new TxtReadandWrite(resultFile, true);
		String title = "GeneID";
		for (String string : lsTitle) {
			title = title + "\t"+ string;
		}
		txtOut.writefileln(title);
		for (Entry<String, int[]> entry : hashResult.entrySet()) {
			String loc = entry.getKey(); int[] value = entry.getValue();
			for (int i : value) {
				loc = loc + "\t" + i;
			}
			txtOut.writefileln(loc);
		}
		txtOut.close();
		return resultFile;
	}
	/**
	 * 给定一组hash表，key：locID   value：expressValue
	 * 将他们合并成一个hash表
	 * @param lsDGEvalue
	 * @return
	 */
	private HashMap<String, int[]> combineHashDGEvalue(ArrayList<HashMap<String, Integer>> lsDGEvalue) {
		HashMap<String, int[]> hashValue = new HashMap<String, int[]>();
		for (int i = 0; i < lsDGEvalue.size(); i++) {
			HashMap<String, Integer> hashTmp = lsDGEvalue.get(i);
			for (Entry<String, Integer> entry : hashTmp.entrySet()) {
				String loc = entry.getKey(); int value = entry.getValue();
			
				if (hashValue.containsKey(loc)) {
					int[] tmpvalue = hashValue.get(loc);
					tmpvalue[i] = value;
				} else {
					int[] tmpvalue = new int[lsDGEvalue.size()];
					tmpvalue[i] = value;
					hashValue.put(loc, tmpvalue);
				}
			}
		}
		return hashValue;
	}
	/**
	 * @param Alltags true: 选择全部tag，false，只选择最多的tag
	 * @return
	 * 返回每个基因所对应的表达量，包括多个tag之和--除了反向tag， 用 int[1]只是为了地址引用。
	 * 输入的align必须排序
	 * @throws Exception
	 */
	private HashMap<String, Integer> getGeneExpress(AlignSeq alignSeq) throws Exception {
		HashMap<String, Integer> mapGene2Exp = new LinkedHashMap<String, Integer>();
		ArrayList<double[]> lsTmpExpValue = new ArrayList<double[]>();
		double[] tmpCount = new double[]{0};
		lsTmpExpValue.add(tmpCount);
		AlignRecord lastRecord = null;
		
		for (AlignRecord alignRecord : alignSeq.readLines()) {
			//mapping到互补链上的，是假的信号
			if (!alignRecord.isMapped() || alignRecord.isCis5to3() != null && !alignRecord.isCis5to3() || alignRecord.getLength() < dgeMinLength) {
				continue;
			}
			//出现新基因
			if (lastRecord != null && !lastRecord.getRefID().equals(alignRecord.getRefID())) {
				addMapGene2Exp(mapGene2Exp, lastRecord.getRefID(), summary(lsTmpExpValue));
				lsTmpExpValue.clear();
				tmpCount = new double[]{0};
				lsTmpExpValue.add(tmpCount);
			}
			else if (lastRecord != null && alignRecord.getStartCis() > lastRecord.getEndCis()) {
				tmpCount = new double[]{0};
				lsTmpExpValue.add(tmpCount);
			}
			lastRecord = alignRecord;
			//TODO 如果是bwa mapping的结果，可能会mapping至多个位点，但是该reads只存在一次。
			//这时候就应该直接+1
			//而tophat的mapping结果，mapping至多个位点，同一个reads就会存在多次。这时候用这个方法就行
			tmpCount[0] = tmpCount[0] + (double)1/alignRecord.getMappedReadsWeight();
		}
		return mapGene2Exp;
	}
	private void addMapGene2Exp(HashMap<String, Integer> mapGene2Exp, String accID, int expValue) {
		String geneID = "";
		if (mapAccID2GeneID != null) {
			geneID = mapAccID2GeneID.get(accID);
			if (geneID == null) {
				geneID = accID;
			}
		}
		if (mapGene2Exp.containsKey(geneID)) {
			int expOld = mapGene2Exp.get(geneID);
			expValue = expOld + expValue;
		}
		mapGene2Exp.put(geneID, expValue);
	}
	private int summary(ArrayList<double[]> lsReads) {
		int result = 0;
		if (allTags) {
			result = sum(lsReads);
		} else {
			result = max(lsReads);
		}
		return result;
	}
	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private int max(ArrayList<double[]> lsReads) {
		double max = lsReads.get(0)[0];
		for (double[] is : lsReads) {
			if (is[0] > max) {
				max = is[0];
			}
		}
		return (int)max;
	}
	/**
	 * 输入int[0] 只有0位有信息
	 * @param lsReads
	 * @return
	 */
	private int sum(ArrayList<double[]> lsReads) {
		double sum = 0;
		for (double[] is : lsReads) {
			sum = sum + is[0];
		}
		return (int)sum;
	}
}
