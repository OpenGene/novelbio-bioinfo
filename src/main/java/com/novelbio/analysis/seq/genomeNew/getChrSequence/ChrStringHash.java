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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类用来将染色体的名字，序列装入染色体类，并且是以Hash表形式返回 目前本类中仅仅含有静态方法 同时用来提取某段位置的序列 和提取反向重复序列
 * 作者：宗杰 20090617
 */
public class ChrStringHash extends SeqHashAbs{
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
	 * 随机硬盘读取染色体文件的方法 注意
	 * 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 一个文本保存一条染色体，以fasta格式保存，每个文本以">"开头，然后接下来每行固定的碱基数(如UCSC为50个，TIGRRice为60个)
	 * 文本文件名(不考虑后缀名，当然没有后缀名也行)应该是待查找的chrID
	 * 最后本类生成一个Hashtable--chrID(String)---SeqFile(RandomAccessFile)表<br>
	 * 和一个Hashtable--chrID(String)---SeqFile(BufferedReader)表<br>
	 * 其中chrID一直为小写
	 * 
	 * @param chrFilePath
	 * @throws Exception 
	 * @throws IOException
	 */
	public ChrStringHash(String chrFilePath) 
	{
		super(chrFilePath, "\\bchr\\w*", TOLOWCASE);
		setFile();
	}
	/**
	 * 设定序列文件夹
	 * @throws FileNotFoundException 
	 */
	protected void setChrFile() throws Exception
	{
		if (!chrFile.endsWith(File.separator)) {
			chrFile = chrFile + File.separator;
		}
		ArrayList<String[]> lsChrFile = FileOperate.getFoldFileName(chrFile,
				regx, "*");
		hashChrSeqFile = new HashMap<String, RandomAccessFile>();
		hashBufChrSeqFile = new HashMap<String, BufferedReader>();

		for (int i = 0; i < lsChrFile.size(); i++) {
			RandomAccessFile chrRAseq = null;
			TxtReadandWrite txtChrTmp = new TxtReadandWrite();
			BufferedReader bufChrSeq = null;
			String[] chrFileName = lsChrFile.get(i);
			String fileNam = "";

			if (chrFileName[1].equals(""))
				fileNam = chrFile + chrFileName[0];
			else
				fileNam = chrFile + chrFileName[0] + "." + chrFileName[1];

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
			if (CaseChange) {
				hashChrSeqFile.put(chrFileName[0].toLowerCase(), chrRAseq);
				hashBufChrSeqFile.put(chrFileName[0].toLowerCase(), bufChrSeq);
			}
			else {
				hashChrSeqFile.put(chrFileName[0], chrRAseq);
				hashBufChrSeqFile.put(chrFileName[0], bufChrSeq);
			}
		}
		setChrLength();
	
	}
	private void setChrLength() throws IOException {
		for (Entry<String, RandomAccessFile> entry : hashChrSeqFile.entrySet()) {
			String chrID = entry.getKey();
			RandomAccessFile chrRAfile = entry.getValue();
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
	 * 给定chrID,chrID会自动转换为小写，和读取的起点以及终点，返回读取的序列
	 * startNum=204;从第几个碱基开始读取，从1开始记数，注意234的话，实际为从234开始读取，类似substring方法 long
	 * endNum=254;//读到第几个碱基，从1开始记数，实际读到第endNum个碱基。 快速提取序列
	 * 
	 * @throws IOException
	 */
	protected String getSeqInfo(String chrID, long startlocation, long endlocation)
			throws IOException {
		startlocation--;
		RandomAccessFile chrRASeqFile = hashChrSeqFile.get(chrID.toLowerCase());// 判断文件是否存在
		if (chrRASeqFile == null) {
			logger.error( "无该染色体: "+ chrID);
			return null;
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
		if (startlocation < 0 || startRealCod >= lengthChrSeq
				|| endlocation < 1 || endRealCod >= lengthChrSeq) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " 染色体坐标错误");
			return null;
		}

		if (endlocation <= startlocation) {
			logger.error(chrID + " "+ startlocation + " " + endlocation + " 体坐标错误");
			return null;
			}
		if (endlocation - startlocation > 200000) {
			logger.error(chrID + " " + startlocation + " " + endlocation
					+ " 最多提取20000bp");
			return null;
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
		for (Map.Entry<String, BufferedReader> entry : hashBufChrSeqFile.entrySet()) {
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


	
}
