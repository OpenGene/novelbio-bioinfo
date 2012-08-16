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
	 * �Զ��ж� FastQ�ĸ�ʽ
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
	 * �趨�ļ�ѹ����ʽ
	 * ��TxtReadandWrite.TXT��
	 * @param cmpInType д���ѹ����ʽ null��""��ʾ����
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
	 * <b>д�������� {@link #closeWrite} �����ر�</b>
	 * ������ʱ��Ҫ�趨Ϊcreatģʽ
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
	 * <b>д�������� {@link #closeWrite} �����ر�</b>
	 * ������ʱ��Ҫ�趨Ϊcreatģʽ
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		creatTxt();
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * ���ر�
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
	 * д�������ô˷����ر�
	 * �ر�������������fastQд��ת��ΪfastQ��ȡ
	 */
	public void close() {
		txtSeqFile.close();
		if (fastQwriteMate != null) {
			fastQwriteMate.close();
		}
	}
}
