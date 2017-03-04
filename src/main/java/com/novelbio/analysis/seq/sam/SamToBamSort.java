package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMSequenceDictionary;

import java.io.InputStream;
import java.util.List;

import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutFile;

public class SamToBamSort {
	SamToBam samToBam = new SamToBam();
	SamToBamOutFile samToBamOutFile = new SamToBamOutFile();
	String rgLine;
	
	/** 需要转化成的bam文件名，自动从sam文件判定是否为双端，会关闭Sam流
	 * @param outFileName
	 * @param samFileSam
	 */
	public SamToBamSort(String outFileName, SamFile samFileSam) {
		samToBam.setInFile(samFileSam);
		samToBamOutFile.setOutFileName(outFileName);
		samToBam.setIsPairend(samFileSam.isPairend());
	}
	/** 需要转化成的bam文件名 */
	public SamToBamSort(String outFileName, SamFile samFileSam, boolean isPairend) {
		samToBam.setInFile(samFileSam);
		samToBam.setIsPairend(isPairend);
		samToBamOutFile.setOutFileName(outFileName);
	}
	/** 需要转化成的bam文件名 */
	public SamToBamSort(String outFileName, InputStream inStream, boolean isPairend) {
		samToBam.setInStream(inStream);
		samToBam.setIsPairend(isPairend);
		samToBamOutFile.setOutFileName(outFileName);
	}
	/** 需要转化成的bam文件名 */
	public SamToBamSort(String outFileName, InputStream inStream) {
		samToBam.setInStream(inStream);
		samToBamOutFile.setOutFileName(outFileName);
	}
	
	/**
	 * 手工添加RGGroup
	 * 部分软件，譬如bowtie，没有添加RGGroup的命令，
	 * 那么我们可以在这里添加，然后直接写入bam文件
	 */
	public void setRgLine(String rgLine) {
		this.rgLine = rgLine;
	}
	
	/** 是否写入bam文件，默认写入
	 * 有时候mapping但不需要写入文件，譬如过滤掉rrna reads的时候，
	 * 只需要将没有mapping的reads输出即可，并不需要要把bam文件输出
	 * @param writeToBam
	 */
	public void setWriteToBam(boolean writeToBam) {
		samToBamOutFile.setWriteOut(writeToBam);
	}
	/** 是否需要排序，默认false */
	public void setNeedSort(boolean isNeedSort) {
		samToBamOutFile.setNeedSort(isNeedSort);
	}
	/** 是否根据samSequenceDictionary重新排列samHeader中的顺序，目前只有mapsplice才遇到 */
	public void setSamSequenceDictionary(SAMSequenceDictionary samSequenceDictionary) {
		samToBam.setSamSequenceDictionary(samSequenceDictionary);
	}

	/**
	 * 设定是否添加比对到多处的标签，暂时仅适用于bowtie2
	 * bwa不需要设定该参数
	 * @param addMultiHitFlag
	 */
	public void setAddMultiHitFlag(boolean addMultiHitFlag) {
		samToBam.setIsAddMultiFlag(addMultiHitFlag);
	}
	
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		samToBam.setLsAlignmentRecorders(lsAlignmentRecorders);
	}

	/**
	 * 开始转换
	 * 转换结束后，关闭输出的bam文件，并关闭输入的sam文件
	 */
	public void convert() {
		samToBam.setRgLine(rgLine);
		samToBam.setSamWriteTo(samToBamOutFile);
		samToBam.readInputStream();
		samToBam.writeToOs();
	}
	
	/** 返回转换好的bam文件 */
	public SamFile getSamFileBam() {
		return samToBamOutFile.getSamFileBam();
	}

}
