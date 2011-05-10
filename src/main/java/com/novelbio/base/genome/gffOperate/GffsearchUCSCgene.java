package com.novelbio.base.genome.gffOperate;

import java.util.ArrayList;



public class GffsearchUCSCgene extends Gffsearch
{

	/**
	 * 当位点处于基因内部时的具体查找,返回GffcodInfoUCSCgene实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	@Override
	protected GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> loclist, int beginnum, int endnum)
	{
		GffDetailUCSCgene LOCdetial=(GffDetailUCSCgene) loclist.get(beginnum);
		GffCodInfoUCSCgene cordInsideGeneInfo=new GffCodInfoUCSCgene();
		cordInsideGeneInfo.result=true;//找到位置
		cordInsideGeneInfo.begincis5to3=LOCdetial.cis5to3;//本基因方向
		cordInsideGeneInfo.insideLOC=true;//在基因内
		cordInsideGeneInfo.LOCID[0]=LOCdetial.locString;//本基因的ID
		if(LOCdetial.cis5to3)
		{
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因起点的位置
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因终点的位置
		}	
		else {
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//到本基因起点的位置
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//到本基因终点的位置
		}
		cordInsideGeneInfo.distancetoLOCStart[1]=-1;
		
		cordInsideGeneInfo.distancetoLOCEnd[1]=-1;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		boolean flag=false;
		
		/** 
		 * 设定注意，如果位点在tss之前到基因起点之后(这种情况很少)，判定为第一个外显子，同样如果在转录终点到基因终点之间，落在3UTR内，判定为最后一个外显子
		 * @param splitnum:转录本数目
		 * @param position：坐标所在大概位置 1.外显子 2.内含子 
		 * @param ExIntronnum: 该基因内含子/外显子的具体位置
		 * @param start:到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
		 * @param end:到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
		 *  0：坐标所在具体位置 1..外显子 2.内含子 <br/>
		 * 1: 该基因内含子/外显子的位置，从1开始记数。注意UCSC定义的外显子内含子，定义时不包含5UTR和3UTR。 <br/>
		 * 2：到该内含子/外显子 的起点距离<br/>
		 * 3：到该内含子/外显子的终点距离<br/>
		 * 4：如果是外显子，坐标是否在5UTR或3UTR内，0: 不在  5:5UTR   3:3UTR<br/>
		 * 5：如果在UTR区域内，5‘UTR为到gene起点距离，  3’UTR为到编码区距离，都跳过内含子计算距离。如果不在，则为-1<br/>
		 * 6：如果在UTR区域内，5‘UTR为到ATG， 3’UTR为到gene尾部距离，都跳过内含子计算距离。如果不在，则为-1<br/>
	
		 */
		//int splitnum=LOCdetial.getSplitlistNumber();//可变剪接的mRNA数目
		//ArrayList<String> lsSplitID=LOCdetial.getLsSplitename();
		String splitID; int position; int ExIntronnum; int start; int end;int UTR; int UTRstart;int UTRend;
		
		
		//for (int i = 0; i < splitnum; i++)//一个一个转录本的检查
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//只检查最长一条染色体的情况
		ArrayList<Object> longSplitExonInfo=LOCdetial.getLongestSplit();//获得最长转录本的信息      
		ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
		splitID=(String)longSplitExonInfo.get(0);
		int exonNum=longSplitExon.size();//转录本里Exon的数目
		cordInsideGeneInfo.codToATG[0]=1000000000;
		cordInsideGeneInfo.codToATG[1]=1000000000;
		
		
		
		flag=false;
		if (cordInsideGeneInfo.begincis5to3 ) 
			cordInsideGeneInfo.codToATG[0]=Coordinate-longSplitExon.get(0);
		else if (!cordInsideGeneInfo.begincis5to3)
			cordInsideGeneInfo.codToATG[0]=longSplitExon.get(1)-Coordinate;
			
		cordInsideGeneInfo.codToATG[1]=-1000000000;
		
