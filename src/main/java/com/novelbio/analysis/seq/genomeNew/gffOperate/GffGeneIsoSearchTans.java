package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class GffGeneIsoSearchTans extends GffGeneIsoSearch{
	private static final Logger logger = Logger.getLogger(GffGeneIsoSearchTans.class);
	public GffGeneIsoSearchTans(GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		super(gffGeneIsoInfo, coord);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void codSearchNum() {

		if (    coord < lsIsoform.get(0)[0] || 
				coord > lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
			codLoc = COD_LOC_OUT;
		}
		//    5-1  5-0    4-1  4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1 0-tss  cood
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		cod2ATG = ATGsite -coord; //  GTAnnnC   CnnngtA
		cod2UAG = UAGsite - coord; //GaunnC    CnnGAU
		cod2TSS = lsIsoform.get(0)[0] - coord;
		cod2TES =  lsIsoform.get(lsIsoform.size() - 1)[1] - coord;
		boolean flag = false; //false��ʾcoord�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (codLoc == COD_LOC_OUT) {
			return;
		}
		for(int i = 0; i < lsIsoform.size(); i++)  //һ��һ��Exon�ļ��
		{
			if (coord > lsIsoform.get(lsIsoform.size()-1)[0]) {
				 logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			}
			//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			if(coord >= lsIsoform.get(i)[1] && coord <= lsIsoform.get(i)[0]) {
				flag = true;
				codLoc = COD_LOC_EXON;
				numExIntron = i + 1;
				if (coord > ATGsite) { // �������atg����5��UTR��,Ҳ������������
					codLocUTR = COD_LOCUTR_5UTR;
				}
				if (coord < UAGsite) { // ����cds��ʼ������3��UTR��
					codLocUTR = COD_LOCUTR_3UTR;
				}
				break;// ������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			}
			//		    5-1 5-0  cood  4-1 uag 4-0     3-1 3-0         2-1 2-0    1-1 gta 1-0    0-1 0-tss  cood
			else if (i <= lsIsoform.size() - 2 && coord < lsIsoform.get(i)[1] && coord > lsIsoform.get(i+1)[0])
			{
				flag=true; codLoc = COD_LOC_INTRON;
				numExIntron = i + 1;
				break; // ������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
			}
		}
		if (flag == false) {    //�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
			logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	}

	@Override
	protected void setCod2ExInStartEnd() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		if (codLoc == COD_LOC_EXON) {
			cod2ExInStart = lsIsoform.get(NumExon)[0] - coord;//���뱾��������ʼ Cnnn
			cod2ExInEnd = coord - lsIsoform.get(NumExon)[1];//���뱾��������ֹ  nnnC
		}
		else if(codLoc == COD_LOC_INTRON) 
		{   //  5-1 5-0  cood  4-1 uag 4-0     3-1 3-0         2-1 2-0    1-1 gta 1-0  cood  0-1 0-tss  cood
			   cod2ExInEnd = coord - lsIsoform.get(numExIntron)[0] - 1;// ���һ�������� NnnCnn
			   cod2ExInStart = lsIsoform.get(NumExon)[1] - coord -1;// ��ǰһ�������� nnnCnnnN
		}
	}

	@Override
	protected void setCod2UTR5() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
	//  5-1 5-0    4-1 uag 4-0     3-1 gta 3-0         2-1 2-0    1-1 cood 1-0            0-1 0-tss  cood
		for (int i = 0; i < NumExon; i++) {
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
	//  5-1 5-0  cood  4-1 uag 4-0     3-1 3-0         2-1 2-0    1-1 gta  cood 1-0      0-1 0-tss  cood
		if (ATGsite >= lsIsoform.get(NumExon)[1]) //һ��Ҫ���ڵ���
		{
			cod2UTRendmRNA = coord - ATGsite - 1;//GTAnnnC
		}
	//  5-1 5-0  cood  4-1 uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///nnnC
			int m = NumExon+1;
			while ( lsIsoform.get(m)[1] > ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(m)[0] - lsIsoform.get(m)[1] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(m)[0] - ATGsite;//Atgn
			if (ATGsite > lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			}
		}
	}

	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		//  5-1 5-0    4-1 cood  uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		if ( UAGsite <= lsIsoform.get(NumExon)[0])//һ��ҪС�ڵ��� 
		{
			cod2UTRstartmRNA = UAGsite - coord - 1;  //CnnnGAU
		}
		//  5-1 cood 5-0    4-1 uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //Cnnnnn
			int m = NumExon - 1;
		//  5-1 cood 5-0       4-1  4-0        3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
			while (m >= 0 && lsIsoform.get(m)[0] < UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[0] - lsIsoform.get(m)[1] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + UAGsite - lsIsoform.get(m)[1]; //nnnGAU
		}
		/////////////////////utrend//////////////////
		//  5-1 5-0    4-1 cood  4-0     3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		for (int i = lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
		}
		cod2UTRendmRNA = cod2UTRendmRNA + cod2ExInEnd;
	}

	@Override
	protected void setCod2StartEndmRNA() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		cod2TSSmRNA = 0; cod2TESmRNA = 0;
		//  5-1 5-0    4-1 cood  4-0     3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		for (int i = 0; i < NumExon; i--) {
			cod2TSSmRNA = cod2TSSmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
		}
		cod2TESmRNA = cod2TESmRNA + cod2ExInEnd;
	}

	@Override
	protected void setCod2StartEndCDS() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		cod2ATGmRNA = 0; cod2UAGmRNA = 0;
		//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 cood 2-0       1-1 gta 1-0       0-1 0-tss  cood
		if (codLocUTR == COD_LOCUTR_5UTR) {
			cod2ATGmRNA = -(cod2UTRendmRNA + 1);
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			cod2UAGmRNA = cod2UTRstartmRNA + 1;
		}
		//��coord��ATG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_5UTR) {
			//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 cood 2-0       1-1 gta 1-0       0-1 0-tss  cood
			for (int i = 0; i < NumExon; i++) {
				if (lsIsoform.get(i)[1] > ATGsite) {
					continue;
				}
				if (lsIsoform.get(i)[0] >= ATGsite && lsIsoform.get(i)[1] <= ATGsite) {
					cod2ATGmRNA = ATGsite - lsIsoform.get(i)[1] + 1; // Atgnn   nnnnC
					continue;
				}
				cod2ATGmRNA = cod2ATGmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
			}
			cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
		}
		//��coord��UAG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_3UTR) {
			//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
			for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
				if (lsIsoform.get(i)[0] > UAGsite) {
					break;
				}
				if (lsIsoform.get(i)[0] <= UAGsite && lsIsoform.get(i)[1] >= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[0] - UAGsite + 1; // Gaunn nnn nnC
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	/**
	 * ����ĳ����������ڵ��ں�����������Ŀ
	 */
	HashMap<Integer, Integer> hashLocExInNum = new HashMap<Integer, Integer>();
	/**
	 * @param location �õ�����
	 * @return
	 */
	protected int getLocExInNum(int location) {
		if (hashLocExInNum.containsKey(location)) {
			return hashLocExInNum.get(location);
		}

		if (    location > lsIsoform.get(0)[0] || 
				location < lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
//			hashLocExInNum.put(location, 0);  //����ת¼���ڵ����겻�����
			return 0;
		}
		for(int i = 0; i < lsIsoform.size(); i++)  //һ��һ��Exon�ļ��
		{
			//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			if(location >= lsIsoform.get(i)[1] && location <= lsIsoform.get(i)[0]) {
				hashLocExInNum.put(location, i + 1);
				return i + 1;
			}
			//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			else if(i <= lsIsoform.size() - 2 && location < lsIsoform.get(i)[1] && location > lsIsoform.get(i+1)[0]) {
				hashLocExInNum.put(location, -(i + 1));
				return -(i + 1);
			}
		}
		hashLocExInNum.put(location, 0);
		return 0;
	}
	/**
	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
	 */
	HashMap<Integer, Integer> hashLocExInStart = new HashMap<Integer, Integer>();
	/**
	 * ���굽������/�ں��� ������
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;   int exIntronNum = getLocExInNum(location); 	int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			loc2ExInStart = lsIsoform.get(NumExon)[0] - location;//���뱾��������ʼ nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{
//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			loc2ExInStart = lsIsoform.get(NumExon)[1] - location -1;// ��ǰһ�������� NnnnCnnnn
			hashLocExInStart.put(location, loc2ExInStart);
		}
		return loc2ExInStart;
	}
	/**
	 * ����ĳ�����굽���ڵ��ں���/�������յ�ľ���
	 */
	HashMap<Integer, Integer> hashLocExInEnd = new HashMap<Integer, Integer>();
	/**
	 * ���굽������/�ں��� �յ����
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	protected int getLoc2ExInEnd(int location) {
		if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000; int exIntronNum = getLocExInNum(location); int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			 loc2ExInEnd = location - lsIsoform.get(NumExon)[1];//���뱾��������ֹ  Cnnnnnnn
		}
		else if(exIntronNum < 0)
		{
//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			 loc2ExInEnd = location - lsIsoform.get(NumExon)[0] - 1;// ���һ�������� nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	/**
	 * ��������֮��ľ��룬mRNA���棬��loc1��loc2����ʱ�����ظ�������loc1��loc2����ʱ����������
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�-1000000000
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
	 */
	protected int locDistance(int loc1, int loc2) {
		//ע���С�Ƿ���
		int locSmall = Math.max(loc1, loc2);  int locBig = Math.min(loc1, loc2);
		int locSmallExInNum = getLocExInNum(locSmall); int locBigExInNum = getLocExInNum(locBig);
		
		int distance = -1000000000;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) {
			return distance;
		}
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locSmall - locBig;
		}
		else {
//		    5-1 big 5-0    4-1 4-0     3-1 small 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			distance = getLoc2ExInEnd(locSmall) + getLoc2ExInStart(locBig);
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + lsIsoform.get(i)[0] -lsIsoform.get(i)[1] + 1;
			}
		}
		if (loc1 > loc2) {
			return -distance;
		}
		return distance;
	}
	
	/**
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ4λ����N��Loc�غ�ʱΪ0
	 */
	public int getATGdistanceSiteTrans(int location, int mRNAnum) {
		if (getLocExInNum(location) <= 0) {
			return -1;
		}
//	    5-1 big 5-0    4-1 4-0     3-1 small 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
		if (mRNAnum < 0) {
			 if (Math.abs(mRNAnum) <= getLoc2ExInStart(location)) {
				return location + Math.abs(mRNAnum);
			 } 
			 else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getLoc2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					int[] tmpExon = lsIsoform.get(i);
					// һ��һ�������ӵ���ǰ����
					if (remain - (tmpExon[0] - tmpExon[1] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[1] + remain - 1;
					}
				}
				return -1;
			}
		} else {
//		    5-1 num 5-0    4-1 4-0     3-1 loc 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			if (mRNAnum <= getLoc2ExInEnd(location)) {
				return location - mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = mRNAnum - getLoc2ExInEnd(location);
				for (int i = exonNum + 1; i < lsIsoform.size(); i++) {
					int[] tmpExon = lsIsoform.get(i);
					// һ��һ�������ӵ���ǰ����
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[0] - remain + 1;
					}
				}
				return -1;
			}
		}
	}
}
