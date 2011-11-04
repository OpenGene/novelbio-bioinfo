package com.novelbio.analysis.project.hxw;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterReads {
	public static void main(String[] args) {
		FilterReads filterReads = new FilterReads();
		filterReads.filterReads();
	}
	
	private void filterReads()
	{
		String parentFileFQ = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/";
		String parentFileOut = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		String seqFile1 = parentFileFQ + "A_1.fastq.gz";
		String seqFile2 = parentFileFQ + "A_2.fastq.gz";
		String outFileFilter = parentFileFQ + "A_Filter.fq";
		String outFileMapping = parentFileOut + "A_BWA_map.sam";
		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
//		fastQMapBwa.setOutFileName(outFileMapping);
//		fastQMapBwa.mapReads();
		 seqFile1 = parentFileFQ + "B_1.fastq.gz";
		 seqFile2 = parentFileFQ + "B_2.fastq.gz";
		 outFileFilter = parentFileFQ + "B_Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "C_1.fastq.gz";
		 seqFile2 = parentFileFQ + "C_2.fastq.gz";
		 outFileFilter = parentFileFQ + "C_Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "D_1.fastq.gz";
		 seqFile2 = parentFileFQ + "D_2.fastq.gz";
		 outFileFilter = parentFileFQ + "D_Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		
	}
	
	
	
	
}
