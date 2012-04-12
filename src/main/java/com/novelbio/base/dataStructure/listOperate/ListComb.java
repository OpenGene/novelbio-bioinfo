package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * ����listabs�ϲ���һ��
 * ���ڲ���Ԫ�ؼ�Ϊ
 * @author zong0jie
 *
 */
public class ListComb<T extends ListDetailAbs> extends ListAbs<ListDetailComb<T>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5877021643535236697L;
	private static Logger logger = Logger.getLogger(ListComb.class);
	/**
	 * ȫ����Ƚϵ�listabs��Ϣ
	 */
	ArrayList<ListAbs<T>> lsAllListAbs = new ArrayList<ListAbs<T>>();
	ListAbs<T> lsAllID = new ListAbs<T>();
	/**
	 * ��������exon�ϲ���ı߽�
	 */
	ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
	public void addListAbs(ListAbs<T> lsListAbs) {
		if (isCis5to3() == null) {
			setCis5to3(lsListAbs.isCis5to3());
		}
		else if (lsListAbs.isCis5to3() != null && isCis5to3() != lsListAbs.isCis5to3()) {
			logger.error("��������ͬ��list���ܽ��бȽ�");
		}
		lsAllListAbs.add(lsListAbs);
	}
	
	
	boolean copelist = false;
	
	/**
	 * ������Ķ���ListAbs�������Ҫ�ĸ�ʽ�����Ұ���element���зֶ�
	 */
	private void copeList()
	{
		if (copelist) {
			return;
		}
		for (ListAbs<T> lsAbs : lsAllListAbs) {
			for (T ele : lsAbs) {
				lsAllID.add(ele);
			}
		}
		lsAllID.sort();
		combExon();
		setExonCluster();
		copelist = true;
	}
	
	/**
	 * �����������exonlist�ϲ�����ü���������exon�����ڷֶ�
	 */
	private void combExon()
	{
		lsExonBounder.clear();
		T exonOld =  lsAllID.get(0);
		int[] exonBoundOld = new int[]{exonOld.getStartCis(), exonOld.getEndCis()};
		boolean allFinal = false;//���һ��exon�Ƿ���Ҫ�����list��
		lsExonBounder.add(exonBoundOld);
		for (int i = 1; i < lsAllID.size(); i++) {
			T ele = lsAllID.get(i);
			int[] exonBound = new int[]{ele.getStartCis(), ele.getEndCis()};
			if (cis5to3 )
			{
				if (exonBound[0] <= exonBoundOld[1]) {
					if (exonBound[1] > exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
			else {
				if (exonBound[0] >= exonBoundOld[1]) {
					if (exonBound[1] < exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
		}
	}
	
	/**
	 * ��������Ż�
	 * �����
	 * ���շ���õı߽�exon����ÿ��ת¼�����л���
	 */
	private void setExonCluster()
	{
		for (int[] exonBound : lsExonBounder) {
			ListDetailComb<T> elementComb = new ListDetailComb<T>(); //ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (int m = 0; m < lsAllListAbs.size(); m ++) {
				ListAbs<T> lsAbs = lsAllListAbs.get(m);
				if (lsAbs.isCis5to3() != isCis5to3()) {
					logger.error("����һ�£����ܱȽ�");
				}
				ArrayList<T> lsExonClusterTmp = new ArrayList<T>();
				//��1��ʼ����
				int beforeExonNum = 0;//�����isoform����û������bounder���е�exon����ô��Ҫ��¼��isoform��ǰ������exon��λ�ã����ڲ��ҿ����û�п����exon
				boolean junc = false;//�����isoform����û������bounder���е�exon����ô����Ҫ��¼������exon��λ�ã��ͽ����flag����Ϊtrue
				for (int i = 0; i < lsAbs.size(); i++) {
					T ele = lsAbs.get(i);
					if (isCis5to3()) {
						if (ele.getEndCis() < exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() >= exonBound[0] && ele.getEndCis() <= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() > exonBound[1]) {
							//��������ڱ��߽磬˵����ȥ�ˣ���ô��lsExonClusterTmp�ж���û��û������ʾ�������ж�����ʾû������
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
					else {
						if (ele.getEndCis() > exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() <= exonBound[0] && ele.getEndCis() >= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() < exonBound[1]) {
							//��������ڱ��߽磬˵����ȥ�ˣ���ô��lsExonClusterTmp�ж���û��û������ʾ�������ж�����ʾû������
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
				}
				if (lsExonClusterTmp.size() > 0) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum+1, beforeExonNum + lsExonClusterTmp.size() );
				}
				else if (junc && beforeExonNum < lsAbs.size()) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum, -beforeExonNum );
				}
			}
			add(elementComb);
		}
	}
	
	/**
	 * �����в����exonϵ��
	 * @return
	 */
	public ArrayList<ListDetailComb<T>> getDifExonCluster() {
		copeList();
		ArrayList<ListDetailComb<T>> lsDifExon = new ArrayList<ListDetailComb<T>>();
		for (ListDetailComb<T> elementComb : this) {
			if (!elementComb.isSameEle()) {
				lsDifExon.add(elementComb);
			}
		}
		return lsDifExon;
	}
}
