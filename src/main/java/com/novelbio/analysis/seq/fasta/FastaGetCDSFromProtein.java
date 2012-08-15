package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListGff;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * ������
 * ����DNA����ΪmRNA���У����ڸ����������У���ø�Fasta����ʼλ�����ֹλ��
 * @author zong0jie
 *
 */
public class FastaGetCDSFromProtein {
	SeqFasta seqFasta;
	String proteinSeq;
	protected FastaGetCDSFromProtein(SeqFasta seqFasta, String protein) {
		this.seqFasta = seqFasta;
		setProtein(protein);
	}
	/** ���뵰������ */
	private void setProtein(String proteinSeq) {
		proteinSeq = proteinSeq.trim();
		if (proteinSeq.endsWith("*")) {
			proteinSeq = proteinSeq.substring(0, proteinSeq.length() - 1);
		}
		this.proteinSeq = proteinSeq;
	}
	/** ���ؽ������û����listGff */
	public ListGff getGffDetailGene() {
		GffGeneIsoInfo gffGeneIsoInfo = getGffGeneIsoInfo();

		ListGff listGff = new ListGff();
		listGff.setName(seqFasta.getSeqName());
		GffDetailGene gffDetailGene = new GffDetailGene(listGff, seqFasta.getSeqName(), gffGeneIsoInfo.isCis5to3());
		gffDetailGene.addIso(gffGeneIsoInfo);
		listGff.add(gffDetailGene);
		listGff.setCis5to3(true);
		
		return listGff;
	}
	public GffGeneIsoInfo getGffGeneIsoInfo() {
		if (proteinSeq != null && !proteinSeq.trim().equals("")) {
			return getGffGeneIsoInfoWtihProtein();
		}
		else {
			return getGffGeneIsoInfoWtihOutProtein();
		}
	}
	private GffGeneIsoInfo getGffGeneIsoInfoWtihOutProtein() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, GeneType.ncRNA, true);
		gffGeneIsoInfo.setATGUAGncRNA();
		gffGeneIsoInfo.add(new ExonInfo(gffGeneIsoInfo, true, 1, seqFasta.getLength()));
		return gffGeneIsoInfo;
	}
	
	/** ���ظ�seqfastq��protein����Ӧ��gffgeneiso */
	private GffGeneIsoInfo getGffGeneIsoInfoWtihProtein() {
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
		boolean startWithM = isStartWithM(blastSeqFasta);
		atgSite = blastSeqFasta.getStartQuery() * 3 + blastSeqFasta.orf;
		uagSite = blastSeqFasta.getEndQuery() * 3 + blastSeqFasta.orf;
		if (!startWithM) {
			atgSite = scanAtgSite(blastSeqFasta.orf, atgSite);
		}
		atgSite = atgSite + 1;//�޸�Ϊ��1��ʼ����ΪGffGeneIso���涼�Ǵ�1��ʼ������
		uagSite = scanUagSite(blastSeqFasta.orf, uagSite);
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, GeneType.mRNA, blastSeqFasta.cis5to3);
		gffGeneIsoInfo.setATGUAG(atgSite, uagSite);
		gffGeneIsoInfo.add(new ExonInfo(gffGeneIsoInfo, blastSeqFasta.cis5to3, atgSite, uagSite));
		return gffGeneIsoInfo;
	}
	private boolean isStartWithM(BlastSeqFastaCompare blastSeqFasta) {
		if (blastSeqFasta.getStartSubject() == 0 && blastSeqFasta.getAlignmentSubject().toLowerCase().startsWith("m") ) {
			return true;
		}
		return false;
	}
	/** �ӱȶԵ������λ����ǰɨ�裬ֱ��ɨ�赽��Զ��UAGλ�㣬ͬʱ��UAG����һλ���ΪATG */
	private int scanAtgSite(int orf, int alignAtgSite) {
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		for (int i = alignAtgSite - 3; i >= 0; i = i - 3) {
			//�ҵ���atg
			if (isATG(new char[]{seq[i], seq[i+1], seq[i+2]})) {
				return i;
			}
			//�ҵ�����ֹλ��
			else if (isUAG(new char[]{seq[i], seq[i+1], seq[i+2]})) {
				return i + 3;
			}
		}
		return orf;
	}
	
	private int scanUagSite(int orf, int alignUagSite) {
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		int finishSite = 0;
		for (int i = alignUagSite ; i < seq.length - 3; i = i + 3) {
			//�ҵ���atg
			if (isUAG(new char[]{seq[i], seq[i+1], seq[i+2]})) {
				return i + 3;
			}
			finishSite = i + 3;
		}
		return finishSite;
	}
	private boolean isATG(char[] seq) {
		if (seq[0] == 'A' && seq[1] == 'T' && seq[2] == 'G')
			return true;
		
		return false;
	}
	private boolean isUAG(char[] seq) {
		if ((seq[0] == 'T' || seq[0] == 'U') &&( (seq[1] == 'A' && seq[2] == 'G') || (seq[1] == 'G' && seq[2] == 'A') || (seq[1] == 'A' && seq[2] == 'A')))
			return true;
		
		return false;
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