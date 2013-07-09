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

import net.sf.samtools.util.StringUtil;
import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.exceptions.UserException;

import java.util.Arrays;

/**
 * BaseUtils contains some basic utilities for manipulating nucleotides.
 */
public class BaseUtils {

    public enum Base {
        A ('A'),
        C ('C'),
        G ('G'),
        T ('T'),
        N ('N'),
        D ('D');

        public byte base;

        private Base(final char base) {
            this.base = (byte)base;
        }
    }

    // todo -- add this to the generalized base abstraction using the Base enum.
    public final static byte[] BASES = {'A', 'C', 'G', 'T'};
    public final static byte[] EXTENDED_BASES = {'A', 'C', 'G', 'T', 'N', 'D'};

    static private final int[] baseIndexMap = new int[256];
    static {
        Arrays.fill(baseIndexMap, -1);
        baseIndexMap['A'] = Base.A.ordinal();
        baseIndexMap['a'] = Base.A.ordinal();
        baseIndexMap['*'] = Base.A.ordinal();    // the wildcard character counts as an A
        baseIndexMap['C'] = Base.C.ordinal();
        baseIndexMap['c'] = Base.C.ordinal();
        baseIndexMap['G'] = Base.G.ordinal();
        baseIndexMap['g'] = Base.G.ordinal();
        baseIndexMap['T'] = Base.T.ordinal();
        baseIndexMap['t'] = Base.T.ordinal();
    }

    static private final int[] baseIndexWithIupacMap = baseIndexMap.clone();
    static {
        baseIndexWithIupacMap['*'] = -1;    // the wildcard character is bad
        baseIndexWithIupacMap['N'] = Base.N.ordinal();
        baseIndexWithIupacMap['n'] = Base.N.ordinal();
        baseIndexWithIupacMap['R'] = Base.N.ordinal();
        baseIndexWithIupacMap['r'] = Base.N.ordinal();
        baseIndexWithIupacMap['Y'] = Base.N.ordinal();
        baseIndexWithIupacMap['y'] = Base.N.ordinal();
        baseIndexWithIupacMap['M'] = Base.N.ordinal();
        baseIndexWithIupacMap['m'] = Base.N.ordinal();
        baseIndexWithIupacMap['K'] = Base.N.ordinal();
        baseIndexWithIupacMap['k'] = Base.N.ordinal();
        baseIndexWithIupacMap['W'] = Base.N.ordinal();
        baseIndexWithIupacMap['w'] = Base.N.ordinal();
        baseIndexWithIupacMap['S'] = Base.N.ordinal();
        baseIndexWithIupacMap['s'] = Base.N.ordinal();
        baseIndexWithIupacMap['B'] = Base.N.ordinal();
        baseIndexWithIupacMap['b'] = Base.N.ordinal();
        baseIndexWithIupacMap['D'] = Base.N.ordinal();
        baseIndexWithIupacMap['d'] = Base.N.ordinal();
        baseIndexWithIupacMap['H'] = Base.N.ordinal();
        baseIndexWithIupacMap['h'] = Base.N.ordinal();
        baseIndexWithIupacMap['V'] = Base.N.ordinal();
        baseIndexWithIupacMap['v'] = Base.N.ordinal();
    }

    /// In genetics, a transition is a mutation changing a purine to another purine nucleotide (A <-> G) or
    // a pyrimidine to another pyrimidine nucleotide (C <-> T).
    // Approximately two out of every three single nucleotide polymorphisms (SNPs) are transitions.
    public enum BaseSubstitutionType {
        TRANSITION,         // A <-> G or C <-> T
        TRANSVERSION
    }

    /**
     * Returns the base substitution type of the 2 state SNP
     *
     * @param base1
     * @param base2
     * @return
     */
    public static BaseSubstitutionType SNPSubstitutionType(byte base1, byte base2) {
        BaseSubstitutionType t = isTransition(base1, base2) ? BaseSubstitutionType.TRANSITION : BaseSubstitutionType.TRANSVERSION;
        //System.out.printf("SNPSubstitutionType( char %c, char %c ) => %s%n", base1, base2, t);
        return t;
    }

