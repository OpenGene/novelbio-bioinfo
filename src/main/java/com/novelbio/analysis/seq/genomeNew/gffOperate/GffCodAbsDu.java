package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;

/**
 * ˫����
 * @author zong0jie
 *
 */
public abstract class GffCodAbsDu<T extends GffDetailAbs, K extends GffCodAbs<T>>  {
	//�����������ھ���������½�
	
	public GffCodAbsDu(ArrayList<T> lsgffDetail, K gffCod1, K gffCod2)
	{
		this.lsgffDetailsMid = lsgffDetail;
		this.gffCod1 = gffCod1;
		this.gffCod2 = gffCod2;
		calInfo();
	}

	K gffCod1 = null;
	K gffCod2 = null;	
	
	//�����˵�֮���gffdetail
	ArrayList<T> lsgffDetailsMid = new ArrayList<T>();
	/**
	 * peak�����Item����ʱ�����������Item����ռ�ı���
	 */
	double opLeftInItem = -1;
	/**
	 * peak�����Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��
	 */
	double opLeftInCod = -1;
	/**
	 * peak�����Item����ʱ��ʵ�ʽ�����bp��
	 */
	int opLeftBp = -1;
	/**
	 * peak���Ҷ�Item����ʱ�����������Item����ռ�ı���
	 */
	double opRightInItem = -1;
	/**
	 * peak���Ҷ�Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��
	 */
	double opRightInCod = -1;
	/**
	 * peak���Ҷ�Item����ʱ��ʵ�ʽ�����bp��
	 */
	int opRightBp = -1;
	/**
	 *  peak�����Item����ʱ�����������Item����ռ�ı���
	 * @return
	 */
	public double getOpLeftInItem() {
		return opLeftInItem;
	}
	/**
	 * peak���Ҷ�Item����ʱ�����������Item����ռ�ı���
	 * @return
	 */
	public double getOpRightInItem() {
		return opRightInItem;
	}
	/**
	 *  peak�����Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��
	 * @return
	 */
	public double getOpLeftInCod() {
		return opLeftInCod;
	}
	/**
	 *  peak���Ҷ�Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��
	 * @return
	 */
	public double getOpRightInCod() {
		return opRightInCod;
	}
	/**
	 *  peak�����Item����ʱ��ʵ�ʽ�����bp��
	 * @return
	 */
	public int getOpLeftBp() {
		return opLeftBp;
	}
	/**
	 *  peak���Ҷ�Item����ʱ��ʵ�ʽ�����bp��
	 * @return
	 */
	public int getOpRightBp() {
		return opRightBp;
	}
	/**
	 * ������˵�GffCod�����ǳ���Ӧ��GffCod��
	 * @return
	 */
	public K getGffCodLeft()
	{
		return gffCod1;
	}
	/**
	 * �����Ҷ˵�GffCod�����ǳ���Ӧ��GffCod��
	 * @return
	 */
	public K getGffCodRight()
	{
		return gffCod2;
	}
	/**
	 * �������������м���ŵĵ�GffDetail�����ǳ���Ӧ��GffDetail��
	 * @return
	 */
	public ArrayList<T> getLsGffDetailMid()
	{
		return lsgffDetailsMid;
	}
	/**
	 * ����ȫ��������gffDetail��Ϣ
	 * @return
	 */
	public ArrayList<T> getAllGffDetail() {
		ArrayList<T> lsGffDetailAll = new ArrayList<T>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp())
				lsGffDetailAll.add(getGffCodLeft().getGffDetailUp());
			lsGffDetailAll.add(getGffCodLeft().getGffDetailThis());
		}
		if (lsgffDetailsMid != null) {
			for (T t : lsgffDetailsMid) {
				lsGffDetailAll.add(t);
			}
		}
		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			if (getGffCodRight().isInsideDown())
				lsGffDetailAll.add(getGffCodRight().getGffDetailDown());
			lsGffDetailAll.add(getGffCodRight().getGffDetailThis());
		}
		return lsGffDetailAll;
	}
	/**
	 * ˫������� ������ص�GffHash�࣬Ȼ����������Ϣ<br>
	 */
	private void calInfo() {
		T gffDetail1 = gffCod1.getGffDetailThis();
		T gffDetail2 = gffCod2.getGffDetailThis();
		int leftItemLength = 0; int leftoverlap = 0;
		int rightItemLength = 0; int rightoverlap = 0;
		int peakLength = gffCod2.getCoord() - gffCod1.getCoord();
		if (gffDetail1 != null) {
			leftItemLength = gffDetail1.numberend - gffDetail1.numberstart;
			leftoverlap = gffDetail1.numberend - gffCod1.getCoord();
		}
		if (gffDetail2 != null) {
			rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;
			rightoverlap = gffCod2.getCoord() - gffDetail2.numberstart;
		}
		/**
		 * ���peak�����˵㶼����ͬһ��Ŀ֮��
		 */
		if (gffCod1.insideLOC && gffCod2.insideLOC && 
				gffDetail1.equals(gffDetail2)
		)
		{
			opLeftInItem = 100 * (double) peakLength / leftItemLength;
			opLeftInCod = 100;
			opLeftBp = peakLength; opRightInItem = opLeftInItem; opRightInCod = 100; opRightBp = opLeftBp;
		}
		// ���peak��˵���һ����Ŀ�ڣ��Ҷ˵�����һ����Ŀ��
		else if (gffCod1.insideLOC
				&& gffCod2.insideLOC
				&& !gffCod1.equals(gffCod2) )
		{
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// peakֻ����˵�����Ŀ��
		else if (gffCod1.insideLOC && !gffCod2.insideLOC) {
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 0; opRightInCod = 0; opRightBp = 0;
		}
		// peakֻ���Ҷ˵�����Ŀ��
		else if (!gffCod1.insideLOC && gffCod2.insideLOC) {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}else {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 0;
			opRightInCod = 0;
			opRightBp = 0;
		}
	}
	
}
