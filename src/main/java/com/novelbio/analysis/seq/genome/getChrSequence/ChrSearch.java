package com.novelbio.analysis.seq.genome.getChrSequence;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * �м�㣬���������ײ�ľ�����ȡ���е���
 * ע�⣬������ȫ�ǽ�����ChrStringHash��Ļ����ϵģ�����
 * ����Ҫ��ChrStringHash���֧�ֲ��ܹ�����
 * ���ڱ����к���һ����ϣ�����ڷ��򻥲������Ծ�û����static����
 * ���ߣ��ڽ� 20090617
 * @author Zong Jie
 *
 */
public class ChrSearch extends ChrStringHash
{
	 private static String Chrpatten="Chr\\w+";//Chr1�� chr2�� chr11����ʽ,ע�⻹��chrx֮��ģ�chr������Դ�"_"������˵������"_"�ָ�chr���ַ�
 
	 /**
	 * �����ı��������ػ���Ĺ�ϣ��
	 * @param genomefilename
	 * �����и����⣺����ϣ������ͨ��ϣ��������õ�genomeinfo��ϣ����ChrstringHash�ľ�̬��ϣ��
	 * ��ô����̬���ȡ�������Ķ�����ʹ��̬��ϣ��ı��
	 * ����ϣ��ᷢ���仯��
	 */
	 public static void LoadGenome(String genomeFilePath)
	 {
		 try {
			 ChrStringHash.setChrFilePath(genomeFilePath);
		 } catch (Exception e) {e.printStackTrace();}//��ȡ��������Ϣ��������genomeinfo��ϣ����
	 }
  
  /**
   * ����peakλ�㣬����ָ����Χ��sequence,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
   * @param chr, 
   * @param peaklocation peak summit������
   * @param region peak���ҵķ�Χ
   * @param cisseq true:������    false�����򻥲���
   */
  
  public static String getSeq(String chr, int peaklocation, int region,boolean cisseq)
  {
	    /**
	     * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
	     */
	    Pattern pattern =Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
	    Matcher matcher; //matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
	    matcher = pattern.matcher(chr);
	    if(!matcher.find())
	    {
	    	return "ReadSiteȾɫ���ʽ����";
	    }
	    else {
			chr = matcher.group().toLowerCase();
		}
	    int startnum=peaklocation-region;
	    int endnum=peaklocation+region;
		return ChrStringHash.getSeq(cisseq,chr, startnum, endnum);	
  }
  
  /**
   * ����Ⱦɫ����λ�úͷ��򷵻�����
   * @param chrlocationȾɫ���ŷ����磺Chr:1000-2000,�Զ���chrIDСд,chrID����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
   * @param cisseq����true:���� false:���򻥲�
   */
  public static String getSeq(String chrlocation, boolean cisseq)
  {
	    /**
	     * �ж�Chr��ʽ�Ƿ���ȷ���Ƿ�����Ч��Ⱦɫ��
	     */
	    Pattern pattern =Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
	    Matcher matcher; 
	    matcher = pattern.matcher(chrlocation);
	    if(!matcher.find())
	    {
	    	return "ReadSiteȾɫ���ʽ����";
	    }
	   String chr=matcher.group();
	    
	    /**
	     * ��ȡ��ʼλ�����ֹλ��
	     */
	    Pattern patternnumber =Pattern.compile("(?<!\\w)\\d+(?!\\w)", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
	    Matcher matchernumber; 
	    matchernumber = patternnumber.matcher(chrlocation);
	    int[] location= new int[2];
	    int i=0;
	    while(matchernumber.find())
	    {
	     	location[i]=Integer.parseInt(matchernumber.group());
	        i++;
	    }
	    if(i>2||location[1]<=location[0])
	    {
	    	return"ReadSiteȾɫ��λ�ô���";
	    }
    	return ChrStringHash.getSeq( cisseq,chr.toLowerCase(), location[0], location[1]);
  }
 
		/**
		 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
		 * @param chrID �ڲ��Զ�ת��ΪСд
		 * @return
		 */
		public static long getChrLength(String chrID) 
		{
			return ChrStringHash.getHashChrLength().get(chrID.toLowerCase());
		}
 
		

		
	
	
}
