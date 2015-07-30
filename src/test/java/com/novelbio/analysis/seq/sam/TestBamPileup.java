package com.novelbio.analysis.seq.sam;

public class TestBamPileup {
	public static void main(String[] args) {
		BamPileup bamPileup = new BamPileup();
		bamPileup.setBamFile("/home/novelbio/NBCsource/test/pileup/stet_sort.bam");
		bamPileup.setMapQuality(20);
		bamPileup.setReferenceFile("/media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa");
		bamPileup.pileup();
	}
}
