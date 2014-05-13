package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

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
		String seq = samRecord.getSeqFasta().toString().toUpperCase();
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
	/**
	 * 注意本方法要和{@link #addIsoMirExp} 中的annotation统一<br>
	 * 目的是将表达量为0的isoMiRNA删除
	 */
	public List<String[]> getLsAllCountsNum(EnumExpression enumExpression) {
		List<String[]> lsFinal = new ArrayList<>();
		List<String[]> lsInfo = super.getLsAllCountsNum(enumExpression);
		//根据不同的mir成熟体来标记iso number
		//譬如
		//mir-169-5p_iso1
		//mir-169-5p_iso2
		//
		//mir-169-3p_iso1
		//mir-169-3p_iso2
		//
		Map<String, int[]> mapMirna2IsoNum = new HashMap<>();
		for (MirMature mirMature : mirPre.getLsElement()) {
			mapMirna2IsoNum.put(mirMature.getNameSingle(), new int[]{1});
		}
		
		for (String[]info : lsInfo) {
			int sum = 0;
			if (!info[1].contains(SepSign.SEP_ID)) {//说明不是isoMiRNA
				lsFinal.add(info);
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
	
}
