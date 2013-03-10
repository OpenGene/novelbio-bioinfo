package com.novelbio.analysis.seq.sam;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SamToFastq implements AlignmentRecorder {
	/** 是否仅挑选没有mapping上的reads */
	boolean justUnMapped = false;
	FastQ fastQ;
	
	/**
	 * 是否仅需要unMapped，默认为false
	 * @param justUnMapped
	 */
	public void setJustUnMapped(boolean justUnMapped) {
		this.justUnMapped = justUnMapped;
	}
	/** 根据是否为mapping上的reads，自动设定文件名 */
	public void setOutFileInfo(SamFile samFile, boolean justUnMapped) {
		String fileName = samFile.getFileName();
		if (justUnMapped) {
			fileName = FileOperate.changeFileSuffix(fileName, "_unMapped", "fastq");
		} else {
			fileName = FileOperate.changeFileSuffix(fileName, "", "fastq");
		}
		this.justUnMapped = justUnMapped;
		setFastqFile(fileName);
	}
	/**
	 * @param outFileName
	 */
	public void setFastqFile(String outFileName) {
		fastQ = new FastQ(outFileName, true);
		String compressOutType = TxtReadandWrite.TXT;
		if (outFileName.endsWith("gz")) {
			compressOutType = TxtReadandWrite.GZIP;
		}
		fastQ.setCompressType(compressOutType, compressOutType);
	}
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!justUnMapped || (justUnMapped && !alignRecord.isMapped())) {
			FastQRecord fastQRecord = alignRecord.getFastQRecord();
			fastQ.writeFastQRecord(fastQRecord);
			fastQRecord = null;
		}
	}
	
	public FastQ getResultFastQ() {
		return fastQ;
	}
	
	@Override
	public void summary() {
		fastQ.close();
	}

}
