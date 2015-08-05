package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import picard.PicardException;

import com.novelbio.base.fileOperate.FileOperate;

/** 给定染色体顺序，将SamHeader的顺序调整为指定的顺序
 * 有两个步骤
 * 1. reorder调整header的顺序(一个bam文件只需要处理一个头文件)
 * 2, 将具体的SamRecord进行处理(bam文件的每条序列都要处理一次)
 * @author zong0jie
 *
 */
public class SamReorder {
	private static final Logger logger = Logger.getLogger(SamReorder.class);
	
	SAMFileHeader samFileHeaderInput;
	SAMFileHeader samFileHeaderNew;
	SAMSequenceDictionary chrDictNew;
	
	/** 老的chrId和新的chrId的对照表，如chr1：1，chr2：2 */
	Map<String, String> mapChrIdOld2New;
	
	Map<Integer, Integer> mapNewOrder;
	
	/** 给定需要调整的samHeader */
	public void setSamFileHeader(SAMFileHeader samFileHeader) {
		this.samFileHeaderInput = samFileHeader;
	}
	/** 给定顺序的chrId，无所谓大小写 */
	public void setSamSequenceDictionary(SAMSequenceDictionary samSequenceDictionary) {
		this.chrDictNew = samSequenceDictionary;
	}
	/** 将旧的chrId换成新的chrId，如将 chr1 换为 1 */
	public void setMapChrIdOld2New(Map<String, String> mapChrIdOld2New) {
		this.mapChrIdOld2New = mapChrIdOld2New;
	}
	/** 返回一个新的header文件 */
	public void reorder() {
		SAMSequenceDictionary chrDictOld = samFileHeaderInput.getSequenceDictionary();
		if (mapChrIdOld2New != null && !mapChrIdOld2New.isEmpty()) {
			chrDictOld = changeChrId(chrDictOld);
		}
		if (chrDictNew == null) {
			chrDictNew = chrDictOld;
		} else {
			mapNewOrder = buildSequenceDictionaryMap(chrDictNew, chrDictOld);
		}
		samFileHeaderNew = samFileHeaderInput.clone();
		samFileHeaderNew.setSequenceDictionary(chrDictNew);
	}
	/** 返回排序正确的头文件，根输入的header不是同一个 */
	public SAMFileHeader getSamFileHeaderNew() {
		return samFileHeaderNew;
	}
	
	private SAMSequenceDictionary changeChrId(SAMSequenceDictionary chrDictOld) {
		SAMSequenceDictionary samSequenceDictionary = new SAMSequenceDictionary();
		for (SAMSequenceRecord record : chrDictOld.getSequences()) {
			String chrIdOld = record.getSequenceName();
			if (mapChrIdOld2New.containsKey(chrIdOld)) {
				record = new SAMSequenceRecord(mapChrIdOld2New.get(chrIdOld), record.getSequenceLength());
			}
			samSequenceDictionary.addSequence(record);
		}
		return samSequenceDictionary;
	}
	
    /**
     * Constructs a mapping from read sequence records index -> new sequence dictionary index for use in
     * reordering the reference index and mate reference index in each read.  -1 means unmapped.
     */
    private Map<Integer, Integer> buildSequenceDictionaryMap(final SAMSequenceDictionary chrDictNew, final SAMSequenceDictionary chrDictOld) {
        Map<Integer, Integer> newOrder = new HashMap<Integer, Integer>();

        logger.info("Reordering SAM/BAM file:");
        for (final SAMSequenceRecord rerRecNew : chrDictNew.getSequences() ) {
            final SAMSequenceRecord refRecordOld = chrDictOld.getSequence(rerRecNew.getSequenceName());

            if (refRecordOld != null) {
                if ( rerRecNew.getSequenceLength() != refRecordOld.getSequenceLength() ) {
                    String msg = String.format("Discordant contig lengths: read %s LN=%d, ref %s LN=%d",
                            refRecordOld.getSequenceName(), refRecordOld.getSequenceLength(),
                            rerRecNew.getSequenceName(), rerRecNew.getSequenceLength());
                    throw new PicardException(msg);
                }
                logger.info(String.format("  Reordering read contig %s [index=%d] to => ref contig %s [index=%d]%n",
                                       refRecordOld.getSequenceName(), refRecordOld.getSequenceIndex(),
                                       rerRecNew.getSequenceName(), rerRecNew.getSequenceIndex()  ));
                newOrder.put(refRecordOld.getSequenceIndex(), rerRecNew.getSequenceIndex());
            }
        }

        for ( SAMSequenceRecord readsRec : chrDictOld.getSequences() ) {
            if ( ! newOrder.containsKey(readsRec.getSequenceIndex()) ) {
            	throw new PicardException("New reference sequence does not contain a matching contig for " + readsRec.getSequenceName());
            }
        }

        return newOrder;
    }
    
