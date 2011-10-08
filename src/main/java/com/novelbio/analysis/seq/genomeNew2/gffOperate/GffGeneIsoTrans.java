package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class GffGeneIsoTrans extends GffGeneIsoInfo{
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	public GffGeneIsoTrans(String IsoName, GffDetailGene gffDetailGene) {
		super(IsoName, gffDetailGene);
	}
//	@Override
//	protected void setCod2ExInStartEnd() {
//		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
//		if (codLoc == COD_LOC_EXON) {
//			cod2ExInStart = lsIsoform.get(NumExon)[0] - coord;//���뱾��������ʼ Cnnn
//			cod2ExInEnd = coord - lsIsoform.get(NumExon)[1];//���뱾��������ֹ  nnnC
//		}
//		else if(codLoc == COD_LOC_INTRON) 
//		{   //  5-1 5-0  cood  4-1 uag 4-0     3-1 3-0         2-1 2-0    1-1 gta 1-0  cood  0-1 0-tss  cood
//			   cod2ExInEnd = coord - lsIsoform.get(numExIntron)[0] - 1;// ���һ�������� NnnCnn
//			   cod2ExInStart = lsIsoform.get(NumExon)[1] - coord -1;// ��ǰһ�������� nnnCnnnN
//		}
//	}

	
	/**
	 * @param location �õ������ڵڼ��������ӻ��ں�����
	 * @return
	 */
	@Override
	protected int getLocExInNum(int location) {
		if (hashLocExInNum == null) {
			hashLocExInNum = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInNum.containsKey(location)) {
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
	 * ���굽������/�ں��� ������
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	@Override
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
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
	 * ���굽������/�ں��� �յ����
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
	 */
	@Override
	protected int getLoc2ExInEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
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
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ4λ����N��Loc�غ�ʱΪ0
	 */
	@Override
	public int getLocdistanceSite(int location, int mRNAnum) {
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
						remain = remain - (tmpExon[0] - tmpExon[1] + 1);
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
					if (remain - (tmpExon[0] - tmpExon[1] + 1) > 0) {
						remain = remain - (tmpExon[0] - tmpExon[1] + 1);
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


	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ ��ӵ�ʱ����밴�ջ�������ӣ� �����С������� �� int0<int1 ����Ӵ�С��� ��
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.max(locStart, locEnd);
		tmpexon[1] = Math.min(locStart, locEnd);
		lsIsoform.add(0, tmpexon);
	}
	
	/**
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected void addExonGFF(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ
		 * ��ӵ�ʱ����밴�ջ�������ӣ�
		 * �����С������� �� int0<int1
		 * ����Ӵ�С��� �� int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.max(locStart, locEnd);
		tmpexon[1] = Math.min(locStart, locEnd);
		lsIsoform.add(tmpexon);
	}
	
	/**
	 * ���5UTR�ĳ���
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		 //5  4   3   2   1   0    ÿ���������� 0 > 1     1    gta   0
		for (int i = 0; i < exonNum; i++) 
		{
			if(lsIsoform.get(i)[1] > getATGSsite())  // gta   1      0
				FUTR = FUTR + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] + 1;
			else if (lsIsoform.get(i)[0] > getATGSsite()  && lsIsoform.get(i)[1] <= getATGSsite() ) //   1     gta      0
				FUTR = FUTR + lsIsoform.get(i)[0] - getATGSsite();
			else if (lsIsoform.get(i)[0] <= getATGSsite())   //   1        0      gta 
				break;
		}
		return FUTR;
	}
	/**
	 * ���3UTR�ĳ���
	 * @return
	 */
	public int getLenUTR3()
	{
		int TUTR=0;
		int exonNum = lsIsoform.size();
		//5  4   3   2   1   0    ÿ���������� 0 > 1      1    gau  0
		for (int i = exonNum-1; i >=0 ; i--) 
		{
			if(lsIsoform.get(i)[0] < getUAGsite())  //     1      0     gau
				TUTR = TUTR + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] + 1;
			else if (lsIsoform.get(i)[0] >= getUAGsite() && lsIsoform.get(i)[1] < getUAGsite())  //     1    gau    0     
				TUTR = TUTR + getUAGsite() - lsIsoform.get(i)[1];
			else if (lsIsoform.get(i)[1] >= getUAGsite())   //   gau   1      0     
				break;
		}
		return TUTR;
	}
	@Override
	public boolean isCis5to3() {
		return false;
	}
	@Override
	protected void addExonGFFCDSUTR(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ ��ӵ�ʱ����밴�ջ�������ӣ� �����С������� �� int0<int1 ����Ӵ�С��� ��
		 * int0>int1
		 */
		int[] tmpexon = new int[2];

		tmpexon[0] = Math.max(locStart, locEnd);
		tmpexon[1] = Math.min(locStart, locEnd);
		if (lsIsoform.size() > 0) {
			int[] exon = lsIsoform.get(lsIsoform.size() - 1);
			if (Math.abs(exon[1] - tmpexon[0]) == 1) {
				exon[1] = tmpexon[1];
				return;
			}
		}
		lsIsoform.add(tmpexon);

	}


	@Override
	public GffGeneIsoInfoCod setCod(int coord) {
		// TODO Auto-generated method stub
		return new GffGeneIsoTransCod(this, coord);
	}
}
