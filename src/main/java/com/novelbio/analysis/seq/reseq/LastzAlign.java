package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
/**
 * 保存LastZ得到的信息，每一行一个lastZ类
 * @author zong0jie
 *
 */
public class LastzAlign {
	Logger logger = Logger.getLogger(LastzAlign.class);
	/**
	 * 第一条链的名字
	 */
	String seqName1 = "";
	/**
	 * 第二条链的名字
	 */
	String seqName2 = "";
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
	int startSeq2 = 2;
	/**
	 * 最后contig序列的终点
	 */
	int endSeq2 = 4;
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
	 * 本reads是否能用于组装
	 */
	boolean booAssemble = false;
	/**
	 * 正反向
	 */
	Boolean cis5to3 = null;
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public boolean isCis5to3() {
		return cis5to3;
	}
	
	
	String fileLastz = "";
	/**
	 * 保存每一个alignment的信息,原始版
	 */
	ArrayList<AlignInfo> lsAlignInfoRaw = new ArrayList<AlignInfo>();
	/**
	 * 装载能够用于判断结合位点的alignment信息
	 */
	ArrayList<AlignInfo> lsAlignInfo = new ArrayList<AlignInfo>();
	public int getStartSeq1() {
		return startSeq1;
	}
	public int getEndSeq1() {
		return endSeq1;
	}
	public int getStartSeq2() {
		return startSeq2;
	}
	public int getEndSeq2() {
		return endSeq2;
	}
	/**
	 * 指定align文本，读取信息
	 */
	public void readInfo(String AlignFile, int seqSubLen,int seqQueryLen) {
		fileLastz = AlignFile;
		seq1Len = seqSubLen;
		seq2Len = seqQueryLen;
		TxtReadandWrite txtAlign = new TxtReadandWrite(AlignFile, false);
		//很有可能没东西，也就是lsInfo.size == 0
		ArrayList<String> lsInfo = txtAlign.readfileLs();
		if (lsInfo.size() == 1) {
			booStartConfirm = false;
			booEndConfirm = false;
			booAssemble = false;
			return;
		}
		setTitleInfo(lsInfo.get(0), lsInfo.get(1));
		AlignInfo.setTitle(lsInfo.get(0));
		for (int i = 1; i < lsInfo.size(); i++) {
			AlignInfo alignInfo = new AlignInfo(lsInfo.get(i));
			if (alignInfo.getScore() < 10000) {
				continue;
			}
			lsAlignInfoRaw.add(alignInfo);
		}
		
		if (lsAlignInfoRaw.size() == 0) {
			booStartConfirm = false;
			booEndConfirm = false;
			booAssemble = false;
			return;
		}
		
		analysis(lsAlignInfoRaw);

		
	}
	/**
	 * 获得该lastz得到的序列信息
	 * @param seq 该scalfold的全长信息，这里会自动被截短以及反向互补为需要的序列
	 * @return
	 */
	public ModifyInfo getModifyInfo(String seq) {
		
		if (cis5to3 != null && !cis5to3) {
			seq = SeqFasta.reservecom(seq);
		}
		if (!booStartConfirm || !booEndConfirm) {
			seq = seq.substring(startSeq2-1,endSeq2);
		}
		ModifyInfo modifyInfo = new ModifyInfo(seqName2, FileOperate.getFileName(fileLastz), seq);
		modifyInfo.setBooEnd(booEndConfirm);
		modifyInfo.setBooStart(booStartConfirm);
		modifyInfo.setStart(startSeq1);
		modifyInfo.setEnd(endSeq1);
		modifyInfo.setAssemle(booAssemble);
		modifyInfo.setStartModify(startSeq2);
		modifyInfo.setEndModify(endSeq2);
		modifyInfo.setCis5to3(cis5to3);
		modifyInfo.setCrossStartSite(crossStartSite);
		modifyInfo.setCrossStartSiteSeq1End(crossStartSiteSeq1End);
		modifyInfo.setCrossStartSiteSeq1Start(crossStartSiteSeq1Start);
		modifyInfo.setCrossStartSiteSeq2Start(crossStartSiteSeq2Start);
		modifyInfo.setCrossStartSiteSeq2End(crossStartSiteSeq2End);
		modifyInfo.setLengthSeq2(seq2Len);
		return modifyInfo;
	}
	
