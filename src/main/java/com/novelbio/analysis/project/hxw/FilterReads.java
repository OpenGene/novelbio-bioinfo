package com.novelbio.analysis.project.hxw;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterReads {
	public static void main(String[] args) {
		FilterReads filterReads = new FilterReads();
//		filterReads.filterReads();
		filterReads.mapping();
	}
	
	private void filterReads()
	{
		String parentFileFQ = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/";
		String parentFileOut = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		String seqFile1 = parentFileFQ + "A_1.fastq.gz";
		String outFileFilter = parentFileFQ + "A_1Filter.fq";
		String outFileMapping = parentFileOut + "BWA_map.sam";
		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
//		fastQMapBwa.setOutFileName(outFileMapping);
//		fastQMapBwa.mapReads();
		 seqFile1 = parentFileFQ + "B_1.fastq.gz";
		 outFileFilter = parentFileFQ + "B_1Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "C_1.fastq.gz";
		 outFileFilter = parentFileFQ + "C_1Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "D_1.fastq.gz";
		 outFileFilter = parentFileFQ + "D_1Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "A_2.fastq.gz";
		 outFileFilter = parentFileFQ + "A_2Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "B_2.fastq.gz";
		 outFileFilter = parentFileFQ + "B_2Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "C_2.fastq.gz";
		 outFileFilter = parentFileFQ + "C_2Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "D_2.fastq.gz";
		 outFileFilter = parentFileFQ + "D_2Filter.fq";
		fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(6);
		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQMapBwa = fastQMapBwa.filterReads(outFileFilter);
		
	}
	
	private void mapping() {
		String parentFileFQ = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/";
		String parentFileOut = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		
		/////////////////////////////////// bwa ////////////////////////////////////////////////////
		
		String seqFile1 = parentFileFQ + "A_1Filter.fq";
		String outFileMapping = parentFileOut + "A_1_BWA_map.sam";
		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();
		
		
		seqFile1 = parentFileFQ + "A_2Filter.fq";
		outFileMapping = parentFileOut + "A_2_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		
		seqFile1 = parentFileFQ + "B_1Filter.fq";
		outFileMapping = parentFileOut + "B_1_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		seqFile1 = parentFileFQ + "B_2Filter.fq";
		outFileMapping = parentFileOut + "B_2_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		
		seqFile1 = parentFileFQ + "C_1Filter.fq";
		outFileMapping = parentFileOut + "C_1_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		seqFile1 = parentFileFQ + "C_2Filter.fq";
		outFileMapping = parentFileOut + "C_2_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();
		
		seqFile1 = parentFileFQ + "D_1Filter.fq";
		outFileMapping = parentFileOut + "D_1_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		seqFile1 = parentFileFQ + "D_2Filter.fq";
		outFileMapping = parentFileOut + "D_2_BWA_map.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();


	}
	
	
	
}
