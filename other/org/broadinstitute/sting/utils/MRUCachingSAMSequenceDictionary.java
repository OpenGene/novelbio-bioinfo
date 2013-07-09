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
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;

/**
 * A wrapper class that provides efficient most recently used caching for the global
 * SAMSequenceDictionary underlying all of the GATK engine capabilities.  It is essential
 * that these class be as efficient as possible.  It doesn't need to be thread-safe, as
 * GenomeLocParser uses a thread-local variable to ensure that each thread gets its own MRU
 * cache.
 *
 * The MRU elements are the SAMSequenceRecord, the lastContig, and the lastIndex.  The
 * cached value is the actual SAMSequenceRecord of the most recently accessed value from
 * getSequence, along with local variables for the contig index and contig string.
 */
final class MRUCachingSAMSequenceDictionary {
    /**
     * Our sequence dictionary
     */
    private final SAMSequenceDictionary dict;

    SAMSequenceRecord lastSSR = null;
    String lastContig = "";
    int lastIndex = -1;

    /**
     * Create a new MRUCachingSAMSequenceDictionary that provides information about sequences in dict
     * @param dict a non-null, non-empty sequencing dictionary
     */
    @Ensures("lastSSR == null")
    public MRUCachingSAMSequenceDictionary(final SAMSequenceDictionary dict) {
        if ( dict == null ) throw new IllegalArgumentException("Dictionary cannot be null");
        if ( dict.size() == 0 ) throw new IllegalArgumentException("Dictionary cannot have size zero");

        this.dict = dict;
    }

    /**
     * Get our sequence dictionary
     * @return a non-null SAMSequenceDictionary
     */
    @Ensures("result != null")
    public SAMSequenceDictionary getDictionary() {
        return dict;
    }

    /**
     * Is contig present in the dictionary?  Efficiently caching.
     * @param contig a non-null contig we want to test
     * @return true if contig is in dictionary, false otherwise
     */
    @Requires("contig != null")
    public final boolean hasContig(final String contig) {
        return contig.equals(lastContig) || dict.getSequence(contig) != null;
    }

    /**
     * Is contig index present in the dictionary?  Efficiently caching.
     * @param contigIndex an integer offset that might map to a contig in this dictionary
     * @return true if contigIndex is in dictionary, false otherwise
     */
    @Requires("contigIndex >= 0")
    public final boolean hasContigIndex(final int contigIndex) {
        return lastIndex == contigIndex || dict.getSequence(contigIndex) != null;
    }

    /**
     * Same as SAMSequenceDictionary.getSequence but uses a MRU cache for efficiency
     *
     * @param contig the contig name we want to get the sequence record of
     * @throws ReviewedStingException if contig isn't present in the dictionary
     * @return the sequence record for contig
     */
    @Requires("contig != null")
    @Ensures("result != null")
    public final SAMSequenceRecord getSequence(final String contig) {
        if ( isCached(contig) )
            return lastSSR;
        else
            return updateCache(contig, -1);
    }

    /**
     * Same as SAMSequenceDictionary.getSequence but uses a MRU cache for efficiency
     *
     * @param index the contig index we want to get the sequence record of
     * @throws ReviewedStingException if contig isn't present in the dictionary
     * @return the sequence record for contig
     */
    @Requires("index >= 0")
    @Ensures("result != null")
    public final SAMSequenceRecord getSequence(final int index) {
        if ( isCached(index) )
            return lastSSR;
        else
            return updateCache(null, index);
    }

    /**
     * Same as SAMSequenceDictionary.getSequenceIndex but uses a MRU cache for efficiency
     *
     * @param contig the contig we want to get the sequence record of
     * @throws ReviewedStingException if index isn't present in the dictionary
     * @return the sequence record index for contig
     */
    @Requires("contig != null")
    @Ensures("result >= 0")
    public final int getSequenceIndex(final String contig) {
        if ( ! isCached(contig) ) {
            updateCache(contig, -1);
        }

        return lastIndex;
    }

    /**
     * Is contig the MRU cached contig?
     * @param contig the contig to test
     * @return true if contig is the currently cached contig, false otherwise
     */
    @Requires({"contig != null"})
    protected boolean isCached(final String contig) {
        return contig.equals(lastContig);
    }

    /**
     * Is the contig index index the MRU cached index?
     * @param index the contig index to test
     * @return true if contig index is the currently cached contig index, false otherwise
     */
    protected boolean isCached(final int index) {
        return lastIndex == index;
    }

    /**
     * The key algorithm.  Given a new record, update the last used record, contig
     * name, and index.
     *
     * @param contig the contig we want to look up.  If null, index is used instead
     * @param index the contig index we want to look up.  Only used if contig is null
     * @throws ReviewedStingException if index isn't present in the dictionary
     * @return the SAMSequenceRecord for contig / index
     */
    @Requires("contig != null || index >= 0")
    @Ensures("result != null")
    private SAMSequenceRecord updateCache(final String contig, int index ) {
        SAMSequenceRecord rec = contig == null ? dict.getSequence(index) : dict.getSequence(contig);
        if ( rec == null ) {
            throw new ReviewedStingException("BUG: requested unknown contig=" + contig + " index=" + index);
        } else {
            lastSSR = rec;
            lastContig = rec.getSequenceName();
            lastIndex = rec.getSequenceIndex();
            return rec;
        }
    }
}
