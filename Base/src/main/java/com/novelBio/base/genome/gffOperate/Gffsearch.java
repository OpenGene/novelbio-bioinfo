package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *  ����ĳ������λ�㷵�ؾ����LOC����Լ���λ
  * ע�⣬������ȫ�ǽ�����GffHash��Ļ����ϵģ�����
  * ����Ҫ��GffHash���֧�ֲ��ܹ�����Ҳ����˵����������GffHash��ķ�����ȡGff�ļ���
  * ������Ҫʵ����
 * @author zong0jie
 *
 */
public abstract class Gffsearch {
	
	
	
	
	/**
	 * ˫�������
	 * ����ChrID����������(���Բ��ִ�С)���Լ�GffHash��<br>
	 *  chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 * �������������ܻ�����Ϣ-�洢��List�ļ���<br>
	 * <b>List�ļ�������������</b><br>
	 * 0��Object[2]:����<br>
	 * &nbsp;&nbsp;  0: ��С�����GffCodInfo��Ϣ���ö�Ӧ��GffCodInfo����ȥ����<br>
	 * &nbsp;&nbsp;  1: ��С�����overlap��������double[3]ȥ���գ�����ϱ����겻����Ŀ�ڣ���Ϊ0<b>�������ǰٷֱ�</b>:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       0: peak�����Item����ʱ�����������Item����ռ�ı���<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       1: peak�����Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       2: peak�����Item����ʱ��ʵ�ʽ�����bp��<br>
	 * 1��Object[2]:����<br>
	 * &nbsp;&nbsp;  0: �ϴ������GffCodInfo��Ϣ���ö�Ӧ��GffCodInfo����ȥ����<br>
	 * &nbsp;&nbsp;    1: �ϴ������overlap��������double[3]ȥ���գ�����ϱ����겻����Ŀ�ڣ���Ϊ0<b>�������ǰٷֱ�</b>:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       0: peak���Ҷ�Item����ʱ���������Ҷ���Ŀ����ռ�ı���<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       1: peak���Ҷ�Item����ʱ��������peak����ռ�ı����������ֵΪ100��˵����peak��һ��Item��<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;       2: peak���Ҷ���Ŀ����ʱ��ʵ�ʽ�����bp��<br>
	 * 2��3��4......�Ⱥ�����������������֮��Item��GffDetail��Ϣ���ö�Ӧ��GffDetail����ȥ����<br>
	 * ������Ҫ�Ľ�����ò�ͬ��GffCodInfo�������<br>
	 * ����GffsearchGene��������GffCodinfoGene����
	 * ����UCSCgene����ʱ��ֻ�������һ��ת¼��
	 */
	public ArrayList<Object> searchLocation(String ChrID, int CoordinateA, int CoordinateB , GffHash gffHash)
	{
		Hashtable<String, ArrayList<GffDetail>> LocHash=gffHash.getChrhash();
		ArrayList<GffDetail> Loclist = LocHash.get(ChrID.toLowerCase());//ĳһ��Ⱦɫ�����Ϣ
		
		//�������ĸ�Coordinate2��С�ĸ�Coordinate1
		int Coordinate1=0;int Coordinate2=0;
		if(CoordinateA>CoordinateB)
		{
			Coordinate1=CoordinateB;Coordinate2=CoordinateA;
		}
		else {
			Coordinate1=CoordinateA;Coordinate2=CoordinateB;
		}
		
		
		GffCodInfo gffCodInfo1=searchLocation(ChrID, Coordinate1, gffHash);
		GffCodInfo gffCodInfo2=searchLocation(ChrID, Coordinate2, gffHash);
		
		
		/**
		 * 0: peak����˽���ʱ�������������Ŀ����ռ�ı���
		 * 1: peak����˽���ʱ��������peak����ռ�ı���
		 * 2: peak����˽���ʱ�������ľ���bp��
		 */
		double[] cod1OvlapProp=new double[3];
		cod1OvlapProp[0]=0;cod1OvlapProp[1]=0;cod1OvlapProp[2]=0;
		/**
		 * 0: peak���Ҷ˽���ʱ�������������Ŀ����ռ�ı���
		 * 1: peak���Ҷ˽���ʱ��������peak����ռ�ı���
		 * 2: peak���Ҷ˽���ʱ�������ľ���bp��
		 */
		double[] cod2OvlapProp=new double[3];
		cod2OvlapProp[0]=0;cod2OvlapProp[1]=0;cod2OvlapProp[2]=0;
		
		/**
		 * ���peak�����˵㶼����ͬһ��Ŀ֮��
		 */
		if (gffCodInfo1.geneChrHashListNum[0]==gffCodInfo2.geneChrHashListNum[0]&&gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetail=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			int thisDetailLength=thisDetail.numberend-thisDetail.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			cod1OvlapProp[0]=100*(double)peakLength/thisDetailLength;
			cod1OvlapProp[1]=100;
			cod1OvlapProp[2]=peakLength;
			cod2OvlapProp[0]=cod1OvlapProp[0];
			cod2OvlapProp[1]=100;
			cod2OvlapProp[2]=cod1OvlapProp[2];
		}//���peak��˵���һ����Ŀ�ڣ��Ҷ˵�����һ����Ŀ��
		else if (gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC&&gffCodInfo1.geneChrHashListNum[0]!=gffCodInfo2.geneChrHashListNum[0])
		{
			GffDetail thisDetailleft=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			GffDetail thisDetailright=Loclist.get(gffCodInfo2.geneChrHashListNum[0]);
			int leftItemLength=thisDetailleft.numberend-thisDetailleft.numberstart;
			int rightItemLength=thisDetailright.numberend-thisDetailright.numberstart;

			int leftoverlap=thisDetailleft.numberend-Coordinate1;
			int rightoverlap=Coordinate2-thisDetailright.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=100*(double)leftoverlap/leftItemLength;
			cod1OvlapProp[1]=100*(double)leftoverlap/peakLength;
			cod1OvlapProp[2]=leftoverlap;
			cod2OvlapProp[0]=100*(double)rightoverlap/rightItemLength;
			cod2OvlapProp[1]=100*(double)rightoverlap/peakLength;
			cod2OvlapProp[2]=rightoverlap;
		}//peakֻ����˵�����Ŀ��
		else if (gffCodInfo1.insideLOC&&!gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetailleft=Loclist.get(gffCodInfo1.geneChrHashListNum[0]);
			int leftItemLength=thisDetailleft.numberend-thisDetailleft.numberstart;
			int leftoverlap=thisDetailleft.numberend-Coordinate1;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=100*(double)leftoverlap/leftItemLength;
			cod1OvlapProp[1]=100*(double)leftoverlap/peakLength;
			cod1OvlapProp[2]=leftoverlap;
			cod2OvlapProp[0]=0;
			cod2OvlapProp[1]=0;
			cod2OvlapProp[2]=0;
		}//peakֻ���Ҷ˵�����Ŀ��
		else if (!gffCodInfo1.insideLOC&&gffCodInfo2.insideLOC) 
		{
			GffDetail thisDetailright=Loclist.get(gffCodInfo2.geneChrHashListNum[0]);
			int rightItemLength=thisDetailright.numberend-thisDetailright.numberstart;
			int rightoverlap=Coordinate2-thisDetailright.numberstart;
			int peakLength=Coordinate2-Coordinate1;
			
			cod1OvlapProp[0]=0;
			cod1OvlapProp[1]=0;
			cod1OvlapProp[2]=0;
			cod2OvlapProp[0]=100*(double)rightoverlap/rightItemLength;
			cod2OvlapProp[1]=100*(double)rightoverlap/peakLength;
			cod2OvlapProp[2]=rightoverlap;
		}
		//��gffCodInfo��Ϣ�Լ���Ӧ��ռ�ı���װ��Object
		Object[] gffCodInfo1_overlap=new Object[2];
		gffCodInfo1_overlap[0]=gffCodInfo1;
		gffCodInfo1_overlap[1]=cod1OvlapProp;
		
		Object[] gffCodInfo2_overlap=new Object[2];
		gffCodInfo2_overlap[0]=gffCodInfo2;
		gffCodInfo2_overlap[1]=cod2OvlapProp;
		
		
		ArrayList<Object> LstGffCodInfo=new ArrayList<Object>();
		LstGffCodInfo.add(gffCodInfo1_overlap);
		LstGffCodInfo.add(gffCodInfo2_overlap);
		////////////////////////////////////////////////������������Լ��м���ŵ���Ŀ��ע�ⲻ�����������ڵĻ���///////////////////////////////////////////////////////////////////////////////
		//���������ID�Լ� ֮����Ŀ��ID��ֹ����-------------- coordinate1(Cod1ID)----------ItemA(startID)-------ItemB-------ItemC------ItemD(endID)-------coordinate2(Cod2ID)-----------------
		int Cod1ID=-1,  Cod2ID=-1;
		
		Cod1ID=gffCodInfo1.geneChrHashListNum[0];//����Ŀ/�ϸ���Ŀ���
			
		if(gffCodInfo2.insideLOC)
			Cod2ID=gffCodInfo2.geneChrHashListNum[0];//����Ŀ���
		else 
			Cod2ID=gffCodInfo2.geneChrHashListNum[1];//�¸���Ŀ���
		
		if((Cod2ID-Cod1ID)>1)//��Cod1ID��Cod2ID֮���������Ŀ��GffDetailװ��LstGffCodInfo
		{
			for (int i = 1; i <(Cod2ID-Cod1ID); i++) {
				GffDetail gffDetail=Loclist.get(Cod1ID+i);
				LstGffCodInfo.add(gffDetail);
			}
		}
		return LstGffCodInfo;
	}
	
	
	
	
	/**
	 * ���������
	 * ����ChrID���������꣬�Լ�GffHash��<br>
	 *  chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
	 * ��û�����Ϣ-�洢��GffCoordiatesInfo����<br>
	 * ������Ҫ�Ľ�����ò�ͬ��GffCodInfo�������<br>
	 * ����GffsearchGene��������GffCodinfoGene����
	 */
	public GffCodInfo searchLocation(String ChrID, int Coordinate, GffHash gffHash)
	{
		Hashtable<String, ArrayList<GffDetail>> LocHash=gffHash.getChrhash();;
		ArrayList<GffDetail> Loclist = LocHash.get(ChrID.toLowerCase());//ĳһ��Ⱦɫ�����Ϣ
	    if (Loclist==null)
	    {
	    	GffCodInfo cordInsideGeneInfo=new GffCodInfoGene();
	    	cordInsideGeneInfo.result=false;
	    	return cordInsideGeneInfo;
	    }
		return searchLocation(Coordinate,Loclist); 
	}
	
	
	/**
	 * ����PeakNum���͵���Chr��list��Ϣ
	 * ���ظ�PeakNum������LOCID���;���λ��
	 * ���ص���GffCoordinatesInfo���ʵ��������
	 */
	private GffCodInfo searchLocation(int Coordinate,ArrayList<GffDetail> Loclist)
	{
		String[] locationString=new String[5];
		locationString[0]="GffCodInfo_searchLocation error";
		locationString[1]="GffCodInfo_searchLocation error";
	    int[] locInfo=LocPosition(Loclist, Coordinate);//���ַ�����peaknum�Ķ�λ
		if(locInfo[0]==1) //��λ�ڻ�����
		{
			GffCodInfo LOCinsid= SearchLOCinside(Coordinate, Loclist,locInfo[1], locInfo[2]);//���Ҿ����ĸ��ں��ӻ���������
			LOCinsid.geneChrHashListNum[0]=locInfo[1];
			
			if (locInfo[1]==-1) 
				LOCinsid.geneDetail[0]=null;
			else 
				LOCinsid.geneDetail[0]=Loclist.get(locInfo[1]);
			
			if (locInfo[2]==-1) 
				LOCinsid.geneDetail[1]=null;
			else 
				LOCinsid.geneDetail[1]=Loclist.get(locInfo[2]);
			
			LOCinsid.geneChrHashListNum[1]=locInfo[2];
			
			
			return LOCinsid;
			}
		else if(locInfo[0]==2)
		{
			GffCodInfo LOCoutsid=SearchLOCoutside(Coordinate, Loclist, locInfo[1], locInfo[2]);//���һ����ⲿ��peak�Ķ�λ���
			if (locInfo[1]==-1) 
				LOCoutsid.geneDetail[0]=null;
			else 
				LOCoutsid.geneDetail[0]=Loclist.get(locInfo[1]);
			
			if (locInfo[2]==-1) 
				LOCoutsid.geneDetail[1]=null;
			else 
				LOCoutsid.geneDetail[1]=Loclist.get(locInfo[2]);
			
			LOCoutsid.geneChrHashListNum[0]=locInfo[1];
	
			LOCoutsid.geneChrHashListNum[1]=locInfo[2];
			return LOCoutsid;
		}
		return null;
	}
	
	

