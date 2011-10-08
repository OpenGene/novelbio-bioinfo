package com.novelbio.analysis.seq.genomeNew.gffOperate;

public class GffDetailAbsCod <T extends GffDetailAbs> {
	T gffDetail = null;
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ������������ľ��룬����������
	 */
	protected Integer cod2Start = null;
	/**
	 * ����������յ�ľ��룬����������
	 */
	protected Integer cod2End = null;

	public T getGffDetail() {
		return gffDetail;
	}

	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 */
	public void setCoord(int coord) {
		this.coord = coord;
	}
	/**
	 * Ⱦɫ�����꣬�����õ��뱾GffDetailAbs�����յ�ľ���
	 * @return
	 */
	public int getCoord() {
		return this.coord;
	}
	
	/**
	 * �����Ƿ��ڻ�����ڲ�������Tss��GeneEnd����չ����
	 */
	public boolean isCodInGenExtend() {
		return isCodInGene() || isCodInPromoter() || isCodInGenEnd();
	}
	
	/**
	 * �Ƿ�����ν��Tss��
	 * @return
	 */
	public boolean isCodInPromoter() {
		if (getCod2Start() == null) {
			return false;
		}
		if (getCod2Start() < 0 && Math.abs(getCod2Start()) <= T.UpStreamTSSbp) {
			return true;
		}
		else if (getCod2Start() >= 0 && Math.abs(getCod2Start()) <= T.DownStreamTssbp) {
			return true;
		}
		return false;
	}
	
	/**
	 * �Ƿ�����ν��GeneEnd��
	 * @return
	 */
	public boolean isCodInGenEnd() {
		if (getCod2End() == null) {
			return false;
		}
		if (getCod2End() > 0 && Math.abs(getCod2End()) <= T.GeneEnd3UTR) {
			return true;
		}
		return false;
	}
	
	/**
	 * �Ƿ��ڻ����ڣ�����չ
	 * @return
	 */
	public boolean isCodInGene() {
		if (coord >= gffDetail.numberstart && coord <= gffDetail.numberend) {
			return true;
		}
		return false;
	}
	
	/**
	 * ������굽��ItemEnd�ľ���,���coordС��0˵�������⣬�򷵻�null
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������end��5������Ϊ����
	 * ���������end��3������Ϊ����
	 * @return
	 */
	public Integer getCod2End() {
		if (cod2End != null) {
			return cod2End;
		}
		if (coord < 0) {
			return null;
		}
		if (gffDetail.isCis5to3()) {
			cod2End =  coord - gffDetail.numberend;
		}
		else {
			cod2End = gffDetail.numberstart- coord;
		}
		return cod2End;
	}
	
	/**
	 * ������굽��ItemStart�ľ���,���coordС��0˵�������⣬�򷵻�null
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������start��5������Ϊ����
	 * ���������start��3������Ϊ����
	 * @return
	 */
	public Integer getCod2Start() {
		if (cod2Start != null) {
			return cod2Start;
		}
		if (coord < 0) {
			return null;
		}
		if (gffDetail.cis5to3) {
			cod2Start =  coord - gffDetail.numberstart;
		}
		else {
			cod2Start = gffDetail.numberend - coord;
		}
		return cod2Start;
	}

	/**
	 * �����Ƿ��ڻ�����
	 * @return
	 */
	public boolean getCodInSide() {
		if (coord < 0) {
			return false;
		}
		if (coord >= gffDetail.numberstart && coord <= gffDetail.numberend) {
			return true;
		}
		else {
			return false;
		}
	}
}
