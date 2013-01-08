package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;

/**
 * 统计序列中有多少是NN，有多少是可能的gap，有多少是边界不清楚
 * 有多少大写，多少小写
 * @author zong0jie
 *
 */
public class StatisticSeqInfo {	
	//string0: flag string1: location string2:endLoc
	ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
	char[] seq;
	boolean flagBound = false; //边界模糊标记，XX
	boolean flagGap = false; //gap标记，小写
	boolean flagAmbitious = false; //不确定碱基标记，NNN
	int bound = 0; int gap = 0; int ambitious = 0;
	int startBound = 0; int startGap = 0; int startAmbitious = 0;
	
	protected StatisticSeqInfo(SeqFasta seqFasta) {
		this.seq = seqFasta.toString().toCharArray();
		statistics();
	}
	/**
	 * 统计序列中小写序列，N的数量以及X的数量等
	 * 获得结果
	 * @return
	 */
	public ArrayList<LocInfo> getLsSeqInfo() {
		return lsResult;
	}
	protected void statistics() {
		for (int i = 0; i < seq.length; i++) {
			if (seq[i] < 'a' && seq[i] != 'X' && seq[i] != 'N') {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					bound ++;
				}
				else {
					flagBound = true;
					bound = 0;
					startBound = i;
				}
			} 
			else if (seq[i] == 'N') {
				if (flagAmbitious) {
					ambitious ++;
				}
				else {
					flagAmbitious = true;
					ambitious = 0;
					startAmbitious = i;
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; // gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // 边界模糊标记，XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					gap ++;
				}
				else {
					flagGap = true;
					gap = 0;
					startGap = i;
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
		}
	}
	/**
	 * @param lsInfo
	 * @param info
	 * @param start 内部会加上1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
	}
}

