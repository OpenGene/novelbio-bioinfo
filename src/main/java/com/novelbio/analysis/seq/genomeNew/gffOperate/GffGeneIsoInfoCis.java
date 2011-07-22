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
	 * 仅仅初始化给查找时用
	 * @param IsoName
	 * @param lsIsoform
	 */
	protected GffGeneIsoInfoCis(String IsoName, ArrayList<int[]> lsIsoform, boolean cis5to3) {
		super(IsoName, lsIsoform, cis5to3);
	}
	
	/**
	 * 查找坐标在第几个外显子或内含子中
	 * 并且指出在是在外显子还是内含子
	 * 是否在UTR中
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
		for(int i = 0; i < lsIsoform.size(); i++)  //一个一个Exon的检查
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(coord <= lsIsoform.get(i)[1] && coord >= lsIsoform.get(i)[0])
			{
				result[0] = COD_LOC_EXON;
				result[1] = i + 1;
				if(coord < ATGsite){        //坐标小于atg，在5‘UTR中,也是在外显子中
					codLocUTR = COD_LOCUTR_5UTR;
				}
				if(coord > UAGsite){       //大于cds起始区，在3‘UTR中
					codLocUTR = COD_LOCUTR_3UTR; 
				}
				break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsIsoform.size() - 2 && coord > lsIsoform.get(i)[1] && coord < lsIsoform.get(i+1)[0])
			{
				flag = true; codLoc = COD_LOC_INTRON;
				numExIntron = i + 1;
				break; // 跳出本转录本的检查，开始上一层的循环，检查下一个转录本
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	}
	
	
	
}
