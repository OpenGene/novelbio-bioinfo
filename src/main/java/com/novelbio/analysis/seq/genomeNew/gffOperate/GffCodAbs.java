package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * peak��λ������Ϣ�Ļ�����,����ֱ�ӿ�������CG��Peak<br>
 * ������GffCodInfoGene
 * 
 * @author zong0jie
 * 
 */
public abstract class GffCodAbs {

	GffHash gffHash = null;
	/**
	 * ���캯������ֵ
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
	 * ����Ⱦɫ��
	 * @return
	 */
	public String getChrID() {
		return chrID;
	}
	/**
	 * ���ؾ�������
	 * @return
	 */
	public int getCoord() {
		return Coordinate;
	}
	/**
	 * �����Ƿ�鵽 ���ҵ�/û�ҵ�
	 */
	protected boolean booFindCod = false;
	/**
	 * �Ƿ�ɹ��ҵ�cod
	 * @return
	 */
	public boolean findCod() {
		return booFindCod;
	}
	/**
	 * ��λ��� ��Ŀ��/��Ŀ��
	 */
	protected boolean insideLOC = false;
	/**
	 * ��λ��� ��Ŀ��/��Ŀ��
	 */
	public boolean locatInfo() {
		return insideLOC;
	}
	
	
	/**
	 * ��һ����Ŀ�ķ���
	 */
	private boolean upCis5to3 = false;
	/**
	 * ��һ����Ŀ�ķ���
	 */
	public boolean getUpCis5to3() {
		return upCis5to3;
	}
	
	/**
	 * ����Ŀ�ķ���
	 */
	private boolean thiscis5to3 = false;
	/**
	 * ����Ŀ�ķ���
	 */
	public boolean getThisCis5to3() {
		return thiscis5to3;
	}
	
	/**
	 * ��һ����Ŀ�ķ��򣬽�������λ����Ŀ��ʱ
	 */
	protected boolean downCis5to3 = false;
	/**
	 * ��һ����Ŀ�ķ��򣬽�������λ����Ŀ��ʱ
	 */
	public boolean getDownCis5to3() {
		return downCis5to3;
	}

	/**
	 * Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)
	 */
	private GffDetailAbs gffDetailUp = null;
	
	/**
	 *  �������Ŀ�ڣ�Ϊ����Ŀ�ľ�����Ϣ��û�ж�λ�ڻ�������Ϊnull<br>
	 */
	private GffDetailAbs gffDetailThis = null;
	
	/**
	 * Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ������)
	 */
	private GffDetailAbs gffDetailDown = null;
	
	
	/**
	 * ���ȿ��ϸ��������¸����� 0: �������Ŀ�ڣ�Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	protected int[] geneChrHashListNum = new int[2];

	/**
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * û�ҵ��ͷ���null
	 */
	protected void searchLocation() {
		ArrayList<GffDetailAbs> Loclist =  gffHash.getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			booFindCod = false;
		}
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition();// ���ַ�����peaknum�Ķ�λ
		if (locInfo[0] == 1) // ��λ�ڻ�����
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
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition() {
		ArrayList<GffDetailAbs> Loclist =  gffHash.getChrhash().get(chrID);// ĳһ��Ⱦɫ�����Ϣ
		if (Loclist == null) {
			booFindCod = false;
			return null;
		}
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < Loclist.get(beginnum).getNumStart()) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// �����һ��Item֮��
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
		if (Coordinate <= Loclist.get(beginnum).getNumStart())// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		return LocInfo;
	}
}
