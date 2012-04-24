package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class ListAbs <E extends ListDetailAbs, T extends ListCodAbs<E>, K extends ListCodAbsDu<E, T>> extends ArrayList<E>  implements Cloneable{
	private static Logger logger = Logger.getLogger(ListAbs.class);
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
//	HashMap<Integer, Integer> hashCodInEleNum;
	/**
	 * 保存某个坐标到所在的内含子/外显子起点的距离
	 */
	HashMap<Integer, Integer> hashLocExInStart;

	/**
	 * 保存某个坐标到所在的内含子/外显子终点的距离
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	
	Boolean cis5to3 = null;
	/**
	 * 本条目的名字
	 */
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
			Collections.sort(this, new CompS2MAbs());
		}
		if (cis5to3) {
			Collections.sort(this, new CompS2M());
		}
		else {
			Collections.sort(this, new CompM2S());
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
	public int getEleLen(int num)
	{
		return get(num-1).getLen();
	}
	public int getLen()
	{
		if (cis5to3 != null) {
			return Math.abs(get(0).getStartCis() - get(size()-1).getEndCis()) + 1;
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
			return get(0).getStartCis();
		}
		return get(0).getStartAbs();
	}
	
	public int getEnd()
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
	public int getLocDistmRNA(int loc1, int loc2)
	{
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getLocInEleNum(locSmall); 
		int locBigExInNum = getLocInEleNum(locBig);
		
		int distance = ListCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		}
		else {
			distance = getCod2ExInEnd(locSmall) + getCod2ExInStart(locBig) + 1;
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
//					GffDetailAbs tmpExon = get(i);
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
//					GffDetailAbs tmpExon = get(i);
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
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getLocInEleNum(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getCod2ExInStart(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else
					return  location + Math.abs(mRNAnum);
			} 
			else {
				int exonNum = getLocInEleNum(location) - 1;
				int remain = Math.abs(mRNAnum) - getCod2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getEndCis() - remain + 1;
						}
						else {
							return tmpExon.getEndCis() + remain - 1;
						}
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getCod2ExInEnd(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else {
					return location - mRNAnum;
				}
			} 
			else {
				int exonNum = getLocInEleNum(location) - 1;
				int remain = mRNAnum - getCod2ExInEnd(location);
				for (int i = exonNum + 1; i < size(); i++) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getStartCis() + remain - 1;
						}
						else {
							return tmpExon.getStartCis() - remain + 1;
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
	public int getCod2ExInStart(int location) {
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
				loc2ExInStart = Math.abs(location - get(NumExon).getStartCis());//距离本外显子起始 nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getStartAbs());//距离本外显子起始 nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - get(NumExon).getEndCis()) -1;// 距前一个外显子 NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getEndAbs()) -1;// 距前一个外显子 NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * 坐标到element 终点距离
	 * @param location 坐标
	 *  * 该点在外显子中为正数，在内含子中为负数，为实际数目
	 */
	public int getCod2ExInEnd(int location) {
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
				 loc2ExInEnd = Math.abs(get(NumExon).getEndCis()- location);//距离本外显子终止  Cnnnnnnn
			else
				 loc2ExInEnd = Math.abs(get(NumExon).getEndAbs()- location);//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartCis() - location) - 1;// 距后一个外显子 nnCnnnnN
			else
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartAbs() - location) - 1;// 距后一个外显子 nnCnnnnN
			
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	

	
	/**
	 * 获得所有element的长度之和
	 */
	public int getListLen() {
		int isoLen = 0;
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
	public boolean compIso(ListAbs<E, T, K> lsOther)
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
	public int[] LocPosition( int Coordinate) {
		if (cis5to3 == null) {
			return BinarySearch.LocPositionAbs(this, Coordinate);
		}
		else if (cis5to3) {
			return BinarySearch.LocPositionCis(this, Coordinate);
		}
		else {
			return BinarySearch.LocPositionTran(this, Coordinate);
		}
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目，从1开始计数
	 * @return
	 */
	public int getLocInEleNum(int location) {
		return LocPosition(location)[3];
	}

	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	public T searchLocation(int Coordinate) {
		int[] locInfo = LocPosition(Coordinate);// 二分法查找peaknum的定位
		if (locInfo == null) {
			return null;
		}
		T gffCod = creatGffCod(listName, Coordinate);
		if (locInfo[0] == 1) // 定位在基因内
		{
			gffCod.setGffDetailThis( get(locInfo[1]) ); 
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = locInfo[1];
			gffCod.insideLOC = true;
			if (locInfo[1] - 1 >= 0) {
				gffCod.setGffDetailUp( get(locInfo[1]-1) );
				gffCod.ChrHashListNumUp = locInfo[1]-1;
				
			}
			if (locInfo[2] != -1) {
				gffCod.setGffDetailDown(get(locInfo[2]));
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		} else if (locInfo[0] == 2) {
			gffCod.insideLOC = false;
			if (locInfo[1] >= 0) {
				gffCod.setGffDetailUp( get(locInfo[1]) );
				gffCod.ChrHashListNumUp = locInfo[1];		
			}
			if (locInfo[2] != -1) {
				gffCod.setGffDetailDown( get(locInfo[2]) );
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		}
		return gffCod;
	}
	
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	public K searchLocationDu(int cod1, int cod2) {
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		T gffCod1 = searchLocation(cod1);
		T gffCod2 = searchLocation(cod2);
		if (gffCod1 == null) {
			System.out.println("error");
		}
		K lsAbsDu = creatGffCodDu(gffCod1, gffCod2);
		
		if (lsAbsDu.getGffCod1().getItemNumDown() >= 0) {
			for (int i = lsAbsDu.getGffCod1().getItemNumDown(); i <= lsAbsDu.getGffCod2().getItemNumUp(); i++) {
				lsAbsDu.getLsGffDetailMid().add(get(i));
			}
		}
		return lsAbsDu;
	}
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract T creatGffCod(String listName, int Coordinate);
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract K creatGffCodDu(T gffCod1, T gffCod2);
	/**
	 * 已测试，能用
	 */
	@SuppressWarnings("unchecked")
	public ListAbs<E, T, K> clone() {
		ListAbs<E, T, K> result = null;
		result = (ListAbs<E, T, K>) super.clone();
		result.cis5to3 = cis5to3;
		result.hashLocExInEnd = hashLocExInEnd;
		result.hashLocExInStart = hashLocExInStart;
		result.listName = listName;
		result.clear();
		for (E ele : this) {
			result.add((E) ele.clone());
		}
		return result;
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
	protected static int[] LocPositionCis(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
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
	protected static int[] LocPositionTran(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
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
	protected static int[] LocPositionAbs(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
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
class CompS2M implements Comparator<ListDetailAbs>
{
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
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
class CompS2MAbs implements Comparator<ListDetailAbs>
{
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
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
class CompM2S implements Comparator<ListDetailAbs>
{
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
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