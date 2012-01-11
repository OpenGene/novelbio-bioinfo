package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;
/**
 * ����ͨͨСд
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

	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene, String geneType) {
		super(IsoName, gffDetailGene, geneType);
	}

	public GffGeneIsoCis(String IsoName, String ChrID, int coord, String geneType) {
		super(IsoName, ChrID, coord, geneType);
	}
	public GffGeneIsoCis(String IsoName, String ChrID, String geneType) {
		super(IsoName, ChrID, geneType);
	}
	/**
	 * ��һ������ģ����������뱾 ������/�ں��� �� ���/�յ� �ľ���
	 */
	@Override
	protected void setCod2ExInStartEnd() {}

	
	/**
	 * ��������3UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //ʵ��������ȥ1���������øñ�������
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag  cood  2-1               3-0      3-1                 4               5
		if ( UAGsite >= lsElement.get(NumExon)[0])//һ��Ҫ���ڵ��� 
		{
			cod2UTRstartmRNA = coord - UAGsite - 1;  //UAGnnnnnnnC
		}
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag    2-1               3-0      3-1                 4-0    cood    4-1               5
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //nnnnnnnC
			int m = NumExon-1;
			while (m >= 0 && lsElement.get(m)[0] > UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + lsElement.get(m)[1] - lsElement.get(m)[0] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + lsElement.get(m)[1] - UAGsite; //UAGnnnnn
		}
		/////////////////////utrend//////////////////
		// tss             0        1-0    atg   1-1      2-0    uag    2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = lsElement.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + lsElement.get(i)[1] - lsElement.get(i)[0] +1;
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
		for (int i = 0; i < NumExon; i++) {
			cod2TSSmRNA = cod2TSSmRNA + lsElement.get(i)[1] - lsElement.get(i)[0] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < lsElement.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + lsElement.get(i)[1] - lsElement.get(i)[0] +1;
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
			//��coord��UAG����ʱ,Ϊ����
			if (NumExon  == getLocExInNum(ATGsite) - 1) {
				cod2ATGmRNA = (coord - ATGsite);
			}
			else {
				// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 uag 5-1
				for (int i = 0; i < NumExon; i++) {
					if (lsElement.get(i)[1] < ATGsite) {
						continue;
					}
					if (lsElement.get(i)[0] <= ATGsite && lsElement.get(i)[1] >= ATGsite) {
						cod2ATGmRNA = lsElement.get(i)[1] - ATGsite + 1; // Atgnn   nnnnC
						continue;
					}
					cod2ATGmRNA = cod2ATGmRNA + lsElement.get(i)[1] - lsElement.get(i)[0] +1;
				}
				cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
			}
		
		}
		//��coord��UAG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_3UTR) {
			if (NumExon  == getLocExInNum(UAGsite) - 1) {
				cod2UAGmRNA = (coord - UAGsite);
				return;
			}
			
			
			
			// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1       4-0 4-1       5-0 uag 5-1       6-0 6-1
			for (int i = NumExon + 1; i < lsElement.size(); i++) {
				if (lsElement.get(i)[0] > UAGsite) {
					break;
				}
				if (lsElement.get(i)[0] <= UAGsite && lsElement.get(i)[1] >= UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + UAGsite - lsElement.get(i)[0] + 1; // nCnnn nnn  nnuaG
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + lsElement.get(i)[1] - lsElement.get(i)[0] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	


	
	
	/**
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 */
	@Override
	public int getLocDistmRNASite(int location, int mRNAnum) {
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
					int[] tmpExon = lsElement.get(i);
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
				for (int i = exonNum + 1; i < lsElement.size(); i++) {
					int[] tmpExon = lsElement.get(i);
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


	@Override
	protected void setCod2SiteAbs() {
		cod2ATG = coord - ATGsite; //CnnnATG    AtgnC
		cod2UAG = coord - UAGsite; //CnuaG    UAGnnnC
		cod2TSS = coord - getTSSsite();
		cod2TES = coord - getTESsite();
		
	}
	
	
	protected void addExonUCSC(int locStart, int locEnd) {
		/**
		 * ��������ӣ������ĩβ ��ӵ�ʱ����밴�ջ�������ӣ� �����С������� �� int0<int1 ����Ӵ�С��� ��
		 * int0>int1
		 */
		int[] tmpexon = new int[2];
		tmpexon[0] = Math.min(locStart, locEnd);
		tmpexon[1] = Math.max(locStart, locEnd);
		lsElement.add(tmpexon);
	}
	
	/**
	 * ���Ҫȷ��
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ�� <br>
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ� <br>
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж� <br>
	 */
	protected void addExonCufflinkGTF(int locStart, int locEnd) {
		addExonGFF( locStart,  locEnd);
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

		lsElement.add(tmpexon);
	}
	
	/**
	 * ���5UTR�ĳ���
	 * @return
	 */
	public int getLenUTR5() {
		int FUTR=0;
		int exonNum = lsElement.size();
		 //0    1     2     3     4     5   ÿ���������� 1 > 0      0    atg   1
			for (int i = 0; i <exonNum; i++) 
			{
				if(lsElement.get(i)[1] < getATGSsite())    // 0       1   atg    
					FUTR = FUTR + lsElement.get(i)[1] - lsElement.get(i)[0] + 1;
				else if (lsElement.get(i)[0] < getATGSsite() && lsElement.get(i)[1] >= getATGSsite())  //     0    atg    1 
					FUTR = FUTR + getATGSsite() - lsElement.get(i)[0];
				else if (lsElement.get(i)[0] >= getATGSsite())  //     atg   0       1   
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
		int exonNum = lsElement.size();
		 //0    1     2     3     4     5   ÿ���������� 0 < 1      0    uag   1
		for (int i = exonNum - 1; i >=0 ; i--) 
		{
			if(lsElement.get(i)[0] > getUAGsite())  //      uag     0      1
				TUTR = TUTR + lsElement.get(i)[1] - lsElement.get(i)[0] + 1;
			else if (lsElement.get(i)[1] > getUAGsite() && lsElement.get(i)[0] <= getUAGsite())  //     0     uag    1
				TUTR = TUTR + lsElement.get(i)[1] - getUAGsite();
			else if (lsElement.get(i)[1] <= getUAGsite())   //   0      1     uag   
				break;
		}
		return TUTR;
	}


	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.clone(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}


	@Override
	public Boolean isCis5to3() {
		return true;
	}
	
	@Override
	public GffGeneIsoCis cloneDeep() {
		GffGeneIsoCis gffGeneIsoCis = new GffGeneIsoCis(IsoName, chrID,coord, getGeneType());
		this.cloneDeep(gffGeneIsoCis);
		gffGeneIsoCis.setCoord(getCoord());
		return gffGeneIsoCis;
	}

	@Override
	public int getStartAbs() {
		return lsElement.get(0).getStartCis();
	}

	@Override
	public int getEndAbs() {
		return lsElement.get(lsElement.size() - 1).getEndCis();
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title,
			String strand) {
		String geneExon = "";
		for (int i = 0; i < getIsoInfo().size(); i++) {
			ExonInfo exons = getIsoInfo().get(i);
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs() + "\t" + exons.getEndAbs()
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=exon:" + getIsoName()  + ":" + (i+1) +";Parent=" + getIsoName() + " \r\n";
		}
		return geneExon;
	
	}
	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (ExonInfo exons : getIsoInfo()) {
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs()  + "\t" + exons.getEndAbs() 
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getIsoName()+"\"; \r\n";
		}
		return geneExon;
	}

	
}
