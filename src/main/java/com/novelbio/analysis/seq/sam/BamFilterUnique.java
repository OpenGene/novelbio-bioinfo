package com.novelbio.analysis.seq.sam;

import net.sf.samtools.SAMFileHeader;

import com.novelbio.base.fileOperate.FileOperate;


/** 提取unique mapped reads */
public class BamFilterUnique {
	static final String isUniqueMapped = "isUniqueMapped";
	static final String suffix = "_uniqueMap";
	SamFile samFile;
	SamFile samFilteredFile;
	String outFile;
	
	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
	}
	/** 过滤后的文件名 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	public SamFile filterUniqueReads() {
		if (samFile == null) {
			throw new SamErrorException("no outfile name exist!");
		}
		String outResult = outFile;
		if (outFile == null) {
			outResult = samFile.getFileName();
			if (outResult == null) {
				throw new SamErrorException("no outfile name exist!");
			}
			outResult = FileOperate.changeFileSuffix(outResult, suffix, null);
		}
		SAMFileHeader header = samFile.getHeader();
		setAttributeUnique(header);
		samFilteredFile = new SamFile(outResult, header);
		for (SamRecord samRecord : samFile.readLines()) {
			if (samRecord.isUniqueMapping()) {
				samFilteredFile.writeSamRecord(samRecord);
			}
		}
		samFilteredFile.close();
		return samFilteredFile;
	}
	
	public static void setAttributeUnique(SAMFileHeader header) {
		header.setAttribute(isUniqueMapped, "true");
	}
	
	public static boolean isUniqueMapped(SamFile samFile) {
		String fileName = samFile.getFileName();
		if (fileName != null && fileName.contains(suffix)) {
			return true;
		}
		String value = samFile.getHeader().getAttribute(isUniqueMapped);
		return "true".equalsIgnoreCase(value) ? true : false;
	}
}
