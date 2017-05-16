package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.AbstractSAMHeaderRecord;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMProgramRecord;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMSequenceDictionary;

import java.util.HashSet;
import java.util.Set;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataStructure.ArrayOperate;

public class SamHeadCreater {
	SAMFileHeader samFileHeader = new SAMFileHeader();
	Set<String> setProgramId = new HashSet<>();
	public void setRefSeq(String refseq) {
		if (!refseq.endsWith(".fai")) {
			refseq = refseq + ".fai";
		}
		SAMSequenceDictionary samSequenceDictionary = SeqHash.getDictionaryFromFai(refseq);
		samFileHeader.setSequenceDictionary(samSequenceDictionary);
	}
	
	/**
	 * 输入这一行 @HD	VN:1.4	SO:coordinate
	 * @param attrLine
	 */
	public void setAttr(String attrLine) {
		//headline, 仅包含 VN项目和SO项目
		String[] ss = attrLine.split("\t");
		addAttr(samFileHeader, ss);
	}
	
	/**
	 * 输入这一行 @RG	ID:Shill64	PL:Illumina	LB:Shill64	SM:Shill64
	 * @param rgLine
	 */
	public void addReadGroup(String rgLine) {
		if (rgLine == null) return;
		
		if (!rgLine.startsWith("@RG")) {
			throw new ExceptionSamError("attrLine error, no @RG flag: " + rgLine);
		}
		SAMReadGroupRecord samReadGroupRecord = getSamReadGroupRecord(rgLine);
		samFileHeader.addReadGroup(samReadGroupRecord);
	}
	
	public static SAMReadGroupRecord getSamReadGroupRecord(String rgLine) {
		if (rgLine == null) return null;
		
		rgLine = rgLine.trim();
		if (!rgLine.startsWith("@RG")) {
			throw new ExceptionSamError("attrLine error, no @RG flag: " + rgLine);
		}
		rgLine = rgLine.replace("\\\\", "\\").replace("\\t", "\t");
		String[] ss = rgLine.split("\t");
		SAMReadGroupRecord samReadGroupRecord = new SAMReadGroupRecord(getId(ss));
		addAttr(samReadGroupRecord, ss);
		return samReadGroupRecord;
	}
	/**
	 * 输入这一行 @PG\tID:bwa\tPN:bwa\tVN:0.7.8-r455\tCL:bwa sampe -a 500 -P -n 10 -N 10 chrAll.fa Shill64_1.sai Shill64_2.sai Shill64_filtered_1.fq.gz Shill64_filtered_2.fq.gz
	 * @param rgLine
	 */
	public void addProgram(String pgLine) {
		if (pgLine == null) return;
		
		if (!pgLine.startsWith("@PG")) {
			throw new ExceptionSamError("attrLine error, no @RG flag: " + pgLine);
		}
		
		String[] ss = pgLine.split("\t");
		SAMProgramRecord samProgramRecord = new SAMProgramRecord(getId(ss));
		if (setProgramId.contains(samProgramRecord.getId())) {
			return;
		} else {
			setProgramId.add(samProgramRecord.getId());
		}		
		addAttr(samProgramRecord, ss);
		samFileHeader.addProgramRecord(samProgramRecord);
	}
	
	public SAMFileHeader generateHeader() {
		return samFileHeader;
	}
	
	private static String getId(String[] ss) {
		String id = null;
		for (String attrValue : ss) {
			String[] attr2Value = attrValue.split(":");
			if (attr2Value[0].equals("ID")) {
				id = attr2Value[1];
				break;
			}
		}
		if (id == null) {
			throw new ExceptionSamError("attrLine error, no ID tag: " + ArrayOperate.cmbString(ss, "\t"));
		}
		return id;
	}
	
	private static void addAttr(AbstractSAMHeaderRecord record, String[] ss) {
		for (String attrValue : ss) {
			if (attrValue.startsWith("@")) continue;
			
			String[] attr2Value = attrValue.split(":");
			if (attr2Value[0].equals("ID")) continue;
			
			record.setAttribute(attr2Value[0], attr2Value[1]);
		}
	}
}
