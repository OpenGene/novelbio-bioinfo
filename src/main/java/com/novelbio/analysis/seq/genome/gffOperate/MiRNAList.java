package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.listOperate.ListCodAbs;
import com.novelbio.listOperate.ListCodAbsDu;
import com.novelbio.listOperate.ListHashSearch;

/** 存储miRNA的list */
public abstract class MiRNAList extends ListHashSearch<MirMature, ListCodAbs<MirMature>, 
ListCodAbsDu<MirMature,ListCodAbs<MirMature>>, MirPre> {
	protected boolean isGetSeq = true;
	public abstract String searchMirName(String miRNApre, int start, int end);
	
	/** 是否提取dat中的序列，默认为true */
	public void setGetSeq(boolean isGetSeq) {
		this.isGetSeq = isGetSeq;
	}
}
