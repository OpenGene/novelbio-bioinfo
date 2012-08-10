package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;
/** 某一组样本的过滤信息
 * 包括最少多少杂合snp，最多多少杂合snp等等
 *  */
public class SampleDetail {
	ArrayList<String> lsSampleName = new ArrayList<String>();
	int SnpIndelHomoNumMin = -1;
	int SnpIndelHomoNumMax = -1;
	
	int SnpIndelHetoNumMin = -1;
	int SnpIndelHetoNumMax = -1;
	
	int RefHomoMin = -1;
	int RefHomoMax = -1;
	
	int SnpIndelAllMin = -1;
	int snpIndelAllMax = -1;
	/** 未知基因型的样本不得超过该比例 */
	double unKnownProp = 0.3;
	
	int ThisSnpIndelHomoNum = 0;
	int ThisSnpIndelHetoNum = 0;
	int ThisRefHomo = 0;
	int ThisSnpIndelAll = 0;
	double ThisUnKnownSite = 0;
	/** 设定样本名称 */
	public void addSampleName(String sampleName) {
		this.lsSampleName.add(sampleName);
	}
	/** 位点为纯合位点的样本，其数量区间 */
	public void setSampleRefHomoNum(int refHomoMin, int refHomoMax) {
		this.RefHomoMin = Math.min(refHomoMin, refHomoMax);
		this.RefHomoMax = Math.max(refHomoMin, refHomoMax);
	}
	/** 位点为杂合snpIndel的样本，其数量区间 */
	public void setSampleSnpIndelHetoNum(int snpIndelHetoNumMin, int snpIndelHetoNumMax) {
		this.SnpIndelHetoNumMin = Math.min(snpIndelHetoNumMin, snpIndelHetoNumMax);
		this.SnpIndelHetoNumMax = Math.max(snpIndelHetoNumMin, snpIndelHetoNumMax);
	}
	public void setSampleSnpIndelHomoNum(int snpIndelHomoNumMin, int snpIndelHomoNumMax) {
		this.SnpIndelHomoNumMin = Math.min(snpIndelHomoNumMin, snpIndelHomoNumMax);
		this.SnpIndelHomoNumMax = Math.max(snpIndelHomoNumMin, snpIndelHomoNumMax);
	}
	/** 不管纯合还是杂合，累计位点的数量 */
	public void setSampleSnpIndelNum(int SnpIndelAllMin, int snpIndelAllMax) {
		this.SnpIndelAllMin = Math.min(SnpIndelAllMin, snpIndelAllMax);
		this.snpIndelAllMax = Math.max(SnpIndelAllMin, snpIndelAllMax);
	}
	/** 未知基因型的样本不得超过该比例，默认0.3 */
	public void setSampleUnKnownProp(double unKnownProp) {
		this.unKnownProp = unKnownProp;
	}
	/** 暴露出来仅供测试，数值清零，包括样本名也会被清空 */
	public void clearAll() {
		lsSampleName.clear();
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisRefHomo = 0;
		ThisUnKnownSite = 0;
		ThisSnpIndelAll = 0;
	}
	/** 暴露出来仅供测试，数值清零，不清空样本名 */
	public void clearData() {
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisRefHomo = 0;
		ThisUnKnownSite = 0;
		ThisSnpIndelAll = 0;
	}
	/** 暴露出来仅供测试，累计并计数 */
	public void addSnpIndelHomoHetoType(SnpIndelHomoHetoType snpIndelHomoHetoType) {
		if (snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHeto || snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHeto) {
			ThisSnpIndelHetoNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHomo || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHomo) {
			ThisSnpIndelHomoNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.RefHomo) {
			ThisRefHomo++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpUnKonwn || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelUnKnown) {
			ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.UnKnown) {
			ThisUnKnownSite++;
		}
	}
	/** 暴露出来仅供测试，是否合格 */
	public boolean isQualified() {
		if (ThisUnKnownSite/lsSampleName.size() >= unKnownProp) {
			return false;
		}
		
		if (compare(4, ThisRefHomo, RefHomoMin) && compare(2, ThisRefHomo, RefHomoMax)
				&&compare(4, ThisSnpIndelHetoNum, SnpIndelHetoNumMin) && compare(2, ThisSnpIndelHetoNum, SnpIndelHetoNumMax)
				&&compare(4, ThisSnpIndelHomoNum, SnpIndelHomoNumMin) && compare(2, ThisSnpIndelHomoNum, SnpIndelHomoNumMax)
				&&compare(4, ThisSnpIndelAll, SnpIndelAllMin) && compare(2, ThisSnpIndelAll, snpIndelAllMax)
				) {
			return true;
		}
		return false;
	}
	/** 
	 * @param compareType<br>
	 *  1：小于 <br>
	 *  2 小于等于 <br>
	 *  3：等于 <br>
	 *  4 大于等于 <br>
	 *  5：大于
	 * @param thisNum 本次
	 * @param compareNum 下一次
	 * @return
	 */
	private boolean compare(int compareType, int thisNum, int compareNum) {
		if (compareNum < 0) {
			return true;
		}
		if (compareType == 1) {
			return thisNum < compareNum;
		}
		else if (compareType == 2) {
			return thisNum <= compareNum;
		}
		else if (compareType == 3) {
			return thisNum == compareNum;
		}
		else if (compareType == 4) {
			return thisNum >= compareNum;
		}
		else if ( compareType == 5) {
			return thisNum > compareNum;
		}
		return false;
	}
}
