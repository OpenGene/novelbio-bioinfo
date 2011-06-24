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

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;




/**
 * 本类用来将读取fasta文本，返回Hash表。key-序列名-小写，value-序列信息
 * 一个类就是一个fasta文件
 * 作者：宗杰 20090617
 */

public class SeqFastaHash {
	
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  
	
	/**
	 * 将序列信息读入哈希表并返回
	 * 哈希表的键是序列名，根据情况不改变大小写或改为小写
	 * 哈希表的值是序列，其中无空格
	 */
	public Hashtable<String,SeqFasta> hashSeq;
	
	/**
	 * 将序列名称按顺序读入list
	 */
	public ArrayList<String> lsSeqName;
	
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
	public Hashtable<String,SeqFasta> readfile(String seqfilename,boolean CaseChange, String regx,boolean append) throws Exception
 {
		Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		Matcher matcher;// matcher.groupCount() 返回此匹配器模式中的捕获组数。
		hashSeq = new Hashtable<String, SeqFasta>();// 本list用来存储染色体
		TxtReadandWrite txtSeqFile = new TxtReadandWrite();
		txtSeqFile.setParameter(seqfilename, false, true);
		StringBuilder SeqStringBuilder = new StringBuilder();
		String content = "";
		BufferedReader reader = txtSeqFile.readfile();// open gff file
		SeqFasta Seq = null;
		lsSeqName = new ArrayList<String>();
		while ((content = reader.readLine()) != null) {
			if (content.trim().startsWith(">"))// 当读到一条序列时，给序列起名字
			{
				if (Seq != null) {
					putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
					SeqStringBuilder = new StringBuilder();// 清空
				}
				Seq = new SeqFasta();
				String tmpSeqName = "";
				// //////////////是否改变序列名字的大小写//////////////////////////////////////////////
				if (CaseChange)
					tmpSeqName = content.trim().substring(1).trim()
							.toLowerCase();// substring(1)，去掉>符号，然后统统改成小写
				else
					tmpSeqName = content.trim().substring(1).trim();// substring(1)，去掉>符号，不变大小写
				// ///////////////用正则表达式抓取序列名中的特定字符////////////////////////////////////////////////
				if (regx.trim().equals("")) {
					Seq.setSeqName(tmpSeqName);
				} else {
					matcher = pattern.matcher(tmpSeqName);
					if (matcher.find()) {
						Seq.setSeqName(matcher.group());
					} else {
						System.out.println("没找到该序列的特定名称，用全称代替 " + tmpSeqName);
						Seq.setSeqName(tmpSeqName);
					}
				}
				continue;
			}
			SeqStringBuilder.append(content.replace(" ", ""));
		}
		// /////////离开循环后，再做一次总结/////////////////////
		Seq.setSeq(SeqStringBuilder.toString());
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
		return hashSeq;
	}