		/**
		 * 如果基因是从5'-3' 
		 */
		if (LOCdetial.cis5to3)
		{
			for(int j=2; j<exonNum; j++)  //一个一个Exon的检查
			{
				if(Coordinate<longSplitExon.get(j) && j%2==0)//在外显子之前（内含子中），外显子为： 2,3  4,5  6,7  8,9   0该转录本的转录起点，1该转录本的转录终点
				{
					flag=true;
				   if(j==2)//在5‘UTR中,也算做在外显子中   tss cod ，2 3 ， 4 5 ， 6 7 ， 8 9 
				   {
					   position=1;
					   start=Coordinate-LOCdetial.numberstart;//距离基因起点
					   end=longSplitExon.get(3)-Coordinate;//距离第一个外显子终点
					   ExIntronnum=1;
					   UTR=5;
					   UTRstart=start;
					   UTRend=0;
					   if (longSplitExon.get(0)<=longSplitExon.get(3)) //如果atg在第一个外显子中  tss cod ，2 0 3 ， 4 5 ， 6 7 ， 8 9 
					   {
						   UTRend=longSplitExon.get(0)-Coordinate;//
					   }
					   else    //如果atg不在第一个外显子中  tss cod ，2 3 ， 4 5 ， 6 0 7 ， 8 9 
					   {
						   UTRend=longSplitExon.get(3)-Coordinate;
						   int m=5;
						   while (m<exonNum&&longSplitExon.get(0)>longSplitExon.get(m)) //这里不能等于，因为后面还要加一次
						   {
							   UTRend=UTRend+longSplitExon.get(m)-longSplitExon.get(m-1);
							   m=m+2;
						   }
						   UTRend=UTRend+longSplitExon.get(0)-longSplitExon.get(m-1);
					   }
					   cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
					   break;//跳出	   
				   }
				   else //在内含子中
				   {
					  // tss  ，2 3 ， 4 5 ，cod   6 7 ， 8 9 
					   position=2;
					   ExIntronnum=j/2-1;//在第j/2-1个内含子中
					   end=longSplitExon.get(j)- Coordinate;//距后一个外显子
					   start=Coordinate-longSplitExon.get(j-1);//距前一个外显子
					   UTR=0; UTRstart=-1; UTRend=-1;
					   cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
					   break;	//跳出本转录本的检查，开始上一层的循环，检查下一个转录本	
				   }
				}
				else if(Coordinate<=longSplitExon.get(j)&&j%2==1) //在外显子之中，外显子为：2,3  4,5  6,7  8,9   0该转录本的转录起点，1该转录本的转录终点
				{
					flag=true;
					position=1;//在外显子之中
					ExIntronnum=(j-1)/2;//在第(j-1)/2个外显子中
					end=longSplitExon.get(j)-Coordinate;//距离本外显子终止
					start=Coordinate-longSplitExon.get(j-1);//距离本外显子起始
					 UTR=0; UTRstart=-1; UTRend=-1;
					 
					if(Coordinate<longSplitExon.get(0))//坐标小于atg，在5‘UTR中,也是在外显子中
					{
						
						UTR=5;UTRstart=0;UTRend=0;
						// tss  2 3,   4 5,   6 cod 7,   8 0 9
						for (int k = 3; k <= j-2; k=k+2) {
							UTRstart=UTRstart+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRstart=UTRstart+start;
						// tss  2 3,   4 5,   6 cod  0 7
						if (longSplitExon.get(0)<=longSplitExon.get(j)) //一定要小于等于
						{
							UTRend=longSplitExon.get(0)-Coordinate;
						}
						// tss  2 3,   4 5,   6 cod 7,   8  9,   10 0 11
						else 
						{
							UTRend=longSplitExon.get(j)-Coordinate;
							int m=j+2;
							while (m<exonNum&&longSplitExon.get(0)>longSplitExon.get(m)) 
							{
								UTRend=UTRend+longSplitExon.get(m)-longSplitExon.get(m-1);
								m=m+2;
							}
							UTRend=UTRend+longSplitExon.get(0)-longSplitExon.get(m-1);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;//跳出
					}
					// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 11
					if(Coordinate>longSplitExon.get(1))//大于cds起始区，在3‘UTR中
					{
						UTR=3; UTRstart=0;UTRend=0;
						// tss  2 3,   4 0 5,   6 1 cod 7,   8  9,   10 11
						if (longSplitExon.get(1)>=longSplitExon.get(j-1))//一定要大于等于 
						{
							UTRstart=Coordinate-longSplitExon.get(1);
						}
						// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 cod 11
						else 
						{
							UTRstart=Coordinate-longSplitExon.get(j-1);
							int m=j-3;
							while (m>=2&&longSplitExon.get(m)>longSplitExon.get(1)) 
							{
								UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(1);
						}
						/////////////////////utrend//////////////////
						// tss  2 3,   4 0 5,   6 1 7,   8  cod 9,   10 11,  12 13
						for (int k = exonNum-1; k >= j+2; k=k-2) {
							UTRend=UTRend+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRend=UTRend+end;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;//跳出
					}
					
					cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
					break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
				}
			}
			if (flag==false)//比最后一个外显子的最后一位还大，也就是上面的循环没有能将flag设置为true,那就是3UTR，并且设为在外显子
			{
				//tss  2 3,   4 0 5,   6 1 7,   8 9,   10 11,  12 13 cod
				position=1;//在外显子
				ExIntronnum=(exonNum-2)/2;//个数
				end=LOCdetial.numberend-Coordinate;//距离本基因终止
				start=Coordinate-longSplitExon.get(exonNum-2);//距离编码区
				UTR=3;
				UTRend=end;
				UTRstart=0;
				if (longSplitExon.get(1)>=longSplitExon.get(exonNum-2)) //如果uag在最后一个外显子中  tss cod ，2 0 3 ， 4 5 ， 6 7 ， 8 1 9 cod
				{
					UTRstart=Coordinate-longSplitExon.get(1);//
				}
				else    //如果uag不在第一个外显子中  tss  ，2 3 ， 4 uag 5 ， 6 7 ， 8 9  cod
				{
					UTRstart=Coordinate-longSplitExon.get(exonNum-2);
					int m=exonNum-4;
					while (m>=2&&longSplitExon.get(m)>longSplitExon.get(1)) //这里不能等于，因为后面还要加一次
					{
						UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(m);
						m=m-2;
					}
					UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(1);
				}
				cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
			}
		}
			//////////////////////////20100810//////////////////////////////////////////////////////////////////////////
		 	/**
		 	 * 如果基因是从3'-5'，该转录本是反向
		 	 */
		else
		{
			for(int j=exonNum-1; j>=2; j--)  
			{
				if(Coordinate>longSplitExon.get(j)&&j%2==1)//在外显子之前，外显子为：2,3  4,5  6,7  8,9 
				{
					flag=true;
					if(j==exonNum-1)//在5‘UTR中,也算做在外显子中   end ，2 3 ， 4 5 ， 6 0 7 ， 8 9, cod  tss
					{
						position=1;//在外显子中
						ExIntronnum=1;//算在第一个外显子中
						end=Coordinate-longSplitExon.get(exonNum-2);//距离第一个外显子终点
						start=LOCdetial.numberend-Coordinate;//距离基因起点,因为gff文件中基因的编号是从小到大的，所以起点反而是大的那个点
						UTR=5;
						UTRstart=start;
						UTRend=0;
						if (longSplitExon.get(0)>=longSplitExon.get(exonNum-2)) //如果atg在第一个外显子中  2 1 3 ， 4 5 ， 6 7 ， 8 0 9 cod tss
						{
							UTRend=Coordinate-longSplitExon.get(0);//
						}
						else    //如果atg不在第一个外显子中  2 3 ， 4 0 5 ， 6 7 ， 8 9 cod tss
						{
							UTRend=Coordinate-longSplitExon.get(exonNum-2);
							int m=exonNum-4;
							while (m>=2&&longSplitExon.get(0)<longSplitExon.get(m)) //这里不能等于，因为后面还要加一次
							{
								UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(0);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;//跳出	   
					}
					else //在内含子中
					{
						//如果atg不在第一个外显子中  2 3 ， 4 0 5 ， 6 7   cod   8 9  tss
						position=2;//在内含子中
						ExIntronnum=(exonNum-j-1)/2;//在第(exonNum-j-1)/2个内含子中
						end=Coordinate-longSplitExon.get(j);//距后一个外显子
						start=longSplitExon.get(j+1)-Coordinate;//距前一个外显子
						UTR=0; UTRstart=-1; UTRend=-1;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;	//跳出转录本的检查
					}
				}
				else if(Coordinate>longSplitExon.get(j)&&j%2==0) //在外显子之中，外显子为：2,3  4,5  6,7  8,9 
				{
					flag=true;
					position=1;//在外显子之中
					ExIntronnum=(exonNum-j)/2;//在第(j-1)/2个外显子中
					end=Coordinate-longSplitExon.get(j);//距离本外显子终止
					start=longSplitExon.get(j+1)-Coordinate;//距离本外显子起始
					UTR=0; UTRstart=-1; UTRend=-1;
					if(Coordinate>longSplitExon.get(1))//小于cds起始区，在5‘UTR中,也就是在外显子中
					{
						//外显子为：2 0 3,     4 1 5,   6 7,   8 cod 9,  10 11,   12 13  tss   
						UTR=5;UTRstart=0;UTRend=0;
						for (int k = exonNum-1; k >= j+3; k=k-2) {
							UTRstart=UTRstart+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRstart=UTRstart+start;
						//外显子为：2 0 3,     4 1 cod 5,   6 7,   8 9,  10 11,   12 13  tss   
						if (longSplitExon.get(1)>=longSplitExon.get(j)) //一定要大于等于
						{
							UTRend=Coordinate-longSplitExon.get(1);
						}
						else //外显子为：2 0 3,     4 1 5,   6 7,   8 cod 9,  10 11,   12 13  tss   
						{
							UTRend=Coordinate-longSplitExon.get(j);
							int m=j-2;
							while (m>=2&&longSplitExon.get(1)<longSplitExon.get(m)) {
								UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(1);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;//跳出
					}
					//////////////////////////////////////////////////////////////////////////////////////////////
					if(Coordinate<longSplitExon.get(0))//大于cds终止区，在3‘UTR中
					{
						UTR=3; UTRstart=0;UTRend=0;
						//外显子为：end   2 3,     4 cod 0 5,   6 7,   8 9,  10 11,   12 13     
						if (longSplitExon.get(0)<=longSplitExon.get(j+1))//一定要小于等于 
						{
							UTRstart=longSplitExon.get(0)-Coordinate;
						}
						else //外显子为：end   2 3,     4 cod 5,   6 7,   8 9,  10 0 11,   12 13    
						{
							UTRstart=longSplitExon.get(j+1)-Coordinate;
							int m=j+3;
							while (m<exonNum&&longSplitExon.get(m)<longSplitExon.get(0)) 
							{
								UTRstart=UTRstart+longSplitExon.get(m)-longSplitExon.get(m-1);
								m=m+2;
							}
							UTRstart=UTRstart+longSplitExon.get(0)-longSplitExon.get(m-1);
						}
						/////////////////////utrend//////////////////
						//外显子为：end   2 3,     4 5,   6 cod 7,   8 9,  10 0 11,   12 13    
						for (int k = 3; k <= j-1; k=k+2) {
							UTRend=UTRend+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRend=UTRend+end;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
						break;//跳出
					}
					cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
					break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本					 }
				 }
				
			}
			 if (flag==false)//比最后一个外显子的最后一位还大，也就是上面的循环没有能将flag设置为true,那就是3UTR，并且设为在外显子
			 {
				 // end cod 2 3,  4 5,  6 7,  8 0 9,  10 11
				 position=1;//在外显子
				 ExIntronnum=(exonNum-2)/2;//个数
				 end=Coordinate-LOCdetial.numberstart;//距离本基因终止
				 start=longSplitExon.get(3)-Coordinate;//距离编码区
				 UTR=3;
				 UTRend=end;
				 UTRstart=0;
				// end cod 2 0 3,  4 5,  6 7,  8 1 9,  10 11
				 if (longSplitExon.get(0)<=longSplitExon.get(3)) //如果end在第一个外显子中  end cod 2 0 3 ， 4 5 ， 6 7 ， 8 9 
				 {
					 UTRstart=longSplitExon.get(0)-Coordinate;//
				 }	
				 else    //如果UAG不在第一个外显子中  end cod ，2 3 ， 4 5 ， 6 0 7 ， 8 9 
				 {
					 UTRstart=longSplitExon.get(3)-Coordinate;
					 int m=5;
					 while (m<exonNum&&longSplitExon.get(m)<longSplitExon.get(0)) //这里不能等于，因为后面还要加一次
					 {
						 UTRstart=UTRstart+longSplitExon.get(m)-longSplitExon.get(m-1);
						 m=m+2;
					 }
					 UTRstart=UTRstart+longSplitExon.get(0)-longSplitExon.get(m-1);
				 }
				 cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//装入list
			 }
		}
		return cordInsideGeneInfo;
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	
	/**
	 * 当位点处于基因外部时的具体查找,返回GffCodInfoGene实例化对象
	 * @param Coordinate 坐标
	 * @param Genlist 某条染色体的list表
	 * @param beginnum本基因的序号
	 * @param endnum下一个基因的序号
	 */
	@Override
	protected GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist, int beginnum, int endnum) {
		GffCodInfoUCSCgene cordOutSideGeneInfo=new GffCodInfoUCSCgene();
		GffDetailUCSCgene endnumlist=null;
		GffDetailUCSCgene beginnumlist=null;
		
		cordOutSideGeneInfo.result=true;//找到位置
		cordOutSideGeneInfo.insideLOC=false;//在基因间
		
		cordOutSideGeneInfo.codToATG[0]=1000000000;
		cordOutSideGeneInfo.codToATG[1]=1000000000;
		
		
		
		
		if (beginnum!=-1) {
			 beginnumlist=(GffDetailUCSCgene) Genlist.get(beginnum);
			 cordOutSideGeneInfo.begincis5to3=beginnumlist.cis5to3;//上个基因方向
			 cordOutSideGeneInfo.LOCID[1]=beginnumlist.locString;//上个基因的ID
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
			 
			 
			 ArrayList<Object> longSplitExonInfo=beginnumlist.getLongestSplit();//获得最长转录本的信息      
			 ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
			 
			 if (cordOutSideGeneInfo.begincis5to3 ) 
				 cordOutSideGeneInfo.codToATG[0]=Coordinate-longSplitExon.get(0);
			 else if (!cordOutSideGeneInfo.begincis5to3)
				 cordOutSideGeneInfo.codToATG[0]=longSplitExon.get(1)-Coordinate;
		}
		
		if (endnum!=-1) {
			endnumlist=(GffDetailUCSCgene) Genlist.get(endnum);
			cordOutSideGeneInfo.endcis5to3=endnumlist.cis5to3;//下个基因方向
			cordOutSideGeneInfo.LOCID[2]=endnumlist.locString;//下个基因的ID
			//与后一个基因转录起点和终点的距离
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
	        
	   	 
			 ArrayList<Object> longSplitExonInfo=endnumlist.getLongestSplit();//获得最长转录本的信息      
			 ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
			 
			 if (cordOutSideGeneInfo.begincis5to3 ) 
				 cordOutSideGeneInfo.codToATG[1]=Coordinate-longSplitExon.get(0);
			 else if (!cordOutSideGeneInfo.begincis5to3)
				 cordOutSideGeneInfo.codToATG[1]=longSplitExon.get(1)-Coordinate;
		}
        return cordOutSideGeneInfo;
    }
}
