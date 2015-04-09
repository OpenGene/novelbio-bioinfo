package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;

public class N50statistics {
	

	/** 记录N25，N50等信息 */
	List<String[]> lsNinfo = new ArrayList<String[]>();
	ArrayListMultimap<String, Integer> mapContigName2Length = ArrayListMultimap.create();
 	/** N50这个统计的步进 */
	int NvalueStep = 5;
	/** contig长度的步进 */
	int lengthStep = 200;
	/** 最短多长的contig，0表示全部统计，200表示统计长度200bp以上的contig */
	int minContigLen = 200;
	/** 最长统计到多长的contig */
	int maxContigLen = 3000;
	/** 统计contig的平均值 */
	int contigMeanLen = 0;
	/** 统计contig的中位值 */
	int contigMedianLen = 0;
	/** 统计所有contigs的个数 */
	double allContigsNum;
	/** 统计所有contigs的长度 */
	double allContigsLen;
	int N50Len;
 	String seqFileName;
	/** 设定后就会计算 */
	public void setSeqFileName(String seqFileName) {
		this.seqFileName = seqFileName;
		clear();
	}
	public void setLsSeqLen(List<Integer> lsSeqLen) {
		
	}
	public int getN50Len(int N50){
		
		return 0;
	}
	private void CalculateN50() {
		List<Integer> lsSeqLen = new ArrayList<Integer>(mapContigName2Length.values());
		//降序排序
		Collections.sort(lsSeqLen, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		allContigsLen = MathComput.sum(lsSeqLen);
		allContigsNum = lsSeqLen.size();
		contigMeanLen = (int) (allContigsLen/allContigsNum);
		double tmpN = 0;
		int lastSeqLen = 0;
		int contigNum = 0;
		int Nvalue = NvalueStep;
		for (Integer seqLen : lsSeqLen) {
			contigNum ++;
			tmpN = tmpN + seqLen;
			if (tmpN*100 / allContigsLen >= Nvalue) {
				String[] tmpNvalue = new String[3];
				tmpNvalue[0] = "N" + Nvalue;
				if (lastSeqLen == 0 || tmpN*100 / allContigsLen == Nvalue) {
					tmpNvalue[1] = seqLen + "";
				} else {
					tmpNvalue[1] = lastSeqLen + "";
				}
				tmpNvalue[2] = contigNum + "";
				Nvalue += NvalueStep;
				if (Nvalue > 100) {
					break;
				}
				lsNinfo.add(tmpNvalue);
			}
			lastSeqLen = seqLen;
		}
		if (Nvalue != 100 ) {
			String[] tmpNvalue = new String[3];
			tmpNvalue[0] = "N" + 100;
			tmpNvalue[1] = lastSeqLen + "";
			tmpNvalue[2] = contigNum + "";
			lsNinfo.add(tmpNvalue);
		}
		lsNinfo.add(0, new String[]{"Nvalue", "Length", "ContigNum"});
		String[] arrN50Len = lsNinfo.get(9);
		N50Len = Integer.parseInt(arrN50Len[1]);
		
	}
	
	private void getInfo() {		
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFileName);
		String tmpName = "";
		int tmpSeqLen = 0;
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith(">")) {
				summaryInfo(tmpName, tmpSeqLen);
				tmpName = string.replace(">", "").trim();
				tmpSeqLen = 0;
				continue;
			}
			tmpSeqLen += string.trim().replace(" ", "").replace("\t", "").length();
		}
		summaryInfo(tmpName, tmpSeqLen);
		txtRead.close();
	}
	private void summaryInfo(String seqName, int seqLen) {
		if (seqLen == 0) return;
		while (mapContigName2Length.containsKey(seqName)) {
			seqName = seqName + "<";
		}
		mapContigName2Length.put(seqName, seqLen);
	}
	
	public void clear() {
		/** 记录N25，N50等信息 */
		lsNinfo.clear();
		mapContigName2Length.clear();
		seqFileName = "";
	}
	
}
