package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

/**
 * 本类用来将染色体的名字，序列装入染色体类，并且是以Hash表形式返回 目前本类中仅仅含有静态方法 同时用来提取某段位置的序列 和提取反向重复序列
 * 作者：宗杰 20090617
 */
public class ChrStringHash extends SeqHashAbs{
	public static void main(String[] args) {
		Species species = new Species(9606);
		SeqHash seqHash = new SeqHash(species.getChromFaPath(), species.getChromFaRegex());
		SeqFasta seqFasta = seqHash.getSeq("chr1", 12345, 12348);
		System.out.println(seqFasta.toString());
	}
	private static Logger logger = Logger.getLogger(ChrStringHash.class);
	
	/** 以下哈希表的键是染色体名称，都是小写，格式如：chr1，chr2，chr10 */
	HashMap<String, RandomAccessFile> mapChrID2RandomFile;
	HashMap<String, BufferedReader> mapChrID2BufReader;
	HashMap<String, TxtReadandWrite> mapChrID2Txt;
	HashMap<String, Integer> mapChrID2EnterType;

	/** 每个文本所对应的单行长度
	 *  Seq文件第二行的长度，也就是每行序列的长度+1，1是回车 
	 *  现在是假设Seq文件第一行都是>ChrID,第二行开始都是Seq序列信息
	 *  并且每一行的序列都等长
	 */
	Map<String, Integer> mapChrFile2LengthRow = new HashMap<String, Integer>();
	
	int maxExtractSeqLength = 2000000;
	/**
	 * 随机硬盘读取染色体文件的方法，貌似很伤硬盘，考虑用固态硬盘 注意
	 * 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * 一个文本保存一条染色体，以fasta格式保存，每个文本以">"开头，然后接下来每行固定的碱基数(如UCSC为50个，TIGRRice为60个)
	 * 文本文件名(不考虑后缀名，当然没有后缀名也行)应该是待查找的chrID
	 * @param chrFilePath
	 * @param regx null走默认，默认为"\\bchr\\w*"， 用该正则表达式去查找文件名中含有Chr的文件，每一个文件就认为是一个染色体
	 * @param CaseChange 是否将序列名转化为小写，一般转为小写
	 */
	public ChrStringHash(String chrFilePath,String regx) {
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
		ArrayList<String> lsChrFile = initialAndGetFileList();
		RandomAccessFile chrRAseq = null;
		TxtReadandWrite txtChrTmp = null;
		BufferedReader bufChrSeq = null;
		mapChrFile2LengthRow.clear();
		for (String fileNam : lsChrFile) {
			String[] chrFileName = FileOperate.getFileNameSep(fileNam);
			lsSeqName.add(chrFileName[0]);

			//TODO
			chrRAseq = new RandomAccessFile(fileNam, "r");
//			chrRAseq = new BufferedRandomAccessFile(fileNam, "r");
			txtChrTmp = new TxtReadandWrite(fileNam, false);
			String enterType = txtChrTmp.getEnterType();
			bufChrSeq = txtChrTmp.readfile();
			String chrID = chrFileName[0].toLowerCase();
			// 假设每一个文件的每一行Seq都相等
			String seqRow = txtChrTmp.readFirstLines(3).get(2);
			mapChrFile2LengthRow.put(chrID, seqRow.length());
			
			mapChrID2RandomFile.put(chrID, chrRAseq);
			mapChrID2BufReader.put(chrID, bufChrSeq);
			mapChrID2Txt.put(chrID, txtChrTmp);
			if (enterType.equals(TxtReadandWrite.ENTER_LINUX)) {
				mapChrID2EnterType.put(chrID, 1);
			} else if (enterType.equals(TxtReadandWrite.ENTER_WINDOWS)) {
				mapChrID2EnterType.put(chrID, 2);
			}
		}
		setChrLength();
	}
	
