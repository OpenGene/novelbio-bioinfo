package com.novelBio.other.pixiv.execute;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataOperate.WebFetch;
import com.novelBio.base.fileOperate.FileOperate;

public class PixivOperate 
{
	
	WebFetch pixiv=new WebFetch();
	/**
	 * 获得pixiv的cookies
	 */
    public void getcookies()
    {
	   String[][] postContent=new String[3][2];
	   postContent[0][0]="mode";postContent[0][1]="login";
		   postContent[1][0]="pixiv_id";postContent[1][1]="facemun";
			   postContent[2][0]="pass";postContent[2][1]="f12344321n";
	  BufferedReader aaa= pixiv.PostFetch(postContent,"http://www.pixiv.net/index.php",true);
	  
	  
	   //pixiv.releaseConnection();	   
	/**
	  String content="";
	  try {
		while((content=aaa.readLine())!=null)
		   {
			 System.out.println(content);
		
			 if(content.contains("<title>"))
			 {
				name=catchID(content);
				pixiv.releaseConnection();
				return name;
			 }
			 
		   }
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
   }

    /**
     * 抓取网页中的作者和图片信息，返回二维数组
     * 0：图片
     * 1：作者
     * 如果没抓成功，返回null
     * @param ID
     */
    private String[] execute(String ID)
    {  
    	String url="http://www.pixiv.net/member_illust.php?mode=medium&illust_id="+ID;
    	pixiv.GET_CONTENT_CHARSET="UTF-8";
 	   BufferedReader test=pixiv.GetFetch(url,true);
 	   String content;
 	  String[] name;
 	   try {
 		while((content=test.readLine())!=null)
 		   {
 			 //System.out.println(content);
 		/****/ 
 			 if(content.contains("<title>"))
 			 {
 				name=catchID(content);
 				pixiv.releaseConnection();
 				return name;
 			 }
 		   }
 	} catch (IOException e) {
 		e.printStackTrace();
 	}
 	 pixiv.releaseConnection();
 	 return null;
    }
    
    /**
     * 抓取文件信息
     * @param title
     * @return
     */
    private String[] catchID(String title) 
    {
    	    String[] result=new String[2];
    	    Pattern pattern =Pattern.compile("「(.*?)」", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
    	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
    	  
    	    matcher = pattern.matcher(title);
    	    if(matcher.find())
    	    result[0]=matcher.group(1);
    	    if(matcher.find())
    	    result[1] = matcher.group(1);      
    	    return result;
	}
    
    /**
     * 输入旧文件夹和新文件夹，将旧文件夹里的图片名字到pixiv网上获得名字放到新文件夹里面，按作者放
     * @param filepath
     * @param newPath
     */
    public void readfile(String filepath,String newPath)
    {
    	 File a=new File(filepath);
         String[] file=a.list();
         //匹配文件名与后缀名
         Pattern pattern =Pattern.compile("(\\d*?)(_p\\d*){0,1}\\.(\\w*$)", Pattern.CASE_INSENSITIVE);
 	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
 	  
 	   String name="";
 	   String houzhuiming="";
 	   String namep="";
 	    int ID;//pixiv文件名
        for (int i = 0; i < file.length; i++) 
        {
		   if (!file[i].contains("."))
		   {
			  continue;
		   }
		   
		 //获取文件名与后缀名
		   matcher=pattern.matcher(file[i]);
		  if(matcher.find())
		  {
			  name=matcher.group(1);
			  houzhuiming=matcher.group(3);
			  namep=matcher.group(2);
			  if(namep==null)
			  {
				  namep="";
			  }
		  }
		  else {
			continue;
		}
		  //将文件名改为数字，这个只能pixiv的文件名才能改
		  try {
			   ID=Integer.parseInt(name.trim());
			  
		     } catch (Exception e) {
	
		    	 continue;
		     }  
		   String pixivID=ID+"";
		   String[] filename =  execute(pixivID);  
		   if(filename[0]==null)
		   {
			   continue;
		   }
		   
		   filename[0]=filename[0].replace("\\", "");
		   filename[0]=filename[0].replace("/", "");
		   filename[0]=filename[0].replace("\"", "");
		   filename[0]=filename[0].replace("*", "");
		   filename[0]=filename[0].replace("?", "");
		   filename[0]=filename[0].replace("<", "");
		   filename[0]=filename[0].replace(">", "");
		   filename[0]=filename[0].replace("|", "");
		   
		   
		   filename[1]=filename[1].replace("\\", "");
		   filename[1]=filename[1].replace("/", "");
		   filename[1]=filename[1].replace("\"", "");
		   filename[1]=filename[1].replace("*", "");
		   filename[1]=filename[1].replace("?", "");
		   filename[1]=filename[1].replace("<", "");
		   filename[1]=filename[1].replace(">", "");
		   filename[1]=filename[1].replace("|", "");
		   
		   String oldfilename=filepath+"/"+file[i];
		   /////String newfilename=filename[0]+namep+"."+houzhuiming;
		   //FileOperate.changeFileName(oldfilename, newfilename);//文件改名
		   //FileOperate.moveFile(filepath+"/"+newfilename, newPath+"/"+filename[1]);//移动文件
		   
		   FileOperate.moveFile(oldfilename, newPath+"/"+filename[1],false);//移动文件
		   /////FileOperate.changeFileName(newPath+"/"+filename[1]+"/"+file[i], newfilename);//文件改名
		   
		   System.out.println(name+filename[0]);
		}
    
    }
    
    /**
     * 给定网址，获得所有作者的菜单
     * @param AuthorUrl pixiv某个作者的网址
     * @param SavePath 保存txt文本
     */
    public void downloadPicture(String AuthorUrl,String SavetxtPath) 
    {
    	
    	BufferedReader pixivauther=pixiv.GetFetch(AuthorUrl,true);
    	
    	
    	String tmString="member.php?id=\\d*";
    	TxtReadandWrite pixivtxt=new TxtReadandWrite();
    	pixivtxt.setParameter(SavetxtPath, true,false);
    	String pixivurl="http://www.pixiv.net/";
    	String pixivauthorurl="";
    	//正则表达式抓url
    	Pattern pattern =Pattern.compile("member\\.php\\?id=\\d*", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
    	Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。    	
    	try {
    		while( (tmString=pixivauther.readLine())!=null) {
				if(tmString.contains("<a href=\"member.php?id="))
				{
				
					matcher = pattern.matcher(tmString);   
					if(matcher.find())
						pixivauthorurl=pixivurl+matcher.group();  //  返回抓到的字符串
					pixivtxt.writefile(pixivauthorurl);
					pixivtxt.writefile("\r\n");
				}
			}
  
		} catch (Exception e) {}
	}
    
    
    
    
    
    
    
    
    
}
