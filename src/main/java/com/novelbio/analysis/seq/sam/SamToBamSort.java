package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMSequenceDictionary;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

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
	
	/** 输入lsChrId，可以用这个来调整samFile的head */
	SAMSequenceDictionary samSequenceDictionary;
	SamReorder samReorder;
	
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
			convertAndAddMultiFlag();
		} else {
			convertNotAddMultiFlag();
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
		for (SamRecord samRecord : samFileSam.readLines()) {
			if (samReorder != null) {
				samReorder.copeReads(samRecord);
			}
			for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
				try {
					alignmentRecorder.addAlignRecord(samRecord);
				} catch (Exception e) { }
			}
			if (writeToBam) {
				samFileBam.writeSamRecord(samRecord);
			}
		}
	}
	
	/** 进行转换 */
	private void convertAndAddMultiFlag() {
		Set<String> setTmp = new HashSet<>();
		/** 相同名字的序列 */
		final Map<String, List<SamRecord>> mapMateInfo2pairReads = new LinkedHashMap<>();
		int i = 0;
		for (SamRecord samRecord : samFileSam.readLines()) {
			if (samReorder != null) {
				samReorder.copeReads(samRecord);
			}
			if (!samRecord.isMapped()) {
				logger.debug("unmapped");
			}
			if (i++%1000000 == 0) {
				logger.info("read lines: " + i);
				System.gc();
			}
			if ((!isPairend && setTmp.size() == 0) || (isPairend && samRecord.isFirstRead())
					) {
				String samName = samRecord.getName();
				if (!setTmp.contains(samName)) {
					addMapSamRecord(mapMateInfo2pairReads);
					setTmp.clear();
					setTmp.add(samName);
					mapMateInfo2pairReads.clear();
				}
				setTmp.add(samRecord.getName());
			}
			addSamRecordToMap(isPairend, samRecord, mapMateInfo2pairReads);
		}
		addMapSamRecord(mapMateInfo2pairReads);
	}
	
	private void addSamRecordToMap(boolean isPairend, SamRecord samRecord, 
			Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		String pairInfo = samRecord.getNameAndFirstSite();
		if (isPairend) {
			//首先看第一端是否出现，出现了就获取第一端，然后放到第二端
			if (mapMateInfo2pairReads.containsKey(pairInfo)) {
				 List<SamRecord> lsRecords = mapMateInfo2pairReads.get(pairInfo);
				if (lsRecords.size() > 1) {
					SamRecord mate = findCloseSamRecord(lsRecords.get(0), lsRecords.get(1), samRecord);
					if (mate != null) {
						lsRecords.set(1, samRecord);
						//将多的全部清掉
						if (lsRecords.size() > 2) {
							SamRecord[] samRecords = new SamRecord[]{lsRecords.get(0), lsRecords.get(1)};
							lsRecords.clear();
							for (SamRecord samRecord2 : samRecords) {
								lsRecords.add(samRecord2);
							}
						}
					} else {
						lsRecords.add(samRecord);
					}
				} else {
					lsRecords.add(samRecord);
				}
			} else {
				addNewRecordInMap(samRecord, mapMateInfo2pairReads);
			}
		} else {
			addNewRecordInMap(samRecord, mapMateInfo2pairReads);
		}
	}
	
	private SamRecord findCloseSamRecord(SamRecord record1, SamRecord record2_1, SamRecord record2_2) {
		if (!record1.isMapped()) {
			return null;
		}
		if (record2_1.isMapped() && !record2_2.isMapped()) {
			return record2_1;
		} else if (!record2_1.isMapped() && record2_2.isMapped()) {
			return record2_2;
		}
		//两个都比上了
		if (record1.getRefID().equals(record2_1.getRefID()) ) {
			if (record1.getRefID().equals(record2_2.getRefID())) {
				int start1 = record1.getStartAbs(), end1 = record1.getEndAbs();
				int start2 = record2_1.getStartAbs(), end2 = record2_1.getEndAbs();
				int start3 = record2_2.getStartAbs(), end3 = record2_2.getEndAbs();
				int distance1 = (start1 < start2)? start2 - end1 : start1 - end2;
				int distance2 = (start1 < start3)? start3 - end1 : start1 - end3;
				return (distance1 < distance2) ? record2_1 : record2_2;
			} else {
				return record2_1;
			}
		} else {
			if (record1.getRefID().equals(record2_2.getRefID())) {
				return record2_2;
			} else {
				return null;
			}
		}
	}
	
	private void addNewRecordInMap(SamRecord samRecord, Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		List<SamRecord> lsRecords = new ArrayList<SamRecord>();
		lsRecords.add(samRecord);
		String pairMateInfo = samRecord.getNameAndFirstSite();
		mapMateInfo2pairReads.put(pairMateInfo, lsRecords);
	}
	
	/**
	 * @param lsSamRecords
	 * @param mapHitNum 小于0表示不需要调整flag
	 */
	private void addMapSamRecord(Map<String, List<SamRecord>> mapMateInfo2pairReads) {
		int multiHitNum = mapMateInfo2pairReads.size();
		int i = 0;
		for (List<SamRecord> samRecords : mapMateInfo2pairReads.values()) {
			i++;
			for (SamRecord samRecord : samRecords) {
				if (samRecord != null) {
					samRecord.setMultiHitNum(multiHitNum);
					samRecord.setMapIndexNum(i);
					
					for (AlignmentRecorder alignmentRecorder : lsAlignmentRecorders) {
						try {
							alignmentRecorder.addAlignRecord(samRecord);
						} catch (Exception e) { e.printStackTrace();}
					}
					if (writeToBam) {
						samFileBam.writeSamRecord(samRecord);
					}
				}
			}
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
		samFileBam.setParamSamFile(samFileSam);
		samFileBam.bamFile = true;
	}
	
	/** 返回转换好的bam文件 */
	public SamFile getSamFileBam() {
		return samFileBam;
	}
	
}
