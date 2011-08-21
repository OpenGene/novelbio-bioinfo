package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.getChrSequence.ChrStringHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class SeqHash {
	private static Logger logger = Logger.getLogger(SeqHash.class);
	
	String Chrpatten = "Chr\\w+";
	/**
	 * 保存chrID和chrLength的对应关系
	 */
	HashMap<String, Long> hashChrLength = new HashMap<String, Long>();
	/**
	 * 从小到大排列chrLength的list
	 */
	ArrayList<String[]> lsChrLen = null;

	/**
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
	 */
	protected static HashMap<Character, Character> getCompmap() {
		return SeqFasta.getCompMap();
	}
	
	boolean CaseChange;
	String regx = "";
	boolean append;
	
	String chrFile = "";
	
	
	/**
	 * 设定常规信息
	 * @param CaseChange 是否将序列名转变为小写
	 * @param regx 序列名的正则表达式，在读取ChromFa文件夹时使用，用于抓取文件夹中的所有序列文件，null不设定
	 * 读取Chr文件夹的时候默认设定了 "\\bchr\\w*"
	 * @param append 读取ChrID的时候没用
	 * @param chrPattern 当输入类似chr1:1123-4567数据时将chr1提取出来的正则表达式
	 */
	public void setInfo(boolean CaseChange, String regx,boolean append, String chrPattern) {
		this.CaseChange = CaseChange;
		if (regx != null) {
			this.regx = regx;
		}
		this.append = append;
		this.Chrpatten = chrPattern;
	}
	
	/**
	 * @param chrFilePath
	 * @throws Exception 
	 * @throws IOException
	 */
	public SeqHash(String chrFile) 
	{
		this.chrFile = chrFile;
	}
	
	/**
	 * @param chrFilePath
	 * @param regx 序列名的正则表达式，null不设定
	 * @throws Exception 
	 * @throws IOException
	 */
	public SeqHash(String chrFile, String regx) 
	{
		this.chrFile = chrFile;
		this.regx = regx;
	}
	
	/**
	 * 返回chrID和chrLength的对应关系
	 * chrID通通小写
	 * @return
	 */
	public HashMap<String, Long> getHashChrLength() {
		return hashChrLength;
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr长度 并且按照chr长度从小到大排序
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		if (lsChrLen != null) {
			return lsChrLen;
		}
		lsChrLen = new ArrayList<String[]>();
		for (Entry<String, Long> entry : hashChrLength.entrySet()) {
			String[] tmpResult = new String[2];
			String chrID = entry.getKey();
			long lengthChrSeq = entry.getValue();
			tmpResult[0] = chrID;
			tmpResult[1] = lengthChrSeq + "";
			lsChrLen.add(tmpResult);
		}
		// //////////////////////////把lsChrLength按照chrLen从小到大进行排序/////////////////////////////////////////////////////////////////////////////
		Collections.sort(lsChrLen, new Comparator<String[]>() {
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
		return lsChrLen;
	}
	
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLength(String chrID) 
	{
		return getHashChrLength().get(chrID.toLowerCase());
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMin() 
	{
		return Integer.parseInt(getChrLengthInfo().get(0)[1]);
//		return getChrLengthInfo().get(0);
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMax() 
	{
		return Integer.parseInt(getChrLengthInfo().get(getChrLengthInfo().size()-1)[1]);
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
	
	public void setFile()
	{
		try {
			setChrFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected abstract void setChrFile() throws Exception;
	
	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * 
	 * @param outFile
	 *            待输出的文件名，带上全部路径
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// 存放最后结果
		for (Entry<String, Long> entry : hashChrLength.entrySet()) {
			String[] tmpResult = new String[2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtChrLength = new TxtReadandWrite();
		txtChrLength.setParameter(outFile, true, false);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			logger.error("输出文件出错："+outFile);
			e.printStackTrace();
		}
	}
	public abstract String getSeq(String chrID, long startlocation, long endlocation) throws IOException ;
	
	
	/**
	 * * 输入染色体list信息 输入序列坐标以及是否为反向互补,其中ChrID为 chr1，chr2，chr10类型 返回序列
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
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
		if (sequence == null) {
			return null;
		}
		if (cisseq ) {
			return sequence;
		} else {
			return resCompSeq(sequence, getCompmap());
		}
	}

	/**
	 * 给出染色体编号位置和方向返回序列<br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
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
			logger.error("ReadSite染色体格式错误"+ chrlocation);
			return null;
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
			return null;
		}
		return getSeq(cisseq, chr.toLowerCase(), location[0], location[1]);
	}

	/**
	 * 给出peak位点，查找指定范围的sequence,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * <br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
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
	 * <br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param cisseq 正反向
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	public String getSeq(boolean cisseq, String chrID,ArrayList<int[]> lsInfo, boolean getIntron) {
		String myChrID = chrID;
		if (CaseChange) {
			myChrID = chrID.toLowerCase();
		}
		if (!hashChrLength.containsKey(myChrID)) {
			logger.error("没有该染色体： "+chrID);
			return null;
		}
		
		String result = ""; boolean cis5to3 = true;
		int[] exon1 = lsInfo.get(0);
		if (exon1[0] > exon1[1]) {
			cis5to3 = false;
		}
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[0], exon[1]).toUpperCase(); 
					if (getIntron && i < lsInfo.size()-1) {
						result = result + getSeq(chrID,exon[1]+1, lsInfo.get(i+1)[0]-1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + getSeq(chrID, exon[1], exon[0]).toUpperCase(); 
					if (getIntron && i > 0) {
						result = result + getSeq(chrID,exon[0] + 1, lsInfo.get(i-1)[1] - 1).toLowerCase();
					}
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		if (!cisseq) {
			result = resCompSeq(result, getCompmap());
		}
		return result;
	}
	
}
