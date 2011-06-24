package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类用来将染色体的名字，序列装入染色体类，并且是以Hash表形式返回 目前本类中仅仅含有静态方法 同时用来提取某段位置的序列 和提取反向重复序列
 * 作者：宗杰 20090617
 */
public class ChrStringHash {
	private static Logger logger = Logger.getLogger(ChrStringHash.class);
	String Chrpatten = "Chr\\w+";
	/**
	 * 将染色体信息读入哈希表,按照RandomAccessFile保存，并返回 哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10
	 * 哈希表的值是染色体的序列，其中无空格
	 */
	HashMap<String, RandomAccessFile> hashChrSeqFile;

	/**
	 * 将染色体信息读入哈希表,按照BufferedReader保存，并返回 哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10
	 * 哈希表的值是染色体的序列，其中无空格
	 */
	HashMap<String, BufferedReader> hashBufChrSeqFile;

	/**
	 * Seq文件第二行的长度，也就是每行序列的长度+1，1是回车 现在是假设Seq文件第一行都是>ChrID,第二行开始都是Seq序列信息
	 * 并且每一行的序列都等长
	 */
	int lengthRow = 0;

	/**
	 * 保存chrID和chrLength的对应关系
	 */
	Hashtable<String, Long> hashChrLength = new Hashtable<String, Long>();

	/**
	 * 碱基互补配对表
	 */
	private HashMap<Character, Character> compMap;// 碱基翻译哈希表

	/**
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
	 */
	private void compmapFill() {
		compMap = new HashMap<Character, Character>();// 碱基翻译哈希表
		compMap.put(Character.valueOf('A'), Character.valueOf('T'));
		compMap.put(Character.valueOf('a'), Character.valueOf('t'));
		compMap.put(Character.valueOf('T'), Character.valueOf('A'));
		compMap.put(Character.valueOf('t'), Character.valueOf('a'));
		compMap.put(Character.valueOf('G'), Character.valueOf('C'));
		compMap.put(Character.valueOf('g'), Character.valueOf('c'));
		compMap.put(Character.valueOf('C'), Character.valueOf('G'));
		compMap.put(Character.valueOf('c'), Character.valueOf('g'));
		compMap.put(Character.valueOf(' '), Character.valueOf(' '));
		compMap.put(Character.valueOf('N'), Character.valueOf('N'));
		compMap.put(Character.valueOf('n'), Character.valueOf('n'));
	}

