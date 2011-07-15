package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodInfoUCSCgene;
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
public class GffDetailUCSCgene extends GffDetailAbs
{


	  
	/**
	 * 顺序储存同一基因不同转录本的名字，与splitList相对应
	 */
	private ArrayList<String> lsIsoName=new ArrayList<String>();
	/**
	 * 顺序储存同一基因的不同转录本坐标，相应转录本名字保存在splitName中
	 */
	private ArrayList<ArrayList<Integer>> lsIsoform=new ArrayList<ArrayList<Integer>>();//存储可变剪接的mRNA
	/**
	 * 顺序存储每个转录本的方向，这个不要用了，因为如果不一样方向的转录本会放入新的gene中去
	 */
	private ArrayList<Boolean> lsSplitCis5to3 = new ArrayList<Boolean>();


	public GffDetailUCSCgene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 指定最后一个转录本的方向。
	 * 这个主要在UCSC中使用，因为UCSC中有极少部分一个基因中同时存在正向和反向序列，所以用这个来标记每个转录本的方向
	 */
	protected void addCis5to3(boolean cis5to3)
	{
		lsSplitCis5to3.add(cis5to3);
	}
	
	/**
	 * 给最后一个转录本添加exon坐标，其中exonList的第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.<br>
	 * 注意这两个都与基因方向无关，永远第一项小于第二项<br>
     *从第三号开始是Exon的信息<br>
     *不管怎么加都是从小加到大。
	 */
	protected void addExon(int locnumber)
	{
		ArrayList<Integer> exonList=lsIsoform.get(lsIsoform.size()-1);//include one special loc start number to end number	
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
		ArrayList<Integer> exonList=lsIsoform.get(lsIsoform.size()-1);//include one special loc start number to end number	
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
  	lsIsoform.add(exonList);
  }
	/**
	 * 顺序储存同一基因不同转录本的名字，与splitList相对应
	 */
	public void addSplitName(String splitName) {
		lsIsoName.add(splitName);
	}
	/**
   * 返回转录本的数目
    * @return
    */
    public int getSplitlistNumber()
    {  
    	return lsIsoName.size();
    }
	
    /**
     * 返回转录本名称的List，名称顺序和getExonlist的顺序相同
     */
	public ArrayList<String> getLsSplitename() {
		return lsIsoName;
	}
	
