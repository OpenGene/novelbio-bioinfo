package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.SAMFileHeader.SortOrder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 将sam文件转化为bam文件，<b>仅用于没有排序过的sam文件</b><br>
 * 其中添加multiHit的功能仅适用于bowtie和bwa 的mem
 *  */
public class SamToBam {
	private static final Logger logger = Logger.getLogger(SamToBamSort.class);
	//TODO 是否添加flag还没测试
	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<>();
	
	boolean isAddMultiFlag = true;
	SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
	
	SamFile samFileIn;
	SAMFileHeader samFileHeader;
	
	SamReorder samReorder;
	
	boolean isError = false;
	Throwable error;
	
	/** 将Sam写入此处 */
	SamToBamOut samWriteTo;
	
	public void setSamWriteTo(SamToBamOut samWriteTo) {
		this.samWriteTo = samWriteTo;
	}
	
	/** 内部关闭流 */
	public void setInStream(InputStream inStream) {
		samFileIn = new SamFile(inStream);
	}
	/** 内部关闭流 */
	public void setInFile(String inSam) {
		samFileIn = new SamFile(inSam);
	}
	/** 内部关闭流 */
	public void setInFile(SamFile samFile) {
		samFileIn = samFile;
	}
	public void setIsPairend(boolean isPairend) {
		samAddMultiFlag.setPairend(isPairend);
	}
	/** 是否添加比较到多个位置的标签 */
	public void setIsAddMultiFlag(boolean isAddMultiFlag) {
		this.isAddMultiFlag = isAddMultiFlag;
	}
	
	/** 直接修改samReorder，与 {@link #setSamSequenceDictionary} 冲突*/
	public void setSamReorder(SamReorder samReorder) {
		this.samReorder = samReorder;
	}
	
