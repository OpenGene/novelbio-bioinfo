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
	 * 覆盖度百分比，统计repeat数目时用到，当大于该百分比时将repeat计入
	 */
	int overLapProp=50;
	public void setOverlapProp(int overLapProp) {
		this.overLapProp=overLapProp;
	}
	
	/**
	 * 读取Repeat的坐标文件
	 * @param gfffilename
	 * @throws Exception
	 */
	public void prepare(String gfffilename) throws Exception 
	{
		gffHashRepeat.ReadGffarray(gfffilename);
	}
	
	
	/**
	 * 给定二维数组,统计各个区域所占的比重，针对UCSCknownGeneRepeatMasker
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 返回坐标统计信息<br>
	 * arraylist-string[2]<br>
	 * 0:repeatClass<br>
	 * 1:Num<br>
	 */
	public ArrayList<String[]> locateCod(String[][] LOCIDInfo)
	{
		String outofRepeat="outofRepeat";
		//统计结果加入hashStatistic，key repeat类型，value repeat数目
		HashMap<String, Integer> hashStatistic=new HashMap<String, Integer>();
		hashStatistic.put(outofRepeat, 0);
		for (int i = 0; i < LOCIDInfo.length; i++) {
			GffCodInfo tmpresult=(GffCodInfo)gffSearchRepeat.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHashRepeat);
			if(tmpresult.insideLOC)//基因内
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
		
		//将哈希表遍历并装入list
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
	 * 给定二维数组,统计各个区域所占的比重，针对UCSCknownGeneRepeatMasker
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标<br>
	 * 返回每个坐标在repeat上的具体情况<br>
	 * arraylist-string[4]<br>
	 * 0:ChrID<br>
	 * 1:坐标<br>
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
			if(tmpCodInfo.insideLOC)//基因内
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
	 * 给定二维数组,统计各个区域所占的比重，针对UCSCknown gene,
	 * @param LOCIDInfo
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * @param Bp
	 * true:返回peak所覆盖的每个repeat的bp数<br>
	 * false:当peak与repeat的交集>50%(两者中比较小的那个的50%)时，该类repeat数目+1<br>
	 * 如果左端点/右端点在repeat内并且左部分的repeat/peak所占比例超过overLapProp，则加1，否则outofRepeat+1<br>
	 * 中间出现某个repeat的话，相应repeat+1，然后outofRepeat+1<br>
	 * 返回坐标统计信息<br>
	 * 如果
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
	 * 给定二维数组,统计各个区域所占的比重，针对UCSCknown gene,只有当peak和region的交集部分大于设定的overLapProp时，才算一个
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * 返回坐标统计信息<br>
	 * 如果左端点/右端点在repeat内并且左部分的repeat/peak所占比例超过overLapProp，则加1，否则outofRepeat+1<br>
	 * 中间出现某个repeat的话，相应repeat+1，然后outofRepeat+1<br>
	 * 如果
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
			double[] leftOverlap=(double[])objleftCodInfo[1];//左边的交集情况
			
			
			Object[] objrightCodInfo=(Object[]) tmpresult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//右边的交集情况
			
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//基因内
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
				//如果左右两点都在一个repeat内，则右点不计数并且左右之间也不同看了，肯定没有，就进入下一个循环
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
			//看peak覆盖范围内有没有repeat，有的话就加入计数
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
		//将哈希表遍历并装入list
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
	 * 给定二维数组,统计各个区域所占的具体bp数,这样子方便最后出结果
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * 返回坐标统计信息<br>
	 * 就是说peak所覆盖的每个类型repeat的具体bp数
	 * 中间出现某个repeat的话，相应repeat+1，然后outofRepeat+1<br>
	 * 如果
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
			long peakLen=Math.abs(Long.parseLong(LOCIDInfo[i][1])-Long.parseLong(LOCIDInfo[i][2]));//peak的长度
			Object[] objleftCodInfo=(Object[]) tmpresult.get(0);
			GffCodInfo leftCodInfo=(GffCodInfo)objleftCodInfo[0];
			double[] leftOverlap=(double[])objleftCodInfo[1];//左边的交集情况
			
			
			Object[] objrightCodInfo=(Object[]) tmpresult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//右边的交集情况
			long tmpOverlap=0;
			if(leftCodInfo.insideLOC)//基因内
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
				//如果左右两点都在一个repeat内，则右点不计数并且左右之间也不同看了，肯定没有，就进入下一个循环
				//现在考虑两个端点不在一个repeat内
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
		
			//看peak覆盖范围内有没有repeat，有的话就加入计数
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
				//当两个repeat重叠时就会出现负数，这种情况是存在的
				System.out.println("error tmpOutOfRepeat<0,set it to 0");
				tmpOutOfRepeat=0;
			}
			long tmp=hashStatistic.get(outofRepeat);
			tmp=tmp+tmpOutOfRepeat;
			hashStatistic.put(outofRepeat, tmp);
		}
		//将哈希表遍历并装入list
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
	 * 给定二维数组,获得每个peak与repeat的具体情况，针对UCSCknown gene
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * 返回每个坐标在repeat上的具体情况<br>
	 * arraylist-string[5]<br>
	 * 0:ChrID<br>
	 * 1:坐标<br>
	 * 2:几个repeat
	 * 3:依次每个repeat的Name<br>
	 * 4:依次每个repeat-Class-family
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
			double[] leftOverlap=(double[])objleftCodInfo[1];//左边的交集情况
			
			Object[] objrightCodInfo=(Object[]) tmpRepeatResult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//右边的交集情况

			int tmpRepeatNum=0;//总共几个repeat
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//基因内)//基因内
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

			//看peak覆盖范围内有没有repeat，有的话就加入计数
			for (int j = 2; j < tmpRepeatResult.size(); j++) {
				tmpRepeatNum++;
				GffDetailRepeat tmpDetailRepeat=(GffDetailRepeat)tmpRepeatResult.get(j);
				tmpRepeatInfo[3]=tmpRepeatInfo[3]+"///"+tmpDetailRepeat.locString;
				tmpRepeatInfo[4]=tmpRepeatInfo[4]+"///"+tmpDetailRepeat.repeatClass+"/"+tmpDetailRepeat.repeatFamily;
			}
			
			///////////////右边的repeat位点///////////////////////
			if(rightOverlap[0]>=overLapProp||rightOverlap[1]>=overLapProp)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//如果左右两点都在一个repeat内，则右点不计数并且左右之间也不同看了，肯定没有，就进入下一个循环
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