	/**
	 * 设定序列名称，根据标题来确定
	 * @param title
	 * @param value
	 */
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
	 * 还没考虑序列横跨起点
	 * 输入原始的连配信息，排序后返回mapping的结果
	 * @param lsAlignInfoRaw
	 */
	public void analysis(ArrayList<AlignInfo> lsAlignInfoRaw) {
		if (!judgeAlignInfo(lsAlignInfoRaw))
		{
			booAssemble = false;
			return;
		}
		if (lsAlignInfoRaw.size() == 1) {
			startSeq1 = lsAlignInfoRaw.get(0).getAlignStart1();
			endSeq1 = lsAlignInfoRaw.get(0).getAlignEnd1();
			startSeq2 = lsAlignInfoRaw.get(0).getAlignStart2();
			endSeq2 = lsAlignInfoRaw.get(0).getAlignEnd2();
			
			if (startSeq2 == 1)
				booStartConfirm = true;
			else 
				booStartConfirm = false;
			
			if (endSeq2 == seq2Len) 
				booEndConfirm = true;
			else 
				booEndConfirm = false;
			//本alignment的长度还是挺长的
			if (endSeq2 - startSeq2 +1 >= seq2Len * 0.8) {
				booAssemble = true;
			}
			else {
				booAssemble = false;
			}
			cis5to3 = lsAlignInfoRaw.get(0).isCis5to3();
			return;
		}
		Collections.sort(lsAlignInfoRaw);
		DirectedSparseGraph<AlignInfo, AlignTogether> dircteWightGraph = new DirectedSparseGraph<AlignInfo, AlignTogether>();
		
		for (AlignInfo alignInfo : lsAlignInfoRaw) {
			dircteWightGraph.addVertex(alignInfo);
		}
		
		for (int i = 0; i < lsAlignInfoRaw.size()-1; i++) {
			AlignInfo alignInfoStart = lsAlignInfoRaw.get(i);
			for (int j = i+1; j < lsAlignInfoRaw.size(); j++) {
				AlignInfo alignInfoEnd = lsAlignInfoRaw.get(j);
				if ( alignInfoStart.getAlignEnd2() >= alignInfoEnd.getAlignEnd2() || alignInfoStart.getAlignStart2() >= alignInfoEnd.getAlignStart2() ) {
					continue;
				}
//				if (alignInfoStart.getAlignEnd1() == 2095698 && alignInfoEnd.getAlignStart1() == 1) {
//					System.out.println("sss");
//				}
//				if (alignInfoEnd.getAlignStart1() == 1) {
//					System.out.println("eee");
//				}
				dircteWightGraph.addEdge(new AlignTogether(alignInfoStart, alignInfoEnd), alignInfoStart, alignInfoEnd);
			}
		}
		ArrayList<AlignInfo> lsresult = calLongestPath(lsAlignInfoRaw, dircteWightGraph, 5);
		setParam(lsresult);
	}

