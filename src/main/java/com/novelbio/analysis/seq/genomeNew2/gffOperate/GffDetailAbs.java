package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import javax.servlet.jsp.tagext.TryCatchFinally;

import com.novelbio.analysis.annotation.copeID.CopedID;

/**
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode
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
	protected String locString = ""; //loc name
	
	
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
		this.ChrID = chrID;
		this.locString = locString;
		this.cis5to3 = cis5to3;
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
	
	public abstract GffDetailAbsCod setCood(int coord);
	///////////////////////////////////////////////  与 coord 有关的属性和方法  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
/////////////////////////////  重写equals等  ////////////////////////////////////

	

	/**
	 * 只比较locString、numberstart、numberend、ChrID、cis5to3
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
	
	
}
