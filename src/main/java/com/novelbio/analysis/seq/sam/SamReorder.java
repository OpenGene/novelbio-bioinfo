package com.novelbio.analysis.seq.sam;

import java.util.HashMap;
import java.util.Map;

import net.sf.picard.PicardException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import org.apache.log4j.Logger;

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
	SAMSequenceDictionary samSequenceDictionary;
	Map<Integer, Integer> mapNewOrder;
	
	/** 给定需要调整的samHeader */
	public void setSamFileHeader(SAMFileHeader samFileHeader) {
		this.samFileHeaderInput = samFileHeader;
	}
	/** 给定顺序的chrId，无所谓大小写 */
	public void setSamSequenceDictionary(SAMSequenceDictionary samSequenceDictionary) {
		this.samSequenceDictionary = samSequenceDictionary;
	}
	
	/** 返回一个新的header文件 */
	public void reorder() {
		SAMSequenceDictionary dictionary = samFileHeaderInput.getSequenceDictionary();
		mapNewOrder = buildSequenceDictionaryMap(samSequenceDictionary, dictionary);
		samFileHeaderNew = samFileHeaderInput.clone();
		samFileHeaderNew.setSequenceDictionary(samSequenceDictionary);
	}
	/** 返回排序正确的头文件，根输入的header不是同一个 */
	public SAMFileHeader getSamFileHeaderNew() {
		return samFileHeaderNew;
	}
    /**
     * Constructs a mapping from read sequence records index -> new sequence dictionary index for use in
     * reordering the reference index and mate reference index in each read.  -1 means unmapped.
     */
    private Map<Integer, Integer> buildSequenceDictionaryMap(final SAMSequenceDictionary refDict, final SAMSequenceDictionary readsDict) {
        Map<Integer, Integer> newOrder = new HashMap<Integer, Integer>();

        logger.info("Reordering SAM/BAM file:");
        for (final SAMSequenceRecord refRec : refDict.getSequences() ) {
            final SAMSequenceRecord readsRec = readsDict.getSequence(refRec.getSequenceName());

            if (readsRec != null) {
                if ( refRec.getSequenceLength() != readsRec.getSequenceLength() ) {
                    String msg = String.format("Discordant contig lengths: read %s LN=%d, ref %s LN=%d",
                            readsRec.getSequenceName(), readsRec.getSequenceLength(),
                            refRec.getSequenceName(), refRec.getSequenceLength());
                    throw new PicardException(msg);
                }
                logger.info(String.format("  Reordering read contig %s [index=%d] to => ref contig %s [index=%d]%n",
                                       readsRec.getSequenceName(), readsRec.getSequenceIndex(),
                                       refRec.getSequenceName(), refRec.getSequenceIndex()  ));
                newOrder.put(readsRec.getSequenceIndex(), refRec.getSequenceIndex());
            }
        }

        for ( SAMSequenceRecord readsRec : readsDict.getSequences() ) {
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
    	String fileOutTmp = FileOperate.changeFileSuffix(fileOut, "_tmp", null);
    	boolean isIndexedInput = samFile.isIndexed();
    	
    	SamFile samFileOut = new SamFile(fileOutTmp, getSamFileHeaderNew());
    	if (isIndexedInput) {
            for (final SAMSequenceRecord contig : samSequenceDictionary.getSequences() ) {
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
        int oldRefIndex = read.getReferenceIndex();
        int oldMateIndex = read.getMateReferenceIndex();
        int newRefIndex = newOrderIndex(read, oldRefIndex, mapNewOrder);

        read.setHeader(samFileHeaderNew);
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
