package com.novelbio.analysis.seq.reseq;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import org.hamcrest.core.Is;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;



public class ModifyInfo implements Comparable<ModifyInfo>
{
	/**
	 * @param seqName ������
	 * @param modifyName �����޸ĵ����֣�������֣�����ܺ��޸ĵ����Ӧ
	 * @param ��������
	 */
	protected ModifyInfo(String seqName, String modifyName, String seq) {
		this.seqName = seqName;
		this.modifyName = modifyName;
		this.modifySeq = seq;
	}
	/**
	 * lastz����ļ���һ���������align
	 * ��ȡlastz���ļ��󣬽�������ڸ�hash����
	 */
	static HashMap<String, ModifyInfo> hashModifyInfo = new HashMap<String, ModifyInfo>();
	/**
	 * ��ø�lastz�õ���������Ϣ
	 * @param lastzFile lastz�ļ�·����lastz����ļ���һ���������align
	 * @param seqModifySeq ����װ��fasta����
	 * @return
	 */
	public static ArrayList<ModifyInfo> getModifyInfo(String lastzFile, SeqFastaHash seqModifySeq) {
		TxtReadandWrite txtLastz = new TxtReadandWrite(lastzFile, false);
		ArrayList<String> lsInfo = txtLastz.readfileLs();
		String title = lsInfo.get(0);
		
		for (int i = 1; i < lsInfo.size(); i++) {
			ModifyInfo modifyInfo = new ModifyInfo(title, lsInfo.get(i));
			String seq = seqModifySeq.getSeqAll(modifyInfo.getModifyName().toLowerCase(), modifyInfo.isCis5to3());
			if (modifyInfo.getStartModify() == 1) {
				modifyInfo.setBooStart(true);
			}
			if (modifyInfo.getEndModify() == seq.length() ) {
				modifyInfo.setBooEnd(true);
			}
			modifyInfo.modifySeq = seq.substring(modifyInfo.getStartModify()-1,modifyInfo.getEndModify());

			
			if (hashModifyInfo.containsKey(modifyInfo.getModifyName())) {
				modifyInfo = hashModifyInfo.get(modifyInfo.getModifyName());
				modifyInfo.boosingle = false;
			}
			else {
				modifyInfo.boosingle = true;
				hashModifyInfo.put(modifyInfo.getModifyName(), modifyInfo);
			}
		}
		
		ArrayList<ModifyInfo> lsResult = new ArrayList<ModifyInfo>();
		for (Entry<String, ModifyInfo> entry : hashModifyInfo.entrySet()) {
			ModifyInfo modifyInfo = entry.getValue();
			if (modifyInfo.boosingle) {
				lsResult.add(modifyInfo);
			}
		}
		return lsResult;
	}
	
	
	/**
	 * ���������к�ֵ����䱾��
	 * ��֮ǰ����setTitle�趨����
	 */
	private ModifyInfo(String title, String value) {
		String[] Info = title.replace("#", "").split("\t");
		String[] ssvalue = value.split("\t");
		for (int i = 0; i < ssvalue.length; i++) {
			setInfo(Info[i], ssvalue[i]);
		}
	}
	
	private void setInfo(String title, String value)
	{
		if (title.equals("score")) {
			this.score = Integer.parseInt(value);
		}
		else if (title.equals("start1")) {
			this.start = Integer.parseInt(value);
		}
		else if (title.equals("end1")) {
			this.end = Integer.parseInt(value);
		}
		else if (title.equals("strand2")) {
			this.cis5to3 = value.equals("+");
		}
		else if (title.equals("start2")) {
			this.startModify = Integer.parseInt(value);
		}
		else if (title.equals("end2")) {
			this.endModify = Integer.parseInt(value);
		}
		else if (title.equals("length2")) {
			this.lengthSeq2 = Integer.parseInt(value);
		}
		else if (title.equals("name1")) {
			this.seqName = value;
		}
		else if (title.equals("name2")) {
			this.modifyName = value;
		}
		
	}
	/**
	 * ����lastz�ļ���ȡ��ʹ�ã��жϸ�align�����ֹ�һ��
	 */
	boolean boosingle = true;
	/**
	 * ���еķ���
	 */
	int score = 0;

	private ModifyInfo()
	{}
	
