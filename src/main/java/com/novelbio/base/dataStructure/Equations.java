package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * �����趨һϵ�е�ArrayList<double[]> lsXY
 * Ȼ����x����
 * Ȼ����ֲ���x�����y
 * @author zong0jie
 */
public class Equations {
	private static Logger logger = Logger.getLogger(Equations.class);
	ArrayList<double[]> lsXY = new ArrayList<double[]>();
	double min = Double.MIN_VALUE;
	double max = Double.MAX_VALUE;
	public void setMin(double min){
		this.min = min;
	}
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * ����һϵ�е����ݣ���Ϊ���У����һ��xy������
	 * @param file û���ļ���ֱ�ӷ���
	 * @param colX x��
	 * @param colY y��
	 * @param rowNum �ӵڼ��п�ʼ��ȥ
	 */
	public void setXYFile(String file, int colX, int colY, int rowStart)
	{
		if (!FileOperate.isFileExist(file)) {
			lsXY.clear();
			return;
		}
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(file, new int[]{colX, colY}, rowStart, -1);
		for (String[] strings : lsInfo) {
			double x = 0;
			double y = 0;
			try {
				x = Double.parseDouble(strings[0]);
				y = Double.parseDouble(strings[1]);
			} catch (Exception e) {
				continue;
			}
			addXY(x, y);
		}
		//����
		Collections.sort(lsXY, new CompLsXY());
	}
	/**
	 * ����һϵ�е����ݣ���Ϊ���У����һ��xy������
	 * @param file  û���ļ���ֱ�ӷ���  x��һ�У�y�ڶ����У��ӵ�һ�п�ʼ��ȡ
	 */
	public void setXYFile(String file)
	{
		setXYFile(file, 1, 2, 1);
	}
	/**
	 * �趨x��y��ֵ
	 * @param x
	 * @param y
	 */
	public void addXY(double x, double y)
	{
		lsXY.add(new double[]{x,y});
	}
	public void setXY(ArrayList<double[]> lsXY)
	{
		this.lsXY = lsXY;
	}
	/**
	 * �����������
	 * @param x
	 * @return
	 */
	public double[] getYinfo(double[] x)
	{		
		double[] y = new double[x.length];

		if (lsXY.size() < 2) {
			for (int i = 0; i < y.length; i++) {
				y[i] = x[i];
			}
			return y;
		}
		
		for (int i = 0; i < y.length; i++) {
			y[i] = getY(x[i]);
		}
		return y;
	}

	/**
	 * ����x����ö�Ӧ��y
	 * @param X
	 * @return
	 */
	public double getY(double X)
	{
		int num = Collections.binarySearch(lsXY, new double[]{X,0}, new CompLsXY());
		if (num >= 0) {
			return lsXY.get(num)[1];
		}
		else {
			//���x����ǰ��
			if (num == -1) {
				return getYinside(lsXY.get(0), lsXY.get(1), X);
			}
			//���x�������
			else if (num == -lsXY.size() - 1) {
				return getYinside(lsXY.get(-num - 3), lsXY.get(-num - 2), X);
			}
			//���x���м�
			else {
				return getYinside(lsXY.get(-num - 2), lsXY.get( -num - 1), X);
			}
		}
	}
	
	/**
	 * ָ����һ�������һ��������꣬��������֮���xֵ������õ��Yֵ
	 * @param upXY
	 * @param downXY
	 * @param X
	 * @return
	 */
	private double getYinside(double[] upXY, double[] downXY, double x)
	{
		double x1 = upXY[0]; double  y1 = upXY[1];
		double x2 = downXY[0]; double y2 = downXY[1];
		if (x1 == x2) {
			logger.error("�������, x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2 + " x: " + x);
		}
//		if (x > Math.max(x1, x2) || x < Math.min(x1, x2)) {
//			logger.error("�������, x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2 + " x: " + x);
//		}
		double y = (y2*x1 - y2*x - y1*x2 + y1*x)/(x1 - x2);
		if (y < min) {
			return min;
		}
		if (y > max) {
			return max;
		}
		return y;
	}
}

class CompLsXY implements Comparator<double[]>
{
	@Override
	public int compare(double[] o1, double[] o2) {
		Double x1 = o1[0];
		Double x2 = o2[0];
		return x1.compareTo(x2);
	}
}
