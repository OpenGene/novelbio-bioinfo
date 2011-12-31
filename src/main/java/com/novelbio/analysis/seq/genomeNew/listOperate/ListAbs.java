package com.novelbio.analysis.seq.genomeNew.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.poi.ss.formula.functions.T;
import org.apache.tomcat.util.bcel.classfile.Code;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodAbs;

public abstract class ListAbs<T extends ElementAbs> {
	
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
	
	public boolean isCis5to3() {
		if (cis5to3 == null) {
			return true;
		}
		return cis5to3;
	}
	
	private void setCis5to3()
	{
		if (cis5to3 != null) {
			return;
		}
		if (lsElement.size() == 0) {
			return;
		}
		for (ElementAbs ele : lsElement) {
			if (ele.getStart() < ele.getEnd()) {
				cis5to3 = true;
				break;
			}
			else if (ele.getStart() > ele.getEnd()) {
				cis5to3 = false;
				break;
			}
		}
	}
	
	/**
	 * 将list中的元素进行排序，如果element里面 start > end，那么就从大到小排序
	 * 如果element里面start < end，那么就从小到大排序
	 */
	protected void sortLsEle()
	{
		setCis5to3();
		if (cis5to3) {
			Collections.sort(lsElement, new CompS2M());
		}
		else {
			Collections.sort(lsElement, new CompM2S());
		}
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	public int getLocInEleNum(int location)
	{
		setCis5to3();
		if (cis5to3 == null || cis5to3) {
			return getLocExInNumCis(location);
		}
		else {
			return getLocExInNumTans(location);
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
		return lsElement.get(0).getStart();
	}
	
	public int getEnd()
	{
		return lsElement.get(lsElement.size() - 1).getEnd();
	}
	
	/**
	 * 两个坐标之间的距离，mRNA层面，当loc1在loc2上游时，返回正数，当loc1在loc2下游时，返回负数
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
			loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStart());//距离本外显子起始 nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEnd()) -1;// 距前一个外显子 NnnnCnnnn
			hashLocExInStart.put(location, loc2ExInStart);
		}
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
			 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEnd()- location);//距离本外显子终止  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStart() - location) - 1;// 距后一个外显子 nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	

	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 为实际数目
	 * 都不在为0
	 * @return
	 */
	private int getLocExInNumCis(int location) {
		if (hashCodInEleNum == null) {
			hashCodInEleNum = new HashMap<Integer, Integer>();
		}
		else if (hashCodInEleNum.containsKey(location)) {
			return hashCodInEleNum.get(location);
		}

		if (    location < getStart() || 
				location > getEnd()  )  	{
//			hashLocExInNum.put(location, 0);  //不在转录本内的坐标不用理会
			return 0;
		}
		for(int i = 0; i < lsElement.size(); i++)  //一个一个Exon的检查
		{
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 cood 3-1                 4-0  4-1               5
			if(location <= lsElement.get(i).getEnd() && location >= lsElement.get(i).getStart()) {
				hashCodInEleNum.put(location, i + 1);
				return i + 1;
			}
			// tss             0-0 0-1        1-0 1-1      2-0 2-1       3-0 3-1        cood         4-0  4-1               5
			else if(i<= lsElement.size() - 2 && location > lsElement.get(i).getEnd() && location < lsElement.get(i+1).getStart()) {
				hashCodInEleNum.put(location, -(i + 1));
				return -(i + 1);
			}
		}
		hashCodInEleNum.put(location, 0);
		return 0;
	}
	
	/**
	 * @param location 该点坐标在第几个外显子或内含子中
	 * @return
	 */
	private int getLocExInNumTans(int location) {
		if (hashCodInEleNum == null) {
			hashCodInEleNum = new HashMap<Integer, Integer>();
		}
		else if (hashCodInEleNum.containsKey(location)) {
			return hashCodInEleNum.get(location);
		}

		if (    location > getStart() || 
				location < getEnd()  )  	{
//			hashLocExInNum.put(location, 0);  //不在转录本内的坐标不用理会
			return 0;
		}
		for(int i = 0; i < lsElement.size(); i++)  //一个一个Exon的检查
		{
			//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			if(location >= lsElement.get(i).getEnd() && location <= lsElement.get(i).getStart()) {
				hashCodInEleNum.put(location, i + 1);
				return i + 1;
			}
			//		    5-1 cood 5-0    4-1 uag 4-0     3-1 cood 3-0         2-1 2-0    1-1 gta 1-0    0-1  cood 0-tss  cood
			else if(i <= lsElement.size() - 2 && location < lsElement.get(i).getEnd() && location > lsElement.get(i+1).getStart()) {
				hashCodInEleNum.put(location, -(i + 1));
				return -(i + 1);
			}
		}
		hashCodInEleNum.put(location, 0);
		return 0;
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
		Integer o1start = o1.getStart();
		Integer o2start = o2.getStart();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEnd();
			Integer o2end = o2.getEnd();
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
		Integer o1start = o1.getStart();
		Integer o2start = o2.getStart();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEnd();
			Integer o2end = o2.getEnd();
			return -o1end.compareTo(o2end);
		}
		return -comp;
	}
}