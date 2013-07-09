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

package org.broadinstitute.sting.gatk.io.storage;

import net.sf.samtools.util.BlockCompressedOutputStream;
import org.apache.log4j.Logger;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureCodec;
import org.broadinstitute.sting.gatk.io.stubs.VariantContextWriterStub;
import org.broadinstitute.sting.gatk.refdata.tracks.FeatureManager;
import org.broadinstitute.variant.bcf2.BCF2Utils;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.exceptions.UserException;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.variantcontext.writer.Options;
import org.broadinstitute.variant.variantcontext.writer.VariantContextWriter;
import org.broadinstitute.variant.variantcontext.writer.VariantContextWriterFactory;

import java.io.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Provides temporary and permanent storage for genotypes in VCF format.
 *
 * @author mhanna
 * @version 0.1
 */
public class VariantContextWriterStorage implements Storage<VariantContextWriterStorage>, VariantContextWriter {
    /**
     * our log, which we want to capture anything from this class
     */
    private static Logger logger = Logger.getLogger(VariantContextWriterStorage.class);

    private final static int BUFFER_SIZE = 1048576;

    protected final File file;
    protected OutputStream stream;
    protected final VariantContextWriter writer;
    boolean closed = false;

    /**
     * Constructs an object which will write directly into the output file provided by the stub.
     * Intentionally delaying the writing of the header -- this should be filled in by the walker.
     * @param stub Stub to use when constructing the output file.
     */
    public VariantContextWriterStorage(VariantContextWriterStub stub)  {
        if ( stub.getOutputFile() != null ) {
            this.file = stub.getOutputFile();
            writer = vcfWriterToFile(stub,stub.getOutputFile(),true);
        }
        else if ( stub.getOutputStream() != null ) {
            this.file = null;
            this.stream = stub.getOutputStream();
            writer = VariantContextWriterFactory.create(stream,
                    stub.getMasterSequenceDictionary(), stub.getWriterOptions(false));
        }
        else
            throw new ReviewedStingException("Unable to create target to which to write; storage was provided with neither a file nor a stream.");
    }

    /**
     * Constructs an object which will redirect into a different file.
     * @param stub Stub to use when synthesizing file / header info.
     * @param tempFile File into which to direct the output data.
     */
    public VariantContextWriterStorage(VariantContextWriterStub stub, File tempFile) {
        //logger.debug("Creating temporary output file " + tempFile.getAbsolutePath() + " for VariantContext output.");
        this.file = tempFile;
        this.writer = vcfWriterToFile(stub, file, false);
        writer.writeHeader(stub.getVCFHeader());
    }

    /**
     * common initialization routine for multiple constructors
     * @param stub Stub to use when constructing the output file.
     * @param file Target file into which to write VCF records.
     * @param indexOnTheFly true to index the file on the fly.  NOTE: will be forced to false for compressed files.
     * @return A VCF writer for use with this class
     */
    private VariantContextWriter vcfWriterToFile(VariantContextWriterStub stub, File file, boolean indexOnTheFly) {
        try {
            if ( stub.isCompressed() )
                stream = new BlockCompressedOutputStream(file);
            else
                stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE));
        }
        catch(IOException ex) {
            throw new UserException.CouldNotCreateOutputFile(file, "Unable to open target output stream", ex);
        }

        // The GATK/Tribble can't currently index block-compressed files on the fly.  Disable OTF indexing even if the user explicitly asked for it.
        EnumSet<Options> options = stub.getWriterOptions(indexOnTheFly);
        VariantContextWriter writer = VariantContextWriterFactory.create(file, this.stream, stub.getMasterSequenceDictionary(), options);

        // if the stub says to test BCF, create a secondary writer to BCF and an 2 way out writer to send to both
        // TODO -- remove me when argument generateShadowBCF is removed
        if ( stub.alsoWriteBCFForTest() && ! VariantContextWriterFactory.isBCFOutput(file, options)) {
            final File bcfFile = BCF2Utils.shadowBCF(file);
            if ( bcfFile != null ) {
                VariantContextWriter bcfWriter = VariantContextWriterFactory.create(bcfFile, stub.getMasterSequenceDictionary(), options);
                writer = new TestWriter(writer, bcfWriter);
            }
        }

        return writer;
    }

    private final static class TestWriter implements VariantContextWriter {
        final List<VariantContextWriter> writers;

        private TestWriter(final VariantContextWriter ... writers) {
            this.writers = Arrays.asList(writers);
        }

        @Override
        public void writeHeader(final VCFHeader header) {
            for ( final VariantContextWriter writer : writers ) writer.writeHeader(header);
        }

        @Override
        public void close() {
            for ( final VariantContextWriter writer : writers ) writer.close();
        }

        @Override
        public void add(final VariantContext vc) {
            for ( final VariantContextWriter writer : writers ) writer.add(vc);
        }
    }

    public void add(VariantContext vc) {
        if ( closed ) throw new ReviewedStingException("Attempting to write to a closed VariantContextWriterStorage " + vc.getStart() + " storage=" + this);
        writer.add(vc);
    }

    /**
     * initialize this VCF header
     *
     * @param header  the header
     */
    public void writeHeader(VCFHeader header) {
        writer.writeHeader(header);
    }

    /**
     * Close the VCF storage object.
     */
    public void close() {
        writer.close();
        closed = true;
    }

    public void mergeInto(VariantContextWriterStorage target) {
        try {
            if ( ! closed )
                throw new ReviewedStingException("Writer not closed, but we are merging into the file!");
            final String targetFilePath = target.file != null ? target.file.getAbsolutePath() : "/dev/stdin";
            logger.debug(String.format("Merging VariantContextWriterStorage from %s into %s", file.getAbsolutePath(), targetFilePath));

            // use the feature manager to determine the right codec for the tmp file
            // that way we don't assume it's a specific type
            final FeatureManager.FeatureDescriptor fd = new FeatureManager().getByFiletype(file);
            if ( fd == null )
                throw new UserException.LocalParallelizationProblem(file);

            final FeatureCodec<VariantContext> codec = fd.getCodec();
            final AbstractFeatureReader<VariantContext> source =
                    AbstractFeatureReader.getFeatureReader(file.getAbsolutePath(), codec, false);
            
            for ( final VariantContext vc : source.iterator() ) {
                target.writer.add(vc);
            }

            source.close();
            file.delete(); // this should be last to aid in debugging when the process fails
        } catch (IOException e) {
            throw new UserException.CouldNotReadInputFile(file, "Error reading file in VCFWriterStorage: ", e);
        }
    }
}
