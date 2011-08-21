package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.novelbio.base.dataOperate.TxtReadandWrite;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

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
		Graph<AlignInfo, AlignTogether> dircteWightGraph = new DirectedSparseGraph<AlignInfo, AlignTogether>();
		
		for (AlignInfo alignInfo : lsAlignInfoRaw) {
			dircteWightGraph.addVertex(alignInfo);
		}
		for (int i = 0; i < lsAlignInfoRaw.size(); i++) {
			for (int j = i; j < lsAlignInfoRaw.size(); j++) {
				AlignInfo alignInfoStart = lsAlignInfoRaw.get(i);
				AlignInfo alignInfoEnd = lsAlignInfoRaw.get(j);
				if (alignInfoStart.getAlignEnd2() > alignInfoEnd.getAlignEnd2()) {
					continue;
				}
				dircteWightGraph.addEdge(new AlignTogether(alignInfoStart, alignInfoEnd), alignInfoStart, alignInfoEnd);
			}
		}
		
		
		
	}
	
	/**
	 * 判断输入的ArrayList-AlignInfo 是否合理，主要判断依据是，
	 * 最大的两个片段如果是重叠的--在任意一条链上有超过70%重叠，而在另一条链上距离超过query链的长度，那么就需要人工判断了
	 * @param lsAlignInfoRaw 已经排过序了
	 * @return
	 */
	private boolean judgeAlignInfo(ArrayList<AlignInfo> lsAlignInfoRaw) {
		Collections.sort(lsAlignInfoRaw, new Comp());
		
		return true;
	}
	
	
	
	
	
	
	private void calLongestPath(ArrayList<AlignInfo> lsAlignInfoRaw,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph,int seed)
	{
		//按照score进行排序
		Collections.sort(lsAlignInfoRaw, new Comp());
		for (int i = 0; i < Math.min(seed,lsAlignInfoRaw.size()); i++) {

		}
	}
	/**
	 * 计算某个align的分数，分数越高说明越可信
	 * @return
	 */
	private int calScore(ArrayList<AlignTogether> lsAlignTogethers)
	{
		int score = lsAlignTogethers.get(0).getAlignInfoStart().getScore();
		for (AlignTogether alignTogether : lsAlignTogethers) {
			score = score + alignTogether.getScore() + alignTogether.getAlignInfoEnd().getScore();
		}
		return score;
	}

	private ArrayList<AlignTogether> getLongestPath(AlignInfo alignInfoFirst,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph) {
		ArrayList<AlignTogether> lsAlignTogethersUp = new ArrayList<AlignTogether>();
		ArrayList<AlignTogether> lsAlignTogethersDown = new ArrayList<AlignTogether>();
		AlignTogether alignTogetherTmp = null;
		while ((alignTogetherTmp = calLNextBigPath(alignInfoFirst, directedSparseGraph, true)) != null) {
			lsAlignTogethersDown.add(alignTogetherTmp);
			alignInfoFirst = alignTogetherTmp.getAlignInfoEnd();
		}
		
		while ((alignTogetherTmp = calLNextBigPath(alignInfoFirst, directedSparseGraph, false)) != null) {
			lsAlignTogethersUp.add(0,alignTogetherTmp);
			alignInfoFirst = alignTogetherTmp.getAlignInfoStart();
		}
		lsAlignTogethersUp.addAll(lsAlignTogethersDown);
		return lsAlignTogethersUp;
	}
	
	
	/**
	 * 指定一个alignInfo，返回该有向图中，该节点上游/下游最大的一条边。
	 * 如果没有则返回null
	 * @param alignInfo
	 * @param directedSparseGraph
	 * @param Down true, 该点的下游， false，该点的上游
	 * @return
	 */
	private AlignTogether calLNextBigPath(AlignInfo alignInfo, DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph, boolean Down) {
		List<AlignTogether> lsAlignTogether = new ArrayList<AlignTogether>();
		Collection<AlignTogether> colAlign = null;
		if (Down) 
			colAlign = directedSparseGraph.getOutEdges(alignInfo);
		else
			colAlign = directedSparseGraph.getInEdges(alignInfo);
		if (colAlign == null || colAlign.size() == 0) {
			return null;
		}
		for (AlignTogether alignTogether2 : colAlign) {
			lsAlignTogether.add(alignTogether2);
		}
		Collections.sort(lsAlignTogether);
		return lsAlignTogether.get(lsAlignTogether.size() - 1);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**本序列和原始序列有交叠，只有突出来的部分大于该值才会计入计算 <br>
	 * -------------------------<br>
	 *  -----------------------------------------------<br>
	 *  --------------------------|<----- LEN----->|<br>
	 */
	private static final int LEN_OVERLAP_OUT = 5000;
	
}


