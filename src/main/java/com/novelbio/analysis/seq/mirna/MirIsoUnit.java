package com.novelbio.analysis.seq.mirna;

import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.gffOperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffOperate.MirMature;
import com.novelbio.analysis.seq.genome.gffOperate.MirPre;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.SepSign;

/**
 * 根据mapping结果获得miRNA的iso
 * @author zong0jie
 */
public class MirIsoUnit extends GeneExpTable {
	MirPre mirPre;
	int i = 1;
	/** 序列名 */
	public MirIsoUnit(MirPre mirPre) {
		super("IsoMirnaSeq");
		this.mirPre = mirPre;
		setTitle();
	}
	
	/** 序列名 */
	public MirIsoUnit(MiRNAList miRNAList, String mirName) {
		super("IsoMirnaSeq");
		mirPre = miRNAList.getListDetail(mirName);
		setTitle();
	}
	
	private void setTitle() {
		for (MirMature mirMature : mirPre.getLsElement()) {
			String mirSeq = mirMature.getSeq().toString().toUpperCase();
			addGeneName(mirSeq);
			addAnnotation(mirSeq, new String[]{mirMature.getNameSingle(), addDotMiRNA(mirMature.getStartAbs(), mirMature.getEndAbs(), mirSeq)});
		}
		
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add("Seq_Name");
		lsTitle.add(mirPre.getMirPreSeq().toString().toUpperCase());
		addLsTitle(lsTitle);
	}
	
	public void addMirSamRecord(MiRNAList miRNAList, SamRecord samRecord) {
		MirMature mirMature = miRNAList.searchElement(samRecord.getRefID(), samRecord.getStartAbs(), samRecord.getEndAbs());
		if (mirMature == null) {
			return;
		}
		addIsoMirExp(mirMature, samRecord);
	}
	
	/** 添加isoMiRNA的表达 */
	private void addIsoMirExp(MirMature mirMature, SamRecord samRecord) {
		String seq = samRecord.getSeqFastaClip().toString().toUpperCase();
		if (!isContainGeneName(seq)) {
			addGeneName(seq);
			if (mirMature.getSeq().toString().equalsIgnoreCase(seq)) {
				addAnnotation(seq, new String[]{mirMature.getNameSingle(), addDot(samRecord)});
			} else {
				addAnnotation(seq, new String[]{mirMature.getNameSingle() + SepSign.SEP_ID + (i++), addDot(samRecord)});
			}
		}
		addGeneExp(seq, (double)1/samRecord.getMappedReadsWeight());
	}
	
	/** 在序列的前后加上点
	 * @param startAbs 比对到miRNA的起点，从1开始计算
	 * @param endAbs 比对到miRNA的终点，从1开始计算
	 * @param seq
	 * @return
	 */
	private String addDotMiRNA(int startAbs, int endAbs, String seq) {
		String startDotStr = "", endDotStr = "";
		char[] dotStart = new char[startAbs - 1];
		for (int i = 0; i < dotStart.length; i++) {
			dotStart[i] = '.';
		}
		startDotStr = String.copyValueOf(dotStart);
		char[] dotEnd = new char[mirPre.getMirPreSeq().Length() - endAbs ];
		for (int i = 0; i < dotEnd.length; i++) {
			dotEnd[i] = '.';
		}
		endDotStr = String.copyValueOf(dotEnd);
		String result = startDotStr + seq.toUpperCase() + endDotStr;
		return result;
	}
	
	/** 在序列的前后加上点
	 * @param startAbs 比对到miRNA的起点，从1开始计算
	 * @param endAbs 比对到miRNA的终点，从1开始计算
	 * @param seq
	 * @return
	 */
	private String addDot(SamRecord samRecord) {
		String startDotStr = "", endDotStr = "";
		char[] dotStart = new char[samRecord.getStartAbs() - 1];
		for (int i = 0; i < dotStart.length; i++) {
			dotStart[i] = ' ';
		}
		startDotStr = String.copyValueOf(dotStart);
		char[] dotEnd = new char[mirPre.getMirPreSeq().Length() - samRecord.getEndAbs() ];
		for (int i = 0; i < dotEnd.length; i++) {
			dotEnd[i] = ' ';
		}
		endDotStr = String.copyValueOf(dotEnd);
		
		String seqMirPre = mirPre.getMirPreSeq().toString().toUpperCase().substring(samRecord.getStartAbs() - 1, samRecord.getEndAbs());
		String seqThis = samRecord.getSeqFastaClip().toString().toUpperCase();
		char[] seqMirPreChar = seqMirPre.toCharArray();
		char[] seqThisChar = seqThis.toCharArray();
		StringBuilder stringFinal = new StringBuilder();
		int numRef = 0, numThis = 0;
		for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()	) {
			CigarOperator cigarOperator = cigarElement.getOperator();
			if (cigarOperator == CigarOperator.S || cigarOperator == CigarOperator.H) {
				continue;
			}
			if (cigarOperator == CigarOperator.I) {
				stringFinal.append('[');
				for (int i = 0; i < cigarElement.getLength(); i++) {
					stringFinal.append(seqThisChar[numThis++]);
				}
				stringFinal.append(']');
			} else if (cigarOperator == CigarOperator.D) {
				for (int i = 0; i < cigarElement.getLength(); i++) {
					stringFinal.append('-');
					numRef++;
				}
			} else {
				for (int i = 0; i < cigarElement.getLength(); i++) {
					char thisChar = seqThisChar[numThis++];
					char refChar = seqMirPreChar[numRef++];
					if (thisChar == refChar) {
						stringFinal.append(".");
					} else {
						stringFinal.append(thisChar);
					}
				}
			}
		}
		String result = startDotStr + stringFinal.toString() + endDotStr;
		return result;
	}
	
