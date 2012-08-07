package com.novelbio.analysis.seq.reseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.LocInfo;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;

/**
 * ���ڸ�����Ŀ���Ѿ����˲������У��������е�gap��NNN��XXX��ȫ����ȡ������д���ı�
 * @author zong0jie
 *
 */
public class GetSeqInfo extends SeqFastaHash{

	public GetSeqInfo(String chrFile) {
		super(chrFile);
	}
	/**
	 * ���������Ϣ����ȡ����
	 * @param flankingLen �������г���
	 * @param distance ����gap���������٣�С�ڸ�ֵ�ͺϲ�
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
		 * �ϲ������1000bp���ڵ�gap
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
