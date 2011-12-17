package com.novelbio.analysis.project.cr;

import java.util.HashMap;
import java.util.Map.Entry;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.statusAckLog;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class DGEexpress {
	public static void main(String[] args) {

	}
	
	public static void getExpress() {
		String bedFile = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6dn.fq_filter_map_Sorted.bed";
		getDGEexpress(bedFile, FileOperate.changeFileSuffix(bedFile, "_dgeExpress", "txt"));
		bedFile = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_NS6d.fq_filter_map_Sorted.bed";
		getDGEexpress(bedFile, FileOperate.changeFileSuffix(bedFile, "_dgeExpress", "txt"));
	}
	public static void getDGEexpress(String bedFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		BedSeq bedSeq = new BedSeq(bedFile);
		HashMap<String, Integer> hashDge = bedSeq.getDGEnum(false);
		for (Entry<String, Integer> entry : hashDge.entrySet()) {
			String result = entry.getKey();
			int exp = entry.getValue();
			txtOut.writefileln(result+"\t" + exp);
		}
		txtOut.close();
	}
	
	public static void combDGE()
	{
		
	}
	
}
