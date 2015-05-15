package com.novelbio.analysis.seq.fasta;

import java.io.File;

import picard.sam.CreateSequenceDictionary;

import com.novelbio.base.fileOperate.FileOperate;

public class FastaDictMake extends CreateSequenceDictionary {
	String reference;
	String outDict;

	public FastaDictMake(String reference, String outDict) {
		this.reference = reference;
		this.outDict = outDict;
	}
	
    /**
     * Do the work after command line has been parsed.
     * RuntimeException may be thrown by this method, and are reported appropriately.
     *
     * @return program exit status.
     */
	public boolean makeDict() {
		if (!FileOperate.isFileExistAndBigThanSize(reference, 0)) {
			return false;
		}
		if (FileOperate.isFileExistAndBigThanSize(outDict, 0)) {
			return true;
		}
		FileOperate.delFile(outDict);
		REFERENCE = FileOperate.getFile(reference);
		OUTPUT = FileOperate.getFile(outDict);
		try {
			doWork();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
