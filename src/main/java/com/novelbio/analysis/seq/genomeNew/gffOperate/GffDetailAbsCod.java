package com.novelbio.analysis.seq.genomeNew.gffOperate;

public class GffDetailAbsCod <T extends GffDetailAbs> {
	T gffDetail = null;
	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 坐标与基因起点的距离，考虑正反向
	 */
	protected Integer cod2Start = null;
	/**
	 * 坐标与基因终点的距离，考虑正反向
	 */
	protected Integer cod2End = null;

	public T getGffDetail() {
		return gffDetail;
	}

	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 */
	public void setCoord(int coord) {
		this.coord = coord;
	}
	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 * @return
	 */
	public int getCoord() {
		return this.coord;
	}
	
	/**
	 * 坐标是否在基因的内部，包括Tss和GeneEnd的拓展区域
	 */
	public boolean isCodInGenExtend() {
		return isCodInGene() || isCodInPromoter() || isCodInGenEnd();
	}
	
	/**
	 * 是否在所谓的Tss内
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
	 * 是否在所谓的GeneEnd内
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
	 * 是否在基因内，不拓展
	 * @return
	 */
	public boolean isCodInGene() {
		if (coord >= gffDetail.numberstart && coord <= gffDetail.numberend) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获得坐标到该ItemEnd的距离,如果coord小于0说明有问题，则返回null
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在end的5方向，则为负数
	 * 如果坐标在end的3方向，则为正数
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
	 * 获得坐标到该ItemStart的距离,如果coord小于0说明有问题，则返回null
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在start的5方向，则为负数
	 * 如果坐标在start的3方向，则为正数
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
	 * 坐标是否在基因内
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