	/**
	 *  如果没有同名序列，直接装入hash表
	 *  对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 *  连续向后加上"<"直到hash中没有这条名字为止，然后装入hash表
	 * @param seqFasta
	 * @param seq
	 * @param append
	 */
	private void putSeqFastaInHash(SeqFasta seqFasta, String seq, boolean append) {
		seqFasta.setSeq(seq);
		SeqFasta tmpSeq = hashSeq.get(seqFasta.getSeqName());// 看是否有同名的序列出现
		// 如果没有同名序列，直接装入hash表
		if (tmpSeq == null) {
			hashSeq.put(seqFasta.getSeqName(), seqFasta);
			lsSeqName.add(seqFasta.getSeqName());
		} else {// 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
			if (append)
			 { //连续向后加上"<"直到hash中没有这条名字为止，然后装入hash表
				 while (hashSeq.containsKey(seqFasta.getSeqName()))
				 {
					 seqFasta.setSeqName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
			 }
			 else 
			 {
				if (tmpSeq.getSeq().length()<seqFasta.getSeq().length()) 
				{
					hashSeq.put(seqFasta.getSeqName(), seqFasta);
					//因为已经有了同名的序列，所以 lsSeqName 中不需要添加新的名字
				}
			}
		 }
	}
	/**
	 * 输入序列信息：序列名,正反向
	 * 返回序列
	 * @param SeqID 序列名称
	 * @param chr 序列参数之序列名，用来在哈希表中查找具体某条序列
	 * @param cisseq序列正反向，蛋白序列就输true
	 */
	public String getsequence(String SeqID,boolean cisseq) 
	{
	    if (cisseq)
	    {
	    	return hashSeq.get(SeqID).getSeq();
	    }
	    else 
	    {
		  return hashSeq.get(SeqID).getSeqRC();	
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
	public String getsequence(String SeqID, int startlocation, int endlocation,boolean cisseq) 
	{
		SeqFasta targetChr=hashSeq.get(SeqID);
		if (targetChr == null) {
			logger.error("没有该序列 " +SeqID);
			return "没有该序列 " +SeqID;
		}
		return targetChr.getsequence(startlocation, endlocation, cisseq);
	}

	/**
	 * 输入序列名
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	public String getsequence(String seqID, int startlocation, int endlocation) 
	{ 
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("没有该序列 " +seqID);
			return "没有该序列 " +seqID;
		}
		return targetChr.getsequence(startlocation, endlocation);
	}
}

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);  
	protected SeqFasta() {
		SetCompMap();
	}
	/**
	 * 获得互补配对hash表
	 */
	private HashMap<Character,Character> getCompMap() {
		SetCompMap();
		return compmap;
	}
	
	/**
	 * 设定序列名
	 */
	public void setSeqName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/**
	 * 获得序列名
	 */
	public String getSeqName() {
		return SeqName;
	}
	
	/**
	 * 设定序列
	 */
	protected void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	
	/**
	 * 获得具体序列
	 */
	public String getSeq() {
		return SeqSequence;
	}
	/**
	 * 获得具体序列的反向互补序列
	 */
	public String getSeqRC() {
		return reservecomplement(getSeq());
	}
	/**
	 * 反向互补哈希表
	 */
	private static HashMap<Character, Character> compmap = null;// 碱基翻译哈希表

	/**
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
	 */
	private static void SetCompMap() {
		if (compmap != null) {
			return;
		}
		compmap = new HashMap<Character, Character>();// 碱基翻译哈希表
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
	 * 输入序列坐标信息：序列名-序列起点和终点 返回序列
	 * 
	 * @param hashSeq
	 *            序列的哈希表，键为序列名称，值为具体序列
	 * @param chr
	 *            序列参数之序列名，用来在哈希表中查找具体某条序列
	 * @param startlocation
	 *            序列起点
	 * @param endlocation
	 *            序列终点
	 * @param cisseq序列正反向
	 *            ，蛋白序列就输true
	 */
	public String getsequence(int startlocation,
			int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		if (cisseq) {
			return sequence;
		} else {
			return reservecomplement(sequence);
		}
	}

	/**
	 * 输入序列坐标，起点和终点 返回序列
	 */
	public String getsequence(int startlocation, int endlocation) {
		/**
		 * 如果位点超过了范围，那么修正位点
		 */
		int length = SeqSequence.length();
		if (startlocation < 1 || startlocation >= length || endlocation < 1
				|| endlocation >= length) {
			logger.error("序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation);
			return "序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation;
		}

		if (endlocation <= startlocation) {
			logger.error("序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation);
			return "序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation;
		}
		
		if (endlocation - startlocation > 20000) {
			logger.error("最多提取20000bp "+SeqName+" "+startlocation+" "+endlocation);
			return "最多提取20000bp"+SeqName+" "+startlocation+" "+endlocation;
		}
		return SeqSequence.substring(startlocation - 1, endlocation);// substring方法返回找到的序列
	}

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	private String reservecomplement(String sequence) {
		if (compmap == null) {
			SetCompMap();
		}

		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				logger.error(SeqName + " 含有未知碱基 " + sequence.charAt(i));
				return SeqName + "含有未知碱基 " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

}



