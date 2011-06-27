package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
/**
 * Repeat��Ȼ�������򣬵���ʵ���Ϻ����ǲ���������ģ���ô�������Ȳ���������
 * @author zong0jie
 *
 */
public class GffsearchRepeat extends Gffsearch{

	@Override
	protected GffCodInfo SearchLOCoutside(int Coordinate, ArrayList<GffDetailAbs> Genlist, int beginnum, int endnum) {

		GffDetailRepeat endnumlist=null;
		GffDetailRepeat beginnumlist=null;
		GffCodInfo GffRepeatInfo=new GffCodInfo();
		
		GffRepeatInfo.result=true;
		GffRepeatInfo.insideLOC=false;
		int[] distancetoLOCEnd=new int[2];//���굽��һ�����Լ���һ����Ŀ�յ�ľ���
		int[] distancetoLOCStart=new int[2];//���굽��һ�����Լ���һ����Ŀ���ľ���
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailRepeat) Genlist.get(beginnum);
			GffRepeatInfo.LOCID[1]=beginnumlist.locString;//�ϸ������ID
			GffRepeatInfo.begincis5to3=beginnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailRepeat) Genlist.get(endnum);
			GffRepeatInfo.LOCID[2]=endnumlist.locString;//�¸������ID
			GffRepeatInfo.endcis5to3=endnumlist.cis5to3;//һֱΪ��
			distancetoLOCEnd[1]=Math.abs(Coordinate-endnumlist.numberend);
			distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
		
		GffRepeatInfo.distancetoLOCEnd=distancetoLOCEnd;
		GffRepeatInfo.distancetoLOCStart=distancetoLOCStart;
		return GffRepeatInfo;
	}

	@Override
	protected GffCodInfo SearchLOCinside(int Coordinate, ArrayList<GffDetailAbs> Genlist, int beginnum, int endnum) {

		GffDetailRepeat LOCdetial=(GffDetailRepeat) Genlist.get(beginnum);
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
