package com.novelbio.analysis.seq.mirna;

import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.base.dataStructure.listOperate.ListBin;

public interface ListMiRNAInt {
	String searchMirName(String miRNApre, int start, int end);
	
	ListDetailBin searchLOC(String matureID);
	
	Map<String, ListBin<ListDetailBin>> getMapChrID2LsGff();
	
	List<ListDetailBin> getGffDetailAll();
}