	/**
	 * 设定参数
	 * @param lsAlignTogethers 已经按照seq2的start位点进行了排序
	 */
	private void setParam(ArrayList<AlignInfo> lsAlignInfo) {
		setCis5To3(lsAlignInfo);
		if (cis5to3 == null) {
			booAssemble = false;
			return;
		}
		while (!booAssemble) 
		{
			AlignInfo alignInfoStart = lsAlignInfo.get(0);
			AlignInfo alignInfoEnd = lsAlignInfo.get(lsAlignInfo.size()-1);

			startSeq1 = alignInfoStart.getAlignStart1();
			startSeq2 = alignInfoStart.getAlignStart2();
			endSeq1 = alignInfoEnd.getAlignEnd1();
			endSeq2 = alignInfoEnd.getAlignEnd2();
			booAssemble = true;
			booStartConfirm = true;
			booEndConfirm = true;
			//还需调整，q和s两者的距离都要计算
			if (
					
				!
				(  alignInfoStart.isCis5to3() == cis5to3 && //方向和总体方向一致
						(
								alignInfoStart.getAlignLen2() > seq2Len*0.2  //第一个片段的长度大于总长度的20%
								|| (lsAlignInfo.size() > 1 //或 对于seq2来说， 第一个片段的长度大于 与其紧邻align的gap 长度的20倍
										&& alignInfoStart.getAlignLen2() > 20* ( lsAlignInfo.get(1).getAlignStart2() - alignInfoStart.getAlignEnd2()) 
										 && 
										 (		//并且对于seq1来说， 第一个片段的长度大于 与其紧邻align的gap 长度的20倍
												 (lsAlignInfo.get(1).getAlignStart1() > alignInfoStart.getAlignEnd1()	&&	
												 alignInfoStart.getAlignLen2() > 20* ( lsAlignInfo.get(1).getAlignStart1() - alignInfoStart.getAlignEnd1()))
												 || ( //并且对于seq1来说， 第一个片段长度大于 其距离起点的长度+与其紧邻align距离终点的长度 的 20倍
														 //实际上这个是判断align横跨首尾
														 lsAlignInfo.get(1).getAlignStart1() < alignInfoStart.getAlignEnd1()	&&	 
														 alignInfoStart.getAlignLen2() > 20* ( lsAlignInfo.get(1).getAlignStart1() -1 + seq1Len - alignInfoStart.getAlignEnd1())
												 )
								    	)
								)
						   )
				)
				
			)
			{
				booAssemble = false;
				lsAlignInfo.remove(0);
			}
			
			if (lsAlignInfo.size() == 0) {
				break;
			}
			if(
					!(	alignInfoEnd.isCis5to3() == cis5to3 &&
							(
									alignInfoEnd.getAlignLen2() > seq2Len*0.2 
									|| (lsAlignInfo.size() > 1 
											&& alignInfoEnd.getAlignLen2() > 20* ( alignInfoEnd.getAlignStart2() - lsAlignInfo.get(lsAlignInfo.size() - 2).getAlignEnd2()) 
											&&
											(      (alignInfoEnd.getAlignStart1() > lsAlignInfo.get(lsAlignInfo.size() - 2).getAlignEnd1()
													&&
													alignInfoEnd.getAlignLen2() > 20* ( alignInfoEnd.getAlignStart1() - lsAlignInfo.get(lsAlignInfo.size() - 2).getAlignEnd1()))
													|| (
															alignInfoEnd.getAlignStart1() < lsAlignInfo.get(lsAlignInfo.size() - 2).getAlignEnd1() &&
															alignInfoEnd.getAlignLen2() > 20* ( alignInfoEnd.getAlignStart1() -1 + seq1Len -  lsAlignInfo.get(lsAlignInfo.size() - 2).getAlignEnd1()) 
													)
											)
										)
									)
					)
				
			)
			{
				booAssemble = false;
				lsAlignInfo.remove(lsAlignInfo.size()-1);
			}
			if (lsAlignInfo.size() == 0) {
				break;
			}
		}
		
		if (lsAlignInfo.size() == 0) {
			return;
		}
		AlignInfo alignInfoStart = lsAlignInfo.get(0);
		AlignInfo alignInfoEnd = lsAlignInfo.get(lsAlignInfo.size()-1);
		if (alignInfoStart.getAlignStart2() != 1) {
			booStartConfirm = false;
		}
		if (alignInfoEnd.getAlignEnd2() != seq2Len) {
			booEndConfirm = false;
		}
		if (alignInfoEnd.getAlignEnd2() - alignInfoStart.getAlignStart2() < seq2Len * 0.7) {
			booAssemble = false;
		}
		//考虑拆分成两个不同的lastZalign
		//专门判断align是否横跨了起点，如果是的话，记录横跨位点
		if (booAssemble && alignInfoEnd.getAlignEnd1() < alignInfoStart.getAlignStart1()) {
			for (int i = 0; i < lsAlignInfo.size()-1; i++) {
				//该位点发生了横跨事件
				if (lsAlignInfo.get(i).getAlignStart1() > lsAlignInfo.get(i+1).getAlignEnd1() && lsAlignInfo.get(i).getAlignEnd1() - lsAlignInfo.get(i+1).getAlignStart1() > seq1Len *0.90 ) {
					crossStartSite = true;
					crossStartSiteSeq1Start = lsAlignInfo.get(i+1).getAlignStart1();
					crossStartSiteSeq1End = lsAlignInfo.get(i).getAlignEnd1();
					
					crossStartSiteSeq2Start = lsAlignInfo.get(i+1).getAlignStart2();
					crossStartSiteSeq2End = lsAlignInfo.get(i).getAlignEnd2();
				}
			}
		}
	}
	/**
	 * 发生了横跨起点事件
	 */
	boolean crossStartSite = false;
	public boolean isCrossStartSite() {
		return crossStartSite;
	}
	int crossStartSiteSeq2End = -1;
	int crossStartSiteSeq2Start = -1;
	int crossStartSiteSeq1Start = -1;
	int crossStartSiteSeq1End = -1;
	
	
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
	 * 横跨起点的seq1序列的最尾端，即星号位置，闭区间      --------------------          --------------*
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
	
	
	
