package com.novelbio.analysis.seq.genomeNew.gffOperate;

/**
 * 本类重写了equal代码，用于比较两个loc是否一致
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
	 * 该条目在List-GffDetail中的具体位置
	 */
	protected int itemNum = -10;
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
	protected String locString = ""; //loc name
	
	
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	protected int numberstart = -1000000000; // loc start number 
	
	/**
	 * @GffHashGene
	 * 本基因终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	protected int numberend = -1000000000; //loc end number
	
	/**
	 * 染色体编号，都小写
	 */
	protected String ChrID="";
	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 */
	protected boolean cis5to3 = true; 
	/**
	 * 
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
		this.ChrID = chrID;
		this.locString = locString;
		this.cis5to3 = cis5to3;
	}
	///////////////////////////////////////////////  与 coord 有关的属性和方法  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 */
	protected int coord = -100;
	/**
	 * 坐标与基因起点的距离，考虑正反向
	 */
	protected Integer cod2Start = null;
	/**
	 * 坐标与基因终点的距离，考虑正反向
	 */
	protected Integer cod2End = null;

	/**
	 * 染色体坐标，会计算该点与本GffDetailAbs起点和终点的距离
	 */
	protected void setCoord(int coord) {
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
	public boolean getCis5to3() {
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
		cis5to3 == otherObj.cis5to3;
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
		
}
