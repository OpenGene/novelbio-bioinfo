package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import sun.tools.tree.AddExpression;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;


/** ��ͬ��������Snp���˹��򣬷��ϸù����snp�ᱻ��ѡ���� */
public class SnpFilter {
	private static Logger logger = Logger.getLogger(SnpFilter.class);
	
	/** �ж�Ϊ����snp������reads�� */
	int Snp_Homo_ReadsAllNumMin = 3;
	/** �ж�ΪSnp�����е�ref�������ô��ڸ���ֵ */
	int Snp_Homo_Contain_RefNumMax = 2;
	/**�ж�Ϊ����snp�����е�ref�������ô��ڸ���ֵ */
	double Snp_Homo_Contain_RefProp_Max = 0.04;
	
	/** �ж�Ϊ�Ӻ�snp HetoMore������reads�� */
	int Snp_HetoMore_ReadsAllNumMin = 3;
	/** �ж�ΪSnp�����е�snp��������С�ڸ���ֵ */
	int Snp_HetoMore_Contain_SnpNumMin = 2;
	/**�ж�Ϊ����snp�����е�snp��������С���ڸ���ֵ */
	double Snp_HetoMore_Contain_SnpProp_Min = 0.7;
	
	/** �ж�ΪSnp Heto������reads�� */
	int Snp_Heto_ReadsAllNumMin = 3;
	/** �ж�Ϊ Snp Heto������ ref reads��������������ֵ */
	int Snp_Hete_Contain_RefNumMin = 1;
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	double Snp_Hete_Contain_SnpProp_Min = 0.1;
	/** �ж�Ϊsnp Heto�����е�ref��������С�ڸ���ֵ */
	double Snp_Hete_Contain_RefProp_Min = 0.1;
	
	
	/** �ж�Ϊref������reads�� */
	int Ref_ReadsAllNumMin = 10;
	/** �ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	int Ref_Contain_SnpNumMin = 2;
	/**�ж�Ϊref�����е�snp�������ô��ڸ���ֵ */
	double Ref_Contain_SnpProp_Max = 0.04;
	
	/** �ж�ΪSnp HetoLess������reads�� */
	int Snp_HetoLess_ReadsAllNumMin = 3;
	/** �ж�Ϊ Snp HetoLess������ ref reads��������������ֵ */
	int Snp_HetoLess_Contain_RefNumMin = 2;
	
	//����ֵͬSnp_Hete_Contain_SnpProp_Min
	/**�ж�Ϊsnp HetoLess�����е�ref�������ô��ڸ���ֵ */
//	static double Snp_HetoLess_Contain_SnpProp_Max = 0.1;
	
	HashSet<SnpGroupFilterInfo> setSampleFilterInfo = new HashSet<SnpGroupFilterInfo>();

	public void setSnp_HetoMore_Contain_SnpProp_Min(
			double snp_HetoMore_Contain_SnpProp_Min) {
		Snp_HetoMore_Contain_SnpProp_Min = snp_HetoMore_Contain_SnpProp_Min;
	}
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	public void setSnp_Hete_Contain_SnpProp_Min(
			double snp_Hete_Contain_SnpProp_Min) {
		Snp_Hete_Contain_SnpProp_Min = snp_Hete_Contain_SnpProp_Min;
	}
	
