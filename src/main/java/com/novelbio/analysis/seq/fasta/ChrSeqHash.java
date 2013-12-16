package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hg.doc.ch;
import com.novelbio.analysis.seq.fasta.RandomChrFileInt.RandomChrFileFactory;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类用来将染色体的名字，序列装入染色体类，并且是以Hash表形式返回 目前本类中仅仅含有静态方法 同时用来提取某段位置的序列 和提取反向重复序列
 * 作者：宗杰 20090617
 */
public class ChrSeqHash extends SeqHashAbs {
	private static Logger logger = Logger.getLogger(ChrSeqHash.class);
	
	/** 以下哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10 */
	Map<String, Long> mapChrID2Start = new LinkedHashMap<>();
	RandomChrFileInt randomChrFileInt;
	
	/** 每个文本所对应的单行长度
	 *  Seq文件第二行的长度，也就是每行序列的长度+1，1是回车 
	 *  现在是假设Seq文件第一行都是>ChrID,第二行开始都是Seq序列信息
	 *  并且每一行的序列都等长<br>
	 *  <b>key小写</b>
	 */
	Map<String, Integer> mapChrID2LenRow = new LinkedHashMap<>();
	/** 行中内容加上换行符和空格等<br>
	 * <b>key小写</b>
	 */
	Map<String, Integer> mapChrID2LenRowEnter = new LinkedHashMap<>();
	
	int maxExtractSeqLength = 2000000;
	
	/** 独立文本的数量不能超过1000，不超过就在开始的时候初始化RandomFile类，超过就在提取序列时初始化 */
	int maxSeqNum = 500;
	/**
	 * 随机硬盘读取染色体文件的方法，貌似很伤硬盘，考虑用固态硬盘 注意
	 * @param chrFileName 染色体文件路径，单个文件
	 * @param regx null和""：选择序列全名<br>
	 *  输入 " "表示选择第一个空格前的序列名，如">chr1 ater ddddd" 则截取”chr1“
	 * @param CaseChange 是否将序列名转化为小写，一般转为小写
	 */
	public ChrSeqHash(String chrFileName,String regx) {
		super(chrFileName, regx);
		setFile();
	}
	/** 设定最长读取的sequence长度 */
	public void setMaxExtractSeqLength(int maxExtractSeqLength) {
		this.maxExtractSeqLength = maxExtractSeqLength;
	}
	
	/**
	 * 设定序列文件
	 * @throws FileNotFoundException 
	 */
	protected void setChrFile() throws Exception {
		String indexFile = null;
		if (FileOperate.isFileExist(getChrIndexFileName()) &&
				FileOperate.getTimeLastModify(getChrIndexFileName()) > FileOperate.getTimeLastModify(chrFile)) {
			indexFile = getChrIndexFileName();
		} else if (FileOperate.isFileExist(getChrIndexFileNameFaidx()) &&
				FileOperate.getTimeLastModify(getChrIndexFileNameFaidx()) > FileOperate.getTimeLastModify(chrFile)) {
			indexFile = getChrIndexFileNameFaidx();
		} else {
			createIndex(chrFile, getChrIndexFileName());
			indexFile = getChrIndexFileName();
		}
		readIndex(indexFile);
		randomChrFileInt = RandomChrFileFactory.createInstance(chrFile);
	}
	
