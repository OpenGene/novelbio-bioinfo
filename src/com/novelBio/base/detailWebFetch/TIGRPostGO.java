package com.novelBio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.WebFetch;




/**
 * 本类用来上TIGR post LOC编号 获得GO信息和序列网址，
 * 但是不进入连接获取蛋白序列<br/>
 * 本类需要实例化
 * @author Zong Jie
 *
 */
public class TIGRPostGO 
{
	/**
	 * 用来上TIGR网站post LOC编号
	 */
	WebFetch TIGRLOC;
	
	/**
	 * TIGR查询LOCID的网址
	 */
	public static String  TIGRurl = "http://rice.plantbiology.msu.edu/cgi-bin/ORF_infopage.cgi";
	
	/**
	 * LOCIDpost的内容
	 */
	public String[][] postData=new String[2][2];
	
	
	
	  /**
	    * post的延迟时间，默认为1秒
	    */
	   public int postdelaytime=1;
	   
	   
	
	
	BufferedReader LOCIDreader;
	
	/**
	 * 构造函数，实例化以后自动实例化TIGRLOC
	 */
	public TIGRPostGO()
	{
	
	}

	
	/**
	 * 获得蛋白链接的网址
	 * 构造函数里面实例化了一个TIGRLOC,那么这个方法里面就不用在每次实例化
	 * 输入LOCID
	 * 返回String[4]数组<br/>
	 * 0：protein连接<br/>
	 * 1：GObio<br/>
	 * 2：GOmol<br/>
	 * 3：GOcel<br/>
	 * @param LOCID
	 * @throws IOException 
	 */
	public String[]TIGRLOCpost(String LOCID) throws IOException
	{
		
		TIGRLOC=new WebFetch();
		TIGRLOC.postSleepTime=postdelaytime;
		TIGRLOC.GetUrl(TIGRurl);//获得TIGRpost网址
		/**
		 * 指定post内容
		 */
		postData[0][0]="db";
		postData[0][1]="osa1r5";
		postData[1][0]="orf";
		postData[1][1]=LOCID;
		TIGRLOC.GetPostContent(postData);
		
		LOCIDreader= TIGRLOC.PostFetch();
		
		/**
		 * 要返回的数组
		 */
		String[] Tigrinfo=new String[4];
		
		 Tigrinfo=ProteinUrl(LOCIDreader);
	
       return  Tigrinfo;
	}


/**
 * 输入TigrLOCID所获得的页面，提取蛋白链接
 * @param TigrLOCID
 * @return
 * @throws IOException 
 */
    private String[] ProteinUrl(BufferedReader TigrLOCID) throws IOException 
    {
    	
    	/**
		 * 抓TIGR网蛋白链接的正则表达式
		 */
		 Pattern PatSeqUrl= Pattern.compile("(?<=a href=\").*(?=\">Download)",  Pattern.CASE_INSENSITIVE);
	     Matcher matchsequenceurl;
         String content="";
         /**
          * 蛋白链接
          */
         String proteinurl="";

    	  
    	        while ((content=TigrLOCID.readLine())!= null)//读取该网页
    	        {
    	        	matchsequenceurl = PatSeqUrl.matcher(content);     
    	            if (matchsequenceurl.find())
    	            {

    	            	proteinurl = "http://rice.plantbiology.msu.edu" + matchsequenceurl.group();
    	                break;
    	            }
    	        }
    	       

    	         /**
    	          * GOID正则表达式
    	          */
    			 Pattern PatGOID= Pattern.compile("(?<=\">)GO:\\d*(?=<)",Pattern.CASE_INSENSITIVE);
    		     Matcher matchGOID;
    		     
    		     /**
    	          * GO具体内容正则表达式
    	          */
    			 Pattern PatGOinfo= Pattern.compile("(?<=\"centersmall\">).*?(?=<)",Pattern.CASE_INSENSITIVE);
    		     Matcher matchinfo;
    		     

    		     
    	        
    	         
    	         /**
    	          * 存储GO信息的数组
    	          * 0：biological process
    	          * 1：molecular function
    	          * 2：cellular component
    	          */
    	         String[] GOclass=new String[4];
    	         
    	         String  tmp="";

    	         GOclass[0]=proteinurl;
    	         GOclass[1]="";
    	         GOclass[2]="";
    	         GOclass[3]="";
    	        
    	        		 
    	    	        while ((content=TigrLOCID.readLine())!= null)//读取该网页
    	    	        {
    	    	        	/**
    	    	        	 * 不含GO就退出
    	    	        	 */
    	    	        	if (content.contains("No Gene Ontology annotation found"))
    	    	        	{
    	    	        		GOclass[1]="No Gene Ontology annotation found";
    	    	        		break;	
    	    	        	}
    	    	        	
    	    	        	/**
    	    	        	 * 读到染色体也退出
    	    	        	 */
    	    	        	if (content.contains("Chromosome"))
    	    	        	{
    	    	        		break;
    	    	        	}
    	    	        	
    	    	        	
    	    	        	/**
    	    	        	 * 找到GO分类符号后，抓取信息
    	    	        	 */
    	    	        	matchGOID = PatGOID.matcher(content);   
    	    	       
    	    	            if (matchGOID.find())
    	    	            {
    	    	            	tmp= matchGOID.group();
    	    	            	content=TigrLOCID.readLine();
    	    	            	if(content.contains("biological_process"))
    	    	            	{
    	    	            		content=TigrLOCID.readLine();
    	    	            		if((matchinfo=PatGOinfo.matcher(content)).find())
    	    	            		GOclass[1]=GOclass[1]+" "+tmp+""+matchinfo.group();
    	    	            	}
    	    	            	else if (content.contains("molecular_function")) 
    	    	            	{
    	    	            		content=TigrLOCID.readLine();
    	    	            		if((matchinfo=PatGOinfo.matcher(content)).find())
    	    	            		GOclass[2]=GOclass[2]+" "+tmp+""+matchinfo.group();
    	    	            		
    							}
    	    	            	else if (content.contains("cellular_component"))
    	    	            	{
    	    	            		content=TigrLOCID.readLine();
    	    	            		if((matchinfo=PatGOinfo.matcher(content)).find())
    	    	            		GOclass[3]=GOclass[3]+" "+tmp+""+matchinfo.group();
    							}
    	    	            	else//错误信息
    	    	            	{
    	    	            		GOclass[1]="wrong"+GOclass[1];
    							}
    	    	            }
    	    	        }
    	    	   
    		
    	    	       return GOclass;
    	
	}
    
    
	/**
	 * 抓TIGR网GO信息
     * 存储GO信息的数组
     * 0：biological process
     * 1：molecular function
     * 2：cellular component
	 * @throws IOException 
     */
  //  private String[] GOinfo(BufferedReader TigrLOCID) throws IOException 
    {

    }



   public void close()
   {
	   TIGRLOC.closeall();
   }















}