	/**
	 * ���޸����е�����
	 */
	String seqName = "";
	/**
	 * �����޸ĵ����֣�������֣�����ܺ��޸ĵ����Ӧ
	 */
	String modifyName = "";
	/**
	 * �ڵڼ�λ��ʼ�滻��������
	 */
	int start = -1;
	/**
	 * �ڵڼ�λ�����滻��������
	 * ���end<start������뵽start֮��
	 * ���end == start����Ϊsnp
	 */
	int end = -1;
	int lengthSeq2 = 0;
	/**
	 * align�ĳ���
	 * @return
	 */
	public int getLengthSeq2() {
		return lengthSeq2;
	}
	public void setLengthSeq2(int lengthSeq2) {
		this.lengthSeq2 = lengthSeq2;
	}
	int startModify = -1;
	int endModify = -1;
	int crossStartSiteSeq2End = -1;
	int crossStartSiteSeq2Start = -1;
	int crossStartSiteSeq1Start = -1;
	int crossStartSiteSeq1End = -1;
	/**
	 * �����˺������¼�
	 */
	boolean crossStartSite = false;
	public boolean isCrossStartSite() {
		return crossStartSite;
	}
	/**
	 * ��crossStartSiteΪtrueʱʹ��
	 * �������seq2���е���β�ˣ����Ǻ�λ�ã�������      --------------------*          --------------
	 * @return
	 */
	public int getCrossStartSiteSeq2End() {
		return crossStartSiteSeq2End;
	}
	/**
	 * ��crossStartSiteΪtrueʱʹ��
	 * �������seq2���е���ǰ�ˣ����Ǻ�λ�ã�������      --------------------          *-------------
	 * @return
	 */
	public int getCrossStartSiteSeq2Start() {
		return crossStartSiteSeq2Start;
	}
	/**
	 * ��crossStartSiteΪtrueʱʹ��
	 * �������seq1���е���β�ˣ����Ǻ�λ�ã�������      --------------------          --------------*\ 
	 * @return
	 */
	public int getCrossStartSiteSeq1End() {
		return crossStartSiteSeq1End;
	}
	/**
	 * ��crossStartSiteΪtrueʱʹ��
	 * �������seq1���е���ǰ�ˣ����Ǻ�λ�ã�������      *--------------------          --------------
	 * @return
	 */
	public int getCrossStartSiteSeq1Start() {
		return crossStartSiteSeq1Start;
	}
		
	public void setCrossStartSite(boolean crossStartSite) {
		this.crossStartSite = crossStartSite;
	}
	public void setCrossStartSiteSeq1End(int crossStartSiteSeq1End) {
		this.crossStartSiteSeq1End = crossStartSiteSeq1End;
	}
	public void setCrossStartSiteSeq1Start(int crossStartSiteSeq1Start) {
		this.crossStartSiteSeq1Start = crossStartSiteSeq1Start;
	}
	public void setCrossStartSiteSeq2End(int crossStartSiteSeq2End) {
		this.crossStartSiteSeq2End = crossStartSiteSeq2End;
	}
	public void setCrossStartSiteSeq2Start(int crossStartSiteSeq2Start) {
		this.crossStartSiteSeq2Start = crossStartSiteSeq2Start;
	}
	public void setEndModify(int endModify) {
		this.endModify = endModify;
	}
	public void setStartModify(int startModify) {
		this.startModify = startModify;
	}
	public int getStartModify() {
		return startModify;
	}
	public int getEndModify() {
		return endModify;
	}
	
	/**
	 *  �滻���е�ǰ���Ƿ�û����
	 *  true��û����
	 *  false��������
	 */
	boolean booStart = true;
	Boolean cis5to3 = true;
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	/**
	 * �滻���еĺ��Ƿ�û����
	 *  true��û����
	 *  false��������
	 */
	boolean booEnd = true;
	boolean assemle = true;
	public void setAssemle(boolean assemle) {
		this.assemle = assemle;
	}
	public boolean isAssemle() {
		return assemle;
	}
	String modifySeq = "";
	
