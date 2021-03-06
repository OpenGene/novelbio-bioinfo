package com.novelbio.bioinfo.base.binarysearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import com.novelbio.base.SepSign;
import com.novelbio.bioinfo.base.Alignment;

/**
 * compare的比较取决于父节点的方向，如果父节点的方向为null，则按照绝对值排序，如果是cis，那么就按cis的排序，如果为trans就按照trans的方式排序
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode 仅比较ChrID + "//" + numberstart + "//" + numberstart;
 * 存储Gff文件中每个条目的具体信息，直接用于GffPeak文件
 * 包括<br>
 * 条目名 locString<br>
 * 条目起点 numberstart<br>
 * 条目终点 numberend<br>
 * 条目所在染色体编号 ChrID<br>
 * 条目方向 cis5to3
 * @author zong0jie
 */
@Deprecated
public class ListDetailAbs implements Alignment, Cloneable {
	private static final Logger logger = LoggerFactory.getLogger(ListDetailAbs.class);

	/** 父树 */
	@Transient
	protected ListAbs<? extends ListDetailAbs> listAbs;
	/**
	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 */
	protected Set<String> setItemName = new LinkedHashSet<String>(); //loc name
	/**  染色体编号，都小写 */
	@Indexed(unique = false)
	protected String parentName;
	/** 转录方向，假设同一基因不管多少转录本都同一转录方向 */
	protected Boolean cis5to3 = null;
	
	@Indexed(unique = false)
	/** 本条目起点,起点位置总是小于终点，无视基因方向 */
	protected int numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number
	@Indexed(unique = false)
	/** 本条目终点，终点位置总是大于起点，无视基因方向 */
	protected int numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
	/** 本基因起点到上一个基因边界的距离 */
	@Transient
	protected int tss2UpGene = ListCodAbs.LOC_ORIGINAL;
	/** 本基因终点到下一个基因边界的距离 */
	@Transient
	protected int tes2DownGene = ListCodAbs.LOC_ORIGINAL;
	
	public ListDetailAbs() {}
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
	public ListDetailAbs(String chrID, String ItemName, Boolean cis5to3) {
		if (chrID != null && !chrID.equals("")) {
			this.parentName = chrID;
		}
		if (ItemName != null && !ItemName.equals("")) {
			this.setItemName.add(ItemName);
		}
		this.cis5to3 = cis5to3;
	}
	/**
	 * 没有就设定为""或null
	 * @param listAbs 父节点的信息
	 * @param ItemName 本节点的名字
	 * @param cis5to3 正反向 不确定就输入null
	 */
	public ListDetailAbs(ListAbs<? extends ListDetailAbs> listAbs, String ItemName, Boolean cis5to3) {
		this.listAbs = listAbs;
		this.parentName = listAbs.getName();
		if (ItemName != null) {
			this.setItemName.add(ItemName);
		}
		this.cis5to3 = cis5to3;
	}
	/**
	 * 设定parentList和ParentName
	 * @param listAbs
	 */
	public void setParentListAbs(ListAbs<? extends ListDetailAbs> listAbs) {
		this.listAbs = listAbs;
		if (this.parentName == null) {
			this.parentName = listAbs.getName();
		}
	}
	public ListAbs<? extends ListDetailAbs> getParent() {
		return listAbs;
	}

	/** 
	 * <b>从0开始计算</b>
	 * 该条目在List-GffDetail中的具体位置 */
	public int getItemNum() {
		return getParent().indexOf(this);
	}
    /**
     * Item的名字，返回第一个
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public String getNameSingle() {
		if (setItemName.size() == 0) {
			return "";
		}
		return this.setItemName.iterator().next();
	}
	/** 全体item的名字 */
	public ArrayList<String > getName() {
		return new ArrayList<String>(setItemName);
	}
    /**
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public void addItemName(String itemName) {
		this.setItemName.add(itemName);
	}
	/**
	 * 染色体编号等信息，父ID
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	/** 本基因起点到上一个基因边界的距离  */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/** 本基因起点到上一个基因边界的距离 */
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
	 * @param numberend 条目终点,根据基因方向确定,从1开始记数
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
	 * @param numberstart 条目起点,根据基因方向确定,从1开始记数
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
	 * 
	 * 坐标是否在基因的内部，包括Tss和GeneEnd的拓展区域
	 * @param tss
	 * @param geneEnd
	 * @param coord
	 * @return
	 */
	public boolean isCodInGeneExtend(int[] tss, int geneEnd[], int coord) {
		return isCodInSide(coord) || isCodInPromoter(tss, coord) || isCodInGenEnd(geneEnd, coord);
	}
	
