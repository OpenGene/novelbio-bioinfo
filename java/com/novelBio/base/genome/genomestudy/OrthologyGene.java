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
 * ��ȡˮ�����Ͻ�ͬԴ���򲢱���ΪHash��
 * @author Administrator
 *
 */
public class OrthologyGene 
{
	
	/**
	 * ÿ�����Ͻ��Ӧ��ˮ��ͬԴ����<br/>
	 * ���Ͻ�Ϊkey
	 * valueΪlist--����ΪͬԴ��ˮ������
	 */
	Hashtable<String, ArrayList<String>> HashAthomoOs;
	
	/**
	 * ÿ��ˮ����Ӧ�����Ͻ�ͬԴ����<br/>
	 * ˮ��Ϊkey
	 * valueΪlist--����ΪͬԴ�����Ͻ����
	 */
	Hashtable<String, ArrayList<String>> HashOshomoAt;
	
   /**
 * ����list��Ҫ��ӵ�Ԫ��
 * ��Ԫ����list����û���ظ�ʱ����ӣ����ظ������
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
	   	String[] ss;//���� ��content��"\t"�и�����ɵ�����
	   	String[] tmpLOC;//�����ss[4]��" "�и�����ɵ����飬�����е�LOC���
	   	try 
	   	 {
	       	while((content=reader.readLine())!=null)//������β
	       	{  
	       		ss= content.split("\t");
	       		
	       		//������ˮ�������Ͻ棬����
	       		if(!ss[3].contains("rice")||!ss[3].contains("Arabidopsis"))
	       		{
	       			continue;
	       		}
	       		
	       		/**
	       		 * ��������а�����ˮ�������Ͻ��ͬԴ����
	       		 * ��ô�������е�ˮ�������Ͻ�Ķ�Ӧ��ϵװ���ϣ��
	       		 * ������ϣ��ˮ��--���Ͻ�ͬԴlist
	       		 * ���Ͻ�--ˮ��ͬԴlist
	       		 */
	       		
	       		   tmpLOC= ss[4].split(" ");
	       		   int length=tmpLOC.length;
	       		   ArrayList<String> OsList=new ArrayList<String>();
	       		   ArrayList<String> AtList=new ArrayList<String>();
	       		  
	       		   for(int i=0; i<length; i++)//��ˮ�������Ͻ�ֱ�װ��list
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
	       		   
	       		 //ˮ����Ӧ���Ͻ�Ĺ�ϣ��
	       		 for(int i=0; i<oslength; i++)
	       		 {
	       			 if(HashOshomoAt.get(OsList.get(i))==null)//����ǹ�ϣ���е�һ�γ��ֵ�Ԫ�أ���ôֱ��װ���ϣ��
	       			 {
	       			    HashOshomoAt.put(OsList.get(i), AtList);
	       			 }
	       			 else//�����ǰ�Ѿ�װ�����Ƶ�Ԫ�� 
	       			 {
						List<String> attmpList=HashOshomoAt.get(OsList.get(i));
						for(int j=0;j<atlength;j++)
						{
							AddNonRepList(attmpList,AtList.get(j));
						}
					 }
	       		 }
	       		 //���Ͻ��Ӧˮ���Ĺ�ϣ��
	       		 for(int i=0; i<atlength; i++)
	       		 {
	       			 if(HashAthomoOs.get(AtList.get(i))==null)//����ǹ�ϣ���е�һ�γ��ֵ�Ԫ�أ���ôֱ��װ���ϣ��
	       			 {
	       				HashAthomoOs.put(AtList.get(i), OsList);
	       			 }
	       			 else//�����ǰ�Ѿ�װ�����Ƶ�Ԫ�� 
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
