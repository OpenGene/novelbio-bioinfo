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
 * 中间层，用来操作底层的具体提取序列的类
 * 注意，本类完全是建立在ChrStringHash类的基础上的，所以
 * 必须要有ChrStringHash类的支持才能工作！
 * 由于本类中含有一个哈希表用于反向互补，所以就没有做static方法
 * 作者：宗杰 20090617
 * @author Zong Jie
 *
 */
public class ChrSearch extends ChrStringHash
{
	 private static String Chrpatten="Chr\\w+";//Chr1， chr2， chr11的形式,注意还有chrx之类的，chr里面可以带"_"，所以说不能用"_"分割chr与字符
 
	 /**
	 * 给定文本名，返回基因的哈希表
	 * @param genomefilename
	 * 这里有个问题：本哈希表是普通哈希表，而所获得的genomeinfo哈希表是ChrstringHash的静态哈希表
	 * 那么当静态类读取了其他的东西，使静态哈希表改变后
	 * 本哈希表会发生变化吗？
	 */
	 public static void LoadGenome(String genomeFilePath)
	 {
		 try {
			 ChrStringHash.setChrFilePath(genomeFilePath);
		 } catch (Exception e) {e.printStackTrace();}//读取基因组信息并保存在genomeinfo哈希表中
	 }
  
  /**
   * 给出peak位点，查找指定范围的sequence,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写
   * @param chr, 
   * @param peaklocation peak summit点坐标
   * @param region peak左右的范围
   * @param cisseq true:正向链    false：反向互补链
   */
  
  public static String getSeq(String chr, int peaklocation, int region,boolean cisseq)
  {
	    /**
	     * 判断Chr格式是否正确，是否是有效的染色体
	     */
	    Pattern pattern =Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
	    Matcher matcher; //matcher.groupCount() 返回此匹配器模式中的捕获组数。
	    matcher = pattern.matcher(chr);
	    if(!matcher.find())
	    {
	    	return "ReadSite染色体格式错误";
	    }
	    else {
			chr = matcher.group().toLowerCase();
		}
	    int startnum=peaklocation-region;
	    int endnum=peaklocation+region;
		return ChrStringHash.getSeq(cisseq,chr, startnum, endnum);	
  }
  
  /**
   * 给出染色体编号位置和方向返回序列
   * @param chrlocation染色体编号方向如：Chr:1000-2000,自动将chrID小写,chrID采用正则表达式抓取，无所谓大小写，会自动转变为小写
   * @param cisseq方向，true:正向 false:反向互补
   */
  public static String getSeq(String chrlocation, boolean cisseq)
  {
	    /**
	     * 判断Chr格式是否正确，是否是有效的染色体
	     */
	    Pattern pattern =Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Matcher matcher; 
	    matcher = pattern.matcher(chrlocation);
	    if(!matcher.find())
	    {
	    	return "ReadSite染色体格式错误";
	    }
	   String chr=matcher.group();
	    
	    /**
	     * 获取起始位点和终止位点
	     */
	    Pattern patternnumber =Pattern.compile("(?<!\\w)\\d+(?!\\w)", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
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
	    	return"ReadSite染色体位置错误";
	    }
    	return ChrStringHash.getSeq( cisseq,chr.toLowerCase(), location[0], location[1]);
  }
 
		/**
		 * 在读取chr长度文件后，可以通过此获得每条chr的长度
		 * @param chrID 内部自动转换为小写
		 * @return
		 */
		public static long getChrLength(String chrID) 
		{
			return ChrStringHash.getHashChrLength().get(chrID.toLowerCase());
		}
 
		

		
	
	
}
