package com.novelbio.analysis.seq.sam;

import java.util.HashMap;
import java.util.Map;

import net.sf.picard.PicardException;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.fastq.ExceptionFastq;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 在没有排序的情况下，非unique mapping只会输出一条reads
 * 只能用于单个样本
 * @author zong0jie
 *
 */
public class SamToFastq implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(SamToFastq.class);
	/** 是否仅挑选没有mapping上的reads */
	SamToFastqType  samToFastqType = SamToFastqType.AllReads;
	String outFileName;
	boolean initial = false;
	boolean isPairend = false;
	/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字 */
	boolean isGenerateTmpFile = true;
    final Map<String, SamRecord> firstSeenMates = new HashMap<String, SamRecord>();

	FastQ fastQ1;
	FastQ fastQ2;
	/** 如果比对到多个位置，reads是从几开始计算的 */
	Boolean readsIndexNumFrom1 = null;
	AlignRecord lastRecord;
	/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字，默认是true */
	public void setGenerateTmpFile(boolean isGenerateTmpFile) {
		this.isGenerateTmpFile = isGenerateTmpFile;
	}
	/** 根据是否为mapping上的reads，自动设定文件名，并返回设定好的文件名 */
	public String setOutFileInfo(SamFile samFile, SamToFastqType samToFastqType) {
		clear();
		String fileName = samFile.getFileName();
		
		if (justUnMapped) {
			fileName = FileOperate.changeFileSuffix(fileName, "_unMapped", "fastq.gz");
		} else {
			fileName = FileOperate.changeFileSuffix(fileName, "", "fastq.gz");
		}
		this.justUnMapped = justUnMapped;
		this.outFileName = fileName;
		return fileName;
	}
	/** 根据是否为mapping上的reads，自动设定文件名，并返回设定好的文件名 */
	public void setOutFileInfo(String outFileName, boolean justUnMapped) {
		clear();
		this.justUnMapped = justUnMapped;
		this.outFileName = outFileName;
	}
	/**
	 * @param outFileName
	 */
	@Deprecated
	public void setFastqFile(String outFileName) {
		this.outFileName = outFileName;
	}
	
	//TODO 待测试
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (justUnMapped) {
			if (alignRecord.isMapped()) {
				return;
			} else if (alignRecord instanceof SamRecord) {
				SamRecord samRecord = (SamRecord)alignRecord;
				if (samRecord.isHavePairEnd() && samRecord.isMateMapped()) {
					return;
				}
			}
		}
		if (alignRecord.getMappedReadsWeight() > 1) {
			throw new ExceptionFastq(outFileName + " cannot convert sam file while reads mapping to many locations");
		}
		if (alignRecord.getSeqFasta().getSeqName().equals(lastRecord.getName())
					&& alignRecord.getSeqFasta().toString().equalsIgnoreCase(lastRecord.getSeqFasta().toString())) {
			return;
		}
		lastRecord = alignRecord;
		
		if (alignRecord instanceof SamRecord) {
			SamRecord samRecord = (SamRecord)alignRecord;
			addAlignRecordSam(samRecord);
		} else {
			addAlignRecordNormal(alignRecord);
		}
		 
	}
	
	private void addAlignRecordNormal(AlignRecord alignRecord) {
		fastQ1.writeFastQRecord(alignRecord.toFastQRecord());
	}
	
	private void addAlignRecordSam(SamRecord samRecord) {
		if (readsIndexNumFrom1 == null && samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != null && samRecord.getMapIndexNum() == 0) {
			readsIndexNumFrom1 = false;
		}
		
          if (samRecord.getReadPairedFlag()) {
              final String currentReadName = samRecord.getName();
              final SamRecord firstRecord = firstSeenMates.remove(currentReadName);
              if (firstRecord == null) {
                  firstSeenMates.put(currentReadName, samRecord);
              } else {
                  assertPairedMates(firstRecord, samRecord);

                  final SamRecord read1 =  samRecord.isFirstRead() ? samRecord : firstRecord;
                  final SamRecord read2 = samRecord.isFirstRead() ? firstRecord : samRecord;
                  fastQ1.writeFastQRecord(read1.toFastQRecord());
                  fastQ2.writeFastQRecord(read2.toFastQRecord());
              }
          } else {
              fastQ1.writeFastQRecord(samRecord.toFastQRecord());
          }
	}
	
	private initialReadsIndex(SamRecord samRecord) {
		if (readsIndexNumFrom1 != null) return;
		if (samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != null) {
			//我们假定reads的index是从0开始的
			if (samRecord.getMapIndexNum() > 1) {
				throw new SamErrorException(outFileName + " error: reads map num Index is not from 0 or 1");
			}
		}
	}
	
    private void assertPairedMates(final SamRecord record1, final SamRecord record2) {
        if (! (record1.isFirstRead() && !record2.isFirstRead() ||
               record2.isFirstRead() && !record1.isFirstRead() ) ) {
            throw new PicardException("Illegal mate state: " + record1.getName());
        }
    }
    
	private void initial(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord) {
			isPairend = ((SamRecord)alignRecord).isHavePairEnd();
		}
		String[] finalFileName = getFinalName(isGenerateTmpFile);
		if (isPairend) {
			fastQ1 = new FastQ(finalFileName[0], true);
			fastQ2 = new FastQ(finalFileName[1], true);
		} else {
			fastQ1 = new FastQ(finalFileName[0], true);
		}
	}
	
	private String[] getFinalName(boolean isTmp) {
		String[] finalFileName = new String[isPairend? 2 : 1];
		if (isPairend) {
			finalFileName[0] = FileOperate.changeFileSuffix(outFileName, "_1", "fastq|fq", null);
			finalFileName[1] = FileOperate.changeFileSuffix(outFileName, "_2", "fastq|fq", null);
		} else {
			finalFileName[0] = outFileName;
		}
		if (isTmp) {
			for (int i = 0; i < finalFileName.length; i++) {
				finalFileName[i] = FileOperate.changeFileSuffix(outFileName, "_tmp", "fastq|fq", null);
			}
		}

		return finalFileName;
	}

	public FastQ[] getResultFastQ() {
		FastQ[] fastQ = new FastQ[isPairend? 2 : 1];
		fastQ[0] = fastQ1;
		if (isPairend) {
			fastQ[1] = fastQ2;
		}
		return fastQ;
	}
	
	@Override
	public void summary() {
		fastQ1.close();
		if (isPairend) {
			fastQ2.close();
		}
		if (isGenerateTmpFile) {
			String[] finalFileName = getFinalName(false);
			FileOperate.moveFile(true, fastQ1.getReadFileName(), finalFileName[0]);
			fastQ1 = new FastQ(finalFileName[0]);
			if (isPairend) {
				FileOperate.moveFile(true, fastQ2.getReadFileName(), finalFileName[1]);
				fastQ2 = new FastQ(finalFileName[1]);
			}
		}
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	
	public void clear() {
		/** 是否仅挑选没有mapping上的reads */
		justUnMapped = false;
		outFileName = null;
		initial = false;
		isPairend = false;
		/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字 */
		isGenerateTmpFile = true;
		firstSeenMates.clear();
		if (fastQ1 != null) fastQ1.close();
		
		if (fastQ2 != null) fastQ2.close();
		
		fastQ1 = null;
		fastQ2 = null;
		lastRecord = null;
		readsIndexNumFrom1 = null;
	}
	
	public static enum SamToFastqType {
		/** 全部reads */
		AllReads,
		/** Mapped Reads，双端测序只要有一段比对上就算是比对上了 */
		MappedReads,
		/** 双端测序只提取两端都比对上的 */
		MappedReadsPairend,
		/** 双端测序两端都没比对上的 */
		UnmappedReads
	}
}








