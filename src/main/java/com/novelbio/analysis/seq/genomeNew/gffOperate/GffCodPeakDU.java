package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodPeakDU extends GffCodAbsDu<GffDetailPeak, GffCodPeak, GffDetailPeakCod>{
	
	public GffCodPeakDU(ArrayList<GffDetailPeakCod> lsgffDetail,
			GffCodPeak gffCod1, GffCodPeak gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	
}