    /**
     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
     * 返回某个转录本，其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
     * 不管怎么加都是从小加到大<br>
     */
    public ArrayList<Integer> getExonlist(int splitnum)
    {  
    	return lsIsoform.get(splitnum);//include one special loc start number to end number	
    }
    
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     * 返回某个转录本，其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
     * 不管怎么加都是从小加到大<br>
     */
    public ArrayList<Integer> getExonlist(String splitID)
    {  
    	return lsIsoform.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
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
		String splitName=lsIsoName.get(longsplitID);
		ArrayList<Integer> splitresult=lsIsoform.get(longsplitID);
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
    	return lsSplitCis5to3.get(lsIsoName.indexOf(splitID));//include one special loc start number to end number	
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
		if(lsIsoform.size()==1)
		{
			return 0;
		}
		ArrayList<Integer> lslength=new ArrayList<Integer>();
		for(int i=0;i<lsIsoform.size();i++)
		{
			ArrayList<Integer>  subsplit=lsIsoform.get(i);
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
	///////////////////// 与 coord 相关的属性和方法 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 顺序存储每个转录本的的坐标情况
	 */
	ArrayList<CodIsoInfo> lsCodIsoInfos = new ArrayList<CodIsoInfo>();
	/**
	 * 遍历每个转录本并查找坐标在里面定位的情况，结果保存在lsCodIsoInfos中
	 */
	private void searchCoordInSplice()
	{
		if (!lsCodIsoInfos.isEmpty()) {
			return;
		}
	}
	
}
/**
 * 坐标在单个转录本中的定位情况
 * @author zong0jie
 *
 */
abstract class CodIsoInfo
{
	/**
	 * 标记codInExon处在外显子中
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * 标记codInExon处在内含子中
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * 标记codInExon不在转录本中
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * 标记codInExon处在5UTR中
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * 标记codInExon处在3UTR中
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * 标记codInExon不在UTR中
	 */
	public static final int COD_LOCUTR_OUT = 0;
	
	public CodIsoInfo(String IsoName, ArrayList<Integer> lsIsoform) {
		this.IsoName = IsoName;
		this.lsIsoform = lsIsoform;
	}
	/**
	 * 转录本的名字
	 */
	protected String IsoName = "";
	/**
	 * 坐标
	 */
	protected int coord = -100;
	/**
	 * 转录本方向
	 */
	protected boolean cis5to3;
	/**
	 * 转录本具体信息
	 */
	protected ArrayList<Integer> lsIsoform;
	
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * 坐标在起点上游为负数，下游为正数
	 */
	protected int cod2start = -1000000000;
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * 坐标在终点上游为负数，下游为正数
	 */
	protected int cod2end = -1000000000;
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
	 */
	protected int exIntronNum = -1;
	/**
	 * 坐标在外显子、内含子还是在该转录本外
	 * 与codLocExon和codLocIntron比较即可
	 */
	protected int codLoc = 0;
	
	/**
	 * 坐标在5UTR、3UTR还是不在
	 */
	protected int codLocUTR = 0;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2start/cod2cdsEnd
	 */
	protected int UTRstart = -100000000;
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2atg/cod2End
	 */
	protected int UTRend = -100000000;
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子起点的距离
	 * 都为正数
	 */
	protected int cod2ExInStart = -1000000000;
	/**
	 * 如果坐标在外显子/内含子中，
	 * 坐标与该外显子/内含子终点的距离
	 * 都为正数
	 */
	protected int cod2ExInEnd = -1000000000;
	/**
	 * 坐标与该ATG的距离
	 */
	protected int cod2ATG = -1000000000;
	/**
	 * 坐标与CDSend的距离
	 */
	protected int cod2cdsEnd = -1000000000;
	/**
	 * 该转录本的长度
	 */
	protected int lengthIso = -100;
	
	public void setCoord(int coord) {
		this.coord = coord;
	}
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * 在转录本的哪个位置
	 * 有COD_LOC_EXON，COD_LOC_INTRON，COD_LOC_OUT三种
	 * @return
	 */
	public int getCodLocUTR() {
		return codLoc;
	}
	/**
	 * 坐标到该转录本起点的距离，考虑正反向
	 * @return
	 */
	public int getCod2IsoStart() {
		return cod2start;
	}
	/**
	 * 坐标到该转录本终点的距离，考虑正反向
	 * @return
	 */
	public int getCod2IsoEnd() {
		return cod2end;
	}
	/**
	 * 坐标在第几个外显子或内含子中，如果不在就为负数
	 * @return
	 */
	public int getExInNum() {
		return exIntronNum;
	}
	/**
	 * 坐标到该外显子/内含子起点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * 坐标到该外显子/内含子终点的距离，考虑正反向
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * 坐标到ATG的距离，考虑正反向
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的起点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2start/cod2cdsEnd
	 */
	public int getCod2UTRstart() {
		return UTRstart;
	}
	/**
	 * 使用前先判定在UTR中
	 * 如果坐标在UTR中，坐标距离UTR的终点，注意这个会去除内含子
	 * 不去除内含子的直接用cod2atg/cod2End
	 */
	public int getCod2UTRend() {
		return UTRend;
	}
	public abstract void searchCoord();
	
}
/**
 *   tss>---coord-----atg--------->-----------tes---->
 * @author zong0jie
 *
 */
class CodIsoInfoCis extends CodIsoInfo
{
	private static final Logger logger = Logger.getLogger(CodIsoInfoCis.class);
	public CodIsoInfoCis(String IsoName, ArrayList<Integer> lsIsoform) {
		super(IsoName, lsIsoform);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 需要检查一遍
	 */
	@Override
	public void searchCoord()
	{
		  /**
	     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
	     * 返回某个转录本，其中第一项是该转录本的Coding region start，第二项是该转录本的Coding region end.注意这两个都与基因方向无关，永远第一项小于第二项<br>
	     * 从第三项开始是exon的信息，exon成对出现，第一个exon坐标是该转录本的转录起点，最后一个exon坐标是该转录本的转录终点<br>
	     * 不管怎么加都是从小加到大<br>
	     */
		if (    coord < lsIsoform.get(2) || 
				coord > lsIsoform.get(lsIsoform.size()-1)  	)
		{
			codLoc = COD_LOC_OUT;
		}
		cod2ATG = coord - lsIsoform.get(0);
		cod2cdsEnd = coord - lsIsoform.get(1);
		cod2start = coord - lsIsoform.get(2);
		cod2end = coord - lsIsoform.get(lsIsoform.size() - 1);
		boolean flag=false; //false表示coord比最后一个外显子的最后一位还大，也就是下面的循环没有能将flag设置为true,那就是3UTR，并且设为在外显子
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (codLoc == COD_LOC_OUT) {
			return;
		}
		for(int j=2; j<lsIsoform.size(); j++)  //一个一个Exon的检查
		{
			if(coord<lsIsoform.get(j) && j%2==0)//在外显子之前（内含子中），外显子为： 2,3  4,5  6,7  8,9   0该转录本的转录起点，1该转录本的转录终点
			{
				flag=true;
				//以下不对
			   if(j==2)//在5‘UTR中,也算做在外显子中   tss cod ，2 3 ， 4 5 ， 6 7 ， 8 9 
			   {
				   logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
			   }
			   else // 在内含子中
			   {
				   // tss ，2 3 ， 4 5 ，cod 6 7 ， 8 9
				   codLoc = COD_LOC_INTRON;
				   exIntronNum = j / 2 - 1;// 在第j/2-1个内含子中
				   cod2ExInEnd = lsIsoform.get(j) - coord;// 距后一个外显子
				   cod2ExInStart = coord - lsIsoform.get(j - 1);// 距前一个外显子
				   break; // 跳出本转录本的检查，开始上一层的循环，检查下一个转录本
			   }
			}
			else if(coord <= lsIsoform.get(j) && j%2 == 1) //在外显子之中，外显子为：2,3  4,5  6,7  8,9   0该转录本的转录起点，1该转录本的转录终点
			{
				flag=true;
				codLoc = COD_LOC_EXON;
				exIntronNum = (j-1)/2;//在第(j-1)/2个外显子中
				cod2ExInEnd = lsIsoform.get(j) - coord;//距离本外显子终止
				cod2ExInStart = coord - lsIsoform.get(j-1);//距离本外显子起始
				if(coord<lsIsoform.get(0))//坐标小于atg，在5‘UTR中,也是在外显子中
				{
					codLocUTR = COD_LOCUTR_5UTR;
					UTRstart=0;UTRend=0;
					// tss  2 3,   4 5,   6 cod 7,   8 0 9
					for (int k = 3; k <= j-2; k=k+2) {
						UTRstart = UTRstart + lsIsoform.get(k) - lsIsoform.get(k-1);
					}
					UTRstart = UTRstart + cod2ExInStart;
					// tss  2 3,   4 5,   6 cod  0 7
					if (lsIsoform.get(0) <= lsIsoform.get(j)) //一定要小于等于
					{
						UTRend = lsIsoform.get(0) - coord;
					}
					// tss  2 3,   4 5,   6 cod 7,   8  9,   10 0 11
					else 
					{
						UTRend = lsIsoform.get(j) - coord;
						int m = j+2;
						while (m < lsIsoform.size() && lsIsoform.get(0) > lsIsoform.get(m)) 
						{
							UTRend = UTRend + lsIsoform.get(m) - lsIsoform.get(m-1);
							m=m+2;
						}
						UTRend = UTRend + lsIsoform.get(0) - lsIsoform.get(m-1);
					}
					break;//跳出
				}
				// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 11
				if(coord > lsIsoform.get(1))//大于cds起始区，在3‘UTR中
				{
					codLocUTR = COD_LOCUTR_3UTR; UTRstart=0;UTRend=0;
					// tss  2 3,   4 0 5,   6 1 cod 7,   8  9,   10 11
					if (lsIsoform.get(1)>=lsIsoform.get(j-1))//一定要大于等于 
					{
						UTRstart=coord-lsIsoform.get(1);
					}
					// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 cod 11
					else 
					{
						UTRstart=coord-lsIsoform.get(j-1);
						int m=j-3;
						while (m>=2&&lsIsoform.get(m)>lsIsoform.get(1)) 
						{
							UTRstart=UTRstart+lsIsoform.get(m+1)-lsIsoform.get(m);
							m=m-2;
						}
						UTRstart=UTRstart+lsIsoform.get(m+1)-lsIsoform.get(1);
					}
					/////////////////////utrend//////////////////
					// tss  2 3,   4 0 5,   6 1 7,   8  cod 9,   10 11,  12 13
					for (int k = lsIsoform.size() - 1; k >= j+2; k=k-2) {
						UTRend=UTRend+lsIsoform.get(k)-lsIsoform.get(k-1);
					}
					UTRend=UTRend+cod2ExInEnd;
					break;//跳出
				}
				break;//跳出本转录本的检查，开始上一层的循环，检查下一个转录本
			}
		}
		if (flag == false)//比最后一个外显子的最后一位还大，也就是上面的循环没有能将flag设置为true,那就是3UTR，并且设为在外显子
		{
			logger.error("coord is out of the isoform, but the codLoc is: "+codLoc+" coord: "+ coord + IsoName);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
}

class CodIsoInfoTrans extends CodIsoInfo
{

	public CodIsoInfoTrans(String IsoName, ArrayList<Integer> lsIsoform) {
		super(IsoName, lsIsoform);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void searchCoord() {
		// TODO Auto-generated method stub
		
	}
	


}