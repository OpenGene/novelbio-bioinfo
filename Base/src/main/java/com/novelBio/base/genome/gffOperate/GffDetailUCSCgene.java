package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.ibatis.migration.commands.NewCommand;



/**
 * 专门存储UCSC的gene坐标文件
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * GffDetailList类中保存每个基因的起点终点和CDS的起点终点 
 * @author zong0jie
 * @GffHashGene读取Gff文件，每个基因可以获得以下信息
 * 基因名<br>
 * 本基因起点，这是UCSC konwn gene某位点所有基因的最靠前的exon的起点<br>
 * 本基因终点，这是UCSC konwn gene某位点所有基因的最靠后的intron的终点<br>
 * 本基因所在染色体编号<br>
 * 本基因的不同转录本<br>
 * 本基因转录方向<br>
 * 本类中的几个方法都和Gff基因有关<br>
 */
public class GffDetailUCSCgene extends GffDetail
{
	/**
	 * 顺序储存同一基因的不同转录本坐标，相应转录本名字保存在splitName中
	 */
	private ArrayList<ArrayList<Integer>> splitList=new ArrayList<ArrayList<Integer>>();//存储可变剪接的mRNA
	
	private ArrayList<Boolean> lsSplitCis5to3 = new ArrayList<Boolean>();

	/**
	 * 指定最后一个转录本的方向。
	 * 这个主要在UCSC中使用，因为UCSC中有极少部分一个基因中同时存在正向和反向序列，所以用这个来标记每个转录本的方向
	 */
	public void addCis5to3(boolean cis5to3)
	{
		lsSplitCis5to3.add(cis5to3);
	}
	
	/**
	 * 给最后一个转录本添加exon坐标，其中exonList的第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.<br>
	 * 注意这两个都与基因方向无关，永远第一项小于第二项<br>
     *从第三号开始是Exon的信息<br>
     *不管怎么加都是从小加到大。
	 */
	public void addExon(int locnumber)
	{
		ArrayList<Integer> exonList=splitList.get(splitList.size()-1);//include one special loc start number to end number	
		exonList.add(locnumber);
	}	
	
	/**
	 * 给最后一个转录本添加exon坐标，其中exonList的第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.<br>
	 * 注意这两个都与基因方向无关，永远第一项小于第二项<br>
     *从第三号开始是Exon的信息<br>
     *不管怎么加都是从小加到大。<br>
     *这个方法主要是读取gff文件时使用，因为gff文件的exon在反向的时候是 7，8  5，6  3，4  1，2这种格式，所以要反着加
     *这时候num=0，每组exon先加后一个再加前一个
     *@param num 如果num<0,就将值加在最后，所以num一般就取两个值，要么反向的exon，取0，要么正向的exon，取-1
     *@param locnumber
     *@param replace 是否替换上一个值。这个主要用于TIGR和TAIR的gff文件，它里面把含有atg的cds分割成了两份
     *因为是成对出现的exon的两个坐标，所以如果replace为true的话，本方法会比较插入位置的值与待插入的值是否只相差1，如果是的话，会将num所在位置的一个元素除去
	 */
	public void addExon(int num, int locnumber,boolean replace)
	{
		//说明该exon是反向排列的
		ArrayList<Integer> exonList=splitList.get(splitList.size()-1);//include one special loc start number to end number	
		if (num >= 0)
		{
			if (replace)
			{
				int tmpLocnumber = exonList.get(num);
				if (Math.abs(locnumber-tmpLocnumber) <= 1) {
					exonList.remove(num);//
				}
				else {
					exonList.add(num,locnumber);
				}
			}
			else {
				exonList.add(num,locnumber);
			}
		}
		//该exon是正向排列的
		else 
		{
			num = exonList.size()-1;
			if (replace)
			{
				int tmpLocnumber = exonList.get(num);
				if (Math.abs(locnumber-tmpLocnumber) <= 1) {
					exonList.remove(num);//
				}
				else {
					exonList.add(locnumber);
				}
			}
			else {
				exonList.add(locnumber);
			}
		}
		
	}	
	/**
	 * 直接添加转录本，之后用addcds()方法给该转录本添加exon
	 */
  public void addsplitlist()
  {   /**
       *装载单个可变剪接的信息<br>
       *其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
       *从第三号开始是Exon的信息<br>
       *不管怎么加都是从小加到大。
       */	
  	ArrayList<Integer> exonList=new ArrayList<Integer>();
  	splitList.add(exonList);
  }
  
  
	/**
	 * 顺序储存同一基因不同转录本的名字，与splitList相对应
	 */
	private ArrayList<String> lssplitName=new ArrayList<String>();
	/**
	 * 顺序储存同一基因不同转录本的名字，与splitList相对应
	 */
	public void addSplitName(String splitName) {
		lssplitName.add(splitName);
	}
	
	
	
