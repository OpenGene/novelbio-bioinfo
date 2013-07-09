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

package org.broadinstitute.sting.utils.activeregion;

import com.google.java.contract.Ensures;
import com.google.java.contract.Invariant;
import net.sf.picard.reference.IndexedFastaSequenceFile;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.GenomeLocParser;
import org.broadinstitute.sting.utils.GenomeLocSortedSet;
import org.broadinstitute.sting.utils.HasGenomeLocation;
import org.broadinstitute.sting.utils.clipping.ReadClipper;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
import org.broadinstitute.sting.utils.sam.ReadUtils;

import java.util.*;

/**
 * Represents a single active region created by the Active Region Traversal for processing
 *
 * An active region is a single contiguous span of bases on the genome that should be operated
 * on as a single unit for the active region traversal.  The action may contains a list of
 * reads that overlap the region (may because there may be no reads in the region).  The region
 * is tagged as being either active or inactive, depending on the probabilities provided by
 * the isActiveProb results from the ART walker.  Each region carries with it the
 * exact span of the region (bases which are the core of the isActiveProbs from the walker) as
 * well as an extended size, that includes the ART walker's extension size.  Reads in the region
 * provided by ART include all reads overlapping the extended span, not the raw span.
 *
 * User: rpoplin
 * Date: 1/4/12
 */
@Invariant({
        "extension >= 0",
        "activeRegionLoc != null",
        "genomeLocParser != null",
        "spanIncludingReads != null",
        "extendedLoc != null"
})
public class ActiveRegion implements HasGenomeLocation {
    /**
     * The reads included in this active region.  May be empty upon creation, and expand / contract
     * as reads are added or removed from this region.
     */
    private final List<GATKSAMRecord> reads = new ArrayList<GATKSAMRecord>();

    /**
     * An ordered list (by genomic coordinate) of the ActivityProfileStates that went
     * into this active region.  May be empty, which says that no supporting states were
     * provided when this region was created.
     */
    private final List<ActivityProfileState> supportingStates;

    /**
     * The raw span of this active region, not including the active region extension
     */
    private final GenomeLoc activeRegionLoc;

    /**
     * The span of this active region on the genome, including the active region extension
     */
    private final GenomeLoc extendedLoc;

    /**
     * The extension, in bp, of this active region.
     */
    private final int extension;

    /**
     * A genomeLocParser so we can create genomeLocs
     */
    private final GenomeLocParser genomeLocParser;

    /**
     * Does this region represent an active region (all isActiveProbs above threshold) or
     * an inactive region (all isActiveProbs below threshold)?
     */
    private final boolean isActive;

    /**
     * The span of this active region, including the bp covered by all reads in this
     * region.  This union of extensionLoc and the loc of all reads in this region.
     *
     * Must be at least as large as extendedLoc, but may be larger when reads
     * partially overlap this region.
     */
    private GenomeLoc spanIncludingReads;

    /**
     * Create a new ActiveRegion containing no reads
     *
     * @param activeRegionLoc the span of this active region
     * @param supportingStates the states that went into creating this region, or null / empty if none are available.
     *                         If not empty, must have exactly one state for each bp in activeRegionLoc
     * @param isActive indicates whether this is an active region, or an inactve one
     * @param genomeLocParser a non-null parser to let us create new genome locs
     * @param extension the active region extension to use for this active region
     */
    public ActiveRegion( final GenomeLoc activeRegionLoc, final List<ActivityProfileState> supportingStates, final boolean isActive, final GenomeLocParser genomeLocParser, final int extension ) {
        if ( activeRegionLoc == null ) throw new IllegalArgumentException("activeRegionLoc cannot be null");
        if ( activeRegionLoc.size() == 0 ) throw new IllegalArgumentException("Active region cannot be of zero size, but got " + activeRegionLoc);
        if ( genomeLocParser == null ) throw new IllegalArgumentException("genomeLocParser cannot be null");
        if ( extension < 0 ) throw new IllegalArgumentException("extension cannot be < 0 but got " + extension);

        this.activeRegionLoc = activeRegionLoc;
        this.supportingStates = supportingStates == null ? Collections.<ActivityProfileState>emptyList() : new ArrayList<ActivityProfileState>(supportingStates);
        this.isActive = isActive;
        this.genomeLocParser = genomeLocParser;
        this.extension = extension;
        this.extendedLoc = genomeLocParser.createGenomeLocOnContig(activeRegionLoc.getContig(), activeRegionLoc.getStart() - extension, activeRegionLoc.getStop() + extension);
        this.spanIncludingReads = extendedLoc;

        if ( ! this.supportingStates.isEmpty() ) {
            if ( this.supportingStates.size() != activeRegionLoc.size() )
                throw new IllegalArgumentException("Supporting states wasn't empty but it doesn't have exactly one state per bp in the active region: states " + this.supportingStates.size() + " vs. bp in region = " + activeRegionLoc.size());
            GenomeLoc lastStateLoc = null;
            for ( final ActivityProfileState state : this.supportingStates ) {
                if ( lastStateLoc != null ) {
                    if ( state.getLoc().getStart() != lastStateLoc.getStart() + 1 || state.getLoc().getContigIndex() != lastStateLoc.getContigIndex())
                        throw new IllegalArgumentException("Supporting state has an invalid sequence: last state was " + lastStateLoc + " but next state was " + state);
                }
                lastStateLoc = state.getLoc();
            }
        }
    }

