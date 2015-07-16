package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMSequenceDictionary;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/** 将sam文件转化为bam文件，<b>仅用于没有排序过的sam文件</b><br>
 * 其中添加multiHit的功能仅适用于bowtie和bwa 的mem
 *  */
public class SamToBamSort {
	private static final Logger logger = Logger.getLogger(SamToBamSort.class);
	boolean writeToBam = true;
	SamFile samFileBam;//需要转化成的bam文件
	String outFileName;
	SamFile samFileSam;//输入的sam文件
	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<>();
	RunProcess runProcess;
	/** 输入lsChrId，可以用这个来调整samFile的head */
	SAMSequenceDictionary samSequenceDictionary;
	SamReorder samReorder;
	SamAddMultiFlag samAddMultiFlag;
	boolean addMultiHitFlag = false;
	boolean isPairend = false;
	boolean isUsingTmpFile = false;
	/** 默认不排序 */
	boolean isNeedSort = false;
	
	/** 需要转化成的bam文件名，自动从sam文件判定是否为双端，会关闭Sam流
	 * @param outFileName
	 * @param samFileSam
	 */
	public SamToBamSort(String outFileName, SamFile samFileSam) {
		this.outFileName = outFileName;
		this.samFileSam = samFileSam;
		this.isPairend = samFileSam.isPairend();
	}
	/** 需要转化成的bam文件名 */
	public SamToBamSort(String outFileName, SamFile samFileSam, boolean isPairend) {
		this.outFileName = outFileName;
		this.samFileSam = samFileSam;
		this.isPairend = isPairend;
	}
	/** 当本进程出错的时候需要将该进程也关闭，主要是关闭cmd命令 */
	public void setRunProcessNeedStopWhenError(RunProcess runProcess) {
		this.runProcess = runProcess;
	}
	/** 是否写入bam文件，默认写入
	 * 有时候mapping但不需要写入文件，譬如过滤掉rrna reads的时候，
	 * 只需要将没有mapping的reads输出即可，并不需要要把bam文件输出
	 * @param writeToBam
	 */
	public void setWriteToBam(boolean writeToBam) {
		this.writeToBam = writeToBam;
	}
	/** 是否需要排序，默认false */
	public void setNeedSort(boolean isNeedSort) {
		this.isNeedSort = isNeedSort;
	}
	/** 是否根据samSequenceDictionary重新排列samHeader中的顺序，目前只有mapsplice才遇到 */
	public void setSamSequenceDictionary(
			SAMSequenceDictionary samSequenceDictionary) {
		this.samSequenceDictionary = samSequenceDictionary;
	}
	/** 是否使用临时文件<br>
	 * 意思就是说在转化过程中用中间文件保存，只有当成功后才会改为最后文件名<br>
	 * <b>默认false</b>，因为mapping模块里面已经采用了中间文件名
	 * @param isUsingTmpFile
	 */
	public void setUsingTmpFile(boolean isUsingTmpFile) {
		this.isUsingTmpFile = isUsingTmpFile;
	}
	/**
	 * 设定是否添加比对到多处的标签，暂时仅适用于bowtie2
	 * bwa不需要设定该参数
	 * @param addMultiHitFlag
	 */
	public void setAddMultiHitFlag(boolean addMultiHitFlag) {
		this.addMultiHitFlag = addMultiHitFlag;
	}
	
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		if (lsAlignmentRecorders == null) return;
			
