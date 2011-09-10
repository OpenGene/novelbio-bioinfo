package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt{

	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile)
	{
		if (FileOperate.isFile(chrFile)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true, false);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*", true);
		}
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
	
	SeqHashAbs seqHashAbs = null;
	/**
	 * 结果的文件是否转化为大小写
	 * True：小写
	 * False：大写
	 * null：不变
	 */
	Boolean TOLOWCASE = null;
	@Override
	public HashMap<String, Long> getHashChrLength() {
		return seqHashAbs.getHashChrLength();
	}

	@Override
	public ArrayList<String[]> getChrLengthInfo() {
		seqHashAbs.getChrLengthInfo();
		return null;
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
	public String getSeq(String chrID, long startlocation, long endlocation)
			throws IOException {
		return getSeqCase(seqHashAbs.getSeq(chrID, startlocation, endlocation),TOLOWCASE);
	}

	@Override
	public String getSeq(boolean cisseq, String chrID, long startlocation,
			long endlocation) {
		return getSeqCase(seqHashAbs.getSeq(cisseq, chrID, startlocation, endlocation),TOLOWCASE);
	}

	@Override
	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq) {
		return getSeqCase(seqHashAbs.getSeq(chr, peaklocation, region, cisseq),TOLOWCASE);
	}


	@Override
	public String getSeq(boolean cisseq, String chrID, ArrayList<int[]> lsInfo,
			boolean getIntron) {
		return getSeqCase(seqHashAbs.getSeq(cisseq, chrID, lsInfo, getIntron),TOLOWCASE);
	}
	/**
	 * 根据TOLOWCASE的选项，返回相应的seq序列
	 * @param seq
	 * @param TOLOWCASE null：不变，false：大写，true：小写
	 * @return
	 */
	static private String getSeqCase(String seq, Boolean TOLOWCASE)
	{
		if (TOLOWCASE == null) {
			return seq;
		}
		else {
			return TOLOWCASE.equals(true) ?  seq.toLowerCase() :  seq.toUpperCase();
		}
	}


}