    @Override
    public String toString() {
        return "ActiveRegion "  + activeRegionLoc.toString() + " active?=" + isActive() + " nReads=" + reads.size() + " ";
    }

    /**
     * See #getActiveRegionReference but with padding == 0
     */
    public byte[] getActiveRegionReference( final IndexedFastaSequenceFile referenceReader ) {
        return getActiveRegionReference(referenceReader, 0);
    }

    /**
     * Get the reference bases from referenceReader spanned by the extended location of this active region,
     * including additional padding bp on either side.  If this expanded region would exceed the boundaries
     * of the active region's contig, the returned result will be truncated to only include on-genome reference
     * bases
     * @param referenceReader the source of the reference genome bases
     * @param padding the padding, in BP, we want to add to either side of this active region extended region
     * @return a non-null array of bytes holding the reference bases in referenceReader
     */
    @Ensures("result != null")
    public byte[] getActiveRegionReference( final IndexedFastaSequenceFile referenceReader, final int padding ) {
        return getReference(referenceReader, padding, extendedLoc);
    }

    /**
     * See #getActiveRegionReference but using the span including regions not the extended span
     */
    public byte[] getFullReference( final IndexedFastaSequenceFile referenceReader ) {
        return getFullReference(referenceReader, 0);
    }

    /**
     * See #getActiveRegionReference but using the span including regions not the extended span
     */
    public byte[] getFullReference( final IndexedFastaSequenceFile referenceReader, final int padding ) {
        return getReference(referenceReader, padding, spanIncludingReads);
    }

    /**
     * Get the reference bases from referenceReader spanned by the extended location of this active region,
     * including additional padding bp on either side.  If this expanded region would exceed the boundaries
     * of the active region's contig, the returned result will be truncated to only include on-genome reference
     * bases
     * @param referenceReader the source of the reference genome bases
     * @param padding the padding, in BP, we want to add to either side of this active region extended region
     * @param genomeLoc a non-null genome loc indicating the base span of the bp we'd like to get the reference for
     * @return a non-null array of bytes holding the reference bases in referenceReader
     */
    @Ensures("result != null")
    private byte[] getReference( final IndexedFastaSequenceFile referenceReader, final int padding, final GenomeLoc genomeLoc ) {
        if ( referenceReader == null ) throw new IllegalArgumentException("referenceReader cannot be null");
        if ( padding < 0 ) throw new IllegalArgumentException("padding must be a positive integer but got " + padding);
        if ( genomeLoc == null ) throw new IllegalArgumentException("genomeLoc cannot be null");
        if ( genomeLoc.size() == 0 ) throw new IllegalArgumentException("GenomeLoc must have size > 0 but got " + genomeLoc);

        final byte[] reference =  referenceReader.getSubsequenceAt( genomeLoc.getContig(),
                Math.max(1, genomeLoc.getStart() - padding),
                Math.min(referenceReader.getSequenceDictionary().getSequence(genomeLoc.getContig()).getSequenceLength(), genomeLoc.getStop() + padding) ).getBases();

        return reference;
    }

    /**
     * Get the raw span of this active region (excluding the extension)
     * @return a non-null genome loc
     */
    @Override
    @Ensures("result != null")
    public GenomeLoc getLocation() { return activeRegionLoc; }

    /**
     * Get the span of this active region including the extension value
     * @return a non-null GenomeLoc
     */
    @Ensures("result != null")
    public GenomeLoc getExtendedLoc() { return extendedLoc; }

    /**
     * Get the span of this active region including the extension and the projects on the
     * genome of all reads in this active region.  That is, returns the bp covered by this
     * region and all reads in the region.
     * @return a non-null genome loc
     */
    @Ensures("result != null")
    public GenomeLoc getReadSpanLoc() { return spanIncludingReads; }

    /**
     * Get the active profile states that went into creating this region, if possible
     * @return an unmodifiable list of states that led to the creation of this region, or an empty
     *         list if none were provided
     */
    @Ensures("result != null")
    public List<ActivityProfileState> getSupportingStates() {
        return Collections.unmodifiableList(supportingStates);
    }

    /**
     * Get the active region extension applied to this region
     *
     * The extension is >= 0 bp in size, and indicates how much padding this art walker wanted for its regions
     *
     * @return the size in bp of the region extension
     */
    @Ensures("result >= 0")
    public int getExtension() { return extension; }

    /**
     * Get an unmodifiable list of reads currently in this active region.
     *
     * The reads are sorted by their coordinate position
     *
     * @return an unmodifiable list of reads in this active region
     */
    @Ensures("result != null")
    public List<GATKSAMRecord> getReads() {
        return Collections.unmodifiableList(reads);
    }

