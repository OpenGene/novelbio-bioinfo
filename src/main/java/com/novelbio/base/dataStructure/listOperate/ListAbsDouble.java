package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ListAbsDouble <E extends ElementAbsDouble> extends ArrayList<E>{
	private static Logger logger = Logger.getLogger(ListAbsDouble.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4583552188474447935L;
	/**
	 * 好像是分割同一个element的多个name的符号，待确认
	 */
	public static final String SEP = "/";

	/**
	 * 保存某个坐标和所在的element数目,
	 * value: 正数，element中，
	 * 负数，两个element之间
	 */
	HashMap<Integer, Double> hashCodInEleNum;
	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Double, Double> hashLocExInStart;

	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Double, Double> hashLocExInEnd;
	
	Boolean cis5to3 = null;
	protected String listName = "";
	public void setName(String listName) {
		this.listName = listName;
	}
	public String getName() {
		return listName;
	}
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
	public void sort()
	{
		if (cis5to3 == null) {
			Collections.sort(this, new CompS2MAbsDouble());
		}
		if (cis5to3) {
			Collections.sort(this, new CompS2MDouble());
		}
		else {
			Collections.sort(this, new CompM2SDouble());
		}
	}
	
	/**
	 * 返回实际第num个element间区的长度
	 * @param num 实际数目
	 * @return
	 */
	public double getInterGenic(int num)
	{
		if (cis5to3 == null) {
			return get(num).getStartAbs() - get(num - 1).getEndAbs();
		}
		else {
			return Math.abs(get(num).getStartCis() - get(num - 1).getEndCis());
		}
	}
	/**
	 * 返回实际第num个element的长度
	 * @param num 实际数目
	 * @return
	 */
	public double getEleLen(int num)
	{
		return get(num-1).getLen();
	}
	/**
	 * 用时需谨慎
	 * @return
	 */
	public double getLen()
	{
		if (cis5to3 != null) {
			return Math.abs(get(0).getStartCis() - get(size()-1).getEndCis());
 		}
		else {
			if (size() == 1) {
				return get(0).getLen();
			}
			else {
				if (get(0).getStartAbs() < get(1).getStartAbs()) {
					return get(size()-1).getEndAbs() - get(0).getStartAbs();
				}
				else {
					return get(0).getEndAbs() - get(size()-1).getStartAbs();
				}
			}
		}
	}
	
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected double getLoc2Start(int loc) {
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
	protected double getLoc2End(int loc) {
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
	
	public double getStart()
	{
		if (cis5to3 != null) {
			return get(0).getStartCis();
		}
		return get(0).getStartAbs();
	}
	
	public double getEnd()
	{
		if (cis5to3 != null) {
			return get(size() - 1).getEndCis();
		}
		return get(size() - 1).getEndAbs();
	}
	
	/**
	 * 两个坐标之间的距离，仅仅计算他们在mRNA层面的距离，也就是只计算ele上的距离。
	 * 当两者重叠时，返回0
	 * 当loc1在loc2上游时，返回正数，当loc1在loc2下游时，返回负数
	 * 要求这两个坐标都在exon上.如果不符合，则返回GffCodAbs.LOC_ORIGINAL
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public double getLocDistmRNA(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = searchLocInEleNum(locSmall); 
		int locBigExInNum = searchLocInEleNum(locBig);
		
		double distance = ListCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		}
		else {
			//double这里不需要加一
			distance = getLoc2EleEnd(locSmall) + getLoc2EleStart(locBig);
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + get(i).getLen();
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return Math.abs(distance);
		}
		return -Math.abs(distance);
	}
	
//	/**
//	 * TO BE CHECKED
//	 * 只有在cis存在的时候才能使用
//	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
//	 * 在loc下游时num为正数
//	 * 如果num Bp外就没有基因了，则返回-1；
//	 * @param mRNAnum
//	 * NnnnLoc 为-4位，当N与Loc重合时为0
//	 */
//	public int getLocDistmRNASite(int location, int mRNAnum) {
//		if (getLocInEleNum(location) <= 0) {
//			return -1;
//		}
//		if (mRNAnum < 0) {
//			if (Math.abs(mRNAnum) <= getLoc2EleStart(location)) {
//				return location + mRNAnum;
//			} 
//			else {
//				int exonNum = getLocInEleNum(location) - 1;
//				int remain = Math.abs(mRNAnum) - getLoc2EleStart(location);
//				for (int i = exonNum - 1; i >= 0; i--) {
//					ElementAbs tmpExon = get(i);
//					// 一个一个外显子的向前遍历
//					if (remain - tmpExon.getLen() > 0) {
//						remain = remain - tmpExon.getLen();
//						continue;
//					}
//					else {
//						return tmpExon.getEndCis() - remain + 1;
//					}
//				}
//				return -1;
//			}
//		}
//		else {
//			if (mRNAnum <= getLoc2EleEnd(location)) {
//				return location + mRNAnum;
//			} 
//			else {
//				int exonNum = getLocInEleNum(location) - 1;
//				int remain = mRNAnum - getLoc2EleEnd(location);
//				for (int i = exonNum + 1; i < size(); i++) {
//					ElementAbs tmpExon = get(i);
//					// 一个一个外显子的向前遍历
//					if (remain - tmpExon.getLen() > 0) {
//						remain = remain - tmpExon.getLen();
//						continue;
//					}
//					else {
//						return tmpExon.getStartCis() + remain - 1;
//					}
//				}
//				return -1;
//			}
//		}
//	}
	/**
	 * TO BE CHECKED
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 * LnnnnN为5位
	 */
	public double getLocDistmRNASite(int location, int mRNAnum) {
		if (searchLocInEleNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getLoc2EleStart(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else
					return  location + Math.abs(mRNAnum);
			} 
			else {
				int exonNum = searchLocInEleNum(location) - 1;
				double remain = Math.abs(mRNAnum) - getLoc2EleStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						//不需要加一或减一
						if (isCis5to3()) {
							return tmpExon.getEndCis() - remain;
						}
						else {
							return tmpExon.getEndCis() + remain;
						}
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getLoc2EleEnd(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else {
					return location - mRNAnum;
				}
			} 
			else {
				int exonNum = searchLocInEleNum(location) - 1;
				double remain = mRNAnum - getLoc2EleEnd(location);
				for (int i = exonNum + 1; i < size(); i++) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						//不需要加一或减一
						if (isCis5to3()) {
							return tmpExon.getStartCis() + remain;
						}
						else {
							return tmpExon.getStartCis() - remain;
						}
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
	protected double getLoc2EleStart(double location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Double, Double>();
		}
		else if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		double loc2ExInStart = -1000000000;
		int exIntronNum = searchLocInEleNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - get(NumExon).getStartCis());//距离本外显子起始 nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getStartAbs());//距离本外显子起始 nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			//不需要加一或减一
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - get(NumExon).getEndCis());// 距前一个外显子 NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getEndAbs());// 距前一个外显子 NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * 坐标到element 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	protected double getLoc2EleEnd(double location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Double, Double>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		double loc2ExInEnd = -1000000000;
		int exIntronNum = searchLocInEleNum(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				 loc2ExInEnd = Math.abs(get(NumExon).getEndCis()- location);//距离本外显子终止  Cnnnnnnn
			else
				 loc2ExInEnd = Math.abs(get(NumExon).getEndAbs()- location);//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartCis() - location);// 距后一个外显子 nnCnnnnN
			else
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartAbs() - location);// 距后一个外显子 nnCnnnnN
			
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	

	
	/**
	 * 获得所有element的长度之和
	 */
	public double getListLen() {
		double isoLen = 0;
		for (E exons : this) {
			isoLen = isoLen + exons.getLen();
		}
		return isoLen;
	}
	
	/**
	 * 依次比较两个list中的元素是否一致。内部调用每个元素的equals方法
	 * 不比较name，如果需要比较name，那么就用equal
	 * 暂时还没重写equal
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	public boolean compIso(ListAbsDouble<E> lsOther)
	{
		if (lsOther.size() != size() ) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			E otherT = lsOther.get(i);
			E thisT = get(i);
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
	public void getLocHashtable(HashMap<String,E> hashLocMap)
	{
		for (E ele : this) {
			String[] ss = ele.getName().split(SEP);
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
		for (int i = 0; i < size(); i++) {
			E ele = get(i);
			String[] ss = ele.getName().split(SEP);
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
		for (int i = 0; i < size(); i++) {
			E ele = get(i);
			String[] ss = ele.getName().split(SEP);
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
		for (E ele : this) {
			lsLocID.add(ele.getName());
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
	public int[] LocPosition( double Coordinate) {
		if (cis5to3 == null) {
			return BinarySearchDouble.LocPositionAbs(this, Coordinate);
		}
		else if (cis5to3) {
			return BinarySearchDouble.LocPositionCis(this, Coordinate);
		}
		else {
			return BinarySearchDouble.LocPositionTran(this, Coordinate);
		}
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 * @return
	 */
	public int searchLocInEleNum(double location) {
		return LocPosition(location)[3];
	}
	
}




class BinarySearchDouble
{
	
 
	

	/**
	 * 待验证
	 * 前闭后开区间
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static int[] LocPositionCis(ArrayList<? extends ElementAbsDouble> lsElement, double Coordinate) {
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
		else if (Coordinate >= lsElement.get(endnum).getStartCis())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[1] = endnum;
			LocInfo[2] = endnum + 1;
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
	protected static int[] LocPositionTran(ArrayList<? extends ElementAbsDouble> lsElement, double Coordinate) {
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
		if (Coordinate >= lsElement.get(beginnum).getEndCis())
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) 
		{// location在基因内部
			LocInfo[0] = 1;
			LocInfo[1] = endnum;
			LocInfo[2] = endnum + 1;
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
	protected static int[] LocPositionAbs(ArrayList<? extends ElementAbsDouble> lsElement, double Coordinate) {
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
class CompS2MDouble implements Comparator<ElementAbsDouble>
{
	@Override
	public int compare(ElementAbsDouble o1, ElementAbsDouble o2) {
		Double o1start = o1.getStartCis();
		Double o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Double o1end = o1.getEndCis();
			Double o2end = o2.getEndCis();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从小到大排序，用绝对坐标值排序
 * @author zong0jie
 */
class CompS2MAbsDouble implements Comparator<ElementAbsDouble>
{
	@Override
	public int compare(ElementAbsDouble o1, ElementAbsDouble o2) {
		Double o1start = o1.getStartAbs();
		Double o2start = o2.getStartAbs();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Double o1end = o1.getEndAbs();
			Double o2end = o2.getEndAbs();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从大到小排序
 * @author zong0jie
 */
class CompM2SDouble implements Comparator<ElementAbsDouble>
{
	@Override
	public int compare(ElementAbsDouble o1, ElementAbsDouble o2) {
		Double o1start = o1.getStartCis();
		Double o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Double o1end = o1.getEndCis();
			Double o2end = o2.getEndCis();
			return -o1end.compareTo(o2end);
		}
		return -comp;
	}
	

}