package com.novelbio.bioinfo.fastq;

import org.apache.log4j.Logger;

public abstract class FQrecordFilter implements FQrecordCopeInt {
	private static final Logger logger = Logger.getLogger(FQrecordFilter.class);
	
	int readsMinLen = 20;
	int readsMaxLen = -1;
	int fastqOffset;

	/** 裁剪序列时最短为多少， 默认为20
	 */
	public void setTrimMinLen(int trimMinLen) {
		if (trimMinLen < 5) {
			trimMinLen = 5;
		}
		this.readsMinLen = trimMinLen;
	}
	/** 裁剪序列时最短为多少， 默认为20
	 */
	public void setTrimMaxLen(int readsMaxLen) {
		this.readsMaxLen = readsMaxLen;
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
	 * 成功过滤则返回 true，并且输入的fastQRecord也被过滤
	 * reads质量太差或过滤失败则返回false
	 * @return
	 */
	public boolean copeReads(FastQRecord fastQRecord) {
		int start = trimLeft(fastQRecord);
		int end = trimRight(fastQRecord);
		
		if (start < 0 && end < 0) {
			int len = fastQRecord.getLength();
			if (len < readsMinLen || (readsMaxLen > 0 && len > readsMaxLen)) {
				return false;
			} else {
				return true;
			}
		}
		
		return trimSeq(fastQRecord, readsMinLen, readsMaxLen, start, end);
	}
	
	/**
	 * 获得过滤左端的Num，可以用substring截取<br>
	 * 如果trimLeft和trimRight的结果都小于0，则说明不用trim<br>
	 * 如果trimLeft和trimRight的结果都等于0，则说明没有通过过滤
	 * @return
	 */
	protected abstract int trimLeft(FastQRecord fastQRecord);
	
	/**
	 * 获得过滤左端的Num，可以用substring截取<br>
	 * 如果trimLeft和trimRight的结果都小于0，则说明不用trim<br>
	 * 如果trimLeft和trimRight的结果都等于0，则说明没有通过过滤
	 * @return
	 */
	protected abstract int trimRight(FastQRecord fastQRecord);
	
	/**
	 * 本过滤器是否参与过滤
	 * @return
	 */
	public abstract boolean isUsing();
	
	/**
	 * 给定左右的坐标，然后将seqfasta截短
	 * 如果start为0，end为seqLength，则直接返回true，表示不过滤
	 * @param start 和substring一样的用法
	 * @param end 和substring一样的用法
	 * @return 返回截短后的string
	 * 如果截短后的长度小于设定的最短reads长度，那么就返回false
	 */
	public static boolean trimSeq(FastQRecord fastQRecord, int readsMinLen, int readsMaxLen, int start, int end) {
		if (start > end) {
			start = end;
		}
		int len = end - start;
		if (len < readsMinLen || (readsMaxLen > 0 && len > readsMaxLen)) {
			return false;
		}
		if (start == 0 && end == fastQRecord.seqQuality.length()) {
			return true;
		}
		try {
			fastQRecord.seqFasta = fastQRecord.seqFasta.trimSeq(start, end);
			fastQRecord.seqQuality = fastQRecord.seqQuality.substring(start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
    
}
