package com.novelbio.analysis.seq.mirna;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * 比对到RefSeq上的ncRNA，看其具体情况
 * @author zong0jie
 *
 */
public class ReadsOnNCrna {
	public static void main(String[] args) {
		String bedFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_RefNcRNA.bed";
		String outFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/resultNcRNA/TG_NCrna.txt";
		ReadsOnNCrna readsOnNCrna = new ReadsOnNCrna();
		readsOnNCrna.setBedSed(bedFile);
		readsOnNCrna.searchNCrna();
		readsOnNCrna.writeToFile(outFile);
	}
	/**
	 * key: ncrnaID
	 * value 0: ncrnaID
	 * 1: ncrnaDescription
	 * 2: num
	 */
	HashMap<String, Double> mapNCrnaID_2_nameDescripValue = new HashMap<String, Double>();
	BedSeq bedSeq;
	
	public void setBedSed(String bedseqFile) {
		bedSeq = new BedSeq(bedseqFile);
	}
	
	public void searchNCrna() {
		mapNCrnaID_2_nameDescripValue.clear();
		for (BedRecord bedRecord : bedSeq.readLines()) {
			if (mapNCrnaID_2_nameDescripValue.containsKey(bedRecord.getRefID())) {
				double info = mapNCrnaID_2_nameDescripValue.get(bedRecord.getRefID());
				info = (double)1/bedRecord.getMappingNum() + info;
				mapNCrnaID_2_nameDescripValue.put(bedRecord.getRefID(), info);
			}
			else {
				mapNCrnaID_2_nameDescripValue.put(bedRecord.getRefID(), (double)1/bedRecord.getMappingNum() );
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
	
}
