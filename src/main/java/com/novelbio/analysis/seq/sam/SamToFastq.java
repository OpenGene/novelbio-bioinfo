package com.novelbio.analysis.seq.sam;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 在没有排序的情况下，非unique mapping只会输出一条reads
 * @author zong0jie
 *
 */
public class SamToFastq implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(SamToFastq.class);
	/** 是否仅挑选没有mapping上的reads */
	boolean justUnMapped = false;
	FastQ fastQ;
	FastQRecord lastFastqRecord;
	/**
	 * 是否仅需要unMapped，默认为false
	 * @param justUnMapped
	 */
	public void setJustUnMapped(boolean justUnMapped) {
		this.justUnMapped = justUnMapped;
	}
	/** 根据是否为mapping上的reads，自动设定文件名，并返回设定好的文件名 */
	public String setOutFileInfo(SamFile samFile, boolean justUnMapped) {
		String fileName = samFile.getFileName();
		if (justUnMapped) {
			fileName = FileOperate.changeFileSuffix(fileName, "_unMapped", "fastq.gz");
		} else {
			fileName = FileOperate.changeFileSuffix(fileName, "", "fastq.gz");
		}
		this.justUnMapped = justUnMapped;
		setFastqFile(fileName);
		return fileName;
	}
	/**
	 * @param outFileName
	 */
	public void setFastqFile(String outFileName) {
		fastQ = new FastQ(outFileName, true);
	}
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		FastQRecord fastQRecord = null;
		if (!justUnMapped || (justUnMapped && !alignRecord.isMapped())) {
			if (!alignRecord.isUniqueMapping() && lastFastqRecord != null && 
					alignRecord.getSeqFasta().getSeqName().equals(lastFastqRecord.getName())
					&& alignRecord.getSeqFasta().toString().equalsIgnoreCase(lastFastqRecord.getSeqFasta().toString())
					) {
				//不写入文本
				fastQRecord = alignRecord.toFastQRecord();
			} else {
				fastQRecord = alignRecord.toFastQRecord();
				if (!fastQRecord.isValidate()) {
					logger.error("出错" + alignRecord.toString());
				}
				fastQ.writeFastQRecord(fastQRecord);
				fastQRecord = null;
			}
			lastFastqRecord = fastQRecord;
		}
	}
	
	public FastQ getResultFastQ() {
		return fastQ;
	}
	
	@Override
	public void summary() {
		fastQ.close();
	}
	@Override
	public Align getReadingRegion() {
		return null;
	}

}
