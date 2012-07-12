package com.novelbio.base.dataOperate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.omg.CosNaming._BindingIteratorImplBase;
/**
 * ʱ��������
 * �������������������ʱ��
 * @author zong0jie
 *
 */
public class DateTime {
	long start = 0;
	/**
	 * �Զ��趨��ʼʱ��
	 */
	public DateTime() {
		start = System.currentTimeMillis(); //��ȡ���ʱ��
	}
	/**
	 * �趨��ʼʱ��
	 */
	public void setStartTime() {
		start = System.currentTimeMillis(); //��ȡ���ʱ��
	}
	
	/**
	 * ���趨starttime��ʼ��������ʱ�䣬��λms
	 * @return
	 */
	public long getEclipseTime() {
		long end=System.currentTimeMillis(); //��ȡ���н���ʱ��
		return end-start; 
	}
	
	/**
	 * ���ص�ǰ���ڣ���ʽ "yyyy-MM-dd"
	 * @return
	 */
	public static String getDate() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     return formatDate.format(currentDate); //������ʱ���ʽ��
	}
	/**
	 * ���ص�ǰ���ڼ���һ�����������Ψһ�ļ������ã���ʽ "yyyy-MM-dd"
	 * @return
	 */
	public static String getDateAndRandom() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-ddhhss");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     String date = formatDate.format(currentDate); //������ʱ���ʽ��
	     Random random = new Random(System.currentTimeMillis());
	     short Tmp = (short)random.nextInt();
	     return date + Tmp;
	}
	/**
	 * ���ص�ǰ���ڣ���ʽ "yyyy-MM-dd"
	 * @return
	 */
	public static String getDateDetail() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd-hh-mm-ss");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     return formatDate.format(currentDate); //������ʱ���ʽ��
	}
	/**
	 * ���ص�ǰ���ڣ���ʽ "yyyy-MM"
	 * @return
	 */
	public static String getDateMM() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     return formatDate.format(currentDate); //������ʱ���ʽ��
	}
}
