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

package org.broadinstitute.sting.gatk.refdata;

import net.sf.samtools.util.SequenceUtil;
import org.broad.tribble.Feature;
import org.broad.tribble.annotation.Strand;
import org.broad.tribble.dbsnp.OldDbSNPFeature;
import org.broad.tribble.gelitext.GeliTextFeature;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.classloader.PluginManager;
import org.broadinstitute.sting.utils.codecs.hapmap.RawHapMapFeature;
import org.broadinstitute.sting.utils.variant.GATKVariantContextUtils;
import org.broadinstitute.variant.variantcontext.*;

import java.util.*;

/**
 * A terrible but temporary approach to converting objects to VariantContexts.  If you want to add a converter,
 * you need to create a adaptor object here and register a converter from your class to this object.  When tribble arrives,
 * we'll use a better approach.
 *
 * To add a new converter:
 *
 *   create a subclass of VCAdaptor, overloading the convert operator
 *   add it to the static map from input type -> converter where the input type is the object.class you want to convert
 *
 * That's it 
 *
 * @author depristo@broadinstitute.org
 */
public class VariantContextAdaptors {
    // --------------------------------------------------------------------------------------------------------------
    //
    // Generic support routines.  Do not modify
    //
    // --------------------------------------------------------------------------------------------------------------

    private static Map<Class<? extends Feature>,VCAdaptor> adaptors = new HashMap<Class<? extends Feature>,VCAdaptor>();

    static {
        PluginManager<VCAdaptor> vcAdaptorManager = new PluginManager<VCAdaptor>(VCAdaptor.class);
        List<VCAdaptor> adaptorInstances = vcAdaptorManager.createAllTypes();
        for(VCAdaptor adaptor: adaptorInstances)
            adaptors.put(adaptor.getAdaptableFeatureType(),adaptor);
    }

    public static boolean canBeConvertedToVariantContext(Object variantContainingObject) {
        return adaptors.containsKey(variantContainingObject.getClass());
    }

    /** generic superclass */
    public interface VCAdaptor {
        /**
         * Gets the type of feature that this adaptor can 'adapt' into a VariantContext.
         * @return Type of adaptable feature.  Must be a Tribble feature class.
         */
        Class<? extends Feature> getAdaptableFeatureType();
        VariantContext convert(String name, Object input, ReferenceContext ref);
    }