	/**
	 * 是否在所谓的Tss内,既可以在内也可以在
	 * 所以如果需要只在基因外的tss，需要同时加上isCodInside==false判断
	 * @return
	 */
	public boolean isCodInPromoter(int[] tss, int coord) {
		if (getCod2Start(coord) == null) {
			return false;
		}
		int cod2start = getCod2Start(coord);
		if (cod2start >= tss[0] && cod2start <= tss[1]) {
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
	public boolean isCodInGenEnd(int[] geneEnd, int coord) {
		if (getCod2End(coord) == null) {
			return false;
		}
		int cod2end = getCod2End(coord);
		if (cod2end >= geneEnd[0] && cod2end <= geneEnd[1] ) {
			return true;
		}
		return false;
	}
	/**
	 * 是否在基因内，不拓展
	 * @return
	 */
	public boolean isCodInSide(int coord) {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
	}
	/**
	 * 所属listAbs编号，都小写
	 */
	public String getChrId() {
		return this.parentName;
	}

	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 * 一个转录本里面既有正向也有反向，选择方向最多的那个
	 */
	public Boolean isCis5to3() {
		if (cis5to3 != null) return cis5to3;
		if (listAbs != null && listAbs.isCis5to3() != null) {
			return listAbs.isCis5to3();
		}
		return cis5to3;
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
			return coord - getStartAbs();
		}
		if (cis5to3) {
			return coord -numberstart;
		}
		else {
			return numberend - coord;
		}
	}
	
/////////////////////////////  重写equals等  ////////////////////////////////////
	/**
	 * 只比较numberstart、numberend、ChrID、cis5to3
	 * 不比较coord
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		ListDetailAbs otherObj = (ListDetailAbs)obj;
		
		return
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		parentName.equalsIgnoreCase(otherObj.parentName) &&
//		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	}
	/** 重写hashcode */
	public int hashCode(){
		String hash = "";
		hash = parentName + SepSign.SEP_ID + numberstart + SepSign.SEP_ID + numberstart;
		return hash.hashCode();
	}
	/** 没有方向则返回startAbs */
	public int getStartCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberstart;
		}
		return numberend;
	}
	/** 没有方向则返回endAbs */
	public int getEndCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberend;
		}
		return numberstart;
	}
	
	public int getLength() {
		return Math.abs(numberend-numberstart) + 1;
	}
	
	public ListDetailAbs clone() {
		ListDetailAbs result = null;
		try {
			result = (ListDetailAbs) super.clone();
			result.cis5to3 = cis5to3;
			result.setItemName = new LinkedHashSet<String>(setItemName);
//			result.itemNum = itemNum;
			result.numberend = numberend;
			result.numberstart = numberstart;
			result.parentName = parentName;
			result.tes2DownGene = tes2DownGene;
			result.tss2UpGene = tss2UpGene;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/** 有方向的排序 */
	public static class ListDetailAbsCompareStrand implements Comparator<Alignment> {

		@Override
        public int compare(Alignment o1, Alignment o2) {
			Integer o1startCis = o1.getStartCis(); Integer o1endCis = o1.getEndCis();
			Integer o2startCis = o2.getStartCis(); Integer o2endCis = o2.getEndCis();
			
			if (o1.isCis5to3() == null || o1.isCis5to3()) {
				int result = o1startCis.compareTo(o2startCis);
				if (result == 0) {
					return o1endCis.compareTo(o2endCis);
				}
				return result;
			} else {
				int result = - o1startCis.compareTo(o2startCis);
				if (result == 0) {
					return - o1endCis.compareTo(o2endCis);
				}
				return result;
			}
        }
		
	}
	/** 没有方向的排序 */
	public static class ListDetailAbsCompareNoStrand implements Comparator<Alignment> {
		@Override
        public int compare(Alignment o1, Alignment o2) {
			Integer o1startAbs = o1.getStartAbs(); Integer o1endAbs = o1.getEndAbs();
			Integer o2startAbs = o2.getStartAbs(); Integer o2endAbs = o2.getEndAbs();
			int result = o1startAbs.compareTo(o2startAbs);
			if (result == 0) {
				return o1endAbs.compareTo(o2endAbs);
			}
			return result;
 
        }
	}
	
}
