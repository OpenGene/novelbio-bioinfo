package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;

public class Allele extends Align {
	String marker;
	String other;
	
	String ref;
	String alt;
	
	/** 该snp的序号 */
	int index;
	/**
	 * ref 位点是否为高频位点
	 * 对于plink.bim 来说，ref位点不一定是高频位点
	 * 很可能alt是高频
	 */
	Boolean isRefMajor;
	
	String allele1;
	String allele2;
	
	public Allele() {}
	
	/**
	 * 在读取plinkMap的时候，初始化本方法不会设定{@link #setIndex(int)}
	 * 所以需要手工设定
	 * @param chrInfo
	 */
	public Allele(String chrInfo) {
		String[] ss = chrInfo.split("\t");
		setChrID(ss[0]);
		marker = ss[1];
		other = ss[2];
		setEndAbs(Integer.parseInt(ss[3]));
		setStartAbs(Integer.parseInt(ss[3]));
		if (ss.length >= 5) {
			ref = ss[4];
		}
		if (ss.length >= 6) {
			alt = ss[5];
		}
		if (ss.length >= 7) {
			if (StringOperate.isEqual(ss[6], "1")) {
				isRefMajor = true;
			} else if (StringOperate.isEqual(ss[6], "-1")) {
				isRefMajor = false;
			}
		}
		if (ss.length >= 8) {
			other = ss[7];
		}
	}
	public String getMarker() {
		return marker;
	}
	public void setMarker(String marker) {
		this.marker = marker;
	}
	/**
	 * ref 位点是否为高频位点
	 * 对于plink.bim 来说，ref位点不一定是高频位点
	 * 很可能alt是高频
	 */
	public void setIsRefMajor(boolean isRefMajor) {
		this.isRefMajor = isRefMajor;
	}
	public Boolean isRefMajor() {
		return isRefMajor;
	}
	/** ref和alt交换 */
	public void changeRefAlt() {
		String tmp = ref;
		ref = alt;
		alt = tmp;
	}
	/** 该snp的序号，在plinkPed中，只能知道allel的序号而不知道snp的具体position<br>
	 * 必须从plinkMap相同序号的allel中获取position<br>
	 * <br>
	 * 从1开始计数
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/** 该snp的序号，在plinkPed中，只能知道allel的序号而不知道snp的具体position<br>
	 * 必须从plinkMap相同序号的allel中获取position<br>
	 * <br>
	 * 从1开始计数
	 * @param index
	 */
	public int getIndex() {
		return index;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public void setAlt(Character alt) {
		this.alt = alt+"";
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public void setRef(Character ref) {
		this.ref = ref+"";
	}
	public String getRefBase() {
		return ref;
	}
	public String getAltBase() {
		return alt;
	}
	public void setAllele1(Character allele1) {
		this.allele1 = allele1+"";
	}
	public void setAllele2(Character allele2) {
		this.allele2 = allele2+"";
	}
	public void setAllele1(String allele1) {
		this.allele1 = allele1;
	}
	public void setAllele2(String allele2) {
		this.allele2 = allele2;
	}
	public String getAllele1() {
		return allele1;
	}
	public String getAllele2() {
		return allele2;
	}
	/** 本allel所在的染色体坐标 */
	public int getPosition() {
		return getStartAbs();
	}
	public String getOther() {
		return other;
	}
	
	public void setRef(Allele alleleRef) {
		this.ref = alleleRef.getRefBase();
		this.alt = alleleRef.getAltBase();
		this.setChrID(alleleRef.getRefID());
		this.setStartAbs(alleleRef.getStartAbs());
		this.setEndAbs(alleleRef.getEndAbs());
		this.setCis5to3(alleleRef.isCis5to3());
		this.marker = alleleRef.marker;
		this.isRefMajor = alleleRef.isRefMajor();
		
		if (!allele1.equals("0") && !ref.equalsIgnoreCase(allele1) && !alt.equalsIgnoreCase(allele1)
				|| !allele2.equals("0") && !ref.equalsIgnoreCase(allele2) && !alt.equalsIgnoreCase(allele2)
				) {
			throw new ExceptionNBCPlink("Error! allele ref " + alleleRef.toString() + " is not correspond with " + allele1 + " " + allele2);
		}
	}
	
	public String toString() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
		lsResult.add(marker);
		lsResult.add(other);
		lsResult.add(getStartAbs()+"");
		lsResult.add(ref);
		lsResult.add(alt);
		if (isRefMajor != null) {
			String isRefMajorStr = isRefMajor ? "1" : "-1";
			lsResult.add(isRefMajorStr);
		}
		return ArrayOperate.cmbString(lsResult, "\t");
	}
	public String toStringSimple() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
//		lsResult.add(marker);
//		lsResult.add(other);
		lsResult.add(getStartAbs()+"");
//		lsResult.add(ref);
//		lsResult.add(alt);
//		if (isRefMajor != null) {
//			String isRefMajorStr = isRefMajor ? "1" : "-1";
//			lsResult.add(isRefMajorStr);
//		}
		return ArrayOperate.cmbString(lsResult, "\t");
	}
	/**
	 * 根据碱基频率来获取值
	 * 譬如高频位点为A，低频位点为T
	 * 则 AA = 1
	 * AT = 0
	 * TT = -1
	 * @return
	 */
	public int getFrq() {
		boolean isAllele1SameToRef = StringOperate.isEqual(allele1, ref);
		boolean isAllele2SameToRef = StringOperate.isEqual(allele2, ref);
		
		if (StringOperate.isEqual(allele1, "0") || StringOperate.isEqual(allele2, "0")) {
//			return isRefMajor ? 1 : -1;
			return -9;
		}
		
		int result = 0;
		if (isAllele1SameToRef && isAllele2SameToRef) {
			result = 1;
		} else if (isAllele1SameToRef || isAllele2SameToRef) {
			result = 0;
		} else {
			result = -1;
		}
		result = isRefMajor ? result : -result;
		return result;
		
	}
	public String toStringAlleleGwas() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
		lsResult.add(marker);
		lsResult.add(getStartAbs()+"");
		lsResult.add(ref);
		if (!StringOperate.isRealNull(alt)) {
			lsResult.add(alt);
		}
		if (!StringOperate.isRealNull(allele1)) {
			lsResult.add(allele1);
			lsResult.add(allele2);
		}
		lsResult.add(getFrq()+"");
		return ArrayOperate.cmbString(lsResult, "\t");
	}
}