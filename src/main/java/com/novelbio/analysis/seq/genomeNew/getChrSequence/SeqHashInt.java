package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public interface SeqHashInt {
	/**
	 * 设定常规信息
	 * @param CaseChange 是否将序列名转变为小写
	 * @param regx 序列名的正则表达式，在读取ChromFa文件夹时使用，用于抓取文件夹中的所有序列文件，null不设定
	 * 读取Chr文件夹的时候默认设定了 "\\bchr\\w*"
	 * @param append 读取ChrID的时候没用
	 * @param chrPattern 当输入类似chr1:1123-4567数据时将chr1提取出来的正则表达式
	 */
	public void setInfo(boolean CaseChange, String regx,boolean append, String chrPattern) ;
	
	/**
	 * 返回chrID和chrLength的对应关系
	 * chrID通通小写
	 * @return
	 */
	public HashMap<String, Long> getHashChrLength();
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
	 * 具体读取文件
	 */
	public void setFile();
	
	/**
	 * 当设定Chr文件后，可以将序列长度输出到文件 输出文件为 chrID(小写)+“\t”+chrLength+换行 不是顺序输出
	 * 
	 * @param outFile
	 *            待输出的文件名，带上全部路径
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) ;
	public String getSeq(String chrID, long startlocation, long endlocation) throws IOException ;
	
	
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
	public String getSeq(boolean cisseq, String chrID, long startlocation, long endlocation);

	/**
	 * 给出染色体编号位置和方向返回序列<br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param chrlocation染色体编号方向如
	 *            ：Chr:1000-2000,自动将chrID小写,chrID采用正则表达式抓取，无所谓大小写，会自动转变为小写
	 * @param cisseq方向
	 *            ，true:正向 false:反向互补
	 */
	public String getSeq(String chrlocation, boolean cisseq);

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
			boolean cisseq);

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap);
	/**
	 * <br>
	 * 提取序列为闭区间，即如果提取30-40bp那么实际提取的是从30开始到40结束的11个碱基
	 * @param cisseq 正反向
	 * @param lsInfo ArrayList-int[] 给定的转录本，每一对是一个外显子
	 * @param getIntron 是否提取内含子区域，True，内含子小写，外显子大写。False，只提取外显子
	 */
	public String getSeq(boolean cisseq, String chrID,ArrayList<int[]> lsInfo, boolean getIntron);
	

}
