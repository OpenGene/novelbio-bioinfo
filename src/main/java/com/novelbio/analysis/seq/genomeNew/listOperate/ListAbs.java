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
	 * ��list�е�Ԫ�ؽ����������element���� start > end����ô�ʹӴ�С����
	 * ���element����start < end����ô�ʹ�С��������
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
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
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
		return lsElement.get(0).getStart();
	}
	
	public int getEnd()
	{
		return lsElement.get(lsElement.size() - 1).getEnd();
	}
	
	/**
	 * ��������֮��ľ��룬mRNA���棬��loc1��loc2����ʱ��������������loc1��loc2����ʱ�����ظ���
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
			loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getStart());//���뱾��������ʼ nnnnnnnnC
			hashLocExInStart.put(location, loc2ExInStart);
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			loc2ExInStart = Math.abs(location - lsElement.get(NumExon).getEnd()) -1;// ��ǰһ�������� NnnnCnnnn
			hashLocExInStart.put(location, loc2ExInStart);
		}
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
			 loc2ExInEnd = Math.abs(lsElement.get(NumExon).getEnd()- location);//���뱾��������ֹ  Cnnnnnnn
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			 loc2ExInEnd = Math.abs(lsElement.get(NumExon+1).getStart() - location) - 1;// ���һ�������� nnCnnnnN
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	
	

	/**
	 * �õ�����������Ϊ���������ں�����Ϊ����
	 * Ϊʵ����Ŀ
	 * ������Ϊ0
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
//			hashLocExInNum.put(location, 0);  //����ת¼���ڵ����겻�����
			return 0;
		}
		for(int i = 0; i < lsElement.size(); i++)  //һ��һ��Exon�ļ��
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
	 * @param location �õ������ڵڼ��������ӻ��ں�����
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
//			hashLocExInNum.put(location, 0);  //����ת¼���ڵ����겻�����
			return 0;
		}
		for(int i = 0; i < lsElement.size(); i++)  //һ��һ��Exon�ļ��
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
 * ��С��������
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
 * �Ӵ�С����
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