package com.novelbio.analysis.seq.genomeNew.gffOperate;
 
import java.util.ArrayList;
 

 

/**
 * 本类用来保存查找的坐标的信息，被GffSearch类调用
 * @author Zong jie
 *
 */
public class GffCodInfoGene extends GffCodAbs
{
	GffCodInfoGene(String chrID, int Coordinate) {
		super(chrID, Coordinate, gffHash);
		// TODO Auto-generated constructor stub
	}


	/**
	 * 当Coordinate在基因间时，
	 * 本list用来装载Coordinate与后一个基因不同转录本的起点或终点的距离
	 * @其中所含为int[2]
	 * 如果坐标位于后一个基因的头部<br/>
	 * 0: 转录本编号<br/>
	 * 1: 和后一个基因ATG距离<br/>
	 * 如果坐标位于后一个基因的尾部，<br/>
	 * 0：-1<br/>
	 * 1：和后一个基因尾部的距离<br/>
	 */
	public ArrayList<int[]> enddistance=new ArrayList<int[]>();	

	/**
	 * 如果坐标在基因间,
	 * 添加坐标与后一个基因的距离
	 * @int[2]的一个数组
	 * 0：转录本编号<br/>
	 * 1：和后一个基因的距离，如果在后基因开头（endcis5to3=true），则为距离ATG位置，如在后基因尾部（endcis5to3=false），则为距离基因终点位置
	 */
	public void addenddistance(int splitID,int distanceend)
	    {   int[] distance=new int[2] ;//装载单个可变剪接的信息,里面只有cds的信息，具体在GffHash中装入
	        
	        distance[0]=splitID;//转录本编号
	        distance[1]=distanceend;
	        enddistance.add(distance);
	    }

	
	/**
	 * 当Coordinate在基因间时，
	 * 本list用来装载Coordinate与前一个基因不同转录本的起点或终点的距离
	 * 其中所含为int[2]<br/>
	 * @如果坐标位于前一个基因的头部<br/>
	 * 0: 转录本编号<br/>
	 * 1: 和前一个基因ATG距离<br/>
	 * @如果坐标位于前一个基因的尾部，<br/>
	 * 0：-1<br/>
	 * 1：和前一个基因尾部的距离<br/>
	 */
	public ArrayList<int[]> begindistance=new ArrayList<int[]>();	
	 
	/**
	 * 如果坐标在基因间
	 * 添加坐标与前一个基因的距离
	 * int[2]的一个数组
	 * 0：转录本编号
	 * 1：和前一个基因的距离，如果在前基因开头（begincis5to3=true），则为距离ATG位置，如在前基因尾部（begincis5to3=false），则为距离基因终点位置
	 */
	public void addbegindistance(int splitID,int distancebegin)
	    {   int[] distance=new int[2] ;//装载单个可变剪接的信息,里面只有cds的信息，具体在GffHash中装入
	        
	        distance[0]=splitID;//转录本编号
	        distance[1]=distancebegin;
	        begindistance.add(distance);
	    }
	
	
	
	
	/**
	 * 当Coordinate在基因中时，
	 * 本list用来装载Coordinate的不同转录本位置信息
	 * @list内为int[5]数组
     * 0：转录本编号<br/>
     * 1：坐标所在具体位置 1. 5‘UTR 2.外显子 3.内含子 4. 3’UTR<br/>
     * 2: 该基因内含子/外显子的位置 ，5‘UTR为-1，3’UTR为-2<br/>
	 * 3：到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离<br/>
	 * 4：到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离<br/>
	 */
	public ArrayList<int[]> GeneInfo=new ArrayList<int[]>();	
	
	/**
	 * 如果坐标在基因内
	 * 添加坐标在不同转录本中的内容
	 * @param splitID:转录本数目
	 * @param position：坐标所在大概位置 1. 5‘UTR 2.外显子 3.内含子 4. 3’UTR 
	 * @param ExIntronnum: 该基因内含子/外显子的位置 ，5‘UTR为-1，3’UTR为-2
	 * @param start:到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
	 * @param end:到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
	 * <br>
	 * 
	 * @最后装入的int[5]数组
     * 0：转录本编号<br>
     * 1：坐标所在具体位置 1. 5‘UTR 2.外显子 3.内含子 4. 3’UTR  <br>
     * 2: 该基因内含子/外显子的位置 ，5‘UTR为-1，3’UTR为-2<br>
	 * 3：到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离<br>
	 * 4：到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
	 */
    public void addingeneinfo(int splitID, int position ,int ExIntronnum,int start,int end)
    {   int[] CordtGeneInfo=new int[5] ;//装载单个可变剪接的信息,里面只有cds的信息，具体在GffHash中装入
        
        CordtGeneInfo[0]=splitID;//转录本编号
		CordtGeneInfo[1]=position;//坐标所在大概位置 1. 5‘UTR 2.外显子 3.内含子 4. 3’UTR  
        CordtGeneInfo[2]=ExIntronnum;//该基因内含子/外显子的位置 ，5‘UTR为-1，3’UTR为-2
        CordtGeneInfo[3]=start;//到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
        CordtGeneInfo[4]=end;//到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
        GeneInfo.add(CordtGeneInfo);//装入list
    }

	@Override
	protected void SearchLOCinside(ArrayList<GffDetailAbs> loclist, int i, int j) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void SearchLOCoutside(ArrayList<GffDetailAbs> loclist, int i, int j) {
		// TODO Auto-generated method stub
		
	}
}