	/** 初始化并返回文件夹中的所有符合正则表达式的文本名 */
	private ArrayList<String> initialAndGetFileList() {
		chrFile = FileOperate.addSep(chrFile);
		if (regx == null)
			regx = "\\bchr\\w*";
		
		mapChrID2RandomFile = new HashMap<String, RandomAccessFile>();
		mapChrID2BufReader = new HashMap<String, BufferedReader>();
		mapChrID2Txt = new HashMap<String, TxtReadandWrite>();
		mapChrID2EnterType = new HashMap<String, Integer>();
		lsSeqName = new ArrayList<String>();
		return FileOperate.getFoldFileNameLs(chrFile,regx, "*");
	}
	/** 设定染色体长度 */
	private void setChrLength() throws IOException {
		for (Entry<String, RandomAccessFile> entry : mapChrID2RandomFile.entrySet()) {
			String chrID = entry.getKey();
			int lengthRow = mapChrFile2LengthRow.get(chrID);
			RandomAccessFile chrRAfile = entry.getValue();
			// 设定到0位
			chrRAfile.seek(0);
			// 获得每条染色体的长度，文件长度-第一行的
			String fastaID = chrRAfile.readLine();
			int lengthChrID = -1;
			if (fastaID.contains(">"))
				lengthChrID = fastaID.length();// 第一行，有>号的长度
			
			long lengthChrSeq = chrRAfile.length();
			long tmpChrLength = (lengthChrSeq - lengthChrID - 1) / (lengthRow + 1) * lengthRow + (lengthChrSeq - lengthChrID - 1) % (lengthRow + 1);
			hashChrLength.put(chrID, tmpChrLength);
		}
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
		chrID = chrID.toLowerCase();
		
		int lengthRow = mapChrFile2LengthRow.get(chrID);
		if (startlocation <= 0) {
			startlocation = 1;
		}
		if (endlocation <= 0) {
			endlocation = getChrLength(chrID);
		}
		startlocation--;
	
		RandomAccessFile chrRASeqFile = mapChrID2RandomFile.get(chrID);// 判断文件是否存在
		int entryNum = mapChrID2EnterType.get(chrID);
		if (chrRASeqFile == null) {
			logger.error( "无该染色体: "+ chrID);
			return null;
		}
		int startrowBias = 0, endrowBias = 0;
		// 设定到0位
		chrRASeqFile.seek(0);
		String fastaID = chrRASeqFile.readLine();
		int lengthChrID = -1;
		if (fastaID.contains(">"))
			lengthChrID = fastaID.length();// 第一行，有>号的长度
		else
			logger.error("不是正规的fasta格式：" + chrID);

		long lengthChrSeq = chrRASeqFile.length();
		long rowstartNum = startlocation / lengthRow;
		long rowendNum = endlocation / lengthRow;

		startrowBias = (int) (startlocation % lengthRow);
		endrowBias = (int) (endlocation % lengthRow);
		// 实际序列在文件中的起点
		long startRealCod = (lengthChrID + entryNum) + (lengthRow + entryNum) * rowstartNum + startrowBias;
		long endRealCod = (lengthChrID + entryNum) + (lengthRow + entryNum) * rowendNum + endrowBias;
		//如果位点超过了范围，那么修正位点
		if (startlocation < 0 || startRealCod >= lengthChrSeq || endlocation < 1 || endRealCod >= lengthChrSeq || endlocation < startlocation) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " 染色体坐标错误");
			return null;
		}
		if (endlocation - startlocation > maxExtractSeqLength) {
			logger.error(chrID + " " + startlocation + " " + endlocation + " 最多提取" + maxExtractSeqLength + "bp");
			return null;
		}

		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName(chrID + "_" + startlocation + "_" + endlocation);
		// 定到目标坐标
		StringBuilder sequence = new StringBuilder();
		chrRASeqFile.seek(startRealCod);
		
		if (rowendNum - rowstartNum == 0) {
			String seqResult = chrRASeqFile.readLine();
			seqResult = seqResult.substring(0, endrowBias - startrowBias);
			seqFasta.setSeq(seqResult);
		} else {
			for (int i = 0; i < rowendNum - rowstartNum; i++) {
				sequence.append(chrRASeqFile.readLine());
			}
			String endline = chrRASeqFile.readLine();
			endline = endline.substring(0, endrowBias);
			sequence.append(endline);
			seqFasta.setSeq(sequence.toString());
		}
		return seqFasta;
	}
	/**
	 * 获得每条染色体对应的bufferedreader类，方便从头读取
	 * @param chrID
	 * @return
	 */
	public BufferedReader getBufChrSeq(String chrID) {
		return mapChrID2BufReader.get(chrID.toLowerCase());
	}
	/**
	 * 获得每条染色体对应的bufferedreader类，方便从头读取
	 * @param refID
	 * @return
	 */
	public HashMap<String, BufferedReader> getBufChrSeq() {
		return mapChrID2BufReader;
	}
	/**
	 * 返回有意义的碱基数量
	 * @return
	 * @throws IOException
	 */
	public long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for (Map.Entry<String, BufferedReader> entry : mapChrID2BufReader.entrySet()) {
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
	@Override
	public Iterable<Character> readBase(String refID) {
		final String myRefID = refID.toLowerCase();
		return new Iterable<Character>() {
			@Override
			public Iterator<Character> iterator() {
				IteratorBase iteratorBase = new IteratorBase();
				iteratorBase.setReader(mapChrID2Txt.get(myRefID));
				return iteratorBase;
			}
		};
	}
	
	/** 专门用于排序比较的类 */
	public static class CompareChrID implements Comparator<String> {
		PatternOperate patChrID = new PatternOperate("\\d+", false);
		@Override
		public int compare(String o1, String o2) {
			String chrID1 = patChrID.getPatFirst(o1);
			String chrID2 = patChrID.getPatFirst(o2);
			if (chrID1 == null || chrID2 == null) {
				if (o1.equalsIgnoreCase("chrm") ) {
					return 1;
				} else if (o2.equalsIgnoreCase("chrm")) {
					return -1;
				} else if (o1.equalsIgnoreCase("chrc")) {
					return 1;
				} else if (o2.equalsIgnoreCase("chrc")) {
					return -1;
				}
				return o1.compareTo(o2);
			}
			Integer chr1 = Integer.parseInt(chrID1);
			Integer chr2 = Integer.parseInt(chrID2);
			return chr1.compareTo(chr2);
		}
		
	}

}


