package com.novelbio.analysis.seq.fasta;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffoperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingoperate.SiteSeqInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Alignment;

public abstract class SeqHashAbs implements SeqHashInt, Closeable {
	private static Logger logger = Logger.getLogger(SeqHashAbs.class);
	/** 保存chrID和chrLength的对应关系<br>
	 * <b>key小写</b>
	 *  */
	LinkedHashMap<String, Long> mapChrID2Length = new LinkedHashMap<String, Long>();
	/** 从小到大排列chrLength的list */
	ArrayList<String[]> lsChrLen = null;
	/** 是否要设定为DNA，也就是将序列中的U全部转化为T */
	boolean isDNAseq = false;
	/** 抓取chrID的正则表达式 */
	String regx = null;
	String chrFile = "";
	/** 将序列名称按顺序读入list */
	public ArrayList<String> lsSeqName;
	/** 外显子之间用什么来分割，默认为"" */
	String sep = "";
	int getLongestSeqLen = 2000000;
	/**
	 * @param chrFile
	 * @param regx 序列名的正则表达式，null和""都不设定
	 * @param TOLOWCASE 是否将序列结果改为小写 True：小写，False：大写，null不变
	 */
	public SeqHashAbs(String chrFile, String regx) {
		this.chrFile = chrFile;
		if (regx != null && !regx.equals("")) {
			this.regx = regx;
		}
	}
	
	public abstract void setMaxExtractSeqLength(int maxSeqLen);
	
	public String getChrFile() {
		return chrFile;
	}
	
