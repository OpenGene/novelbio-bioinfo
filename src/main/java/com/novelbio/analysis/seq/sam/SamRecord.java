package com.novelbio.analysis.seq.sam;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.Cigar;
import net.sf.samtools.SAMBinaryTagAndUnsignedArrayValue;
import net.sf.samtools.SAMBinaryTagAndValue;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.SAMValidationError;
import net.sf.samtools.util.BinaryCodec;
import net.sf.samtools.util.DateParser;
import net.sf.samtools.util.Iso8601Date;
import net.sf.samtools.util.StringUtil;

public class SamRecord extends SiteInfo implements AlignRecord{
	private static Logger logger = Logger.getLogger(SamRecord.class);
	SAMRecord samRecord;
	Boolean isJunctionReads;
	Boolean isHavePaireReads;
	int numMappedReadsInFile = 0;
	
	
    // From SAM specification
    private static final int QNAME_COL = 0;
    private static final int FLAG_COL = 1;
    private static final int RNAME_COL = 2;
    private static final int POS_COL = 3;
    private static final int MAPQ_COL = 4;
    private static final int CIGAR_COL = 5;
    private static final int MRNM_COL = 6;
    private static final int MPOS_COL = 7;
    private static final int ISIZE_COL = 8;
    private static final int SEQ_COL = 9;
    private static final int QUAL_COL = 10;

    private static final int NUM_REQUIRED_FIELDS = 11;

    
    
	public SamRecord() {
		// TODO Auto-generated constructor stub
	}

	public SamRecord(SAMRecord samRecord) {
		this.samRecord = samRecord;
		setSiteInfo(samRecord);
	}
	protected SAMRecord getSamRecord() {
		return samRecord;
	}
	private void setSiteInfo(SAMRecord samRecord) {
		try { super.setCis5to3(!samRecord.getReadNegativeStrandFlag()); } catch (Exception e) { }
		try { super.setRefID(samRecord.getReferenceName()); } catch (Exception e) { }
		try { super.setSeq(new SeqFasta(samRecord.getReadString()), false); } catch (Exception e) { }
		try { super.setStartEndLoc(samRecord.getAlignmentStart(), samRecord.getAlignmentEnd()); } catch (Exception e) { }
		try { super.setName(samRecord.getReadName());} catch (Exception e) { }
	}
	public String getDescription() {
		return samRecord.toString();
	}	  
	public boolean isJunctionReads() {
		if (isJunctionReads != null) {
			return isJunctionReads;
		}
		if (samRecord.getCigar().toString().contains("N")) {
			return true;
		} else {
			isJunctionReads = false;
		}
		return isJunctionReads;
	}
	
	@Override
	public boolean isJunctionCovered() {
		if (samRecord.getCigar().toString().contains("N")) {
			return true;
		}
		return false;
	}
	
	/** 当为junction reads的时候才会有意义 */
	public ArrayList<Align> getAlignmentBlocks() {
		if (samRecord.getCigar().toString().contains("N")) {
			List<AlignmentBlock> lsAlignmentBlocks = samRecord.getAlignmentBlocks();
			ArrayList<Align> lsAligns = new ArrayList<Align>();
			for (AlignmentBlock alignmentBlock : lsAlignmentBlocks) {
				Align align = new Align(getRefID(), alignmentBlock.getReferenceStart(), alignmentBlock.getLength() + alignmentBlock.getReferenceStart() - 1);
				align.setCis5to3(isCis5to3());
				lsAligns.add(align);
			}
			return lsAligns;
		}
		ArrayList<Align> lsAligns = new ArrayList<Align>();
		Align align = new Align(getRefID(), getStartAbs(), getEndAbs());
		align.setCis5to3(isCis5to3());
		lsAligns.add(align);
		return lsAligns;
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
			if (!attrXT.equals('R'))
				return true;
			else {
				if (getMappingNum() == 1) {
					return true;
				}
				return false;
			}
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

	public Integer getMapQuality() {
		return samRecord.getMappingQuality();
	}
	/**
	 * reads的权重，意思相同的reads在本sam文件中出现了几次
	 * bwa的结果，一条reads只有一行，所以恒返回1
	 * tophat的结果，一条reads如果mapping至多个位置，在文件中就会出现多次，所以返回可能大于1
	 * */
	protected int getMappedReadsWeight() {
		Object attrCC = samRecord.getAttribute("NH");
		if (attrCC != null) {
			numMappedReadsInFile = (Integer) attrCC;
			return numMappedReadsInFile;
		}
		return 1;
	}
	/**
	 * 本序列可以mapping至几个不同位置
	 * */
	public Integer getMappingNum() {
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
			if (samRecord.getAttribute("XT").equals('U')) {
				numMappedReadsInFile = 1;
				return numMappedReadsInFile;
			}
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
	public Cigar getCigar() {
		return samRecord.getCigar();
	}
	/** 给定一条reads，看是否为其成对的reads */
	public boolean isPaireReads(SamRecord samRecord) {
		if (!isHavePairEnd()) {
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
	public FastQRecord toFastQRecord() {
		FastQRecord fastQRecord = new FastQRecord();
		fastQRecord.setFastaQuality(samRecord.getBaseQualityString());
		fastQRecord.setName(getName());
		fastQRecord.setSeq(samRecord.getReadString());
		return fastQRecord;
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
		bedRecord.setMappingNum(getMappingNum());
		bedRecord.setName(samRecord.getReadName());
		bedRecord.setAlignmentBlocks(getAlignmentBlocks());
		return bedRecord;
	}

	/** 这样返回就是每个SamRecord返回一系列BedRecord */
	public ArrayList<BedRecord> toBedRecordSELs() {
		ArrayList<BedRecord> lsBedRecords = new ArrayList<BedRecord>();
		if (!isMapped()) {
			return new ArrayList<BedRecord>();
		}
		lsBedRecords.add(toBedRecordSE());
		
		if (isUniqueMapping()) {
			return lsBedRecords;
		}
		
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
			bedRecord.setMappingNum(getMappingNum());
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
	@Override
	public String toString() {
		return samRecord.getSAMString();
	}
	@Override
	public String getRawStringInfo() {
		return samRecord.getSAMString();
	}
}
