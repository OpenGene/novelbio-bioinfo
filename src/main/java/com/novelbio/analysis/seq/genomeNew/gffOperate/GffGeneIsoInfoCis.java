package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffGeneIsoInfoCis extends GffGeneIsoInfo{

	protected GffGeneIsoInfoCis(GffGeneIsoInfo gffGeneIsoInfo) {
		super(gffGeneIsoInfo);
	}
	public GffGeneIsoInfoCis(String IsoName) {
		super(IsoName);
	}
	/**
	 * ������ʼ��������ʱ��
	 * @param IsoName
	 * @param lsIsoform
	 */
	protected GffGeneIsoInfoCis(String IsoName, ArrayList<int[]> lsIsoform, boolean cis5to3) {
		super(IsoName, lsIsoform, cis5to3);
	}
	
	/**
	 * ���������ڵڼ��������ӻ��ں�����
	 * ����ָ�������������ӻ����ں���
	 * �Ƿ���UTR��
	 */
	protected int[] codSearchNum(int coord) {
		int[] result = new int[2];
		if (    coord < lsIsoform.get(0)[0] || 
				coord > lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
			result[0] = COD_LOC_OUT;
			result[1] = -10;
			return result;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		for(int i = 0; i < lsIsoform.size(); i++)  //һ��һ��Exon�ļ��
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(coord <= lsIsoform.get(i)[1] && coord >= lsIsoform.get(i)[0])
			{
				result[0] = COD_LOC_EXON;
				result[1] = i + 1;
				if(coord < ATGsite){        //����С��atg����5��UTR��,Ҳ������������
					codLocUTR = COD_LOCUTR_5UTR;
				}
				if(coord > UAGsite){       //����cds��ʼ������3��UTR��
					codLocUTR = COD_LOCUTR_3UTR; 
				}
				break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsIsoform.size() - 2 && coord > lsIsoform.get(i)[1] && coord < lsIsoform.get(i+1)[0])
			{
				flag = true; codLoc = COD_LOC_INTRON;
				numExIntron = i + 1;
				break; // ������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	}
	
	
	
}
