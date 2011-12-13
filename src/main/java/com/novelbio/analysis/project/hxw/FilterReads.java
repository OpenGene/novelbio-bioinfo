package com.novelbio.analysis.project.hxw;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterReads {
	public static void main(String[] args) {
		FilterReads filterReads = new FilterReads();
//		filterReads.filterReads();
		filterReads.mappingSOAP();
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
	
	private void mappingBWA() {
		String parentFileFQ = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/";
		String parentFileOut = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/";
		String sampleGroup = "";
		/////////////////////////////////// bwa ////////////////////////////////////////////////////
		
		String seqFile1 = parentFileFQ + "A_Filter.fq";
		String outFileMapping = parentFileOut + "A_BWA_map_R.sam";
		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
//		sampleGroup = "@RG\tID:ExomeA\tLB:ExomeA\tSM:ExomeA\tPL:ILLUMINA";
//		fastQMapBwa.setSampleGroup(sampleGroup);
//		fastQMapBwa.setGapLength(10);
//		fastQMapBwa.mapReads();
		
		
//		seqFile1 = parentFileFQ + "B_Filter.fq";
//		outFileMapping = parentFileOut + "B_BWA_map_R.sam";
//		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
//		sampleGroup = "@RG\tID:ExomeB\tLB:ExomeB\tSM:ExomeB\tPL:ILLUMINA";
//		fastQMapBwa.setSampleGroup(sampleGroup);
//		fastQMapBwa.setGapLength(10);
//		fastQMapBwa.mapReads();

		
		seqFile1 = parentFileFQ + "C_Filter.fq";
		outFileMapping = parentFileOut + "C_1_BWA_map_R.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		sampleGroup = "@RG\tID:ExomeC\tLB:ExomeC\tSM:ExomeC\tPL:ILLUMINA";
		fastQMapBwa.setSampleGroup(sampleGroup);
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

		seqFile1 = parentFileFQ + "D_Filter.fq";
		outFileMapping = parentFileOut + "D_BWA_map_R.sam";
		fastQMapBwa = new FastQMapBwa(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa");
		sampleGroup = "@RG\tID:ExomeD\tLB:ExomeD\tSM:ExomeD\tPL:ILLUMINA";
		fastQMapBwa.setSampleGroup(sampleGroup);
		fastQMapBwa.setGapLength(10);
		fastQMapBwa.mapReads();

	}
	
	private void mappingSOAP() {
		String parentFileFQ = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/";
		String parentFileOut = "/media/winF/NBC/Project/Project_HXW/mappingsoap/";
		
		/////////////////////////////////// bwa ////////////////////////////////////////////////////
		
		String seqFile1 = parentFileFQ + "A_Filter.fq";
		String outFileMapping = parentFileOut + "A_SOAP_map.sam";
		FastQMapSoap fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
//		fastQMapBwa.setMisMatch(3);
//		fastQMapBwa.mapReads();
//		
//		
//		seqFile1 = parentFileFQ + "A_2Filter.fq";
//		outFileMapping = parentFileOut + "A_2_SOAP_map.sam";
//		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
//		fastQMapBwa.setMisMatch(3);
//		fastQMapBwa.mapReads();
//
//		
//		seqFile1 = parentFileFQ + "B_1Filter.fq";
//		outFileMapping = parentFileOut + "B_1_SOAP_map.sam";
//		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
//		fastQMapBwa.setMisMatch(3);
//		fastQMapBwa.mapReads();
//
//		seqFile1 = parentFileFQ + "B_2Filter.fq";
//		outFileMapping = parentFileOut + "B_2_SOAP_map.sam";
//		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
//		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
//		fastQMapBwa.setMisMatch(3);
//		fastQMapBwa.mapReads();
		seqFile1 = parentFileFQ + "A_Filter.fq";
		outFileMapping = parentFileOut + "A_SOAP_map.txt";
		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setMisMatch(3);
		fastQMapBwa.mapReads();

		
		seqFile1 = parentFileFQ + "B_Filter.fq";
		outFileMapping = parentFileOut + "B_SOAP_map.txt";
		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setMisMatch(3);
		fastQMapBwa.mapReads();

		seqFile1 = parentFileFQ + "C_Filter.fq";
		outFileMapping = parentFileOut + "C_SOAP_map.txt";
		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setMisMatch(3);
		fastQMapBwa.mapReads();
		
		seqFile1 = parentFileFQ + "D_Filter.fq";
		outFileMapping = parentFileOut + "D_SOAP_map.txt";
		fastQMapBwa = new FastQMapSoap(seqFile1,  FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN, outFileMapping, true);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/soap_chromFa/UCSC_hg19.fa");
		fastQMapBwa.setMisMatch(3);
		fastQMapBwa.mapReads();

	}
	
	
}
