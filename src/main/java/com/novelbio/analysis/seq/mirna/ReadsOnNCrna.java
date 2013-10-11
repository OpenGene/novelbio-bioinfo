package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.bed.BedRecord;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;
/**
 * 比对到RefSeq上的ncRNA，看其具体情况
 * @author zong0jie
 *
 */
public class ReadsOnNCrna {
	/**
	 * key: ncrnaID
	 * value 0: ncrnaID
	 * 1: ncrnaDescription
	 * 2: num
	 */
	HashMap<String, Double> mapNCrnaID_2_nameDescripValue;
	AlignSeq alignSeq;
	
	public void setAlignSeq(AlignSeq alignSeq) {
		this.alignSeq = alignSeq;
	}
	
	public void searchNCrna() {
		mapNCrnaID_2_nameDescripValue = new HashMap<String, Double>();
		for (AlignRecord alignRecord : alignSeq.readLines()) {
			if (!alignRecord.isMapped()) {
				continue;
			}
			if (mapNCrnaID_2_nameDescripValue.containsKey(alignRecord.getRefID())) {
				double info = mapNCrnaID_2_nameDescripValue.get(alignRecord.getRefID());
				info = (double)1/alignRecord.getMappedReadsWeight() + info;
				mapNCrnaID_2_nameDescripValue.put(alignRecord.getRefID(), info);
			}
			else {
				mapNCrnaID_2_nameDescripValue.put(alignRecord.getRefID(), (double)1/alignRecord.getMappedReadsWeight() );
			}
		}
	}
	/**
	 * 将结果写入文本中
	 * @param outTxt
	 */
	public void writeToFile(String outTxt) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxt, true);
		for (Entry<String, Double> entry : mapNCrnaID_2_nameDescripValue.entrySet()) {
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
	
	public HashMap<String, Double> getMapNCrnaID_2_nameDescripValue() {
		return mapNCrnaID_2_nameDescripValue;
	}
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combValue(Map<String, Map<String, Double>> mapPrefix2NcRNAValue) {
		CombMapNcRNA combMapNcRNA = new CombMapNcRNA();
		return combMapNcRNA.combValue(mapPrefix2NcRNAValue);
	}
}

class CombMapNcRNA extends MirCombMapGetValueAbs {

	@Override
	protected String[] getTitleIDAndInfo() {
		String[] title = new String[3];
		title[0] = TitleFormatNBC.NCRNAID.toString();
		title[1] = TitleFormatNBC.Symbol.toString();
		title[2] = TitleFormatNBC.Description.toString();
		return title;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		GeneID geneID = new GeneID(id, 0);
		lsTmpResult.add(id);
		lsTmpResult.add(geneID.getSymbol());
		lsTmpResult.add(geneID.getDescription());
	}

	@Override
	protected Integer getExpValue(String condition, Double readsCount) {
		return readsCount.intValue();
	}
	
}
