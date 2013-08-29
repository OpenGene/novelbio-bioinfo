package com.novelbio.analysis.seq.fasta;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt, Closeable {
	private static Logger logger = Logger.getLogger(SeqHash.class);
	
	SeqHashAbs seqHashAbs = null;
	/**
	 * 结果的文件是否转化为小写
	 * True：小写 False：大写
	 * null：不变
	 */
	Boolean TOLOWCASE = null;
	
	/**
	 * 重名文件就返回，认为一样的东西
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,0.001)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*");
		}
	}
	/**
	 * 返回文件名 
	 * @return
	 */
	public String getChrFile() {
		return seqHashAbs.getChrFile();
	}
	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 */
	public SeqHash(String chrFile, String regx) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx);
		}
	}
	
	/**
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 如果是序列文件，则用该正则表达式提取每个序列的名字，如果是序列文件夹，则用该正则表达式提取含有该文件名的文件
	 * 单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile, String regx,Boolean TOLOWCASE) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, true);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx);
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
	public SeqHash(String chrFile, String regx,Boolean TOLOWCASE, boolean append) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new SeqFastaHash(chrFile, regx, append);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx);
		}
		this.TOLOWCASE = TOLOWCASE;
	}

	@Override
	public LinkedHashMap<String, Long> getMapChrLength() {
		return seqHashAbs.getMapChrLength();
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
	public SeqFasta getSeq(String seqName) {
		return seqHashAbs.getSeq(seqName, 0 , 0);
	}
	
	@Override
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chrID, startlocation, endlocation);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(Boolean cisseq, String chrID, long startlocation, long endlocation) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cisseq, chrID, startlocation, endlocation);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}

	@Override
	public SeqFasta getSeq(String chr, int peaklocation, int region, boolean cisseq) {
		SeqFasta seqFasta = seqHashAbs.getSeq(chr, peaklocation, region, cisseq);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	/**
	 * 根据给定的mapInfo，获得序列，注意序列会根据cis5to3进行反向
	 * 自动给seqfasta添加名字
	 * @param mapInfo
	 */
	@Override
	public void getSeq(SiteSeqInfo mapInfo) {
		seqHashAbs.getSeq(mapInfo);
	}
	/**
	 * 根据给定的mapInfo，获得序列，注意序列会根据cis5to3进行反向
	 * @param mapinfoRefSeqIntactAA
	 */
	public void getSeq(ArrayList<? extends SiteSeqInfo> lsMapInfos) {
		for (SiteSeqInfo mapInfo : lsMapInfos) {
			getSeq(mapInfo);
		}
	}

	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(gffGeneIsoInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(Boolean cis5to3All, String chrID, List<ExonInfo> lsInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cis5to3All, chrID, lsInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public SeqFasta getSeq(Boolean cis5to3All, String chrID, int start, int end, List<ExonInfo> lsInfo, boolean getIntron) {
		SeqFasta seqFasta = seqHashAbs.getSeq(cis5to3All, chrID, start, end, lsInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	@Override
	public void setSep(String sep) {
		seqHashAbs.setSep(sep);
		
	}
	@Override
	public void setDNAseq(boolean isDNAseq) {
		seqHashAbs.setDNAseq(isDNAseq);
	}
	@Override
	public void setMaxExtractSeqLength(int maxSeqLen) {
		seqHashAbs.setMaxExtractSeqLength(maxSeqLen);
	}
	//////////////////////  static method  ////////////////////////////////////////////////////////////////////////////////}

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
		else {// 有下限
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
	/**
	 * 给定fasta文件，假设该fasta文件里面只有一种序列，蛋白或核酸，判断该fasta文件是蛋白，还是DNA，还是RNA
	 * @return
	 * SeqFasta.SEQ_DNA等
	 */
	public static int getSeqType(String fastaFile) {
		int readBp = 1000;//读取前1000个碱基来判断
		TxtReadandWrite txtRead = new TxtReadandWrite(fastaFile);
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : txtRead.readlines()) {
			if (string.trim().startsWith(">")) {
				continue;
			}
			stringBuilder.append(string);
			if (stringBuilder.length() > readBp) {
				break;
			}
		}
		SeqFasta seqFasta = new SeqFasta("test", stringBuilder.toString());
		txtRead.close();
		return seqFasta.getSeqType();		
	}
	
	@Override
	public Iterable<Character> readBase(String refID) {
		return seqHashAbs.readBase(refID);
	}
	
	public void close() {
		seqHashAbs.close();
	}
	
}
