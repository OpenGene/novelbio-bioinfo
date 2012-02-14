package com.novelbio.analysis.seq.genome.mappingOperate;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.seq.genome.getChrSequence.ChrSearch;
import com.novelbio.analysis.seq.genome.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;


/**
 * 不考虑内存限制的编
 * @author zong0jie
 *
 */
public class MapReads {
	
	/**
	 * 用来保存每个染色体中的基因坐标-invNum精度里面的reads数目
	 * chrID(小写)--short[]
	 * 其中short[]从1开始，0记录了short的长度，但是会溢出所以不准。
	 */
	 Hashtable<String, int[]> hashChrBpReads=new Hashtable<String, int[]>();
	
	/**
	 * 用来保存mapping文件中出现过的每个chr 的长度
	 */
	 ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	 int invNum=10;
	 int tagLength=400;//由ReadMapFile方法赋值
	/**
	 * 设定双端readsTag拼起来后长度的估算值，目前solexa双端送样长度大概是400bp，不用太精确
	 * 这个是方法：getReadsDensity来算reads密度的东西
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	
	HashMap<String, Integer> hashChrReadsNum = new HashMap<String, Integer>();
	
	public double calBG()
	{
		return MathComput.mean(hashChrBpReads.values());
	}
	public ArrayList<String[]> calBGperChrID()
	{
		ArrayList<String[]> lsInfo = new ArrayList<String[]>();
		for (Entry<String, int[]> entry : hashChrBpReads.entrySet()) {
			String[] tmp = new String[2];
			tmp[0] = entry.getKey();
			tmp[1] = MathComput.mean(entry.getValue()) + "";
			lsInfo.add(tmp);
		}
		return lsInfo;
	}
	
	public int getReadsNum() {
		return ReadsNum;
	}
	public HashMap<String, Integer> getHashChrReadsNum() {
		return hashChrReadsNum;
	}
	/**
	 * 将一些peak区域重新设定值，实际上是将peak的值删除然后获得background的信息
	 * @param lsRange
	 */
	public void setNum(ArrayList<MapInfo> lsRange, int value)
	{
		int tmp = 0;
		for (MapInfo mapInfo : lsRange) {
			int[] chrNumInfo = hashChrBpReads.get(mapInfo.getChrID().toLowerCase());
			if (chrNumInfo == null) {
				System.out.println("没有该染色体：" + mapInfo.getChrID());
				continue;
			}
			for (int i = mapInfo.getStart()/invNum; i < mapInfo.getEnd()/invNum; i++) {
				tmp = chrNumInfo[i];
				chrNumInfo[i] = value;
			}
		}
	}
	
	int ReadsNum = 0;
	
