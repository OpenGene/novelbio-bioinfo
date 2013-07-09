/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.utils.sam;

import com.google.java.contract.Ensures;
import net.sf.samtools.*;
import org.broadinstitute.sting.utils.NGSPlatform;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.recalibration.EventType;

import java.util.*;

/**
 * @author ebanks, depristo
 * GATKSAMRecord
 *
 * this class extends the samtools BAMRecord class (and SAMRecord) and caches important
 * (and oft-accessed) data that's not already cached by the SAMRecord class
 *
 * IMPORTANT NOTE: Because ReadGroups are not set through the SAMRecord,
 *   if they are ever modified externally then one must also invoke the
 *   setReadGroup() method here to ensure that the cache is kept up-to-date.
 *
 * WARNING -- GATKSAMRecords cache several values (that are expensive to compute)
 * that depending on the inferred insert size and alignment starts and stops of this read and its mate.
 * Changing these values in any way will invalidate the cached value. However, we do not monitor those setter
 * functions, so modifying a GATKSAMRecord in any way may result in stale cached values.
 */
public class GATKSAMRecord extends BAMRecord {
    // ReduceReads specific attribute tags
    public static final String REDUCED_READ_CONSENSUS_TAG = "RR";                   // marks a synthetic read produced by the ReduceReads tool
    public static final String REDUCED_READ_ORIGINAL_ALIGNMENT_START_SHIFT = "OP";  // reads that are clipped may use this attribute to keep track of their original alignment start
    public static final String REDUCED_READ_ORIGINAL_ALIGNMENT_END_SHIFT = "OE";    // reads that are clipped may use this attribute to keep track of their original alignment end

    // Base Quality Score Recalibrator specific attribute tags
    public static final String BQSR_BASE_INSERTION_QUALITIES = "BI";                // base qualities for insertions
    public static final String BQSR_BASE_DELETION_QUALITIES = "BD";                 // base qualities for deletions

    /**
     * The default quality score for an insertion or deletion, if
     * none are provided for this read.
     */
    public static final byte DEFAULT_INSERTION_DELETION_QUAL = (byte)45;

    // the SAMRecord data we're caching
    private String mReadString = null;
    private GATKSAMReadGroupRecord mReadGroup = null;
    private byte[] reducedReadCounts = null;
    private final static int UNINITIALIZED = -1;
    private int softStart = UNINITIALIZED;
    private int softEnd = UNINITIALIZED;
    private Integer adapterBoundary = null;

    // because some values can be null, we don't want to duplicate effort
    private boolean retrievedReadGroup = false;
    private boolean retrievedReduceReadCounts = false;

    // These temporary attributes were added here to make life easier for
    // certain algorithms by providing a way to label or attach arbitrary data to
    // individual GATKSAMRecords.
    // These attributes exist in memory only, and are never written to disk.
    private Map<Object, Object> temporaryAttributes;

    /**
     * HACK TO CREATE GATKSAMRECORD WITH ONLY A HEADER FOR TESTING PURPOSES ONLY
     * @param header
     */
    public GATKSAMRecord(final SAMFileHeader header) {
        this(new SAMRecord(header));
    }

    /**
     * HACK TO CREATE GATKSAMRECORD BASED ONLY A SAMRECORD FOR TESTING PURPOSES ONLY
     * @param read
     */
    public GATKSAMRecord(final SAMRecord read) {
        super(read.getHeader(), read.getMateReferenceIndex(),
                read.getAlignmentStart(),
                read.getReadName() != null ? (short)read.getReadNameLength() : 0,
                (short)read.getMappingQuality(),
                0,
                read.getCigarLength(),
                read.getFlags(),
                read.getReadLength(),
                read.getMateReferenceIndex(),
                read.getMateAlignmentStart(),
                read.getInferredInsertSize(),
                null);
        SAMReadGroupRecord samRG = read.getReadGroup();
        clearAttributes();
        if (samRG != null) {
            GATKSAMReadGroupRecord rg = new GATKSAMReadGroupRecord(samRG);
            setReadGroup(rg);
        }
    }

