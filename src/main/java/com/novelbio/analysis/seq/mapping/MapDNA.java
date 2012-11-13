package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public abstract class MapDNA {
	
	/** �����Ѿ����˺õ�fastq�ļ� */
	public abstract void setFqFile(FastQ leftFq, FastQ rightFq);
	
	/**
	 * @param outFileName ����ļ�������׺�Զ���Ϊsam
	 */
	public abstract void setOutFileName(String outFileName);
	/**
	 * �ٷ�֮���ٵ�mismatch�����߼���mismatch
	 * @param mismatch
	 */
	public abstract void setMismatch(double mismatch);
	
	public abstract void setChrFile(String chrFile);
	/**
	 * �趨bwa���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public abstract void setExePath(String exePath);
	
	/** �߳�������Ĭ��4�߳� */
	public abstract void setThreadNum(int threadNum);

	public abstract void setMapLibrary(MapLibrary mapLibrary);
	/**
	 * ����mapping���飬���в����������пո�
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public abstract void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform);
	/**
	 * Ĭ��gapΪ4�������indel���ҵĻ������õ�5����6�ȽϺ���
	 * @param gapLength
	 */
	public abstract void setGapLength(int gapLength);

	/**
	 * �����趨��������solid
	 */
	public abstract SamFile mapReads();
	
	/**
	 * Ŀǰֻ��bwa��bowtie2����
	 * @param softMapping
	 * @return
	 */
	public static MapDNA creatMapDNA(SoftWare softMapping) {
		MapDNA mapSoftware = null;
		if (softMapping == SoftWare.bwa) {
			mapSoftware = new MapBwa();
		} else if (softMapping == SoftWare.bowtie2) {
			mapSoftware = new MapBowtie(softMapping);
		}
		return mapSoftware;
	}
}
