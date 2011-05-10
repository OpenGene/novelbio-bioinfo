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
	    * tair��վblast����ַ
	    */
	   private String COGUrl="http://www.ncbi.nlm.nih.gov/COG/old/xognitor.cgi";
	 
	   /**
	    * post������
	    */
	   private String[][] COGpostData=new String[2][2];
	   
	   /**
	    * COG�Ľ�����ļ�
	    */
	   private BufferedReader COGReader;
	   
	   /**
	    * post���ӳ�ʱ�䣬Ĭ��Ϊ3��
	    */
	   public int COGdelaytime=3;
	   
	   
	
	  
	   
	   
	   

	   /**
	    * �ṩˮ�����У���COG��վ<br/>
	    * ���Ҹõ������е�COG<br/>
	    * ��ȡ��ҳ�д�����׳�<br/>
	    * @throws IOException 
	    * �������飬����õ���COG��Ϣ<br/>
		* 0��COGID
	    * 1��COGnum
	    * 2��COGtext
		* ���[0]��Ϊwrong ˵������ҳ��ȡ������<br/>
	    */
	   public String[] COGPost(String seq) throws IOException
		{
		   COGFetch=new WebFetch();
		   COGFetch.postSleepTime=COGdelaytime;	
		   COGFetch.GetUrl(COGUrl);//���TIGRpost��ַ
			/**
			 * ָ��post����
			 */
		    COGpostData[0][0]="hit"; COGpostData[0][1]="3";
		    COGpostData[1][0]="seq";COGpostData[1][1]=seq;
			 

		    COGFetch.GetPostContent(COGpostData);
		    COGReader= COGFetch.PostFetch();
			
			String[] COGinfo=ncbiCOGinformation(COGReader);
	      return  COGinfo;
		}


	   /**
	    * ������ʽץCOG��Ϣ������string[3]����
	    * �洢COG����Ϣ
	    * 0��COGID
	    * 1��COGnum
	    * 2��COGtext
	    */
       private String[] ncbiCOGinformation(BufferedReader COGReader) throws IOException//���COG��Ϣ
       { 
    	   String[] COGinfo=new String[3];
    	   
    	   COGinfo[0]="";COGinfo[1]="";COGinfo[2]="";
    	   /**
    	    * ��ص�������ʽ
    	    */
    	 
           Pattern regCOGnom =Pattern.compile("\\w*(?=</a></th>)",Pattern.CASE_INSENSITIVE);//COGnumber
           Matcher matCOGnom;
           //Pattern PatCOGid = Pattern.compile("(?<=\\bcolor=.*>).*(?=</font></a></th>)",Pattern.CASE_INSENSITIVE);//COGID
           Pattern PatCOGid = Pattern.compile("\\bcolor=.*>(.*)</font></a></th>",Pattern.CASE_INSENSITIVE);
           Matcher matCOGid;
           Pattern regCOGtxt = Pattern.compile("(?<=>).*?(?=</th>)",Pattern.CASE_INSENSITIVE);//COGtext
           Matcher matCOGtxt;
           
          String content;
           while ((content=COGReader.readLine()) !=null)//��ȡ����ҳ
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
