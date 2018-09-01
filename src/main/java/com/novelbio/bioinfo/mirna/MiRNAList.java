package com.novelbio.bioinfo.mirna;

import com.novelbio.bioinfo.base.binarysearch.ListEleSearch;

/** 存储miRNA的list */
public abstract class MiRNAList extends ListEleSearch<MirMature, MirPre>{
	protected boolean isGetSeq = true;
	public abstract String searchMirName(String miRNApre, int start, int end);
	
	/** 是否提取dat中的序列，默认为true */
	public void setGetSeq(boolean isGetSeq) {
		this.isGetSeq = isGetSeq;
	}
	
	public void addMirMature(MiRNAList miRNAList) {
		for (MirPre mirPre : miRNAList.getMapChrID2LsGff().values()) {
			getMapChrID2LsGff().put(mirPre.getName().toLowerCase(), mirPre);
			if (lsNameNoRedundent != null) {
				for (MirMature gff : mirPre) {
					lsNameNoRedundent.add(gff.getName().toLowerCase());
				}
			}
		}
	}
}
