package com.novelbio.test;

import java.io.File;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

public class TestPicard {
	public static void main(String[] args) {
		String bam = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA.bam";
		File filebam = new File(bam);
		SAMFileReader samFileReader = new SAMFileReader(filebam);
		int i = 0;
		for (SAMRecord samRecord : samFileReader) {
			i++;
			if (i > 5) {
				break;
			}
			System.out.println(samRecord.getSAMString());
			System.out.println(samRecord.getReferenceName());
			System.out.println(samRecord.getAlignmentStart() + "\t" + samRecord.getAlignmentEnd());
			System.out.println(samRecord.getCigarString());
			System.out.println(samRecord.getMappingQuality());
			System.out.println(samRecord.getDuplicateReadFlag());
			System.out.println(samRecord.getAttribute("MD"));
			System.out.println(samRecord.getAttribute("RG"));
			System.out.println(samRecord.getReadNegativeStrandFlag() );
		}
		
	}
}
