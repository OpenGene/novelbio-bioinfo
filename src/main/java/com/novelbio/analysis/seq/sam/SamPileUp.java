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

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SamPileUp {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winF/NBC/Project/Project_HXW/20120705/2A_sorted.bam");
		TxtReadandWrite txtOut = new TxtReadandWrite("/media/winF/NBC/Project/RNASeq_Snp_WJ120725/test.txt", true);
		for (SamRecord samRecord : samFile.readLines()) {
			if (samRecord.getCigar().toString().contains("I")) {
				txtOut.writefileln(samRecord.toString());
			}
		}
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
/** 专门为pileup准备的samrecord */
class SamRecordPileUp {
	Logger logger = Logger.getLogger(SamRecordPileUp.class);
	/** reference 必须大写 */
	private Queue<Character> queueBase;
	
	int baseNum;
	
	SamRecord samRecord;
	String seqRecord;
	List<CigarElement> lsCigarElements;
	/** 本序列内部跳过的区域，譬如deletion。那么遇到这些区域，就不能考虑该reads */
	ArrayList<int[]> lsNoneRegion; 
	public SamRecordPileUp(SamRecord samRecord) {
		this.samRecord = samRecord;
		lsCigarElements = samRecord.getCigar().getCigarElements();
		seqRecord = samRecord.getSeqFasta().toString().toUpperCase();
	}
	/** 设定碱基位数 */
	public void setBaseNum(int baseNum) {
		this.baseNum = baseNum;
	}
	/** 是否在samRecord头尾覆盖的区域内 */
	protected boolean isInRecordRange() {
		if (baseNum<samRecord.getStartAbs() || baseNum > samRecord.getEndAbs()) {
			return false;
		}
		return true;
	}
	/** 给定位置，返回该位置对应的序列 */
	protected String getSequence(int position) {
		int offset = position - samRecord.getStartAbs();
		int numLen = 0;
		for (CigarElement cigarElement : lsCigarElements) {
			if (cigarElement.getOperator() != CigarOperator.I) {
				numLen = numLen + cigarElement.getLength();
			}
			if (offset <= numLen) {
				cigarElement.
				return getSiteSequence(cigarElement, offsetToCigerStart, offsetToSeqStart);
			}
		}
	}
	
	private String getSiteSequence(CigarElement cigarElement, int offsetToCigerStart, int offsetToSeqStart) {
		CigarOperator cigarOperator = cigarElement.getOperator();
		if (cigarOperator == CigarOperator.M || cigarOperator == CigarOperator.X || cigarOperator == CigarOperator.EQ) {
			return compareMisMatch(offsetToSeqStart);
		}
		else if (cigarOperator == CigarOperator.S || cigarOperator == CigarOperator.H || cigarOperator == CigarOperator.N) {
			return "";
		}
		else if (cigarOperator == CigarOperator.I) {
			if (offsetToCigerStart == 0) {
				String result =  "+" + cigarElement.getLength() + seqRecord.substring(offsetToSeqStart, offsetToSeqStart + cigarElement.getLength());
				result = addStartEndSymbol(offsetToSeqStart, result);
				return result;
			}
			return "";
		}
		else if (cigarOperator == CigarOperator.D) {
			return getDeletion(offsetToCigerStart, cigarElement.getLength());
		}
		else {
			logger.error("出现未知 CIGAR 操作符" + cigarElement.toString());
			return null;
		}
	}
	/** 
	 * @param offsetToSeqStart 从0开始计算
	 * @return
	 */
	private String compareMisMatch(int offsetToSeqStart) {
		String result;
		Character charThis = seqRecord.charAt(offsetToSeqStart);
		Character charRef = queueBase.peek();
		if (charThis == charRef) {
			if (samRecord.isCis5to3())
				result = ".";
			else
				result = ",";
		}
		else {
			if (samRecord.isCis5to3())
				result = charThis + "";
			else
				result = Character.toLowerCase(charThis) + "";
		}
		result = addStartEndSymbol(offsetToSeqStart, result);
		return result;
	}
	
	private String getDeletion(int offsetToCigerStart, int deletionLen) {
		if (offsetToCigerStart != 0) {
			return "*";
		}
		char[] deletion = new char[deletionLen];
		int index = 0;
		for (Character character : queueBase) {
			deletion[index] = character;
			index++;
		}
		return "-"+deletionLen + String.copyValueOf(deletion);
	}
	
	private String addStartEndSymbol(int offsetToSeqStart, String result) {
		if (offsetToSeqStart == 0) {
			result = "^" + (char)(samRecord.getMapQuality() + 33) + result;
		}
		else if (offsetToSeqStart == seqRecord.length() - 1) {
			result = "$" + result;
		}
		return result;
	}
}
class BaseInfo {
	long baseNum;
	/** + 开头表示插入
	 * - 开头表示删除
	 */
	String baseDetail;
}