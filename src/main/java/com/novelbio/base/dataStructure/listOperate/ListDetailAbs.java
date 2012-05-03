package com.novelbio.base.dataStructure.listOperate;

import org.apache.log4j.Logger;

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
public class ListDetailAbs implements Cloneable{
	/**
	 * 根据cis在起点的上游多少bp，在此范围内则认为在tss区域
	 */
	protected int upTss = 0;
	/**
	 * 根据cis在起点的下游多少bp，在此范围内则认为在tss区域
	 */
	protected int downTss = 0;
	/**
	 * 根据cis在终点的上游多少bp，在此范围内则认为在tes区域
	 */
	protected int upGeneEnd3UTR = 0;
	/**
	 * 根据cis在终点的下游多少bp，在此范围内则认为在tes区域
	 */
	protected int downGeneEnd3UTR = 0;
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int upTss, int downTss) {
		this.upTss = upTss;
		this.downTss = downTss;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int[] Tss) {
		if (Tss != null) {
			this.upTss = Tss[0];
			this.downTss = Tss[1];
		}
	}
	
	/**
	 * 划定Tes范围上游为负数，下游为正数
	 * @param upTes
	 * @param downTes
	 */
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTesRegion(int[] Tes) {
		if (Tes != null) {
			this.upGeneEnd3UTR = Tes[0];
			this.downGeneEnd3UTR = Tes[1];
		}
	}
	/**
	 * 0：uptss
	 * 1：downtss
	 * @return
	 */
	public int[] getTssRegion() {
		return new int[]{upTss, downTss};
	}
	/**
	 * 0：uptes
	 * 1：downtes
	 * @return
	 */
	public int[] getTesRegion() {
		return new int[]{upGeneEnd3UTR, downGeneEnd3UTR};
	}
	/**
	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 */
	private String ItemName = ""; //loc name
	/**
	 * 染色体编号，都小写
	 */
	protected String parentName="";
	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 */
	protected Boolean cis5to3 = null; 
	/**
	 * 没有就设定为""或null
	 * @param chrID 染色体编号，自动变成小写
	 * @param locString 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 * @param cis5to3 不确定就输入null
	 */
	public ListDetailAbs(String chrID, String ItemName,Boolean cis5to3)
	{
		if (chrID != null) {
			this.parentName = chrID.toLowerCase();
		}
		this.ItemName = ItemName;
		this.cis5to3 = cis5to3;
	}

	public ListDetailAbs() {}
	
	private static Logger logger = Logger.getLogger(ListDetailAbs.class);
	
	int number = 0;
	/**
	 * 计数加一
	 */
	public void addNumber() {
		number++;
	}
	/**
	 * 本区域内出现多少的元素，必须前面调用addNumber添加
	 * @return
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * 该条目在List-GffDetail中的具体位置
	 */
	protected int itemNum = ListCodAbs.LOC_ORIGINAL;
	/**
	 * 该条目在List-GffDetail中的具体位置
	 */
	public int getItemNum() {
		return this.itemNum;
	}
    /**
     * Item的名字
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public String getName() {
		return this.ItemName;
	}
    /**
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public void setName(String locString) {
		this.ItemName = locString;
	}
	/**
	 * 染色体编号等信息，父ID
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	/**
	 * @GffHashGene
	 * 本条目起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	protected int numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number 
	
	/**
	 * @GffHashGene
	 * 本条目终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	protected int numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
	/**
	 * 本基因起点到上一个基因边界的距离
	 */
	protected int tss2UpGene = ListCodAbs.LOC_ORIGINAL;
	/**
	 * 本基因起点到上一个基因边界的距离
	 */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/**
	 * 本基因终点到下一个基因边界的距离
	 */
	protected int tes2DownGene = ListCodAbs.LOC_ORIGINAL;
	/**
	 * 本基因终点到下一个基因边界的距离
	 */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
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
	/**
	 * @GffHashGene
	 * 本基因终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	public int getEndAbs() {
		return numberend;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public int getStartAbs() {
		return numberstart;
	}
	/**
	 * @param numberend 条目终点,终点位置总是大于起点，无视基因方向
	 */
	public void setEndAbs(int numberend) {
		this.numberend = numberend;
	}
	/**
	 * @param numberstart 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public void setStartAbs(int numberstart) {
		this.numberstart = numberstart;
	}
	/**
	 * @param numberend 条目终点,根据基因方向确定
	 */
	public void setEndCis(int numberend) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberend = numberend;
		}
		else {
			this.numberstart = numberend;
		}
	}
	/**
	 * @param numberstart 条目起点,根据基因方向确定
	 */
	public void setStartCis(int numberstart) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberstart = numberstart;
		}
		else {
			this.numberend = numberstart;
		}
	}
	/**
	 * 坐标是否在基因的内部，包括Tss和GeneEnd的拓展区域
	 */
	public boolean isCodInGeneExtend(int coord) {
		return isCodInGene(coord) || isCodInPromoter(coord) || isCodInGenEnd(coord);
	}
	
	/**
	 * 是否在所谓的Tss内,既可以在内也可以在
	 * 所以如果需要只在基因外的tss，需要同时加上isCodInside==false判断
	 * @return
	 */
	public boolean isCodInPromoter(int coord) {
		if (getCod2Start(coord) == null) {
			return false;
		}
		int cod2start = getCod2Start(coord);
		if (cod2start >= upTss && cod2start <= downTss) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否在所谓的GeneEnd内,既可以在内也可以在外
	 * 所以如果需要只在基因外的geneEnd，需要同时加上isCodInside==false判断
	 * 也就是尾部点，左右扩展geneEnd3UTR长度的bp
	 * @return
	 */
	public boolean isCodInGenEnd(int coord) {
		if (getCod2End(coord) == null) {
			return false;
		}
		int cod2end = getCod2End(coord);
		if (cod2end >= upGeneEnd3UTR && cod2end <= downGeneEnd3UTR ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否在基因内，不拓展
	 * @return
	 */
	public boolean isCodInGene(int coord) {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
	}
	
	/**
	 * 所属listAbs编号，都小写
	 */
	public String getParentName() {
		return this.parentName;
	}

	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 * 一个转录本里面既有正向也有反向，选择方向最多的那个
	 */
	public Boolean isCis5to3() {
		return this.cis5to3;
	}
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}

	/**
	 * 获得坐标到该ItemEnd的距离
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在end的5方向，则为负数
	 * 如果坐标在end的3方向，则为正数
	 * @return
	 */
	public Integer getCod2End(int coord) {
		if (cis5to3 == null) {
			logger.error("不能确定该Item的方向");
			return null;
		}
		if (cis5to3) {
			return coord -numberend;
		}
		else {
			return numberstart- coord;
		}
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
	public Integer getCod2Start(int coord) {
		if (cis5to3 == null) {
			logger.error("不能确定该Item的方向");
			return null;
		}
		if (cis5to3) {
			return coord -numberstart;
		}
		else {
			return numberend - coord;
		}
	}

	/**
	 * 坐标是否在基因内
	 * @return
	 */
	public boolean isCodInSide(int coord) {
		if (coord >= numberstart && coord <=  numberend) {
			return true;
		}
		return false;
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
		
		ListDetailAbs otherObj = (ListDetailAbs)obj;
		
		return ItemName.equals(otherObj.ItemName) && 
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		parentName.equals(otherObj.parentName) &&
		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	} 

	
	/**
	 * 重写hashcode
	 */
	public int hashCode(){
		String hash = "";
		hash = parentName + "//" + ItemName + "//" + numberstart + "//" + numberstart;
		return hash.hashCode();
	}
	
	/**
	 * 将本类的信息全部复制到gffDetailAbs上去
	 * locString，ChrID，cis5to3不复制
	 * cod2Start,cod2End不复制
	 * @param gffDetailAbs1
	 * @param gffDetailAbs2
	 */
	protected void clone(ListDetailAbs gffDetailAbs)
	{
		gffDetailAbs.parentName = parentName;
		gffDetailAbs.cis5to3 = cis5to3;
		gffDetailAbs.ItemName = ItemName;
		gffDetailAbs.itemNum = itemNum;
		gffDetailAbs.numberstart = numberstart;
		gffDetailAbs.numberend = numberend;
		gffDetailAbs.tes2DownGene = tes2DownGene;
		gffDetailAbs.tss2UpGene = tss2UpGene;
		gffDetailAbs.number = number;
	}
	public int getStartCis() {
		if (isCis5to3()) {
			return numberstart;
		}
		return numberend;
	}
	
	public int getEndCis() {
		if (isCis5to3()) {
			return numberend;
		}
		return numberstart;
	}
	
	public int getLen() {
		return Math.abs(numberend-numberstart) + 1;
	}
	
	public ListDetailAbs clone()
	{
		ListDetailAbs result = null;
		try {
			result = (ListDetailAbs) super.clone();
			result.cis5to3 = cis5to3;
			result.downGeneEnd3UTR = downGeneEnd3UTR;
			result.downTss = downTss;
			result.ItemName = ItemName;
			result.itemNum = itemNum;
			result.number = number;
			result.numberend = numberend;
			result.numberstart = numberstart;
			result.parentName = parentName;
			result.tes2DownGene = tes2DownGene;
			result.tss2UpGene = tss2UpGene;
			result.upGeneEnd3UTR = upGeneEnd3UTR;
			result.upTss = upTss;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}
}
