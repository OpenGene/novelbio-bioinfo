package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.BAMIndexer;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMFileHeader;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;

public class BamIndex {
	private static Logger logger = Logger.getLogger(BamIndex.class);
	SamFile samFile;
//	String bamFile;
	
	String ExePath = "";
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	}
	
	public BamIndex(SamFile samFile) {
		this.samFile = samFile;
	}
	
	/**
	 * 返回建好的索引名字
	 * @return
	 */
	public String indexC() {
		if (FileOperate.isFileExistAndBigThanSize(samFile.getFileName() + ".bai", 1000)) {
			return samFile.getFileName() + ".bai";
		}
		String tmpIndex = PathDetail.getTmpPathWithSep() + 
				DateUtil.getDateAndRandom() + FileOperate.getFileName(samFile.getFileName()) + ".bai";
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("index");
		lsCmd.add(samFile.getFileName());
		lsCmd.add(tmpIndex);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
		
		FileOperate.moveFile(true, tmpIndex, samFile.getFileName() + ".bai");
		return tmpIndex;
	}
	
	/**
	 * 返回建好的索引名字
	 * @return
	 */
	public String index() {
		if (!FileOperate.isFileExistAndBigThanSize(getIndexFileName(), 1000)) {
			makeIndex();
		}
		return getIndexFileName();
	}
	
	private String getIndexFileName() {
		return samFile.getFileName() + ".bai";
	}
	
    private void makeIndex() {
    	String outFile = getIndexFileName();
        if (!samFile.getSamReader().isBinary()) {
            throw new SAMException("Input file must be bam file, not sam file.");
        }

//        if (!samFile.getHeader().getSortOrder().equals(SAMFileHeader.SortOrder.coordinate)) {
//        	throw new SAMException("Input bam file must be sorted by coordinates");
//        }
        try {
        	makeIndex(samFile.samReader, outFile);
        } catch (Exception e) {
        	FileOperate.DeleteFileFolder(FileOperate.changeFileSuffix(outFile, "_tmp", null));
        	logger.error(e);
        	indexC();
        }
    }
    
    private static void makeIndex(SamReader reader, String output) {
    	String outTmp = FileOperate.changeFileSuffix(output, "_tmp", null);
    	OutputStream outStream = FileOperate.getOutputStream(outTmp, true);
    	
    	SAMFileHeader samFileHeader = reader.getSamFileHead();
        BAMIndexer indexer = new BAMIndexer(outStream, samFileHeader);
        reader.samFileReader.enableFileSource(true);
        int allRecordsNum = 0;

        for (SamRecord rec : reader.readLines()) {
        	if (allRecordsNum % 5000000 == 0) {
        		logger.info(allRecordsNum + " reads processed ...");
        	}
//            try {
//            	
//			} catch (Exception e) {
//				logger.error(rec.toString(), e);
//			}
            indexer.processAlignment(rec.samRecord);
            allRecordsNum ++;
        }

        indexer.finish();
        FileOperate.moveFile(true, outTmp, output);
    }

}
