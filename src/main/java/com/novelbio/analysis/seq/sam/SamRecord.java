package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.Cigar;
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
	protected SAMRecord getSamRecord() {
		return samRecord;
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
		if (samRecord.getCigar().toString().contains("N")) {
			return true;
		} else {
			isJunctionReads = false;
		}
		return isJunctionReads;
	}
	/** ��Ϊjunction reads��ʱ��Ż������� */
	public ArrayList<Align> getAlignmentBlocks() {
		if (samRecord.getCigar().toString().contains("N")) {
			List<AlignmentBlock> lsAlignmentBlocks = samRecord.getAlignmentBlocks();
			ArrayList<Align> lsAligns = new ArrayList<Align>();
			for (AlignmentBlock alignmentBlock : lsAlignmentBlocks) {
				Align align = new Align(alignmentBlock.getReferenceStart(), alignmentBlock.getLength() + alignmentBlock.getReferenceStart() - 1);
				align.setCis5to3(isCis5to3());
				lsAligns.add(align);
			}
			return lsAligns;
		}
		ArrayList<Align> lsAligns = new ArrayList<Align>();
		Align align = new Align(getStartAbs(), getEndAbs());
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
				if (getNumMappedReads() == 1) {
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

	public int getMapQuality() {
		return samRecord.getMappingQuality();
	}
	/**
	 * reads��Ȩ�أ���˼��ͬ��reads�ڱ�sam�ļ��г����˼���
	 * bwa�Ľ����һ��readsֻ��һ�У����Ժ㷵��1
	 * tophat�Ľ����һ��reads���mapping�����λ�ã����ļ��оͻ���ֶ�Σ����Է��ؿ��ܴ���1
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
	 * �����п���mapping��������ͬλ��
	 * */
	public int getNumMappedReads() {
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
	 * �Ƿ�Ϊ˫�˻���˵����һ�� ����null����ʾ��֪��������û����һ�ˣ���ô��Ҫ����������ļ������ж�
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

	/** ˫�˵���һ���Ƿ�mapping���ˣ�����Ҳ����false */
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
	/** ����һ��reads�����Ƿ�Ϊ��ɶԵ�reads */
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
				logger.error("δ֪���");
				return false;
			}
		} else {
			return false;
		}
	}
	/**
	 * ���ص�һ�����ص�bedrecord û��mapping�Ͼͷ���null
	 * */
	public FastQRecord toFastQRecord() {
		FastQRecord fastQRecord = new FastQRecord();
		fastQRecord.setFastaQuality(samRecord.getBaseQualityString());
		fastQRecord.setName(getName());
		fastQRecord.setSeq(samRecord.getReadString());
		return fastQRecord;
	}
	/**
	 * ���ص�һ�����ص�bedrecord û��mapping�Ͼͷ���null
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
		// ������mapping���˼���
		bedRecord.setMappingNum(getNumMappedReads());
		bedRecord.setName(samRecord.getReadName());
		bedRecord.setAlignmentBlocks(getAlignmentBlocks());
		return bedRecord;
	}

	/** �������ؾ���ÿ��SamRecord����һϵ��BedRecord */
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
			bedRecord.setMappingNum(getNumMappedReads());
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

}
