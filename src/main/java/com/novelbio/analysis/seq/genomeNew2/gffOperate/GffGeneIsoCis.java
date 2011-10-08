package com.novelbio.analysis.seq.genomeNew2.gffOperate;


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
public class GffGeneIsoCis extends GffGeneIsoInfo {
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene) {
		super(IsoName, gffDetailGene);
	}

	
//	/**
//	 * ��һ������ģ����������뱾 ������/�ں��� �� ���/�յ� �ľ���
//	 */
//	@Override
//	protected void setCod2ExInStartEnd() {}
	



	/**
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
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
	 * ���굽������/�ں��� ������
	 * @param location ����
	 */
	@Override
	protected int getLoc2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
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
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 */
	@Override
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



	
	
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ ��ӵ�ʱ����밴�ջ�������ӣ� �����С������� �� int0<int1 ����Ӵ�С��� ��
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
		lsIsoform.add(tmpexon);
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
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);

		lsIsoform.add(tmpexon);
	}
	
	/**
	 * ���5UTR�ĳ���
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsIsoform.size();
		 //0    1     2     3     4     5   ÿ���������� 1 > 0      0    atg   1
			for (int i = 0; i <exonNum; i++) 
			{
				if(lsIsoform.get(i)[1] < getATGSsite())    // 0       1   atg    
					FUTR = FUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
				else if (lsIsoform.get(i)[0] < getATGSsite() && lsIsoform.get(i)[1] >= getATGSsite())  //     0    atg    1 
					FUTR = FUTR + getATGSsite() - lsIsoform.get(i)[0];
				else if (lsIsoform.get(i)[0] >= getATGSsite())  //     atg   0       1   
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
		 //0    1     2     3     4     5   ÿ���������� 0 < 1      0    uag   1
		for (int i = exonNum - 1; i >=0 ; i--) 
		{
			if(lsIsoform.get(i)[0] > getUAGsite())  //      uag     0      1
				TUTR = TUTR + lsIsoform.get(i)[1] - lsIsoform.get(i)[0] + 1;
			else if (lsIsoform.get(i)[1] > getUAGsite() && lsIsoform.get(i)[0] <= getUAGsite())  //     0     uag    1
				TUTR = TUTR + lsIsoform.get(i)[1] - getUAGsite();
			else if (lsIsoform.get(i)[1] <= getUAGsite())   //   0      1     uag   
				break;
		}
		return TUTR;
	}

	@Override
	public boolean isCis5to3() {
		return true;
	}


	@Override
	protected void addExonGFFCDSUTR(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ ��ӵ�ʱ����밴�ջ�������ӣ� �����С������� �� int0<int1 ����Ӵ�С��� ��
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
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
		return new GffGeneIsoCisCod(this, coord);
	}
}