	/**�������������Ϣ��ע���Сд */
	public void addSampleFilterInfo(SnpGroupFilterInfo sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** ����������Ϣ */
	public void clearSampleFilterInfo() {
		setSampleFilterInfo.clear();
	}
	public boolean isFilterdSnp(MapInfoSnpIndel mapInfoSnpIndel) {
		if (getFilterdSnp(mapInfoSnpIndel).size() > 0) {
			return true;
		}
		return false;
	}
	/** 
	 * �������Ƿ�ͨ���ʼ�
	 * ���ͨ���ʼ��ˣ��ͷ���ͨ���ʼ���Ǹ�snp����
	 * ���򷵻ؿյ�list
	 * */
	public ArrayList<SiteSnpIndelInfo> getFilterdSnp(MapInfoSnpIndel mapInfoSnpIndel) {
		ArrayList<SiteSnpIndelInfo> lsSnpFiltered = new ArrayList<SiteSnpIndelInfo>();
		boolean isQualified = true;
		for (SiteSnpIndelInfo siteSnpIndelInfo : mapInfoSnpIndel.getLsAllenInfoSortBig2Small()) {
			for (SnpGroupFilterInfo sampleDetail : setSampleFilterInfo) {
				sampleDetail.clearData();
				for (String sampleName : sampleDetail.lsSampleName) {
					siteSnpIndelInfo.setSampleName(sampleName);
					mapInfoSnpIndel.setSampleName(sampleName);
					sampleDetail.addSnpIndelHomoHetoType(getSnpIndelType(mapInfoSnpIndel, siteSnpIndelInfo));
				}
				//ֻҪ��һ������û��ͨ�����飬������
				isQualified = sampleDetail.isQualified();
				if (!isQualified) 
					break;
			}
			if (isQualified) {
				lsSnpFiltered.add(siteSnpIndelInfo);
			}
		}
		return lsSnpFiltered;
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
				&& ((double)numSnpIndel/numAll >= Snp_HetoMore_Contain_SnpProp_Min || numRef == 1) )//ֻ��1��ref����˵������ 
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
				&& ((double)numSnpIndel/numAll < Snp_Hete_Contain_SnpProp_Min && numSnpIndel >= 1 )//ֻ��1��snp����˵������ 
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
}

/**
 * ������������������ʾ���ο������Ը���
 * ����������������½磬��Ϊ���ڵ���
 * ������Ͻ磬��ΪС��
 * @author zong0jie
 *
 */
class FilterUnit {
	ArrayList<FilterSmallRegion> lsCompareInfo = new ArrayList<FilterSmallRegion>();
	String UnitName;
	
	/**
	 * �趨�Ƚϵ�����
	 * @param filterSmallRegion
	 */
	public void addRegionInfo(FilterSmallRegion filterSmallRegion) {
		lsCompareInfo.add(filterSmallRegion);
	}
	
	/** �ж��Ƿ����ڸ������� */
	public boolean isInThisUnit(int numSnpIndel, int numRef, int numAll) {
		if (Ref_Num_Max > 0 ) {
			
		}
		
		return false;
	}
	
}
/** �Ƚϵ���С��Ԫ */
class FilterSmallRegion {
	String info;
	/**
	 * �±߽�<br>
	 *  true: ��ʾ������Сֵ��>=<br>
	 * false: ��ʾ������Сֵ�� >
	 *  */
	boolean minBound = true;
	/**
	 * �ϱ߽�<br>
	 *  true: ��ʾ�������ֵ��< = <br>
	 * false: ��ʾ�������ֵ�� <
	 *  */
	boolean maxBound = false;
	/**С��0��ʾ������ */
	double min = -1;
	/**С��0��ʾ������ */
	double max = -1;
	
	/**
	 * �趨����
	 * @param min -1��ʾ�����Ǹ÷���
	 * @param max -1��ʾ�����Ǹ÷���
	 */
	public FilterSmallRegion(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	/** �趨�Ƚϵ����ݣ�Ʃ��heto_more֮��� */
	public void setInfo(String info) {
		this.info = info;
	}
	
	/**
	 * �趨���±߽�<br>
	 * �����±߽磺<br>
	 *  true: ��ʾ������Сֵ��>=<br>
	 * false: ��ʾ������Сֵ�� ><br>
	 * <br>
	 * �����ϱ߽�<br>
	 * true: ��ʾ�������ֵ��<= <br>
	 * false: ��ʾ�������ֵ�� <
	 *  */
	public void setBound(boolean minBound, boolean maxBound) {
		this.minBound = minBound;
		this.maxBound = maxBound;
	}
	
	public boolean isInRegion(double info) {
		boolean result = true;
		if (min >= 0) {
			if (minBound) {
				result = result && (info >= min);
			} else {
				result = result && (info > min);
			}
		}
		
		if (max >= 0) {
			if (minBound) {
				result = result && (info <= max);
			} else {
				result = result && (info < max);
			}
		}
		return result;
	}
}
