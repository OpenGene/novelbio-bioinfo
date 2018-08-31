package com.novelbio.bioinfo.sam;

import htsjdk.samtools.AlignmentBlock;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecord.SAMTagAndValue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.ExceptionNullParam;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignRecord;
import com.novelbio.bioinfo.bed.BedRecord;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fastq.FastQRecord;

public class SamRecord implements AlignRecord {
	private static Logger logger = Logger.getLogger(SamRecord.class);
	SAMRecord samRecord;
	Boolean isJunctionReads;
	int numMappedReadsInFile = 0;
    
	public SamRecord() {}

	public SamRecord(SAMRecord samRecord) {
		this.samRecord = samRecord;
	}
	protected SAMRecord getSamRecord() {
		return samRecord;
	}
	
	public void setHeader(SAMFileHeader samFileHeader) {
		samRecord.setHeader(samFileHeader);
	}
	
	public void setReferenceName(String chrID) {
		samRecord.setReferenceName(chrID);
	}
	
    /**
     * Do not modify the value returned by this method.  If you want to change the bases, create a new
     * byte[] and call setReadBases() or call setReadString().
     * @return read sequence as ASCII bytes ACGTN=.
     */
	public byte[] getReadBase() {
		return samRecord.getReadBases();
	}
    /**
     * Do not modify the value returned by this method.  If you want to change the qualities, create a new
     * byte[] and call setBaseQualities() or call setBaseQualityString().
     * @return Base qualities, as binary phred scores (not ASCII).
     */
	public byte[] getBaseQualities() {
		return samRecord.getBaseQualities();
	}
	
	public String getDescription() {
		return samRecord.toString();
	}
	
