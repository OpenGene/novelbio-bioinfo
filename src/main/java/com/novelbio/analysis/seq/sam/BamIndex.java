package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import net.sf.picard.io.IoUtil;
import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloserUtil;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamIndex {
	private static Logger logger = Logger.getLogger(BamIndex.class);
	SamFile samFile;
	String bamFile;

	public BamIndex(SamFile samFile) {
		this.samFile = samFile;
	}
	public void setBamFile(String bamFile) {
		this.bamFile = bamFile;
	}
	public String index() {
		if (!FileOperate.isFileExistAndBigThanSize(bamFile + ".bai", 1000)) {
			makeIndex();
		}
		samFile.close();
		return bamFile;
	}
	
	private String getIndexFileName() {
		return bamFile + ".bai";
	}
	
    private void makeIndex() {
    	String outFile = getIndexFileName();
        if (!samFile.samReader.isBinary()) {
            throw new SAMException("Input file must be bam file, not sam file.");
        }

        if (!samFile.getHeader().getSortOrder().equals(SAMFileHeader.SortOrder.coordinate)) {
            throw new SAMException("Input bam file must be sorted by coordinates");
        }

        makeIndex(samFile.samReader, outFile);
        logger.info("Finished Make Index " + outFile);
    }
    
    private static void makeIndex(SamReader reader, String output) {
    	SAMFileReader samFileReader = reader.samFileReader;
    	File fileOut = new File(output);
        BAMIndexer indexer = new BAMIndexer(fileOut, samFileReader.getFileHeader());

        samFileReader.enableFileSource(true);
        int allRecordsNum = 0;

        for (SAMRecord rec : samFileReader) {
            if (allRecordsNum % 1000000 == 0) {
            	logger.info(allRecordsNum + " reads processed ...");
            }
            indexer.processAlignment(rec);
            allRecordsNum ++;
        }
        indexer.finish();
    }

}
