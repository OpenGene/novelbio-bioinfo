package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.database.entity.kegg.noUseKGCentry2Ko2Gen;

/**
 * ˫����
 * @author zong0jie
 *
 */
public abstract class GffCodAbsDu {
	//�����������ھ���������½�
	GffCodAbs gffCodAbs1 = null;
	GffCodAbs gffCodAbs2 = null;
	//������˵��gffdetail
	GffDetail gffDetail1 = null;
	//�����Ҷ˵��gffdetail
	GffDetail gffDetail2 = null;
	//�����˵�֮���gffdetail
	ArrayList<GffDetail> lsgffDetailsMid = new ArrayList<GffDetail>();
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
	public abstract GffCodAbs getGffCodLeft();
	/**
	 * ������˵�GffCod�����ǳ���Ӧ��GffCod��
	 * @return
	 */
	public abstract GffCodAbs getGffCodRight();
	/**
	 * �������������м���ŵĵ�GffDetail�����ǳ���Ӧ��GffDetail��
	 * @return
	 */
	public abstract ArrayList<GffDetail> getLsGffDetailMid();
	
	/**
	 * ˫������� ������ص�GffHash�࣬Ȼ����������Ϣ<br>
	 */
	public void searchLocation(GffHash gffHash) {
		Hashtable<String, ArrayList<GffDetail>> LocHash = gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(gffCodAbs1.getChrID().toLowerCase());// ĳһ��Ⱦɫ�����Ϣ
		gffCodAbs1.searchLocation(gffHash);
		gffCodAbs2.searchLocation(gffHash);
		/**
		 * ���peak�����˵㶼����ͬһ��Ŀ֮��
		 */
		if (gffCodAbs1.geneChrHashListNum[0] == gffCodAbs2.geneChrHashListNum[0]
				&& gffCodAbs1.insideLOC && gffCodAbs2.insideLOC) 
		{
			gffDetail1= Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			int thisDetailLength = gffDetail1.numberend - gffDetail1.numberstart;
			int peakLength = gffCodAbs1.getCoord() - gffCodAbs2.getCoord();
			opLeftInItem = 100 * (double) peakLength / thisDetailLength;
			opLeftInCod = 100;
			opLeftBp = peakLength; opRightInItem = opLeftInItem; opRightInCod = 100; opRightBp = opLeftBp;
		}
		// ���peak��˵���һ����Ŀ�ڣ��Ҷ˵�����һ����Ŀ��
		else if (gffCodAbs1.insideLOC
				&& gffCodAbs2.insideLOC
				&& gffCodAbs1.geneChrHashListNum[0] != gffCodAbs2.geneChrHashListNum[0]) 
		{
			gffDetail1 = Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			gffDetail2 = Loclist.get(gffCodAbs2.geneChrHashListNum[0]);
			int leftItemLength = gffDetail1.numberend - gffDetail1.numberstart;
			int rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;

			int leftoverlap = gffDetail1.numberend - gffCodAbs1.getCoord();
			int rightoverlap = gffCodAbs2.getCoord() - gffDetail2.numberstart;
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();

			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// peakֻ����˵�����Ŀ��
		else if (gffCodAbs1.insideLOC && !gffCodAbs2.insideLOC) {
			gffDetail1= Loclist.get(gffCodAbs1.geneChrHashListNum[0]);
			int leftItemLength = gffDetail1.numberend
					- gffDetail1.numberstart;
			int leftoverlap = gffDetail1.numberend - gffCodAbs1.getCoord();
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();

			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 0; opRightInCod = 0; opRightBp = 0;
		}
		// peakֻ���Ҷ˵�����Ŀ��
		else if (!gffCodAbs1.insideLOC && gffCodAbs2.insideLOC) {
			gffDetail2 = Loclist.get(gffCodAbs2.geneChrHashListNum[0]);
			int rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;
			int rightoverlap = gffCodAbs2.getCoord() - gffDetail2.numberstart;
			int peakLength = gffCodAbs2.getCoord() - gffCodAbs1.getCoord();
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// //////////////////////////////////////////////������������Լ��м���ŵ���Ŀ��ע�ⲻ�����������ڵĻ���///////////////////////////////////////////////////////////////////////////////
		// ���������ID�Լ� ֮����Ŀ��ID��ֹ����--------------
		// coordinate1(Cod1ID)----------ItemA(startID)-------ItemB-------ItemC------ItemD(endID)-------coordinate2(Cod2ID)-----------------
		int Cod1ID = -1, Cod2ID = -1;
		Cod1ID = gffCodAbs1.geneChrHashListNum[0];// ����Ŀ/�ϸ���Ŀ���
		if (gffCodAbs2.insideLOC)
			Cod2ID = gffCodAbs2.geneChrHashListNum[0];// ����Ŀ���
		else
			Cod2ID = gffCodAbs2.geneChrHashListNum[1];// �¸���Ŀ���
		if ((Cod2ID - Cod1ID) > 1)// ��Cod1ID��Cod2ID֮���������Ŀ��GffDetailװ��LstGffCodInfo
		{
			for (int i = 1; i < (Cod2ID - Cod1ID); i++) {
				GffDetail gffDetail = Loclist.get(Cod1ID + i);
				lsgffDetailsMid.add(gffDetail);
			}
		}
	}
}
