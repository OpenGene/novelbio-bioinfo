package com.novelbio.software.coordtransform;

import com.novelbio.bioinfo.base.Alignment;

/** 对于ref来说的indel
 * insertion
 * start是插入的具体起点(t)，end是插入的具体终点(c)，都从1开始计算
 * 1    ca tgc gcat
 * 1    ca- - - gcat
 * 
 * deletion
 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算
 * 1    ca- - - gcat
 * 1    ca tgc gcat
 * @author zong0jie
 * @data 2018年8月6日
 */
public class IndelForRef implements Alignment {
	
	/** refStart一定是从小到大 */
	int refStart;
	
	boolean isAltCis;
	int altStartCis;
	
	/** 表示alt的长度 */
	int altLen = 0;
	/** 表示ref的长度 */
	int refLen = 0;
	
	public IndelForRef(boolean isAltCis) {
		this.isAltCis = isAltCis;
	}
	/**
	 * 如果ref为插入，则为插入前的位点<br>
	 * ATA[C] ACATCGGCA T<br>
	 * 如果ref为缺失，则为缺失前的位点<br>
	 * ATA[C] -------CGGCA T
	 * @param start
	 */
	public void setRefStart(int start) {
		this.refStart = start;
	}
	/**
	 * 如果alt为插入，则为插入前的位点<br>
	 * ATA[C] ACATCGGCA T<br>
	 * 如果alt为缺失，则为缺失前的位点<br>
	 * ATA[C] -------CGGCA T
	 * @param start
	 */
	public void setAltStartCis(int altStartCis) {
		this.altStartCis = altStartCis;
	}

	/** 跟 {@link #getStartAbs()} 一样<br>
	 *  * insertion<br>
	 * start是插入前的位点，end是插入的后一个位点，都从1开始计算<br>
	 * 1    c[a] tgc [g]cat : alt<br>
	 * 1      ca  - - - gcat : ref<br>
	 * <br>
	 * deletion<br>
	 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 1    ca   tgc  gcat : ref
	 * @return
	 */
	public int getStartCisAlt() {
		return altStartCis;
	}
	/** 跟 {@link #getEndAbs()} 一样<br>
	 *  * insertion<br>
	 * start是插入前的位点，end是插入的后一个位点，都从1开始计算<br>
	 * 1    c[a] tgc [g]cat : alt<br>
	 * 1      ca  - - - gcat : ref<br>
	 * <br>
	 * deletion<br>
	 * start是缺失的前一个位点(a)，end是缺失的后一个位点(g)，都从1开始计算<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 1    ca   tgc  gcat : ref
	 * @return
	 */
	public int getEndCisAlt() {
		return isAltCis ? altStartCis + altLen+1 : altStartCis - altLen-1;
	}
	public boolean isAltCis() {
		return isAltCis;
	}
	/** end向后延长n为多少，注意不修改值<br>
	 * 1    c[a]- - - [g]cat : alt<br>
	 * 延长3bp变成<br>
	 * 1    c[a]- - - gca[t] : alt
	 * @param num
	 * @return
	 */
	public int getEndExtendAlt(int num) {
		return isAltCis() ? getEndCisAlt() + num : getEndCisAlt()-num;
	}
	/** start向前延长n为多少，注意不修改值
	 * 1    atgc[a]- - - [g]cat : alt
	 * 延长3bp变成
	 * 1    a[t]gca- - - gca[t] : alt
	 * @param num
	 * @return
	 */
	public int getStartExtendAlt(int num) {
		return isAltCis() ? getStartCisAlt() - num : getStartCisAlt()+num;
	}
	/** end向后延长n为多少，注意不修改值<br>
	 * 1    c[a]- - - [g]cat : ref<br>
	 * 延长3bp变成<br>
	 * 1    c[a]- - - gca[t] : ref
	 * @param num
	 * @return
	 */
	public int getEndExtend(int num) {
		return getEndAbs() + num;
	}
	/** start向前延长n为多少，注意不修改值<br>
	 * 1    atgc[a]- - - [g]cat : ref<br>
	 * 延长3bp变成<br>
	 * 1    a[t]gca- - - gca[t] : ref
	 * @param num
	 * @return
	 */
	public int getStartExtend(int num) {
		return getStartAbs() - num;
	}
	public void setRefLen(int refLen) {
		this.refLen = refLen;
	}
	public void setAltLen(int altLen) {
		this.altLen = altLen;
	}
	public void addAltLen1() {
		altLen++;
	}
	public int getAltLen() {
		return altLen;
	}
	
	public void addRefLen1() {
		refLen++;
	}
	public int getRefLen() {
		return refLen;
	}
	
	@Deprecated
	public int getLength() {
		return refLen;
	}
	/**
	 * 注意 {@link #isRefInsertion()} 和 {@link #isAltInsertion()}
	 * 可以同时为true，表示这个就是一个替换
	 * true: ref相对于alt为插入<br>
	 * false: ref相对于alt没插入<br>
	 * @return
	 */
	public boolean isRefInsertion() {
		return refLen > 0;
	}
	
	/**
	 * true: alt相对于ref为插入<br>
	 * false: alt相对于ref为缺失<br>
	 * @return
	 */
	public boolean isAltInsertion() {
		return altLen > 0;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		if (!super.equals(obj)) {
			return false;
		}
		IndelForRef otherAlign = (IndelForRef)obj;
		return altStartCis == otherAlign.altStartCis && altLen == otherAlign.altLen
				&& isAltCis == otherAlign.isAltCis && refLen == otherAlign.refLen;
	}

	/**
	 * 如果ref为插入，则为插入前的位点
	 * ATA[C] ACATCGGCA T
	 * 如果ref为缺失，则为缺失前的位点
	 * ATA[C] -------CGGCA T
	 */
	@Override
	public int getStartAbs() {
		return refStart;
	}
	/**
	 * 如果ref为插入，则为插入后的位点
	 * ATAC ACAT  [C]GGCA
	 * 如果ref为缺失，则为缺失后的位点
	 * ATAC -------[C]GGCA
	 */
	@Override
	public int getEndAbs() {
		return refStart+refLen+1;
	}

	@Override
	public int getStartCis() {
		return getStartAbs();
	}

	@Override
	public int getEndCis() {
		return getEndAbs();
	}

	@Override
	public Boolean isCis5to3() {
		return true;
	}

	@Override
	public String getChrId() {
		return null;
	}
	
}