	/**
	 * ���ַ�����location���ڵ�λ��,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ�  /  �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 */
	private int[] LocPosition(ArrayList<GffDetail> Loclist,int Coordinate)
	{
		int[]LocInfo= new int[3]; 
		
		int endnum=0;
		
		endnum=Loclist.size()-1;

		int beginnum=0;
		int number=0;
		
		
		
		
		//�ڵ�һ��Item֮ǰ
		if (Coordinate<Loclist.get(beginnum).numberstart) {
			LocInfo[0]=2;
			LocInfo[1]=-1;
			LocInfo[2]=0;
			return LocInfo;
		}
		//�����һ��Item֮��
		else if (Coordinate>Loclist.get(endnum).numberstart) {
			LocInfo[1]=endnum;
			LocInfo[2]=-1;		
			if (Coordinate<Loclist.get(endnum).numberend) 
			{
				LocInfo[0]=1;
				return LocInfo;
			}
			else 
			{
				LocInfo[0]=2;
				return LocInfo;
			}	
		}
		
		
		do
		{
			number=(beginnum+endnum+1)/2;//3/2=1,5/2=2
			if(Coordinate==Loclist.get(number).numberstart)
		   {
			   beginnum=number;
			   endnum=number+1;
			   break;
		   }
			
		   else if(Coordinate<Loclist.get(number).numberstart&&number!=0)
		   {
			   endnum=number;
		   }
		   else
		   {
			   beginnum=number;
		   }
		}while((endnum-beginnum)>1);	
		LocInfo[1]=beginnum;
		LocInfo[2]=endnum;
		if(Coordinate<=Loclist.get(beginnum).numberend )//��֪���᲻�����PeakNumber��biginnumС�����
		{ //location�ڻ����ڲ�
           LocInfo[0]=1;
			return LocInfo;
		}
	     //location�ڻ����ⲿ
		 LocInfo[0]=2;
		return LocInfo;
	}
	
	/**
	 * ���뱻����
	 * @param coordinate
	 * @param loclist
	 * @param i
	 * @param j
	 * @return
	 */
	protected abstract  GffCodInfo SearchLOCinside(int coordinate,ArrayList<GffDetail> loclist, int i, int j) ;
	
	/**
	 * ���뱻����
	 * @param coordinate
	 * @param loclist
	 * @param i
	 * @param j
	 * @return
	 */
	protected abstract GffCodInfo SearchLOCoutside(int coordinate,ArrayList<GffDetail> loclist, int i, int j);
	
	
	/**
	 * ���������������ظû�����Ϣ,һ��GffDetailList��
	 * ������Ҫ�Ľ�����ò�ͬ��GffDetail�������<br>
	 * ����GffsearchGene��������GffDetailGene����
	 * @param LocID
	 */
	public static GffDetail LOCsearch(String LocID,GffHash gffHash)
	{
		return gffHash.LOCsearch(LocID);
	}
	
	
}
