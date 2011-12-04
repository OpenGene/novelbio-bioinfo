package com.novelbio.analysis.project.zhy;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class Statistic {
	
	public static void main(String[] args) {
		getMappingInfo();
	}
	
	private static void getMappingInfo() {
		String fastQN = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/1.rawseq/1.rawseq.txt";
		String fastQ2N = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/2.rawseq/2.rawseq.txt";
		String fastQ3N = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/3.rawseq/3.rawseq.txt";
		
		String bedSeN = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/1.bwa_all.bed";
		String bedSe2N = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/2.bwa_all.bed";
		String bedSe3N = "/media/winE/NBC/Project/Project_ZHY_Lab/mRNA/mapping/3.bwa_all.bed";
		
		String txtOutMappingInfo = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/statistic/mappingratesDGE.txt";
		
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutMappingInfo,true);
		txtOut.writefileln("Sample\tRawReads\tFilteredAndUniqMappingReads\tMappingRates");
		txtOut.writefileln(mappingRates(fastQN, bedSeN, "N"));
		txtOut.writefileln(mappingRates(fastQ2N, bedSe2N, "2N"));
		txtOut.writefileln(mappingRates(fastQ3N, bedSe3N, "3N"));
		txtOut.close();
	}
	
	/**
	 * @param fastQFile
	 * @param bedFile
	 * @param title
	 * @return
	 */
	private static String mappingRates(String fastQFile, String bedFile, String title) {
		FastQ fastQ = new FastQ(fastQFile, FastQ.QUALITY_MIDIAN);
		BedSeq bedSeq = new BedSeq(bedFile);
		int rawReadsNum = fastQ.getSeqNum();
		int FilteredAndUniqMappingReads = bedSeq.getSeqNum();
		String result = title + "\t" +rawReadsNum + "\t" + FilteredAndUniqMappingReads +"\t"+ (double)FilteredAndUniqMappingReads/rawReadsNum;
		return result;
	}
	
}
