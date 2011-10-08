package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import org.apache.log4j.Logger;

public class GffGeneIsoCisCod extends GffGeneIsoInfoCod{
	public GffGeneIsoCisCod(GffGeneIsoCis gffgeneIso, int coord) {
		super(gffgeneIso, coord);
		// TODO Auto-generated constructor stub
	}
	private static final Logger logger = Logger.getLogger(GffGeneIsoCisCod.class);
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
			cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
		//  tss             0        1-0    cood  atg  1-1      2               3               4               5
		if (gffGeneIso.ATGsite <= gffGeneIso.lsIsoform.get(NumExon)[1]) //һ��ҪС�ڵ���
		{
			cod2UTRendmRNA = gffGeneIso.ATGsite - coord - 1;//CnnnnnnATG
		}
		// tss             0        1-0    cood   1-1      2               3-0   atg   3-1                 4               5
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///Cnnnnnn
			int m = NumExon+1;
			while ( gffGeneIso.lsIsoform.get(m)[1] <gffGeneIso.ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.lsIsoform.get(m)[1] - gffGeneIso.lsIsoform.get(m)[0] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.ATGsite - gffGeneIso.lsIsoform.get(m)[0];//nnnnnnnATG
			
			if (gffGeneIso.ATGsite < gffGeneIso.lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + gffGeneIso.IsoName);
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
		if ( gffGeneIso.UAGsite >= gffGeneIso.lsIsoform.get(NumExon)[0])//һ��Ҫ���ڵ��� 
		{
			cod2UTRstartmRNA = coord - gffGeneIso.UAGsite - 1;  //UAGnnnnnnnC
		}
		// tss             0-0 0-1        1-0    atg   1-1      2-0    uag    2-1               3-0      3-1                 4-0    cood    4-1               5
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //nnnnnnnC
			int m = NumExon-1;
			while (m >= 0 && gffGeneIso.lsIsoform.get(m)[0] > gffGeneIso.UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.lsIsoform.get(m)[1] - gffGeneIso.lsIsoform.get(m)[0] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.lsIsoform.get(m)[1] - gffGeneIso.UAGsite; //UAGnnnnn
		}
		/////////////////////utrend//////////////////
		// tss             0        1-0    atg   1-1      2-0    uag    2-1               3-0   cood    3-1                 4-0 4-1               5-0 5-1
		for (int i = gffGeneIso.lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] +1;
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
			cod2TSSmRNA = cod2TSSmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < gffGeneIso.lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] +1;
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
			if (NumExon  == gffGeneIso.getLocExInNum(gffGeneIso.ATGsite) - 1) {
				cod2ATGmRNA = (coord - gffGeneIso.ATGsite);
			}
			else {
				// tss             0-0 0-1        1-0 atg 1-1      2-0 2-1               3-0   cood    3-1                 4-0 4-1               5-0 uag 5-1
				for (int i = 0; i < NumExon; i++) {
					if (gffGeneIso.lsIsoform.get(i)[1] < gffGeneIso.ATGsite) {
						continue;
					}
					if (gffGeneIso.lsIsoform.get(i)[0] <= gffGeneIso.ATGsite && gffGeneIso.lsIsoform.get(i)[1] >= gffGeneIso.ATGsite) {
						cod2ATGmRNA = gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.ATGsite + 1; // Atgnn   nnnnC
						continue;
					}
					cod2ATGmRNA = cod2ATGmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] +1;
				}
				cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
			}
		
		}
		//��coord��UAG����ʱ,Ϊ����
		if (codLocUTR != COD_LOCUTR_3UTR) {
			if (NumExon  == gffGeneIso.getLocExInNum(gffGeneIso.UAGsite) - 1) {
				cod2UAGmRNA = (coord - gffGeneIso.UAGsite);
				return;
			}
			
			
			
			// tss             0-0 0-1        1-0 1-1      2-0 2-1               3-0   cood    3-1       4-0 4-1       5-0 uag 5-1       6-0 6-1
			for (int i = NumExon + 1; i < gffGeneIso.lsIsoform.size(); i++) {
				if (gffGeneIso.lsIsoform.get(i)[0] > gffGeneIso.UAGsite) {
					break;
				}
				if (gffGeneIso.lsIsoform.get(i)[0] <= gffGeneIso.UAGsite && gffGeneIso.lsIsoform.get(i)[1] >= gffGeneIso.UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + gffGeneIso.UAGsite - gffGeneIso.lsIsoform.get(i)[0] + 1; // nCnnn nnn  nnuaG
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + gffGeneIso.lsIsoform.get(i)[1] - gffGeneIso.lsIsoform.get(i)[0] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	@Override
	protected void setCod2SiteAbs() {
		cod2ATG = coord - gffGeneIso.ATGsite; //CnnnATG    AtgnC
		cod2UAG = coord - gffGeneIso.UAGsite; //CnuaG    UAGnnnC
		cod2TSS = coord - gffGeneIso.getTSSsite();
		cod2TES = coord - gffGeneIso.getTESsite();
		
	}
}
