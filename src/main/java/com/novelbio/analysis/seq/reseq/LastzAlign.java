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
 * ����LastZ�õ�����Ϣ��ÿһ��һ��lastZ��
 * @author zong0jie
 *
 */
public class LastzAlign {
	Logger logger = Logger.getLogger(LastzAlign.class);
	/**
	 * ��һ����������
	 */
	String seqName1 = "";
	/**
	 * �ڶ�����������
	 */
	String seqName2 = "";
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
	int startSeq2 = 2;
	/**
	 * ���contig���е��յ�
	 */
	int endSeq2 = 4;
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
	 * ��reads�Ƿ���������װ
	 */
	boolean booAssemble = false;
	/**
	 * ������
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
	 * ����ÿһ��alignment����Ϣ,ԭʼ��
	 */
	ArrayList<AlignInfo> lsAlignInfoRaw = new ArrayList<AlignInfo>();
	/**
	 * װ���ܹ������жϽ��λ���alignment��Ϣ
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
	 * ָ��align�ı�����ȡ��Ϣ
	 */
	public void readInfo(String AlignFile, int seqSubLen,int seqQueryLen) {
		fileLastz = AlignFile;
		seq1Len = seqSubLen;
		seq2Len = seqQueryLen;
		TxtReadandWrite txtAlign = new TxtReadandWrite(AlignFile, false);
		//���п���û������Ҳ����lsInfo.size == 0
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
	 * ��ø�lastz�õ���������Ϣ
	 * @param seq ��scalfold��ȫ����Ϣ��������Զ����ض��Լ����򻥲�Ϊ��Ҫ������
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
	 * �趨�������ƣ����ݱ�����ȷ��
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
	 * ��û�������к�����
	 * ����ԭʼ��������Ϣ������󷵻�mapping�Ľ��
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
			//��alignment�ĳ��Ȼ���ͦ����
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
	 * �趨����
	 * @param lsAlignTogethers �Ѿ�����seq2��startλ�����������
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
			//���������q��s���ߵľ��붼Ҫ����
			if (
					
				!
				(  alignInfoStart.isCis5to3() == cis5to3 && //��������巽��һ��
						(
								alignInfoStart.getAlignLen2() > seq2Len*0.2  //��һ��Ƭ�εĳ��ȴ����ܳ��ȵ�20%
								|| (lsAlignInfo.size() > 1 //�� ����seq2��˵�� ��һ��Ƭ�εĳ��ȴ��� �������align��gap ���ȵ�20��
										&& alignInfoStart.getAlignLen2() > 20* ( lsAlignInfo.get(1).getAlignStart2() - alignInfoStart.getAlignEnd2()) 
										 && 
										 (		//���Ҷ���seq1��˵�� ��һ��Ƭ�εĳ��ȴ��� �������align��gap ���ȵ�20��
												 (lsAlignInfo.get(1).getAlignStart1() > alignInfoStart.getAlignEnd1()	&&	
												 alignInfoStart.getAlignLen2() > 20* ( lsAlignInfo.get(1).getAlignStart1() - alignInfoStart.getAlignEnd1()))
												 || ( //���Ҷ���seq1��˵�� ��һ��Ƭ�γ��ȴ��� ��������ĳ���+�������align�����յ�ĳ��� �� 20��
														 //ʵ����������ж�align�����β
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
		//���ǲ�ֳ�������ͬ��lastZalign
		//ר���ж�align�Ƿ�������㣬����ǵĻ�����¼���λ��
		if (booAssemble && alignInfoEnd.getAlignEnd1() < alignInfoStart.getAlignStart1()) {
			for (int i = 0; i < lsAlignInfo.size()-1; i++) {
				//��λ�㷢���˺���¼�
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
	 * �����˺������¼�
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
	 * �������seq1���е���β�ˣ����Ǻ�λ�ã�������      --------------------          --------------*
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
	
	
	
	/**
	 * �ж������ArrayList-AlignInfo �Ƿ������Ҫ�ж������ǣ�
	 * ��������Ƭ��������ص���--������һ�������г���60%�ص���������һ�����Ͼ��볬��query���ĳ��ȣ���ô����Ҫ�˹��ж���
	 * @param lsAlignInfoRaw �Ѿ��Ź�����
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
		if (persentage >= 0.6 && (alignInfo1.getScore()/alignInfo2.getScore() > 0.7 && alignInfo1.getScore()/alignInfo2.getScore() < 1.4) ) {//�����ص�����0.6
			//��һ��reads
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
	 * ѡ��������ǰ�ļ���align��Ϊseed���齨�align�����ҳ���Щ�����һ��align��������
	 * @param lsAlignInfoRaw
	 * @param directedSparseGraph
	 * @param seed ѡ����ļ���align
	 * @return
	 * �������seq2��startλ������
	 */
	private ArrayList<AlignInfo> calLongestPath(ArrayList<AlignInfo> lsAlignInfoRaw,DirectedSparseGraph<AlignInfo, AlignTogether> directedSparseGraph,int seed)
	{
		HashMap<Integer, ArrayList<AlignTogether>> hashResult = new HashMap<Integer, ArrayList<AlignTogether>>();
		ArrayList<Integer> lsScore = new ArrayList<Integer>();//����ÿ��align��ϵĴ��
		//����score��������
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
					logger.error("ֻ��һ��align������alignTogetherȴ�кܶ�");
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
	 * ����ĳ��align�ķ���������Խ��˵��Խ����
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
		//�����û�ҵ���˵����alignInfoFirst��������һ��align��ֱ���þͺ�
		if (lsAlignTogethersUp.size() == 0) {
			AlignTogether alignTogether = new AlignTogether(alignInfoFirst, null);
			lsAlignTogethersUp.add(alignTogether);
		}
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
 * ��С��������
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
	
	public boolean isCis5to3() {
		return strand;
	}
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
 * ��������
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
	 * ˵��û����ԣ�ֻ��alignInfoStart����
	 */
	boolean boopair = true;
	/**
	 * ��ģ�����ϣ�����align��β���ľ���
	 */
	int alignDist1 = 0;
	/**
	 * ��query���ϣ�����align��β���ľ���
	 */
	int alignDist2 = 0;
	/**
	 * 
	 * @param alignInfoStart
	 * @param alignInfoEnd ������Ϊnull��˵��ֻ��һ�����ɶԣ����pair����Ϊfalse
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
	 * false˵��û����ԣ�ֻ��alignInfoStart����
	 */
	public boolean isPaired() {
		return boopair;
	}
	/**
	 * ��÷�����Ϊ����
	 * @return
	 */
	public int getScore() {
		return -(int)score;
	}
	/**
	 * ��������
	 */
	@Override
	public int compareTo(AlignTogether o) {
		Integer a = alignInfoStart.score + alignInfoEnd.score + getScore();
		Integer b = o.getAlignInfoStart().getScore() + o.getAlignInfoEnd().getScore() + o.getScore();
		return -a.compareTo(b);
	}
	
	
}


