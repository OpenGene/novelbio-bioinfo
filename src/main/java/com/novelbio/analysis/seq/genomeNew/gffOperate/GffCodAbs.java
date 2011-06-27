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
		distancetoLOCStart[0] = -1000000000;
		distancetoLOCEnd[0] = -1000000000;
		geneChrHashListNum[0] = -1000000000;
		distancetoLOCStart[1] = -1000000000;
		distancetoLOCEnd[1] = -1000000000;
		geneChrHashListNum[1] = -1000000000;
		this.chrID = chrID;
		this.Coordinate = Coordinate;
		this.gffHash = gffHash;
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
	protected boolean result = false;
	/**
	 * �Ƿ�ɹ��ҵ�cod
	 * @return
	 */
	public boolean findCod() {
		return result;
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
	 * ����Ŀ/��һ����Ŀ�ķ���
	 */
	protected boolean begincis5to3 = false;
	/**
	 * ����Ŀ/��һ����Ŀ�ķ���
	 */
	public boolean getBiginCis5to3() {
		return begincis5to3;
	}
	
	/**
	 * ��һ����Ŀ�ķ��򣬽�������λ����Ŀ��ʱ
	 */
	protected boolean endcis5to3 = false;

	/**
	 * ����LOCID��ΪchrHash����ı�ţ�ע�⣺����Ų�һ����LOCIDlist��ı����ͬ��Ŀǰ����UCSCgene�в�ͬ��
	 * UCSCgeneҪ��ͨ��split("/")�и���ܽ���locHashtable���� �� 0������Ŀ��� 1: �ϸ���Ŀ��� 2���¸���Ŀ���
	 * �������ǰ/��û����Ӧ�Ļ���(Ʃ����������ǰ��)����ô��Ӧ��LOCIDΪnull
	 */
	protected String[] LOCID = new String[3];

	/**
	 * ���굽��Ŀ����λ��,����������<br/>
	 * Ϊint[2]��<br>
	 * <b>�������Ŀ��</b><br>
	 * 0:����Ϊ�ͱ���Ŀ���ľ��룬��������<br>
	 * 1��-1<br>
	 * <br>
	 * <b>�������Ŀ�䣬����������Ŀ�ľ��룬�������û����/����Ŀ������Ӧ��Ϊ0</b><br>
	 * 0:������ϸ���Ŀ���ľ���<br>
	 * ����ϸ���ĿΪ������Ϊ����+<br>
	 * ����ϸ���ĿΪ������Ϊ����-<br>
	 * <br>
	 * 1:������¸���Ŀ���ľ���<br>
	 * ����¸�����Ϊ������Ϊ����-<br/>
	 * ����¸�����Ϊ������Ϊ����+<br/>
	 */
	protected int[] distancetoLOCStart = new int[2];

	/**
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * Ϊint[2]��<br>
	 * <b>�������Ŀ��</b><br>
	 * 0:����Ϊ�ͱ���Ŀ�յ�ľ��룬��������<br>
	 * 1��-1<br>
	 * <br>
	 * <b>�������Ŀ�䣬������/����Ŀ�ľ��룬�������û����/����Ŀ������Ӧ��Ϊ0
	 * ������Ҫ����û����/����Ŀ����geneChrHashListNum���������ӦֵΪ-1����˵��û�и���</b><br>
	 * 0:������ϸ���Ŀ�յ�ľ���<br>
	 * ����ϸ���ĿΪ������Ϊ����-<br>
	 * ����ϸ���ĿΪ������Ϊ����+<br>
	 * <br>
	 * 1:������¸���Ŀ�յ�ľ���<br>
	 * ����¸���ĿΪ������Ϊ����+<br/>
	 * ����¸���ĿΪ������Ϊ����-<br/>
	 */
	protected int[] distancetoLOCEnd = new int[2];

	/**
	 * 0: �������Ŀ�ڣ�Ϊ����Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ������)
	 */
	protected GffDetailAbs[] geneDetail = new GffDetailAbs[2];

	/**
	 * ���ȿ��ϸ��������¸����� 0: �������Ŀ�ڣ�Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	protected int[] geneChrHashListNum = new int[2];

	

}
