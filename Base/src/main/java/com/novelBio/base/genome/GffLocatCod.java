package com.novelBio.base.genome;

import java.util.ArrayList;
import java.util.List;

import com.novelBio.base.genome.gffOperate.GffCodInfo;
import com.novelBio.base.genome.gffOperate.GffCodInfoUCSCgene;
import com.novelBio.base.genome.gffOperate.GffDetailUCSCgene;
import com.novelBio.base.genome.gffOperate.GffHash;
import com.novelBio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelBio.base.genome.gffOperate.GffsearchUCSCgene;


/**
 * 将目标定位到染色体具体位置做Annotation，同时也可统计内含子外显子等信息
 * @author zong0jie
 *
 */
public class GffLocatCod extends GffChrUnion
{
 
	 
	
 
	
	/**
	 * 给定二维数组,计算出每个peakLOC所在的基因，针对UCSCknown gene以及refseq
	 * @param LOCIDInfo <br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 仅输出2k上游和50bp下游的数据<br>
	 * @return 输出ArrayList-String[10]<br>
	 * 0: ChrID<br>
	 * 1: 坐标<br>
	 * 2: 在基因内显示"基因内"<br>
	 * 3: 在基因间并且距离上下基因很近，显示"基因间，距离上/下基因很近"<br>
	 * 4 在基因间 "基因间，距离上/下基因有点远"
	 * 5: 在基因内，本基因名<br>
	 * 6: 在基因内的具体信息<br>
	 * 7: 在基因间并且距离上个基因很近，上个基因名<br>
	 * 8: 上个基因方向，到上个基因起点/终点的距离<br>
	 * 9: 在基因间并且距离下个基因很近，下个基因名<br>
	 * 10: 下个基因方向，到下个基因起点/终点的距离 
	 * 
	 */
	public ArrayList<String[]> peakAnnotationCH(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=null;
		//	try {
				 tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);
		//	} catch (Exception e) {
			//	System.out.println(LOCIDInfo[i][0]+" "+LOCIDInfo[i][1]);
			//}
			
