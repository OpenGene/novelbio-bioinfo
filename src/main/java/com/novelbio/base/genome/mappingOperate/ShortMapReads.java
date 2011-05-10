package com.novelbio.base.genome.mappingOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * 不考虑内存限制的编
 * @author zong0jie
 *
 */
public class ShortMapReads {
	
	/**
	 * 用来保存每个染色体中的基因坐标-invNum精度里面的reads数目
	 * chrID(小写)--short[]
	 * 其中short[]从1开始，0记录了short的长度，但是会溢出所以不准。
	 *  
	 */
	static Hashtable<String, short[]> hashChrBpReads=new Hashtable<String, short[]>();
	
	/**
	 * 用来保存每个染色体对应的chr长度
	 */
	static Hashtable<String, Integer> hashChrLength=new Hashtable<String, Integer>();
	/**
	 * 用来保存mapping文件中出现过的每个chr 的长度
	 */
	static ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	static int invNum=0;
	static int tagLength=400;//由ReadMapFile方法赋值
	public static void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * 设定双端readsTag拼起来后长度的估算值，目前solexa双端送样长度大概是400bp，不用太精确
	 * 这个是方法：getReadsDensity来算reads密度的东西
	 * @param readsTagLength
	 */
	public void setReadsTagLength(int readsTagLength)
	{
		this.tagLength=readsTagLength;
	}
	
	
	/**
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param chrLengthFile 里面保存每个chr有多长，告诉程序该生成多长的short数组，文件格式为 chrID(小写)+"\t"+chrLength(long)+换行
	 * @param colChrID ChrID在第几列，从1开始
	 * @param colStartNum mapping起点在第几列，从1开始
	 * @param colEndNum mapping终点在第几列，从1开始
	 * @param invNum 每隔多少位计数
	 * @throws Exception
	 */
	public static void  ReadMapFile(String mapFile,String chrLengthFile,String sep,int colChrID,int colStartNum,int colEndNum,int thisinvNum) throws Exception 
	{
		//所谓结算就是说每隔invNum的bp就把这invNumbp内每个bp的Reads叠加数取平均或中位数，保存进chrBpReads中
		colChrID--;colStartNum--;colEndNum--;
		invNum=thisinvNum;
		
		/////////////////////////////////////////获得每条染色体的长度并保存在hashChrLength中////////////////////////////////////////////////////
		TxtReadandWrite txtChrLength=new TxtReadandWrite();
		txtChrLength.setParameter(chrLengthFile,false, true);
		String[][] chrLengthInfo=txtChrLength.ExcelRead("\t", 1, 1, txtChrLength.ExcelRows(), 2);
		for (int i = 0; i < chrLengthInfo.length; i++) {
			hashChrLength.put(chrLengthInfo[i][0], Integer.parseInt(chrLengthInfo[i][1]));
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		short[] chrBpReads=null;//保存每个bp的reads累计数
		short[] SumChrBpReads=null;//直接从0开始记录，1代表第二个invNum,也和实际相同
		
		/////////////////读文件的准备工作///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile, false,true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";
		////////////////////////////////////////////////////////////////////////////////////////////////
		//先假设mapping结果已经排序好，并且染色体已经分开好。
		while ((content=bufmap.readLine())!=null) 
		{
			String[] tmp=content.split(sep);
			
			///////////////////当每列里面含有chrID的时候///////////////////////////////////
			if (colChrID>=0) 
			{
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //出现了新的chrID，则开始剪切老的chrBpReads,然后新建chrBpReads，最后装入哈希表
				{
					if (!lastChr.equals("")) //前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
					}
					
					lastChr=tmp[colChrID].trim().toLowerCase();//实际这是新出现的ChrID
				
					
					
					//////////////////释放内存，感觉加上这段有点用，本来内存到1.2g，加了后降到990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads=null;//看看能不能释放掉内存
					System.gc();//显式调用gc
					/////////chrBpReads设定/////////////////////////
					int chrLength;
					chrLength=hashChrLength.get(lastChr);
					chrBpReads=new short[chrLength+1];//同样为方便，0位记录总长度。这样实际bp就是实际长度
					chrBpReads[0]=(short) chrLength;//会溢出所以不能看
					////////////SumChrBpReads设定//////////////////////////////////
					//这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,
					//为方便，0位记录总长度。这样实际bp就是实际长度
					int SumLength=chrBpReads.length/invNum+1;//保证不会溢出，这里是要让SumChrBpReads长一点
					SumChrBpReads=new short[SumLength];//直接从0开始记录，1代表第二个invNum,也和实际相同
					
					
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
						chrBpReads=null;//看看能不能释放掉内存
						System.gc();//显式调用gc
						/////////chrBpReads设定/////////////////////////
						
					 /////////////chrBpReads设定////////////////////////////////////////////////////////////////////////////////////
					 int chrLength=hashChrLength.get(lastChr);
					 chrBpReads=new short[chrLength+1];//同样为方便，0位记录总长度。这样实际bp就是实际长度
					 chrBpReads[0]=(short) chrLength;//会溢出所以不能看
					 ////////////SumChrBpReads设定//////////////////////////////////
					 //这个不是很精确，最后一位可能不准，但是实际应用中无所谓了,
					 //为方便，0位记录总长度。这样实际bp就是实际长度
					 int SumLength=chrBpReads.length/invNum+1;//保证不会溢出，这里是要让SumChrBpReads长一点
					 SumChrBpReads=new short[SumLength];//直接从0开始记录，1代表第二个invNum,也和实际相同
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
			int tmpStart=Integer.parseInt(tmp[colStartNum]);//本reads 的起点
			int tmpEnd=Integer.parseInt(tmp[colEndNum]);//本reads的终点
			for (int i = tmpStart; i <= tmpEnd; i++) {//直接计算实际起点和实际终点
				chrBpReads[i]++;
				if (chrBpReads[i]<0) 
				{
					System.out.println("单碱基溢出");
				}
			}
		}
		///////////////////循环结束后还要将最后一次的内容做总结////////////////////////////////////
		 sumChrBp(chrBpReads, 1, SumChrBpReads);
		 //把lsChrLength按照chrLen从小到大进行排序
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
		 
		 
	}
	
	/**
	 * 给定chrBpReads，将chrBpReads里面的值按照invNum区间放到SumChrBpReads里面
	 * 因为是引用传递，里面修改了SumChrBpReads后，外面会变掉
	 * @param chrBpReads 每个碱基的reads累计值
	 * @param invNum 区间
	 * @param type 取值类型，中位数或平均值，0中位数，1均值 其他的默认中位数
	 * @param SumChrBpReads 将每个区间内的
	 */
	private static void sumChrBp(short[] chrBpReads,int type,short[] SumChrBpReads) 
	{
		 int SumLength=chrBpReads.length/invNum-1;//保证不会溢出，因为java默认除数直接忽略小数而不是四舍五入
		 for (int i = 0; i < SumLength; i++) 
		 {
			 short[] tmpSumReads=new short[invNum];//将总的chrBpReads里的每一段提取出来
			 int sumStart=i*invNum+1;int k=0;//k是里面tmpSumReads的下标，实际下标就行，不用-1
			 for (int j =sumStart; j <sumStart+invNum; j++) 
			 {
				 tmpSumReads[k]=chrBpReads[j];
				 k++;
			 }
			 if (type==0) //每隔一段区域取样，建议每隔10bp取样，取中位数
				 SumChrBpReads[i]=(short) median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(short) mean(tmpSumReads);
			 else //默认取中位数
				 SumChrBpReads[i]=(short) median(tmpSumReads);
		 }
	}
	
	
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	private static int mean(short[] unsortNum)
	{
		int length=unsortNum.length;
		int sum=0;
		for(int i=1;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		int avg=sum/length;
		if(avg>32767)
			System.out.println("avg总结时溢出");
		return avg;
	}
	
	
	
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	private static int median(short[] unsortNum)
	{
		int med=-100;
		int tmp=-10000;
		int length=unsortNum.length;
		for(int i=1;i<length;i++)
		{
			tmp=unsortNum[i];
			int j=i;
			for(;j>0;j--)
			{
				if(tmp<unsortNum[j-1])
				{
					unsortNum[j]=unsortNum[j-1];
				}
				else break;
			}
			unsortNum[j]=(short) tmp;
		}
		if (length%2==0) 
			med=(unsortNum[length/2-1]+unsortNum[length/2])/2;
		else 
			med=unsortNum[length/2];
		if(med>32767)
			System.out.println("med总结时溢出");
		return med;
	}
	
	/**
	 * 输入坐标区间，和每个区间的bp数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param thisInvNum 每个区域内所含的bp数，大于等于invNum，最好是invNum的倍数
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点
	 * @param endNum 终点坐标，为实际终点
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public static double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{
		int binNum=(endNum-startNum)/thisInvNum;
		return getRengeInfo( chrID, startNum, endNum, binNum,type);
	}
	
	
	/**
	 * 输入坐标区间，需要划分的块数，返回该段区域内reads的数组
	 * 定位到两个端点所在的 读取invNum区间，然后计算新的invNum区间
	 * @param chrID 一定要小写
	 * @param startNum 起点坐标，为实际起点
	 * @param endNum 终点坐标，为实际终点
	 * @param binNum 待分割的区域数目
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public static double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		short[] invNumReads=hashChrBpReads.get(chrID.toLowerCase());
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
		short[] tmpRegReads=new short[rightNum-leftNum+1];
		int k=0;
		for (int i = leftNum; i <= rightNum; i++) {
			tmpRegReads[k]=invNumReads[i];
			k++;
		}
		return mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
	}
	
	
	
	
	/**
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定获得加权平均，最后获得指定分割数量的数组
	 * 譬如现在有int[20]的一组数，我想要把这组数缩小到int[10]里面并且保持其比例大体吻合，这时候我采用加权平均的方法
	 * 检查了一遍，感觉可以
	 * 用于将500或更多份的基因中tag累计数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param binNum 后面要生成的分割的块数
	 * @param startBias 从起点的多少开始
	 * @param endBias 到终点的多少结束
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	private static double[] mySpline(short[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //将每一个分隔的长度标准化为一个比值，基准为invNum为1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//某区域内treatNum最靠左边的一个值(包含边界)的下标+1，因为数组都是从0开始的
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//最左边值的权重
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////某区域内treatNum最右边的一个值(不包含边界)的下标+1，因为数组都是从0开始的
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//最右边值的权重
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////如果左右端点都在一个区域内，那么加权平均，最大值，加和都等于该区域的值/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////看是否会错，可删//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//中间有几个值
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////根据条件选择加权平均或最大值或加和////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //最大值
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //默认加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	
	
	
	/**
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定获得加权平均，最后获得指定分割数量的数组
	 * 譬如现在有int[20]的一组数，我想要把这组数缩小到int[10]里面并且保持其比例大体吻合，这时候我采用加权平均的方法
	 * 检查了一遍，感觉可以
	 * 用于将500或更多份的基因中tag累计数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param binNum 后面要生成的分割的块数
	 * @param startBias 从起点的多少开始
	 * @param endBias 到终点的多少结束
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	private static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //将每一个分隔的长度标准化为一个比值，基准为invNum为1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//某区域内treatNum最靠左边的一个值(包含边界)的下标+1，因为数组都是从0开始的
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//最左边值的权重
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////某区域内treatNum最右边的一个值(不包含边界)的下标+1，因为数组都是从0开始的
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//最右边值的权重
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////如果左右端点都在一个区域内，那么加权平均，最大值，加和都等于该区域的值/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////看是否会错，可删//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//中间有几个值
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////根据条件选择加权平均或最大值或加和////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //最大值
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //默认加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	
	
	
	
	
	/**
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布
	 * @param chrID 
	 * @param startLoc 起点坐标，为实际起点
	 * @param endLoc 当终点为-1时，则直到染色体的结尾。
	 * @param binNum 待分割的块数
	 * @return
	 */
	public static double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		if (startLoc==0) 
			startLoc=1;
		if(endLoc==-1)
			endLoc=hashChrLength.get(chrID.trim().toLowerCase());
		
		
		Hashtable<String, short[]>aaaHashtable=hashChrBpReads;
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		double[] resultTagDensityNum=mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	
	
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID
	 * @return
	 */
	public static int getChrLength(String chrID) 
	{
		return hashChrLength.get(chrID);
	}
	
	/**
	 * 在读取chr长度文件后，可以通过此获得最长和最短chr的长度
	 * @param chrID
	 * @return int[]
	 * 0: 最短chr长度
	 * 1: 最长chr长度
	 */
	public static int[] getThreshodChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	
	
	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * @param chrID
	 * @return ArrayList<String[]>
	 * 0: chrID
	 * 1: chr长度
	 */
	public static ArrayList<String[]> getChrLengthInfo()
	{

		return lsChrLength;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
