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

package org.broadinstitute.sting.commandline;

import com.google.java.contract.Requires;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.Feature;
import org.broad.tribble.FeatureCodec;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
import org.broadinstitute.sting.gatk.refdata.ReferenceDependentFeatureCodec;
import org.broadinstitute.sting.gatk.refdata.tracks.FeatureManager;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.GenomeLocParser;
import org.broadinstitute.sting.utils.exceptions.UserException;
import org.broadinstitute.sting.utils.interval.IntervalUtils;

import java.util.*;

/**
 * An IntervalBinding representing a walker argument that gets bound to either a ROD track or interval string.
 *
 * The IntervalBinding<T> is a formal GATK argument that bridges between a walker and
 * the engine to construct intervals for traversal at runtime.  The IntervalBinding can
 * either be a RodBinding<T>, a string of one interval, or a file with interval strings.
 * The GATK Engine takes care of initializing the binding when appropriate and determining intervals from it.
 *
 * Note that this class is immutable.
 */
public final class IntervalBinding<T extends Feature> {

    private RodBinding<T> featureIntervals;
    private String stringIntervals;

    @Requires({"type != null", "rawName != null", "source != null", "tribbleType != null", "tags != null"})
    public IntervalBinding(Class<T> type, final String rawName, final String source, final String tribbleType, final Tags tags) {
        featureIntervals = new RodBinding<T>(type, rawName, source, tribbleType, tags);
    }

    @Requires({"intervalArgument != null"})
    public IntervalBinding(String intervalArgument) {
        stringIntervals = intervalArgument;
    }

    public String getSource() {
        if ( featureIntervals != null )
            return featureIntervals.getSource();
        return stringIntervals;
    }

    public List<GenomeLoc> getIntervals(final GenomeAnalysisEngine toolkit) {
        return getIntervals(toolkit.getGenomeLocParser());
    }

    public List<GenomeLoc> getIntervals(final GenomeLocParser genomeLocParser) {
        List<GenomeLoc> intervals;

        if ( featureIntervals != null ) {
            intervals = new ArrayList<GenomeLoc>();

            // TODO -- after ROD system cleanup, go through the ROD system so that we can handle things like gzipped files

            final FeatureCodec codec = new FeatureManager().getByName(featureIntervals.getTribbleType()).getCodec();
            if ( codec instanceof ReferenceDependentFeatureCodec )
                ((ReferenceDependentFeatureCodec)codec).setGenomeLocParser(genomeLocParser);
            try {
                FeatureReader<Feature> reader = AbstractFeatureReader.getFeatureReader(featureIntervals.getSource(), codec, false);
                for ( Feature feature : reader.iterator() )
                    intervals.add(genomeLocParser.createGenomeLoc(feature));
            } catch (Exception e) {
                throw new UserException.MalformedFile(featureIntervals.getSource(), "Problem reading the interval file", e);
            }

        } else {
            intervals = IntervalUtils.parseIntervalArguments(genomeLocParser, stringIntervals);
        }

        return intervals;
    }

    public String toString() {
        return getSource();
    }
}
