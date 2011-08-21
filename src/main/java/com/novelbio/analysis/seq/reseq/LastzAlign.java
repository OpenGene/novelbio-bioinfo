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
	 * ��һ����������
	 */
	String seqName1 = "";
	/**
	 * �ڶ�����������
	 */
	String seqName2 = "";
	/**
	 * ��һ�����ĳ���
	 */
	int seqLen1 = 0;
	/**
	 * �ڶ������ĳ���
	 */
	int seqLen2 = 0;
	/**
	 * ����ÿһ��alignment����Ϣ
	 */
	ArrayList<AlignInfo> lsAlignInfos = new ArrayList<AlignInfo>();
	
	HashMap<String, Integer> hashSeqLen = new HashMap<String, Integer>();
	
	
	/**
	 * ָ��align�ı�����ȡ��Ϣ
	 */
	public void readInfo(String AlignFile) {
		TxtReadandWrite txtAlign = new TxtReadandWrite(AlignFile, false);
		//���п���û������Ҳ����lsInfo.size == 0
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
	 * ������������������
	 */
	public void getLenInterval() {
		for (AlignInfo alignInfo : lsAlignInfos) {
			
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * ����������е����䷶Χ
	 */
	public void setSeqLen() {
		
		//TODO: �趨���г���
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}


/**
 * �������ɸѡ�������
 */
class alignResult
{
	/**
	 * �����Ŀ�����е����
	 */
	int startSeq1 = 0;
	/**
	 * �����Ŀ�����е��յ�
	 */
	int endSeq1 = 0;
	/**
	 * ���contig���е����
	 */
	int startSeq2 = 0;
	/**
	 * ���contig���е��յ�
	 */
	int endSeq2 = 0;
	/**
	 * Ŀ�����еĳ���
	 */
	int seq1Len = 0;
	/**
	 * contig�ĳ���
	 */
	int seq2Len = 0;
	
	/**
	 * ȷ��ͷ������ʾͷ���Ϳ����ø�λ��������
	 * false�Ļ���ͷ����Ҫ���NNN����ʾΪǱ��gap
	 */
	boolean booStartConfirm = false;
	/**
	 * ȷ��β������ʾβ���Ϳ����ø�λ��������
	 * false�Ļ���β����Ҫ���NNN����ʾΪǱ��gap
	 */
	boolean booEndConfirm = false;
	/**
	 * װ���ܹ������жϽ��λ���alignment��Ϣ
	 */
	ArrayList<AlignInfo> lsAlignInfo = new ArrayList<AlignInfo>();
	/**
	 * ��reads�Ƿ���������װ
	 */
	boolean booAssemble = false;
	
	/**
	 * ����ԭʼ��������Ϣ������󷵻�mapping�Ľ��
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
	 * �ж������ArrayList-AlignInfo �Ƿ������Ҫ�ж������ǣ�
	 * ��������Ƭ��������ص���--������һ�������г���70%�ص���������һ�����Ͼ��볬��query���ĳ��ȣ���ô����Ҫ�˹��ж���
	 * @param lsAlignInfoRaw �Ѿ��Ź�����
	 * @return
	 */
	private boolean judgeAlignInfo(ArrayList<AlignInfo> lsAlignInfoRaw) {
		Collections.sort(lsAlignInfoRaw, new Comp());
		
		return true;
	}
	
	
	
	
	
	
	private void calLongestPath(ArrayList<AlignInfo> lsAlignInfoRaw,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph,int seed)
	{
		//����score��������
		Collections.sort(lsAlignInfoRaw, new Comp());
		for (int i = 0; i < Math.min(seed,lsAlignInfoRaw.size()); i++) {

		}
	}
	/**
	 * ����ĳ��align�ķ���������Խ��˵��Խ����
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
	 * ָ��һ��alignInfo�����ظ�����ͼ�У��ýڵ�����/��������һ���ߡ�
	 * ���û���򷵻�null
	 * @param alignInfo
	 * @param directedSparseGraph
	 * @param Down true, �õ�����Σ� false���õ������
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**�����к�ԭʼ�����н�����ֻ��ͻ�����Ĳ��ִ��ڸ�ֵ�Ż������� <br>
	 * -------------------------<br>
	 *  -----------------------------------------------<br>
	 *  --------------------------|<----- LEN----->|<br>
	 */
	private static final int LEN_OVERLAP_OUT = 5000;
	
}


/**
 * lastz��alignment ����Ϣ
 * ��֮ǰ����setTitle�趨����
 * ��������Ƚϣ�����query��������������
 * @author zong0jie
 *
 */
class AlignInfo implements Comparable<AlignInfo>
{
	/**
	 * alignment�ķ���
	 */
	int score = 0; 
	/**
	 * align�ڵ�һ�����ϵĳ���
	 */
	int alignLen1 = 0;
	/**
	 * align�ڵڶ������ϵĳ���
	 */
	int alignLen2 = 0;
	/**
	 * �ڶ���������ڵ�һ�����ķ���
	 */
	boolean strand = true;
	/**
	 * align�ڵ�һ���������
	 */
	int alignStart1 = 0;
	/**
	 * align�ڵ�һ�������յ�
	 */
	int alignEnd1 = 0;
	/**
	 * align�ڵڶ����������
	 */
	int alignStart2 = 0;
	/**
	 * align�ڵڶ��������յ�
	 */
	int alignEnd2 = 0;
	/**
	 * ��ȷƥ�����
	 */
	int matchNum = 0;
	/**
	 * ����ƥ�����
	 */
	int misMatchNum = 0;
	/**
	 * gap����
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
	 * �趨���⣬��һ������Ĺ���
	 */
	public static void setTitle(String title) {
		Info = title.replace("#", "").split("\t");
	}

	/**
	 * ���������к�ֵ����䱾��
	 * ��֮ǰ����setTitle�趨����
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
	 * ����alignstart2��alignEnd2����
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
 * ����align����һ���һ��
 * @author zong0jie
 *
 */
class AlignTogether implements Comparable<AlignTogether>
{
	AlignInfo alignInfoStart = null;
	AlignInfo alignInfoEnd = null;
	double score = 0;
	/**
	 * ��ģ�����ϣ�����align��β���ľ���
	 */
	int alignDist1 = 0;
	/**
	 * ��query���ϣ�����align��β���ľ���
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
	 * �������align֮��ķ�����Ϊ���������������ԽԶ����Խ�ͣ�gap�ļ��㷽��Ϊ
	 * 1һ��gapΪ1���ڶ���Ϊ0.95��������Ϊ0.90����ô������
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
	 * ��÷�����Ϊ����
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


