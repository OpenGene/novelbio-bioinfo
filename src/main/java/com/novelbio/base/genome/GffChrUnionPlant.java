package com.novelbio.base.genome;

import genome.getChrSequence.ChrSearch;
import genome.getChrSequence.ChrStringHash;;
import genome.gffOperate.*;

public class GffChrUnionPlant extends GffChrUnion{
	/**
	 * 设定基因的ATG上游长度，默认为3000bp
	 */
	static int ATGUpStreambp=3000;
	/**
	 * 设定基因的ATG上游长度，默认为3000bp
	 */
	public void setATGUpStreambp(int upstreambp) {
		ATGUpStreambp=upstreambp;
	}
	
	
	
	/**
	 * 读取Gff文件并生成一个基因组的哈希表-list表 
	 * 和 一个LOC与对对应信息的哈希表
	 * 具体信息见GffHash类
	 * @param Gfffilename
	 */
	public void GffLoading(String Gfffilename)
	{
		gffHash=new GffHashGene();
		((GffHashGene)gffHash).GeneName="LOC_Os\\d{2}g\\d{5}";
		((GffHashGene)gffHash).splitmRNA="(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";
		try {  gffHash.ReadGffarray(Gfffilename);                   } catch (Exception e) {	e.printStackTrace(); }
	}
	


	/**
	 * 输入LOCID和上游长度，返回获得的上游序列(指与ATG距离)
	 * 自动考虑序列的正反向
	 * @param length
	 * @param LOCID
	 * @return
	 */
	public String getATGUpseq(String LOCID,int length)
	{
		gffsearch=new GffsearchGene();
		GffDetailGene locinfo = (GffDetailGene) Gffsearch.LOCsearch(LOCID,gffHash);
		int ATGnum=locinfo.getcdslist(0).get(1);
		if(locinfo.cis5to3)
		{
		return	chrSearch.GetSequence(locinfo.cis5to3,locinfo.ChrID, ATGnum-length,ATGnum+2);
		}
		else
		{
		return	chrSearch.GetSequence(locinfo.cis5to3,locinfo.ChrID, ATGnum-2, ATGnum+length);
		}
	}
	
	
	
	
	
	/**
	 * 输入染色体序号，Peak坐标，返回该peak定位
	 * 当peak在基因内部时，考虑基因的方向<br>
	 * 内部是指在基因转录起点上游指定范围内。
	 * 返回string[2]
	 * 0:LOC 编号，基因间的话就是LOCID+" "+LOCID
	 * 1:具体文字信息
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public String[] peakLocation(String ChrID, int peakloc)
	{
		/**
		 * 将GeneInfo内容转化为具体信息
		 */
		HashMap<Integer, String> Detail1=new HashMap<Integer, String>();
		Detail1.put(1, "5‘UTR");
        Detail1.put(2, "外显子");  		
        Detail1.put(3, "内含子");  
        Detail1.put(4, "3‘UTR");  
		/**
		 * 返回的查找信息
		 */
	   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
		int numbegin = peakInfo.begindistance.size();
		int numend=peakInfo.enddistance.size();
		boolean flagupstream=false;//是否在上游3000bp以内，默认在以外
		boolean flag3UTR=false;//是否在基因的100bp以内
		boolean cis5to3=true;
		
