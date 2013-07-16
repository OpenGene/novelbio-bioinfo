package com.novelbio.analysis.seq.fasta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;

public interface SeqHashInt {
	/**
	 * 返回chrID和chrLength的对应关系
	 * chrID通通小写
	 * @return
	 */
	public HashMap<String, Long> getMapChrLength();
	/**
	 * 在读取chr长度文件后，可以通过此获得所有chr的长度信息
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr长度 并且按照chr长度从小到大排序
	 */
	public ArrayList<String[]> getChrLengthInfo();
	
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLength(String chrID) ;
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMin() ;
	/**
	 * 在读取chr长度文件后，可以通过此获得每条chr的长度
	 * @param chrID 内部自动转换为小写
	 * @return
	 */
	public long getChrLenMax() ;
	/**
	 * 指定最长染色体的值，返回按比例每条染色体相应值下染色体的坐标数组,resolution和int[resolution]，可用于画图
	 * 那么resolution就是返回的int[]的长度
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception ;
	
	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * 
	 * @param outFile
	 *            待输出的文件名，带上全部路径
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) ;
	/**
	 * 获得所有序列的名字
	 * @return
	 */
	public ArrayList<String> getLsSeqName();
	
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation);
	/**
	 * * 输入染色体list信息 输入序列坐标以及是否为反向互补,其中ChrID为 chr1，chr2，chr10类型 返回序列
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param cisseq 正反向
	 * @param chrID 目的染色体名称，用来在哈希表中查找具体某条染色体
	 * @param startlocation 序列起点
	 * @param endlocation 序列终点
	 * @return
	 * 没有就返回null
	 */
	public SeqFasta getSeq(Boolean cisseq, String chrID, long startlocation, long endlocation);
	/**
	 * 给出peak位点，查找指定范围的sequence，根据CaseChange改变大小写
	 * <br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param chr
	 *            ,
	 * @param peaklocation peak summit点坐标
	 * @param region peak左右的范围
	 * @param cisseq true:正向链 false：反向互补链
	 */
	public SeqFasta getSeq(String chr, int peaklocation, int region, boolean cisseq);

	/**
	 * 
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * <b>按照List-ExonInfo中的方向，自动提取相对于基因转录方向的序列</b>
	 * @param cis5to3All  正反向，输入的转录本的方向，null则使用第一个exon的方向
	 * @param chrID
	 * @param lsInfo 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 * @return 没有染色体或序列超出范围则返回null
	 */
	public SeqFasta getSeq(Boolean cis5to3Iso, String chrID, List<ExonInfo> lsInfo, boolean getIntron);
	/**
	 * 
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基<br>
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param cis5to3All  正反向，输入的转录本的方向，null则使用第一个exon的方向
	 * @param chrID
	 * @param start 实际第几个exon
	 * @param end 实际第几个exon
	 * @param lsInfo
	 * @param getIntron 是否获取内含子，内含子自动小写
	 * @return
	 */
	SeqFasta getSeq(Boolean cis5to3Iso, String chrID, int start, int end, List<ExonInfo> lsInfo, boolean getIntron);
	
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron);
	/**
	 * 外显子之间用什么分割
	 * @param sep
	 */
	void setSep(String sep);
	/**
	 * 是否要设定为DNA，也就是将序列中的U全部转化为T
	 */
	public void setDNAseq(boolean isDNAseq);
	/**
	 * 根据给定的mapInfo，获得序列，注意序列并没有根据cis5to3进行反向
	 * 会用mapInfo的名字替换seqfasta的名字
	 * @param mapInfo
	 */
	void getSeq(SiteSeqInfo mapInfo);
	/** 从头到尾遍历某条序列上的碱基 */
	Iterable<Character> readBase(String refID);
	
	/** 设定最长可以读取的序列长度 */
	void setMaxExtractSeqLength(int maxSeqLen);
	
}
