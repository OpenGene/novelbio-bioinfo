package com.novelbio.analysis.seq.genomeNew;

 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailCG;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashCG;
import com.novelbio.analysis.seq.genome.gffOperate.Gffsearch;
import com.novelbio.analysis.seq.genome.gffOperate.GffsearchCG;

public class GffToCG {
	Gffsearch gffSearchCG=new GffsearchCG();
	GffHashCG gffHashCG=new GffHashCG();
	/**
	 * ���ǶȰٷֱȣ�ͳ��repeat��Ŀʱ�õ��������ڸðٷֱ�ʱ��repeat����
	 */
	int overLapProp=50;
	public void setOverlapProp(int overLapProp) {
		this.overLapProp=overLapProp;
	}
	
	/**
	 * ��ȡRepeat�������ļ�
	 * @param gfffilename
	 * @throws Exception
	 */
	public void prepare(String gfffilename) throws Exception 
	{
		gffHashCG.ReadGffarray(gfffilename);
	}
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ľ���bp��,�����ӷ����������
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * ��������ͳ����Ϣ<br>
	 * ����˵peak�����ǵ�ÿ������CpG�ľ���bp��
	 * �м����ĳ��repeat�Ļ�����ӦCpG+1��Ȼ��NoCpG+1<br>
	 * ���
	 * arraylist-string[2]
	 * 0:CGClass
	 * 1:Num
	 */
	public ArrayList<String[]> locCodRegBp(String[][] LOCIDInfo)
	{
		String outofCG="OutOfCG";
		HashMap<String, Long> hashStatistic=new HashMap<String, Long>();
		hashStatistic.put(outofCG, (long)0);
		for (int i = 0; i < LOCIDInfo.length; i++) {
			ArrayList<Object> tmpresult=gffSearchCG.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]),Integer.parseInt(LOCIDInfo[i][2]), gffHashCG);
			long peakLen=Math.abs(Long.parseLong(LOCIDInfo[i][1])-Long.parseLong(LOCIDInfo[i][2]));//peak�ĳ���
			Object[] objleftCodInfo=(Object[]) tmpresult.get(0);
			GffCodInfo leftCodInfo=(GffCodInfo)objleftCodInfo[0];
			double[] leftOverlap=(double[])objleftCodInfo[1];//��ߵĽ������
			
			
			Object[] objrightCodInfo=(Object[]) tmpresult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//�ұߵĽ������
			long tmpOverlap=0;
			if(leftCodInfo.insideLOC)//������
			{
				String locString=leftCodInfo.LOCID[0];
				GffDetailCG tmpCG =(GffDetailCG)gffHashCG.getLocHashtable().get(locString);
				String CGClass="CG";
				if(hashStatistic.containsKey(CGClass))
				{
					long tmp=hashStatistic.get(CGClass);
					tmp=tmp+(long)leftOverlap[2];
					hashStatistic.put(CGClass, tmp);
				}
				else 
				{
					hashStatistic.put(CGClass, (long)leftOverlap[2]);
				}
				tmpOverlap=(long)leftOverlap[2];
			}
			if(rightCodInfo.insideLOC)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//����������㶼��һ��CG�ڣ����ҵ㲻������������֮��Ҳ��ͬ���ˣ��϶�û�У��ͽ�����һ��ѭ��
				//���ڿ��������˵㲻��һ��CG��
				if (!locString.equals(leftCodInfo.LOCID[0])) 
				{
					GffDetailCG tmpCG =(GffDetailCG)gffHashCG.getLocHashtable().get(locString);
					String CGClass="CG";
					if(hashStatistic.containsKey(CGClass))
					{
						long tmp=hashStatistic.get(CGClass);
						tmp=tmp+(long)rightOverlap[2];
						hashStatistic.put(CGClass, tmp);
					}
					else 
					{
						hashStatistic.put(CGClass, (long)rightOverlap[2]);
					}
					tmpOverlap=tmpOverlap+(long)rightOverlap[2];
				}
			}
		
			//��peak���Ƿ�Χ����û��CG���еĻ��ͼ������
			for (int j = 2; j < tmpresult.size(); j++) {
				GffDetailCG tmpDetailCG=(GffDetailCG)tmpresult.get(j);
				String CGClass="CG";
				long CGLen=tmpDetailCG.numberend-tmpDetailCG.numberstart;
				if(hashStatistic.containsKey(CGClass))
				{
					long tmp=hashStatistic.get(CGClass);
					tmp=tmp+CGLen;
					hashStatistic.put(CGClass, tmp);
				}
				else 
				{
					hashStatistic.put(CGClass,CGLen);
				}
				
				tmpOverlap=tmpOverlap+CGLen;
			}
			long tmpOutOfCG=peakLen-tmpOverlap;
			if(tmpOutOfCG<0)
			{
				System.out.println("error tmpOutOfCG<0");
			}
			long tmp=hashStatistic.get(outofCG);
			tmp=tmp+tmpOutOfCG;
			hashStatistic.put(outofCG, tmp);
		}
		//����ϣ�������װ��list
		ArrayList<String[]> result=new ArrayList<String[]>();
		Iterator iter = hashStatistic.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String[] tmpresult=new String[2]; 
		    
		    tmpresult[0]=(String)entry.getKey();
		    tmpresult[1]=(Long)entry.getValue()+"";
		    result.add(tmpresult);
		}
		return result;
	}
	
	
	
	
	/**
	 * ������ά����,���ÿ��peak��CpG�ľ�����������UCSCknown gene
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * ����ÿ��������repeat�ϵľ������<br>
	 * arraylist-string[5]<br>
	 * 0:ChrID<br>
	 * 1:����<br>
	 * 2:����CpG
	 * 3:����ÿ��CpG��CG����<br>
	 * 4:����ÿ��CpG��CG�ٷֱ�
	 */
	public ArrayList<String[]> locateCodregionInfo(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lsresult=new ArrayList<String[]>();

		for (int i = 0; i < LOCIDInfo.length; i++) {
			String[] tmpCGInfo=new String[5];
			tmpCGInfo[0]=LOCIDInfo[i][0];
			tmpCGInfo[1]=LOCIDInfo[i][1];
			
			ArrayList<Object> tmpCGResult=gffSearchCG.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]),Integer.parseInt(LOCIDInfo[i][2]), gffHashCG);
			
			Object[] objleftCodInfo=(Object[]) tmpCGResult.get(0);
			GffCodInfo leftCodInfo=(GffCodInfo)objleftCodInfo[0];
			double[] leftOverlap=(double[])objleftCodInfo[1];//��ߵĽ������
			
			Object[] objrightCodInfo=(Object[]) tmpCGResult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//�ұߵĽ������

			int tmpCGNum=0;//�ܹ�����repeat
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//������)//������
			{
				tmpCGNum=1;
				String locString=leftCodInfo.LOCID[0];
				GffDetailCG tmpDetailCG =(GffDetailCG)gffHashCG.getLocHashtable().get(locString);
				tmpCGInfo[3]=tmpDetailCG.lengthCpG+"";
				tmpCGInfo[4]=tmpDetailCG.perCpG+"";;
			}
			else {
				tmpCGInfo[3]="outofCG";
				tmpCGInfo[4]="none";
			}

			//��peak���Ƿ�Χ����û��repeat���еĻ��ͼ������
			for (int j = 2; j < tmpCGResult.size(); j++) {
				tmpCGNum++;
				GffDetailCG tmpDetailCG=(GffDetailCG)tmpCGResult.get(j);
				tmpCGInfo[3]=tmpCGInfo[3]+"///"+tmpDetailCG.lengthCpG;
				tmpCGInfo[4]=tmpCGInfo[4]+"///"+tmpDetailCG.perCpG;
			}
			
			///////////////�ұߵ�repeatλ��///////////////////////
			if(rightOverlap[0]>=overLapProp||rightOverlap[1]>=overLapProp)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//����������㶼��һ��repeat�ڣ����ҵ㲻������������֮��Ҳ��ͬ���ˣ��϶�û�У��ͽ�����һ��ѭ��
				if(locString!=leftCodInfo.LOCID[0])
				{
					tmpCGNum++;
					GffDetailCG tmpCG =(GffDetailCG)gffHashCG.getLocHashtable().get(locString);
					tmpCGInfo[3]=tmpCGInfo[3]+"///"+tmpCG.locString;
					tmpCGInfo[4]=tmpCGInfo[4]+"///"+tmpCG.perCpG;
				}
				//////////////////////////////////////////
			}
			else 
			{
				tmpCGInfo[3]=tmpCGInfo[3]+"///"+"outofCG";
				tmpCGInfo[4]=tmpCGInfo[4]+"///"+"none";
			}
			tmpCGInfo[2]=tmpCGNum+"";
			lsresult.add(tmpCGInfo);
		}
		return lsresult;
	}
}
