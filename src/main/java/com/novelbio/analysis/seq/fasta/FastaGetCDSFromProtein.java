package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.model.modcopeid.GeneType;

/**
 * ����DNA����ΪmRNA���У����ڸ����������У���ø�Fasta����ʼλ�����ֹλ��
 * @author zong0jie
 *
 */
class FastaGetCDSFromProtein {
	SeqFasta seqFasta;
	String proteinSeq;
	public void setSeqFasta(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
	}
	/** ���뵰������ */
	public void setProtein(String proteinSeq) {
		proteinSeq = proteinSeq.trim();
		if (proteinSeq.endsWith("*")) {
			proteinSeq = proteinSeq.substring(0, proteinSeq.length() - 1);
		}
		this.proteinSeq = proteinSeq;
	}
	/** ���ظ�seqfastq��protein����Ӧ��gffgeneiso */
	public GffGeneIsoInfo getGffGeneIsoInfo() {
		ArrayList<BlastSeqFastaCompare> lsAllBlastSeqFasta = new ArrayList<BlastSeqFastaCompare>();
		lsAllBlastSeqFasta.add(proteinBlast(true, 0, seqFasta.toStringAA(true, 0)));
		lsAllBlastSeqFasta.add(proteinBlast(true, 1, seqFasta.toStringAA(true, 1)));
		lsAllBlastSeqFasta.add(proteinBlast(true, 2, seqFasta.toStringAA(true, 2)));
		lsAllBlastSeqFasta.add(proteinBlast(false, 0, seqFasta.toStringAA(false, 0)));
		lsAllBlastSeqFasta.add(proteinBlast(false, 1, seqFasta.toStringAA(false, 1)));
		lsAllBlastSeqFasta.add(proteinBlast(false, 2, seqFasta.toStringAA(false, 2)));
		
		sortLsBlastSeqFasta(lsAllBlastSeqFasta);
		BlastSeqFastaCompare blastSeqFasta = lsAllBlastSeqFasta.get(0);
		return getGffGeneIsoInfo(blastSeqFasta);
		
	}
	/** ָ���������н��бȽϣ������ƶ��ж��
	 * @param proteinSeqFastq ��������
	 * */
	private BlastSeqFastaCompare proteinBlast(boolean cis5to3, int orf, String proteinSeqFastq) {
		BlastSeqFastaCompare blastSeqFasta = new BlastSeqFastaCompare(proteinSeqFastq, proteinSeq);
		blastSeqFasta.cis5to3 = cis5to3;
		blastSeqFasta.orf = orf;
		
		blastSeqFasta.blast();
		return blastSeqFasta;
	}
	/** ����blast��score�������򣬴Ӵ�С�������������� */
	private void sortLsBlastSeqFasta(ArrayList<BlastSeqFastaCompare> lsBlastSeqFastas) {
		Collections.sort(lsBlastSeqFastas, new Comparator<BlastSeqFastaCompare>() {
			@Override
			public int compare(BlastSeqFastaCompare o1, BlastSeqFastaCompare o2) {
				Double score1 = o1.getScore();
				Double score2 = o2.getScore();
				return -score1.compareTo(score2);
			}
		});
	}
	/** ���ݱȶԵĽ������ø����в�����GffGeneIsoInfo����GffGeneIso��û���ں��ӵģ�ֻ��Atg��Uagλ�� */
	private GffGeneIsoInfo getGffGeneIsoInfo(BlastSeqFastaCompare blastSeqFasta) {
		int atgSite, uagSite;
		boolean startWithM = blastSeqFasta.getAlignmentSubject().toLowerCase().startsWith("m");
		atgSite = blastSeqFasta.getStartQuery() * 3 + blastSeqFasta.orf;
		uagSite = blastSeqFasta.getEndQuery() * 3 + blastSeqFasta.orf + 3;
		if (!startWithM) {
			atgSite = scanAtgSite(blastSeqFasta.orf, atgSite);
		}
		uagSite = scanUagSite(blastSeqFasta.orf, uagSite);
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, GeneType.mRNA, blastSeqFasta.cis5to3);
		gffGeneIsoInfo.setATGUAG(atgSite, uagSite);
		gffGeneIsoInfo.add(new ExonInfo(gffGeneIsoInfo, blastSeqFasta.cis5to3, atgSite, uagSite));
		return gffGeneIsoInfo;
	}
	/** �ӱȶԵ������λ����ǰɨ�裬ֱ��ɨ�赽��Զ��UAGλ�㣬ͬʱ��UAG����һλ���ΪATG */
	private int scanAtgSite(int orf, int alignAtgSite) {
		int atgStartSite = orf + alignAtgSite * 3;
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		for (int i = atgStartSite - 3; i >= 0; i = i - 3) {
			//�ҵ���atg
			if (seq[i] == 'A' && seq[i+1] == 'T' && seq[i] == 'G') {
				return i;
			}
			//�ҵ�����ֹλ��
			else if (seq[i] == 'U' &&( (seq[i+1] == 'A' && seq[i + 2] == 'G') || (seq[i+1] == 'G' && seq[i+2] == 'A') || (seq[i+1] == 'G' && seq[i + 2] == 'G'))) {
				return i + 3;
			}
		}
		return orf;
	}
	
	private int scanUagSite(int orf, int alignUagSite) {
		int uagStartSite = orf + alignUagSite * 3;
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		int finishSite = 0;
		for (int i = uagStartSite ; i < seq.length - 3; i = i + 3) {
			//�ҵ���atg
			if (seq[i] == 'U' &&( (seq[i+1] == 'A' && seq[i + 2] == 'G') || (seq[i+1] == 'G' && seq[i+2] == 'A') || (seq[i+1] == 'G' && seq[i + 2] == 'G'))) {
				return i + 2;
			}
			finishSite = i + 2;
		}
		return finishSite + orf;
	}
}

class BlastSeqFastaCompare extends BlastSeqFasta {
	public BlastSeqFastaCompare(SeqFasta seqFastaQuery, SeqFasta seqFastaSubject) {
		super(seqFastaQuery, seqFastaSubject);
	}
	public BlastSeqFastaCompare(String seqFastaQuery, String seqFastaSubject) {
		super(seqFastaQuery, seqFastaSubject);
	}
	boolean cis5to3;
	int orf;
}