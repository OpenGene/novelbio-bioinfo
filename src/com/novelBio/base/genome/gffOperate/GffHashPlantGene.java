package com.novelBio.base.genome.gffOperate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.TxtReadandWrite;





/**
 * 获得Gff的基因数组信息,本类必须实例化才能使用<br/>
 * 输入Gff文件，最后获得两个哈希表和一个list表,
 * 结构如下：<br/>
 * 1.hash（ChrID）--ChrList--GffDetail(GffDetail类,实际是GffDetailGene子类)<br/>
 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
 * chr格式，全部小写 chr1,chr2,chr11<br/>
 * 
 * 2.hash（LOCID）--GffDetail，其中LOCID代表具体的基因编号 <br/>
 * 
 * 3.list（LOCID）--LOCList，按顺序保存LOCID<br/>
 * 
 * 每个基因的起点终点和CDS的起点终点保存在GffDetailList类中<br/>
 */
public class GffHashPlantGene extends GffHashGene{
	


	

	/**
	 * 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认  "AT\\w{1}G\\d{5}"
	 * 水稻是 "LOC_Os\\d{2}g\\d{5}";
	 */
	public String GeneName="AT\\w{1}G\\d{5}";
	
	/**
	 * 可变剪接mRNA的正则，水稻是："(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";，默认拟南芥" (?<=AT\\w{1}G\\d{5}\\.)\\d"
	 */
	public String splitmRNA="(?<=AT\\w{1}G\\d{5}\\.)\\d";
	
