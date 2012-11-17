package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.PatternOperate;

public class SeqFastaMotifSearch {
	SeqFasta seqFasta;
	String regx;
	int site;
	protected SeqFastaMotifSearch(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
	}
	/**
	 * ���ܲ��ܾ�ȷ�������<b>ͬʱ����������</b>
	 * ����motif���������ϲ�����Ӧ��������ʽ<br>
	 * �����������кͷ������в��ҵĽ��<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: �����motif����<br>
	 * 3: motif���һ������뱾����site��ľ���
	 * @param regex
	 * @param site �趨siteΪ0
	 * ��󷵻�motif��site�㣬��ô<b>����</b>��ʾmotif��site�����Σ�<b>����</b>��ʾmotif��site������
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex) {
		return getMotifScanResult(regex, 0);
	}
	/**
	 * ���ܲ��ܾ�ȷ�������<b>ͬʱ����������</b>
	 * ����motif���������ϲ�����Ӧ��������ʽ<br>
	 * �����������кͷ������в��ҵĽ��<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: �����motif����<br>
	 * 3: motif���һ������뱾����site��ľ���
	 * @param regex
	 * @param site ����������յ��λ�ã�����Ϊ����������Ϊ������Ʃ���λ��Ϊtss������tss����seq�յ�500bp����siteΪ-500��
	 * Ҳ��������ȡ��tss����500bp��
	 * ��󷵻�motif��site�㣬��ô<b>����</b>��ʾmotif��site�����Σ�<b>����</b>��ʾmotif��site������
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex, int site) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsTmpResultFor = PatternOperate.getPatLoc(seqFasta.toString(), regex, false);
		ArrayList<String[]> lsTmpResultRev = PatternOperate.getPatLoc(seqFasta.reservecom().toString(), regex, false);

		copeMotifResultToList(true, lsTmpResultFor, site, lsResult);
		copeMotifResultToList(false, lsTmpResultRev, site, lsResult);
		
		return lsResult;
	}
	
	/**
	 * @param strand ���з���
	 * @param lsTmpResult �ҵ���motif��Ϣ������PatternOperate.getPatLoc()����
	 * ��󷵻�motif��site�㣬��ô<b>����</b>��ʾmotif��site�����Σ�<b>����</b>��ʾmotif��site������
	 * @param site ����������յ��λ�ã�����Ϊ����������Ϊ������Ʃ��tss����seq�յ�500bp����siteΪ-500��
	 * Ҳ��������ȡ��tss����500bp��
	 * @param lsResult ���ص�list
	 */
	private void copeMotifResultToList(boolean strand, ArrayList<String[]> lsTmpResultRev, int site, ArrayList<String[]> lsResult) {
		if (lsTmpResultRev != null && lsTmpResultRev.size() > 0) {
			for (String[] strings : lsTmpResultRev) {
				String[] tmpResult = new String[4];
				tmpResult[0] = seqFasta.getSeqName();
				if (strand) {
					tmpResult[1] = "+";
				} else {
					tmpResult[1] = "-";
				}
				
				tmpResult[2] = strings[0];//�����motif����
				
				String toEndSite = strings[2];
				if (!strand) {
					toEndSite = strings[1];
				}
				
				if (site != 0) {
					tmpResult[3] = (Integer.parseInt(toEndSite) + site) * -1 + "";
				} else {
					tmpResult[3] = toEndSite;
				}
				
				lsResult.add(tmpResult);
			}
		}
	}
}
