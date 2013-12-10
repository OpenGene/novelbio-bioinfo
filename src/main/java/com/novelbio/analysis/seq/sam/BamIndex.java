package com.novelbio.analysis.seq.sam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.HdfsBase;
import com.novelbio.base.fileOperate.FileHadoop;
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
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools");
		lsCmd.add("index");
		lsCmd.add(samFile.getFileName());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("samtools index error:\n" + cmdOperate.getCmdExeStr());
		}
		return samFile.getFileName() + ".bai";
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

        if (!samFile.getHeader().getSortOrder().equals(SAMFileHeader.SortOrder.coordinate)) {
            throw new SAMException("Input bam file must be sorted by coordinates");
        }
        try {
        	  makeIndex(samFile.samReader, outFile);
		} catch (Exception e) {
			logger.error("make index error:" + outFile);
		}
        logger.info("Finished Make Index " + outFile);
    }
    
    private static void makeIndex(SamReader reader, String output) throws IOException {
    	SAMFileReader samFileReader = reader.samFileReader;
    	OutputStream outStream = null;
    	if (HdfsBase.isHdfs(output)) {
			FileHadoop fileHadoop = new FileHadoop(output);
			outStream = fileHadoop.getOutputStreamNew(true);
		} else {
			outStream = new FileOutputStream(new File(output));
		}
        BAMIndexer indexer = new BAMIndexer(outStream, samFileReader.getFileHeader());

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