	public boolean isJunctionCovered() {
		if (isJunctionReads != null) {
			return isJunctionReads;
		}
		if (samRecord.getCigar().toString().contains("N")) {
			isJunctionReads = true;
		} else {
			isJunctionReads = false;
		}
		return isJunctionReads;
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
	public List<SAMTagAndValue> getAttributes() {
		return samRecord.getAttributes();
	}
	
	public void clearAttributes() {
		samRecord.clearAttributes();
	}
	
	public void setAttribute(final String tag, final Object value) {
		samRecord.setAttribute(tag, value);
	}
	@Override
	public boolean isUniqueMapping() {
		Object attrXT = samRecord.getAttribute("XT");
		if (samRecord.getMappingQuality() < 10) {
			return false;
		}
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
		Integer attrCC = samRecord.getIntegerAttribute("NH");
		if (attrCC != null) {
			if (attrCC > 1) {
				return false;
			}
			return true;
		}
		attrCC = samRecord.getIntegerAttribute("IH");
		if (attrCC != null) {
			if (attrCC > 1) {
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
	public int getMappedReadsWeight() {
		//Tophat的标记，bowtie2我们会人为标记该flag
		Integer attrCC = null;
		try {
			attrCC = samRecord.getIntegerAttribute("NH");
		} catch (Exception e) { }
		if (attrCC != null) {
			numMappedReadsInFile = attrCC;
			return numMappedReadsInFile;
		}
		//mapsplice的标记
		try {
			attrCC = samRecord.getIntegerAttribute("IH");
		} catch (Exception e) {}
		
		if (attrCC != null) {
			numMappedReadsInFile = attrCC;
			return numMappedReadsInFile;
		}
		return 1;
	}
	
	/**
	 * 本序列在本文件中出现了几次，
	 * 意思就是如果是非unique mapping，但是该Reads只出现一次，则返回1，譬如BWA<br>
	 * 如果是Tophat，则会出现多次，则返回多次的信息
	 */
	@Deprecated
	public Integer getMappingNum() {
		if (numMappedReadsInFile > 0) {
			return numMappedReadsInFile;
		}
		//Tophat的标记，bowtie2我们会人为标记该flag
		Integer attrCC = samRecord.getIntegerAttribute("NH");
		if (attrCC != null) {
			numMappedReadsInFile = attrCC;
			return numMappedReadsInFile;
		}
		//mapsplice的标记
		attrCC = samRecord.getIntegerAttribute("IH");
		if (attrCC != null) {
			numMappedReadsInFile = attrCC;
			return numMappedReadsInFile;
		}
		//bwa的标记
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
	
	/** 设定本reads在sam文件中出现了几次，也就是比对到基因组的几个位置去了
	 * 就是修改NH标签
	 * @param multiHitNum 出现了几次，如果小于等于0就不设定
	 */
	public void setMultiHitNum(int multiHitNum) {
		if (multiHitNum < 1) return;
		samRecord.setAttribute("NH", multiHitNum);
	}
	
	/** 如果一条reads在基因组上出现了若干次，那么该reads是第几个出现的
	 * 其中tophat从0开始计算，mapsplice从1开始计算，novelbio添加的flag也是从1 开始
	 * @return
	 */
	public Integer getMapIndexNum() {
		Integer attrCC = samRecord.getIntegerAttribute("HI");
		if (attrCC == null) {
			return 1;
		}
		return attrCC;
	}
	/** 如果一条reads在基因组上出现了若干次，那么该reads是第几个出现的
	 * novelbio添加的flag是从1 开始
	 * @param multiHitNum 出现了几次，如果小于等于0就不设定
	 */
	public void setMapIndexNum(int multiHitNum) {
		if (multiHitNum < 1) return;
		samRecord.setAttribute("HI", multiHitNum);
	}
	/** the read is paired in sequencing, no matter whether it is mapped in a pair. */
	public boolean isHavePairEnd() {
		return samRecord.getReadPairedFlag();
	}

	/** 双端的另一半是否mapping上了，单端也返回false */
	public boolean isMateMapped() {
		if (!isHavePairEnd()) {
			return false;
		}
		boolean result = !samRecord.getMateUnmappedFlag();
		return result;
	}
	
	public String getMateRefID() {
		return samRecord.getMateReferenceName();
	}

	public int getMateAlignmentStart() {
		return samRecord.getMateAlignmentStart();
	}
	
	public SamRGroup getReadGroup() {
		SAMReadGroupRecord samReadGroupRecord = samRecord.getReadGroup();
		if (samReadGroupRecord == null) {
			return null;
		}
		return new SamRGroup(samReadGroupRecord);
	}
	
	public void setReadGroup(SamRGroup samRGroup) {
		samRecord.setAttribute("RG", samRGroup.getSamReadGroupRecord().getId());
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
	public String getReadString() {
		return samRecord.getReadString();
	}
	public String getBaseQualityString() {
		return samRecord.getBaseQualityString();
	}
	/**
	 * 返回第一个记载的bedrecord 没有mapping上就返回null
	 * bedRecord中的sequence是与fastq一致的sequence
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
		bedRecord.setMappingWeight(getMappedReadsWeight());
		// 计数，mapping到了几次
		bedRecord.setMappingNum(getMappingNum());
		bedRecord.setName(samRecord.getReadName());
		bedRecord.setAlignmentBlocks(getAlignmentBlocks());
		return bedRecord;
	}
	
	/** 获得原始的序列，不会根据cis5to3进行反向互补操作
	 * 而mapping的结果，如果是mapping到互补链上，好象已经对reads进行了反相处理
	 */
	public SeqFasta getSeqFasta() {
		SeqFasta seqFasta = new SeqFasta(samRecord.getReadString());
		seqFasta.setName(samRecord.getReadName());
		return seqFasta;
	}
	
	/**
	 * <b>只有当soft clip的时候才会用到</b><p>
	 * 获得clip好的序列，不会根据cis5to3进行反向互补操作
	 * 而mapping的结果，如果是mapping到互补链上，好象已经对reads进行了反相处理
	 */
	public SeqFasta getSeqFastaClip() {
		return getSeqFastaClip(0);
	}
	
	/**
	 * <b>只有当soft clip的时候才会用到</b><p>
	 * 获得clip好的序列，不会根据cis5to3进行反向互补操作
	 * 而mapping的结果，如果是mapping到互补链上，好象已经对reads进行了反相处理
	 * @param ignoreNum 小于等于该长度的碱基，就忽略softclip
	 * 意思就是如果末尾是一个碱基的错配，则认为这个碱基是合适的
	 */
	public SeqFasta getSeqFastaClip(int ignoreNum) {
		SeqFasta seqFasta = new SeqFasta(samRecord.getReadString());
		int num = 0;
		int seqfastaLen = seqFasta.Length();
		int left = 0, right = seqfastaLen;
		for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {
			if (cigarElement.getOperator() == CigarOperator.SOFT_CLIP && cigarElement.getLength() > ignoreNum) {
				if (num == 0) {
					left = cigarElement.getLength();
				} else {
					right = seqFasta.Length() - cigarElement.getLength();
				}
			}
			num++;
		}
		if (left != 0 || right != seqfastaLen) {
			seqFasta = seqFasta.trimSeq(left, right);
		}
		seqFasta.setName(samRecord.getReadName());
		return seqFasta;
	}
	/**
	 * <b>只有当soft clip的时候才会用到，根{@link #getSeqFastaClip}配合使用</b><p>
	 * 获得clip好的num，意思往前后各延展几bp，向前为负数，向后为正数
	 * @param ignoreNum 小于等于该长度的碱基，就忽略softclip
	 * 意思就是如果末尾是一个碱基的错配，则认为这个碱基是合适的
	 */
	public int[] getStartEndClip(int ignoreNum) {
		int num = 0;
		int left = 0, right = 0;
		for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {
			if (cigarElement.getOperator() == CigarOperator.SOFT_CLIP && cigarElement.getLength() <= ignoreNum) {
				if (num == 0) {
					left = -cigarElement.getLength();
				} else {
					right = cigarElement.getLength();
				}
			}
			num++;
		}
		return new int[]{left, right};
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
		lsBedRecords.get(0).setMappingWeight(tmpInfo.length + 1);
		for (String string : tmpInfo) {
			BedRecord bedRecord = new BedRecord();
			String[] info = string.split(",");
			bedRecord.setRefID(info[0]);
			int start1 = Integer.parseInt(info[1].substring(1));
			int end1 = start1 + getLength() - 1;
			bedRecord.setStartEndLoc(start1, end1);
			bedRecord.setCIGAR(info[2]);
			bedRecord.setCis5to3(info[1].charAt(0));
			bedRecord.setMappingNum(getMappingNum());
			bedRecord.setMappingWeight(tmpInfo.length + 1);
			bedRecord.setMapQuality(getMapQuality());
			bedRecord.setScore(samRecord.getMappingQuality());
			bedRecord.setSeq(new SeqFasta(samRecord.getReadString()), false);
			bedRecord.setName(samRecord.getReadName());
			lsBedRecords.add(bedRecord);
		}
		return lsBedRecords;
	}
	public String getReadsQuality() {
		return samRecord.getBaseQualityString();
	}
	
	/** 双端测序的话，是否为第一条reads
	 * 单端测序恒返回true
	 * @return
	 */
	public boolean isFirstRead() {
		if (getReadPairedFlag()) {
			return samRecord.getFirstOfPairFlag();
		} else {
			return true;
		}
		
	}
	
	public boolean getReadPairedFlag() {
		return samRecord.getReadPairedFlag();
	}
	
	public int hashCode() {
		return samRecord.hashCode();
	}

	public boolean equals(Object o) {
		return samRecord.equals(o);
	}
	@Override
	public String toString() {
		return samRecord.getSAMString().trim();
	}
	@Override
	public String getRawStringInfo() {
		return samRecord.getSAMString().trim();
	}
	
	public boolean getDuplicateReadFlag() {
		return samRecord.getDuplicateReadFlag();
	}
	
	@Override
	public String getCIGAR() {
		return samRecord.getCigarString();
	}
	
	/**
	 * 结合测序双端信息，链特异性信息，来判定该reads到底是正向还是反向
	 * 会通过链特异性信息进行校正
	 * 如链特异性是正向，reads是正向，则返回true
	 * 连特异性是反向，reads是正向，则返回false，意思该reads比对到了反向基因组上
	 * @return 如果不是链特异性测序，返回null
	 */
	public Boolean isCis5to3ConsiderStrand(StrandSpecific strandSpecific) {
		return isCis5to3ConsiderStrand(strandSpecific, isFirstRead(), isCis5to3());
	}
	/**
	 * 结合测序双端信息，链特异性信息，来判定该reads到底是正向还是反向
	 * 会通过链特异性信息进行校正
	 * 如链特异性是正向，reads是正向，则返回true
	 * 连特异性是反向，reads是正向，则返回false，意思该reads比对到了反向基因组上
	 * @return 如果不是链特异性测序，返回null
	 */
	protected static Boolean isCis5to3ConsiderStrand(StrandSpecific strandSpecific, boolean isFirstRead, boolean isCis5to3) {
		if (strandSpecific == null) {
			throw new ExceptionNullParam("No Param StrandSpecific");
		} else if (strandSpecific == StrandSpecific.NONE) {
			return null;
		}
		boolean cis5to3 = true;
		boolean readsStrand = (!isFirstRead ^ isCis5to3) ? true : false;
		if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			cis5to3 = readsStrand;
		} else if (strandSpecific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			cis5to3 = !readsStrand;
		} else {
			throw new ExceptionSamError("Find No StrandSpecific Type: " + strandSpecific.toString());
		}
		return cis5to3;
	}
	/**
	 * 返回第一个记载的bedrecord 没有mapping上就返回null
	 * */
	public FastQRecord toFastQRecord() {
		FastQRecord fastQRecord = new FastQRecord();
		fastQRecord.setName(getName());
		if (!isCis5to3()) {
			fastQRecord.setSeq(SeqFasta.reverseComplement(samRecord.getReadString()));
			fastQRecord.setFastaQuality(SeqFasta.reverse(samRecord.getBaseQualityString()));
		} else {
			fastQRecord.setSeq(samRecord.getReadString());
			fastQRecord.setFastaQuality(samRecord.getBaseQualityString());
		}
		
		return fastQRecord;
	}
//	/** 返回名字和左端序列的起点信息 */
//	public String getNameAndFirstSite() {
//		return isFirstRead()? getName() + getRefID() + getStartAbs() : getName() + getMateRefID() + getMateAlignmentStart();
//	}

	@Override
	public int getStartAbs() {
		return samRecord.getAlignmentStart();
	}
	
	@Override
	public int getEndAbs() {
		return samRecord.getAlignmentEnd();
	}

	@Override
	public int getStartCis() {
		if (isCis5to3()) {
			return samRecord.getAlignmentStart();
		} else {
			return samRecord.getAlignmentEnd();
		}
	}

	@Override
	public int getEndCis() {
		if (isCis5to3()) {
			return samRecord.getAlignmentEnd();
		} else {
			return samRecord.getAlignmentStart();
		}
	}

	@Override
	public Boolean isCis5to3() {
		return !samRecord.getReadNegativeStrandFlag();
	}

	/** 比对上的长度 */
	@Override
	public int getLength() {
		return Math.abs(samRecord.getAlignmentEnd() - samRecord.getAlignmentStart()) + 1;
	}
	/** reads的实际长度，如果是hard clip，就算clip之后的长度 */
	public int getLengthReal() {
		return getSeqFasta().Length();
	}
	
	@Override
	public String getRefID() {
		return samRecord.getReferenceName();
	}

	@Override
	public String getName() {
		return samRecord.getReadName();
	}

}