	/**
	 * 是否要设定为DNA，也就是将序列中的U全部转化为T
	 * 只有当序列为RNA时才会用到
	 */
	@Override
	public void setDNAseq(boolean isDNAseq) {
		this.isDNAseq = isDNAseq;
	}
	/**
	 * 外显子之间用什么来分割，默认为""
	 * @param sep
	 */
	@Override
	public void setSep(String sep) {
		this.sep = sep;
	}
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
	public LinkedHashMap<String, Long> getMapChrLength() {
		return mapChrID2Length;
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public Long getChrLength(String chrID) {
		return getMapChrLength().get(chrID.toLowerCase());
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param refID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMin() {
		return Integer.parseInt(getChrLengthInfo().get(0)[1]);
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param refID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMax() {
		return Integer.parseInt(getChrLengthInfo().get(getChrLengthInfo().size()-1)[1]);
	}
	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * 
	 * @param refID
	 * @return ArrayList<String[]> 0: chrID 1: chr长度 并且按照chr长度从小到大排序
	 */
	public ArrayList<String[]> getChrLengthInfo() {
		if (lsChrLen != null) {
			return lsChrLen;
		}
		lsChrLen = new ArrayList<String[]>();
		for (Entry<String, Long> entry : mapChrID2Length.entrySet()) {
			String[] tmpResult = new String[2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			lsChrLen.add(tmpResult);
		}
		//把lsChrLength按照chrLen从小到大进行排序
		Collections.sort(lsChrLen, new Comparator<String[]>() {
			public int compare(String[] arg0, String[] arg1) {
				Integer a1 = Integer.parseInt(arg0[1]);
				Integer a2 = Integer.parseInt(arg1[1]);
				return a1.compareTo(a2);
			}
		});
		return lsChrLen;
	}
	/**
	 * 指定最长染色体的值，返回按比例每条染色体相应值下染色体的坐标数组,resolution和int[resolution]，可用于画图
	 * 那么resolution就是返回的int[]的长度
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		chrID = chrID.toLowerCase();
		ArrayList<String[]> chrLengthArrayList = getChrLengthInfo();
		int binLen = Integer.parseInt(chrLengthArrayList.get(chrLengthArrayList.size() - 1)[1]) / maxresolution;
		int resolution = (int) (mapChrID2Length.get(chrID) / binLen);
		Long chrLength = mapChrID2Length.get(chrID.toLowerCase());
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
	protected void setFile() {
		try {
			setChrFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 读取序列
	 * @throws Exception
	 */
	protected abstract void setChrFile() throws Exception;
	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * @param outFile 待输出的文件名，带上全部路径
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();// 存放最后结果
		for (Entry<String, Long> entry : mapChrID2Length.entrySet()) {
			String[] tmpResult = new String[2];
			tmpResult[0] = entry.getKey();
			tmpResult[1] = entry.getValue() + "";
			lsResult.add(tmpResult);
		}
		TxtReadandWrite txtChrLength = new TxtReadandWrite(outFile, true);
		try {
			txtChrLength.ExcelWrite(lsResult);
		} catch (Exception e) {
			logger.error("输出文件出错："+outFile);
			e.printStackTrace();
		}
		txtChrLength.close();
	}
	/**
	 * @param chrID 染色体编号或序列名
	 * @param startlocation 序列起点 <b>注意起点从1开始</b>，跟string的subString不一样
	 * @param endlocation 终点
	 * @return 返回序列，出错就返回null
	 */
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation) {
		chrID = chrID.toLowerCase();
		SeqFasta seqFasta = null;
		try {
			seqFasta = getSeqInfo(chrID, startlocation, endlocation);
		} catch (Exception e) {
			logger.error("cannot get location: " + chrID + "\t" + startlocation + "\t"+ endlocation, e);
			return null;
		}
		if (seqFasta == null) {
			logger.error("cannot get location: " + chrID + "\t" + startlocation + "\t"+ endlocation);
			return null;
		}
		seqFasta.setDNA(isDNAseq);
		return seqFasta;
	}
	/** 提取序列 */
	protected abstract SeqFasta getSeqInfo(String chrID, long startlocation, long endlocation);
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
	public SeqFasta getSeq(Boolean cisseq, String chrID, long startlocation, long endlocation) {
		SeqFasta seqFasta = getSeq(chrID, startlocation, endlocation);
		if (seqFasta == null) return null;
		if (cisseq == null) cisseq = true;
		
		if (cisseq )
			return seqFasta;
		else
			return seqFasta.reservecom();
	}
	/**
	 * 给出peak位点，查找指定范围的sequence，根据CaseChange改变大小写
	 * <br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param chr
	 * @param peaklocation peak summit点坐标
	 * @param region peak左右的范围
	 * @param cisseq true:正向链 false：反向互补链
	 */
	public SeqFasta getSeq(String chr, int peaklocation, int region, boolean cisseq) {
		int startnum = peaklocation - region;
		int endnum = peaklocation + region;
		return getSeq(cisseq, chr, startnum, endnum);
	}
	/**
	 * seqname = chrID_第一个外显子的起点_第一个外显子的终点
	 * 完全兼容gffgeneinfo获得的序列
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param strandType 提取的方向
	 * @param chrID 无所谓大小写
	 * @param lsInfo 给定的转录本，每一对是一个外显子，必须是cis5to3从小到大排序，trans 从大到小排序
	 * @param sep intron和exon之间的间隔符
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 * @return
	 */
	private SeqFasta getSeq(StrandType strandType, String chrID, List<ExonInfo> lsInfo, String sep, boolean getIntron) {
		String myChrID = chrID.toLowerCase();
		if (!mapChrID2Length.containsKey(myChrID)) {
			logger.error("no such chromosome: "+myChrID);
			return null;
		}
		
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName(chrID + "_" + lsInfo.get(0).getName() + "_");

		StringBuilder result = new StringBuilder();
		
		boolean cis5to3 = getStrand(lsInfo);
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				ExonInfo exon = lsInfo.get(i);
				SeqFasta seqfastaTmp = getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs());
				if (seqfastaTmp == null) return null;
//				if (seqfastaTmp == null) continue;
				addSep(result, sep);
				result.append(seqfastaTmp.toString().toUpperCase());
				
				if (getIntron && i < lsInfo.size()-1) {
					SeqFasta seqfastaTmpIntron = getSeq(myChrID, exon.getEndAbs()+1, lsInfo.get(i+1).getStartAbs()-1);
					addSep(result, sep);
					result.append(seqfastaTmpIntron.toString().toLowerCase());
				}
			}
		} else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				ExonInfo exon = lsInfo.get(i);
				SeqFasta seqfastaTmp = getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs());
				if (seqfastaTmp == null) return null;
