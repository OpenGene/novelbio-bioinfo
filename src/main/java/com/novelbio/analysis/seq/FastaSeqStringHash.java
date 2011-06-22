package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;




/**
 * 本类用来将读取fasta文本，返回Hash表。key-序列名-小写，value-序列信息
 * 目前本类中仅仅含有静态方法
 * 作者：宗杰 20090617
 */

public class FastaSeqStringHash {

	/**
	 * 将序列信息读入哈希表并返回
	 * 哈希表的键是序列名，根据情况不改变大小写或改为小写
	 * 哈希表的值是序列，其中无空格
	 */
	public static Hashtable<String,SeqInfo> hashSeq;
	
	/**
	 * 将序列名称按顺序读入list
	 */
	public static ArrayList<String> lsSeqName;
	
	/**
	 * 反向互补哈希表 
	 */
	private static HashMap<Character, Character> compmap=new HashMap<Character, Character>();//碱基翻译哈希表

	 /**
	  * 给碱基对照哈希表赋值
	  * 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应）
	  * 将来可能要添加新的 
	  */
	private static void SetCompMap() 
	{
		 compmap.put(Character.valueOf('A'), Character.valueOf('T'));
		 compmap.put(Character.valueOf('a'), Character.valueOf('T'));
		 compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		 compmap.put(Character.valueOf('t'), Character.valueOf('A'));
		 compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		 compmap.put(Character.valueOf('g'), Character.valueOf('C'));
		 compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		 compmap.put(Character.valueOf('c'), Character.valueOf('G'));
		 compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		 compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		 compmap.put(Character.valueOf('n'), Character.valueOf('N'));	
	}
	
	
	
	/**
	 * 读取序列文件，将序列保存入Seqhash哈希表<br/>
	 * 读取完毕后，生成<br/>
	 * 一个listSeqName是序列名字List<br/>
	 * 一个Seqhash是序列名--序列HashTable<br/>
	 * 同时本函数返回一个同样的哈希表
	 * @param seqfilename
	 * @param CaseChange 序列名是否要改变大小写,true都改为小写，false不改大小写
	 * @param regx 需要提取的fasta格式序列名的正则表达式，""为全部名字。如果没抓到，则将全部名称作为序列名
	 * @param append 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 * false：如果出现重名序列，则用长的序列去替换短的序列
	 * @return
	 * @throws Exception 
	 */
	public static Hashtable<String,SeqInfo> readfile(String seqfilename,boolean CaseChange, String regx,boolean append) throws Exception
	{
		Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
	    
		
		 hashSeq=new Hashtable<String,SeqInfo>();//本list用来存储染色体
		 TxtReadandWrite txtSeqFile=new TxtReadandWrite();
		 txtSeqFile.setParameter(seqfilename, false, true);
	     StringBuilder SeqStringBuilder=new StringBuilder();
	     String content="";
	     BufferedReader reader=txtSeqFile.readfile();//open gff file
	     SeqInfo Seq=null;
	     String tmpSeqID="";
	     lsSeqName=new ArrayList<String>();
	     while ((content=reader.readLine())!=null)
	     {
	    	 if(content.trim().startsWith(">"))//当读到一条序列时，给序列起名字
	    	 {
	    		 if (Seq!=null)
	    		 {
	    			 Seq.SeqSequence=SeqStringBuilder.toString();
	    			 SeqStringBuilder=new StringBuilder();//清空
	    			 SeqInfo tmpSeq=hashSeq.get(Seq.SeqName);//看是否有同名的序列出现
	    			 //如果没有同名序列，直接装入hash表
	    			 if (tmpSeq==null)
	    			 {
	    				 hashSeq.put(Seq.SeqName, Seq);
	    				 lsSeqName.add(Seq.SeqName);
	    			 }
	    			 else
	    			 {//对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	    				 if (append) 
	    				 { //连续向后加上"<"直到hash中没有这条名字为止，然后装入hash表
	    					 while (hashSeq.containsKey(Seq.SeqName))
	    					 {
	 							Seq.SeqName=Seq.SeqName+"<";
	    					 }
	    					 hashSeq.put(Seq.SeqName, Seq);
	    					 lsSeqName.add(Seq.SeqName);
	    				 }
	    				 else 
	    				 {
							if (tmpSeq.SeqSequence.length()<Seq.SeqSequence.length()) 
							{
								hashSeq.put(Seq.SeqName, Seq);
								//因为已经有了同名的序列，所以 lsSeqName 中不需要添加新的名字
							}
							else 
								continue;
						}
	    			 }
	    		 }
	    		 Seq=new SeqInfo();
	    		 String tmpSeqName="";
	    		
	    		 ////////////////是否改变序列名字的大小写//////////////////////////////////////////////
	    		 if(CaseChange)
	    			 tmpSeqName=content.trim().substring(1).trim().toLowerCase();//substring(1)，去掉>符号，然后统统改成小写
	    		 else 
	    			 tmpSeqName=content.trim().substring(1).trim();//substring(1)，去掉>符号，不变大小写
	    		 /////////////////用正则表达式抓取序列名中的特定字符////////////////////////////////////////////////
	    		 if (regx.trim().equals("")) {
	    			 Seq.SeqName=tmpSeqName;
	    		 }
	    		 else {
	    			 matcher=pattern.matcher(tmpSeqName);
	    			 if (matcher.find()) {
	    				 Seq.SeqName=matcher.group();
	    			 }
	    			 else {
	    				 System.out.println(tmpSeqName);
	    				 Seq.SeqName=tmpSeqName;
	    			 }
	    		 }
	    		 
				continue;
		   	}
			SeqStringBuilder.append(content.replace(" ",""));
		  }
	     ///////////离开循环后，再做一次总结/////////////////////
		 Seq.SeqSequence=SeqStringBuilder.toString();
		 SeqInfo tmpSeq=hashSeq.get(Seq.SeqName);//看是否有同名的序列出现
		 //如果没有同名序列，直接装入hash表
		 if (tmpSeq==null)
		 {
			 hashSeq.put(Seq.SeqName, Seq);
			 lsSeqName.add(Seq.SeqName);
		 }
		 else
		 {//对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
			 if (append) 
			 { //连续向后加上"<"直到hash中没有这条名字为止，然后装入hash表
				 while (hashSeq.containsKey(Seq.SeqName))
				 {
						Seq.SeqName=Seq.SeqName+"<";
				 }
				 hashSeq.put(Seq.SeqName, Seq);
				 lsSeqName.add(Seq.SeqName);
			 }
			 else 
			 {
				if (tmpSeq.SeqSequence.length()<Seq.SeqSequence.length()) 
				{
					hashSeq.put(Seq.SeqName, Seq);
					//因为已经有了同名的序列，所以 lsSeqName 中不需要添加新的名字
				}
			}
		 }
	return hashSeq;
	}	
	

