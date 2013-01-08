package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** 
 * 一组样本的过滤，可以设定一组样本，然后根据一组样本里面的信息，进行过滤
 * 譬如本组样本中，该位点最少多少杂合snp，最多多少杂合snp等等
 *  */
public class SnpGroupFilterInfo {
	public static final int Homo = 5;
	public static final int HetoLess = 10;
	public static final int Heto = 20;
	public static final int HetoMore = 30;
	
	HashSet<String> setSampleName = new HashSet<String>();
	int SnpIndelHomoNumMin = -1;
	int SnpIndelHomoNumMax = -1;
	
	int SnpIndelHetoNumMin = -1;
	int SnpIndelHetoNumMax = -1;
	
	int SnpIndelHetoLessNumMin = -1;
	int SnpIndelHetoLessNumMax = -1;
	
	int SnpIndelHetoMoreNumMin = -1;
	int SnpIndelHetoMoreNumMax = -1;
	
	int RefHomoMin = -1;
	int RefHomoMax = -1;
	
	int SnpIndelAllMin = -1;
	int snpIndelAllMax = -1;
	/** 未知基因型的样本不得超过该比例，默认0.5 */
	double unKnownProp = 0.5;
	
	int ThisSnpIndelHomoNum = 0;
	int ThisSnpIndelHetoLessNum = 0;
	int ThisSnpIndelHetoNum = 0;
	int ThisSnpIndelHetoMoreNum = 0;
	int ThisRefHomo = 0;	
	double ThisUnKnownSite = 0;
	int ThisSnpIndelAll = 0;
	
