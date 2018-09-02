package com.novelbio.bioinfo.fasta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.PatternOperate.PatternUnit;
import com.novelbio.bioinfo.base.Align;

/** 给定一条很长的染色体，顺序查找，并获取染色体上指定motif的位置 */
public class MotifFastaSearch implements Cloneable {
	/** 缓存，最多保存多少碱基 */
	int bufferSize = 10000;
	/** 这个长度必须长于motif的长度，意思每次读入新的序列时，必须保留原来的至少100bp的序列 */
	int overlapSize = 100;
	
	/** 是否查找反向互补序列 */
	boolean isSearchReverseComplement = true;
	
	String chrId;
	
	/** 当前保存了多少碱基 */
	int nowSize = 0;
	
	/** 保存具体的序列 */
	LinkedList<String> lsSeq = new LinkedList<>();
	
	/** lsSeq的第一个碱基的坐标，从0开始计数 */
	int startSite = 0;
	
	TxtReadandWrite txtRead;
	Iterator<String> itSeq;
	
	/** 待查找的motif */
	PatternOperate patternOperate;
	
	public MotifFastaSearch(String fasta) {
		txtRead = new TxtReadandWrite(fasta);
		itSeq = txtRead.readlines().iterator();
	}
	
	public void setMotif(String motif) {
		patternOperate = new PatternOperate(motif);
	}
	/** 是否查找反向互补序列，如果motif是回文的则不需要查找反向互补序列 */
	public void setSearchReverseComplement(boolean isSearchReverseComplement) {
		this.isSearchReverseComplement = isSearchReverseComplement;
	}
	/**
	 * @return true 表示还没结束
	 */
	public boolean isNotFinish() {
		return itSeq.hasNext();
	}
	
	/** 读取序列，直到充满缓存 */
	public void readSeq() {
		removeSeq(overlapSize);
		
		while (nowSize < bufferSize && itSeq.hasNext()) {
			String seq = itSeq.next();
			if (seq.startsWith(">")) {
				chrId = seq.substring(1).trim();
				lsSeq.clear();
				nowSize = 0;
				startSite = 0;
				continue;
			}	
			lsSeq.add(seq);
			nowSize += seq.length();
		}
	}
	
	/** 清除lsSeq中过时的序列，保留仅总计overlapSize bp的序列 */
	protected void removeSeq(int remainSize) {
		String lastSeq = null;
		while (nowSize > remainSize) {
			lastSeq = lsSeq.poll();
			nowSize = nowSize - lastSeq.length();
			startSite = startSite + lastSeq.length();
		}
		
		if (nowSize < remainSize && lastSeq != null) {
			int remainSizeTmp = remainSize - nowSize;
			String remainSeq = lastSeq.substring(lastSeq.length() - remainSizeTmp, lastSeq.length());
			nowSize += remainSizeTmp;
			startSite -= remainSizeTmp;
			lsSeq.addFirst(remainSeq);
		}
	}
	
	/** 没有overlap的motif */
	public List<MotifLoc> searchMotifWithoutOverlap() {
		List<MotifLoc> lsMotifLocs = searchMotif();
		return removeOverlapMotifLoc(lsMotifLocs);
	}
	
	public List<MotifLoc> searchMotif() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String seq : lsSeq) {
			stringBuilder.append(seq);
		}
		String tmpSeq = stringBuilder.toString();
		if (tmpSeq.length() != nowSize) {
			throw new RuntimeException("now size is not equal to tmpseq size!");
		}
		
		List<MotifLoc> lsMotifLocs = searchMotif(tmpSeq, true);
		if (isSearchReverseComplement) {
			lsMotifLocs.addAll(searchMotif(tmpSeq, false));
			//排序
			Collections.sort(lsMotifLocs, new ComparatorMotif());
		}
		
		//TODO 删除重复或者说overlap的位点
		if (!lsMotifLocs.isEmpty()) {
			MotifLoc motifLocLast = lsMotifLocs.get(lsMotifLocs.size() - 1);
			int remainSize = startSite + nowSize - motifLocLast.getEnd();
			removeSeq(remainSize);
		}
		return lsMotifLocs;
	}
	
	private List<MotifLoc> searchMotif(String tmpSeq, boolean isSearchCis) {
		if (!isSearchCis) {
			tmpSeq = SeqFasta.reverseComplement(tmpSeq);
		}
		
		List<PatternUnit> lsPatternUnits = patternOperate.searchStr(tmpSeq);		
		if (lsPatternUnits.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<MotifLoc> lsMotifLocs = new ArrayList<>();
		//查找正向序列
		for (PatternUnit patternUnit : lsPatternUnits) {
			int start = isSearchCis? patternUnit.getStartLoc() : patternUnit.getEndLoc();
			
			int startReal = startSite + start;
			int endReal = startReal + patternUnit.getPatternStr().length() - 1;
			MotifLoc motifLoc = new MotifLoc(chrId, startReal, endReal, true, patternOperate.getRegex(), patternUnit.getPatternStr());
			lsMotifLocs.add(motifLoc);
		}
		return lsMotifLocs;
	}
	
	public void close() {
		txtRead.close();
	}
	
	/** 要求输入的lsMotifLocs必须是排过序的 */
	public static List<MotifLoc> removeOverlapMotifLoc(List<MotifLoc> lsMotifLocs) {
		List<MotifLoc> lsMotifResult = new ArrayList<>();
		MotifLoc motifLast = null;
		for (MotifLoc motifLoc : lsMotifLocs) {
			if (motifLast != null && motifLoc.getStart() < motifLast.getEnd() && motifLoc.getEnd() > motifLast.getStart()) {
				continue;
			}
			lsMotifResult.add(motifLoc);
			motifLast = motifLoc;
		}		
		return lsMotifResult;
	}
	
	public static class MotifLoc {
		String motif;
		String motifReal;
		Align align;
		public MotifLoc(String chrId, int start, int end, boolean isCis, String motif, String motifReal) {
			align = new Align(chrId, start, end);
			this.motif = motif;
			this.motifReal = motifReal;
			align.setCis5to3(isCis);
		}
		
		public String getMotif() {
			return motif;
		}
		public String getMotifReal() {
			return motifReal;
		}
		public String getRefId() {
			return align.getChrId();
		}
		public int getStart() {
			return align.getStartAbs();
		}
		public int getEnd() {
			return align.getEndAbs();
		}
		public boolean isCis5to3() {
			return align.isCis5to3();
		}
		public String toString() {
			return motif + "\t" + motifReal + "\t" + align.getChrId() + "\t" + align.getStartAbs() + "\t" + align.getEndAbs();
		}
	}
		
	public static class ComparatorMotif implements Comparator<MotifLoc> {
		public int compare(MotifLoc o1, MotifLoc o2) {
			int result = o1.getRefId().compareTo(o2.getRefId());
			if (result != 0) return result;
			
			Integer o1S = o1.getStart();
			Integer o2S = o2.getStart();
			result = o1S.compareTo(o2S);
			if (result != 0) return result;
			
			Integer o1e = o1.getEnd();
			Integer o2e = o2.getEnd();
			result = o1e.compareTo(o2e);
			if (result != 0) return result;

			return 0;
		}
	}
}
