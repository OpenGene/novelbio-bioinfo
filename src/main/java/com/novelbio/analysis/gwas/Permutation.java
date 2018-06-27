package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.novelbio.analysis.gwas.combinesnp.ClusterKmean;
import com.novelbio.base.dataStructure.ArrayOperate;

public class Permutation {
	/** 每一组就一个排列组合 */
	List<List<Integer>> lsNumber = new ArrayList<>();
	List<Allele> lsAllele;
	String geneName;
	String chrId;
	
	/** 最多三个snp合并到一起 */
	int maxSnpNum = 3;
	
	/** 输入的snp-index */
	int index;
	/** 结束之后的snp-index */
	int indexNew;

	/**
	 * c(m,n)的n<br>
	 * maxSnpNum=3表示从n个里面抽取1-3个<br>
	 * 如果是全排列的话会非常多，因此要限制下<br>
	 * 默认为3<br>
	 * @param maxSnpNum
	 */
	public void setMaxSnpNum(int maxSnpNum) {
		this.maxSnpNum = maxSnpNum;
	}
	
	public void setLsAlle(List<Allele> lsAllele) {
		this.lsAllele = lsAllele;
	}
	public void setIndex(int index) {
		this.index = index;
		this.indexNew = index;
	}
	public int getIndexNew() {
		return indexNew;
	}
	public void setGeneInfo(String chrId, String geneName) {
		this.chrId = chrId;
		this.geneName = geneName;
	}
	
	public List<Allele> getLsAllele() {
		return lsAllele;
	}
	
	public List<List<Integer>> getLsPermutations() {
		return lsNumber;
	}
	
	/**
	 * 给定lsIndex，譬如 0，1，3，4
	 * 返回 具体的listAllele
	 * @return
	 */
	public static List<Allele> getLsAlleleFromLsIndex(List<Allele> lsAlleles, List<Integer> lsIndex) {
		List<Allele> lsAlleleNew = new ArrayList<>();
		for (Integer index : lsIndex) {
			lsAlleleNew.add(lsAlleles.get(index));
		}
		return lsAlleleNew;
	}
	
	public void generatePermutation() {
		permutation(lsAllele.size());
//		filterByIso();
	}
	
	protected void permutation(int number) {
		Integer[] lsIndex = new Integer[number];
		for (int i = 0; i < lsIndex.length; i++) {
			lsNumber.add(Lists.newArrayList(i));
			lsIndex[i] = i;
		}
		lsNumber.add(ArrayOperate.converArray2List(lsIndex));
		//排列组合
		if (maxSnpNum <= 0) maxSnpNum = lsIndex.length;
		int num = Math.min(maxSnpNum, lsIndex.length);
		for (int i = 2; i <= num; i++) {
			combinationSelect(lsIndex, 0, new Integer[i], 0);
		}
	}
	
	//TODO 尚未测试
//	protected void filterByIso() {
//		List<List<Integer>> lsPermutationFinal = new ArrayList<>();
//		//TODO 暂时停用
//		for (List<Integer> lsPermutationUnit : lsNumber) {
//			Set<String> setOverlap = new HashSet<>();
//			for (Integer index : lsPermutationUnit) {
//				Set<String> setIsoThis = mapSnp2SetIsoName.get(lsAlleleFinal.get(index));
//				if (setOverlap.isEmpty()) {
//					setOverlap = setIsoThis;
//					continue;
//				}
//				Set<String> setOverlapTmp = new HashSet<>();
//				for (String isoName : setIsoThis) {
//					if (setOverlap.contains(isoName)) {
//						setOverlapTmp.add(isoName);
//					}
//				}
//				setOverlap = setOverlapTmp;
//				if (setOverlap.isEmpty()) {
//					break;
//				}
//			}
//			if (!setOverlap.isEmpty()) {
//				lsPermutationFinal.add(lsPermutationUnit);
//			}
//		}
//		lsNumber = lsPermutationFinal;
//	}
	
