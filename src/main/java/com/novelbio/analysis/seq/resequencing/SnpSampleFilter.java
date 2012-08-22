package com.novelbio.analysis.seq.resequencing;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;


/** ��ͬ��������Snp���˹��򣬷��ϸù����snp�ᱻ��ѡ���� */
public class SnpSampleFilter {
	private static Logger logger = Logger.getLogger(SnpSampleFilter.class);
	
	/** �ж�Ϊ����snp������reads�� */
	static int Snp_Homo_ReadsAllNumMin = 3;
	/** �ж�ΪSnp�����е�ref�������ô��ڸ���ֵ */
	static int Snp_Homo_Contain_RefNumMax = 2;
	/**�ж�Ϊ����snp�����е�ref�������ô��ڸ���ֵ */
	static double Snp_Homo_Contain_RefProp_Max = 0.02;
	
	/** �ж�Ϊ�Ӻ�snp HetoMore������reads�� */
	static int Snp_HetoMore_ReadsAllNumMin = 3;
	/** �ж�ΪSnp�����е�snp��������С�ڸ���ֵ */
	static int Snp_HetoMore_Contain_SnpNumMin = 2;
	/**�ж�Ϊ����snp�����е�ref�������ô��ڸ���ֵ */
	static double Snp_HetoMore_Contain_RefProp_Max = 0.1;
	
	/** �ж�ΪSnp Heto������reads�� */
	static int Snp_Heto_ReadsAllNumMin = 3;
	/** �ж�Ϊ Snp Heto������ ref reads��������������ֵ */
	static int Snp_Hete_Contain_RefNumMin = 1;
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	static double Snp_Hete_Contain_SnpProp_Min = 0.1;
	/** �ж�Ϊsnp Heto�����е�ref��������С�ڸ���ֵ */
	static double Snp_Hete_Contain_RefProp_Min = 0.1;
	
	
	/** �ж�Ϊref������reads�� */
	static int Ref_ReadsAllNumMin = 10;
	/** �ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	static int Ref_Contain_SnpNumMin = 2;
	/**�ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	static double Ref_Contain_SnpProp_Max = 0.02;
	
	/** �ж�ΪSnp HetoLess������reads�� */
	static int Snp_HetoLess_ReadsAllNumMin = 3;
	/** �ж�Ϊ Snp HetoLess������ ref reads��������������ֵ */
	static int Snp_HetoLess_Contain_RefNumMin = 2;
	/**�ж�Ϊsnp HetoLess�����е�ref�������ô��ڸ���ֵ */
	static double Snp_HetoLess_Contain_SnpProp_Max = 0.1;
	
	HashSet<SampleDetail> setSampleFilterInfo = new HashSet<SampleDetail>();
	
