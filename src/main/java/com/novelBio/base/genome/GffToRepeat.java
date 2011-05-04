package com.novelBio.base.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.novelBio.base.genome.gffOperate.GffCodInfo;
import com.novelBio.base.genome.gffOperate.GffDetailRepeat;
import com.novelBio.base.genome.gffOperate.GffHashRepeat;
import com.novelBio.base.genome.gffOperate.GffsearchRepeat;




public class GffToRepeat {
	
	GffsearchRepeat gffSearchRepeat=new GffsearchRepeat();
	GffHashRepeat gffHashRepeat=new GffHashRepeat();
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
		gffHashRepeat.ReadGffarray(gfffilename);
	}
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ı��أ����UCSCknownGeneRepeatMasker
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * ��������ͳ����Ϣ<br>
	 * arraylist-string[2]<br>
	 * 0:repeatClass<br>
	 * 1:Num<br>
	 */
	public ArrayList<String[]> locateCod(String[][] LOCIDInfo)
	{
		String outofRepeat="outofRepeat";
		//ͳ�ƽ������hashStatistic��key repeat���ͣ�value repeat��Ŀ
		HashMap<String, Integer> hashStatistic=new HashMap<String, Integer>();
		hashStatistic.put(outofRepeat, 0);
		for (int i = 0; i < LOCIDInfo.length; i++) {
			GffCodInfo tmpresult=(GffCodInfo)gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHashRepeat);
			if(tmpresult.insideLOC)//������
			{
				String locString=tmpresult.LOCID[0];
				GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
				String repeatClass=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
				if(hashStatistic.containsKey(repeatClass))
				{
					Integer tmp=hashStatistic.get(repeatClass);
					tmp=tmp+1;
					hashStatistic.put(repeatClass, tmp);
				}
				else 
				{
					hashStatistic.put(repeatClass, 1);
				}
			}
			else 
			{
				Integer tmp=hashStatistic.get(outofRepeat);
				tmp++;
				hashStatistic.put(outofRepeat, tmp);
			}
		}
		
		//����ϣ�������װ��list
		ArrayList<String[]> result=new ArrayList<String[]>();
		Iterator iter = hashStatistic.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String[] tmpresult=new String[2]; 
		    
		    tmpresult[0]=(String)entry.getKey();
		    tmpresult[1]=(Integer)entry.getValue()+"";
		    result.add(tmpresult);
		}
		return result;
	}
	
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ı��أ����UCSCknownGeneRepeatMasker
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * ����ÿ��������repeat�ϵľ������<br>
	 * arraylist-string[4]<br>
	 * 0:ChrID<br>
	 * 1:����<br>
	 * 2:repeatName<br>
	 * 3:repeat-Class-family
	 */
	public ArrayList<String[]> locateCodInfo(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lsresult=new ArrayList<String[]>();
		for (int i = 0; i < LOCIDInfo.length; i++) 
		{
			String[] tmprepeatInfo=new String[4];
			tmprepeatInfo[0]=LOCIDInfo[i][0];
			tmprepeatInfo[1]=LOCIDInfo[i][1];
			GffCodInfo tmpCodInfo=(GffCodInfo)gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHashRepeat);
			if(tmpCodInfo.insideLOC)//������
			{
				tmprepeatInfo[2]=tmpCodInfo.LOCID[0];
				GffDetailRepeat tmpDetailRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(tmprepeatInfo[2]);
				tmprepeatInfo[3]=tmpDetailRepeat.repeatClass+"/"+tmpDetailRepeat.repeatFamily;
			}
			else 
			{
				tmprepeatInfo[2]="outofrepeat";
				tmprepeatInfo[3]="none";
			}
			lsresult.add(tmprepeatInfo);
		}
		return lsresult;
	}
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ı��أ����UCSCknown gene,
	 * @param LOCIDInfo
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * @param Bp
	 * true:����peak�����ǵ�ÿ��repeat��bp��<br>
	 * false:��peak��repeat�Ľ���>50%(�����бȽ�С���Ǹ���50%)ʱ������repeat��Ŀ+1<br>
	 * �����˵�/�Ҷ˵���repeat�ڲ����󲿷ֵ�repeat/peak��ռ��������overLapProp�����1������outofRepeat+1<br>
	 * �м����ĳ��repeat�Ļ�����Ӧrepeat+1��Ȼ��outofRepeat+1<br>
	 * ��������ͳ����Ϣ<br>
	 * ���
	 * arraylist-string[2]
	 * 0:repeatClass
	 * 1:Num
	 */
	public ArrayList<String[]> locCodReg(String[][] LOCIDInfo,boolean Bp)
	{
		if (Bp) {
			return locCodRegBp(LOCIDInfo);
		}
		else {
			return locCodRegNum(LOCIDInfo);
		}
	}
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ı��أ����UCSCknown gene,ֻ�е�peak��region�Ľ������ִ����趨��overLapPropʱ������һ��
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * ��������ͳ����Ϣ<br>
	 * �����˵�/�Ҷ˵���repeat�ڲ����󲿷ֵ�repeat/peak��ռ��������overLapProp�����1������outofRepeat+1<br>
	 * �м����ĳ��repeat�Ļ�����Ӧrepeat+1��Ȼ��outofRepeat+1<br>
	 * ���
	 * arraylist-string[2]
	 * 0:repeatClass
	 * 1:Num
	 */
	private ArrayList<String[]> locCodRegNum(String[][] LOCIDInfo)
	{
		String outofRepeat="outofRepeat";
		HashMap<String, Integer> hashStatistic=new HashMap<String, Integer>();
		hashStatistic.put(outofRepeat, 0);
		for (int i = 0; i < LOCIDInfo.length; i++) {
			ArrayList<Object> tmpresult=gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]),Integer.parseInt(LOCIDInfo[i][2]), gffHashRepeat);
			
			Object[] objleftCodInfo=(Object[]) tmpresult.get(0);
			GffCodInfo leftCodInfo=(GffCodInfo)objleftCodInfo[0];
			double[] leftOverlap=(double[])objleftCodInfo[1];//��ߵĽ������
			
			
			Object[] objrightCodInfo=(Object[]) tmpresult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//�ұߵĽ������
			
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//������
			{
				String locString=leftCodInfo.LOCID[0];
				GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
				String repeatClass=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
				if(hashStatistic.containsKey(repeatClass))
				{
					Integer tmp=hashStatistic.get(repeatClass);
					tmp=tmp+1;
					hashStatistic.put(repeatClass, tmp);
				}
				else 
				{
					hashStatistic.put(repeatClass, 1);
				}
			}
			else {
				Integer tmp=hashStatistic.get(outofRepeat);
				tmp++;
				hashStatistic.put(outofRepeat, tmp);
			}

			if(rightOverlap[0]>=overLapProp||rightOverlap[1]>=overLapProp)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//����������㶼��һ��repeat�ڣ����ҵ㲻������������֮��Ҳ��ͬ���ˣ��϶�û�У��ͽ�����һ��ѭ��
				if(locString!=leftCodInfo.LOCID[0])
				{
					GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
					String repeatClass=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
					if(hashStatistic.containsKey(repeatClass))
					{
						Integer tmp=hashStatistic.get(repeatClass);
						tmp=tmp+1;
						hashStatistic.put(repeatClass, tmp);
					}
					else 
					{
						hashStatistic.put(repeatClass, 1);
					}
				}
				//////////////////////////////////////////
			}
			else 
			{
				Integer tmp=hashStatistic.get(outofRepeat);
				tmp++;
				hashStatistic.put(outofRepeat, tmp);
			}
			//��peak���Ƿ�Χ����û��repeat���еĻ��ͼ������
			for (int j = 2; j < tmpresult.size(); j++) {
				GffDetailRepeat tmpDetailRepeat=(GffDetailRepeat)tmpresult.get(j);
				String repeatClass=tmpDetailRepeat.repeatClass+"/"+tmpDetailRepeat.repeatFamily;
				if(hashStatistic.containsKey(repeatClass))
				{
					Integer tmp=hashStatistic.get(repeatClass);
					tmp=tmp+1;
					hashStatistic.put(repeatClass, tmp);
				}
				else 
				{
					hashStatistic.put(repeatClass, 1);
				}
				
				Integer tmp=hashStatistic.get(outofRepeat);
				tmp++;
				hashStatistic.put(outofRepeat, tmp);
			}
		}
		//����ϣ�������װ��list
		ArrayList<String[]> result=new ArrayList<String[]>();
		Iterator iter = hashStatistic.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    String[] tmpresult=new String[2]; 
		    
		    tmpresult[0]=(String)entry.getKey();
		    tmpresult[1]=(Integer)entry.getValue()+"";
		    result.add(tmpresult);
		}
		return result;
	}
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ľ���bp��,�����ӷ����������
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * ��������ͳ����Ϣ<br>
	 * ����˵peak�����ǵ�ÿ������repeat�ľ���bp��
	 * �м����ĳ��repeat�Ļ�����Ӧrepeat+1��Ȼ��outofRepeat+1<br>
	 * ���
	 * arraylist-string[2]
	 * 0:repeatClass
	 * 1:Num
	 */
	private ArrayList<String[]> locCodRegBp(String[][] LOCIDInfo)
	{
		String outofRepeat="OutOfRepeat";
		HashMap<String, Long> hashStatistic=new HashMap<String, Long>();
		hashStatistic.put(outofRepeat, (long)0);
		for (int i = 0; i < LOCIDInfo.length; i++) {
			ArrayList<Object> tmpresult=gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]),Integer.parseInt(LOCIDInfo[i][2]), gffHashRepeat);
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
				GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
				String repeatClass=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
				if(hashStatistic.containsKey(repeatClass))
				{
					long tmp=hashStatistic.get(repeatClass);
					tmp=tmp+(long)leftOverlap[2];
					hashStatistic.put(repeatClass, tmp);
				}
				else 
				{
					hashStatistic.put(repeatClass, (long)leftOverlap[2]);
				}
				tmpOverlap=(long)leftOverlap[2];
			}
			if(rightCodInfo.insideLOC)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//����������㶼��һ��repeat�ڣ����ҵ㲻������������֮��Ҳ��ͬ���ˣ��϶�û�У��ͽ�����һ��ѭ��
				//���ڿ��������˵㲻��һ��repeat��
				if (!locString.equals(leftCodInfo.LOCID[0])) 
				{
					GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
					String repeatClass=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
					if(hashStatistic.containsKey(repeatClass))
					{
						long tmp=hashStatistic.get(repeatClass);
						tmp=tmp+(long)rightOverlap[2];
						hashStatistic.put(repeatClass, tmp);
					}
					else 
					{
						hashStatistic.put(repeatClass, (long)rightOverlap[2]);
					}
					tmpOverlap=tmpOverlap+(long)rightOverlap[2];
				}
			}
		
			//��peak���Ƿ�Χ����û��repeat���еĻ��ͼ������
			for (int j = 2; j < tmpresult.size(); j++) {
				GffDetailRepeat tmpDetailRepeat=(GffDetailRepeat)tmpresult.get(j);
				String repeatClass=tmpDetailRepeat.repeatClass+"/"+tmpDetailRepeat.repeatFamily;
				long repeatLen=tmpDetailRepeat.numberend-tmpDetailRepeat.numberstart;
				if(hashStatistic.containsKey(repeatClass))
				{
					long tmp=hashStatistic.get(repeatClass);
					tmp=tmp+repeatLen;
					hashStatistic.put(repeatClass, tmp);
				}
				else 
				{
					hashStatistic.put(repeatClass,repeatLen);
				}
				
				tmpOverlap=tmpOverlap+repeatLen;
			}
			long tmpOutOfRepeat=peakLen-tmpOverlap;
			if(tmpOutOfRepeat<0)
			{
				//System.out.println("error tmpOutOfRepeat<0,set it to 0");
				//������repeat�ص�ʱ�ͻ���ָ�������������Ǵ��ڵ�
				System.out.println("error tmpOutOfRepeat<0,set it to 0");
				tmpOutOfRepeat=0;
			}
			long tmp=hashStatistic.get(outofRepeat);
			tmp=tmp+tmpOutOfRepeat;
			hashStatistic.put(outofRepeat, tmp);
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
	 * ������ά����,���ÿ��peak��repeat�ľ�����������UCSCknown gene
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������,����άҲ������<br>
	 * ����ÿ��������repeat�ϵľ������<br>
	 * arraylist-string[5]<br>
	 * 0:ChrID<br>
	 * 1:����<br>
	 * 2:����repeat
	 * 3:����ÿ��repeat��Name<br>
	 * 4:����ÿ��repeat-Class-family
	 */
	public ArrayList<String[]> locateCodregionInfo(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lsresult=new ArrayList<String[]>();

		for (int i = 0; i < LOCIDInfo.length; i++) {
			String[] tmpRepeatInfo=new String[5];
			tmpRepeatInfo[0]=LOCIDInfo[i][0];
			tmpRepeatInfo[1]=LOCIDInfo[i][1];
			
			ArrayList<Object> tmpRepeatResult=gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]),Integer.parseInt(LOCIDInfo[i][2]), gffHashRepeat);
			
			Object[] objleftCodInfo=(Object[]) tmpRepeatResult.get(0);
			GffCodInfo leftCodInfo=(GffCodInfo)objleftCodInfo[0];
			double[] leftOverlap=(double[])objleftCodInfo[1];//��ߵĽ������
			
			Object[] objrightCodInfo=(Object[]) tmpRepeatResult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//�ұߵĽ������

			int tmpRepeatNum=0;//�ܹ�����repeat
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//������)//������
			{
				tmpRepeatNum=1;
				String locString=leftCodInfo.LOCID[0];
				tmpRepeatInfo[3]=locString;
				GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
				tmpRepeatInfo[4]=tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
			}
			else {
				tmpRepeatInfo[3]="outofRepeat";
				tmpRepeatInfo[4]="none";
			}

			//��peak���Ƿ�Χ����û��repeat���еĻ��ͼ������
			for (int j = 2; j < tmpRepeatResult.size(); j++) {
				tmpRepeatNum++;
				GffDetailRepeat tmpDetailRepeat=(GffDetailRepeat)tmpRepeatResult.get(j);
				tmpRepeatInfo[3]=tmpRepeatInfo[3]+"///"+tmpDetailRepeat.locString;
				tmpRepeatInfo[4]=tmpRepeatInfo[4]+"///"+tmpDetailRepeat.repeatClass+"/"+tmpDetailRepeat.repeatFamily;
			}
			
			///////////////�ұߵ�repeatλ��///////////////////////
			if(rightOverlap[0]>=overLapProp||rightOverlap[1]>=overLapProp)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//����������㶼��һ��repeat�ڣ����ҵ㲻������������֮��Ҳ��ͬ���ˣ��϶�û�У��ͽ�����һ��ѭ��
				if(locString!=leftCodInfo.LOCID[0])
				{
					tmpRepeatNum++;
					GffDetailRepeat tmpRepeat =(GffDetailRepeat)gffHashRepeat.getLocHashtable().get(locString);
					tmpRepeatInfo[3]=tmpRepeatInfo[3]+"///"+tmpRepeat.locString;
					tmpRepeatInfo[4]=tmpRepeatInfo[4]+"///"+tmpRepeat.repeatClass+"/"+tmpRepeat.repeatFamily;
				}
				//////////////////////////////////////////
			
			}
			else 
			{
				tmpRepeatInfo[3]=tmpRepeatInfo[3]+"///"+"outofRepeat";
				tmpRepeatInfo[4]=tmpRepeatInfo[4]+"///"+"none";
			}
			tmpRepeatInfo[2]=tmpRepeatNum+"";
			lsresult.add(tmpRepeatInfo);
		}
		return lsresult;
	}
}