    /**
     * Get the number of reads currently in this active region
     * @return an integer >= 0
     */
    @Ensures("result >= 0")
    public int size() { return reads.size(); }

    /**
     * Add read to this active region
     *
     * Read must have alignment start >= than the last read currently in this active region.
     *
     * @throws IllegalArgumentException if read doesn't overlap the extended region of this active region
     *
     * @param read a non-null GATKSAMRecord
     */
    @Ensures("reads.size() == old(reads.size()) + 1")
    public void add( final GATKSAMRecord read ) {
        if ( read == null ) throw new IllegalArgumentException("Read cannot be null");

        final GenomeLoc readLoc = genomeLocParser.createGenomeLoc( read );
        if ( ! readOverlapsRegion(read) )
            throw new IllegalArgumentException("Read location " + readLoc + " doesn't overlap with active region extended span " + extendedLoc);

        spanIncludingReads = spanIncludingReads.union( readLoc );

        if ( ! reads.isEmpty() ) {
            final GATKSAMRecord lastRead = reads.get(size() - 1);
            if ( ! lastRead.getReferenceIndex().equals(read.getReferenceIndex()) )
                throw new IllegalArgumentException("Attempting to add a read to ActiveRegion not on the same contig as other reads: lastRead " + lastRead + " attempting to add " + read);

            if ( read.getAlignmentStart() < lastRead.getAlignmentStart() )
                throw new IllegalArgumentException("Attempting to add a read to ActiveRegion out of order w.r.t. other reads: lastRead " + lastRead + " at " + lastRead.getAlignmentStart() + " attempting to add " + read + " at " + read.getAlignmentStart());
        }

        reads.add( read );
    }

    /**
     * Returns true if read would overlap the extended extent of this region
     * @param read the read we want to test
     * @return true if read can be added to this region, false otherwise
     */
    public boolean readOverlapsRegion(final GATKSAMRecord read) {
        final GenomeLoc readLoc = genomeLocParser.createGenomeLoc( read );
        return readLoc.overlapsP(extendedLoc);
    }

    /**
     * Add all reads to this active region
     * @param reads a collection of reads to add to this active region
     */
    public void addAll(final Collection<GATKSAMRecord> reads) {
        if ( reads == null ) throw new IllegalArgumentException("reads cannot be null");
        for ( final GATKSAMRecord read : reads )
            add(read);
    }

    /**
     * Clear all of the reads currently in this active region
     */
    @Ensures("size() == 0")
    public void clearReads() {
        spanIncludingReads = extendedLoc;
        reads.clear();
    }

    /**
     * Remove all of the reads in readsToRemove from this active region
     * @param readsToRemove the collection of reads we want to remove
     */
    public void removeAll( final Collection<GATKSAMRecord> readsToRemove ) {
        reads.removeAll(readsToRemove);
        spanIncludingReads = extendedLoc;
        for ( final GATKSAMRecord read : reads ) {
            spanIncludingReads = spanIncludingReads.union( genomeLocParser.createGenomeLoc(read) );
        }
    }

    /**
     * Is this region equal to other, excluding any reads in either region in the comparison
     * @param other the other active region we want to test
     * @return true if this region is equal, excluding any reads and derived values, to other
     */
    protected boolean equalExceptReads(final ActiveRegion other) {
        if ( activeRegionLoc.compareTo(other.activeRegionLoc) != 0 ) return false;
        if ( isActive() != other.isActive()) return false;
        if ( genomeLocParser != other.genomeLocParser ) return false;
        if ( extension != other.extension ) return false;
        if ( extendedLoc.compareTo(other.extendedLoc) != 0 ) return false;
        return true;
    }

    /**
     * Does this region represent an active region (all isActiveProbs above threshold) or
     * an inactive region (all isActiveProbs below threshold)?
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Intersect this active region with the allowed intervals, returning a list of active regions
     * that only contain locations present in intervals
     *
     * Note that the returned list may be empty, if this active region doesn't overlap the set at all
     *
     * @param intervals a non-null set of intervals that are allowed
     * @return an ordered list of active region where each interval is contained within intervals
     */
    @Ensures("result != null")
    protected List<ActiveRegion> splitAndTrimToIntervals(final GenomeLocSortedSet intervals) {
        final List<GenomeLoc> allOverlapping = intervals.getOverlapping(getLocation());
        final List<ActiveRegion> clippedRegions = new LinkedList<ActiveRegion>();

        for ( final GenomeLoc overlapping : allOverlapping ) {
            final GenomeLoc subLoc = getLocation().intersect(overlapping);
            final int subStart = subLoc.getStart() - getLocation().getStart();
            final int subEnd = subStart + subLoc.size();
            final List<ActivityProfileState> subStates = supportingStates.isEmpty() ? supportingStates : supportingStates.subList(subStart, subEnd);
            final ActiveRegion clipped = new ActiveRegion( subLoc, subStates, isActive, genomeLocParser, extension );
            clippedRegions.add(clipped);
        }

        return clippedRegions;
    }
}