    public static boolean isTransition(byte base1, byte base2) {
        final int b1 = simpleBaseToBaseIndex(base1);
        final int b2 = simpleBaseToBaseIndex(base2);
        return b1 == Base.A.ordinal() && b2 == Base.G.ordinal() || b1 == Base.G.ordinal() && b2 == Base.A.ordinal() ||
                b1 == Base.C.ordinal() && b2 == Base.T.ordinal() || b1 == Base.T.ordinal() && b2 == Base.C.ordinal();
    }

    public static boolean isTransversion(byte base1, byte base2) {
        return !isTransition(base1, base2);
    }

    /**
     * Private constructor.  No instantiating this class!
     */
    private BaseUtils() {}

    static public boolean basesAreEqual(byte base1, byte base2) {
        return simpleBaseToBaseIndex(base1) == simpleBaseToBaseIndex(base2);
    }

    static public boolean extendedBasesAreEqual(byte base1, byte base2) {
        return extendedBaseToBaseIndex(base1) == extendedBaseToBaseIndex(base2);
    }

    /**
     * @return true iff the bases array contains at least one instance of base
     */
    static public boolean containsBase(final byte[] bases, final byte base) {
        for ( final byte b : bases ) {
            if ( b == base )
                return true;
        }
        return false;
    }

    public static boolean isUpperCase(final byte[] bases) {
        for ( byte base : bases )
            if ( ! isUpperCase(base) )
                return false;
        return true;
    }

    public static boolean isUpperCase(final byte base) {
        return base >= 'A' && base <= 'Z';
    }

    public static byte[] convertIUPACtoN(final byte[] bases, final boolean errorOnBadReferenceBase, final boolean ignoreConversionOfFirstByte) {
        final int length = bases.length;
        final int start = ignoreConversionOfFirstByte ? 1 : 0;

        for ( int i = start; i < length; i++ ) {
            final int baseIndex = baseIndexWithIupacMap[bases[i]];
            if ( baseIndex == Base.N.ordinal() ) {
                bases[i] = 'N';
            } else if ( errorOnBadReferenceBase && baseIndex == -1 ) {
                throw new UserException.BadInput("We encountered a non-standard non-IUPAC base in the provided reference: '" + bases[i] + "'");
            }
        }
        return bases;
    }

    /**
     * Converts a IUPAC nucleotide code to a pair of bases
     *
     * @param code
     * @return 0, 1, 2, 3, or -1 if the base can't be understood
     */
    @Deprecated
    static public char[] iupacToBases(char code) {
        char[] bases = new char[2];
        switch (code) {
            case '*':               // the wildcard character counts as an A
            case 'A':
            case 'a':
                bases[0] = bases[1] = 'A';
                break;
            case 'C':
            case 'c':
                bases[0] = bases[1] = 'C';
                break;
            case 'G':
            case 'g':
                bases[0] = bases[1] = 'G';
                break;
            case 'T':
            case 't':
                bases[0] = bases[1] = 'T';
                break;
            case 'R':
            case 'r':
                bases[0] = 'A';
                bases[1] = 'G';
                break;
            case 'Y':
            case 'y':
                bases[0] = 'C';
                bases[1] = 'T';
                break;
            case 'S':
            case 's':
                bases[0] = 'G';
                bases[1] = 'C';
                break;
            case 'W':
            case 'w':
                bases[0] = 'A';
                bases[1] = 'T';
                break;
            case 'K':
            case 'k':
                bases[0] = 'G';
                bases[1] = 'T';
                break;
            case 'M':
            case 'm':
                bases[0] = 'A';
                bases[1] = 'C';
                break;
            default:
                bases[0] = bases[1] = 'N';
        }
        return bases;
    }

