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
		txtSeqFile = new TxtReadandWrite(cmpOutType, seqFile, true);
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
	/**
	 * <b>д�������� {@link #closeWrite} �����ر�</b>
	 * ������ʱ��Ҫ�趨Ϊcreatģʽ
	 * @param bedRecord
	 */
	public void writeFastQRecord(FastQRecord fastQRecord) {
		txtSeqFile.writefileln(fastQRecord.toString());
	}
	/**
	 * ���ر�
	 * @param lsBedRecord
	 */
	public void wirteFastqRecord(List<FastQRecord> lsFastQRecords) {
		for (FastQRecord fastQRecord : lsFastQRecords) {
			txtSeqFile.writefileln(fastQRecord.toString());
		}
	}
	/**
	 * д�������ô˷����ر�
	 * �ر�������������fastQд��ת��ΪfastQ��ȡ
	 */
	public void closeWrite() {
		txtSeqFile.close();
	}
}
