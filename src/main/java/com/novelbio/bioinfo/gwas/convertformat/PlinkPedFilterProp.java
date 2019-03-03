package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 如果两个位点距离很近，并且相似度超过95%，则删除其中一个位点
 * 这里最好是已经去除了杂合子之后的信息，当然也可以考虑去除之前
 * @author zong0jie
 *
 */
public class PlinkPedFilterProp extends PlinkPedFilterAbs {
	
	/** 变异频率小于0.05的位点删除 */
	double prop = 0.05;
	
	/**
	 * key：strain name
	 * value：每个位点是否杂合，纯合0,杂合1,N为2
	 * 不过应该不含有N，因为之前一步需要做imputation
	 */
	Map<String, char[]> mapStrain2Site = new HashMap<>();
	
	public void setProp(double prop) {
		this.prop = prop;
	}
	
	/**
	 * 把突变信息读取到内存中
	 * @param plinkPed
	 * @param plinkMid
	 */
	protected void readPed() {
		TxtReadandWrite txtReadPed = new TxtReadandWrite(ped);
		for (String conent : txtReadPed.readlines()) {
			String[] ss = conent.split("\t");
			String strain = ss[0];
			char[] site = new char[ss.length-6];
			for (int i = 6; i < ss.length; i++) {
				String[] bases = ss[i].split(" ");
				site[i-6] = bases[0].toCharArray()[0];
			}
			mapStrain2Site.put(strain, site);
		}
		txtReadPed.close();
	}
	
	/** 开始获得过滤信息，填充{@link #setStrainNeedDelete} 和 {@link #lsSiteNeedDelete}*/
	protected void fillFilterInfo() {
		int snpNum = mapStrain2Site.values().iterator().next().length;
		for (int i = 0; i < snpNum; i++) {
			char[] site = getLsSnps(i);
			if (!isNeedSite(site)) {
				setSiteNeedDelete.add(i);
			}
		}
	}
	
	/** 返回某个位点的snp情况
	 * @param index 从0开始计算
	 * @return
	 */
	private char[] getLsSnps(int index) {
		char[] snpOnSite = new char[mapStrain2Site.size()];
		int num = 0;
		for (String strain : mapStrain2Site.keySet()) {
			char[] site = mapStrain2Site.get(strain);
			snpOnSite[num++] = site[index];
		}
		return snpOnSite;
	}
	
	@VisibleForTesting
	protected boolean isNeedSite( char[] site) {
		Map<String, int[]> mapAllele2Num = new HashMap<>();
		for (char c : site) {
			String allele = (c+"").toUpperCase();
			int[] num = mapAllele2Num.get(allele);
			if (num == null) {
				num = new int[] {0};
				mapAllele2Num.put(allele, num);
			}
			num[0]++;
		}
		List<String[]> lsNum = new ArrayList<>();
		for (String allele : mapAllele2Num.keySet()) {
			if (allele.equals("N")) {
				continue;
			}
			lsNum.add(new String[] {allele, mapAllele2Num.get(allele)[0]+""});
		}
		
		Collections.sort(lsNum, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer num1 = Integer.parseInt(o1[1]);
				Integer num2 = Integer.parseInt(o2[1]);
				return -num1.compareTo(num2);
			}
		});
		if (lsNum.size() <= 1) {
			return false;
		}
		int num = Integer.parseInt(lsNum.get(0)[1]);
		int sum = 0;
		for (String[] allele2Num : lsNum) {
			sum += Integer.parseInt(allele2Num[1]);
		}
		double value1 = (double)num/sum;
		double value2 = (double)num/site.length;
		return value1 >= prop && value1 <= 1-prop && value2 >= prop && value2 <= 1-prop;
	}	
	
}
