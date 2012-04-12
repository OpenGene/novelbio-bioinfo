package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;

public class GffCodCGDU extends ListCodAbsDu<GffDetailCG, GffCodCG>{

	public GffCodCGDU(ArrayList<GffDetailCG> lsgffDetail,
			GffCodCG gffCod1, GffCodCG gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	
}
