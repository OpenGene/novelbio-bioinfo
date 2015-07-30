package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordFactory;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.ValidationStringency;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * mapreduce中使用，将bwa等输出的流加上unique mapping标签，并写成Mapreduce识别的格式
 * @author novelbio
 *
 */
public class Sam2SysOutMR {
	Iterator<String> itSam;
	SAMFileHeader samFileHeader;
	LinkedList<String> lsHeader = new LinkedList<>();
	SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
	
	boolean isError = false;
	Exception error;
	
	OutputStream outputStream;
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void setIsPairend(boolean isPairend) {
		samAddMultiFlag.setPairend(isPairend);
	}
	
	/** 内部不关闭流 */
	public void setInStream(InputStream inStream) {
		TxtReadandWrite txtRead = new TxtReadandWrite(inStream);
		itSam = txtRead.readlines().iterator();
		String firstRecord = fillLsHeadAndGetFirstRecord(itSam);
		SAMTextHeaderCodec samTextHeaderCodec = new SAMTextHeaderCodec();
		samFileHeader = samTextHeaderCodec.decode(lsHeader, null);
		addToMultiFlag(firstRecord);
	}
	
	private String fillLsHeadAndGetFirstRecord(Iterator<String> it) {
		String content = "";
		while (it.hasNext()) {
			content = it.next();
			if (!content.startsWith("@")) {
				break;
			}
			lsHeader.add(content);
		}
		String firstRecord = null;
		if (!content.startsWith("@")) {
			firstRecord = content;
		}
		return firstRecord;
	}

	public void readInputStream() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (itSam.hasNext()) {
						addToMultiFlag(itSam.next());
					}
				} catch (Exception e) {
					isError = true;
					error = e;
					throw new ExceptionSamError(e);
				} finally {
					samAddMultiFlag.finish();
				}				
			}
		});
		thread.start();
	}
	
	private void addToMultiFlag(String samRecordTxt) {
		SAMRecordFactory samRecordFactory = new DefaultSAMRecordFactory();
		SAMLineParser parser = new SAMLineParser(samRecordFactory, ValidationStringency.STRICT, samFileHeader, null, null);
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
	
	public void writeToOs() {
		try {
			writeToOsExp();
		} catch (ExceptionSamError e) {
			throw e;
		} catch (Exception e) {
			 throw new ExceptionSamError(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}
	
	private void writeToOsExp() throws UnsupportedEncodingException, IOException {
		for (String string : lsHeader) {
			outputStream.write((string + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
		}
		for (SamRecord samRecord : samAddMultiFlag.readlines()) {
			if (isError) throw new ExceptionSamError(error);
			
			String record = samRecord.toString();
			String[] ss = record.split("\t");
			String key = ss[2] + "_@_" + ss[3] + "_@_" + ss[0];
			record = key + "\t" + record;
			record = samRecord.isMapped()? "m" + record : "u" + record;
			outputStream.write((record + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
		}
	}
	
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public Iterable<String> readLines() throws Exception {
		final Iterator<SamRecord> itSamRecord = samAddMultiFlag.readlines().iterator();
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}
					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					String getLine() {
						if (isError) throw new ExceptionSamError(error);
						
						if (!lsHeader.isEmpty()) {
							return lsHeader.poll();
						}
						if (!itSamRecord.hasNext()) {
							return null;
						}
						SamRecord samRecord = itSamRecord.next();
						String record = samRecord.toString();
						String[] ss = record.split("\t");
						String key = ss[2] + "_@_" + ss[3] + "_@_" + ss[0];
						record = key + "\t" + record;
						record = samRecord.isMapped()? "m" + record : "u" + record;
						return record;
					}
					String line = getLine();
				};
			}
		};
	}
	
}
