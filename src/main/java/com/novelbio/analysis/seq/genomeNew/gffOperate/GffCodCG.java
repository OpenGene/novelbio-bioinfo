package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodCG extends GffCodAbs{

	GffCodCG(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 * �������ǰ/��û����Ӧ�Ļ�����ô��Ӧ��LOCIDΪnull
	 */
	protected  void SearchLOCoutside(ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{
		GffDetailCG beginnumlist=null;
		GffDetailCG endnumlist=null;		
		result=true;
		insideLOC=false;
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailCG) Genlist.get(beginnum);
			LOCID[1]=beginnumlist.locString;//�ϸ������ID
			begincis5to3=beginnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailCG) Genlist.get(endnum);
			LOCID[2]=endnumlist.locString;//�¸������ID
			endcis5to3=endnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberend);
			distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
	}
	
	/**
	 * ��λ�㴦�ڻ����ڲ�ʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	protected  void SearchLOCinside(ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetailCG LOCdetial=(GffDetailCG) Genlist.get(beginnum);		
		result=true;
		insideLOC=true;
		
		LOCID[0]=LOCdetial.locString;//�������ID
		begincis5to3=LOCdetial.cis5to3;//һֱΪ��
		
		distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//������������λ��
		distancetoLOCStart[1]=-1;
		
		distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//������������λ��
		distancetoLOCEnd[1]=-1;
	}

}
