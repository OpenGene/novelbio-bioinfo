package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
public class SeqFasta implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(SeqFasta.class);

	public static final int SEQ_UNKNOWN = 128;
	public static final int SEQ_PRO = 256;
	public static final int SEQ_DNA = 512;
	public static final int SEQ_RNA = 1024;

	/**
	 * 当用指定序列来插入或替换本序列中的位置时，如果插入的位置并不是很确定
	 * 譬如插入一段序列到 10-20上去，但是是否精确插入到10并不清楚，那么该区域再加上一段XXX用以标记
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	protected String SeqName;
	protected String SeqSequence;
	StringBuilder stringBuilder;
	/**
	 * 结果的文件是否转化为大小写 True：小写 False：大写 null：不变
	 * @return
	 */
	protected Boolean TOLOWCASE = null;
	/** 默认返回三字母长度的氨基酸 */
	boolean AA3Len = true;
	
	public SeqFasta() { }
	
	public SeqFasta(String seqName, String SeqSequence) {
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String SeqSequence) {
		this.SeqSequence = SeqSequence;
	}
	/**
	 * @param seqName
	 * @param SeqSequence
	 * @param cis5to3 仅仅标记一下，并不会反向序列
	 */
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3) {
		this.SeqName = seqName;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecomplement(SeqSequence);
		}
	}
	
	/**
	 * 默认返回三字母长度的氨基酸
	 */
	public void setAA3Len(boolean aA3Len) {
		AA3Len = aA3Len;
	}
	public void setTOLOWCASE(Boolean TOLOWCASE) {
		this.TOLOWCASE = TOLOWCASE;
	}
	/**
	 * 将RNA序列转化为DNA，也就是将U替换为T
	 */
	public void setDNA(boolean isDNAseq) {
		if (isDNAseq) {
			SeqSequence = SeqSequence.replace('u', 't').replace('U', 'T');
		}
	}
	/** 设定序列名 */
	public void setName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/** 获得序列名 */
	public String getSeqName() {
		return SeqName;
	}
	/** 设定序列 */
	public void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	/**
	 * nr序列的长度
	 * @return
	 */
	public int Length() {
		if (SeqSequence == null) {
			return 0;
		}
		return SeqSequence.length();
	}
	/**
	 * 给定左右的坐标，然后将seqfasta截短
	 * @param start 和substring一样的用法
	 * @param end 和substring一样的用法
	 * @return 返回截短后的string
	 */
	public SeqFasta trimSeq(int start, int end) {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqName = SeqName;
		seqFasta.TOLOWCASE = TOLOWCASE;
		if (SeqSequence == null) {
			seqFasta.SeqSequence = SeqSequence;
			return seqFasta;
		}
		seqFasta.SeqSequence = SeqSequence.substring(start, end);
		return seqFasta;
	}
	/**
	 * 输入序列坐标信息：序列名-序列起点和终点 返回序列
	 * @param startlocation 序列起点 <b>注意起点从1开始</b>，跟string的subString不一样
	 * @param endlocation 序列终点
	 * @param cisseq序列正反向，蛋白序列就输true
	 */
	public SeqFasta getSubSeq(int startlocation, int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		SeqFasta seqFasta = new SeqFasta(SeqName+"_" + startlocation + "_" + endlocation, sequence, cisseq);
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.AA3Len = AA3Len;
		return seqFasta;
	}

	/**
	 * 
	 * 输入序列坐标，起点和终点 返回序列
	 * 如果位点超过了范围，那么修正位点
	 * @param startlocation <b>注意起点从1开始</b>，跟string的subString不一样
	 * @param endlocation
	 * @return
	 */
	private String getsequence(int startlocation, int endlocation) {
		int length = SeqSequence.length();
		if (startlocation < 1) {
			throw new ExceptionSeqFasta("startLocation should start from 1, but is: " + startlocation);
		}
		if (startlocation < 1 || startlocation > length || endlocation < 1
				|| endlocation > length) {
			logger.error("location error "+SeqName+" "+startlocation+" "+endlocation);
			return "location error "+SeqName+" "+startlocation+" "+endlocation;
		}

		if (endlocation < startlocation) {
			logger.error("location error "+SeqName+" "+startlocation+" "+endlocation);
			return "location error "+SeqName+" "+startlocation+" "+endlocation;
		}
		
		if (endlocation - startlocation > 1000000) {
			logger.error("can extract less than 20000bp "+SeqName+" "+startlocation+" "+endlocation);
			return "can extract less than 20000bp "+SeqName+" "+startlocation+" "+endlocation;
		}
		return SeqSequence.substring(startlocation - 1, endlocation);// substring方法返回找到的序列
	}

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 * 出错则返回null;
	 */
	private String reservecomplement(String sequence) {
		String[] revSeq = CodeInfo.reverseComplement(sequence);
		if (revSeq[1] != null) {
			logger.error(SeqName + " 含有未知碱基 " + revSeq[1] + " " + sequence.toCharArray()[Integer.parseInt(revSeq[1])]);
		}
		return revSeq[0];
	}
	/** 输入序列，互补对照表 获得反向互补序列 */
	public static String reverseComplement(String sequence) {
		return CodeInfo.revComplement(sequence);
	}
	/** 输入序列，互补对照表 获得互补序列，一般用来做miRNA结合计算自由能 */
	public static String complement(String sequence) {
		return CodeInfo.complement(sequence);
	}
	/** 输入序列，互补对照表 获得反向序列，一般用来颠倒FastQ文件的quality序列 */
	public static String reverse(String sequence) {
		return CodeInfo.reverse(sequence);
	}
	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 * 其中SeqName不变，cis5to3反向，序列反向互补
	 */
	public SeqFasta reservecom() {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.SeqName = SeqName;
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqSequence = reservecomplement(SeqSequence);
		return seqFasta;
	}
	/**
	 * 指定范围，然后用指定的序列去替换原来的序列
	 * @param start 要替换序列的起点，实际位点,并且包含该位点 如果start<= 0，则不考虑end，直接将序列插到最前面
	 * 如果start比序列长，则不考虑end，直接将序列插到最后面
	 * @param end 要替换序列的终点，实际位点,并且包含该位点，<br>
	 * 如果 start == end 那么就是将该点替换成指定序列<br>
	 * 如果 start > end 说明是插入紧挨着start位点之后<br>
	 * @param seq 要替换的序列
	 */
	public void modifySeq(int start, int end, String seq) {
		modifySeq(start, end, seq, false, false);
	}
	/**
	 * 指定范围，然后用指定的序列去替换原来的序列
	 * @param start 要替换序列的起点，实际位点,并且包含该位点 如果start<= 0，则不考虑end，直接将序列插到最前面
	 * 如果start比序列长，则不考虑end，直接将序列插到最后面
	 * @param end 要替换序列的终点，实际位点,并且包含该位点，<br>
	 * 如果 start == end 那么就是将该点替换成指定序列<br>
	 * 如果 start > end 说明是插入紧挨着start位点之后<br>
	 * @param seq 要替换的序列
	 * @param boostart 替换序列的前部是否插入XXX true：插入
	 * @param booend 替换序列的后部是否插入XXX true：插入
	 */
	public void modifySeq(int start, int end, String seq, boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		String FinalSeq = null;
		if (boostart)
			startSeq = SEP_SEQ;
		if (booend)
			endSeq = SEP_SEQ;

		if (start <= 0) {
			SeqSequence = startSeq + seq.toUpperCase() + endSeq + SeqSequence;
			return;
		}
		if (start >= SeqSequence.length() + 1) {
			SeqSequence = SeqSequence + startSeq + seq.toUpperCase() + endSeq;
			return;
		}
		
		if (start < end) {
			start --;
		}
		else if (start == end) {
			if (seq.length() == 1) {
				modifySeq(start, seq.charAt(0));
				return;
			}
			start --;
		}
		else if (start > end){
			end = start;
		}
		FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	/** 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = String.copyValueOf(chrSeq);
		SeqSequence = FinalSeq;
	}
	
	/** append完了之后务必调用 {@link #appendFinish()} */
	public void appendSeq(char c) {
		if (c == ' ' || c == '\n' || c == '\t') {
			return;
		}
		if (stringBuilder == null) {
			stringBuilder = new StringBuilder();
			if (SeqSequence != null) {
				stringBuilder.append(SeqSequence);
			}
		}
		stringBuilder.append(c);
	}
	
	/** append完了之后务必调用 {@link #appendFinish()} */
	public void appendSeq(String seq) {
		for (char chr : seq.toCharArray()) {
			appendSeq(chr);
		}
	}
	
	/** 结束append */
	public void appendFinish() {
		if (stringBuilder != null) {
			SeqSequence = stringBuilder.toString();
			stringBuilder = null;
		}
	}
	
	/**
	 * 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列
	 */
	public void modifySeq(ArrayList<SoapsnpInfo> lsSoapsnpInfos) {
		char[] chrSeq = SeqSequence.toCharArray();
		for (SoapsnpInfo soapsnpInfo : lsSoapsnpInfos) {
			chrSeq[soapsnpInfo.getStart()-1] = soapsnpInfo.getBestBase();
		}
		String FinalSeq = String.copyValueOf(chrSeq);
		SeqSequence = FinalSeq;
	}
	
	/** 统计序列中小写序列，N的数量以及X的数量等 */
	public StatisticSeqInfo getSeqAssemblyInfo() {
		return new StatisticSeqInfo(this);
	}
	/**@return 将nr序列转变为单字母aa序列，首先正反向之后，然后按照该顺序进行orf选择 */
	public String toStringAA1() {
		return toStringAA(true, 0, true);
	}
	/**
	 * 判定是否为蛋白序列，如果序列中含有超过stopCodeNum个终止密码子，就返回false
	 * @param stopCodeNum 可以容忍的终止密码子个数，不包括最后一个终止密码子<br>
	 * 有些基因譬如 XM_008758589，中间就包含一个终止密码子，但是NCBI依然记录其为蛋白序列，这个设置为1就也可以判定该序列为蛋白序列
	 * @return
	 */
	public boolean isAA(int stopCodeNum) {
		return isAA(true, 0, true, stopCodeNum);
	}
	/**@return 将nr序列转变为三字母aa序列，首先正反向之后，然后按照该顺序进行orf选择 */
	public String toStringAA3() {
		return toStringAA(true, 0, false);
	}
	/**
	 * @param AAnum true 单字母AA，false 三字母AA
	 * @return 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择 
	 */
	public String toStringAA(boolean AAnum) {
		return toStringAA(true, 0, AAnum);
	}
	/**
	 * 将nr序列转变为单字母aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @return
	 */
	public String toStringAA(boolean cis,int orf) {
		return toStringAA(cis, orf, true);
	}
	/**
	 * 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @param AAnum true 单字母AA，false 三字母AA
	 * @return
	 */
	public String toStringAA(boolean cis,int orf, boolean AAnum) {
		if (SeqSequence == null) {
			return "";
		}
		char[] nrChar = null;
		if (!cis) {
			nrChar = reverseComplement(SeqSequence).toCharArray();
		}
		else {
			nrChar = SeqSequence.toCharArray();
		}
		StringBuilder resultAA = new StringBuilder();
		for (int i = orf; i <= nrChar.length - 3; i = i+3) {
			String tmp = String.valueOf(new char[]{nrChar[i],nrChar[i+1],nrChar[i+2]});
			resultAA.append(CodeInfo.convertDNACode2AA(tmp, AAnum));
		}
		return resultAA.toString();
	}
	
	/**
	 * 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @param AAnum true 单字母AA，false 三字母AA
	 * @return
	 */
	public boolean isAA(boolean cis,int orf, boolean AAnum, int stopCodeNum) {
		if (SeqSequence == null) {
			return false;
		}
		char[] nrChar = null;
		if (!cis) {
			nrChar = reverseComplement(SeqSequence).toCharArray();
		}
		else {
			nrChar = SeqSequence.toCharArray();
		}
		int stopNum = 0;
		for (int i = orf; i <= nrChar.length - 6; i = i+3) {
			String tmp = String.valueOf(new char[]{nrChar[i],nrChar[i+1],nrChar[i+2]});
			String aa = CodeInfo.convertDNACode2AA(tmp, AAnum);
			if (aa.contains("*")) {
				stopNum++;
			}
		}
		if (stopNum > stopCodeNum) {
			return false;
		}
		return true;
	}
	
	/**
	 * 进行moti查找
	 * @return
	 */
	public SeqFastaMotifSearch getMotifScan() {
		return new SeqFastaMotifSearch(this);
	}

	/**
	 * 判断该序列是DNA，RNA，还是蛋白，或者也不知道是什么
	 * @return
	 * SeqFasta.SEQ_DNA等
	 */
	public int getSeqType() {
		int len = 2000;
		if (len > Length()) {
			len = Length() - 1;
		}
		char[] chr = SeqSequence.substring(0, len).toCharArray();
		int num = 0;
		boolean flagFindU = false;
		for (char c : chr) {
			if (c == 'u' || c == 'U')
				flagFindU = true;
			
			if (CodeInfo.getCompMap().containsKey(c))
				continue;
			else
				num ++ ;
		}
		if (num == 0) {
			if (flagFindU) return SEQ_RNA;
			
			return SEQ_DNA;
		}
		else if ((double)num/Length() < 0.1) {
			return SEQ_UNKNOWN;
		}
		else {
			return SEQ_PRO;
		}
	}
	
	public FastaGetCDSFromProtein getCDSfromProtein(String proteinSeq) {
		return new FastaGetCDSFromProtein(this, proteinSeq);
	}
	/** 根据TOLOWCASE返回序列 */
	public String toString() {
		if (SeqSequence == null) {
			return "";
		}
		if (TOLOWCASE == null) {
			return SeqSequence;
		}
		else {
			return TOLOWCASE.equals(true) ?  SeqSequence.toLowerCase() :  SeqSequence.toUpperCase();
		}
	}
	/** 返回AA的fasta序列
	 *  每行60个AA
	 *  */
	public String toStringAAfasta() {
		return toStringAAfasta(60);
	}
	/** 如果读取的是nr，则将其翻译为AA的fasta序列并返回
	 * @param basePerLine 每多少个AA换行
	 *  */
	public String toStringAAfasta(int basePerLine) {
		return getMultiLineSeq(SeqName, toStringAA(true, 0), basePerLine);
	}
	/** 返回读取的fasta序列，如果读取的AA序列，返回的也是aa序列
	 * 每隔60个碱基换行
	 */
	public String toStringNRfasta() {
		return toStringNRfasta(60);
	}
	/** 返回Nr的fasta序列
	 * @param basePerLine 每多少个bp换行
	 *  */
	public String toStringNRfasta(int basePerLine) {
		return getMultiLineSeq(SeqName, toString(), basePerLine);
	}
	/** 返回Nr的fasta序列
	 * @param seqName 序列名
	 * @param seq 具体的序列
	 * @param basePerLine 每多少个bp换行
	 *  */
	private String getMultiLineSeq(String seqName, String seq, int basePerLine) {
		String result = ">" + seqName;
		char[] tmpAll = seq.toCharArray();
		char[] tmpLines = new char[basePerLine];
		int m = 0;
		for (int i = 0; i < tmpAll.length; i++) {
			if (m >= basePerLine) {
				String tmpline = String.copyValueOf(tmpLines);
				result = result + TxtReadandWrite.ENTER_LINUX + tmpline;
				tmpLines = new char[basePerLine];
				m = 0;
			}
			tmpLines[m] = tmpAll[i];
			m++;
		}
		/** 最后一个tmpLines也是一个长度为basePerLine的数组，
		 * 但是其实际序列长度可能并没有basePerLine长，所以多出来的地方就会用null填充，那么我们要把这些null删掉，否则会出错
		 */
		char[] tmpFinal = new char[m];
		for (int i = 0; i < tmpFinal.length; i++) {
			tmpFinal[i] = tmpLines[i];
		}
		String tmpline = String.copyValueOf(tmpFinal);
		result = result + TxtReadandWrite.ENTER_LINUX + tmpline;
		return result;
	}
	
	/** 克隆序列 */
	public SeqFasta clone() {
		SeqFasta seqFasta = null;
		try {
			seqFasta = (SeqFasta) super.clone();
			seqFasta.SeqName = SeqName;
			seqFasta.AA3Len = AA3Len;
			seqFasta.SeqSequence = SeqSequence;
			seqFasta.TOLOWCASE = TOLOWCASE;
			return seqFasta;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return seqFasta;
	}
	/** 找到一段序列中最长的转录本等等 */
	public SeqfastaStatisticsCDS statisticsCDS() {
		SeqfastaStatisticsCDS seqfastaStatisticsCDS = new SeqfastaStatisticsCDS(this);
		return seqfastaStatisticsCDS;
	}
	////////////////////// static 方法 //////////////////////////////////////
	/**
	 * 当查找完motif后，获得motif的titile
	 * @return
	 */
	public static String[] getMotifScanTitle() {
		String[] title = new String[]{"SeqName","Strand","MotifSeq","Distance2SeqEnd"};
		return title;
	}
	/**
	 * 比较两个序列是否一致，计数不一致的碱基数
	 * 从头开始比较，头尾可以有空格，中间不能有。不是blast模式的比较
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
	 * 获得氨基酸的特性，极性，电荷等，按照genedoc的分类标准
	 * @return
	 * string[3]: 0：极性--带电荷--负电
	 * 1：
	 */
	public static String[] getAAquality(String AA) {
		return CodeInfo.getAAquality(AA);
	}
	/**
	 * 将氨基酸在单字母和三字母之间转换
	 */
	public static String convertAA(String AA) {
		return CodeInfo.convertAA(AA);
	}
	
	/**
	 * 输入的是DNA三联密码字
	 * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	 * 譬如如果极性不同就返回极性
	 * 格式 polar --> nonpolar等
	 * 都一样则返回"";
	 * @param DNAcode1 第一个DNA编码
	 * @param DNAcode2 第二个DNA编码
	 * @return
	 */
	public static String cmpAAqualityDNA(String DNAcode1, String DNAcode2) {
		return CodeInfo.cmpAAqualityDNA(DNAcode1, DNAcode2);
	}
	
	/**
	 * 输入的是氨基酸，无所谓三字符还是单字符
	 * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	 * 譬如如果极性不同就返回极性
	 * 格式 polar --> nonpolar等
	 * 都一样则返回"";
	 */
	public static String cmpAAquality(String AA1, String AA2) {
		return CodeInfo.cmpAAquality(AA1, AA2);
	}
	
	/** 依次读取碱基 */
	public Iterable<Character> readBase() {
		final char[] seq = toString().toCharArray();
		return new Iterable<Character>() {
			public Iterator<Character> iterator() {
				return new Iterator<Character>() {
					int index = 0;
					Character base = getBase();
					public boolean hasNext() {
						return base != null;
					}
					public Character next() {
						Character retval = base;
						base = getBase();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					Character getBase() {
						if (index >= seq.length) {
							return null;
						}
						Character base = seq[index];
						index++;
						return base;
					}
				};
			}
		};
	}
}
