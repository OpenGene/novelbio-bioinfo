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
	/** �����ܲ���ļ�� */
	int maxBaseNum = 100;
	
	private Queue<SamRecord> queueSamRecord = new ConcurrentLinkedQueue<SamRecord>();
	/** ���ܲ��� */
	private Queue<Character> queueBase = new ArrayBlockingQueue<Character>(maxBaseNum);
	/** ����ĳ��λ����˵���Ƿ��Ѿ�׼������Ӧ�����м� */
	boolean prepareForBase = false;
	
	private void name() {
		queueSamRecord.
	}
	
	
	
}

class BaseInfo {
	long baseNum;
	/** + ��ͷ��ʾ����
	 * - ��ͷ��ʾɾ��
	 */
	String baseDetail;
}