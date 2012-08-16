package com.novelbio.analysis.seq.fastq;

import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

class FastQwrite {
	private static Logger logger = Logger.getLogger(FastQ.class);
	
	boolean isTxtExist = false;
	String txtFileName = "";
	TxtReadandWrite txtSeqFile;
	protected String cmpOutType = TxtReadandWrite.TXT;
	
	FastQwrite fastQwriteMate;
	
	public FastQwrite() {}
	/**
	 * 自动判断 FastQ的格式
	 * @param seqFile
	 * @param QUALITY
	 */
	public FastQwrite(String seqFile) {
		setFastqFile(seqFile);
	}
	public void setFastqFile(String seqFile) {
		String houzhui = FileOperate.getFileNameSep(seqFile)[1];
		if (houzhui.equals("gz")) {
			setCompressType(TxtReadandWrite.GZIP);
		}
		else {
			setCompressType(TxtReadandWrite.TXT);
		}
		txtFileName = seqFile;
	}
	protected String getFileName() {
		return txtFileName;
	}
	/**
	 * 设定文件压缩格式
	 * 从TxtReadandWrite.TXT来
	 * @param cmpInType 写入的压缩格式 null或""表示不变
	 */
	public void setCompressType(String cmpOutType) {
		if (cmpOutType != null && !cmpOutType.equals("")) {
			this.cmpOutType = cmpOutType;
		}
	}
	public void setFastQwriteMate(FastQwrite fastQwriteMate) {
		this.fastQwriteMate = fastQwriteMate;
	}
	
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	protected void writeFastQRecord(FastQRecord fastQRecord1, FastQRecord fastQRecord2) {
		creatTxt();
		txtSeqFile.writefileln(fastQRecord1.toString());
		if (fastQwriteMate != null) {
			fastQwriteMate.writeFastQRecord(fastQRecord2);
		}
	}
	
	/**
	 * <b>写完后务必用 {@link #closeWrite} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		creatTxt();
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * 不关闭
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		creatTxt();
		for (FastQRecord fastQRecord : lsFastQRecords) {
			txtSeqFile.writefileln(fastQRecord.toString());
		}
	}
	private void creatTxt() {
		if (isTxtExist) {
			return;
		}
		isTxtExist = true;
		txtSeqFile = new TxtReadandWrite(cmpOutType, txtFileName, true);
	}
	/**
	 * 写完后务必用此方法关闭
	 * 关闭输入流，并将fastQ写入转化为fastQ读取
	 */
	public void close() {
		txtSeqFile.close();
		if (fastQwriteMate != null) {
			fastQwriteMate.close();
		}
	}
}