    public GATKSAMRecord(final SAMFileHeader header,
                         final int referenceSequenceIndex,
                         final int alignmentStart,
                         final short readNameLength,
                         final short mappingQuality,
                         final int indexingBin,
                         final int cigarLen,
                         final int flags,
                         final int readLen,
                         final int mateReferenceSequenceIndex,
                         final int mateAlignmentStart,
                         final int insertSize,
                         final byte[] variableLengthBlock) {
        super(header, referenceSequenceIndex, alignmentStart, readNameLength, mappingQuality, indexingBin, cigarLen,
                flags, readLen, mateReferenceSequenceIndex, mateAlignmentStart, insertSize, variableLengthBlock);
    }

    public static GATKSAMRecord createRandomRead(int length) {
        List<CigarElement> cigarElements = new LinkedList<CigarElement>();
        cigarElements.add(new CigarElement(length, CigarOperator.M));
        Cigar cigar = new Cigar(cigarElements);
        return ArtificialSAMUtils.createArtificialRead(cigar);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // *** The following methods are overloaded to cache the appropriate data ***//
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public String getReadString() {
        if ( mReadString == null )
            mReadString = super.getReadString();
        return mReadString;
    }

    @Override
    public void setReadString(String s) {
        super.setReadString(s);
        mReadString = s;
    }

    /**
     * Get the GATKSAMReadGroupRecord of this read
     * @return a non-null GATKSAMReadGroupRecord
     */
    @Override
    public GATKSAMReadGroupRecord getReadGroup() {
        if ( ! retrievedReadGroup ) {
            final SAMReadGroupRecord rg = super.getReadGroup();

            // three cases: rg may be null (no rg, rg may already be a GATKSAMReadGroupRecord, or it may be
            // a regular SAMReadGroupRecord in which case we have to make it a GATKSAMReadGroupRecord
            if ( rg == null )
                mReadGroup = null;
            else if ( rg instanceof GATKSAMReadGroupRecord )
                mReadGroup = (GATKSAMReadGroupRecord)rg;
            else
                mReadGroup = new GATKSAMReadGroupRecord(rg);

            retrievedReadGroup = true;
        }
        return mReadGroup;
    }

    public void setReadGroup( final GATKSAMReadGroupRecord readGroup ) {
        mReadGroup = readGroup;
        retrievedReadGroup = true;
        setAttribute("RG", mReadGroup.getId()); // todo -- this should be standardized, but we don't have access to SAMTagUtils!
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof GATKSAMRecord)) return false;

