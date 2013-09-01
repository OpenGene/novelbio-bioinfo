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
	 *  并且每一行的序列都等长
	 */
	Map<String, Integer> mapChrID2LenRow = new LinkedHashMap<>();
	/** 行中内容加上换行符和空格等 */
	Map<String, Integer> mapChrID2LenRowEnter = new LinkedHashMap<>();
	
	int maxExtractSeqLength = 2000000;
	
	/** 独立文本的数量不能超过1000，不超过就在开始的时候初始化RandomFile类，超过就在提取序列时初始化 */
	int maxSeqNum = 500;
	/**
	 * 随机硬盘读取染色体文件的方法，貌似很伤硬盘，考虑用固态硬盘 注意
	 * 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 一个文本保存一条染色体，以fasta格式保存，每个文本以">"开头，然后接下来每行固定的碱基数(如UCSC为50个，TIGRRice为60个)
	 * 文本文件名(不考虑后缀名，当然没有后缀名也行)应该是待查找的chrID
	 * @param chrFilePath
	 * @param regx null走默认，默认为"\\bchr\\w*"， 用该正则表达式去查找文件名中含有Chr的文件，每一个文件就认为是一个染色体
	 * @param CaseChange 是否将序列名转化为小写，一般转为小写
	 */
	public ChrSeqHash(String chrFilePath,String regx) {
		super(chrFilePath, regx);
		setFile();
	}
	
	/** 设定最长读取的sequence长度 */
	public void setMaxExtractSeqLength(int maxExtractSeqLength) {
		this.maxExtractSeqLength = maxExtractSeqLength;
	}
	
	/**
	 * 设定序列文件夹
	 * @throws FileNotFoundException 
	 */
	protected void setChrFile() throws Exception {
		String indexFile = null;
		if (FileOperate.isFileExist(getChrIndexFileName()) &&
				FileOperate.getLastModifyTime(getChrIndexFileName()) > FileOperate.getLastModifyTime(chrFile)) {
			indexFile = getChrIndexFileName();
		} else if (FileOperate.isFileExist(getChrIndexFileNameFaidx()) &&
				FileOperate.getLastModifyTime(getChrIndexFileNameFaidx()) > FileOperate.getLastModifyTime(chrFile)) {
			indexFile = getChrIndexFileNameFaidx();
		} else {
			createIndex();
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
		chrID = chrID.toLowerCase();
		if (!mapChrID2Length.containsKey(chrID)) {
			logger.error( "无该染色体: "+ chrID);
			return null;
		}
		long chrLength = getChrLength(chrID);
		if (start <= 0) start = 1;
		if (end <= 0) end = chrLength;
		
		start--;
		//如果位点超过了范围，那么修正位点
		if (start < 0 || start >= chrLength || end < 1 || end >= chrLength || end < start) {
			logger.error(chrID + " " + start + " " + end + " 染色体坐标错误");
			return null;
		}
		if (end - start > maxExtractSeqLength) {
			logger.error(chrID + " " + start + " " + end + " 最多提取" + maxExtractSeqLength + "bp");
			return null;
		}
		
		long startChr = mapChrID2Start.get(chrID);
		int lengthRow = mapChrID2LenRow.get(chrID);
		int lenRowEnter = mapChrID2LenRowEnter.get(chrID);
		try {
			long startReal = getRealSite(startChr, start, lengthRow, lenRowEnter);
			long endReal = getRealSite(startChr, end, lengthRow, lenRowEnter);
			return new long[]{startReal, endReal};
		} catch (Exception e) {
			logger.error("文件出错：" + chrFile + "\t" + chrID + " " + start + " " + end);
			return null;
		}
	}
	
	@Override
	public Iterable<Character> readBase(String refID) {
		final String myRefID = refID.toLowerCase();
		return new Iterable<Character>() {
			@Override
			public Iterator<Character> iterator() {
				IteratorBase iteratorBase = new IteratorBase();
				TxtReadandWrite txtRead = new TxtReadandWrite(mapChrID2FileName.get(myRefID));
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
	
	/** index 文件格式如下
	 * chrID chrLength start   rowLength rowLenWithEnter
	 * @throws IOException 
	 */
	private void createIndex() throws IOException {
		mapChrID2Start.clear();
		mapChrID2Length.clear();
		mapChrID2LenRow.clear();
		mapChrID2LenRowEnter.clear();
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
				chrID = content.replace(">", "").toLowerCase();
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
		TxtReadandWrite txtIndex = new TxtReadandWrite(getChrIndexFileName(), true);
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
	private int getEnterLen(BufferedReader bfreader, int contentLen) throws IOException {
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
		mapChrID2Start.clear();
		mapChrID2Length.clear();
		mapChrID2LenRow.clear();
		mapChrID2LenRowEnter.clear();
		PatternOperate patternOperate = null;
		if (regx != null && !regx.equals("")) {
			patternOperate = new PatternOperate(regx, false);
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String chrID =ss[0].toLowerCase();
			if (patternOperate != null) {
				chrID = patternOperate.getPatFirst(ss[0]);
				if (chrID != null) {
					chrID = chrID.toLowerCase();
				} else {
					chrID = ss[0].toLowerCase();
				}
			}
			long length = Long.parseLong(ss[1].trim());
			long start = Long.parseLong(ss[2].trim());
			int lenRow = Integer.parseInt(ss[3].trim());
			int lenRowEnter = Integer.parseInt(ss[4].trim());
			mapChrID2LenRow.put(chrID, lenRow);
			mapChrID2Start.put(chrID, start);
			mapChrID2Length.put(chrID, length);
			mapChrID2LenRowEnter.put(chrID, lenRowEnter);
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
