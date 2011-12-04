package com.novelbio.analysis.project.fy;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterReads {

	public static void main(String[] args) {
		FilterReads filterReads = new FilterReads();
		filterReads.filterReadsLH();
	}
	
	private void filterReadsZXX()
	{
		String parentFileFQ = "/media/winE/NBC/Project/Project_FY_Lab/clean_reads/compress/";
		String parentFileOut = "/media/winE/NBC/Project/Project_FY_Lab/clean_reads/";
		String seqFile1 = parentFileFQ + "DT40_KO0h_L1_1.fq.gz";
		String seqFile2 = parentFileFQ + "DT40_KO0h_L1_2.fq.gz";
		String outFileFilter = parentFileOut + "DT40_KO0h_trimEnd_min_70";
		FastQ fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setReadsLenMin(70);
		fastQ.setTrimNNN(true);
		fastQ = fastQ.filterReads(outFileFilter);
//		fastQMapBwa.setOutFileName(outFileMapping);
//		fastQMapBwa.mapReads();
		 seqFile1 = parentFileFQ + "DT40_KO5h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_KO5h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_KO5h_trimEnd_min_70";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "DT40_WT0h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_WT0h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_WT0h_trimEnd_min_70";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "DT40_WT5h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_WT5h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_WT5h_trimEnd_min_70";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
	}
	
	private void filterReadsLH()
	{
		String parentFileFQ = "/media/新加卷/NBC/Project/Project_FY/冯英组小鼠测序数据20111122/";
		String parentFileOut = "/media/新加卷/NBC/Project/Project_FY/冯英组小鼠测序数据20111122/filteredReads/";
		String seqFile1 = parentFileFQ + "mouse heart rna seq/clean read/WT_L1_1.fq.gz";
		String seqFile2 = parentFileFQ + "mouse heart rna seq/clean read/WT_L1_2.fq.gz";
		String outFileFilter = parentFileOut + "MMU_Heart_WT_L1_trimEnd_min_70.fq";
		FastQ fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setReadsLenMin(70);
		fastQ.setTrimNNN(true);
		fastQ = fastQ.filterReads(outFileFilter);
//		fastQMapBwa.setOutFileName(outFileMapping);
//		fastQMapBwa.mapReads();
		seqFile1 = parentFileFQ + "mouse heart rna seq/clean read/KO_L1_1.fq.gz";
		seqFile2 = parentFileFQ + "mouse heart rna seq/clean read/KO_L1_2.fq.gz";
		outFileFilter = parentFileOut +  "MMU_Heart_KO_L1_trimEnd_min_70.fq";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		seqFile1 = parentFileFQ + "MEF rna seq/clean reads/WT0d_L1_1.fq.gz";
		seqFile2 = parentFileFQ + "MEF rna seq/clean reads/WT0d_L1_2.fq.gz";
		outFileFilter = parentFileOut + "MMU_MEF_WT0d_L1_trimEnd_min_70.fq";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		seqFile1 = parentFileFQ + "MEF rna seq/clean reads/WT2d_L1_1.fq.gz";
		seqFile2 = parentFileFQ + "MEF rna seq/clean reads/WT2d_L1_2.fq.gz";
		outFileFilter = parentFileOut + "MMU_MEF_WT2d_L1_trimEnd_min_70.fq";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		seqFile1 = parentFileFQ + "MEF rna seq/clean reads/KO0d_L1_1.fq.gz";
		seqFile2 = parentFileFQ + "MEF rna seq/clean reads/KO0d_L1_2.fq.gz";
		outFileFilter = parentFileOut + "MMU_MEF_KO0d_L1_trimEnd_min_70.fq";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
		
		seqFile1 = parentFileFQ + "MEF rna seq/clean reads/KO2d_L1_1.fq.gz";
		seqFile2 = parentFileFQ + "MEF rna seq/clean reads/KO2d_L1_2.fq.gz";
		outFileFilter = parentFileOut + "MMU_MEF_KO2d_L1_trimEnd_min_70.fq";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ.setTrimNNN(true);
		fastQ.setReadsLenMin(70);
		fastQ = fastQ.filterReads(outFileFilter);
	}
	

}
