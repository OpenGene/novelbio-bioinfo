package com.novelbio.base.genome.gffOperate;

import java.util.ArrayList;

public class GffsearchPeak extends Gffsearch{


	/**
	 * 当位点处于基因外部时的具体查找,返回GffCodInfo实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
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
			gffPeakInfo.LOCID[1]=beginnumlist.locString;//上个基因的ID
			gffPeakInfo.begincis5to3=beginnumlist.cis5to3;//一直为正
			 //与前一个基因转录起点和终点的距离
			if(gffPeakInfo.begincis5to3)
	        {//当基因正向时，与TSS距离为正数，与End为负数        |>----->------*
				gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
				gffPeakInfo.distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
	        }
	        else
	        {//当基因反向时，与TSS距离为负数，与End为正数   <-------<|----*
	        	gffPeakInfo.distancetoLOCStart[0]=-Math.abs(beginnumlist.numberend-Coordinate);
	        	gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberstart);
	        }
		}
		if (endnum!=-1) {
			endnumlist=Genlist.get(endnum);
			gffPeakInfo.LOCID[2]=endnumlist.locString;//下个基因的ID
			gffPeakInfo.endcis5to3=endnumlist.cis5to3;//一直为正
			//与后一个基因转录起点和终点的距离
	        if(gffPeakInfo.endcis5to3)
	        {//当基因正向时，与TSS距离为负数，与End为正数         *---|>----->----
	        	gffPeakInfo.distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
	        	gffPeakInfo.distancetoLOCEnd[1]=Math.abs(Coordinate-endnumlist.numberend);
	        }
	        else
	        {//当基因反向时，与TSS距离为正数，与End为负数        *----<-------<|
	        	gffPeakInfo.distancetoLOCStart[1]=Math.abs(endnumlist.numberend-Coordinate);
	        	gffPeakInfo.distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberstart);
	        }	
		}
		return gffPeakInfo;
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
		GffDetail LOCdetial=Genlist.get(beginnum);
		GffCodInfo gffPeakInfo=new GffCodInfo();
		
		gffPeakInfo.result=true;
		gffPeakInfo.insideLOC=true;
		
		gffPeakInfo.LOCID[0]=LOCdetial.locString;//本基因的ID
		
		gffPeakInfo.begincis5to3=LOCdetial.cis5to3;//一直为正
		
		
		gffPeakInfo.LOCID[0]=LOCdetial.locString;//本基因的ID
		if(LOCdetial.cis5to3)
		{
			gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因实际起点的位置
			gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因实际终点的位置
		}	
		else {
			gffPeakInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因实际起点的位置
			gffPeakInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因实际终点的位置
		}
		gffPeakInfo.distancetoLOCStart[1]=-1;
		gffPeakInfo.distancetoLOCEnd[1]=-1;
		
		return gffPeakInfo;
	}


}
