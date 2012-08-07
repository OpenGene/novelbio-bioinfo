package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.LocInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;

/**
 * 吴宗福的项目，已经有了测序序列，将序列中的gap和NNN和XXX等全部提取出来并写入文本
 * @author zong0jie
 *
 */
public class GetSeqInfo extends SeqFastaHash{

	public GetSeqInfo(String chrFile) {
		super(chrFile);
	}
	/**
	 * 获得序列信息并提取出来
	 * @param flankingLen 旁临序列长度
	 * @param distance 两个gap最短相近多少，小于该值就合并
	 */
	public void getSeqInfoAll(int flankingLen, int distance,String txtOutFile) {
		SeqFasta seqFasta = getSeqFastaAll().get(0);
		ArrayList<LocInfo> lslocInfo = seqFasta.getSeqInfo();
		ArrayList<double[]> lsTmp = new ArrayList<double[]>();
		for (LocInfo locInfo : lslocInfo) {
			double[] tmploc = new double[2];
			tmploc[0] = locInfo.getStartLoc();
			tmploc[1] = locInfo.getEndLoc();
			lsTmp.add(tmploc);
		}
		/**
		 * 合并间隔在1000bp以内的gap
		 */
		ArrayList<double[]> lsTmpResult = MathComput.combInterval(lsTmp, 1000);
		ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
 		for (double[] ds : lsTmpResult) {
			int[] is = new int[2];
			is[0] = (int) (ds[0] - flankingLen);
			is[1] = (int) (ds[1] + flankingLen);
			LocInfo locInfo = new LocInfo(seqFasta.getSeqName(), is[0], is[1]);
			lsResult.add(locInfo);
		}
 		ArrayList<SeqFasta> lsSeqFastas = getRegionSeqFasta(lsResult);
 		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
 		for (int i = 0; i < lsSeqFastas.size(); i++) {
			txtOut.writefileln(">GapNum:"+i+"  "+lsResult.get(i).toString().replace(seqFasta.getSeqName(), ""));
			txtOut.writefilePerLine(lsSeqFastas.get(i).toString(), 100);
			txtOut.writefileln();
		}
 		txtOut.close();
	}
	
}
