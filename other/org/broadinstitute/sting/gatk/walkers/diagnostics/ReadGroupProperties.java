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

package org.broadinstitute.sting.gatk.walkers.diagnostics;

import net.sf.samtools.SAMReadGroupRecord;
import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.CommandLineGATK;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.report.GATKReport;
import org.broadinstitute.sting.gatk.report.GATKReportTable;
import org.broadinstitute.sting.gatk.walkers.ReadWalker;
import org.broadinstitute.sting.utils.Median;
import org.broadinstitute.sting.utils.help.DocumentedGATKFeature;
import org.broadinstitute.sting.utils.help.HelpConstants;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Emits a GATKReport containing read group, sample, library, platform, center, sequencing data,
 * paired end status, simple read type name (e.g. 2x76) median insert size and median read length
 * for each read group in every provided BAM file
 *
 * Note that this walker stops when all read groups have been observed at least a few thousand times so that
 * the median statistics are well determined.  It is safe to run it WG and it'll finish in an appropriate
 * timeframe.
 *
 * <h2>Input</h2>
 *  <p>
 *      Any number of BAM files
 *  </p>
 *
 * <h2>Output</h2>
 *  <p>
 *      GATKReport containing read group, sample, library, platform, center, median insert size and median read length.
 *
 *      For example, running this tool on the NA12878 data sets:
 *
 *      <pre>
 *      ##:GATKReport.v0.2 ReadGroupProperties : Table of read group properties
 *      readgroup  sample   library       platform  center  date     has.any.reads  is.paired.end  n.reads.analyzed  simple.read.type  median.read.length  median.insert.size
 *      20FUK.1    NA12878  Solexa-18483  illumina  BI      2/2/10   true           true                        498  2x101                            101                 386
 *      20FUK.2    NA12878  Solexa-18484  illumina  BI      2/2/10   true           true                        476  2x101                            101                 417
 *      20FUK.3    NA12878  Solexa-18483  illumina  BI      2/2/10   true           true                        407  2x101                            101                 387
 *      20FUK.4    NA12878  Solexa-18484  illumina  BI      2/2/10   true           true                        389  2x101                            101                 415
 *      20FUK.5    NA12878  Solexa-18483  illumina  BI      2/2/10   true           true                        433  2x101                            101                 386
 *      20FUK.6    NA12878  Solexa-18484  illumina  BI      2/2/10   true           true                        480  2x101                            101                 418
 *      20FUK.7    NA12878  Solexa-18483  illumina  BI      2/2/10   true           true                        450  2x101                            101                 386
 *      20FUK.8    NA12878  Solexa-18484  illumina  BI      2/2/10   true           true                        438  2x101                            101                 418
 *      20GAV.1    NA12878  Solexa-18483  illumina  BI      1/26/10  true           true                        490  2x101                            101                 391
 *      20GAV.2    NA12878  Solexa-18484  illumina  BI      1/26/10  true           true                        485  2x101                            101                 417
 *      20GAV.3    NA12878  Solexa-18483  illumina  BI      1/26/10  true           true                        460  2x101                            101                 392
 *      20GAV.4    NA12878  Solexa-18484  illumina  BI      1/26/10  true           true                        434  2x101                            101                 415
 *      20GAV.5    NA12878  Solexa-18483  illumina  BI      1/26/10  true           true                        479  2x101                            101                 389
 *      20GAV.6    NA12878  Solexa-18484  illumina  BI      1/26/10  true           true                        461  2x101                            101                 416
 *      20GAV.7    NA12878  Solexa-18483  illumina  BI      1/26/10  true           true                        509  2x101                            101                 386
 *      20GAV.8    NA12878  Solexa-18484  illumina  BI      1/26/10  true           true                        476  2x101                            101                 410                           101                 414
 *      </pre>
 *  </p>
 *
 * <h2>Examples</h2>
 *  <pre>
 *    java
 *      -jar GenomeAnalysisTK.jar
 *      -T ReadGroupProperties
 *      -I example1.bam -I example2.bam etc
 *      -R reference.fasta
 *      -o example.gatkreport.txt
 *  </pre>
 *
 * @author Mark DePristo
 */
@DocumentedGATKFeature( groupName = HelpConstants.DOCS_CAT_QC, extraDocs = {CommandLineGATK.class} )
public class ReadGroupProperties extends ReadWalker<Integer, Integer> {
    @Output
    public PrintStream out;

    @Argument(shortName="maxElementsForMedian", doc="Calculate median from the first maxElementsForMedian values observed", required=false)
    public int MAX_VALUES_FOR_MEDIAN = 10000;

    private final static String TABLE_NAME = "ReadGroupProperties";
    private final Map<String, PerReadGroupInfo> readGroupInfo = new HashMap<String, PerReadGroupInfo>();