	/**
	 * 随机硬盘读取染色体文件的方法 注意
	 * 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 一个文本保存一条染色体，以fasta格式保存，每个文本以">"开头，然后接下来每行固定的碱基数(如UCSC为50个，TIGRRice为60个)
	 * 文本文件名(不考虑后缀名，当然没有后缀名也行)应该是待查找的chrID
	 * 最后本类生成一个Hashtable--chrID(String)---SeqFile(RandomAccessFile)表<br>
	 * 和一个Hashtable--chrID(String)---SeqFile(BufferedReader)表<br>
	 * 其中chrID一直为小写
	 * 
	 * @param chrFilePath
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public ChrStringHash(String chrFilePath) throws Exception {
		if (compMap == null) {
			compmapFill();
		}
		if (!chrFilePath.endsWith(File.separator)) {
			chrFilePath = chrFilePath + File.separator;
		}
		ArrayList<String[]> chrFile = FileOperate.getFoldFileName(chrFilePath,
				"\\bchr\\w*", "*");
		hashChrSeqFile = new HashMap<String, RandomAccessFile>();
		hashBufChrSeqFile = new HashMap<String, BufferedReader>();

		for (int i = 0; i < chrFile.size(); i++) {
			RandomAccessFile chrRAseq = null;
			TxtReadandWrite txtChrTmp = new TxtReadandWrite();
			BufferedReader bufChrSeq = null;
			String[] chrFileName = chrFile.get(i);
			String fileNam = "";

			if (chrFileName[1].equals(""))
				fileNam = chrFilePath + chrFileName[0];
			else
				fileNam = chrFilePath + chrFileName[0] + "." + chrFileName[1];

			chrRAseq = new RandomAccessFile(fileNam, "r");
			txtChrTmp.setParameter(fileNam, false, true);
			bufChrSeq = txtChrTmp.readfile();

			if (i == 0) // 假设每一个文件的每一行Seq都相等
			{
				chrRAseq.seek(0);
				chrRAseq.readLine();
				String seqRow = chrRAseq.readLine();
				lengthRow = seqRow.length();// 每行几个碱基
			}
			hashChrSeqFile.put(chrFileName[0].toLowerCase(), chrRAseq);
			hashBufChrSeqFile.put(chrFileName[0].toLowerCase(), bufChrSeq);
		}
		getChrLength();
	}

	private void getChrLength() throws IOException {
		Iterator iter = hashChrSeqFile.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// 存放最后结果
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			RandomAccessFile chrRAfile = (RandomAccessFile) entry.getValue();
			// 设定到0位
			chrRAfile.seek(0);
			// 获得每条染色体的长度，文件长度-第一行的
			String fastaID = chrRAfile.readLine();
			int lengthChrID = -1;
			if (fastaID.contains(">"))
				lengthChrID = fastaID.length();// 第一行，有>号的长度

			long lengthChrSeq = chrRAfile.length();
			long tmpChrLength = (lengthChrSeq - lengthChrID - 1)
					/ (lengthRow + 1) * lengthRow
					+ (lengthChrSeq - lengthChrID - 1) % (lengthRow + 1);
			hashChrLength.put(chrID, tmpChrLength);
		}
	}

	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * 
	 * @param outFile
	 *            待输出的文件名，带上全部路径
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) {
		Iterator iter = hashChrLength.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// 存放最后结果
		while (iter.hasNext()) {
			String[] tmpResult = new String[2];
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			long lengthChrSeq = (Long) entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtChrLength = new TxtReadandWrite();
		txtChrLength.setParameter(outFile, true, false);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给定chrID,chrID会自动转换为小写，和读取的起点以及终点，返回读取的序列
	 * startNum=204;从第几个碱基开始读取，从1开始记数，注意234的话，实际为从234开始读取，类似substring方法 long
	 * endNum=254;//读到第几个碱基，从1开始记数，实际读到第endNum个碱基。 快速提取序列
	 * 
	 * @throws IOException
	 */
	private String getSeq(String chrID, long startlocation, long endlocation)
			throws IOException {
		startlocation--;
		RandomAccessFile chrRASeqFile = hashChrSeqFile.get(chrID.toLowerCase());// 判断文件是否存在
		if (chrRASeqFile == null) {
			return "ChrStringHash.getSeq: 底层染色体格式错误或者无该染色体";
		}
		int startrowBias = 0;
		int endrowBias = 0;
		// 设定到0位
		chrRASeqFile.seek(0);
		String fastaID = chrRASeqFile.readLine();
		int lengthChrID = -1;
		if (fastaID.contains(">"))
			lengthChrID = fastaID.length();// 第一行，有>号的长度
		long lengthChrSeq = chrRASeqFile.length();
		long rowstartNum = startlocation / lengthRow;
		startrowBias = (int) (startlocation % lengthRow);
		long rowendNum = endlocation / lengthRow;
		endrowBias = (int) (endlocation % lengthRow);
		// 实际序列在文件中的起点
		long startRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowstartNum
				+ startrowBias;
		long endRealCod = (lengthChrID + 1) + (lengthRow + 1) * rowendNum
				+ endrowBias;
		/**
		 * 如果位点超过了范围，那么修正位点
		 */
		if (startlocation < 1 || startRealCod >= lengthChrSeq
				|| endlocation < 1 || endRealCod >= lengthChrSeq) {
			logger.error(chrID + startlocation + " " + endlocation + " 染色体坐标错误");
			return "ChrStringHash.getSeq: 染色体坐标错误";
		}

		if (endlocation <= startlocation) {
			logger.error(chrID + startlocation + " " + endlocation + " 体坐标错误");
			return "ChrStringHash.getSeq: 坐标错误";
		}
		if (endlocation - startlocation > 20000) {
			logger.error(chrID + startlocation + " " + endlocation
					+ " 最多提取20000bp");
			return "ChrStringHash.getSeq: 最多提取20000bp";
		}
		// 定到目标坐标
		StringBuilder sequence = new StringBuilder();
		chrRASeqFile.seek(startRealCod);

		if (rowendNum - rowstartNum == 0) {
			String seqResult = chrRASeqFile.readLine();
			seqResult = seqResult.substring(0, endrowBias - startrowBias);
			return seqResult;
		} else {
			for (int i = 0; i < rowendNum - rowstartNum; i++) {
				sequence.append(chrRASeqFile.readLine());
			}
			String endline = chrRASeqFile.readLine();
			endline = endline.substring(0, endrowBias);
			sequence.append(endline);
			String seqResult = sequence.toString();
			return seqResult;
		}
	}

