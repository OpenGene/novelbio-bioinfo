package com.novelbio.analysis.seq.mirna;

import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.GeneID;
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
	HashMap<String, String[]> hashNCrna = new HashMap<String, String[]>();
	BedSeq bedSeq;
	public void setBedSed(String bedseqFile) {
		bedSeq = new BedSeq(bedseqFile);
	}
	public void searchNCrna() {
		for (BedRecord bedRecord : bedSeq.readlines()) {
			if (hashNCrna.containsKey(bedRecord.getRefID())) {
				String[] info = hashNCrna.get(bedRecord.getRefID());
				info[2] = ((double)1/bedRecord.getMappingNum() + Double.parseDouble(info[2])) + "";
			}
			else {
				String[] info = new String[3];
				GeneID copedID = new GeneID(bedRecord.getRefID(), 0);
				info[0] = copedID.getSymbol();
				info[1] = copedID.getDescription();
				info[2] = (double)1/bedRecord.getMappingNum() + "";
				hashNCrna.put(bedRecord.getRefID(), info);
			}
		}
	}
	/**
	 * 将结果写入文本中
	 * @param outTxt
	 */
	public void writeToFile(String outTxt) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxt, true);
		for (Entry<String, String[]> entry : hashNCrna.entrySet()) {
			String[] value = entry.getValue();
			String[] result = new String[value.length + 1];
			result[0] = entry.getKey();
			for (int i = 1; i < result.length; i++) {
				result[i] = value[i-1];
			}
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
}
