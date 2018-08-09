package com.novelbio.listoperate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.Alignment;

/**
 * peak定位查找信息的基本类,并且直接可以用于CG与Peak<br>
 * 子类有GffCodInfoGene 
 * @author zong0jie
 */
public class BsearchSite<T extends Alignment> {
	private static final Logger logger = LoggerFactory.getLogger(BsearchSite.class);
	
	/** 所有坐标的起始信息  */
	public static final int LOC_ORIGINAL = -1000000000;
	protected int coord = -1;
	/**  坐标是否查到 查找到/没找到  */
	protected boolean booFindCod = false;
	
	/** 上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1*/
	protected int indexAlignUp = -1;
	/** 为本条目在ChrHash-list中的编号，从0开始<br> 如果本条目不存在，则为-1 */
	protected int indexAlignThis = -1;
	/** 为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1 */
	protected int indexAlignDown = -1;
	
	/**
	 * 为上个条目的具体信息，如果没有则为null(譬如定位在最前端)<br>
	 * 1: 如果在条目内，为下个条目的具体信息<br>
	 * 如果在条目间，为下个条目的具体信息，如果没有则为null(譬如定位在最前端)
	 */
	protected T alignUp = null;
	/** 如果在条目内，为本条目的具体信息，没有定位在基因内则为null */
	protected T alignThis = null;
	/** 只有geneDetail用到， 为下个条目的具体信息，如果没有则为null(譬如定位在最后端) */
	protected T alignDown = null;
	
	/**  构造函数赋初值 */
	public  BsearchSite(int Coordinate) {
		this.coord = Coordinate;
	}

	/**
	 * 返回具体坐标
	 * @return
	 */
	public int getCoord() {
		return coord;
	}
	/**
	 * 是否成功找到cod
	 * @return
	 */
	public boolean findCod() {
		return booFindCod;
	}
	/**
	 * 定位情况 条目内/条目外
	 */
	protected boolean insideLOC = false;
	/**
	 * 定位情况 条目内/条目外，不考虑Tss上游和geneEnd下游之类的信息
	 */
	public boolean isInsideLoc() {
		return insideLOC;
	}
	
	/**
	 * 是否在基因内，不拓展
	 * @return
	 */
	public static boolean isCodInSide(Alignment align, int coord) {
		return coord >= align.getStartAbs() && coord <= align.getEndAbs();
	}
	
	/**
	 * 
	 * 坐标是否在基因的内部，包括Tss和GeneEnd的拓展区域
	 * @param tss
	 * @param geneEnd
	 * @param coord
	 * @return
	 */
	public static boolean isCodInGeneExtend(Alignment align, int[] tss, int geneEnd[], int coord) {
		return isCodInSide(align, coord) || isCodInPromoter(align, tss, coord) || isCodInGenEnd(align, geneEnd, coord);
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
	public static Integer getCod2Start(Alignment alignment, int coord) {
		if (alignment.isCis5to3() == null) {
			logger.error("cannot get strand info!");
			return coord - alignment.getStartAbs();
		}
		return alignment.isCis5to3() ? coord - alignment.getStartAbs() : alignment.getEndAbs() - coord;
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
	public static Integer getCod2End(Alignment alignment, int coord) {
		if (alignment.isCis5to3() == null) {
			logger.error("cannot get strand info!");
			return coord - alignment.getEndAbs();
		}
		return alignment.isCis5to3() ? coord - alignment.getEndAbs() : alignment.getStartAbs() - coord;
	}
	/**
	 * 是否在所谓的Tss内,既可以在内也可以在
	 * 所以如果需要只在基因外的tss，需要同时加上isCodInside==false判断
	 * @return
	 */
	public static boolean isCodInPromoter(Alignment alignment, int[] tss, int coord) {
		Integer cod2start = getCod2Start(alignment, coord);
		if (cod2start == null) {
			return false;
		}
		return cod2start >= tss[0] && cod2start <= tss[1];
	}
	
	/**
	 * 是否在所谓的GeneEnd内,既可以在内也可以在外
	 * 所以如果需要只在基因外的geneEnd，需要同时加上isCodInside==false判断
	 * 也就是尾部点，左右扩展geneEnd3UTR长度的bp
	 * @return
	 */
	private static boolean isCodInGenEnd(Alignment alignment, int[] geneEnd, int coord) {
		Integer cod2End = getCod2End(alignment, coord);
		if (cod2End == null) {
			return false;
		}
		return cod2End >= geneEnd[0] && cod2End <= geneEnd[1];
	}
	
	/** 是否在上一个条目内 */
	public boolean isInsideUp() {
		if (alignUp == null) {
			return false;
		}
		return isCodInSide(alignUp, coord);
	}
	/** 是否在下一个条目内 */
	public boolean isInsideDown() {
		if (alignDown == null) {
			return false;
		}
		return isCodInSide(alignDown, coord);
	}
	/**
	 * 
	 * 是否在上一个条目内
	 * 扩展tss和tes，有正负号
	 * @param upTss 负数为上游
	 * @param downTes 正数为下游
	 * @return
	 */
	public boolean isInsideUpExtend(int upTss, int downTes) {
		if (alignUp == null) {
			return false;
		}
		return isCodInGeneExtend(alignUp, new int[]{upTss, 0}, new int[]{0, downTes}, coord);
	}
	/**
	 * 是否在下一个条目内
	 * 扩展tss和tes，无视正负号
	 * @return
	 */
	public boolean isInsideDownExtend(int upTss, int downTes) {
		if (alignDown == null) {
			return false;
		}
		return isCodInGeneExtend(alignDown, new int[]{upTss, 0}, new int[]{0, downTes}, coord);
	}

	/**
	 * 只有geneDetail用到
	 * 获得上个条目的具体信息
	 * @return
	 */
	public T getAlignUp() {
		return alignUp;
	}
	public void setAlignUp(T alignUp) {
		this.alignUp = alignUp;
	}

	/**
	 * 只有geneDetail用到
	 * 获得本条目的具体信息，
	 * 如果本条目为null，说明不在条目内
	 * @return
	 */
	public T getAlignThis() {
		return alignThis;
	}
	public void setAlignThis(T alignThis) {
		this.alignThis = alignThis;
	}

	/**
	 * 只有geneDetail用到
	 * 获得下一个条目的具体信息
	 * @return
	 */
	public T getAlignDown() {
		return alignDown;
	}
	public void setAlignDown(T alignDown) {
		this.alignDown = alignDown;
	}

	/** 上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1*/
	public void setChrHashListNumUp(int chrHashListNumUp) {
		indexAlignUp = chrHashListNumUp;
	}
	/**
	 * 上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 */
	public int getItemNumUp() {
		return indexAlignUp;
	}
	/**
	 * 为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果本条目不存在，则为-1<br>
	 */
	public void setChrHashListNumThis(int chrHashListNumThis) {
		indexAlignThis = chrHashListNumThis;
	}
	/**
	 * 为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果本条目不存在，则为-1<br>
	 */
	public int getItemNumThis() {
		return indexAlignThis;
	}
	/** 为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1 */
	public void setChrHashListNumDown(int chrHashListNumDown) {
		indexAlignDown = chrHashListNumDown;
	}
	/**
	 * 为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	public int getItemNumDown() {
		return indexAlignDown;
	}
}
