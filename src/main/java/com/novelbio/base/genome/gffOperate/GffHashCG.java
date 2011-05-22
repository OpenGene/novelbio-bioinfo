package com.novelbio.base.genome.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import com.novelbio.base.dataOperate.TxtReadandWrite;



/**
 * 获得UCSC中CpG等Gff的条目信息,本类必须实例化才能使用<br/>
 * 输入Gff文件，最后获得两个哈希表和一个list表,
 * 结构如下：<br/>
 * 1.hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br/>
 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
 * chr格式，全部小写 chr1,chr2,chr11<br/>
 * 
 * 2.hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号,本类中定义为：107_chr1_CpG: 128_36568608 <br/>
	 * 具体为：#bin_chrom_name_chromStart
 * 
 * 3.list（LOCID）--LOCList，按顺序保存LOCID<br/>
 * 
 * 每个基因的起点终点和CDS的起点终点保存在GffDetailList类中<br/>
 */
public class GffHashCG extends GffHash
{	
	/**
	 * 最底层读取gff的方法<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表,
	 * 结构如下：<br/>
	 * @3.LOCIDList
	 * （LOCID）--LOCIDList，按顺序保存LOCID<br/>
	 ** <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类,,实际是GffDetailCG子类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * （LOCID）--GffDetail，其中LOCID代表具体的条目编号,,本类中定义为：107_chr1_CpG: 128_36568608 <br/>
     *  * 具体为：#bin_chrom_name_chromStart<br>
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存LOCID<br>
     * <b>LOCChrHashIDList </b><br>
     *  LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致<br>
     *
	 * @throws Exception 
	 */
	@Override
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename) throws Exception 
	{
		  //实例化三个表
		   locHashtable =new Hashtable<String, GffDetail>();//存储每个LOCID和其具体信息的对照表
		   Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//一个哈希表来存储每条染色体
		   LOCIDList=new ArrayList<String>();//顺序存储每个基因号，这个打算用于提取随机基因号
		   LOCChrHashIDList=new ArrayList<String>();//顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID
		   //为读文件做准备
		   TxtReadandWrite txtgff=new TxtReadandWrite();
		   txtgff.setParameter(gfffilename, false,true);
		   BufferedReader reader=txtgff.readfile();//open gff file
	       
		   String[] ss = null;//存储分割数组的临时变量
		   String content="";
		   //临时变量
		   ArrayList<GffDetail> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
		   String chrnametmpString=""; //染色体的临时名字
		   
		   reader.readLine();//跳过第一行
		   while((content=reader.readLine())!=null)//读到结尾
		   {
			   ss=content.split("\t");
			   chrnametmpString=ss[1].toLowerCase();//小写
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			    //新的染色体
			   if (!Chrhash.containsKey(chrnametmpString)) //新的染色体
			   {
				   if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
				   {
					   LOCList.trimToSize();
					   //我收集的一个list/array排序的方法，很简单易用
					   Collections.sort(LOCList,new Comparator<GffDetail>(){
				            public int compare(GffDetail arg0, GffDetail arg1) {
				                int Compareresult;
				            	if(arg0.numberstart<arg1.numberstart)
				            		Compareresult=-1;
				            	else if (arg0.numberstart==arg1.numberstart) 
				            		Compareresult=0;
				            	else 
				            		Compareresult=1;
				            	return Compareresult;
				            }
				        });
					   //排序完后把CG号装入LOCIDList
					   for (GffDetail gffDetail : LOCList) {
						   LOCIDList.add(gffDetail.locString);
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				   }
				   LOCList=new ArrayList<GffDetail>();//新建一个LOCList并放入Chrhash
				   Chrhash.put(chrnametmpString, LOCList);
			   }
			  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			   //每一行就是一个CG
			   GffDetailCG gffGCtmpDetail=new GffDetailCG();
			   gffGCtmpDetail.ChrID=chrnametmpString;//是小写的
			   gffGCtmpDetail.cis5to3=true;
			   gffGCtmpDetail.locString=ss[0]+"_"+ss[1]+"_"+ss[4]+"_"+ss[2];
			   gffGCtmpDetail.numberstart=Integer.parseInt(ss[2]);
			   gffGCtmpDetail.numberend=Integer.parseInt(ss[3]);
			   gffGCtmpDetail.lengthCpG=Integer.parseInt(ss[5]);
			   gffGCtmpDetail.numCpG=Integer.parseInt(ss[6]);
			   gffGCtmpDetail.numGC=Integer.parseInt(ss[7]);
			   gffGCtmpDetail.perCpG=Double.parseDouble(ss[8]);
			   gffGCtmpDetail.perGC=Double.parseDouble(ss[9]);
			   gffGCtmpDetail.obsExp=Double.parseDouble(ss[10]);
			   //装入LOCList和locHashtable
			   LOCList.add(gffGCtmpDetail);  
			   locHashtable.put(gffGCtmpDetail.locString, gffGCtmpDetail);
		   }
		   /////////////////////////////////////////////////////////////////////////////////////////////
		   LOCList.trimToSize();
		   //最后结束后再排个序。
		   Collections.sort(LOCList,new Comparator<GffDetail>(){
	            public int compare(GffDetail arg0, GffDetail arg1) {
	                int Compareresult;
	            	if(arg0.numberstart<arg1.numberstart)
	            		Compareresult=-1;
	            	else if (arg0.numberstart==arg1.numberstart) 
	            		Compareresult=0;
	            	else 
	            		Compareresult=1;
	            	return Compareresult;
	            }});
		 //排序完后装入LOCIDList
		   for (GffDetail gffDetail : LOCList) {
			   LOCIDList.add(gffDetail.locString);
			   LOCChrHashIDList.add(gffDetail.locString);
		}
		   txtgff.close();
		 /////////////////////////////////////////////////////////////////////////////////////////////////
	   	return Chrhash;//返回这个LOCarray信息
	}

	/**
	 * 返回CG比例，以hash表形式返回
	 * @return
	 */
	public Hashtable<String, Integer> getLength() 
	{
		int LOCNum=LOCIDList.size();
		Hashtable<String, Integer> hashCGLength=new Hashtable<String, Integer>();
		
		for (int i = 0; i < LOCNum; i++) 
		{
			GffDetailCG gffDetailCG=(GffDetailCG)locHashtable.get(LOCIDList.get(i));
			int tmpLength=gffDetailCG.numberend-gffDetailCG.numberstart;
			String tmpCGClass="CpG";//就只有一种CpG
			if (hashCGLength.containsKey(tmpCGClass)) //含有已知的repeat，则把repeat的长度累加上去
			{
				tmpLength=tmpLength+hashCGLength.get(tmpCGClass);
				hashCGLength.put(tmpCGClass, tmpLength);
			}
			else//不含有则把新的repeat加进去 
			{
				hashCGLength.put(tmpCGClass,tmpLength);
			}
		}
		return hashCGLength;
	}

}