			String[] tmpPeakAnnotation=new String[11];//最后结果
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//全部设置为""
			}
			///////////////////////////////////////////////////////////////////////
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//如果在基因内
			if (tmpresult.insideLOC) 
			{
				tmpPeakAnnotation[2]="基因内";tmpPeakAnnotation[3]="";

				/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
				tmpPeakAnnotation[5]=tmpresult.LOCID[0];
				
               ////////////////// 方   向 ///////////////////////////
				if (tmpresult.begincis5to3) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"本基因方向："+"正向";
				else
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"本基因方向："+"反向";
				
				/////////////////////  UTR  ////////////////////////////////////////////////////////////
				if (tmpresult.GeneInfo.get(0)[4]==5) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"5UTR";
				else if (tmpresult.GeneInfo.get(0)[4]==3) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"3UTR";
					
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1)
					tmpPeakAnnotation[6]="外显子"+tmpPeakAnnotation[6]+"_"+"处在"+"第"+tmpresult.GeneInfo.get(0)[1]+"个"+"外显子中";
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
					tmpPeakAnnotation[6]="内含子"+tmpPeakAnnotation[6]+"_"+"处在"+"第"+tmpresult.GeneInfo.get(0)[1]+"个"+"内含子中";
				
			}
			else
			{
				//与上个基因的关系
				/**
				 * * 0: ChrID<br>
				 * 1: 坐标<br>
				 * 2: 在基因内显示"基因内"<br>
				 * 3: 在基因间并且距离上下基因很近，显示"基因间，距离上/下基因很近"<br>
				 * 4 在基因间 "基因间，距离上/下基因有点远"
				 * 5: 在基因内，本基因名<br>
				 * 6: 在基因内的具体信息<br>
				 * 7: 在基因间并且距离上个基因很近，上个基因名<br>
				 * 8: 上个基因方向，到上个基因起点/终点的距离<br>
				 * 9: 在基因间并且距离下个基因很近，下个基因名<br>
				 * 10: 下个基因方向，到下个基因起点/终点的距离 
				 */
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						tmpPeakAnnotation[4]="基因间，距离上个基因终点很近";
						tmpPeakAnnotation[7]=tmpresult.LOCID[1];
						tmpPeakAnnotation[8]="上个基因方向为正向，到上个基因终点的距离为"+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=UpStreamTSSbp)
					{
						tmpPeakAnnotation[4]="基因间，距离上个基因TSS很近";
						tmpPeakAnnotation[7]=tmpresult.LOCID[1];
						tmpPeakAnnotation[8]="上个基因方向为反向，到上个基因TSS的距离为"+Math.abs(tmpresult.distancetoLOCStart[0])+"";
					}
				}
				//与下个基因的关系
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						if (tmpPeakAnnotation[4].equals("")) 
							tmpPeakAnnotation[4]="基因间，距离下个基因TSS很近";
						else 
							tmpPeakAnnotation[4]=tmpPeakAnnotation[4]+"_基因间，距离下个基因TSS很近";
						
						tmpPeakAnnotation[9]=tmpresult.LOCID[2];
						tmpPeakAnnotation[10]="下个基因方向为正向，到下个基因TSS的距离为"+Math.abs(tmpresult.distancetoLOCStart[1])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{
						if (tmpPeakAnnotation[4].equals("")) 
							tmpPeakAnnotation[4]="基因间，距离下个基因终点很近";
						else 
							tmpPeakAnnotation[4]=tmpPeakAnnotation[4]+"_基因间，距离下个基因终点很近";
						
						tmpPeakAnnotation[9]=tmpresult.LOCID[2];
						tmpPeakAnnotation[10]="下个基因方向为反向，到下个基因终点的距离为"+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
					}
				}
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	/**
	 * 给定二维数组,计算出每个peakLOC所在的基因，针对UCSCknown gene以及refseq
	 * @param LOCIDInfo <br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 仅输出2k上游和50bp下游的数据<br>
	 * @return 输出ArrayList-String[8]<br>
	 * 0: ChrID<br>
	 * 1: 坐标<br>
	 * 2: 在基因内，本基因名<br>
	 * 3: 在基因内的具体信息<br>
	 * 4: 在基因间并且距离上个基因很近，上个基因名<br>
	 * 5: 上个基因方向，到上个基因起点/终点的距离<br>
	 * 6: 在基因间并且距离下个基因很近，下个基因名<br>
	 * 7: 下个基因方向，到下个基因起点/终点的距离 
	 * 
	 */
	public ArrayList<String[]> peakAnnotationEN(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			String[] tmpPeakAnnotation=new String[8];//最后结果
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//全部设置为""
			}
			GffCodInfoUCSCgene tmpresult=null;
			try {
				tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);

			} catch (Exception e) {
				System.out.println("peakAnnotationEN error");
				tmpPeakAnnotation[3] = "noLOC";
				lspeakAnnotation.add(tmpPeakAnnotation);
				continue;
			}
			

			///////////////////////////////////////////////////////////////////////
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//如果在基因内
			if (tmpresult.insideLOC) 
			{
				if (tmpresult.distancetoLOCStart[0]<DownStreamTssbp) {
					tmpPeakAnnotation[3]="Promoter:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfTss";
				}
				/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
				tmpPeakAnnotation[2]=tmpresult.LOCID[0];
				if (tmpresult.GeneInfo.get(0)[4]==5) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"5UTR";
				else if (tmpresult.GeneInfo.get(0)[4]==3) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"3UTR";
	
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1)
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
			}
			else
			{
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						tmpPeakAnnotation[4]=tmpresult.LOCID[1];
						tmpPeakAnnotation[5]="Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0]) <= UpStreamTSSbp)
					{
						tmpPeakAnnotation[4]=tmpresult.LOCID[1];
						if (Math.abs(tmpresult.distancetoLOCStart[0])>=10000) 
						{
							tmpPeakAnnotation[5]="InterGenic_";
						}
						else if (Math.abs(tmpresult.distancetoLOCStart[0])<10000&&Math.abs(tmpresult.distancetoLOCStart[0])>=5000) {
							tmpPeakAnnotation[5]="Distal Promoter_";
						}
						else
						{
							tmpPeakAnnotation[5]="Proximal Promoter_";
						}
						tmpPeakAnnotation[5]=tmpPeakAnnotation[5]+"Distance to TSS of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[0])+"";
					}
				}
				//与下个基因的关系
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						tmpPeakAnnotation[6]=tmpresult.LOCID[2];
						if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
						{
							tmpPeakAnnotation[7]="InterGenic_";
						}
						else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
							tmpPeakAnnotation[7]="Distal Promoter_";
						}
						else
						{
							tmpPeakAnnotation[7]="Proximal Promoter_";
						}
						tmpPeakAnnotation[7]=tmpPeakAnnotation[7]+"Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{						
						tmpPeakAnnotation[6]=tmpresult.LOCID[2];
						tmpPeakAnnotation[7]="Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
					}
				}
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * 指定条件，将符合条件的peak抓出来并做注释，主要是筛选出合适的peak然后做后续比较工作
	 * 不符合的会跳过
	 * @param LOCIDInfo 把excel全部读取后的List-String[]
	 * @param colChrID chrID在第几列，实际列
	 * @param colSummit summit位点在第几列，实际列
	 * @param filterTss 是否进行tss筛选，null不进行，如果进行，那么必须是int[2],0：tss上游多少bp  1：tss下游多少bp，都为正数 <b>只有当filterGeneBody为false时，tss下游才会发会作用</b>
	 * @param filterGenEnd 是否进行geneEnd筛选，null不进行，如果进行，那么必须是int[2],0：geneEnd上游多少bp  1：geneEnd下游多少bp，都为正数<b>只有当filterGeneBody为false时，geneEnd上游才会发会作用</b>
	 * @param filterGeneBody 是否处于geneBody，true，将处于geneBody的基因全部筛选出来，false，不进行geneBody的筛选<br>
	 * <b>以下条件只有当filterGeneBody为false时才能发挥作用</b>
	 * @param filter5UTR 是否处于5UTR中
	 * @param filter3UTR 是否处于3UTR中
	 * @param filterExon 是否处于外显子中
	 * @param filterIntron 是否处于内含子中
	 * 0-n:输入的loc信息<br>
	 * n+1: 基因名<br>
	 * n+2: 基因信息<br>
	 **/
	public ArrayList<String[]> peakAnnoFilter(List<String[]> LOCIDInfo,int colChrID,int colSummit,int[] filterTss, int[] filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron)
	{
		int imputLength = LOCIDInfo.get(0).length;
		colChrID--; colSummit--;
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		for (int i = 0; i < LOCIDInfo.size(); i++)
		{
			//标记，因为tss下游与genebody实际上是重复的，那么如果tss是true，就可以不进行geneBody的定位
			//同样如果geneEnd是true，可以不进行geneBody的定位
			//所以只有当peak处在tss下游，和geneEnd上游，也就是处于gene内部时，这两个mark才会标记 
			boolean tss = false; boolean geneEnd = false;
			//标记，因为UTR下游与内含子外显子部分重复的，那么如果UTR是true，就可以不进行内含子与外显子的定位
			//所以只有当peak处在UTR内部时，这两个mark才会标记 
			boolean UTR5  = false; boolean UTR3 = false;
			GffCodInfoUCSCgene tmpresult=null;
			try {
				String chrID = LOCIDInfo.get(i)[colChrID].toLowerCase();
				int summit = Integer.parseInt(LOCIDInfo.get(i)[colSummit]);
				tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(chrID, summit, gffHash);

			} catch (Exception e) {
				System.out.println("peakAnnoFilter error");
				continue;
			}
			//本基因/上一个基因
			String[] tmpPeakAnnotation=new String[imputLength+5];//最后结果需要保存输入的所有信息
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//全部设置为""
			}
			for (int j = 0; j < imputLength; j++) {
				try {
					tmpPeakAnnotation[j] = LOCIDInfo.get(i)[j];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			//下一个基因
			boolean downGene = false;
			String[] tmpPeakAnnotation2=new String[imputLength+5];//最后结果需要保存输入的所有信息
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation2.length; j++) {
				tmpPeakAnnotation2[j]="";//全部设置为""
			}
			for (int j = 0; j < imputLength; j++) {
				try {
					tmpPeakAnnotation2[j] = LOCIDInfo.get(i)[j];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			if (filterTss != null) //筛选tss上下游
			{
				if (!tmpresult.insideLOC)//首先筛选tss上游
				{
					if (!tmpresult.begincis5to3) 
					{
						if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=filterTss[0])
						{
							tmpPeakAnnotation[imputLength+0]=tmpresult.LOCID[1];
							if (Math.abs(tmpresult.distancetoLOCStart[0])>=10000) 
							{
								tmpPeakAnnotation[imputLength+1]="InterGenic_";
							}
							else if (Math.abs(tmpresult.distancetoLOCStart[0])<10000&&Math.abs(tmpresult.distancetoLOCStart[0])>=5000) {
								tmpPeakAnnotation[imputLength+1]="Distal Promoter_";
							}
							else
							{
								tmpPeakAnnotation[imputLength+1]="Proximal Promoter_";
							}
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1]+"Distance to TSS of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[0])+"";
						}
					}
					//与下个基因的关系
					if(tmpresult.endcis5to3)
					{
						if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=filterTss[0])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[2])) 
							{
								//说明是新的LOC
								downGene = true;
								tmpPeakAnnotation2[imputLength+0] = tmpresult.LOCID[2];
								if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
								{
									tmpPeakAnnotation2[imputLength+1]= "InterGenic_";
								}
								else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
									tmpPeakAnnotation2[imputLength+1]="Distal Promoter_";
								}
								else
								{
									tmpPeakAnnotation2[imputLength+1]="Proximal Promoter_";
								}
								tmpPeakAnnotation2[imputLength+1] = tmpPeakAnnotation2[imputLength+1] + "Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength+0] = tmpresult.LOCID[2];
								if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
								{
									tmpPeakAnnotation[imputLength+1]= "InterGenic_";
								}
								else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
									tmpPeakAnnotation[imputLength+1]="Distal Promoter_";
								}
								else
								{
									tmpPeakAnnotation[imputLength+1]="Proximal Promoter_";
								}
								tmpPeakAnnotation[imputLength+1] = tmpPeakAnnotation[imputLength+1] + "Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
							}
						}
					}
				}
				else //筛选tss下游
				{
					if (tmpresult.distancetoLOCStart[0]<filterTss[1])
					{
						tss = true;
						tmpPeakAnnotation[imputLength] = tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]="Promoter:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfTss";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			if (filterGenEnd != null) //筛选geneEnd上下游
			{
				if (!tmpresult.insideLOC)//首先筛选geneEnd下游
				{	
					if(tmpresult.begincis5to3)
					{
						if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=filterGenEnd[1])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[1])) 
							{
								downGene = true;
								tmpPeakAnnotation2[imputLength] = tmpresult.LOCID[1];
								tmpPeakAnnotation2[imputLength+1] = "Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength] = tmpresult.LOCID[1];
								tmpPeakAnnotation[imputLength+1] = "Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
							}
						}
					}
					if(!tmpresult.endcis5to3)
					{
						if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=filterGenEnd[1])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[2])) 
							{
								downGene = true;
								tmpPeakAnnotation2[imputLength] = tmpresult.LOCID[2];
								tmpPeakAnnotation2[imputLength+1] = "Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength] = tmpresult.LOCID[2];
								tmpPeakAnnotation[imputLength+1] = "Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
							}
						}
					}
				}
				else
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					if (Math.abs(tmpresult.distancetoLOCEnd[0]) < filterGenEnd[0])
					{
						geneEnd = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength] + sep + tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"GeneEnd:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfGeneEnd";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//geneBody
			if (filterGeneBody && !tss && !geneEnd)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
					if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
						tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
					boolean utr = false;
					if (tmpresult.GeneInfo.get(0)[4]==5)
					{
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"5UTR_";utr = true;
					}
					else if (tmpresult.GeneInfo.get(0)[4]==3) 
					{
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"3UTR_";utr = true;
					}
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if(tmpresult.GeneInfo.get(0)[0]==1 )
					{
						if (utr) {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
						else {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
					}
						
					else if (tmpresult.GeneInfo.get(0)[0]==2) 
						if (utr) {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
						else {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//5UTR
			if ( !filterGeneBody && filter5UTR && !tss && !geneEnd )
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[4]==5)
					{
						UTR5 = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"5UTR";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//3UTR
			if ( !filterGeneBody && filter3UTR && !tss && !geneEnd)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[4]==3) 
					{
						UTR3 = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"3UTR";
					}
				}
			}
			
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//Exon
			if (!filterGeneBody && filterExon && !tss && !geneEnd && UTR5 && UTR3)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
					
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if(tmpresult.GeneInfo.get(0)[0]==1 )
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//Intron
			if (!filterGeneBody && filterIntron && !tss && !geneEnd && UTR5 && UTR3)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  本 基 因 名   //////////////////////////////////////////////////////////////////////////
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[0]==2) 
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				}
			}
			if (tmpPeakAnnotation[imputLength].trim().equals("")) {
				continue;
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
			if (downGene) {
				lspeakAnnotation.add(tmpPeakAnnotation2);
			}
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * 给定二维数组,统计peakLOC所在基因位置的统计结果，针对UCSCknown gene以及refseq
	 * @param LOCIDInfo <br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 仅输出3k上游和100bp下游的数据<br>
	 * @return 输出String[6][2]，第一维：标题，第二维：数量<br>
	 * [0]: 5UTR <br>
	 * [1]: 3UTR<br>
	 * [2]: Exon 注意不包括5UTR和3UTR<br> 
	 * [3]: Intron<br>
	 * [4]:Up3k<br>
	 * [5]:InterGenic
	 */
	public String[][] peakStatistic(String[][] LOCIDInfo)
	{
		String[][] peakStatistic=new String[6][2];
		//初始化
		peakStatistic[0][0]="5UTR";peakStatistic[1][0]="3UTR";peakStatistic[2][0]="Exon";
		peakStatistic[3][0]="Intron";peakStatistic[4][0]="Up3k";peakStatistic[5][0]="InterGenic";
		peakStatistic[0][1]="0";peakStatistic[1][1]="0";peakStatistic[2][1]="0";
		peakStatistic[3][1]="0";peakStatistic[4][1]="0";peakStatistic[5][1]="0";
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=null; String chrID = LOCIDInfo[i][0].toLowerCase(); int summit = Integer.parseInt(LOCIDInfo[i][1]);
			if (summit == 26842076) {
				System.out.println("sss");
			}
			try {
				tmpresult = (GffCodInfoUCSCgene)gffSearch.searchLocation(chrID, summit, gffHash);
			} catch (Exception e) {
				System.out.println("peakStatistic"+LOCIDInfo[i][0].toLowerCase()+" " + LOCIDInfo[i][1]);
				continue;
			}
		
			
			//////////////////////////////////////////////////////////////////////////////
 
			if (tmpresult.insideLOC) 
			{
				if (tmpresult.GeneInfo.get(0)[4]==5) //5UTR
				{
					peakStatistic[0][1]=Integer.parseInt(peakStatistic[0][1])+1+"";
					continue;
				}
				else if (tmpresult.GeneInfo.get(0)[4]==3) //3UTR
				{
					peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
					continue;
				}
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1 && tmpresult.GeneInfo.get(0)[4] == 0) //Exon
				{
					peakStatistic[2][1]=Integer.parseInt(peakStatistic[2][1])+1+"";
					continue;
				}
				else if (tmpresult.GeneInfo.get(0)[0]==2 && tmpresult.GeneInfo.get(0)[4] == 0) //Intron
				{
					peakStatistic[3][1]=Integer.parseInt(peakStatistic[3][1])+1+"";
					continue;
				}
			}
			else
			{
				//与上个基因的关系
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
						continue;//距离上个基因在GeneEnd3UTR内
					}
				}
				else if (!tmpresult.begincis5to3) 
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=UpStreamTSSbp)
					{
						peakStatistic[4][1]=Integer.parseInt(peakStatistic[4][1])+1+"";
						continue;
					}
				}
				//与下个基因的关系
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						peakStatistic[4][1]=Integer.parseInt(peakStatistic[4][1])+1+"";
						continue;
					}
				}
				else if(!tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{			 
						peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
						continue;//InterGenic
					}
				}
				peakStatistic[5][1]=Integer.parseInt(peakStatistic[5][1])+1+"";
			}
		}
		return peakStatistic;
	}
	
	
	
	/**
	 *  给定二维数组,计算出每个peakLOC所在的基因，针对UCSCknown gene以及refseq
	 * @param LOCIDInfo
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * @param considerDistance 是否仅输出2k上游和50bp下游的数据
	 * @return 返回每个peak的详细坐标信息结果
	 * 0: ChrID<br>
	 * 1: 坐标<br>
	 * 2: 基因内还是基因间<br>
	 * 3: 在基因内: 内含子还是外显子<br>
	 * 4: 内含子与内含子起点距离比例<br>
	 * 5: 内含子与内含子终点距离比例<br>
	 * 6: 外显子与外显子起点距离比例<br>
	 * 7: 外显子与外显子终点距离比例<br>
	 * 8: 是否5UTR<br>
	 * 9: 5UTR与基因起点距离比例<br>
	 * 10: 5UTR与ATG距离比例<br>
	 * 11: 是否3UTR<br>
	 * 12: 3UTR与UAG距离比例<br>
	 * 13: 3UTR与基因结尾距离比例<br>
	 * 14: peak与TSS距离<br>
	 * 15: peak与ATG距离<br>
	 * 16: peak与基因终点的距离
	 */
	public ArrayList<String[]> peakAnnotationDetail(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		for (int i = 0; i < LOCIDInfo.length; i++) 
		{
			if (LOCIDInfo[i][1].contains("79963824")) {
				System.out.println("stop");
			}
			
			GffCodInfoUCSCgene tmpresult=null;
			try {
				String LOCID=LOCIDInfo[i][0].toLowerCase();
				int LOCcod=Integer.parseInt(LOCIDInfo[i][1]);
				 tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCID, LOCcod, gffHash);
			} catch (Exception e) {
				String test=LOCIDInfo[i][1];
				System.out.println(test);
			}
			////////////////// 都 赋 空 值 /////////////////////////////////
			String[] tmpPeakAnnotation=new String[17];
			for (int j = 0; j <17; j++) {
				tmpPeakAnnotation[j]="";
			}
			
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//如果在基因内
			
			
			if (tmpresult.insideLOC) 
			{
				tmpPeakAnnotation[2]="基因内";
				GffDetailUCSCgene tmpDetailUCSCgene=(GffDetailUCSCgene) tmpresult.geneDetail[0];
				if (tmpresult.geneChrHashListNum[0]==-1) {
					continue;
				}
				if(tmpresult.GeneInfo.get(0)[0]==1)
				{
					tmpPeakAnnotation[3]="外显子";	
					double ItemLength=tmpDetailUCSCgene.getTypeLength("Exon",  tmpresult.GeneInfo.get(0)[1]);
					////////////////////测 试 代 码 //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("Exon0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("5UTR", 0);
					}
					if (tmpresult.GeneInfo.get(0)[2]+tmpresult.GeneInfo.get(0)[3]!=ItemLength) {
						System.out.println("Exon"+tmpresult.LOCID[0]);
					}
					///////////////// 测 试 代 码 //////////////////////////////////////////////
					tmpPeakAnnotation[6]=tmpresult.GeneInfo.get(0)[2]/ItemLength+"";
					tmpPeakAnnotation[7]=tmpresult.GeneInfo.get(0)[3]/ItemLength+"";
				}
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
				{
					tmpPeakAnnotation[3]="内含子";
					double ItemLength=tmpDetailUCSCgene.getTypeLength("Intron", tmpresult.GeneInfo.get(0)[1]);
					//////////////////// 测 试 代 码 //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("Intron0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[2]+tmpresult.GeneInfo.get(0)[3]!=ItemLength) {
						System.out.println("Intron"+tmpresult.LOCID[0]);
					}
					///////////////// 测 试 代 码 //////////////////////////////////////////////
					tmpPeakAnnotation[4]=tmpresult.GeneInfo.get(0)[2]/ItemLength+"";
					tmpPeakAnnotation[5]=tmpresult.GeneInfo.get(0)[3]/ItemLength+"";
				}
					
				if (tmpresult.GeneInfo.get(0)[4]==3) 
				{
					tmpPeakAnnotation[11]="3UTR";
					
					double ItemLength=tmpDetailUCSCgene.getTypeLength("3UTR", 0);
					//////////////////// 测 试 代 码 //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("3UTR0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[5]+tmpresult.GeneInfo.get(0)[6]!=ItemLength) {
						System.out.println("3UTR"+tmpresult.LOCID[0]);
					}
					///////////////// 测 试 代 码 //////////////////////////////////////////////

					tmpPeakAnnotation[12]=tmpresult.GeneInfo.get(0)[5]/ItemLength+"";
					tmpPeakAnnotation[13]=tmpresult.GeneInfo.get(0)[6]/ItemLength+"";
				}
					
				else if(tmpresult.GeneInfo.get(0)[4]==5)
				{
					tmpPeakAnnotation[8]="5UTR";
					double ItemLength=tmpDetailUCSCgene.getTypeLength("5UTR",0);
					//////////////////// 测 试 代 码 //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("5UTR0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[5]+tmpresult.GeneInfo.get(0)[6]!=ItemLength) {
						System.out.println("5UTR"+tmpresult.LOCID[0]);
					}
					///////////////// 测 试 代 码 //////////////////////////////////////////////

					tmpPeakAnnotation[9]=tmpresult.GeneInfo.get(0)[5]/ItemLength+"";
					tmpPeakAnnotation[10]=tmpresult.GeneInfo.get(0)[6]/ItemLength+"";
				}
				tmpPeakAnnotation[14]=tmpresult.distancetoLOCStart[0]+"";
				tmpPeakAnnotation[15]=tmpresult.codToATG[0]+"";
				//这里要反向，在基因内为负号，基因外为正号
				tmpPeakAnnotation[16]=-tmpresult.distancetoLOCEnd[0]+"";
			}
			else
			{
				tmpPeakAnnotation[2]="基因间";
			
				int tmpUpend=100000000;int tmpUpstart=100000000; int tmpUpATG = 100000000;
				int tmpDownend=100000000;int tmpDownstart=100000000; int tmpDownATG = 100000000;
				
				//和上个条目起点/终点距离
				if (tmpresult.geneChrHashListNum[0]!=-1) {
					if (tmpresult.begincis5to3) {
						tmpUpend=-tmpresult.distancetoLOCEnd[0];
					}
					else {
						tmpUpstart=tmpresult.distancetoLOCStart[0];
						tmpUpATG = tmpresult.codToATG[0];
					}
				}
				
				//和下个条目起点/终点距离
				if  (tmpresult.geneChrHashListNum[1]!=-1){
					if(tmpresult.endcis5to3)
					{
						tmpDownstart=tmpresult.distancetoLOCStart[1];
						tmpDownATG = tmpresult.codToATG[1];
					}
					else {
						tmpDownend=-tmpresult.distancetoLOCEnd[1];
					}
				}
				//取两者之间小的那个ATG
				if (Math.abs(tmpUpATG)<Math.abs(tmpDownATG))
					tmpPeakAnnotation[15]=tmpUpATG+"";
				else 
					tmpPeakAnnotation[15]=tmpDownATG+"";
				//GeneEnd
				if (Math.abs(tmpUpend)<Math.abs(tmpDownend)) 
					tmpPeakAnnotation[16]=tmpUpend+"";
				else 
					tmpPeakAnnotation[16]=tmpDownend+"";
				//TSS
				if (Math.abs(tmpUpstart)<Math.abs(tmpDownstart)) 
					tmpPeakAnnotation[14]=tmpUpstart+"";
				else 
					tmpPeakAnnotation[14]=tmpDownstart+"";
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * 给定二维数组,统计各个区域所占的比重，针对UCSCknown gene
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 返回坐标统计信息<br>
	 * int[6]<br>
	 * 0:Intron<br>
	 * 1:Exon<br>
	 * 2:5UTR<br>
	 * 3:3UTR<br>
	 * 4:up2k<br>
	 * 5:Intergeneic<br>
	 */
	public int[] locateCod(String[][] LOCIDInfo)
	{
		
		int Intron=0;int Exon=0;int fiveUTR=0;int threeUTR=0;int up2k=0;int intergeneic=0;
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);
			if(tmpresult.insideLOC)//基因内
			{
				if(tmpresult.GeneInfo.get(0)[0]==1)
					fiveUTR++;
				else if(tmpresult.GeneInfo.get(0)[0]==2)
					Exon++;
				else if(tmpresult.GeneInfo.get(0)[0]==3)	
					Intron++;
				else if(tmpresult.GeneInfo.get(0)[0]==4)
					threeUTR++;
			}
			else 
			{
				if(!tmpresult.begincis5to3 && tmpresult.distancetoLOCStart[0]>-2000)
					up2k++;
				else if(tmpresult.endcis5to3 && tmpresult.distancetoLOCStart[1]>-2000)
					up2k++;
				else 
					intergeneic++;
			}
		}
		int[] result =new int[6];
		result[0]=Intron;result[1]=Exon;result[2]=fiveUTR;result[3]=threeUTR;result[4]=up2k;result[5]=intergeneic;
		return result;
	}
	
	
	
	
	
	
}
