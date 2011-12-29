package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public abstract class SeqHashAbs implements SeqHashInt{
	private static Logger logger = Logger.getLogger(SeqHashAbs.class);
	/**
	 * 保存chrID和chrLength的对应关系
	 */
	LinkedHashMap<String, Long> hashChrLength = new LinkedHashMap<String, Long>();
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
	
	String regx = null;
	boolean append;
	
	String chrFile = "";
	/**
	 * 是否将序列名改为小写
	 */
	boolean CaseChange = true;
//	/**
//	 * @param chrFilePath
//	 */
//	public SeqHashAbs(String chrFile) 
//	{
//		this.chrFile = chrFile;
//	}
//	
//	/**
//	 * @param chrFile
//	 * @param regx 序列名的正则表达式，null不设定
//	 * @param TOLOWCASE 是否将序列结果改为小写 True：小写，False：大写，null不变
//	 */
//	public SeqHashAbs(String chrFile, String regx, Boolean TOLOWCASE) 
//	{
//		this.chrFile = chrFile;
//		this.regx = regx;
//		this.TOLOWCASE = TOLOWCASE;
//	}
	/**
	 * 
	 * @param chrFile
	 * @param regx 序列名的正则表达式，null和"   "都不设定
	 * @param CaseChange 是否将序列名改为小写
	 * @param TOLOWCASE 是否将序列结果改为小写 True：小写，False：大写，null不变
	 */
	public SeqHashAbs(String chrFile, String regx,boolean CaseChange) 
	{
		this.chrFile = chrFile;
		if (regx != null && !regx.trim().equals("")) {
			this.regx = regx;
		}
		this.CaseChange = CaseChange;
	}
	
	/**
	 * 将序列名称按顺序读入list
	 */
	public ArrayList<String> lsSeqName;
	/**
	 * 获得所有序列的名字
	 * @return
	 */
	public ArrayList<String> getLsSeqName() {
		return lsSeqName;
	}
	/**
	 * 返回chrID和chrLength的对应关系
	 * chrID通通小写
	 * @return
	 */
	public LinkedHashMap<String, Long> getHashChrLength() {
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
	/**
	 * 具体读取文件
	 */
	protected void setFile()
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
		TxtReadandWrite txtChrLength = new TxtReadandWrite(outFile, true);
		try {
			txtChrLength.ExcelWrite(lsResult, "\t", 1, 1);
		} catch (Exception e) {
			logger.error("输出文件出错："+outFile);
			e.printStackTrace();
		}
	}
	protected abstract String getSeqInfo(String chrID, long startlocation, long endlocation) throws IOException;
	
	/**
	 * @param chrID 染色体编号或序列名
	 * @param startlocation 起点
	 * @param endlocation 终点
	 * @return 返回序列，出错就返回null
	 */
	public String getSeq(String chrID, long startlocation, long endlocation)
	{
		try {
			return getSeqInfo(chrID, startlocation, endlocation);
		} catch (IOException e) {
			return null;
		}
	}
	
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
		String sequence = getSeq(chrID, startlocation, endlocation);

		if (sequence == null) {
			return null;
		}
		if (cisseq ) {
			return sequence;
		} else {
			return SeqFasta.reservecom(sequence);
		}
	}

	/**
	 * 给出peak位点，查找指定范围的sequence，根据CaseChange改变大小写
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
		if (CaseChange) {
			chr = chr.toLowerCase();
		}
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}

	/**
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * 不管转录本的方向，总是从基因组的5‘向3’提取。
	 * 方向需要人工设定cisseq
	 * @param cisseq 正反向，是否需要反向互补。正向永远是5to3
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	private String getSeq(boolean cisseq, String chrID,List<int[]> lsInfo, String sep, boolean getIntron) {
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
		if (exon1[0] > exon1[1] || (lsInfo.size() > 1 && lsInfo.get(0)[0] > lsInfo.get(1)[0]) ) {
			cis5to3 = false;
		}
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + sep + getSeq(chrID, exon[0], exon[1]).toUpperCase(); 
					if (getIntron && i < lsInfo.size()-1) {
						result = result + sep + getSeq(chrID,exon[1]+1, lsInfo.get(i+1)[0]-1).toLowerCase();
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				int[] exon = lsInfo.get(i);
				try {	
					result = result + sep + getSeq(chrID, exon[1], exon[0]).toUpperCase();
					if (getIntron && i > 0) {
						result = result + sep + getSeq(chrID,exon[0] + 1, lsInfo.get(i-1)[1] - 1).toLowerCase();;
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		result = result.substring(sep.length());
		if (!cisseq) {
			result = SeqFasta.reservecom(result);
		}
		return result;
	}
	String sep = "";
	/**
	 * 外显子之间用什么来分割，默认为""
	 * @param sep
	 */
	@Override
	public void setSep(String sep) {
		this.sep = sep;
	}
	/**
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * 不管转录本的方向，总是从基因组的5‘向3’提取。
	 * 方向需要人工设定cisseq
	 * @param cisseq 正反向，是否需要反向互补，正向永远是5to3。
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	public String getSeq(boolean cisseq, String chrID,List<int[]> lsInfo, boolean getIntron) {
		return getSeq(cisseq, chrID, lsInfo, sep, getIntron);
	}
	
	/**
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	@Override
	public String getSeq(String chrID,List<int[]> lsInfo, boolean getIntron) {
		 boolean cis5to3 = true;
		 int[] exon1 = lsInfo.get(0);
		 if (exon1[0] > exon1[1] || (lsInfo.size() > 1 && lsInfo.get(0)[0] > lsInfo.get(1)[0]) ) {
			 cis5to3 = false;
		 }
		 return this.getSeq(cis5to3, chrID, lsInfo, getIntron);
	}
	
	/**
	 * 测试git
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * 仅获得起始exon到终止exon（包括起点和终点）的exon list
	 * @param chrID
	 * @param cisseq 正反向，在提出的正向转录本的基础上，是否需要反向互补。
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 * @param cisseq 正反向
	 * @param start 实际第几个exon 起点必须小于等于终点
	 * @param end 实际第几个axon
	 * @param lsInfo
	 * @param getIntron 是否获取内含子，内含子自动小写
	 * @return
	 */
	@Override
	public String getSeq(String chrID,boolean cisseq, int start, int end, List<int[]> lsInfo, boolean getIntron) {
		start--;
		if (start < 0) {
			start = 0;
		}
		if (end <= 0 || end > lsInfo.size()) {
			end = lsInfo.size();
		}
		boolean cis5to3 = true;
		int[] exon1 = lsInfo.get(0);
		if (exon1[0] > exon1[1]) {
			cis5to3 = false;
		}
		List<int[]> lsExon = lsInfo.subList(start, end);
		String seq = getSeq(cis5to3, chrID, lsExon, getIntron);
		if (cisseq) {
			return seq;
		}
		else {
			return SeqFasta.reservecom(seq);
		}
	}
	
	
	/**
	 * 按顺序提取闭区间序列，每一个区段保存为一个SeqFasta对象
	 * SeqFasta的名字为chrID:起点坐标-终点坐标 都是闭区间
	 * @param chrID 序列ID
	 * @param lsInfo 具体的区间
	 * @return
	 */
	public ArrayList<SeqFasta> getRegionSeqFasta(List<LocInfo> lsLocInfos) {
		ArrayList<SeqFasta> lsSeqfasta = new ArrayList<SeqFasta>();
		for (LocInfo locInfo : lsLocInfos) {
			String myChrID = locInfo.getChrID();
			if (CaseChange) {
				myChrID = myChrID.toLowerCase();
			}
			if (!hashChrLength.containsKey(myChrID)) {
				logger.error("没有该染色体： "+ locInfo.getChrID());
				return null;
			}
			SeqFasta seqFasta = new SeqFasta(locInfo.getChrID()+":"+locInfo.getStartLoc()+"-"+ locInfo.getEndLoc(),
					getSeq(myChrID, locInfo.getStartLoc(),
							locInfo.getEndLoc()), locInfo.isCis5to3());
			lsSeqfasta.add(seqFasta);
		}
		return lsSeqfasta;
	}
	
	
}