	/** 
	 * 计算组合数，即C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
	public static long combination(int n, int m) {
		long factN = factorial(n);
		long factN_M = factorial(n - m);
		long factM = factorial(m);
		long value = (n >= m) ? factN / factN_M / factM : 0;
		return value;
	}
	
	/** 
	 * 计算组合数，即C(n, m) = n!/((n-m)! * m!) 
	 * @param n 
	 * @param m 
	 * @return 
	 */  
	public static long combinationNew(int n, int m) {
		if (m == 0 || m == n) {
			return 1;
		}
		long factNtoM = n-m > m ? factorial(n, n-m+1) : factorial(n, m+1);
		long factN_M = n-m > m ? factorial(m) : factorial(n - m);
		long value = (n >= m) ? factNtoM / factN_M : 0;
		return value;
	}
	/** 
	 * 计算阶乘数，即n! = n * (n-1) * ... * 2 * 1 
	 * @param n 
	 * @return 
	 */  
	public static long factorial(int n) {
		long value = (n > 1) ? n * factorial(n - 1) : 1;
		if (value < 0) {
			throw new RuntimeException("factorial error, " + n + "! larger than Long!");
		}
		return value;
	}
	/** 
	 * 计算阶乘数，即n! = n * (n-1) * ... * m
	 * @param n 
	 * @return 
	 */  
	public static long factorial(int n, int m) {
		if (m == n) {
			return m;
		} else if (m > n) {
			throw new RuntimeException(m + " is bigger than " + n);
		}
		long value = (n > 1) ? n * factorial(n - 1, m) : 1;  
		if (value < 0) {
			throw new RuntimeException("factorial error, " + n + "!/"+m+"! larger than Long!");
		}
		return value;
	}
    /** 
     * 组合选择 
     * @param lsIndex 待选列表 
     * @param dataIndex 待选开始索引 
     * @param lsResult 前面（resultIndex-1）个的组合结果 
     * @param resultIndex 选择索引，从0开始 
     */  
	private void combinationSelect(Integer[] lsIndex, int dataIndex, Integer[] lsResult, int resultIndex) {
		int resultLen = lsResult.length;
		int resultCount = resultIndex + 1;
		if (resultCount > resultLen) { // 全部选择完时，输出组合结果
			lsNumber.add(ArrayOperate.converArray2List(lsResult));
			return;
		}

		// 递归选择下一个  
		for (int i = dataIndex; i < lsIndex.length + resultCount - resultLen; i++) {
			lsResult[resultIndex] = lsIndex[i];
			combinationSelect(lsIndex, i + 1, lsResult, resultIndex + 1);
		}
	}
	
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 注意最后无 \n 结尾
	 */
	public String toStringMap() {
		indexNew = index;
		List<String> lsResult = new ArrayList<>();
		for (List<Integer> list : getLsPermutations()) {
			List<String> lsResultUnit = new ArrayList<>();
			Allele alleleFirst = lsAllele.get(list.get(0));
			lsResultUnit.add(chrId);
			lsResultUnit.add(geneName+ "." +(indexNew));
			lsResultUnit.add("0");
			lsResultUnit.add(alleleFirst.getPosition() + "");
			if (list.size() == 1) {
				if (alleleFirst.isRefMajor()) {
					lsResultUnit.add(alleleFirst.getAltBase());
					lsResultUnit.add(alleleFirst.getRefBase());
				} else {
					lsResultUnit.add(alleleFirst.getRefBase());
					lsResultUnit.add(alleleFirst.getAltBase());
				}
			} else {
				lsResultUnit.add(ClusterKmean.getMajor());
				lsResultUnit.add(ClusterKmean.getMinor());
			}
			lsResult.add(ArrayOperate.cmbString(lsResultUnit, "\t"));
			indexNew++;
		}
		return ArrayOperate.cmbString(lsResult, "\n");
	}
	
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 注意最后无 \n 结尾
	 */
	public String toStringMap(List<Integer> list ) {
		List<String> lsResultUnit = new ArrayList<>();
		Allele alleleFirst = lsAllele.get(list.get(0));
		lsResultUnit.add(chrId);
		lsResultUnit.add(geneName+ "." +(indexNew));
		lsResultUnit.add("0");
		lsResultUnit.add(alleleFirst.getPosition() + "");
		if (list.size() == 1) {
			if (alleleFirst.isRefMajor()) {
				lsResultUnit.add(alleleFirst.getAltBase());
				lsResultUnit.add(alleleFirst.getRefBase());
			} else {
				lsResultUnit.add(alleleFirst.getRefBase());
				lsResultUnit.add(alleleFirst.getAltBase());
			}
		} else {
			lsResultUnit.add(ClusterKmean.getMajor());
			lsResultUnit.add(ClusterKmean.getMinor());
		}
		return ArrayOperate.cmbString(lsResultUnit, "\t");
	}
	
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 注意最后无 \n 结尾
	 */
	public Allele getAllele(List<Integer> list ) {
		Allele alleleFirst = lsAllele.get(list.get(0));
		if (list.size() > 1) {
			alleleFirst.setRef(ClusterKmean.getMajor());
			alleleFirst.setAlt(ClusterKmean.getMinor());
			alleleFirst.setAllele1(ClusterKmean.getMajor());
			alleleFirst.setAllele2(ClusterKmean.getMinor());
		}
		return alleleFirst;
	}
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 组合前的snp与组合后的snp的对照表
	 * 注意最后无 \n 结尾
	 */
	public String toStringConvertor() {
		indexNew = index;
		List<String> lsResult = new ArrayList<>();
		for (List<Integer> list : getLsPermutations()) {
			StringBuilder sBuilder = new StringBuilder();
			Allele alleleFirst = null;
			for (Integer indexAllele : list) {
				Allele allele = lsAllele.get(indexAllele);
				if (alleleFirst == null) {
					alleleFirst = allele;
					sBuilder.append(alleleFirst.toStringSimple()+"\t" + allele.getMarker());
				} else {
					//用 "|"作为排列组合snp之间的分隔符
					sBuilder.append("|"+allele.getMarker());
				}
			}
			sBuilder.append("\t" + geneName+ "." + indexNew);
			lsResult.add(sBuilder.toString());
			indexNew++;
		}
		return ArrayOperate.cmbString(lsResult, "\n");
	}
	/**
	 * 把排列组合的值按照Map的形式输出
	 * 组合前的snp与组合后的snp的对照表
	 * 注意最后无 \n 结尾
	 */
	public String toStringConvertor(List<Integer> lsIndex) {
		StringBuilder sBuilder = new StringBuilder();
		Allele alleleFirst = null;
		for (Integer indexAllele : lsIndex) {
			Allele allele = lsAllele.get(indexAllele);
			if (alleleFirst == null) {
				alleleFirst = allele;
				sBuilder.append(alleleFirst.toStringSimple()+"\t" + allele.getMarker());
			} else {
				//用 "|"作为排列组合snp之间的分隔符
				sBuilder.append("|"+allele.getMarker());
			}
		}
		sBuilder.append("\t" + geneName+ "." + indexNew);
		return sBuilder.toString();
	}
	
	public void indexAdd() {
		indexNew++;
	}
}
