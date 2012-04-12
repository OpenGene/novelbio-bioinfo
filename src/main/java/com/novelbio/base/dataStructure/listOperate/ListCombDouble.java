package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * ����listabs�ϲ���һ��
 * ���ڲ���Ԫ�ؼ�Ϊ
 * @author zong0jie
 *
 */
public class ListCombDouble<T extends ElementAbsDouble> extends ListAbsDouble<ElementCombDouble<T>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4068714755622947354L;
	private static Logger logger = Logger.getLogger(ListCombDouble.class);
	/**
	 * ȫ����Ƚϵ�listabs��Ϣ
	 */
	ArrayList<ListAbsDouble<T>> lsAllListAbsDouble = new ArrayList<ListAbsDouble<T>>();
	ListAbsDouble<T> lsAllIdouble = new ListAbsDouble<T>();
	/**
	 * ��������exon�ϲ���ı߽�
	 */
	ArrayList<double[]> lsExonBounder = new ArrayList<double[]>();
	public void addListAbs(ListAbsDouble<T> lsListAbs) {
		if (isCis5to3() == null) {
			setCis5to3(lsListAbs.isCis5to3());
		}
		else if (lsListAbs.isCis5to3() != null && isCis5to3() != lsListAbs.isCis5to3()) {
			logger.error("��������ͬ��list���ܽ��бȽ�");
		}
		lsAllListAbsDouble.add(lsListAbs);
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
		for (ListAbsDouble<T> lsAbs : lsAllListAbsDouble) {
			for (T ele : lsAbs) {
				lsAllIdouble.add(ele);
			}
		}
		lsAllIdouble.sort();
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
		T exonOld =  lsAllIdouble.get(0);
		double[] exonBoundOld = new double[]{exonOld.getStartCis(), exonOld.getEndCis()};
		lsExonBounder.add(exonBoundOld);
		for (int i = 1; i < lsAllIdouble.size(); i++) {
			T ele = lsAllIdouble.get(i);
			double[] exonBound = new double[]{ele.getStartCis(), ele.getEndCis()};
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
		for (double[] exonBound : lsExonBounder) {
			ElementCombDouble<T> elementComb = new ElementCombDouble<T>(); //ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (int m = 0; m < lsAllListAbsDouble.size(); m ++) {
				ListAbsDouble<T> lsAbs = lsAllListAbsDouble.get(m);
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
	public ArrayList<ElementCombDouble<T>> getDifExonCluster() {
		copeList();
		ArrayList<ElementCombDouble<T>> lsDifExon = new ArrayList<ElementCombDouble<T>>();
		for (ElementCombDouble<T> elementComb : this) {
			if (!elementComb.isSameEle()) {
				lsDifExon.add(elementComb);
			}
		}
		return lsDifExon;
	}
}
