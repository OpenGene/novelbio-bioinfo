package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;

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
		aaa
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
		for (AlignInfo alignInfo : lsAlignInfoRaw) {
			
		}
		
		
		
		
		
		
	}
	
	private void copeAlign(AlignInfo alignInfo)
	{
		if (alignInfo.get) {
			
		}
	}
	/**�����к�ԭʼ�����н�����ֻ��ͻ�����Ĳ��ִ��ڸ�ֵ�Ż������� <br>
	 * -------------------------<br>
	 *  -----------------------------------------------<br>
	 *  --------------------------|<----- LEN----->|<br>
	 */
	private static final int LEN_OVERLAP_OUT = 5000;
	
	/**
	 * �о�Ŀ�������Ƿ��Ѿ��������ˣ�������ڣ��Ƿ�Ҫ����ȥ
	 * @param start
	 * @param end
	 */
	private void setInterval(int start1, int end1, int start2, int end2) {
		/**
		 * ��һ��
		 */
		if (startSeq1 == 0 && endSeq1 ==0) {
			startSeq1 = start1; endSeq1 = end1;
			startSeq2 = start2; endSeq2 = end2;
			if (start2 == 1) {
				booStartConfirm = true;
				booEndConfirm = true;
			}
		}
		
		
		
		
		/**�����а�����ԭʼ������
		 *           --------------------------
		 *                     -----------
		 */
		if (start2 >= startSeq2 && end2 <= endSeq2) {
			return;
		}
		
		
		/**�����к�ԭʼ�����н���
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
 * lastz��alignment ����Ϣ
 * ��֮ǰ����setTitle�趨����
 * ��������Ƚϣ����ȱȽ�score��Ȼ��Ƚ�alignLen����
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