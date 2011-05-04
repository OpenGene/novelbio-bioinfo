package com.novelBio.base.genome.genomestudy;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hssf.record.formula.functions.True;
import org.apache.poi.hwpf.usermodel.Table;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.genome.gffOperate.GffCodInfoGene;
import com.novelBio.base.genome.gffOperate.GffsearchGene;




public class ClasterSearch 
{
	

	/**
	 * ��֪cluster���ģ���������չ��bp����
	 */
	public static int clsdistance=20000;
	
	/**
	 * ��֪cluster���ģ���������չ�Ļ�����
	 */
	public static int clsNum=2;
	
	/**
	 * ��ͬcluster������
	 */
	public static int clusternum=1;
	
	/**
	 * cluster��ͬԴ�������Ŀ
	 */
	public static int homogenenum=3;
	/**
     * ʵ����һ��A����GffHash����
     */
	GffHashPlantGene AGffHash=new GffHashPlantGene();
    /**
     * ʵ����һ��B����ffHash����
     */
	GffHashPlantGene BGffHash=new GffHashPlantGene();;
	
    /**
	 * �趨���ҵķ�ʽ�ǰ��վ���bp���ǰ��ջ�������˳��
	 * Ĭ���Ǿ���bp
	 */
	boolean distance=false;;
	
	
	/**
	 * ÿ��A��Ӧ��BͬԴ����<br/>
	 * AΪkey
	 * valueΪlist--����ΪͬԴ��B����
	 */
	Hashtable<String, ArrayList<String>> HashAhomoB;
	
	/**
	 * ÿ��B��Ӧ��AͬԴ����<br/>
	 * BΪkey
	 * valueΪlist--����ΪͬԴ��A����
	 */
	Hashtable<String, ArrayList<String>> HashBhomoA;
    
    /**
	 * ÿ��ClusterID����Ӧ�Ļ���
	 */
	Hashtable<Integer, ArrayList<String>> HashClsIDGene;
	
	/**
	 * ÿ����������Ӧ��ClusterID
	 */
	Hashtable<String,Integer> HashGeneClsID;
	
