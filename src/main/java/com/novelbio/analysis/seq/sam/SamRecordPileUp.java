package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

import org.apache.log4j.Logger;

/** ר��Ϊpileup׼����samrecord */
public class SamRecordPileUp {
	Logger logger = Logger.getLogger(SamRecordPileUp.class);
	/** reference �����д */
	private Queue<Character> queueBase;
	
	int baseNum;
	
	SamRecord samRecord;
	String seqRecord;
	List<CigarElement> lsCigarElements;
	/** �������ڲ�����������Ʃ��deletion����ô������Щ���򣬾Ͳ��ܿ��Ǹ�reads */
	ArrayList<int[]> lsNoneRegion; 
	
	public SamRecordPileUp() {}

	public SamRecordPileUp(SamRecord samRecord) {
		setSamRecord(samRecord);
	}
	public void setSamRecord(SamRecord samRecord) {
		this.samRecord = samRecord;
		lsCigarElements = samRecord.getCigar().getCigarElements();
		seqRecord = samRecord.getSeqFasta().toString().toUpperCase();
	}
	/** ������ */
	public void addBase(String base) {
		if (queueBase == null) {
			queueBase = new ArrayBlockingQueue<Character>(500);
		}
		char[] chrbase = base.toCharArray();
		for (char c : chrbase) {
			queueBase.add(c);
		}
	}
	/** �����ã�ɾ�������еĵ�һ����� */
	public void pollBase() {
		queueBase.poll();
	}
	/** �趨���λ�� */
	public void setBaseNum(int baseNum) {
		this.baseNum = baseNum;
	}
	/** �Ƿ���samRecordͷβ���ǵ������� */
	protected boolean isInRecordRange() {
		if (baseNum<samRecord.getStartAbs() || baseNum > samRecord.getEndAbs()) {
			return false;
		}
		return true;
	}
	/** ����λ�ã����ظ�λ�ö�Ӧ������ */
	public String getSequence(int position) {
		int offsetToSeqStartRef = position - samRecord.getStartAbs();
		int offsetToSeqStartThis = offsetToSeqStartRef;
		int offsetToCigarStart, offsetToCigarEnd;
		int numAllRefSeqLen = 0;//��¼����refseq�ĳ���
		int numAllRefSeqLenLast = 0;//��¼����refseq�ĳ��ȵ�ǰһλ
		for (int i = 0; i < lsCigarElements.size(); i++) {
			CigarElement cigarElement = lsCigarElements.get(i);
			if (cigarElement.getOperator() != CigarOperator.I) {
				numAllRefSeqLen = numAllRefSeqLen + cigarElement.getLength();
			}
			else if (cigarElement.getOperator() == CigarOperator.I) {
				offsetToSeqStartThis = offsetToSeqStartThis + cigarElement.getLength();
			}
			if (offsetToSeqStartRef < numAllRefSeqLen) {
				offsetToCigarStart = offsetToSeqStartRef - numAllRefSeqLenLast;
				offsetToCigarEnd = numAllRefSeqLen - offsetToSeqStartRef - 1;
				CigarElement cigarElementNext = null;
				if (i + 1 < lsCigarElements.size()) {
					cigarElementNext = lsCigarElements.get(i + 1);
				}
				//���������ļ���һ�����������������֮��ֱ�Ӿͷ��ؽ����
				if (cigarElement.getOperator() == CigarOperator.D) {
					offsetToSeqStartThis = offsetToSeqStartThis - offsetToCigarStart;
				}
				return getSiteSequence(cigarElement, offsetToCigarStart, offsetToCigarEnd, offsetToSeqStartRef, offsetToSeqStartThis, cigarElementNext);
			}
			
			if (cigarElement.getOperator() == CigarOperator.D) {
				offsetToSeqStartThis = offsetToSeqStartThis - cigarElement.getLength();
			}
			numAllRefSeqLenLast = numAllRefSeqLen;
		}
		return null;
	}
	
	private String getSiteSequence(CigarElement cigarElement, int offsetToCigarStart, int offsetToCigarEnd,int offsetToSeqStartRef, int offsetToSeqStartThis, CigarElement cigarElementNext) {
		CigarOperator cigarOperator = cigarElement.getOperator();
		CigarOperator cigarOperatorNext = CigarOperator.EQ;
		if (cigarElementNext != null) {
			cigarOperatorNext = cigarElementNext.getOperator();
		}
		
		if (cigarOperator == CigarOperator.M || cigarOperator == CigarOperator.X || cigarOperator == CigarOperator.EQ) {
			if (offsetToCigarEnd != 0 || (cigarOperatorNext != CigarOperator.I && cigarOperatorNext != CigarOperator.D) ) {
				return compareMisMatch(offsetToSeqStartThis);
			}
			else {
				if (cigarOperatorNext == CigarOperator.I) {
					String result =  compareMisMatch(offsetToSeqStartThis) + "+" + cigarElementNext.getLength() +
							seqRecord.substring(offsetToSeqStartThis + 1, offsetToSeqStartThis + 1 + cigarElementNext.getLength());
					result = addStartEndSymbol(offsetToSeqStartThis, result);
					return result;
				}
				else {
					String result = getDeletion(cigarElementNext.getLength());
					result = compareMisMatch(offsetToSeqStartThis) + "-" + cigarElementNext.getLength() + result;
					return result;
				}
			}
		}
		else if (cigarOperator == CigarOperator.S || cigarOperator == CigarOperator.H || cigarOperator == CigarOperator.N) {
			return  "";
		}
		else if (cigarOperator == CigarOperator.D) {
			return "*";
		}
		else {
			logger.error("����δ֪ CIGAR ������" + cigarElement.toString());
			return null;
		}
	}
	/** 
	 * @param offsetToSeqStart ��0��ʼ����
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
	
	private String getDeletion(int deletionLen) {
		char[] deletion = new char[deletionLen];
		int index = -2;//һ����ѭ���ͻ��1��Ȼ��Ҫ����һλ
		for (Character character : queueBase) {
			index++;
			if (index == -1) {//�ڶ�λ��ʼ����ȱʧ���������������һλ
				continue;
			}
			if (index >= deletionLen) {
				break;
			}
			deletion[index] = character;
		}
		return String.copyValueOf(deletion);
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
