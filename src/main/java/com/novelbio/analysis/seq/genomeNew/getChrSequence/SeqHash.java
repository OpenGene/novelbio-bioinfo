package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{
private static Logger logger = Logger.getLogger(SeqHash.class);
	String chrFile = "";
	SeqHashAbs seqHashAbs = null;
	/**
	 * 重名文件就返回，认为一样的东西
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile)
	{
		if (this.chrFile.equals(chrFile)) {
			return;
		}
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*", true);
		}
	}
	/**
	 * 返回文件名 
	 * @return
	 */
	public String getChrFile() {
		return chrFile;
	}
	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile, String regx)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, true);
		}
	}
	
	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile, String regx,Boolean TOLOWCASE)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, true);
		}
		this.TOLOWCASE = TOLOWCASE;
	}
	
	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param CaseChange 是否将序列名小写 默认小写
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * @param append <b>仅针对单个文本的序列</b>对于相同名称序列的处理，
	 * true：如果出现重名序列，则在第二条名字后加上"<"作为标记 false：如果出现重名序列，则用长的序列去替换短的序列
	 * 默认为false
	 */
	public SeqHash(String chrFile, String regx, boolean CaseChange,Boolean TOLOWCASE, boolean append)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, CaseChange, append);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx, CaseChange);
		}
		this.TOLOWCASE = TOLOWCASE;
	}
	

	/**
	 * 结果的文件是否转化为大小写
	 * True：小写
	 * False：大写
	 * null：不变
	 */
	Boolean TOLOWCASE = null;
	@Override
	public LinkedHashMap<String, Long> getHashChrLength() {
		return seqHashAbs.getHashChrLength();
	}
	
	@Override
	public ArrayList<String[]> getChrLengthInfo() {
		return seqHashAbs.getChrLengthInfo();
	}

	@Override
	public long getChrLength(String chrID) {
		return seqHashAbs.getChrLength(chrID);
	}

	@Override
	public long getChrLenMin() {
		return seqHashAbs.getChrLenMin();
	}

	@Override
	public long getChrLenMax() {
		return seqHashAbs.getChrLenMax();
	}

	@Override
	public int[] getChrRes(String chrID, int maxresolution) throws Exception {
		return seqHashAbs.getChrRes(chrID, maxresolution);
	}

	@Override
	public void saveChrLengthToFile(String outFile) {
		seqHashAbs.saveChrLengthToFile(outFile);
		
	}
	@Override
	public ArrayList<String> getLsSeqName() {
		return seqHashAbs.getLsSeqName();
	}
	/////////////////////  提 取 序 列  /////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation)
	{
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, startlocation, endlocation);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cisseq, chrID, startlocation, endlocation);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chr, peaklocation, region, cisseq);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}


	@Override
	public SeqFasta getSeq(boolean cisseq, String chrID, List<ExonInfo> lsInfo,
			boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cisseq, chrID, lsInfo, getIntron);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(String chrID, List<ExonInfo> lsInfo,
			boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, lsInfo, getIntron);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(String chrID, boolean cisseq, int start, int end,
			List<ExonInfo> lsInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, cisseq, start, end, lsInfo, getIntron);
		seqFasta.setTOLOWCASE(TOLOWCASE);
		return seqFasta;
	}
	//////////////////////  static method  ////////////////////////////////////////////////////////////////////////////////
	/**
	 * 根据TOLOWCASE的选项，返回相应的seq序列
	 * @param seq
	 * @param TOLOWCASE null：不变，false：大写，true：小写
	 * @return
	 */
	private static String getSeqCase(String seq, Boolean TOLOWCASE)
	{
		if (TOLOWCASE == null) {
			return seq;
		}
		else {
			return TOLOWCASE.equals(true) ?  seq.toLowerCase() :  seq.toUpperCase();
		}
	}

	/**
	 * 判断输入的长度是否在目的区间内，闭区间
	 * @param seqlen 输入序列的长度
	 * @param len
	 * 	int[2] :0：下限，小于0表示没有下限
	 * 1：上限，小于0表示没有上限
	 * 上限必须大于等于下限，如果上限小于下限，则报错
	 * @return
	 */
	public static boolean testSeqLen(int seqlen, int[] len) {
		if (len[1] > 0 && len[1] < len[0]) {
			logger.error("要求输出序列的长度上限不能小于下限");
		}
		
		if (len[0] <= 0) { //无下限
			if (len[1] <= 0)  //无上限
				return true;
			else { //有上限
				if (seqlen <= len[1])  //长度小于等于上限
					return true;
				else
					// 长度大于上限
					return false;
			}
		}
		else // 有下限
		{
			if (seqlen < len[0]) //长度小于下限
				return false;
			else {  //长度大于下限
				if (len[1] > 0) { //有上限
					if (seqlen <= len[1]) //长度小于上限
						return true;
					else
						return false;
				}
				else {
					return true;
				}
			}
		}
	}
	@Override
	public void setSep(String sep) {
		seqHashAbs.setSep(sep);
		
	}



	
}
