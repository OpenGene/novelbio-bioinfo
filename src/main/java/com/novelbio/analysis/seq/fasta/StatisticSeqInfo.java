package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;

/**
 * ͳ���������ж�����NN���ж����ǿ��ܵ�gap���ж����Ǳ߽粻���
 * �ж��ٴ�д������Сд
 * @author zong0jie
 *
 */
public class StatisticSeqInfo {	
	//string0: flag string1: location string2:endLoc
	ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
	char[] seq;
	boolean flagBound = false; //�߽�ģ����ǣ�XX
	boolean flagGap = false; //gap��ǣ�Сд
	boolean flagAmbitious = false; //��ȷ�������ǣ�NNN
	int bound = 0; int gap = 0; int ambitious = 0;
	int startBound = 0; int startGap = 0; int startAmbitious = 0;
	
	protected StatisticSeqInfo(SeqFasta seqFasta) {
		this.seq = seqFasta.toString().toCharArray();
		statistics();
	}
	/**
	 * ͳ��������Сд���У�N�������Լ�X��������
	 * ��ý��
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
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
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
					flagGap = false; // gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // �߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
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
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
		}
	}
	/**
	 * @param lsInfo
	 * @param info
	 * @param start �ڲ������1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
	}
}