	/**
	 * 当输入为macs的bed文件时，自动跳过chrm项目
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * @param colChrID ChrID在第几列，从1开始,如果chrID<=0，则将采用王丛茂的格式读取
	 * @param colStartNum mapping起点在第几列，从1开始
	 * @param colEndNum mapping终点在第几列，从1开始
	 * @param thisinvNum 每隔多少位计数
	 * @return ReadsNum 总共多少reads，用于标准化计算
	 * @throws Exception
	 */
	public  long  ReadMapFile(String mapFile,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int thisinvNum) throws Exception 
	{
		int ReadsNumchr = 0;
		 ReadsNum = 0;
		//所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
		colChrID--;colStartNum--;colEndNum--;
		invNum=thisinvNum;
		/////////////////////////////////////////获得每条染色体的长度并保存在hashChrLength中////////////////////////////////////////////////////
		ChrSearch.setChrFilePath(chrFilePath);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		int[] chrBpReads=null;//保存每个bp的reads累计数
		int[] SumChrBpReads=null;//直接从0开始记录，1代表第二个invNum,也和实际相同
		/////////////////读文件的准备工作///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";
		int tmpStartOld = 0 ;
		int tmpEndOld = 0;
		int tmpStart = 0;
		int tmpEnd = 0;
		////////////////////////////////////////////////////////////////////////////////////////////////
		//先假设mapping结果已经排序好，并且染色体已经分开好。
		while ((content=bufmap.readLine())!=null) 
		{
			String[] tmp=content.split(sep);
			///////////////////当每列里面含有chrID的时候///////////////////////////////////
			if (colChrID>=0) 
			{
				if (tmp[colChrID].trim().toLowerCase().equals("chrm")) {
					continue;
				}
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //出现了新的chrID，则开始剪切老的chrBpReads,然后新建chrBpReads，最后装入哈希表
				{
					if (!lastChr.equals("")) //前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
						hashChrReadsNum.put(lastChr, ReadsNumchr);
						ReadsNumchr = 0;
					}
					lastChr=tmp[colChrID].trim().toLowerCase();//实际这是新出现的ChrID
					//////////////////释放内存，感觉加上这段有点用，本来内存到1.2g，加了后降到990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads=null;//看看能不能释放掉内存
					System.gc();//显式调用gc
					/////////chrBpReads设定/////////////////////////
					int chrLength=(int) ChrSearch.getChrLength(lastChr);
					chrBpReads=new int[chrLength+1];//同样为方便，0位记录总长度。这样实际bp就是实际长度
					chrBpReads[0]=(int) chrLength;//会溢出所以不能看
					////////////SumChrBpReads设定//////////////////////////////////
					//这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,
					//为方便，0位记录总长度。这样实际bp就是实际长度
					int SumLength=chrBpReads.length/invNum+1;//保证不会溢出，这里是要让SumChrBpReads长一点
					SumChrBpReads=new int[SumLength];//直接从0开始记录，1代表第二个invNum,也和实际相同
					 ////////////将新出现的chr装入哈希表////////////////////////////////
					hashChrBpReads.put(lastChr, SumChrBpReads);//将新出现的chrID和新建的SumChrBpReads装入hash表
					
					/////////////将每一条序列长度装入lsChrLength///////////////////
					String[] tmpChrLen=new String[2];
					tmpChrLen[0]=lastChr;tmpChrLen[1]=chrLength+"";
					lsChrLength.add(tmpChrLen);
				}
			}
			//////////////////////////假设为fasta格式，每个染色体一个>chrID接下来才是mapping坐标///////////////////////////////////
			else 
			{
				if(content.startsWith(">"))
				{
					if (lastChr != null && lastChr != "") {
						hashChrReadsNum.put(lastChr, ReadsNumchr);
						ReadsNumchr = 0;
					}
					 Pattern pattern =Pattern.compile("chr\\w+", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
					 Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
					 matcher = pattern.matcher(content);     
					 if (matcher.find()) 
						 lastChr=matcher.group().toLowerCase();//小写
					 else 
						System.out.println("error");
					
					 if (!hashChrBpReads.isEmpty()) //说明里面已经有东西了，那么现在开始总结
					 {
						 sumChrBp(chrBpReads, 1, SumChrBpReads);
					 }

					//////////////////释放内存，感觉加上这段有点用，本来内存到1.2g，加了后降到990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads = null;// 看看能不能释放掉内存
					System.gc();// 显式调用gc
					/////////chrBpReads设定/////////////////////////

					/////////////chrBpReads设定////////////////////////////////////////////////////////////////////////////////////

					 int chrLength=(int) ChrSearch.getChrLength(lastChr);
					 chrBpReads=new int[chrLength+1];//同样为方便，0位记录总长度。这样实际bp就是实际长度
					 chrBpReads[0]=(int) chrLength;//会溢出所以不能看
					 ////////////SumChrBpReads设定//////////////////////////////////
					 //这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,
					 //为方便，0位记录总长度。这样实际bp就是实际长度
					 int SumLength=chrBpReads.length/invNum+1;//保证不会溢出，这里是要让SumChrBpReads长一点
					 SumChrBpReads=new int[SumLength];//直接从0开始记录，1代表第二个invNum,也和实际相同
					 ////////////将新出现的chr装入哈希表////////////////////////////////
					 hashChrBpReads.put(lastChr, SumChrBpReads);//将新出现的chrID和新建的SumChrBpReads装入hash表
					 /////////////将每一条序列长度装入lsChrLength///////////////////
					 String[] tmpChrLen=new String[2];
					 tmpChrLen[0]=lastChr;tmpChrLen[1]=chrLength+"";
					 lsChrLength.add(tmpChrLen);
					 continue;
				}
			}
			////////////////////按照位点加和chrBpReads////////////////////////////////
			
			tmpStart=Integer.parseInt(tmp[colStartNum]);//本reads 的起点
			tmpEnd=Integer.parseInt(tmp[colEndNum]);//本reads的终点
			//如果本reads和上一个reads相同，则认为是线性扩增，跳过
			if (tmpStart == tmpStartOld && tmpEnd == tmpEndOld) {
				continue;
			}
			for (int i = tmpStart; i <= tmpEnd; i++) {//直接计算实际起点和实际终点
				//如果bed文件中的坐标大于ref基因的坐标，那么就跳出
				if (i >= chrBpReads.length) {
					break;
				}
				else if (i < 0) {
					continue;
				}
				chrBpReads[i]++;
				if (chrBpReads[i]<0) 
				{
					System.out.println("单碱基溢出");
				}
			}
			tmpStartOld = tmpStart;
			tmpEndOld = tmpEnd;
			ReadsNum++;
			ReadsNumchr++;
		}
		
		///////////////////循环结束后还要将最后一次的内容做总结////////////////////////////////////
		 sumChrBp(chrBpReads, 1, SumChrBpReads);
		 hashChrReadsNum.put(lastChr, ReadsNumchr);
		 ////////////////////////////把lsChrLength按照chrLen从小到大进行排序/////////////////////////////////////////////////////////////////////////////
		  Collections.sort(lsChrLength,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1)
	            {
	               if( Integer.parseInt(arg0[1])<Integer.parseInt(arg1[1]))
	            	   return -1;
	            else if (Integer.parseInt(arg0[1])==Integer.parseInt(arg1[1])) 
					return 0;
	             else 
					return 1;
	            }
	        });
		 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		  
		 return ReadsNum;
	}
	
	/**
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	private  void sumChrBp(int[] chrBpReads,int type,int[] SumChrBpReads) 
	{
		 int SumLength=chrBpReads.length/invNum-1;//保证不会溢出，因为java默认除数直接忽略小数而不是四舍五入
		 for (int i = 0; i < SumLength; i++) 
		 {
			 int[] tmpSumReads=new int[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart = i*invNum + 1;int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j =sumStart; j <sumStart+invNum; j++) 
			 {
				 tmpSumReads[k]=chrBpReads[j];
				 k++;
			 }
			 if (type==0) //每隔一段区域取样，建议每隔10bp取样，取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //默认取中位数
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
	}
	/**
	 * 用本mapReads减去另一个mapReads中的信号
	 * 主要用来比较BG
	 * @param mapReads
	 */
	public void minusMapReads(MapReads mapReads)
	{
		
	}

	
	/**
	 * 输入坐标区间，和每个区间的bp数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间，如果该染色体在mapping时候不存在，则返回null
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点
	 * @param endNum 终点坐标，为实际终点
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public  double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{
		int binNum=(endNum-startNum)/thisInvNum;
		return getRengeInfo( chrID, startNum, endNum, binNum,type);
	}
	
	
	/**
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组。如果该染色体在mapping时候不存在，则返回null
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点
	 * @param endNum 终点坐标，为实际终点
	 * @param binNum 待分割的区域数目
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public  double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		int[] invNumReads=hashChrBpReads.get(chrID.toLowerCase());
		if (invNumReads==null) 
		{
			return null;
		}
		startNum--;endNum--;
		////////////////确定要提取区域的左端点和右端点/////////////////////////////////
		int leftNum=0;//在invNumReads中的实际起点
		int rightNum=0;//在invNumReads中的实际终点

		leftNum=startNum/invNum;
		double leftBias=(double)startNum/invNum-leftNum;//最左边分隔到起点的距离比值
		double rightBias=0;
		if (endNum%invNum==0) 
			rightNum=endNum/invNum-1;//前提是java小数转成int通通直接去掉小数点
		else 
		{
			rightNum=endNum/invNum;//前提是java小数转成int通通直接去掉小数点
			rightBias=(double)endNum/invNum-rightNum;//最右边分隔到终点的距离比值
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		int[] tmpRegReads=new int[rightNum-leftNum+1];
		int k=0;
		try {
			for (int i = leftNum; i <= rightNum; i++) {
				tmpRegReads[k]=invNumReads[i];
				k++;
		}
		} catch (Exception e) {
		  System.out.println("error");
		}
	
		return MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
	}
	
	
	
	
	
	
	
	
	/**
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布，如果该染色体在mapping时候不存在，则返回null
	 * @param chrID 
	 * @param startLoc 起点坐标，为实际起点
	 * @param endLoc 当终点为-1时，则直到染色体的结尾。
	 * @param binNum 待分割的块数
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum)
	{
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		if (startLoc==0) 
			startLoc=1;
		if(endLoc==-1)
			endLoc=(int) ChrSearch.getChrLength(chrID);
		Hashtable<String, int[]>aaaHashtable=hashChrBpReads;
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		for (int i = 0; i < resultTagDensityNum.length; i++) {
			resultTagDensityNum[i] =  resultTagDensityNum[i] * 5000000/this.ReadsNum;
		}
		return resultTagDensityNum;
	}
	
	
	
	
	/**
	 * 获得Mapping文件中最长和最短chr的长度
	 * @param chrID
	 * @return int[]
	 * 0: 最短chr长度
	 * 1: 最长chr长度
	 */
	public  int[] getLimChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
