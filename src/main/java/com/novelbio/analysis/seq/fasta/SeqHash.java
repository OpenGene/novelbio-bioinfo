package com.novelbio.analysis.seq.fasta;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class SeqHash implements SeqHashInt {
	private static Logger logger = Logger.getLogger(SeqHash.class);
	
	SeqHashAbs seqHashAbs = null;
	/**
	 * 结果的文件是否转化为小写
	 * True：小写 False：大写
	 * null：不变
	 */
	Boolean TOLOWCASE = null;
	
	/** 有些提取的基因会明显超过染色体范围，这种序列是否提取 */
	boolean isGetOutBoundSeq = true;
	
	public SeqHash(SeqHashAbs seqHashAbs) {
		this.seqHashAbs = seqHashAbs;
	}
	
	/**
	 * 小于100MB的直接读入内存，不建立索引
	 * 大于100MB的建索引读
	 * <b>不会建立文件夹</b><p>
	 * 重名文件就返回，认为一样的东西
	 * @param chrFile 序列文件或序列文件夹
	 * @param TOLOWCASE 是否将最后结果的序列转化为小写 True：小写，False：大写，null不变 默认为null
	 * 默认为false
	 */
	public SeqHash(String chrFile) {
		initial(chrFile, " ");
	}
	/**
	 * 适合无重复ID的文件，每一行的序列要相等<br>
	 * 文件小于100M就读入内存
	 * <b>如果是单个fasta文件，就会建立索引</b><p>
	 * @param chrFile 序列文件或序列文件夹
	 * @param regx 序列名的正则表达式，会用该正则表达式把序列名字过滤，如果没有符合该正则表达式，则返回全名。单文件默认为"";文件夹默认为"\\bchr\\w*"；
	 * <br>
	 * 单文件如果为" "表示序列名仅选择空格前面的字段，如">chr1 mouse test" 仅截取"chr1"
	 */
	public SeqHash(String chrFile, String regx) {
		initial(chrFile, regx);
	}
	
	
	
	private void initial(String chrFile, String regx) {
		if (FileOperate.isFileExistAndBigThanSize(chrFile,1)) {
			long fileSize = FileOperate.getFileSizeLong(chrFile);
			if (fileSize > 100_000_000) {
				seqHashAbs =new ChrSeqHash(chrFile, regx);
			} else {
				seqHashAbs =new SeqFastaHash(chrFile, regx, true);
			}
		}
		if (FileOperate.isFileDirectory(chrFile)) {
			seqHashAbs = new ChrFoldHash(chrFile, regx);
		}
	}
	
	/** 有些提取的基因会明显超过染色体范围，这种序列是否提取 */
	public void setGetOutBoundSeq(boolean isGetOutBoundSeq) {
		this.isGetOutBoundSeq = isGetOutBoundSeq;
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
	public Long getChrLength(String chrID) {
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
		SeqFasta seqFasta = seqHashAbs.getSeq(seqName);
		seqFasta.setName(seqName);
		return seqFasta;
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
	public SeqFasta getSeq(StrandType strandType, String chrID, List<ExonInfo> lsInfo, boolean getIntron) {
		return seqHashAbs.getSeq(strandType, chrID, lsInfo, getIntron);
	}
	/**
	 * 提取序列块
	 * @param length 每个块多大的长度
	 * @param chrID 染色体ID
	 * @param chrunk 第几个块
	 * @return
	 */
	public SeqFasta getSeq(int length, String chrID, int chrunk) {
		long start = length * chrunk + 1;
		long end = start + length;
		return getSeq(chrID, start, end);
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
		Long chrLen = getChrLength(gffGeneIsoInfo.getRefID());
		if (chrLen == null) return null;
		
		if (gffGeneIsoInfo.getStartAbs() < 0 || gffGeneIsoInfo.getEndAbs() > chrLen) {
//			logger.error("out of bound, the gene location is: " + gffGeneIsoInfo.getStartAbs() + "-" + gffGeneIsoInfo.getEndAbs() + ", but the chromosome length is: " + chrLen);
			if (!isGetOutBoundSeq) {
				return null;
			}
		}

		SeqFasta seqFasta = seqHashAbs.getSeq(gffGeneIsoInfo, getIntron);
		if (seqFasta != null) {
			seqFasta.setTOLOWCASE(TOLOWCASE);
		}
		return seqFasta;
	}
	
	public SAMSequenceDictionary getDictionary() {
		SAMSequenceDictionary samSequenceDictionary = new SAMSequenceDictionary();
		for (String chrId : getLsSeqName()) {
			SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(chrId, getChrLength(chrId).intValue());
			samSequenceDictionary.addSequence(samSequenceRecord);
		}
		return samSequenceDictionary;
	}
	
	public static Map<String, Long> getMapChrId2Len(String seqFai) {
		Map<String, Long> mapChrId2Len = new LinkedHashMap<>();
		SAMSequenceDictionary samSequenceDictionary = getDictionaryFromFai(seqFai, " ");
		for (SAMSequenceRecord samSequenceRecord : samSequenceDictionary.getSequences()) {
			mapChrId2Len.put(samSequenceRecord.getSequenceName(), (long)samSequenceRecord.getSequenceLength());
		}
		return mapChrId2Len;
	}
	
	public static SAMSequenceDictionary getDictionary(String fasta) {
		return getDictionaryFromFai(SamIndexRefsequence.getIndexFile(fasta), " ");
	}
	
	public static SAMSequenceDictionary getDictionaryFromFai(String seqFai) {
		return getDictionaryFromFai(seqFai, " ");
	}
	
	public static SAMSequenceDictionary getDictionaryFromFai(String seqFai, String regx) {
		FileOperate.validateFileExistAndBigThan0(seqFai);
		
		Map<String, Long> mapChrId2Len = new LinkedHashMap<>();
		PatternOperate patternOperate = null;
		if (regx != null && !regx.equals("") && !regx.equals(" ")) {
			patternOperate = new PatternOperate(regx, false);
		}
		
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFai);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String chrID = null;
			if (" ".equals(regx)) {
				chrID = ss[0].split(" ")[0];
			} else if (patternOperate != null) {
				chrID = patternOperate.getPatFirst(ss[0]);
				if (chrID == null) {
					chrID = ss[0];
				}
			} else {
				chrID = ss[0];
			}
			long length = Long.parseLong(ss[1].trim());
			mapChrId2Len.put(chrID, length);
		}
		txtRead.close();
		
		SAMSequenceDictionary samSequenceDictionary = new SAMSequenceDictionary();
		for (String chrId : mapChrId2Len.keySet()) {
			SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(chrId, mapChrId2Len.get(chrId).intValue());
			samSequenceDictionary.addSequence(samSequenceRecord);
		}
		return samSequenceDictionary;
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
