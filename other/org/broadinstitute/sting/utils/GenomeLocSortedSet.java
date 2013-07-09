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

import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.log4j.Logger;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.interval.IntervalMergingRule;
import org.broadinstitute.sting.utils.interval.IntervalUtils;

import java.util.*;

/**
 *         <p/>
 *         Class GenomeLocCollection
 *         <p/>
 *         a set of genome locations. This collection is self sorting,
 *         and will merge genome locations that are overlapping. The remove function
 *         will also remove a region from the list, if the region to remove is a
 *         partial interval of a region in the collection it will remove the region from
 *         that element.
 *
 * @author aaron
 * Date: May 22, 2009
 * Time: 10:54:40 AM
 */
public class GenomeLocSortedSet extends AbstractSet<GenomeLoc> {
    private static Logger logger = Logger.getLogger(GenomeLocSortedSet.class);

    private GenomeLocParser genomeLocParser;

    // our private storage for the GenomeLoc's
    private final List<GenomeLoc> mArray = new ArrayList<GenomeLoc>();

    // cache this to make overlap checking much more efficient
    private int previousOverlapSearchIndex = -1;

    /**
     * Create a new, empty GenomeLocSortedSet
     *
     * @param parser a non-null the parser we use to create genome locs
     */
    public GenomeLocSortedSet(final GenomeLocParser parser) {
        if ( parser == null ) throw new IllegalArgumentException("parser cannot be null");
        this.genomeLocParser = parser;
    }

    /**
     * Create a new GenomeLocSortedSet containing location e
     *
     * @param parser a non-null the parser we use to create genome locs
     * @param e a single genome locs to add to this set
     */
    public GenomeLocSortedSet(final GenomeLocParser parser, final GenomeLoc e) {
        this(parser);
        add(e);
    }

    /**
     * Create a new GenomeLocSortedSet containing locations l
     *
     * The elements in l can be in any order, and can be overlapping.  They will be sorted first and
     * overlapping (but not contiguous) elements will be merged
     *
     * @param parser a non-null the parser we use to create genome locs
     * @param l a collection of genome locs to add to this set
     */
    public GenomeLocSortedSet(final GenomeLocParser parser, final Collection<GenomeLoc> l) {
        this(parser);

        final ArrayList<GenomeLoc> sorted = new ArrayList<GenomeLoc>(l);
        Collections.sort(sorted);
        mArray.addAll(IntervalUtils.mergeIntervalLocations(sorted, IntervalMergingRule.OVERLAPPING_ONLY));
    }

    /**
     * Gets the GenomeLocParser used to create this sorted set.
     * @return The parser.  Will never be null.
     */
    public GenomeLocParser getGenomeLocParser() {
        return genomeLocParser;
    }

    /**
     * get an iterator over this collection
     *
     * @return an iterator<GenomeLoc>
     */
    public Iterator<GenomeLoc> iterator() {
        return mArray.iterator();
    }

    /**
     * return the size of the collection
     *
     * @return the size of the collection
     */
    public int size() {
        return mArray.size();
    }

    /**
     * Return the size, in bp, of the genomic regions by all of the regions in this set
     * @return size in bp of the covered regions
     */
    public long coveredSize() {
        long s = 0;
        for ( GenomeLoc e : this )
            s += e.size();
        return s;
    }

    /**
     * Return the number of bps before loc in the sorted set
     *
     * @param loc the location before which we are counting bases
     * @return the number of base pairs over all previous intervals
     */
    public long sizeBeforeLoc(GenomeLoc loc) {
        long s = 0;

        for ( GenomeLoc e : this ) {
            if ( e.isBefore(loc) )
                s += e.size();
            else if ( e.isPast(loc) )
                break; // we are done
            else // loc is inside of s
                s += loc.getStart() - e.getStart();
        }

        return s;
    }

    /**
     * determine if the collection is empty
     *
     * @return true if we have no elements
     */
    public boolean isEmpty() {
        return mArray.isEmpty();
    }

    /**
     * Determine if the given loc overlaps any loc in the sorted set
     *
     * @param loc the location to test
     * @return trip if the location overlaps any loc
     */
    public boolean overlaps(final GenomeLoc loc) {
        // edge condition
        if ( mArray.isEmpty() )
            return false;

        // use the cached version first
        if ( previousOverlapSearchIndex != -1 && overlapsAtOrImmediatelyAfterCachedIndex(loc, true) )
            return true;

        // update the cached index
        previousOverlapSearchIndex = Collections.binarySearch(mArray, loc);

        // if it matches an interval exactly, we are done
        if ( previousOverlapSearchIndex >= 0 )
            return true;

        // check whether it overlaps the interval before or after the insertion point
        previousOverlapSearchIndex = Math.max(0, -1 * previousOverlapSearchIndex - 2);
        return overlapsAtOrImmediatelyAfterCachedIndex(loc, false);
    }

