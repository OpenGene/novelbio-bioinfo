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
		geneChrHashListNum[0] = -1000000000;
		geneChrHashListNum[1] = -1000000000;
		this.chrID = chrID;
		this.Coordinate = Coordinate;
		this.gffHash = gffHash;
		searchLocation();
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
	 * 定位情况 条目内/条目外
	 */
	public boolean locatInfo() {
		return insideLOC;
	}
	
	
	/**
	 * 上一个条目的方向
	 */
	private boolean upCis5to3 = false;
	/**
	 * 上一个条目的方向
	 */
	public boolean getUpCis5to3() {
		return upCis5to3;
	}
	
	/**
	 * 本条目的方向
	 */
	private boolean thiscis5to3 = false;
	/**
	 * 本条目的方向
	 */
	public boolean getThisCis5to3() {
		return thiscis5to3;
	}
	
	/**
	 * 下一个条目的方向，仅当坐标位于条目间时
	 */
	protected boolean downCis5to3 = false;
	/**
	 * 下一个条目的方向，仅当坐标位于条目间时
	 */
	public boolean getDownCis5to3() {
		return downCis5to3;
	}

	/**
	 * 为上个条目的具体信息，如果没有则为null(譬如定位在最前端)<br>
	 * 1: 如果在条目内，为下个条目的具体信息<br>
	 * 如果在条目间，为下个条目的具体信息，如果没有则为null(譬如定位在最前端)
	 */
	private GffDetailAbs gffDetailUp = null;
	
	/**
	 *  如果在条目内，为本条目的具体信息，没有定位在基因内则为null<br>
	 */
	private GffDetailAbs gffDetailThis = null;
	
	/**
	 * 为下个条目的具体信息，如果没有则为null(譬如定位在最后端)
	 */
	private GffDetailAbs gffDetailDown = null;
	
	
	/**
	 * 首先看上个基因与下个基因 0: 如果在条目内，为本条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为上个条目在ChrHash-list中的编号，从0开始，<b>如果上个条目不存在，则为-1</b><br>
	 * 1: 如果在条目内，为下个条目在ChrHash-list中的编号，从0开始<br>
	 * 如果在条目间，为下个条目在ChrHash-list中的编号，从0开始，<b>如果下个条目不存在，则为-1</b>
	 */
	protected int[] geneChrHashListNum = new int[2];

	/**
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 没找到就返回null
	 */
	protected void searchLocation() {
		ArrayList<GffDetailAbs> Loclist =  gffHash.getChrhash().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			booFindCod = false;
		}
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition();// 二分法查找peaknum的定位
		if (locInfo[0] == 1) // 定位在基因内
		{
			gffDetailThis = Loclist.get(locInfo[1]); gffDetailThis.setCoord(Coordinate);
			if (locInfo[1] - 1 >= 0) {
				gffDetailUp =  Loclist.get(locInfo[1]-1);
				gffDetailUp.setCoord(Coordinate);
			}
			if (locInfo[2] != -1) {
				gffDetailDown = Loclist.get(locInfo[2]);
				gffDetailDown.setCoord(Coordinate);
			}
		} else if (locInfo[0] == 2) {
			if (locInfo[1] >= 0) {
				gffDetailUp =  Loclist.get(locInfo[1]);
				gffDetailUp.setCoord(Coordinate);
			}
			if (locInfo[2] != -1) {
				gffDetailDown = Loclist.get(locInfo[2]);
				gffDetailDown.setCoord(Coordinate);
			}
		}
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 */
	private int[] LocPosition() {
		ArrayList<GffDetailAbs> Loclist =  gffHash.getChrhash().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			booFindCod = false;
			return null;
		}
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < Loclist.get(beginnum).getNumStart()) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate > Loclist.get(endnum).getNumStart()) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			if (Coordinate < Loclist.get(endnum).getNumStart()) {
				LocInfo[0] = 1;
				return LocInfo;
			} else {
				LocInfo[0] = 2;
				return LocInfo;
			}
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == Loclist.get(number).getNumStart()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < Loclist.get(number).getNumStart()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= Loclist.get(beginnum).getNumStart())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		return LocInfo;
	}
}