		String insidegene="";//是否在基因内
		String begfangxiang="";//和前一个基因的信息
		String endfangxiang="";//和后一个基因的信息
		String inseidfangxiang="";//基因内部的定位情况 
		String LOCbeg="";//前一个基因编号
		String LOCend="";//后一个基因编号
		/**
		 * 当在基因间时
		 */
	   if(!peakInfo.insidegene)
	   {	
		   insidegene="基因间";
		 
		   /**
		    *  如果前一个基因的方向为反向<-----那么就是peak和基因起点的距离
	         */
			if (!peakInfo.begincis5to3&& Math.abs(peakInfo.DistancetoGeneStart[0])<UpstreamTSSbp)
			  { cis5to3=false; 
			    flagupstream=true;
			    insidegene="内部基因间";
			    begfangxiang="反向前一个基因，距离基因起点"+Math.abs(peakInfo.DistancetoGeneStart[0])+"bp";
			    LOCbeg=peakInfo.LOCID[1];
			
			  }
			/**
			 * 如果前一个基因为正向---->那么就是peak和前一个基因末尾的距离，向后延长100bp就好
			 */
			else if(peakInfo.begincis5to3&& peakInfo.begindistance.get(0)[1]<GeneEnd3UTR)
			  {
				cis5to3=true;
				flag3UTR=true;
				insidegene="内部基因间";
				begfangxiang="正向前一个基因，距离3UTR"+peakInfo.begindistance.get(0)[1]+"bp";
				LOCbeg=peakInfo.LOCID[1];
			
			  }
			else
			{    
				 begfangxiang="距前一个基因"+peakInfo.begindistance.get(0)[1]+"bp";
				 LOCbeg=peakInfo.LOCID[1]+" no ";
			}
	       
	  	  
	  		    /**
			     *  如果后一个基因的方向为正向----->那么就是peak和基因起点的距离
		         */
				if (peakInfo.endcis5to3&& Math.abs(peakInfo.DistancetoGeneStart[1])<UpstreamTSSbp)
				{ cis5to3=true;
				  flagupstream=true;
				  insidegene="内部基因间";
				 endfangxiang="正向后一个基因，距离ATG"+Math.abs(peakInfo.DistancetoGeneStart[1])+"bp";
				 LOCend=peakInfo.LOCID[2];
	
				}
				 /**
			     *  如果后一个基因的方向反向<-----那么就是peak和后一个基因末尾的距离，向后延长100bp就好
		         */
				else if(!peakInfo.endcis5to3&& peakInfo.enddistance.get(0)[1]<GeneEnd3UTR)
				  {
					cis5to3=false;
					flag3UTR=true;
					insidegene="内部基因间";
					endfangxiang="反向后一个基因，距离3UTR"+peakInfo.enddistance.get(0)[1]+"bp";
					LOCend=peakInfo.LOCID[2];
			
				  }
				else 
				{
					endfangxiang="距后一个基因"+peakInfo.enddistance.get(0)[1]+"bp";
					LOCend=peakInfo.LOCID[2]+" no ";
				}
		     
	   }
	   else //基因内
	   {
		   insidegene="基因内";
		   begfangxiang="";
		   endfangxiang="";
		    cis5to3=peakInfo.begincis5to3;
		    flagupstream=true;
		    
		    /**
		     * 依次查找所有转录本，peakInfo.GeneInfo.size()获得转录本的数目
		     */
		   for(int i=0;i<peakInfo.GeneInfo.size();i++)
		   {
			   
			   /**
			    * 当定位不在5‘UTR时
			    */
			 if(peakInfo.GeneInfo.get(i)[1]!=1) 
			 {
			   if(!inseidfangxiang.contains(inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "第"+peakInfo.GeneInfo.get(i)[2]+"个"+"距前面的距离为"+peakInfo.GeneInfo.get(i)[3]))
			   inseidfangxiang=inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "第"+peakInfo.GeneInfo.get(i)[2]+"个"+"距前面的距离为"+peakInfo.GeneInfo.get(i)[3];
			 }
			 /**
			    * 当定位在5‘UTR时，返回和ATG的距离
			    */
			 else 
			 {
				 if(!inseidfangxiang.contains(inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "第"+peakInfo.GeneInfo.get(i)[2]+"个"+"距ATG的距离为"+peakInfo.GeneInfo.get(i)[4]))
				 inseidfangxiang=inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "第"+peakInfo.GeneInfo.get(i)[2]+"个"+"距ATG的距离为"+peakInfo.GeneInfo.get(i)[4]; 
			 }
		   }
		   
		   
		   LOCbeg=peakInfo.LOCID[0];
		   LOCend="";
	    }
	   String[] result=new String[2];
	   if(LOCbeg.contains("no")&&LOCend.contains("no"))
	   {
	    result[0]=LOCbeg+" "+LOCend;
	   }
	   else if(LOCbeg.contains("no")&&!LOCend.contains("no")) 
	   {
		result[0]=LOCend;
  	   }
	   else if(!LOCbeg.contains("no")&&LOCend.contains("no")) 
	   {
		result[0]=LOCbeg;
	   }
	   else 
	   {
		result[0]=LOCbeg+" "+LOCend;
	   }
	   result[1]=insidegene+begfangxiang+endfangxiang+inseidfangxiang;
	   return  result;
	}
	
	
	/**
	 * 用于画motif密度分布图
	 * 输入染色体序号，Peak坐标，返回该peak距离Gene起点以及ATG的距离
	 * 当peak在基因内部时，考虑基因的方向
	 * 返回int
	 * 和基因起点距离
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public int[] peakDistance(String ChrID, int peakloc)
	{
 
			/**
			 * 返回的查找信息
			 */
		   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
			int numbegin = peakInfo.begindistance.size();
			int numend=peakInfo.enddistance.size();
			boolean flagupstream=false;//是否在上游3000bp以内，默认在以外
			boolean flag3UTR=false;//是否在基因的100bp以内
			boolean cis5to3=true;
			
			String insidegene="";//是否在基因内
			String begfangxiang="";//和前一个基因的信息
			String endfangxiang="";//和后一个基因的信息
			String inseidfangxiang="";//基因内部的定位情况 
			String LOCbeg="";//前一个基因编号
			String LOCend="";//后一个基因编号
			
			/**
			 * 当在基因间时,看和哪个基因近就计算和该基因起点的距离
			 */
			int DistanceResult[]=new int[2]; //这个就是最后保存的peak坐标和基因起点的距离。
		 
			 
		
			  if(!peakInfo.insidegene){
				  GffDetailList BeforeGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[1]);//前一个基因的信息
				  GffDetailList AfterGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[2]);//前一个基因的信息
				  int BeginDistance = peakloc-BeforeGeneInfo.numberend;
				  int EndDistance = AfterGeneInfo.numberstart-peakloc;
				  if(BeginDistance<0||EndDistance<0)
				  {
					  System.out.println(peakloc);
				  }
			  if(peakInfo.begincis5to3)//前一个基因正向
			  {
				  if(peakInfo.endcis5to3)//后一个基因也正向
				  {
					  for(int i=0;i<peakInfo.enddistance.size();i++)//和后一个基因的不同转录本的ATG有3k以内的
					  { if(peakInfo.enddistance.get(i)[1]<3000)
					     {
						   DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;;//记录距离，为负值
						   DistanceResult[0]=-1;
						   return DistanceResult;
					     }
					  }
					//和后一个基因的ATG距离较远，那么就比较坐标和前后基因的距离，取短的
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=peakloc-BeforeGeneInfo.numberstart;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
				  }
				  else //后一个基因反向
				  {
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=peakloc-BeforeGeneInfo.numberstart;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=AfterGeneInfo.numberend-peakloc;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
				  }
			  }
			  else//前一个基因反向 
			  {
				  if(peakInfo.endcis5to3)//后一个基因正向
				  {
				
					  
					//比较坐标和前后基因的距离，取短的
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
				  }
				  else //后一个基因反向
				  {
					  
					  for(int i=0;i<peakInfo.begindistance.size();i++)//和前一个基因的不同转录本的ATG有3k以内的
					  { if(peakInfo.begindistance.get(i)[1]<3000)
					     {
						   DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//记录距离
						   DistanceResult[0]=-1;
						   return DistanceResult;
					     }
					  }
					  
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//记录距离
						   DistanceResult[0]=-1;
						   return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=AfterGeneInfo.numberend-peakloc;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
				  }
			  }
			  }
			  else
			  {
				  if(peakInfo.begincis5to3)
				  {
					  DistanceResult[1]=peakloc- GffHash.locHashtable.get(peakInfo.LOCID[0]).numberstart;
					  DistanceResult[0]=0;
					  return DistanceResult;
				  }
				  else 
				  {
					  DistanceResult[1]=GffHash.locHashtable.get(peakInfo.LOCID[0]).numberstart-peakloc;
					  DistanceResult[0]=0;
					  return DistanceResult;
				}
			  }

		 
	}
	
	
	

	/**
	 * 用于画motif密度分布图
	 * 输入染色体序号，Peak坐标，返回该peak距离Gene起点以及ATG的距离
	 * 当peak在基因内部时，考虑基因的方向
	 * 返回int
	 * 和基因起点距离
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public int[] peakDistancenew(String ChrID, int peakloc)
	{
 
			/**
			 * 返回的查找信息
			 */
		   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
			int numbegin = peakInfo.begindistance.size();
			int numend=peakInfo.enddistance.size();
			boolean flagupstream=false;//是否在上游3000bp以内，默认在以外
			boolean flag3UTR=false;//是否在基因的100bp以内
			boolean cis5to3=true;
			
			String insidegene="";//是否在基因内
			String begfangxiang="";//和前一个基因的信息
			String endfangxiang="";//和后一个基因的信息
			String inseidfangxiang="";//基因内部的定位情况 
			String LOCbeg="";//前一个基因编号
			String LOCend="";//后一个基因编号
			
			/**
			 * 当在基因间时,看和哪个基因近就计算和该基因起点的距离
			 */
			int DistanceResult[]=new int[2]; //这个就是最后保存的peak坐标和基因起点的距离。
		 
			 
		
			  if(!peakInfo.insidegene){
				  GffDetailList BeforeGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[1]);//前一个基因的信息
				  GffDetailList AfterGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[2]);//前一个基因的信息
				  int BeginDistance = peakloc-BeforeGeneInfo.numberend;
				  int EndDistance = AfterGeneInfo.numberstart-peakloc;
				  if(BeginDistance<0||EndDistance<0)
				  {
					  System.out.println(peakloc);
				  }
			  if(peakInfo.begincis5to3)//前一个基因正向
			  {
				  if(peakInfo.endcis5to3)//后一个基因也正向
				  {
					 if(peakInfo.begindistance.get(0)[1]<100)
					  {
						  DistanceResult[1]=peakloc-BeforeGeneInfo.numberstart;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
				  }
				  else //后一个基因反向
				  {
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=peakloc-BeforeGeneInfo.numberstart;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=AfterGeneInfo.numberend-peakloc;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
				  }
			  }
			  else//前一个基因反向 
			  {
				  if(peakInfo.endcis5to3)//后一个基因正向
				  {
				
					  
					//比较坐标和前后基因的距离，取短的
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
					  else 
					  {
						  DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;
						  DistanceResult[0]=-1;
						  return DistanceResult;
					  }
				  }
				  else //后一个基因反向
				  {
					  if(peakInfo.enddistance.get(0)[1]<100)
					  {
						  DistanceResult[1]=AfterGeneInfo.numberend-peakloc;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//记录距离
						   DistanceResult[0]=-1;
						   return DistanceResult;
					  }
				  }
			  }
			  }
			  else
			  {
				  if(peakInfo.begincis5to3)
				  {
					  DistanceResult[1]=peakloc- GffHash.locHashtable.get(peakInfo.LOCID[0]).numberstart;
					  DistanceResult[0]=0;
					  return DistanceResult;
				  }
				  else 
				  {
					  DistanceResult[1]=GffHash.locHashtable.get(peakInfo.LOCID[0]).numberstart-peakloc;
					  DistanceResult[0]=0;
					  return DistanceResult;
				}
			  }
	}
	

	
	/**
	 * 输入序列，查找motif的数目
	 * @param Sequence
	 * @return
	 */
	public String[] MotifDetailSearch(String Sequence)
	{
		//motifregex
		ArrayList<String[]> MotifResultList=Patternlocation.getPatLoc(Sequence, motifregex, true);
		String MotifResult[]=new String[MotifResultList.size()];
		for (int i = 0; i < MotifResult.length; i++) 
		{
		   MotifResult[i]=	MotifResultList.get(i)[2];
		}
		return MotifResult;
	}
	
	
	
	
	
	
	
	/**
	 * 输入序列，查找motif的数目
	 * @param Sequence
	 * @return
	 */
	public int MotifSearch(String Sequence)
	{
	   Pattern motifPattern =Pattern.compile(motifregex, Pattern.CASE_INSENSITIVE); //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
   	   Matcher motifmMatche;
   	   motifmMatche=motifPattern.matcher(Sequence);
   	   int motifcount=0;//motif的数目
   	   while (motifmMatche.find())
   	   {
   		   motifcount++;
   	   }
   	   return motifcount;
	}

	
	public void writefile(String filepath,String filecontent,boolean append)    
    {
     try
     {
      boolean addStr=append; //通过这个对象来判断是否向文本文件中追加内容
                           //filepath 得到文本文件的路径
                            // filecontent 需要写入的内容
      File writefile=new File(filepath);
      if(writefile.exists()==false)    //如果文本文件不存在则创建它
      {
          writefile.createNewFile();   
          writefile=new File(filepath);  //重新实例化
      }
      FileWriter filewriter=new FileWriter(writefile,addStr);
      BufferedWriter bufwriter=new BufferedWriter(filewriter);
      filewriter.write(filecontent);
      filewriter.flush();
     }catch(Exception d){System.out.println(d.getMessage());}
    }
 
	/**
	 * 输入数目，返回若干个随机基因编号
	 * @param number
	 */
	public String[] getRandomLOC(int number)
	{
		int locnum=GffHash.LOCIDList.size();//所有水稻基因的数目
		HashMap<Integer, Integer> CheckNoneRepeatHash=new HashMap<Integer, Integer>();//存储所生成的随机数
		 int randomloc=0;
		for(int i=0; i<number; i++)//产生number个不重复伪随机数
		{
		    do//
		    {
			 randomloc=(int)(Math.random()*locnum);//产生0-1000的整数随机数 ，这里Math.random()产生0-1之间的双精度伪随机数（线性同余），乘以所有水稻数目后，再取int就好	 
		     } while (CheckNoneRepeatHash.containsKey(randomloc));//确定生成的随机数没有重复
		    CheckNoneRepeatHash.put(randomloc, 1);
		}
		
		/**
		 * 遍历CheckNoneRepeatHash表，将所有数据装入randomlocnum数组中。
		 */
		int[] randomlocnum=new int[number];
		Iterator iter = CheckNoneRepeatHash.entrySet().iterator();
		int j=0;
		while (iter.hasNext()) //遍历
		{
			Map.Entry entry = (Map.Entry) iter.next();
			randomlocnum[j] = Integer.parseInt(entry.getKey().toString());
		    j++;
		}
		
		/**
		 * 获得所产生的随机LOCID
		 */
		String[] randomLOCID=new String[number];//保存所生成的随机LOCID
		for(int i=0; i<number; i++)
		{
			 randomLOCID[i]=GffHash.LOCIDList.get(randomlocnum[i]);
		}		
		return randomLOCID;
	}
	
	
	
	int locationrange=1850;
	/**
	 * 输入LOCID，返回其随机的ATG前150-2000之间的坐标，
	 * 返回一维数组string[3]<br/>
	 * 0:chrID<br/>
	 * 1:location,实际上是数字，要类型转换成int<br/>
	 * 2:相对上游多少bp
	 */
	public String[] getRandomPromotorLocation(String LOCID) 
	{
		GffDetailList randomLOCID=Gffsearch.LOCsearch(LOCID);
		String chrID=randomLOCID.ChrID;//染色体位置
		int location=0;
		int upstreamlength=(int)(Math.random()*locationrange+150);//150~1850+150
		if(randomLOCID.cis5to3)
		{
			int tmp=randomLOCID.getcdslist(0).get(1);
			location=tmp-upstreamlength;
		}
		else 
		{
			int tmp=randomLOCID.getcdslist(0).get(1);
			location=tmp+upstreamlength;
		}
		String[] locationinfo=new String[3];
		locationinfo[0]=chrID;
		locationinfo[1]=location+"";
		locationinfo[2]=upstreamlength+"";
		return locationinfo;
	}
	
	
	/**
	 * 输入染色体序号，Peak坐标，LOCID 返回该LOCID的peak定位
	 * 以及所有外显子内含子的信息
	 * 返回string
	 * LOC 方向 peak距离    0-200----400-600----899-1200----1600<br>
	 * 0点位转录起点
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public String peakInfo(String ChrID, int peakloc)
	{
		/**
		 * 返回的查找信息
		 */
	   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
		int numbegin = peakInfo.begindistance.size();
		int numend=peakInfo.enddistance.size();
	
		boolean cis5to3=true;
		
		
	
		String LOCbeg="";//前一个基因编号
		String LOCend="";//后一个基因编号
		String lOCInfo="";//基因具体信息， 如   0----200-400-----600-899------1200-1600 0点是基因起点
		int StartLoc=0;//基因起点
		int PeaktoStartDistance=0;//Peak到Atg的距离，在Atg前为负数，后为正数
		
		/**
		 * 当在基因间时
		 */
	   if(!peakInfo.insidegene)
	   {	
		  
	
		   /**
		    *  如果前一个基因的方向为反向<-----那么就是peak和启动子ATG的距离
	         */
			if (!peakInfo.begincis5to3&& Math.abs(peakInfo.DistancetoGeneStart[0])<ATGUpStreambp)
			  { cis5to3=false; 
			    LOCbeg=peakInfo.LOCID[1];
			    //获得某个转录本的信息，此时peak距离该基因起点3K以内，且为反向。
			    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(1);
			    StartLoc=GffHash.locHashtable.get(LOCbeg).numberend;//基因起点位置
			    //Peak 到基因起点的距离，负数表示在基因前
			    PeaktoStartDistance=StartLoc-peakloc; 
			    lOCInfo=LOCbeg+" 反向:Peak距离  "+PeaktoStartDistance+ "   0";
			    //做出基因长度的图
			    for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//外显子终点，因为反向所以StartLoc最大
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//外显子起点
				   }
			    
			    }
			    //3'UTR到基因起点
			    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCbeg).numberstart);
			    LOCbeg="";
			    
			  }
			/**
			 * 如果前一个基因为正向---->那么就是peak和前一个基因末尾的距离，向后延长100bp就好
			 */
			else if(peakInfo.begincis5to3&& peakInfo.begindistance.get(0)[1]<GeneEnd3UTR)
			  {
				cis5to3=true;
			
				LOCbeg=peakInfo.LOCID[1];
				 //获得某个转录本的信息，此时peak距离该基因尾部100bp以内，且为正向。
				LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCbeg).numberstart;//基因起点位置
				  PeaktoStartDistance=peakloc-StartLoc; 
				  lOCInfo=LOCbeg+" 正向:Peak距离  "+PeaktoStartDistance+ "   0";
				 //做出基因长度的图
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//外显子终点，因为正向所以StartLoc最小
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//外显子起点
				   }
			    }
				//3'UTR到基因起点
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCbeg).numberend-StartLoc);
				 LOCbeg="";
				
			  }
			else
			{    
				
			}
	       
		   
		  
	  	 
	  		    /**
			     *  如果后一个基因的方向为正向----->那么就是peak和基因起点的距离
		         */
				if (peakInfo.endcis5to3&& Math.abs(peakInfo.DistancetoGeneStart[1])<ATGUpStreambp)
				{ cis5to3=true;
				 LOCend=peakInfo.LOCID[2];
				 LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCend).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCend).numberstart;//基因起点位置
				  PeaktoStartDistance=peakloc-StartLoc; 
				  if(lOCInfo.equals(""))//判断前一个基因是否有信息写入
				  {
				     lOCInfo=LOCend+" 正向:Peak距离  "+PeaktoStartDistance+ "   0";
				  }
				  else 
				  {
					  lOCInfo=lOCInfo+"     "+LOCend+" 正向:Peak距离  "+PeaktoStartDistance+ "   0";
				  }
				 //做出基因长度的图
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//外显子终点，因为正向所以StartLoc最小
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//外显子起点
				   }
			    }
				//3'UTR到基因起点
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCend).numberend-StartLoc);
				LOCend="";
	
				}
				 /**
			     *  如果后一个基因的方向反向<-----那么就是peak和后一个基因末尾的距离，向后延长100bp就好
		         */
				else if(!peakInfo.endcis5to3&& peakInfo.enddistance.get(0)[1]<GeneEnd3UTR)
				  {
					cis5to3=false;
				    
					LOCend=peakInfo.LOCID[2];
					//获得某个转录本的信息，此时peak距离该基因起点3K以内，且为反向。
				    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCend).getcdslist(0);
				    StartLoc=GffHash.locHashtable.get(LOCend).numberend;//基因起点位置
				    //Peak 到基因起点的距离，负数表示在基因前
				    PeaktoStartDistance=StartLoc-peakloc; 
				    if(lOCInfo.equals(""))
					  {
					     lOCInfo=LOCend+" 反向:Peak距离  "+PeaktoStartDistance+ "   0";
					  }
					  else 
					  {
						  lOCInfo=lOCInfo+"     "+LOCend+" 反向:Peak距离  "+PeaktoStartDistance+ "   0";
					  }
				    //做出基因长度的图
				    for(int m=1;m<CdsInfo.size();m++)
				    {
				    	if(m%2==0)
				    	{
				    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//外显子终点，因为反向所以StartLoc最大
				    	}
				       else 
				       {
				    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//外显子起点
					   }
				    
				    }
				    //3'UTR到基因起点
				    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCend).numberstart);
				    LOCend="";
				  }
				else 
				{
				
				}
		     
		   
	   }
	   else //基因内
	   {
		  
		    cis5to3=peakInfo.begincis5to3;
		    /**
		     * 分成正反向分别讨论
		     */
		    if(cis5to3)//正向
		    {
		    	 LOCbeg=peakInfo.LOCID[0];
				 LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCbeg).numberstart;//基因起点位置
				  PeaktoStartDistance=peakloc-StartLoc; 
				  lOCInfo=LOCbeg+" 正向:Peak距离  "+PeaktoStartDistance+ "   0";
				 //做出基因长度的图
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//外显子终点，因为正向所以StartLoc最小
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//外显子起点
				   }
			    }
				//3'UTR到基因起点
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCbeg).numberend-StartLoc);
				LOCbeg="";
		    }
		    else //反向
		    {
		        LOCbeg=peakInfo.LOCID[0];
			    //获得某个转录本的信息，此时peak距离该基因起点3K以内，且为反向。
			    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
			    StartLoc=GffHash.locHashtable.get(LOCbeg).numberend;//基因起点位置
			    //Peak 到基因起点的距离，负数表示在基因前
			    PeaktoStartDistance=StartLoc-peakloc; 
			    lOCInfo=LOCbeg+" 反向:Peak距离  "+PeaktoStartDistance+ "   0";
			    //做出基因长度的图
			    for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//外显子终点，因为反向所以StartLoc最大
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//外显子起点
				   }
			    
			    }
			    //3'UTR到基因起点
			    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCbeg).numberstart);
			    LOCbeg="";   
			}
	    }	   
	   return  lOCInfo;
	}

}
