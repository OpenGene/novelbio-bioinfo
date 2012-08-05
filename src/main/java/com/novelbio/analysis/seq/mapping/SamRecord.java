package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class SamRecord extends SiteInfo {
	private static Logger logger = Logger.getLogger(SamRecord.class);
	SAMRecord samRecord;
	Boolean isJunctionReads;
	Boolean isHavePaireReads;
	int numMappedReadsInFile = 0;
	public SamRecord() {
		// TODO Auto-generated constructor stub
	}

	protected SamRecord(SAMRecord samRecord) {
		this.samRecord = samRecord;
		setSiteInfo(samRecord);
	}

	private void setSiteInfo(SAMRecord samRecord) {
		super.setCis5to3(!samRecord.getReadNegativeStrandFlag());
		super.setName(samRecord.getReadName());
		super.setRefID(samRecord.getReferenceName());
		super.setSeq(new SeqFasta(samRecord.getReadString()), false);
		super.setStartEndLoc(samRecord.getAlignmentStart(),
				samRecord.getAlignmentEnd());
	}

	public String getDescription() {
		return samRecord.toString();
	}

	public boolean isJunctionReads() {
		if (isJunctionReads != null) {
			return isJunctionReads;
		}
		if (getAlignmentBlocks().size() > 1) {
			isJunctionReads = true;
		} else {
			isJunctionReads = false;
		}
		return isJunctionReads;
	}

	public List<AlignmentBlock> getAlignmentBlocks() {
		List<AlignmentBlock> lsAlignmentBlocks = samRecord.getAlignmentBlocks();
		return lsAlignmentBlocks;
	}

	public boolean isMapped() {
		return !samRecord.getReadUnmappedFlag();
	}

	public Object getAttribute(String tag) {
		return samRecord.getAttribute(tag);
	}

	public boolean isUniqueMapping() {
		Object attrXT = samRecord.getAttribute("XT");
		if (attrXT != null) {
			if (!attrXT.equals("R"))
				return true;
		}
		Object attrCC = samRecord.getAttribute("NH");
		if (attrCC != null) {
			Integer number = (Integer) attrCC;
			if (number > 1) {
				return false;
			}
			return true;
		}
		return true;
	}

	public int getMapQuality() {
		return samRecord.getMappingQuality();
	}

	/**
	 * 本reads的权重，意思相同的reads在文件中有多少条 一般bwa出来的文件一行就是一条reads 而tophat出来的文件，一条reads
	 * mapping在几个位置就有几行
	 * */
	public int getNumMappedReadsInFile() {
		if (numMappedReadsInFile > 0) {
			return numMappedReadsInFile;
		}
		Object attrCC = samRecord.getAttribute("NH");
		if (attrCC != null) {
			numMappedReadsInFile = (Integer) attrCC;
			return numMappedReadsInFile;
		}
		String[] tmpInfo = null;
		if (samRecord.getAttribute("XA") != null) {
			tmpInfo = samRecord.getAttribute("XA").toString().split(";");
			numMappedReadsInFile = tmpInfo.length + 1;
			return numMappedReadsInFile;
		}
		numMappedReadsInFile = 1;
		return numMappedReadsInFile;
	}

	/**
	 * 是否为双端或者说有令一端 返回null：表示不知道到底有没有另一端，那么就要根据输入的文件进行判断
	 * */
	public boolean isHavePairEnd() {
		if (isHavePaireReads != null) {
			return isHavePaireReads;
		}
		String aa = samRecord.getMateReferenceName();
		if (aa.equals("*")) {
			if (getRefID().equals("*")) {
				try {
					samRecord.getMateUnmappedFlag();
					isHavePaireReads = true;
				} catch (Exception e) {
					isHavePaireReads = false;
				}
			}
			isHavePaireReads = false;
		} else
			isHavePaireReads = true;
		return isHavePaireReads;
	}

	/** 双端的另一半是否mapping上了，单端也返回false */
	public boolean isMateMapped() {
		if (!isHavePairEnd()) {
			return false;
		}
		boolean result = !samRecord.getMateUnmappedFlag();
		return result;
	}

	public String getMateReferenceName() {
		return samRecord.getMateReferenceName();
	}

	public int getMateAlignmentStart() {
		return samRecord.getMateAlignmentStart();
	}

	public boolean isMateCis5to3() {
		return !samRecord.getMateNegativeStrandFlag();
	}

	/** 给定一条reads，看是否为其成对的reads */
	public boolean isPaireReads(SamRecord samRecord) {
		if (isHavePairEnd()) {
			return false;
		}
		if (samRecord.getRefID().equals(getRefID())
				&& samRecord.getName().equals(getName())) {
			if (isMateMapped() && isMateCis5to3() == samRecord.isCis5to3()
					&& getMateAlignmentStart() == samRecord.getStartAbs()) {
				return true;
			} else if (!isMateMapped() && !samRecord.isMapped()) {
				return true;
			} else {
				logger.error("未知情况");
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 返回第一个记载的bedrecord 没有mapping上就返回null
	 * */
	public BedRecord toBedRecordSE() {
		if (!isMapped()) {
			return null;
		}
		BedRecord bedRecord = new BedRecord();
		bedRecord.setRefID(getRefID());
		bedRecord.setStartEndLoc(getStartAbs(), getEndAbs());
		bedRecord.setCIGAR(samRecord.getCigarString());
		bedRecord.setCis5to3(isCis5to3());
		bedRecord.setMapQuality(getMapQuality());
		bedRecord.setSeq(getSeqFasta());
		bedRecord.setScore(getMapQuality());
		// 计数，mapping到了几次
		bedRecord.setMappingNum(getNumMappedReadsInFile());
		bedRecord.setName(samRecord.getReadName());
		return bedRecord;
	}

	/** 这样返回就是每个SamRecord返回一系列BedRecord */
	public ArrayList<BedRecord> toBedRecordSELs() {
		ArrayList<BedRecord> lsBedRecords = new ArrayList<BedRecord>();
		if (!isMapped()) {
			return new ArrayList<BedRecord>();
		}
		lsBedRecords.add(toBedRecordSE());
		/**
		 * XA: Alternative hits; format: (chr,pos,CIGAR,NM;) XT:A:U flag in the
		 * sam file denotes unique read and XT:A:R denotes multiple mappings for
		 * that read. For paired-end reads, you might also want to consider the
		 * flag XT:A:M (one-mate recovered) which means that one of the pairs is
		 * uniquely mapped and the other isn't.
		 */
		String[] tmpInfo = null;
		if (samRecord.getAttribute("XA") != null) {
			tmpInfo = samRecord.getAttribute("XA").toString().split(";");
		}

		if (tmpInfo == null) {
			return lsBedRecords;
		}
		for (String string : tmpInfo) {
			BedRecord bedRecord = new BedRecord();
			String[] info = string.split(",");
			bedRecord.setRefID(info[0]);
			int start1 = Integer.parseInt(info[1].substring(1));
			int end1 = start1 + Length() - 1;
			bedRecord.setStartEndLoc(start1, end1);
			bedRecord.setCIGAR(info[2]);
			bedRecord.setCis5to3(info[1].charAt(0));
			bedRecord.setMappingNum(getNumMappedReadsInFile());
			bedRecord.setMapQuality(getMapQuality());
			bedRecord.setScore(samRecord.getMappingQuality());
			bedRecord.setSeq(new SeqFasta(samRecord.getReadString()), false);
			bedRecord.setName(samRecord.getReadName());
			lsBedRecords.add(bedRecord);
		}
		return lsBedRecords;
	}

	public int hashCode() {
		return samRecord.hashCode();
	}

	public boolean equals(Object o) {
		return samRecord.equals(o);
	}

}
