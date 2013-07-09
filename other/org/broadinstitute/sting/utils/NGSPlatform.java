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

import org.broadinstitute.sting.utils.sam.GATKSAMReadGroupRecord;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;

/**
 * A canonical, master list of the standard NGS platforms.  These values
 * can be obtained (efficiently) from a GATKSAMRecord object with the
 * getNGSPlatform method.
 *
 * @author Mark DePristo
 * @since 2011
 */
public enum NGSPlatform {
    // note the order of elements here determines the order of matching operations, and therefore the
    // efficiency of getting a NGSPlatform from a string.
    ILLUMINA("ILLUMINA", "SLX", "SOLEXA"),
    SOLID("SOLID"),
    LS454("454"),
    COMPLETE_GENOMICS("COMPLETE"),
    PACBIO("PACBIO"),
    ION_TORRENT("IONTORRENT"),
    CAPILLARY("CAPILLARY"),
    HELICOS("HELICOS"),
    UNKNOWN("UNKNOWN");

    /**
     * Array of the prefix names in a BAM file for each of the platforms.
     */
    protected final String[] BAM_PL_NAMES;

    NGSPlatform(final String... BAM_PL_NAMES) {
        if ( BAM_PL_NAMES.length == 0 ) throw new IllegalStateException("Platforms must have at least one name");

        for ( int i = 0; i < BAM_PL_NAMES.length; i++ )
            BAM_PL_NAMES[i] = BAM_PL_NAMES[i].toUpperCase();

        this.BAM_PL_NAMES = BAM_PL_NAMES;
    }

    /**
     * Returns a representative PL string for this platform
     * @return
     */
    public final String getDefaultPlatform() {
        return BAM_PL_NAMES[0];
    }

    /**
     * Convenience get -- get the NGSPlatform from a GATKSAMRecord.
     *
     * Just gets the platform from the GATKReadGroupRecord associated with this read.
     *
     * @param read a non-null GATKSAMRecord
     * @return an NGSPlatform object matching the PL field of the header, of UNKNOWN if there was no match,
     *         if there is no read group for read, or there's no PL field for the read group
     */
    public static NGSPlatform fromRead(final GATKSAMRecord read) {
        if ( read == null ) throw new IllegalArgumentException("read cannot be null");
        final GATKSAMReadGroupRecord rg = read.getReadGroup();
        return rg == null ? UNKNOWN : rg.getNGSPlatform();
    }

    /**
     * Returns the NGSPlatform corresponding to the PL tag in the read group
     * @param plFromRG -- the PL field (or equivalent) in a ReadGroup object.  Can be null => UNKNOWN
     * @return an NGSPlatform object matching the PL field of the header, or UNKNOWN if there was no match or plFromRG is null
     */
    public static NGSPlatform fromReadGroupPL(final String plFromRG) {
        if ( plFromRG == null ) return UNKNOWN;

        // todo -- algorithm could be implemented more efficiently, as the list of all
        // todo -- names is known upfront, so a decision tree could be used to identify
        // todo -- a prefix common to PL
        final String pl = plFromRG.toUpperCase();
        for ( final NGSPlatform ngsPlatform : NGSPlatform.values() ) {
            for ( final String bamPLName : ngsPlatform.BAM_PL_NAMES ) {
                if ( pl.contains(bamPLName) )
                    return ngsPlatform;
            }
        }

        return UNKNOWN;
    }

    /**
     * checks whether or not the requested platform is listed in the set (and is not unknown)
     *
     * @param platform the read group string that describes the platform used.  can be null
     * @return true if the platform is known (i.e. it's in the list and is not UNKNOWN)
     */
    public static boolean isKnown(final String platform) {
        return fromReadGroupPL(platform) != UNKNOWN;
    }
}
