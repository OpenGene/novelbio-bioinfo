package com.novelbio.base.dataStructure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.ibatis.annotations.Insert;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;


public class MathComput {
	private static Logger logger = Logger.getLogger(MathComput.class);
	
	
	/**
	 * �������ݣ����ƽ����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double mean(int[] unsortNum)
	{
		int length=unsortNum.length;
		int sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		double avg= (double)sum/length;
		return avg;
	}
	
	/**
	 * �������ݣ����ƽ����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double mean(int[]... unsortNum)
	{
		long sum = 0;
		long num = 0;
		for (int i = 0; i < unsortNum.length; i++) {
			for (int j = 0; j < unsortNum[i].length; j++) {
				sum = sum + unsortNum[i][j];
				num ++ ;
			}
		}
		double avg=(double) sum/num;
		return avg;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double mean(Iterable<int[]> unsortNum)
	{
		long sum = 0;
		long num = 0;
		for (int[] is : unsortNum) {
			for (int i : is) {
				sum = sum + i;
				num ++;
			}
		}
		double avg=(double) sum/num;
		return avg;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static long mean(long[] unsortNum)
	{
		int length=unsortNum.length;
		long sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		long avg=sum/length;
		return avg;
	}
	
	public static double mean(List<? extends Number> lsNumbers) {
		double length=lsNumbers.size();
		double sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+lsNumbers.get(i).doubleValue();
		}
		double avg=sum/length;
		return avg;
	}
	
	
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double mean(double[] unsortNum)
	{
		double length=unsortNum.length;
		double sum=0;
		for(int i=0;i<length;i++) {
			sum=sum+unsortNum[i];
		}
		double avg=sum/length;
		return avg;
	}
	/**
	 * ����ID����ȡһ���������λ��
	 * ÿ�б�ʾ��ͬ����Ϣ��ÿ�б�ʾһ������
	 * ���ܴ����ظ���������Ҫ���ظ���(Ҳ�����ظ�����)��ȡ��λ��
	 * @param lsIn
	 * @param colAccID
	 * @param colNum
	 * @return
	 */
	public static ArrayList<String[]> getMedian(List<String[]> lsIn, int colAccID, List<Integer> colNum) {
		/**
		 * ÿ��IDһ������
		 */
		HashMap<String, ArrayList<String[]>> hashGeneInfo = new HashMap<String, ArrayList<String[]>>();
		
		hashGeneInfo = new HashMap<String, ArrayList<String[]>>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		colAccID--;
		ArrayList<Integer> lsColNum = new ArrayList<Integer>();
		for (int i = 0; i < colNum.size(); i++) {
			lsColNum.add(colNum.get(i) - 1);
		}
		lsResult.add(lsIn.remove(0));
		for (String[] strings : lsIn) {
			if (hashGeneInfo.containsKey(strings[colAccID].trim()) ) {
				ArrayList<String[]> lsInfo = hashGeneInfo.get(strings[colAccID].trim());
				lsInfo.add(strings);
			}
			else {
				ArrayList<String[]> lsInfo = new ArrayList<String[]>();
				lsInfo.add(strings);
				hashGeneInfo.put(strings[colAccID].trim(), lsInfo);
			}
		}
		Collection<ArrayList<String[]>> values = hashGeneInfo.values();
		for(ArrayList<String[]> value:values)
		{
			try {
				lsResult.add(getMediaInfo(value, lsColNum));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return lsResult;
	}
	/**
	 * @param lsInfo ָ��������Ϣ
	 * @param col ָ�����ڵ���
	 * @return ���������е���λ��
	 * �������õ�һ��list������
	 */
	public static String[] getMediaInfo(List<String[]> lsInfo, List<Integer> col) {
		
		if (lsInfo.size() == 1) {
			return lsInfo.get(0);
		}
		String[] result = lsInfo.get(0);
		
		for (int i = 0; i < col.size(); i++) {
			double[] info = new double[lsInfo.size()];
			for (int m = 0; m < lsInfo.size(); m++) {
				info[m] = Double.parseDouble(lsInfo.get(m)[col.get(i)]);
			}
			double infoNew = median(info);
			result[col.get(i)] = infoNew + "";
		}
		return result;
	}
	/**
	 * �������ݣ���ú�
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
	 * �������ݣ���ú�
	 * @param Num
	 * @return
	 */
	public static	double sum(ArrayList<? extends Number> lsNum) {
		double sum = 0;
		for (Number d : lsNum) {
			sum = sum + d.doubleValue();
		}
		return sum;
	}
	/**
	 * �������ݣ���ú�
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
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static int median(int[] unsortNum) {
		int med=-100;
		int length=unsortNum.length;
		int[] unsortNew = copyArray(unsortNum);
		sort(unsortNew, true);
 
		if (length%2==0) 
			med=(unsortNew[length/2-1]+unsortNew[length/2])/2;
		else 
			med=unsortNew[length/2];
		return med;
	}
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * @return
	 */
	public static double median(List<? extends Number> lsNumbers) {
		double[] mydouble = new double[lsNumbers.size()];
		for (int i = 0; i < mydouble.length; i++) {
			mydouble[i] = lsNumbers.get(i).doubleValue();
		}
		return median(mydouble);
	}
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * @param lsNumbers
	 * @param percentage ����100��ֵ
	 * @return
	 */
	public static double median(List<? extends Number> lsNumbers, int percentage) {
		double[] mydouble = new double[lsNumbers.size()];
		for (int i = 0; i < mydouble.length; i++) {
			mydouble[i] = lsNumbers.get(i).doubleValue();
		}
		return median(mydouble, percentage);
	}
	
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * �����������������
	 * @return
	 */
	public static double median(double[] unsortNum) {
		return median(unsortNum, 50);
	}
	/**
	 * 
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * @param unsortNum �������ݣ������������
	 * @param percentage ����100��ֵ
	 * @return
	 */
	public static double median(double[] unsortNum, int percentage)
	{
		double med = -100;
		int length = unsortNum.length;
		double[] unsortNew = copyArray(unsortNum);
		sort(unsortNew, true);
		if (length*percentage%100==0) 
			med = (unsortNew[length*percentage/100-1] + unsortNew[length*percentage/100])/2;
		else 
			med = unsortNew[length*percentage/100];
		return med;
	}
	/**
	 * copy an array
	 * @param array
	 * @return a new array which is been copied
	 */
	private static double[] copyArray(double[] array)
	{
		double[] arrayResult = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			arrayResult[i] = array[i];
		}
		return arrayResult;
	}
	/**
	 * copy an array
	 * @param array
	 * @return a new array which is been copied
	 */
	private static int[] copyArray(int[] array)
	{
		int[] arrayResult = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			arrayResult[i] = array[i];
		}
		return arrayResult;
	}
	/**
	 * �������ݣ������ӽ���λ�����Ǹ���, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * @return
	 */
	public static double medianLike(double[] unsortNum)
	{
		double med=-100;
		int length=unsortNum.length;
		double[] unsortNew = copyArray(unsortNum);
		sort(unsortNew, true);

		if (length%2==0){
			med=(unsortNew[length/2-1]+unsortNew[length/2])/2;
			if (Math.abs(unsortNew[length/2-1] - med) <= Math.abs(unsortNew[length/2] - med)) {
				return unsortNew[length/2-1];
			}
			else {
				return unsortNew[length/2];
			}
		}
		else 
			return unsortNew[length/2];
	}
	
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double max(ArrayList<? extends Number> lsNum) {
		double max = lsNum.get(0).doubleValue();
		for (Number number : lsNum) {
			double tmp = number.doubleValue();
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double max(double[] num) {
		double max = num[0];
		for (double number : num) {
			double tmp = number;
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static int max(int[] num) {
		int max = num[0];
		for (int number : num) {
			int tmp = number;
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	
	
	
	
	
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double min(ArrayList<? extends Number> lsNum) {
		double min = lsNum.get(0).doubleValue();
		for (Number number : lsNum) {
			double tmp = number.doubleValue();
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static double min(double[] num) {
		double min = num[0];
		for (double number : num) {
			double tmp = number;
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	public static int min(int[] num) {
		int min = num[0];
		for (int number : num) {
			int tmp = number;
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	
	/**
	 * �������ݽ�������
	 * @param unsortNum �����������
	 * @param smallToBig �Ƿ��С��������
	 * @return
	 */
	public static void sort(int[] unsortNum, boolean smallToBig)
	{
		int tmp = -10000;
		int length = unsortNum.length;
		if (smallToBig) {
			for(int i = 1;i<length;i++) {
				tmp = unsortNum[i];
				int j = i;
				for(;j > 0; j --) {
					if(tmp < unsortNum[j-1]) {
						unsortNum[j] = unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j] = tmp;
			}
		}
		else {
			for(int i = 1; i < length; i ++) {
				tmp = unsortNum[i];
				int j = i;
				for(;j > 0;j --) {
					if(tmp > unsortNum[j-1]) {
						unsortNum[j] = unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j] = tmp;
			}
		}
	}
	
	/**
	 * �������飬ֱ������
	 * @param unsortNum
	 */
	private static void sort(double[] unsortNum, boolean smallToBig)
	{
		double tmp=-10000;
		int length=unsortNum.length;
		if (smallToBig) {
			for(int i=1;i<length;i++) {
				tmp=unsortNum[i];
				int j=i;
				for(;j>0;j--) {
					if(tmp<unsortNum[j-1]) {
						unsortNum[j]=unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j]= tmp;
			}
		}
		else {
			for(int i=1;i<length;i++) {
				tmp=unsortNum[i];
				int j=i;
				for(;j>0;j--) {
					if(tmp>unsortNum[j-1]) {
						unsortNum[j]=unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j]= tmp;
			}
		}
	}
	
	
	/**
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ����ü�Ȩƽ���������ָ���ָ�����������
	 * Ʃ��������int[20]��һ����������Ҫ����������С��int[10]���沢�ұ�������������Ǻϣ���ʱ���Ҳ��ü�Ȩƽ���ķ���
	 * �����һ�飬�о�����
	 * ���ڽ�500�����ݵ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param binNum ����Ҫ���ɵķָ�Ŀ���
	 * @param startBias �����Ķ��ٿ�ʼ Ϊ 0,1֮���С������ʾ�ӵ�һ��ֵ�ļ���֮����ʼ
	 * @param endBias ���յ�Ķ��ٽ���  Ϊ 0,1֮���С������ʾ�� (����λ�㵽�յ�ľ���/ÿ����Ԫ�ĳ���)
	 * 0-*--|---1---------2--------3----------4----------5---------6----|--*-7
	 * �Ǻű�ǵĵط�
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public static double[] mySpline(int[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //��ÿһ���ָ��ĳ��ȱ�׼��Ϊһ����ֵ����׼ΪinvNumΪ1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//ĳ������treatNum���ߵ�һ��ֵ(�����߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//�����ֵ��Ȩ��
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////ĳ������treatNum���ұߵ�һ��ֵ(�������߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//���ұ�ֵ��Ȩ��
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////������Ҷ˵㶼��һ�������ڣ���ô��Ȩƽ�������ֵ���ӺͶ����ڸ������ֵ/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////���Ƿ�����ɾ//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�м��м���ֵ
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
			//////////////////��������ѡ���Ȩƽ�������ֵ��Ӻ�////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//��Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //���ֵ
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //Ĭ�ϼ�Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	/**
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ����ü�Ȩƽ���������ָ���ָ�����������
	 * Ʃ��������int[20]��һ����������Ҫ����������С��int[10]���沢�ұ�������������Ǻϣ���ʱ���Ҳ��ü�Ȩƽ���ķ���
	 * �����һ�飬�о�����
	 * ���ڽ�500�����ݵĻ�����tag�ۼ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param binNum ����Ҫ���ɵķָ�Ŀ���
	 * @param startBias �����Ķ��ٿ�ʼ ����߷ָ������ľ����ֵ
	 * @param endBias ���յ�Ķ��ٽ��� ���ұ߷ָ����յ�ľ����ֵ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2���Ӻ�
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type) {
		double rawlength=treatNum.length - startBias - endBias;
		double binlength=rawlength/binNum; //��ÿһ���ָ��ĳ��ȱ�׼��Ϊһ����ֵ����׼ΪinvNumΪ1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//ĳ������treatNum���ߵ�һ��ֵ(�����߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//�����ֵ��Ȩ��
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////ĳ������treatNum���ұߵ�һ��ֵ(�������߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//���ұ�ֵ��Ȩ��
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////������Ҷ˵㶼��һ�������ڣ���ô��Ȩƽ�������ֵ���ӺͶ����ڸ������ֵ/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////���Ƿ�����ɾ//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�м��м���ֵ
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
			//////////////////��������ѡ���Ȩƽ�������ֵ��Ӻ�////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//��Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //���ֵ
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //Ĭ�ϼ�Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}

	
	/**
	 * �����ʱ��Ϊ��������
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ�����кϲ��������ָ���ָ�����������
	 * ���ڽ�500�����ݵĻ�����tag�ۼ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param invBpNum ÿһ�������bp�����ȷ������Ҫ����3��bpһ��coding��ô����
	 * @param startBp �����ĵڼ���Bp��ʼ��ʵ����㣬��1��ʼ��������Ϊ���в�һ����3�ı�������ô����ָ�������ĵڼ���bp��ʼ���Ӹ�Bp(<b>������Bp</b>)���л���
	 * �����ֵ���С��invBpNum
	 * @param Num ѡ���invBp�У�Ҳ����3��bp�еڼ�����Ϊ���Ľ������1��ʼ��������ô����Ļ���Ӧ��ѡ�����һ��--Ҳ���ǵ�����bp�Ľ����Ϊ���ֵĽ��
	 * Ҳ����˵���������Ӧ��Ϊ3
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int invBpNum,int startBp,int Num)
	{
		//���������ó���
		int length = (int)((double)(treatNum.length - startBp + 1)/invBpNum + 0.5);
		double[] result = new double[length]; int k = 0; int m = 0;
		//startBp - 2 startbp��ʵ��λ�ã���ǰ��һλ�Ǵ�0��ʼ�ı�λ�㣬����ǰ��һλ��ǰһλ�㣬Ȼ�����Numƫ��
		for (int i = startBp - 2 + Num; i < treatNum.length; i++) {
			if (m%invBpNum == 0) {
				result[k] = treatNum[i];
				k++;
			}
			m++;
		}
		return result;
	}
	
	/**
	 * �����Ϊ��������
	 * ����һ�����飬ָ��ATG���ڵ�λ��(ʵ��λ��)��Ȼ��Ӹ�λ����ǰ(������)�����(������)�����ݸ����ķָ�����ָ�����кϲ��������ָ���ָ�����������
	 * ���ڽ�500�����ݵĻ�����tag�ۼ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param invBpNum ÿһ�������bp�����ȷ������Ҫ����3��bpһ��coding��ô����
	 * @param ATGsite �����ĵڼ���Bp��ʼ(ʵ��λ��)����Ϊ���в�һ����3�ı�������ô����ָ�������ĵڼ���bp��ʼ���Ӹ�Bp(<b>Ҳ����ATG��ʵ��λ�㣬������Bp</b>)���л���
	 * �����ֵ���С��invBpNum
	 * @param Num ѡ���invBp�У�Ҳ����3��bp�еڼ�����Ϊ���Ľ������1��ʼ��������ô����Ļ���Ӧ��ѡ�����һ��--Ҳ���ǵ�����bp�Ľ����Ϊ���ֵĽ��
	 * Ҳ����˵���������Ӧ��Ϊ3
	 * @return
	 */
	public static double[] mySplineHY(double[] treatNum, int invBpNum,int ATGsite,int Num)
	{
		//���������ó���
		int lengthDown = (int)Math.ceil((double)(treatNum.length - ATGsite + 1)/invBpNum);
		int lengthUp = (int)Math.ceil((double)(ATGsite -  1)/invBpNum);
		double[] result = new double[lengthDown + lengthUp];
		
		int k = lengthUp;
		//��벿��
		for (int i = ATGsite - 1; i < treatNum.length + 1 - Num;  i = i + invBpNum) {
			result[k] = treatNum[i + Num - 1];
			k++;
		}
		//ǰ�벿��
		k = lengthUp - 1;
		for (int i = ATGsite - 1 - invBpNum; i >= 1-Num; i = i - invBpNum) {
			result[k] = treatNum[i + Num - 1];
			k--;
		}
		return result;
	}
	
	
	/**
	 * �� aArray��bArray��ӣ������������aArray��
	 * ���bArray==null����ֱ�ӷ���aArray�����ǻ�system.out.println����
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
	 * �� aArray��bArray������һ�𲢼���a��bÿ�����ֱַ�ı�����aArray��bArray�ĳ��ȱ���һ��
	 * @param aArray ʵ��������
	 * @param bArray ���������ݣ�Ҳ���Ǳ���
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
	 * ����pvaule�������Ӧ��fdr����R�������<br>
	 * ʹ��R��workspaceĿǰ��  /media/winE/Bioinformatics/R/practice_script/platform/pvalue2fdr/  ��
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
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+RworkSpace+ "pvalue2Fdr.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
		Integer aa = 0;
	}
	
	
	
	/**
	 * ��List�е����ְ�����ȡ��λ����Ҳ����ÿһ��Number[]ȡһ����λ��
	 * ���Բ�Ҫ��Numbers[]�ĳ�����ȡ�
	 * ����������Ҫ̫�󣬱𳬹����ڴ�����
	 * @return
	 */
	public static double[] getMediaByRow(List<? extends Number []> lsNum) {
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
	 * ��List�е����ְ�����ȡ��λ����Ҳ����ÿһ��double[]ȡһ����λ��
	 * ���Բ�Ҫ��doubles[]�ĳ�����ȡ�
	 * ����������Ҫ̫�󣬱𳬹����ڴ�����
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
	 * ��List�е����ְ����У�Ҳ��������ÿһ�е�Numberȡһ��ֵ������е�ƽ����
	 * ����Ҫ��Numbers[]�ĳ�����ȡ�
	 * ����������Ҫ̫�󣬱𳬹����ڴ�����
	 * @return
	 */
	public static double[] getMediaByCol(List<? extends Number []> lsNum) {
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++) {
				Number[] number = lsNum.get(j);
				tmpMedia[j] = (Double) number[i];			
			}
			result[i] = StatUtils.percentile(tmpMedia, 50);
		}
		return result;
	}
	
	
	/**
	 * ��List�е����ְ����У�Ҳ��������ÿһ�е�Numberȡһ��ֵ������е�ƽ����
	 * ����Ҫ��doubles[]�ĳ�����ȡ�
	 * ����������Ҫ̫�󣬱𳬹����ڴ�����
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
	 * ��List�е����ְ����У�Ҳ��������ÿһ�е�Numberȡһ��ֵ������е�ƽ����
	 * ����Ҫ��doubles[]�ĳ�����ȡ�
	 * ����������Ҫ̫�󣬱𳬹����ڴ�����
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
	
	/**
	 * ����һ��������������Ȼ�����򰤵ĺܽ�--С��distance--������ϲ�Ϊһ������󷵻غϲ��������list
	 * ����ֻ���յ�һ������������򣬴�С��������
	 * @param lsNum ����list��ÿ��Ϊdouble[0]������ꡣdouble[1] �յ����� �������С���յ�
	 * @param distance��С��0��Ĭ��Ϊ0����ֻ�ϲ��ص�������
	 * @return
	 */
	public static ArrayList<double[]>  combInterval(List<double[]> lsNum, double distance) 
	{
		Collections.sort(lsNum, new Comparator<double[]>() {
			//��С��������
			@Override
			public int compare(double[] o1, double[] o2) {
				if (o1[0] < o2 [0]) 
					return -1;
				else if (o1[0] > o2[0]) 
					return 1;
				else {
					if (o1[1] < o2[1]) 
						return -1;
					else if (o1[1] > o2[1]) 
						return 1;
					else 
						return 0;
				}
			}
		});
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		int i = 1;double[] tmpResult = lsNum.get(0);
		while (i < lsNum.size()) {
			double[] loc = lsNum.get(i);
			if (tmpResult[1] >= loc[0] - distance) {
				tmpResult[1] = loc[1];
				i++;
			}
			else {
				lsResult.add(tmpResult);
				tmpResult  = loc;
				i++;
			}
		}
		lsResult.add(tmpResult);
		return lsResult;
	}
	
	
	/**
	 * 
	 * ����һ�������������ӽ�����С��distance�����ϲ�������Ȩ�ش����һ��
	 * ��󷵻ذ���λ�ý�������Ľ��
	 * @param lsNum double[2] 0:���� 1:Ȩ��
	 * @param distance ���ֵľ��룬����С�ڸ�ֵ
	 * @param max true ѡ��Ȩ�����ģ�minѡ��Ȩ����С��
	 * @return
	 */
	public static ArrayList<double[]>  combLs(List<double[]> lsNum, double distance, boolean max) {
		Collections.sort(lsNum, new Comparator<double[]>() {
			//��С��������
			public int compare(double[] o1, double[] o2) {
				if (o1[0] == o2 [0]) return 0;
				return o1[0] < o2[0] ? -1:1;
			}});
		//����һ����
		double bigNum = lsNum.get(lsNum.size() -1)[0];
		double binNum =  (bigNum - lsNum.get(0)[0])/distance;//������и�ɶ��ٷ�
		
		int lastInsertNum = 0;
		int insertNum = 0;
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		for (int i = 0; i < binNum; i++) {
			double[] binNum2 = new double[2]; binNum2[0] = binNum; binNum2[1] = 0;
			lastInsertNum = insertNum;
			insertNum = Collections.binarySearch(lsNum, binNum2,new Comparator<double[]>() {
				public int compare(double[] o1, double[] o2) {
					if (o1[0] == o2 [0]) return 0;
					return o1[0] < o2[0] ? -1:1;
				} });
			//��ȡ��Ӧ������
			List<double[]> lsTmp = null;
			if (insertNum >= 0) {
				insertNum ++;
				lsTmp = lsNum.subList(lastInsertNum, insertNum);
				
			}
			else {
				insertNum = -insertNum - 1;
				lsTmp = lsNum.subList(lastInsertNum, insertNum);
			}
			double[] tmpResult = getBigestWeight(lsTmp,max);
			if (tmpResult != null) {
				lsResult.add(tmpResult);
			}
		}
		return lsResult;
	}
	
	
	/**
	 * �ҵ����������Ȩ������һ�û���򷵻�null
	 * @param lsNum 0:���� 1:Ȩ��
	 * @param max true ѡ��Ȩ�����ģ�minѡ��Ȩ����С��
	 * @return
	 */
	private static double[] getBigestWeight(List<double[]> lsNum, boolean max)
	{
		double[] result = null;
		if (max) {
			double big = Double.MIN_VALUE;
			for (double[] ds : lsNum) {
				if (ds[1] > big) {
					result = ds;
					big = ds[1];
				}
			}
		}
		else {
			double small = Double.MAX_VALUE;
			for (double[] ds : lsNum) {
				if (ds[1] < small) {
					result = ds;
					small = ds[1];
				}
			}
		}
		return result;
	}
	
//////////////////////////// java ��� fdr ���㣬 BH ���� //////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����pvaule�������Ӧ��fdr����java�������<br>
	 * @param lsPvalue
	 * @return ���ظ�����һ��˳���fdrlist
	 * @throws Exception 
	 */
	public static ArrayList<Double> pvalue2Fdr(Collection<Double> lsPvalue) {
		ArrayList<Double[]> lsPvalueInfo = new ArrayList<Double[]>();
		int i = 0;
		for (Double doubles : lsPvalue) {
			Double[] dou = new Double[2];
			dou[0] = (double) i;
			dou[1] = doubles;
			lsPvalueInfo.add(dou);
			i ++;
		}
		
		HashMap<Integer, Double> hashResult = getFDR(lsPvalueInfo);
		ArrayList<Double> lsResult = new ArrayList<Double>();
		int resultSize = lsPvalue.size();
		for (int m = 0; m < resultSize; m++) {
			lsResult.add(hashResult.get(m));
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