    /**
     * Converts a simple base to a base index
     *
     * @param base [AaCcGgTt]
     * @return 0, 1, 2, 3, or -1 if the base can't be understood
     */
    static public int simpleBaseToBaseIndex(final byte base) {
        if ( base < 0 || base >= 256 )
            throw new UserException.BadInput("Non-standard bases were encountered in either the input reference or BAM file(s)");
        return baseIndexMap[base];
    }

    /**
     * Converts a simple base to a base index
     *
     * @param base [AaCcGgTt]
     * @return 0, 1, 2, 3, or -1 if the base can't be understood
     */
    @Deprecated
    static public int simpleBaseToBaseIndex(char base) {
        return baseIndexMap[base];
    }

    static public int extendedBaseToBaseIndex(byte base) {
        switch (base) {
            case 'd':
            case 'D':
                return Base.D.ordinal();
            case 'n':
            case 'N':
                return Base.N.ordinal();

            default:
                return simpleBaseToBaseIndex(base);
        }
    }

    @Deprecated
    static public boolean isRegularBase( final char base ) {
        return simpleBaseToBaseIndex(base) != -1;
    }

    static public boolean isRegularBase( final byte base ) {
        return simpleBaseToBaseIndex(base) != -1;
    }

    static public boolean isAllRegularBases( final byte[] bases ) {
        for( final byte base : bases) {
            if( !isRegularBase(base) ) { return false; }
        }
        return true;
    }

    static public boolean isNBase(byte base) {
        return base == 'N' || base == 'n';
    }

    /**
     * Converts a base index to a simple base
     *
     * @param baseIndex 0, 1, 2, 3
     * @return A, C, G, T, or '.' if the index can't be understood
     */
    static public byte baseIndexToSimpleBase(int baseIndex) {
        switch (baseIndex) {
            case 0:
                return 'A';
            case 1:
                return 'C';
            case 2:
                return 'G';
            case 3:
                return 'T';
            default:
                return '.';
        }
    }

    /**
     * Return the complement (A <-> T or C <-> G) of a base, or the specified base if it can't be complemented (i.e. an ambiguous base).
     *
     * @param base the base [AaCcGgTt]
     * @return the complementary base, or the input base if it's not one of the understood ones
     */
    static public byte simpleComplement(byte base) {
        switch (base) {
            case 'A':
            case 'a':
                return 'T';
            case 'C':
            case 'c':
                return 'G';
            case 'G':
            case 'g':
                return 'C';
            case 'T':
            case 't':
                return 'A';
            default:
                return base;
        }
    }

    @Deprecated
    static private char simpleComplement(char base) {
        return (char) simpleComplement((byte) base);
    }

    /**
     * Reverse complement a byte array of bases (that is, chars casted to bytes, *not* base indices in byte form)
     *
     * @param bases the byte array of bases
     * @return the reverse complement of the base byte array
     */
    static public byte[] simpleReverseComplement(byte[] bases) {
        byte[] rcbases = new byte[bases.length];

        for (int i = 0; i < bases.length; i++) {
            rcbases[i] = simpleComplement(bases[bases.length - 1 - i]);
        }

        return rcbases;
    }

    /**
     * Reverse complement a char array of bases
     *
     * @param bases the char array of bases
     * @return the reverse complement of the char byte array
     */
    @Deprecated
    static public char[] simpleReverseComplement(char[] bases) {
        char[] rcbases = new char[bases.length];

        for (int i = 0; i < bases.length; i++) {
            rcbases[i] = simpleComplement(bases[bases.length - 1 - i]);
        }

        return rcbases;
    }

    /**
     * Reverse complement a String of bases.  Preserves ambiguous bases.
     *
     * @param bases the String of bases
     * @return the reverse complement of the String
     */
    @Deprecated
    static public String simpleReverseComplement(String bases) {
        return new String(simpleReverseComplement(bases.getBytes()));
    }

