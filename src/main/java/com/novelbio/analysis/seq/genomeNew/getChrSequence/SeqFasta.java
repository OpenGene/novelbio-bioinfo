package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);  
	protected SeqFasta() {
		getCompMap();
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
	 * 获得互补配对hash表
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
	 */
	public static HashMap<Character, Character> getCompMap() {
		if (compmap != null) {
			return compmap;
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
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
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
	
}