//	public List<String[]> getLsCountsNum(EnumExpression enumExpression) {
//		List<String[]> lsInfo = super.getLsCountsNum(enumExpression);
//		return lsInfo;
//	}
	
	
	/**
	 * 注意本方法要和{@link #addIsoMirExp} 中的annotation统一<br>
	 * 目的是将表达量为0的isoMiRNA删除
	 */
	public List<String[]> getLsAllCountsNum(EnumExpression enumExpression) {
		List<String[]> lsInfo = super.getLsAllCountsNum(enumExpression);
		return modifyFinalIso(lsInfo);
	}
	
	/**
	 * 根据不同的mir成熟体来标记iso number
	 * 譬如
	 * mir-169-5p_iso1
	 * mir-169-5p_iso2
	 * 
	 * mir-169-3p_iso1
	 * mir-169-3p_iso2
	 * @param lsInfo
	 * @return
	 */
	private List<String[]> modifyFinalIso(List<String[]> lsInfo) {
		List<String[]> lsFinal = new ArrayList<>();
		Map<String, int[]> mapMirna2IsoNum = new HashMap<>();
		for (MirMature mirMature : mirPre.getLsElement()) {
			mapMirna2IsoNum.put(mirMature.getNameSingle(), new int[]{1});
		}
		int m = 0;
		for (String[] info : lsInfo) {
			int sum = 0;

			if (!info[1].contains(SepSign.SEP_ID)) {//说明不是isoMiRNA
				//第一个miRNA放在第一位，第二个miRNA放在第二位
				//譬如mir-164-5p放在第一位，mir-164-3p放在第二位
				lsFinal.add(m++, info);
				continue;
			}
			for (int i = 3; i < info.length; i++) {
				try {
					sum += Double.parseDouble(info[i]);
				} catch (Exception e) {
					break;
				}
			}
			if (sum == 0) {
				continue;
			}
			String mirMatureName = info[1].split(SepSign.SEP_ID)[0];
			int[] isoNum = mapMirna2IsoNum.get(mirMatureName);
			info[1] = mirMatureName + "_iso" + (isoNum[0]++);
			lsFinal.add(info);
		}
		return lsFinal;
	}
	
	/** 返回一系列基因的名称，并按照表达量进行排序 */
	public Set<String> getSetGeneName() {
		Map<String, List<String[]>> mapMirName2LsIsoValue = new HashMap<>();
		for (String seqIso : mapGene_2_Cond2Exp.keySet()) {
			List<String> lsAnno = mapGene2Anno.get(seqIso);//annotation 第一个是mirName@@iso 第二个是mir alignment
			String mirName = null;
			mirName = lsAnno.get(0).split(SepSign.SEP_ID)[0];
			List<String[]> lsGene2Value = mapMirName2LsIsoValue.get(mirName);
			if (lsGene2Value == null) {
				lsGene2Value = new ArrayList<>();
				mapMirName2LsIsoValue.put(mirName, lsGene2Value);
			}
			
			lsGene2Value.add(new String[]{seqIso, getSum(mapGene_2_Cond2Exp.get(seqIso)) + ""});
		}
		
		/** 对于每个miRNA的不同iso形式，按照iso的数量从大到小排序 */
		for (List<String[]> lsGene2Value : mapMirName2LsIsoValue.values()) {
			Collections.sort(lsGene2Value, new Comparator<String[]>() {
				public int compare(String[] o1, String[] o2) {
					Double o1value = Double.parseDouble(o1[1]);
					Double o2value = Double.parseDouble(o2[1]);
					return -o1value.compareTo(o2value);
				}
			});
		}
		
		List<String> lsMirName = new ArrayList<>(mapMirName2LsIsoValue.keySet());
		Collections.sort(lsMirName, new Comparator<String>() {
			public int compare(String o1, String o2) {
				Integer startO1 = mirPre.searchMirName(o1).getStartAbs();
				Integer startO2 = mirPre.searchMirName(o2).getStartAbs();				
				return startO1.compareTo(startO2);
			}
		});
		Set<String> lsIsoNameResult = new LinkedHashSet<>();
		for (String string : lsMirName) {
			for (String[] geneName  : mapMirName2LsIsoValue.get(string)) {
				lsIsoNameResult.add(geneName[0]);
			}
		}
		return lsIsoNameResult;
	}
	
	private double getSum(Map<String, Double> mapCond2Value ) {
		double sum = 0;
		for (Double value : mapCond2Value.values()) {
			sum+=value;
		}
		return sum;
	}
	
}
