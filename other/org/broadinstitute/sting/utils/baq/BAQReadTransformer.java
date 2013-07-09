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

package org.broadinstitute.sting.utils.baq;

import net.sf.picard.reference.IndexedFastaSequenceFile;
import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
import org.broadinstitute.sting.gatk.WalkerManager;
import org.broadinstitute.sting.gatk.iterators.ReadTransformer;
import org.broadinstitute.sting.gatk.walkers.BAQMode;
import org.broadinstitute.sting.gatk.walkers.Walker;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.exceptions.UserException;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;

/**
 * Applies Heng's BAQ calculation to a stream of incoming reads
 */
public class BAQReadTransformer extends ReadTransformer {
    private BAQ baqHMM;
    private IndexedFastaSequenceFile refReader;
    private BAQ.CalculationMode cmode;
    private BAQ.QualityMode qmode;

    @Override
    public ApplicationTime initializeSub(final GenomeAnalysisEngine engine, final Walker walker) {
        final BAQMode mode = WalkerManager.getWalkerAnnotation(walker, BAQMode.class);
        this.refReader = engine.getReferenceDataSource().getReference();
        this.cmode = engine.getArguments().BAQMode;
        this.qmode = mode.QualityMode();
        baqHMM = new BAQ(engine.getArguments().BAQGOP);

        if ( qmode == BAQ.QualityMode.DONT_MODIFY )
            throw new ReviewedStingException("BUG: shouldn't create BAQ transformer with quality mode DONT_MODIFY");

        if ( mode.ApplicationTime() == ReadTransformer.ApplicationTime.FORBIDDEN && enabled() )
            throw new UserException.BadArgumentValue("baq", "Walker cannot accept BAQ'd base qualities, and yet BAQ mode " + cmode + " was requested.");

        return mode.ApplicationTime();
    }

    @Override
    public boolean enabled() {
        return cmode != BAQ.CalculationMode.OFF;
    }

    @Override
    public GATKSAMRecord apply(final GATKSAMRecord read) {
        baqHMM.baqRead(read, refReader, cmode, qmode);
        return read;
    }
}