	/**
	 * 最底层读取gff的方法，本方法只能读取UCSCknown gene<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表<br/>
	 * 结构如下：<br/>
     * 输入Gff文件，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * 其中LOCID代表具体的条目编号，在UCSCkonwn gene里面没有转录本一说，
	 * 只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存LOCID,这里不考虑多个转录本，每一个转录本就是一个单独的LOCID <br>
     * <b>4. LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起： NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
   public  Hashtable<String, ArrayList<GffDetail>>  ReadGffarray(String gfffilename) throws Exception
   {
		//实例化四个表
		locHashtable =new Hashtable<String, GffDetail>();//存储每个LOCID和其具体信息的对照表
		Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//一个哈希表来存储每条染色体
		LOCIDList=new ArrayList<String>();//顺序存储每个基因号，这个打算用于提取随机基因号
		LOCChrHashIDList=new ArrayList<String>();
		
	   TxtReadandWrite txtgff=new TxtReadandWrite();
	   txtgff.setParameter(gfffilename, false,true);
	   BufferedReader reader=txtgff.readfile();//open gff file
	   
	   ArrayList<GffDetail> LOCList = null;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
	   //基因名字
	   Pattern genepattern =Pattern.compile(GeneName, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher genematcher;
       
	   //mRNA可变剪接的序号
	   Pattern mRNApattern =Pattern.compile(splitmRNA, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher mRNAmatcher;
	   String content="";
	   String chrnametmpString=""; //染色体的临时名字
	   boolean UTR5start = false; boolean UTR3start = false; boolean UTR5end = false; boolean UTR3end = false;
	   boolean CDSstart = false; boolean CDSend = false; boolean mRNAsplit = false;//是否结束了一个mRNA
	   int cdsStart = -100; int cdsEnd = -100; int mRNAstart = -100;  int mRNAend = -100; 
	   GffDetailUCSCgene gffDetailLOC= null;
	   while((content=reader.readLine())!=null)//读到结尾
	   {
		   if(content.charAt(0)=='#')
		   {
			   continue;
		   }
		   String[] ss=content.split("\t");//按照tab分开
		   chrnametmpString=ss[0].toLowerCase();//小写的chrID
		 //新的染色体
			if (!Chrhash.containsKey(chrnametmpString)) //新的染色体
			{
				if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么截短并装入LOCChrHashIDList
				{
					LOCList.trimToSize();
					 //把peak名称顺序装入LOCIDList
					   for (GffDetail gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				}
				LOCList=new ArrayList<GffDetail>();//新建一个LOCList并放入Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			
		   /**
		    * 当读取到gene时，就是读到了一个新的基因，那么将这个基因的起点，终点和每个CDS的长度都放入list数组中
		    */
		   if (ss[2].equals("gene")) //when read the # and the line contains gene, it means the new LOC
       	   {
			   if (mRNAsplit) {
				   //将上一组mRNA的信息装入
				   if (cdsStart < 0 && cdsEnd <0) {
					   cdsStart = mRNAend;
					   cdsEnd = mRNAend;
				   }
				   gffDetailLOC.addExon(0,cdsEnd,false); 
				   gffDetailLOC.addExon(0,cdsStart,false);
				   if (cdsStart <0 || cdsEnd<0 || cdsStart > cdsEnd) {
					   System.out.println("GffHashPlantGeneError: 文件  "+gfffilename+"  本组或上组基因有问题，cdsStart或cdsEnd出错  " +gffDetailLOC.locString);
				   }
				   mRNAsplit = false;//全新的基因，将其归位false
			   }
			   /**
			    * 每当出现一个新的Chr，那么就将这个Chr加入哈希表
			    * chr格式，全部小写 chr1,chr2,chr11
			    */
			   genematcher = genepattern.matcher(content);//查找基因名字
       		   if(genematcher.find())//找到了
       		   {
       			   gffDetailLOC=new GffDetailUCSCgene();//新建一个基因类
       			   gffDetailLOC.locString=genematcher.group(); 
       			   gffDetailLOC.numberstart=Integer.parseInt(ss[3].toLowerCase());gffDetailLOC.numberend=Integer.parseInt(ss[4]);//基因起止
       			   gffDetailLOC.ChrID=chrnametmpString;
       		      /**
       		       * 基因的正反向
       		       */
       		      if( ss[6].equals("+"))
       		    	  gffDetailLOC.cis5to3=true;
       		      else if(ss[6].equals("-"))
       		    	  gffDetailLOC.cis5to3=false;
       		      LOCList.add(gffDetailLOC);//添加进入LOClist
       		      locHashtable.put(gffDetailLOC.locString, gffDetailLOC);//添加进入hash（LOCID）--GeneInforlist哈希表，确定各个基因和他们的类之间的关系    
       		      LOCIDList.add(gffDetailLOC.locString);
       		   }
       		   else {
       			   System.out.println("GffHashPlantGeneError: 文件  "+gfffilename+"  在本行可能没有指定的基因ID  " +content);
       		   }
       		   //重置标签，表示在5UTR和CDS的前面了，那么在后面 if 遇到的的就是第一个UTR或第一个CDS
       		   UTR5start = true; 
       		   CDSstart = true;
       	   }
		   /**
       	    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
       	    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
       	    * 
       	    * 一旦出现了mRNA，就要开始指定5UTR，3UTR，CDS的起点和终止
       	    *
       	    */
		   else if (ss[2].equals("mRNA")) 
		   {
			   if (mRNAsplit) {
				   //将上一组mRNA的信息装入
				   if (cdsStart < 0 && cdsEnd <0) {
					   cdsStart = mRNAend;
					   cdsEnd = mRNAend;
				   }
				   gffDetailLOC.addExon(0,cdsEnd,false); 
				   gffDetailLOC.addExon(0,cdsStart,false);
				   if (cdsStart <0 || cdsEnd<0 || cdsStart >= cdsEnd) {
					   System.out.println("GffHashPlantGeneError: 文件  "+gfffilename+"  本组或上组基因有问题，cdsStart或cdsEnd出错  " +gffDetailLOC.locString);
				   }
				   mRNAsplit =false;
			   }
			   mRNAmatcher = mRNApattern.matcher(content);
			   if(mRNAmatcher.find())
			   {
				   //每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
				   gffDetailLOC.addSplitName(mRNAmatcher.group());
				   //添加一个转录本，然后将相应信息:
				   //第一项是该转录本的Coding region start，第二项是该转录本的Coding region end,从第三项开始是该转录本的Exon坐标信息
				   gffDetailLOC.addsplitlist();
				   //仿照UCSC的做法，如果是一个非编码的mRNA，那么cdsStart = cdsEnd = mRNAend
				   mRNAstart = Integer.parseInt(ss[3]); mRNAend = Integer.parseInt(ss[4]); 
				   cdsStart = -100; cdsEnd = -100;
			   }
			   else {
				   System.out.println("GffHashPlantGeneError: 文件  "+gfffilename+"  在本行可能没有指定的基因ID  " +content);
			   }
			   //重置标签，表示在5UTR和CDS的前面了，那么在后面 if 遇到的的就是第一个UTR或第一个CDS
       		   UTR5start = true; 
       		   CDSstart = true;
     	   }
		   