        // note that we do not consider the GATKSAMRecord internal state at all
        return super.equals(o);
    }

    /**
     * Setters and Accessors for base insertion and base deletion quality scores
     */
    public void setBaseQualities( final byte[] quals, final EventType errorModel ) {
        switch( errorModel ) {
            case BASE_SUBSTITUTION:
                setBaseQualities(quals);
                break;
            case BASE_INSERTION:
                setAttribute( GATKSAMRecord.BQSR_BASE_INSERTION_QUALITIES, quals == null ? null : SAMUtils.phredToFastq(quals) );
                break;
            case BASE_DELETION:
                setAttribute( GATKSAMRecord.BQSR_BASE_DELETION_QUALITIES, quals == null ? null : SAMUtils.phredToFastq(quals) );
                break;
            default:
                throw new ReviewedStingException("Unrecognized Base Recalibration type: " + errorModel );
        }
    }

    public byte[] getBaseQualities( final EventType errorModel ) {
        switch( errorModel ) {
            case BASE_SUBSTITUTION:
                return getBaseQualities();
            case BASE_INSERTION:
                return getBaseInsertionQualities();
            case BASE_DELETION:
                return getBaseDeletionQualities();
            default:
                throw new ReviewedStingException("Unrecognized Base Recalibration type: " + errorModel );
        }
    }

    /**
     * @return whether or not this read has base insertion or deletion qualities (one of the two is sufficient to return true)
     */
    public boolean hasBaseIndelQualities() {
        return getAttribute( BQSR_BASE_INSERTION_QUALITIES ) != null || getAttribute( BQSR_BASE_DELETION_QUALITIES ) != null;
    }

    /**
     * @return the base deletion quality or null if read doesn't have one
     */
    public byte[] getExistingBaseInsertionQualities() {
        return SAMUtils.fastqToPhred( getStringAttribute(BQSR_BASE_INSERTION_QUALITIES));
    }

    /**
     * @return the base deletion quality or null if read doesn't have one
     */
    public byte[] getExistingBaseDeletionQualities() {
        return SAMUtils.fastqToPhred( getStringAttribute(BQSR_BASE_DELETION_QUALITIES));
    }

    /**
     * Default utility to query the base insertion quality of a read. If the read doesn't have one, it creates an array of default qualities (currently Q45)
     * and assigns it to the read.
     *
     * @return the base insertion quality array
     */
    public byte[] getBaseInsertionQualities() {
        byte [] quals = getExistingBaseInsertionQualities();
        if( quals == null ) {
            quals = new byte[getBaseQualities().length];
            Arrays.fill(quals, DEFAULT_INSERTION_DELETION_QUAL); // Some day in the future when base insertion and base deletion quals exist the samtools API will
                                           // be updated and the original quals will be pulled here, but for now we assume the original quality is a flat Q45
        }
        return quals;
    }

    /**
     * Default utility to query the base deletion quality of a read. If the read doesn't have one, it creates an array of default qualities (currently Q45)
     * and assigns it to the read.
     *
     * @return the base deletion quality array
     */
    public byte[] getBaseDeletionQualities() {
        byte[] quals = getExistingBaseDeletionQualities();
        if( quals == null ) {
            quals = new byte[getBaseQualities().length];
            Arrays.fill(quals, DEFAULT_INSERTION_DELETION_QUAL);  // Some day in the future when base insertion and base deletion quals exist the samtools API will
                                            // be updated and the original quals will be pulled here, but for now we assume the original quality is a flat Q45
        }
        return quals;
    }

    /**
     * Efficient caching accessor that returns the GATK NGSPlatform of this read
     * @return
     */
    public NGSPlatform getNGSPlatform() {
        return getReadGroup().getNGSPlatform();
    }

    ///////////////////////////////////////////////////////////////////////////////
    // *** ReduceReads functions                                              ***//
    ///////////////////////////////////////////////////////////////////////////////

    public byte[] getReducedReadCounts() {
        if ( ! retrievedReduceReadCounts ) {
            reducedReadCounts = getByteArrayAttribute(REDUCED_READ_CONSENSUS_TAG);
            retrievedReduceReadCounts = true;
        }

        return reducedReadCounts;
    }

    public boolean isReducedRead() {
        return getReducedReadCounts() != null;
    }

    /**
     * The number of bases corresponding the i'th base of the reduced read.
     *
     * @param i the read based coordinate inside the read
     * @return the number of bases corresponding to the i'th base of the reduced read
     */
    public final byte getReducedCount(final int i) {
        byte firstCount = getReducedReadCounts()[0];
        byte offsetCount = getReducedReadCounts()[i];
        return (i==0) ? firstCount : (byte) Math.min(firstCount + offsetCount, Byte.MAX_VALUE);
    }

    ///////////////////////////////////////////////////////////////////////////////
    // *** GATKSAMRecord specific methods                                     ***//
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Checks whether an attribute has been set for the given key.
     *
     * Temporary attributes provide a way to label or attach arbitrary data to
     * individual GATKSAMRecords. These attributes exist in memory only,
     * and are never written to disk.
     *
     * @param key key
     * @return True if an attribute has been set for this key.
     */
    public boolean containsTemporaryAttribute(Object key) {
        if(temporaryAttributes != null) {
            return temporaryAttributes.containsKey(key);
        }
        return false;
    }

    /**
     * Sets the key to the given value, replacing any previous value. The previous
     * value is returned.
     *
     * Temporary attributes provide a way to label or attach arbitrary data to
     * individual GATKSAMRecords. These attributes exist in memory only,
     * and are never written to disk.
     *
     * @param key    key
     * @param value  value
     * @return attribute
     */
    public Object setTemporaryAttribute(Object key, Object value) {
        if(temporaryAttributes == null) {
            temporaryAttributes = new HashMap<Object, Object>();
        }
        return temporaryAttributes.put(key, value);
    }

    /**
     * Looks up the value associated with the given key.
     *
     * Temporary attributes provide a way to label or attach arbitrary data to
     * individual GATKSAMRecords. These attributes exist in memory only,
     * and are never written to disk.
     *
     * @param key key
     * @return The value, or null.
     */
    public Object getTemporaryAttribute(Object key) {
        if(temporaryAttributes != null) {
            return temporaryAttributes.get(key);
        }
        return null;
    }

    /**
     * Checks whether if the read has any bases.
     *
     * Empty reads can be dangerous as it may have no cigar strings, no read names and
     * other missing attributes.
     *
     * @return true if the read has no bases
     */
    public boolean isEmpty() {
        return super.getReadBases() == null || super.getReadLength() == 0;
    }

    /**
     * Clears all attributes except ReadGroup of the read.
     */
    public GATKSAMRecord simplify () {
        GATKSAMReadGroupRecord rg = getReadGroup(); // save the read group information
        byte[] insQuals = (this.getAttribute(BQSR_BASE_INSERTION_QUALITIES) == null) ? null : getBaseInsertionQualities();
        byte[] delQuals = (this.getAttribute(BQSR_BASE_DELETION_QUALITIES)  == null) ? null : getBaseDeletionQualities();
        this.clearAttributes(); // clear all attributes from the read
        this.setReadGroup(rg); // restore read group
        if (insQuals != null)
           this.setBaseQualities(insQuals, EventType.BASE_INSERTION); // restore base insertion if we had any
        if (delQuals != null)
            this.setBaseQualities(delQuals, EventType.BASE_DELETION); // restore base deletion if we had any
        return this;
    }

    /**
     * Calculates the reference coordinate for the beginning of the read taking into account soft clips but not hard clips.
     *
     * Note: getUnclippedStart() adds soft and hard clips, this function only adds soft clips.
     *
     * @return the unclipped start of the read taking soft clips (but not hard clips) into account
     */
    public int getSoftStart() {
        if ( softStart == UNINITIALIZED ) {
            softStart = getAlignmentStart();
            for (final CigarElement cig : getCigar().getCigarElements()) {
                final CigarOperator op = cig.getOperator();

                if (op == CigarOperator.SOFT_CLIP)
                    softStart -= cig.getLength();
                else if (op != CigarOperator.HARD_CLIP)
                    break;
            }
        }
        return softStart;
    }

    /**
     * Calculates the reference coordinate for the end of the read taking into account soft clips but not hard clips.
     *
     * Note: getUnclippedEnd() adds soft and hard clips, this function only adds soft clips.
     *
     * @return the unclipped end of the read taking soft clips (but not hard clips) into account
     */
    public int getSoftEnd() {
        if ( softEnd == UNINITIALIZED ) {
            boolean foundAlignedBase = false;
            softEnd = getAlignmentEnd();
            final List<CigarElement> cigs = getCigar().getCigarElements();
            for (int i = cigs.size() - 1; i >= 0; --i) {
                final CigarElement cig = cigs.get(i);
                final CigarOperator op = cig.getOperator();

                if (op == CigarOperator.SOFT_CLIP) // assumes the soft clip that we found is at the end of the aligned read
                    softEnd += cig.getLength();
                else if (op != CigarOperator.HARD_CLIP) {
                    foundAlignedBase = true;
                    break;
                }
            }
            if( !foundAlignedBase ) { // for example 64H14S, the soft end is actually the same as the alignment end
                softEnd = getAlignmentEnd();
            }
        }

        return softEnd;
    }

    /**
     * If the read is hard clipped, the soft start and end will change. You can set manually or just reset the cache
     * so that the next call to getSoftStart/End will recalculate it lazily.
     */
    public void resetSoftStartAndEnd() {
        softStart = -1;
        softEnd = -1;
    }

    /**
     * If the read is hard clipped, the soft start and end will change. You can set manually or just reset the cache
     * so that the next call to getSoftStart/End will recalculate it lazily.
     */
    public void resetSoftStartAndEnd(int softStart, int softEnd) {
        this.softStart = softStart;
        this.softEnd = softEnd;
    }

    /**
     * Determines the original alignment start of a previously clipped read.
     * 
     * This is useful for reads that have been trimmed to a variant region and lost the information of it's original alignment end
     * 
     * @return the alignment start of a read before it was clipped
     */
    public int getOriginalAlignmentStart() {
        int originalAlignmentStart = getUnclippedStart();
        Integer alignmentShift = (Integer) getAttribute(REDUCED_READ_ORIGINAL_ALIGNMENT_START_SHIFT);
        if (alignmentShift != null)
            originalAlignmentStart += alignmentShift;
        return originalAlignmentStart;    
    }

    /**
     * Determines the original alignment end of a previously clipped read.
     *
     * This is useful for reads that have been trimmed to a variant region and lost the information of it's original alignment end
     * 
     * @return the alignment end of a read before it was clipped
     */
    public int getOriginalAlignmentEnd() {
        int originalAlignmentEnd = getUnclippedEnd();
        Integer alignmentShift = (Integer) getAttribute(REDUCED_READ_ORIGINAL_ALIGNMENT_END_SHIFT);
        if (alignmentShift != null)
            originalAlignmentEnd -= alignmentShift;
        return originalAlignmentEnd;
    }

    /**
     * Creates an empty GATKSAMRecord with the read's header, read group and mate
     * information, but empty (not-null) fields:
     *  - Cigar String
     *  - Read Bases
     *  - Base Qualities
     *
     * Use this method if you want to create a new empty GATKSAMRecord based on
     * another GATKSAMRecord
     *
     * @param read a read to copy the header from
     * @return a read with no bases but safe for the GATK
     */
    public static GATKSAMRecord emptyRead(GATKSAMRecord read) {
        GATKSAMRecord emptyRead = new GATKSAMRecord(read.getHeader(),
                read.getReferenceIndex(),
                0,
                (short) 0,
                (short) 0,
                0,
                0,
                read.getFlags(),
                0,
                read.getMateReferenceIndex(),
                read.getMateAlignmentStart(),
                read.getInferredInsertSize(),
                null);

        emptyRead.setCigarString("");
        emptyRead.setReadBases(new byte[0]);
        emptyRead.setBaseQualities(new byte[0]);

        SAMReadGroupRecord samRG = read.getReadGroup();
        emptyRead.clearAttributes();
        if (samRG != null) {
            GATKSAMReadGroupRecord rg = new GATKSAMReadGroupRecord(samRG);
            emptyRead.setReadGroup(rg);
        }

        return emptyRead;
    }

    /**
     * Shallow copy of everything, except for the attribute list and the temporary attributes. 
     * A new list of the attributes is created for both, but the attributes themselves are copied by reference.  
     * This should be safe because callers should never modify a mutable value returned by any of the get() methods anyway.
     * 
     * @return a shallow copy of the GATKSAMRecord
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final GATKSAMRecord clone = (GATKSAMRecord) super.clone();
        if (temporaryAttributes != null) {
            clone.temporaryAttributes = new HashMap<Object, Object>();
            for (Object attribute : temporaryAttributes.keySet())
                clone.setTemporaryAttribute(attribute, temporaryAttributes.get(attribute));
        }
        return clone;
    }

    /**
     * A caching version of ReadUtils.getAdaptorBoundary()
     *
     * @see ReadUtils.getAdaptorBoundary(SAMRecord) for more information about the meaning of this function
     *
     * WARNING -- this function caches a value depending on the inferred insert size and alignment starts
     * and stops of this read and its mate.  Changing these values in any way will invalidate the cached value.
     * However, we do not monitor those setter functions, so modifying a GATKSAMRecord in any way may
     * result in stale cached values.
     *
     * @return the result of calling ReadUtils.getAdaptorBoundary on this read
     */
    @Ensures("result == ReadUtils.getAdaptorBoundary(this)")
    public int getAdaptorBoundary() {
        if ( adapterBoundary == null )
            adapterBoundary = ReadUtils.getAdaptorBoundary(this);
        return adapterBoundary;
    }
}
