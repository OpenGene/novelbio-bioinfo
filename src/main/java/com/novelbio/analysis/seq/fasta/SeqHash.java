package com.novelbio.analysis.seq.fasta;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.analysis.seq.mapping.Align;
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
	 * 适合小文件，直接读入内存，不建立索引
	 * <b>不会建立文件夹</b><p>
	 * 重名文件就返回，认为一样的东西
	 * @param chrFile 序列文件或序列文件夹
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile, 0)) {
			seqHashAbs =new SeqFastaHash(chrFile, "", true);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, "\\bchr\\w*");
		}
	}
	/**
	 * 适合无重复ID的大文件<br>
	 * <b>如果是单个fasta文件，就会建立索引</b><p>
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 序列名的正则表达式，会用该正则表达式把序列名字过滤，如果没有符合该正则表达式，则返回全名。单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * <br>
	 * 单文件如果为" "表示序列名仅选择空格前面的字段，如">chr1 mouse test" 仅截取"chr1"
	 */
	public SeqHash(String chrFile, String regx) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			seqHashAbs =new ChrSeqHash(chrFile, regx);
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrStringHash(chrFile, regx);
		}
	}
	
	
	
	/**
	 * 返回文件名 
	 * @return
	 */
	public String getChrFile() {
		return seqHashAbs.getChrFile();
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
	
	public SeqFasta getSeq(Align align) {
		if (align == null) {
			return null;
		}
		SeqFasta seqFasta = seqHashAbs.getSeq(align.getRefID(), align.getStartAbs(), align.getEndAbs());
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
	
	@Override
	public Iterable<Character> readBase(String refID) {
		return seqHashAbs.readBase(refID);
	}
	
	public void close() {
		if (seqHashAbs != null) {
			seqHashAbs.close();
		}
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

	/** 不占内存的读取文件中所有序列名 */
	public static List<String> getLsSeqName(String fastaFile) {
		List<String> lsName = new ArrayList<>();
		if (!FileOperate.isFileExistAndBigThanSize(fastaFile, 0)) {
			return lsName;
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(fastaFile);
		for (String content : txtRead.readlines()) {
			content = content.trim();
			if (content.startsWith(">")) {
				lsName.add(content.replace(">", "").trim());
			}
		}
		txtRead.close();
		return lsName;
	}
	
}
