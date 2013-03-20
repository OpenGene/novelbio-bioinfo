package com.novelbio.analysis.seq.fastq;

import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

class FastQwrite {
	private static Logger logger = Logger.getLogger(FastQwrite.class);
	
	TxtReadandWrite txtSeqFile;
	
	FastQwrite fastQwriteMate;
	
	public FastQwrite() {}
	/**
	 * 自动判断 FastQ的格式
	 * @param seqFile
	 */
	public FastQwrite(String seqFile) {
		txtSeqFile = new TxtReadandWrite(seqFile, true);
	}
	
	protected String getFileName() {
		return txtSeqFile.getFileName();
	}
	
	public void setFastQwriteMate(FastQwrite fastQwriteMate) {
		this.fastQwriteMate = fastQwriteMate;
	}
	
	/**
	 * 写入文本
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	protected void writeFastQRecordString(String fastQRecord1, String fastQRecord2) {
		txtSeqFile.writefileln(fastQRecord1);
		if (fastQwriteMate != null) {
			fastQwriteMate.writeFastQRecordString(fastQRecord2);
		}
	}
	/**
	 * 写入文本
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecordString(String fastQRecord) {
		txtSeqFile.writefileln(fastQRecord);
	}
	
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * 不关闭
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		for (FastQRecord fastQRecord : lsFastQRecords) {
			txtSeqFile.writefileln(fastQRecord.toString());
		}
	}
	
	/**
	 * 写完后务必用此方法关闭
	 * 关闭输入流，并将fastQ写入转化为fastQ读取
	 */
	public void close() {
		try { txtSeqFile.close(); } catch (Exception e) { }
	
		if (fastQwriteMate != null) {
			fastQwriteMate.close();
		}
	}
}
