package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.fastq.FQrecordFilterAdaptor;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.database.model.modgeneid.GeneType;

/**
 * 不对外
 * 假设DNA序列为mRNA序列，现在给定蛋白序列，获得该Fasta的起始位点和终止位点
 * @author zong0jie
 *
 */
public class FastaGetCDSFromProtein {
	public static void main(String[] args) {
		String nrseq = "ATGGAGCCTGGCAGCATGGAAAATCTGTCCATCGTGTACCAGAGCAGTGACTTCTTGGTGGTGAACAAGCATTGGGAT" +
				"CTACGCATTGATAGCAAGACCTGGCAGGAGACCCTGACCCTACAGAAGCAACTATGTCACCGCTTCCCAGAACTGGCTGACCCTGACACC" +
				"TGCTATGGGTTCAGGTTCTGCCACCAATTGGACTTCTCCACCAGTGGGGCGTTATGTGTGGCCCTGAATAAAGCAGCTGCAGGCAGCGCTTATAA" +
				"ATGCTTCAAGGAACGGCGAGTCACCAAAGCCTACCTTGCACTAGTCCGGGGCCATGTCCAAAAAAGCCAGGTGACAATCAGCTATGCCATTGGCAGGA" +
				"ACAGTACAGAGGGGCGGACCCACACCATGTGCATCGAGGGCACACACGGTTGCGAGAACCCTAAGCCAAGTCTCACAGAGCTGTTGGTTCTGGAGCATG" +
				"GACAGTATGCTGGAGACCCTGTGTCCAAAGTGCTGCTGAAACCACTCACAGGCCGGACACACCAGCTTCGAGTACACTGCAGTGCCCTCGGCCACCCTATTGT" +
				"GGGTGACTTGACTTATGGGCAGGCTGAGGACCAGGAGGACCAACCTTTCCGCATGATGTTGCACGCCTTCTACCTACGCATCCCCACACAGGCCGAGTGTGTGG" +
				"AGGCCTGCACACCTGACCCCTTCCTGCCTGCCCTTGATGCCTGCTGGAGTCCCCATACCTGCATTCAACCCCTTGAAGAGCTCATCCAGGCCCTACAGACTGG" +
				"CCCTGACCCAAACCCTGTGGACGGAGG" +
				"GCACAGTCCCTCCACACCCCTGGCCAGGCCAGGTCGGCCGCCTCCTGAGACTGAGGCGCAGCGAGCATCATGCCTGCAGTGGCTATCAGAGTGGGCACTAGAGCCAGACAACTGA";
		
		
		String proteinStartSeq = "MEPGSMENLSIVYQSSDFLVVNKHWDLRIDSKTWQETLTLQKQLCH" +
				"RFPELADPDTCYGFRFCHQLDFSTSGALCVALNKAAAGSAYKCFKERRVTKAYLALVRGHV" +
				"QKSQVTISYAIGRNSTEGRTHTMCIEGTHGCENPKPSLTELLVLEHGQYAGDPVSKVLLKPLTGR" +
				"THQLRVHCSALGHPIVGDLTYGQAEDQEDQPFRMMLHAFYLRIPTQAECVEACTPDPFLPALDAC" +
				"WSPHTCIQPLEELIQALQTGPDPNPVDGGHSPSTPLARPGRPPPETEAQRASCLQWLSEWALEPDN";
		
		SeqFasta seqFastaNR = new SeqFasta(nrseq);
		
		FastaGetCDSFromProtein fastaGetCDSFromProtein = new FastaGetCDSFromProtein(seqFastaNR, proteinStartSeq);
		GffGeneIsoInfo gffGeneIsoInfo1 = fastaGetCDSFromProtein.getGffGeneIsoInfo();
		GffGeneIsoInfo gffGeneIsoInfo2 = fastaGetCDSFromProtein.getGffGeneIsoInfoWtihProteinBlast();
		System.out.println(gffGeneIsoInfo1.equals(gffGeneIsoInfo2));
	}
	SeqFasta seqFasta;
	String proteinSeq;
	boolean getBlastIso = false;
	public FastaGetCDSFromProtein(SeqFasta seqFasta, String protein) {
		this.seqFasta = seqFasta;
		setProtein(protein);
	}
	/** 是否采用blast的方法来比对序列并获得atg和uag信息 */
	public void setGetBlastIso(boolean getBlastIso) {
		this.getBlastIso = getBlastIso;
	}
	/** 是否通过blast的方法来获取序列
	public void setGetBlastIso(boolean getBlastIso) {
		this.getBlastIso = getBlastIso;
	}
	/** 输入蛋白序列 */
	private void setProtein(String proteinSeq) {
		if (proteinSeq == null) {
			return;
		}
		proteinSeq = proteinSeq.trim();
		if (proteinSeq.endsWith("*")) {
			proteinSeq = proteinSeq.substring(0, proteinSeq.length() - 1);
		}
		this.proteinSeq = proteinSeq;
	}
	/** 返回仅包含该基因的listGff */
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
	/** 用blast的方法来找位点 */
	public GffGeneIsoInfo getGffGeneIsoInfo() {
		if (proteinSeq != null && !proteinSeq.trim().equals("")) {
			if (getBlastIso) {
				return getGffGeneIsoInfoWtihProteinBlast();
			}
			else {
				return getGffGeneIsoInfoWtihProteinNoBlast();
			}
		}
		else {
			return getGffGeneIsoInfoWtihOutProtein();
		}
	}
	private GffGeneIsoInfo getGffGeneIsoInfoWtihOutProtein() {
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, seqFasta.SeqName, GeneType.ncRNA, true);
		gffGeneIsoInfo.add(new ExonInfo(true, 1, seqFasta.Length()));
		gffGeneIsoInfo.setATGUAGncRNA();
		return gffGeneIsoInfo;
	}
	/** 返回该seqfastq和protein所对应的gffgeneiso */
	private GffGeneIsoInfo getGffGeneIsoInfoWtihProteinBlast() {
		SeqfastaStatisticsCDS seqfastaStatisticsCDS = seqFasta.statisticsCDS();
		int orf = seqfastaStatisticsCDS.getOrfAllLen();
		boolean cis5to3 = seqfastaStatisticsCDS.isCis5to3AllLen();
		ArrayList<BlastSeqFastaCompare> lsAllBlastSeqFasta = new ArrayList<BlastSeqFastaCompare>();
		lsAllBlastSeqFasta.add(proteinBlast(cis5to3, orf, seqFasta.toStringAA(cis5to3, orf)));
		
		sortLsBlastSeqFasta(lsAllBlastSeqFasta);
		BlastSeqFastaCompare blastSeqFasta = lsAllBlastSeqFasta.get(0);
		return getGffGeneIsoInfo(blastSeqFasta);
	}
	/** 指定蛋白序列进行比较，看相似度有多高
	 * @param proteinSeqFastq 蛋白序列
	 * */
	private BlastSeqFastaCompare proteinBlast(boolean cis5to3, int orf, String proteinSeqFastq) {
		BlastSeqFastaCompare blastSeqFasta = new BlastSeqFastaCompare(proteinSeqFastq, proteinSeq);
		blastSeqFasta.cis5to3 = cis5to3;
		blastSeqFasta.orf = orf;
		
		blastSeqFasta.blast();
		return blastSeqFasta;
	}
	/** 按照blast的score进行排序，从大到小排序，最上面的最大 */
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
	/** 根据比对的结果，获得该序列产生的GffGeneIsoInfo，本GffGeneIso是没有内含子的，只有Atg和Uag位点 */
	private GffGeneIsoInfo getGffGeneIsoInfo(BlastSeqFastaCompare blastSeqFasta) {
		int atgSite, uagSite;
		boolean startWithM = isStartWithM(blastSeqFasta);
		atgSite = blastSeqFasta.getStartQuery() * 3 + blastSeqFasta.orf;
		uagSite = blastSeqFasta.getEndQuery() * 3 + blastSeqFasta.orf;
		if (!startWithM) {
			atgSite = scanAtgSite(blastSeqFasta.orf, atgSite);
		}
		atgSite = atgSite + 1;//修改为从1开始，因为GffGeneIso里面都是从1开始记数的
		uagSite = scanUagSite(blastSeqFasta.orf, uagSite);
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, seqFasta.SeqName, GeneType.mRNA, blastSeqFasta.cis5to3);
		gffGeneIsoInfo.setATGUAGauto(atgSite, uagSite);
		gffGeneIsoInfo.add(new ExonInfo(blastSeqFasta.cis5to3, atgSite, uagSite));
		return gffGeneIsoInfo;
	}
	
	private boolean isStartWithM(BlastSeqFastaCompare blastSeqFasta) {
		if (blastSeqFasta.getStartSubject() == 0 && blastSeqFasta.getAlignmentSubject().toLowerCase().startsWith("m") ) {
			return true;
		}
		return false;
	}
	
	/** 从比对的最近的位点向前扫描，直到扫描到最远的UAG位点，同时将UAG后面一位标记为ATG */
	private int scanAtgSite(int orf, int alignAtgSite) {
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		for (int i = alignAtgSite - 1; i >= 0; i = i - 3) {
			//找到了atg
			if (isATG(new char[]{seq[i], seq[i + 1], seq[i + 2]})) {
				return i + 1;
			}
			//找到了终止位点
			else if (isUAG(new char[]{seq[i], seq[i + 1], seq[i + 2]})) {
				return i + 3;
			}
		}
		return alignAtgSite;
	}
	private int scanUagSite(int orf, int alignUagSite) {
		char[] seq = seqFasta.SeqSequence.toUpperCase().toCharArray();
		int finishSite = 0;
		for (int i = alignUagSite - 3; i <= seq.length - 3; i = i + 3) {
			//找到了atg
			if (isUAG(new char[]{seq[i], seq[i + 1], seq[i+2]})) {
				return i + 3;
			}
			finishSite = i + 3;
		}
		if (finishSite == 0) {
			return alignUagSite;
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
	
	
	/** 返回该seqfastq和protein所对应的gffgeneiso */
	private GffGeneIsoInfo getGffGeneIsoInfoWtihProteinNoBlast() {
		CompareInfo compareInfo = getStartEndSite();
		if (compareInfo == null) {
			compareInfo = getStartEndWithMaxCDS();
		}

		return getGffGeneIsoInfo(compareInfo, proteinSeq);
	}
	
	private CompareInfo getStartEndWithMaxCDS() {
		SeqfastaStatisticsCDS seqfastaStatisticsCDS = seqFasta.statisticsCDS();
		CompareInfo compareInfo = new CompareInfo();
		compareInfo.atgAASite = seqfastaStatisticsCDS.getStartIndexMAA() + 1;
		compareInfo.uagAASite = seqfastaStatisticsCDS.getEndIndexMAA();
		compareInfo.orf = seqfastaStatisticsCDS.getOrfMstartLen();
		compareInfo.cis5to3 = seqfastaStatisticsCDS.isCis5to3MstartLen();
		return compareInfo;
	}
	
	/**
	 * 获得序列的前30个氨基酸去扫描序列
	 * @return
	 */
	private CompareInfo getStartEndSite () {
		boolean cis5to3 = true;
		CompareInfo compareInfo = null;
		String proteinStartSite = "";
		String proteinEndSite = "";
		if (proteinSeq.length() <= 30) {
			proteinStartSite = proteinSeq;
			proteinEndSite = proteinSeq;
		} else {
			proteinStartSite = proteinSeq.substring(0, 30);
			proteinEndSite = proteinSeq.substring(proteinSeq.length() - 30, proteinSeq.length());
		}

		String proteinSeqTranslate = null;
		int atgsite = 0;
		for (int orf = 0; orf < 3; orf++) {
			proteinSeqTranslate = seqFasta.toStringAA(cis5to3, orf);
			atgsite = FQrecordFilterAdaptor.trimAdaptorR(proteinSeqTranslate, proteinStartSite, 0, 3, 1, 80,20);
			if (atgsite >= 0 && atgsite < proteinSeqTranslate.length()) {
				compareInfo = new CompareInfo();
				compareInfo.atgAASite = atgsite;
				compareInfo.cis5to3 = cis5to3;
				compareInfo.orf = orf;
				break;
			}
		}

		
		if (compareInfo == null) {
			return null;
		}
		
		int uagsite = FQrecordFilterAdaptor.trimAdaptorL(proteinSeqTranslate, proteinEndSite, 0, 3, 1, 80, 20);
		if (uagsite > atgsite && uagsite <= proteinSeqTranslate.length()) {
			compareInfo.uagAASite = uagsite;
			return compareInfo;
		}
		return null;
	}
	
	/** 根据比对的结果，获得该序列产生的GffGeneIsoInfo，本GffGeneIso是没有内含子的，只有Atg和Uag位点 */
	private GffGeneIsoInfo getGffGeneIsoInfo(CompareInfo compareInfo, String proteinSeq) {
		int atgSite, uagSite;
		atgSite = compareInfo.atgAASite * 3 + compareInfo.orf - 2;
		uagSite = compareInfo.uagAASite * 3 + compareInfo.orf + 3;
		
		boolean startWithM = isStartWithM(atgSite);

		if (!startWithM) {
			atgSite = scanAtgSite(compareInfo.orf, atgSite);
		}
		uagSite = scanUagSite(compareInfo.orf, uagSite);
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(seqFasta.SeqName, seqFasta.SeqName, GeneType.mRNA, compareInfo.cis5to3);
		gffGeneIsoInfo.setATGUAGauto(atgSite, uagSite);
		gffGeneIsoInfo.add(new ExonInfo(compareInfo.cis5to3, 1, seqFasta.Length()));
		return gffGeneIsoInfo;
	}
	
	private boolean isStartWithM(int atgSite) {
		String seq = seqFasta.toString().substring(atgSite-1, atgSite + 2).toUpperCase();
		if (seq.startsWith("ATG") ) {
			return true;
		}
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

/**
 * 比较的结果，包含atg位点，uag位点等
 * @author zong0jie
 *
 */
class CompareInfo {
	/** 从1开始记数 */
	int atgAASite;
	/** 从1开始记数 */
	int uagAASite;
	boolean cis5to3;
	int orf;
}
