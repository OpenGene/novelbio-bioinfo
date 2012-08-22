package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类用来将读取fasta文本，返回Hash表。key-序列名-小写，value-序列信息
 * 将序列名中的空格全部换为下划线
 * 一个类就是一个fasta文件
 * 作者：宗杰 20090617
 */

public class SeqFastaHash extends SeqHashAbs {
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  
	Boolean TOLOWCASE = null;
	/**
	 * 将序列信息读入哈希表并返回<br>
	 * 哈希表的键是序列名，根据情况不改变大小写或改为小写<br>
	 * 哈希表的值是序列，其中无空格<br>
	 */
	public HashMap<String,SeqFasta> hashSeq;
	
	boolean append = false;
	/**
	 * @param chrFile
	 * @param regx 序列名的正则表达式，null不设定
	 * @param CaseChange 是否将序列名改为小写，默认为true
	 * @param append 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 * false：如果出现重名序列，则用长的序列去替换短的序列，默认为false
	 */
	public SeqFastaHash(String chrFile) {
		super(chrFile, "", true);
		setFile();
	}
	/**
	 * @param chrFile
	 * @param regx 序列名的正则表达式，null不设定
	 * @param CaseChange 是否将序列名改为小写，默认为true
	 * @param append 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 * false：如果出现重名序列，则用长的序列去替换短的序列，默认为false
	 */
	public SeqFastaHash(String chrFile, String regx, boolean CaseChange,
			boolean append) {
		super(chrFile, regx, CaseChange);
		this.append = append;
		setFile();
	}
	/**
	 * @param chrFile
	 * @param regx 序列名的正则表达式，null不设定
	 * @param CaseChange 是否将序列名改为小写，默认为true
	 * @param append 对于相同名称序列的处理，true：如果出现重名序列，则在第二条名字后加上"<"作为标记
	 * @param TOLOWCASE  是否将序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * false：如果出现重名序列，则用长的序列去替换短的序列，默认为false
	 */
	public SeqFastaHash(String chrFile, String regx, boolean CaseChange,
			boolean append,Boolean TOLOWCASE) {
		super(chrFile, regx, CaseChange);
		this.append = append;
		this.TOLOWCASE = TOLOWCASE;
		setFile();
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
	protected void setChrFile() throws Exception {
		Pattern pattern = null;
		if (regx == null) {
			pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE); // flags
		}
		else {
			pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		}
		
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
				tmpSeqName = content.trim().substring(1).trim();
				tmpSeqName = getChrIDisLowCase(tmpSeqName);
				
				// ///////////////用正则表达式抓取序列名中的特定字符////////////////////////////////////////////////
				if (regx == null || regx.trim().equals("")) {
					Seq.setName(tmpSeqName);
				} else {
					matcher = pattern.matcher(tmpSeqName);
					if (matcher.find()) {
						Seq.setName(matcher.group());
					} else {
						System.out.println("没找到该序列的特定名称，用全称代替 " + tmpSeqName);
						Seq.setName(tmpSeqName);
					}
				}
				continue;
			}
			SeqStringBuilder.append(content.replace(" ", ""));
		}
		// /////////离开循环后，再做一次总结/////////////////////
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
		txtSeqFile.close();
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
		if (TOLOWCASE != null) {
			seq = (TOLOWCASE == true ? seq.toLowerCase() : seq.toUpperCase());
		}
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
				 while (hashSeq.containsKey(seqFasta.getSeqName())) {
					 seqFasta.setName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
				 hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
			 }
			 else {
				if (tmpSeq.Length()<seqFasta.Length()) 
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
	public String getSeqAll(String SeqID,boolean cisseq) {
		SeqID = getChrIDisLowCase(SeqID);
		if (hashSeq.containsKey(SeqID)) {
			if (cisseq) {
				return hashSeq.get(SeqID).toString();
			} else {
				return hashSeq.get(SeqID).reservecom().toString();
			}
		}
	   return null;
	}
	/**
	 * 输入序列名
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	protected SeqFasta getSeqInfo(String seqID, long startlocation, long endlocation) throws IOException {
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("没有该序列 " +seqID);
			return null;
		}
		return targetChr.getSubSeq((int)startlocation, (int)endlocation, true);
	}
	/**
	 * 输入序列名，自动转变为小写
	 * 输入序列坐标，起点和终点
	 * 返回序列
	 */
	public SeqFasta getSeqFasta(String seqID) {
		seqID = getChrIDisLowCase(seqID);
		SeqFasta seqFasta = hashSeq.get(seqID);
		if (seqFasta == null) {
			logger.error("没有该ID：" + seqID);
			return null;
		}
		seqFasta.setDNA(isDNAseq);
		return seqFasta;
	}
	/**
	 * 返回全部序列
	 */
	public ArrayList<SeqFasta>  getSeqFastaAll() {
		ArrayList<SeqFasta> lsresult = new ArrayList<SeqFasta>();
		for (SeqFasta seqFasta : hashSeq.values()) {
			seqFasta.setDNA(isDNAseq);
			lsresult.add(seqFasta);
		}
		return lsresult;
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
	public void writeFileSep(String filePath, String prix, int[] len, boolean sepFile, int writelen) {
		filePath = FileOperate.addSep(filePath);
		TxtReadandWrite txtResultSeqName = new TxtReadandWrite(filePath + prix + "seqName.txt", true);
		TxtReadandWrite txtReadandWrite = null;
		if (!sepFile) {
			txtReadandWrite = new TxtReadandWrite(filePath + prix + ".fasta", true);
			txtResultSeqName.writefileln(txtReadandWrite.getFileName());
		}
		for (Entry<String, SeqFasta> entry : hashSeq.entrySet()) {
			SeqFasta seqFasta = entry.getValue();
			if (SeqHash.testSeqLen(seqFasta.toString().length(), len)) {//长度在目标范围内
				if (sepFile) {
					TxtReadandWrite txtReadandWrite2 = new TxtReadandWrite(filePath + prix + seqFasta.getSeqName().replace(" ", "_")+".fasta", true);
					txtReadandWrite2.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite2.writefilePerLine(seqFasta.toString(), writelen);
					txtResultSeqName.writefileln(txtReadandWrite2.getFileName());
					txtReadandWrite2.close();
				}
				else {
					txtReadandWrite.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite.writefilePerLine(seqFasta.toString(), writelen);
					txtReadandWrite.writefileln("");
				}
			}
		}
		if (!sepFile) {
			txtReadandWrite.close();
		}
		txtResultSeqName.close();
	}
	
	public void writeToFile(String seqOut) {
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			txtOut.writefileln(seqFasta.toStringNRfasta(50));
		}
		txtOut.close();
	}
	/**
	 * 将<b>序列名</b>含有该正则表达式的序列写入文件<br>
	 * 必须写上正则表达式
	 * @param regx
	 * @param seqOut
	 */
	public void writeToFile(String regx, String seqOut) {
		PatternOperate patternOperate = new PatternOperate(regx, false);
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			ArrayList<String> lsName = patternOperate.getPat(seqFasta.getSeqName());
			if (lsName != null && lsName.size() > 0) {
				txtOut.writefileln(seqFasta.toStringNRfasta());
			}
		}
		txtOut.close();
	}
	/**
	 * 将<b>序列名</b>含有该正则表达式的序列写入文件<br>
	 * 必须写上正则表达式
	 * 例如序列名为：hsa-mir-101-1 MI0000103 Homo sapiens miR-101-1 stem-loop <br>
	 * regSearch = Homo sapiens<br>
	 * regWrite = hsa-mir-101-1<br>
	 * 最后就会获得hsa-mir-101-1<br>
	 * @param regxSearch 用该正则表达式查找序列名
	 * @param regxWrite 找到后将序列名字设置为该正则表达式抓到的信息
	 * @param seqOut
	 */
	public void writeToFile(String regxSearch, String regxWrite, String seqOut) {
		PatternOperate patSearch = new PatternOperate(regxSearch, false);
		PatternOperate patWrite = new PatternOperate(regxWrite, false);

		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			ArrayList<String> lsName = patSearch.getPat(seqFasta.getSeqName());
			if (lsName != null && lsName.size() > 0) {
				SeqFasta seqFastaNew = seqFasta.clone();
				String name = patWrite.getPatFirst(seqFasta.getSeqName());
				if (name != null && name.equals("")) {
					seqFastaNew.setName(name);
				}
				txtOut.writefileln(seqFasta.toStringNRfasta());
			}
		}
		txtOut.close();
	}
	/**
	 * 将<b>序列名用sep分割</b>然后将第几位的名字写入文件<br>
	 * 必须写上正则表达式
	 * @param sep 分隔符 
	 * @param num 第几位的文本
	 */
	public void writeToFile(String sep, int num, String seqOut) {
		num--;
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			SeqFasta seqFastaOut = seqFasta.clone();
			String name = seqFasta.getSeqName().split(sep)[num];
			seqFastaOut.setName(name);
			seqFastaOut.setDNA(true);
			txtOut.writefileln(seqFastaOut.toStringNRfasta());
		}
		txtOut.close();
	}
	@Override
	public Iterable<Character> readBase(String refID) {
		refID = getChrIDisLowCase(refID);
		SeqFasta seqFasta = hashSeq.get(refID);
		return seqFasta.readBase();
	}
}