	/**
	 * 单个样本过滤时使用，直接设定这个，别的都不用设定了
	 * @param snpLevel Homo，HetoLess等
	 */
	public void setSnpLevel(int snpLevel) {
		if (snpLevel == Homo) {
			setSampleRefHomoNum(1, 1);
		}
		else if (snpLevel == HetoLess) {
			setSampleSnpIndelNum(1, 1);
		}
		else if (snpLevel == Heto) {
			setSampleSnpIndelNum(1, 1);
			setSampleSnpIndelHetoLessNum(0, 0);
		}
		else if (snpLevel == HetoMore) {
			setSampleSnpIndelNum(1, 1);
			setSampleSnpIndelHetoLessNum(0, 0);
			setSampleSnpIndelHetoNum(0, 0);
		}
	}
	/** 设定样本名称,也就是需要过滤哪些样本
	 * 当过滤一组样本时使用
	 * @param sampleName 可以输入null，这样就会使用refSiteSnpIndel自带的名字
	 */
	public void addSampleName(String sampleName) {
		this.setSampleName.add(sampleName);
	}
	public HashSet<String> getSetSampleName() {
		return setSampleName;
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
	/** 位点为含少量杂合snpIndel的样本，其数量区间 */
	public void setSampleSnpIndelHetoLessNum(int snpIndelHetoLessNumMin, int snpIndelHetoLessNumMax) {
		this.SnpIndelHetoLessNumMin = Math.min(snpIndelHetoLessNumMin, snpIndelHetoLessNumMax);
		this.SnpIndelHetoLessNumMax = Math.max(snpIndelHetoLessNumMin, snpIndelHetoLessNumMax);
	}
	/** 位点为含大量杂合snpIndel的样本，其数量区间 */
	public void setSampleSnpIndelHetoMoreNum(int snpIndelHetoMoreNumMin, int snpIndelHetoMoreNumMax) {
		this.SnpIndelHetoMoreNumMin = Math.min(snpIndelHetoMoreNumMin, snpIndelHetoMoreNumMax);
		this.SnpIndelHetoMoreNumMax = Math.max(snpIndelHetoMoreNumMin, snpIndelHetoMoreNumMax);
	}
	/** 含纯合SnpIndel位点的样本，其数量区间 */
	public void setSampleSnpIndelHomoNum(int snpIndelHomoNumMin, int snpIndelHomoNumMax) {
		this.SnpIndelHomoNumMin = Math.min(snpIndelHomoNumMin, snpIndelHomoNumMax);
		this.SnpIndelHomoNumMax = Math.max(snpIndelHomoNumMin, snpIndelHomoNumMax);
	}
	/** 不管纯合还是杂合，累计位点的数量 */
	public void setSampleSnpIndelNum(int SnpIndelAllMin, int snpIndelAllMax) {
		this.SnpIndelAllMin = Math.min(SnpIndelAllMin, snpIndelAllMax);
		this.snpIndelAllMax = Math.max(SnpIndelAllMin, snpIndelAllMax);
	}
	/** 未知基因型的样本不得超过该比例，默认0.5 */
	public void setSampleUnKnownProp(double unKnownProp) {
		this.unKnownProp = unKnownProp;
	}
	public void clearSampleName() {
		setSampleName.clear();
	}
	/** 暴露出来仅供测试，数值清零，包括样本名也会被清空 */
	public void clearAll() {
		setSampleName.clear();
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoLessNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisSnpIndelHetoMoreNum = 0;
		ThisRefHomo = 0;	
		ThisUnKnownSite = 0;
		ThisSnpIndelAll = 0;
	}
	/** 暴露出来仅供测试，数值清零，不清空样本名 */
	public void clearData() {
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoLessNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisSnpIndelHetoMoreNum = 0;
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
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHetoLess || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHetoLess) {
			ThisSnpIndelHetoLessNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHetoMore || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHetoMore) {
			ThisSnpIndelHetoMoreNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.UnKnown) {
			ThisUnKnownSite++;
		}
	}
	/** 暴露出来仅供测试，是否合格 */
	public boolean isQualified() {
		if (ThisUnKnownSite/setSampleName.size() >= unKnownProp) {
			return false;
		}
		
		if (compare(CompareType.BigerEqual, ThisRefHomo, RefHomoMin) && compare(CompareType.SmallEqual, ThisRefHomo, RefHomoMax)
				&&compare(CompareType.BigerEqual, ThisSnpIndelHetoNum, SnpIndelHetoNumMin) && compare(CompareType.SmallEqual, ThisSnpIndelHetoNum, SnpIndelHetoNumMax)
				
				&&compare(CompareType.BigerEqual, ThisSnpIndelHetoLessNum, SnpIndelHetoLessNumMin) && compare(CompareType.SmallEqual, ThisSnpIndelHetoLessNum, SnpIndelHetoLessNumMax)
				&&compare(CompareType.BigerEqual, ThisSnpIndelHetoMoreNum, SnpIndelHetoMoreNumMin) && compare(CompareType.SmallEqual, ThisSnpIndelHetoMoreNum, SnpIndelHetoMoreNumMax)
				
				&&compare(CompareType.BigerEqual, ThisSnpIndelHomoNum, SnpIndelHomoNumMin) && compare(CompareType.SmallEqual, ThisSnpIndelHomoNum, SnpIndelHomoNumMax)
				&&compare(CompareType.BigerEqual, ThisSnpIndelAll, SnpIndelAllMin) && compare(CompareType.SmallEqual, ThisSnpIndelAll, snpIndelAllMax)
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
	private boolean compare(CompareType compareType, int thisNum, int compareNum) {
		if (compareNum < 0) {
			return true;
		}
		
		if (compareType == CompareType.Small) {
			return thisNum < compareNum;
		}
		else if (compareType == CompareType.SmallEqual) {
			return thisNum <= compareNum;
		}
		else if (compareType == CompareType.Equal) {
			return thisNum == compareNum;
		}
		else if (compareType == CompareType.BigerEqual) {
			return thisNum >= compareNum;
		}
		else if ( compareType == CompareType.Biger) {
			return thisNum > compareNum;
		}
		return false;
	}
	
	public static HashMap<String, Integer> getMap_Str2SnpLevel() {
		HashMap<String, Integer> mapStr2SnpLevel = new HashMap<String, Integer>();
		mapStr2SnpLevel.put("Homo", Homo);
		mapStr2SnpLevel.put("HetoLess", HetoLess);
		mapStr2SnpLevel.put("Heto", Heto);
		mapStr2SnpLevel.put("HetoMore", HetoMore);
		return mapStr2SnpLevel;
	}
	
	enum CompareType {
		Biger, BigerEqual, Equal, SmallEqual, Small
	}
}
