package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
/**
 * �������ʱ��SnnnC<br>
 * S����CΪ5��S��C�غ�ʱ����Ϊ0<br>
 * CnnnATG<br>
 * C��UTRend�ľ���: ATGsite - coord - 1;//CnnnATG<br>
 * C��ATG�ľ���: coord - ATGsite//CnnnATG<br>
 * ���뱾��������ʼ nnnCnnΪ3������������յ�Ϊ2�����<br>
 * ���뱾��������ʼ CnnΪ0�����<br>
 * @author zong0jie
 *
 */
public class GffGeneIsoSearchCis extends GffGeneIsoSearch {
	private static final Logger logger = Logger.getLogger(GffGeneIsoSearchCis.class);

	public GffGeneIsoSearchCis(GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		super(gffGeneIsoInfo,coord);
	}
	/**
	 * ���������ڵڼ��������ӻ��ں�����
	 * ����ָ�������������ӻ����ں���
	 * �Ƿ���UTR��
	 * ͬʱ���		
	 * cod2ATG
		cod2cdsEnd 
		cod2start 
		cod2end 
		��
	 */
	@Override
	protected void codSearchNum() {
		int ExIntronnum = getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if(coord < ATGsite){        //����С��atg����5��UTR��,Ҳ������������
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if(coord > UAGsite){       //����cds��ʼ������3��UTR��
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		cod2ATG = coord - ATGsite; //CnnnATG    AtgnC
		cod2UAG = coord - UAGsite; //CnuaG    UAGnnnC
		cod2TSS = coord - lsIsoform.get(0)[0];
		cod2TES = coord - lsIsoform.get(lsIsoform.size() - 1)[1];
		cod2ExInStart = getLoc2ExInStart(coord);
		cod2ExInEnd = getLoc2ExInEnd(coord);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		numExIntron = Math.abs(ExIntronnum);
	}
	
	/**
	 * ��һ������ģ����������뱾 ������/�ں��� �� ���/�յ� �ľ���
	 */
	@Override
	protected void setCod2ExInStartEnd() {}
	
	/**
	 * ��������5UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	@Override
	protected void setCod2UTR5() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		//             tss             0-0   0-1        1-0 cood 1-1           2-0  2-1               3-0  atg  3-1               4               5
		for (int i = 0; i < NumExon; i++) {
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
		//  tss             0        1-0    cood  atg  1-1      2               3               4               5
		if (ATGsite <= lsIsoform.get(NumExon)[1]) //һ��ҪС�ڵ���
		{
			cod2UTRendmRNA = ATGsite - coord - 1;//CnnnnnnATG
		}
		// tss             0        1-0    cood   1-1      2               3-0   atg   3-1                 4               5
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///Cnnnnnn
			int m = NumExon+1;
			while ( lsIsoform.get(m)[1] < ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + ATGsite - lsIsoform.get(m)[0];//nnnnnnnATG
			
			if (ATGsite < lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			}
		}
	}
	
	/**
	 * ��������3UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag  cood  2-1               3-0      3-1                 4               5
		if ( UAGsite >= lsIsoform.get(NumExon)[0])//һ��Ҫ���ڵ��� 
		{
			cod2UTRstartmRNA = coord - UAGsite - 1;  //UAGnnnnnnnC
		}
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag    2-1               3-0      3-1                 4-0    cood    4-1               5
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //nnnnnnnC
			int m = NumExon-1;
			while (m >= 0 && lsIsoform.get(m)[0] > UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - lsIsoform.get(m)[0] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsIsoform.get(m)[1] - UAGsite; //UAGnnnnn
		}
		/////////////////////utrend//////////////////
		// tss             0        1-0    atg   1-1      2-0    uag    2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2UTRendmRNA = cod2UTRendmRNA + cod2ExInEnd;
	}
	
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA�������յ�ľ���
	 * �������ں���
	 */
	protected void setCod2StartEndmRNA() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		cod2TSSmRNA = 0; cod2TESmRNA = 0;
		// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = 0; i < NumExon; i--) {
			cod2TSSmRNA = cod2TSSmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
		}
		cod2TESmRNA = cod2TESmRNA + cod2ExInEnd;
	}
	
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA��atg��uag�ľ���
	 * �ȼ���setCod2StartEndmRNA
	 * �������ں���
	 */
	protected void setCod2StartEndCDS() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		cod2ATGmRNA = 0; cod2UAGmRNA = 0;
		// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		if (codLocUTR == COD_LOCUTR_5UTR) {
			cod2ATGmRNA = -(cod2UTRendmRNA + 1);
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			cod2UAGmRNA = cod2UTRstartmRNA + 1;
		}
		//��coord��ATG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_5UTR) {
			// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 uag 5-1
			for (int i = 0; i < NumExon; i++) {
				if (lsIsoform.get(i)[1] < ATGsite) {
					continue;
				}
				if (lsIsoform.get(i)[0] <= ATGsite && lsIsoform.get(i)[1] >= ATGsite) {
					cod2ATGmRNA = lsIsoform.get(i)[1] - ATGsite + 1; // Atgnn   nnnnC
					continue;
				}
				cod2ATGmRNA = cod2ATGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
			}
			cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
		}
		//��coord��UAG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_3UTR) {
			// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1       4-0 4-1       5-0 uag 5-1       6-0 6-1
			for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
				if (lsIsoform.get(i)[0] > UAGsite) {
					break;
				}
				if (lsIsoform.get(i)[0] <= UAGsite && lsIsoform.get(i)[1] >= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + UAGsite - lsIsoform.get(i)[0] + 1; // nCnnn nnn  nnuaG
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	
	/**
	 * ����ĳ����������ڵ��ں�����������Ŀ
	 */
	HashMap<Integer, Integer> hashLocExInNum = new HashMap<Integer, Integer>();
	/**
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
	 * @return
	 */
	protected int getLocExInNum(int location) {
		if (hashLocExInNum.containsKey(location)) {
			return hashLocExInNum.get(location);
		}

		if (    location < lsIsoform.get(0)[0] || 
				location > lsIsoform.get(lsIsoform.size()-1)[1]  )  	{
//			hashLocExInNum.put(location, 0);  //����ת¼���ڵ����겻�����
			return 0;
		}
		for(int i = 0; i < lsIsoform.size(); i++)  //һ��һ��Exon�ļ��
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(location <= lsIsoform.get(i)[1] && location >= lsIsoform.get(i)[0]) {
				hashLocExInNum.put(location, i + 1);
				return i + 1;
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsIsoform.size() - 2 && location > lsIsoform.get(i)[1] && location < lsIsoform.get(i+1)[0]) {
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
	 */
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			loc2ExInStart = location - lsIsoform.get(NumExon)[0];//���뱾��������ʼ nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = location - lsIsoform.get(NumExon)[1] -1;// ��ǰһ�������� NnnnCnnnn
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
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getLocExInNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			 loc2ExInEnd = lsIsoform.get(NumExon)[1] - location;//���뱾��������ֹ  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			 loc2ExInEnd = lsIsoform.get(NumExon)[0] - location - 1;// ���һ�������� nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	/**
	 * ��������֮��ľ��룬mRNA���棬��loc1��loc2����ʱ�����ظ�������loc1��loc2����ʱ����������
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�-1000000000
	 * @param loc1 ��һ������Ŀ
	 * @param loc2 �ڶ�������
	 */
	protected int getLocDistance(int loc1, int loc2) {
		int locSmall = Math.min(loc1, loc2);
		int locBig = Math.max(loc1, loc2);
		int loc1ExInNum = getLocExInNum(locSmall);
		int loc2ExInNum = getLocExInNum(locBig);
		
		int distance = -1000000000;
		
		if (loc1ExInNum <= 0 || loc2ExInNum <= 0) {
			return distance;
		}
		
		loc1ExInNum--; loc2ExInNum--;
		if (loc1ExInNum == loc2ExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getLoc2ExInEnd(locSmall) + getLoc2ExInStart(locBig);
			for (int i = loc1ExInNum+1; i <= loc2ExInNum - 1; i++) {
				distance = distance + lsIsoform.get(i)[1] -lsIsoform.get(i)[0] + 1;
			}
		}
		if (loc1 < loc2) {
			return -distance;
		}
		return distance;
	}
	
	/**
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 */
	public int getLocdistanceSite(int location, int mRNAnum) {
		if (getLocExInNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getLoc2ExInStart(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocExInNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getLoc2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					int[] tmpExon = lsIsoform.get(i);
					// һ��һ�������ӵ���ǰ����
					if (remain - (tmpExon[1] - tmpExon[0] + 1) > 0) {
						remain = remain - (tmpExon[1] - tmpExon[0] + 1);
						continue;
					}
					else {
						return tmpExon[1] - remain + 1;
					}
				}
				return -1;
			}
		} 
		else {
			if (mRNAnum <= getLoc2ExInEnd(location)) {
				return location + mRNAnum;
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
						return tmpExon[0] + remain - 1;
					}
				}
				return -1;
			}
		}
	}
	
}
