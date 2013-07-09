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

package org.broadinstitute.sting.utils;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;
import net.sf.samtools.SAMFileHeader;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mdepristo
 * Date: Mar 2, 2009
 * Time: 8:50:11 AM
 *
 * Genome location representation.  It is *** 1 *** based closed.  Note that GenomeLocs start and stop values
 * can be any positive or negative number, by design.  Bound validation is a feature of the GenomeLocParser,
 * and not a fundamental constraint of the GenomeLoc
 */
public class GenomeLoc implements Comparable<GenomeLoc>, Serializable, HasGenomeLocation {
    /**
     * the basic components of a genome loc, its contig index,
     * start and stop position, and (optionally) the contig name
     */
    protected final int contigIndex;
    protected final int start;
    protected final int stop;
    protected final String contigName;

    /**
     * A static constant to use when referring to the unmapped section of a datafile
     * file.  The unmapped region cannot be subdivided.  Only this instance of
     * the object may be used to refer to the region, as '==' comparisons are used
     * in comparators, etc.
     */
    // TODO - WARNING WARNING WARNING code somehow depends on the name of the contig being null!
    public static final GenomeLoc UNMAPPED = new GenomeLoc((String)null);
    public static final GenomeLoc WHOLE_GENOME = new GenomeLoc("all");

