package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.HistList;

/** 计算序列的N50和其他统计长度信息，最后画出柱状图 */
public class N50AndSeqLen {
	/** 记录N25，N50等信息 */
	List<String[]> lsNinfo = new ArrayList<String[]>();
	ArrayListMultimap<String, Integer> mapContigName2Length = ArrayListMultimap.create();
 	String seqFileName;
 	/** N50这个统计的步进 */
	int NvalueStep = 5;
	/** contig长度的步进 */
	int lengthStep = 200;
	int contigMeanLen = 0;
	/** 长度统计 */
	HistList hListLength;
	
	/** 设定后就会计算 */
	public N50AndSeqLen(String seqFileName) {
		this.seqFileName = seqFileName;
		doStatistics();
	}
	/** 设定后就会计算 */
	public void setSeqFileName(String seqFileName) {
		this.seqFileName = seqFileName;
		clear();
		doStatistics();
	}
	/** 设定步长 */
	public void setNvalueStep(int nvalueStep) {
		NvalueStep = nvalueStep;
	}
	
	private void doStatistics() {
		getInfo();
		CalculateN50();
		statisticContigLen();
	}
	
	/** 获得N5,N10这种，步进为{@link #setNvalueStep(int)}的N50所有信息 */
	public List<String[]> getLsNinfo() {
		if (lsNinfo.size() > 1) {
			return lsNinfo;
		}
		CalculateN50();
		return lsNinfo;
	}
	
	public HistList gethListLength() {
		return hListLength;
	}
	
	public Integer getLenAvg() {
		return contigMeanLen;
	}
	
	private void getInfo() {
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFileName);
		String tmpName = "", tmpSeq = "";
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith(">")) {
				summaryInfo(tmpName, tmpSeq);
				tmpName = string.replace(">", "").trim();
				tmpSeq = "";
				continue;
			}
			tmpSeq = tmpSeq + string.trim().replace(" ", "").replace("\t", "");
		}
		summaryInfo(tmpName, tmpSeq);
		txtRead.close();
	}
	
	private void summaryInfo(String seqName, String seqDetail) {
		if (seqDetail == null || seqDetail.equals("")) return;
		
		if (seqDetail.length() < 400) {
			System.out.println(seqName + " " +seqDetail.length());
		}
		while (mapContigName2Length.containsKey(seqName)) {
			seqName = seqName + "<";
		}
		mapContigName2Length.put(seqName, seqDetail.length());
	}
	
	private void CalculateN50() {
		List<Integer> lsSeqLen = new ArrayList<Integer>(mapContigName2Length.values());
		//降序排序
		Collections.sort(lsSeqLen, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return -o1.compareTo(o2);
			}
		});
		double lenAll = MathComput.sum(lsSeqLen);
		contigMeanLen = (int) (lenAll/lsSeqLen.size());
		double tmpN = 0;
		int lastSeqLen = 0;
		int Nvalue = NvalueStep;
		for (Integer seqLen : lsSeqLen) {
			tmpN = tmpN + seqLen;
			if (tmpN*100 / lenAll > Nvalue) {
				String[] tmpNvalue = new String[2];
				tmpNvalue[0] = "N" + Nvalue;
				tmpNvalue[1] = lastSeqLen + "";
				Nvalue += NvalueStep;
				if (NvalueStep >= 100) {
					break;
				}
				lsNinfo.add(tmpNvalue);
			}
			lastSeqLen = seqLen;
		}
		lsNinfo.add(0, new String[]{"Nvalue", "Length"});
	}
	
	private void statisticContigLen() {
		hListLength = HistList.creatHistList("SeqLen", true);
		hListLength.setStartBin(lengthStep*2, lengthStep +"-" + lengthStep*2, lengthStep, lengthStep*2);
		for (int i = lengthStep*3; i < 3000; i+=lengthStep) {
			hListLength.addHistBin(i, (i-lengthStep)+"-"+(i+lengthStep), i);
		}
		hListLength.addHistBin(3000, ">3000", 2000000);
		for (Integer length : mapContigName2Length.values()) {
			hListLength.addNum(length);
		}
	}
	
	public void clear() {
		/** 记录N25，N50等信息 */
		lsNinfo.clear();
		mapContigName2Length.clear();
		seqFileName = "";
	}
}
