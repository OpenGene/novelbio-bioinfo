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
	 * ����ĳ����������ڵ�element��Ŀ,
	 * value: ������element�У�
	 * ����������element֮��
	 */
	HashMap<Integer, Integer> hashCodInEleNum;

	/**
	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
	 */
	HashMap<Integer, Integer> hashLocExInStart;

	/**
	 * ����ĳ�����굽���ڵ��ں���/�������յ�ľ���
	 */
	HashMap<Integer, Integer> hashLocExInEnd;
	
	/**
	 * ���뱣֤������װ��
	 */
	protected ArrayList<T> lsElement = new ArrayList<T>();
	Boolean cis5to3 = null;
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
	 * ����ʵ�ʵ�num��element�����ĳ���
	 * @param num ʵ����Ŀ
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
	 * ����ʵ�ʵ�num��element�ĳ���
	 * @param num ʵ����Ŀ
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
	 * �����η������������η��ظ���
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
	 * �����η������������η��ظ���
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
	 * ��������֮��ľ��룬��������������mRNA����ľ��룬Ҳ����ֻ����ele�ϵľ��롣
	 * ��loc1��loc2����ʱ��������������loc1��loc2����ʱ�����ظ���
	 * Ҫ�����������궼��exon��.��������ϣ��򷵻�GffCodAbs.LOC_ORIGINAL
	 * @param loc1 ��һ������
	 * @param loc2 �ڶ�������
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
	 * ֻ����cis���ڵ�ʱ�����ʹ��
	 * ���ؾ���loc��num Bp�����꣬��mRNA���棬��loc����ʱnum Ϊ����
	 * ��loc����ʱnumΪ����
	 * ���num Bp���û�л����ˣ��򷵻�-1��
	 * @param mRNAnum
	 * NnnnLoc Ϊ-4λ����N��Loc�غ�ʱΪ0
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
					// һ��һ�������ӵ���ǰ����
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
					// һ��һ�������ӵ���ǰ����
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
	 * ���������趨ListAbs�ķ��򣬲��Ҹ÷�������ڲ���element�ķ���Ҫһ��
	 * ���굽element ������
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
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
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStartCis());//���뱾��������ʼ nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStartAbs());//���뱾��������ʼ nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEndCis()) -1;// ��ǰһ�������� NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEndAbs()) -1;// ��ǰһ�������� NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * ���굽element �յ����
	 * @param location ����
	 *  * �õ�����������Ϊ���������ں�����Ϊ������Ϊʵ����Ŀ
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
		int NumExon = Math.abs(exIntronNum) - 1; //ʵ��������ȥ1���������øñ�������
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEndCis()- location);//���뱾��������ֹ  Cnnnnnnn
			else
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEndAbs()- location);//���뱾��������ֹ  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStartCis() - location) - 1;// ���һ�������� nnCnnnnN
			else
				 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStartAbs() - location) - 1;// ���һ�������� nnCnnnnN
			
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	

	
	/**
	 * �������element�ĳ���֮��
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
	 * �����ӱȽ����һģһ���򷵻�true��
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
	 * ����ÿ��ID��Ӧ�ľ���element
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
	 * ����ÿ��ID��Ӧ��Num
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
	 * ����ÿ��ID��Ӧ�ľ���element
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
	 * ���ر�ListAbs�е�����string����\
	 * �������Item���ص��ģ�����ListAbs.SEP������
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
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���<br>
	 * 3���õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
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
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * ����Ϊ0
	 * Ϊʵ����Ŀ
	 * @return
	 */
	public int getLocInEleNum(int location) {
		return LocPosition(location)[3];
	}
	
	
	
	
	
	
	
	
}




class BinarySearch
{
	
 
	

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
	protected static int[] LocPositionCis(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
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
	protected static int[] LocPositionTran(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
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
		if (Coordinate >= lsElement.get(beginnum).getEndCis())// ��֪���᲻�����PeakNumber��biginnumС�����
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
	protected static int[] LocPositionAbs(ArrayList<? extends ElementAbs> lsElement, int Coordinate) {
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
 * ��С���������þ�������ֵ����
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
 * �Ӵ�С����
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