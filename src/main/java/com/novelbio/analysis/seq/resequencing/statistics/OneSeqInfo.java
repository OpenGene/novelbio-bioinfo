package com.novelbio.analysis.seq.resequencing.statistics;

import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;

public class OneSeqInfo extends MapInfoSnpIndel {
	/** 本位点最多snp的类型 */
	private SnpIndelType snpIndelType;
	private int indelReadsNum;
	
	int snpThresholdReadsNum = 5;
	/**
	 * percentage snp数量大于该百分比的位点认为是存在snp
	 * 输入为 百分比 * 100
	 */
	int snpThresholdPercentage = 10;
	
	/** 记录连续的at或GC数量 */
	int sameTypeNum = 0;
	/** 周边连续相同AT 或 相同CG 的reads覆盖度累加，方便最后取平均值 */
	int readsNumCumulation = 0;
	
	OneSeqInfo oneSeqInfoLast;
	
	
	private OneSeqInfo(String chrID) {
		super.chrID = chrID;
	}
	
	/**
	 * 用类似
	 * chr1	914899	A	9	.,,.,,,..	9@>FC@3IG
	 * 信息来设置seq
	 * @param pileupLines 类似chr1	914899	A	9	.,,.,,,..	9@>FC@3IG
	 * 来自sam/bam文件pileup之后的文件
	 * @param oneSeqInfoUp 上一行的信息
	 * @param snpThresholdReadsNum  得失位点的最小值，snpThresholdPercentage
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
	/** 清除对上一个OneSeqInfo的引用 */
	public void clearOneSeqInfoLast() {
		this.oneSeqInfoLast = null;
	}
	/**
	 * <b>没有将上一个引用清空</b>
	 * 获得下一个位点的信息，注意：
	 * readsNumAll  堆叠的reads数量
	 * indelReadsNum  得失位点数量
	 * 都为0
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
	
	/** 是否和上一个OneSeqInfo连续 */
	public boolean isContinuesSite() {
		if (isSameChrID() && super.refSnpIndelStart == oneSeqInfoLast.getRefSnpIndelStart() + 1) {
			return true;
		}
		return false;
	}
	/** 是否和上一个OneSeqInfo在同一条染色体上 */
	public boolean isSameChrID() {
		if (oneSeqInfoLast == null) {
			return false;
		}
		if (chrID.equalsIgnoreCase(oneSeqInfoLast.chrID)) {
			return true;
		}
		return false;
	}
	/** 是否与上一个site中间有gap */
	public boolean isGapWithOneSeqLast() {
		if (oneSeqInfoLast != null 
				&& chrID.equalsIgnoreCase(oneSeqInfoLast.chrID) 
				&& super.refSnpIndelStart > oneSeqInfoLast.getRefSnpIndelStart() + 1) {
			return true;
		}
		return false;
	}
	/** 必须 isGapWithOneSeqLast 判定为true时 本项目才有意义
	 * 与上一个site中间的gap长度。
	 * 如果连续，则为0
	 */
	public int getGapLengthWithLastSeq() {
		return getRefSnpIndelStart() - oneSeqInfoLast.getRefSnpIndelStart() - 1;
	}
	/**
	 * 是连续碱基，并且为相同的CG或AT，并且不为N
	 * @param oneSeqInfoUp  上一个oneSeqInfo
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
	/** 获得连续AT或者GC的覆盖度平均数 */
	public double getSameSiteNumAvg() {
		return readsNumCumulation/sameTypeNum;
	}
	/** 连续AT或者GC的数量 */
	public int getSameSiteNum() {
		return sameTypeNum;
	}
	
	/**
	 * 判断是不是insert
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
	/** 获得该位点的类型，譬如是CG，还是AT，还是N */
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