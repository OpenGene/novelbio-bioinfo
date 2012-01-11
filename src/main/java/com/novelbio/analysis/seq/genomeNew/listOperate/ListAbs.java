package com.novelbio.analysis.seq.genomeNew.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.poi.ss.formula.functions.T;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.thresholdEntry;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodAbs;

public abstract class ListAbs <T extends ElementAbs> {
	public static final String SEP = "//";
	/**
	 * 保存某个坐标和所在的element数目,
	 * value: 正数，element中，
	 * 负数，两个element之间
	 */
	HashMap<Integer, Integer> hashCodInEleNum;

	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Integer, Integer> hashLocExInStart;

	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	
	/**
	 * 必须保证按次序装入
	 */
	protected ArrayList<T> lsElement = new ArrayList<T>();
	Boolean cis5to3 = null;
	/**
	 * 没有方向则返回null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
 	}
	
	/**
	 * 将list中的元素进行排序，如果element里面 start > end，那么就从大到小排序
	 * 如果element里面start < end，那么就从小到大排序
	 */
	protected void sortLsEle()
	{
		if (cis5to3 == null) {
			Collections.sort(lsElement, new CompS2MAbs());
		}
		if (cis5to3) {
			Collections.sort(lsElement, new CompS2M());
		}
		else {
			Collections.sort(lsElement, new CompM2S());
		}
	}
	/**
	 * 返回实际第num个element间区的长度
	 * @param num 实际数目
	 * @return
	 */
	public int getInterGenic(int num)
	{
		if (cis5to3 == null) {
			return lsElement.get(num).getStartAbs() - lsElement.get(num - 1).getEndAbs();
		}
		else {
			return Math.abs(lsElement.get(num).getStartCis() - lsElement.get(num - 1).getEndCis());
		}
	}
	/**
	 * 返回实际第num个element的长度
	 * @param num 实际数目
	 * @return
	 */
	public int getEleLen(int num)
	{
		return lsElement.get(num-1).getLen();
	}
	public int getLen()
	{
		if (cis5to3 != null) {
			return Math.abs(lsElement.get(0).getStartCis() - lsElement.get(lsElement.size()-1).getEndCis());
		}
		else {
			if (lsElement.size() == 1) {
				return lsElement.get(0).getLen();
			}
			else {
				if (lsElement.get(0).getStartAbs() < lsElement.get(1).getStartAbs()) {
					return lsElement.get(lsElement.size()-1).getEndAbs() - lsElement.get(0).getStartAbs();
				}
				else {
					return lsElement.get(0).getEndAbs() - lsElement.get(lsElement.size()-1).getStartAbs();
				}
			}
		}
	}
	
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2Start(int loc) {
		if (isLocDownStart(loc)) {
			return Math.abs(loc - getStart());
		}
		else {
			return -Math.abs(loc - getStart());
		}
	}
	
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2End(int loc) {
		if (isLocDownEnd(loc)) {
			return Math.abs(loc - getEnd());
		}
		else {
			return -Math.abs(loc - getEnd());
		}
	}
	
