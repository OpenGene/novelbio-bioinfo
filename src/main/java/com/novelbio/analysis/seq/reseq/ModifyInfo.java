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
	 * @param seqName 序列名
	 * @param modifyName 本次修改的名字，起个名字，最好能和修改的相对应
	 * @param 具体序列
	 */
	protected ModifyInfo(String seqName, String modifyName, String seq) {
		this.seqName = seqName;
		this.modifyName = modifyName;
		this.modifySeq = seq;
	}
	/**
	 * lastz结果文件，一个结果多条align
	 * 读取lastz的文件后，将结果放在该hash表中
	 */
	static HashMap<String, ModifyInfo> hashModifyInfo = new HashMap<String, ModifyInfo>();
	/**
	 * 获得该lastz得到的序列信息
	 * @param lastzFile lastz文件路径，lastz结果文件，一个结果多条align
	 * @param seqModifySeq 待组装的fasta序列
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
	 * 给定标题列和值，填充本类
	 * 用之前先用setTitle设定标题
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
	 * 仅在lastz文件读取中使用，判断该align仅出现过一次
	 */
	boolean boosingle = true;
	/**
	 * 序列的分数
	 */
	int score = 0;

	private ModifyInfo()
	{}
	
	/**
	 * 待修改序列的名字
	 */
	String seqName = "";
	/**
	 * 本次修改的名字，起个名字，最好能和修改的相对应
	 */
	String modifyName = "";
	/**
	 * 在第几位开始替换，闭区间
	 */
	int start = -1;
	/**
	 * 在第几位结束替换，闭区间
	 * 如果end<start，则插入到start之后
	 * 如果end == start，则为snp
	 */
	int end = -1;
	int lengthSeq2 = 0;
	/**
	 * align的长度
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
	 * 发生了横跨起点事件
	 */
	boolean crossStartSite = false;
	public boolean isCrossStartSite() {
		return crossStartSite;
	}
	/**
	 * 当crossStartSite为true时使用
	 * 横跨起点的seq2序列的最尾端，即星号位置，闭区间      --------------------*          --------------
	 * @return
	 */
	public int getCrossStartSiteSeq2End() {
		return crossStartSiteSeq2End;
	}
	/**
	 * 当crossStartSite为true时使用
	 * 横跨起点的seq2序列的最前端，即星号位置，闭区间      --------------------          *-------------
	 * @return
	 */
	public int getCrossStartSiteSeq2Start() {
		return crossStartSiteSeq2Start;
	}
	/**
	 * 当crossStartSite为true时使用
	 * 横跨起点的seq1序列的最尾端，即星号位置，闭区间      --------------------          --------------*\ 
	 * @return
	 */
	public int getCrossStartSiteSeq1End() {
		return crossStartSiteSeq1End;
	}
	/**
	 * 当crossStartSite为true时使用
	 * 横跨起点的seq1序列的最前端，即星号位置，闭区间      *--------------------          --------------
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
	 *  替换序列的前部是否没问题
	 *  true：没问题
	 *  false：有问题
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
	 * 替换序列的后部是否没问题
	 *  true：没问题
	 *  false：有问题
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
	 *  替换序列的前部是否有问题
	 */
	public void setBooStart(boolean booStart) {
		this.booStart = booStart;
	}
	/**
	 * 替换序列的后部是否有问题
	 */
	public void setBooEnd(boolean booEnd) {
		this.booEnd = booEnd;
	}
	/**
	 * 待修改的序列
	 * @return
	 */
	public String getModifySeq() {
		return modifySeq;
	}
	/**
	 * 在第几位开始替换，闭区间
	 */
	public int getStart() {
		return start;
	}
	/**
	 * 在第几位结束替换，闭区间
	 * 如果end<start，则插入到start之后
	 * 如果end == start，则为snp
	 */
	public int getEnd() {
		return end;
	}
	/**
	 * 本次修改的名字，起个名字，最好能和修改的相对应
	 */
	public String getModifyName() {
		return modifyName;
	}
	public String getSeqName() {
		return seqName;
	}
	/**
	 * 替换序列的后部是否没问题
	 *  true：没问题
	 *  false：有问题
	 */
	public boolean isBooEnd() {
		return booEnd;
	}
	/**
	 *  替换序列的前部是否没问题
	 *  true：没问题
	 *  false：有问题
	 */
	public boolean isBooStart() {
		return booStart;
	}

	
	/**
	 * 待修改区域的长度
	 * @return
	 */
	public Integer getLenModify() {
		if (end < start) {//插入修改
			return 0;
		}
		return end - start;
	}
	/**
	 * 从小到大排序，只排列start位点
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
	 * 本函数需要验证
	 * 将需要挑选的基因中，overlap的删除
	 * @param lsModifyInfos
	 * @return 返回一个list list0：不重叠的ModifyInfo，按照start从小到大排序
	 * list1：所有其他ModifyInfo，准备下次继续分析
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
			//将待替换片段长度按照从大到小排序
			@Override
			public int compare(ModifyInfo o1, ModifyInfo o2) {
				return -o1.getLenModify().compareTo(o2.getLenModify());
			}
		});
		//最后要放入分析的的list
		ArrayList<ModifyInfo> lsFinal = new ArrayList<ModifyInfo>();
		//先不进行分析的list
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
				//插入的modifyInfo与前后都没有重叠。-index-2<0表示在第一个
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
