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

package org.broadinstitute.sting.gatk.walkers.annotator;

import org.broadinstitute.sting.commandline.Hidden;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.annotator.interfaces.AnnotatorCompatible;
import org.broadinstitute.sting.gatk.walkers.annotator.interfaces.ExperimentalAnnotation;
import org.broadinstitute.sting.gatk.walkers.annotator.interfaces.InfoFieldAnnotation;
import org.broadinstitute.sting.utils.genotyper.PerReadAlleleLikelihoodMap;
import org.broadinstitute.variant.vcf.VCFHeaderLineType;
import org.broadinstitute.variant.vcf.VCFInfoHeaderLine;
import org.broadinstitute.sting.utils.pileup.PileupElement;
import org.broadinstitute.sting.utils.pileup.ReadBackedPileup;
import org.broadinstitute.sting.utils.sam.ReadUtils;
import org.broadinstitute.variant.variantcontext.VariantContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Counts of bases from Illumina, 454, and SOLiD at this site
 */
@Hidden
public class TechnologyComposition extends InfoFieldAnnotation implements ExperimentalAnnotation {
    private String nIllumina = "NumIllumina";
    private String n454 ="Num454";
    private String nSolid = "NumSOLiD";
    private String nOther = "NumOther";
    public Map<String, Object> annotate(final RefMetaDataTracker tracker,
                                        final AnnotatorCompatible walker,
                                        final ReferenceContext ref,
                                        final Map<String, AlignmentContext> stratifiedContexts,
                                        final VariantContext vc,
                                        final Map<String, PerReadAlleleLikelihoodMap> stratifiedPerReadAlleleLikelihoodMap) {
        if ( stratifiedContexts.size() == 0 )
            return null;

        int readsIllumina = 0;
        int readsSolid = 0;
        int reads454 = 0;
        int readsOther = 0;

        for ( Map.Entry<String, AlignmentContext> sample : stratifiedContexts.entrySet() ) {
            AlignmentContext context = sample.getValue();
            final ReadBackedPileup pileup = context.getBasePileup();
            for ( PileupElement p : pileup ) {
                if(ReadUtils.is454Read(p.getRead()))
                    reads454++;
                else if (ReadUtils.isSOLiDRead(p.getRead()))
                    readsSolid++;
                else if (ReadUtils.isIlluminaRead(p.getRead()))
                    readsIllumina++;
                else
                    readsOther++;
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(nIllumina, String.format("%d", readsIllumina));
        map.put(n454, String.format("%d", reads454));
        map.put(nSolid, String.format("%d", readsSolid));
        map.put(nOther, String.format("%d", readsOther));
            return map;
    }

     public List<String> getKeyNames() { return Arrays.asList(nIllumina,n454,nSolid,nOther); }

     public List<VCFInfoHeaderLine> getDescriptions() { return Arrays.asList(new VCFInfoHeaderLine(nIllumina, 1, VCFHeaderLineType.Integer, "Number of Illumina reads"),
             new VCFInfoHeaderLine(n454, 1, VCFHeaderLineType.Integer, "Number of 454 reads"),
             new VCFInfoHeaderLine(nSolid, 1, VCFHeaderLineType.Integer, "Number of SOLiD reads"),
             new VCFInfoHeaderLine(nOther, 1, VCFHeaderLineType.Integer, "Number of Other technology reads")); }

}