    private class PerReadGroupInfo {
        public final Median<Integer> readLength = new Median<Integer>(MAX_VALUES_FOR_MEDIAN);
        public final Median<Integer> insertSize = new Median<Integer>(MAX_VALUES_FOR_MEDIAN);
        public int nReadsSeen = 0, nReadsPaired = 0;

        public boolean needsMoreData() {
            return ! readLength.isFull() || ! insertSize.isFull();
        }
    }

    @Override
    public void initialize() {
        for ( final SAMReadGroupRecord rg : getToolkit().getSAMFileHeader().getReadGroups() ) {
            readGroupInfo.put(rg.getId(), new PerReadGroupInfo());
        }
    }

    @Override
    public boolean filter(ReferenceContext ref, GATKSAMRecord read) {
        return ! (read.getReadFailsVendorQualityCheckFlag() || read.getReadUnmappedFlag());
    }

    @Override
    public boolean isDone() {
        for ( PerReadGroupInfo info : readGroupInfo.values() ) {
            if ( info.needsMoreData() )
                return false;
        }

        return true;
    }

    @Override
    public Integer map(ReferenceContext referenceContext, GATKSAMRecord read, RefMetaDataTracker RefMetaDataTracker) {
        final String rgID = read.getReadGroup().getId();
        final PerReadGroupInfo info = readGroupInfo.get(rgID);

        if ( info.needsMoreData() ) {
            info.readLength.add(read.getReadLength());
            info.nReadsSeen++;
            if ( read.getReadPairedFlag() ) {
                info.nReadsPaired++;
                if ( read.getInferredInsertSize() != 0) {
                    info.insertSize.add(Math.abs(read.getInferredInsertSize()));
                }
            }
        }

        return null;
    }

    @Override
    public Integer reduceInit() {
        return null;
    }

    @Override
    public Integer reduce(Integer integer, Integer integer1) {
        return null;
    }

    @Override
    public void onTraversalDone(Integer sum) {
        final GATKReport report = new GATKReport();
        report.addTable(TABLE_NAME, "Table of read group properties", 12);
        GATKReportTable table = report.getTable(TABLE_NAME);
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);

        table.addColumn("readgroup");
        //* Emits a GATKReport containing read group, sample, library, platform, center, median insert size and
        //* median read length for each read group in every BAM file.
        table.addColumn("sample", "%s");
        table.addColumn("library", "%s");
        table.addColumn("platform", "%s");
        table.addColumn("center", "%s");
        table.addColumn("date", "%s");
        table.addColumn("has.any.reads");
        table.addColumn("is.paired.end");
        table.addColumn("n.reads.analyzed", "%d");
        table.addColumn("simple.read.type", "%s");
        table.addColumn("median.read.length");
        table.addColumn("median.insert.size");

        for ( final SAMReadGroupRecord rg : getToolkit().getSAMFileHeader().getReadGroups() ) {
            final String rgID = rg.getId();
            table.addRowID(rgID, true);
            PerReadGroupInfo info = readGroupInfo.get(rgID);

            // we are paired if > 25% of reads are paired
            final boolean isPaired = info.nReadsPaired / (1.0 * (info.nReadsSeen+1)) > 0.25;
            final boolean hasAnyReads = info.nReadsSeen > 0;
            final int readLength = info.readLength.getMedian(0);

            setTableValue(table, rgID, "sample", rg.getSample());
            setTableValue(table, rgID, "library", rg.getLibrary());
            setTableValue(table, rgID, "platform", rg.getPlatform());
            setTableValue(table, rgID, "center", rg.getSequencingCenter());
            try {
                setTableValue(table, rgID, "date", rg.getRunDate() != null ? dateFormatter.format(rg.getRunDate()) : "NA");
            } catch ( NullPointerException e ) {
                // TODO: remove me when bug in Picard is fixed that causes NPE when date isn't present
                setTableValue(table, rgID, "date", "NA");
            }
            setTableValue(table, rgID, "has.any.reads", hasAnyReads);
            setTableValue(table, rgID, "is.paired.end", isPaired);
            setTableValue(table, rgID, "n.reads.analyzed", info.nReadsSeen);
            setTableValue(table, rgID, "simple.read.type", hasAnyReads ? String.format("%dx%d", isPaired ? 2 : 1, readLength) : "NA");
            setTableValue(table, rgID, "median.read.length", hasAnyReads ? readLength : "NA" );
            setTableValue(table, rgID, "median.insert.size", hasAnyReads && isPaired ? info.insertSize.getMedian(0) : "NA" );
        }

        report.print(out);
    }

    private final void setTableValue(GATKReportTable table, final String rgID, final String key, final Object value) {
        table.set(rgID, key, value == null ? "NA" : value);
    }
}
