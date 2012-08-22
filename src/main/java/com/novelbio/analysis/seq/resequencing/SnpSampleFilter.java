package com.novelbio.analysis.seq.resequencing;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;


/** 不同的样本的Snp过滤规则，符合该规则的snp会被挑选出来 */
public class SnpSampleFilter {
	private static Logger logger = Logger.getLogger(SnpSampleFilter.class);
	
	/** 判定为纯合snp的最少reads数 */
	static int Snp_Homo_ReadsAllNumMin = 3;
	/** 判定为Snp所含有的ref数量不得大于该数值 */
	static int Snp_Homo_Contain_RefNumMax = 2;
	/**判定为纯合snp所含有的ref比例不得大于该数值 */
	static double Snp_Homo_Contain_RefProp_Max = 0.02;
	
	/** 判定为杂合snp HetoMore的最少reads数 */
	static int Snp_HetoMore_ReadsAllNumMin = 3;
	/** 判定为Snp所含有的snp数量不得小于该数值 */
	static int Snp_HetoMore_Contain_SnpNumMin = 2;
	/**判定为纯合snp所含有的ref比例不得大于该数值 */
	static double Snp_HetoMore_Contain_RefProp_Max = 0.1;
	
	/** 判定为Snp Heto的最少reads数 */
	static int Snp_Heto_ReadsAllNumMin = 3;
	/** 判定为 Snp Heto的最少 ref reads数，必须大于这个值 */
	static int Snp_Hete_Contain_RefNumMin = 1;
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	static double Snp_Hete_Contain_SnpProp_Min = 0.1;
	/** 判定为snp Heto所含有的ref比例不得小于该数值 */
	static double Snp_Hete_Contain_RefProp_Min = 0.1;
	
	
	/** 判定为ref的最少reads数 */
	static int Ref_ReadsAllNumMin = 10;
	/** 判定为ref所含有的snp数量不得大于该数值 */
	static int Ref_Contain_SnpNumMin = 2;
	/**判定为ref所含有的snp比例不得大于该数值 */
	static double Ref_Contain_SnpProp_Max = 0.02;
	
	/** 判定为Snp HetoLess的最少reads数 */
	static int Snp_HetoLess_ReadsAllNumMin = 3;
	/** 判定为 Snp HetoLess的最少 ref reads数，必须大于这个值 */
	static int Snp_HetoLess_Contain_RefNumMin = 2;
	/**判定为snp HetoLess所含有的ref比例不得大于该数值 */
	static double Snp_HetoLess_Contain_SnpProp_Max = 0.1;
	
	HashSet<SampleDetail> setSampleFilterInfo = new HashSet<SampleDetail>();
	
	/**添加样本过滤信息，注意大小写 */
	public void addSampleFilterInfo(SampleDetail sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** 重置样本信息 */
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
	 * 该样本是否通过质检
	 * 如果通过质检了，就返回通过质检的那个snp类型
	 * 否则返回null
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
		//出来之后再判断一次
		if (isQualified) {
			return siteSnpIndelInfoResult;
		}
		return null;
	}
	/** 输入之前要指定样本名，
	 * 返回指定的snpindel的信息 */
	private SnpIndelHomoHetoType getSnpIndelType(MapInfoSnpIndel mapInfoSnpIndel, SiteSnpIndelInfo siteSnpIndelInfo) {
		int numSnpIndel = siteSnpIndelInfo.getReadsNum();
		int numAll = mapInfoSnpIndel.getReadsNumAll();
		
		//因为可能有别的基因型，所以用这种方式，将所有非本snp的位点都忽略为ref位点。
		int numRef = numAll - numSnpIndel;//mapInfoSnpIndel.getReadsNumRef();
		return getSnpIndelType(siteSnpIndelInfo.getSnpIndelType(), numSnpIndel, numRef, numAll);
	}
	/**
	 * public 仅供Junit测试 
	 * 输入之前要指定样本名，
	 * 返回指定的snpindel的信息 */
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
				&& ((double)numRef/numAll <= Snp_HetoMore_Contain_RefProp_Max || numRef == 1) )//只有1条ref很难说明问题 
		{
			if (siteSnpIndelInfo == SnpIndelType.INSERT || siteSnpIndelInfo == SnpIndelType.DELETION) {
				return SnpIndelHomoHetoType.IndelHetoMore;
			}
			else if (siteSnpIndelInfo == SnpIndelType.MISMATCH) {
				return SnpIndelHomoHetoType.SnpHetoMore;
			}
		}
		//先判定纯合再判定杂合小
		else if (numSnpIndel <= Ref_Contain_SnpNumMin && numAll >= Ref_ReadsAllNumMin 
				&& (double)numSnpIndel/numAll <= Ref_Contain_SnpProp_Max  )
		{
			return SnpIndelHomoHetoType.RefHomo;
		}
		else if (numRef >= Snp_HetoLess_Contain_RefNumMin && numAll >= Snp_HetoLess_ReadsAllNumMin
				&& ((double)numSnpIndel/numAll <= Snp_HetoLess_Contain_SnpProp_Max || numSnpIndel == 1 )//只有1条snp很难说明问题 
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
	
	/** 该样本中SnpIndel的杂合情况，未知，snp杂合，snp纯合，indel杂合，indel纯合 这几种*/
	public static enum SnpIndelHomoHetoType {
		SnpHomo, SnpHetoMore, SnpHeto, SnpHetoLess, IndelHomo, IndelHetoMore, IndelHeto, IndelHetoLess, RefHomo, UnKnown;
	}
}