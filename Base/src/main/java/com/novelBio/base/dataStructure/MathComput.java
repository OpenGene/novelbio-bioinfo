package com.novelBio.base.dataStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import com.novelBio.base.dataOperate.TxtReadandWrite;


public class MathComput {
	/**
	 * �������ݣ����ƽ����
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
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
		if(avg>32767)
			System.out.println("avg�ܽ�ʱ���  "+avg);
		return avg;
	}
	
	
	
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
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
		if(med>32767)
			System.out.println("med�ܽ�ʱ���");
		return med;
	}
	
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
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
	 * �������ݽ�������
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
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ����ü�Ȩƽ���������ָ���ָ�����������
	 * Ʃ��������int[20]��һ����������Ҫ����������С��int[10]���沢�ұ�������������Ǻϣ���ʱ���Ҳ��ü�Ȩƽ���ķ���
	 * �����һ�飬�о�����
	 * ���ڽ�500�����ݵ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param binNum ����Ҫ���ɵķָ�Ŀ���
	 * @param startBias �����Ķ��ٿ�ʼ Ϊ 0,1֮���С������ʾ�ӵ�һ��ֵ�ļ���֮����ʼ
	 * @param endBias ���յ�Ķ��ٽ���  Ϊ 0,1֮���С������ʾ�����һ��ֵ�ļ���֮������
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
				//////////���Ƿ�������ɾ//////////////////////
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
	 * @param startBias �����Ķ��ٿ�ʼ
	 * @param endBias ���յ�Ķ��ٽ���
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2���Ӻ�
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type)
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
				//////////���Ƿ�������ɾ//////////////////////
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
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static void convertArray(int[] array) 
	{
		int tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static void convertArray(double[] array) 
	{
		double tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static<T> void convertArray(T[] array) 
	{
		T tmpValue=null;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
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
	public static ArrayList<Double> pvalue2Fdr(ArrayList<Double> lsPvalue) throws Exception {
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
	
	
}