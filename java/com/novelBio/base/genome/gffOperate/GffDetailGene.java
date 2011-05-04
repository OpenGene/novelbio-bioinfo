package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * GffDetailList类中保存每个基因的起点终点和CDS的起点终点 
 * @GffHashGene读取Gff文件，每个基因可以获得以下信息
 * 基因名<br>
 * 本基因起点<br>
 * 本基因终点<br>
 * 本基因所在染色体编号<br>
 * 本基因的不同转录本<br>
 * 本基因转录方向<br>
 * 本类中的几个方法都和Gff基因有关<br>
 * @GffHashItem读取Gff文件，每个条目可以获得以下信息
 * 条目名<br>
 * 条目起点<br>
 * 条目终点<br>
 * 条目所在染色体编号<br>
 * 条目的转录方向，这个不一定会有，如果没有的话，默认就是正向<br>
 */
public class GffDetailGene extends GffDetail
{
	/**
	 * 储存同一基因的不同转录本
	 */
	public ArrayList<LinkedList<Integer>> splitlist=new ArrayList<LinkedList<Integer>>();//存储可变剪接的mRNA


	/**
	 * 给最后一个转录本添加cds，其中cdslist的第一项存储mRNA转录本的序号
	 */
	public void addcds(int locnumber)
	{
		LinkedList<Integer> cdslist=splitlist.get(splitlist.size()-1);//include one special loc start number to end number	
		cdslist.add(locnumber);
	}	

	/**
	 * 添加转录本
	 */
  public void addsplitlist()
  {   /**
       *装载单个可变剪接的信息<br>
       *其中第一个是转录本的ID，从第二号开始是CDS的信息<br>
       *不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
       */	
  	LinkedList<Integer> cdslist=new LinkedList<Integer>();
      splitlist.add(cdslist);
  }
  
 /**
 * 返回转录本的数目
  * @return
  */
  public int getSplitlistNumber()
  {  
  	return splitlist.size();
  }

  /**
   * 给定编号(从0开始，编号不是转录本的具体ID)<br>
   * 返回某个转录本，其中第一项是转录本的ID，从第二号开始是CDS的信息<br>
   * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。<br>
   */
  public LinkedList<Integer> getcdslist(int splitnum)
  {  
  	return splitlist.get(splitnum);//include one special loc start number to end number	
  }
  
  
  /**
   * 获得该基因中最长的一条转录本名称和具体信息
   * @return 返回一个ArrayList-object
   * 第一个用 String 接收，是该转录本的名称
   * 第二个用ArrayList-Integer接收，是该转录本的具体信息
   * 其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
   * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
   * 不管怎么加都是从小加到大<br>
   */
	public ArrayList<Object> getLongestSplit() 
	{
		ArrayList<Object> result=new ArrayList<Object>();
		if(splitlist.size()==1)
		{
			result.add(lssplitName.get(0));
			result.add(splitlist.get(0));
			return result;
		}
		
		
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<splitlist.size();i++)
		{
			ArrayList<Integer>  subsplit=splitlist.get(i);
			lslength.add(subsplit.get(subsplit.size()-1)-subsplit.get(2));
		}
		int max=lslength.get(0);
		for (int i = 0; i < lslength.size(); i++) {
			if(lslength.get(i)>max)
				max=lslength.get(i);
		}
		
		int longsplitID=lslength.indexOf(max);
		ArrayList<Integer> splitresult=splitlist.get(longsplitID);
		int splitID=splitlist.indexOf(splitresult);
		String splitName=lssplitName.get(splitID);
		result.add(splitName);
		result.add(splitresult);
		return result;
	}
	
  
  
  
  
  
}