package com.novelbio.analysis.project.lhx;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Patternlocation;

/**
 * 祝冬祺给了一系列序列和一个motif
 * 用motif扫描这些序列
 * @author zong0jie
 *
 */
public class MotifScan {
	
	
	
	public static void main(String[] args) {
		String fastaSeqFile = "/home/zong0jie/桌面/linhongxuan/PROMOTER.txt";
		String out = "/home/zong0jie/桌面/linhongxuan/promoterOut.txt";
		String regx = null;
		String regFor = "[CT]CAAA\\W{0,4}[GA]T";
		String regRev = "A[TC]\\W{0,4}TTTG[AG]";
		MotifScan motifScan = new MotifScan();
		motifScan.getSeq(fastaSeqFile, regx);
		ArrayList<String[]> lsResult = motifScan.scanSeq(regFor, regRev);
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	SeqFastaHash seqFastaHash;
	private void getSeq(String fastaSeqFile,String regx) {
		seqFastaHash = new SeqFastaHash(fastaSeqFile, regx, false, false);
	}
	
	private ArrayList<String[]> scanSeq(String regexFor, String regexRev) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"GeneName","Strand","Motif","DistanceToSeqEnd"});
		ArrayList<SeqFasta> lsFastaAll = seqFastaHash.getSeqFastaAll();
		for (SeqFasta seqFasta : lsFastaAll) {
			ArrayList<String[]> lsTmpResult = Patternlocation.getPatLoc(seqFasta.toString(), regexFor, false);
			if (lsTmpResult != null && lsTmpResult.size() > 0) {
				for (String[] strings : lsTmpResult) {
					String[] tmpResult = new String[4];
					tmpResult[0] = seqFasta.getSeqName();
					tmpResult[1] = "+";
					tmpResult[2] = strings[0];
					tmpResult[3] = strings[2];
					lsResult.add(tmpResult);
				}
			}
			lsTmpResult = Patternlocation.getPatLoc(seqFasta.toString(), regexRev, false);
			if (lsTmpResult != null && lsTmpResult.size() > 0) {
				for (String[] strings : lsTmpResult) {
					String[] tmpResult = new String[4];
					tmpResult[0] = seqFasta.getSeqName();
					tmpResult[1] = "-";
					tmpResult[2] = strings[0];
					tmpResult[3] = strings[2];
					lsResult.add(tmpResult);
				}
			}
		}
		return lsResult;
	}
}
