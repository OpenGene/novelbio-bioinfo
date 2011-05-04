package com.novelBio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.WebFetch;




/**
 * ����������TIGR post LOC��� ���GO��Ϣ��������ַ��
 * ���ǲ��������ӻ�ȡ��������<br/>
 * ������Ҫʵ����
 * @author Zong Jie
 *
 */
public class TIGRPostGO 
{
	/**
	 * ������TIGR��վpost LOC���
	 */
	WebFetch TIGRLOC;
	
	/**
	 * TIGR��ѯLOCID����ַ
	 */
	public static String  TIGRurl = "http://rice.plantbiology.msu.edu/cgi-bin/ORF_infopage.cgi";
	
	/**
	 * LOCIDpost������
	 */
	public String[][] postData=new String[2][2];
	
	
	
	  /**
	    * post���ӳ�ʱ�䣬Ĭ��Ϊ1��
	    */
	   public int postdelaytime=1;
	   
	   
	
	
	BufferedReader LOCIDreader;
	
	/**
	 * ���캯����ʵ�����Ժ��Զ�ʵ����TIGRLOC
	 */
	public TIGRPostGO()
	{
	
	}

	
	/**
	 * ��õ������ӵ���ַ
	 * ���캯������ʵ������һ��TIGRLOC,��ô�����������Ͳ�����ÿ��ʵ����
	 * ����LOCID
	 * ����String[4]����<br/>
	 * 0��protein����<br/>
	 * 1��GObio<br/>
	 * 2��GOmol<br/>
	 * 3��GOcel<br/>
	 * @param LOCID
	 * @throws IOException 
	 */
	public String[]TIGRLOCpost(String LOCID) throws IOException
	{
		
		TIGRLOC=new WebFetch();
		TIGRLOC.postSleepTime=postdelaytime;
		TIGRLOC.GetUrl(TIGRurl);//���TIGRpost��ַ
		/**
		 * ָ��post����
		 */
		postData[0][0]="db";
		postData[0][1]="osa1r5";
		postData[1][0]="orf";
		postData[1][1]=LOCID;
		TIGRLOC.GetPostContent(postData);
		
		LOCIDreader= TIGRLOC.PostFetch();
		
		/**
		 * Ҫ���ص�����
		 */
		String[] Tigrinfo=new String[4];
		
		 Tigrinfo=ProteinUrl(LOCIDreader);
	
       return  Tigrinfo;
	}


/**
 * ����TigrLOCID����õ�ҳ�棬��ȡ��������
 * @param TigrLOCID
 * @return
 * @throws IOException 
 */
    private String[] ProteinUrl(BufferedReader TigrLOCID) throws IOException 
    {
    	
    	/**
		 * ץTIGR���������ӵ�������ʽ
		 */
		 Pattern PatSeqUrl= Pattern.compile("(?<=a href=\").*(?=\">Download)",  Pattern.CASE_INSENSITIVE);
	     Matcher matchsequenceurl;
         String content="";
         /**
          * ��������
          */
         String proteinurl="";

    	  
    	        while ((content=TigrLOCID.readLine())!= null)//��ȡ����ҳ
    	        {
    	        	matchsequenceurl = PatSeqUrl.matcher(content);     
    	            if (matchsequenceurl.find())
    	            {

    	            	proteinurl = "http://rice.plantbiology.msu.edu" + matchsequenceurl.group();
    	                break;
    	            }
    	        }
    	       

    	         /**
    	          * GOID������ʽ
    	          */
    			 Pattern PatGOID= Pattern.compile("(?<=\">)GO:\\d*(?=<)",Pattern.CASE_INSENSITIVE);
    		     Matcher matchGOID;
    		     
    		     /**
    	          * GO��������������ʽ
    	          */
    			 Pattern PatGOinfo= Pattern.compile("(?<=\"centersmall\">).*?(?=<)",Pattern.CASE_INSENSITIVE);
    		     Matcher matchinfo;
    		     

    		     
    	        
    	         
    	         /**
    	          * �洢GO��Ϣ������
    	          * 0��biological process
    	          * 1��molecular function
    	          * 2��cellular component
    	          */
    	         String[] GOclass=new String[4];
    	         
    	         String  tmp="";

    	         GOclass[0]=proteinurl;
    	         GOclass[1]="";
    	         GOclass[2]="";
    	         GOclass[3]="";
    	        
    	        		 
    	    	        while ((content=TigrLOCID.readLine())!= null)//��ȡ����ҳ
    	    	        {
    	    	        	/**
    	    	        	 * ����GO���˳�
    	    	        	 */
    	    	        	if (content.contains("No Gene Ontology annotation found"))
    	    	        	{
    	    	        		GOclass[1]="No Gene Ontology annotation found";
    	    	        		break;	
    	    	        	}
    	    	        	
    	    	        	/**
    	    	        	 * ����Ⱦɫ��Ҳ�˳�
    	    	        	 */
    	    	        	if (content.contains("Chromosome"))
    	    	        	{
    	    	        		break;
    	    	        	}
    	    	        	
    	    	        	
    	    	        	/**
    	    	        	 * �ҵ�GO������ź�ץȡ��Ϣ
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
    	    	            	else//������Ϣ
    	    	            	{
    	    	            		GOclass[1]="wrong"+GOclass[1];
    							}
    	    	            }
    	    	        }
    	    	   
    		
    	    	       return GOclass;
    	
	}
    
    
	/**
	 * ץTIGR��GO��Ϣ
     * �洢GO��Ϣ������
     * 0��biological process
     * 1��molecular function
     * 2��cellular component
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