class IteratorBase implements Iterator<Character> {
	TxtReadandWrite txtReader;
	BufferedReader reader;
	
	char[] tmpSeq;
	int index = 0;
	
	Character base;
	
	public void setReader(TxtReadandWrite txtRead) {
		this.txtReader = txtRead;
		try { reader = txtReader.readfile(); } catch (Exception e) { e.printStackTrace(); }
		base = getBase();
	}
	@Override
	public boolean hasNext() {
		return base != null;
	}

	@Override
	public Character next() {
		Character retval = base;
		base = getBase();
		return retval;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private Character getBase() {
		try {
			return getBaseWithExp();
		} catch (IOException e) {
			return null;
		}
	}
	
	private Character getBaseWithExp() throws IOException {
		Character base = null;
		if (tmpSeq == null || index >= tmpSeq.length) {
			String lineTmp = reader.readLine();
			if (lineTmp == null) {
				return null;
			}
			lineTmp = lineTmp.trim();
			/////skip blank lines
			while (lineTmp.startsWith(">") || lineTmp.length() == 0) {
				lineTmp = reader.readLine();
				if (lineTmp == null) {
					return null;
				}
				lineTmp = lineTmp.trim();
			}
			/////////////////
			tmpSeq = lineTmp.toCharArray();
			index = 0;
		}

		base = tmpSeq[index];
		while (base==' ' || base == '\t' || base == '\r' || base == '\n') {
			index ++;
			base = tmpSeq[index];
		}
		index++;
		return base;
	}

}
