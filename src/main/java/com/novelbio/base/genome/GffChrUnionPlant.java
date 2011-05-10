package com.novelbio.base.genome;

import genome.getChrSequence.ChrSearch;
import genome.getChrSequence.ChrStringHash;;
import genome.gffOperate.*;

public class GffChrUnionPlant extends GffChrUnion{
	/**
	 * �趨�����ATG���γ��ȣ�Ĭ��Ϊ3000bp
	 */
	static int ATGUpStreambp=3000;
	/**
	 * �趨�����ATG���γ��ȣ�Ĭ��Ϊ3000bp
	 */
	public void setATGUpStreambp(int upstreambp) {
		ATGUpStreambp=upstreambp;
	}
	
	
	
	/**
	 * ��ȡGff�ļ�������һ��������Ĺ�ϣ��-list�� 
	 * �� һ��LOC��Զ�Ӧ��Ϣ�Ĺ�ϣ��
	 * ������Ϣ��GffHash��
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
	 * ����LOCID�����γ��ȣ����ػ�õ���������(ָ��ATG����)
	 * �Զ��������е�������
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
	 * ����Ⱦɫ����ţ�Peak���꣬���ظ�peak��λ
	 * ��peak�ڻ����ڲ�ʱ�����ǻ���ķ���<br>
	 * �ڲ���ָ�ڻ���ת¼�������ָ����Χ�ڡ�
	 * ����string[2]
	 * 0:LOC ��ţ������Ļ�����LOCID+" "+LOCID
	 * 1:����������Ϣ
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public String[] peakLocation(String ChrID, int peakloc)
	{
		/**
		 * ��GeneInfo����ת��Ϊ������Ϣ
		 */
		HashMap<Integer, String> Detail1=new HashMap<Integer, String>();
		Detail1.put(1, "5��UTR");
        Detail1.put(2, "������");  		
        Detail1.put(3, "�ں���");  
        Detail1.put(4, "3��UTR");  
		/**
		 * ���صĲ�����Ϣ
		 */
	   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
		int numbegin = peakInfo.begindistance.size();
		int numend=peakInfo.enddistance.size();
		boolean flagupstream=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
		boolean flag3UTR=false;//�Ƿ��ڻ����100bp����
		boolean cis5to3=true;
		
