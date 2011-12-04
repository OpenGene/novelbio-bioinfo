package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;

public class GffGeneIsoTrans extends GffGeneIsoInfo{
	private static final Logger logger = Logger.getLogger(GffGeneIsoTrans.class);
	public GffGeneIsoTrans(String IsoName, GffDetailGene gffDetailGene, String geneTpye) {
		super(IsoName, gffDetailGene, geneTpye);
	}
	public GffGeneIsoTrans(String IsoName, String chrID, int coord, String geneTpye) {
		super(IsoName, chrID, coord, geneTpye);
	}
	public GffGeneIsoTrans(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
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
		try {
			 lsIsoform.get(NumExon);
		} catch (Exception e) {
			System.out.println(this.IsoName);
		}
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
		for (int i = 0; i < NumExon; i++) {
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
			if (NumExon  == getLocExInNum(ATGsite) - 1) {
				cod2ATGmRNA = -(coord - ATGsite);
			}
			else {
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

		}
		//��coord��UAG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_3UTR) {
				if (NumExon  == getLocExInNum(UAGsite) - 1) {
					cod2UAGmRNA = -(coord - UAGsite);
					return;
				}
				
			//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
			for (int i = NumExon + 1; i < lsIsoform.size(); i++) {
				if (lsIsoform.get(i)[0] < UAGsite) {
					break;
				}
				if (lsIsoform.get(i)[0] >= UAGsite && lsIsoform.get(i)[1] <= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[0] - UAGsite + 1; // Gaunn nnn nnC
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsIsoform.get(i)[0] - lsIsoform.get(i)[1] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
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
	public int getLocDistmRNASite(int location, int mRNAnum) {
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

	@Override
	protected void setCod2SiteAbs() {
		cod2ATG =  ATGsite - coord; //CnnnATG    AtgnC
		cod2UAG = UAGsite - coord; //CnuaG    UAGnnnC
		cod2TSS = getTSSsite() - coord;
		cod2TES = getTESsite() - coord;
		
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
	@Override
	protected void addExonCufflinkGTF(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ
		 * ��ӵ�ʱ����밴�ջ�������ӣ�
		 * �����С������� �� int0<int1
		 * ����Ӵ�С��� �� int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.max(locStart, locEnd);
		tmpexon[1] = Math.min(locStart, locEnd);
		lsIsoform.add(0,tmpexon);
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
	public GffGeneIsoTrans clone() {
		GffGeneIsoTrans gffGeneIsoTrans = new GffGeneIsoTrans(IsoName, chrID, coord, getGeneType());
		this.clone(gffGeneIsoTrans);
		gffGeneIsoTrans.setCoord(getCoord());
		return gffGeneIsoTrans;
	}
	@Override
	public GffGeneIsoTrans cloneDeep() {
		GffGeneIsoTrans gffGeneIsoTrans = new GffGeneIsoTrans(IsoName, chrID, coord, getGeneType());
		this.cloneDeep(gffGeneIsoTrans);
		gffGeneIsoTrans.setCoord(getCoord());
		return gffGeneIsoTrans;
	}
	@Override
	public int getStartAbs() {
		return lsIsoform.get(lsIsoform.size() - 1)[1];
		
	}

	@Override
	public int getEndAbs() {
		// TODO Auto-generated method stub
		return lsIsoform.get(0)[0];
	}

	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (int i = getIsoInfo().size() - 1; i >= 0; i--) {
			int[] exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" + title + "\texon\t" + exons[1] + "\t" + exons[0] + "\t" + "0.000000" + "\t"
					+ strand + "\t.\t" + "gene_id \"" + geneID + "\"; transcript_id " + getIsoName() + "\"; \r\n";
		}
		return geneExon;
	}

}
