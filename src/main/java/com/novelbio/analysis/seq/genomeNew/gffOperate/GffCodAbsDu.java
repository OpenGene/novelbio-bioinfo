package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.database.entity.kegg.noUseKGCentry2Ko2Gen;

/**
 * 双坐标
 * @author zong0jie
 *
 */
public abstract class GffCodAbsDu {
	//这两个都会在具体的类中新建
	GffCodAbs gffCodAbs1 = null;
	GffCodAbs gffCodAbs2 = null;
	//保存左端点的gffdetail
	GffDetail gffDetail1 = null;
	//保存右端点的gffdetail
	GffDetail gffDetail2 = null;
	//两个端点之间的gffdetail
	ArrayList<GffDetail> lsgffDetailsMid = new ArrayList<GffDetail>();
	/**
	 * peak与左端Item交集时，交集在左端Item中所占的比例
	 */
	double opLeftInItem = -1;
	/**
	 * peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 */
	double opLeftInCod = -1;
	/**
	 * peak与左端Item交集时，实际交集的bp数
	 */
	int opLeftBp = -1;
	/**
	 * peak与右端Item交集时，交集在左端Item中所占的比例
	 */
	double opRightInItem = -1;
	/**
	 * peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 */
	double opRightInCod = -1;
	/**
	 * peak与右端Item交集时，实际交集的bp数
	 */
	int opRightBp = -1;
	/**
	 *  peak与左端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpLeftInItem() {
		return opLeftInItem;
	}
	/**
	 * peak与右端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpRightInItem() {
		return opRightInItem;
	}
	/**
	 *  peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpLeftInCod() {
		return opLeftInCod;
	}
	/**
	 *  peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpRightInCod() {
		return opRightInCod;
	}
	/**
	 *  peak与左端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpLeftBp() {
		return opLeftBp;
	}
	/**
	 *  peak与右端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpRightBp() {
		return opRightBp;
	}
	/**
	 * 返回左端的GffCod，覆盖成相应的GffCod类
	 * @return
	 */
	public abstract GffCodAbs getGffCodLeft();
	/**
	 * 返回左端的GffCod，覆盖成相应的GffCod类
	 * @return
	 */
	public abstract GffCodAbs getGffCodRight();
	/**
	 * 返回两个坐标中间夹着的的GffDetail，覆盖成相应的GffDetail类
	 * @return
	 */
	public abstract ArrayList<GffDetail> getLsGffDetailMid();
	
	/**
	 * 双坐标查找 输入相关的GffHash类，然后填充相关信息<br>
	 */
	public void searchLocation(GffHash gffHash) {
		Hashtable<String, ArrayList<GffDetail>> LocHash = gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(gffCodAbs1.getChrID().toLowerCase());// 某一条染色体的信息
		gffCodAbs1.searchLocation(gffHash);
		gffCodAbs2.searchLocation(gffHash);
		/**
		 * 如果peak两个端点都在在同一条目之内
		 */
		if (gffCodAbs1.geneChrHashListNum[0] == gffCodAbs2.geneChrHashListNum[0]
				&& gffCodAbs1.insideLOC && gffCodAbs2.insideLOC) 
		{
			gffDetail1= Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			int thisDetailLength = gffDetail1.numberend - gffDetail1.numberstart;
			int peakLength = gffCodAbs1.getCoord() - gffCodAbs2.getCoord();
			opLeftInItem = 100 * (double) peakLength / thisDetailLength;
			opLeftInCod = 100;
			opLeftBp = peakLength; opRightInItem = opLeftInItem; opRightInCod = 100; opRightBp = opLeftBp;
		}
		// 如果peak左端点在一个条目内，右端点在另一个条目内
		else if (gffCodAbs1.insideLOC
				&& gffCodAbs2.insideLOC
				&& gffCodAbs1.geneChrHashListNum[0] != gffCodAbs2.geneChrHashListNum[0]) 
		{
			gffDetail1 = Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			gffDetail2 = Loclist.get(gffCodAbs2.geneChrHashListNum[0]);
			int leftItemLength = gffDetail1.numberend - gffDetail1.numberstart;
			int rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;

			int leftoverlap = gffDetail1.numberend - gffCodAbs1.getCoord();
			int rightoverlap = gffCodAbs2.getCoord() - gffDetail2.numberstart;
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();

			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// peak只有左端点在条目内
		else if (gffCodAbs1.insideLOC && !gffCodAbs2.insideLOC) {
			gffDetail1= Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			int leftItemLength = gffDetail1.numberend
					- gffDetail1.numberstart;
			int leftoverlap = gffDetail1.numberend - gffCodAbs1.getCoord();
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();

			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 0; opRightInCod = 0; opRightBp = 0;
		}
		// peak只有右端点在条目内
		else if (!gffCodAbs1.insideLOC && gffCodAbs2.insideLOC) {
			gffDetail2 = Loclist.get(gffCodAbs2.geneChrHashListNum[0]);
			int rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;
			int rightoverlap = gffCodAbs2.getCoord() - gffDetail2.numberstart;
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// //////////////////////////////////////////////获得两个坐标以及中间夹着的条目，注意不包括坐标所在的基因///////////////////////////////////////////////////////////////////////////////
		// 两个坐标的ID以及 之间条目的ID起止，如--------------
		// coordinate1(Cod1ID)----------ItemA(startID)-------ItemB-------ItemC------ItemD(endID)-------coordinate2(Cod2ID)-----------------
		int Cod1ID = -1, Cod2ID = -1;
		Cod1ID = gffCodAbs1.geneChrHashListNum[0];// 本条目/上个条目编号
		if (gffCodAbs2.insideLOC)
			Cod2ID = gffCodAbs2.geneChrHashListNum[0];// 本条目编号
		else
			Cod2ID = gffCodAbs2.geneChrHashListNum[1];// 下个条目编号
		if ((Cod2ID - Cod1ID) > 1)// 把Cod1ID和Cod2ID之间的所有条目的GffDetail装入LstGffCodInfo
		{
			for (int i = 1; i < (Cod2ID - Cod1ID); i++) {
				GffDetail gffDetail = Loclist.get(Cod1ID + i);
				lsgffDetailsMid.add(gffDetail);
			}
		}
	}
}