    public static VariantContext toVariantContext(String name, Object variantContainingObject, ReferenceContext ref) {
        if ( ! adaptors.containsKey(variantContainingObject.getClass()) )
            return null;
        else {
            return adaptors.get(variantContainingObject.getClass()).convert(name, variantContainingObject, ref);
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // From here below you can add adaptor classes for new rods (or other types) to convert to VC
    //
    // --------------------------------------------------------------------------------------------------------------
    private static class VariantContextAdaptor implements VCAdaptor {
        /**
         * 'Null' adaptor; adapts variant contexts to variant contexts.
         * @return VariantContext.
         */
        @Override
        public Class<? extends Feature> getAdaptableFeatureType() { return VariantContext.class; }

        // already a VC, just cast and return it
        @Override        
        public VariantContext convert(String name, Object input, ReferenceContext ref) {
            return (VariantContext)input;
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // dbSNP to VariantContext
    //
    // --------------------------------------------------------------------------------------------------------------

    private static class DBSnpAdaptor implements VCAdaptor {
        private static boolean isSNP(OldDbSNPFeature feature) {
            return feature.getVariantType().contains("single") && feature.getLocationType().contains("exact");
        }

        private static boolean isMNP(OldDbSNPFeature feature) {
            return feature.getVariantType().contains("mnp") && feature.getLocationType().contains("range");
        }

        private static boolean isInsertion(OldDbSNPFeature feature) {
            return feature.getVariantType().contains("insertion");
        }

        private static boolean isDeletion(OldDbSNPFeature feature) {
            return feature.getVariantType().contains("deletion");
        }

        private static boolean isIndel(OldDbSNPFeature feature) {
            return isInsertion(feature) || isDeletion(feature) || isComplexIndel(feature);
        }

        public static boolean isComplexIndel(OldDbSNPFeature feature) {
            return feature.getVariantType().contains("in-del");
        }

        /**
         * gets the alternate alleles.  This method should return all the alleles present at the location,
         * NOT including the reference base.  This is returned as a string list with no guarantee ordering
         * of alleles (i.e. the first alternate allele is not always going to be the allele with the greatest
         * frequency).
         *
         * @return an alternate allele list
         */
        public static List<String> getAlternateAlleleList(OldDbSNPFeature feature) {
            List<String> ret = new ArrayList<String>();
            for (String allele : getAlleleList(feature))
                if (!allele.equals(String.valueOf(feature.getNCBIRefBase()))) ret.add(allele);
            return ret;
        }

        /**
         * gets the alleles.  This method should return all the alleles present at the location,
         * including the reference base.  The first allele should always be the reference allele, followed
         * by an unordered list of alternate alleles.
         *
         * @return an alternate allele list
         */
        public static List<String> getAlleleList(OldDbSNPFeature feature) {
            List<String> alleleList = new ArrayList<String>();
            // add ref first
            if ( feature.getStrand() == Strand.POSITIVE )
                alleleList = Arrays.asList(feature.getObserved());
            else
                for (String str : feature.getObserved())
                    alleleList.add(SequenceUtil.reverseComplement(str));
            if ( alleleList.size() > 0 && alleleList.contains(feature.getNCBIRefBase())
                    && !alleleList.get(0).equals(feature.getNCBIRefBase()) )
                Collections.swap(alleleList, alleleList.indexOf(feature.getNCBIRefBase()), 0);

            return alleleList;
        }

        /**
         * Converts non-VCF formatted dbSNP records to VariantContext. 
         * @return OldDbSNPFeature.
         */
        @Override
        public Class<? extends Feature> getAdaptableFeatureType() { return OldDbSNPFeature.class; }

        @Override        
        public VariantContext convert(String name, Object input, ReferenceContext ref) {
            OldDbSNPFeature dbsnp = (OldDbSNPFeature)input;

            int index = dbsnp.getStart() - ref.getWindow().getStart() - 1;
            if ( index < 0 )
                return null; // we weren't given enough reference context to create the VariantContext

            final byte refBaseForIndel = ref.getBases()[index];
            final boolean refBaseIsDash = dbsnp.getNCBIRefBase().equals("-");

            boolean addPaddingBase;
            if ( isSNP(dbsnp) || isMNP(dbsnp) )
                addPaddingBase = false;
            else if ( isIndel(dbsnp) || dbsnp.getVariantType().contains("mixed") )
                addPaddingBase = refBaseIsDash || GATKVariantContextUtils.requiresPaddingBase(stripNullDashes(getAlleleList(dbsnp)));
            else
                return null; // can't handle anything else

            Allele refAllele;
            if ( refBaseIsDash )
                refAllele = Allele.create(refBaseForIndel, true);
            else if ( ! Allele.acceptableAlleleBases(dbsnp.getNCBIRefBase()) )
                return null;
            else
                refAllele = Allele.create((addPaddingBase ? (char)refBaseForIndel : "") + dbsnp.getNCBIRefBase(), true);

            final List<Allele> alleles = new ArrayList<Allele>();
            alleles.add(refAllele);

            // add all of the alt alleles
            for ( String alt : getAlternateAlleleList(dbsnp) ) {
                if ( Allele.wouldBeNullAllele(alt.getBytes()))
                    alt = "";
                else if ( ! Allele.acceptableAlleleBases(alt) )
                    return null;

                alleles.add(Allele.create((addPaddingBase ? (char)refBaseForIndel : "") + alt, false));
            }

            final VariantContextBuilder builder = new VariantContextBuilder();
            builder.source(name).id(dbsnp.getRsID());
            builder.loc(dbsnp.getChr(), dbsnp.getStart() - (addPaddingBase ? 1 : 0), dbsnp.getEnd() - (addPaddingBase && refAllele.length() == 1 ? 1 : 0));
            builder.alleles(alleles);
            return builder.make();
        }

        private static List<String> stripNullDashes(final List<String> alleles) {
            final List<String> newAlleles = new ArrayList<String>(alleles.size());
            for ( final String allele : alleles ) {
                if ( allele.equals("-") )
                    newAlleles.add("");
                else
                    newAlleles.add(allele);
            }
            return newAlleles;
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // GELI to VariantContext
    //
    // --------------------------------------------------------------------------------------------------------------

    private static class GeliTextAdaptor implements VCAdaptor {
        /**
         * Converts Geli text records to VariantContext. 
         * @return GeliTextFeature.
         */
        @Override
        public Class<? extends Feature> getAdaptableFeatureType() { return GeliTextFeature.class; }

        /**
         * convert to a Variant Context, given:
         * @param name  the name of the ROD
         * @param input the Rod object, in this case a RodGeliText
         * @param ref   the reference context
         * @return a VariantContext object
         */
        @Override
        public VariantContext convert(String name, Object input, ReferenceContext ref) {
            GeliTextFeature geli = (GeliTextFeature)input;
            if ( ! Allele.acceptableAlleleBases(String.valueOf(geli.getRefBase())) )
                return null;
            Allele refAllele = Allele.create(String.valueOf(geli.getRefBase()), true);

            // make sure we can convert it
            if ( geli.getGenotype().isHet() || !geli.getGenotype().containsBase(geli.getRefBase())) {
                // add the reference allele
                List<Allele> alleles = new ArrayList<Allele>();
                List<Allele> genotypeAlleles = new ArrayList<Allele>();
                // add all of the alt alleles
                for ( char alt : geli.getGenotype().toString().toCharArray() ) {
                    if ( ! Allele.acceptableAlleleBases(String.valueOf(alt)) ) {
                        return null;
                    }
                    Allele allele = Allele.create(String.valueOf(alt), false);
                    if (!alleles.contains(allele) && !refAllele.basesMatch(allele.getBases())) alleles.add(allele);

                    // add the allele, first checking if it's reference or not
                    if (!refAllele.basesMatch(allele.getBases())) genotypeAlleles.add(allele);
                    else genotypeAlleles.add(refAllele);
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                Collection<Genotype> genotypes = new ArrayList<Genotype>();
                Genotype call = GenotypeBuilder.create(name, genotypeAlleles);

                // add the call to the genotype list, and then use this list to create a VariantContext
                genotypes.add(call);
                alleles.add(refAllele);
                GenomeLoc loc = ref.getGenomeLocParser().createGenomeLoc(geli.getChr(),geli.getStart());
                return new VariantContextBuilder(name, loc.getContig(), loc.getStart(), loc.getStop(), alleles).genotypes(genotypes).log10PError(-1 * geli.getLODBestToReference()).attributes(attributes).make();
            } else
                return null; // can't handle anything else
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    //
    // HapMap to VariantContext
    //
    // --------------------------------------------------------------------------------------------------------------

    private static class HapMapAdaptor implements VCAdaptor {
        /**
         * Converts HapMap records to VariantContext. 
         * @return HapMapFeature.
         */
        @Override
        public Class<? extends Feature> getAdaptableFeatureType() { return RawHapMapFeature.class; }

        /**
         * convert to a Variant Context, given:
         * @param name  the name of the ROD
         * @param input the Rod object, in this case a RodGeliText
         * @param ref   the reference context
         * @return a VariantContext object
         */
        @Override        
        public VariantContext convert(String name, Object input, ReferenceContext ref) {
            if ( ref == null )
                throw new UnsupportedOperationException("Conversion from HapMap to VariantContext requires a reference context");

            RawHapMapFeature hapmap = (RawHapMapFeature)input;

            int index = hapmap.getStart() - ref.getWindow().getStart();
            if ( index < 0 )
                return null; // we weren't given enough reference context to create the VariantContext

            HashSet<Allele> alleles = new HashSet<Allele>();
            Allele refSNPAllele = Allele.create(ref.getBase(), true);
            int deletionLength = -1;

            Map<String, Allele> alleleMap = hapmap.getActualAlleles();
            // use the actual alleles, if available
            if ( alleleMap != null ) {
                alleles.addAll(alleleMap.values());
                Allele deletionAllele = alleleMap.get(RawHapMapFeature.INSERTION);  // yes, use insertion here (since we want the reference bases)
                if ( deletionAllele != null && deletionAllele.isReference() )
                    deletionLength = deletionAllele.length();
            } else {
                // add the reference allele for SNPs
                alleles.add(refSNPAllele);
            }

            // make a mapping from sample to genotype
            String[] samples = hapmap.getSampleIDs();
            String[] genotypeStrings = hapmap.getGenotypes();

            GenotypesContext genotypes = GenotypesContext.create(samples.length);
            for ( int i = 0; i < samples.length; i++ ) {
                // ignore bad genotypes
                if ( genotypeStrings[i].contains("N") )
                    continue;

                String a1 = genotypeStrings[i].substring(0,1);
                String a2 = genotypeStrings[i].substring(1);
                ArrayList<Allele> myAlleles = new ArrayList<Allele>(2);

                // use the mapping to actual alleles, if available
                if ( alleleMap != null ) {
                    myAlleles.add(alleleMap.get(a1));
                    myAlleles.add(alleleMap.get(a2));
                } else {
                    // ignore indels (which we can't handle without knowing the alleles)
                    if ( genotypeStrings[i].contains("I") || genotypeStrings[i].contains("D") )
                        continue;

                    Allele allele1 = Allele.create(a1, refSNPAllele.basesMatch(a1));
                    Allele allele2 = Allele.create(a2, refSNPAllele.basesMatch(a2));

                    myAlleles.add(allele1);
                    myAlleles.add(allele2);
                    alleles.add(allele1);
                    alleles.add(allele2);
                }

                Genotype g = GenotypeBuilder.create(samples[i], myAlleles);
                genotypes.add(g);
            }

            long end = hapmap.getEnd();
            if ( deletionLength > 0 )
                end += (deletionLength - 1);
            VariantContext vc = new VariantContextBuilder(name, hapmap.getChr(), hapmap.getStart(), end, alleles).id(hapmap.getName()).genotypes(genotypes).make();
            return vc;
       }
    }
}