	protected SeqFasta getSeqInfo(String chrID, long startlocation, long endlocation) {
		try {
			return getSeqInfoExp(chrID, startlocation, endlocation);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 给定chrID,chrID会自动转换为小写，和读取的起点以及终点，返回读取的序列
	 * @param chrID
	 * @param startlocation 从第几个碱基开始读取，从1开始记数，注意234的话，实际为从234开始读取，类似substring方法 long
	 * 小于0表示从头开始读取
	 * @param endlocation 读到第几个碱基，从1开始记数，实际读到第endNum个碱基。 快速提取序列
	 * 小于0表示读到末尾
	 * @return
	 * @throws IOException
	 */
	private SeqFasta getSeqInfoExp(String chrID, long startlocation, long endlocation) throws IOException {
		if (randomChrFileInt == null) {
			logger.error("没有该文件：" + chrFile);
			return null;
		}
		
		chrID = chrID.toLowerCase();
		long[] startEndReal = getStartEndReal(chrID, startlocation, endlocation);
		if (startEndReal == null) return null;
		long startReal = startEndReal[0]; long endReal = startEndReal[1];
		
		if (endReal > FileOperate.getFileSizeLong(chrFile)) {
			logger.error("文件出错");
			return null;
		}
		
		byte[] readInfo = new byte[(int) (endReal - startReal)];
		randomChrFileInt.seek(startReal);
		randomChrFileInt.read(readInfo);
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName(chrID + "_" + startlocation + "_" + endlocation);
		StringBuilder sequence = new StringBuilder();
		for (byte b : readInfo) {
			if ((int)b <= 0) {
				logger.error("error: " + chrID + " " + startlocation + " " + endlocation );
				return null;
			}
			char seq = (char)b;
			if (seq == '\r' || seq == '\n' || seq == ' ') {
				continue;
			} else if (seq == '>') {
				logger.error("error: " + chrID + " " + startlocation + " " + endlocation );
				return null;
			}
			sequence.append(seq);
		}
		seqFasta.setSeq(sequence.toString());
		return seqFasta;
	}

	/**
	 * 修正输入的start和end的坐标为随机文本的实际坐标
	 * @return null表示失败
	 * @throws IOException 
	 */
	private long[] getStartEndReal(String chrID, long start, long end) {
		String chrIDLowcase = chrID.toLowerCase();
		if (!mapChrID2Length.containsKey(chrIDLowcase)) {
			logger.error( "无该染色体: "+ chrID);
			return null;
		}
		long chrLength = getChrLength(chrIDLowcase);
		if (start <= 0) start = 1;
		if (end <= 0 || end > chrLength) end = chrLength;
		
		start--;
		//如果位点超过了范围，那么修正位点
		if (start < 0 || start >= chrLength || end < 1 || end < start) {
			logger.error(chrIDLowcase + " " + start + " " + end + " 染色体坐标错误");
			return null;
		}
		if (end - start > maxExtractSeqLength) {
			logger.error(chrIDLowcase + " " + start + " " + end + " 最多提取" + maxExtractSeqLength + "bp");
			return null;
		}
		
		long startChr = mapChrID2Start.get(chrIDLowcase);
		int lengthRow = mapChrID2LenRow.get(chrIDLowcase);
		int lenRowEnter = mapChrID2LenRowEnter.get(chrIDLowcase);
		try {
			long startReal = getRealSite(startChr, start, lengthRow, lenRowEnter);
			long endReal = getRealSite(startChr, end, lengthRow, lenRowEnter);
			return new long[]{startReal, endReal};
		} catch (Exception e) {
			logger.error("文件出错：" + chrFile + "\t" + chrIDLowcase + " " + start + " " + end, e);
			throw new RuntimeException("extract seq error: " + chrFile + "\t" + chrIDLowcase + " " + start + " " + end, e);
		}
	}
	
	@Override
	public Iterable<Character> readBase(String refID) {
		final String myRefID = refID.toLowerCase();
		return new Iterable<Character>() {
			@Override
			public Iterator<Character> iterator() {
				IteratorBase iteratorBase = new IteratorBase();
				TxtReadandWrite txtRead = new TxtReadandWrite("");
				//TODO 
				iteratorBase.setReader(txtRead);
				return iteratorBase;
			}
		};
	}
	
	/**
	 * @param chrStart 该染色体起点在文本中的位置
	 * @param site 想转换的某个染色体坐标
	 * @param rowLen 每行有多少碱基
	 * @return
	 * @throws IOException 
	 */
	private long getRealSite(long chrStart, long site, int rowLen, int rowEnterLen) throws IOException {
		// 设定到0位
		randomChrFileInt.seek(0);
		// 实际序列在文件中的起点
		long siteReal = chrStart + rowEnterLen * getRowNum(site, rowLen) + getBias(site, rowLen);
		return siteReal;
	}
	
	/** 指定染色体的某个位点，返回该位点的偏移，也就是在某一行的第几个碱基 */
	private int getBias(long site, int rowLen) {
		return (int) (site % rowLen);
	}
	
	private long getRowNum(long site, int rowLen) {
		return site / rowLen;
	}
	
	public void close() {
		try {
			randomChrFileInt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * 建索引
	 * index 文件格式如下
	 * chrID chrLength start   rowLength rowLenWithEnter
	 * @param chrFile 染色体序列，必须每一行等长
	 * @param indexFile 输出的index文件夹
	 * @throws IOException
	 */
	public static void createIndex(String chrFile, String indexFile) throws IOException {
		Map<String, Long> mapChrID2Start = new LinkedHashMap<>();
		Map<String, Long> mapChrID2Length = new LinkedHashMap<>();
		Map<String, Integer> mapChrID2LenRow = new LinkedHashMap<>();
		Map<String, Integer> mapChrID2LenRowEnter = new LinkedHashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(chrFile);
		long start = 0, length = 0;
		int enterLen = -1;
		String chrID = null;
		for (String content : txtRead.readlines()) {
			if (enterLen < 0) {
				enterLen = getEnterLen(txtRead.getBufferedReader(), content.length());
			}
			
			start = start + content.length() + enterLen;
			length = length + content.length();
			if (content.startsWith(">")) {
				if (chrID != null) {
					length = length - content.length();
					mapChrID2Length.put(chrID, length);
				}
				chrID = content.split(" ")[0].replace(">", "").toLowerCase();
				mapChrID2Start.put(chrID, start);
				length = 0;
			} else if (!mapChrID2LenRow.containsKey(chrID)) {
				enterLen = getEnterLen(txtRead.getBufferedReader(), content.length());
				mapChrID2LenRowEnter.put(chrID, content.length() + enterLen);
				mapChrID2LenRow.put(chrID, content.trim().length());
			}
		}
		if (chrID != null) {
			mapChrID2Length.put(chrID, length);
		}
		TxtReadandWrite txtIndex = new TxtReadandWrite(indexFile, true);
		for (String chr : mapChrID2LenRow.keySet()) {
			String[] out = new String[]{chr + "\t" + mapChrID2Length.get(chr) + "\t" + mapChrID2Start.get(chr)
					+ "\t" + mapChrID2LenRow.get(chr) + "\t" + mapChrID2LenRowEnter.get(chr)};
			txtIndex.writefileln(out);
		}
		txtIndex.close();
		txtRead.close();
	}
	
	/** 给定 bfReader，返回换行的格式 
	 * @throws IOException */
	private static int getEnterLen(BufferedReader bfreader, int contentLen) throws IOException {
		bfreader.mark(contentLen * 2);
		int lineByteNum = bfreader.readLine().length();
		bfreader.reset();
		char[] mychar = new char[lineByteNum + 2];
		bfreader.read(mychar);
		bfreader.reset();
		if (mychar[mychar.length - 2] == 13 && mychar[mychar.length - 1] == 10) {
			return 2;//windows 的 \r\n
		} else {
			return 1;
		}
	}
	
	/** index 文件格式如下
	 * chrID chrLength start    rowLength rowLenWithEnter
	 * @throws IOException 
	 */
	private void readIndex(String indexFile) {
		lsSeqName = new ArrayList<>();
		mapChrID2Start.clear();
		mapChrID2Length.clear();
		mapChrID2LenRow.clear();
		mapChrID2LenRowEnter.clear();
		PatternOperate patternOperate = null;
		if (regx != null && !regx.equals("") && !regx.equals(" ")) {
			patternOperate = new PatternOperate(regx, false);
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String chrID = null;
			if (regx != null && regx.equals(" ")) {
				chrID = ss[0].split(" ")[0];
			} else if (patternOperate != null) {
				chrID = patternOperate.getPatFirst(ss[0]);
				if (chrID == null) {
					chrID = ss[0];
				}
			} else {
				chrID = ss[0];
			}
			lsSeqName.add(chrID);
			String chrIDlowcase = chrID.toLowerCase();
			long length = Long.parseLong(ss[1].trim());
			long start = Long.parseLong(ss[2].trim());
			int lenRow = Integer.parseInt(ss[3].trim());
			int lenRowEnter = Integer.parseInt(ss[4].trim());
			mapChrID2LenRow.put(chrIDlowcase, lenRow);
			mapChrID2Start.put(chrIDlowcase, start);
			mapChrID2Length.put(chrIDlowcase, length);
			mapChrID2LenRowEnter.put(chrIDlowcase, lenRowEnter);
		}
		txtRead.close();
	}
	
	private String getChrIndexFileName() {
		return chrFile + ".index";
	}
	private String getChrIndexFileNameFaidx() {
		return chrFile + ".fai";
	}
}
