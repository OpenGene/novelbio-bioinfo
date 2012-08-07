package com.novelbio.analysis.seq.fastq;

import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQwrite {
	private static Logger logger = Logger.getLogger(FastQ.class);
	
	TxtReadandWrite txtSeqFile;
	protected String cmpOutType = TxtReadandWrite.TXT;
	
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
		txtSeqFile = new TxtReadandWrite(cmpOutType, seqFile, true);
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
	public void closeWrite() {
		txtSeqFile.close();
	}
}
