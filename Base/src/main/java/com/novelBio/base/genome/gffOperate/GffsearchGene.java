package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;

 /**
  * 给定某个坐标位点返回具体的LOC编号以及定位
  * 注意，本类完全是建立在GffHash类的基础上的，所以
  * 必须要有GffHash类的支持才能工作！也就是说必须首先用GffHash类的方法读取Gff文件！
  * 本类需要实例化
  * 作者：宗杰 20090617
  */
 
public class GffsearchGene extends Gffsearch{
	
	/**
	 * 当位点处于基因内部时的具体查找,返回GffCodInfoGene实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	protected  GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetailGene LOCdetial=(GffDetailGene) Genlist.get(beginnum);
		GffCodInfoGene cordInsideGeneInfo=new GffCodInfoGene();
		cordInsideGeneInfo.result=true;//找到位置
		cordInsideGeneInfo.begincis5to3=LOCdetial.cis5to3;//本基因方向
		cordInsideGeneInfo.insideLOC=true;//在基因内
		cordInsideGeneInfo.LOCID[0]=LOCdetial.locString;//本基因的ID
		if(LOCdetial.cis5to3)
		{
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因实际起点的位置
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因实际终点的位置
		}	
		else {
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因实际起点的位置
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因实际终点的位置
		}
		cordInsideGeneInfo.distancetoLOCStart[1]=-1;
		cordInsideGeneInfo.distancetoLOCEnd[1]=-1;
		
		//表示和cds序列比较的结果，如果flag为true，说明peak位点大于最大的cds，说明在peak在3'-UTR上
		boolean flag=false;
		
		/** 
		 * @param splitnum:转录本数目
		 * @param position：坐标所在大概位置 1. 5‘UTR 2.外显子 3.内含子  4.3’UTR 
		 * @param ExIntronnum: 该基因内含子/外显子的具体位置 ，5‘UTR为-1，3’UTR为-2
		 * @param start:到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
		 * @param end:到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
		*/
		int splitnum=LOCdetial.getSplitlistNumber();//可变剪接的mRNA数目
		int splitID; int position; int ExIntronnum; int start; int end;
		
		
		for (int i = 0; i < splitnum; i++)//一个一个转录本的检查
		{  
            splitID= LOCdetial.getcdslist(i).get(0);//获得转录本的ID，转录本第一个值get(0)是该转录本的ID
            
			int cdsnum=LOCdetial.getcdslist(i).size();//转录本里cds的数目
			
			flag=false;
			/**
		 	 * 如果基因是从5'-3' 
		 	 */
			if (LOCdetial.cis5to3)
			 {   
		        for(int j=1; j<cdsnum; j++)  //一个一个cds的检查
		         {
		        	if(Coordinate<LOCdetial.getcdslist(i).get(j) && j%2==1)//在外显子之前（内含子中），外显子为： 1,2  3,4  5,6  7,8   0保存转录本的ID
					{  flag=true;
						if(j==1)//在5‘UTR中
						{
						     position=1;//在5‘UTR中
						     ExIntronnum=-1;//在5‘UTR中
						     end=LOCdetial.getcdslist(i).get(j)-Coordinate;//距离编码区
                             start=Coordinate-LOCdetial.numberstart;//距离基因起点
                             cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
                             break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
						}
						else //在内含子中
						{
							 position=3;
						     ExIntronnum=(j-1)/2;//在第(j-1)/2)个内含子中
						     end=LOCdetial.getcdslist(i).get(j)- Coordinate;//距后一个外显子
                             start=Coordinate-LOCdetial.getcdslist(i).get(j-1);//距前一个外显子
                             cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
                             break;	//跳出本转录本的检查，开始上一层的循环，检查下一个转录本	
						}
					}
					else if(Coordinate<LOCdetial.getcdslist(i).get(j)&&j%2==0) //在外显子之中，外显子为： 1,2  3,4  5,6  7,8   0保存转录本的ID
					{  
						flag=true;
						position=2;//在外显子之中
						ExIntronnum=j/2;//在第j/2个外显子中
						end=LOCdetial.getcdslist(i).get(j)-Coordinate;//距离本外显子终止
						start=Coordinate-LOCdetial.getcdslist(i).get(j-1);//距离本外显子起始
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
						break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
					}
		         }
		        if (flag==false)//比最后一个外显子的最后一位还大，也就是上面的循环没有能将flag设置为true
		        {
		        	position=4;//在3’UTR
		        	ExIntronnum=-2;//在3’UTR
		        	end=LOCdetial.numberend-Coordinate;//距离本基因终止
		        	start=Coordinate-LOCdetial.getcdslist(i).get(cdsnum-1);//距离编码区
		        	cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
		        	break;//可有可无
		        }	  
			 
			 }
			
		 	/**
		 	 * 如果基因是从3'-5'，该转录本是反向
		 	 */
			 else
			 {
				  for(int j=1; j<cdsnum; j++)  
			         {
			        	if(Coordinate>LOCdetial.getcdslist(i).get(j)&&j%2==1)//在外显子之前，外显子为： 1,2  3,4  5,6  7,8  
						{ flag=true;
							if(j==1)//在5'-UTR中
							{
							     position=1;//在5‘UTR中
							     ExIntronnum=-1;//在5‘UTR中
							     end=Coordinate-LOCdetial.getcdslist(i).get(j);//距离编码区
	                             start=LOCdetial.numberend-Coordinate;//距离基因起点,因为gff文件中基因的编号是从小到大的，所以起点反而是大的那个点
							  cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
							  break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
							}
							else //在内含子中
							{  
								position=3;//在内含子中
							     ExIntronnum=(j-1)/2;//在第(j-1)/2个内含子中
							     end=Coordinate-LOCdetial.getcdslist(i).get(j);//距后一个外显子
	                             start=LOCdetial.getcdslist(i).get(j-1)-Coordinate;//距前一个外显子
	                            cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
								
								break;	//跳出本转录本的检查，开始上一层的循环，检查下一个转录本	
									
							}
						}
						else if(Coordinate>LOCdetial.getcdslist(i).get(j)&&j%2==0) //在外显子之中
						{  flag=true;
						   position=2;//在外显子之中
					       ExIntronnum=j/2;//在第j/2个外显子中
					       end=Coordinate-LOCdetial.getcdslist(i).get(j);//距离本外显子终止
	                       start=LOCdetial.getcdslist(i).get(j-1)-Coordinate;//距离本外显子起始
	                    cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list 
							  break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
						}
			         }
				  if (flag==false)
				  {
					  position=4;//在3’UTR
				      ExIntronnum=-2;//在3’UTR
				      end=Coordinate-LOCdetial.numberstart;//距离本基因终止,因为gff文件中基因的编号是从小到大的，所以终点反而是小的那个点
                      start=LOCdetial.getcdslist(i).get(cdsnum-1)-Coordinate;//距离编码区
                     cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//装入list
						break;//可有可无
				  }	  
			 }
		}
		return cordInsideGeneInfo;
	}
	
	
	/**
	 * 当位点处于基因外部时的具体查找,返回GffCodInfoGene实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	protected GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
    {   
	
		GffDetailGene endnumlist=(GffDetailGene) Genlist.get(endnum);
		GffDetailGene beginnumlist=(GffDetailGene) Genlist.get(beginnum);
	
		int distanceend=-1; int distancebegin=-1;
		
		GffCodInfoGene cordOutSideGeneInfo=new GffCodInfoGene();
		cordOutSideGeneInfo.result=true;//找到位置
		cordOutSideGeneInfo.begincis5to3=beginnumlist.cis5to3;//上个基因方向
		cordOutSideGeneInfo.endcis5to3=endnumlist.cis5to3;//下个基因方向
		cordOutSideGeneInfo.insideLOC=false;//在基因间
		cordOutSideGeneInfo.LOCID[1]=beginnumlist.locString;//上个基因的ID
		cordOutSideGeneInfo.LOCID[2]=endnumlist.locString;//下个基因的ID
     
		
        
        //与前一个基因转录起点和终点的距离
		if(cordOutSideGeneInfo.begincis5to3)
        {//当基因正向时，与TSS距离为正数，与End为负数        |>----->------*
			cordOutSideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
			cordOutSideGeneInfo.distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
        }
        else
        {//当基因反向时，与TSS距离为负数，与End为正数   <-------<|----*
        	cordOutSideGeneInfo.distancetoLOCStart[0]=-Math.abs(beginnumlist.numberend-Coordinate);
        	cordOutSideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberstart);
        }
        
		//与后一个基因转录起点和终点的距离
        if(cordOutSideGeneInfo.endcis5to3)
        {//当基因正向时，与TSS距离为负数，与End为正数         *---|>----->----
        	cordOutSideGeneInfo.distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
        	cordOutSideGeneInfo.distancetoLOCEnd[1]=Math.abs(Coordinate-endnumlist.numberend);
        }
        else
        {//当基因反向时，与TSS距离为正数，与End为负数        *----<-------<|
        	cordOutSideGeneInfo.distancetoLOCStart[1]=Math.abs(endnumlist.numberend-Coordinate);
        	cordOutSideGeneInfo.distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberstart);
        }	
        
        
		/** 
		 * @param splitnum:转录本数目
		 * @param position：坐标所在大概位置 1. 5‘UTR 2.外显子 3.内含子  4.3’UTR 
		 * @param ExIntronnum: 该基因内含子/外显子的具体位置 ，5‘UTR为-1，3’UTR为-2
		 * @param start:到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
		 * @param end:到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
		*/
		int splitID=-1; //int position; int ExIntronnum; int start; int end;
		
		int endsplitnumber=endnumlist.getSplitlistNumber();//后一个基因的转录本数目
		int begsplitnumber=beginnumlist.getSplitlistNumber();//前一个基因的转录本数目
		
	/**
	 * 和下个基因的距离
	 */
		if (endnumlist.cis5to3)//正向,那么就是到ATG的距离
		{  
           for(int i=0;i<endsplitnumber;i++)
           {     splitID= endnumlist.getcdslist(i).get(0);
        	     distanceend=endnumlist.getcdslist(i).get(1)-Coordinate;
        	     cordOutSideGeneInfo.addenddistance(splitID, distanceend); 
           }
		}
		else //反向，那么就是到基因尾部的距离
		{            
			//距离下个基因终止,因为反向，而gff文件中基因的编号是从小到大的，所以终点反而是小的那个点
			distanceend=endnumlist.numberstart-Coordinate;
			cordOutSideGeneInfo.addenddistance(splitID, distanceend);
	           
		}
		
		/**
		 * 和上个基因的距离
		 */
   		if (!beginnumlist.cis5to3)//反向
		{  
           for(int i=0;i<begsplitnumber;i++)
           {
        	   
               //如果peak到前一个基因的编码区在所确定的范围内，那么也是和起始的cds的距离
        	   splitID=  beginnumlist.getcdslist(i).get(0);
        	   distancebegin=beginnumlist.getcdslist(i).get(1);
        	   cordOutSideGeneInfo.addbegindistance(splitID, distancebegin);
           }			
		}
   		else //正向
   		{
   		    distancebegin=Coordinate-beginnumlist.numberend;
   		 cordOutSideGeneInfo.addbegindistance(splitID, distancebegin);
		}
			return cordOutSideGeneInfo;
    }
	
}






																								