	public void setStart(int start) {
		this.start = start;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	/**
	 *  �滻���е�ǰ���Ƿ�������
	 */
	public void setBooStart(boolean booStart) {
		this.booStart = booStart;
	}
	/**
	 * �滻���еĺ��Ƿ�������
	 */
	public void setBooEnd(boolean booEnd) {
		this.booEnd = booEnd;
	}
	/**
	 * ���޸ĵ�����
	 * @return
	 */
	public String getModifySeq() {
		return modifySeq;
	}
	/**
	 * �ڵڼ�λ��ʼ�滻��������
	 */
	public int getStart() {
		return start;
	}
	/**
	 * �ڵڼ�λ�����滻��������
	 * ���end<start������뵽start֮��
	 * ���end == start����Ϊsnp
	 */
	public int getEnd() {
		return end;
	}
	/**
	 * �����޸ĵ����֣�������֣�����ܺ��޸ĵ����Ӧ
	 */
	public String getModifyName() {
		return modifyName;
	}
	public String getSeqName() {
		return seqName;
	}
	/**
	 * �滻���еĺ��Ƿ�û����
	 *  true��û����
	 *  false��������
	 */
	public boolean isBooEnd() {
		return booEnd;
	}
	/**
	 *  �滻���е�ǰ���Ƿ�û����
	 *  true��û����
	 *  false��������
	 */
	public boolean isBooStart() {
		return booStart;
	}

	
	/**
	 * ���޸�����ĳ���
	 * @return
	 */
	public Integer getLenModify() {
		if (end < start) {//�����޸�
			return 0;
		}
		return end - start;
	}
	/**
	 * ��С��������ֻ����startλ��
	 */
	@Override
	public int compareTo(ModifyInfo o) {
		Integer a = start;
		Integer b = o.getStart();
		return a.compareTo(b);
	}
	
	public ModifyInfo clone()
	{
		ModifyInfo modifyInfo2 = new ModifyInfo();
		modifyInfo2.modifyName = this.modifyName;
		modifyInfo2.start = this.start;
		modifyInfo2.end = this.end;
		modifyInfo2.booStart = this.booStart;
		modifyInfo2.booEnd = this.booEnd;
		modifyInfo2.seqName = this.seqName;
		modifyInfo2.assemle = this.assemle;
		modifyInfo2.cis5to3 = this.cis5to3;
		modifyInfo2.endModify = this.endModify;
		modifyInfo2.modifySeq = this.modifySeq;
		modifyInfo2.startModify = this.startModify;
		modifyInfo2.crossStartSiteSeq2End = this.crossStartSiteSeq2End;
		modifyInfo2.crossStartSiteSeq2Start = this.crossStartSiteSeq2Start;
		modifyInfo2.crossStartSiteSeq1Start = this.crossStartSiteSeq1Start;
		modifyInfo2.crossStartSiteSeq1End = this.crossStartSiteSeq1End;
		modifyInfo2.crossStartSite = this.crossStartSite;
		modifyInfo2.score = this.score;
		modifyInfo2.lengthSeq2 = this.lengthSeq2;
		return modifyInfo2;
	}
	
	/**
	 * ��������Ҫ��֤
	 * ����Ҫ��ѡ�Ļ����У�overlap��ɾ��
	 * @param lsModifyInfos
	 * @return ����һ��list list0�����ص���ModifyInfo������start��С��������
	 * list1����������ModifyInfo��׼���´μ�������
	 */
	public static ArrayList<ArrayList<ModifyInfo>> delOverlap(ArrayList<ModifyInfo> lsModifyInfos)
	{
//		for (int i = 0; i < lsModifyInfos.size(); i++) {
//			ModifyInfo modifyInfo = lsModifyInfos.get(i);
//			if (modifyInfo.getStart() > modifyInfo.getEnd()) {
//				lsModifyInfos.remove(i);
//			}
//			ModifyInfo modifyInfoSub1 = modifyInfo.clone();
//			modifyInfoSub1. = modifyInfo.
//		}
		
		Collections.sort(lsModifyInfos, new Comparator<ModifyInfo>() {
			//�����滻Ƭ�γ��Ȱ��մӴ�С����
			@Override
			public int compare(ModifyInfo o1, ModifyInfo o2) {
				return -o1.getLenModify().compareTo(o2.getLenModify());
			}
		});
		//���Ҫ��������ĵ�list
		ArrayList<ModifyInfo> lsFinal = new ArrayList<ModifyInfo>();
		//�Ȳ����з�����list
		ArrayList<ModifyInfo> lsNot = new ArrayList<ModifyInfo>();
		for (ModifyInfo modifyInfo : lsModifyInfos) {
			if (!modifyInfo.isAssemle()) {
				lsNot.add(modifyInfo);
				continue;
			}
			int index = Collections.binarySearch(lsFinal, modifyInfo);
			if (index >= 0)
			{
				if (lsFinal.get(index).getLenModify() < modifyInfo.getLenModify()) 
				{
					if (index == lsFinal.size() || lsFinal.get(index+1).start > modifyInfo.end) {
						lsFinal.set(index, modifyInfo);//.copy(modifyInfo);
						continue;
					}
				}
			}
			else {
				//�����modifyInfo��ǰ��û���ص���-index-2<0��ʾ�ڵ�һ��
				if (lsFinal.size() == 0) {
					lsFinal.add(modifyInfo);
					continue;
				}
				if (  (-index-1 >= lsFinal.size() || modifyInfo.end < lsFinal.get(-index-1).start) &&  (-index -2 < 0 || modifyInfo.start > lsFinal.get(-index-2).end) ) {
					lsFinal.add(-index-1,modifyInfo);
					continue;
				}
			}
			lsNot.add(modifyInfo);
		}
		ArrayList<ArrayList<ModifyInfo>> lsResult = new ArrayList<ArrayList<ModifyInfo>>();
		lsResult.add(lsFinal);
		lsResult.add(lsNot);
		return lsResult;
	}
}
