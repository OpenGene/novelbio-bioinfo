package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;

/**
 *  ����ĳ������λ�㷵�ؾ����LOC����Լ���λ
  * ע�⣬������ȫ�ǽ�����GffHash��Ļ����ϵģ�����
  * ����Ҫ��GffHash���֧�ֲ��ܹ�����Ҳ����˵����������GffHash��ķ�����ȡGff�ļ���
 * CG�����������
  * ������Ҫʵ����
 * @author zong0jie
 */
public class GffsearchCG extends Gffsearch
{
	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 * �������ǰ/��û����Ӧ�Ļ�����ô��Ӧ��LOCIDΪnull
	 */
	protected  GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{
		GffDetailCG beginnumlist=null;
		GffDetailCG endnumlist=null;
		GffCodInfo GffCGInfo=new GffCodInfo();
		
		GffCGInfo.result=true;
		GffCGInfo.insideLOC=false;
		int[] distancetoLOCEnd=new int[2];//���굽��һ�����Լ���һ����Ŀ�յ�ľ���
		int[] distancetoLOCStart=new int[2];//���굽��һ�����Լ���һ����Ŀ���ľ���
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailCG) Genlist.get(beginnum);
			GffCGInfo.LOCID[1]=beginnumlist.locString;//�ϸ������ID
			GffCGInfo.begincis5to3=beginnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailCG) Genlist.get(endnum);
			GffCGInfo.LOCID[2]=endnumlist.locString;//�¸������ID
			GffCGInfo.endcis5to3=endnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberend);
			distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
		
		
		GffCGInfo.distancetoLOCEnd=distancetoLOCEnd;
		GffCGInfo.distancetoLOCStart=distancetoLOCStart;
		
		return GffCGInfo;
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
		GffDetailCG LOCdetial=(GffDetailCG) Genlist.get(beginnum);
		GffCodInfo GffCGInfo=new GffCodInfo();
		
		GffCGInfo.result=true;
		GffCGInfo.insideLOC=true;
		
		GffCGInfo.LOCID[0]=LOCdetial.locString;//�������ID
		
		GffCGInfo.begincis5to3=LOCdetial.cis5to3;//һֱΪ��
		
		GffCGInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//������������λ��
		GffCGInfo.distancetoLOCStart[1]=-1;
		
		GffCGInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//������������λ��
		GffCGInfo.distancetoLOCEnd[1]=-1;
		
		return GffCGInfo;
	}
}
