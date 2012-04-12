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
	 * �����Ƿָ�ͬһ��element�Ķ��name�ķ��ţ���ȷ��
	 */
	public static final String SEP = "/";

	/**
	 * ����ĳ����������ڵ�element��Ŀ,
	 * value: ������element�У�
	 * ����������element֮��
	 */
	HashMap<Integer, Double> hashCodInEleNum;
	/**
	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
	 */
	HashMap<Double, Double> hashLocExInStart;

	/**
	 * ����ĳ�����굽���ڵ��ں���/�������յ�ľ���
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
	 * û�з����򷵻�null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
 	}
	
	/**
	 * ��list�е�Ԫ�ؽ����������element���� start > end����ô�ʹӴ�С����
	 * ���element����start < end����ô�ʹ�С��������
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
	 * ����ʵ�ʵ�num��element�����ĳ���
	 * @param num ʵ����Ŀ
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
	 * ����ʵ�ʵ�num��element�ĳ���
	 * @param num ʵ����Ŀ
	 * @return
	 */
	public double getEleLen(int num)
	{
		return get(num-1).getLen();
	}
	/**
	 * ��ʱ�����
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
	 * �����η������������η��ظ���
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
	 * �����η������������η��ظ���
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
	 * �����loc�Ƿ��ڱ�list�ķ�Χ��
	 * @return
	 */
	protected boolean isLocInside(int loc) {
		if (loc >= Math.max(getStart(), getEnd()) || loc <= Math.min(getStart(), getEnd())) {
			return false;
		}
		return true;
	}
	/**
	 * �����loc�Ƿ���Start������
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
	 * �����loc�Ƿ���Start������
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
	 * ��������֮��ľ��룬��������������mRNA����ľ��룬Ҳ����ֻ����ele�ϵľ��롣
	 * �������ص�ʱ������0
	 * ��loc1��loc2����ʱ��������������loc1��loc2����ʱ�����ظ���
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�GffCodAbs.LOC_ORIGINAL
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
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
			//double���ﲻ��Ҫ��һ
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
//	 * ֻ����cis���ڵ�ʱ�����ʹ��
//	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
//	 * ��loc����ʱnumΪ����
//	 * ���num Bp���û�л����ˣ��򷵻�-1��
//	 * @param mRNAnum
//	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
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
//					// һ��һ�������ӵ���ǰ����
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
//					// һ��һ�������ӵ���ǰ����
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
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
	 * LnnnnNΪ5λ
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
					// һ��һ�������ӵ���ǰ����
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						//����Ҫ��һ���һ
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
					// һ��һ�������ӵ���ǰ����
					if (remain - tmpExon.getLen() > 0) {
						remain = remain - tmpExon.getLen();
						continue;
					}
					else {
						//����Ҫ��һ���һ
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
	 * ���������趨ListAbs�ķ��򣬲��Ҹ÷�������ڲ���element�ķ���Ҫһ��
	 * ���굽element ������
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
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
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - get(NumExon).getStartCis());//���뱾��������ʼ nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getStartAbs());//���뱾��������ʼ nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			//����Ҫ��һ���һ
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - get(NumExon).getEndCis());// ��ǰһ�������� NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getEndAbs());// ��ǰһ�������� NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * ���굽element �յ����
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
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
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				 loc2ExInEnd = Math.abs(get(NumExon).getEndCis()- location);//���뱾��������ֹ  Cnnnnnnn
			else
				 loc2ExInEnd = Math.abs(get(NumExon).getEndAbs()- location);//���뱾��������ֹ  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartCis() - location);// ���һ�������� nnCnnnnN
			else
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartAbs() - location);// ���һ�������� nnCnnnnN
			
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	

	
	/**
	 * �������element�ĳ���֮��
	 */
	public double getListLen() {
		double isoLen = 0;
		for (E exons : this) {
			isoLen = isoLen + exons.getLen();
		}
		return isoLen;
	}
	
	/**
	 * ���αȽ�����list�е�Ԫ���Ƿ�һ�¡��ڲ�����ÿ��Ԫ�ص�equals����
	 * ���Ƚ�name�������Ҫ�Ƚ�name����ô����equal
	 * ��ʱ��û��дequal
	 * �����ӱȽ����һģһ���򷵻�true��
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
	 * ����ÿ��ID��Ӧ�ľ���element
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
	 * ����ÿ��ID��Ӧ��Num
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
	 * ����ÿ��ID��Ӧ�ľ���element
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
	 * ���ر�ListAbs�е�����string����\
	 * �������Item���ص��ģ�����ListAbs.SEP������
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
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���<br>
	 * 3���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
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
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 * @return
	 */
	public int searchLocInEleNum(double location) {
		return LocPosition(location)[3];
	}
	
}




class BinarySearchDouble
{
	
 
	

	/**
	 * ����֤
	 * ǰ�պ�����
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
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
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < lsElement.get(beginnum).getStartCis()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
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
		if (Coordinate <= lsElement.get(beginnum).getEndCis())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		else if (Coordinate >= lsElement.get(endnum).getStartCis())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[1] = endnum;
			LocInfo[2] = endnum + 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
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
		// �ڵ�һ��Item֮ǰ
		if (Coordinate > lsElement.get(beginnum).getStartCis()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
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
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) 
		{// location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[1] = endnum;
			LocInfo[2] = endnum + 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}
	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3��������һ����ǩ���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
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
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
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
		if (Coordinate <= lsElement.get(beginnum).getEndAbs())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[3] = LocInfo[1] + 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		LocInfo[3] = -LocInfo[1] - 1;
		return LocInfo;
	}
	
}
/**
 * ��С��������
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
 * ��С���������þ�������ֵ����
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
 * �Ӵ�С����
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