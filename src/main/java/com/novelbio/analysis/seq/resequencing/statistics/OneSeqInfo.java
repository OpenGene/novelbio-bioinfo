package com.novelbio.analysis.seq.resequencing.statistics;

import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;

public class OneSeqInfo extends MapInfoSnpIndel {
	/** ��λ�����snp������ */
	private SnpIndelType snpIndelType;
	private int indelReadsNum;
	
	int snpThresholdReadsNum = 5;
	/**
	 * percentage snp�������ڸðٷֱȵ�λ����Ϊ�Ǵ���snp
	 * ����Ϊ �ٷֱ� * 100
	 */
	int snpThresholdPercentage = 10;
	
	/** ��¼������at��GC���� */
	int sameTypeNum = 0;
	/** �ܱ�������ͬAT �� ��ͬCG ��reads���Ƕ��ۼӣ��������ȡƽ��ֵ */
	int readsNumCumulation = 0;
	
	OneSeqInfo oneSeqInfoLast;
	
	
	private OneSeqInfo(String chrID) {
		super.chrID = chrID;
	}
	
	/**
	 * ������
	 * chr1	914899	A	9	.,,.,,,..	9@>FC@3IG
	 * ��Ϣ������seq
	 * @param pileupLines ����chr1	914899	A	9	.,,.,,,..	9@>FC@3IG
	 * ����sam/bam�ļ�pileup֮����ļ�
	 * @param oneSeqInfoUp ��һ�е���Ϣ
	 * @param snpThresholdReadsNum  ��ʧλ�����Сֵ��snpThresholdPercentage
	 */
	public OneSeqInfo(String pileupLines, OneSeqInfo oneSeqInfoLast, int snpThresholdReadsNum, int snpThresholdPercentage) {
		this.snpThresholdPercentage = snpThresholdPercentage;
		this.snpThresholdReadsNum = snpThresholdReadsNum;

		setSamToolsPilup(pileupLines);
		SiteSnpIndelInfo siteSnpIndelInfo = getSiteSnpInfoBigAllen();
		SnpIndelType snpIndelType = siteSnpIndelInfo.getSnpIndelType();
		
		int indelReadsNum = 0;
		if (snpIndelType == SnpIndelType.CORRECT || snpIndelType == SnpIndelType.MISMATCH) {
			indelReadsNum = 0;
		}else {
			indelReadsNum = siteSnpIndelInfo.getReadsNum();
		}
		this.snpIndelType = snpIndelType;
		this.indelReadsNum = indelReadsNum;
		
		this.oneSeqInfoLast = oneSeqInfoLast;
		if (oneSeqInfoLast != null) {
			if (isSameSiteType_And_Not_N()) {
				this.sameTypeNum = oneSeqInfoLast.sameTypeNum + 1;
				this.readsNumCumulation = oneSeqInfoLast.readsNumCumulation + getReadsNumAll();			
			} else {
				sameTypeNum = 1;
				readsNumCumulation = getReadsNumAll();			
			}
			oneSeqInfoLast.clearOneSeqInfoLast();
		} else {
			sameTypeNum = 1;
			readsNumCumulation = getReadsNumAll();		
		}
	}
	
	public SnpIndelType getSnpIndelType() {
		return snpIndelType;
	}
	
	public int getIndelReadsNum() {
		return indelReadsNum;
	}
	
	public int getReadsNumCumulation() {
		return readsNumCumulation;
	}
	public OneSeqInfo getOneSeqInfoLast() {
		return oneSeqInfoLast;
	}
	/** �������һ��OneSeqInfo������ */
	public void clearOneSeqInfoLast() {
		this.oneSeqInfoLast = null;
	}
	/**
	 * <b>û�н���һ���������</b>
	 * �����һ��λ�����Ϣ��ע�⣺
	 * readsNumAll  �ѵ���reads����
	 * indelReadsNum  ��ʧλ������
	 * ��Ϊ0
	 * @return
	 */
	public OneSeqInfo getOneSeqInfoNext(String refNextBase) {
		OneSeqInfo oneSeqInfoNext = new OneSeqInfo(this.chrID);
		oneSeqInfoNext.oneSeqInfoLast = this;
		oneSeqInfoNext.setRefBase(refNextBase);
		oneSeqInfoNext.refSnpIndelStart = getRefSnpIndelStart() + 1;
		oneSeqInfoNext.snpThresholdPercentage = snpThresholdPercentage;
		oneSeqInfoNext.snpThresholdReadsNum = snpThresholdReadsNum;
		
		if (oneSeqInfoNext.isSameSiteType_And_Not_N()) {
			oneSeqInfoNext.sameTypeNum = this.sameTypeNum + 1;
		} else {
			oneSeqInfoNext.sameTypeNum = 1;
		}
		
		return oneSeqInfoNext;
	}
	
