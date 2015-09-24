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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 用于hadoop streaming跑mapping时的reducer模块
 * @author novelbio
 *
 */
public class SamReducer {
	private static final Logger logger = Logger.getLogger(SamReducer.class);
	
	SAMFileHeader header;
	SamFile samFile;
	
	String outFileName;
	
	InputStream is;
	OutputStream os;
	boolean start = false;
	
	int i = 0;
	
	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption("refseq", true, "refseq, used to create sam head");
		opts.addOption("outFileName", true, "saveToFileName: /hdfs:/nbCloud/public/AllProject/project_123434/task_t23ef/save_sorted.bam");
				
		logger.info(ArrayOperate.cmbString(args, "\t"));
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			System.exit(1);
		}

		String refseq = cliParser.getOptionValue("refseq");
		String outFileName = cliParser.getOptionValue("outFileName");
		final SamReducer samReducer = new SamReducer();
		
		if (StringOperate.isRealNull(outFileName)) {
			samReducer.setOutStream(System.out);
		} else {
			samReducer.setOutFileName(outFileName);
		}
		samReducer.readInStream(refseq, System.in);
		samReducer.finish();
	}
	
	/** 输出流 */
	public void setOutStream(OutputStream os) {
		this.os = os;
	}
	/** 输出文件 */
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	protected void readInStream(String refseq, InputStream is) {
		this.is = is;
		SamHeadCreater samHeadCreater = new SamHeadCreater();
		samHeadCreater.setRefSeq(refseq);
		//这里不需要关闭，流会在外部被关闭
		TxtReadandWrite txtRead = new TxtReadandWrite(is);
		
		for (String content : txtRead.readlines()) {	
			if (!start) {
				if (content.startsWith("@HD")) {
					samHeadCreater.setAttr(content);
				} else if (content.startsWith("@PG")) {
					samHeadCreater.addProgram(content);
				} else if (content.startsWith("@RG")) {
					samHeadCreater.addReadGroup(content);
				} else if (!content.startsWith("@")) {
					SAMFileHeader header = samHeadCreater.generateHeader();
					header.setSortOrder(SortOrder.coordinate);
					start = true;
					setHeader(header);
					initial();
					addSamRecordTxt(content);
				}
				continue;
			}
			addSamRecordTxt(content);
			
			if (++i % 5000000 == 0) {
				logger.info("write record num " + i);
			}
		}
		
	}
	
	protected void setHeader(SAMFileHeader header) {
		this.header = header;
	}
	
	protected void initial() {
		if (os != null) {
			samFile = new SamFile(os, header, true, true);
		} else {
			samFile = new SamFile(getTmpFileName(), header, true);
		}
	
	}
	
	private String getTmpFileName() {
		return FileOperate.changeFileSuffix(outFileName, "_tmp", null);
	}
	
	protected void finish() {
		try {
			is.close();
		} catch (Exception e) {
		}
		samFile.close();
		if (!StringOperate.isRealNull(outFileName)) {
			FileOperate.moveFile(true, getTmpFileName(), outFileName);
		}
	}
	
	/** 输入项目为 m00001chr1_@_10019193_@_HWI-D00175:261:C6L59ANXX:7:1101:4774:82368	HWI-D00175:261:C6L59ANXX:7:1101:4774:82368	185	chr1	10019193	0	18S36M71S	=	10019193	0	TTATATTCTATGTATATATCTCCTCTCTCTCTCTCTCTCTCTCTCTCTCTCTCTGTGTCCCCTTCTGTAGATGAATATATATGTTCAAGTCTGAAAGATCTTATTTACTGCTAATTAAGTATAAG	AAAAAAAADAAAABFADAAFCAFADGGGFABGDDEADFAAEABDDGAAGGGBFAEFAADBBFCAGGGGGGFCAAAFACFAAACAGGFDGDGEGEECFFGAGGEDEFFAFGGGGGEGGGEGABBBB	MD:Z:36	NH:i:1	HI:i:1	NM:i:0	AS:i:36	XS:i:35
		* 这种类型，最前面是用来排序的，删掉就好
	*/
	protected void addSamRecordTxt(String samRecordTxt) {
		String[] ss = samRecordTxt.split("\t");
		String[] samRecordNewArray = new String[ss.length - 1];
		for (int i = 0; i < samRecordNewArray.length; i++) {
			samRecordNewArray[i] = ss[i+1];
		}
		String samRecordNew = ArrayOperate.cmbString(samRecordNewArray, "\t");
		SAMRecordFactory samRecordFactory = new DefaultSAMRecordFactory();
		SAMLineParser parser = new SAMLineParser(samRecordFactory, ValidationStringency.STRICT, header, null, null);
		SAMRecord samRecord = null;
		try {
			samRecord = parser.parseLine(samRecordNew);
		} catch (Exception e) {
		}
		if (samRecord != null) {
			samFile.writeSamRecord(samRecord);
		}
	}
}
