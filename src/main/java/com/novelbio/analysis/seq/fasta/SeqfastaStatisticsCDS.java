package com.novelbio.analysis.seq.fasta;
/** �ҵ�һ�����������ת¼���ȵ� */
public class SeqfastaStatisticsCDS {
	
	/** �aa���ڵ�orf */
	int orfAllLen;
	/** ���M��ͷ��aa���ڵ�orf */
	int orfMstartLen;
	/** �aa�ķ��� */
	boolean cis5to3AllLen;
	/** ���M��ͷ��aa�ķ��� */
	boolean cis5to3MstartLen;
	/** �Ƿ�Ϊȫ��cds��Ҳ��������Ҫ֪����ָ�� */
	boolean fullCds;
	/** ��һ��stop����һ��stop���aa���� */
	int AllAAlen;
	/** ��һ��M��һ��stop���aa���� */
	int MstartAAlen;
	
	/** AllAA���дӵڼ��������Ὺʼ����0��ʼ���� */
	int startIndexAllAA = 0;
	/** Maa���дӵڼ��������Ὺʼ����0��ʼ���� */
	int startIndexMAA = 0;
	
	SeqFasta seqFasta;
	
	protected SeqfastaStatisticsCDS(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
		calculateAAseqInfo();
	}
 
	public int getAllAAlen() {
		return AllAAlen;
	}
	public int getMstartAAlen() {
		return MstartAAlen;
	}
	public int getOrfAllLen() {
		return orfAllLen;
	}
	public int getOrfMstartLen() {
		return orfMstartLen;
	}
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}
	public int getStartIndexAllAA() {
		return startIndexAllAA;
	}
	public int getStartIndexMAA() {
		return startIndexMAA;
	}
	public boolean isCis5to3AllLen() {
		return cis5to3AllLen;
	}
	public boolean isCis5to3MstartLen() {
		return cis5to3MstartLen;
	}
	public boolean isFullCds() {
		return fullCds;
	}
	
	public void calculateAAseqInfo() {
		String seqAAcis0 = seqFasta.toStringAA(true, 0);
		String seqAAcis1 = seqFasta.toStringAA(true, 1);
		String seqAAcis2 = seqFasta.toStringAA(true, 2);

		String seqAAtrans0 = seqFasta.toStringAA(false, 0);
		String seqAAtrans1 = seqFasta.toStringAA(false, 1);
		String seqAAtrans2 = seqFasta.toStringAA(false, 2);
		
		calculateFullLengthCDS(seqAAcis0, 0, true);
		calculateFullLengthCDS(seqAAcis1, 1, true);
		calculateFullLengthCDS(seqAAcis2, 2, true);
		
		calculateFullLengthCDS(seqAAtrans0, 0, false);
		calculateFullLengthCDS(seqAAtrans1, 1, false);
		calculateFullLengthCDS(seqAAtrans2, 2, false);
	}
	/** ��Junit�����õ� */
	public void calculateFullLengthCDS(String aaseq, int orf, boolean cis5to3) {
		int tmpAllAAlen = 0;
		int tmpMstartAAlen = 0;
		
		int tmpStart = 0;
		
		boolean mStart = false;
		
		char[] aachar = aaseq.toCharArray();
		for (int i = 0; i < aachar.length; i++) {
			char c = aachar[i];
			if (CodeInfo.AA1_STOP.equals(c+"")) {
				setAllLen(tmpStart, tmpAllAAlen, orf, cis5to3);
				tmpAllAAlen = 0;
				
				setMstartAALen(tmpStart, tmpMstartAAlen, orf, cis5to3);
				tmpMstartAAlen = 0;
				
				mStart = false;
				tmpStart = i + 1;
			}
			else {
				tmpAllAAlen++;
				if (mStart) {
					tmpMstartAAlen++;
				}
				else if (CodeInfo.AA1_Met.equals(c+"") && !mStart) {
					mStart = true;
					tmpMstartAAlen++;
				}
			}
		}
		setAllLen(tmpStart, tmpAllAAlen, orf, cis5to3);
		if (setMstartAALen(tmpStart, tmpMstartAAlen, orf, cis5to3)) {
			fullCds = false;
		}
	}
	
	private boolean setAllLen(int AAstartNum, int tmpAllAALen, int orf, boolean cis5to3) {
		if (tmpAllAALen > AllAAlen) {
			AllAAlen = tmpAllAALen;
			this.orfAllLen = orf;
			this.cis5to3AllLen = cis5to3;
			this.startIndexAllAA = AAstartNum;
			return true;
		}
		return false;
	}
	
	private boolean setMstartAALen(int AAstartNum, int tmpMstartAALen, int orf, boolean cis5to3) {
		if (tmpMstartAALen > MstartAAlen) {
			MstartAAlen = tmpMstartAALen;
			this.orfMstartLen = orf;
			fullCds = true;
			this.cis5to3MstartLen = cis5to3;
			this.startIndexMAA = AAstartNum;
			return true;
		}
		return false;
	}
}