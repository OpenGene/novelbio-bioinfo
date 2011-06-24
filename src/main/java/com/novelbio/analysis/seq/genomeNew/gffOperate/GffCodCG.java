package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

public class GffCodCG extends GffCodAbs{

	GffCodCG(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 当位点处于基因外部时的具体查找,返回GffCodInfo实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 * 如果坐标前/后没有相应的基因，那么相应的LOCID为null
	 */
	protected  void SearchLOCoutside(ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{
		GffDetailCG beginnumlist=null;
		GffDetailCG endnumlist=null;		
		result=true;
		insideLOC=false;
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailCG) Genlist.get(beginnum);
			LOCID[1]=beginnumlist.locString;//上个基因的ID
			begincis5to3=beginnumlist.cis5to3;//一直为正
			distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailCG) Genlist.get(endnum);
			LOCID[2]=endnumlist.locString;//下个基因的ID
			endcis5to3=endnumlist.cis5to3;//一直为正
			distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberend);
			distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
	}
	
	/**
	 * 当位点处于基因内部时的具体查找,返回GffCodInfo实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	protected  void SearchLOCinside(ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetailCG LOCdetial=(GffDetailCG) Genlist.get(beginnum);		
		result=true;
		insideLOC=true;
		
		LOCID[0]=LOCdetial.locString;//本基因的ID
		begincis5to3=LOCdetial.cis5to3;//一直为正
		
		distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因起点的位置
		distancetoLOCStart[1]=-1;
		
		distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因起点的位置
		distancetoLOCEnd[1]=-1;
	}

}
