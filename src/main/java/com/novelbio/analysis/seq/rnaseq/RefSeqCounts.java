package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.Map;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 输入文件不能排序
 * @author zomg0jie
 */
public class RefSeqCounts implements AlignmentRecorder {
	GeneExpTable geneExpTable = new GeneExpTable(TitleFormatNBC.GeneID);
	/** iso到基因的对照表 */
	//TODO key是否要小写
	Map<String, String> mapIso2Gene = new HashMap<>();
	
	public void readGene2IsoFile(String gene2IsoFile) {
		
	}
	
	@Override
	public Align getReadingRegion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		String refId = alignRecord.getRefID();
		String geneName = mapIso2Gene.get(refId);
		if (geneName == null) {
			throw new RuntimeException("error cannot find geneName: " + refId);
		}
		geneExpTable.addGeneExp(geneName, (double)1/alignRecord.getMappedReadsWeight());
	}

	@Override
	public void summary() {
		// TODO Auto-generated method stub
		
	}

}
