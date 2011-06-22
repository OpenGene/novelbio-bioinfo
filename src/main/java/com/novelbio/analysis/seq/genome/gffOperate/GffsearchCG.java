package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;

/**
 *  给定某个坐标位点返回具体的LOC编号以及定位
  * 注意，本类完全是建立在GffHash类的基础上的，所以
  * 必须要有GffHash类的支持才能工作！也就是说必须首先用GffHash类的方法读取Gff文件！
 * CG不分正反向的
  * 本类需要实例化
 * @author zong0jie
 */
public class GffsearchCG extends Gffsearch
{
	/**
	 * 当位点处于基因外部时的具体查找,返回GffCodInfo实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 * 如果坐标前/后没有相应的基因，那么相应的LOCID为null
	 */
	protected  GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{
		GffDetailCG beginnumlist=null;
		GffDetailCG endnumlist=null;
		GffCodInfo GffCGInfo=new GffCodInfo();
		
		GffCGInfo.result=true;
		GffCGInfo.insideLOC=false;
		int[] distancetoLOCEnd=new int[2];//坐标到上一个，以及下一个条目终点的距离
		int[] distancetoLOCStart=new int[2];//坐标到上一个，以及下一个条目起点的距离
		
		if (beginnum!=-1) {
			beginnumlist=(GffDetailCG) Genlist.get(beginnum);
			GffCGInfo.LOCID[1]=beginnumlist.locString;//上个基因的ID
			GffCGInfo.begincis5to3=beginnumlist.cis5to3;//一直为正
			distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberend);
			distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
		}
		if (endnum!=-1) {
			endnumlist=(GffDetailCG) Genlist.get(endnum);
			GffCGInfo.LOCID[2]=endnumlist.locString;//下个基因的ID
			GffCGInfo.endcis5to3=endnumlist.cis5to3;//一直为正
			distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberend);
			distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
		}
		
		
		GffCGInfo.distancetoLOCEnd=distancetoLOCEnd;
		GffCGInfo.distancetoLOCStart=distancetoLOCStart;
		
		return GffCGInfo;
	}
	
	/**
	 * 当位点处于基因内部时的具体查找,返回GffCodInfo实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	protected  GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetailCG LOCdetial=(GffDetailCG) Genlist.get(beginnum);
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