		   //遇到5UTR
		   else if (ss[2].equals("five_prime_UTR")) 
		   {
			   //不管顺反，都要从小加到大
			   if (gffDetailLOC.cis5to3) 
			   {
				   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//添加子坐标,从小加到大
				   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
			   }
			   else//反着装
			   {
				   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
				   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//添加子坐标,从小加到大
				  
			   }
			   //5UTR过去了
			   UTR5start = false;
			   UTR5end = true;//5UTR会有结束
			   mRNAsplit = true;//全新的基因，将其归位false
			   CDSstart = true; 
		   }
		   else if (ss[2].equals("CDS"))
		   {
			   if (CDSstart)
			   {
				   if (UTR5end) 
				   {
					   if (gffDetailLOC.cis5to3) {
						   gffDetailLOC.addExon(-1,Integer.parseInt(ss[3]),true);
						   gffDetailLOC.addExon(Integer.parseInt(ss[4]));//添加子坐标,从小加到大
					   }
					   else {
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),true);//添加子坐标，从小加到大
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);
					   }
					   UTR5start = false;
					   UTR5end = false;
				   }
				   else {
					   if (gffDetailLOC.cis5to3) {
						   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//添加子坐标,从小加到大
						   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
					   }
					   else {//反着装
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//添加子坐标,从小加到大
					   }
				   }
				   CDSstart = false;
				   CDSend = true;
				   cdsStart = Integer.parseInt(ss[3]);
				   cdsEnd = Integer.parseInt(ss[4]);
			   }
			   else {
				   if (gffDetailLOC.cis5to3) {
					   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//添加子坐标,从小加到大
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
					   cdsEnd = Integer.parseInt(ss[4]);
				   }
				   else {//反着装
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//添加子坐标,从小加到大
					   cdsStart = Integer.parseInt(ss[3]);
				   }
				   CDSend = true;
			   }
			   mRNAsplit = true;//全新的基因，将其归位false
		   }
		   else if (ss[2].equals("three_prime_UTR")) 
		   {
			   if (UTR5end || CDSend) {//紧跟着最后一个CDS了
				   //不管顺反，都要从小加到大
				   if (gffDetailLOC.cis5to3) 
				   {
					   gffDetailLOC.addExon(-1,Integer.parseInt(ss[3]),true);//添加子坐标,从小加到大
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
				   }
				   else//反着装
				   {
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),true);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//添加子坐标,从小加到大
				   }
				   UTR5end = false; 
				   CDSend = false; //已经不是紧跟着最后一个CDS了
			   }
			   else {
				   //不管顺反，都要从小加到大
				   if (gffDetailLOC.cis5to3) 
				   {
					   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//添加子坐标,从小加到大
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
				   }
				   else//反着装
				   {
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//添加子坐标,从小加到大
				   }
			   }
			   //5UTR过去了
			   UTR3start = false;
			   UTR3end = true;//5UTR会有结束
			   mRNAsplit = true;//全新的基因，将其归位false
		   }
	   }
	   if (mRNAsplit) {
		   //将上一组mRNA的信息装入
		   if (cdsStart < 0 && cdsEnd <0) {
			   cdsStart = mRNAend;
			   cdsEnd = mRNAend;
		   }
		   gffDetailLOC.addExon(0,cdsEnd,false); 
		   gffDetailLOC.addExon(0,cdsStart,false);
		   if (cdsStart <0 || cdsEnd<0 || cdsStart > cdsEnd) {
			   System.out.println("GffHashPlantGeneError: 文件  "+gfffilename+"  本组或上组基因有问题，cdsStart或cdsEnd出错  " +gffDetailLOC.locString);
		   }
		   mRNAsplit = false;//全新的基因，将其归位false
	   }
	   
	   LOCList.trimToSize();
	   //把peak名称顺序装入LOCIDList
	   for (GffDetail gffDetail : LOCList) {
		   LOCChrHashIDList.add(gffDetail.locString);
	   }
	   txtgff.close();
	   return Chrhash;//返回这个LOCarray信息
   }
   
   /**
	 * 	返回外显子总长度，内含子总长度等信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength 不包括5UTR和3UTR的长度 <br> 
	 * 3: allIntronLength <br>
	 * 4: allup2kLength <br>
	 * 5: allGeneLength <br>
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Long> getGeneStructureLength()
	{
		ArrayList<Long> lsbackground=new ArrayList<Long>();
		
		long ChrLength=0;
		long allGeneLength=0;
		long allIntronLength=0;
		long allExonLength=0;
		long all5UTRLength=0;
		long all3UTRLength=0;
		long allup2kLength=0;

		int errorNum=0;//看UCSC中有多少基因的TSS不是最长转录本的起点
		/////////////////////正   式   计   算//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //一条一条染色体的去检查内含子和外显子的长度
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    allup2kLength=allup2kLength+chrLOCNum*2000;
		    for (int i = 0; i < chrLOCNum; i++) 
			{
		    	long leftUTR=0;
		    	long rightUTR=0;
				GffDetailUCSCgene tmpUCSCgene=(GffDetailUCSCgene)val.get(i);
				
				allGeneLength=allGeneLength+(tmpUCSCgene.numberend-tmpUCSCgene.numberstart);
			//获得最长的转录本
				ArrayList<Object>  lstmpSplitInfo=tmpUCSCgene.getLongestSplit();
				ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
				
				///////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
				if ((tmpUCSCgene.cis5to3&&lstmpSplit.get(2)>tmpUCSCgene.numberstart) || ( !tmpUCSCgene.cis5to3&& lstmpSplit.get(lstmpSplit.size()-1)<tmpUCSCgene.numberend )){
					errorNum++;
				}
				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				///////////////////////// 内 含 子 加 和 ////////////////////////////////////////
				for (int j = 4; j < lstmpSplit.size(); j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					allIntronLength=allIntronLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
				}
				//////////////////////////////5UTR、外显子、3UTR 加和////////////////////////////////////////////////////
				int exonSize=lstmpSplit.size();                  // start  2,3   4,0,5   6,7  8,9   10,1,11  12,13 end
				leftUTR=lstmpSplit.get(2)-tmpUCSCgene.numberstart;
				rightUTR=tmpUCSCgene.numberend-lstmpSplit.get(exonSize-1);
				for (int j = 3; j <exonSize;j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					//转录起点在外显子后
					if(lstmpSplit.get(j)<=lstmpSplit.get(0))
					{
						leftUTR=leftUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					//转录起点在外显子中
					if (lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0) ) 
					{
						leftUTR=leftUTR+(lstmpSplit.get(0)-lstmpSplit.get(j-1));
						//转录终点在同一个外显子中
						if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							allExonLength=allExonLength+(lstmpSplit.get(1)-lstmpSplit.get(0));
						}
						else 
						{
							allExonLength=allExonLength+(lstmpSplit.get(j)-lstmpSplit.get(0));
						}
						continue;
					}
					//转录起点在外显子前，转录终点在外显子后
					if(lstmpSplit.get(j-1)>lstmpSplit.get(0)&&lstmpSplit.get(j)<lstmpSplit.get(1))
					{
						allExonLength=allExonLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					
					
					//转录终点在外显子中
					if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
					{    //转录起点在同一个外显子中
						if(lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0))
						{
							continue;//上面已经计算过了
						}
						else 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							allExonLength=allExonLength+(lstmpSplit.get(1)-lstmpSplit.get(j-1));
						}
						continue;
					}
					//转录终点在外显子前
					if (lstmpSplit.get(j-1)>=lstmpSplit.get(1)) 
					{
						rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
				}
				if (tmpUCSCgene.cis5to3) 
				{
					all5UTRLength=all5UTRLength+leftUTR;
					all3UTRLength=all3UTRLength+rightUTR;
				}
				else 
				{
					all5UTRLength=all5UTRLength+rightUTR;
					all3UTRLength=all3UTRLength+leftUTR;
				}
			}
		}
		lsbackground.add(all5UTRLength);
		lsbackground.add(all3UTRLength);
		lsbackground.add(allExonLength);
		lsbackground.add(allIntronLength);
		lsbackground.add(allup2kLength);
		lsbackground.add(allGeneLength);
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点"+errorNum);
		return lsbackground;
		
	}
	
}



