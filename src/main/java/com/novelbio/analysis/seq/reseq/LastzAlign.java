package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class LastzAlign {
	/**
	 * 第一条链的名字
	 */
	String seqName1 = "";
	/**
	 * 第二条链的名字
	 */
	String seqName2 = "";
	/**
	 * 第一条链的长度
	 */
	int seqLen1 = 0;
	/**
	 * 第二条链的长度
	 */
	int seqLen2 = 0;
	/**
	 * 保存每一个alignment的信息
	 */
	ArrayList<AlignInfo> lsAlignInfos = new ArrayList<AlignInfo>();
	
	HashMap<String, Integer> hashSeqLen = new HashMap<String, Integer>();
	
	
	/**
	 * 指定align文本，读取信息
	 */
	public void readInfo(String AlignFile) {
		TxtReadandWrite txtAlign = new TxtReadandWrite(AlignFile, false);
		//很有可能没东西，也就是lsInfo.size == 0
		ArrayList<String> lsInfo = txtAlign.readfileLs();
		setTitleInfo(lsInfo.get(0), lsInfo.get(1));
		AlignInfo.setTitle(lsInfo.get(0));
		for (int i = 1; i < lsInfo.size(); i++) {
			AlignInfo alignInfo = new AlignInfo(lsInfo.get(i));
			lsAlignInfos.add(alignInfo);
		}
	}
	
	private void setTitleInfo(String title, String value) {
		String[] Info = title.replace("#", "").split("\t");
		String[] ssvalue = value.split("\t");
		for (int i = 0; i < ssvalue.length; i++) {
			setInfo(Info[i], ssvalue[i]);
		}
	}
	private void setInfo(String title, String value) {
		if (title.equals("name1")) {
			this.seqName1 = value;
		}
		if (title.equals("name2")) {
			this.seqName2 = value;
		}
	}
	
	/**
	 * 获得有意义的排列区间
	 */
	public void getLenInterval() {
		for (AlignInfo alignInfo : lsAlignInfos) {
			
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * 获得所有序列的区间范围
	 */
	public void setSeqLen() {
		aaa
		//TODO: 设定序列长度
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}


/**
 * 保存最后筛选结果的类
 */
class alignResult
{
	/**
	 * 最后在目标序列的起点
	 */
	int startSeq1 = 0;
	/**
	 * 最后在目标序列的终点
	 */
	int endSeq1 = 0;
	/**
	 * 最后contig序列的起点
	 */
	int startSeq2 = 0;
	/**
	 * 最后contig序列的终点
	 */
	int endSeq2 = 0;
	/**
	 * 目标序列的长度
	 */
	int seq1Len = 0;
	/**
	 * contig的长度
	 */
	int seq2Len = 0;
	
	/**
	 * 确定头部，表示头部就可以用该位点来代替
	 * false的话，头部就要添加NNN，表示为潜在gap
	 */
	boolean booStartConfirm = false;
	/**
	 * 确定尾部，表示尾部就可以用该位点来代替
	 * false的话，尾部就要添加NNN，表示为潜在gap
	 */
	boolean booEndConfirm = false;
	/**
	 * 装载能够用于判断结合位点的alignment信息
	 */
	ArrayList<AlignInfo> lsAlignInfo = new ArrayList<AlignInfo>();
	/**
	 * 本reads是否能用于组装
	 */
	boolean booAssemble = false;
	
	/**
	 * 输入原始的连配信息，排序后返回mapping的结果
	 * @param lsAlignInfoRaw
	 */
	public void analysis(ArrayList<AlignInfo> lsAlignInfoRaw) {
		Collections.sort(lsAlignInfoRaw);
		for (AlignInfo alignInfo : lsAlignInfoRaw) {
			
		}
		
		
		
		
		
		
	}
	
	private void copeAlign(AlignInfo alignInfo)
	{
		if (alignInfo.get) {
			
		}
	}
	/**本序列和原始序列有交叠，只有突出来的部分大于该值才会计入计算 <br>
	 * -------------------------<br>
	 *  -----------------------------------------------<br>
	 *  --------------------------|<----- LEN----->|<br>
	 */
	private static final int LEN_OVERLAP_OUT = 5000;
	
	/**
	 * 研究目的序列是否已经在里面了，如果不在，是否要加上去
	 * @param start
	 * @param end
	 */
	private void setInterval(int start1, int end1, int start2, int end2) {
		/**
		 * 第一步
		 */
		if (startSeq1 == 0 && endSeq1 ==0) {
			startSeq1 = start1; endSeq1 = end1;
			startSeq2 = start2; endSeq2 = end2;
			if (start2 == 1) {
				booStartConfirm = true;
				booEndConfirm = true;
			}
		}
		
		
		
		
		/**本序列包含在原始序列内
		 *           --------------------------
		 *                     -----------
		 */
		if (start2 >= startSeq2 && end2 <= endSeq2) {
			return;
		}
		
		
		/**本序列和原始序列有交叠
		 * -------------------------
		 *                    -----------------------------
		 */
		else if (start2 >= startSeq2 && end2 >= endSeq2) {
			if (end2 - endSeq2 > LEN_OVERLAP_OUT) {
				if (condition) {
					
				}
				return;
			}
			else {
				return;
			}
			
		}
		
		
		
		
		return;
	}
	
}


/**
 * lastz中alignment 的信息
 * 用之前先用setTitle设定标题
 * 内置排序比较，首先比较score，然后比较alignLen长度
 * @author zong0jie
 *
 */
class AlignInfo implements Comparable<AlignInfo>
{
	/**
	 * alignment的分数
	 */
	int score = 0; 
	/**
	 * align在第一条链上的长度
	 */
	int alignLen1 = 0;
	/**
	 * align在第二条链上的长度
	 */
	int alignLen2 = 0;
	/**
	 * 第二条链相对于第一条链的方向
	 */
	boolean strand = true;
	/**
	 * align在第一条链的起点
	 */
	int alignStart1 = 0;
	/**
	 * align在第一条链的终点
	 */
	int alignEnd1 = 0;
	/**
	 * align在第二条链的起点
	 */
	int alignStart2 = 0;
	/**
	 * align在第二条链的终点
	 */
	int alignEnd2 = 0;
	/**
	 * 正确匹配个数
	 */
	int matchNum = 0;
	/**
	 * 错误匹配个数
	 */
	int misMatchNum = 0;
	/**
	 * gap个数
	 */
	int gapNum = 0;
	
//	public AlignInfo(int score, int alignLen1, int alignLen2, boolean strand,int alignStart1, int alignEnd1, int alignStart2, int alignEnd2, int matchNum, int misMatchNum, int gapNum) {
//		this.score = score;
//		this.alignLen1 = alignLen1;
//		this.alignLen2 = alignLen2;
//		this.strand = strand;
//		this.alignStart1 = alignStart1;
//		this.alignEnd1 = alignEnd1;
//		this.alignStart2 = alignStart2;
//		this.alignEnd2 = alignEnd2;
//		this.matchNum = matchNum;
//		this.misMatchNum = misMatchNum;
//		this.gapNum = gapNum;
//	}
//	
	static String[] Info = null;
	
	/**
	 * 设定标题，第一步必须的工作
	 */
	public static void setTitle(String title) {
		Info = title.replace("#", "").split("\t");
	}

	/**
	 * 给定标题列和值，填充本类
	 * 用之前先用setTitle设定标题
	 */
	public AlignInfo(String value) {
		String[] ssvalue = value.split("\t");
		for (int i = 0; i < ssvalue.length; i++) {
			setInfo(Info[i], ssvalue[i]);
		}
	}
	private void setInfo(String title, String value) {

		if (title.equals("score")) {
			this.score = Integer.parseInt(value);
		}
		if (title.equals("start1")) {
			this.alignStart1 = Integer.parseInt(value);
		}
		if (title.equals("end1")) {
			this.alignEnd1 = Integer.parseInt(value);
		}
		if (title.equals("length1")) {
			this.alignLen1 = Integer.parseInt(value);
		}
		if (title.equals("strand2")) {
			this.strand = value.equals("+");
		}
		if (title.equals("start2")) {
			this.alignStart2 = Integer.parseInt(value);
		}
		if (title.equals("length2")) {
			this.alignEnd2 = Integer.parseInt(value);
		}
		if (title.equals("nmatch")) {
			this.matchNum = Integer.parseInt(value);
		}
		if (title.equals("nmismatch")) {
			this.misMatchNum = Integer.parseInt(value);
		}
		if (title.equals("ngap")) {
			this.gapNum = Integer.parseInt(value);
		}
	}
	public int getAlignEnd1() {
		return alignEnd1;
	}
	public int getAlignEnd2() {
		return alignEnd2;
	}
	public int getAlignLen1() {
		return alignLen1;
	}
	public int getAlignLen2() {
		return alignLen2;
	}
	public int getAlignStart1() {
		return alignStart1;
	}
	public int getAlignStart2() {
		return alignStart2;
	}
	public int getGapNum() {
		return gapNum;
	}
	public int getMatchNum() {
		return matchNum;
	}
	public int getMisMatchNum() {
		return misMatchNum;
	}
	public int getScore() {
		return score;
	}

	@Override
	public int compareTo(AlignInfo o) {
		if (score< o.getScore()) 
			return -1;
		else if (score > o.getScore()) {
			return 1;
		}
		else {
			if (alignLen1 + alignLen2 < o.getAlignLen1() + o.getAlignEnd2()) {
				return -1;
			}
			else if (alignLen1 + alignLen2 == o.getAlignLen1() + o.getAlignEnd2()) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}
}