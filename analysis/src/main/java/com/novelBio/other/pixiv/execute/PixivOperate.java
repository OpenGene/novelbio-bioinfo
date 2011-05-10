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
	 * ���pixiv��cookies
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
     * ץȡ��ҳ�е����ߺ�ͼƬ��Ϣ�����ض�ά����
     * 0��ͼƬ
     * 1������
     * ���ûץ�ɹ�������null
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
     * ץȡ�ļ���Ϣ
     * @param title
     * @return
     */
    private String[] catchID(String title) 
    {
    	    String[] result=new String[2];
    	    Pattern pattern =Pattern.compile("��(.*?)��", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
    	    Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
    	  
    	    matcher = pattern.matcher(title);
    	    if(matcher.find())
    	    result[0]=matcher.group(1);
    	    if(matcher.find())
    	    result[1] = matcher.group(1);      
    	    return result;
	}
    
    /**
     * ������ļ��к����ļ��У������ļ������ͼƬ���ֵ�pixiv���ϻ�����ַŵ����ļ������棬�����߷�
     * @param filepath
     * @param newPath
     */
    public void readfile(String filepath,String newPath)
    {
    	 File a=new File(filepath);
         String[] file=a.list();
         //ƥ���ļ������׺��
         Pattern pattern =Pattern.compile("(\\d*?)(_p\\d*){0,1}\\.(\\w*$)", Pattern.CASE_INSENSITIVE);
 	    Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
 	  
 	   String name="";
 	   String houzhuiming="";
 	   String namep="";
 	    int ID;//pixiv�ļ���
        for (int i = 0; i < file.length; i++) 
        {
		   if (!file[i].contains("."))
		   {
			  continue;
		   }
		   
		 //��ȡ�ļ������׺��
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
		  //���ļ�����Ϊ���֣����ֻ��pixiv���ļ������ܸ�
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
		   //FileOperate.changeFileName(oldfilename, newfilename);//�ļ�����
		   //FileOperate.moveFile(filepath+"/"+newfilename, newPath+"/"+filename[1]);//�ƶ��ļ�
		   
		   FileOperate.moveFile(oldfilename, newPath+"/"+filename[1],false);//�ƶ��ļ�
		   /////FileOperate.changeFileName(newPath+"/"+filename[1]+"/"+file[i], newfilename);//�ļ�����
		   
		   System.out.println(name+filename[0]);
		}
    
    }
    
    /**
     * ������ַ������������ߵĲ˵�
     * @param AuthorUrl pixivĳ�����ߵ���ַ
     * @param SavePath ����txt�ı�
     */
    public void downloadPicture(String AuthorUrl,String SavetxtPath) 
    {
    	
    	BufferedReader pixivauther=pixiv.GetFetch(AuthorUrl,true);
    	
    	
    	String tmString="member.php?id=\\d*";
    	TxtReadandWrite pixivtxt=new TxtReadandWrite();
    	pixivtxt.setParameter(SavetxtPath, true,false);
    	String pixivurl="http://www.pixiv.net/";
    	String pixivauthorurl="";
    	//������ʽץurl
    	Pattern pattern =Pattern.compile("member\\.php\\?id=\\d*", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
    	Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������    	
    	try {
    		while( (tmString=pixivauther.readLine())!=null) {
				if(tmString.contains("<a href=\"member.php?id="))
				{
				
					matcher = pattern.matcher(tmString);   
					if(matcher.find())
						pixivauthorurl=pixivurl+matcher.group();  //  ����ץ�����ַ���
					pixivtxt.writefile(pixivauthorurl);
					pixivtxt.writefile("\r\n");
				}
			}
  
		} catch (Exception e) {}
	}
    
    
    
    
    
    
    
    
    
}
