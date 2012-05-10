package com.novelbio.analysis.seq.chipseq;

import java.util.HashMap;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;

public abstract class PeakCalling {
	public static final String SPECIES_RICE = "os";
	public static final String SPECIES_HUMAN = "hs";
	public static final String SPECIES_C_ELEGAN = "ce";
	public static final String SPECIES_DROSOPHYLIA = "dm";
	public static final String SPECIES_MOUSE = "mm";
	public static final String SPECIES_ARABIDOPSIS = "ath";
	static HashMap<String, Long> hashSpecies2GenomeSize = new HashMap<String, Long>();
	static
	{
		hashSpecies2GenomeSize.put(SPECIES_HUMAN, 3095677436L);
		hashSpecies2GenomeSize.put(SPECIES_RICE, 372317579L);
		hashSpecies2GenomeSize.put(SPECIES_ARABIDOPSIS, 119667757L);
		hashSpecies2GenomeSize.put(SPECIES_MOUSE, 2725749214L);
	}
	double effectiveGenomeSize = 0.85;
	/** ������ļ� */
	String file = null;
	/** ���գ�һ����input control�������ļ�������ʵ�����ʽһ�� */
	String controlFile = null;
	/**����ļ�·����ǰ׺ */
	String outPrefix = "";
	public PeakCalling(String file) {
		this.file = file;
	}
	/** �ļ���ʽ��ʹ��ö��FileFormat 
	 * ��������˲�֧�ֵ��ļ���ʽ���򷵻�false
	 * */
	public abstract boolean setFileFormat(FormatSeq fileformat);
	/** ���֣����Ը�������ѡ����Ӧ�Ĳ�����һ���� genome size */
	public abstract void setSpecies(String species);
	/**����ļ�·����ǰ׺ */
	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}
	/** ���գ�һ����input control�������ļ�������ʵ�����ʽһ�� */
	public void setControlFile(String controlFile) {
		this.controlFile = controlFile;
	}
	/**
	 * Ĭ��85
	 * ��readsһ������ʱ������ⱥ��������reads������reads���ȵ����ƣ�uniqmapping��reads��������ȫ����ȫ�����飬һ����˵���Խ�����ǵ����Լ��
	 * ��ôһ��25bp����Ϊ65%��35bp����75%��50bp 80% 100bp���ܸ�������������Ϊ100����
	 * @param effectiveGenomeSize
	 */
	public void setEffectiveGenomeSize(int effectiveGenomeSize) {
		this.effectiveGenomeSize = (double)effectiveGenomeSize/100;
	}
	/**
	 * û��ʵ�֣���Ҫ���า��
	 * @param bedTreat ʵ��
	 * @param bedCol ����
	 * @param species ���֣�����effective genome size����hs��mm��dm��ce��os
	 * @param outFile Ŀ���ļ��У����ü�"/"
	 * @throws Exception 
	 */
	public abstract void peakCallling();
}
