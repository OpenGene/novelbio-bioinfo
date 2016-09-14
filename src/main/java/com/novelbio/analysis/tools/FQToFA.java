package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uk.ac.babraham.FastQC.Sequence.BAMFile;

import com.novelbio.analysis.seq.denovo.N50statistics;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FQToFA {
	FastQ fqLeft, fqRight;
	Iterator<FastQRecord[]> itFqPE;
	Iterator<FastQRecord> itFqSE;
	public static void main(String[] args) {
		
		
		String faFile = "/home/novelbio/bianlianle/project/yuanzheng/GCA_000715075.1_Ntab-K326_genomic_modify.fa";
//		String faFileResult = "/home/novelbio/bianlianle/test/getresult.fa";
		SeqFastaHash seqFastaHash = new SeqFastaHash (faFile);
		ArrayList<String[]> liGeneLeng = seqFastaHash.getChrLengthInfo();
//	
		String lengthFile = "/home/novelbio/bianlianle/project/yuanzheng/K326_genome_Chrlength.txt";
		TxtReadandWrite write = new TxtReadandWrite(lengthFile,true);
		String bamFilePath = "";
//		BAMFile bamFile = new BAMFile(FileOperate.getFile(bamFilePath) false);
		
//	HashMap<String, Integer> maGeneLength = new HashMap<>();
//	String trinityFile = "/home/novelbio/bianlianle/tmp/Hap-2trinity.Trinity.fasta";
//
//	FQToFA fqToFA = new FQToFA();
//	fqToFA.copeAfterAssembly(trinityFile);
//	for (String[] content:liGeneLeng) {
//	
//		write.writefileln(content);
//	}
//	write.writefile(liGeneLeng);
//	write.close();
//	for(int i=0; i<liGeneLeng.size();i++) {
//		String[] arrayStrings = liGeneLeng.get(i);
//	}	
//	write.close();
//		SeqFasta subSeqFasta = seq.getSubSeq(1, 4, true);
		
//		System.out.println("all seq " + seq.toString());
//		System.out.println("subSeqFasta seq " + subSeqFasta.toString());
		
//		SeqFastaHash seqFastaHashResult = new SeqFastaHash (faFileResult);
		
//		seqFastaHashResult.writeToFile(name);
//		seqFastaHah.g
//		SeqFasta SeqFasta= new SeqFasta();
//		SeqFasta.
		
		
	}
	
	public ArrayList<String> getLsGeneName(){
		ArrayList<String> lsGeneName = new  ArrayList();
		
		return lsGeneName;
	}
	public void initial() {
		if (fqRight != null) {
			itFqPE =  fqLeft.readlinesPE(fqRight).iterator();
		} else {
			itFqSE = fqLeft.readlines().iterator();
		}
	}
	public void FQToFA() {
		
	}
	public void setFastqLeft(String fastqLeft) {
		this.fqLeft = new FastQ(fastqLeft);
		
//		fqLeftModify = new FastQ(FileOperate.changeFileSuffix(fastqLeft, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	public void setFastqRight(String fastqRight) {
		this.fqRight = new FastQ(fastqRight);
//		fqRightModify = new FastQ(FileOperate.changeFileSuffix(fastqRight, "_remove", "fq.gz|fq|fastq|fastq.gz", null), true);
	}
	
	
	private void copeAfterAssembly(String trinityFile) {
		if (!FileOperate.isFileExistAndBigThanSize(trinityFile, 0)) {
			return;
		}
//		N50AndSeqLen n50Statistics = new N50AndSeqLen(trinityFile);
		N50statistics n50Statistics = new N50statistics(trinityFile);
		n50Statistics.doStatistics();
		//TODＯ 这里需要自动化生成图表
//		n50AndSeqLen.setLengthStep(500);
//		n50AndSeqLen.setMaxContigLen(10000);
//		HistList histList = n50AndSeqLen.gethListLength();
//		histList.getPlotHistBar(new BarStyle()).saveToFile("/media/hdfs/nbCloud/public/Testforzong/reads Len distribution2.png", 1000, 1000);
//
//		BufferedImage img = histList.getPlotHistBar("SeqStatistics", "Reads Length", "Reads Num").createBufferedImage(1200, 1000);
//		ImageUtils.saveBufferedImage(img, "/media/hdfs/nbCloud/public/Testforzong/reads Len distribution.png");
		n50Statistics.getLsNinfo();
		String faStatResult = trinityFile + ".stat.xls";
		TxtReadandWrite txtWrite = new TxtReadandWrite(faStatResult, true);
		int allContigsNum = n50Statistics.getAllContigsNum();
		long allContigsLen = n50Statistics.getAllContigsLen();
		int minContigsLen = n50Statistics.getRealMinConLen();
		int maxContigsLen = n50Statistics.getRealMaxConLen();
		int averageLen = n50Statistics.getLenAvg();
		int N50Len = n50Statistics.getN50Len();
		int medianLen = n50Statistics.getMedianLen();
		txtWrite.writefileln("Number of contigs\t" + allContigsNum + "\nNumber of characters(bp)\t" + allContigsLen + "\nAverage Length(bp)\t" + averageLen + "\nMinimum Contigs Length\t" + minContigsLen + "\nMaximum Contigs Length\t" + maxContigsLen + "\nN50 Length\t" + N50Len + "\nMedian Length\t" + medianLen);
		txtWrite.close();
		
		//因为最后需要再做一次聚类，所以这里就不做聚类了
//		TrinityCopeIso trinityCopeIso = new TrinityCopeIso();
//		trinityCopeIso.setInFileName(trinityFile);
//		trinityCopeIso.setOutTrinityGeneFile(FileOperate.changeFileSuffix(trinityFile, "_Gene", "fa"));
//		trinityCopeIso.setOutTrinityIsoFile(FileOperate.changeFileSuffix(trinityFile, "_Iso", "fa"));
//		trinityCopeIso.removeTmpFile();
//		trinityCopeIso.cope();
	}
}