    /** 前面都设定好后，最后再用这个来转化sam文件 */
	public SamFile reorderSam(SamFile samFile) {
		String fileIn = samFile.getFileName();
		String fileOut = FileOperate.changeFileSuffix(fileIn, "_reorder", null);
		if (FileOperate.isFileExistAndBigThanSize(fileOut, 0)) {
			return new SamFile(fileOut);
		}
		String fileOutTmp = FileOperate.changeFileSuffix(fileOut, "_tmp", null);
		boolean isIndexedInput = samFile.isIndexed();

		SamFile samFileOut = new SamFile(fileOutTmp, getSamFileHeaderNew());
		if (isIndexedInput) {
			for (final SAMSequenceRecord contig : chrDictNew.getSequences()) {
				for (SamRecord samRecord : samFile.readLinesOverlap(contig.getSequenceName(), 0, 0)) {
					copeReads(samRecord);
					samFileOut.writeSamRecord(samRecord);
				}
			}
		} else {
			for (SamRecord samRecord : samFile.readLines()) {
				copeReads(samRecord);
				samFileOut.writeSamRecord(samRecord);
			}
		}
		samFile.close();
		samFileOut.close();
		FileOperate.moveFile(true, fileOutTmp, fileOut);
		SamFile samFileFinal = new SamFile(fileOut);
		if (isIndexedInput) {
			samFileFinal.indexMake();
			samFileFinal.close();
		}
		return samFileFinal;
	}
    
    /** 通过这项处理后，samRecord写入结果文件才不会出错 */
    public void copeReads(SamRecord samRecord) {
        final SAMRecord read = samRecord.getSamRecord();
        read.setHeader(samFileHeaderNew);
        
        int oldRefIndex = read.getReferenceIndex();
        int oldMateIndex = read.getMateReferenceIndex();
        int newRefIndex = newOrderIndex(read, oldRefIndex, mapNewOrder);

        read.setReferenceIndex(newRefIndex);

        int newMateIndex = newOrderIndex(read, oldMateIndex, mapNewOrder);
        if ( oldMateIndex != -1 && newMateIndex == -1 ) { // becoming unmapped
            read.setMateAlignmentStart(0);
            read.setMateUnmappedFlag(true);
        }
        read.setMateReferenceIndex(newMateIndex);
    }
    
    /**
     * Low-level helper function that returns the new reference index for oldIndex according to the
     * ordering map newOrder.  Read is provided in case an error occurs, so that an informative message
     * can be made.
     */
    private int newOrderIndex(SAMRecord read, int oldIndex, Map<Integer, Integer> newOrder) {
        if (mapNewOrder == null) {
			return oldIndex;
		}
        
        if ( oldIndex == -1 )
            return -1; // unmapped read
        else {
            final Integer n = newOrder.get(oldIndex);

            if (n == null) throw new PicardException("BUG: no mapping found for read " + read.format());
            else return n;
        }
    }
    
	/** 序列顺序是否一致，注意，只要d1或d2有一个出现null，就认为一致，因为没有比的必要了 */
    public static boolean isConsistant(SAMSequenceDictionary d1, SAMSequenceDictionary d2) {
		if (d1 == null || d2 == null) return true;	
		
		if (d1.size() != d2.size()) return false;
		
		for (SAMSequenceRecord samSequenceRecord : d1.getSequences()) {
			SAMSequenceRecord record = d2.getSequence(samSequenceRecord.getSequenceName());
			if (record == null) {
				return false;
			}
			if (samSequenceRecord.getSequenceIndex() != record.getSequenceIndex()) {
				return false;
			}
		}
		return true;
	}
}
