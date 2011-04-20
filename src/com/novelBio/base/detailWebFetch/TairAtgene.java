package com.novelBio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.WebFetch;


public class TairAtgene 
{
	WebFetch TairgetAtgene;
	
	/**
	 * 拟南芥的查找gene的url
	 */
   public String Urlatgene="http://www.arabidopsis.org/servlets/TairObject?type=locus&name=";	
	
   /**
    * bufferreader流文件
    */
	private BufferedReader atdetailreader;
   
   /**
    * 返回string[4]数组<br/>
    * 0：description<br/>
    * 1：GObio<br/>
    * 2：GOmol<br/>
    * 3：GOcel<br/>
    * @param AtID
    * @throws IOException
    */
  public String[] AtDetail(String AtID) throws IOException
  {
	  TairgetAtgene=new WebFetch();
	  TairgetAtgene.GetUrl(Urlatgene+AtID);
	  atdetailreader=TairgetAtgene.GetFetch();
	 
	  String content="";
	  String description="";
	  
	  /**
	   * 抓取description
	   */
	  Pattern Patdescrip=Pattern.compile("(?<=>).*?(?=</td>)",Pattern.CASE_INSENSITIVE);
	  Matcher Matdescrip;
	  
	  
	  
	  /**
	   * 抓取GO信息
	   */
	  Pattern GodescripPat=Pattern.compile("(?<=>).*?(?=</A>)",Pattern.CASE_INSENSITIVE);
	  Matcher GodescripMat;
	  
	  /**
	   * Go分类
	   */
	  String Gobio="";String Gomol="";String Gocel="";
	  
	  while ((content=atdetailreader.readLine())!=null)
	  {
		  /**
		   * 查找Description
		   */
		  if(content.contains("\"top\">Description <a href"))
		  {
			  atdetailreader.readLine();//跳过一行
			  content=atdetailreader.readLine();
			  if((Matdescrip=Patdescrip.matcher(content)).find())
			  description=Matdescrip.group();
		  }
		  
		  /**
		   * 查找GO Biological Process
		   */
		  if(content.contains("GO Biological Process"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gobio=Gobio+" "+content.trim();
					 for(int i=0;i<4;i++)//向下读四行
					 {
						 content=atdetailreader.readLine();
					 }
					 GodescripMat=GodescripPat.matcher(content);
					 boolean flag=false;
					 while(GodescripMat.find())
					 {
						Gobio=Gobio+" "+ GodescripMat.group(); 
						flag=true;
					 }
					  if(!flag)
					 {
						 Gobio=Gobio+" wrong";
					 }
					 
				  }
				  if(content.contains("</TR>"))
				  {
					  break;
				  }
				  
			  }
		  }
		  /**
		   * 查找GO Cellular Component
		   */
		  if(content.contains("GO Cellular Component"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gocel=Gocel+" "+content.trim();
					  for(int i=0;i<4;i++)//向下读四行
						 {
							 content=atdetailreader.readLine();
						 }
						 GodescripMat=GodescripPat.matcher(content);
						 
						 
						 boolean flag=false;
						 while(GodescripMat.find())
						 {
							 Gocel=Gocel+" "+ GodescripMat.group();
							flag=true;
						 }
						  if(!flag)
						 {
							  Gocel=Gocel+" wrong";
							
						 }
						  if(content.contains("</TR>"))
						  {
							  break;
						  }
					  }
						 
						 
						 
						 
				  
			  }
		  }
		  /**
		   * 查找GO Molecular Function
		   */
		  if(content.contains("GO Molecular Function"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gomol=Gomol+" "+content.trim();
					  for(int i=0;i<4;i++)//向下读四行
						 {
							 content=atdetailreader.readLine();
						 }
						 GodescripMat=GodescripPat.matcher(content);
						  
						 
						 
						 boolean flag=false;
						 while(GodescripMat.find())
						 {
							 Gomol=Gomol+" "+ GodescripMat.group(); 
							flag=true;
						 }
						  if(!flag)
						 {
							  Gomol=Gomol+" wrong";
						 }
						  if(content.contains("</TR>"))
						  {
							  break;
						  }
					  }
						 
						 
			  }
			  }
				  
	  
		  
		  /**
		   * 跳出循环
		   */
		  if(content.contains("Annotation Detail"))
		  {
			  break;
		  }
	  }
	  
	  String[] Atinfo=new String[4];
	  Atinfo[0]=description;
	  Atinfo[1]=Gobio;
	  Atinfo[2]=Gomol;
	  Atinfo[3]=Gocel;
	  return Atinfo;
  }

   public void close()
   {
	   TairgetAtgene.closeall();
   }








}
