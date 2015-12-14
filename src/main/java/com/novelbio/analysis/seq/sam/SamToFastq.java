package com.novelbio.analysis.seq.sam;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import picard.PicardException;

import com.novelbio.analysis.seq.AlignRecord;
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
	EnumSamToFastqType samToFastqType = EnumSamToFastqType.AllReads;
	
	/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字 */
	boolean isGenerateTmpFile = true;
	/** 临时结果文件名 */
	String[] outFileNameTmp;
	/** 修改好的文件名 */
	String[] outFileName;
	
	boolean isPairend = false;
	
	FastQ fastQ1;
	FastQ fastQ2;
	
    final Map<String, SamRecord> firstSeenMates = new WeakHashMap<String, SamRecord>(500);
    
	/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字，默认是true */
    public SamToFastq() {}
    
	/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字，默认是true */
    public SamToFastq(boolean isGenerateTmpFile) {
    	this.isGenerateTmpFile = isGenerateTmpFile;
	}

	/** 根据是否为mapping上的reads，自动设定文件名，并返回设定好的文件名 */
	public void setOutFileInfo(SamFile samFile, EnumSamToFastqType samToFastqType) {
		clear();
		String fileName = samFile.getFileName();
		this.samToFastqType = samToFastqType;
		this.isPairend = samFile.isPairend();
		fileName = FileOperate.changeFileSuffix(fileName, samToFastqType.getSuffix(), "fastq.gz");
		
		outFileName = getFinalName(fileName, false);
		if (isGenerateTmpFile) {
			outFileNameTmp =  getFinalName(fileName, true);
		}
	}
	/** 根据是否为mapping上的reads，自动设定文件名，并返回设定好的文件名
	 * 
	 * @param isPairend
	 * @param outFileName 必须以fastq.gz, fastq, fq.gz, fq 结尾
	 * @param samToFastqType
	 */
	public void setOutFileInfo(boolean isPairend, String outFileName, EnumSamToFastqType samToFastqType) {
		clear();
		this.isPairend = isPairend;
		this.samToFastqType = samToFastqType;
		this.outFileName = getFinalName(outFileName, false);
		if (isGenerateTmpFile) {
			outFileNameTmp = getFinalName(outFileName, true);
		}
	}
	
	/** 根据是否为mapping上的reads，指定文件名，并返回设定好的文件名 */
	public void setOutFileInfo(boolean isPairend, String outFile1, String outFile2, EnumSamToFastqType samToFastqType) {
		clear();
		this.isPairend = isPairend;
		this.samToFastqType = samToFastqType;
		outFileName = new String[isPairend? 2 : 1];
		outFileNameTmp = new String[isPairend? 2 : 1];
		outFileName[0] = outFile1;
		if (isPairend) {
			outFileName[1] = outFile2;
		}
		if (isGenerateTmpFile) {
			outFileNameTmp[0] = FileOperate.changeFileSuffix(outFileName[0], "_tmp", null);
			if (isPairend) {
				outFileNameTmp[1] = FileOperate.changeFileSuffix(outFileName[1], "_tmp", null);
			}
		}
		initialFq();
	}
	
	public void initialFq() {
		if (samToFastqType == EnumSamToFastqType.UnmappedReadsOneFile) {
			isPairend = false;
		}
		
		if (isGenerateTmpFile) {
			fastQ1 = new FastQ(outFileNameTmp[0], true);
			if (isPairend) {
				fastQ2 = new FastQ(outFileNameTmp[1], true);
			}
		} else {
			fastQ1 = new FastQ(outFileName[0], true);
			if (isPairend) {
				fastQ2 = new FastQ(outFileName[1], true);
			}
		}
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord) {
			SamRecord samRecord = (SamRecord)alignRecord;
			if (isCanAddSamRecord(samRecord)) {
				addAlignRecordSam(samRecord);
            }
		} else {
			throw new ExceptionSamError("samtofastq can only support samrecord!");
		}
	}
	
	protected boolean isCanAddSamRecord(SamRecord samRecord) {
		//比对到多个位置的，只取其中一条
		if (samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != 1) {
			return false;
		}
		
		if (samToFastqType == EnumSamToFastqType.UnmappedReads) {
			if (samRecord.isMapped() && (!isPairend || isPairend && samRecord.isMateMapped())) {
				return false;
			}
		} else if (samToFastqType == EnumSamToFastqType.UnmappedReadsBoth) {
			if (samRecord.isMapped() || (isPairend && samRecord.isMateMapped())) {
				return false;
			}
		} else if (samToFastqType == EnumSamToFastqType.UnmappedReadsOneFile) {
			if (samRecord.isMapped()) {
				return false;
			}
		}else if (samToFastqType == EnumSamToFastqType.MappedReads) {
			if (!samRecord.isMapped() && (!isPairend || isPairend && !samRecord.isMateMapped())) {
				return false;
			}
		} else if (samToFastqType == EnumSamToFastqType.MappedReadsPairend) {
			if (!samRecord.isMapped() || (isPairend && !samRecord.isMateMapped())) {
				return false;
			}
		} else if (samToFastqType == EnumSamToFastqType.MappedReadsOnlyOne) {
			if (isPairend 
					&& (
							(samRecord.isMapped() && samRecord.isMateMapped())
							|| (!samRecord.isMapped() && !samRecord.isMateMapped())
							)
			) {
				return false;
			}
		}
		return true;
	}
	
	private void addAlignRecordSam(SamRecord samRecord) {
		if (samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != null && samRecord.getMapIndexNum() != 1) {
			return;
		}
		
		if (isPairend) {
			final String currentReadName = samRecord.getName();
			final SamRecord firstRecord = firstSeenMates.remove(currentReadName);
			if (firstRecord == null) {
				firstSeenMates.put(currentReadName, samRecord);
			} else {
				try {
					assertPairedMates(firstRecord, samRecord);
					if (firstSeenMates.size() > 1000000) {
						throw new ExceptionSamError("here are more than" + firstSeenMates.size() + "reads cannot find their mate, "
								+ "maybe you should rerun the task using \"unpaired\" parameter");
                    }
				} catch (Exception e) {
					//同一条reads比对两次就会有这个结果，没关系继续放入hashmap
					firstSeenMates.put(currentReadName, samRecord);
					if (firstSeenMates.size() > 100000) {
						firstSeenMates.clear();
                    }
					return;
				}
				
				final SamRecord read1 =  samRecord.isFirstRead() ? samRecord : firstRecord;
				final SamRecord read2 = samRecord.isFirstRead() ? firstRecord : samRecord;
				fastQ1.writeFastQRecord(read1.toFastQRecord());
				fastQ2.writeFastQRecord(read2.toFastQRecord());
			}
		} else {
			fastQ1.writeFastQRecord(samRecord.toFastQRecord());
		}
	}
	
    private void assertPairedMates(final SamRecord record1, final SamRecord record2) {
        if (! (record1.isFirstRead() && !record2.isFirstRead() ||
               record2.isFirstRead() && !record1.isFirstRead() ) ) {
            throw new PicardException("Illegal mate state: " + record1.getName() + " " + record2.getName());
        }
    }
    
    /** 返回输出的文件名 */
    public String[] getOutFileName() {
		return outFileName;
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
		logger.info("if need change file name "+ isGenerateTmpFile);
		if (isGenerateTmpFile) {
			logger.info("change file name from "+ outFileNameTmp[0] + " to " + outFileName[0]);
			FileOperate.moveFile(true, outFileNameTmp[0], outFileName[0]);
			fastQ1 = new FastQ(outFileName[0]);
			if (isPairend) {
				logger.info("change file name from "+ outFileNameTmp[1] + " to " + outFileName[1]);
				FileOperate.moveFile(true, outFileNameTmp[1], outFileName[1]);
				fastQ2 = new FastQ(outFileName[1]);
			}
		}
		if (!firstSeenMates.isEmpty()) {
			logger.error(firstSeenMates.size() + " reads have unpaired");
		}
		firstSeenMates.clear();
//		for (String name : firstSeenMates.keySet()) {
//			System.out.println(name);
//		}
	}
	
	private String[] getFinalName(String outFileName, boolean isTmp) {
		String[] finalFileName = new String[isPairend? 2 : 1];
		if (isPairend) {
			finalFileName[0] = FileOperate.changeFileSuffix(outFileName, "_1", "fastq|fq", null);
			finalFileName[1] = FileOperate.changeFileSuffix(outFileName, "_2", "fastq|fq", null);
		} else {
			finalFileName[0] = outFileName;
		}
		if (isTmp) {
			for (int i = 0; i < finalFileName.length; i++) {
				finalFileName[i] = FileOperate.changeFileSuffix(finalFileName[i], "_tmp", "fastq|fq", null);
			}
		}

		return finalFileName;
	}

	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	
	public void clear() {
		/** 是否仅挑选没有mapping上的reads */
		samToFastqType = EnumSamToFastqType.AllReads;		
		outFileName = null;
		isPairend = false;
		/** 是否产生临时文件，意思就是如果顺利结束才会将文件名改成正式名字 */
		isGenerateTmpFile = true;
		firstSeenMates.clear();
		if (fastQ1 != null) fastQ1.close();
		
		if (fastQ2 != null) fastQ2.close();
		
		fastQ1 = null;
		fastQ2 = null;
	}
	
	public static enum EnumSamToFastqType {
		/** 全部reads */
		AllReads("_All"),
		/** Mapped Reads，双端测序只要有一段比对上就算是比对上了 */
		MappedReads("_Mapped"),
		/** 双端测序只提取两端都比对上的 */
		MappedReadsPairend("_BothMapped"),
		/** 双端测序一端比上另一端没比上 */
		MappedReadsOnlyOne("_OnlyOneMapped"),
		/** 双端测序只要有一个没比对上的 */
		UnmappedReads("_UnMapped"),
		/** 双端测序只要有一个没比对上的 */
		UnmappedReadsOneFile("_UnMappedOneFile"),
		/** 双端测序两端都没比对上的 */
		UnmappedReadsBoth("_BothUnMapped");
		
		String suffix;
		EnumSamToFastqType(String suffix) {
			this.suffix = suffix;
		}
		
		public String getSuffix() {
			return suffix;
		}
		
	}
}








