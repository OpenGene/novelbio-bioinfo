package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FastQRecord implements Cloneable {
	private static Logger logger = Logger.getLogger(FastQRecord.class);
	/** 万一fastq没有名字，就给它随机加个名字 */
	static String SEQNAME = "Novelbio";
	static long i = 0;
	
	/** fastQ里面asc||码的指标与个数 */
	HashMap<Integer, Integer> mapFastQFilter;
	
	/** 读入的文本，仅仅用于初始化 */
	String fastqStringReadIn;

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
	protected FastQRecord(String fastqlines, int fastqOffset, boolean initial) {
		fastqStringReadIn = fastqlines;
		this.fastqOffset = fastqOffset;
		if (initial) {
			initialReadRecord();
		}
	}
	/**初始化读入的数据 */
	protected void initialReadRecord() {
		if (seqFasta.getSeqName() != null) {
			return;
		}
		String[] ss = fastqStringReadIn.split(TxtReadandWrite.ENTER_LINUX);
		if (ss.length == 1) {
			ss = fastqStringReadIn.split(TxtReadandWrite.ENTER_WINDOWS);
		}
		String seqName = ss[0].substring(1).trim();
		if (seqName == null || seqName.equals("")) {
			seqName = SEQNAME + i;
			i ++;
		}
		seqFasta.setName(seqName);
		seqFasta.setSeq(ss[1]);
		setFastaQuality(ss[3]);
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
	/** 如果过滤出错，就要用这个重新设定quality，全部设置为f */
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
		if (seqQuality.length() != seqFasta.Length() || modifyQuality) {
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
		return "@" + seqFasta.getSeqName() + TxtReadandWrite.ENTER_LINUX + seqFasta.toString() + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + seqQuality;
	}
	
	/**
	 * 克隆序列
	 */
	public FastQRecord clone() {
		FastQRecord seqFasta = null;
		try {
			seqFasta = (FastQRecord) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		seqFasta.seqQuality = seqQuality;
		seqFasta.fastqOffset = fastqOffset;
		seqFasta.mapFastQFilter = mapFastQFilter;
		return seqFasta;
	}

}