	/**
	 * 输入的loc是否在本list的范围外
	 * @return
	 */
	protected boolean isLocInside(int loc) {
		if (loc >= Math.max(getStart(), getEnd()) || loc <= Math.min(getStart(), getEnd())) {
			return false;
		}
		return true;
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownStart(int loc) {
		if (isCis5to3() && loc >= getStart()
		||
		!isCis5to3() && loc <= getStart()
		) {
			return true;
		}
		return false;
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownEnd(int loc) {
		if (isCis5to3() && loc >= getEnd()
		||
		!isCis5to3() && loc <= getEnd()
		) {
			return true;
		}
		return false;
	}
	
	public int getStart()
	{
		if (cis5to3 != null) {
			return lsElement.get(0).getStartCis();
		}
		return lsElement.get(0).getStartAbs();
	}
	
	public int getEnd()
	{
		if (cis5to3 != null) {
			return lsElement.get(lsElement.size() - 1).getEndCis();
		}
		return lsElement.get(lsElement.size() - 1).getEndAbs();
	}
	
	/**
	 * 两个坐标之间的距离，仅仅计算他们在mRNA层面的距离，也就是只计算ele上的距离。
	 * 当loc1在loc2上游时，返回正数，当loc1在loc2下游时，返回负数
	 * 要求这两个坐标都在exon上.如果不符合，则返回GffCodAbs.LOC_ORIGINAL
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public int getLocDistmRNA(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getLocInEleNum(locSmall); int locBigExInNum = getLocInEleNum(locBig);
		
		int distance = GffCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getLoc2EleEnd(locSmall) + getLoc2EleStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + lsElement.get(i).getLen();
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return Math.abs(distance);
		}
		return -Math.abs(distance);
	}
	
	/**
	 * 只有在cis存在的时候才能使用
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 */
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getLocInEleNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getLoc2EleStart(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocInEleNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getLoc2EleStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					ElementAbs tmpExon = lsElement.get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						return tmpExon.getEndCis() - remain + 1;
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getLoc2EleEnd(location)) {
				return location + mRNAnum;
			} 
			else {
				int exonNum = getLocInEleNum(location) - 1;
				int remain = mRNAnum - getLoc2EleEnd(location);
				for (int i = exonNum + 1; i < lsElement.size(); i++) {
					ElementAbs tmpExon = lsElement.get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						return tmpExon.getStartCis() + remain - 1;
					}
				}
				return -1;
			}
		}
	}
	
	
	/**
	 * 必须首先设定ListAbs的方向，并且该方向和其内部的element的方向要一致
	 * 坐标到element 起点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected int getLoc2EleStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getLocInEleNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStartCis());//距离本外显子起始 nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStartAbs());//距离本外显子起始 nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEndCis()) -1;// 距前一个外显子 NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEndAbs()) -1;// 距前一个外显子 NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * 坐标到element 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected int getLoc2EleEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getLocInEleNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEndCis()- location);//距离本外显子终止  Cnnnnnnn
			else
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEndAbs()- location);//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStartCis() - location) - 1;// 距后一个外显子 nnCnnnnN
			else
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStartAbs() - location) - 1;// 距后一个外显子 nnCnnnnN
			
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	

	
	/**
	 * 获得所有element的长度之和
	 */
	public int getListLen() {
		int isoLen = 0;
		for (T exons : lsElement) {
			isoLen = isoLen + exons.getLen();
		}
		return isoLen;
	}
	
	public int size()
	{
		return lsElement.size();
	}
	public T get(int i)
	{
		return lsElement.get(i);
	}
	
	/**
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	public boolean compIso(ListAbs<T> lsOther)
	{
		if (lsOther.size() != size()) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			T otherT = lsOther.get(i);
			T thisT = get(i);
			if (!otherT.equals(thisT)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 返回每个ID对应的具体element
	 * @return
	 */
	public void getLocHashtable(HashMap<String,T> hashLocMap)
	{
		for (T ele : lsElement) {
			String[] ss = ele.getLocString().split(SEP);
			for (String string : ss) {
				hashLocMap.put(string, ele);
			}
		}
	}
	/**
	 * 返回每个ID对应的Num
	 * @return
	 */
	public void getHashLocNum(HashMap<String,Integer> hashLocNum)
	{
		for (int i = 0; i < lsElement.size(); i++) {
			T ele = lsElement.get(i);
			String[] ss = ele.getLocString().split(SEP);
			for (String string : ss) {
				hashLocNum.put(string, i);
			}
		}
	}
	
	/**
	 * 返回每个ID对应的具体element
	 * @return
	 */
	public HashMap<String,Integer> getHash2Num()
	{
		HashMap<String, Integer> hashID2Num = new HashMap<String, Integer>();
		for (int i = 0; i < lsElement.size(); i++) {
			T ele = lsElement.get(i);
			String[] ss = ele.getLocString().split(SEP);
			for (String string : ss) {
				hashID2Num.put(string, i);
			}
		}
		return hashID2Num;
	}
	/**
	 * 返回本ListAbs中的所有string名字\
	 * 如果两个Item是重叠的，就用ListAbs.SEP隔开，
	 * @return
	 */
	public ArrayList<String> getLOCIDList()
	{
		ArrayList<String> lsLocID = new ArrayList<String>();
		for (T ele : lsElement) {
			lsLocID.add(ele.getLocString());
		}
		return lsLocID;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因<br>
	 * 3：该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	public int[] LocPosition( int Coordinate) {
		if (cis5to3 == null) {
			return BinarySearch.LocPositionAbs(lsElement, Coordinate);
		}
		else if (cis5to3) {
			return BinarySearch.LocPositionCis(lsElement, Coordinate);
		}
		else {
			return BinarySearch.LocPositionTran(lsElement, Coordinate);
		}
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 * @return
	 */
	public int getLocInEleNum(int location) {
		return LocPosition(location)[3];
	}
	
	
	
	
	
	
	
	
}




class BinarySearch
{
	
 
	

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static int[] LocPositionCis(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		int[] LocInfo = new int[4];
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartCis()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartCis()) {
			if (Coordinate > lsElement.get(endnum).getEndCis()) {
				LocInfo[0] = 2;
				LocInfo[3] = 0;
			}
			else {
				LocInfo[0] = 1;
				LocInfo[3] = endnum + 1;
			}
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			return LocInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= lsElement.get(beginnum).getEndCis())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static int[] LocPositionTran(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		int[] LocInfo = new int[4];
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate > lsElement.get(beginnum).getStartCis()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) {
			if (Coordinate < lsElement.get(endnum).getEndCis()) {
				LocInfo[0] = 2;
				LocInfo[3] = 0;
			}
			else {
				LocInfo[0] = 1;
				LocInfo[3] = endnum + 1;
			}
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			return LocInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate > lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate >= lsElement.get(beginnum).getEndCis())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static int[] LocPositionAbs(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		int[] LocInfo = new int[4];
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartAbs()) {
			if (Coordinate > lsElement.get(endnum).getEndAbs()) {
				LocInfo[0] = 2;
				LocInfo[3] = 0;
			}
			else {
				LocInfo[0] = 1;
				LocInfo[3] = endnum + 1;
			}
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			return LocInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartAbs()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= lsElement.get(beginnum).getEndAbs())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}
	
}
/**
 * 从小到大排序
 * @author zong0jie
 */
class CompS2M implements Comparator<ElementAbs>
{
	@Override
	public int compare(ElementAbs o1, ElementAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从小到大排序，用绝对坐标值排序
 * @author zong0jie
 */
class CompS2MAbs implements Comparator<ElementAbs>
{
	@Override
	public int compare(ElementAbs o1, ElementAbs o2) {
		Integer o1start = o1.getStartAbs();
		Integer o2start = o2.getStartAbs();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndAbs();
			Integer o2end = o2.getEndAbs();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从大到小排序
 * @author zong0jie
 */
class CompM2S implements Comparator<ElementAbs>
{
	@Override
	public int compare(ElementAbs o1, ElementAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return -o1end.compareTo(o2end);
		}
		return -comp;
	}
	

}