package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.SeekablePathInputStream;

/**
 * 本类用来给定染色体，然后顺序返回碱基，用iterator来返回
 * @author zongjie 20171021
 */
public class ChrBaseIter implements Closeable {
	private static Logger logger = Logger.getLogger(ChrBaseIter.class);
	
	/** 抓取chrID的正则表达式 */
	String regx = null;
	String chrFile = "";

	/** 以下哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10 */
	Map<String, Long> mapChrID2Start = new LinkedHashMap<>();
	SeekablePathInputStream seekablePathInputStream;
	
	/** 保存chrID和chrLength的对应关系<br>
	 * <b>key小写</b>
	 */
	LinkedHashMap<String, Long> mapChrID2Length = new LinkedHashMap<String, Long>();
	/** 每个文本所对应的单行长度
	 *  Seq文件第二行的长度，也就是每行序列的长度+1，1是回车
	 * 现在是假设Seq文件第一行都是>ChrID,第二行开始都是Seq序列信息
	 * 并且每一行的序列都等长<br>
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
	public ChrBaseIter(String chrFileName,String regx) {
		this.chrFile = chrFileName;
		if (regx != null && !regx.equals("")) {
			this.regx = regx;
		}
		try {
			setChrFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 随机硬盘读取染色体文件的方法，貌似很伤硬盘，考虑用固态硬盘 注意
	 * @param chrFileName 染色体文件路径，单个文件
	 *  " "选择第一个空格前的序列名，如">chr1 ater ddddd" 则截取”chr1“
	 * @param CaseChange 是否将序列名转化为小写，一般转为小写
	 */
	public ChrBaseIter(String chrFileName) {
		this(chrFileName, " ");
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
		if (FileOperate.isFileExistAndNotDir(getChrIndexFileName()) &&
				FileOperate.getTimeLastModify(getChrIndexFileName()) > FileOperate.getTimeLastModify(chrFile)) {
			indexFile = getChrIndexFileName();
		} else {
			SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
			samIndexRefsequence.setRefsequence(chrFile);
			indexFile = samIndexRefsequence.indexSequence();
		}
		readIndex(indexFile);
		seekablePathInputStream = FileOperate.getInputStreamSeekable(chrFile);
	}
	