	/**
	 * 输入序列信息：序列名,正反向
	 * 返回序列
	 * @param SeqID 序列名称
	 * @param chr 序列参数之序列名，用来在哈希表中查找具体某条序列
	 * @param cisseq序列正反向，蛋白序列就输true
	 */
	public static String getsequence(String SeqID,boolean cisseq) 
	{
		String sequence=hashSeq.get(SeqID).SeqSequence;
	    if (cisseq)
	    {
	    	return sequence;
	    }
	    else 
	    {
		  return reservecomplement(sequence);	
		}
	}
	
	/**
	 * 输入序列坐标信息：序列名-序列起点和终点
	 * 返回序列
	 * @param hashSeq 序列的哈希表，键为序列名称，值为具体序列
	 * @param chr 序列参数之序列名，用来在哈希表中查找具体某条序列
	 * @param startlocation 序列起点
	 * @param endlocation 序列终点
	 * @param cisseq序列正反向，蛋白序列就输true
	 */
	public static String getsequence(String SeqID, int startlocation, int endlocation,boolean cisseq) 
	{
		String sequence=getsequence(SeqID, startlocation, endlocation);
	    if (cisseq)
	    {
	    	return sequence;
	    }
	    else 
	    {
		  return reservecomplement(sequence);	
		}
	}

	
	/**
	 * 输入序列名
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	public static String getsequence(String seqID, int startlocation, int endlocation) 
	{ 
		SeqInfo targetChr=hashSeq.get(seqID);
        if (targetChr==null)
        	{
        	return "底层序列格式错误或者无该序列";
        	}
	   /**
	    * 如果位点超过了范围，那么修正位点
	    */
        int length=targetChr.SeqSequence.length();
	   if (startlocation<1||startlocation>=length||endlocation<1||endlocation>=length)
	   {
		   return "序列坐标错误";
	   }
	   
	   if(endlocation<=startlocation)
	   {
		   return "坐标错误";
	   }
	   if(endlocation-startlocation>20000)
	   {
		   return "最多提取20000bp";
	   }

	 return   targetChr.SeqSequence.substring(startlocation-1, endlocation);//substring方法返回找到的序列
	}
	
	/**
	 * 输入序列，互补对照表
	 * 获得反向互补序列
	 */
	public static String reservecomplement(String sequence)
	{
		if(compmap==null)
    	{
    		SetCompMap();
    	}
		
		StringBuilder recomseq=new StringBuilder();
		int length=sequence.length();
		Character base;
		for(int i=length-1;i>=0;i--)
		{
			base=compmap.get(sequence.charAt(i));
			if (base!=null)
			{
			recomseq.append(compmap.get(sequence.charAt(i)));
			}
			else 
			{
			 return "含有未知碱基 "+	sequence.charAt(i);
			}
		}	
		return recomseq.toString();
	}	
}



