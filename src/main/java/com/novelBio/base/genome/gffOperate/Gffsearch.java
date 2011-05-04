package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *  给定某个坐标位点返回具体的LOC编号以及定位
  * 注意，本类完全是建立在GffHash类的基础上的，所以
  * 必须要有GffHash类的支持才能工作！也就是说必须首先用GffHash类的方法读取Gff文件！
  * 本类需要实例化
 * @author zong0jie
 *
 */
public abstract class Gffsearch {
	
	
	
	
	/**
	 * 双坐标查找
	 * 输入ChrID，两个坐标(可以不分大小)，以及GffHash类<br>
	 *  chr格式，全部小写 chr1,chr2,chr11<br>
	 * 获得两个坐标的总基因信息-存储在List文件中<br>
	 * <b>List文件包含以下内容</b><br>
	 * 0：Object[2]:其中<br>
	 * &nbsp;&nbsp;  0: 较小坐标的GffCodInfo信息，用对应的GffCodInfo子类去接收<br>
	 * &nbsp;&nbsp;  1: 较小坐标的overlap比例，用double[3]去接收，如果较本坐标不在项目内，则都为0<b>比例都是百分比</b>:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       0: peak与左端Item交集时，交集在左端Item中所占的比例<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       1: peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       2: peak与左端Item交集时，实际交集的bp数<br>
	 * 1：Object[2]:其中<br>
	 * &nbsp;&nbsp;  0: 较大坐标的GffCodInfo信息，用对应的GffCodInfo子类去接收<br>
	 * &nbsp;&nbsp;    1: 较大坐标的overlap比例，用double[3]去接收，如果较本坐标不在项目内，则都为0<b>比例都是百分比</b>:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       0: peak与右端Item交集时，交集在右端条目中所占的比例<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       1: peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       2: peak与右端条目交集时，实际交集的bp数<br>
	 * 2、3、4......等后续依次是两个坐标之间Item的GffDetail信息，用对应的GffDetail子类去接收<br>
	 * 按照想要的结果，用不同的GffCodInfo子类接收<br>
	 * 如用GffsearchGene搜索则用GffCodinfoGene接收
	 * 在用UCSCgene接收时，只考虑最长的一条转录本
	 */
	public ArrayList<Object> searchLocation(String ChrID, int CoordinateA, int CoordinateB , GffHash gffHash)
	{
		Hashtable<String, ArrayList<GffDetail>> LocHash=gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(ChrID.toLowerCase());//某一条染色体的信息
		
		//将坐标大的给Coordinate2，小的给Coordinate1
		int Coordinate1=0;int Coordinate2=0;
		if(CoordinateA>CoordinateB)
		{
			Coordinate1=CoordinateB;Coordinate2=CoordinateA;
		}
		else {
			Coordinate1=CoordinateA;Coordinate2=CoordinateB;
		}
		
		
		GffCodInfo gffCodInfo1=searchLocation(ChrID, Coordinate1, gffHash);
		GffCodInfo gffCodInfo2=searchLocation(ChrID, Coordinate2, gffHash);
		
		
		/**
		 * 0: peak与左端交集时，交集在左端条目中所占的比例
		 * 1: peak与左端交集时，交集在peak中所占的比例
		 * 2: peak与左端交集时，交集的具体bp数
		 */
		double[] cod1OvlapProp=new double[3];
		cod1OvlapProp[0]=0;cod1OvlapProp[1]=0;cod1OvlapProp[2]=0;
		/**
		 * 0: peak与右端交集时，交集在左端条目中所占的比例
		 * 1: peak与右端交集时，交集在peak中所占的比例
		 * 2: peak与右端交集时，交集的具体bp数
		 */
		double[] cod2OvlapProp=new double[3];
		cod2OvlapProp[0]=0;cod2OvlapProp[1]=0;cod2OvlapProp[2]=0;
		
		/**
		 * 如果peak两个端点都在在同一条目之内
		 */
		if (gffCodInfo1.geneChrHashListNum[0]==gffCodInfo2.geneChrHashListNum[0]&&gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetail=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			int thisDetailLength=thisDetail.numberend-thisDetail.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			cod1OvlapProp[0]=100*(double)peakLength/thisDetailLength;
			cod1OvlapProp[1]=100;
			cod1OvlapProp[2]=peakLength;
			cod2OvlapProp[0]=cod1OvlapProp[0];
			cod2OvlapProp[1]=100;
			cod2OvlapProp[2]=cod1OvlapProp[2];
		}//如果peak左端点在一个条目内，右端点在另一个条目内
		else if (gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC&&gffCodInfo1.geneChrHashListNum[0]!=gffCodInfo2.geneChrHashListNum[0])
		{
			GffDetail thisDetailleft=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			GffDetail thisDetailright=Loclist.get(gffCodInfo2.geneChrHashListNum[0]);
			int leftItemLength=thisDetailleft.numberend-thisDetailleft.numberstart;
			int rightItemLength=thisDetailright.numberend-thisDetailright.numberstart;

			int leftoverlap=thisDetailleft.numberend-Coordinate1;
			int rightoverlap=Coordinate2-thisDetailright.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=100*(double)leftoverlap/leftItemLength;
			cod1OvlapProp[1]=100*(double)leftoverlap/peakLength;
			cod1OvlapProp[2]=leftoverlap;
			cod2OvlapProp[0]=100*(double)rightoverlap/rightItemLength;
			cod2OvlapProp[1]=100*(double)rightoverlap/peakLength;
			cod2OvlapProp[2]=rightoverlap;
		}//peak只有左端点在条目内
		else if (gffCodInfo1.insideLOC&&!gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetailleft=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			int leftItemLength=thisDetailleft.numberend-thisDetailleft.numberstart;
			int leftoverlap=thisDetailleft.numberend-Coordinate1;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=100*(double)leftoverlap/leftItemLength;
			cod1OvlapProp[1]=100*(double)leftoverlap/peakLength;
			cod1OvlapProp[2]=leftoverlap;
			cod2OvlapProp[0]=0;
			cod2OvlapProp[1]=0;
			cod2OvlapProp[2]=0;
		}//peak只有右端点在条目内
		else if (!gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetailright=Loclist.get(gffCodInfo2.geneChrHashListNum[0]);
			int rightItemLength=thisDetailright.numberend-thisDetailright.numberstart;
			int rightoverlap=Coordinate2-thisDetailright.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=0;
			cod1OvlapProp[1]=0;
			cod1OvlapProp[2]=0;
			cod2OvlapProp[0]=100*(double)rightoverlap/rightItemLength;
			cod2OvlapProp[1]=100*(double)rightoverlap/peakLength;
			cod2OvlapProp[2]=rightoverlap;
		}
		//将gffCodInfo信息以及相应的占的比例装入Object
		Object[] gffCodInfo1_overlap=new Object[2];
		gffCodInfo1_overlap[0]=gffCodInfo1;
		gffCodInfo1_overlap[1]=cod1OvlapProp;
		
		Object[] gffCodInfo2_overlap=new Object[2];
		gffCodInfo2_overlap[0]=gffCodInfo2;
		gffCodInfo2_overlap[1]=cod2OvlapProp;
		
		
		ArrayList<Object> LstGffCodInfo=new ArrayList<Object>();
		LstGffCodInfo.add(gffCodInfo1_overlap);
		LstGffCodInfo.add(gffCodInfo2_overlap);
		////////////////////////////////////////////////获得两个坐标以及中间夹着的条目，注意不包括坐标所在的基因///////////////////////////////////////////////////////////////////////////////
		//两个坐标的ID以及 之间条目的ID起止，如-------------- coordinate1(Cod1ID)----------ItemA(startID)-------ItemB-------ItemC------ItemD(endID)-------coordinate2(Cod2ID)-----------------
		int Cod1ID=-1,  Cod2ID=-1;
		
		Cod1ID=gffCodInfo1.geneChrHashListNum[0];//本条目/上个条目编号
			
		if(gffCodInfo2.insideLOC)
			Cod2ID=gffCodInfo2.geneChrHashListNum[0];//本条目编号
		else 
			Cod2ID=gffCodInfo2.geneChrHashListNum[1];//下个条目编号
		
		if((Cod2ID-Cod1ID)>1)//把Cod1ID和Cod2ID之间的所有条目的GffDetail装入LstGffCodInfo
		{
			for (int i = 1; i <(Cod2ID-Cod1ID); i++) {
				GffDetail gffDetail=Loclist.get(Cod1ID+i);
				LstGffCodInfo.add(gffDetail);
			}
		}
		return LstGffCodInfo;
	}
	
	
	
	
	/**
	 * 单坐标查找
	 * 输入ChrID，单个坐标，以及GffHash类<br>
	 *  chr格式，全部小写 chr1,chr2,chr11<br>
	 * 获得基因信息-存储在GffCoordiatesInfo类中<br>
	 * 按照想要的结果，用不同的GffCodInfo子类接收<br>
	 * 如用GffsearchGene搜索则用GffCodinfoGene接收
	 */
	public GffCodInfo searchLocation(String ChrID, int Coordinate, GffHash gffHash)
	{
		Hashtable<String, ArrayList<GffDetail>> LocHash=gffHash.getChrhash();;
		ArrayList<GffDetail> Loclist = LocHash.get(ChrID.toLowerCase());//某一条染色体的信息
	    if (Loclist==null)
	    {
	    	GffCodInfo cordInsideGeneInfo=new GffCodInfoGene();
	    	cordInsideGeneInfo.result=false;
	    	return cordInsideGeneInfo;
	    }
		return searchLocation(Coordinate,Loclist); 
	}
	
	
	/**
	 * 输入PeakNum，和单条Chr的list信息
	 * 返回该PeakNum的所在LOCID，和具体位置
	 * 返回的是GffCoordinatesInfo类的实例化对象
	 */
	private GffCodInfo searchLocation(int Coordinate,ArrayList<GffDetail> Loclist)
	{
		String[] locationString=new String[5];
		locationString[0]="GffCodInfo_searchLocation error";
		locationString[1]="GffCodInfo_searchLocation error";
	    int[] locInfo=LocPosition(Loclist, Coordinate);//二分法查找peaknum的定位
		if(locInfo[0]==1) //定位在基因内
		{
			GffCodInfo LOCinsid= SearchLOCinside(Coordinate, Loclist,locInfo[1], locInfo[2]);//查找具体哪个内含子或者外显子
			LOCinsid.geneChrHashListNum[0]=locInfo[1];
			
			if (locInfo[1]==-1) 
				LOCinsid.geneDetail[0]=null;
			else 
				LOCinsid.geneDetail[0]=Loclist.get(locInfo[1]);
			
			if (locInfo[2]==-1) 
				LOCinsid.geneDetail[1]=null;
			else 
				LOCinsid.geneDetail[1]=Loclist.get(locInfo[2]);
			
			LOCinsid.geneChrHashListNum[1]=locInfo[2];
			
			
			return LOCinsid;
			}
		else if(locInfo[0]==2)
		{
			GffCodInfo LOCoutsid=SearchLOCoutside(Coordinate, Loclist, locInfo[1], locInfo[2]);//查找基因外部的peak的定位情况
			if (locInfo[1]==-1) 
				LOCoutsid.geneDetail[0]=null;
			else 
				LOCoutsid.geneDetail[0]=Loclist.get(locInfo[1]);
			
			if (locInfo[2]==-1) 
				LOCoutsid.geneDetail[1]=null;
			else 
				LOCoutsid.geneDetail[1]=Loclist.get(locInfo[2]);
			
			LOCoutsid.geneChrHashListNum[0]=locInfo[1];
	
			LOCoutsid.geneChrHashListNum[1]=locInfo[2];
			return LOCoutsid;
		}
		return null;
	}
	
	

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内）  /  上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 */
	private int[] LocPosition(ArrayList<GffDetail> Loclist,int Coordinate)
	{
		int[]LocInfo= new int[3]; 
		
		int endnum=0;
		
		endnum=Loclist.size()-1;

		int beginnum=0;
		int number=0;
		
		
		
		
		//在第一个Item之前
		if (Coordinate<Loclist.get(beginnum).numberstart) {
			LocInfo[0]=2;
			LocInfo[1]=-1;
			LocInfo[2]=0;
			return LocInfo;
		}
		//在最后一个Item之后
		else if (Coordinate>Loclist.get(endnum).numberstart) {
			LocInfo[1]=endnum;
			LocInfo[2]=-1;		
			if (Coordinate<Loclist.get(endnum).numberend) 
			{
				LocInfo[0]=1;
				return LocInfo;
			}
			else 
			{
				LocInfo[0]=2;
				return LocInfo;
			}	
		}
		
		
		do
		{
			number=(beginnum+endnum+1)/2;//3/2=1,5/2=2
			if(Coordinate==Loclist.get(number).numberstart)
		   {
			   beginnum=number;
			   endnum=number+1;
			   break;
		   }
			
		   else if(Coordinate<Loclist.get(number).numberstart&&number!=0)
		   {
			   endnum=number;
		   }
		   else
		   {
			   beginnum=number;
		   }
		}while((endnum-beginnum)>1);	
		LocInfo[1]=beginnum;
		LocInfo[2]=endnum;
		if(Coordinate<=Loclist.get(beginnum).numberend )//不知道会不会出现PeakNumber比biginnum小的情况
		{ //location在基因内部
           LocInfo[0]=1;
			return LocInfo;
		}
	     //location在基因外部
		 LocInfo[0]=2;
		return LocInfo;
	}
	
	/**
	 * 必须被覆盖
	 * @param coordinate
	 * @param loclist
	 * @param i
	 * @param j
	 * @return
	 */
	protected abstract  GffCodInfo SearchLOCinside(int coordinate,ArrayList<GffDetail> loclist, int i, int j) ;
	
	/**
	 * 必须被覆盖
	 * @param coordinate
	 * @param loclist
	 * @param i
	 * @param j
	 * @return
	 */
	protected abstract GffCodInfo SearchLOCoutside(int coordinate,ArrayList<GffDetail> loclist, int i, int j);
	
	
	/**
	 * 给定基因名，返回该基因信息,一个GffDetailList类
	 * 按照想要的结果，用不同的GffDetail子类接收<br>
	 * 如用GffsearchGene搜索则用GffDetailGene接收
	 * @param LocID
	 */
	public static GffDetail LOCsearch(String LocID,GffHash gffHash)
	{
		return gffHash.LOCsearch(LocID);
	}
	
	
}