    public static final boolean isUnmapped(GenomeLoc loc) {
        return loc == UNMAPPED;
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // constructors
    //
    // --------------------------------------------------------------------------------------------------------------

    @Requires({
            "contig != null",
            "contigIndex >= 0", // I believe we aren't allowed to create GenomeLocs without a valid contigIndex
            "start <= stop"})
    protected GenomeLoc( final String contig, final int contigIndex, final int start, final int stop ) {
        this.contigName = contig;
        this.contigIndex = contigIndex;
        this.start = start;
        this.stop = stop;
    }

    /** Unsafe constructor for special constant genome locs */
    private GenomeLoc( final String contig ) {
        this.contigName = contig;
        this.contigIndex = -1;
        this.start = 0;
        this.stop = 0;
    }

    //
    // Accessors
    //
    @Ensures("result != null")
    public final GenomeLoc getLocation() { return this; }

    public final GenomeLoc getStartLocation() { return new GenomeLoc(getContig(),getContigIndex(),getStart(),getStart()); }

    public final GenomeLoc getStopLocation() { return new GenomeLoc(getContig(),getContigIndex(),getStop(),getStop()); }

    /**
     * @return the name of the contig of this GenomeLoc
     */
    public final String getContig() {
        return this.contigName;
    }

    public final int getContigIndex() { return this.contigIndex; }
    public final int getStart()    { return this.start; }
    public final int getStop()     { return this.stop; }

    @Ensures("result != null")
    public final String toString()  {
        if(GenomeLoc.isUnmapped(this)) return "unmapped";
        if ( throughEndOfContigP() && atBeginningOfContigP() )
            return getContig();
        else if ( throughEndOfContigP() || getStart() == getStop() )
            return String.format("%s:%d", getContig(), getStart());
        else
            return String.format("%s:%d-%d", getContig(), getStart(), getStop());
    }

    private boolean throughEndOfContigP() { return this.stop == Integer.MAX_VALUE; }

    private boolean atBeginningOfContigP() { return this.start == 1; }

    @Requires("that != null")
    public final boolean disjointP(GenomeLoc that) {
        return this.contigIndex != that.contigIndex || this.start > that.stop || that.start > this.stop;
    }

    @Requires("that != null")
    public final boolean discontinuousP(GenomeLoc that) {
        return this.contigIndex != that.contigIndex || (this.start - 1) > that.stop || (that.start - 1) > this.stop;
    }

    @Requires("that != null")
    public final boolean overlapsP(GenomeLoc that) {
        return ! disjointP( that );
    }

    @Requires("that != null")
    public final boolean contiguousP(GenomeLoc that) {
        return ! discontinuousP( that );
    }

    /**
     * Return true if this GenomeLoc represents the UNMAPPED location
     * @return
     */
    public final boolean isUnmapped() {
        return isUnmapped(this);
    }


    /**
     * Returns a new GenomeLoc that represents the entire span of this and that.  Requires that
     * this and that GenomeLoc are contiguous and both mapped
     */
    @Requires({
            "that != null",
            "isUnmapped(this) == isUnmapped(that)"})
    @Ensures("result != null")
    public GenomeLoc merge( GenomeLoc that ) throws ReviewedStingException {
        if(GenomeLoc.isUnmapped(this) || GenomeLoc.isUnmapped(that)) {
            if(! GenomeLoc.isUnmapped(this) || !GenomeLoc.isUnmapped(that))
                throw new ReviewedStingException("Tried to merge a mapped and an unmapped genome loc");
            return UNMAPPED;
        }

        if (!(this.contiguousP(that))) {
            throw new ReviewedStingException("The two genome loc's need to be contiguous");
        }

        return new GenomeLoc(getContig(), this.contigIndex,
                             Math.min( getStart(), that.getStart() ),
                             Math.max( getStop(), that.getStop()) );
    }

    /**
     * Returns a new GenomeLoc that represents the region between the endpoints of this and that. Requires that
     * this and that GenomeLoc are both mapped.
     */
    @Requires({"that != null", "isUnmapped(this) == isUnmapped(that)"})
    @Ensures("result != null")
    public GenomeLoc endpointSpan(GenomeLoc that) throws ReviewedStingException {
        if(GenomeLoc.isUnmapped(this) || GenomeLoc.isUnmapped(that)) {
            throw new ReviewedStingException("Cannot get endpoint span for unmerged genome locs");
        }

        if ( ! this.getContig().equals(that.getContig()) ) {
            throw new ReviewedStingException("Cannot get endpoint span for genome locs on different contigs");
        }

        return new GenomeLoc(getContig(),this.contigIndex,Math.min(getStart(),that.getStart()),Math.max(getStop(),that.getStop()));
    }

    /**
     * Splits the contig into to regions: [start,split point) and [split point, end].
     * @param splitPoint The point at which to split the contig.  Must be contained in the given interval.
     * @return A two element array consisting of the genome loc before the split and the one after.
     */
    public GenomeLoc[] split(final int splitPoint) {
        if(splitPoint < getStart() || splitPoint > getStop())
            throw new ReviewedStingException(String.format("Unable to split contig %s at split point %d; split point is not contained in region.",this,splitPoint));
        return new GenomeLoc[] { new GenomeLoc(getContig(),contigIndex,getStart(),splitPoint-1), new GenomeLoc(getContig(),contigIndex,splitPoint,getStop()) };
    }

    public GenomeLoc union( GenomeLoc that ) { return merge(that); }

    @Requires("that != null")
    @Ensures("result != null")
    public GenomeLoc intersect( GenomeLoc that ) throws ReviewedStingException {
        if(GenomeLoc.isUnmapped(this) || GenomeLoc.isUnmapped(that)) {
            if(! GenomeLoc.isUnmapped(this) || !GenomeLoc.isUnmapped(that))
                throw new ReviewedStingException("Tried to intersect a mapped and an unmapped genome loc");
            return UNMAPPED;
        }

        if (!(this.overlapsP(that))) {
            throw new ReviewedStingException("GenomeLoc::intersect(): The two genome loc's need to overlap");
        }

        return new GenomeLoc(getContig(), this.contigIndex,
                             Math.max(getStart(), that.getStart()),
                             Math.min( getStop(), that.getStop()) );
    }

    @Requires("that != null")
    public final List<GenomeLoc> subtract( final GenomeLoc that ) {
        if(GenomeLoc.isUnmapped(this) || GenomeLoc.isUnmapped(that)) {
            if(! GenomeLoc.isUnmapped(this) || !GenomeLoc.isUnmapped(that))
                throw new ReviewedStingException("Tried to intersect a mapped and an unmapped genome loc");
            return Arrays.asList(UNMAPPED);
        }

        if (!(this.overlapsP(that))) {
            throw new ReviewedStingException("GenomeLoc::minus(): The two genome loc's need to overlap");
        }

        if (equals(that)) {
            return Collections.emptyList();
        } else if (containsP(that)) {
            List<GenomeLoc> l = new ArrayList<GenomeLoc>(2);

            /**
             * we have to create two new region, one for the before part, one for the after
             * The old region:
             * |----------------- old region (g) -------------|
             *        |----- to delete (e) ------|
             *
             * product (two new regions):
             * |------|  + |--------|
             *
             */
            int afterStop = this.getStop(), afterStart = that.getStop() + 1;
            int beforeStop = that.getStart() - 1, beforeStart = this.getStart();
            if (afterStop - afterStart >= 0) {
                GenomeLoc after = new GenomeLoc(this.getContig(), getContigIndex(), afterStart, afterStop);
                l.add(after);
            }
            if (beforeStop - beforeStart >= 0) {
                GenomeLoc before = new GenomeLoc(this.getContig(), getContigIndex(), beforeStart, beforeStop);
                l.add(before);
            }

            return l;
        } else if (that.containsP(this)) {
            /**
             * e completely contains g, delete g, but keep looking, there may be more regions
             * i.e.:
             *   |--------------------- e --------------------|
             *       |--- g ---|    |---- others ----|
             */
            return Collections.emptyList();   // don't need to do anything
        } else {
            /**
             * otherwise e overlaps some part of g
             *
             * figure out which region occurs first on the genome.  I.e., is it:
             * |------------- g ----------|
             *       |------------- e ----------|
             *
             * or:
             *       |------------- g ----------|
             * |------------ e -----------|
             *
             */

            GenomeLoc n;
            if (that.getStart() < this.getStart()) {
                n = new GenomeLoc(this.getContig(), getContigIndex(), that.getStop() + 1, this.getStop());
            } else {
                n = new GenomeLoc(this.getContig(), getContigIndex(), this.getStart(), that.getStart() - 1);
            }

            // replace g with the new region
            return Arrays.asList(n);
        }
    }

    @Requires("that != null")
    public final boolean containsP(GenomeLoc that) {
        return onSameContig(that) && getStart() <= that.getStart() && getStop() >= that.getStop();
    }

    @Requires("that != null")
    public final boolean onSameContig(GenomeLoc that) {
        return (this.contigIndex == that.contigIndex);
    }

    @Requires("that != null")
    @Ensures("result >= 0")
    public final int distance( final GenomeLoc that ) {
        if ( this.onSameContig(that) )
            return Math.abs(this.getStart() - that.getStart());
        else
            return Integer.MAX_VALUE;
    }

    @Requires({"left != null", "right != null"})
    public final boolean isBetween( final GenomeLoc left, final GenomeLoc right ) {
        return this.compareTo(left) > -1 && this.compareTo(right) < 1;
    }

    /**
     * Tests whether this contig is completely before contig 'that'.
     * @param that Contig to test against.
     * @return true if this contig ends before 'that' starts; false if this is completely after or overlaps 'that'.
     */
    @Requires("that != null")
    public final boolean isBefore( GenomeLoc that ) {
        int comparison = this.compareContigs(that);
        return ( comparison == -1 || ( comparison == 0 && this.getStop() < that.getStart() ));        
    }

    /**
     * Tests whether this genome loc starts at the same position as that.
     *
     * i.e., do this and that have the same contig and the same start position
     *
     * @param that genome loc to compare to
     * @return true if this and that have the same contig and the same start position
     */
    @Requires("that != null")
    public final boolean startsAt( GenomeLoc that ) {
        int comparison = this.compareContigs(that);
        return comparison == 0 && this.getStart() == that.getStart();
    }

    /**
     * Tests whether any portion of this contig is before that contig.
     * @param that Other contig to test.
     * @return True if the start of this contig is before the start of the that contig.
     */
    @Requires("that != null")
    public final boolean startsBefore(final GenomeLoc that) {
        int comparison = this.compareContigs(that);
        return ( comparison == -1 || ( comparison == 0 && this.getStart() < that.getStart() ));
    }

    /**
     * Tests whether this contig is completely after contig 'that'.
     * @param that Contig to test against.
     * @return true if this contig starts after 'that' ends; false if this is completely before or overlaps 'that'.
     */
    @Requires("that != null")
    public final boolean isPast( GenomeLoc that ) {
        int comparison = this.compareContigs(that);
        return ( comparison == 1 || ( comparison == 0 && this.getStart() > that.getStop() ));
    }

    /**
     * Return the minimum distance between any pair of bases in this and that GenomeLocs:
     */
    @Requires("that != null")
    @Ensures("result >= 0")
    public final int minDistance( final GenomeLoc that ) {
        if (!this.onSameContig(that))
            return Integer.MAX_VALUE;

        int minDistance;
        if (this.isBefore(that))
            minDistance = distanceFirstStopToSecondStart(this, that);
        else if (that.isBefore(this))
            minDistance = distanceFirstStopToSecondStart(that, this);
        else // this and that overlap [and possibly one contains the other]:
            minDistance = 0;

        return minDistance;
    }

    @Requires({
            "locFirst != null",
            "locSecond != null",
            "locSecond.isPast(locFirst)"
    })
    @Ensures("result >= 0")
    private static int distanceFirstStopToSecondStart(GenomeLoc locFirst, GenomeLoc locSecond) {
        return locSecond.getStart() - locFirst.getStop();
    }



    /**
     * Check to see whether two genomeLocs are equal.
     * Note that this implementation ignores the contigInfo object.
     * @param other Other contig to compare.
     */
    @Override
    public boolean equals(Object other) {
        if(other == null)
            return false;
        if(other instanceof GenomeLoc) {
            GenomeLoc otherGenomeLoc = (GenomeLoc)other;
            return this.contigIndex == otherGenomeLoc.contigIndex &&
                   this.start == otherGenomeLoc.start &&
                   this.stop == otherGenomeLoc.stop;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return start << 16 | stop << 4 | contigIndex;
    }


    /**
     * conpare this genomeLoc's contig to another genome loc
     * @param that the genome loc to compare contigs with
     * @return 0 if equal, -1 if that.contig is greater, 1 if this.contig is greater
     */
    @Requires("that != null")
    @Ensures("result == 0 || result == 1 || result == -1")
    public final int compareContigs( GenomeLoc that ) {
        if (this.contigIndex == that.contigIndex)
            return 0;
        else if (this.contigIndex > that.contigIndex)
            return 1;
        return -1;
    }

    @Requires("that != null")
    @Ensures("result == 0 || result == 1 || result == -1")
    public int compareTo( GenomeLoc that ) {
        int result = 0;

        if ( this == that ) {
            result = 0;
        }
        else if(GenomeLoc.isUnmapped(this))
            result = 1;
        else if(GenomeLoc.isUnmapped(that))
            result = -1;
        else {
            final int cmpContig = compareContigs(that);

            if ( cmpContig != 0 ) {
                result = cmpContig;
            } else {
                if ( this.getStart() < that.getStart() ) result = -1;
                else if ( this.getStart() > that.getStart() ) result = 1;
                // these have the same start, so check the ends
                else if ( this.getStop() < that.getStop() ) result = -1;
                else if ( this.getStop() > that.getStop() ) result = 1;
            }
        }

        return result;
    }

    @Requires("that != null")
    public boolean endsAt(GenomeLoc that) {
        return (this.compareContigs(that) == 0) && ( this.getStop() == that.getStop() );
    }

    /**
     * How many BPs are covered by this locus?
     * @return Number of BPs covered by this locus.  According to the semantics of GenomeLoc, this should
     *         never be < 1.
     */
    @Ensures("result > 0")
    public int size() {
        return stop - start + 1;
    }

    /**
     * reciprocialOverlap: what is the min. percent of gl1 and gl2 covered by both
     *
     * gl1.s ---------- gk1.e
     * gl2.s ---------- gl2.e
     * 100%
     *
     * gl1.s ---------- gk1.e
     *      gl2.s ---------- gl2.e
     * 50%
     *
     * gl1.s ---------- gk1.e
     *      gl2.s -------------------- gl2.e
     * 25% (50% for gl1 but only 25% for gl2)
     */
    public final double reciprocialOverlapFraction(final GenomeLoc o) {
        if ( overlapsP(o) )
            return Math.min(overlapPercent(this, o), overlapPercent(o, this));
        else
            return 0.0;
    }

    private final static double overlapPercent(final GenomeLoc gl1, final GenomeLoc gl2) {
        return (1.0 * gl1.intersect(gl2).size()) / gl1.size();
    }

    public long sizeOfOverlap( final GenomeLoc that ) {
        return ( this.overlapsP(that) ? Math.min( getStop(), that.getStop() ) - Math.max( getStart(), that.getStart() ) + 1L : 0L );
    }

    /**
     * Returns the maximum GenomeLoc of this and other
     * @param other another non-null genome loc
     * @return the max of this and other
     */
    public GenomeLoc max(final GenomeLoc other) {
        final int cmp = this.compareTo(other);
        return cmp == -1 ? other : this;
    }

    /**
     * create a new genome loc from an existing loc, with a new start position
     * Note that this function will NOT explicitly check the ending offset, in case someone wants to
     * set the start of a new GenomeLoc pertaining to a read that goes off the end of the contig.
     *
     * @param loc   the old location
     * @param start a new start position
     *
     * @return a newly allocated GenomeLoc as loc but with start == start
     */
    public GenomeLoc setStart(GenomeLoc loc, int start) {
        return new GenomeLoc(loc.getContig(), loc.getContigIndex(), start, loc.getStop());
    }

    /**
     * create a new genome loc from an existing loc, with a new stop position
     * Note that this function will NOT explicitly check the ending offset, in case someone wants to
     * set the stop of a new GenomeLoc pertaining to a read that goes off the end of the contig.
     *
     * @param loc  the old location
     * @param stop a new stop position
     *
     * @return a newly allocated GenomeLoc as loc but with stop == stop
     */
    public GenomeLoc setStop(GenomeLoc loc, int stop) {
        return new GenomeLoc(loc.getContig(), loc.getContigIndex(), loc.start, stop);
    }

    /**
     * return a new genome loc, with an incremented position
     *
     * @param loc the old location
     *
     * @return a newly allocated GenomeLoc as loc but with start == loc.getStart() + 1
     */
    public GenomeLoc incPos(GenomeLoc loc) {
        return incPos(loc, 1);
    }

    /**
     * return a new genome loc, with an incremented position
     *
     * @param loc the old location
     * @param by  how much to move the start and stop by
     *
     * @return a newly allocated GenomeLoc as loc but with start == loc.getStart() + by
     */
    public GenomeLoc incPos(GenomeLoc loc, int by) {
        return new GenomeLoc(loc.getContig(), loc.getContigIndex(), loc.start + by, loc.stop + by);
    }

    /**
     * Merges 2 *contiguous* locs into 1
     *
     * @param a   GenomeLoc #1
     * @param b   GenomeLoc #2
     * @return one merged loc
     */
    @Requires("a != null && b != null")
    public static <T extends GenomeLoc> GenomeLoc merge(final T a, final T b) {
        if ( isUnmapped(a) || isUnmapped(b) ) {
            throw new ReviewedStingException("Tried to merge unmapped genome locs");
        }

        if ( !(a.contiguousP(b)) ) {
            throw new ReviewedStingException("The two genome locs need to be contiguous");
        }

        return new GenomeLoc(a.getContig(), a.contigIndex, Math.min(a.getStart(), b.getStart()), Math.max(a.getStop(), b.getStop()));
    }

    /**
     * Merges a list of *sorted* *contiguous* locs into 1
     *
     * @param sortedLocs a sorted list of contiguous locs
     * @return one merged loc
     */
    @Requires("sortedLocs != null")
    public static <T extends GenomeLoc> GenomeLoc merge(final SortedSet<T> sortedLocs) {
        GenomeLoc result = null;

        for ( GenomeLoc loc : sortedLocs ) {
            if ( loc.isUnmapped() )
                throw new ReviewedStingException("Tried to merge unmapped genome locs");

            if ( result == null )
                result = loc;
            else if ( !result.contiguousP(loc) )
                throw new ReviewedStingException("The genome locs need to be contiguous");
            else
                result = merge(result, loc);
        }

        return result;
    }

    /**
     * Calculates the distance between two genomeLocs across contigs (if necessary).
     *
     * Returns minDistance(other) if in same contig.
     * Works with intervals!
     * Uses the SAMFileHeader to extract the size of the contigs and follows the order in the dictionary.
     *
     * @param other         the genome loc to compare to
     * @param samFileHeader the contig information
     * @return the sum of all the bases in between the genomeLocs, including entire contigs
     */
    public long distanceAcrossContigs(GenomeLoc other, SAMFileHeader samFileHeader) {
        if (onSameContig(other))
            return minDistance(other);

        // add the distance from the first genomeLoc to the end of it's contig and the distance from the
        // second genomeLoc to the beginning of it's contig.
        long distance = 0;
        if (contigIndex < other.contigIndex) {
            distance += samFileHeader.getSequence(contigIndex).getSequenceLength() - stop;
            distance += other.start;
        } else {
            distance += samFileHeader.getSequence(other.contigIndex).getSequenceLength() - other.stop;
            distance += start;
        }

        // add any contig (in its entirety) in between the two genomeLocs
        for (int i=Math.min(this.contigIndex, other.contigIndex) + 1; i < Math.max(this.contigIndex, other.contigIndex); i++) {
            distance += samFileHeader.getSequence(i).getSequenceLength();
        }
        return distance;
    }
}
