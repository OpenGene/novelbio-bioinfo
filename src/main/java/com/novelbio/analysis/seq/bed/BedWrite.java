package com.novelbio.analysis.seq.bed;

import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class BedWrite {
	TxtReadandWrite txtWrite;
	
	public BedWrite(String fileName) {
		txtWrite = new TxtReadandWrite(fileName, true);
	}
	
	public String getFileName() {
		return txtWrite.getFileName();
	}
	
	/**
	 * <b>写完后务必用 {@link #close} 方法关闭</b>
	 * 创建的时候要设定为creat模式
	 * @param bedRecord
	 */
	public void writeBedRecord(BedRecord bedRecord) {
		if (bedRecord == null) {
			return;
		}
		txtWrite.writefileln(bedRecord.toString());
	}
	
	/**
	 * 不关闭
	 * @param lsBedRecord
	 */
	public void writeBedRecord(List<BedRecord> lsBedRecord) {
		for (BedRecord bedRecord : lsBedRecord) {
			txtWrite.writefileln(bedRecord.toString());
		}
	}

}