    /**
     * Returns the uppercased version of the bases
     *
     * @param bases   the bases
     * @return the upper cased version
     */
    static public void convertToUpperCase(final byte[] bases) {
        StringUtil.toUpperCase(bases);
    }

    /**
     * Returns the index of the most common base in the basecounts array. To be used with
     * pileup.getBaseCounts.
     *
     * @param baseCounts counts of a,c,g,t in order.
     * @return the index of the most common base
     */
    static public int mostFrequentBaseIndex(int[] baseCounts) {
        int mostFrequentBaseIndex = 0;
        for (int baseIndex = 1; baseIndex < 4; baseIndex++) {
            if (baseCounts[baseIndex] > baseCounts[mostFrequentBaseIndex]) {
                mostFrequentBaseIndex = baseIndex;
            }
        }
        return mostFrequentBaseIndex;
    }

    static public int mostFrequentBaseIndexNotRef(int[] baseCounts, int refBaseIndex) {
        int tmp = baseCounts[refBaseIndex];
        baseCounts[refBaseIndex] = -1;
        int result = mostFrequentBaseIndex(baseCounts);
        baseCounts[refBaseIndex] = tmp;
        return result;
    }

    static public int mostFrequentBaseIndexNotRef(int[] baseCounts, byte refSimpleBase) {
        return mostFrequentBaseIndexNotRef(baseCounts, simpleBaseToBaseIndex(refSimpleBase));
    }

    /**
     * Returns the most common base in the basecounts array. To be used with pileup.getBaseCounts.
     *
     * @param baseCounts counts of a,c,g,t in order.
     * @return the most common base
     */
    static public byte mostFrequentSimpleBase(int[] baseCounts) {
        return baseIndexToSimpleBase(mostFrequentBaseIndex(baseCounts));
    }

    /**
     * For the most frequent base in the sequence, return the percentage of the read it constitutes.
     *
     * @param sequence the read sequence
     * @return the percentage of the read that's made up of the most frequent base
     */
    static public double mostFrequentBaseFraction(byte[] sequence) {
        int[] baseCounts = new int[4];

        for (byte base : sequence) {
            int baseIndex = simpleBaseToBaseIndex(base);

            if (baseIndex >= 0) {
                baseCounts[baseIndex]++;
            }
        }

        int mostFrequentBaseIndex = mostFrequentBaseIndex(baseCounts);

        return ((double) baseCounts[mostFrequentBaseIndex]) / ((double) sequence.length);
    }

    // --------------------------------------------------------------------------------
    //
    // random bases
    //
    // --------------------------------------------------------------------------------

    /**
     * Return a random base index (A=0, C=1, G=2, T=3).
     *
     * @return a random base index (A=0, C=1, G=2, T=3)
     */
    static public int getRandomBaseIndex() {
        return getRandomBaseIndex(-1);
    }

    /**
     * Return a random base index, excluding some base index.
     *
     * @param excludeBaseIndex the base index to exclude
     * @return a random base index, excluding the one specified (A=0, C=1, G=2, T=3)
     */
    static public int getRandomBaseIndex(int excludeBaseIndex) {
        int randomBaseIndex = excludeBaseIndex;

        while (randomBaseIndex == excludeBaseIndex) {
            randomBaseIndex = GenomeAnalysisEngine.getRandomGenerator().nextInt(4);
        }

        return randomBaseIndex;
    }

    public static byte getComplement(byte base) {
        switch(base) {
            case 'a':
            case 'A':
                return 'T';
            case 'c':
            case 'C':
                return 'G';
            case 'g':
            case 'G':
                return 'C';
            case 't':
            case 'T':
                return 'A';
            case 'n':
            case 'N':
                return 'N';
            default:
                throw new ReviewedStingException("base must be A, C, G or T. " + (char) base + " is not a valid base.");
        }
    }
}