	public Iterable<Base> readBase(String chrId) {
		return readBase(chrId, -1L, -1L);
	}
	public Iterable<Base> readBase(String chrId, int start) {
		return readBase(chrId, start, -1L);
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
	public Iterable<Base> readBase(String chrId, long startlocation, long endlocation) {
		if (seekablePathInputStream == null) {
			throw new ExceptionSeqFasta("no file exist: " + chrFile);
		}
		String chrIDLowcase = chrId.toLowerCase();
		if (!mapChrID2Length.containsKey(chrIDLowcase)) {
			throw new ExceptionNbcFile("cannot find chrId " + chrId + " in chr file " + chrFile);
		}
		long chrLength = mapChrID2Length.get(chrIDLowcase);
		
		if (startlocation <= 0) startlocation = 1;
		if (endlocation <= 0 || endlocation > chrLength) endlocation = chrLength;
		long startLoc = startlocation;
		long endLoc = endlocation;
		

		long[] startEndReal = getStartEndReal(chrIDLowcase, startLoc, endLoc, chrLength);
		if (startEndReal == null) return null;
		long startReal = startEndReal[0]; long endReal = startEndReal[1];
		try {
			seekablePathInputStream.seek(startReal);
		} catch (Exception e) {
			throw new ExceptionSeqFasta("read file error " + chrFile);
		}
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(seekablePathInputStream));
		int basesPerLine = mapChrID2LenRow.get(chrIDLowcase);
		int terminatorLength = mapChrID2LenRowEnter.get(chrIDLowcase) - basesPerLine;
		int lineNum = getBias(startLoc, mapChrID2LenRow.get(chrIDLowcase));
		
		//when the lineNum is 0, it means at the end of the line.
		if (lineNum == 0) lineNum = basesPerLine;
		
		int[] lineNums = new int[]{lineNum};
		int[] byteNums = new int[]{1};
		boolean[] isBases = new boolean[]{true};
		int[] numAlls = new int[]{0};
		int[] position = new int[]{(int)startLoc};

		return new Iterable<Base>() {
			public Iterator<Base> iterator() {
				return new Iterator<Base>() {
					Base base = getNext();

					public boolean hasNext() {
						return base != null;
					}
					public Base next() {
						Base retval = base;
						base = getNext();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					Base getNext() {
						if (numAlls[0] >= endReal - startReal) {
							return null;
						}
						
						while(true) {
							int c = 0;
							try {
								c = bufferedReader.read();
							} catch (Exception e) {
								throw new ExceptionNbcFile("read chr file error " + chrFile);
							}
							numAlls[0]++;
							if (isBases[0]) {
								if (c <= 0) {
									logger.error("error: " + chrId + " " + startLoc + " " + endLoc );
									return null;
								}
								base = new Base();
								char baseUnit = (char)c;
								base.setBase((char)c);
								if (baseUnit == '\r' || baseUnit == '\n' || baseUnit == ' ') {
									throw new ExceptionSeqFasta("error find enter symbol: " + chrFile + "\t" + chrId + " " + startLoc + " " + endLoc);
								}
								base.setPosition(position[0]);
								base.setRefId(chrId);
								if (lineNums[0] == basesPerLine) {
									lineNums[0] = 1;
									isBases[0] = false;
								} else {
									lineNums[0]++;
								}
								position[0]++;

								break;
							} else {
								if (byteNums[0] == terminatorLength) {
									byteNums[0] = 1;
									isBases[0] = true;
								} else {
									byteNums[0]++;
								}
							}
						}
						return base;
					}
				};
			}
		};
	}

	/**
	 * 修正输入的start和end的坐标为随机文本的实际坐标
	 * @return null表示失败
	 * @throws IOException 
	 */
	private long[] getStartEndReal(String chrID, long start, long end, long chrLength) {
		if (start <= 0) start = 1;
		if (end <= 0 || end > chrLength) end = chrLength;
		
		start = start - 1;
		//如果位点超过了范围，那么修正位点
		if (start < 0 || start >= chrLength || end < 1 || end < start) {
			throw new ExceptionSeqFasta(chrID + " " + start + " " + end + " chromosome location error");
		}
		
		String chrIDLowcase = chrID.toLowerCase();
		if (!mapChrID2Length.containsKey(chrIDLowcase)) {
			throw new ExceptionSeqFasta( "no chromosome ID: "+ chrID);
		}
				
		long startChr = mapChrID2Start.get(chrIDLowcase);
		int lengthRow = mapChrID2LenRow.get(chrIDLowcase);
		int lenRowEnter = mapChrID2LenRowEnter.get(chrIDLowcase);
		try {
			long startReal = getRealSiteStart(startChr, start, lengthRow, lenRowEnter);
			long endReal = getRealSiteEnd(startChr, end, lengthRow, lenRowEnter);
			return new long[]{startReal, endReal};
		} catch (Exception e) {
			throw new RuntimeException("extract seq error: " + chrFile + "\t" + chrIDLowcase + " " + start + " " + end, e);
		}
	}
	
	
	/**
	 * @param chrStart 该染色体起点在文本中的位置
	 * @param site 想转换的某个染色体坐标
	 * @param rowLen 每行有多少碱基
	 * @return
	 * @throws IOException 
	 */
	private long getRealSiteStart(long chrStart, long site, int rowLen, int rowEnterLen) throws IOException {
		// 实际序列在文件中的起点
		long siteReal = chrStart + rowEnterLen * getRowNum(site, rowLen) + getBias(site, rowLen);
		return siteReal;
	}
	/**
	 * @param chrStart 该染色体起点在文本中的位置
	 * @param site 想转换的某个染色体坐标
	 * @param rowLen 每行有多少碱基
	 * @return
	 * @throws IOException 
	 */
	private long getRealSiteEnd(long chrStart, long site, int rowLen, int rowEnterLen) throws IOException {
		// 实际序列在文件中的起点
		int bias = getBias(site, rowLen);
		if (bias == 0 && site > 0) {
			bias = rowLen - rowEnterLen;
		}
		long siteReal = chrStart + rowEnterLen * getRowNum(site, rowLen) + bias;
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
			seekablePathInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 建索引，本方法有错误
	 * index 文件格式如下
	 * chrID chrLength start   rowLength rowLenWithEnter
	 * @param chrFile 染色体序列，必须每一行等长
	 * @param indexFile 输出的index文件夹
	 * @throws IOException
	 */
	private static void createIndex(String chrFile, String indexFile) throws IOException {
		//TODO 本方法有错误
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
				chrID = content.split(" ")[0].replace(">", "");
				mapChrID2Start.put(chrID, start);
				try {
					enterLen = getEnterLen(txtRead.getBufferedReader(), content.length());
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(chrID);
				}
				length = 0;
			} else if (!mapChrID2LenRow.containsKey(chrID)) {
				
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
		bfreader.mark(10000);
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
		if (regx != null && !regx.equals("") && !regx.equals(" ")) {
			patternOperate = new PatternOperate(regx, false);
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String chrID = null;
			if (" ".equals(regx)) {
				chrID = ss[0].split(" ")[0];
			} else if (patternOperate != null) {
				chrID = patternOperate.getPatFirst(ss[0]);
				if (chrID == null) {
					chrID = ss[0];
				}
			} else {
				chrID = ss[0];
			}
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
		return chrFile + ".fai";
	}
	
	/** 专门用于排序比较的类 */
	public static class CompareChrID implements Comparator<String> {
		PatternOperate patChrID = new PatternOperate("\\d+", false);
		@Override
		public int compare(String o1, String o2) {
			String chrID1 = patChrID.getPatFirst(o1);
			String chrID2 = patChrID.getPatFirst(o2);

			if (chrID1 == null && chrID2 == null) {
				return o1.compareTo(o2);
			}
			
			if (chrID1 == null || chrID2 == null) {	
				if (chrID1 != null) {
					return -1;
				} else {
					return 1;
				}
			}
			Long chr1 = Long.parseLong(chrID1);
			Long chr2 = Long.parseLong(chrID2);
			return chr1.compareTo(chr2);
		}
		
	}
}
