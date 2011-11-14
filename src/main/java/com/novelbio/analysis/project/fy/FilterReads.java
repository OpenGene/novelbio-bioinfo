package com.novelbio.analysis.project.fy;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FilterReads {

	public static void main(String[] args) {
		FilterReads filterReads = new FilterReads();
		filterReads.filterReads();
	}
	
	private void filterReads()
	{
		String parentFileFQ = "/media/winE/NBC/Project/Project_FY_Lab/clean_reads/compress/";
		String parentFileOut = "/media/winE/NBC/Project/Project_FY_Lab/clean_reads/";
		String seqFile1 = parentFileFQ + "DT40_KO0h_L1_1.fq.gz";
		String seqFile2 = parentFileFQ + "DT40_KO0h_L1_2.fq.gz";
		String outFileFilter = parentFileOut + "DT40_KO0h_L1.fq";
		FastQ fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ = fastQ.filterReads(outFileFilter);
//		fastQMapBwa.setOutFileName(outFileMapping);
//		fastQMapBwa.mapReads();
		 seqFile1 = parentFileFQ + "DT40_KO5h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_KO5h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_KO5h_L1.fq.gz";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ = fastQ.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "DT40_WT0h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_WT0h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_WT0h_L1.fq.gz";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ = fastQ.filterReads(outFileFilter);
		
		 seqFile1 = parentFileFQ + "DT40_WT5h_L1_1.fq.gz";
		 seqFile2 = parentFileFQ + "DT40_WT5h_L1_2.fq.gz";
		 outFileFilter = parentFileOut + "DT40_WT5h_L1.fq.gz";
		fastQ = new FastQ(seqFile1, seqFile2, FastQ.FASTQ_ILLUMINA_OFFSET, FastQ.QUALITY_MIDIAN_PAIREND);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ = fastQ.filterReads(outFileFilter);
	}
	
	
	

}
