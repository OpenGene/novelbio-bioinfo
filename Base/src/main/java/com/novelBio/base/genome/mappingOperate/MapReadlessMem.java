package com.novelBio.base.genome.mappingOperate;

import java.io.BufferedReader;
import java.util.LinkedList;

import com.novelBio.base.dataOperate.TxtReadandWrite;



/**
 * 消耗内存比较少的版本，编起来很累
 * @author zong0jie
 *
 */
public class MapReadlessMem {
	
	
	/**
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param chrLengthFile 里面保存每个chr有多长，告诉程序该生成多长的short数组
	 * @param colChrID ChrID在第几列，从0开始
	 * @param colStartNum mapping起点在第几列，从0开始
	 * @param colEndNum mapping终点在第几列，从0开始
	 * @param invNum 每隔多少位计数
	 * @throws Exception 
	 */
	public void  ReadMapFile(String mapFile,String chrLengthFile,String sep,int colChrID,int colStartNum,int colEndNum,int invNum) throws Exception 
	{
		//所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
		int m=100000000;
		short[] chrBpReads=new short[m];
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		//二位数组，第一维记录第几个bp，第二维记录该bp上有多少个reads叠加
		LinkedList<int[]> lstmpBpNum=new LinkedList<int[]>();
		
		//先假设mapping结果已经排序好，并且染色体已经分开好。
		while ((content=bufmap.readLine())!=null) 
		{
			String[] tmp=content.split(sep);
			if (colChrID>0) //当每列里面含有chrID的时候
			{
				//tmp[colChrID];
			}
			int tmpStart=Short.parseShort(tmp[colStartNum]);//本reads 的起点
			int tmpEnd=Short.parseShort(tmp[colEndNum]);//本reads的终点
			
			int lengthLsTmp= lstmpBpNum.size();//临时list的长度
			int lastlsBpNum=lstmpBpNum.get(lengthLsTmp-1)[0];//临时list中记录的最后一位bp的坐标
			
			//如果新的reads比lstmp的最后一位+invNum小，那么将reads叠加上去，同时看是否清算前面的bp
			int restNum=invNum-lastlsBpNum%invNum;//看最后一位距离结算bp还有几位
			if (restNum==invNum) //如果最后一位就可以结算了，则距离结算bp为0位
				restNum=0;
			
			if (tmpStart<=lastlsBpNum+restNum)//下一个reads起点在lstmp最后一个bp的结算范围内
			{
				int coverStartBpNum=lengthLsTmp-(lastlsBpNum-tmpStart)-1;//从ls的第几个bp上开始顺序加一，注意这里是从0开始计数。
				if(coverStartBpNum>lengthLsTmp-1)//下一个reads的起点超出了ls的范围
				{
					int tmpBpLoc=lastlsBpNum+1;//bp坐标，从最后一个+1开始计算
					while (tmpBpLoc<tmpStart) //将空缺的地方补上0
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=0;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}
					//上面的循环出来后，tmpBpLoc==tmpStart
					while (tmpBpLoc<=tmpEnd) 
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=1;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}
				}
				else//下一个reads的起点在ls的范围内
				{
					int tmpBpLoc=lstmpBpNum.get(coverStartBpNum)[0];//直接从这里开始
					while (tmpBpLoc<=lastlsBpNum&&tmpBpLoc<=tmpEnd) //当还在ls内部时
					{
						int[] tmpCod=lstmpBpNum.get(coverStartBpNum);
						tmpCod[1]++;
						tmpBpLoc++;
						coverStartBpNum++;
					}
					if (coverStartBpNum!=lengthLsTmp) {
						System.out.println("error");
					}
					while (tmpBpLoc<=tmpEnd) //当出了ls范围时
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=1;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}	
				}				
			}
			else //下一个reads起点超过了lstmp最后一个bp的结算范围
			{
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
			}
			
			
			
			
			
			for (int i = 0; i < lstmpBpNum.size(); i++) {
				
			}
			
			
			
			
			
			if (tmpStart%10==0) 
			{
				int shortNum=tmpStart/10;
				
				
				
			}
			
			
			
			
			
		}
		
		
		
		
		
		
	}
	
	
	
	
	
	

}