	/**
	 * ��ȡA����Gff�ļ������ɹ�ϣ��,�����趨A�������Ͻ�
	 * @param Gfffilename
	 */
	public void AGffLoad(String AGfffilename) {
		 
		try {
			AGffHash.ReadGffarray(AGfffilename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ��ȡB����Gff�ļ������ɹ�ϣ�������趨B����ˮ��
	 * @param Gfffilename
	 */
    public void BGffLoad(String BGfffilename) {
    	 
    	BGffHash.GeneName="LOC_Os\\d{2}g\\d{5}";
    	BGffHash.splitmRNA="(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";
    	try {
			BGffHash.ReadGffarray(BGfffilename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * �������򣬺���Ӧ��GffHash���󣬻�����������ɾ���Ļ������
     * @param LOCID :��������
     * @param gffHash ������GffHash����
     * @param distance ��cluster�Ƿ�Ϊ���룺�ǣ�ѡ�������������distance��Χ�� �񣺸����������һ���Χ
     * @return  ����int[2] 0:��˻������ 1���Ҷ˻������
     * ʧ�ܷ���null
     */
    private Integer[] ClusterBorderGeneNum(String LOCID,GffHashPlantGene gffHash)
    {
    	Integer  GeneID,leftLOCnum, rightLOCnum;//��������ź��������˻�������
    	Integer[] border=new Integer[2];
    	
    	if(distance==false)
    	{
    		GeneID=gffHash.LOCIDHash.get(LOCID);
    		if(GeneID==null)
    		{
    			System.out.println(LOCID);
        		return null;
    		}
    		if((GeneID-clsNum)<0)
    		{
    			leftLOCnum=0;
    		}
    		else 
    		{
    			leftLOCnum=GeneID-clsNum;
			}
    		rightLOCnum=GeneID+clsNum;//����ע����п��ܻ�����������һ��Ⱦɫ����
    		border[0]=leftLOCnum;
        	border[1]=rightLOCnum;
        	return border;
    	}
    	GffDetailGene thisLOC=gffHash.LOCsearch(LOCID);
    	if(thisLOC==null)
    	{
    		System.out.println(LOCID);
    		return null;
    	}
    	//cluster������Ϊѡ�����������
    	int location=(thisLOC.numberend-thisLOC.numberstart)/2+thisLOC.numberstart;
    	//ȷ��cluster�������˵ľ���
    	int leftcls=0,rightcls=0;
    	if((location-clsdistance)<=0)
    	{
    		leftcls=1;
    	}
    	else 
    	{
		   leftcls=	location-clsdistance;
		}
    	rightcls=location+clsdistance;
    	GffCodInfoGene leftLOCinfo=null;
    	GffCodInfoGene rightLOCinfo=null;
    	
    	//������Ҷ˻������Ϣ
    	if((leftLOCinfo=GffsearchGene.searchLocation(thisLOC.ChrID, leftcls, gffHash.Chrhash))==null||
    			(rightLOCinfo=GffsearchGene.searchLocation(thisLOC.ChrID, rightcls, gffHash.Chrhash))==null)
    	{
			return null;
		}
    	
    	String leftLOCID, rightLOCID;
    	 
    	//ȷ��cluster�������˵Ļ���
    	if(leftLOCinfo.insidegene)
    	{
    	   leftLOCID=leftLOCinfo.LOCID[0];
    	}
    	else 
    	{
    	   leftLOCID=leftLOCinfo.LOCID[2];
		}
    	
    	if(rightLOCinfo.insidegene)
    	{
    		rightLOCID=rightLOCinfo.LOCID[0];
    	}
    	else 
    	{
    		rightLOCID=rightLOCinfo.LOCID[1];
		}
    	//����������˻�������
          leftLOCnum=gffHash.LOCIDHash.get(leftLOCID);
    	  rightLOCnum=gffHash.LOCIDHash.get(rightLOCID);	
    	
    	border[0]=leftLOCnum;
    	border[1]=rightLOCnum;
    	return border;

    }
    
	/**
     * ����A�����������˻������ţ���B�����������˻������ţ�
     * �ֱ���A��B������Ӧ���������л���
     * ����ArrayList<String>[]
     * 0��A����ָ�����������л���
     * 1��B����ָ�����������л���
     * @param leftLOCnum
     * @param rightLOCnum
     */
    private ArrayList<String>[] ClusterGeneCompare(int AleftLOCnum,int ArightLOCnum,int BleftLOCnum,int BrightLOCnum)
    {
    	String tmpLOCID="";
    	ArrayList<String> AgeneList=new ArrayList<String>();//A���ָ����������л���
    	ArrayList<String> BgeneList=new ArrayList<String>();//B���ָ����������л���
    	for(int i=AleftLOCnum;i<=ArightLOCnum;i++)
    	{
    		AgeneList.add(AGffHash.LOCIDList.get(i));
    	}
    	for(int i=BleftLOCnum;i<=BrightLOCnum;i++)
    	{
    		if(i>=BGffHash.LOCIDList.size())
    		{
    			continue;
    		}
    		BgeneList.add(BGffHash.LOCIDList.get(i));
    	}
    	ArrayList<String>[] geneLists=new ArrayList[2];
    	geneLists[0]=AgeneList;
    	geneLists[1]=BgeneList;
    	return geneLists;
    	
    } 
    
    /**
     * ����A��B��������genelist,����ͬԴ�����ϣ��HashAhomoB
     * �ж�������list���ж���ͬԴ����
     * ע���ж�û��ͬԴ��������
     */
    private void judgeHomoGene(ArrayList<String>[] geneLists) 
    {
    	int Alength=geneLists[0].size();
    	int Blength=geneLists[1].size();
    	//�������ҵ���ͬԴ����key��B����value��B�������ڵ�cluster
    	Hashtable<String, Integer> BtoAHomoHash=new Hashtable<String, Integer>();
    	//�������ҵ���ͬԴ����key��A����value��A�������ڵ�cluster
    	Hashtable<String, Integer> AtoBHomoHash=new Hashtable<String, Integer>();
    	///���ϣ�AtoB��BtoA��cluster��ͬ��ʾΪͬһ���ͬԴ����
    	
    	Integer cluster=1;
    	Integer clustertmp=0;
    	
    	boolean flag=false;
    	
    	
        for(int i=0;i<Alength;i++)//����A����ĳ���������л���     
        {   //A����ͬԴB�����list
        	ArrayList<String> BhomoList=HashAhomoB.get(geneLists[0].get(i));
        	if(BhomoList==null)
        	{
        		continue;//û��ͬԴ���������
        	}
        	int Bhomolength=BhomoList.size();//A����ͬԴB�������Ŀ
        	//�½�һ����ϣ�����Ա�����ѡ���A���������B����ͬԴ����Ҳ���ǰ�BhomoList��Ԫ��װ���ϣ���������
        	Hashtable<String , Integer> BtmpHomoGeneHash=new Hashtable<String, Integer>();
        	for(int j=0;j<Bhomolength;j++)
        	{
        		BtmpHomoGeneHash.put(BhomoList.get(j), 3);
        	}
        	
        	for(int j=0; j<Blength;j++)
        	{
        		flag=false;
        		if(BtmpHomoGeneHash.get(geneLists[1].get(j))==null)//�����A��ͬԴ������û�ҵ�B����ô����
        		{ continue;}
        		//����ҵ��ˣ������������BtoA���Ƿ��Ѿ����ڣ�����������룬ͬʱ����cluster������˵���͸ոյ�cluster����һ�࣬��ô����
        		 flag=true;//��ʾ���������BtoA���Ѿ��ҵ�
        		if((clustertmp=BtoAHomoHash.get(geneLists[1].get(j)))==null)
        		{   //����ҵ��µ�cluster
        			BtoAHomoHash.put(geneLists[1].get(j), cluster);//���ҵ���B���ֻ����Լ�����cluster�����ϣ��
        		}
        	}
        	if(flag)
        	{
        		if(clustertmp==null)
        		{
        	      AtoBHomoHash.put(geneLists[0].get(i), cluster);//�ѱ����ҵ�A���ֻ����Լ�����cluster�����ϣ��	
        	      cluster++;
        		}
        		else 
        		{
        		  AtoBHomoHash.put(geneLists[0].get(i), clustertmp);//�ѱ����ҵ�A���ֻ����Լ�����cluster�����ϣ��	
				}
        	}
        	
        }
        
        
        
        
        
        
	}
    
    
    /**
     * ����A��B��������genelist�����ݹ�ϣ��HashGeneClsID
     * ͨ��ClusterID�ж�������list���ж���ͬԴ����
     * ע���ж�û��ͬԴ��������
     * ��������ĳ����ͬԴ������С��3ʱ������null
     * 0:gene
     * 1:����cluster
     */
    private ArrayList<String[]> judgeHomoGenebyClusterID(ArrayList<String>[] geneLists) 
    {  
    	int Alength=geneLists[0].size();
    	int Blength=geneLists[1].size();
    	//�������ҵ���ͬԴ����key��B����value��B�������ڵ�cluster
    	Hashtable<String, Integer> BtoAHomoHash=new Hashtable<String, Integer>();
    	//�������ҵ���ͬԴ����key��A����value��A�������ڵ�cluster
    	Hashtable<String, Integer> AtoBHomoHash=new Hashtable<String, Integer>();
    	///���ϣ�AtoB��BtoA��cluster��ͬ��ʾΪͬһ���ͬԴ����
    	Integer cluster=1;
    	Integer clustertmp=0;
    	boolean flag=false; //�ж�A�����Ƿ��ҵ�ͬԴ��B����   	
    	
        for(int i=0;i<Alength;i++)//����A����ĳ���������л���     
        {   //A�������ڵ�ClusterID
        	Integer AClusterID=HashGeneClsID.get(geneLists[0].get(i));
        	if(AClusterID==null)
        	{
        		continue;
        	}
        	flag=false;
        	for(int j=0; j<Blength;j++)
        	{
        		Integer testInteger;
        		testInteger=HashGeneClsID.get(geneLists[1].get(j));
        		
        		try {
        			if(testInteger==null||!testInteger.equals(AClusterID))//��B����ĳ�������л������ң����Ƿ��л��������ں�Aһ�µ�ClusterID
            		{ continue;}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(geneLists[1].get(j)+"  "+HashGeneClsID.get(geneLists[1].get(j)));
				}
        		
        		//����ҵ��ˣ������������BtoA��ϣ�����Ƿ��Ѿ����ڣ�����������룬ͬʱ����cluster������˵���͸ոյ�cluster����һ�࣬��ô����
        		 flag=true;//��ʾ���������B����ĳ�������Ѿ��ҵ�ͬԴ����
        		if((clustertmp=BtoAHomoHash.get(geneLists[1].get(j)))==null)
        		{   //����ҵ��µ�cluster
        			BtoAHomoHash.put(geneLists[1].get(j), cluster);//���ҵ���B���ֻ����Լ�����cluster�����ϣ��
        		}
        	}
        	if(flag)
        	{
        		if(clustertmp==null)
        		{
        	      AtoBHomoHash.put(geneLists[0].get(i),cluster);//�ѱ����ҵ�A���ֻ����Լ�����cluster�����ϣ��
        	      cluster++;
        		}
        		else 
        		{
        		  AtoBHomoHash.put(geneLists[0].get(i), clustertmp);//�ѱ����ҵ�A���ֻ����Լ�����cluster�����ϣ��	
				}
        	}
        }
        
        //�����д��list��ֻ�е�
        ArrayList<String[]> result=new ArrayList<String[]>();
        if(AtoBHomoHash.size()>=homogenenum&&BtoAHomoHash.size()>=homogenenum&&cluster>clusternum)
        {
        	Iterator iterA = AtoBHomoHash.entrySet().iterator();
        	while (iterA.hasNext()) {
        		String[] detailresult=new String[2];
        	    Map.Entry entry = (Map.Entry)iterA.next();
        	    detailresult[0] = entry.getKey().toString();
        	    detailresult[1] = entry.getValue().toString();
        	    result.add(detailresult);
        	}
        	Iterator iterB = BtoAHomoHash.entrySet().iterator();
        	while (iterB.hasNext()) {
        		String[] detailresult=new String[2];
        	    Map.Entry entry = (Map.Entry)iterB.next();
        	    detailresult[0] = entry.getKey().toString();
        	    detailresult[1] = entry.getValue().toString();
        	    result.add(detailresult);
        	}
        	return result;
        }
        
        return null;        
	}
    
    
 
    private ArrayList<String> listcope(ArrayList<String> listhomogene,String gene)
    {
    	String flag=gene.substring(0,2);
    	int length=listhomogene.size();
    	ArrayList<String> listhomogenefinal=new ArrayList<String>();
    	for (int i = 0; i < length; i++) 
    	{
		   if(listhomogene.get(i).startsWith(flag))
		   {
			   continue;
		   }
		   else 
		   {
			   listhomogenefinal.add(listhomogene.get(i));
		   }
		}
    	if(listhomogenefinal.size()==0)
    	{
    		return null;
    	}
    	else 
    	{	
    	   return listhomogenefinal;	
		}
    }
    
    /**
	 * ��ȡclusterID��txt�ļ�
	 * ����HashGeneClsID���ͬԴ�����
	 * @param HomoFile
	 */
    public void ReadClusterIDFile(String ClusterIDFile) 
    {
    	HashGeneClsID=new Hashtable<String, Integer>();
    	HashClsIDGene=new Hashtable<Integer, ArrayList<String>>();
    	HashAhomoB=new Hashtable<String, ArrayList<String>>();
    	HashBhomoA=new Hashtable<String, ArrayList<String>>();
    	String content;
		 BufferedReader bufread;
		 String[] ss;
		 ArrayList<Integer> ListClusterID=new ArrayList<Integer>();
		 ArrayList<String> ListClusterGene=new ArrayList<String>();
      try
      {  //�õ��ı��ļ���·��
         File file=new File(ClusterIDFile);
         FileReader fileread=new FileReader(file);
         bufread=new BufferedReader(fileread);
         bufread.readLine();
         while((content=bufread.readLine())!=null)
         {
    	   ss=content.split("\\t");
    	   HashGeneClsID.put(ss[0], Integer.parseInt(ss[1]));//�����ӦclusterID��
    	   ListClusterGene.add(ss[0]);
    	  
    	   
    	   ArrayList<String> ListGeneName;
    	   if((ListGeneName=HashClsIDGene.get(Integer.parseInt(ss[1])))!=null)
    	   {
    		   ListGeneName.add(ss[0]);
    	   }
    	   else
    	   {
			  ListGeneName=new ArrayList<String>();
			  ListGeneName.add(ss[0]);
			  HashClsIDGene.put(Integer.parseInt(ss[1]),ListGeneName);//clusterID��Ӧ�����
			  ListClusterID.add(Integer.parseInt(ss[1]));
		   }
         }
      }catch(Exception d)
      {System.out.println(d.getMessage());}
      int genenum=ListClusterGene.size();
      for (int i = 0; i < genenum; i++) 
      {
    	  String gene=ListClusterGene.get(i);
    	  Integer clusterID=HashGeneClsID.get(gene);
    	  if(clusterID==null)
    		  continue;
    	  ArrayList<String> listhomogene=HashClsIDGene.get(clusterID);
    	  ArrayList<String> list;
    	  if(gene.startsWith("AT"))//HashAhomoBΪ���Ͻ�--ˮ��
    	  {    
    		  if((list=listcope(listhomogene,gene))!=null)
    		  HashAhomoB.put(gene, listcope(listhomogene,gene));
    	  }
    	  else //HashBhomoAΪˮ��--���Ͻ�
    	  {
    		  if((list=listcope(listhomogene,gene))!=null)
			  HashBhomoA.put(gene, listcope(listhomogene,gene));
		  }
   	  }
      
    }
    
    /**
	 * ����ȫ������ͬԴ�����ϣ���Ѿ�����
	 * @param HomoFile
	 */
    public void ReadHomoFile(String HomoFile) 
    {
    	HashAhomoB=null;
    	HashBhomoA=null;
    }
    
    
    /**
     * 
     */
	public void run(String writeresultfile) 
	{
		 int Agenenum=AGffHash.LOCIDList.size();//A�������л������Ŀ
		 
		 int rowNum=1;
		 for (int i = 0; i < Agenenum; i++) 
		 {
			 String ALOCID=AGffHash.LOCIDList.get(i);
			 ArrayList<String> BhomoList=HashAhomoB.get(ALOCID);//����A���ֻ��򣬻��B���ֻ����ͬԴ����
			 if(BhomoList==null)//û��ͬԴ����
			 {
				 continue;
			 }
			 int BhomoNum=BhomoList.size();
			 Integer[] Agenelocinfo=ClusterBorderGeneNum(ALOCID,AGffHash);
			 if(Agenelocinfo==null)
			 {
				 continue;
			 }
		    	for (int j = 0; j < BhomoNum; j++) 
		    	{
		    		String BLOCID=BhomoList.get(j);
		    		Integer[] Bgenelocinfo=ClusterBorderGeneNum(BLOCID,BGffHash);
		    		 if(Bgenelocinfo==null)
					 {
						 continue;
					 }
		    		ArrayList<String>[] geneLists= ClusterGeneCompare(Agenelocinfo[0], Agenelocinfo[1], Bgenelocinfo[0], Bgenelocinfo[1]);
		    		ArrayList<String[]> result=judgeHomoGenebyClusterID(geneLists);
		    		if(result==null)
		    		{
		    			continue;
		    		}
		    		//String[][] write=new String[2][result.size()];
		    		String filecontent="";
		    		for (int k = 0; k < result.size(); k++) 
		    		{
		    			//write[0][k]=result.get(k)[0];
		    			//write[1][k]=result.get(k)[1];
		    			filecontent=filecontent+ result.get(k)[0]+"  "+result.get(k)[1]+"  ";
					}
		    		filecontent=filecontent+"\n";
		    		//aaa.WriteExcel(rowNum, 1, write);
		    		//System.out.println(""+i+"  "+write+"\n");
		    		String filepath,read;
		    		File writefile;
		    		  BufferedWriter bufwriter;
		    	     try
		    	     {
		    	      boolean addStr=true; //ͨ������������ж��Ƿ����ı��ļ���׷������
		    	      filepath=writeresultfile;       //�õ��ı��ļ���·��
		    	     
		    	      writefile=new File(filepath);
		    	      if(writefile.exists()==false)    //����ı��ļ��������򴴽���
		    	      {
		    	          writefile.createNewFile();   
		    	          writefile=new File(filepath);  //����ʵ����
		    	      }
		    	      FileWriter filewriter=new FileWriter(writefile,addStr);
		    	      bufwriter=new BufferedWriter(filewriter);
		    	      filewriter.write(filecontent);
		    	      filewriter.flush();
		    	     }catch(Exception d){System.out.println(d.getMessage());}
		    	    
				}
			 
			 
		 }
		
		
	}
    
    
    
    



}