		this.lsAlignmentRecorders = lsAlignmentRecorders;
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder instanceof SamFileStatistics) {
				((SamFileStatistics)alignmentRecorder).setStandardData(samFileSam.getMapChrID2Length());
			}
		}
	}

	/** 转换结束后，关闭输出的bam文件，但是不关闭输入的sam文件 */
	public void convert() {
		setBamWriteFile();
		if (addMultiHitFlag) {
			/** 线程是否崩溃 */
			final boolean[] isCollapse = new boolean[]{false};
			final Throwable[] throwable = new Throwable[]{null};
			samAddMultiFlag = new SamAddMultiFlag();
			samAddMultiFlag.setPairend(isPairend);
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						AddMultiFlag(isCollapse);
					} catch (Throwable e) {
						runProcess.threadStop();
						samAddMultiFlag.finish();
						isCollapse[0] = true;
						throwable[0] = e;
					}
				}
			});
			thread.start();
			
			try {
				for (SamRecord samRecord : samAddMultiFlag.readlines()) {
					addToRecorderAndWriteToBam(samRecord);
					if (isCollapse[0]) {
						runProcess.threadStop();
					}
				}
			} catch (Throwable e) {
				isCollapse[0] = true;
				throw e;
			} finally{
				if (runProcess != null) {
					runProcess.threadStop();
				}
			}
			
			//意思是在AddMultiFlag线程中报错的，就把这个异常抛出
			if (isCollapse[0]) {
				throw new RuntimeException(throwable[0]);
			}
			
		} else {
			//直接从sam文件中读取
			try {
				convertNotAddMultiFlag();
			} catch (Throwable e) {
				runProcess.threadStop();
				throw e;
			}
		}
		finishConvert();
	}
	
	private void setBamWriteFile() {
		String outFileName = this.outFileName;
		if (!writeToBam) return;
		
		if (isUsingTmpFile) {
			outFileName = getTmpFileName();
		}

		SAMFileHeader samFileHeader = samFileSam.getHeader();
		if (samSequenceDictionary != null) {
			samReorder = new SamReorder();
			samReorder.setSamSequenceDictionary(samSequenceDictionary);
			samReorder.setSamFileHeader(samFileHeader);
			samReorder.reorder();
			samFileHeader = samReorder.getSamFileHeaderNew();
		}
		
		if (isNeedSort && samFileSam.getHeader().getSortOrder()== SortOrder.unsorted) {
			samFileHeader.setSortOrder(SAMFileHeader.SortOrder.coordinate);
			samFileBam = new SamFile(outFileName, samFileHeader, false);
		} else {
			samFileBam = new SamFile(outFileName, samFileHeader);
		}
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			if (alignmentRecorder instanceof SamFileStatistics) {
				((SamFileStatistics)alignmentRecorder).setStandardData(samFileSam.getMapChrID2Length());
			}
		}
	}
	
	private String getTmpFileName() {
		if (!writeToBam) {
			return null;
		}
		return FileOperate.changeFileSuffix(outFileName, "_tmp", null);
	}
	
	private void convertNotAddMultiFlag() {
		int i = 0;
		for (SamRecord samRecord : samFileSam.readLines()) {
			if (samReorder != null) {
				samReorder.copeReads(samRecord);
			}
			if (i++%1000000 == 0) {
				logger.info("read lines: " + i);
				System.gc();
			}
			addToRecorderAndWriteToBam(samRecord);
		}
	}
	
	private void AddMultiFlag(final boolean[] isCollapse) {
		int i = 0;
		for (SamRecord samRecord : samFileSam.readLines()) {
			if (samReorder != null) {
				samReorder.copeReads(samRecord);
			}
			if (isCollapse[0]) {
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
	
	private void  addToRecorderAndWriteToBam(SamRecord samRecord) {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			try {
				alignmentRecorder.addAlignRecord(samRecord);
			} catch (Exception e) { }
		}
		if (writeToBam) {
			samFileBam.writeSamRecord(samRecord);
		}
	}
	
	private void finishConvert() {
		for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
			alignmentRecorder.summary();
		}
		if (!writeToBam) {
			return;
		}
		samFileBam.close();
		if (isUsingTmpFile) {
			samFileBam = null;
		}
		FileOperate.moveFile(true, getTmpFileName(), outFileName);
		samFileBam = new SamFile(outFileName);
		samFileSam.setParamSamFile(samFileBam);
	}
	
	/** 返回转换好的bam文件 */
	public SamFile getSamFileBam() {
		return samFileBam;
	}
	
}
