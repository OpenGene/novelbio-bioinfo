package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SamPileUp {
	public static void main(String[] args) {
		SeqFasta seqFasta1 = new SeqFasta("NAAGAGAAATCTTTAGTATATGGTCCGTTGGTCAAGGGGTTAAGACACCGCCTTTTCACGGCGGTAACACGGGTTCGAATCNTATTCCGCCATAGCTCAGTTGGTAGAGCGCATGACTGTTAATCATGATGTCACAGGTTCGAGCCCTGNTAAGCGGGTGTAGTTTAGTGGTAAAACTACAGCCTTCCAAGCTGTTGTCGCGAGTTCGATTCTCGTCACCCGCTTTGAACATAGTTCATACCCAAACTTGGTTTGGGCGCGTAGCTCAGATGGTTAGAGCGCACGCCTGATAAGCGTGAGGTCGGTGGTTCGATTCCN");
		SeqFasta seqFasta2 = new SeqFasta("TATTCCGCCATAGCTCAGTTGGTAGAGCGCATGACTGTTAATCATGATGTCACAGGTTCGAGCCCTGTTGGCGGAGTAAAGAGAAATCTTTAGTATATGGTCCGTTGGTCAAGGGGTTAAGACACCGCCTTTTCACGGCGGTAACACGGGTTCGAATCCCGTACGGACTATATT");
		BlastSeqFasta blastSeqFasta = new BlastSeqFasta(seqFasta1, seqFasta2);
		blastSeqFasta.blast();
		System.out.println(blastSeqFasta.getAlignmentQuery());
		System.out.println(blastSeqFasta.getAlignmentSubject());
	
	}
	/** 最多可能插入的碱基 */
	int maxBaseNum = 100;
	
	private Queue<SamRecord> queueSamRecord = new ConcurrentLinkedQueue<SamRecord>();
	/** 不能并发 */
	private Queue<Character> queueBase = new ArrayBlockingQueue<Character>(maxBaseNum);
	/** 对于某个位点来说，是否已经准备好相应的序列集 */
	boolean prepareForBase = false;
	
	private void name() {
		queueSamRecord.
	}
	
	
	
}

class BaseInfo {
	long baseNum;
	/** + 开头表示插入
	 * - 开头表示删除
	 */
	String baseDetail;
}