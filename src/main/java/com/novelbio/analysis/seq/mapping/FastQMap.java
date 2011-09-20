package com.novelbio.analysis.seq.mapping;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;

public class FastQMap implements FastQMapInt{
	private static final Logger logger = Logger.getLogger(FastQMap.class);
	
	FastQMapAbs fastQMap;
	
	
	public static final String MAPPING_BWA = "bwa";
	public static final String MAPPING_SOAP = "soap";
	
	/**
	 * 
	 * @param mappingType
	 * @param seqFile1
	 * @param seqFile2
	 * @param FastQFormateOffset
	 * @param QUALITY
	 * @param outFile
	 * @param uniqMapping 是否为uniqMapping，只有当单端测序才有
	 */
	public FastQMap(String mappingType, String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY,String outFile,boolean uniqMapping) {
		if (mappingType.equals(FastQMap.MAPPING_BWA)) {
			fastQMap = new FastQMapBwa(seqFile1, seqFile2, FastQFormateOffset, QUALITY,outFile,uniqMapping);
		}
		else if (mappingType.equals(FastQMap.MAPPING_SOAP)) {
			fastQMap = new FastQMapSoap(seqFile1, seqFile2, FastQFormateOffset, QUALITY, outFile,uniqMapping);
		}
	}
	
	public FastQMap(String mappingType, FastQ fastQ,String outFile,boolean uniqMapping) {
		if (mappingType.equals(FastQMap.MAPPING_BWA)) {
			fastQMap = new FastQMapBwa(fastQ,outFile,uniqMapping);
		}
		else if (mappingType.equals(FastQMap.MAPPING_SOAP)) {
			fastQMap = new FastQMapSoap(fastQ,outFile,uniqMapping);
		}
	}
	
	
	private FastQMap(FastQMapAbs fasqMap) {
		this.fastQMap = fasqMap;
	}
	/**
	 * 过滤低质量reads
	 * @param fileFilterOut 返回新的文件
	 * @return
	 */
	public FastQMap filterReads(String fileFilterOut) {
		return new FastQMap(fastQMap.filterReads(fileFilterOut));
	}
	
	
	@Override
	public void setExtendTo(int extendTo) {
		fastQMap.setExtendTo(extendTo);
		
	}

	@Override
	public void setInsertSize(int minInsertLen, int maxInsertLen) {
		fastQMap.setInsertSize(minInsertLen, maxInsertLen);
	}

	@Override
	public void setFilePath(String exeFile, String chrFile) {
		fastQMap.setFilePath(exeFile, chrFile);
	}

	@Override
	public void mapReads() {
		fastQMap.mapReads();
	}

	@Override
	public BedSeq getBedFile(String bedFile) {
		return fastQMap.getBedFile(bedFile);
	}

	@Override
	public BedSeq getBedFileSE(String bedFile) {
		return fastQMap.getBedFileSE(bedFile);
	}
	
}