	/** <b>首先设定 {@link #setInStream(InputStream)}</b><br>
	 * 是否根据samSequenceDictionary重新排列samHeader中的顺序，目前只有mapsplice才遇到 */
	public void setSamSequenceDictionary(SAMSequenceDictionary samSequenceDictionary) {
		if (samSequenceDictionary != null) {
			samReorder = new SamReorder();
			samReorder.setSamSequenceDictionary(samSequenceDictionary);

		}
	}

	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		if (lsAlignmentRecorders == null) {
			this.lsAlignmentRecorders.clear();
		} else {
			this.lsAlignmentRecorders = lsAlignmentRecorders;
		}
	}
	
	public void readInputStream() {
		samFileHeader = samFileIn.getHeader();

		if (!isAddMultiFlag) return;
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AddMultiFlag();
				} catch (Throwable e) {
					samAddMultiFlag.finish();
					isError = true;
					error = e;
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	private void AddMultiFlag() {
		int i = 0;
		for (SamRecord samRecord : samFileIn.readLines()) {
			if (isError) {
				break;
			}
			if (i++%1000000 == 0) {
				logger.info("read lines: " + i);
				System.gc();
			}
			
			samAddMultiFlag.addSamRecord(samRecord);
		}
		samAddMultiFlag.finish();
	}
	
	public void writeToOs() {
		try {
			setSamHeader();
			setLsRecorders();
		} catch (Exception e) {
			isError = true;
			throw e;
		}

		Iterable<SamRecord> itSamRecord = isAddMultiFlag? samAddMultiFlag.readlines() : samFileIn.readLines();

		try {
			for (SamRecord samRecord : itSamRecord) {
				if (isError) throw new ExceptionSamError(error);
				
				if (samReorder != null) samReorder.copeReads(samRecord);

				addRecordToLsRecorders(samRecord);
				samWriteTo.write(samRecord);
			}
		} catch (ExceptionSamError e) {
			isError = true;
			throw e;
		} catch (Exception e) {
			isError = true;
			 throw new ExceptionSamError(e);
		} finally {
			samFileIn.close();
			samWriteTo.close();
		}
		
		if (!isError) {
			samWriteTo.finish();
			summaryLsRecorder();
		}

	}
	
	protected void setSamHeader() {
		if (samReorder != null) {
			samReorder.setSamFileHeader(samFileHeader);
			samReorder.reorder();
			samFileHeader = samReorder.getSamFileHeaderNew();
		}
		samWriteTo.setSamHeader(samFileHeader);
	}
	
	private void setLsRecorders() {		
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder instanceof SamFileStatistics) {
				((SamFileStatistics)alignmentRecorder).setStandardData(SamReader.getMapChrId2Len(samFileHeader));
			}
		}
	}
	
	private void addRecordToLsRecorders(SamRecord samRecord) {		
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			try {
				alignmentRecorder.addAlignRecord(samRecord);
			} catch (Exception e) { }
		}
	}
	
	private void summaryLsRecorder() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
	}
	
	//==================================================================================
	public static interface SamToBamOut extends Closeable {
		/** 不需要设置 */
		void setSamHeader(SAMFileHeader header);
		/** 不需要设置 */
		void write(SamRecord samRecord)  throws UnsupportedEncodingException, IOException;
		
		/** 不需要设置，结束之后将tmp文件名修改为正常文件名等收尾工作 */
		void finish();
		
		void close();
	}
	
	//==================================================================================
	/** 将sam文件转化为bam文件，<b>仅用于没有排序过的sam文件</b><br>
	 * 其中添加multiHit的功能仅适用于bowtie和bwa 的mem
	 *  */
	public static class SamToBamOutFile implements SamToBamOut {
		/** 需要转化成的bam文件 */
		SamFile samFileBam;
		boolean isNeedSort = false;
		String outFileName;
		boolean isWriteOut = true;
		
		public void setOutFileName(String outFileName) {
			this.outFileName = outFileName;
		}
		/**
		 * <b>需要在最开始就设定好</b><br><br>
		 * 
		 * 是否将结果输出，默认输出
		 * 有时候如过滤rrna时也会不输出bam文件
		 * @param isWriteOut
		 */
		public void setWriteOut(boolean isWriteOut) {
			this.isWriteOut = isWriteOut;
		}
		private String getTmpFileName() {
			return  FileOperate.changeFileSuffix(outFileName, "_tmp", null);
		}
		
		public void setNeedSort(boolean isNeedSort) {
			this.isNeedSort = isNeedSort;
		}
		
		@Override
		public void setSamHeader(SAMFileHeader header) {
			if (!isWriteOut) return;
			
			if (isNeedSort && header.getSortOrder()== SortOrder.unsorted) {
				header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
				samFileBam = new SamFile(getTmpFileName(), header, false);
			} else {
				samFileBam = new SamFile(getTmpFileName(), header);
			}
			
		}

		@Override
		public void write(SamRecord samRecord) throws UnsupportedEncodingException, IOException {
			if (!isWriteOut) return;
			
			samFileBam.writeSamRecord(samRecord);
		}
		
		@Override
		public void close() {
			if (!isWriteOut) return;
			
			samFileBam.close();
		}
		
		@Override
		public void finish() {
			if (!isWriteOut) return;
			
			FileOperate.moveFile(true, getTmpFileName(), outFileName);
		}
		
		/** 返回转换好的bam文件 */
		public SamFile getSamFileBam() {
			return new SamFile(outFileName);
		}
	}
	
	
	//==================================================================================
	/**
	 * mapreduce中使用，将bwa等输出的流加上unique mapping标签，并写成Mapreduce识别的格式<br>
	 * 仅需要设置 {@link #setOutputStream(OutputStream)} 方法
	 * @author novelbio
	 *
	 */
	public static class SamToBamOutMR implements SamToBamOut {
		OutputStream outputStream;
		/** chrId和具体的顺序，这是方便hadoop后面排序的，因为chr2可能会排在chr11之后，所以我们用这个map来重新定义排序 */
		Map<String, String> mapChrId2Index = new HashMap<>();
		
		public void setOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}
		
		@Override
		public void close() {
			try {
				outputStream.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		
		/** 不需要要设置 */
		@Override
		public void setSamHeader(SAMFileHeader header) {
			int i = 0;
			for (SAMSequenceRecord samSequenceRecord : header.getSequenceDictionary().getSequences()) {
				i++;
				mapChrId2Index.put(samSequenceRecord.getSequenceName(), fillBy0(i+"", 7));
            }
			List<String> lsHeader = getLsHeader(header);
			for (String string : lsHeader) {
				try {
					outputStream.write((string + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
				} catch (Exception e) {
					throw new ExceptionSamError("set samFile header error", e);
				}
			}
		}
		
		private static List<String> getLsHeader(SAMFileHeader header) {
			final StringWriter headerTextBuffer = new StringWriter();
	        new SAMTextHeaderCodec().encode(headerTextBuffer, header);
	        final String headerText = headerTextBuffer.toString();
	        String[] headers = headerText.split(TxtReadandWrite.ENTER_LINUX);
	        List<String> lsHeader = new ArrayList<>();
	        for (String string : headers) {
				lsHeader.add(string.trim());
			}
	        return lsHeader;
		}
		
		/** 不需要要设置 */
		@Override
		public void write(SamRecord samRecord) throws UnsupportedEncodingException, IOException {
			String record = samRecord.toString();
			String[] ss = record.split("\t");
			String index = mapChrId2Index.get(ss[2]);
			String key = index+ "_@_" + ss[2] + "_@_" + fillBy0(ss[3], maxLen) + "_@_" + ss[0];
			record = key + "\t" + record;
			boolean isMapped = samRecord.isMapped();
			if (!isMapped && (!samRecord.getRefID().equals("*") || samRecord.getStartAbs() > 0)) {
				isMapped = true;
			}
			record = isMapped ? "m" + record : "u" + record;
			outputStream.write((record + TxtReadandWrite.ENTER_LINUX).getBytes("UTF-8"));
		}
		
		int maxLen = 15;
		/** 因为hadoop的key是按照字符串排列的，所以会出现 1234 排在 234 的后面
		 * 目前我想到的解决方案是将数字前面用0填充，改称 00001234 00000234 这种
		 * @param location
		 * @param nameLen
		 * @return
		 */
		private String fillBy0(String location, int nameLen) {
			int len = location.length();
			if (nameLen < len) {
				throw new ExceptionSamError("very long chromosome: " + len);
			}
			int num0 = nameLen - len;
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < num0; i++) {
				builder.append("0");
			}
			builder.append(location);
			return builder.toString();
		}
		
		/** 不用收尾 */
		@Override
		public void finish() {
		}
		
	}

}
