package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * peak定位查找信息的基本类,并且直接可以用于CG与Peak<br>
 * 子类有GffCodInfoGene
 * 
 * @author zong0jie
 * 
 */
public abstract class GffCodAbs {

	GffHash gffHash = null;
	/**
	 * 构造函数赋初值
	 */
	protected  GffCodAbs(String chrID, int Coordinate,GffHash gffHash) {
		distancetoLOCStart[0] = -1000000000;
		distancetoLOCEnd[0] = -1000000000;
		geneChrHashListNum[0] = -1000000000;
		distancetoLOCStart[1] = -1000000000;
		distancetoLOCEnd[1] = -1000000000;
		geneChrHashListNum[1] = -1000000000;
		this.chrID = chrID;
		this.Coordinate = Coordinate;
		this.gffHash = gffHash;
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
	protected boolean result = false;
	/**
	 * 是否成功找到cod
	 * @return
	 */
	public boolean findCod() {
		return result;
	}
	/**
	 * 定位情况 条目内/条目外
	 */
	protected boolean insideLOC = false;
	/**
	 * 定位情况 条目内/条目外
	 */
	public boolean locatInfo() {
		return insideLOC;
	}
	/**
	 * 本条目/上一个条目的方向
	 */
	protected boolean begincis5to3 = false;
	/**
	 * 本条目/上一个条目的方向
	 */
	public boolean getBiginCis5to3() {
		return begincis5to3;
	}
	
	/**
	 * 下一个条目的方向，仅当坐标位于条目间时
	 */
	protected boolean endcis5to3 = false;

	/**
	 * 基因LOCID，为chrHash里面的编号，注意：本编号不一定与LOCIDlist里的编号相同！目前仅在UCSCgene中不同，
	 * UCSCgene要先通过split("/")切割才能进入locHashtable查找 在 0：本条目编号 1: 上个条目编号 2：下个条目编号
	 * 如果坐标前/后没有相应的基因(譬如坐标在最前端)，那么相应的LOCID为null
	 */
	protected String[] LOCID = new String[3];

	/**
	 * 坐标到条目起点的位置,考虑正反向<br/>
	 * 为int[2]：<br>
	 * <b>如果是条目内</b><br>
	 * 0:坐标为和本条目起点的距离，都是正号<br>
	 * 1：-1<br>
	 * <br>
	 * <b>如果是条目间，是与上下项目的距离，但是如果没有上/下项目，则相应项为0</b><br>
	 * 0:坐标和上个条目起点的距离<br>
	 * 如果上个条目为正向，则为正号+<br>
	 * 如果上个条目为反向，则为负号-<br>
	 * <br>
	 * 1:坐标和下个条目起点的距离<br>
	 * 如果下个基因为正向，则为负号-<br/>
	 * 如果下个基因为反向，则为正号+<br/>
	 */
	protected int[] distancetoLOCStart = new int[2];

	/**
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 为int[2]：<br>
	 * <b>如果是条目内</b><br>
	 * 0:坐标为和本条目终点的距离，都是正号<br>
	 * 1：-1<br>
	 * <br>
	 * <b>如果是条目间，是与上/下项目的距离，但是如果没有上/下项目，则相应项为0
	 * 所以先要看有没有上/下项目。用geneChrHashListNum看，如果相应值为-1，则说明没有该项</b><br>
	 * 0:坐标和上个条目终点的距离<br>
	 * 如果上个条目为正向，则为负号-<br>
	 * 如果上个条目为反向，则为正号+<br>
	 * <br>
	 * 1:坐标和下个条目终点的距离<br>
	 * 如果下个条目为正向，则为正号+<br/>
	 * 如果下个条目为反向，则为负号-<br/>
	 */
	protected int[] distancetoLOCEnd = new int[2];

	/**
	 * 0: 如果在条目内，为本条目的具体信息<br>
	 * 如果在条目间，为上个条目的具体信息，如果没有则为null(譬如定位在最前端)<br>
	 * 1: 如果在条目内，为下个条目的具体信息<br>
	 * 如果在条目间，为下个条目的具体信息，如果没有则为null(譬如定位在最后端)
	 */
	protected GffDetailAbs[] geneDetail = new GffDetailAbs[2];

	/**
	 * 首先看上个基因与下个基因 0: 如果在条目内，为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 * 1: 如果在条目内，为下个条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	protected int[] geneChrHashListNum = new int[2];

	

}