	/**
	 * 判断输入的ArrayList-AlignInfo 是否合理，主要判断依据是，
	 * 最大的两个片段如果是重叠的--在任意一条链上有超过60%重叠，而在另一条链上距离超过query链的长度，那么就需要人工判断了
	 * @param lsAlignInfoRaw 已经排过序了
	 * @return
	 */
	private boolean judgeAlignInfo(ArrayList<AlignInfo> lsAlignInfoRaw) {
		if (lsAlignInfoRaw.size() <= 1) {
			return true;
		}
		Collections.sort(lsAlignInfoRaw, new Comp());
		AlignInfo alignInfo1 = lsAlignInfoRaw.get(0);
		AlignInfo alignInfo2 = lsAlignInfoRaw.get(1);
		double persentage = (double)Math.abs(alignInfo1.getAlignLen2() - alignInfo2.getAlignLen2())/Math.max(alignInfo1.getAlignLen2(),alignInfo2.getAlignLen2());
		if (persentage >= 0.6 && (alignInfo1.getScore()/alignInfo2.getScore() > 0.7 && alignInfo1.getScore()/alignInfo2.getScore() < 1.4) ) {//两个重叠超过0.6
			//第一条reads
			if (alignInfo1.getAlignStart1() < alignInfo2.getAlignStart1() && alignInfo2.getAlignStart1() - alignInfo1.getAlignEnd1()> seq2Len ) {
				return false;
			}
			else if (alignInfo1.getAlignStart1() > alignInfo2.getAlignStart1() && alignInfo1.getAlignStart1() - alignInfo2.getAlignEnd1()> seq2Len) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 选择排名靠前的几条align作为seed，组建最长align，并找出这些中最长的一条align，并返回
	 * @param lsAlignInfoRaw
	 * @param directedSparseGraph
	 * @param seed 选择最长的几条align
	 * @return
	 * 结果按照seq2的start位点排序
	 */
	private ArrayList<AlignInfo> calLongestPath(ArrayList<AlignInfo> lsAlignInfoRaw,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph,int seed)
	{
		HashMap<Integer, ArrayList<AlignTogether>> hashResult = new HashMap<Integer, ArrayList<AlignTogether>>();
		ArrayList<Integer> lsScore = new ArrayList<Integer>();//保存每个align组合的打分
		//按照score进行排序
		Collections.sort(lsAlignInfoRaw, new Comp());
		for (int i = 0; i < Math.min(seed,lsAlignInfoRaw.size()); i++) {
			ArrayList<AlignTogether> lsAlignTogethersTmp = getLongestPath(lsAlignInfoRaw.get(i), directedSparseGraph);
			int score = calScore(lsAlignTogethersTmp);
			hashResult.put(score, lsAlignTogethersTmp);
			lsScore.add(score);
		}
		Collections.sort(lsScore);
		ArrayList<AlignTogether> lsAlignTogethersResult = hashResult.get(lsScore.get(lsScore.size()-1));
		ArrayList<AlignInfo> lsResult = new ArrayList<AlignInfo>();
		for (int i = 0; i < lsAlignTogethersResult.size(); i++) {
			AlignTogether alignTogether = lsAlignTogethersResult.get(i);
			if (i == 0) {
				lsResult.add(alignTogether.getAlignInfoStart());
			}
			
			if (alignTogether.isPaired() == false) {
				if (lsAlignTogethersResult.size() > 1) {
					logger.error("只有一个align，但是alignTogether却有很多");
					break;
				}
				continue;
			}
			lsResult.add(alignTogether.getAlignInfoEnd());
		}
		
		return lsResult;
	}
	
	
	
	private void setCis5To3(ArrayList<AlignInfo> lsAlignInfo) {
		int scoreCis = 0;
		int scoreTrans = 0;
		for (AlignInfo alignInfo : lsAlignInfo) {
			if (alignInfo.isCis5to3()) 
				scoreCis = scoreCis + alignInfo.getScore();
			else 
				scoreTrans = scoreTrans + alignInfo.getScore();
		}
		
		
		if ( (scoreCis == 0 || scoreTrans/scoreCis >= 3) ) {
			cis5to3 = false;
		}
		else if (scoreTrans == 0 || scoreCis/scoreTrans >= 3) {
			cis5to3 = true;
		}
		else {
			cis5to3 = null;
		}
	}
	
	/**
	 * 计算某个align的分数，分数越高说明越可信
	 * @return
	 */
	private int calScore(ArrayList<AlignTogether> lsAlignTogethers)
	{
		int score = lsAlignTogethers.get(0).getAlignInfoStart().getScore();
		if (lsAlignTogethers.size() > 1 || lsAlignTogethers.get(0).isPaired() == true ) {
			for (AlignTogether alignTogether : lsAlignTogethers) {
				score = score + alignTogether.getScore() + alignTogether.getAlignInfoEnd().getScore();
			}
		}
		return score;
	}

	private ArrayList<AlignTogether> getLongestPath(AlignInfo alignInfoFirst,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph) {
		ArrayList<AlignTogether> lsAlignTogethersUp = new ArrayList<AlignTogether>();
		ArrayList<AlignTogether> lsAlignTogethersDown = new ArrayList<AlignTogether>();
		AlignTogether alignTogetherTmp = null;
		AlignInfo alignInfoFirstCopy = alignInfoFirst.copy();
		
		while ((alignTogetherTmp = calLNextBigPath(alignInfoFirstCopy, directedSparseGraph, true)) != null) {
			lsAlignTogethersDown.add(alignTogetherTmp);
			alignInfoFirstCopy = alignTogetherTmp.getAlignInfoEnd();
		}
		
		while ((alignTogetherTmp = calLNextBigPath(alignInfoFirst, directedSparseGraph, false)) != null) {
			lsAlignTogethersUp.add(0,alignTogetherTmp);
			alignInfoFirst = alignTogetherTmp.getAlignInfoStart();
		}
		lsAlignTogethersUp.addAll(lsAlignTogethersDown);
		//如果都没找到，说明该alignInfoFirst就是最大的一个align，直接用就好
		if (lsAlignTogethersUp.size() == 0) {
			AlignTogether alignTogether = new AlignTogether(alignInfoFirst, null);
			lsAlignTogethersUp.add(alignTogether);
		}
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
	private AlignTogether calLNextBigPath(AlignInfo alignInfo, DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph, boolean Down) 
	{
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
		return lsAlignTogether.get(0);
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
 * 从小到大排列
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
	
	public boolean isCis5to3() {
		return strand;
	}
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

	private AlignInfo() {
	}
	
	private void setInfo(String title, String value) {
		if (title.equals("score")) {
			this.score = Integer.parseInt(value);
		}
		else if (title.equals("start1")) {
			this.alignStart1 = Integer.parseInt(value);
		}
		else if (title.equals("end1")) {
			this.alignEnd1 = Integer.parseInt(value);
		}
		else if (title.equals("length1")) {
			this.alignLen1 = Integer.parseInt(value);
		}
		else if (title.equals("strand2")) {
			this.strand = value.equals("+");
		}
		else if (title.equals("start2")) {
			this.alignStart2 = Integer.parseInt(value);
		}
		else if (title.equals("end2")) {
			this.alignEnd2 = Integer.parseInt(value);
		}
		else if (title.equals("length2")) {
			this.alignLen2 = Integer.parseInt(value);
		}
		else if (title.equals("nmatch")) {
			this.matchNum = Integer.parseInt(value);
		}
		else if (title.equals("nmismatch")) {
			this.misMatchNum = Integer.parseInt(value);
		}
		else if (title.equals("ngap")) {
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
	
	public AlignInfo copy() {
		AlignInfo alignInfo = new AlignInfo();
		alignInfo.alignEnd1 = this.alignEnd1;
		alignInfo.alignEnd2 = this.alignEnd2;
		alignInfo.alignLen1 = this.alignLen1;
		alignInfo.alignLen2 = this.alignLen2;
		alignInfo.alignStart1 = this.alignStart1;
		alignInfo.alignStart2 = this.alignStart2;
		alignInfo.gapNum = this.gapNum;
		alignInfo.matchNum = this.matchNum;
		alignInfo.misMatchNum = this.misMatchNum;
		alignInfo.score = this.score;
		alignInfo.strand = this.strand;
		
		return alignInfo;
	}
}
/**
 * 降序排列
 * @author zong0jie
 *
 */
class Comp implements Comparator<AlignInfo>
{
	@Override
	public int compare(AlignInfo o1, AlignInfo o2) {
		if (o1.getScore() < o2.getScore())
			return 1;
		else if (o1.getScore() > o2.getScore()) {
			return -1;
		} else {
			if (o1.getAlignLen1() + o1.getAlignLen2() < o2.getAlignLen1()
					+ o2.getAlignEnd2()) {
				return 1;
			} else if (o1.getAlignLen1() + o1.getAlignLen2() == o2
					.getAlignLen1() + o2.getAlignEnd2()) {
				return 0;
			} else {
				return -1;
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
	 * 说明没有配对，只有alignInfoStart存在
	 */
	boolean boopair = true;
	/**
	 * 在模板链上，两个align首尾相距的距离
	 */
	int alignDist1 = 0;
	/**
	 * 在query链上，两个align首尾相距的距离
	 */
	int alignDist2 = 0;
	/**
	 * 
	 * @param alignInfoStart
	 * @param alignInfoEnd 如果这个为null，说明只有一个不成对，最后pair设置为false
	 */
	public AlignTogether(AlignInfo alignInfoStart, AlignInfo alignInfoEnd) {
		this.alignInfoStart = alignInfoStart;
		if (alignInfoEnd == null) {
			boopair = false;
			return;
		}
		
		
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
	 * false说明没有配对，只有alignInfoStart存在
	 */
	public boolean isPaired() {
		return boopair;
	}
	/**
	 * 获得分数，为负数
	 * @return
	 */
	public int getScore() {
		return -(int)score;
	}
	/**
	 * 降序排列
	 */
	@Override
	public int compareTo(AlignTogether o) {
		Integer a = alignInfoStart.score + alignInfoEnd.score + getScore();
		Integer b = o.getAlignInfoStart().getScore() + o.getAlignInfoEnd().getScore() + o.getScore();
		return -a.compareTo(b);
	}
	
	
}


