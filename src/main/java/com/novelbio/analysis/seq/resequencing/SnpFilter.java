package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import sun.tools.tree.AddExpression;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;


/** 不同的样本的Snp过滤规则，符合该规则的snp会被挑选出来 */
public class SnpFilter {
	private static Logger logger = Logger.getLogger(SnpFilter.class);
	
	/** 判定为纯合snp的最少reads数 */
	int Snp_Homo_ReadsAllNumMin = 3;
	/** 判定为Snp所含有的ref数量不得大于该数值 */
	int Snp_Homo_Contain_RefNumMax = 2;
	/**判定为纯合snp所含有的ref比例不得大于该数值 */
	double Snp_Homo_Contain_RefProp_Max = 0.04;
	
	/** 判定为杂合snp HetoMore的最少reads数 */
	int Snp_HetoMore_ReadsAllNumMin = 3;
	/** 判定为Snp所含有的snp数量不得小于该数值 */
	int Snp_HetoMore_Contain_SnpNumMin = 2;
	/**判定为纯合snp所含有的snp比例不得小于于该数值 */
	double Snp_HetoMore_Contain_SnpProp_Min = 0.7;
	
	/** 判定为Snp Heto的最少reads数 */
	int Snp_Heto_ReadsAllNumMin = 3;
	/** 判定为 Snp Heto的最少 ref reads数，必须大于这个值 */
	int Snp_Hete_Contain_RefNumMin = 1;
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	double Snp_Hete_Contain_SnpProp_Min = 0.1;
	/** 判定为snp Heto所含有的ref比例不得小于该数值 */
	double Snp_Hete_Contain_RefProp_Min = 0.1;
	
	
	/** 判定为ref的最少reads数 */
	int Ref_ReadsAllNumMin = 10;
	/** 判定为ref所含有的snp数量不得大于该数值 */
	int Ref_Contain_SnpNumMin = 2;
	/**判定为ref所含有的snp比例不得大于该数值 */
	double Ref_Contain_SnpProp_Max = 0.04;
	
	/** 判定为Snp HetoLess的最少reads数 */
	int Snp_HetoLess_ReadsAllNumMin = 3;
	/** 判定为 Snp HetoLess的最少 ref reads数，必须大于这个值 */
	int Snp_HetoLess_Contain_RefNumMin = 2;
	
	//该数值同Snp_Hete_Contain_SnpProp_Min
	/**判定为snp HetoLess所含有的ref比例不得大于该数值 */
//	static double Snp_HetoLess_Contain_SnpProp_Max = 0.1;
	
	HashSet<SnpGroupFilterInfo> setSampleFilterInfo = new HashSet<SnpGroupFilterInfo>();

	public void setSnp_HetoMore_Contain_SnpProp_Min(
			double snp_HetoMore_Contain_SnpProp_Min) {
		Snp_HetoMore_Contain_SnpProp_Min = snp_HetoMore_Contain_SnpProp_Min;
	}
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	public void setSnp_Hete_Contain_SnpProp_Min(
			double snp_Hete_Contain_SnpProp_Min) {
		Snp_Hete_Contain_SnpProp_Min = snp_Hete_Contain_SnpProp_Min;
	}
	
	/**添加样本过滤信息，注意大小写 */
	public void addSampleFilterInfo(SnpGroupFilterInfo sampleDetail) {
		this.setSampleFilterInfo.add(sampleDetail);
	}
	/** 重置样本信息 */
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
	 * 该样本是否通过质检
	 * 如果通过质检了，就返回通过质检的那个snp类型
	 * 否则返回空的list
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
				//只要有一组样本没有通过检验，就跳出
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
				&& ((double)numSnpIndel/numAll >= Snp_HetoMore_Contain_SnpProp_Min || numRef == 1) )//只有1条ref很难说明问题 
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
				&& ((double)numSnpIndel/numAll < Snp_Hete_Contain_SnpProp_Min && numSnpIndel >= 1 )//只有1条snp很难说明问题 
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
 * 所有条件，负数都表示不参考，忽略该项
 * 所有条件，如果是下界，则为大于等于
 * 如果是上界，则为小于
 * @author zong0jie
 *
 */
class FilterUnit {
	ArrayList<FilterSmallRegion> lsCompareInfo = new ArrayList<FilterSmallRegion>();
	String UnitName;
	
	/**
	 * 设定比较的区间
	 * @param filterSmallRegion
	 */
	public void addRegionInfo(FilterSmallRegion filterSmallRegion) {
		lsCompareInfo.add(filterSmallRegion);
	}
	
	/** 判定是否落在该区间内 */
	public boolean isInThisUnit(int numSnpIndel, int numRef, int numAll) {
		if (Ref_Num_Max > 0 ) {
			
		}
		
		return false;
	}
	
}
/** 比较的最小单元 */
class FilterSmallRegion {
	String info;
	/**
	 * 下边界<br>
	 *  true: 表示遇到最小值是>=<br>
	 * false: 表示遇到最小值是 >
	 *  */
	boolean minBound = true;
	/**
	 * 上边界<br>
	 *  true: 表示遇到最大值是< = <br>
	 * false: 表示遇到最大值是 <
	 *  */
	boolean maxBound = false;
	/**小于0表示不考虑 */
	double min = -1;
	/**小于0表示不考虑 */
	double max = -1;
	
	/**
	 * 设定区间
	 * @param min -1表示不考虑该方向
	 * @param max -1表示不考虑该方向
	 */
	public FilterSmallRegion(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	/** 设定比较的内容，譬如heto_more之类的 */
	public void setInfo(String info) {
		this.info = info;
	}
	
	/**
	 * 设定上下边界<br>
	 * 对于下边界：<br>
	 *  true: 表示遇到最小值是>=<br>
	 * false: 表示遇到最小值是 ><br>
	 * <br>
	 * 对于上边界<br>
	 * true: 表示遇到最大值是<= <br>
	 * false: 表示遇到最大值是 <
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
