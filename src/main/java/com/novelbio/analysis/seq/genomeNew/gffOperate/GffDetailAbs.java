package com.novelbio.analysis.seq.genomeNew.gffOperate;

import javax.servlet.jsp.tagext.TryCatchFinally;

import com.novelbio.analysis.annotation.copeID.CopedID;

/**
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode 仅比较ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
 * 存储Gff文件中每个条目的具体信息，直接用于GffPeak文件
 * 包括<br>
 * 条目名 locString<br>
 * 条目起点 numberstart<br>
 * 条目终点 numberend<br>
 * 条目所在染色体编号 ChrID<br>
 * 条目方向 cis5to3
 * @author zong0jie
 *
 */
public abstract class GffDetailAbs {
	
	
	
	/**
	 * 设定基因的转录起点上游长度，默认为3000bp
	 */
	protected static int UpStreamTSSbp = 3000;
	
	/**
	 * 设定基因的转录起点下游长度，默认为2000bp
	 */
	protected static int DownStreamTssbp=2000;
	/**
	 * 设定基因结尾向外延伸的长度，默认为100bp
	 * 就是说将基因结束点向后延伸100bp，认为是3’UTR
	 * 那么在统计peak区域的时候，如果这段区域里面没有被peak所覆盖，则不统计该区域内reads的情况
	 */
	protected static int GeneEnd3UTR=100;
	/**
	 * 设定基因的转录起点终点位置信息
	 * @param UpStreamTSSbp 设定基因的转录起点上游长度，默认为3000bp
	 * @param DownStreamTssbp 设定基因的转录起点下游长度，默认为2000bp
	 * @param GeneEnd3UTR 设定基因结尾向外延伸的长度，默认为100bp
	 */
	protected static void setCodLocation(int upStreamTSSbp, int downStreamTssbp, int geneEnd3UTR) {
		UpStreamTSSbp = upStreamTSSbp;
		DownStreamTssbp = downStreamTssbp;
		GeneEnd3UTR = geneEnd3UTR;
	}
	
	
	/**
	 * 该条目在List-GffDetail中的具体位置
	 */
	protected int itemNum = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 该条目在List-GffDetail中的具体位置
	 */
	public int getItemNum() {
		return this.itemNum;
	}
	
	/**
	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 */
	private String locString = ""; //loc name
	public void setLocString(String locString) {
		this.locString = locString;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	protected int numberstart = GffCodAbs.LOC_ORIGINAL; // loc start number 
	
	/**
	 * @GffHashGene
	 * 本基因终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	protected int numberend = GffCodAbs.LOC_ORIGINAL; //loc end number
	/**
	 * 本基因起点到上一个基因边界的距离
	 */
	protected int tss2UpGene = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 本基因终点到下一个基因边界的距离
	 */
	protected int tes2DownGene = GffCodAbs.LOC_ORIGINAL;
	/**
	 * 染色体编号，都小写
	 */
	protected String ChrID="";
	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 */
	protected boolean cis5to3 = true; 
	/**
	 * 没有就设定为""或null
	 * @param chrID 染色体编号，都小写
	 * @param locString 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 * @param cis5to3
	 */
	public GffDetailAbs(String chrID, String locString,boolean cis5to3)
	{
		this.ChrID = chrID.toLowerCase();
		this.locString = locString;
		this.cis5to3 = cis5to3;
	}
	///////////////////////////////////////////////  与 coord 有关的属性和方法  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	/**
	 * 本基因终点到下一个基因边界的距离
	 */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/**
	 * 本基因起点到上一个基因边界的距离
	 */
	public int getTss2UpGene() {
		return tss2UpGene;
	}
	/**
	 * @GffHashGene
	 * 本基因终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	public int getNumberend() {
		return numberend;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public int getNumberstart() {
		return numberstart;
	}
	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 */
	public void setCoord(int coord) {
		this.coord = coord;
		cod2End = null;
		cod2Start = null;
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
		if (getCod2Start() < 0 && Math.abs(getCod2Start()) <= UpStreamTSSbp) {
			return true;
		}
		else if (getCod2Start() >= 0 && Math.abs(getCod2Start()) <= DownStreamTssbp) {
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
		if (getCod2End() > 0 && Math.abs(getCod2End()) <= GeneEnd3UTR) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否在基因内，不拓展
	 * @return
	 */
	public boolean isCodInGene() {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
	}
	/**
	 * 染色体编号，都小写
	 */
	public String getChrID() {
		return this.ChrID;
	}
    /**
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public String getLocString()
	{
		return this.locString;
	}
	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 */
	public boolean isCis5to3() {
		return this.cis5to3;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public int getNumEnd()
	{
		return this.numberend;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public int getNumStart()
	{
		return this.numberstart;
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
		if (cis5to3) {
			cod2End =  coord -numberend;
		}
		else {
			cod2End = numberstart- coord;
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
		if (cis5to3) {
			cod2Start =  coord -numberstart;
		}
		else {
			cod2Start = numberend - coord;
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
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		else {
			return false;
		}
	}
	
/////////////////////////////  重写equals等  ////////////////////////////////////

	

	/**
	 * 只比较locString、numberstart、numberend、ChrID、cis5to3
	 * 不比较coord
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		GffDetailAbs otherObj = (GffDetailAbs)obj;
		
		return locString.equals(otherObj.locString) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		ChrID.equals(otherObj.ChrID) &&
		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	} 

	
	/**
	 * 重写hashcode
	 */
	public int hashCode(){
		String hash = "";
		hash = ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
		return hash.hashCode();
	}
	
	public abstract GffDetailAbs clone();
	/**
	 * 将本类的信息全部复制到gffDetailAbs上去
	 * locString，ChrID，cis5to3不复制
	 * cod2Start,cod2End不复制
	 * @param gffDetailAbs1
	 * @param gffDetailAbs2
	 */
	protected void clone(GffDetailAbs gffDetailAbs)
	{
//		gffDetailAbs1.ChrID = gffDetailAbs2.ChrID;
//		gffDetailAbs1.cis5to3 = gffDetailAbs2.cis5to3
//		gffDetailAbs1.locString = gffDetailAbs2.locString;
		gffDetailAbs.coord = coord;
		gffDetailAbs.itemNum = itemNum;
		gffDetailAbs.numberstart = numberstart;
		gffDetailAbs.numberend = numberend;
		gffDetailAbs.tes2DownGene = tes2DownGene;
		gffDetailAbs.tss2UpGene = tss2UpGene;
	}
}
