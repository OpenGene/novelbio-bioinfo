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

import com.google.java.contract.Requires;
import net.sf.samtools.Cigar;
import org.apache.commons.lang.ArrayUtils;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.sam.ReadUtils;
import org.broadinstitute.variant.variantcontext.Allele;
import org.broadinstitute.variant.variantcontext.VariantContext;

import java.io.Serializable;
import java.util.*;

public class Haplotype extends Allele {

    private GenomeLoc genomeLocation = null;
    private Map<Integer, VariantContext> eventMap = null;
    private Cigar cigar;
    private int alignmentStartHapwrtRef;
    private Event artificialEvent = null;

    /**
     * Main constructor
     *
     * @param bases bases
     * @param isRef is reference allele?
     */
    public Haplotype( final byte[] bases, final boolean isRef ) {
        super(bases.clone(), isRef);
    }

    public Haplotype( final byte[] bases ) {
        this(bases, false);
    }

    /**
     * Copy constructor.  Note the ref state of the provided allele is ignored!
     *
     * @param allele allele to copy
     */
    public Haplotype( final Allele allele ) {
        super(allele, true);
    }

    protected Haplotype( final byte[] bases, final Event artificialEvent ) {
        this(bases, false);
        this.artificialEvent = artificialEvent;
    }

    public Haplotype( final byte[] bases, final GenomeLoc loc ) {
        this(bases, false);
        this.genomeLocation = loc;
    }

    @Override
    public boolean equals( Object h ) {
        return h instanceof Haplotype && Arrays.equals(getBases(), ((Haplotype) h).getBases());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBases());
    }

    public Map<Integer, VariantContext> getEventMap() {
        return eventMap;
    }

    public void setEventMap( final Map<Integer, VariantContext> eventMap ) {
        this.eventMap = eventMap;
    }

    @Override
    public String toString() {
        return getDisplayString();
    }

    public long getStartPosition() {
        return genomeLocation.getStart();
    }

    public long getStopPosition() {
        return genomeLocation.getStop();
    }

    public int getAlignmentStartHapwrtRef() {
        return alignmentStartHapwrtRef;
    }

    public void setAlignmentStartHapwrtRef( final int alignmentStartHapwrtRef ) {
        this.alignmentStartHapwrtRef = alignmentStartHapwrtRef;
    }

    public Cigar getCigar() {
        return cigar;
    }

    public void setCigar( final Cigar cigar ) {
        this.cigar = cigar;
    }

    public boolean isArtificialHaplotype() {
        return artificialEvent != null;
    }

    public Event getArtificialEvent() {
        return artificialEvent;
    }

    public Allele getArtificialRefAllele() {
        return artificialEvent.ref;
    }

    public Allele getArtificialAltAllele() {
        return artificialEvent.alt;
    }

    public int getArtificialAllelePosition() {
        return artificialEvent.pos;
    }

    public void setArtificialEvent( final Event artificialEvent ) {
        this.artificialEvent = artificialEvent;
    }

    @Requires({"refInsertLocation >= 0"})
    public Haplotype insertAllele( final Allele refAllele, final Allele altAllele, final int refInsertLocation, final int genomicInsertLocation ) {
        // refInsertLocation is in ref haplotype offset coordinates NOT genomic coordinates
        final int haplotypeInsertLocation = ReadUtils.getReadCoordinateForReferenceCoordinate(alignmentStartHapwrtRef, cigar, refInsertLocation, ReadUtils.ClippingTail.RIGHT_TAIL, true);
        final byte[] myBases = this.getBases();
        if( haplotypeInsertLocation == -1 || haplotypeInsertLocation + refAllele.length() >= myBases.length ) { // desired change falls inside deletion so don't bother creating a new haplotype
            return null;
        }

        byte[] newHaplotypeBases = new byte[]{};
        newHaplotypeBases = ArrayUtils.addAll(newHaplotypeBases, ArrayUtils.subarray(myBases, 0, haplotypeInsertLocation)); // bases before the variant
        newHaplotypeBases = ArrayUtils.addAll(newHaplotypeBases, altAllele.getBases()); // the alt allele of the variant
        newHaplotypeBases = ArrayUtils.addAll(newHaplotypeBases, ArrayUtils.subarray(myBases, haplotypeInsertLocation + refAllele.length(), myBases.length)); // bases after the variant
        return new Haplotype(newHaplotypeBases, new Event(refAllele, altAllele, genomicInsertLocation));
    }

    public static class HaplotypeBaseComparator implements Comparator<Haplotype>, Serializable {
        @Override
        public int compare( final Haplotype hap1, final Haplotype hap2 ) {
            return compareHaplotypeBases(hap1, hap2);
        }

        public static int compareHaplotypeBases(final Haplotype hap1, final Haplotype hap2) {
            final byte[] arr1 = hap1.getBases();
            final byte[] arr2 = hap2.getBases();
            // compares byte arrays using lexical ordering
            final int len = Math.min(arr1.length, arr2.length);
            for( int iii = 0; iii < len; iii++ ) {
                final int cmp = arr1[iii] - arr2[iii];
                if (cmp != 0) { return cmp; }
            }
            return arr2.length - arr1.length;
        }
    }

    public static LinkedHashMap<Allele,Haplotype> makeHaplotypeListFromAlleles(final List<Allele> alleleList,
                                                                               final int startPos,
                                                                               final ReferenceContext ref,
                                                                               final int haplotypeSize,
                                                                               final int numPrefBases) {

        LinkedHashMap<Allele,Haplotype> haplotypeMap = new LinkedHashMap<Allele,Haplotype>();

        Allele refAllele = null;

        for (Allele a:alleleList) {
            if (a.isReference()) {
                refAllele = a;
                break;
            }
        }

        if (refAllele == null)
            throw new ReviewedStingException("BUG: no ref alleles in input to makeHaplotypeListfrom Alleles at loc: "+ startPos);

        final byte[] refBases = ref.getBases();

        final int startIdxInReference = 1 + startPos - numPrefBases - ref.getWindow().getStart();
        final String basesBeforeVariant = new String(Arrays.copyOfRange(refBases, startIdxInReference, startIdxInReference + numPrefBases));

        // protect against long events that overrun available reference context
        final int startAfter = Math.min(startIdxInReference + numPrefBases + refAllele.getBases().length - 1, refBases.length);
        final String basesAfterVariant = new String(Arrays.copyOfRange(refBases, startAfter, refBases.length));

        // Create location for all haplotypes
        final int startLoc = ref.getWindow().getStart() + startIdxInReference;
        final int stopLoc = startLoc + haplotypeSize-1;

        final GenomeLoc locus = ref.getGenomeLocParser().createGenomeLoc(ref.getLocus().getContig(),startLoc,stopLoc);

        for (final Allele a : alleleList) {

            final byte[] alleleBases = a.getBases();
            // use string concatenation
            String haplotypeString = basesBeforeVariant + new String(Arrays.copyOfRange(alleleBases, 1, alleleBases.length)) + basesAfterVariant;
            haplotypeString = haplotypeString.substring(0,haplotypeSize);

            haplotypeMap.put(a,new Haplotype(haplotypeString.getBytes(), locus));
        }

        return haplotypeMap;
    }

    private static class Event {
        public Allele ref;
        public Allele alt;
        public int pos;

        public Event( final Allele ref, final Allele alt, final int pos ) {
            this.ref = ref;
            this.alt = alt;
            this.pos = pos;
        }
    }
}
