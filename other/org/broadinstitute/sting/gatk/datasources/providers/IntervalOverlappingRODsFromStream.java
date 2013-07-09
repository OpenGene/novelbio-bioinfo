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

package org.broadinstitute.sting.gatk.datasources.providers;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;
import net.sf.picard.util.PeekableIterator;
import org.broadinstitute.sting.gatk.refdata.RODRecordListImpl;
import org.broadinstitute.sting.gatk.refdata.utils.GATKFeature;
import org.broadinstitute.sting.gatk.refdata.utils.RODRecordList;
import org.broadinstitute.sting.utils.GenomeLoc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Key algorithmic helper for ReadBasedReferenceOrderedData
 *
 * Takes a single iterator of features, and provides a single capability that returns
 * the list of RODs that overlap an interval.  Allows sequential getOverlapping calls
 * from intervals provided that these intervals always have increasing getStart() values.
 *
 */
class IntervalOverlappingRODsFromStream {
    /**
     * Only held for QC purposes
     */
    GenomeLoc lastQuery = null;

    private final String name;
    private final LinkedList<GATKFeature> currentFeatures = new LinkedList<GATKFeature>();
    private final PeekableIterator<RODRecordList> futureFeatures;

    /**
     * Create a new IntervalOverlappingRODsFromStream that reads elements from futureFeatures and
     * returns RODRecordLists having name
     *
     * @param name
     * @param futureFeatures
     */
    IntervalOverlappingRODsFromStream(final String name, final PeekableIterator<RODRecordList> futureFeatures) {
        if ( futureFeatures == null ) throw new IllegalArgumentException("futureFeatures cannot be null");

        this.name = name;
        this.futureFeatures = futureFeatures;
    }

    /**
     * Get the list of RODs overlapping loc from this stream of RODs.
     *
     * Sequential calls to this function must obey the rule that loc2.getStart >= loc1.getStart
     *
     * @param loc the interval to query
     * @return a non-null RODRecordList containing the overlapping RODs, which may be empty
     */
    @Ensures({"overlaps(loc, result)",
            "! futureFeatures.hasNext() || futureFeatures.peek().getLocation().isPast(loc)",
            "result != null"})
    public RODRecordList getOverlapping(final GenomeLoc loc) {
        if ( lastQuery != null && loc.getStart() < lastQuery.getStart() )
            throw new IllegalArgumentException(String.format("BUG: query interval (%s) starts before the previous interval %s", loc, lastQuery));

        trimCurrentFeaturesToLoc(loc);
        readOverlappingFutureFeatures(loc);
        return new RODRecordListImpl(name, subsetToOverlapping(loc, currentFeatures), loc);
    }


    /**
     * For contract assurance.  Checks that all bindings in loc overlap
     *
     * @param loc
     * @param bindings
     * @return
     */
    @Requires({"loc != null", "bindings != null"})
    private boolean overlaps(final GenomeLoc loc, final RODRecordList bindings) {
        for ( final GATKFeature feature : bindings )
            if ( ! feature.getLocation().overlapsP(loc) )
                return false;
        return true;
    }

    /**
     * Subset the features in all to those that overlap with loc
     *
     * The current features list contains everything read that cannot be thrown away yet, but not
     * everything in there necessarily overlaps with loc.  Subset to just those that do overlap
     *
     * @param loc the location that features must overlap
     * @param all the list of all features
     * @return a subset of all that overlaps with loc
     */
    @Requires({"loc != null", "all != null"})
    @Ensures("result.size() <= all.size()")
    private Collection<GATKFeature> subsetToOverlapping(final GenomeLoc loc, final Collection<GATKFeature> all) {
        final LinkedList<GATKFeature> overlapping = new LinkedList<GATKFeature>();
        for ( final GATKFeature feature : all )
            if ( feature.getLocation().overlapsP(loc) )
                overlapping.add(feature);
        return overlapping;
    }

    /**
     * Update function.  Remove all elements of currentFeatures that end before loc
     *
     * @param loc the location to use
     */
    @Requires("loc != null")
    @Ensures("currentFeatures.size() <= old(currentFeatures.size())")
    private void trimCurrentFeaturesToLoc(final GenomeLoc loc) {
        final ListIterator<GATKFeature> it = currentFeatures.listIterator();
        while ( it.hasNext() ) {
            final GATKFeature feature = it.next();
            if ( feature.getLocation().isBefore(loc) )
                it.remove();
        }
    }

    /**
     * Update function: Read all elements from futureFeatures that overlap with loc
     *
     * Stops at the first element that starts before the end of loc, or the stream empties
     *
     * @param loc
     */
    @Requires("loc != null")
    @Ensures("currentFeatures.size() >= old(currentFeatures.size())")
    private void readOverlappingFutureFeatures(final GenomeLoc loc) {
        while ( futureFeatures.hasNext() ) {
            final GenomeLoc nextLoc = futureFeatures.peek().getLocation();
            if ( nextLoc.isBefore(loc) ) {
                futureFeatures.next(); // next rod element is before loc, throw it away and keep looking
            } else if ( nextLoc.isPast(loc) ) {
                break; // next element is past loc, stop looking but don't pop it
            } else if ( nextLoc.overlapsP(loc) ) {
                // add overlapping elements to our current features, removing from stream
                for ( final GATKFeature feature : futureFeatures.next() ) {
                    currentFeatures.add(feature);
                }
            }
        }
    }
}
