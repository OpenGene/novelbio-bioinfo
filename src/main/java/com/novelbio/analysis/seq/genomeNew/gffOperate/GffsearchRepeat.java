package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
/**
 * Repeat虽然有正反向，但是实际上好像是不分正反向的，那么我这里先不分正反向
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
		int[] distancetoLOCEnd=new int[2];//坐标到上一个，以及下一个条目终点的距离
		int[] distancetoLOCStart=new int[2];//坐标到上一个，以及下一个条目起点的距离
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailRepeat) Genlist.get(beginnum);
			GffRepeatInfo.LOCID[1]=beginnumlist.locString;//上个基因的ID
			GffRepeatInfo.begincis5to3=beginnumlist.cis5to3;//一直为正
			distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailRepeat) Genlist.get(endnum);
			GffRepeatInfo.LOCID[2]=endnumlist.locString;//下个基因的ID
			GffRepeatInfo.endcis5to3=endnumlist.cis5to3;//一直为正
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
		
		GffCGInfo.LOCID[0]=LOCdetial.locString;//本基因的ID
		
		GffCGInfo.begincis5to3=LOCdetial.cis5to3;//一直为正
		
		GffCGInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因起点的位置
		GffCGInfo.distancetoLOCStart[1]=-1;
		
		GffCGInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因起点的位置
		GffCGInfo.distancetoLOCEnd[1]=-1;
		
		return GffCGInfo;
	}

}
