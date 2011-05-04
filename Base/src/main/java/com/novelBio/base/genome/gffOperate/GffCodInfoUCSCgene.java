package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;



/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodInfoUCSCgene extends GffCodInfo
{
	/**
	 * 保存坐标到最长转录本的ATG的距离
	 * 坐标在ATG下游，为正数
	 * 坐标在ATG上游，为负数
	 * 0: 坐标到本基因/上个基因 ATG距离
	 * 1: 如果坐标在基因间:坐标到下个基因ATG的距离.否则为-1000000000
	 * @return
	 */
	public int[] codToATG=new int[2];
	
	
	
	/**
	 * 当Coordinate在基因中时，
	 * 本list用来装载Coordinate的不同转录本位置信息
	 * @list内为int[7]数组
     * 0：坐标所在具体位置 1..外显子 2.内含子 <br/>
     * 1: 该基因内含子/外显子的位置。注意UCSC定义的外显子内含子，定义时5UTR和3UTR也算入外显子。 <br/>
	 * 2：到该内含子/外显子 的起点距离<br/>
	 * 3：到该内含子/外显子的终点距离<br/>
	 * 4：如果是外显子，坐标是否在5UTR或3UTR内，0: 不在  5:5UTR    3:3UTR<br/>
	 * 5：如果在UTR区域内，5‘UTR为到gene起点距离，  3’UTR为到编码区距离，都跳过内含子计算距离。如果不在，则为-1<br/>
	 * 6：如果在UTR区域内，5‘UTR为到ATG， 3’UTR为到gene尾部距离，都跳过内含子计算距离。如果不在，则为-1<br/>
	 */
	public ArrayList<int[]> GeneInfo=new ArrayList<int[]>();	
	
	/**
	 * 当Coordinate在基因中时，
	 * 本list用来装载Coordinate的不同转录本的名称,名称与GeneInfo一一对应
	 */
	public ArrayList<String> GeneID=new ArrayList<String>();
	/**
	 * 如果坐标在基因内
	 * 添加坐标在不同转录本中的内容
	 * @param splitID 转录本名称
	 * @param position 坐标所在大概位置 1.外显子 2.内含子
	 * @param ExIntronnum 该基因内含子/外显子的位置，注意UCSC定义的外显子内含子，定义时5UTR和3UTR也算入外显子。
	 * @param start 到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离，  3’UTR为到编码区距离
	 * @param end 到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
	 * @param UTRinfo 如果是外显子，坐标是否在5UTR或3UTR内，0: 不在  5:5UTR    3:3UTR
	 * @param UTRstart 如果在UTR区域内，5‘UTR为到gene起点距离,3’UTR为到编码区距离，都跳过内含子计算距离。如果不在，则为-1
	 * @param UTRend 如果在UTR区域内，5‘UTR为到ATG， 3’UTR为到gene尾部距离，都跳过内含子计算距离。如果不在，则为-1
	 * <br>
	 * 
	 * @最后装入的int[7]数组
     * 0：坐标所在具体位置 1..外显子 2.内含子 <br/>
     * 1: 该基因内含子/外显子的位置。注意UCSC定义的外显子内含子，定义时不包含5UTR和3UTR。 <br/>
	 * 2：到该内含子/外显子 的起点距离<br/>
	 * 3：到该内含子/外显子的终点距离<br/>
	 * 4：如果是外显子，坐标是否在5UTR或3UTR内，0: 不在  5:5UTR    3:3UTR<br/>
	 * 5：如果在UTR区域内，5‘UTR为到gene起点距离，  3’UTR为到编码区距离，都跳过内含子计算距离。如果不在，则为-1<br/>
	 * 6：如果在UTR区域内，5‘UTR为到ATG， 3’UTR为到gene尾部距离，都跳过内含子计算距离。如果不在，则为-1<br/>
	 */
    public void addingeneinfo(String splitID, int position ,int ExIntronnum,int start,int end,int UTRinfo,int UTRstart, int UTRend)
    {   int[] CordtGeneInfo=new int[7] ;//装载单个可变剪接的信息,里面只有cds的信息，具体在GffHash中装入
        
    	GeneID.add(splitID);//转录本编号
		CordtGeneInfo[0]=position;//坐标所在大概位置 1. 5‘UTR 2.外显子 3.内含子 4. 3’UTR  
        CordtGeneInfo[1]=ExIntronnum;//该基因内含子/外显子的位置 ，5‘UTR为-1，3’UTR为-2
        CordtGeneInfo[2]=start;//到该内含子/外显子 的起点距离，5‘UTR为到gene起点距离， 3’UTR为到最后一个CDS距离
        CordtGeneInfo[3]=end;//到该内含子/外显子的终点距离，，5‘UTR为到ATG， 3’UTR为到gene尾部距离
        CordtGeneInfo[4]=UTRinfo;
        CordtGeneInfo[5]=UTRstart;
        CordtGeneInfo[6]=UTRend;
        
        
        GeneInfo.add(CordtGeneInfo);//装入list
    }
}
