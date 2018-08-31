package com.novelbio.bioinfo.fastq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;
import uk.ac.babraham.FastQC.Sequence.SequenceFormatException;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;

public class FastQRecord implements Cloneable {
	private static final Logger logger = Logger.getLogger(FastQRecord.class);
	/** 万一fastq没有名字，就给它随机加个名字 */
	static String SEQNAME = "Novelbio";
	static long i = 0;
	
	/** fastQ里面asc||码的指标与个数 */
	HashMap<Integer, Integer> mapFastQFilter;
	
	SeqFasta seqFasta = new SeqFasta();
	protected int fastqOffset = FastQ.FASTQ_SANGER_OFFSET;
	protected String seqQuality = "";
	
	/** 如果过滤出错，就要用这个重新设定quality，全部设置为f */
	boolean modifyQuality = false;
	
	public FastQRecord() {
		seqFasta = new SeqFasta();
		seqFasta.setTOLOWCASE(null);
	}
	/**
	 * 每四行一个记录，将这四行用linux回车隔开，然后输入
	 * @param fastqlines
	 */
	public FastQRecord(String fastqlines, int fastqOffset) {
		this(fastqlines, fastqOffset, true);
	}
	/** 读入fastq文件但根据需要进行初始化
	 * 用在fastq过滤的时候，可以先不初始化，然后在多线程的时候进行初始化
	 *  */
	protected FastQRecord(List<String> lsFqInfo, int fastqOffset) {
		this.fastqOffset = fastqOffset;
		String[] fastqInfo = new String[4];
		int i = 0;
		for (String string : lsFqInfo) {
			fastqInfo[i++] = string;
		}
		if (i != 4) {
			throw new ExceptionFastq("fastq format error");
		}
		initialReadRecord(fastqInfo);
	}
	/** 读入fastq文件但根据需要进行初始化
	 * 用在fastq过滤的时候，可以先不初始化，然后在多线程的时候进行初始化
	 *  */
	protected FastQRecord(String[] ss, int fastqOffset, boolean initial) {
		this.fastqOffset = fastqOffset;
		initialReadRecord(ss);
	}
	/** 读入fastq文件但根据需要进行初始化
	 * 用在fastq过滤的时候，可以先不初始化，然后在多线程的时候进行初始化
	 *  */
	protected FastQRecord(String fastqlines, int fastqOffset, boolean initial) {
		this.fastqOffset = fastqOffset;
		String[] fastqInfo = fastqlines.split(TxtReadandWrite.ENTER_LINUX);
		if (fastqInfo.length == 1) {
			fastqInfo = fastqlines.split(TxtReadandWrite.ENTER_WINDOWS);
		}
		initialReadRecord(fastqInfo);
	}
	/**初始化读入的数据 */
	protected void initialReadRecord(String[] fastqInfo) {
		if (seqFasta.getSeqName() != null) {
			return;
		}
		if (!fastqInfo[0].startsWith("@") || fastqInfo.length != 4 || !fastqInfo[2].startsWith("+")) {
			throw new ExceptionFastq("fastq format error");
		}
		if (fastqInfo[1].length() != fastqInfo[3].length()) {
			throw new ExceptionFastq("fastq format error");
		}
		char[] seq = fastqInfo[1].toLowerCase().toCharArray();
		for (char c : seq) {
			if (c < 97 || c > 122) {
				throw new ExceptionFastq("fastq format error");
			}
		}
		
		String seqName = fastqInfo[0].substring(1).trim();
		if (seqName == null || seqName.equals("")) {
			seqName = SEQNAME + i;
			i ++;
		}
		seqFasta.setName(seqName);
		seqFasta.setSeq(fastqInfo[1]);
		if (fastqInfo[1].length() == 0) {
			setFastaQuality("");
		} else {
			setFastaQuality(fastqInfo[3]);
		}
	}
	protected void setMapFastqFilter(HashMap<Integer, Integer> mapFastQFilter) {
		this.mapFastQFilter = mapFastQFilter;
	}
	public void setName(String SeqName) {
		seqFasta.setName(SeqName);
	}
	public void setSeq(String Seq) {
		seqFasta.setSeq(Seq);
	}
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}
	public String getName() {
		return seqFasta.getSeqName();
	}
	/**
	 * 设定序列质量字符串，用phred格式设定
	 * @param fastaQuality
	 */
	public void setFastaQuality(String fastaQuality) {
		char[] quality = fastaQuality.toCharArray();
		if (fastqOffset == FastQ.FASTQ_SANGER_OFFSET) {
			for (int i = 0; i < quality.length; i++) {
				if (quality[i] <= 33 ) {
					quality[i] = 34;
				} else if (quality[i] >= 125) {
					quality[i] = 125;
				}
			}
		} else {
			for (int i = 0; i < quality.length; i++) {
				if (quality[i] <= 64 ) {
					quality[i] = 65;
				} else if (quality[i] >= 125) {
					quality[i] = 125;
				}
			}
		}
		
		this.seqQuality = String.copyValueOf(quality);
	}
	public String getSeqQuality() {
		return seqQuality;
	}
	/** 
	 * 重置fastQuality<br>
	 * 
	 * 如果过滤出错，就要用这个重新设定quality，全部设置为f
	 */
	public void setModifyQuality(boolean modifyQuality) {
		this.modifyQuality = modifyQuality;
	}
	public int getLength() {
		return seqFasta.Length();
	}
	/**
	 * 设定偏移
	 * FASTQ_SANGER_OFFSET
	 * @param fastqOffset
	 */
	public void setFastqOffset(int fastqOffset) {
		this.fastqOffset = fastqOffset;
	}

	/**
	 * 返回fastq格式的文本
	 * @return
	 */
	public String toString() {
		if (modifyQuality) {
			char[] quality = new char[seqFasta.Length()];
			if (fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET) {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'f';
				}
			} else {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'A';
				}
			}
			seqQuality = String.copyValueOf(quality);
		}
		int seqLen = seqFasta.Length();
		int qualityLen = seqQuality.length();
		if (seqLen != qualityLen) {
			if (seqLen < qualityLen) {
				seqQuality = seqQuality.substring(0, seqLen);
			} else if (seqLen > qualityLen) {
				char[] qualityAppend = new char[seqLen - qualityLen];
				if (fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET) {
					for (int i = 0; i < qualityAppend.length; i++) {
						qualityAppend[i] = 'd';
					}
				} else {
					for (int i = 0; i < qualityAppend.length; i++) {
						qualityAppend[i] = 'F';
					}
				}
				seqQuality = seqQuality + String.copyValueOf(qualityAppend);
			}
		}
		if (!isValidate()) {
			return null;
		}
		return "@" + seqFasta.getSeqName() + TxtReadandWrite.ENTER_LINUX + seqFasta.toString() + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + seqQuality;
	}
	
	public boolean isValidate() {
		if (seqFasta == null || seqFasta.getSeqName() == null || seqFasta.Length() < 10) {
			return false;
		}
		return true;
	}
	
	/**
	 * 转换成fastqc的序列，方便其进行处理
	 * @param fileName 输入文件名，最后统计时候用到
	 * @return
	 */
	public Sequence toFastQCsequence() {
		Sequence sequence = new Sequence(null, seqFasta.toString(), seqQuality, "@" + seqFasta.getSeqName());
		sequence.setIsFiltered(true);
		return sequence;
	}
	
	/**
	 * 克隆序列
	 */
	public FastQRecord clone() {
		FastQRecord fastQRecord = null;
		try {
			fastQRecord = (FastQRecord) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		fastQRecord.seqFasta = seqFasta.clone();
		fastQRecord.seqQuality = seqQuality;
		fastQRecord.fastqOffset = fastqOffset;
		fastQRecord.mapFastQFilter = mapFastQFilter;
		return fastQRecord;
	}
	
	public static boolean isPairedByName(FastQRecord fq1, FastQRecord fq2) {
		if (fq1 == null || fq2 == null) {
			return false;
        }
		String name1 = fq1.getName(), name2 = fq2.getName();
		if (StringOperate.isRealNull(name1) || StringOperate.isRealNull(name2)) {
			return false;
        }
		String lname = name1.split(" ")[0].split("#")[0];
		String rname = name2.split(" ")[0].split("#")[0];
		List<char[]> lsDif = new ArrayList<>();
		char[] lchr = lname.toCharArray();
		char[] rchr = rname.toCharArray();
		if (lchr.length != rchr.length) {
			return false;
		}
		for (int i = 0; i < lchr.length; i++) {
			char l = lchr[i];
			char r = rchr[i];
			if (l != r) {
				lsDif.add(new char[] {l, r});
			}
		}
		if (lsDif.isEmpty()) {
			return true;
		}
		if (lsDif.size() > 1) {
			return false;
		}
		char[] pair = lsDif.get(0);
		return pair[0] == '1' && pair[1] == '2';
	}
	
}
