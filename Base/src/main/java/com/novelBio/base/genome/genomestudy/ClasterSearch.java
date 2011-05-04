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
	 * 已知cluster中心，向左右扩展的bp距离
	 */
	public static int clsdistance=20000;
	
	/**
	 * 已知cluster中心，向左右扩展的基因数
	 */
	public static int clsNum=2;
	
	/**
	 * 不同cluster的数量
	 */
	public static int clusternum=1;
	
	/**
	 * cluster中同源基因的数目
	 */
	public static int homogenenum=3;
	/**
     * 实例化一个A物种GffHash对象
     */
	GffHashPlantGene AGffHash=new GffHashPlantGene();
    /**
     * 实例化一个B物种ffHash对象
     */
	GffHashPlantGene BGffHash=new GffHashPlantGene();;
	
    /**
	 * 设定查找的方式是按照距离bp还是按照基因排列顺序
	 * 默认是距离bp
	 */
	boolean distance=false;;
	
	
	/**
	 * 每个A对应的B同源基因<br/>
	 * A为key
	 * value为list--里面为同源的B基因
	 */
	Hashtable<String, ArrayList<String>> HashAhomoB;
	
	/**
	 * 每个B对应的A同源基因<br/>
	 * B为key
	 * value为list--里面为同源的A基因
	 */
	Hashtable<String, ArrayList<String>> HashBhomoA;
    
    /**
	 * 每个ClusterID所对应的基因
	 */
	Hashtable<Integer, ArrayList<String>> HashClsIDGene;
	
	/**
	 * 每个基因所对应的ClusterID
	 */
	Hashtable<String,Integer> HashGeneClsID;
	
	/**
	 * 读取A物种Gff文件并生成哈希表,本次设定A物种拟南芥
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
	 * 读取B物种Gff文件并生成哈希表，本次设定B物种水稻
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
     * 给定基因，和相应的GffHash对象，获得其左右若干距离的基因序号
     * @param LOCID :给定基因
     * @param gffHash ：给定GffHash对象
     * @param distance ：cluster是否为距离：是：选择给定基因左右distance范围。 否：给定基因左右基因范围
     * @return  返回int[2] 0:左端基因序号 1：右端基因序号
     * 失败返回null
     */
    private Integer[] ClusterBorderGeneNum(String LOCID,GffHashPlantGene gffHash)
    {
    	Integer  GeneID,leftLOCnum, rightLOCnum;//本基因序号和左右两端基因的序号
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
    		rightLOCnum=GeneID+clsNum;//这里注意很有可能会两个基因不在一条染色体上
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
    	//cluster的中心为选定基因的中心
    	int location=(thisLOC.numberend-thisLOC.numberstart)/2+thisLOC.numberstart;
    	//确定cluster左右两端的距离
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
    	
    	//获得左右端基因的信息
    	if((leftLOCinfo=GffsearchGene.searchLocation(thisLOC.ChrID, leftcls, gffHash.Chrhash))==null||
    			(rightLOCinfo=GffsearchGene.searchLocation(thisLOC.ChrID, rightcls, gffHash.Chrhash))==null)
    	{
			return null;
		}
    	
    	String leftLOCID, rightLOCID;
    	 
    	//确定cluster左右两端的基因
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
    	//获得左右两端基因的序号
          leftLOCnum=gffHash.LOCIDHash.get(leftLOCID);
    	  rightLOCnum=gffHash.LOCIDHash.get(rightLOCID);	
    	
    	border[0]=leftLOCnum;
    	border[1]=rightLOCnum;
    	return border;

    }
    
	/**
     * 给定A物种左右两端基因的序号，和B物种左右两端基因的序号，
     * 分别获得A，B物种相应区域内所有基因
     * 返回ArrayList<String>[]
     * 0：A物种指定区域内所有基因
     * 1：B物种指定区域内所有基因
     * @param leftLOCnum
     * @param rightLOCnum
     */
    private ArrayList<String>[] ClusterGeneCompare(int AleftLOCnum,int ArightLOCnum,int BleftLOCnum,int BrightLOCnum)
    {
    	String tmpLOCID="";
    	ArrayList<String> AgeneList=new ArrayList<String>();//A物种该区域内所有基因
    	ArrayList<String> BgeneList=new ArrayList<String>();//B物种该区域内所有基因
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
     * 给定A，B物种两个genelist,根据同源基因哈希表：HashAhomoB
     * 判断这两个list中有多少同源基因
     * 注意判断没有同源基因的情况
     */
    private void judgeHomoGene(ArrayList<String>[] geneLists) 
    {
    	int Alength=geneLists[0].size();
    	int Blength=geneLists[1].size();
    	//保存所找到的同源基因，key，B基因，value，B基因所在的cluster
    	Hashtable<String, Integer> BtoAHomoHash=new Hashtable<String, Integer>();
    	//保存所找到的同源基因，key，A基因，value，A基因所在的cluster
    	Hashtable<String, Integer> AtoBHomoHash=new Hashtable<String, Integer>();
    	///以上，AtoB和BtoA的cluster相同表示为同一组的同源基因
    	
    	Integer cluster=1;
    	Integer clustertmp=0;
    	
    	boolean flag=false;
    	
    	
        for(int i=0;i<Alength;i++)//遍历A物种某区域内所有基因     
        {   //A基因同源B基因的list
        	ArrayList<String> BhomoList=HashAhomoB.get(geneLists[0].get(i));
        	if(BhomoList==null)
        	{
        		continue;//没有同源基因则继续
        	}
        	int Bhomolength=BhomoList.size();//A基因同源B基因的数目
        	//新建一个哈希表用以保存所选择的A基因的所有B物种同源基因，也就是把BhomoList的元素装入哈希表，方便查找
        	Hashtable<String , Integer> BtmpHomoGeneHash=new Hashtable<String, Integer>();
        	for(int j=0;j<Bhomolength;j++)
        	{
        		BtmpHomoGeneHash.put(BhomoList.get(j), 3);
        	}
        	
        	for(int j=0; j<Blength;j++)
        	{
        		flag=false;
        		if(BtmpHomoGeneHash.get(geneLists[1].get(j))==null)//如果在A的同源基因中没找到B，那么继续
        		{ continue;}
        		//如果找到了，看这个基因在BtoA中是否已经存在，不存在则加入，同时加入cluster。存在说明和刚刚的cluster属于一类，那么跳过
        		 flag=true;//表示这个基因在BtoA中已经找到
        		if((clustertmp=BtoAHomoHash.get(geneLists[1].get(j)))==null)
        		{   //如果找到新的cluster
        			BtoAHomoHash.put(geneLists[1].get(j), cluster);//把找到的B物种基因以及所属cluster放入哈希表
        		}
        	}
        	if(flag)
        	{
        		if(clustertmp==null)
        		{
        	      AtoBHomoHash.put(geneLists[0].get(i), cluster);//把被查找的A物种基因以及所属cluster放入哈希表	
        	      cluster++;
        		}
        		else 
        		{
        		  AtoBHomoHash.put(geneLists[0].get(i), clustertmp);//把被查找的A物种基因以及所属cluster放入哈希表	
				}
        	}
        	
        }
        
        
        
        
        
        
	}
    
    
    /**
     * 给定A，B物种两个genelist，根据哈希表HashGeneClsID
     * 通过ClusterID判断这两个list中有多少同源基因
     * 注意判断没有同源基因的情况
     * 当两物种某区段同源基因数小于3时，返回null
     * 0:gene
     * 1:所属cluster
     */
    private ArrayList<String[]> judgeHomoGenebyClusterID(ArrayList<String>[] geneLists) 
    {  
    	int Alength=geneLists[0].size();
    	int Blength=geneLists[1].size();
    	//保存所找到的同源基因，key，B基因，value，B基因所在的cluster
    	Hashtable<String, Integer> BtoAHomoHash=new Hashtable<String, Integer>();
    	//保存所找到的同源基因，key，A基因，value，A基因所在的cluster
    	Hashtable<String, Integer> AtoBHomoHash=new Hashtable<String, Integer>();
    	///以上，AtoB和BtoA的cluster相同表示为同一组的同源基因
    	Integer cluster=1;
    	Integer clustertmp=0;
    	boolean flag=false; //判断A基因是否找到同源的B基因   	
    	
        for(int i=0;i<Alength;i++)//遍历A物种某区域内所有基因     
        {   //A基因所在的ClusterID
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
        			if(testInteger==null||!testInteger.equals(AClusterID))//在B物种某区段所有基因中找，看是否有基因是属于和A一致的ClusterID
            		{ continue;}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(geneLists[1].get(j)+"  "+HashGeneClsID.get(geneLists[1].get(j)));
				}
        		
        		//如果找到了，看这个基因在BtoA哈希表中是否已经存在，不存在则加入，同时加入cluster。存在说明和刚刚的cluster属于一类，那么跳过
        		 flag=true;//表示这个基因在B物种某区段中已经找到同源基因
        		if((clustertmp=BtoAHomoHash.get(geneLists[1].get(j)))==null)
        		{   //如果找到新的cluster
        			BtoAHomoHash.put(geneLists[1].get(j), cluster);//把找到的B物种基因以及所属cluster放入哈希表
        		}
        	}
        	if(flag)
        	{
        		if(clustertmp==null)
        		{
        	      AtoBHomoHash.put(geneLists[0].get(i),cluster);//把被查找的A物种基因以及所属cluster放入哈希表
        	      cluster++;
        		}
        		else 
        		{
        		  AtoBHomoHash.put(geneLists[0].get(i), clustertmp);//把被查找的A物种基因以及所属cluster放入哈希表	
				}
        	}
        }
        
        //将结果写入list，只有当
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
	 * 读取clusterID的txt文件
	 * 返回HashGeneClsID表和同源基因表
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
      {  //得到文本文件的路径
         File file=new File(ClusterIDFile);
         FileReader fileread=new FileReader(file);
         bufread=new BufferedReader(fileread);
         bufread.readLine();
         while((content=bufread.readLine())!=null)
         {
    	   ss=content.split("\\t");
    	   HashGeneClsID.put(ss[0], Integer.parseInt(ss[1]));//基因对应clusterID表
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
			  HashClsIDGene.put(Integer.parseInt(ss[1]),ListGeneName);//clusterID对应基因表
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
    	  if(gene.startsWith("AT"))//HashAhomoB为拟南芥--水稻
    	  {    
    		  if((list=listcope(listhomogene,gene))!=null)
    		  HashAhomoB.put(gene, listcope(listhomogene,gene));
    	  }
    	  else //HashBhomoA为水稻--拟南芥
    	  {
    		  if((list=listcope(listhomogene,gene))!=null)
			  HashBhomoA.put(gene, listcope(listhomogene,gene));
		  }
   	  }
      
    }
    
    /**
	 * 待补全，假设同源基因哈希表已经存在
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
		 int Agenenum=AGffHash.LOCIDList.size();//A物种所有基因的数目
		 
		 int rowNum=1;
		 for (int i = 0; i < Agenenum; i++) 
		 {
			 String ALOCID=AGffHash.LOCIDList.get(i);
			 ArrayList<String> BhomoList=HashAhomoB.get(ALOCID);//给定A物种基因，获得B物种基因的同源序列
			 if(BhomoList==null)//没有同源基因
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
		    	      boolean addStr=true; //通过这个对象来判断是否向文本文件中追加内容
		    	      filepath=writeresultfile;       //得到文本文件的路径
		    	     
		    	      writefile=new File(filepath);
		    	      if(writefile.exists()==false)    //如果文本文件不存在则创建它
		    	      {
		    	          writefile.createNewFile();   
		    	          writefile=new File(filepath);  //重新实例化
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
