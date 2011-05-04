package com.novelBio.base.genome.genomestudy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * 读取水稻拟南芥同源基因并保存为Hash表
 * @author Administrator
 *
 */
public class OrthologyGene 
{
	
	/**
	 * 每个拟南芥对应的水稻同源基因<br/>
	 * 拟南芥为key
	 * value为list--里面为同源的水稻基因
	 */
	Hashtable<String, ArrayList<String>> HashAthomoOs;
	
	/**
	 * 每个水稻对应的拟南芥同源基因<br/>
	 * 水稻为key
	 * value为list--里面为同源的拟南芥基因
	 */
	Hashtable<String, ArrayList<String>> HashOshomoAt;
	
   /**
 * 输入list和要添加的元素
 * 当元素与list里面没有重复时才添加，有重复不添加
 * @param list
 * @param obj
 */
    private void AddNonRepList(List<String> list,String obj) 
    {   
    	obj=obj.trim();
    	int length=list.size();
	      for (int i = 0; i <length; i++) 
	      {
		    if(list.get(i).contains(obj))
		    {
		  	  return; 
		    }
	   	  }
	   	  list.add(obj);
    } 

public void ReadFile(String filename)
   {
	   
	   File file=new File(filename);
	   BufferedReader reader=null;
	   HashOshomoAt=new Hashtable<String, ArrayList<String>>();
	   HashAthomoOs=new Hashtable<String, ArrayList<String>>();
	   try
	   	 {
	   	 reader=new BufferedReader(new FileReader(file));//open gff file
	   	 }
	        catch (Exception e) {
	   	// TODO: handle exception
	       	return;
	        }        
	   	String content="";
	   	String[] ss;//保存 把content用"\t"切割后生成的数组
	   	String[] tmpLOC;//保存把ss[4]用" "切割后生成的数组，是所有的LOC编号
	   	try 
	   	 {
	       	while((content=reader.readLine())!=null)//读到结尾
	       	{  
	       		ss= content.split("\t");
	       		
	       		//不包含水稻和拟南芥，跳过
	       		if(!ss[3].contains("rice")||!ss[3].contains("Arabidopsis"))
	       		{
	       			continue;
	       		}
	       		
	       		/**
	       		 * 如果该行中包含了水稻和拟南芥的同源基因
	       		 * 那么将该行中的水稻和拟南芥的对应关系装入哈希表
	       		 * 两个哈希表：水稻--拟南芥同源list
	       		 * 拟南芥--水稻同源list
	       		 */
	       		
	       		   tmpLOC= ss[4].split(" ");
	       		   int length=tmpLOC.length;
	       		   ArrayList<String> OsList=new ArrayList<String>();
	       		   ArrayList<String> AtList=new ArrayList<String>();
	       		  
	       		   for(int i=0; i<length; i++)//将水稻和拟南芥分别装入list
	       		   {
	       			   
	       			   if(tmpLOC[i].trim().startsWith("LOC_Os"))
	       			   {   
	       				   AddNonRepList(OsList, tmpLOC[i]);
	       			   }
	       			   if(tmpLOC[i].trim().startsWith("AT"))
	       			   {   
	       				   AddNonRepList(AtList, tmpLOC[i]);
	       			   }
	       		   }
	       		 
   				    
	       		   int oslength=OsList.size();
	       		   int atlength=AtList.size();
	       		   
	       		 //水稻对应拟南芥的哈希表
	       		 for(int i=0; i<oslength; i++)
	       		 {
	       			 if(HashOshomoAt.get(OsList.get(i))==null)//如果是哈希表中第一次出现的元素，那么直接装入哈希表
	       			 {
	       			    HashOshomoAt.put(OsList.get(i), AtList);
	       			 }
	       			 else//如果以前已经装过类似的元素 
	       			 {
						List<String> attmpList=HashOshomoAt.get(OsList.get(i));
						for(int j=0;j<atlength;j++)
						{
							AddNonRepList(attmpList,AtList.get(j));
						}
					 }
	       		 }
	       		 //拟南芥对应水稻的哈希表
	       		 for(int i=0; i<atlength; i++)
	       		 {
	       			 if(HashAthomoOs.get(AtList.get(i))==null)//如果是哈希表中第一次出现的元素，那么直接装入哈希表
	       			 {
	       				HashAthomoOs.put(AtList.get(i), OsList);
	       			 }
	       			 else//如果以前已经装过类似的元素 
	       			 {
	       				List<String> ostmpList=HashAthomoOs.get(AtList.get(i));
	       				for(int j=0;j<oslength;j++)
						{
							AddNonRepList(ostmpList,OsList.get(j));
						}
					 }
	       		 }
	       		
	       		
	       		
	        }
	       	System.out.println("OshomoAt"+HashOshomoAt.size()+"AthomoOs"+HashAthomoOs.size());
	   	 }
	   	catch (Exception e) {
			// TODO: handle exception
		}
   }
    




}
