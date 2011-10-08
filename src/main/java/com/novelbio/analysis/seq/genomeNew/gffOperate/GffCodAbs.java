package com.novelbio.analysis.seq.genomeNew.gffOperate;


/**
 * peak定位查找信息的基本类,并且直接可以用于CG与Peak<br>
 * 子类有GffCodInfoGene 
 * @author zong0jie
 */
public abstract class GffCodAbs<T extends GffDetailAbs> {

	/**
	 * 所有坐标的起始信息
	 */
	public static final int LOC_ORIGINAL = -1000000000;
	/**
	 * 构造函数赋初值
	 */
	protected  GffCodAbs(String chrID, int Coordinate) {
		this.chrID = chrID;
		this.Coordinate = Coordinate;
	}
	
	
	
	
	
	String chrID = "";
	int Coordinate = -1;
	/**
	 * 返回染色体
	 * @return
	 */
	public String getChrID() {
		return chrID;
	}
	/**
	 * 返回具体坐标
	 * @return
	 */
	public int getCoord() {
		return Coordinate;
	}
	/**
	 * 坐标是否查到 查找到/没找到
	 */
	protected boolean booFindCod = false;
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
	 * 是否在上一个条目内
	 * @return
	 */
	public boolean isInsideUp() {
		if (gffDetailUp == null) {
			return false;
		}
		return gffDetailUp.isCodInGene();
	}
	/**
	 * 是否在下一个条目内
	 * @return
	 */
	public boolean isInsideDown() {
		if (gffDetailDown == null) {
			return false;
		}
		return gffDetailDown.isCodInGene();
	}
	/**
	 * 为上个条目的具体信息，如果没有则为null(譬如定位在最前端)<br>
	 * 1: 如果在条目内，为下个条目的具体信息<br>
	 * 如果在条目间，为下个条目的具体信息，如果没有则为null(譬如定位在最前端)
	 */
	protected T gffDetailUp = null;
	/**
	 * 只有geneDetail用到
	 * 获得上个条目的具体信息
	 * @return
	 */
	public T getGffDetailUp()
	{
		return gffDetailUp;
	}

	/**
	 *  如果在条目内，为本条目的具体信息，没有定位在基因内则为null<br>
	 */
	protected T gffDetailThis = null;
	/**
	 * 只有geneDetail用到
	 * 获得本条目的具体信息，
	 * 如果本条目为null，说明不在条目内
	 * @return
	 */
	public T getGffDetailThis()
	{
		return gffDetailThis;
	}
	/**
	 * 只有geneDetail用到
	 * 为下个条目的具体信息，如果没有则为null(譬如定位在最后端)
	 */
	protected T gffDetailDown = null;
	/**
	 * 只有geneDetail用到
	 * 获得下一个条目的具体信息
	 * @return
	 */
	public T getGffDetailDown()
	{
		return gffDetailDown;
	}
	
	/**
	 * 上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 */
	protected int ChrHashListNumUp = -1;
	/**
	 * 上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 */
	public int getItemNumUp() {
		return ChrHashListNumUp;
	}
	/**
	 * 为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果本条目不存在，则为-1<br>
	 */
	protected int ChrHashListNumThis = -1;
	/**
	 * 为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果本条目不存在，则为-1<br>
	 */
	public int getItemNumThis() {
		return ChrHashListNumThis;
	}
	/**
	 * 为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	protected int ChrHashListNumDown = -1;
	/**
	 * 为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	public int getItemNumDown() {
		return ChrHashListNumDown;
	}
	
	
}
