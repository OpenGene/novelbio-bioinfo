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
	 * ���Ͻ�Ĳ���gene��url
	 */
   public String Urlatgene="http://www.arabidopsis.org/servlets/TairObject?type=locus&name=";	
	
   /**
    * bufferreader���ļ�
    */
	private BufferedReader atdetailreader;
   
   /**
    * ����string[4]����<br/>
    * 0��description<br/>
    * 1��GObio<br/>
    * 2��GOmol<br/>
    * 3��GOcel<br/>
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
	   * ץȡdescription
	   */
	  Pattern Patdescrip=Pattern.compile("(?<=>).*?(?=</td>)",Pattern.CASE_INSENSITIVE);
	  Matcher Matdescrip;
	  
	  
	  
	  /**
	   * ץȡGO��Ϣ
	   */
	  Pattern GodescripPat=Pattern.compile("(?<=>).*?(?=</A>)",Pattern.CASE_INSENSITIVE);
	  Matcher GodescripMat;
	  
	  /**
	   * Go����
	   */
	  String Gobio="";String Gomol="";String Gocel="";
	  
	  while ((content=atdetailreader.readLine())!=null)
	  {
		  /**
		   * ����Description
		   */
		  if(content.contains("\"top\">Description <a href"))
		  {
			  atdetailreader.readLine();//����һ��
			  content=atdetailreader.readLine();
			  if((Matdescrip=Patdescrip.matcher(content)).find())
			  description=Matdescrip.group();
		  }
		  
		  /**
		   * ����GO Biological Process
		   */
		  if(content.contains("GO Biological Process"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gobio=Gobio+" "+content.trim();
					 for(int i=0;i<4;i++)//���¶�����
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
		   * ����GO Cellular Component
		   */
		  if(content.contains("GO Cellular Component"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gocel=Gocel+" "+content.trim();
					  for(int i=0;i<4;i++)//���¶�����
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
		   * ����GO Molecular Function
		   */
		  if(content.contains("GO Molecular Function"))
		  {
			  while ((content=atdetailreader.readLine())!=null&&!content.contains("td class=\"sm\" valign=\"top\" align=\"left\">"))
			  {
				  if(content.contains("<td class=\"sm\" valign=\"top\" nowrap>"))
				  {
					  content=atdetailreader.readLine();
					  Gomol=Gomol+" "+content.trim();
					  for(int i=0;i<4;i++)//���¶�����
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
		   * ����ѭ��
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
