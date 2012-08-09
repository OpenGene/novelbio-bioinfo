package com.novelbio.analysis.seq.resequencing;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;


/** 不同的样本的Snp过滤规则，符合该规则的snp会被挑选出来 */
public class SnpSampleFilter {
	private static Logger logger = Logger.getLogger(SnpSampleFilter.class);
	
	/** 判定为ref的最少reads数 */
	static int Ref_ReadsAllNumMin = 3;
	/** 判定为ref所含有的snp数量不得大于该数值 */
	static int Ref_Contain_SnpNumMin = 2;
	/**判定为ref所含有的snp比例不得大于该数值 */
	static double Ref_Contain_SnpProp_Max = 0.05;
	
	/** 判定为Snp Heto的最少reads数 */
	static int Snp_Hete_ReadsAllNumMin = 2;
	/** 判定为 Snp Heto的最少 ref reads数，必须大于这个值 */
	static int Snp_Hete_Contain_RefNumMin = 2;
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	static double Snp_Hete_Contain_SnpProp_Min = 0.01;
	
	/** 判定为ref的最少reads数 */
	static int Snp_Homo_ReadsAllNumMin = 3;
	/** 判定为Snp所含有的snp数量不得小于该数值 */
	static int Snp_Homo_Contain_SnpNumMin = 2;
	/**判定为纯合snp所含有的ref比例不得大于该数值 */
	static double Snp_Homo_Contain_SnpProp_Max = 0.05;

	
	
	HashSet<SampleDetail> setSampleFilterInfo = new HashSet<SampleDetail>();
	
	/**添加样本过滤信息，注意大小写 */
	public void addSampleFilterInfo(SampleDetail sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** 重置样本信息 */
	public void clearSampleFilterInfo() {
		setSampleFilterInfo.clear();
	}
	/** 
	 * 该样本是否通过质检
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
	/** 输入之前要指定样本名，
	 * 返回最大的snpindel的信息 */
	private SnpIndelHomoHetoType getSnpIndelType(MapInfoSnpIndel mapInfoSnpIndel) {
		SiteSnpIndelInfo siteSnpIndelInfo = mapInfoSnpIndel.getSiteSnpInfoBigAllen();
		return getSnpIndelType(mapInfoSnpIndel, siteSnpIndelInfo);
	}
	/** 输入之前要指定样本名，
	 * 返回指定的snpindel的信息 */
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
					logger.error("出现未知snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
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
				logger.error("出现未知snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
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
				logger.error("出现未知snp" + mapInfoSnpIndel.getRefID() + " " + mapInfoSnpIndel.getRefSnpIndelStart());
				return SnpIndelHomoHetoType.UnKnown;
			}
		}
		else {
			return SnpIndelHomoHetoType.UnKnown;
		}
	}
	/** 该样本中SnpIndel的杂合情况，未知，snp杂合，snp纯合，indel杂合，indel纯合 这几种*/
	public static enum SnpIndelHomoHetoType {
		SnpHomo, SnpHeto, IndelHomo, IndelHeto, SnpUnKonwn, IndelUnKnown, RefHomo, UnKnown;
	}
}