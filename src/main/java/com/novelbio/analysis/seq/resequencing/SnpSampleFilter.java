package com.novelbio.analysis.seq.resequencing;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;


/** ��ͬ��������Snp���˹��򣬷��ϸù����snp�ᱻ��ѡ���� */
public class SnpSampleFilter {
	private static Logger logger = Logger.getLogger(SnpSampleFilter.class);
	
	/** �ж�Ϊref������reads�� */
	static int Ref_ReadsAllNumMin = 3;
	/** �ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	static int Ref_Contain_SnpNumMin = 2;
	/**�ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	static double Ref_Contain_SnpProp_Max = 0.05;
	
	/** �ж�ΪSnp Heto������reads�� */
	static int Snp_Hete_ReadsAllNumMin = 2;
	/** �ж�Ϊ Snp Heto������ ref reads��������������ֵ */
	static int Snp_Hete_Contain_RefNumMin = 2;
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	static double Snp_Hete_Contain_SnpProp_Min = 0.01;
	
	/** �ж�Ϊref������reads�� */
	static int Snp_Homo_ReadsAllNumMin = 3;
	/** �ж�ΪSnp�����е�snp��������С�ڸ���ֵ */
	static int Snp_Homo_Contain_SnpNumMin = 2;
	/**�ж�Ϊ����snp�����е�ref�������ô��ڸ���ֵ */
	static double Snp_Homo_Contain_SnpProp_Max = 0.05;

	
	
	HashSet<SampleDetail> setSampleFilterInfo = new HashSet<SampleDetail>();
	
	/**�������������Ϣ��ע���Сд */
	public void addSampleFilterInfo(SampleDetail sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** ����������Ϣ */
	public void clearSampleFilterInfo() {
		setSampleFilterInfo.clear();
	}
	/** 
	 * �������Ƿ�ͨ���ʼ�
	 * */
	public boolean isFilterdSnp(MapInfoSnpIndel mapInfoSnpIndel) {
		boolean isQualified = true;
		for (SampleDetail sampleDetail : setSampleFilterInfo) {
			sampleDetail.clearData();
			for (String sampleName : sampleDetail.lsSampleName) {
				mapInfoSnpIndel.setSampleName(sampleName);
				sampleDetail.addSnpIndelHomoHetoType(getSnpIndelType(mapInfoSnpIndel));
			}
			isQualified = sampleDetail.isQualified();
			if (!isQualified) {
				break;
			}
		}
		return isQualified;
	}
	/** ����֮ǰҪָ����������
	 * ��������snpindel����Ϣ */
	private SnpIndelHomoHetoType getSnpIndelType(MapInfoSnpIndel mapInfoSnpIndel) {
		SiteSnpIndelInfo siteSnpIndelInfo = mapInfoSnpIndel.getSiteSnpInfoBigAllen();
		return getSnpIndelType(mapInfoSnpIndel, siteSnpIndelInfo);
	}
	/** ����֮ǰҪָ����������
	 * ����ָ����snpindel����Ϣ */
	private SnpIndelHomoHetoType getSnpIndelType(MapInfoSnpIndel mapInfoSnpIndel, SiteSnpIndelInfo siteSnpIndelInfo) {
		int numSnpIndel = siteSnpIndelInfo.getReadsNum();
		int numAll = mapInfoSnpIndel.getReadsNumAll();
		int numRef = mapInfoSnpIndel.getReadsNumRef();
		if (numSnpIndel <= Ref_Contain_SnpNumMin && numAll >= Ref_ReadsAllNumMin 
				&& (double)numSnpIndel/numAll <= Ref_Contain_SnpProp_Max  )
		{
			return SnpIndelHomoHetoType.RefHomo;
		}
		else if (numRef >= Snp_Hete_Contain_RefNumMin && numAll >= Snp_Hete_ReadsAllNumMin 
				&& (double)numSnpIndel/numAll >= Snp_Hete_Contain_SnpProp_Min
				&& (double)numRef/numAll > Snp_Homo_Contain_SnpProp_Max   ) 
		{
				if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.INSERT || siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.DELETION) {
					return SnpIndelHomoHetoType.IndelHeto;
				}
				else if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.MISMATCH) {
					return SnpIndelHomoHetoType.SnpHeto;
				}
				else {
					logger.error("����δ֪snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
					return SnpIndelHomoHetoType.UnKnown;
				}
		}
		else if (numSnpIndel >= Snp_Homo_Contain_SnpNumMin && numAll >= Snp_Homo_ReadsAllNumMin 
				&& (double)numRef/numAll <= Snp_Homo_Contain_SnpProp_Max) {
			if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.INSERT || siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelHomo;
			}
			else if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpHomo;
			}
			else {
				logger.error("����δ֪snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
				return SnpIndelHomoHetoType.UnKnown;
			}
		}
		else if (numSnpIndel >= Snp_Homo_Contain_SnpNumMin && numAll >= Snp_Homo_ReadsAllNumMin 
				&& (double)numRef/numAll >= Snp_Hete_Contain_SnpProp_Min) {
			if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.INSERT || siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelUnKnown;
			}
			else if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpUnKonwn;
			}
			else {
				logger.error("����δ֪snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
				return SnpIndelHomoHetoType.UnKnown;
			}
		}
		else {
			return SnpIndelHomoHetoType.UnKnown;
		}
	}
	/** ��������SnpIndel���Ӻ������δ֪��snp�Ӻϣ�snp���ϣ�indel�Ӻϣ�indel���� �⼸��*/
	public static enum SnpIndelHomoHetoType {
		SnpHomo, SnpHeto, IndelHomo, IndelHeto, SnpUnKonwn, IndelUnKnown, RefHomo, UnKnown;
	}
}