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

package org.broadinstitute.sting.utils.codecs.refseq;

import org.broad.tribble.AsciiFeatureCodec;
import org.broad.tribble.Feature;
import org.broad.tribble.TribbleException;
import org.broadinstitute.sting.gatk.refdata.ReferenceDependentFeatureCodec;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.GenomeLocParser;
import org.broadinstitute.sting.utils.Utils;
import org.broadinstitute.sting.utils.exceptions.UserException;

import java.util.ArrayList;

/**
 * Allows for reading in RefSeq information
 *
 * <p>
 * Parses a sorted UCSC RefSeq file (see below) into relevant features: the gene name, the unique gene name (if multiple transcrips get separate entries), exons, gene start/stop, coding start/stop,
 * strandedness of transcription. 
 * </p>
 *
 * <p>
 * Instructions for generating a RefSeq file for use with the RefSeq codec can be found on the Wiki here
 * <a href="http://www.broadinstitute.org/gsa/wiki/index.php/RefSeq">http://www.broadinstitute.org/gsa/wiki/index.php/RefSeq</a>
 * </p>
 * <h2> Usage </h2>
 * The RefSeq Rod can be bound as any other rod, and is specified by REFSEQ, for example
 * <pre>
 * -refSeqBinding:REFSEQ /path/to/refSeq.txt
 * </pre>
 *
 * You will need to consult individual walkers for the binding name ("refSeqBinding", above)
 *
 * <h2>File format example</h2>
 * If you want to define your own file for use, the format is (tab delimited):
 * bin, name, chrom, strand, transcription start, transcription end, coding start, coding end, num exons, exon starts, exon ends, id, alt. name, coding start status (complete/incomplete), coding end status (complete,incomplete)
 * and exon frames, for example:
 * <pre>
 * 76 NM_001011874 1 - 3204562 3661579 3206102 3661429 3 3204562,3411782,3660632, 3207049,3411982,3661579, 0 Xkr4 cmpl cmpl 1,2,0,
 * </pre>
 * for more information see <a href="http://skip.ucsc.edu/cgi-bin/hgTables?hgsid=5651&hgta_doSchemaDb=mm8&hgta_doSchemaTable=refGene">here</a>
 * <p>
 *     
 * </p>
 *
 * @author Mark DePristo
 * @since 2010
 */
public class RefSeqCodec extends AsciiFeatureCodec<RefSeqFeature> implements ReferenceDependentFeatureCodec {

    /**
     * The parser to use when resolving genome-wide locations.
     */
    private GenomeLocParser genomeLocParser;
    private boolean zero_coding_length_user_warned = false;

    public RefSeqCodec() {
        super(RefSeqFeature.class);
    }

    /**
     * Set the parser to use when resolving genetic data.
     * @param genomeLocParser The supplied parser.
     */
    @Override
    public void setGenomeLocParser(GenomeLocParser genomeLocParser) {
        this.genomeLocParser =  genomeLocParser;
    }

    @Override
    public Feature decodeLoc(String line) {
        if (line.startsWith("#")) return null;
        String fields[] = line.split("\t");
        if (fields.length < 3) throw new TribbleException("RefSeq (decodeLoc) : Unable to parse line -> " + line + ", we expected at least 3 columns, we saw " + fields.length);
        String contig_name = fields[2];
        try {
            return new RefSeqFeature(genomeLocParser.createGenomeLoc(contig_name, Integer.parseInt(fields[4])+1, Integer.parseInt(fields[5])));
        } catch ( UserException.MalformedGenomeLoc e ) {
            Utils.warnUser("RefSeq file is potentially incorrect, as some transcripts or exons have a negative length ("+fields[2]+")");
            return null;
        } catch ( NumberFormatException e ) {
            throw new UserException.MalformedFile("Could not parse location from line: " + line);
        }
    }

    /** Fills this object from a text line in RefSeq (UCSC) text dump file */
    @Override
    public RefSeqFeature decode(String line) {
        if (line.startsWith("#")) return null;
        String fields[] = line.split("\t");

        // we reference postion 15 in the split array below, make sure we have at least that many columns
        if (fields.length < 16) throw new TribbleException("RefSeq (decode) : Unable to parse line -> " + line + ", we expected at least 16 columns, we saw " + fields.length);
        String contig_name = fields[2];
        RefSeqFeature feature = new RefSeqFeature(genomeLocParser.createGenomeLoc(contig_name, Integer.parseInt(fields[4])+1, Integer.parseInt(fields[5])));

        feature.setTranscript_id(fields[1]);
        if ( fields[3].length()==1 && fields[3].charAt(0)=='+') feature.setStrand(1);
        else if ( fields[3].length()==1 && fields[3].charAt(0)=='-') feature.setStrand(-1);
        else throw new UserException.MalformedFile("Expected strand symbol (+/-), found: "+fields[3] + " for line=" + line);

        int coding_start = Integer.parseInt(fields[6])+1;
        int coding_stop = Integer.parseInt(fields[7]);

        if ( coding_start > coding_stop ) {
            if ( ! zero_coding_length_user_warned ) {
                Utils.warnUser("RefSeq file contains transcripts with zero coding length. "+
                        "Such transcripts will be ignored (this warning is printed only once)");
                zero_coding_length_user_warned = true;
            }
            return null;
        }

        feature.setTranscript_interval(genomeLocParser.createGenomeLoc(contig_name, Integer.parseInt(fields[4])+1, Integer.parseInt(fields[5])));
        feature.setTranscript_coding_interval(genomeLocParser.createGenomeLoc(contig_name, coding_start, coding_stop));
        feature.setGene_name(fields[12]);
        String[] exon_starts = fields[9].split(",");
        String[] exon_stops = fields[10].split(",");
        String[] eframes = fields[15].split(",");

        if ( exon_starts.length != exon_stops.length )
            throw new UserException.MalformedFile("Data format error: numbers of exon start and stop positions differ for line=" + line);
        if ( exon_starts.length != eframes.length )
            throw new UserException.MalformedFile("Data format error: numbers of exons and exon frameshifts differ for line=" + line);

        ArrayList<GenomeLoc> exons = new ArrayList<GenomeLoc>(exon_starts.length);
        ArrayList<Integer> exon_frames = new ArrayList<Integer>(eframes.length);

        for ( int i = 0 ; i < exon_starts.length  ; i++ ) {
            exons.add(genomeLocParser.createGenomeLoc(contig_name, Integer.parseInt(exon_starts[i])+1, Integer.parseInt(exon_stops[i]) ) );
            exon_frames.add(Integer.decode(eframes[i]));
        }

        feature.setExons(exons);
        feature.setExon_frames(exon_frames);
        return feature;
    }
}