    private boolean overlapsAtOrImmediatelyAfterCachedIndex(final GenomeLoc loc, final boolean updateCachedIndex) {
        // check the cached entry
        if ( mArray.get(previousOverlapSearchIndex).overlapsP(loc) )
            return true;

        // check the entry after the cached entry since we may have moved to it
        boolean returnValue = false;
        if ( previousOverlapSearchIndex < mArray.size() - 1 ) {
            returnValue = mArray.get(previousOverlapSearchIndex + 1).overlapsP(loc);
            if ( updateCachedIndex )
                previousOverlapSearchIndex++;
        }

        return returnValue;
    }

    /**
     * Return a list of intervals overlapping loc
     *
     * @param loc the location we want overlapping intervals
     * @return a non-null list of locations that overlap loc
     */
    public List<GenomeLoc> getOverlapping(final GenomeLoc loc) {
        // the max ensures that if loc would be the first element, that we start searching at the first element
        final int index = Collections.binarySearch(mArray, loc);
        if ( index >= 0 )
            // we can safely return a singleton because overlapping regions are merged and loc is exactly in
            // the set already
            return Collections.singletonList(loc);

        // if loc isn't in the list index is (-(insertion point) - 1). The insertion point is defined as the point at
        // which the key would be inserted into the list: the index of the first element greater than the key, or list.size()
        // -ins - 1 = index => -ins = index + 1 => ins = -(index + 1)
        // Note that we look one before the index in this case, as loc might occur after the previous overlapping interval
        final int start = Math.max(-(index + 1) - 1, 0);
        final int size = mArray.size();

        final List<GenomeLoc> overlapping = new LinkedList<GenomeLoc>();
        for ( int i = start; i < size; i++ ) {
            final GenomeLoc myLoc = mArray.get(i);
            if ( loc.overlapsP(myLoc) )
                overlapping.add(myLoc);
            else if ( myLoc.isPast(loc) )
                // since mArray is ordered, if myLoc is past loc that means all future
                // intervals cannot overlap loc either.  So we can safely abort the search
                // note that we need to be a bit conservative on our tests since index needs to start
                // at -1 the position of index, so it's possible that myLoc and loc don't overlap but the next
                // position might
                break;
        }

        return overlapping;
    }

    /**
     * Return a list of intervals overlapping loc by enumerating all locs and testing for overlap
     *
     * Purely for testing purposes -- this is way to slow for any production code
     *
     * @param loc the location we want overlapping intervals
     * @return a non-null list of locations that overlap loc
     */
    protected List<GenomeLoc> getOverlappingFullSearch(final GenomeLoc loc) {
        final List<GenomeLoc> overlapping = new LinkedList<GenomeLoc>();

        // super slow, but definitely works
        for ( final GenomeLoc myLoc : mArray ) {
            if ( loc.overlapsP(myLoc) )
                overlapping.add(myLoc);
        }

        return overlapping;
    }

    /**
     * add a genomeLoc to the collection, simply inserting in order into the set
     *
     * TODO -- this may break the contract of the GenomeLocSortedSet if e overlaps or
     * TODO -- other locations already in the set.  This code should check to see if
     * TODO -- e is overlapping with its nearby elements and merge them or alternatively
     * TODO -- throw an exception
     *
     * @param e the GenomeLoc to add
     *
     * @return true
     */
    public boolean add(GenomeLoc e) {
        // assuming that the intervals coming arrive in order saves us a fair amount of time (and it's most likely true)
        if (mArray.size() > 0 && e.isPast(mArray.get(mArray.size() - 1))) {
            mArray.add(e);
            return true;
        } else {
            final int loc = Collections.binarySearch(mArray,e);
            if (loc >= 0) {
                throw new ReviewedStingException("Genome Loc Sorted Set already contains the GenomicLoc " + e.toString());
            } else {
                mArray.add((loc+1) * -1,e);
                return true;
            }
        }
    }

    /**
     * Adds a GenomeLoc to the collection, merging it if it overlaps another region.
     * If it's not overlapping then we add it in sorted order.
     *
     * TODO TODO TODO -- this function is buggy and will not properly create a sorted
     * TODO TODO TODO -- genome loc is addRegion is called sequentially where the second
     * TODO TODO TODO -- loc added is actually before the first.  So when creating
     * TODO TODO TODO -- sets make sure to sort the input locations first!
     *
     * @param e the GenomeLoc to add to the collection
     *
     * @return true, if the GenomeLoc could be added to the collection
     */
    public boolean addRegion(GenomeLoc e) {
        if (e == null) {
            return false;
        }
        // have we added it to the collection?
        boolean haveAdded = false;

        /**
         * check if the specified element overlaps any current locations, if so
         * we should merge the two.
         */
        for (GenomeLoc g : mArray) {
            if (g.contiguousP(e)) {
                GenomeLoc c = g.merge(e);
                mArray.set(mArray.indexOf(g), c);
                haveAdded = true;
            } else if ((g.getContigIndex() == e.getContigIndex()) &&
                    (e.getStart() < g.getStart()) && !haveAdded) {
                mArray.add(mArray.indexOf(g), e);
                return true;
            } else if (haveAdded && ((e.getContigIndex() > e.getContigIndex()) ||
                    (g.getContigIndex() == e.getContigIndex() && e.getStart() > g.getStart()))) {
                return true;
            }
        }
        /** we're at the end and we haven't found locations that should fall after it,
         * so we'll put it at the end
         */
        if (!haveAdded) {
            mArray.add(e);
        }
        return true;
    }

