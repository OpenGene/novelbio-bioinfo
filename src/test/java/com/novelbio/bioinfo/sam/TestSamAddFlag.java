package com.novelbio.bioinfo.sam;

import java.io.IOException;
import java.util.ArrayList;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqFastaReader;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;
import com.novelbio.bioinfo.sam.SamToBam.SamToBamOutFile;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.SAMFileHeader.SortOrder;

public class TestSamAddFlag {
public static void main(String[] args) {
//	String inFile = "/run/media/novelbio/A/tmp/mock_S_a.hisat.sam";
// 	String outFile = "/run/media/novelbio/A/tmp/mock_S_a.bowtie2.bam";
////  	String inFile = args[0];
////  	String outFile = args[1];
//	
// 	SamFile samFileIn = new SamFile(inFile);
////
// 	SamFileStatistics samFileStatistics = new SamFileStatistics("test");
//  	samFileStatistics.setCorrectChrReadsNum(true);
// 	samFileStatistics.setStandardData(samFileIn.getMapChrID2Length());
//  	samFileStatistics.initial();

//	
// 	boolean isAddMultiFlag = true;
// 	SamAddMultiFlag samAddMultiFlag = new SamAddMultiFlag();
	String inBwaUniqueFile = "/run/media/novelbio/A/tmp/tophat_hisat_mapsplice/A_hisat.addFlag.sam"; 
	String inBowtie2File = "/run/media/novelbio/A/tmp/tophat_hisat_mapsplice/A_mapsplice_mapsplice.sam";
	String outBowtieMutiMappedFile = "/run/media/novelbio/A/tmp/tophat_hisat_mapsplice/A_hisatVSmapsplice_mapsplicespecific.sam";
	SamFile samFileIn = new SamFile(inBwaUniqueFile);
	String readID = "";
	long i = 0;
//	TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outBowtieMutiMappedFile, true);
	ArrayList<String> lsSamRead = new ArrayList<>();
	for (SamRecord samRecord : samFileIn.readLines()) {
//		samFileStatistics.addAlignRecord(samRecord);
		String name = samRecord.getName();
		Integer mappedNum = samRecord.getMappingNum();	
		if (!name.equals(readID)) { 
			if (mappedNum>=1) {
				readID = name;
			}
			lsSamRead.add(name);
			
		}
		
//		if (mappedNum>=1) {
//			i++;
////			txtReadandWrite.writefileln(samRecord.toString());
//		}
//		System.out.println("name is " + name + "\t" + mappedNum + "\t" + samRecord.getRefID());
//		addRecordToLsRecorders(samRecord);
//		samWriteTo.write(samRecord);
	}
//	System.out.println(i);
//	txtReadandWrite.close();
	SamFile samBowtie2FileIn = new SamFile(inBowtie2File);
	TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outBowtieMutiMappedFile, true);
//	ArrayList<String> lsBowtie2SamRead = new ArrayList<>();
	for (SamRecord bowtie2SamRecord : samBowtie2FileIn.readLines()) {		
		String bowtieReadName = bowtie2SamRecord.getName();
		Integer mappedNum = bowtie2SamRecord.getMappingNum();	
		String ref = bowtie2SamRecord.getRefID();
		if (!(lsSamRead.contains(bowtieReadName)) && (mappedNum>0) ) { 
			txtReadandWrite.writefileln(bowtie2SamRecord.toString());
		} else {
			
		}
		
	}
	txtReadandWrite.close();
	System.out.println("Finish!");
//	
	
//	System.out.println(samFileStatistics.getReadsNum(MappingReadsType.All));
	
	
//	String inFaFile = "/media/nbfs/nbCloud/public/AllProject/project_5525df20e4b0a6b11a8c13aa/task_55debfb060b29e0f388880a5/other_result/Puccinia_striiformis_inte_V4.fa";
//	String changeFaFile = "/media/nbfs/nbCloud/public/AllProject/project_5525df20e4b0a6b11a8c13aa/task_55debfb060b29e0f388880a5/other_result/Puccinia_striiformis_inte_V4_change.fa";
//	SeqFastaReader seqFastaReader = new SeqFastaReader(inFaFile);
//	TxtReadandWrite faFileReadandWrite = new TxtReadandWrite(changeFaFile, true);
//	int i = 0;
//	for (SeqFasta seqFasta : seqFastaReader.readlines()) {
////		if (i++>2) {
////			break;
////		}
////		System.out.println(seqFasta.getSeqName());
////		System.out.println(seqFasta.toString());
//		faFileReadandWrite.writefileln(">" + seqFasta.getSeqName());
//		faFileReadandWrite.writefileln(seqFasta.toString());
//	}
//	
	}



}
