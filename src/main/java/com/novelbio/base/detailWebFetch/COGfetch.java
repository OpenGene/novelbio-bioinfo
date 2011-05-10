package com.novelbio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.WebFetch;


public class COGfetch 
{
	   WebFetch COGFetch;
	   
	   /**
	    * tair网站blast的网址
	    */
	   private String COGUrl="http://www.ncbi.nlm.nih.gov/COG/old/xognitor.cgi";
	 
	   /**
	    * post的内容
	    */
	   private String[][] COGpostData=new String[2][2];
	   
	   /**
	    * COG的结果流文件
	    */
	   private BufferedReader COGReader;
	   
	   /**
	    * post的延迟时间，默认为3秒
	    */
	   public int COGdelaytime=3;
	   
	   
	
	  
	   
	   
	   

	   /**
	    * 提供水稻序列，上COG网站<br/>
	    * 查找该蛋白序列的COG<br/>
	    * 读取网页有错误就抛出<br/>
	    * @throws IOException 
	    * 返回数组，保存该蛋白COG信息<br/>
		* 0：COGID
	    * 1：COGnum
	    * 2：COGtext
		* 如果[0]号为wrong 说明本网页读取有问题<br/>
	    */
	   public String[] COGPost(String seq) throws IOException
		{
		   COGFetch=new WebFetch();
		   COGFetch.postSleepTime=COGdelaytime;	
		   COGFetch.GetUrl(COGUrl);//获得TIGRpost网址
			/**
			 * 指定post内容
			 */
		    COGpostData[0][0]="hit"; COGpostData[0][1]="3";
		    COGpostData[1][0]="seq";COGpostData[1][1]=seq;
			 

		    COGFetch.GetPostContent(COGpostData);
		    COGReader= COGFetch.PostFetch();
			
			String[] COGinfo=ncbiCOGinformation(COGReader);
	      return  COGinfo;
		}


	   /**
	    * 正则表达式抓COG信息，返回string[3]数组
	    * 存储COG的信息
	    * 0：COGID
	    * 1：COGnum
	    * 2：COGtext
	    */
       private String[] ncbiCOGinformation(BufferedReader COGReader) throws IOException//获得COG信息
       { 
    	   String[] COGinfo=new String[3];
    	   
    	   COGinfo[0]="";COGinfo[1]="";COGinfo[2]="";
    	   /**
    	    * 相关的正则表达式
    	    */
    	 
           Pattern regCOGnom =Pattern.compile("\\w*(?=</a></th>)",Pattern.CASE_INSENSITIVE);//COGnumber
           Matcher matCOGnom;
           //Pattern PatCOGid = Pattern.compile("(?<=\\bcolor=.*>).*(?=</font></a></th>)",Pattern.CASE_INSENSITIVE);//COGID
           Pattern PatCOGid = Pattern.compile("\\bcolor=.*>(.*)</font></a></th>",Pattern.CASE_INSENSITIVE);
           Matcher matCOGid;
           Pattern regCOGtxt = Pattern.compile("(?<=>).*?(?=</th>)",Pattern.CASE_INSENSITIVE);//COGtext
           Matcher matCOGtxt;
           
          String content;
           while ((content=COGReader.readLine()) !=null)//读取该网页
           {
              
               if (content.contains("Anonymous"))
               {
                   while ((content=COGReader.readLine()) !=null)
                   {
                       if (content.contains("NO related COG"))
                       {
                    	   COGinfo[2] = "NO related COG";
                           return COGinfo;
                       }
                       if (content.contains("No hits"))
                       {
                    	   COGinfo[2] = "No hits";
                           return COGinfo;
                       }
                       matCOGid = PatCOGid.matcher(content);
                       if (matCOGid.find())
                       {
                    	   COGinfo[0] = matCOGid.group(1);
                           content = COGReader.readLine();
                           matCOGnom = regCOGnom.matcher(content);
                           if (matCOGnom.find())
                           {
                        	   COGinfo[1] = matCOGnom.group();
                           }


                           content = COGReader.readLine();
                           matCOGtxt = regCOGtxt.matcher(content);
                           if (matCOGtxt.find())
                           {
                        	   COGinfo[2] = matCOGtxt.group();
                               return COGinfo;
                           }


                       }
                   }
               }
           }
           COGinfo[0] = "wrong";
           return COGinfo;
       }
     public void close() 
     {
    	 COGFetch.closeall();
	 }


}
