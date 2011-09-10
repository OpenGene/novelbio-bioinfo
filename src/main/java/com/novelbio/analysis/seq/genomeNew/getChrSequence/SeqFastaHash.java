package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;




/**
 * 本类用来将读取fasta文本，返回Hash表。key-序列名-小写，value-序列信息
 * 将序列名中的空格全部换为下划线
 * 一个类就是一个fasta文件
 * 作者：宗杰 20090617
 */

public class SeqFastaHash extends SeqHashAbs {
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  

	
	public ArrayList<String> getLsSeqName() {
		return lsSeqName;
	}
	/**
	 * 将序列信息读入哈希表并返回<br>
	 * 哈希表的键是序列名，根据情况不改变大小写或改为小写<br>
	 * 哈希表的值是序列，其中无空格<br>
	 */
	public HashMap<String,SeqFasta> hashSeq;
	
	/**
	 * 将序列名称按顺序读入list
	 */
	public ArrayList<String> lsSeqName;
	
	boolean append;
	public void setInfo(boolean append) {
		this.append = append;
	}

	/**
	 * 读取序列文件，将序列保存入Seqhash哈希表<br/>
	 * 读取完毕后，生成<br/>
	 * 一个listSeqName是序列名字List<br/>
	 * 一个Seqhash是序列名--序列HashTable<br/>
	 * 同时本函数返回一个同样的哈希表
	 * @param chrFile
	 * @param CaseChange 序列名是否要改变大小写,true都改为小写，false不改大小写
	 * @param regx 需要提取的fasta格式序列名的正则表达式，""为全部名字。如果没抓到，则将全部名称作为序列名
	 * @param append 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 * false：如果出现重名序列，则用长的序列去替换短的序列
	 * @return
	 * @throws Exception 
	 */
	protected void setChrFile() throws Exception
	{
		Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		Matcher matcher;// matcher.groupCount() 返回此匹配器模式中的捕获组数。
		hashSeq = new HashMap<String, SeqFasta>();// 本list用来存储染色体
		TxtReadandWrite txtSeqFile = new TxtReadandWrite(chrFile,false);
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
							.toLowerCase().replace(" ", "_");// substring(1)，去掉>符号，然后统统改成小写
				else
					tmpSeqName = content.trim().substring(1).trim().replace(" ", "_");// substring(1)，去掉>符号，不变大小写
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
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
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
			hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
		} else {// 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
			if (append)
			 { //连续向后加上"<"直到hash中没有这条名字为止，然后装入hash表
				 while (hashSeq.containsKey(seqFasta.getSeqName()))
				 {
					 seqFasta.setSeqName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
				 hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
			 }
			 else 
			 {
				if (tmpSeq.getSeq().length()<seqFasta.getSeq().length()) 
				{
					hashSeq.put(seqFasta.getSeqName(), seqFasta);
					hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
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
	 * 如果没有序列则返回null
	 */
	public String getSeqAll(String SeqID,boolean cisseq) 
	{
		if (hashSeq.containsKey(SeqID)) {
			if (cisseq) {
				return hashSeq.get(SeqID).getSeq().toLowerCase();
			} else {
				return hashSeq.get(SeqID).getSeqRC();
			}
		}
	   return null;
	}
	
	/**
	 * 输入序列名
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	protected String getSeqInfo(String seqID, long startlocation, long endlocation) throws IOException 
	{ 
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("没有该序列 " +seqID);
			return "没有该序列 " +seqID;
		}
		return targetChr.getsequence((int)startlocation, (int)endlocation);
	}
	/**
	 * 输入序列名
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	public SeqFasta getSeqFasta(String seqID) 
	{ 
		return hashSeq.get(seqID);
	}
	/**
	 * 返回全部序列
	 */
	public ArrayList<SeqFasta>  getSeqFastaAll()
	{
		ArrayList<SeqFasta> lsresult = new ArrayList<SeqFasta>();
		for (SeqFasta seqFasta : hashSeq.values()) {
			lsresult.add(seqFasta);
		}
		return lsresult;
	}
	
	/**
	 * 比较两个序列是否一致，计数不一致的碱基数
	 * 从头开始比较，可以有空格
	 */
	public static int compare2Seq(String seq1, String seq2) {
		char[] chrSeq1 = seq1.trim().toLowerCase().toCharArray();
		char[] chrSeq2 = seq2.trim().toLowerCase().toCharArray();
		int result = 0;
		int i = Math.min(chrSeq1.length, chrSeq2.length);
		for (int j = 0; j < i; j++) {
			if (chrSeq1[j] != chrSeq2[j]) {
				result ++ ;
			}
		}
		result = result + Math.max(chrSeq1.length, chrSeq2.length) - i;
		return result;
	}
	
	/**
	 * 将指定长度的序列写入文本，主要用于做lastz分析,后缀名通通改为.fasta
	 * @param filePath 写入文件路径
	 * @param prix 文件前缀
	 * @param len seq的长度区间
	 * int[2] :0：下限，小于0表示没有下限
	 * 1：上限，小于0表示没有上限
	 * 上限必须大于等于下限，如果上限小于下限，则报错
	 * @param sepFile 是否分为不同文件保存
	 * @param writelen
	 */
	public void writeFileSep(String filePath, String prix, int[] len, boolean sepFile, int writelen)
	{
		filePath = FileOperate.addSep(filePath);
		TxtReadandWrite txtResultSeqName = new TxtReadandWrite(filePath + prix + "seqName.txt", true);
		TxtReadandWrite txtReadandWrite = null;
		if (!sepFile) {
			txtReadandWrite = new TxtReadandWrite(filePath + prix + ".fasta", true);
			txtResultSeqName.writefileln(txtReadandWrite.getFileName());
		}

		for (Entry<String, SeqFasta> entry : hashSeq.entrySet()) {
			String seqName = entry.getKey();
			SeqFasta seqFasta = entry.getValue();
			if (testSeqLen(seqFasta.getSeq().length(), len))//长度在目标范围内
			{
				if (sepFile) {
					
					TxtReadandWrite txtReadandWrite2 = new TxtReadandWrite(filePath + prix + seqFasta.getSeqName().replace(" ", "_")+".fasta", true);
					txtReadandWrite2.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite2.writefilePerLine(seqFasta.getSeq(), writelen);
					txtResultSeqName.writefileln(txtReadandWrite2.getFileName());
					txtReadandWrite2.close();
				}
				else {
					txtReadandWrite.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite.writefilePerLine(seqFasta.getSeq(), writelen);
					txtReadandWrite.writefileln("");
				}
			}
		}
		if (!sepFile) {
			txtReadandWrite.close();
		}
		
		txtResultSeqName.close();
	}
	
	/**
	 * 判断输入的长度是否在目的区间内，闭区间
	 * @param seqlen 
	 * @param len
	 * 	int[2] :0：下限，小于0表示没有下限
	 * 1：上限，小于0表示没有上限
	 * 上限必须大于等于下限，如果上限小于下限，则报错
	 * @return
	 */
	public static boolean testSeqLen(int seqlen, int[] len) {
		
		if (len[1] > 0 && len[1] < len[0]) {
			logger.error("要求输出序列的长度上限不能小于下限");
		}
		
		if (len[0] <= 0) { //无下限
			if (len[1] <= 0)  //无上限
				return true;
			else { //有上限
				if (seqlen <= len[1])  //长度小于等于上限
					return true;
				else
					// 长度大于上限
					return false;
			}
		}
		else // 有下限
		{
			if (seqlen < len[0]) //长度小于下限
				return false;
			else {  //长度大于下限
				if (len[1] > 0) { //有上限
					if (seqlen <= len[1]) //长度小于上限
						return true;
					else
						return false;
				}
				else {
					return true;
				}
			}
		}
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

