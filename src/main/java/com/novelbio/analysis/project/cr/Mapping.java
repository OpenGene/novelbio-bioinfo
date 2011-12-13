package com.novelbio.analysis.project.cr;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class Mapping {
	
	public static void main(String[] args) {
		Mapping mapping = new Mapping();
		mapping.map();
	}
	
	private void map() {
		try {
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6dn.fq.gz";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
			fastQMapBwa.setAdaptorRight("CCAGTACTTCTCGTATGCCGTCTTCTGCTTGACGA");
			fastQMapBwa.setReadsLenMin(12);
			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa2.setAdapterParam(3, 2);
			fastQMapBwa2.setGapLength(3);
			fastQMapBwa2.setMapQ(12);
			BedSeq bedSeq = fastQMapBwa2.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" + fastQMapBwa.getSeqNum() +"\t" +fastQMapBwa2.getSeqNum()+"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6d.fq.gz";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
			fastQMapBwa.setAdaptorRight("TTAGATCAGGTCGTATGCCGTCTTCTGCTTGACG");
			fastQMapBwa.setReadsLenMin(12);
			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa2.setAdapterParam(3, 2);
			fastQMapBwa2.setGapLength(3);
			fastQMapBwa2.setMapQ(12);
			BedSeq bedSeq = fastQMapBwa2.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" + fastQMapBwa.getSeqNum() +"\t" +fastQMapBwa2.getSeqNum()+"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6dn.fq/HUMarbE_NS6d.fq.gz";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
			fastQMapBwa.setAdaptorRight("TGGACTACTGTCGTATGCCGTCTTCTGCTTGACT");
			fastQMapBwa.setReadsLenMin(12);
			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa2.setAdapterParam(3, 2);
			fastQMapBwa2.setGapLength(3);
			fastQMapBwa2.setMapQ(12);
			BedSeq bedSeq = fastQMapBwa2.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" + fastQMapBwa.getSeqNum() +"\t" +fastQMapBwa2.getSeqNum()+"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
