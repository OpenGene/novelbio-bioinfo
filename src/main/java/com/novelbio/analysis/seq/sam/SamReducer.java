package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SAMFileHeader.SortOrder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 用于hadoop streaming跑mapping时的reducer模块
 * @author novelbio
 *
 */
public class SamReducer {
	private static final Logger logger = Logger.getLogger(SamReducer.class);
	
	SAMFileHeader header;
	boolean isWriteOs = true;
	SamFile samFile;
	
	String outFile;
	
	OutputStream os;
	boolean isBam;
	SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
	boolean start = false;
	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption("refseq", true, "refseq, used to create sam head");
		opts.addOption("pgLine", true, "program cmd line, example: @PG\\tID:bwa\tPN:bwa\tVN:0.7.8-r455"
				+ "\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz "
				+ "Shill64_filtered_2.fq.gz");
		opts.addOption("rgLine", true, "readgroup info, example: @RG ID:Shill64 PL:Illumina LB:Shill64 SM:Shill64");
		
		opts.addOption("isPairend", true, "is pairend");
		
		logger.info(ArrayOperate.cmbString(args, "\t"));
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			System.exit(1);
		}

		String refseq = cliParser.getOptionValue("refseq");
		String pgLine = cliParser.getOptionValue("pgLine");
		String isPairend = cliParser.getOptionValue("isPairend");
		if (pgLine != null) {
			pgLine = pgLine.replace("\\t", "\t");	
		}
		String rgLine = cliParser.getOptionValue("rgLine");
		if (rgLine != null) {
			rgLine = rgLine.replace("\\t", "\t");
		}
	
		final SamReducer samReducer = new SamReducer();
		samReducer.setIsWriteOs(true);
		samReducer.setOutStream(System.out, true);
		if (!StringOperate.isRealNull(isPairend) && (isPairend.toLowerCase().equals("true") || isPairend.toLowerCase().equals("t"))) {
			samReducer.setIsPairend(true);
		}
		
		samReducer.readInStream(refseq, pgLine, rgLine, System.in);
		samReducer.runWriteToSam();
	}
	
	public void setIsPairend(boolean isPairend) {
		samAddMultiFlag.setPairend(isPairend);
	}
	
	public void readInStream(final String refseq, final String pgLine, final String rgLine, final InputStream is) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					readInStreamExp(refseq, pgLine, rgLine, is);
				} catch (Exception e) {
					samAddMultiFlag.finish();
					throw new SamErrorException(e);
				}
			}
		});
		thread.start();
	}
	
	private void readInStreamExp(String refseq, String pgLine, String rgLine, InputStream is) {		
		int maxLine = 100;
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refseq);
		samHeadCreater.addProgram(pgLine);
		samHeadCreater.addReadGroup(rgLine);
		//这里不需要关闭，流会在外部被关闭
		TxtReadandWrite txtRead = new TxtReadandWrite(is);
	
		List<String> lsTmp = new ArrayList<String>();
		for (String content : txtRead.readlines()) {			
			if (!start) {
				if (content.startsWith("@HD")) {
					start(lsTmp, samHeadCreater, content);
				} else if (!content.startsWith("@")) {
					//我看到有个文件的结果中没有 @HD\tVN:1.4\tSO:unsorted
					if (lsTmp.size() > maxLine) {
						start(lsTmp, samHeadCreater, "@HD\tVN:1.4\tSO:unsorted");
					} else {
						lsTmp.add(content);
						continue;
					}
				} else {
					continue;
				}
			}
			addSamRecordTxt(content);
		}
		samAddMultiFlag.finish();
	}
	
	private void start(List<String> lsTmp, SamHeadCreater samHeadCreater, String hdLine) {
		samHeadCreater.setAttr(hdLine);
		SAMFileHeader header = samHeadCreater.generateHeader();
		setSamHeader(header);
		initial();
		for (String content : lsTmp) {
			addSamRecordTxt(content);
		}
		lsTmp.clear();
	}
	
	public void setSamHeader(SAMFileHeader header) {
		this.header = header;
	}
	
	/** 是否写入输出流中 */
	public void setIsWriteOs(boolean isWriteToOs) {
		this.isWriteOs = isWriteToOs;
	}
	
	/** 输出文件路径 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/** 输出流 */
	public void setOutStream(OutputStream os, boolean isBam) {
		this.os = os;
	}
	
	public void initial() {
		if (isWriteOs) {
			samFile = new SamFile(os, header, isBam);
		} else {
			samFile = new SamFile(outFile, header);
		}
		start = true;
	}
	
	protected void addSamRecordTxt(String samRecordTxt) {
		SAMRecordFactory samRecordFactory = new DefaultSAMRecordFactory();
		SAMLineParser parser = new SAMLineParser(samRecordFactory, ValidationStringency.STRICT, header, null, null);
		SAMRecord samRecord = null;
		try {
			samRecord = parser.parseLine(samRecordTxt);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (samRecord != null) {
			samAddMultiFlag.addSamRecord(new SamRecord(samRecord));
		}
	}
	
	//另一个线程专门往sam文件里写结果
	public void runWriteToSam() {
		while (!start) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (SamRecord samRecord : samAddMultiFlag.readlines()) {
			samFile.writeSamRecord(samRecord);
		}
		samFile.close();
	}
}
