package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;
/**
 * 比对到RefSeq上的ncRNA，看其具体情况
 * @author zong0jie
 *
 */
public class ReadsOnNCrna implements AlignmentRecorder {
	/**
	 * key: ncrnaID
	 * value 0: ncrnaID
	 * 1: ncrnaDescription
	 * 2: num
	 */
	Map<String, Double> mapNCrnaID2Value;
	SamFile samFile;
	int lenMin = 17;
	int lenMax = 32;
	
	public void setSamFile(SamFile alignSeq) {
		this.samFile = alignSeq;
	}
	
	public Map<String, String[]> getLsMapGene2Anno(List<String> lsName) {
		Map<String, String[]> mapGene2Anno = new HashMap<>();
		for (String geneName : lsName) {
			GeneID geneID= new GeneID(geneName, 0);
			mapGene2Anno.put(geneName, new String[]{geneID.getSymbol(), geneID.getDescription()});
		}
		return mapGene2Anno;
	}
	
	public void searchNCrna() {
		mapNCrnaID2Value = new HashMap<String, Double>();
		for (AlignRecord alignRecord : samFile.readLines()) {
			addAlignRecord(alignRecord);
		}
	}

	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!alignRecord.isMapped()) {
			return;
		}
		if (mapNCrnaID2Value.containsKey(alignRecord.getRefID())) {
			double info = mapNCrnaID2Value.get(alignRecord.getRefID());
			info = (double)1/alignRecord.getMappedReadsWeight() + info;
			mapNCrnaID2Value.put(alignRecord.getRefID(), info);
		}
		else {
			mapNCrnaID2Value.put(alignRecord.getRefID(), (double)1/alignRecord.getMappedReadsWeight() );
		}
	}
	
	public void initial() {
		mapNCrnaID2Value = new HashMap<String, Double>();
	}
	
	@Override
	public void summary() {}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	/**
	 * 将结果写入文本中
	 * @param outTxt
	 */
	public void writeToFile(String outTxt) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxt, true);
		for (Entry<String, Double> entry : mapNCrnaID2Value.entrySet()) {
			GeneID geneID = new GeneID(entry.getKey(), 0);
			String[] result = new String[3];
			result[0] = entry.getKey();
			result[1] = geneID.getSymbol();
			result[2] = geneID.getDescription();
			result[3] = entry.getValue().intValue() + "";
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
	public Map<String, Double> getMapNCrnaID2Value() {
		return mapNCrnaID2Value;
	}

	public static List<String> getLsTitleAnno() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		return lsTitle;
	}

}
