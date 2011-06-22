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
		gffHashCG.ReadGffarray(gfffilename);
	}
	
	/**
	 * 给定二维数组,统计各个区域所占的具体bp数,这样子方便最后出结果
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * 返回坐标统计信息<br>
	 * 就是说peak所覆盖的每个类型CpG的具体bp数
	 * 中间出现某个repeat的话，相应CpG+1，然后NoCpG+1<br>
	 * 如果
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
				//如果左右两点都在一个CG内，则右点不计数并且左右之间也不同看了，肯定没有，就进入下一个循环
				//现在考虑两个端点不在一个CG内
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
		
			//看peak覆盖范围内有没有CG，有的话就加入计数
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
	 * 给定二维数组,获得每个peak与CpG的具体情况，针对UCSCknown gene
	 * 输入的数据，<br>
	 * 第一维是ChrID<br>
	 * 第二维是坐标,第三维也是坐标<br>
	 * 返回每个坐标在repeat上的具体情况<br>
	 * arraylist-string[5]<br>
	 * 0:ChrID<br>
	 * 1:坐标<br>
	 * 2:几个CpG
	 * 3:依次每个CpG的CG长度<br>
	 * 4:依次每个CpG的CG百分比
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
			double[] leftOverlap=(double[])objleftCodInfo[1];//左边的交集情况
			
			Object[] objrightCodInfo=(Object[]) tmpCGResult.get(1);
			GffCodInfo rightCodInfo=(GffCodInfo)objrightCodInfo[0];
			double[] rightOverlap=(double[])objrightCodInfo[1];//右边的交集情况

			int tmpCGNum=0;//总共几个repeat
			if(leftOverlap[0]>=overLapProp||leftOverlap[1]>=overLapProp)//基因内)//基因内
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

			//看peak覆盖范围内有没有repeat，有的话就加入计数
			for (int j = 2; j < tmpCGResult.size(); j++) {
				tmpCGNum++;
				GffDetailCG tmpDetailCG=(GffDetailCG)tmpCGResult.get(j);
				tmpCGInfo[3]=tmpCGInfo[3]+"///"+tmpDetailCG.lengthCpG;
				tmpCGInfo[4]=tmpCGInfo[4]+"///"+tmpDetailCG.perCpG;
			}
			
			///////////////右边的repeat位点///////////////////////
			if(rightOverlap[0]>=overLapProp||rightOverlap[1]>=overLapProp)
			{
				String locString=rightCodInfo.LOCID[0];
				//////////////////////////////////////////////////
				//如果左右两点都在一个repeat内，则右点不计数并且左右之间也不同看了，肯定没有，就进入下一个循环
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