  /**
   * 返回转录本的数目
    * @return
    */
    public int getSplitlistNumber()
    {  
    	return lssplitName.size();
    }
	
    /**
     * 返回转录本名称的List，名称顺序和getExonlist的顺序相同
     */
	public ArrayList<String> getLsSplitename() {
		return lssplitName;
	}
	
    /**
     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
     * 返回某个转录本，其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
     * 不管怎么加都是从小加到大<br>
     */
    public ArrayList<Integer> getExonlist(int splitnum)
    {  
    	return splitList.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     * 返回某个转录本，其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
     * 不管怎么加都是从小加到大<br>
     */
    public ArrayList<Integer> getExonlist(String splitID)
    {  
    	return splitList.get(lssplitName.indexOf(splitID));//include one special loc start number to end number	
    }
    /**
     * 获得该基因中最长的一条转录本名称和方向
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
		int longsplitID = getLongestSplitNum();
		String splitName=lssplitName.get(longsplitID);
		ArrayList<Integer> splitresult=splitList.get(longsplitID);
		result.add(splitName);
		result.add(splitresult);
		return result;
	}
   
	
    /**
     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
     * 返回某个转录本的方向	，这个主要在UCSC中使用，
     * 因为UCSC中有极少部分一个基因中同时存在正向和反向序列，所以用这个来标记每个转录本的方向
     */
    public boolean getCis5to3(int splitnum)
    {  
    	return lsSplitCis5to3.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     * 返回某个转录本的方向	，这个主要在UCSC中使用，
     * 因为UCSC中有极少部分一个基因中同时存在正向和反向序列，所以用这个来标记每个转录本的方向
     */
    public boolean getCis5to3(String splitID)
    {  
    	return lsSplitCis5to3.get(lssplitName.indexOf(splitID));//include one special loc start number to end number	
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
	public boolean getLongestSplitCis5to3() 
	{
		int longsplitID = getLongestSplitNum();
		return lsSplitCis5to3.get(longsplitID);
	}
	
	
	  /**
     * 获得该基因中最长的一条转录本编号，由该编号能够到splitList中获得相应的转录本信息
     * @return 返回一个ArrayList-object
     * 第一个用 String 接收，是该转录本的名称<br>
     * 第二个用ArrayList-Integer接收，是该转录本的具体信息<br>
     * 其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
     */
	public int getLongestSplitNum() 
	{
		if(splitList.size()==1)
		{
			return 0;
		}
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<splitList.size();i++)
		{
			ArrayList<Integer>  subsplit=splitList.get(i);
			lslength.add(subsplit.get(subsplit.size()-1)-subsplit.get(2));
		}
		int max=lslength.get(0);
		for (int i = 0; i < lslength.size(); i++) {
			if(lslength.get(i)>max)
				max=lslength.get(i);
		}
		return lslength.indexOf(max);
	}
	
	
    /**
     * 
     * 获得该基因中最长的一条转录本的部分区域的信息
     * @param type 指定为"Intron","Exon","5UTR","3UTR"
     * @param num 如果type为"Intron"或"Exon"，指定第几个，如果超出，则返回0
     * @return
     */
	public int getTypeLength(String type,int num)  
	{
		ArrayList<Object>  lstmpSplitInfo=getLongestSplit();
		ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
		int exonNum=lstmpSplit.size();
		//TODO 如果超出需要返回0
		if (type.equals("Intron")) 
		{
			int IntronLength=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{
				IntronLength=lstmpSplit.get(num*2+2)-lstmpSplit.get(num*2+1);
			}
			else 
			{
				IntronLength=lstmpSplit.get(exonNum-num*2)-lstmpSplit.get(exonNum-num*2-1);
			}
			return IntronLength;
		}
		if (type.equals("Exon")) 
		{
			int ExonLength=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{
				//转录起点和终点都在外显子之外
				//if(lstmpSplit.get(num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)<=lstmpSplit.get(1))
					ExonLength=lstmpSplit.get(num*2+1)-lstmpSplit.get(num*2);
				/**
				//转录起点和终点都在外显子之内
				else if (lstmpSplit.get(num*2)<=lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)>lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(0);
				//转录起点在外显子内，终点在外显子外
				else if (lstmpSplit.get(num*2)<lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)>lstmpSplit.get(0)&&lstmpSplit.get(num*2+1)<=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(num*2+1)-lstmpSplit.get(0);
				//转录起点在外显子外，终点在外显子内
				else if (lstmpSplit.get(num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(num*2)<lstmpSplit.get(1)&&lstmpSplit.get(num*2+1)>=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(num*2);
				*/
			}
			else //2,3 4,5 6,7 8,9
			{
				//转录起点和终点都在外显子之外
				//if(lstmpSplit.get(exonNum-num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)<=lstmpSplit.get(1))
					ExonLength=lstmpSplit.get(exonNum-num*2+1)-lstmpSplit.get(exonNum-num*2);
			/**
					//转录起点和终点都在外显子之内
				else if (lstmpSplit.get(exonNum-num*2)<=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)>lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(0);
				//转录起点在外显子外，终点在外显子内
				else if (lstmpSplit.get(exonNum-num*2)<lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)>lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2+1)<=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(exonNum-num*2+1)-lstmpSplit.get(0);
				//转录起点在外显子内，终点在外显子外
				else if (lstmpSplit.get(exonNum-num*2)>=lstmpSplit.get(0)&&lstmpSplit.get(exonNum-num*2)<lstmpSplit.get(1)&&lstmpSplit.get(exonNum-num*2+1)>=lstmpSplit.get(1)) 
					ExonLength=lstmpSplit.get(1)-lstmpSplit.get(exonNum-num*2);
			*/
			}
			return ExonLength;
		}
		if (type.equals("5UTR")) 
		{
			int FUTR=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{	
				FUTR=lstmpSplit.get(2)-numberstart;
				for (int i = 3; i <exonNum; i=i+2) 
				{
					if(lstmpSplit.get(i)<=lstmpSplit.get(0))
						FUTR=FUTR+(lstmpSplit.get(i)-lstmpSplit.get(i-1));
					else if (lstmpSplit.get(i-1)<=lstmpSplit.get(0)&&lstmpSplit.get(i)>lstmpSplit.get(0))
						FUTR=FUTR+lstmpSplit.get(0)-lstmpSplit.get(i-1);
					else if (lstmpSplit.get(i-1)>lstmpSplit.get(0)) 
						break;
				}
			}
			else 
			{
				FUTR=numberend-lstmpSplit.get(exonNum-1);
				for (int i = exonNum-2; i >=2; i=i-2) 
				{
					if(lstmpSplit.get(i)>=lstmpSplit.get(1))
						FUTR=FUTR+(lstmpSplit.get(i+1)-lstmpSplit.get(i));
					else if (lstmpSplit.get(i)<lstmpSplit.get(1)&&lstmpSplit.get(i+1)>=lstmpSplit.get(1))
						FUTR=FUTR+lstmpSplit.get(i+1)-lstmpSplit.get(1);
					else if (lstmpSplit.get(i+1)<lstmpSplit.get(1))
						break;
				}
			}
			return FUTR;
		}
		if (type.equals("3UTR")) 
		{
			int TUTR=0;
			if (cis5to3) //2,3 4,5 6,7 8,9
			{	
				TUTR=numberend-lstmpSplit.get(exonNum-1);
				for (int i = exonNum-2; i >=2; i=i-2) 
				{
					if(lstmpSplit.get(i)>=lstmpSplit.get(1))
						TUTR=TUTR+(lstmpSplit.get(i+1)-lstmpSplit.get(i));
					else if (lstmpSplit.get(i)<lstmpSplit.get(1)&&lstmpSplit.get(i+1)>=lstmpSplit.get(1))
						TUTR=TUTR+lstmpSplit.get(i+1)-lstmpSplit.get(1);
					else if (lstmpSplit.get(i+1)<lstmpSplit.get(1))
						break;
				}
			}
			else 
			{
				TUTR=lstmpSplit.get(2)-numberstart;
				for (int i = 3; i <exonNum; i=i+2) {
					if(lstmpSplit.get(i)<=lstmpSplit.get(0))
						TUTR=TUTR+(lstmpSplit.get(i)-lstmpSplit.get(i-1));
					else if (lstmpSplit.get(i-1)<=lstmpSplit.get(0)&&lstmpSplit.get(i)>lstmpSplit.get(0))
						TUTR=TUTR+lstmpSplit.get(0)-lstmpSplit.get(i-1);
					else if (lstmpSplit.get(i-1)>lstmpSplit.get(0)) 
						break;
				}
			}
			return TUTR;
		}

		return -1000000;
	}
	


}