/**
 * lastz中alignment 的信息
 * 用之前先用setTitle设定标题
 * 内置排序比较，按照query序列起点进行排序
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

	/**
	 * 按照alignstart2和alignEnd2排序
	 */
	@Override
	public int compareTo(AlignInfo o) {
		if (alignStart2 < o.getAlignStart2()) 
			return -1;
		else if (alignStart2 > o.getAlignStart2()) {
			return 1;
		}
		else {
			if (alignEnd2 < o.getAlignEnd2()) {
				return -1;
			}
			else if (alignEnd2 == o.getAlignEnd2()) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}
}

class Comp implements Comparator<AlignInfo>
{
	@Override
	public int compare(AlignInfo o1, AlignInfo o2) {
		if (o1.getScore() < o2.getScore())
			return -1;
		else if (o1.getScore() > o2.getScore()) {
			return 1;
		} else {
			if (o1.getAlignLen1() + o1.getAlignLen2() < o2.getAlignLen1()
					+ o2.getAlignEnd2()) {
				return -1;
			} else if (o1.getAlignLen1() + o1.getAlignLen2() == o2
					.getAlignLen1() + o2.getAlignEnd2()) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}


/**
 * 两个align连在一起的一组
 * @author zong0jie
 *
 */
class AlignTogether implements Comparable<AlignTogether>
{
	AlignInfo alignInfoStart = null;
	AlignInfo alignInfoEnd = null;
	double score = 0;
	/**
	 * 在模板链上，两个align首尾相距的距离
	 */
	int alignDist1 = 0;
	/**
	 * 在query链上，两个align首尾相距的距离
	 */
	int alignDist2 = 0;
	public AlignTogether(AlignInfo alignInfoStart, AlignInfo alignInfoEnd) {
		this.alignInfoStart = alignInfoStart;
		this.alignInfoEnd = alignInfoEnd;
		alignDist1 = alignInfoStart.alignEnd1 - alignInfoEnd.alignStart1;
		alignDist2 = alignInfoStart.alignEnd2 - alignInfoEnd.alignStart2; 
		setEdgeScore();
	}
	/**
	 * 获得两个align之间的分数，为负数，两个链离的越远分数越低，gap的计算方法为
	 * 1一个gap为1，第二个为0.95，第三个为0.90，这么加起来
	 */
	private void setEdgeScore()
	{
		for (int i = 0; i < Math.abs(alignDist1); i++) {
			score = score + 2/(1+ (double)i*0.001);
		}
		for (int i = 0; i < Math.abs(alignDist2); i++) {
			score = score + 2/(1+ (double)i*0.001);
		}
	}
	
	public AlignInfo getAlignInfoStart() {
		return alignInfoStart;
	}
	public AlignInfo getAlignInfoEnd() {
		return alignInfoEnd;
	}
	public int getAlignDist1() {
		return alignDist1;
	}
	public int getAlignDist2() {
		return alignDist2;
	}
	/**
	 * 获得分数，为负数
	 * @return
	 */
	public int getScore() {
		return -(int)score;
	}
	@Override
	public int compareTo(AlignTogether o) {
		Integer a = alignInfoStart.score + alignInfoEnd.score + getScore();
		Integer b = o.getAlignInfoStart().getScore() + o.getAlignInfoEnd().getScore() + o.getScore();
		return a.compareTo(b);
	}
	
	
}


