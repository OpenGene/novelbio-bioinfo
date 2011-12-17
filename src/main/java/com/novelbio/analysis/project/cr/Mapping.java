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
			String seqFileRaw = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6d.fq.gz";
			FastQ fastQ = new FastQ(seqFileRaw, FastQ.QUALITY_MIDIAN);
			fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6d.fq_filter.fq";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
//			fastQMapBwa.setAdaptorRight("CCAGTACTTCTCGTATGCCGTCTTCTGCTTGACGA");
//			fastQMapBwa.setReadsLenMin(12);
//			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa.setAdapterParam(3, 2);
			fastQMapBwa.setGapLength(3);
			fastQMapBwa.setMapQ(5);
			BedSeq bedSeq = fastQMapBwa.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" +fastQ.getSeqNum()+"\t" + fastQMapBwa.getSeqNum() +"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String seqFileRaw = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6dn.fq.gz";
			FastQ fastQ = new FastQ(seqFileRaw, FastQ.QUALITY_MIDIAN);
			fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_E6dn.fq_filter.fq";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
//			fastQMapBwa.setAdaptorRight("CCAGTACTTCTCGTATGCCGTCTTCTGCTTGACGA");
//			fastQMapBwa.setReadsLenMin(12);
//			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa.setAdapterParam(3, 2);
			fastQMapBwa.setGapLength(3);
			fastQMapBwa.setMapQ(5);
			BedSeq bedSeq = fastQMapBwa.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" +fastQ.getSeqNum()+"\t" + fastQMapBwa.getSeqNum() +"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String seqFileRaw = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_NS6d.fq.gz";
			FastQ fastQ = new FastQ(seqFileRaw, FastQ.QUALITY_MIDIAN);
			fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
			String seqFile1 = "/media/winE/NBC/Project/RNA-Seq_CR_20111201/HUMarbE_NS6d.fq_filter.fq";
	 		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_MIDIAN, FileOperate.changeFileSuffix(seqFile1, "_map", "sam"), true);
	 		fastQMapBwa.setCompressType(TxtReadandWrite.TXT, TxtReadandWrite.TXT);
			fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_refseq/RefSeqFromChr.fa");
//			fastQMapBwa.setAdaptorRight("CCAGTACTTCTCGTATGCCGTCTTCTGCTTGACGA");
//			fastQMapBwa.setReadsLenMin(12);
//			FastQMapBwa fastQMapBwa2 = fastQMapBwa.filterReads(FileOperate.changeFileSuffix(seqFile1, "_filter", "fq"));
			fastQMapBwa.setAdapterParam(3, 2);
			fastQMapBwa.setGapLength(3);
			fastQMapBwa.setMapQ(5);
			BedSeq bedSeq = fastQMapBwa.getBedFileSE( FileOperate.changeFileSuffix(seqFile1, "_map", "bed"));
			bedSeq.sortBedFile(FileOperate.changeFileSuffix(seqFile1, "_map_Sorted", "bed"));
			TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile1, "_statistic", "txt"),true);
			txtOut.writefileln(seqFile1 + "\t" +fastQ.getSeqNum()+"\t" + fastQMapBwa.getSeqNum() +"\t"+ bedSeq.getSeqNum());
			txtOut.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