    public GenomeLocSortedSet subtractRegions(GenomeLocSortedSet toRemoveSet) {
        LinkedList<GenomeLoc> good = new LinkedList<GenomeLoc>();
        Stack<GenomeLoc> toProcess = new Stack<GenomeLoc>();
        Stack<GenomeLoc> toExclude = new Stack<GenomeLoc>();

        // initialize the stacks
        toProcess.addAll(mArray);
        Collections.reverse(toProcess);
        toExclude.addAll(toRemoveSet.mArray);
        Collections.reverse(toExclude);

        int i = 0;
        while ( ! toProcess.empty() ) {    // while there's still stuff to process
            if ( toExclude.empty() ) {
                good.addAll(toProcess);         // no more excludes, all the processing stuff is good
                break;
            }

            GenomeLoc p = toProcess.peek();
            GenomeLoc e = toExclude.peek();

            if ( p.overlapsP(e) ) {
                toProcess.pop();
                for ( GenomeLoc newP : p.subtract(e) )
                    toProcess.push(newP);
            } else if ( p.compareContigs(e) < 0 ) {
                good.add(toProcess.pop());         // p is now good
            } else if ( p.compareContigs(e) > 0 ) {
                toExclude.pop();                 // e can't effect anything
            } else if ( p.getStop() < e.getStart() ) {
                good.add(toProcess.pop());         // p stops before e starts, p is good
            } else if ( e.getStop() < p.getStart() ) {
                toExclude.pop();                 // p starts after e stops, e is done
            } else {
                throw new ReviewedStingException("BUG: unexpected condition: p=" + p + ", e=" + e);
            }

            if ( i++ % 10000 == 0 )
                logger.debug("removeRegions operation: i = " + i);
        }

        return createSetFromList(genomeLocParser,good);
    }


    /**
     * a simple removal of an interval contained in this list.  The interval must be identical to one in the list (no partial locations or overlapping)
     * @param location the GenomeLoc to remove
     */
    public void remove(GenomeLoc location) {
        if (!mArray.contains(location)) throw new IllegalArgumentException("Unable to remove location: " + location + ", not in the list");
        mArray.remove(location);
    }

    /**
     * create a list of genomic locations, given a reference sequence
     *
     * @param dict the sequence dictionary to create a collection from
     *
     * @return the GenomeLocSet of all references sequences as GenomeLoc's
     */
    public static GenomeLocSortedSet createSetFromSequenceDictionary(SAMSequenceDictionary dict) {
        GenomeLocParser parser = new GenomeLocParser(dict);
        GenomeLocSortedSet returnSortedSet = new GenomeLocSortedSet(parser);
        for (SAMSequenceRecord record : dict.getSequences()) {
            returnSortedSet.add(parser.createGenomeLoc(record.getSequenceName(), 1, record.getSequenceLength()));
        }
        return returnSortedSet;
    }

    /**
     * Create a sorted genome location set from a list of GenomeLocs.
     *
     * @param locs the list<GenomeLoc>
     *
     * @return the sorted genome loc list
     */
    public static GenomeLocSortedSet createSetFromList(GenomeLocParser parser,List<GenomeLoc> locs) {
        GenomeLocSortedSet set = new GenomeLocSortedSet(parser);
        set.addAll(locs);
        return set;
    }


    /**
     * return a deep copy of this collection.
     *
     * @return a new GenomeLocSortedSet, identical to the current GenomeLocSortedSet.
     */
    public GenomeLocSortedSet clone() {
        GenomeLocSortedSet ret = new GenomeLocSortedSet(genomeLocParser);
        for (GenomeLoc loc : this.mArray) {
            // ensure a deep copy
            ret.mArray.add(genomeLocParser.createGenomeLoc(loc.getContig(), loc.getStart(), loc.getStop()));
        }
        return ret;
    }

    /**
     * convert this object to a list
     * @return the lists
     */
    public List<GenomeLoc> toList() {
        return this.mArray;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for ( GenomeLoc e : this ) {
            s.append(" ");
            s.append(e.toString());
        }
        s.append("]");

        return s.toString();
    }
}
