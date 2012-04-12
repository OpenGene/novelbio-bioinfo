package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;

public class GffCodPeakDU extends ListCodAbsDu<GffDetailPeak, GffCodPeak>{
	
	public GffCodPeakDU(ArrayList<GffDetailPeak> lsgffDetail,
			GffCodPeak gffCod1, GffCodPeak gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	
}