	/**�������������Ϣ��ע���Сд */
	public void addSampleFilterInfo(SampleDetail sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** ����������Ϣ */
	public void clearSampleFilterInfo() {
		setSampleFilterInfo.clear();
	}
	public boolean isFilterdSnp(MapInfoSnpIndel mapInfoSnpIndel) {
		if (getFilterdSnp(mapInfoSnpIndel) != null) {
			return true;
		}
		return false;
	}
	/** 
	 * �������Ƿ�ͨ���ʼ�
	 * ���ͨ���ʼ��ˣ��ͷ���ͨ���ʼ���Ǹ�snp����
	 * ���򷵻�null
	 * */
	public SiteSnpIndelInfo getFilterdSnp(MapInfoSnpIndel mapInfoSnpIndel) {
		SiteSnpIndelInfo siteSnpIndelInfoResult = null;
		boolean isQualified = true;
		for (SiteSnpIndelInfo siteSnpIndelInfo : mapInfoSnpIndel.getLsAllenInfoSortBig2Small()) {
			siteSnpIndelInfoResult = siteSnpIndelInfo;
			for (SampleDetail sampleDetail : setSampleFilterInfo) {
				sampleDetail.clearData();
				for (String sampleName : sampleDetail.lsSampleName) {
					siteSnpIndelInfo.setSampleName(sampleName);
					mapInfoSnpIndel.setSampleName(sampleName);
					sampleDetail.addSnpIndelHomoHetoType(getSnpIndelType(mapInfoSnpIndel, siteSnpIndelInfo));
				}
				isQualified = sampleDetail.isQualified();
				if (!isQualified) {
					break;
				}
			}
			if (isQualified) {
				return siteSnpIndelInfoResult;
			}
		}
		//����֮�����ж�һ��
		if (isQualified) {
			return siteSnpIndelInfoResult;
		}
		return null;
	}
	/** ����֮ǰҪָ����������
	 * ����ָ����snpindel����Ϣ */
	private SnpIndelHomoHetoType getSnpIndelType(MapInfoSnpIndel mapInfoSnpIndel, SiteSnpIndelInfo siteSnpIndelInfo) {
		int numSnpIndel = siteSnpIndelInfo.getReadsNum();
		int numAll = mapInfoSnpIndel.getReadsNumAll();
		
		//��Ϊ�����б�Ļ����ͣ����������ַ�ʽ�������зǱ�snp��λ�㶼����Ϊrefλ�㡣
		int numRef = numAll - numSnpIndel;//mapInfoSnpIndel.getReadsNumRef();
		return getSnpIndelType(siteSnpIndelInfo.getSnpIndelType(), numSnpIndel, numRef, numAll);
	}
	/**
	 * public ����Junit���� 
	 * ����֮ǰҪָ����������
	 * ����ָ����snpindel����Ϣ */
	public SnpIndelHomoHetoType getSnpIndelType(SnpIndelType siteSnpIndelInfo, int numSnpIndel, int numRef, int numAll) {
		if (numAll >= Snp_Homo_ReadsAllNumMin && numRef <= Snp_Homo_Contain_RefNumMax
			&& (double)numRef/numAll <= Snp_Homo_Contain_RefProp_Max ) 
		{
			if (siteSnpIndelInfo == SnpIndelType.INSERT || siteSnpIndelInfo == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelHomo;
			}
			else if (siteSnpIndelInfo == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpHomo;
			}
		}
		else if (numSnpIndel >= Snp_HetoMore_Contain_SnpNumMin && numAll >= Snp_HetoMore_ReadsAllNumMin 
				&& ((double)numRef/numAll <= Snp_HetoMore_Contain_RefProp_Max || numRef == 1) )//ֻ��1��ref����˵������ 
		{
			if (siteSnpIndelInfo == SnpIndelType.INSERT || siteSnpIndelInfo == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelHetoMore;
			}
			else if (siteSnpIndelInfo == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpHetoMore;
			}
		}
		//���ж��������ж��Ӻ�С
		else if (numSnpIndel <= Ref_Contain_SnpNumMin && numAll >= Ref_ReadsAllNumMin 
				&& (double)numSnpIndel/numAll <= Ref_Contain_SnpProp_Max  )
		{
			return SnpIndelHomoHetoType.RefHomo;
		}
		else if (numRef >= Snp_HetoLess_Contain_RefNumMin && numAll >= Snp_HetoLess_ReadsAllNumMin
				&& ((double)numSnpIndel/numAll <= Snp_HetoLess_Contain_SnpProp_Max || numSnpIndel == 1 )//ֻ��1��snp����˵������ 
				) {
			if (siteSnpIndelInfo == SnpIndelType.INSERT || siteSnpIndelInfo == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelHetoLess;
			}
			else if (siteSnpIndelInfo == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpHetoLess;
			}
		}
		else if (numRef >= Snp_Hete_Contain_RefNumMin && numAll >= Snp_Heto_ReadsAllNumMin 
				&& (double)numSnpIndel/numAll >= Snp_Hete_Contain_SnpProp_Min
				&& (double)numRef/numAll > Snp_Hete_Contain_RefProp_Min   )
		{
				if (siteSnpIndelInfo == SnpIndelType.INSERT || siteSnpIndelInfo == SnpIndelType.DELETION) {
					return SnpIndelHomoHetoType.IndelHeto;
				}
				else if (siteSnpIndelInfo == SnpIndelType.MISMATCH) {
					return SnpIndelHomoHetoType.SnpHeto;
				}
		}

		return SnpIndelHomoHetoType.UnKnown;
	}
	
	/** ��������SnpIndel���Ӻ������δ֪��snp�Ӻϣ�snp���ϣ�indel�Ӻϣ�indel���� �⼸��*/
	public static enum SnpIndelHomoHetoType {
		SnpHomo, SnpHetoMore, SnpHeto, SnpHetoLess, IndelHomo, IndelHetoMore, IndelHeto, IndelHetoLess, RefHomo, UnKnown;
	}
}