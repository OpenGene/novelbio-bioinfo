package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;

public interface MapDNA {
	
	/** �����Ѿ����˺õ�fastq�ļ� */
	public void setFqFile(FastQ leftFq, FastQ rightFq);
	
	/**
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 */
	public void setOutFileName(String outFileName);
	/**
	 * �ٷ�֮���ٵ�mismatch�����߼���mismatch
	 * @param mismatch
	 */
	public void setMismatch(double mismatch);
	
	public void setChrFile(String chrFile);
	/**
	 * �趨bwa���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePath);
	
	/** �߳�������Ĭ��4�߳� */
	public void setThreadNum(int threadNum);

	public void setMapLibrary(MapLibrary mapLibrary);
	/**
	 * ����mapping���飬���в����������пո�
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform);
	/**
	 * Ĭ��gapΪ4�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	public void setGapLength(int gapLength);

	/**
	 * �����趨��������solid
	 */
	public SamFile mapReads();
}
