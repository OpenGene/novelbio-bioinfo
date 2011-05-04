package com.novelBio.base.genome.gffOperate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
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
public class CopyOfGffHashPlantGene extends GffHash{
	


	

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
      		    /**
      		     * 每当出现一个新的Chr，那么就将这个Chr加入哈希表
       		     * chr格式，全部小写 chr1,chr2,chr11
      		     */
			  
			   genematcher = genepattern.matcher(content);//查找基因名字
       		   if(genematcher.find())//找到了
       		   {
       			   GffDetail LOC=new GffDetailUCSCgene();//新建一个基因类
       			   LOC.locString=genematcher.group(); 
       			   LOC.numberstart=Integer.parseInt(ss[3].toLowerCase());LOC.numberend=Integer.parseInt(ss[4]);//基因起止
       			   LOC.ChrID=chrnametmpString;
       		      /**
       		       * 基因的正反向
       		       */
       		      if( ss[6].equals("+"))
       		    	  LOC.cis5to3=true;
       		      else if(ss[6].equals("-"))
       		    	  LOC.cis5to3=false;
       		      LOCList.add(LOC);//添加进入LOClist
       		      locHashtable.put(LOC.locString, LOC);//添加进入hash（LOCID）--GeneInforlist哈希表，确定各个基因和他们的类之间的关系    
       		      LOCIDList.add(LOC.locString);
       		   }
       	   }
      	   /**
       	    * 当读取到mRNA时，就是说是可变剪接时，添加一个新的可变剪接list
       	    * 不管怎么加都是从第一个cds开始加到最后一个cds，正向的话就是从小加到大，反向就是从大加到小。
       	    */
		   else if (ss[2].equals("mRNA")) 
		   {
			   mRNAmatcher = mRNApattern.matcher(content);
			   GffDetailUCSCgene lastGffdetailUCSCgene =  (GffDetailUCSCgene) LOCList.get(LOCList.size()-1);
			   if(mRNAmatcher.find())
			   {
				   //每遇到一个mRNA就添加一个可变剪接,先要类型转换为子类
				 
				   lastGffdetailUCSCgene.addSplitName(mRNAmatcher.group());
					//添加一个转录本，然后将相应信息:
					//第一项是该转录本的Coding region start，第二项是该转录本的Coding region end,从第三项开始是该转录本的Exon坐标信息
					lastGffdetailUCSCgene.addsplitlist();
					//仿照UCSC的做法，如果是一个非编码的mRNA，那么cdsStart = cdsEnd = mRNAend
					int cdsStart = -100; int cdsEnd = -100; int mRNAstart = Integer.parseInt(ss[3]);  int mRNAend = Integer.parseInt(ss[4]); 
					
			   }
       	        while((content = reader.readLine())!=null
       	        		&&content.charAt(0)!='#'
       	        			&&!content.contains("three")
       	        			&&!content.contains("gene"))//读到结尾，这里可能有问题
       	        {
       	        	ss=content.split("\t");//按照tab分开
       	        	if(ss[2].equals("CDS"))
       	        	{ 
       	        		if(LOCList.get(LOCList.size()-1).cis5to3)
       	        		{
       	        		 ((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[3]));//添加子坐标,从小加到大
       	        		 ((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[4]));
       	        		}
       	        		else 
       	        		{
       	        			((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[4]));//添加子坐标,反着加，从大加到小
       	        			((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[3]));
       	        		}
       	        	}
       	        	else 
       	        	{
       	        		continue;
       	        	}
       	        }
       	        if (content.contains("gene"))
    	        {
       	        	ss=content.split("\t");//按照tab分开
    	         /**
    			     * 每当出现一个新的Chr，那么就将这个Chr加入哈希表
    			     * chr格式，全部小写 chr1,chr2,chr11
    			     */
       	        	if(!chrnametmpString.equals(ss[0]))//每当出现一个新的Chr，那么就将这个Chr加入哈希表
    	  		   {
       	        		LOCList=new ArrayList<GffDetail>(); 
       	        		chrnametmpString=ss[0].toLowerCase();//把这个记录下来
       	        		Chrhash.put(chrnametmpString,LOCList);
	    	  		   }
       	        	genematcher = genepattern.matcher(content);
       	        	if(genematcher.find())
       	        	{
       	        		GffDetailGene LOC=new GffDetailGene();//新建一个基因类
       	        		LOC.locString=genematcher.group(); 
       	        		LOC.numberstart=Integer.parseInt(ss[3].toLowerCase());LOC.numberend=Integer.parseInt(ss[4]);//基因起止
       	        		LOC.ChrID=chrnametmpString;
    			      
    			      /**
    			       * 基因的正反向
    			       */
    			      if( ss[6].equals("+"))
    			      {
    			    	  LOC.cis5to3=true;
    			      }
    			      else if(ss[6].equals("-"))
    		    	   {
    			    	  LOC.cis5to3=false;
    			       }
    			      LOCList.add(LOC);//添加进入LOClist
    			      locHashtable.put(LOC.locString, LOC);//添加进入hash（LOCID）--GeneInforlist哈希表，确定各个基因和他们的类之间的关系    
    			      LOCIDList.add(LOC.locString);
    			    }
    	        }
     	   }
       	}
   	return null;//返回这个LOCarray信息
   }
   

}



