package com.novelbio.analysis.seq.fasta;

import java.io.File;

import com.novelbio.base.fileOperate.FileOperate;

import net.sf.picard.sam.CreateSequenceDictionary;

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
        if (FileOperate.isFileExistAndBigThanSize(outDict, 0)) {
        	return true;
        }
        if (FileOperate.isFileExist(outDict)) {
			FileOperate.delFile(outDict);
		}
        if (!FileOperate.isFileExistAndBigThanSize(reference, 0)) {
			return false;
		}
       	REFERENCE = new File(reference);
       	OUTPUT = new File(outDict);
        try {
			doWork();
			return true;
		} catch (Exception e) {
			return false;
		}
    }
}
