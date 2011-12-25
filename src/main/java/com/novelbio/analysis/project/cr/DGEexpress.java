package com.novelbio.analysis.project.cr;

import java.util.HashMap;
import java.util.Map.Entry;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.statusAckLog;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.tools.compare.CombineTab;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class DGEexpress {
	public static void main(String[] args) {
		combDGE();
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
		String parentFile = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/";
		String file1 = parentFile + "HUMarbE_E6d.fq_filter_map_Sorted_dgeExpress.txt";
		String file2 = parentFile + "HUMarbE_E6dn.fq_filter_map_Sorted_dgeExpress.txt";
		String file3 = parentFile + "HUMarbE_NS6d.fq_filter_map_Sorted_dgeExpress.txt";

		CombineTab comb = new CombineTab();
		comb.setColDetai(file1, "E6d", 2);
		comb.setColDetai(file2 ,"E6dn", 2);
		comb.setColDetai(file3, "NS6d", 2);
		comb.setColID(1);
		comb.exeToFile(parentFile + "CR.xls");
	}
	
}