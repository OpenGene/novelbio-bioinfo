package com.novelbio.base.genome.gffOperate;

import java.util.ArrayList;

public class GffsearchPeak extends Gffsearch{


	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	protected  GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{
		GffDetail endnumlist=null;
		GffDetail beginnumlist= null;
		
		GffCodInfo gffPeakInfo=new GffCodInfo();
		
		gffPeakInfo.result=true;
		gffPeakInfo.insideLOC=false;
		
		if (beginnum!=-1) {
			beginnumlist= Genlist.get(beginnum);
			gffPeakInfo.LOCID[1]=beginnumlist.locString;//�ϸ������ID
			gffPeakInfo.begincis5to3=beginnumlist.cis5to3;//һֱΪ��
			 //��ǰһ������ת¼�����յ�ľ���
			if(gffPeakInfo.begincis5to3)
	        {//����������ʱ����TSS����Ϊ��������EndΪ����        |>----->------*
				gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
				gffPeakInfo.distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
	        }
	        else
	        {//��������ʱ����TSS����Ϊ��������EndΪ����   <-------<|----*
	        	gffPeakInfo.distancetoLOCStart[0]=-Math.abs(beginnumlist.numberend-Coordinate);
	        	gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberstart);
	        }
		}
		if (endnum!=-1) {
			endnumlist=Genlist.get(endnum);
			gffPeakInfo.LOCID[2]=endnumlist.locString;//�¸������ID
			gffPeakInfo.endcis5to3=endnumlist.cis5to3;//һֱΪ��
			//���һ������ת¼�����յ�ľ���
	        if(gffPeakInfo.endcis5to3)
	        {//����������ʱ����TSS����Ϊ��������EndΪ����         *---|>----->----
	        	gffPeakInfo.distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
	        	gffPeakInfo.distancetoLOCEnd[1]=Math.abs(Coordinate-endnumlist.numberend);
	        }
	        else
	        {//��������ʱ����TSS����Ϊ��������EndΪ����        *----<-------<|
	        	gffPeakInfo.distancetoLOCStart[1]=Math.abs(endnumlist.numberend-Coordinate);
	        	gffPeakInfo.distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberstart);
	        }	
		}
		return gffPeakInfo;
	}
	
	/**
	 * ��λ�㴦�ڻ����ڲ�ʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	protected  GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetail LOCdetial=Genlist.get(beginnum);
		GffCodInfo gffPeakInfo=new GffCodInfo();
		
		gffPeakInfo.result=true;
		gffPeakInfo.insideLOC=true;
		
		gffPeakInfo.LOCID[0]=LOCdetial.locString;//�������ID
		
		gffPeakInfo.begincis5to3=LOCdetial.cis5to3;//һֱΪ��
		
		
		gffPeakInfo.LOCID[0]=LOCdetial.locString;//�������ID
		if(LOCdetial.cis5to3)
		{
			gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//��������ʵ������λ��
			gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//��������ʵ���յ��λ��
		}	
		else {
			gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//��������ʵ������λ��
			gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//��������ʵ���յ��λ��
		}
		gffPeakInfo.distancetoLOCStart[1]=-1;
		gffPeakInfo.distancetoLOCEnd[1]=-1;
		
		return gffPeakInfo;
	}


}
