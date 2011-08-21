package com.novelbio.base.dataStructure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import com.novelbio.base.dataOperate.TxtReadandWrite;


public class MathComput {
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static int mean(int[] unsortNum)
	{
		int length=unsortNum.length;
		int sum=0;
		for(int i=1;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		int avg=sum/length;
		return avg;
	}
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double mean(double[] unsortNum)
	{
		double length=unsortNum.length;
		double sum=0;
		for(int i=1;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		double avg=sum/length;
		return avg;
	}
	/**
	 * 输入数据，获得和
	 * @param Num
	 * @return
	 */
	public static	double sum(double[] Num) {
		double sum = 0;
		for (double d : Num) {
			sum = sum + d;
		}
		return sum;
	}
	/**
	 * 输入数据，获得和
	 * @param Num
	 * @return
	 */
	public static	int sum(int[] Num) {
		int sum = 0;
		for (int d : Num) {
			sum = sum + d;
		}
		return sum;
	}
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static int median(int[] unsortNum)
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
			unsortNum[j]= tmp;
		}
		if (length%2==0) 
			med=(unsortNum[length/2-1]+unsortNum[length/2])/2;
		else 
			med=unsortNum[length/2];
		return med;
	}
	
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @return
	 */
	public static double median(double[] unsortNum)
	{
		double med=-100;
		double tmp=-10000;
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
			unsortNum[j]= tmp;
		}
		if (length%2==0) 
			med=(unsortNum[length/2-1]+unsortNum[length/2])/2;
		else 
			med=unsortNum[length/2];
		return med;
	}
	
	/**
	 * 输入数据，获得最接近中位数的那个数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @return
	 */
	public static double medianLike(double[] unsortNum)
	{
		double med=-100;
		double tmp=-10000;
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
			unsortNum[j]= tmp;
		}
		if (length%2==0){
			med=(unsortNum[length/2-1]+unsortNum[length/2])/2;
			if (Math.abs(unsortNum[length/2-1] - med) <= Math.abs(unsortNum[length/2] - med)) {
				return unsortNum[length/2-1];
			}
			else {
				return unsortNum[length/2];
			}
			
		}
		else 
			return unsortNum[length/2];
	}
	
	
	/**
	 * 输入数据进行排序，
	 * @return
	 */
	public static void sort(int[] unsortNum,boolean smallToBig)
	{
		int tmp=-10000;
		int length=unsortNum.length;
		if (smallToBig) {
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
				unsortNum[j]= tmp;
			}
		}
		else {
			for(int i=1;i<length;i++)
			{
				tmp=unsortNum[i];
				int j=i;
				for(;j>0;j--)
				{
					if(tmp>unsortNum[j-1])
					{
						unsortNum[j]=unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j]= tmp;
			}
		}
	
	}
	
	
	
	
	/**
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定获得加权平均，最后获得指定分割数量的数组
	 * 譬如现在有int[20]的一组数，我想要把这组数缩小到int[10]里面并且保持其比例大体吻合，这时候我采用加权平均的方法
	 * 检查了一遍，感觉可以
	 * 用于将500或更多份的数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param binNum 后面要生成的分割的块数
	 * @param startBias 从起点的多少开始 为 0,1之间的小数，表示从第一个值的几分之几开始
	 * @param endBias 到终点的多少结束  为 0,1之间的小数，表示到 (结束位点到终点的距离/每个单元的长度)
	 * 0-*--|---1---------2--------3----------4----------5---------6----|--*-7
	 * 星号标记的地方
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public static double[] mySpline(int[] treatNum, int binNum,double startBias,double endBias,int type)
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
	 * @param type 0：加权平均 1：取最高值，2：加和
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type)
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
	 * 将 aArray与bArray相加，最后结果保存在aArray中
	 * 如果bArray==null，则直接返回aArray，但是会system.out.println报错
	 * @param aArray
	 * @param bArray
	 * @return
	 */
	public static double[] addArray(double[] aArray,double[] bArray) 
	{
		if(bArray==null)
		{
			System.out.println("addArray Error: bArray==null");
			return aArray;
		}
		
		for (int i = 0; i < aArray.length; i++) {
			aArray[i]=aArray[i]+bArray[i];
		}
		return aArray;
	}
	
	
	/**
	 * 将 aArray与bArray整合在一起并计算a和b每个部分分别的比例。aArray和bArray的长度必须一致
	 * @param aArray 实验组数据
	 * @param bArray 对照组数据，也就是背景
	 * @return
	 */
	public static String[][] batStatistic(long[] aArray,long[] bArray,String[] item,String aName,String bName) 
	{
		String[][] result=new String[aArray.length+1][5];
		long sumaArray=0;long sumbArray=0;
		for (int i = 0; i < aArray.length; i++) {
			sumaArray=sumaArray+aArray[i];
		}
		for (int i = 0; i < bArray.length; i++) {
			sumbArray=sumbArray+bArray[i];
		}
		result[0][0]="item";
		result[0][1]=aName;
		result[0][2]=aName+" proportion";
		result[0][3]=bName;
		result[0][4]=bName+" proportion";
		
		
		for (int i = 1; i < result.length; i++) {
			result[i][0]=item[i-1];
			result[i][1]=aArray[i-1]+"";
			result[i][2]=(double)aArray[i-1]/sumaArray+"";
			result[i][3]=bArray[i-1]+"";
			result[i][4]=(double)bArray[i-1]/sumbArray+"";
		}
		return result;
	}
	

	/**
	 * 给定pvaule，获得相应的fdr，用R来计算的<br>
	 * 使用R的workspace目前在  /media/winE/Bioinformatics/R/practice_script/platform/pvalue2fdr/  中
	 * @param lsPvalue
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<Double> pvalue2FdrR(ArrayList<Double> lsPvalue) throws Exception {
		TxtReadandWrite txtPvalue = new TxtReadandWrite();
		txtPvalue.setParameter("/media/winE/Bioinformatics/R/practice_script/platform/pvalue2fdr/pvalue.txt", true, false);
		double[] tmpDouble = new double[lsPvalue.size()];
		for (int i = 0; i < tmpDouble.length; i++) {
			tmpDouble[i] = lsPvalue.get(i);
		}
		txtPvalue.Rwritefile(tmpDouble);
		getPvalue("/media/winE/Bioinformatics/R/practice_script/platform/");
		txtPvalue.setParameter("/media/winE/Bioinformatics/R/practice_script/platform/pvalue2fdr/fdr.txt", false, true);
		String[][] strFdr = txtPvalue.ExcelRead("\t", 2, 2, txtPvalue.ExcelRows(), 2);
		ArrayList<Double> lsFdr = new ArrayList<Double>();
		for (int i = 0; i < lsPvalue.size(); i++) {
			lsFdr.add(Double.parseDouble(strFdr[i][0]));
		}
		return lsFdr;
	}
	
	private static void getPvalue(String RworkSpace) throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+RworkSpace+ "pvalue2Fdr.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
		Integer aa = 0;
	}
	
	
	
	/**
	 * 将List中的数字按照行取中位数，也就是每一个Number[]取一个中位数
	 * 所以不要求Numbers[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByRow(List<? extends Number []> lsNum) 
	{
		double[] result = new double[lsNum.size()];
		for (int i = 0; i< lsNum.size() ; i++) {
			Number[] numbers = lsNum.get(i);
			
			int length = numbers.length;
			double[] tmp = new double[length];
			result[i] = StatUtils.percentile(tmp, 50);
		}
		return result;
	}
	
	
	/**
	 * 将List中的数字按照行取中位数，也就是每一个double[]取一个中位数
	 * 所以不要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByRowdou(List<double[]> lsNum) {
		double[] result = new double[lsNum.size()];
		for (int i = 0; i< lsNum.size() ; i++) {
			double[] numbers = lsNum.get(i);
			result[i] = StatUtils.percentile(numbers, 50);
		}
		return result;
	}
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求Numbers[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByCol(List<? extends Number []> lsNum) 
	{
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++)
			{
				Number[] number = lsNum.get(j);
				tmpMedia[j] = (Double) number[i];			
			}
			result[i] = StatUtils.percentile(tmpMedia, 50);
		}
		return result;
	}
	
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByColdou(List<double[]> lsNum) 
	{
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++)
			{
				tmpMedia[j] = lsNum.get(j)[i];			
			}
			result[i] = StatUtils.percentile(tmpMedia, 50);
		}
		return result;
	}
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMeanByColdou(List<double[]> lsNum) 
	{
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++)
			{
				tmpMedia[j] = lsNum.get(j)[i];			
			}
			result[i] = StatUtils.mean(tmpMedia);
		}
		return result;
	}
//////////////////////////// java 版的 fdr 计算， BH 方法 //////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定pvaule，获得相应的fdr，用java来计算的<br>
	 * @param lsPvalue
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<Double> pvalue2Fdr(ArrayList<Double> lsPvalue) {
		ArrayList<Double[]> lsPvalueInfo = new ArrayList<Double[]>();
		for (int i = 0; i < lsPvalue.size(); i++) {
			Double[] dou = new Double[2];
			dou[0] = (double) i;
			dou[1] = lsPvalue.get(i);
			lsPvalueInfo.add(dou);
		}
		HashMap<Integer, Double> hashResult = getFDR(lsPvalueInfo);
		ArrayList<Double> lsResult = new ArrayList<Double>();
		for (int i = 0; i < lsPvalue.size(); i++) {
			lsResult.add(hashResult.get(i));
		}
		return lsResult;
	}
	
	private static HashMap<Integer, Double> getFDR(ArrayList<Double[]> lsPvalue) {
		// ordening the pvalues.
		Collections.sort(lsPvalue, new Comparator<Double[]>() {
			@Override
			public int compare(Double[] o1, Double[] o2) {
				if (o1[1] < o2[1])
					return -1;
				else if (o1[1] == o2[1])
					return 0;
				else
					return 1;
			}
		});
		double[] ordenedPvalues = new double[lsPvalue.size()];
		double[] adjustedPvalues = new double[lsPvalue.size()];
		for (int i = 0; i < ordenedPvalues.length; i++) {
			ordenedPvalues[i] = lsPvalue.get(i)[1];
		}
		
		HashMap<Integer, Double> hashResult = new HashMap<Integer, Double>();
		// calculating adjusted p-values.
		double min = 1;
		double mkprk;
		for (int i = ordenedPvalues.length; i > 0; i--) {
			mkprk = ordenedPvalues.length * ordenedPvalues[i - 1] / i;
			if (mkprk < min) {
				min = mkprk;
			}
			adjustedPvalues[i - 1] = min;
		}
		for (int i = 0; i < adjustedPvalues.length; i++) {
			hashResult.put(lsPvalue.get(i)[0].intValue(), adjustedPvalues[i]);
		}
		return hashResult;
	}
	
}
