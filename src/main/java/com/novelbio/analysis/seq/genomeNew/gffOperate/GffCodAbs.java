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
	/**
	 * ���캯������ֵ
	 */
	protected  GffCodAbs(String chrID, int Coordinate) {
		this.chrID = chrID;
		this.Coordinate = Coordinate;
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
	 * Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)
	 */
	protected GffDetailAbs gffDetailUp = null;
	/**
	 * ֻ��geneDetail�õ�
	 * ����ϸ���Ŀ�ľ�����Ϣ��������
	 * return (GffDetailAbs)gffDetailUp;
	 * @return
	 */
	public abstract GffDetailAbs getGffDetailUp();

	/**
	 *  �������Ŀ�ڣ�Ϊ����Ŀ�ľ�����Ϣ��û�ж�λ�ڻ�������Ϊnull<br>
	 */
	protected GffDetailAbs gffDetailThis = null;
	/**
	 * ֻ��geneDetail�õ�
	 * ��ñ���Ŀ�ľ�����Ϣ�������¡�
	 * �������ĿΪnull��˵��������Ŀ��
	 * return (GffDetailAbs)gffDetailThis;
	 * @return
	 */
	public abstract GffDetailAbs getGffDetailThis();
	/**
	 * ֻ��geneDetail�õ�
	 * Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ������)
	 */
	protected GffDetailAbs gffDetailDown = null;
	/**
	 * ֻ��geneDetail�õ�
	 * ��ñ���Ŀ�ľ�����Ϣ��������
	 * return (GffDetailAbs)gffDetailDown;
	 * @return
	 */
	public abstract GffDetailAbs getGffDetailDown();
	
	/**
	 * �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 */
	protected int ChrHashListNumUp = -1;
	/**
	 * �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 */
	public int getItemNumUp() {
		return ChrHashListNumUp;
	}
	/**
	 * Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�����ڣ���Ϊ-1<br>
	 */
	protected int ChrHashListNumThis = -1;
	/**
	 * Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�����ڣ���Ϊ-1<br>
	 */
	public int getItemNumThis() {
		return ChrHashListNumThis;
	}
	/**
	 * Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	protected int ChrHashListNumDown = -1;
	/**
	 * Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	public int getItemNumDown() {
		return ChrHashListNumDown;
	}
	
	
}