		String insidegene="";//�Ƿ��ڻ�����
		String begfangxiang="";//��ǰһ���������Ϣ
		String endfangxiang="";//�ͺ�һ���������Ϣ
		String inseidfangxiang="";//�����ڲ��Ķ�λ��� 
		String LOCbeg="";//ǰһ��������
		String LOCend="";//��һ��������
		/**
		 * ���ڻ����ʱ
		 */
	   if(!peakInfo.insidegene)
	   {	
		   insidegene="�����";
		 
		   /**
		    *  ���ǰһ������ķ���Ϊ����<-----��ô����peak�ͻ������ľ���
	         */
			if (!peakInfo.begincis5to3&& Math.abs(peakInfo.DistancetoGeneStart[0])<UpstreamTSSbp)
			  { cis5to3=false; 
			    flagupstream=true;
			    insidegene="�ڲ������";
			    begfangxiang="����ǰһ�����򣬾���������"+Math.abs(peakInfo.DistancetoGeneStart[0])+"bp";
			    LOCbeg=peakInfo.LOCID[1];
			
			  }
			/**
			 * ���ǰһ������Ϊ����---->��ô����peak��ǰһ������ĩβ�ľ��룬����ӳ�100bp�ͺ�
			 */
			else if(peakInfo.begincis5to3&& peakInfo.begindistance.get(0)[1]<GeneEnd3UTR)
			  {
				cis5to3=true;
				flag3UTR=true;
				insidegene="�ڲ������";
				begfangxiang="����ǰһ�����򣬾���3UTR"+peakInfo.begindistance.get(0)[1]+"bp";
				LOCbeg=peakInfo.LOCID[1];
			
			  }
			else
			{    
				 begfangxiang="��ǰһ������"+peakInfo.begindistance.get(0)[1]+"bp";
				 LOCbeg=peakInfo.LOCID[1]+" no ";
			}
	       
	  	  
	  		    /**
			     *  �����һ������ķ���Ϊ����----->��ô����peak�ͻ������ľ���
		         */
				if (peakInfo.endcis5to3&& Math.abs(peakInfo.DistancetoGeneStart[1])<UpstreamTSSbp)
				{ cis5to3=true;
				  flagupstream=true;
				  insidegene="�ڲ������";
				 endfangxiang="�����һ�����򣬾���ATG"+Math.abs(peakInfo.DistancetoGeneStart[1])+"bp";
				 LOCend=peakInfo.LOCID[2];
	
				}
				 /**
			     *  �����һ������ķ�����<-----��ô����peak�ͺ�һ������ĩβ�ľ��룬����ӳ�100bp�ͺ�
		         */
				else if(!peakInfo.endcis5to3&& peakInfo.enddistance.get(0)[1]<GeneEnd3UTR)
				  {
					cis5to3=false;
					flag3UTR=true;
					insidegene="�ڲ������";
					endfangxiang="�����һ�����򣬾���3UTR"+peakInfo.enddistance.get(0)[1]+"bp";
					LOCend=peakInfo.LOCID[2];
			
				  }
				else 
				{
					endfangxiang="���һ������"+peakInfo.enddistance.get(0)[1]+"bp";
					LOCend=peakInfo.LOCID[2]+" no ";
				}
		     
	   }
	   else //������
	   {
		   insidegene="������";
		   begfangxiang="";
		   endfangxiang="";
		    cis5to3=peakInfo.begincis5to3;
		    flagupstream=true;
		    
		    /**
		     * ���β�������ת¼����peakInfo.GeneInfo.size()���ת¼������Ŀ
		     */
		   for(int i=0;i<peakInfo.GeneInfo.size();i++)
		   {
			   
			   /**
			    * ����λ����5��UTRʱ
			    */
			 if(peakInfo.GeneInfo.get(i)[1]!=1) 
			 {
			   if(!inseidfangxiang.contains(inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "��"+peakInfo.GeneInfo.get(i)[2]+"��"+"��ǰ��ľ���Ϊ"+peakInfo.GeneInfo.get(i)[3]))
			   inseidfangxiang=inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "��"+peakInfo.GeneInfo.get(i)[2]+"��"+"��ǰ��ľ���Ϊ"+peakInfo.GeneInfo.get(i)[3];
			 }
			 /**
			    * ����λ��5��UTRʱ�����غ�ATG�ľ���
			    */
			 else 
			 {
				 if(!inseidfangxiang.contains(inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "��"+peakInfo.GeneInfo.get(i)[2]+"��"+"��ATG�ľ���Ϊ"+peakInfo.GeneInfo.get(i)[4]))
				 inseidfangxiang=inseidfangxiang+"  "+ Detail1.get(peakInfo.GeneInfo.get(i)[1])+ "��"+peakInfo.GeneInfo.get(i)[2]+"��"+"��ATG�ľ���Ϊ"+peakInfo.GeneInfo.get(i)[4]; 
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
	 * ���ڻ�motif�ܶȷֲ�ͼ
	 * ����Ⱦɫ����ţ�Peak���꣬���ظ�peak����Gene����Լ�ATG�ľ���
	 * ��peak�ڻ����ڲ�ʱ�����ǻ���ķ���
	 * ����int
	 * �ͻ���������
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public int[] peakDistance(String ChrID, int peakloc)
	{
 
			/**
			 * ���صĲ�����Ϣ
			 */
		   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
			int numbegin = peakInfo.begindistance.size();
			int numend=peakInfo.enddistance.size();
			boolean flagupstream=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
			boolean flag3UTR=false;//�Ƿ��ڻ����100bp����
			boolean cis5to3=true;
			
			String insidegene="";//�Ƿ��ڻ�����
			String begfangxiang="";//��ǰһ���������Ϣ
			String endfangxiang="";//�ͺ�һ���������Ϣ
			String inseidfangxiang="";//�����ڲ��Ķ�λ��� 
			String LOCbeg="";//ǰһ��������
			String LOCend="";//��һ��������
			
			/**
			 * ���ڻ����ʱ,�����ĸ�������ͼ���͸û������ľ���
			 */
			int DistanceResult[]=new int[2]; //���������󱣴��peak����ͻ������ľ��롣
		 
			 
		
			  if(!peakInfo.insidegene){
				  GffDetailList BeforeGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[1]);//ǰһ���������Ϣ
				  GffDetailList AfterGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[2]);//ǰһ���������Ϣ
				  int BeginDistance = peakloc-BeforeGeneInfo.numberend;
				  int EndDistance = AfterGeneInfo.numberstart-peakloc;
				  if(BeginDistance<0||EndDistance<0)
				  {
					  System.out.println(peakloc);
				  }
			  if(peakInfo.begincis5to3)//ǰһ����������
			  {
				  if(peakInfo.endcis5to3)//��һ������Ҳ����
				  {
					  for(int i=0;i<peakInfo.enddistance.size();i++)//�ͺ�һ������Ĳ�ͬת¼����ATG��3k���ڵ�
					  { if(peakInfo.enddistance.get(i)[1]<3000)
					     {
						   DistanceResult[1]=peakloc-AfterGeneInfo.numberstart;;//��¼���룬Ϊ��ֵ
						   DistanceResult[0]=-1;
						   return DistanceResult;
					     }
					  }
					//�ͺ�һ�������ATG�����Զ����ô�ͱȽ������ǰ�����ľ��룬ȡ�̵�
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
				  else //��һ��������
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
			  else//ǰһ�������� 
			  {
				  if(peakInfo.endcis5to3)//��һ����������
				  {
				
					  
					//�Ƚ������ǰ�����ľ��룬ȡ�̵�
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
				  else //��һ��������
				  {
					  
					  for(int i=0;i<peakInfo.begindistance.size();i++)//��ǰһ������Ĳ�ͬת¼����ATG��3k���ڵ�
					  { if(peakInfo.begindistance.get(i)[1]<3000)
					     {
						   DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//��¼����
						   DistanceResult[0]=-1;
						   return DistanceResult;
					     }
					  }
					  
					  if(BeginDistance<EndDistance)//
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//��¼����
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
	 * ���ڻ�motif�ܶȷֲ�ͼ
	 * ����Ⱦɫ����ţ�Peak���꣬���ظ�peak����Gene����Լ�ATG�ľ���
	 * ��peak�ڻ����ڲ�ʱ�����ǻ���ķ���
	 * ����int
	 * �ͻ���������
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public int[] peakDistancenew(String ChrID, int peakloc)
	{
 
			/**
			 * ���صĲ�����Ϣ
			 */
		   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
			int numbegin = peakInfo.begindistance.size();
			int numend=peakInfo.enddistance.size();
			boolean flagupstream=false;//�Ƿ�������3000bp���ڣ�Ĭ��������
			boolean flag3UTR=false;//�Ƿ��ڻ����100bp����
			boolean cis5to3=true;
			
			String insidegene="";//�Ƿ��ڻ�����
			String begfangxiang="";//��ǰһ���������Ϣ
			String endfangxiang="";//�ͺ�һ���������Ϣ
			String inseidfangxiang="";//�����ڲ��Ķ�λ��� 
			String LOCbeg="";//ǰһ��������
			String LOCend="";//��һ��������
			
			/**
			 * ���ڻ����ʱ,�����ĸ�������ͼ���͸û������ľ���
			 */
			int DistanceResult[]=new int[2]; //���������󱣴��peak����ͻ������ľ��롣
		 
			 
		
			  if(!peakInfo.insidegene){
				  GffDetailList BeforeGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[1]);//ǰһ���������Ϣ
				  GffDetailList AfterGeneInfo= GffHash.locHashtable.get(peakInfo.LOCID[2]);//ǰһ���������Ϣ
				  int BeginDistance = peakloc-BeforeGeneInfo.numberend;
				  int EndDistance = AfterGeneInfo.numberstart-peakloc;
				  if(BeginDistance<0||EndDistance<0)
				  {
					  System.out.println(peakloc);
				  }
			  if(peakInfo.begincis5to3)//ǰһ����������
			  {
				  if(peakInfo.endcis5to3)//��һ������Ҳ����
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
				  else //��һ��������
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
			  else//ǰһ�������� 
			  {
				  if(peakInfo.endcis5to3)//��һ����������
				  {
				
					  
					//�Ƚ������ǰ�����ľ��룬ȡ�̵�
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
				  else //��һ��������
				  {
					  if(peakInfo.enddistance.get(0)[1]<100)
					  {
						  DistanceResult[1]=AfterGeneInfo.numberend-peakloc;
						  DistanceResult[0]=1;
						  return DistanceResult;
					  }
					  else
					  {
						  DistanceResult[1]=BeforeGeneInfo.numberend-peakloc;//��¼����
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
	 * �������У�����motif����Ŀ
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
	 * �������У�����motif����Ŀ
	 * @param Sequence
	 * @return
	 */
	public int MotifSearch(String Sequence)
	{
	   Pattern motifPattern =Pattern.compile(motifregex, Pattern.CASE_INSENSITIVE); //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
   	   Matcher motifmMatche;
   	   motifmMatche=motifPattern.matcher(Sequence);
   	   int motifcount=0;//motif����Ŀ
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
      boolean addStr=append; //ͨ������������ж��Ƿ����ı��ļ���׷������
                           //filepath �õ��ı��ļ���·��
                            // filecontent ��Ҫд�������
      File writefile=new File(filepath);
      if(writefile.exists()==false)    //����ı��ļ��������򴴽���
      {
          writefile.createNewFile();   
          writefile=new File(filepath);  //����ʵ����
      }
      FileWriter filewriter=new FileWriter(writefile,addStr);
      BufferedWriter bufwriter=new BufferedWriter(filewriter);
      filewriter.write(filecontent);
      filewriter.flush();
     }catch(Exception d){System.out.println(d.getMessage());}
    }
 
	/**
	 * ������Ŀ���������ɸ����������
	 * @param number
	 */
	public String[] getRandomLOC(int number)
	{
		int locnum=GffHash.LOCIDList.size();//����ˮ���������Ŀ
		HashMap<Integer, Integer> CheckNoneRepeatHash=new HashMap<Integer, Integer>();//�洢�����ɵ������
		 int randomloc=0;
		for(int i=0; i<number; i++)//����number�����ظ�α�����
		{
		    do//
		    {
			 randomloc=(int)(Math.random()*locnum);//����0-1000����������� ������Math.random()����0-1֮���˫����α�����������ͬ�ࣩ����������ˮ����Ŀ����ȡint�ͺ�	 
		     } while (CheckNoneRepeatHash.containsKey(randomloc));//ȷ�����ɵ������û���ظ�
		    CheckNoneRepeatHash.put(randomloc, 1);
		}
		
		/**
		 * ����CheckNoneRepeatHash������������װ��randomlocnum�����С�
		 */
		int[] randomlocnum=new int[number];
		Iterator iter = CheckNoneRepeatHash.entrySet().iterator();
		int j=0;
		while (iter.hasNext()) //����
		{
			Map.Entry entry = (Map.Entry) iter.next();
			randomlocnum[j] = Integer.parseInt(entry.getKey().toString());
		    j++;
		}
		
		/**
		 * ��������������LOCID
		 */
		String[] randomLOCID=new String[number];//���������ɵ����LOCID
		for(int i=0; i<number; i++)
		{
			 randomLOCID[i]=GffHash.LOCIDList.get(randomlocnum[i]);
		}		
		return randomLOCID;
	}
	
	
	
	int locationrange=1850;
	/**
	 * ����LOCID�������������ATGǰ150-2000֮������꣬
	 * ����һά����string[3]<br/>
	 * 0:chrID<br/>
	 * 1:location,ʵ���������֣�Ҫ����ת����int<br/>
	 * 2:������ζ���bp
	 */
	public String[] getRandomPromotorLocation(String LOCID) 
	{
		GffDetailList randomLOCID=Gffsearch.LOCsearch(LOCID);
		String chrID=randomLOCID.ChrID;//Ⱦɫ��λ��
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
	 * ����Ⱦɫ����ţ�Peak���꣬LOCID ���ظ�LOCID��peak��λ
	 * �Լ������������ں��ӵ���Ϣ
	 * ����string
	 * LOC ���� peak����    0-200----400-600----899-1200----1600<br>
	 * 0��λת¼���
	 * @param ChrID
	 * @param peakloc
	 * @return
	 */
	public String peakInfo(String ChrID, int peakloc)
	{
		/**
		 * ���صĲ�����Ϣ
		 */
	   GffCoordinatesInfo peakInfo = Gffsearch.searchLocation(ChrID, peakloc, GffHash.Chrhash);
		int numbegin = peakInfo.begindistance.size();
		int numend=peakInfo.enddistance.size();
	
		boolean cis5to3=true;
		
		
	
		String LOCbeg="";//ǰһ��������
		String LOCend="";//��һ��������
		String lOCInfo="";//���������Ϣ�� ��   0----200-400-----600-899------1200-1600 0���ǻ������
		int StartLoc=0;//�������
		int PeaktoStartDistance=0;//Peak��Atg�ľ��룬��AtgǰΪ��������Ϊ����
		
		/**
		 * ���ڻ����ʱ
		 */
	   if(!peakInfo.insidegene)
	   {	
		  
	
		   /**
		    *  ���ǰһ������ķ���Ϊ����<-----��ô����peak��������ATG�ľ���
	         */
			if (!peakInfo.begincis5to3&& Math.abs(peakInfo.DistancetoGeneStart[0])<ATGUpStreambp)
			  { cis5to3=false; 
			    LOCbeg=peakInfo.LOCID[1];
			    //���ĳ��ת¼������Ϣ����ʱpeak����û������3K���ڣ���Ϊ����
			    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(1);
			    StartLoc=GffHash.locHashtable.get(LOCbeg).numberend;//�������λ��
			    //Peak ���������ľ��룬������ʾ�ڻ���ǰ
			    PeaktoStartDistance=StartLoc-peakloc; 
			    lOCInfo=LOCbeg+" ����:Peak����  "+PeaktoStartDistance+ "   0";
			    //�������򳤶ȵ�ͼ
			    for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//�������յ㣬��Ϊ��������StartLoc���
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//���������
				   }
			    
			    }
			    //3'UTR���������
			    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCbeg).numberstart);
			    LOCbeg="";
			    
			  }
			/**
			 * ���ǰһ������Ϊ����---->��ô����peak��ǰһ������ĩβ�ľ��룬����ӳ�100bp�ͺ�
			 */
			else if(peakInfo.begincis5to3&& peakInfo.begindistance.get(0)[1]<GeneEnd3UTR)
			  {
				cis5to3=true;
			
				LOCbeg=peakInfo.LOCID[1];
				 //���ĳ��ת¼������Ϣ����ʱpeak����û���β��100bp���ڣ���Ϊ����
				LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCbeg).numberstart;//�������λ��
				  PeaktoStartDistance=peakloc-StartLoc; 
				  lOCInfo=LOCbeg+" ����:Peak����  "+PeaktoStartDistance+ "   0";
				 //�������򳤶ȵ�ͼ
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//�������յ㣬��Ϊ��������StartLoc��С
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//���������
				   }
			    }
				//3'UTR���������
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCbeg).numberend-StartLoc);
				 LOCbeg="";
				
			  }
			else
			{    
				
			}
	       
		   
		  
	  	 
	  		    /**
			     *  �����һ������ķ���Ϊ����----->��ô����peak�ͻ������ľ���
		         */
				if (peakInfo.endcis5to3&& Math.abs(peakInfo.DistancetoGeneStart[1])<ATGUpStreambp)
				{ cis5to3=true;
				 LOCend=peakInfo.LOCID[2];
				 LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCend).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCend).numberstart;//�������λ��
				  PeaktoStartDistance=peakloc-StartLoc; 
				  if(lOCInfo.equals(""))//�ж�ǰһ�������Ƿ�����Ϣд��
				  {
				     lOCInfo=LOCend+" ����:Peak����  "+PeaktoStartDistance+ "   0";
				  }
				  else 
				  {
					  lOCInfo=lOCInfo+"     "+LOCend+" ����:Peak����  "+PeaktoStartDistance+ "   0";
				  }
				 //�������򳤶ȵ�ͼ
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//�������յ㣬��Ϊ��������StartLoc��С
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//���������
				   }
			    }
				//3'UTR���������
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCend).numberend-StartLoc);
				LOCend="";
	
				}
				 /**
			     *  �����һ������ķ�����<-----��ô����peak�ͺ�һ������ĩβ�ľ��룬����ӳ�100bp�ͺ�
		         */
				else if(!peakInfo.endcis5to3&& peakInfo.enddistance.get(0)[1]<GeneEnd3UTR)
				  {
					cis5to3=false;
				    
					LOCend=peakInfo.LOCID[2];
					//���ĳ��ת¼������Ϣ����ʱpeak����û������3K���ڣ���Ϊ����
				    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCend).getcdslist(0);
				    StartLoc=GffHash.locHashtable.get(LOCend).numberend;//�������λ��
				    //Peak ���������ľ��룬������ʾ�ڻ���ǰ
				    PeaktoStartDistance=StartLoc-peakloc; 
				    if(lOCInfo.equals(""))
					  {
					     lOCInfo=LOCend+" ����:Peak����  "+PeaktoStartDistance+ "   0";
					  }
					  else 
					  {
						  lOCInfo=lOCInfo+"     "+LOCend+" ����:Peak����  "+PeaktoStartDistance+ "   0";
					  }
				    //�������򳤶ȵ�ͼ
				    for(int m=1;m<CdsInfo.size();m++)
				    {
				    	if(m%2==0)
				    	{
				    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//�������յ㣬��Ϊ��������StartLoc���
				    	}
				       else 
				       {
				    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//���������
					   }
				    
				    }
				    //3'UTR���������
				    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCend).numberstart);
				    LOCend="";
				  }
				else 
				{
				
				}
		     
		   
	   }
	   else //������
	   {
		  
		    cis5to3=peakInfo.begincis5to3;
		    /**
		     * �ֳ�������ֱ�����
		     */
		    if(cis5to3)//����
		    {
		    	 LOCbeg=peakInfo.LOCID[0];
				 LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
				 StartLoc=GffHash.locHashtable.get(LOCbeg).numberstart;//�������λ��
				  PeaktoStartDistance=peakloc-StartLoc; 
				  lOCInfo=LOCbeg+" ����:Peak����  "+PeaktoStartDistance+ "   0";
				 //�������򳤶ȵ�ͼ
				for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(CdsInfo.get(m)-StartLoc);//�������յ㣬��Ϊ��������StartLoc��С
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(CdsInfo.get(m)-StartLoc);//���������
				   }
			    }
				//3'UTR���������
				lOCInfo=lOCInfo+"----"+(GffHash.locHashtable.get(LOCbeg).numberend-StartLoc);
				LOCbeg="";
		    }
		    else //����
		    {
		        LOCbeg=peakInfo.LOCID[0];
			    //���ĳ��ת¼������Ϣ����ʱpeak����û������3K���ڣ���Ϊ����
			    LinkedList<Integer> CdsInfo=GffHash.locHashtable.get(LOCbeg).getcdslist(0);
			    StartLoc=GffHash.locHashtable.get(LOCbeg).numberend;//�������λ��
			    //Peak ���������ľ��룬������ʾ�ڻ���ǰ
			    PeaktoStartDistance=StartLoc-peakloc; 
			    lOCInfo=LOCbeg+" ����:Peak����  "+PeaktoStartDistance+ "   0";
			    //�������򳤶ȵ�ͼ
			    for(int m=1;m<CdsInfo.size();m++)
			    {
			    	if(m%2==0)
			    	{
			    		lOCInfo=lOCInfo+"-"+(StartLoc-CdsInfo.get(m));//�������յ㣬��Ϊ��������StartLoc���
			    	}
			       else 
			       {
			    	   lOCInfo=lOCInfo+"----"+(StartLoc-CdsInfo.get(m));//���������
				   }
			    
			    }
			    //3'UTR���������
			    lOCInfo=lOCInfo+"----"+(StartLoc-GffHash.locHashtable.get(LOCbeg).numberstart);
			    LOCbeg="";   
			}
	    }	   
	   return  lOCInfo;
	}

}