	/** �Ƿ����һ��OneSeqInfo���� */
	public boolean isContinuesSite() {
		if (isSameChrID() && super.refSnpIndelStart == oneSeqInfoLast.getRefSnpIndelStart() + 1) {
			return true;
		}
		return false;
	}
	/** �Ƿ����һ��OneSeqInfo��ͬһ��Ⱦɫ���� */
	public boolean isSameChrID() {
		if (oneSeqInfoLast == null) {
			return false;
		}
		if (chrID.equalsIgnoreCase(oneSeqInfoLast.chrID)) {
			return true;
		}
		return false;
	}
	/** �Ƿ�����һ��site�м���gap */
	public boolean isGapWithOneSeqLast() {
		if (oneSeqInfoLast != null 
				&& chrID.equalsIgnoreCase(oneSeqInfoLast.chrID) 
				&& super.refSnpIndelStart > oneSeqInfoLast.getRefSnpIndelStart() + 1) {
			return true;
		}
		return false;
	}
	/** ���� isGapWithOneSeqLast �ж�Ϊtrueʱ ����Ŀ��������
	 * ����һ��site�м��gap���ȡ�
	 * �����������Ϊ0
	 */
	public int getGapLengthWithLastSeq() {
		return getRefSnpIndelStart() - oneSeqInfoLast.getRefSnpIndelStart() - 1;
	}
	/**
	 * ���������������Ϊ��ͬ��CG��AT�����Ҳ�ΪN
	 * @param oneSeqInfoUp  ��һ��oneSeqInfo
	 * @return
	 */
	public boolean isSameSiteType_And_Not_N() {
		if (oneSeqInfoLast == null || oneSeqInfoLast.getSiteSeqType() == SeqType.N) {
			return false;
		}
		if (isContinuesSite() && oneSeqInfoLast.getSiteSeqType() == this.getSiteSeqType()) {
			return true;
		}
		return false;
	}
	/** �������AT����GC�ĸ��Ƕ�ƽ���� */
	public double getSameSiteNumAvg() {
		return readsNumCumulation/sameTypeNum;
	}
	/** ����AT����GC������ */
	public int getSameSiteNum() {
		return sameTypeNum;
	}
	
	/**
	 * �ж��ǲ���insert
	 * @param oneSeqInfo
	 * @return
	 */
	public boolean isInsert() {
		boolean isInsert = (getSnpIndelType() == SnpIndelType.INSERT) 
									&& (getIndelReadsNum() >= snpThresholdReadsNum)
									&& (getIndelReadsNum()*100/getReadsNumCumulation() > snpThresholdPercentage);
		return isInsert;
	}

	public boolean isDeletion() {
		boolean isDeletion = (getSnpIndelType() == SnpIndelType.DELETION) 
									&& (getIndelReadsNum() >= snpThresholdReadsNum)
									&& (getIndelReadsNum()*100/getReadsNumCumulation() > snpThresholdPercentage);
		return isDeletion;
	}
	/** ��ø�λ������ͣ�Ʃ����CG������AT������N */
	public SeqType getSiteSeqType() {
		if(getRefBase().equals("A") || getRefBase().equals("T")) {
			return SeqType.AT;
		} else if (getRefBase().equals("C") || getRefBase().equals("G")) {
			return SeqType.CG;
		} else {
			return SeqType.N;
		}
	}
	
}

enum SeqType {
	AT, CG, N
}