	/**
	 * * 输入染色体list信息 输入序列坐标以及是否为反向互补,其中ChrID为 chr1，chr2，chr10类型 返回序列
	 * 
	 * @param cisseq
	 *            正反向
	 * @param chrID
	 *            目的染色体名称，用来在哈希表中查找具体某条染色体
	 * @param startlocation
	 *            序列起点
	 * @param endlocation
	 *            序列终点
	 * @return
	 */
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		String sequence = null;
		try {
			sequence = getSeq(chrID, startlocation, endlocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (cisseq) {
			return sequence;
		} else {
			return resCompSeq(sequence, compMap);
		}
	}

	/**
	 * 给出染色体编号位置和方向返回序列
	 * 
	 * @param chrlocation染色体编号方向如
	 *            ：Chr:1000-2000,自动将chrID小写,chrID采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * @param cisseq方向
	 *            ，true:正向 false:反向互补
	 */
	public String getSeq(String chrlocation, boolean cisseq) {
		/**
		 * 判断Chr格式是否正确，是否是有效的染色体
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(chrlocation);
		if (!matcher.find()) {
			return "ReadSite染色体格式错误";
		}
		String chr = matcher.group();

		/**
		 * 获取起始位点和终止位点
		 */
		Pattern patternnumber = Pattern.compile("(?<!\\w)\\d+(?!\\w)",
				Pattern.CASE_INSENSITIVE);
		Matcher matchernumber;
		matchernumber = patternnumber.matcher(chrlocation);
		int[] location = new int[2];
		int i = 0;
		while (matchernumber.find()) {
			location[i] = Integer.parseInt(matchernumber.group());
			i++;
		}
		if (i > 2 || location[1] <= location[0]) {
			logger.error(chrlocation + " " + cisseq + " 染色体位置错误");
			return chrlocation + " " + cisseq + " 染色体位置错误";
		}
		return getSeq(cisseq, chr.toLowerCase(), location[0], location[1]);
	}

	/**
	 * 给出peak位点，查找指定范围的sequence,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * 
	 * @param chr
	 *            ,
	 * @param peaklocation
	 *            peak summit点坐标
	 * @param region
	 *            peak左右的范围
	 * @param cisseq
	 *            true:正向链 false：反向互补链
	 */

	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		/**
		 * 判断Chr格式是否正确，是否是有效的染色体
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE);
		Matcher matcher; // matcher.groupCount() 返回此匹配器模式中的捕获组数。
		matcher = pattern.matcher(chr);
		if (!matcher.find()) {
			logger.error(chr + " " + peaklocation + " " + region + " 染色体格式错误");
			return "ReadSite染色体格式错误";
		} else {
			chr = matcher.group().toLowerCase();
		}
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap) {
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = complementmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(complementmap.get(sequence.charAt(i)));
			} else {
				logger.error(sequence + " 含有未知碱基");

				return "含有未知碱基 " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

	/**
	 * 获得每条染色体对应的bufferedreader类，方便从头读取
	 * 
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID) {
		return hashBufChrSeqFile.get(chrID.toLowerCase());
	}

	/**
	 * 获得每条染色体对应的bufferedreader类，方便从头读取
	 * 
	 * @param chrID
	 * @return
	 */
	public HashMap<String, BufferedReader> getBufChrSeq() {
		return hashBufChrSeqFile;
	}

	public long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for (Map.Entry<String, BufferedReader> entry : hashBufChrSeqFile
				.entrySet()) {
			String chrID = entry.getKey();
			BufferedReader chrReader = entry.getValue();
			String content = "";
			while ((content = chrReader.readLine()) != null) {
				if (content.startsWith(">")) {
					continue;
				}
				String tmp = content.trim().replace("N", "").replace("n", "");
				effGenomSize = effGenomSize + tmp.length();
			}
		}
		return effGenomSize;
	}

	/**
	 * 本Chr文件每一行的长度
	 * 
	 * @return
	 */
	public int getChrLineLength() {
		return lengthRow;
	}

	/**
	 * 返回chrID和chrLength的对应关系
	 * 
	 * @return
	 */
	public Hashtable<String, Long> getHashChrLength() {
		return hashChrLength;
	}

	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr长度 并且按照chr长度从小到大排序
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		Iterator iter = hashChrLength.entrySet().iterator();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// 存放最后结果
		while (iter.hasNext()) {
			String[] tmpResult = new String[2];
			Map.Entry entry = (Map.Entry) iter.next();
			String chrID = (String) entry.getKey();
			long lengthChrSeq = (Long) entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsResult.add(tmpResult);
		}
		// //////////////////////////把lsChrLength按照chrLen从小到大进行排序/////////////////////////////////////////////////////////////////////////////
		Collections.sort(lsResult, new Comparator<String[]>() {
			public int compare(String[] arg0, String[] arg1) {
				if (Integer.parseInt(arg0[1]) < Integer.parseInt(arg1[1]))
					return -1;
				else if (Integer.parseInt(arg0[1]) == Integer.parseInt(arg1[1]))
					return 0;
				else
					return 1;
			}
		});
		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return lsResult;
	}

	/**
	 * 指定最长染色体的值，返回按比例每条染色体相应值下染色体的坐标数组,resolution和int[resolution]，可用于画图
	 * 那么resolution就是返回的int[]的长度
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		ArrayList<String[]> chrLengthArrayList = getChrLengthInfo();
		int binLen = Integer.parseInt(chrLengthArrayList.get(chrLengthArrayList
				.size() - 1)[1]) / maxresolution;
		int resolution = (int) (hashChrLength.get(chrID) / binLen);

		Long chrLength = hashChrLength.get(chrID.toLowerCase());
		double binLength = (double) chrLength / resolution;
		int[] chrLengtharray = new int[resolution];
		for (int i = 0; i < resolution; i++) {
			chrLengtharray[i] = (int) ((i + 1) * binLength);
		}
		return chrLengtharray;
	}
}
