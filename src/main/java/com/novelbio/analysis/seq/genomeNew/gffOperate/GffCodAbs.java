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

	/**
	 * 构造函数赋初值
	 */
	GffCodAbs(String chrID, int Coordinate) {
		distancetoLOCStart[0] = -1000000000;
		distancetoLOCEnd[0] = -1000000000;
		geneChrHashListNum[0] = -1000000000;
		distancetoLOCStart[1] = -1000000000;
		distancetoLOCEnd[1] = -1000000000;
		geneChrHashListNum[1] = -1000000000;
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
	public boolean result = false;

	/**
	 * 定位情况 条目内/条目外
	 */
	public boolean insideLOC = false;

	/**
	 * 本条目/上一个条目的方向
	 */
	public boolean begincis5to3 = false;

	/**
	 * 下一个条目的方向，仅当坐标位于条目间时
	 */
	public boolean endcis5to3 = false;

	/**
	 * 基因LOCID，为chrHash里面的编号，注意：本编号不一定与LOCIDlist里的编号相同！目前仅在UCSCgene中不同，
	 * UCSCgene要先通过split("/")切割才能进入locHashtable查找 在 0：本条目编号 1: 上个条目编号 2：下个条目编号
	 * 如果坐标前/后没有相应的基因(譬如坐标在最前端)，那么相应的LOCID为null
	 */
	public String[] LOCID = new String[3];

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
	public int[] distancetoLOCStart = new int[2];

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
	public int[] distancetoLOCEnd = new int[2];

	/**
	 * 0: 如果在条目内，为本条目的具体信息<br>
	 * 如果在条目间，为上个条目的具体信息，如果没有则为null(譬如定位在最前端)<br>
	 * 1: 如果在条目内，为下个条目的具体信息<br>
	 * 如果在条目间，为下个条目的具体信息，如果没有则为null(譬如定位在最后端)
	 */
	public GffDetail[] geneDetail = new GffDetail[2];

	/**
	 * 首先看上个基因与下个基因 0: 如果在条目内，为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 * 1: 如果在条目内，为下个条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	public int[] geneChrHashListNum = new int[2];

	/**
	 * 单坐标查找 输入ChrID，单个坐标，以及GffHash类<br>
	 * ,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写, chr1,chr2,chr11<br>
	 * 获得基因信息-存储在GffCoordiatesInfo类中<br>
	 * 按照想要的结果，用不同的GffCodInfo子类接收<br>
	 * 如用GffsearchGene搜索则用GffCodinfoGene接收
	 */
	public void searchLocation(GffHash gffHash) {
		String Chrpatten = "Chr\\w+";// Chr1， chr2，
										// chr11的形式,注意还有chrx之类的，chr里面可以带"_"，所以说不能用"_"分割chr与字符
		/**
		 * 判断Chr格式是否正确，是否是有效的染色体
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
		Matcher matcher;
		matcher = pattern.matcher(chrID);
		if (!matcher.find()) {
			result = false;
			return;
		}
		chrID = matcher.group().toLowerCase();

		Hashtable<String, ArrayList<GffDetail>> LocHash = gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			result = false;
			return;
		}
		searchLocation(Loclist);
	}

	/**
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 */
	private void searchLocation(ArrayList<GffDetail> Loclist) {
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition(Loclist);// 二分法查找peaknum的定位
		if (locInfo[0] == 1) // 定位在基因内
		{
			SearchLOCinside(Loclist, locInfo[1], locInfo[2]);// 查找具体哪个内含子或者外显子
			geneChrHashListNum[0] = locInfo[1];

			if (locInfo[1] == -1)
				geneDetail[0] = null;
			else
				geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				geneDetail[1] = null;
			else
				geneDetail[1] = Loclist.get(locInfo[2]);

			geneChrHashListNum[1] = locInfo[2];
			return;
		} else if (locInfo[0] == 2) {
			SearchLOCoutside(Loclist, locInfo[1], locInfo[2]);// 查找基因外部的peak的定位情况
			if (locInfo[1] == -1)
				geneDetail[0] = null;
			else
				geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				geneDetail[1] = null;
			else
				geneDetail[1] = Loclist.get(locInfo[2]);

			geneChrHashListNum[0] = locInfo[1];
			geneChrHashListNum[1] = locInfo[2];
			return;
		}
		return;
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 */
	private int[] LocPosition(ArrayList<GffDetail> Loclist) {
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < Loclist.get(beginnum).numberstart) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate > Loclist.get(endnum).numberstart) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			if (Coordinate < Loclist.get(endnum).numberend) {
				LocInfo[0] = 1;
				return LocInfo;
			} else {
				LocInfo[0] = 2;
				return LocInfo;
			}
		}

		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == Loclist.get(number).numberstart) {
				beginnum = number;
				endnum = number + 1;
				break;
			}

			else if (Coordinate < Loclist.get(number).numberstart
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= Loclist.get(beginnum).numberend)// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		return LocInfo;
	}

	/**
	 * 必须被覆盖，填充 result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3 等几乎所有信息
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param i
	 *            本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因
	 * @param j
	 *            下个基因的序号 -1表示后面没有基因
	 * @return
	 */
	protected abstract void SearchLOCinside(ArrayList<GffDetail> loclist,
			int i, int j);

	/**
	 * 必须被覆盖，填充 result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param i
	 *            本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因
	 * @param j
	 *            下个基因的序号 -1表示后面没有基因
	 * @return
	 */
	protected abstract void SearchLOCoutside(ArrayList<GffDetail> loclist,
			int i, int j);

	/**
	 * 需要覆盖 给定基因名，返回该基因信息,一个GffDetailList类 按照想要的结果，用不同的GffDetail子类接收<br>
	 * 如用GffsearchGene搜索则用GffDetailGene接收
	 * 
	 * @param LocID
	 */
	public static GffDetail LOCsearch(String LocID, GffHash gffHash) {
		return gffHash.LOCsearch(LocID);
	}

}
