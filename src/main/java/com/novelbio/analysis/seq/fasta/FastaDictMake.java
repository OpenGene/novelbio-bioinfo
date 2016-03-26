package com.novelbio.analysis.seq.fasta;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.StringUtil;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.fastq.ExceptionFastq;
import com.novelbio.base.fileOperate.FileOperate;

public class FastaDictMake {
	String reference;
	String outDict;
	
    public Path REFERENCE;
    public Path OUTPUT;
    
	private final MessageDigest md5;
	
    /** Make sequence name the first word from the > line in the fasta file.
     * By default the entire contents of the > line is used, excluding leading and trailing whitespace */
    private boolean TRUNCATE_NAMES_AT_WHITESPACE = true;
    
   /** Stop after writing this many sequences.  For testing. */
    private int NUM_SEQUENCES = Integer.MAX_VALUE;
    
    /** Put into AS field of sequence dictionary entry if supplied */
    private String GENOME_ASSEMBLY;
    
    /** Put into SP field of sequence dictionary entry */
    private String SPECIES;
    
    /** Put into UR field of sequence dictionary entry.  If not supplied, input reference file is used */
    public String URI;
    
	public FastaDictMake(String reference, String outDict) {
		this.reference = reference;
		this.outDict = outDict;
		
		  try {
	            md5 = MessageDigest.getInstance("MD5");
	        } catch (NoSuchAlgorithmException e) {
	            throw new ExceptionFastq("MD5 algorithm not found", e);
	        }
	}
	
	public FastaDictMake(String reference) {
		this.reference = reference;
		this.outDict = FileOperate.changeFileSuffix(reference, "", "dict");
		
		  try {
	            md5 = MessageDigest.getInstance("MD5");
	        } catch (NoSuchAlgorithmException e) {
	            throw new ExceptionFastq("MD5 algorithm not found", e);
	        }
	}
	
    /**
     * Do the work after command line has been parsed.
     * RuntimeException may be thrown by this method, and are reported appropriately.
     *
     * @return program exit status.
     */
	public boolean makeDict() {
		if (!FileOperate.isFileExistAndBigThanSize(reference, 0)) {
			return false;
		}
		if (FileOperate.isFileExistAndBigThanSize(outDict, 0)) {
			return true;
		}
		FileOperate.delFile(outDict);
		REFERENCE = FileOperate.getPath(reference);
		OUTPUT = FileOperate.getPath(outDict);
		try {
			doWork();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

    /**
     * Do the work after command line has been parsed.
     * RuntimeException may be thrown by this method, and are reported appropriately.
     *
     * @return program exit status.
     */
    protected int doWork() {
        if (FileOperate.isFileExistAndNotDir(OUTPUT)) {
            throw new ExceptionFastq(OUTPUT.toString() +
                    " already exists.  Delete this file and try again, or specify a different output file.");
        }
        final SAMSequenceDictionary sequences = makeSequenceDictionary(REFERENCE);
        final SAMFileHeader samHeader = new SAMFileHeader();
        samHeader.setSequenceDictionary(sequences);
        try {
            final SAMFileWriter samWriter = new SAMFileWriterFactory().makeSAMWriter(samHeader, false, FileOperate.getOutputStream(OUTPUT));
            samWriter.close();
            return 0;  
        } catch (Exception e) {
        	  return -1;
        }

    }


    /**
     * Read all the sequences from the given reference file, and convert into SAMSequenceRecords
     * @param referenceFile fasta or fasta.gz
     * @return SAMSequenceRecords containing info from the fasta, plus from cmd-line arguments.
     */
	SAMSequenceDictionary makeSequenceDictionary(final Path referenceFile) {
        final ReferenceSequenceFile refSeqFile =
                ReferenceSequenceFileFactory.getReferenceSequenceFile(referenceFile, TRUNCATE_NAMES_AT_WHITESPACE);
        ReferenceSequence refSeq;
        final List<SAMSequenceRecord> ret = new ArrayList<SAMSequenceRecord>();
        final Set<String> sequenceNames = new HashSet<String>();
        for (int numSequences = 0; numSequences < NUM_SEQUENCES && (refSeq = refSeqFile.nextSequence()) != null; ++numSequences) {
            if (sequenceNames.contains(refSeq.getName())) {
                throw new ExceptionSeqFasta("Sequence name appears more than once in reference: " + refSeq.getName());
            }
            sequenceNames.add(refSeq.getName());
            ret.add(makeSequenceRecord(refSeq));
        }
        return new SAMSequenceDictionary(ret);
    }

    /**
     * Create one SAMSequenceRecord from a single fasta sequence
     */
    private SAMSequenceRecord makeSequenceRecord(final ReferenceSequence refSeq) {
        final SAMSequenceRecord ret = new SAMSequenceRecord(refSeq.getName(), refSeq.length());

        // Compute MD5 of upcased bases
        final byte[] bases = refSeq.getBases();
        for (int i = 0; i < bases.length; ++i) {
                bases[i] = StringUtil.toUpperCase(bases[i]);
            }

        ret.setAttribute(SAMSequenceRecord.MD5_TAG, md5Hash(bases));
        if (GENOME_ASSEMBLY != null) {
            ret.setAttribute(SAMSequenceRecord.ASSEMBLY_TAG, GENOME_ASSEMBLY);
        }
        ret.setAttribute(SAMSequenceRecord.URI_TAG, URI);
        if (SPECIES != null) {
                ret.setAttribute(SAMSequenceRecord.SPECIES_TAG, SPECIES);
            }
        return ret;
    }

    private String md5Hash(final byte[] bytes) {
        md5.reset();
        md5.update(bytes);
        String s = new BigInteger(1, md5.digest()).toString(16);
        if (s.length() != 32) {
            final String zeros = "00000000000000000000000000000000";
            s = zeros.substring(0, 32 - s.length()) + s;
        }
        return s;
    }
}
