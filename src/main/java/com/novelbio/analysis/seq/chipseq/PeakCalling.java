package com.novelbio.analysis.seq.chipseq;

import java.util.HashMap;

import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAnno;
import com.novelbio.database.model.species.Species;

public abstract class PeakCalling {

	static HashMap<String, Long> hashSpecies2GenomeSize = new HashMap<String, Long>();
	double effectiveGenomeSize = 0.85;
	/** ������ļ� */
	String file = null;
	/** ���գ�һ����input control�������ļ�������ʵ�����ʽһ�� */
	String controlFile = null;
	/**����ļ�·����ǰ׺ */
	String outPrefix = "";
	Species species;
	
	
	public PeakCalling(String file) {
		this.file = file;
	}
	/**
	 * �趨�ļ�
	 * @param file
	 */
	public void setFile(String file) {
		this.file = file;
	}
	/** ���֣����Ը�������ѡ����Ӧ�Ĳ�����һ���� genome size */
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** ���֣����Ը�������ѡ����Ӧ�Ĳ�����һ���� genome size */
	public void setSpecies(int taxID) {
		this.species = new Species(taxID);
	}
	/** �ļ���ʽ��ʹ��ö��FileFormat 
	 * ��������˲�֧�ֵ��ļ���ʽ���򷵻�false
	 * */
	public abstract boolean setFileFormat(FormatSeq fileformat);
	/**����ļ�·����ǰ׺ */
	public void setOutPathPrefix(String outPrefix) {
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
