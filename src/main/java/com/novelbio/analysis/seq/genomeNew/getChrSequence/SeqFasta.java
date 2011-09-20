package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);
	private boolean cis5to3 = true;
	
	/**
	 * 当用指定序列来插入或替换本序列中的位置时，如果插入的位置并不是很确定
	 * 譬如插入一段序列到 10-20上去，但是是否精确插入到10并不清楚，那么该区域再加上一段XXX用以标记
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	public SeqFasta(String seqName, String SeqSequence)
	{
		getCompMap();
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3)
	{
		getCompMap();
		this.SeqName = seqName;
		
		this.cis5to3 = cis5to3;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecom(SeqSequence);
		}
	}
	
	protected SeqFasta() {
		getCompMap();
	}
	/**
	 * 本序列在生成的时候是正向还是反向，实际没有影响
	 * @return
	 */
	public boolean isCis5to3() {
		return cis5to3;
	};
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
	 * 获得互补配对hash表
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
	 */
	public static HashMap<Character, Character> getCompMap() {
		if (compmap != null) {
			return compmap;
		}
		compmap = new HashMap<Character, Character>();// 碱基翻译哈希表
		compmap.put(Character.valueOf('A'), Character.valueOf('T'));
		compmap.put(Character.valueOf('a'), Character.valueOf('t'));
		compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		compmap.put(Character.valueOf('t'), Character.valueOf('a'));
		compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		compmap.put(Character.valueOf('g'), Character.valueOf('c'));
		compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		compmap.put(Character.valueOf('c'), Character.valueOf('g'));
		compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		compmap.put(Character.valueOf('n'), Character.valueOf('n'));
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
		compmap.put(Character.valueOf('X'), Character.valueOf('X'));
		return compmap;
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
			getCompMap();
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

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public static String reservecom(String sequence) {
		if (compmap == null) {
			getCompMap();
		}
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				return "第"+ i+ "位含有未知碱基: " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}
	
	/**
	 * 待测试
	 * 指定范围，然后用指定的序列去替换原来的序列
	 * @param start 要替换序列的起点，实际位点,并且包含该位点
	 * @param end 要替换序列的终点，实际位点,并且包含该位点，<br>
	 * 如果end<0，说明是插入紧挨着start位点之后<br>
	 * 如果 start == end 那么就是将该点替换成指定序列<br>
	 * 如果 start > end && end >0 说明出错
	 * @param seq 要替换的序列
	 * @param boostart 替换序列的前部是否有问题
	 * @param booend 替换序列的后部是否有问题
	 */
	public void modifySeq(int start, int end, String seq,boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		if (!boostart) {
			startSeq = SEP_SEQ;
		}
		if (!booend) {
			endSeq = SEP_SEQ;
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
		else if (end < 0){
			end = start;
		}
		else if (start > end && end > 0) {//插入的序列横跨了，这个在外面处理
			logger.error("start < end: "+ start + " "+ end);
		}
		
		String FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	
	/**
	 * 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列
	 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = chrSeq.toString();
		SeqSequence = FinalSeq;
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
	/**
	 * 直接返回序列
	 */
	public String toString()
	{
		return SeqSequence;
		
	}
	/**
	 * 统计序列中小写序列，N的数量以及X的数量等
	 */
	public ArrayList<LocInfo> getSeqInfo()
	{
		//string0: flag string1: location string2:endLoc
		ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
		
		
		char[] seq = SeqSequence.toCharArray();
		boolean flagBound = false; //边界模糊标记，XX
		boolean flagGap = false; //gap标记，小写
		boolean flagAmbitious = false; //不确定碱基标记，NNN
		int bound = 0; int gap = 0; int ambitious = 0;
		int startBound = 0; int startGap = 0; int startAmbitious = 0;
		for (int i = 0; i < seq.length; i++) {
			if (seq[i] < 'a' && seq[i] != 'X' && seq[i] != 'N') {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					bound ++;
				}
				else {
					flagBound = true;
					bound = 0;
					startBound = i;
				}
			} 
			else if (seq[i] == 'N') {
				if (flagAmbitious) {
					ambitious ++;
				}
				else {
					flagAmbitious = true;
					ambitious = 0;
					startAmbitious = i;
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; // gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // 边界模糊标记，XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					gap ++;
				}
				else {
					flagGap = true;
					gap = 0;
					startGap = i;
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
		}
		return lsResult;
	}
	/**
	 * 
	 * @param lsInfo
	 * @param info
	 * @param start 内部会加上1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
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
	
	
	
	
	
	
}
