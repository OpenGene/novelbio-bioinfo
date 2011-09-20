package com.novelbio.base.dataOperate;

import java.text.SimpleDateFormat;
import java.util.Date;
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
	 * ���ص�ǰ���ڣ���ʽ "yyyy-MM"
	 * @return
	 */
	public static String getDateMM() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM");
	     Date currentDate = new Date(); //�õ���ǰϵͳʱ��
	     return formatDate.format(currentDate); //������ʱ���ʽ��
	}
}
