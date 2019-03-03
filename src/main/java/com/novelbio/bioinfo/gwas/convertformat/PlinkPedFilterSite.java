package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.gwas.LDcalculate;

/**
 * 如果两个位点距离很近，并且相似度超过95%，则删除其中一个位点
 * 这里最好是已经去除了杂合子之后的信息，当然也可以考虑去除之前
 * @author zong0jie
 *
 */
public class PlinkPedFilterSite extends PlinkPedFilterAbs {
	/** 间隔小于等于200bp且identity大于等于0.95的位点仅保留一个 */
	int interval = 200;
	/** 间隔小于200bp，r2大于等于0.98的位点仅保留一个 */
	double identity = 0.98;
	
	/**
	 * key：strain name
	 * value：每个位点是否杂合，纯合0,杂合1,N为2
	 * 不过应该不含有N，因为之前一步需要做imputation
	 */
	Map<String, char[]> mapStrain2Site = new HashMap<>();
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public void setIdentity(double identity) {
		this.identity = identity;
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
		TxtReadandWrite txtReadMid = new TxtReadandWrite(mid);
		int index = 0;
		PlinkMid plinkMid1 = null, plinkMid2 = null;
		char[] site1 = null, site2 = null;
		for (String content : txtReadMid.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			if (plinkMid1 == null) {
				plinkMid1 = new PlinkMid(content);
				site1 = getLsSnps(index);
				index++;
				continue;
			} else {
				plinkMid2 = new PlinkMid(content);
				site2 = getLsSnps(index);
				boolean isNeed = isNeedSite(plinkMid1, site1, plinkMid2, site2);
				if (isNeed) {
					plinkMid1 = plinkMid2;
					site1 = site2;
				} else {
					setSiteNeedDelete.add(index);
				}
				index++;
			}
		}
		txtReadMid.close();
	}
	
	/** 返回某个位点的snp情况 */
	private char[] getLsSnps(int index) {
		char[] snpOnSite = new char[mapStrain2Site.size()];
		int num = 0;
		for (String strain : mapStrain2Site.keySet()) {
			char[] site = mapStrain2Site.get(strain);
			snpOnSite[num++] = site[index];
		}
		return snpOnSite;
	}
	
	private boolean isNeedSite(PlinkMid mid1, char[] site1, PlinkMid mid2, char[] site2) {
		if (!mid1.getChrId().equals(mid2.getChrId()) || Math.abs(mid1.getPosition() - mid2.getPosition()) > interval ) {
			return true;
		}
		LDcalculate lDcalculate = new LDcalculate();
		lDcalculate.setLsSite1(site1);
		lDcalculate.setLsSite2(site2);
		lDcalculate.calculate();
		return lDcalculate.getR2() < identity;
	}
	
	protected void filter() {
		TxtReadandWrite txtReadPed = new TxtReadandWrite(ped);
		TxtReadandWrite txtWritePedNew = new TxtReadandWrite(pedNew, true);
		for (String content : txtReadPed.readlines()) {
			String[] ss = content.split("\t");
			String strain = ss[0];
			if (setStrainNeedDelete.contains(strain)) {
				continue;
			}
			List<String> lsResult = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				lsResult.add(ss[i]);
			}
			for (int i = 6; i < ss.length; i++) {
				if (setSiteNeedDelete.contains(i-6)) {
					continue;
				}
				lsResult.add(ss[i]);
			}
			txtWritePedNew.writefileln(lsResult);
		}
		txtReadPed.close();
		txtWritePedNew.close();
		
		TxtReadandWrite txtReadMid = new TxtReadandWrite(mid);
		TxtReadandWrite txtWriteMidNew = new TxtReadandWrite(midNew, true);
		int i = -1;
		for (String content : txtReadMid.readlines()) {
			if (content.startsWith("#")) {
				txtWriteMidNew.writefileln(content);
			}
			i++;
			if (setSiteNeedDelete.contains(i)) {
				continue;
			}
			txtWriteMidNew.writefileln(content);
		}
		txtReadMid.close();
		txtWriteMidNew.close();
	}
	
	
}