//				if (seqfastaTmp == null) continue;
				addSep(result, sep);
				result.append(seqfastaTmp.toString().toUpperCase());
				if (getIntron && i > 0) {
					SeqFasta seqfastaTmpIntron = getSeq(myChrID, exon.getEndAbs() + 1, lsInfo.get(i-1).getStartAbs() - 1);
					addSep(result, sep);
					result.append(seqfastaTmpIntron.toString().toLowerCase());
				}
			}
		}
		seqFasta.setSeq(result.toString());
		if (strandType == StrandType.trans || (strandType == StrandType.isoForward && !cis5to3)) {
			return seqFasta.reservecom();
		} else {
			return seqFasta;
		}
	}
	
	/**
	 *
	 * <b>尚未测试</b>
	 * seqname = chrID_第一个Align_最后一个Align
	 * 完全兼容gffgeneinfo获得的序列
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param strandType 提取的方向
	 * @param lsInfo 给定的转录本，每一对是一个外显子，必须是cis5to3从小到大排序，trans 从大到小排序
	 * @param sep intron和exon之间的间隔符
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 * @return
	 */
	//TODO 待测试
	private SeqFasta getSeq(StrandType strandType, List<Alignment> lsInfo, String sep, boolean getIntron) {
		String chrID = lsInfo.get(0).getRefID();
		String myChrID = chrID.toLowerCase();
		if (!mapChrID2Length.containsKey(myChrID)) {
			logger.error("no such chromosome: "+myChrID);
			return null;
		}
		
		SeqFasta seqFasta = new SeqFasta();
		if (lsInfo.size() == 1) {
			seqFasta.setName(lsInfo.get(0).toString());
		} else {
			seqFasta.setName(lsInfo.get(0).toString() + "--" + lsInfo.get(lsInfo.size() - 1).toString());
		}

		StringBuilder result = new StringBuilder();
		
		boolean cis5to3 = getStrand(lsInfo);
		if (cis5to3) {
			for (int i = 0; i < lsInfo.size(); i++) {
				Alignment exon = lsInfo.get(i);
				SeqFasta seqfastaTmp = getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs());
				if (seqfastaTmp == null) return null;
//				if (seqfastaTmp == null) continue;
				addSep(result, sep);
				result.append(seqfastaTmp.toString().toUpperCase());
				
				if (getIntron && i < lsInfo.size()-1) {
					SeqFasta seqfastaTmpIntron = getSeq(myChrID, exon.getEndAbs()+1, lsInfo.get(i+1).getStartAbs()-1);
					addSep(result, sep);
					result.append(seqfastaTmpIntron.toString().toLowerCase());
				}
			}
		} else {
			for (int i = lsInfo.size() - 1; i >= 0; i--) {
				Alignment exon = lsInfo.get(i);
				SeqFasta seqfastaTmp = getSeq(myChrID, exon.getStartAbs(), exon.getEndAbs());
				if (seqfastaTmp == null) return null;
//				if (seqfastaTmp == null) continue;
				addSep(result, sep);
				result.append(seqfastaTmp.toString().toUpperCase());
				if (getIntron && i > 0) {
					SeqFasta seqfastaTmpIntron = getSeq(myChrID, exon.getEndAbs() + 1, lsInfo.get(i-1).getStartAbs() - 1);
					addSep(result, sep);
					result.append(seqfastaTmpIntron.toString().toLowerCase());
				}
			}
		}
		seqFasta.setSeq(result.toString());
		if (strandType == StrandType.trans || (strandType == StrandType.isoForward && !cis5to3)) {
			return seqFasta.reservecom();
		} else {
			return seqFasta;
		}
	}
	
	/** 获得这一系列exon的方向<br>
	 * 如果是正向，就应该是从小到大排列<br>
	 * 如果是反向，就应该是从大到小排列<br>
	 * 如果不符合，就抛出异常
	 * @param lsInfo
	 */
	private boolean getStrand(List<? extends Alignment> lsInfo) {
		boolean cis5to3 = lsInfo.get(0).isCis5to3();
		Alignment exonLast = null;
		for (Alignment exonInfo : lsInfo) {
			if (cis5to3 != exonInfo.isCis5to3()) {
				throw new ExceptionSeqFasta("strand is not consistent, first exon strand is: " + cis5to3 + " but exist exon: " + exonInfo.getStartAbs() + " " + exonInfo.getEndAbs() + " " + exonInfo.isCis5to3());
			}
			if (cis5to3 ) {
				if (exonInfo.getStartCis() > exonInfo.getEndCis()) {
					throw new ExceptionSeqFasta("strand is not consistent, strand is: " + cis5to3 + " but exon start bigger than end " + exonInfo.getStartAbs() + " " + exonInfo.getEndAbs() + " " + exonInfo.isCis5to3());
				}
				if (exonLast != null && exonLast.getEndCis() > exonInfo.getStartCis()) {
					throw new ExceptionSeqFasta("strand is not consistent, strand is: " + cis5to3 + " but last exon end bigger than this start " + exonInfo.getStartAbs() + " " + exonInfo.getEndAbs() + " " + exonInfo.isCis5to3());
				}
			} else {
				if (exonInfo.getStartCis() < exonInfo.getEndCis()) {
					throw new ExceptionSeqFasta("strand is not consistent, strand is: " + cis5to3 + " but exon start less than end " + exonInfo.getStartAbs() + " " + exonInfo.getEndAbs() + " " + exonInfo.isCis5to3());
				}
				if (exonLast != null && exonLast.getEndCis() < exonInfo.getStartCis()) {
					throw new ExceptionSeqFasta("strand is not consistent, strand is: " + cis5to3 + " but last exon end less than this start " + exonInfo.getStartAbs() + " " + exonInfo.getEndAbs() + " " + exonInfo.isCis5to3());
				}
			}
			exonLast = exonInfo;
		}
		return cis5to3;
	}
	
	/** 在输入的result后添加sep */
	private void addSep(StringBuilder result, String sep) {
		if (!result.toString().equals("")) {
			result.append(sep);
		}
	}
	/**
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron) {
		 SeqFasta seqFasta = getSeq(StrandType.isoForward, gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.getLsElement(), sep, getIntron);
		 if (seqFasta == null) {
			return null;
		}
		 seqFasta.setName(gffGeneIsoInfo.getName());
		 return seqFasta;
	}
	/**
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * <b>按照List-ExonInfo中的方向，自动提取相对于基因转录方向的序列</b>
	 * 没有则返回一个空的seqfastq
	 * @param cis5To3Iso
	 * @param chrID 染色体
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	@Override
	public SeqFasta getSeq(StrandType strandType, String chrID,List<ExonInfo> lsInfo, boolean getIntron) {
		if (lsInfo.size() == 0) {
			return new SeqFasta("", "");
		}
		 try {
			 return this.getSeq(strandType, chrID, lsInfo, sep,getIntron);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 按顺序提取闭区间序列，每一个区段保存为一个SeqFasta对象
	 * SeqFasta的名字为chrID:起点坐标-终点坐标 都是闭区间
	 * @param refID 序列ID
	 * @param lsInfo 具体的区间
	 * @return
	 */
	public ArrayList<SeqFasta> getRegionSeqFasta(List<LocInfo> lsLocInfos) {
		ArrayList<SeqFasta> lsSeqfasta = new ArrayList<SeqFasta>();
		for (LocInfo locInfo : lsLocInfos) {
			String myChrID = locInfo.getChrID();
			myChrID = myChrID.toLowerCase();
			
			if (!mapChrID2Length.containsKey(myChrID)) {
				logger.error("没有该染色体： "+ locInfo.getChrID());
				return null;
			}
			SeqFasta seqFasta = getSeq(myChrID, locInfo.getStartLoc(), locInfo.getEndLoc(), locInfo.isCis5to3());
			lsSeqfasta.add(seqFasta);
		}
		return lsSeqfasta;
	}
	@Override
	public void getSeq(SiteSeqInfo siteInfo) {
		SeqFasta seqFasta = getSeq(siteInfo.getRefID(), siteInfo.getStartAbs(), siteInfo.getEndAbs());
		siteInfo.setSeq(seqFasta, true);
	}
	
	public SeqFasta getSeq(String seqName) {
		SeqFasta seqFasta = getSeq(seqName, 0 , 0);
		seqFasta.setName(seqName);
		return seqFasta;
	}
	
	public abstract void close();
	
}
