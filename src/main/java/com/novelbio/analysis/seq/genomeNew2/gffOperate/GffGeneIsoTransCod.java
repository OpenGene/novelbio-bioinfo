package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import org.apache.log4j.Logger;

public class GffGeneIsoTransCod extends GffGeneIsoInfoCod{
	public GffGeneIsoTransCod(GffGeneIsoTrans gffgeneIso, int coord) {
		super(gffgeneIso, coord);
		// TODO Auto-generated constructor stub
	}
	private static final Logger logger = Logger.getLogger(GffGeneIsoTransCod.class);
	@Override
	protected void setCod2UTR5() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
	//  5-1 5-0    4-1 uag 4-0     3-1 gta 3-0         2-1 2-0    1-1 cood 1-0            0-1 0-tss  cood
		for (int i = 0; i < NumExon; i++) {
			cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] + 1;
		}
		cod2UTRstartmRNA = cod2UTRstartmRNA + cod2ExInStart;
	//  5-1 5-0  cood  4-1 uag 4-0     3-1 3-0         2-1 2-0    1-1 gta  cood 1-0      0-1 0-tss  cood
		if (gffGeneIso.ATGsite >= gffGeneIso.lsIsoform.get(NumExon)[1]) //一定要大于等于
		{
			cod2UTRendmRNA = coord - gffGeneIso.ATGsite - 1;//GTAnnnC
		}
	//  5-1 5-0  cood  4-1 uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		else
		{
			cod2UTRendmRNA = cod2ExInEnd;  ///nnnC
			int m = NumExon+1;
			while ( gffGeneIso.lsIsoform.get(m)[1] > gffGeneIso.ATGsite  ) 
			{
				cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.lsIsoform.get(m)[0] - gffGeneIso.lsIsoform.get(m)[1] + 1;
				m++;
			}
			cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.lsIsoform.get(m)[0] - gffGeneIso.ATGsite;//Atgn
			if (gffGeneIso.ATGsite > gffGeneIso.lsIsoform.get(m)[0]) {
				logger.error("setCod2UTR5Cis error: coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + gffGeneIso.IsoName);
			}
		}
	}

	@Override
	protected void setCod2UTR3() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		
		cod2UTRstartmRNA = 0; cod2UTRendmRNA = 0;
		//  5-1 5-0    4-1 cood  uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		if ( gffGeneIso.UAGsite <= gffGeneIso.lsIsoform.get(NumExon)[0])//一定要小于等于 
		{
			cod2UTRstartmRNA = gffGeneIso.UAGsite - coord - 1;  //CnnnGAU
		}
		//  5-1 cood 5-0    4-1 uag 4-0     3-1 gta 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		else 
		{
			cod2UTRstartmRNA = cod2ExInStart; //Cnnnnn
			int m = NumExon - 1;
		//  5-1 cood 5-0       4-1  4-0        3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
			while (m >= 0 && gffGeneIso.lsIsoform.get(m)[0] < gffGeneIso.UAGsite) 
			{
				cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.lsIsoform.get(m)[0] - gffGeneIso.lsIsoform.get(m)[1] + 1;
				m--;
			}
			cod2UTRstartmRNA = cod2UTRstartmRNA + gffGeneIso.UAGsite - gffGeneIso.lsIsoform.get(m)[1]; //nnnGAU
		}
		/////////////////////utrend//////////////////
		//  5-1 5-0    4-1 cood  4-0     3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		for (int i = gffGeneIso.lsIsoform.size() - 1; i > NumExon; i--) {
			cod2UTRendmRNA = cod2UTRendmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] +1;
		}
		cod2UTRendmRNA = cod2UTRendmRNA + cod2ExInEnd;
	}

	@Override
	protected void setCod2StartEndmRNA() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2TSSmRNA = 0; cod2TESmRNA = 0;
		//  5-1 5-0    4-1 cood  4-0     3-1 uag 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
		for (int i = 0; i < NumExon; i++) {
			cod2TSSmRNA = cod2TSSmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] +1;
		}
		cod2TSSmRNA = cod2TSSmRNA + cod2ExInStart;
		
		for (int i = NumExon + 1; i < gffGeneIso.lsIsoform.size(); i++) {
			cod2TESmRNA = cod2TESmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] +1;
		}
		cod2TESmRNA = cod2TESmRNA + cod2ExInEnd;
	}

	@Override
	protected void setCod2StartEndCDS() {
		int NumExon = numExIntron - 1; //实际数量减去1，方法内用该变量运算
		cod2ATGmRNA = 0; cod2UAGmRNA = 0;
		//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 cood 2-0       1-1 gta 1-0       0-1 0-tss  cood
		if (codLocUTR == COD_LOCUTR_5UTR) {
			cod2ATGmRNA = -(cod2UTRendmRNA + 1);
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			cod2UAGmRNA = cod2UTRstartmRNA + 1;
		}
		//当coord在ATG下游时,为正数
		if (codLocUTR != COD_LOCUTR_5UTR) {
			if (NumExon  == gffGeneIso.getLocExInNum(gffGeneIso.ATGsite) - 1) {
				cod2ATGmRNA = -(coord - gffGeneIso.ATGsite);
			}
			else {
				//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 cood 2-0       1-1 gta 1-0       0-1 0-tss  cood
				for (int i = 0; i < NumExon; i++) {
					if (gffGeneIso.lsIsoform.get(i)[1] > gffGeneIso.ATGsite) {
						continue;
					}
					if (gffGeneIso.lsIsoform.get(i)[0] >= gffGeneIso.ATGsite && gffGeneIso.lsIsoform.get(i)[1] <= gffGeneIso.ATGsite) {
						cod2ATGmRNA = gffGeneIso.ATGsite - gffGeneIso.lsIsoform.get(i)[1] + 1; // Atgnn   nnnnC
						continue;
					}
					cod2ATGmRNA = cod2ATGmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] +1;
				}
				cod2ATGmRNA = cod2ATGmRNA + cod2ExInStart;
			}

		}
		//当coord在UAG上游时,为负数
		if (codLocUTR != COD_LOCUTR_3UTR) {
				if (NumExon  == gffGeneIso.getLocExInNum(gffGeneIso.UAGsite) - 1) {
					cod2UAGmRNA = -(coord - gffGeneIso.UAGsite);
					return;
				}
				
			//  5-1 5-0    4-1 cood  4-0     3-1 gau 3-0           2-1 2-0       1-1 cood 1-0       0-1 0-tss  cood
			for (int i = NumExon + 1; i < gffGeneIso.lsIsoform.size(); i++) {
				if (gffGeneIso.lsIsoform.get(i)[0] < gffGeneIso.UAGsite) {
					break;
				}
				if (gffGeneIso.lsIsoform.get(i)[0] >= gffGeneIso.UAGsite && gffGeneIso.lsIsoform.get(i)[1] <= gffGeneIso.UAGsite) {
					cod2UAGmRNA = cod2UAGmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.UAGsite + 1; // Gaunn nnn nnC
					break;
				}
				cod2UAGmRNA = cod2UAGmRNA + gffGeneIso.lsIsoform.get(i)[0] - gffGeneIso.lsIsoform.get(i)[1] +1;
			}
			cod2UAGmRNA = -(cod2UAGmRNA + cod2ExInEnd);
		}
	}
	@Override
	protected void setCod2SiteAbs() {
		cod2ATG =  gffGeneIso.ATGsite - coord; //CnnnATG    AtgnC
		cod2UAG = gffGeneIso.UAGsite - coord; //CnuaG    UAGnnnC
		cod2TSS = gffGeneIso.getTSSsite() - coord;
		cod2TES = gffGeneIso.getTESsite() - coord;
		
	}
}
