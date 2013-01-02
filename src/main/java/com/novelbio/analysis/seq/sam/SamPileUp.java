package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SamPileUp {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("C:\\Users\\jie\\Desktop\\paper\\KOod.bam");
//		samFile.indexMake();chr3:81,944,867-c
		for (SamRecord samRecord : samFile.readLinesContained("chr3", 81944867, 81955050)) {
			System.out.println(samRecord.toString());
		}
	}
	/** �����ܲ���ļ�� */
	int maxBaseNum = 100;
	
	private Queue<SamRecord> queueSamRecord = new ConcurrentLinkedQueue<SamRecord>();
	/** ���ܲ��� */
	private Queue<Character> queueBase = new ArrayBlockingQueue<Character>(maxBaseNum);
	/** ����ĳ��λ����˵���Ƿ��Ѿ�׼������Ӧ�����м� */
	boolean prepareForBase = false;
	
	Map<String, Long> mapChrIDlowcase2Length;
	
	/**
	 * �趨Ⱦɫ�������볤�ȵĶ��ձ�ע��keyΪСд
	 * @param mapChrIDlowcase2Length
	 */
	public void setMapChrIDlowcase2Length(
			Map<String, Long> mapChrIDlowcase2Length) {
		this.mapChrIDlowcase2Length = mapChrIDlowcase2Length;
	}
	
	private double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		
		for (SamRecord samRecord : queueSamRecord) {
			ArrayList<Align> lsAlign = samRecord.getAlignmentBlocks();
		}
		
		
		return null;
	}
	
}

class BaseInfo {
	long baseNum;
	/** + ��ͷ��ʾ����
	 * - ��ͷ��ʾɾ��
	 */
	String baseDetail;
}